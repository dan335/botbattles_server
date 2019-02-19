package arenaworker.abilityobjects;

import java.util.Set;

import arenaworker.Base;
import arenaworker.Box;
import arenaworker.Obj;
import arenaworker.Obstacle;
import arenaworker.Player;
import arenaworker.abilities.Ability;
import arenaworker.abilities.FreezeTrap;
import arenaworker.lib.Physics;
import arenaworker.lib.Vector2;

public class FreezeTrapGrenade extends AbilityObjectPhysics implements Comparable<FreezeTrapGrenade> {

    public double speed = 0.1;
    public double damage = 10;
    public double shieldDamageMultiplier = 1;
    long stunDuration = 1500L;
    double vortexRadius = 250;
    
    public FreezeTrapGrenade(Ability ability, double rotation, double radius) {
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
                ((Player)otherObject).Freeze(stunDuration);
                Destroy();
            }
        } else if (otherObject instanceof Box) {
            Destroy();
        }
    }

    @Override
    public void Destroy() {
        if (ability instanceof FreezeTrap) {
            ((FreezeTrap)ability).traps.remove(this);
        }
        super.Destroy();
    }


    public int compareTo(FreezeTrapGrenade o) {
        return id.compareTo(o.id);
    }
}