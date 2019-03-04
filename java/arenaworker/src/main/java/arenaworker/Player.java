package arenaworker;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONObject;

import arenaworker.abilities.Ability;
import arenaworker.abilities.Blasters;
import arenaworker.abilities.Resurrection;
import arenaworker.abilityobjects.TurretObject;
import arenaworker.lib.Collision;
import arenaworker.lib.Physics;
import arenaworker.lib.Vector2;
import arenaworker.other.Explosion;

public class Player extends Obj {
    public Client client;
    boolean isEngineOnLeft = false;
    boolean isEngineOnRight = false;
    boolean isEngineOnUp = false;
    boolean isEngineOnDown = false;
    public Vector2 mousePosition = new Vector2();
    public ArrayList<Ability> abilities = new ArrayList<>();
    public boolean[] abilityKeysDown;
    public double shield;
    public double health;
    long lastTakenDamage = 0;
    boolean isHealing = false;
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
    public double shipEngineSpeed = 1;
    
    public Player(
            Client client,
            Game game,
            ArrayList<String> abilityTypes,
            Vector2 pos
        ) {
        super(game, pos.x, pos.y, 25, 0, true);

        shipEngineSpeed = game.settings.shipEngineSpeed;

        health = game.settings.maxHealth;
        shield = game.settings.maxShield;
        
        this.abilityKeysDown = new boolean[game.settings.numAbilities];

        this.client = client;
 
        mass = game.settings.playerDefaultMass;
        initialUpdateName = "shipInitial";
        updateName = "shipUpdate";
        destroyUpdateName = "shipDestroy";

        SetupAbilities(abilityTypes);

        SendInitialToAll();

        for (int i = 0; i < game.settings.numAbilities; i++) {
            abilities.get(i).Init();
            abilityKeysDown[i] = false;
        }

        game.players.add(this);
    }


    void SetupAbilities(ArrayList<String> abilityTypes) {
        // check for duplicates
        Set<String> lump = new HashSet<String>();

        for (int i = 0; i < abilityTypes.size(); i++) {

            if (lump.contains(abilityTypes.get(i))) {

                // find an ability to replace it
                boolean found = false;
                Iterator iter = game.settings.abilityNames.iterator();

                while (!found) {
                    String nextAbilityName = (String)iter.next();
                    if (!abilityTypes.contains(nextAbilityName)) {
                        abilityTypes.set(i, nextAbilityName);
                        found = true;
                    }
                }
            }

            lump.add(abilityTypes.get(i));
        }

        for (int i = 0; i < game.settings.numAbilities; i++) {
            Class<?> cls;
            try {
                cls = Class.forName("arenaworker.abilities." + abilityTypes.get(i));
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

            abilities.add(ability);
        }
    }

    private boolean SetContainsDuplicates(Set<String> values) {
        Set<String> lump = new HashSet<String>();

        for (String value : values) {
            if (lump.contains(value)) return true;
            lump.add(value);
        }

        return false;
    }


    public void SetMousePosition(double x, double y) {
        if (mousePosition.x != x || mousePosition.y != y) {
            mousePosition.x = x;
            mousePosition.y = y;
        }
    }


    public void AbilityKeyDown(int num) {
        abilityKeysDown[num] = true;

        if (!isCharging && !isStunned && !isSilenced) {
            abilities.get(num).Start();

            for (int i = 0; i < game.settings.numAbilities; i++) {
                abilities.get(i).PlayerStartedAnAbility(abilities.get(num));
            }

            if (isInvis) {
                LoseInvis();
            }
        }
    }


    // position where bullets should spawn from
    public double FirePositionX() {
        return this.position.x + Math.cos(this.rotation) * this.radius;
    }


    public double FirePositionY() {
        return this.position.y + Math.sin(this.rotation) * this.radius;
    }
    


    public void AbilityKeyUp(int num) {
        abilities.get(num).Stop();
        abilityKeysDown[num] = false;
    }


    // called after being stunned.  restart abilities if keys are still down
    public void RestartAbilities() {
        for (int i = 0; i < game.settings.numAbilities; i++) {
            if (abilityKeysDown[i]) {
                abilities.get(i).Start();
            }
        }
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
                engineForce.scale(shipEngineSpeed);
                engineForce.scale(shipSpeedMultiplier);
                
                forces.add(engineForce);
            }
            
            double rotation = Math.atan2(
                mousePosition.y - position.y,
                mousePosition.x - position.x    
            );

            if (this.rotation != rotation) {
                this.rotation = rotation;
                needsUpdate = true;
            }
        }

        if (isStunned) {
            if (game.tickStartTime >= stunEnd) {
                StunEnd();
            }
        }

        if (isInvis) {
            if (game.tickStartTime >= invisEnd) {
                LoseInvis();
            }
        }

        if (isFrozen) {
            if (game.tickStartTime >= frozenEnd) {
                FreezeEnd();
            }
        }

