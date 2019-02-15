package arenaworker.abilities;

import arenaworker.Player;
import arenaworker.abilityobjects.Grenade;

public class BombDropper extends Ability {

    public long defaultInterval = 4000L;
    
    public BombDropper(Player player, int abilityNum, String abilityType) {
        super(player, abilityNum, abilityType);
        interval = defaultInterval;
    }

    @Override
    public void Fire() {
        super.Fire();
        new Grenade(this, player.rotation, 12, 0, 100);
    }
}