package arenaworker.abilities;

import org.json.JSONObject;

import arenaworker.Player;
import arenaworker.lib.Vector2;

public class Dash extends Ability {

    double extraForce = 1.5;
    
    public Dash(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 1500L;
    }

    @Override
    public void Fire() {
        super.Fire();

        player.forces.add(new Vector2(
            Math.cos(player.GetRotation()) * extraForce,
            Math.sin(player.GetRotation()) * extraForce
        ));

        JSONObject json = new JSONObject();
        json.put("t", "dashInitial");
        json.put("shipId", player.id);
        player.game.SendJsonToClients(json);
    }
} 