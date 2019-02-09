package arenaworker;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import arenaworker.lib.Grid;
import arenaworker.lib.Physics;
import arenaworker.lib.Vector2;

public class Map {
    
    JSONObject json;
    double size;
    Game game;
    long lastUpdate;
    public Grid grid;
    Box[] walls = new Box[4];

    public Map(JSONObject json, Game game) {
        this.json = json;
        this.game = game;
        size = json.getDouble("startSize");
        lastUpdate = game.tickStartTime;
        grid = new Grid(size + game.settings.wallWidth*2, game.settings.gridDivisions);

        BuildWalls();

        JSONArray obs = json.getJSONArray("obstacles");
        for (int i = 0; i < obs.length(); i++) {
            JSONObject ob = obs.getJSONObject(i);

            if (ob.getString("shape") == "circle") {
                Obstacle o = new Obstacle(game);
                o.position = new Vector2(ob.getDouble("x"), ob.getDouble("y"));
                o.radius = ob.getDouble("radius");
                o.mass = ob.getDouble("mass");
                grid.insert(o);
            } else if (ob.getString("shape") == "rectangle") {
                Box b = new Box(game);
                b.position = new Vector2(ob.getDouble("x"), ob.getDouble("y"));
                b.scale = new Vector2(ob.getDouble("scaleX"), ob.getDouble("scaleY"));
                grid.insert(b);
            }
        }
    }


    // 0 : left
    // 1 : right
    // 2 : top
    // 3 : bottom
    public void BuildWalls() {
        walls[0] = new Box(game);
        walls[0].position = new Vector2(-size/2, 0);
        walls[0].scale = new Vector2(game.settings.wallWidth, size + game.settings.wallWidth);
        grid.insert(walls[0]);

        walls[1] = new Box(game);
        walls[1].position = new Vector2(size/2, 0);
        walls[1].scale = new Vector2(game.settings.wallWidth, size + game.settings.wallWidth);
        grid.insert(walls[1]);

        walls[2] = new Box(game);
        walls[2].position = new Vector2(0, -size/2);
        walls[2].scale = new Vector2(size + game.settings.wallWidth, game.settings.wallWidth);
        grid.insert(walls[2]);

        walls[3] = new Box(game);
        walls[3].position = new Vector2(0, size/2);
        walls[3].scale = new Vector2(size + game.settings.wallWidth, game.settings.wallWidth);
        grid.insert(walls[3]);
    }


    public void UpdateWalls() {
        walls[0].position.x = -size/2;
        walls[0].scale.y = size + game.settings.wallWidth;

        walls[1].position.x = size/2;
        walls[1].scale.y = size + game.settings.wallWidth;

        walls[2].position.y = -size/2;
        walls[2].scale.x = size + game.settings.wallWidth;

        walls[3].position.y = size/2;
        walls[3].scale.x = size + game.settings.wallWidth;

        for (int i = 0; i < 4; i++) {
            grid.update(walls[i]);
            walls[i].needsUpdate = true;
        }
    }


    public void Tick() {
        if (game.isStarted) {
            if (game.tickStartTime - lastUpdate >= game.settings.mapUpdateInterval) {
                if (size > game.settings.mapMinSize) {
                    size -= game.settings.mapShrinkPerInterval;
                    UpdateWalls();
                    UpdateClients();
                }
            }
        }
    }


    void UpdateClients() {
        JSONObject json = UpdateData();
        json.put("t", "mapUpdate");
        game.SendJsonToClients(json.toString());
    }


    // sent to client when they join game
    public void SendInitial(Client client) {
        JSONObject json = InitialData();
        json.put("t", "mapInitial");

        client.SendJson(json.toString());
    }


    public Vector2 GetEmptyPos(double r, double minX, double minY, double maxX, double maxY) {
        boolean foundSpot = false;
        Vector2 pos = null;
        int tries = 0;
        int maxTries = 500;

        while (!foundSpot && tries < maxTries) {
            foundSpot = true;
            
            pos = new Vector2(
                minX + (Math.random() * (maxX - minX)),
                minY + (Math.random() * (maxY - minY))
            );
            
            Set<Obj> objs = grid.retrieve(pos, r);
            for (Obj o : objs) {
                if (foundSpot) {
                    if (o instanceof ObjCircle) {
                        ObjCircle oc = (ObjCircle)o;
                        if (Physics.circleInCircle(oc.position.x, oc.position.y, oc.radius, pos.x, pos.y, r)) {
                            foundSpot = false;
                        }
                    } else if (o instanceof ObjRectangle) {
                        ObjRectangle or = (ObjRectangle)o;
                        if (Physics.circleInRectangle(new Vector2(pos.x, pos.y), r, or.position, or.scale)) {
                            foundSpot = false;
                        }
                    }
                    
                }
            }

            tries++;
        }
        
        return pos;
    }


    JSONObject UpdateData() {
        JSONObject json = new JSONObject();

        json.put("size", size);

        return json;
    }

    
    JSONObject InitialData() {
        JSONObject json = new JSONObject();

        json.put("size", size);

        return json;
    }
}