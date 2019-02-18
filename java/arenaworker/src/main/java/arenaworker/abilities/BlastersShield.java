package arenaworker.abilities;

import arenaworker.Player;

public class BlastersShield extends Blasters {

    public BlastersShield(Player player, int abilityNum) {
        super(player, abilityNum);
        color = "0x3ea1de";
        damage = damage * 0.25;
        shieldDamageMultiplier = 8;
    }
}