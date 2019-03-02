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
        boolean isFirst = true;
        for (int i = 0; i < numBullets; i++) {
            double angle = player.rotation + intervalAngle * i - intervalAngle * numBullets / 2;
            new BlasterBullet(this, player.FirePositionX(), player.FirePositionY(), angle, 6, 10, 1, "0xff4444", 0.8, isFirst);
            isFirst = false;
        }
    }
}