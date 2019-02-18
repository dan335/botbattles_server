package arenaworker.abilities;

import java.util.Set;

import arenaworker.Base;
import arenaworker.Player;
import arenaworker.lib.Physics;
import arenaworker.lib.Vector2;

public class Vacuum extends Ability {

    double extraForce = 5;
    double radius = 300;
    
    public Vacuum(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 5000L;
    }

    @Override
    public void Fire() {
        super.Fire();

        Set<Base> objs = player.game.grid.retrieve(player.position, radius);
        for (Base o : objs) {
            if (o instanceof Player) {
                if (o != player) {
                    if (Physics.circleInCircle(player.position.x, player.position.y, radius, o.position.x, o.position.y, o.radius)) {
                        ((Player)o).forces.add(player.position.copy().subtract(o.position).normalize().scale(1.5));
                    }
                }
            }
        }
    }
}