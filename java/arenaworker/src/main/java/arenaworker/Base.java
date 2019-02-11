package arenaworker;

import java.util.UUID;

import org.json.JSONObject;

import arenaworker.lib.Vector2;
import java.util.Date;

public class Base {
    public final String id = UUID.randomUUID().toString().substring(0, 8);
    final int hashcode = id.hashCode();
    public Vector2 position = new Vector2();
    public double rotation = 0;
    public double radius = 1;

    public String initialUpdateName = "bogus";  // for SendInitialToClient() - override this
    public String updateName = "bogus";         // for SendUpdate() - override this
    public String destroyUpdateName = "bogus";

    // if set to true next update will tell the client to teleport
    // instead of smooth to next position
    // used to telport player
    public boolean teleportToNextPosition = false;
    
    public String[] grids = new String[0];
    public Game game;
    public boolean needsUpdate = false;

    public Base (Game game, double x, double y, double radius) {
        this.game = game;
        this.position.x = x;
        this.position.y = y;
        this.radius = radius;
    }


    public void Collision(Obj obj, double magnitude, Vector2 force) {}



    public void Tick() {
        SendUpdate();
    }


    public void SetPosition(double x, double y) {
        if (position.x != x || position.y != y) {
            position.x = x;
            position.y = y;
            needsUpdate = true;
            game.grid.update(this);
        }
    }


    public void SetPosition(Vector2 pos) {
        SetPosition(pos.x, pos.y);
    }

    public void SetRadius(double radius) {
        this.radius = radius;
    }


    public void Destroy() {
        JSONObject json = new JSONObject();
        json.put("t", destroyUpdateName);
        json.put("id", id);
        
        game.grid.remove(this);
        
        game.SendJsonToClients(json);
    }


    // called from Tick()
    public void SendUpdate() {
        if (needsUpdate) {
            JSONObject json = UpdateData();
            json.put("t", updateName);

            if (teleportToNextPosition) {
                json.put("teleport", true);
            }

            game.SendJsonToClients(json);

            needsUpdate = false;
        }
    }


    public void SendInitialToAll() {
        JSONObject json = InitialData();
        json.put("t", initialUpdateName);
        game.SendJsonToClients(json);
    }


    public void SendInitialToClient(Client client) {
        JSONObject json = InitialData();
        json.put("t", initialUpdateName);
        client.SendJson(json);
    }

    
    public JSONObject InitialData() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("x", position.x);
        json.put("y", position.y);
        json.put("rotation", rotation);
        json.put("radius", radius);
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