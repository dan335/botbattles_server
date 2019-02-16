package arenaworker.abilities;

import arenaworker.Player;
import arenaworker.abilityobjects.BlasterBullet;

public class Cannon extends Ability {

    int numBullets = 5;
    double angleInDegrees = 20;
    long chargeTime = 500L;
    long chargeStart;
    boolean isCharging = false;
    

    public Cannon(Player player, int abilityNum, String abilityType) {
        super(player, abilityNum, abilityType);
        interval = 3000L;
    }


    @Override
    public void Tick() {
        super.Tick();

        if (isCharging) {
            if (chargeStart + chargeTime < player.game.tickStartTime) {

                double intervalAngle = Math.toRadians(angleInDegrees / numBullets);
                for (int i = 0; i < numBullets; i++) {
                    double angle = player.rotation + intervalAngle * i - intervalAngle * numBullets / 2;
                    new BlasterBullet(this, player.position.x, player.position.y, angle, 16, 50, 1, "0xff4444");
                }

                isCharging = false;
                player.isCharging = false;
            }
        }
    }


    @Override
    public void Fire() {
        super.Fire();
        player.isCharging = true;
        chargeStart = player.game.tickStartTime;
        isCharging = true;
    }
}