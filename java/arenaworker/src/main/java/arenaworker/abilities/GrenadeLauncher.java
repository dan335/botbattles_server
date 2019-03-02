package arenaworker.abilities;

import org.json.JSONObject;

import arenaworker.Player;
import arenaworker.abilityobjects.Grenade;

public class GrenadeLauncher extends Ability {

    public Grenade grenade;
    double amountOfForce = 2.5;
    public long defaultInterval = 5000L;
    double damage = 100;
    
    public GrenadeLauncher(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = defaultInterval;
    }

    @Override
    public void Fire() {
        lastFired = player.game.tickStartTime;

        if (grenade == null) {
            grenade = new Grenade(this, player.rotation, 12, amountOfForce, damage, true);
            cooldown = 300L;
            SendCooldownMessage();
            JSONObject json = new JSONObject();
            json.put("t", "blast");
            json.put("x", player.position.x);
            json.put("y", player.position.y);
            json.put("rotation", player.rotation);
            json.put("color", "0xff4444");
            player.game.SendJsonToClients(json);
        } else {
            grenade.Explode();  
        }
    }
}