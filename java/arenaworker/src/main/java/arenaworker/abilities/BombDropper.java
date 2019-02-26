package arenaworker.abilities;

import java.util.concurrent.ConcurrentSkipListSet;

import arenaworker.Player;
import arenaworker.abilityobjects.Grenade;

public class BombDropper extends Ability {

    public long defaultInterval = 2500L;
    public ConcurrentSkipListSet<Grenade> grenades = new ConcurrentSkipListSet<Grenade>();
    int maxGrenades = 15;
    
    public BombDropper(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = defaultInterval;
    }

    @Override
    public void Fire() {
        super.Fire();
        grenades.add(new Grenade(this, player.rotation, 12, 0, 100));

        if (grenades.size() > maxGrenades) {
            grenades.last().Explode();
        }
    }
}