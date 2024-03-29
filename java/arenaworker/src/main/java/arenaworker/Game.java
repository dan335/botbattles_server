package arenaworker;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONArray;
import org.json.JSONObject;

import arenaworker.abilities.BombDropper;
import arenaworker.abilities.FreezeTrap;
import arenaworker.abilities.Resurrection;
import arenaworker.abilities.Turret;
import arenaworker.abilityobjects.TurretObject;
import arenaworker.lib.Grid;
import arenaworker.lib.Physics;
import arenaworker.lib.Vector2;

public class Game implements Runnable {
    public final String id = new ObjectId().toHexString();
    public Settings settings;
    public boolean isRunning = true;
    private Thread thread;
    public long tickStartTime = Calendar.getInstance().getTimeInMillis();   // time in ms at start of loop
    private long tickEndTime;     // time in ms at end of loop
    public long gameCreatedTime;     // time in ms that the game was created
    public long gameStartTime;
    public long gameEndTime;
    public Map map;
    public Set<Player> players = ConcurrentHashMap.newKeySet();     // people who are playing game
    public Set<Client> clients = ConcurrentHashMap.newKeySet();   // everyone, players + spectators
    public Set<Obstacle> obstacles = ConcurrentHashMap.newKeySet();
    public Set<Box> boxes = ConcurrentHashMap.newKeySet();
    public Set<Base> other = ConcurrentHashMap.newKeySet();
    Long countdownStarted = null;
    public boolean isStarted = false;
    public boolean isEnded = false;
    public double deltaTime = 0;
    public boolean isInBulletTime = false;
    public long bulletTimeStart;
    public long bulletTimeDuration = 4000L;
    public Grid grid;
    public JSONArray replayJson = new JSONArray();
    boolean hasReplayBeenSaved = false;
    public Set<PlayerInfo> playerInfo = ConcurrentHashMap.newKeySet();  // info on all players who join game even if they leave


    public Game(Settings settings) {
        this.settings = settings;
        grid = new Grid(2500, settings.gridDivisions);
        map = new Map(settings.maps.get(new Random().nextInt(settings.maps.size())), this);
        gameCreatedTime = Calendar.getInstance().getTimeInMillis();
        thread = new Thread(this, "game_" + id);
        thread.start();
    }


    double ComputeQuality() {
        double maxDamage = 0;

        for (PlayerInfo info : playerInfo) {
            if (maxDamage < info.damageDealt) {
                maxDamage = info.damageDealt;
            }
        }

        if (maxDamage < 350) return 0;

        double damageDiff = 0;

        for (PlayerInfo info : playerInfo) {
            damageDiff += maxDamage - info.damageDealt;
        }

        return maxDamage + (1000 - damageDiff) / (playerInfo.size() - 1) + playerInfo.size() * 400;
    }


    void SaveGameToDb() {
        // add game to db
        MongoCollection<Document> games = App.database.getCollection("games");
        MongoCollection<Document> users = App.database.getCollection("users");

        Document doc = new Document("createdAt", (double)gameCreatedTime)
            .append("_id", new ObjectId(id))
            .append("startedAt", (double)gameStartTime)
            .append("endedAt", new Date())
            .append("length", (double)(tickStartTime - gameCreatedTime))
            .append("replayId", null)
            .append("quality", ComputeQuality());

        List<Document> playerInfoDocs = new ArrayList<Document>();

        for (PlayerInfo info : playerInfo) {
            Document playerDoc = new Document("name", info.name)
                .append("userId", info.userId)
                .append("isWinner", info.isWinner)
                .append("kills", info.kills)
                .append("damage", info.damageDealt)
                .append("ratingChange", info.ratingChange);

            List<Document> playerInfoAbilities = new ArrayList<Document>();
            
            for (int i = 0; i < settings.numAbilities; i++) {
                Document abilityDoc = new Document("id", info.abilities[i].toString());
                playerInfoAbilities.add(abilityDoc);
            }

            playerDoc.append("abilities", playerInfoAbilities);

            playerInfoDocs.add(playerDoc);

            // save kills and damage
            if (info.userId != null) {
                Document updateDoc = new Document("kills", info.kills)
                    .append("damage", info.damageDealt)
                    .append("plays", 1);

                if (info.isWinner) {
                    updateDoc.append("wins", 1);
                }

                users.updateOne(eq("_id", new ObjectId(info.userId)), new Document("$inc", updateDoc));
            }
        }

        doc.append("players", playerInfoDocs);

        try {
            games.insertOne(doc);
        } catch (MongoWriteException ex) {
            System.out.println("Error saving game to db.");
            ex.printStackTrace();
        }
        
    }


