package arenaworker.abilities;

import java.util.Set;

import org.json.JSONObject;

import arenaworker.Base;
import arenaworker.Player;
import arenaworker.lib.Physics;

public class Slam extends Ability {

    double radius = 100;
    long stunDuration = 1250L;
    
    public Slam(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 5000L;
    }

    @Override
    public void Fire() {
        super.Fire();

        Set<Base> objs = player.game.grid.retrieve(player.position, radius);
        for (Base o : objs) {
            if (o instanceof Player) {
                if (Physics.circleInCircle(player.position.x, player.position.y, radius, o.position.x, o.position.y, o.radius)) {
                    if (o != player) {
                        ((Player)o).Stun(stunDuration);
                    }
                }
            }
        }

        JSONObject json = new JSONObject();
        json.put("t", "slamInitial");
        json.put("shipId", player.id);
        json.put("radius", radius);
        json.put("x", player.position.x);
        json.put("y", player.position.y);
        player.game.SendJsonToClients(json);
    }
}