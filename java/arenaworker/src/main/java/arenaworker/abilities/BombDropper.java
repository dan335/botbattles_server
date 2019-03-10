package arenaworker.abilities;

import java.util.concurrent.ConcurrentSkipListSet;

import arenaworker.Player;
import arenaworker.abilityobjects.Mine;

public class BombDropper extends Ability {

    double damage = 150;
    double mineRadius = 36;
    String color = "0xffbb44";

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
        grenades.add(new Mine(this, player.GetRotation(), mineRadius, 0, damage, color));

        if (grenades.size() > maxGrenades) {
            grenades.last().Explode();
        }
    }
}