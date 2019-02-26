package arenaworker.abilities;

import arenaworker.Player;

public class Rage extends Ability {

    long rageDuration = 3000L;
    
    public Rage(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 12000L;
    }

    @Override
    public void Fire() {
        super.Fire();

        player.Rage(rageDuration);
    }
}