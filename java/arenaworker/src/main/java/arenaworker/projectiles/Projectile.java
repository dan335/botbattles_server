package arenaworker.projectiles;

import arenaworker.abilities.*;
import arenaworker.Client;
import arenaworker.ObjCircle;

import org.json.JSONObject;
import java.util.UUID;

import arenaworker.lib.Vector2;
import java.util.Date;


public class Projectile {

    public final String id = UUID.randomUUID().toString().substring(0, 8);
    final int hashcode = id.hashCode();
    public Ability ability;
    public Vector2 position = new Vector2();
    public double radius = 15;
    public double rotation = 0;
    public double speed = 1;
    public boolean needsUpdate = false;
    public String initialUpdateName = "bogus";  // for SendInitialToClient() - override this
    public String updateName = "bogus";         // for SendUpdate() - override this
    public String destroyUpdateName = "bogus";
    
    public Projectile(Ability ability) {
        this.ability = ability;
        ability.projectiles.add(this);
        rotation = ability.player.rotation;
        position = new Vector2(ability.player.position.x, ability.player.position.y);
    }


    public void Tick() {
        // move projectile
        position = position.add(new Vector2(
            Math.cos(rotation) * speed * ability.player.game.deltaTime,
            Math.sin(rotation) * speed * ability.player.game.deltaTime
        ));
        needsUpdate = true;
        
        SendUpdate();
    }


    public void SendInitialToAll() {
        JSONObject json = InitialData();
        
        for (Client c : ability.player.game.clients) {
            json.put("t", initialUpdateName);
            c.SendJson(json.toString());
        }
    }


    public void SendInitialToClient(Client client) {
        JSONObject json = InitialData();
        json.put("t", initialUpdateName); // must override this
        client.SendJson(json.toString());
    }


    // called from Tick()
    public void SendUpdate() {
        if (needsUpdate) {
            JSONObject json = UpdateData();
            json.put("t", updateName);

            for (Client c : ability.player.game.clients) {
                c.SendJson(json.toString());
            }

            needsUpdate = false;
        }
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


    public void Destroy() {
        ability.projectiles.remove(this);

        JSONObject json = new JSONObject();
        json.put("t", destroyUpdateName);
        json.put("id", id);
        
        for (Client c : ability.player.game.clients) {
            c.SendJson(json.toString());
        }
    }
}