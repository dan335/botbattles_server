package arenaworker;

import java.util.UUID;

import org.json.JSONObject;

import arenaworker.lib.Vector2;
import java.util.Date;

public class Obj {
    final String id = UUID.randomUUID().toString().substring(0, 8);
    final int hashcode = id.hashCode();
    public Vector2 position = new Vector2();
    public double rotation = 0;
    public Vector2 velocity = new Vector2();
    public Vector2 forces = new Vector2(); // forces to be applied this tick
    public double restitution = 0.5;
    public double mass = 1;
    
    public String[] grids = new String[0];
    public Game game;
    public boolean needsUpdate = false;

    public Obj (Game game) {
        this.game = game;
    }


    public void Collision(Obj obj, double magnitude, Vector2 force) {}



    public void Tick() {
        final Vector2 priorForces = new Vector2(game.settings.drag * velocity.x, game.settings.drag * velocity.y);
        final Vector2 acceleration = new Vector2(priorForces.x + forces.x, priorForces.y + forces.y);

        if (acceleration.x == 0 && acceleration.y == 0 && velocity.x == 0 && velocity.y == 0) {
            return;
        }

        velocity.x = velocity.x + acceleration.x;
        velocity.y = velocity.y + acceleration.y;

        position.x += velocity.x * game.deltaTime;
        position.y += velocity.y * game.deltaTime;
        
        game.map.grid.update(this);

        needsUpdate = true;
    }


    public void Destroy(String type) {
        game.obstacles.remove(this);

        JSONObject json = new JSONObject();
        json.put("t", type);
        json.put("id", id);
        
        for (Client c : game.clients) {
            c.SendJson(json.toString());
        }
    }


    // called from Tick()
    public void SendUpdate(String type) {
        if (needsUpdate) {
            JSONObject json = UpdateData();
            json.put("t", type);

            for (Client c : game.clients) {
                c.SendJson(json.toString());
            }

            needsUpdate = false;
        }
    }


    public void SendInitialToAll(String type) {
        JSONObject json = InitialData();
        
        for (Client c : game.clients) {
            json.put("t", type);
            c.SendJson(json.toString());
        }
    }


    public void SendInitialToClient(Client client, String type) {
        JSONObject json = InitialData();
        json.put("t", type);
        client.SendJson(json.toString());
    }

    
    public JSONObject InitialData() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("x", position.x);
        json.put("y", position.y);
        json.put("rotation", rotation);
        return json;
    }


    public JSONObject UpdateData() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("x", position.x);
        json.put("y", position.y);
        json.put("rotation", rotation);
        json.put("time", new Date().getTime());
        return json;
    }
}