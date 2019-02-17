package arenaworker;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import arenaworker.lib.Physics;
import arenaworker.lib.Vector2;

public class Map {
    
    JSONObject json;
    public double size;
    Game game;
    long lastUpdate;
    
    Box[] walls = new Box[4];

    public Map(JSONObject json, Game game) {
        this.json = json;
        this.game = game;
        size = json.getDouble("startSize");
        lastUpdate = game.tickStartTime;

        // add mapInitial to game replay
        JSONObject j = InitialData();
        j.put("t", "mapInitial");
        game.AddJsonToReplay(j);

        BuildWalls();

        JSONArray obs = json.getJSONArray("obstacles");
        for (int i = 0; i < obs.length(); i++) {
            JSONObject ob = obs.getJSONObject(i);

            if (ob.getString("shape") == "circle") {
                Obstacle o = new Obstacle(game, ob.getDouble("x"), ob.getDouble("y"), ob.getDouble("radius"));
                o.mass = ob.getDouble("mass");
            }
        }
    }


    // 0 : left
    // 1 : right
    // 2 : top
    // 3 : bottom
    public void BuildWalls() {
        walls[0] = new Box(game, -size/2, 0, game.settings.wallWidth, size + game.settings.wallWidth);
        walls[1] = new Box(game, size/2, 0, game.settings.wallWidth, size + game.settings.wallWidth);
        walls[2] = new Box(game, 0, -size/2, size + game.settings.wallWidth, game.settings.wallWidth);
        walls[3] = new Box(game, 0, size/2, size + game.settings.wallWidth, game.settings.wallWidth);
    }


    public void UpdateWalls() {
        walls[0].SetPosition(-size/2, walls[0].position.y);
        walls[0].setScale(walls[0].scale.x, size + game.settings.wallWidth);

        walls[1].SetPosition(size/2, walls[1].position.y);
        walls[1].setScale(walls[1].scale.x, size + game.settings.wallWidth);

        walls[2].SetPosition(walls[2].position.x, -size/2);
        walls[2].setScale(size + game.settings.wallWidth, walls[2].scale.y);

        walls[3].SetPosition(walls[3].position.x, size/2);
        walls[3].setScale(size + game.settings.wallWidth, walls[3].scale.y);
    }


    public void Tick() {
        if (game.isStarted && !game.isEnded) {
            if (game.tickStartTime - lastUpdate >= game.settings.mapUpdateInterval) {
                if (size > game.settings.mapMinSize) {
                    size -= game.settings.mapShrinkPerInterval * game.deltaTime;
                    UpdateWalls();
                    UpdateClients();
                }
            }
        }
    }


    void UpdateClients() {
        JSONObject json = UpdateData();
        json.put("t", "mapUpdate");
        game.SendJsonToClients(json);
    }


    // sent to client when they join game
    public void SendInitial(Client client) {
        JSONObject json = InitialData();
        json.put("t", "mapInitial");
        client.SendJson(json);
    }


    public Vector2 GetEmptyPos(double r, double minX, double minY, double maxX, double maxY, int maxTries) {
        boolean foundSpot = false;
        Vector2 pos = null;
        int tries = 0;

        while (!foundSpot && tries < maxTries) {
            foundSpot = true;
            
            Vector2 testPos = new Vector2(
                minX + (Math.random() * (maxX - minX)),
                minY + (Math.random() * (maxY - minY))
            );
            
            Set<Base> objs = game.grid.retrieve(testPos, r);
            for (Base o : objs) {
                if (foundSpot) {
                    if (o instanceof ObjRectangle) {
                        ObjRectangle or = (ObjRectangle)o;
                        if (Physics.circleInRectangle(new Vector2(testPos.x, testPos.y), r, or.position, or.scale)) {
                            foundSpot = false;
                        }
                    } else {
                        if (Physics.circleInCircle(o.position.x, o.position.y, o.radius, testPos.x, testPos.y, r)) {
                            foundSpot = false;
                        }
                    }
                    
                }
            }

            if (foundSpot) {
                pos = testPos;
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