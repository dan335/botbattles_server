package arenaworker.abilities;

import arenaworker.Player;
import arenaworker.abilityobjects.BlasterBullet;

public class Blasters extends Ability {
    
    public Blasters(Player player) {
        super(player);
        interval = 100L;
    }

    @Override
    public void Fire() {
        super.Fire();

        new BlasterBullet(this, player.rotation);
    }
}