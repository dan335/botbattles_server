package arenaworker.abilities;

import java.util.Set;

import org.json.JSONObject;

import arenaworker.Base;
import arenaworker.Obj;
import arenaworker.Obstacle;
import arenaworker.Player;
import arenaworker.abilityobjects.FreezeTrapGrenade;
import arenaworker.abilityobjects.Grenade;
import arenaworker.abilityobjects.Mine;
import arenaworker.abilityobjects.TurretObject;
import arenaworker.abilityobjects.VortexGrenade;
import arenaworker.lib.Physics;

public class Vacuum extends Ability {

    double extraForce = 5;
    double radius = 300;
    long duration = 1000L;
    boolean isVacuuming = false;
    long vacuumStart;
    
    public Vacuum(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 5000L;
    }

    @Override
    public void Tick() {
        super.Tick();

        if (isVacuuming) {
            Set<Base> objs = player.game.grid.retrieve(player.position, radius);
            for (Base o : objs) {
                if (o instanceof Player || o instanceof Obstacle || o instanceof TurretObject || o instanceof FreezeTrapGrenade || o instanceof Grenade || o instanceof Mine || o instanceof VortexGrenade) {
                    if (o != player) {
                        if (Physics.circleInCircle(player.position.x, player.position.y, radius, o.position.x, o.position.y, o.radius)) {
                            Obj obj = (Obj)o;
                            
                            // scale by inverse mass
                            double inverseMass = 0;
                            if (obj.mass != 0) {
                                inverseMass = 1 / obj.mass;
                            }

                            if (o instanceof Player || o instanceof TurretObject) {
                                obj.forces.add(player.position.copy().subtract(o.position).normalize().scale(0.05).scale(inverseMass));
                            } else {
                                obj.forces.add(player.position.copy().subtract(o.position).normalize().scale(0.005).scale(inverseMass));
                            }
                        }
                    }
                }
            }

            if (vacuumStart + duration < player.game.tickStartTime) {
                isVacuuming = false;
                JSONObject json = new JSONObject();
                json.put("t", "vacuumEnd");
                json.put("id", id);
                player.game.SendJsonToClients(json);
            }
        }
    }

    @Override
    public void Fire() {
        super.Fire();

        isVacuuming = true;
        vacuumStart = player.game.tickStartTime;
        JSONObject json = new JSONObject();
        json.put("t", "vacuumStart");
        json.put("shipId", player.id);
        json.put("id", id);
        json.put("radius", radius);
        player.game.SendJsonToClients(json);
    }
}