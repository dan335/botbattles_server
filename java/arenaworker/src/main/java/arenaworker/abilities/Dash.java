package arenaworker.abilities;

import arenaworker.Player;
import arenaworker.lib.Vector2;

public class Dash extends Ability {

    double extraForce = 5;
    
    public Dash(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 2000L;
    }

    @Override
    public void Fire() {
        super.Fire();

        player.forces.add(new Vector2(
            Math.cos(player.rotation) * extraForce,
            Math.sin(player.rotation) * extraForce
        ));
    }
}