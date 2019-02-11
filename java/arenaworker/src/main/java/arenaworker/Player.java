package arenaworker;

import java.lang.reflect.InvocationTargetException;

import org.json.JSONObject;

import arenaworker.abilities.Ability;
import arenaworker.abilities.Blasters;
import arenaworker.abilityobjects.Projectile;
import arenaworker.lib.Vector2;

public class Player extends Obj {
    Client client;
    boolean isEngineOnLeft = false;
    boolean isEngineOnRight = false;
    boolean isEngineOnUp = false;
    boolean isEngineOnDown = false;
    Vector2 mousePosition = new Vector2();
    Ability[] abilities = new Ability[4];
    double shield = 100;
    double health = 100;
    
    public Player(
            Client client,
            Game game,
            String[] abilityTypes
        ) {
        super(game, 0, 0, 25, 0);

        this.client = client;
        
        position = game.map.GetEmptyPos(200, -game.map.size/2, -game.map.size/2, game.map.size/2, game.map.size/2, 500);
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
        for (int i = 0; i < 4; i++) {
            abilities[i].Destroy();
        }
    }




    // when player is created - send to all clients
    @Override
    public void SendInitialToAll() {
        for (Client c : game.clients) {
            if (c.id == this.client.id) {
                JSONObject json = InitialData();
                json.put("t", "playerInitial");
                c.SendJson(json);
            } else {
                JSONObject json = InitialData();
                json.put("t", "shipInitial");
                c.SendJson(json);
            }
        }

        JSONObject replaly = InitialData();
        replaly.put("t", "shipInitial");
        this.game.AddJsonToReplay(replaly);
    }


    @Override
    public void SendInitialToClient(Client client) {
        super.SendInitialToClient(client);

        for (Ability a : abilities) {
            for (Base obj : a.abilityObjects) {
                obj.SendInitialToClient(client);
            }
        }
    }



    @Override
    public JSONObject InitialData() {
        JSONObject json = super.InitialData();
        json.put("name", client.name);
        return json;
    }


    @Override
    public JSONObject UpdateData() {
        JSONObject json = super.UpdateData();
        json.put("health", health);
        json.put("shield", shield);
        return json;
    }


    public void TakeDamage(double damage) {
        if (!game.isStarted) return;

        if (damage > 0) {
            double shieldToRemove = Math.min(shield, damage);
            shield -= shieldToRemove;
            damage -= shieldToRemove;
            health = Math.max(0, health - damage);
            needsUpdate = true;
            if (health <= 0) {
                Destroy();
            }
        }
    }


    public void ProjectileHit(Base projectile) {
        if (projectile instanceof Projectile) {
            Projectile p = (Projectile)projectile;
            TakeDamage(p.damage);
        }
    }


    @Override
    public void SetPosition(double x, double y) {
        super.SetPosition(x, y);
        if (position.x != x || position.y != y) {
            for (int i = 0; i < 4; i++) {
                abilities[i].PlayerPositionChanged();
            }
        }
    }
}