package arenaworker;

import java.util.Calendar;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.Session;

import arenaworker.lib.Physics;
import arenaworker.projectiles.Projectile;

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
    


    public Game(Settings settings) {
        this.settings = settings;
        map = new Map(settings.defaultMap, this);
        gameCreatedTime = Calendar.getInstance().getTimeInMillis();
        thread = new Thread(this, "game_" + id);
        thread.start();
    }


    // called from GameManager
    public void Destroy() {
        isRunning = false;
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
        }
    }


    // client just joined, send them stuff
    void GetObjsInitialData(Client client) {
        for (Player o : players) {
            o.SendInitialToClient(client, "shipInitial");
        }

        for (Obstacle o : obstacles) {
            o.SendInitialToClient(client, "obstacleInitial");
        }

        for (Box o : boxes) {
            o.SendInitialToClient(client, "boxInitial");
        }
    }


    // called after all players have joined
    void StartGame() {
        isStarted = true;
        gameStartTime = tickStartTime;
    }



    public void SendJsonToClients(String json) {
        for (Client c : clients) {
            if (c.session.isOpen()) {
                try {
                    c.session.getRemote().sendStringByFuture(json);
                }
                catch (Throwable e)
                {
                    e.printStackTrace();
                }
            }
        }
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
            Set<Obj> objs = map.grid.retrieve(p.position, p.radius);
            for (Obj o : objs) {
                if (o.id != p.id) {
                    if (o instanceof ObjCircle) {
                        if (Physics.circleInCircle((ObjCircle)p, (ObjCircle)o)) {
                            Physics.resolveCollision((ObjCircle)p, (ObjCircle)o);
                        }
                    } else if(o instanceof ObjRectangle) {
                        if (Physics.circleInRectangle((ObjCircle)p, (ObjRectangle)o)) {
                            Physics.resolveCollision((ObjCircle)p, (ObjRectangle)o);
                        }
                    }
                }
            }
        }
    }

    private void ObstaclePhysics() {
        for (Obstacle p : obstacles) {
            Set<Obj> objs = map.grid.retrieve(p.position, p.radius);
            for (Obj o : objs) {
                if (o.id != p.id) {
                    if (o instanceof ObjCircle) {
                        if (Physics.circleInCircle((ObjCircle)p, (ObjCircle)o)) {
                            Physics.resolveCollision((ObjCircle)p, (ObjCircle)o);
                        }
                    } else if(o instanceof ObjRectangle) {
                        o.Destroy("obstacleDestroy");
                    }
                }
            }
        }
    }


    private void BoxPhysics() {
        for (Box b : boxes) {
            Set<Obj> objs = map.grid.retrieve(b.position, b.scale);
            for (Obj o : objs) {
                if (o.id != b.id) {
                    if (o instanceof Player) {
                        if (Physics.circleInRectangle((ObjCircle)o, (ObjRectangle)b)) {
                            Physics.PositionalCorrection((ObjCircle)o, (ObjRectangle)b);
                        }
                    } else if (o instanceof Obstacle) {
                        if (Physics.circleInRectangle((ObjCircle)o, (ObjRectangle)b)) {
                            o.Destroy("obstacleDestroy");
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
                for (Projectile pr : p.abilities[i].projectiles) {
                    Set<Obj> objs = map.grid.retrieve(pr.position, pr.radius);
                    for (Obj o : objs) {
                        if (o.id != pr.id) {
                            if (o instanceof Box) {
                                Box b = (Box) o;
                                if (Physics.circleInRectangle(pr.position, pr.radius, b.position, b.scale)) {
                                    pr.Destroy();
                                }
                            } else if (o instanceof Player) {
                                Player pl = (Player) o;
                                if (pl != p) {
                                    pl.ProjectileHit(pr);
                                    pr.Destroy();
                                }
                            } else if (o instanceof Obstacle) {
                                Obstacle ob = (Obstacle) o;
                                if (Physics.circleInCircle(pr.position.x, pr.position.y, pr.radius, ob.position.x, ob.position.y, ob.radius)) {
                                    pr.Destroy();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}