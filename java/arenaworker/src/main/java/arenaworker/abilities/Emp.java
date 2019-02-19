package arenaworker.abilities;

import java.util.Set;

import org.json.JSONObject;

import arenaworker.Base;
import arenaworker.Player;
import arenaworker.lib.Physics;

public class Emp extends Ability {

    double radius = 250;
    
    public Emp(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 9000L;
    }

    @Override
    public void Fire() {
        super.Fire();

        Set<Base> objs = player.game.grid.retrieve(player.position, radius);
        for (Base o : objs) {
            if (o instanceof Player) {
                if (o != player) {
                    if (Physics.circleInCircle(player.position.x, player.position.y, radius, o.position.x, o.position.y, o.radius)) {
                        Player p = (Player) o;
                        p.TakeDamage(p.shield, 1, player);
                    }
                }
            }
        }

        JSONObject json = new JSONObject();
        json.put("t", "empInitial");
        json.put("shipId", player.id);
        json.put("radius", radius);
        player.game.SendJsonToClients(json);
    }
}