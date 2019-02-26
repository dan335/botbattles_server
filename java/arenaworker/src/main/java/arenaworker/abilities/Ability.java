package arenaworker.abilities;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import arenaworker.Base;
import arenaworker.Player;
import arenaworker.lib.Collision;


public class Ability {

    public final String id = UUID.randomUUID().toString().substring(0, 8);
    final int hashcode = id.hashCode();
    public Player player;
    long lastFired = 0L;
    public long cooldown = 1000L;
    public boolean isOn = false;
    public Set<Base> abilityObjects = ConcurrentHashMap.newKeySet();
    public int abilityNum;
    double rageMultiplier = 0.5;

    public Ability(Player player, int abilityNum) {
        this.player = player;
        this.abilityNum = abilityNum;
    }


    public void Tick() {
        if (isOn) {
            if (player.isRaging) {
                if (player.game.tickStartTime >= lastFired + cooldown * rageMultiplier) {
                    Fire();
                }
            } else {
                if (player.game.tickStartTime >= lastFired + cooldown) {
                    Fire();
                }
            }
        }

        for (Base obj : abilityObjects) {
            obj.Tick();
        }
    }


    // called when player is created
    public void Init() {
        
    }


    public void Start() {
        isOn = true;
        if (player.isRaging) {
            if (player.game.tickStartTime >= lastFired + cooldown * rageMultiplier) {
                Fire();
            }
        } else {
            if (player.game.tickStartTime >= lastFired + cooldown) {
                Fire();
            }
        }
    }

    public boolean IsReady() {
        if (player.isRaging) {
            return player.game.tickStartTime >= lastFired + cooldown * rageMultiplier;
        } else {
            return player.game.tickStartTime >= lastFired + cooldown;
        }
    }

    public void Stop() {
        isOn = false;
    }

    public void Fire() {
        lastFired = player.game.tickStartTime;
        SendCooldownMessage();
    }


    public void Destroy() {
        for (Base obj : abilityObjects) {
            obj.Destroy();
        }
    }


    public void PlayerPositionChanged() {

    }


    public void PlayerCollision(Collision collision) {
        
    }


    public void PlayerTookDamage() {

    }


    public void PlayerStartedAnAbility(Ability a) {

    }


    public void BulletTimeEnded() {
        
    }


    public void SendCooldownMessage() {
        JSONObject json = new JSONObject();
        json.put("t", "abilityCooldown");
        json.put("num", abilityNum);
        //json.put("lastFired", lastFired); // using time message was received
        if (player.isRaging) {
            json.put("cooldown", (double)cooldown * rageMultiplier);
        } else {
            json.put("cooldown", (double)cooldown);
        }
        player.client.SendJson(json);
    }
}