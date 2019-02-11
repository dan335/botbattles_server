package arenaworker.abilities;

import arenaworker.Player;
import arenaworker.abilityobjects.BlasterBullet;

public class Shotgun extends Ability {

    int numBullets = 10;
    double angleInDegrees = 30;
    
    public Shotgun(Player player) {
        super(player);
        interval = 1000L;
    }

    @Override
    public void Fire() {
        super.Fire();

        double intervalAngle = Math.toRadians(angleInDegrees / numBullets);
        for (int i = 0; i < numBullets; i++) {
            double angle = player.rotation + intervalAngle * i - intervalAngle * numBullets / 2;
            new BlasterBullet(this, angle);
        }
    }
}