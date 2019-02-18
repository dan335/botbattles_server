package arenaworker.abilities;

import arenaworker.Player;
import arenaworker.abilityobjects.BlasterBullet;

public class Shotgun extends Ability {

    int numBullets = 10;
    double angleInDegrees = 30;
    
    public Shotgun(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 1500L;
    }

    @Override
    public void Fire() {
        super.Fire();

        double intervalAngle = Math.toRadians(angleInDegrees / numBullets);
        for (int i = 0; i < numBullets; i++) {
            double angle = player.rotation + intervalAngle * i - intervalAngle * numBullets / 2;
            new BlasterBullet(this, player.position.x, player.position.y, angle, 6, 10, 1, "0xff4444");
        }
    }
}