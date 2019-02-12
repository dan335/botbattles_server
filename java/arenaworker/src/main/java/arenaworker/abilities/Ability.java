package arenaworker.abilities;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import arenaworker.Base;
import arenaworker.Player;
import arenaworker.abilityobjects.Projectile;


public class Ability {

    public Player player;
    long lastFired = 0L;
    public long interval = 1000L;
    boolean isOn = false;
    public Set<Base> abilityObjects = ConcurrentHashMap.newKeySet();
    public int abilityNum;

    public Ability(Player player, int abilityNum) {
        this.player = player;
        this.abilityNum = abilityNum;
    }


    public void Tick() {
        if (isOn) {
            if (player.game.tickStartTime >= lastFired + interval) {
                Fire();
            }
        }

        for (Base obj : abilityObjects) {
            obj.Tick();
        }
    }


    public void Start() {
        isOn = true;
        if (player.game.tickStartTime >= lastFired + interval) {
            Fire();
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


    public void SendCooldownMessage() {
        JSONObject json = new JSONObject();
        json.put("t", "abilityCooldown");
        json.put("num", abilityNum);
        json.put("lastFired", lastFired);
        json.put("interval", (double)interval);
        player.client.SendJson(json);
    }
}