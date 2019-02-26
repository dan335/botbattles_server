package arenaworker.abilities;

import arenaworker.Player;
import arenaworker.abilityobjects.BlasterBullet;

public class Cannon extends Ability {

    int numBullets = 3;
    double angleInDegrees = 20;
    long chargeTime = 1000L;
    long chargeStart;
    boolean isCharging = false;
    double damage = 40;
    

    public Cannon(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 3500L;
    }


    @Override
    public void Tick() {
        super.Tick();

        if (isCharging) {
            if (chargeStart + chargeTime < player.game.tickStartTime) {

                double intervalAngle = Math.toRadians(angleInDegrees / numBullets);
                for (int i = 0; i < numBullets; i++) {
                    double angle = player.rotation + intervalAngle * i - intervalAngle * numBullets / 2;
                    new BlasterBullet(this, player.position.x, player.position.y, angle, 16, damage, 1, "0xff4444", 1.1);
                }

                isCharging = false;
                player.isFrozen = false;
            }
        }
    }


    @Override
    public void Fire() {
        super.Fire();
        player.Freeze(1000L * 60L);
        chargeStart = player.game.tickStartTime;
        isCharging = true;
    }
}