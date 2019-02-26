package arenaworker.abilities;

import java.util.Set;

import arenaworker.Base;
import arenaworker.Player;
import arenaworker.lib.Physics;

public class Vacuum extends Ability {

    double extraForce = 5;
    double radius = 300;
    long duration = 1000L;
    boolean isVacuuming = false;
    long vacuumStart;
    
    public Vacuum(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 5000L;
    }

    @Override
    public void Tick() {
        super.Tick();

        if (isVacuuming) {
            Set<Base> objs = player.game.grid.retrieve(player.position, radius);
            for (Base o : objs) {
                if (o instanceof Player) {
                    if (o != player) {
                        if (Physics.circleInCircle(player.position.x, player.position.y, radius, o.position.x, o.position.y, o.radius)) {
                            ((Player)o).forces.add(player.position.copy().subtract(o.position).normalize().scale(0.1));
                        }
                    }
                }
            }

            if (vacuumStart + duration < player.game.tickStartTime) {
                isVacuuming = false;
                player.mass = player.game.settings.playerDefaultMass;
            }
        }
    }

    @Override
    public void Fire() {
        super.Fire();

        isVacuuming = true;
        vacuumStart = player.game.tickStartTime;
        player.mass = 0;
    }
}