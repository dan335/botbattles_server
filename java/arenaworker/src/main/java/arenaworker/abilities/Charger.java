package arenaworker.abilities;

import org.json.JSONObject;

import arenaworker.Player;
import arenaworker.abilityobjects.BlasterBullet;

public class Charger extends Ability {

    double amountOfForce = 5;
    public long defaultCooldown = 5000L;
    boolean isCharging = false;
    String color = "0xff4444";
    long chargeStart;
    double damageMultiplier = 0.03;
    double sizeMultiplier = 0.008;
    
    public Charger(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = defaultCooldown;
    }

    @Override
    public void Fire() {
        lastFired = player.game.tickStartTime;

        if (isCharging) {
            // fire
            isCharging = false;
            player.isFrozen = false;
            double time = (double)(player.game.tickStartTime - chargeStart);
            new BlasterBullet(this, player.FirePositionX(), player.FirePositionY(), player.rotation, Math.min(80, time * sizeMultiplier), time * damageMultiplier, 1, color, 3, true);
            cooldown = defaultCooldown;
            lastFired = player.game.tickStartTime;
            SendCooldownMessage();

            JSONObject json = new JSONObject();
            json.put("t", "chargeEnd");
            json.put("shipId", player.id);
            player.game.SendJsonToClients(json);
        } else {
            // start charging
            isCharging = true;
            chargeStart = player.game.tickStartTime;
            player.isFrozen = true;
            player.frozenEnd = player.game.tickStartTime + 1000L * 60L * 60L;
            cooldown = 300L;
            lastFired = player.game.tickStartTime;
            SendCooldownMessage();

            JSONObject json = new JSONObject();
            json.put("t", "chargeStart");
            json.put("shipId", player.id);
            player.game.SendJsonToClients(json);
        }
    }
}