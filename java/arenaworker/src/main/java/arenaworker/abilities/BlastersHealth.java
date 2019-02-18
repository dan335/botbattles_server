package arenaworker.abilities;

import arenaworker.Player;

public class BlastersHealth extends Blasters {

    public BlastersHealth(Player player, int abilityNum) {
        super(player, abilityNum);
        color = "0x91df3e";
        damage = damage * 2;
        shieldDamageMultiplier = 0.125;
    }
}