    void SaveReplay() {
        if (hasReplayBeenSaved) return;

        MongoCollection<Document> replays = App.database.getCollection("replays");
        MongoCollection<Document> replaydata = App.database.getCollection("replaydatas");
        MongoCollection<Document> games = App.database.getCollection("games");

        ObjectId replayId = new ObjectId();

        // Document replayDoc = new Document("createdAt", new Date())
        //     .append("gameId", id)
        //     .append("_id", replayId);
        
        Document replayDataDoc = new Document("_id", replayId)
        .append("json", replayJson.toString())
        .append("createdAt", new Date());

        //replays.insertOne(replayDoc);
        replaydata.insertOne(replayDataDoc);

        games.updateOne(eq("_id", new ObjectId(id)), new Document("$set", new Document("replayId", replayId)));

        hasReplayBeenSaved = true;
    }


    double kFactor = 32;
    void SaveRating() {
        MongoCollection<Document> collection = App.database.getCollection("users");

        PlayerInfo winner = null;
        
        for (PlayerInfo player : playerInfo) {
            if (player.isWinner) {
                winner = player;
            }
        }

        if (winner == null) return;
        if (winner.userId == null) return;

        winner.ratingChange = 0;

        Double winnerRating = winner.userData.getDouble("rating");
        if (winnerRating == null) {
            winnerRating = settings.defaultRating;
        }

        for (PlayerInfo player : playerInfo) {
            if (player.userData != null) {
                if (player.userId != winner.userId) {
                    Double loserRating = player.userData.getDouble("rating");

                    if (loserRating == null) {
                        loserRating = settings.defaultRating;
                    }

                    // get expected
                    double expectedW = 1 / (1 + Math.pow(10, (loserRating - winnerRating) / 400));
                    double expectedL = 1 / (1 + Math.pow(10, (winnerRating - loserRating) / 400));

                    // score = 0=loss 0.5=draw 1=win
                    winner.ratingChange += kFactor * (1 - expectedW);
                    player.ratingChange = kFactor * (0 - expectedL);

                    // update db
                    Document document = new Document("rating", loserRating + player.ratingChange);
                    collection.updateOne(eq("_id", new ObjectId(player.userId)), new Document("$set", document));
                }
            }
        }

        // update db
        if (winner.ratingChange != 0) {
            Document document = new Document("rating", winnerRating + winner.ratingChange);
            collection.updateOne(eq("_id", new ObjectId(winner.userId)), new Document("$set", document));
        }
    }


    // called from GameManager
    public void Destroy() {
        isRunning = false;
        SaveReplay();
    }


    public void DeclareWinner(Player player) {
        if (isEnded) return;

        isEnded = true;
        gameEndTime = tickStartTime;

        player.playerInfo.isWinner = true;

        JSONObject msg = new JSONObject();
        msg.put("t", "winner");
        msg.put("name", player.client.name);
        SendJsonToClients(msg);

        SaveRating();
        SaveGameToDb();
    }


    public void JoinGame( Session session, String name, String userId, ArrayList<String> abilityTypes) {
        Client c = Clients.GetClient(session);
        if (c != null) {
            // not sure why this would happen
            System.out.println("Error: Session trying to join game twice.");
            return;
        }

        if (abilityTypes.size() != settings.numAbilities) {
            System.out.println("not enough abilities in joinGame");
            return;
        }

        if (!session.isOpen()) {
            return;
        }

        Client client = new Client(session, this, name, userId);
        clients.add(client);

        // call before creating player
        map.SendInitial(client);
        GetObjsInitialData(client);

        // if game hasn't started create a player for them
        if (!isStarted) {
            Vector2 pos = map.GetEmptyPos(30, -map.size/2, -map.size/2, map.size/2, map.size/2, 100);

            if (pos == null) return;

            Player player = new Player(
                client,
                this,
                abilityTypes,
                pos
                );
            client.AddPlayer(player);
            
            if (players.size() == 2) {
                countdownStarted = tickStartTime;
            }
        } else {
            JSONObject msg = new JSONObject();
            msg.put("t", "spectatorJoined");
            msg.put("name", client.name);
            SendJsonToClients(msg);

            JSONObject json = new JSONObject();
            json.put("t", "spectatorInitial");
            client.SendJson(json);
        }
    }


