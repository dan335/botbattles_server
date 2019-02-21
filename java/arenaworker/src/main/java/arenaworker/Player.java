package arenaworker;

import java.lang.reflect.InvocationTargetException;

import org.json.JSONObject;

import arenaworker.abilities.Ability;
import arenaworker.abilities.Blasters;
import arenaworker.abilities.Resurrection;
import arenaworker.lib.Collision;
import arenaworker.lib.Physics;
import arenaworker.lib.Vector2;

public class Player extends Obj {
    public Client client;
    boolean isEngineOnLeft = false;
    boolean isEngineOnRight = false;
    boolean isEngineOnUp = false;
    boolean isEngineOnDown = false;
    public Vector2 mousePosition = new Vector2();
    public Ability[] abilities;
    public double shield;
    public double health;
    long lastTakenDamage = 0;
    boolean isHealing = true;
    public PlayerInfo playerInfo;
    public boolean isCharging = false;
    public boolean isStunned = false;
    public boolean isFrozen = false;
    public boolean isSilenced = false;
    public boolean isRaging = false;
    public long stunEnd;
    public long frozenEnd;
    public long silencedEnd;
    public long rageEnd;
    public boolean isInvis = false;
    public long invisEnd;
    public double shipSpeedMultiplier = 1;
    
    public Player(
            Client client,
            Game game,
            String[] abilityTypes,
            Vector2 pos
        ) {
        super(game, pos.x, pos.y, 25, 0, true);

        health = game.settings.maxHealth;
        shield = game.settings.maxShield;
        
        this.abilities = new Ability[game.settings.numAbilities];

        this.client = client;
        game.players.add(this);
        
        mass = 1;
        initialUpdateName = "shipInitial";
        updateName = "shipUpdate";
        destroyUpdateName = "shipDestroy";

        for (int i = 0; i < game.settings.numAbilities; i++) {
            Class<?> cls;
            try {
                cls = Class.forName("arenaworker.abilities." + abilityTypes[i]);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
                cls = Blasters.class;
            }

            Ability ability;
            try {
                ability = (Ability) cls.getDeclaredConstructor(new Class[] {Player.class, int.class}).newInstance(this, i);
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException ex) {
                ex.printStackTrace();
                ability = new Blasters(this, i);
            }

            abilities[i] = ability;
        }

        SendInitialToAll();

        for (int i = 0; i < game.settings.numAbilities; i++) {
            abilities[i].Init();
        }
    }


    public void SetMousePosition(double x, double y) {
        if (mousePosition.x != x || mousePosition.y != y) {
            mousePosition.x = x;
            mousePosition.y = y;
            needsUpdate = true;
        }
    }


    public void AbilityKeyDown(int num) {
        if (!isCharging && !isStunned && !isSilenced) {
            abilities[num].Start();

            for (int i = 0; i < game.settings.numAbilities; i++) {
                abilities[i].PlayerStartedAnAbility(abilities[num]);
            }

            if (isInvis) {
                LoseInvis();
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
            if (!isFrozen) {
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
            }
            
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

        if (isInvis) {
            if (game.tickStartTime >= invisEnd) {
                LoseInvis();
            }
        }

        if (isFrozen) {
            if (game.tickStartTime >= frozenEnd) {
                isFrozen = false;
            }
        }

        if (isSilenced) {
            if (game.tickStartTime >= silencedEnd) {
                isSilenced = false;
            }
        }

        if (isRaging) {
            if (game.tickStartTime >= rageEnd) {
                isRaging = false;
                Stun(2000L);
            }
        }

        super.Tick();

        for (int i = 0; i < game.settings.numAbilities; i++) {
            if (abilities[i] != null) { // not sure why it would be null but it keeps complaining
                abilities[i].Tick();
            }
        }

        if (lastTakenDamage + game.settings.playerHealDelay < game.tickStartTime) {
            isHealing = true;
        }

        if (isHealing) {
            if (shield < game.settings.maxShield) {
                double newValue = shield + Math.min(game.settings.playerHealPerInterval * game.deltaTime, game.settings.maxShield - shield);
                if (newValue <= game.settings.maxShield) {
                    shield = newValue;
                    needsUpdate = true;
                }
            }
        }
    }


    public void Rage(long duration) {
        isRaging = true;
        rageEnd = game.tickStartTime + duration;
    }


    public void Stun(long duration) {
        isStunned = true;
        stunEnd = game.tickStartTime + duration;

        for (int i = 0; i < game.settings.numAbilities; i++) {
            if (abilities[i].isOn) {
                abilities[i].Stop();
            }
        }
    }


    public void Silence(long duration) {
        isSilenced = true;
        silencedEnd = game.tickStartTime + duration;

        for (int i = 0; i < game.settings.numAbilities; i++) {
            if (abilities[i].isOn) {
                abilities[i].Stop();
            }
        }
    }


    public void Freeze(long duration) {
        isFrozen = true;
        frozenEnd = game.tickStartTime + duration;
    }


    public void GoInvis(long duration) {
        isInvis = true;
        invisEnd = game.tickStartTime + duration;

        for (int i = 0; i < game.settings.numAbilities; i++) {
            if (abilities[i].isOn) {
                abilities[i].Stop();
            }
        }

        JSONObject json = new JSONObject();
        json.put("t", "goInvisible");
        json.put("shipId", id);
        game.SendJsonToClients(json);
    }


    public void LoseInvis() {
        JSONObject json = new JSONObject();
        json.put("t", "goVisible");
        json.put("shipId", id);
        game.SendJsonToClients(json);
    }


    public void Destroy() {
        for (int i = 0; i < game.settings.numAbilities; i++) {
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


    public void TakeDamage(double damage, double shieldDamageMultiplier, Player otherPlayer) {
        if (!game.isStarted) return;
        if (damage <= 0) return;
        
        if (shield > 0) {
            shield -= Math.min(shield, damage * shieldDamageMultiplier);
            if (otherPlayer != null) {
                otherPlayer.AddDamageDealt(Math.min(shield, damage * shieldDamageMultiplier));
            }
        } else {
            health -= Math.min(health, damage);
            if (otherPlayer != null) {
                otherPlayer.AddDamageDealt(Math.min(health, damage));
            }
        }

        needsUpdate = true;
        lastTakenDamage = game.tickStartTime;
        isHealing = false;

        for (int i = 0; i < game.settings.numAbilities; i++) {
            abilities[i].PlayerTookDamage();
        }

        if (isInvis) {
            LoseInvis();
        }

        if (health <= 0) {
            Ability resurrection = null;

            for (int i = 0; i < game.settings.numAbilities; i++) {
                if (abilities[i] instanceof Resurrection) {
                    if (abilities[i].IsReady()) {
                        resurrection = abilities[i];
                    }
                }
            }

            if (resurrection != null) {
                health = game.settings.maxHealth;
                needsUpdate = true;
                resurrection.Fire();
            } else {
                if (otherPlayer != null) {
                    otherPlayer.AddKill();
                }
                Destroy();
            }
        }
    }


    public void AddKill() {
        playerInfo.kills++;
    }

    public void AddDamageDealt(double damage) {
        playerInfo.damageDealt += damage;
    }



    @Override
    public void SetPosition(double x, double y) {
        if (position.x != x || position.y != y) {
            for (int i = 0; i < game.settings.numAbilities; i++) {
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
                for (int i = 0; i < game.settings.numAbilities; i++) {
                    abilities[i].PlayerCollision(response);
                }
            }
        } else if (otherObject instanceof Box) {
            Physics.resolveCollision(this, (ObjRectangle)otherObject);
        }
    }
}