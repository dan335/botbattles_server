package arenaworker.abilities;

import java.util.Set;

import org.json.JSONObject;

import arenaworker.Base;
import arenaworker.Player;
import arenaworker.lib.Physics;

public class Freezer extends Ability {

    double radius = 150;
    long duration = 1500L;
    
    public Freezer(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 5000L;
    }

    @Override
    public void Fire() {
        super.Fire();

        Set<Base> objs = player.game.grid.retrieve(player.position, player.radius);
        for (Base o : objs) {
            if (o instanceof Player) {
                if (Physics.circleInCircle(player.position.x, player.position.y, radius, o.position.x, o.position.y, o.radius)) {
                    if (o != player) {
                        ((Player)o).Freeze(duration);
                    }
                }
            }
        }

        JSONObject json = new JSONObject();
        json.put("t", "slamInitial");
        json.put("shipId", player.id);
        json.put("radius", radius);
        player.game.SendJsonToClients(json);
    }
}