    // client just joined, send them stuff
    void GetObjsInitialData(Client client) {
        for (Player o : players) {
            o.SendInitialToClient(client);
        }

        for (Obstacle o : obstacles) {
            o.SendInitialToClient(client);
        }

        for (Box o : boxes) {
            o.SendInitialToClient(client);
        }

        for (Base o : other) {
            o.SendInitialToClient(client);
        }
    }


    // called after all players have joined
    void StartGame() {

        // add all players to playerInfo
        for (Player p : players) {
            String[] abilities = new String[settings.numAbilities];
            for (int i = 0; i < settings.numAbilities; i++) {
                abilities[i] = p.abilities.get(i).getClass().getSimpleName().toString();
            }

            PlayerInfo info = new PlayerInfo(p.id, p.client.name, p.client.userId, abilities);
            playerInfo.add(info);
            p.playerInfo = info;
        }

        // unstun players
        for (Player p : players) {
            if (p.isStunned) {
                p.StunEnd();
            }

            if (p.isFrozen) {
                p.FreezeEnd();
            }

            if (p.isSilenced) {
                p.SilenceEnd();
            }
        }

        for (Obstacle o : obstacles) {
            o.GameStarted();
        }        

        // destroy all bomb dropper bombs when game starts
        for (Player p : players) {
            for (int i = 0; i < p.game.settings.numAbilities; i++) {
                if (p.abilities.get(i) instanceof BombDropper) {
                    for (Base b : p.abilities.get(i).abilityObjects) {
                        b.Destroy();
                    }
                } else if (p.abilities.get(i) instanceof FreezeTrap) {
                    for (Base b : p.abilities.get(i).abilityObjects) {
                        b.Destroy();
                    }
                } else if (p.abilities.get(i) instanceof Turret) {
                    for (TurretObject t : ((Turret)p.abilities.get(i)).turrets) {
                        t.Destroy();
                    }
                } else if (p.abilities.get(i) instanceof Resurrection) {
                    p.abilities.get(i).Fire();
                }
            }
        }

        isStarted = true;
        gameStartTime = tickStartTime;

        JSONObject msg = new JSONObject();
        msg.put("t", "gameStarted");
        msg.put("time", (double)tickStartTime);
        SendJsonToClients(msg);
    }



    public void SendJsonToClients(JSONObject json) {
        for (Client c : clients) {
            c.SendJson(json);
        }
        AddJsonToReplay(json);
    }


    public void AddJsonToReplay(JSONObject json) {
        if (tickEndTime - gameCreatedTime < settings.maxReplayTime) {
            JSONObject r = new JSONObject();
            r.put("t", new Date().getTime());
            r.put("j", json);
            replayJson.put(r);
        }
    }

    public void AddJsonToReplay(JSONArray json) {
        JSONObject r = new JSONObject();
        r.put("t", new Date());
        r.put("j", json);
        replayJson.put(r);
    }


    public void StartBulletTime() {
        isInBulletTime = true;
        bulletTimeStart = tickStartTime;
    }

    public void EndBulletTime() {
        isInBulletTime = false;
        for (Player p : players) {
            for (int i = 0; i < settings.numAbilities; i++) {
                p.abilities.get(i).BulletTimeEnded();
            }
        }
    }


