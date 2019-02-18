package arenaworker.abilities;

import org.json.JSONObject;

import arenaworker.Player;
import arenaworker.lib.Collision;

public class Smasher extends Ability {

    double duration = 2000L;
    double lastCreated;
    boolean isOn = false;
    double damage = 60;
    double shieldDamageMultiplier = 1;
    double radius;
    
    public Smasher(Player player, int abilityNum, String abilityType) {
        super(player, abilityNum, abilityType);
        interval = 5000L;
        radius = player.radius + 13;
    }

    @Override
    public void Fire() {
        super.Fire();
        On();
    }

    void On() {
        isOn = true;
        lastCreated = player.game.tickStartTime;
        JSONObject json = new JSONObject();
        json.put("t", "smasherSpikesInitial");
        json.put("radius", radius);
        json.put("shipId", player.id);
        json.put("id", id);
        player.game.SendJsonToClients(json);
    }

    void Off() {
        isOn = false;
        JSONObject json = new JSONObject();
        json.put("t", "smasherSpikesDestroyed");
        json.put("shipId", player.id);
        json.put("id", id);
        player.game.SendJsonToClients(json);
    }


    @Override
    public void Tick() {
        super.Tick();

        if (isOn) {
            if (lastCreated + duration < player.game.tickStartTime) {
                Off();
            }
        }
    }


    @Override
    public void PlayerCollision(Collision collision) {
        if (isOn) {
            if (collision.b instanceof Player) {
                if (collision.b != player) {
                    Player p = (Player) collision.b;
                    p.TakeDamage(damage * collision.magnitude, shieldDamageMultiplier);
                }
            }
        }
    }

    @Override
    public void Destroy() {
        if (isOn) {
            Off();
        }
        super.Destroy();
    }
}