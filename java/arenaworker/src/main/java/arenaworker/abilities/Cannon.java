package arenaworker.abilities;

import org.json.JSONObject;

import arenaworker.Player;
import arenaworker.abilityobjects.BlasterBullet;

public class Cannon extends Ability {

    int numBullets = 3;
    double angleInDegrees = 20;
    long chargeTime = 700L;
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
                boolean isFirst = true;
                for (int i = 0; i < numBullets; i++) {
                    double angle = player.rotation + intervalAngle * i - intervalAngle * numBullets / 2;
                    new BlasterBullet(this, player.position.x, player.position.y, angle, 16, damage, 1, "0xff4444", 1.1, isFirst);
                    isFirst = false;
                }

                isCharging = false;
                player.isCharging = false;

                JSONObject json = new JSONObject();
                json.put("t", "chargeEnd");
                json.put("shipId", player.id);
                player.game.SendJsonToClients(json);
            }
        }
    }


    @Override
    public void Fire() {
        super.Fire();
        player.isCharging = true;
        chargeStart = player.game.tickStartTime;
        isCharging = true;

        JSONObject json = new JSONObject();
        json.put("t", "chargeStart");
        json.put("shipId", player.id);
        player.game.SendJsonToClients(json);
    }
}