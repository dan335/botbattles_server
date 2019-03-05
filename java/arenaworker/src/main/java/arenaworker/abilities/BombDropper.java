package arenaworker.abilities;

import java.util.concurrent.ConcurrentSkipListSet;

import arenaworker.Player;
import arenaworker.abilityobjects.Mine;

public class BombDropper extends Ability {

    double damage = 150;

    public long defaultInterval = 2500L;
    public ConcurrentSkipListSet<Mine> grenades = new ConcurrentSkipListSet<Mine>();
    int maxGrenades = 15;
    
    public BombDropper(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = defaultInterval;
    }

    @Override
    public void Fire() {
        super.Fire();
        grenades.add(new Mine(this, player.GetRotation(), 12, 0, damage));

        if (grenades.size() > maxGrenades) {
            grenades.last().Explode();
        }
    }
}