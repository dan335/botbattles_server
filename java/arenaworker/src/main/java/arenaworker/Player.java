package arenaworker;

import org.json.JSONObject;

import arenaworker.abilities.*;
import arenaworker.lib.Physics;
import arenaworker.lib.Vector2;
import arenaworker.projectiles.Projectile;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

public class Player extends ObjCircle {
    Client client;
    boolean isEngineOnLeft = false;
    boolean isEngineOnRight = false;
    boolean isEngineOnUp = false;
    boolean isEngineOnDown = false;
    Vector2 mousePosition = new Vector2();
    Ability[] abilities = new Ability[4];
    
    public Player(
            Client client,
            Game game,
            String[] abilityTypes
        ) {
        super(game, 0, 0, 25);

        this.client = client;
        
        position = game.map.GetEmptyPos(200, -game.map.size/2, -game.map.size/2, game.map.size/2, game.map.size/2);
        mass = 1;
        initialUpdateName = "shipInitial";
        updateName = "shipUpdate";
        destroyUpdateName = "shipDestroy";

        for (int i = 0; i < 4; i++) {
            Class<?> cls;
            try {
                cls = Class.forName("arenaworker.abilities." + abilityTypes[i]);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
                cls = Blasters.class;
            }

            Ability ability;
            try {
                ability = (Ability) cls.getDeclaredConstructor(new Class[] {Player.class}).newInstance(this);
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException ex) {
                ex.printStackTrace();
                ability = new Blasters(this);
            }

            abilities[i] = ability;
        }

        game.grid.insert(this);

        SendInitialToAll();
    }


    public void SetMousePosition(double x, double y) {
        if (mousePosition.x != x || mousePosition.y != y) {
            mousePosition.x = x;
            mousePosition.y = y;
            needsUpdate = true;
        }
    }


    public void AbilityKeyDown(int num) {
        abilities[num].Start();
    }


    public void AbilityKeyUp(int num) {
        abilities[num].Stop();
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

        //this.rotation = Physics.slowlyRotateToward(this.position, this.rotation, this.mousePosition, 20);
        Vector2 mouse = mousePosition.subtract(position);
        this.rotation = Math.atan2(mouse.y, mouse.x);

        super.Tick();

        for (int i = 0; i < 4; i++) {
            abilities[i].Tick();
        }
    }


    public void Destroy() {
        game.players.remove(this);
        super.Destroy();
    }




    // when player is created - send to all clients
    @Override
    public void SendInitialToAll() {
        JSONObject json = InitialData();
        
        for (Client c : game.clients) {
            if (c == this.client) {
                json.put("t", "playerInitial");
            } else {
                json.put("t", "shipInitial");
            }
            
            c.SendJson(json);
        }

        JSONObject replaly = InitialData();
        replaly.put("t", "shipInitial");
        this.game.AddJsonToReplay(replaly);
    }


    @Override
    public void SendInitialToClient(Client client) {
        super.SendInitialToClient(client);

        for (Ability a : abilities) {
            for (Projectile p : a.projectiles) {
                p.SendInitialToClient(client);
            }
        }
    }



    @Override
    public JSONObject InitialData() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("x", position.x);
        json.put("y", position.y);
        json.put("name", client.name);
        return json;
    }


    public void ProjectileHit(Projectile projectile) {
        // TODO
    }
}