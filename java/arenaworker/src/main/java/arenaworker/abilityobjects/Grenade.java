package arenaworker.abilityobjects;

import arenaworker.Base;
import arenaworker.Box;
import arenaworker.Obj;
import arenaworker.Obstacle;
import arenaworker.Player;
import arenaworker.abilities.Ability;
import arenaworker.abilities.BombDropper;
import arenaworker.abilities.GrenadeLauncher;
import arenaworker.lib.Physics;
import arenaworker.lib.Vector2;
import arenaworker.other.Explosion;

public class Grenade extends AbilityObjectPhysics implements Comparable<Grenade> {

    public double speed = 0.1;
    public double damage = 10;
    public double shieldDamageMultiplier = 1;

    // sendInitial boolean is so that Mine.java can override initialUpdateName etc
    
    public Grenade(Ability ability, double rotation, double radius, double amountOfForce, double damage, boolean sendInitial) {
        super(ability, ability.player.position.x, ability.player.position.y, radius, rotation, true);
        initialUpdateName = "grenadeInitial";
        updateName = "grenadeUpdate";
        destroyUpdateName = "grenadeDestroy";
        mass = 0.4;
        shieldDamageMultiplier = 1;
        this.damage = damage;

        if (ability.player.game.isInBulletTime) {
            amountOfForce *= 2;
        }
        
        forces = new Vector2(
            Math.cos(rotation) * amountOfForce,
            Math.sin(rotation) * amountOfForce
        );

        if (sendInitial) {
            SendInitialToAll();
        }
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
        new Explosion(ability.player.game, position.x, position.y, 200, damage, 1, "0xff4444", ability.player);
    }

    @Override
    public void Destroy() {
        if (ability instanceof GrenadeLauncher) {
            GrenadeLauncher launcher = (GrenadeLauncher) ability;
            launcher.cooldown = launcher.defaultInterval; 
            launcher.SendCooldownMessage();
            launcher.grenade = null;
        }

        if (ability instanceof BombDropper) {
            BombDropper dropper = (BombDropper)ability;
            dropper.grenades.remove(this);
        }
        super.Destroy();
    }


    public int compareTo(Grenade o) {
        return id.compareTo(o.id);
    }
}