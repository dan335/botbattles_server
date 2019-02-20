package arenaworker;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
    long countdownStarted;
    public boolean isStarted = false;
    public boolean isEnded = false;
    public double deltaTime = 0;
    public boolean isInBulletTime = false;
    public long bulletTimeStart;
    public long bulletTimeDuration = 1000L;
    public Grid grid;
    public JSONArray replayJson = new JSONArray();
    boolean hasReplayBeenSaved = false;
    public Set<PlayerInfo> playerInfo = ConcurrentHashMap.newKeySet();  // info on all players who join game even if they leave


    public Game(Settings settings) {
        this.settings = settings;
        grid = new Grid(2500, settings.gridDivisions);
        map = new Map(settings.defaultMap, this);
        gameCreatedTime = Calendar.getInstance().getTimeInMillis();
        thread = new Thread(this, "game_" + id);
        thread.start();
    }


    void SaveGameToDb() {
        // add game to db
        MongoCollection<Document> games = App.database.getCollection("games");
        MongoCollection<Document> users = App.database.getCollection("users");

        Document doc = new Document("createdAt", (double)gameCreatedTime)
            .append("_id", new ObjectId(id))
            .append("startedAt", (double)gameStartTime)
            .append("endedAt", new Date())
            .append("length", (double)(tickStartTime - gameCreatedTime));

        List<Document> playerInfoDocs = new ArrayList<Document>();

        for (PlayerInfo info : playerInfo) {
            Document playerDoc = new Document("name", info.name)
                .append("userId", info.userId)
                .append("isWinner", info.isWinner)
                .append("kills", info.kills)
                .append("damage", info.damageDealt);

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
            ex.printStackTrace();
        }
        
    }


    void SaveReplay() {
        if (hasReplayBeenSaved) return;

        MongoCollection<Document> collection = App.database.getCollection("replays");

        Document document = new Document("createdAt", new Date())
            .append("json", replayJson.toString())
            .append("gameId", id);

        collection.insertOne(document);

        hasReplayBeenSaved = true;
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

        SaveGameToDb();
    }


    public void JoinGame(
        Session session,
        String name,
        String userId,
        String[] abilityTypes
    ) {
        Client c = Clients.GetClient(session);
        if (c != null) {
            // not sure why this would happen
            System.out.println("Error: Session trying to join game twice.");
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
                StartCountdown();
            }

            PlayerInfo info = new PlayerInfo(player.id, name, userId, abilityTypes);
            playerInfo.add(info);
            player.playerInfo = info;
        } else {
            JSONObject msg = new JSONObject();
            msg.put("t", "spectatorJoined");
            msg.put("name", client.name);
            SendJsonToClients(msg);
        }
    }


    void StartCountdown() {
        countdownStarted = tickStartTime;
        JSONObject json = new JSONObject();
        json.put("t", "countdownStarted");
        json.put("startTime", (double)tickStartTime + settings.gameWaitToStartTimeMs);
        SendJsonToClients(json);
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
        

        // destroy all bomb dropper bombs when game starts
        for (Player p : players) {
            for (int i = 0; i < p.game.settings.numAbilities; i++) {
                if (p.abilities[i] instanceof BombDropper) {
                    for (Base b : p.abilities[i].abilityObjects) {
                        b.Destroy();
                    }
                } else if (p.abilities[i] instanceof FreezeTrap) {
                    for (Base b : p.abilities[i].abilityObjects) {
                        b.Destroy();
                    }
                } else if (p.abilities[i] instanceof Turret) {
                    for (TurretObject t : ((Turret)p.abilities[i]).turrets) {
                        t.Destroy();
                    }
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

    public void SendJsonToClients(JSONArray json) {
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


    double totalTickTime = 0;
    int numTicks = 0;
    double serverTickTime = 0;
    public void run() {
        while (isRunning) {
            tickStartTime = Calendar.getInstance().getTimeInMillis();

            deltaTime = tickStartTime - tickEndTime;// / settings.updateIntervalMs;

            if (isInBulletTime) {
                deltaTime = deltaTime * 0.5;

                if (bulletTimeStart + bulletTimeDuration < tickStartTime) {
                    isInBulletTime = false;
                }
            }

            if (!isStarted && players.size() >= 2) {
                if (tickStartTime - countdownStarted > settings.gameWaitToStartTimeMs) {
                    StartGame();
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
                    ex.printStackTrace();
                }
            } else {
                System.out.println("Game is taking longer than updateIntervalMs");
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
                if (o.id != p.id) {
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
                if (other.id != obstacle.id) {
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
                if (other.id != box.id) {
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
                if (p.abilities[i] != null) {   // it is sometimes null for some reason
                    for (Base ao : p.abilities[i].abilityObjects) {
                        Set<Base> objs = grid.retrieve(ao.position, ao.radius);
                        for (Base other : objs) {
                            if (other.id != ao.id) {
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