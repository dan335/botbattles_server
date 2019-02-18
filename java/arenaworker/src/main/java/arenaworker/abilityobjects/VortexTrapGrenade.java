package arenaworker.abilityobjects;

import java.util.Set;

import arenaworker.Base;
import arenaworker.Box;
import arenaworker.Obj;
import arenaworker.Obstacle;
import arenaworker.Player;
import arenaworker.abilities.Ability;
import arenaworker.abilities.VortexTrap;
import arenaworker.lib.Physics;
import arenaworker.lib.Vector2;

public class VortexTrapGrenade extends AbilityObjectPhysics implements Comparable<VortexTrapGrenade> {

    public double speed = 0.1;
    public double damage = 10;
    public double shieldDamageMultiplier = 1;
    long stunDuration = 1500L;
    double vortexRadius = 250;
    
    public VortexTrapGrenade(Ability ability, double rotation, double radius) {
        super(ability, ability.player.position.x, ability.player.position.y, radius, rotation, false);
        initialUpdateName = "grenadeInitial";
        updateName = "grenadeUpdate";
        destroyUpdateName = "grenadeDestroy";
        mass = 0.4;
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
        }
    }


    public void Explode() {
        Destroy();

        Set<Base> objs = ability.player.game.grid.retrieve(position, vortexRadius);
        for (Base o : objs) {
            if (o instanceof Player) {
                if (Physics.circleInCircle(position.x, position.y, vortexRadius, o.position.x, o.position.y, o.radius)) {
                    Player p = (Player)o;
                    Vector2 diff = position.copy().subtract(o.position);
                    p.forces.add(diff.getNormalized().scale(diff.length() * 0.01));
                    p.Freeze(stunDuration);
                }
            }
        }
    }

    @Override
    public void Destroy() {
        if (ability instanceof VortexTrap) {
            ((VortexTrap)ability).traps.remove(this);
        }
        super.Destroy();
    }


    public int compareTo(VortexTrapGrenade o) {
        return id.compareTo(o.id);
    }
}