package arenaworker.abilities;

import arenaworker.Player;
import arenaworker.abilityobjects.BlasterBullet;

public class Blasters extends Ability {
    
    public Blasters(Player player, int abilityNum) {
        super(player, abilityNum);
        interval = 100L;
    }

    @Override
    public void Fire() {
        super.Fire();

        new BlasterBullet(this, player.rotation);
    }
}