package arenaworker;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.mongodb.client.MongoCollection;

import org.bson.Document;
import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONArray;
import org.json.JSONObject;

import arenaworker.abilityobjects.Projectile;
import arenaworker.lib.Grid;
import arenaworker.lib.Physics;

public class Game implements Runnable {
    public final String id = UUID.randomUUID().toString();
    public Settings settings;
    public boolean isRunning = true;
    private Thread thread;
    public long tickStartTime = Calendar.getInstance().getTimeInMillis();   // time in ms at start of loop
    private long tickEndTime;     // time in ms at end of loop
    public long gameCreatedTime;     // time in ms that the game was created
    public long gameStartTime;
    public Map map;
    public Set<Player> players = ConcurrentHashMap.newKeySet();     // people who are playing game
    public Set<Client> clients = ConcurrentHashMap.newKeySet();   // everyone, players + spectators
    public Set<Obstacle> obstacles = ConcurrentHashMap.newKeySet();
    public Set<Box> boxes = ConcurrentHashMap.newKeySet();
    long secondPlayerJoinTime;
    public boolean isStarted = false;
    public double deltaTime = 0;    // 1 if last frame completed in settings.updateIntervalMs
    public Grid grid;
    public JSONArray replayJson = new JSONArray();


    public Game(Settings settings) {
        this.settings = settings;
        grid = new Grid(2500, settings.gridDivisions);
        map = new Map(settings.defaultMap, this);
        gameCreatedTime = Calendar.getInstance().getTimeInMillis();
        thread = new Thread(this, "game_" + id);
        thread.start();
    }


    // called from GameManager
    public void Destroy() {
        isRunning = false;

        // add replay to db
        MongoCollection<Document> collection = App.database.getCollection("replays");

        Document document = new Document("createdAt", gameCreatedTime)
            .append("startedAt", gameStartTime)
            .append("endedAt", new Date())
            .append("length", (double)(tickStartTime - gameCreatedTime))
            .append("json", replayJson.toString())
            .append("gameId", id);

        collection.insertOne(document);
    }


    public void JoinGame(
        Session session,
        String name,
        String abilityType1,
        String abilityType2,
        String abilityType3,
        String abilityType4
    ) {
        Client client = new Client(session, this, name);
        clients.add(client);
        Clients.AddClient(client);

        // call before creating player
        map.SendInitial(client);
        GetObjsInitialData(client);

        // if game hasn't started create a player for them
        if (!isStarted) {
            Player player = new Player(
                client,
                this,
                new String[]{
                    abilityType1,
                    abilityType2,
                    abilityType3,
                    abilityType4
                }
                );
            players.add(player);
            client.AddPlayer(player);
            
            if (players.size() == 2) {
                secondPlayerJoinTime = tickStartTime;
            }
        } else {
            JSONObject msg = new JSONObject();
            msg.put("t", "spectatorJoined");
            msg.put("name", client.name);
            SendJsonToClients(msg);
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
    }


    // called after all players have joined
    void StartGame() {
        isStarted = true;
        gameStartTime = tickStartTime;

        JSONObject msg = new JSONObject();
        msg.put("t", "gameStarted");
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
        JSONObject r = new JSONObject();
        r.put("t", new Date());
        r.put("j", json);
        replayJson.put(r);
    }

    public void AddJsonToReplay(JSONArray json) {
        JSONObject r = new JSONObject();
        r.put("t", new Date());
        r.put("j", json);
        replayJson.put(r);
    }


    public void run() {
        while (isRunning) {
            tickStartTime = Calendar.getInstance().getTimeInMillis();

            deltaTime = tickStartTime - tickEndTime;// / settings.updateIntervalMs;

            if (!isStarted && players.size() >= 2) {
                if (tickStartTime - secondPlayerJoinTime > settings.gameWaitToStartTimeMs) {
                    StartGame();
                }
            }

            map.Tick();

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
            ProjectilePhysics();
            
            for (Client c : clients) {
                c.Tick();
            }

            tickEndTime = Calendar.getInstance().getTimeInMillis();
            long timeTilNext = settings.updateIntervalMs - (tickStartTime - tickEndTime);
            
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



    private void PlayerPhysics() {
        for (Player p : players) {
            Set<Base> objs = grid.retrieve(p.position, p.radius);
            for (Base o : objs) {
                if (o.id != p.id) {
                    if (o instanceof Obstacle || o instanceof Player) {
                        if (Physics.circleInCircle(p, o)) {
                            Physics.resolveCollision(p, (Obj)o);
                        }
                    } else if(o instanceof Box) {
                        if (Physics.circleInRectangle(p, (ObjRectangle)o)) {
                            Physics.resolveCollision(p, (ObjRectangle)o);
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
                    if (other instanceof Obstacle) {
                        if (Physics.circleInCircle(obstacle, other)) {
                            Physics.resolveCollision(obstacle, (Obj)other);
                        }
                    } else if(other instanceof Box) {
                        if (Physics.circleInRectangle(obstacle, (ObjRectangle)other)) {
                            obstacle.Destroy();
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
                    if (other instanceof Player) {
                        if (Physics.circleInRectangle(other, (ObjRectangle)box)) {
                            Physics.PositionalCorrection((Obj)other, (ObjRectangle)box);
                        }
                    } else if (other instanceof Obstacle) {
                        if (Physics.circleInRectangle((Obj)other, (ObjRectangle)box)) {
                            other.Destroy();
                        }  
                    }
                    // don't react to rectangles
                }
            }
        }
    }


    private void ProjectilePhysics() {
        for (Player p : players) {
            for (int i = 0; i < 4; i++) {
                for (Base ao : p.abilities[i].abilityObjects) {
                    Set<Base> objs = grid.retrieve(ao.position, ao.radius);
                    for (Base other : objs) {
                        if (other.id != ao.id) {
                            if (other instanceof Box) {
                                Box box = (Box) other;
                                if (Physics.circleInRectangle(ao.position, ao.radius, box.position, box.scale)) {
                                    ao.Destroy();
                                }
                            } else if (other instanceof Player) {
                                Player otherPlayer = (Player) other;
                                if (otherPlayer != p) {
                                    if (Physics.circleInCircle(other, ao)) {
                                        otherPlayer.ProjectileHit(ao);
                                        ao.Destroy();
                                    }
                                }
                            } else if (other instanceof Obstacle) {
                                if (Physics.circleInCircle(other, ao)) {
                                    Physics.resolveCollision((Obj)other, (Obj)ao);
                                    ao.Destroy();
                                } 
                            }
                        }
                    }
                }
            }
        }
    }
}