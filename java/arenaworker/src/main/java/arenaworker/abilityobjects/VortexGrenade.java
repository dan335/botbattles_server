package arenaworker.abilityobjects;

import java.util.Set;

import org.json.JSONObject;

import arenaworker.Base;
import arenaworker.Box;
import arenaworker.Obj;
import arenaworker.Obstacle;
import arenaworker.Player;
import arenaworker.abilities.Ability;
import arenaworker.abilities.VortexLauncher;
import arenaworker.lib.Physics;
import arenaworker.lib.Vector2;

public class VortexGrenade extends AbilityObjectPhysics {

    public double speed = 0.1;
    public double damage = 10;
    public double shieldDamageMultiplier = 1;
    double vortexRadius = 250;
    long stunDuration = 1000L;
    
    public VortexGrenade(Ability ability, double rotation, double radius, double amountOfForce) {
        super(ability, ability.player.position.x, ability.player.position.y, radius, rotation, true);
        initialUpdateName = "vortexGrenadeInitial";
        updateName = "vortexGrenadeUpdate";
        destroyUpdateName = "vortexGrenadeDestroy";
        mass = 0.4;
        shieldDamageMultiplier = 1;

        if (ability.player.game.isInBulletTime) {
            amountOfForce *= 2;
        }
        
        forces = new Vector2(
            Math.cos(rotation) * amountOfForce,
            Math.sin(rotation) * amountOfForce
        );

        SendInitialToAll();
    }


    @Override
    public void Contact(Base otherObject) {
        if (otherObject instanceof Obstacle) {
            Physics.resolveCollision(this, (Obj)otherObject);

        } else if (otherObject instanceof ShieldBubble) {
            if (((ShieldBubble)otherObject).ability.player != ability.player) {
                Destroy();
            }

        } else if (otherObject instanceof Player) {
            if (ability.player != otherObject) {
                Physics.resolveCollision(this, (Obj)otherObject);
                Explode();
            }

        } else if (otherObject instanceof Box) {
            Explode();

        } else if (otherObject instanceof TurretObject) {
            Physics.resolveCollision(this, (Obj)otherObject);
        }
    }


    public void Explode() {
        Destroy();
        
        Set<Base> objs = ability.player.game.grid.retrieve(position, vortexRadius);
        for (Base o : objs) {
            if (o instanceof Player || o instanceof Obstacle || o instanceof TurretObject || o instanceof FreezeTrapGrenade || o instanceof Grenade || o instanceof Mine || o instanceof VortexGrenade) {
                if (Physics.circleInCircle(position.x, position.y, vortexRadius, o.position.x, o.position.y, o.radius)) {

                    Obj obj = (Obj)o;

                    Vector2 diff = position.copy().subtract(obj.position);

                    // scale by inverse mass
                    double inverseMass = 0;
                    if (obj.mass != 0) {
                        inverseMass = 1 / obj.mass;
                    }
                    
                    if (o instanceof Player) {
                        Player p = (Player)o;
                        p.forces.add(diff.getNormalized().scale(diff.length() * 0.005).scale(inverseMass));
                        p.Stun(stunDuration);
                    } else if (o instanceof TurretObject) {
                        obj.forces.add(diff.getNormalized().scale(diff.length() * 0.005).scale(inverseMass));
                    } else {
                        obj.forces.add(diff.getNormalized().scale(diff.length() * 0.0005).scale(inverseMass));
                    }
                }
            }
        }
    }

    @Override
    public void Destroy() {
        if (ability instanceof VortexLauncher) {
            VortexLauncher launcher = (VortexLauncher) ability;
            launcher.cooldown = launcher.defaultInterval; 
            launcher.SendCooldownMessage();
            launcher.grenade = null;
        }

        super.Destroy();
    }


    @Override
    public JSONObject DestroyData() {
        JSONObject json = super.DestroyData();
        json.put("radius", vortexRadius);
        json.put("x", position.x);
        json.put("y", position.y);
        return json;
    }
}