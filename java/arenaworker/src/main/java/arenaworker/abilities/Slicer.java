package arenaworker.abilities;

import org.json.JSONObject;

import arenaworker.Player;
import arenaworker.abilityobjects.TurretObject;
import arenaworker.lib.Collision;

public class Slicer extends Ability {

    double duration = 2000L;
    double lastCreated;
    boolean isOn = false;
    double damage = 5;
    double shieldDamageMultiplier = 1;
    double radius;
    
    public Slicer(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 3500L;
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
        json.put("t", "slicerSpikesInitial");
        json.put("radius", radius);
        json.put("shipId", player.id);
        json.put("id", id);
        player.game.SendJsonToClients(json);

        player.shipSpeedMultiplier += 1;
    }

    void Off() {
        isOn = false;
        JSONObject json = new JSONObject();
        json.put("t", "slicerSpikesDestroyed");
        json.put("shipId", player.id);
        json.put("id", id);
        player.game.SendJsonToClients(json);

        player.shipSpeedMultiplier -= 1;
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
                    ((Player)collision.b).TakeDamage(damage, shieldDamageMultiplier, player);
                }
            } else if (collision.b instanceof TurretObject) {
                TurretObject turret = (TurretObject)collision.b;
                if (turret.ability.player != player) {
                    System.out.println(damage);
                    turret.TakeDamage(damage, player);
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