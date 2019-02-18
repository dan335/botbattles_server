package arenaworker.abilities;

import java.util.concurrent.ConcurrentSkipListSet;

import arenaworker.Player;
import arenaworker.abilityobjects.VortexTrapGrenade;

public class VortexTrap extends Ability {

    public ConcurrentSkipListSet<VortexTrapGrenade> traps = new ConcurrentSkipListSet<VortexTrapGrenade>();
    int max = 15;
    
    public VortexTrap(Player player, int abilityNum) {
        super(player, abilityNum);
    }

    @Override
    public void Fire() {
        super.Fire();

        traps.add(new VortexTrapGrenade(this, player.rotation, 12));

        if (traps.size() > max) {
            traps.last().Explode();
        }
    }
}