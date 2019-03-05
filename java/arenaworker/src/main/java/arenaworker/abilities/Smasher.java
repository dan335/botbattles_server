package arenaworker.abilities;

import org.json.JSONObject;

import arenaworker.Player;
import arenaworker.abilityobjects.TurretObject;
import arenaworker.lib.Collision;

public class Smasher extends Ability {

    double duration = 1500L;
    double lastCreated;
    boolean isOn = false;
    double damage = 200;
    double shieldDamageMultiplier = 1;
    double radius;
    boolean hasDoneDamange = false;
    
    public Smasher(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 5000L;
        radius = player.radius + 13;
    }

    @Override
    public void Fire() {
        super.Fire();
        hasDoneDamange = false;
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
    public void Collision(Collision collision) {
        if (isOn) {
            if (collision.b instanceof Player) {
                if (collision.b != player) {
                    if (!hasDoneDamange) {
                        ((Player) collision.b).TakeDamage(damage * collision.magnitude, shieldDamageMultiplier, player);
                        hasDoneDamange = true;
                    }
                }
            } else if (collision.b instanceof TurretObject) {
                if (!hasDoneDamange) {
                    TurretObject turret = (TurretObject)collision.b;
                    if (turret.ability.player != player) {
                        turret.TakeDamage(damage * collision.magnitude, player);
                        hasDoneDamange = true;
                    }
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