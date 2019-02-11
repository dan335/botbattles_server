package arenaworker;

import java.util.UUID;

import org.json.JSONObject;

import arenaworker.lib.Vector2;
import java.util.Date;

public class Obj {
    public final String id = UUID.randomUUID().toString().substring(0, 8);
    final int hashcode = id.hashCode();
    public Vector2 position = new Vector2();
    public double rotation = 0;
    public Vector2 velocity = new Vector2();
    public Vector2 forces = new Vector2(); // forces to be applied this tick
    public double restitution = 0.5;
    public double mass = 1;

    public String initialUpdateName = "bogus";  // for SendInitialToClient() - override this
    public String updateName = "bogus";         // for SendUpdate() - override this
    public String destroyUpdateName = "bogus";
    
    public String[] grids = new String[0];
    public Game game;
    public boolean needsUpdate = false;

    public Obj (Game game, double x, double y) {
        this.game = game;
        this.position.x = x;
        this.position.y = y;
    }


    public void Collision(Obj obj, double magnitude, Vector2 force) {}



    public void Tick() {
        final Vector2 priorForces = new Vector2(game.settings.drag * velocity.x, game.settings.drag * velocity.y);
        final Vector2 acceleration = new Vector2(priorForces.x + forces.x, priorForces.y + forces.y);

        if (acceleration.x != 0 || acceleration.y != 0 || velocity.x != 0 || velocity.y != 0) {
            velocity.x = velocity.x + acceleration.x;
            velocity.y = velocity.y + acceleration.y;

            SetPosition(position.add(velocity.scale(game.deltaTime)));
        }

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