        if (isSilenced) {
            if (game.tickStartTime >= silencedEnd) {
                SilenceEnd();
            }
        }

        if (isRaging) {
            if (game.tickStartTime >= rageEnd) {
                RageEnd();
            }
        }

        super.Tick();

        for (int i = 0; i < game.settings.numAbilities; i++) {
            abilities.get(i).Tick();
        }

        if (shield < game.settings.maxShield && lastTakenDamage + game.settings.playerHealDelay < game.tickStartTime) {
            StartShieldRecharging();
        }

        if (isHealing) {
            if (shield < game.settings.maxShield) {
                shield = Math.min(shield + game.settings.playerHealPerInterval * game.deltaTime, game.settings.maxShield);
                needsUpdate = true;
            } else {
                StopShieldRecharging();
            }
        }
    }


    public void StartShieldRecharging() {
        if (!isHealing) {
            isHealing = true;

            JSONObject json = new JSONObject();
            json.put("t", "shieldRechargeStart");
            json.put("shipId", id);
            game.SendJsonToClients(json);
        }
    }


    public void StopShieldRecharging() {
        if (isHealing) {
            isHealing = false;

            JSONObject json = new JSONObject();
            json.put("t", "shieldRechargeEnd");
            json.put("shipId", id);
            game.SendJsonToClients(json);
        }
    }


    public void Rage(long duration) {
        isRaging = true;
        rageEnd = game.tickStartTime + duration;

        JSONObject json = new JSONObject();
        json.put("t", "rageStart");
        json.put("shipId", id);
        game.SendJsonToClients(json);
    }

    public void RageEnd() {
        isRaging = false;
        Stun(1500L);
    }


    public void Stun(long duration) {
        isStunned = true;
        stunEnd = game.tickStartTime + duration;

        for (int i = 0; i < game.settings.numAbilities; i++) {
            if (abilities.get(i).isOn) {
                abilities.get(i).Stop();
            }
        }

        JSONObject json = new JSONObject();
        json.put("t", "stunnedStart");
        json.put("shipId", id);
        game.SendJsonToClients(json);
    }

    public void StunEnd() {
        isStunned = false;
        RestartAbilities();

        JSONObject json = new JSONObject();
        json.put("t", "stunnedEnd");
        json.put("shipId", id);
        game.SendJsonToClients(json);
    }


    public void Silence(long duration) {
        isSilenced = true;
        silencedEnd = game.tickStartTime + duration;

        for (int i = 0; i < game.settings.numAbilities; i++) {
            if (abilities.get(i).isOn) {
                abilities.get(i).Stop();
            }
        }
    }

    public void SilenceEnd() {
        isSilenced = false;
        RestartAbilities();
    }


    public void Freeze(long duration) {
        isFrozen = true;
        frozenEnd = game.tickStartTime + duration;
    }

    public void FreezeEnd() {
        isFrozen = false;
    }


    public void GoInvis(long duration) {
        isInvis = true;
        invisEnd = game.tickStartTime + duration;

        for (int i = 0; i < game.settings.numAbilities; i++) {
            if (abilities.get(i).isOn) {
                abilities.get(i).Stop();
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
            abilities.get(i).Destroy();
        }

        game.players.remove(this);
        super.Destroy();

        if (game.players.size() == 1) {
            game.DeclareWinner(game.players.iterator().next());
        }

        client.player = null;

        new Explosion(game, position.x, position.y, radius * 4, 20, 1, "0xff4444", null);
    }


    // when player is created - send to all clients
    @Override
    public void SendInitialToAll() {
        for (Client c : game.clients) {
            JSONObject json = InitialData();
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
        json.put("engineDown", isEngineOnDown);
        json.put("engineUp", isEngineOnUp);
        json.put("engineLeft", isEngineOnLeft);
        json.put("engineRight", isEngineOnRight);
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
        if (isHealing) {
            StopShieldRecharging();
        }
        

        for (int i = 0; i < game.settings.numAbilities; i++) {
            abilities.get(i).PlayerTookDamage();
        }

        if (isInvis) {
            LoseInvis();
        }

        if (health <= 0) {
            Ability resurrection = null;

            for (int i = 0; i < game.settings.numAbilities; i++) {
                if (abilities.get(i) instanceof Resurrection) {
                    if (abilities.get(i).IsReady()) {
                        resurrection = abilities.get(i);
                    }
                }
            }

            if (resurrection != null) {
                health = game.settings.maxHealth;
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
                abilities.get(i).PlayerPositionChanged();
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
                    abilities.get(i).Collision(response);
                }
            }

        } else if (otherObject instanceof Box) {
            Physics.resolveCollision(this, (ObjRectangle)otherObject);
        
        } else if (otherObject instanceof TurretObject) {
            Collision response = Physics.resolveCollision(this, (Obj)otherObject);
            if (response != null) {
                for (int i = 0; i < game.settings.numAbilities; i++) {
                    abilities.get(i).Collision(response);
                }
            }
        }
    }
}