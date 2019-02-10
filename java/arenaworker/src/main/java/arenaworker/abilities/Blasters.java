package arenaworker.abilities;

import arenaworker.Player;
import arenaworker.projectiles.BlasterBullet;

public class Blasters extends Ability {
    
    public Blasters(Player player) {
        super(player);
        interval = 100L;
    }

    @Override
    public void Fire() {
        super.Fire();

        new BlasterBullet(this);
    }
}