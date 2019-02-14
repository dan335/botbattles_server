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
    public long interval = 1000L;
    boolean isOn = false;
    public Set<Base> abilityObjects = ConcurrentHashMap.newKeySet();
    public int abilityNum;
    public String abilityType;

    public Ability(Player player, int abilityNum, String abilityType) {
        this.player = player;
        this.abilityNum = abilityNum;
        this.abilityType = abilityType;
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


    public void PlayerCollision(Collision collision) {
        
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