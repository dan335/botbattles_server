package arenaworker.abilityobjects;

import java.util.Set;

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
    double stunDuration = 1000L;
    
    public VortexGrenade(Ability ability, double rotation, double radius, double amountOfForce) {
        super(ability, ability.player.position.x, ability.player.position.y, radius, rotation, false);
        initialUpdateName = "grenadeInitial";
        updateName = "grenadeUpdate";
        destroyUpdateName = "grenadeDestroy";
        mass = 0.4;
        shieldDamageMultiplier = 1;
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
        if (ability instanceof VortexLauncher) {
            VortexLauncher launcher = (VortexLauncher) ability;
            launcher.cooldown = launcher.defaultInterval; 
            launcher.SendCooldownMessage();
            launcher.grenade = null;
        }

        super.Destroy();
    }
}