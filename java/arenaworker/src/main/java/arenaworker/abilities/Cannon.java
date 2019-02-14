package arenaworker.abilities;

import arenaworker.Player;
import arenaworker.abilityobjects.BlasterBullet;

public class Cannon extends Ability {

    int numBullets = 10;
    double angleInDegrees = 30;
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
                new BlasterBullet(this, player.position.x, player.position.y, player.rotation, 20, 100);
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