    double totalTickTime = 0;
    int numTicks = 0;
    double serverTickTime = 0;
    Double lastSentCountdown = null;
    public void run() {
        while (isRunning) {
            deltaTime = Calendar.getInstance().getTimeInMillis() - tickStartTime;

            tickStartTime = Calendar.getInstance().getTimeInMillis();

            if (isInBulletTime) {
                deltaTime = deltaTime * 0.5;

                if (bulletTimeStart + bulletTimeDuration < tickStartTime) {
                    EndBulletTime();
                }
            }

            if (!isStarted && players.size() >= 2) {
                if (tickStartTime - countdownStarted > settings.gameWaitToStartTimeMs) {
                    StartGame();
                } else {
                    double secLeft = Math.round((countdownStarted + settings.gameWaitToStartTimeMs - tickStartTime) / 1000);
                    if (lastSentCountdown == null) {
                        lastSentCountdown = secLeft;
                        JSONObject json = new JSONObject();
                        json.put("t", "text");
                        json.put("m", secLeft+1);
                        SendJsonToClients(json);
                    } else {
                        if (secLeft != lastSentCountdown) {
                            JSONObject json = new JSONObject();
                            json.put("t", "text");
                            json.put("m", secLeft+1);
                            SendJsonToClients(json);
                            lastSentCountdown = secLeft;
                        }
                    }
                }
            }

            map.Tick();

            for (Base o : other) {
                o.Tick();
            }
            for (Player p : players) {
                p.Tick();
            }
            for (Obstacle o : obstacles) {
                o.Tick();
            }
            for (Box b : boxes) {
                b.Tick();
            }

            PlayerPhysics();
            ObstaclePhysics();
            BoxPhysics();
            AbilityObjectPhysics();
            
            for (Client c : clients) {
                c.Tick();
            }

            tickEndTime = Calendar.getInstance().getTimeInMillis();
            long timeTilNext = settings.tickIntervalMs - (tickStartTime - tickEndTime);
            
            if (numTicks > 60 * 10) {
                serverTickTime = totalTickTime / numTicks;
                numTicks = 0;
                totalTickTime = 0;
                SendServerInfo();
            }

            totalTickTime += (double)(tickEndTime - tickStartTime);
            numTicks++;

            if (isEnded) {
                if (tickStartTime > gameEndTime + 1000L * 60L * 5L) {
                    SaveReplay();
                }
            }

            if (timeTilNext > 0) {
                try {
                    Thread.sleep(timeTilNext);
                } catch (InterruptedException ex) {
                    System.out.println("Error while sleeping.");
                    ex.printStackTrace();
                }
            } else {
                System.out.println("Game is taking longer than it should");
            }
        }
    }


    void SendServerInfo() {
        JSONObject json = new JSONObject();
        json.put("t", "serverTickStats");
        json.put("serverTickTime", serverTickTime);
        SendJsonToClients(json);
    }



    private void PlayerPhysics() {
        for (Player p : players) {
            Set<Base> objs = grid.retrieve(p.position, p.radius);
            for (Base o : objs) {
                if (o != p) {
                    if (o instanceof ObjRectangle) {
                        if (Physics.circleInRectangle(p, (ObjRectangle)o)) {
                            p.Contact(o);
                        }
                    } else {
                        if (Physics.circleInCircle(p, o)) {
                            p.Contact(o);
                        }
                    }
                }
            }
        }
    }

    private void ObstaclePhysics() {
        for (Obstacle obstacle : obstacles) {
            Set<Base> objs = grid.retrieve(obstacle.position, obstacle.radius);
            for (Base other : objs) {
                if (other != obstacle) {
                    if (other instanceof ObjRectangle) {
                        if (Physics.circleInRectangle(obstacle, (ObjRectangle)other)) {
                            obstacle.Contact(other);
                        }
                    } else {
                        if (Physics.circleInCircle(obstacle, other)) {
                            obstacle.Contact(other);
                        }
                    }
                }
            }
        }
    }


    private void BoxPhysics() {
        for (Box box : boxes) {
            Set<Base> objs = grid.retrieve(box.position, box.scale);
            for (Base other : objs) {
                if (other != box) {
                    if (other instanceof ObjRectangle) {
                        // don't react
                    } else if (other instanceof Obj) {
                        if (Physics.circleInRectangle(other, (ObjRectangle)box)) {
                            box.Contact(other);
                            Physics.PositionalCorrection((Obj)other, (ObjRectangle)box);
                        }
                    }
                }
            }
        }
    }


    private void AbilityObjectPhysics() {
        for (Player p : players) {
            for (int i = 0; i < settings.numAbilities; i++) {
                if (p.abilities.get(i) != null) {   // it is sometimes null for some reason
                    for (Base ao : p.abilities.get(i).abilityObjects) {
                        Set<Base> objs = grid.retrieve(ao.position, ao.radius);
                        for (Base other : objs) {
                            if (other != ao) {
                                if (other instanceof ObjRectangle) {
                                    if (Physics.circleInRectangle(ao, (ObjRectangle)other)) {
                                        ao.Contact(other);
                                    }
                                } else {
                                    if (Physics.circleInCircle(ao, other)) {
                                        ao.Contact(other);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}