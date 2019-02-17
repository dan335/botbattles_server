package arenaworker;

import java.lang.reflect.InvocationTargetException;

import org.json.JSONObject;

import arenaworker.abilities.Ability;
import arenaworker.abilities.Blasters;
import arenaworker.abilityobjects.Projectile;
import arenaworker.lib.Collision;
import arenaworker.lib.Physics;
import arenaworker.lib.Vector2;

public class Player extends Obj {
    public Client client;
    boolean isEngineOnLeft = false;
    boolean isEngineOnRight = false;
    boolean isEngineOnUp = false;
    boolean isEngineOnDown = false;
    Vector2 mousePosition = new Vector2();
    public Ability[] abilities = new Ability[4];
    public double shield = 100;
    double health = 100;
    long lastTakenDamage = 0;
    boolean isHealing = true;
    public PlayerInfo playerInfo;
    public boolean isCharging = false;
    public boolean isStunned = false;
    public long stunEnd;
    public double shipSpeedMultiplier = 1;
    
    public Player(
            Client client,
            Game game,
            String[] abilityTypes,
            Vector2 pos
        ) {
        super(game, pos.x, pos.y, 25, 0, true);

        this.client = client;
        game.players.add(this);
        
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
                ability = (Ability) cls.getDeclaredConstructor(new Class[] {Player.class, int.class, String.class}).newInstance(this, i+1, abilityTypes[i]);
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException ex) {
                ex.printStackTrace();
                ability = new Blasters(this, i+1, "Blasters");
            }

            abilities[i] = ability;
        }

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
        if (!isCharging && !isStunned) {
            abilities[num].Start();

            for (int i = 0; i < 4; i++) {
                abilities[i].PlayerStartedAnAbility(abilities[num]);
            }
        }
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


    Vector2 engineForce = new Vector2();
    @Override
    public void Tick() {
        if (!isCharging && !isStunned) {
            engineForce.x = 0;
            engineForce.y = 0;

            if (isEngineOnDown) engineForce.y += 1;
            if (isEngineOnLeft) engineForce.x -= 1;
            if (isEngineOnRight) engineForce.x += 1;
            if (isEngineOnUp) engineForce.y -= 1;

            engineForce.normalize();
            engineForce.scale(game.settings.shipEngineSpeed);
            engineForce.scale(shipSpeedMultiplier);
            
            forces.add(engineForce);

            this.rotation = Math.atan2(
                mousePosition.y - position.y,
                mousePosition.x - position.x    
            );
        }

        if (isStunned) {
            if (game.tickStartTime >= stunEnd) {
                isStunned = false;
            }
        }

        super.Tick();

        for (int i = 0; i < abilities.length; i++) {
            abilities[i].Tick();
        }

        if (lastTakenDamage + game.settings.playerHealDelay < game.tickStartTime) {
            isHealing = true;
        }

        if (isHealing) {
            if (shield < 100) {
                double newValue = shield + Math.min(game.settings.playerHealPerInterval * game.deltaTime, 100 - shield);
                if (newValue <= 100) {
                    shield = newValue;
                    needsUpdate = true;
                }
            }
        }
    }


    public void Stun(long duration) {
        isStunned = true;
        stunEnd = game.tickStartTime + duration;

        for (int i = 0; i < abilities.length; i++) {
            if (abilities[i].isOn) {
                abilities[i].Stop();
            }
        }
    }


    public void Destroy() {
        for (int i = 0; i < 4; i++) {
            abilities[i].Destroy();
        }

        game.players.remove(this);
        super.Destroy();

        if (game.players.size() == 1) {
            game.DeclareWinner(game.players.iterator().next());
        }
    }




    // when player is created - send to all clients
    @Override
    public void SendInitialToAll() {
        for (Client c : game.clients) {
            JSONObject json = InitialData();
            if (c.id == this.client.id) {
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


    public void TakeDamage(double damage, double shieldDamageMultiplier) {
        if (!game.isStarted) return;
        if (damage <= 0) return;
        
        if (shield > 0) {
            shield -= Math.min(shield, damage * shieldDamageMultiplier);
        } else {
            health -= Math.min(health, damage);
        }

        needsUpdate = true;
        lastTakenDamage = game.tickStartTime;
        isHealing = false;

        for (int i = 0; i < 4; i++) {
            abilities[i].PlayerTookDamage();
        }

        if (health <= 0) {
            Destroy();
        }
    }


    public void ProjectileHit(Base projectile) {
        if (projectile instanceof Projectile) {
            Projectile p = (Projectile)projectile;
            TakeDamage(p.damage, p.shieldDamageMultiplier);
        }
    }


    @Override
    public void SetPosition(double x, double y) {
        if (position.x != x || position.y != y) {
            for (int i = 0; i < 4; i++) {
                abilities[i].PlayerPositionChanged();
            }
        }
        super.SetPosition(x, y);
    }


    @Override
    public void Contact(Base otherObject) {
        if (otherObject instanceof Obstacle) {
            Physics.resolveCollision(this, (Obj)otherObject);
        } else if (otherObject instanceof Player) {
            Collision response = Physics.resolveCollision(this, (Obj)otherObject);
            if (response != null) {
                for (int i = 0; i < abilities.length; i++) {
                    abilities[i].PlayerCollision(response);
                }
            }
        } else if (otherObject instanceof Box) {
            Physics.resolveCollision(this, (ObjRectangle)otherObject);
        }
    }
}