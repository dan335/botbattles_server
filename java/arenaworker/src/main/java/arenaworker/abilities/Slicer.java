package arenaworker.abilities;

import java.util.Set;

import org.json.JSONObject;

import arenaworker.Base;
import arenaworker.Player;
import arenaworker.abilityobjects.TurretObject;
import arenaworker.lib.Collision;
import arenaworker.lib.Physics;

public class Slicer extends Ability {

    double duration = 1500L;
    double lastCreated;
    boolean isOn = false;
    double damage = 2.5;
    double shieldDamageMultiplier = 1;
    double radius;
    
    public Slicer(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 5000L;
        radius = player.radius + 25;
    }

    @Override
    public void Fire() {
        super.Fire();
        On();
    }

    void On() {
        if (isOn) {
            Off();
        }

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
            Set<Base> objs = player.game.grid.retrieve(player.position, radius);
            for (Base o : objs) {
                if (o instanceof Player) {
                    if (o != player) {
                        if (Physics.circleInCircle(player.position.x, player.position.y, radius, o.position.x, o.position.y, o.radius)) {
                            Player p = (Player) o;
                            p.TakeDamage(damage, shieldDamageMultiplier, player);
                        }
                    }
                } else if (o instanceof TurretObject) {
                    TurretObject turret = (TurretObject)o;
                    if (turret.ability.player != player) {
                        turret.TakeDamage(damage, player);
                    }
                }
            }

            if (lastCreated + duration < player.game.tickStartTime) {
                Off();
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