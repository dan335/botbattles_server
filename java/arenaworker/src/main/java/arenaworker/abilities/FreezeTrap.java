package arenaworker.abilities;

import java.util.concurrent.ConcurrentSkipListSet;

import arenaworker.Player;
import arenaworker.abilityobjects.FreezeTrapGrenade;

public class FreezeTrap extends Ability {

    String color = "0x387bd9";
    double trapRadius = 36;

    public ConcurrentSkipListSet<FreezeTrapGrenade> traps = new ConcurrentSkipListSet<FreezeTrapGrenade>();
    int max = 15;
    
    public FreezeTrap(Player player, int abilityNum) {
        super(player, abilityNum);
    }

    @Override
    public void Fire() {
        super.Fire();

        traps.add(new FreezeTrapGrenade(this, player.GetRotation(), trapRadius, color));

        if (traps.size() > max) {
            traps.last().Destroy();
        }
    }
}