package arenaworker.abilities;

import arenaworker.Player;
import arenaworker.abilityobjects.BlasterBullet;

public class Blasters extends Ability {

    String color = "0xff4444";
    double damage = 15;
    double shieldDamageMultiplier = 1;
    double size = 8;
    double bulletSpeed = 0.9;
    
    public Blasters(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 250L;
    }

    @Override
    public void Fire() {
        super.Fire();

        new BlasterBullet(this, player.FirePositionX(), player.FirePositionY(), player.GetRotation(), size, damage, shieldDamageMultiplier, color, bulletSpeed, true);
    }
}