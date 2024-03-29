package arenaworker.abilities;

import java.util.UUID;

import org.json.JSONObject;

import arenaworker.Player;

public class Boost extends Ability {

    boolean isActive = false;
    long duration = 2000L;
    long start;
    final String id = UUID.randomUUID().toString().substring(0, 8);
    
    public Boost(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 6000L;
    }

    @Override
    public void Fire() {
        super.Fire();

        Activate();
    }

    @Override
    public void Tick() {
        if (isActive) {
            if (start + duration < player.game.tickStartTime) {
                Deactivate();
            }
        }
    }

    void Activate() {
        if (isActive) {
            Deactivate();
        }
        
        isActive = true;
        start = player.game.tickStartTime;

        player.shipSpeedMultiplier += 1;
        
        JSONObject json = new JSONObject();
        json.put("t", "boostStart");
        json.put("shipId", player.id);
        json.put("id", id);
        player.game.SendJsonToClients(json);
    }

    void Deactivate() {
        isActive = false;
        player.shipSpeedMultiplier -= 1;

        JSONObject json = new JSONObject();
        json.put("t", "boostEnd");
        json.put("shipId", player.id);
        json.put("id", id);
        player.game.SendJsonToClients(json);
    }
}