package arenaworker;

import org.json.JSONObject;

import arenaworker.lib.Physics;
import arenaworker.lib.Vector2;
import java.util.Date;

public class Player extends ObjCircle {
    Client client;
    boolean isEngineOnLeft = false;
    boolean isEngineOnRight = false;
    boolean isEngineOnUp = false;
    boolean isEngineOnDown = false;
    Vector2 mousePos = new Vector2();
    

    public Player(Client client, Game game) {
        super(game);

        this.client = client;
        
        radius = 25;
        position = game.map.GetEmptyPos(200, -game.map.size/2, -game.map.size/2, game.map.size/2, game.map.size/2);
        mass = 1;

        game.map.grid.insert(this);

        SendInitialToAll("bogus");
    }


    public void KeyUp(String key) {
        switch (key) {
            case "up":
                isEngineOnUp = false;
                break;
            case "down":
                isEngineOnDown = false;
                break;
            case "left":
                isEngineOnLeft = false;
                break;
            case "right":
                isEngineOnRight = false;
                break;
        }
    }


    public void KeyDown(String key) {
        switch (key) {
            case "up":
                isEngineOnUp = true;
                break;
            case "down":
                isEngineOnDown = true;
                break;
            case "left":
                isEngineOnLeft = true;
                break;
            case "right":
                isEngineOnRight = true;
                break;
        }
    }


    @Override
    public void Tick() {
        Vector2 engineForce = new Vector2();

        if (isEngineOnDown) engineForce.y += 1;
        if (isEngineOnLeft) engineForce.x -= 1;
        if (isEngineOnRight) engineForce.x += 1;
        if (isEngineOnUp) engineForce.y -= 1;

        engineForce.normalize();
        
        forces = engineForce.scale(game.settings.shipEngineSpeed);

        this.rotation = Physics.slowlyRotateToward(this.position, this.rotation, this.mousePos, 20);

        super.Tick();

        SendUpdate("shipUpdate");
    }


    public void Destroy() {
        game.players.remove(this);

        JSONObject json = new JSONObject();
        json.put("t", "shipDestroy");
        json.put("id", id);
        
        for (Client c : game.clients) {
            c.SendJson(json.toString());
        }
    }




    // when player is created - send to all clients
    @Override
    public void SendInitialToAll(String type) {
        JSONObject json = InitialData();
        
        for (Client c : game.clients) {
            if (c == this.client) {
                json.put("t", "playerInitial");
            } else {
                json.put("t", "shipInitial");
            }
            
            c.SendJson(json.toString());
        }
    }



    @Override
    public JSONObject InitialData() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("x", position.x);
        json.put("y", position.y);
        return json;
    }

    
}