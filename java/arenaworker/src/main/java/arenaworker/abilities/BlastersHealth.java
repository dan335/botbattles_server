package arenaworker.abilities;

import arenaworker.Player;

public class BlastersHealth extends Blasters {

    public BlastersHealth(Player player, int abilityNum, String abilityType) {
        super(player, abilityNum, abilityType);
        color = "0x91df3e";
        damage = damage * 2;
        shieldDamageMultiplier = 0.125;
    }
}