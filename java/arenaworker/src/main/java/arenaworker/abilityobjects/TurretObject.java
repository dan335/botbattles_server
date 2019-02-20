package arenaworker.abilityobjects;

import java.util.Set;

import arenaworker.Base;
import arenaworker.Box;
import arenaworker.Obj;
import arenaworker.Obstacle;
import arenaworker.Player;
import arenaworker.abilities.Ability;
import arenaworker.lib.Physics;
import arenaworker.lib.Vector2;
import arenaworker.other.Explosion;

public class TurretObject extends AbilityObjectPhysics {

    Player target;
    long lastFired = 0L;
    long fireCooldown = 500L;
    double health = 100;
    double aquireRadius = 700;
    double bulletDamage = 6;
    double shieldDamageMultiplier = 1;
    String bulletColor = "0xff4444";

    public TurretObject(Ability ability, double rotation, double x, double y) {
        super(ability, x, y, 20, rotation, true);

        initialUpdateName = "turretInitial";
        updateName = "turretUpdate";
        destroyUpdateName = "turretDestroy";
        mass = 1;
        SendInitialToAll();
    }


    @Override
    public void Tick() {
        super.Tick();

        // is target alive?
        if (target != null && !ability.player.game.players.contains(target)) {
            target = null;
        }

        // is target a long ways away?
        if (target != null && position.copy().subtract(target.position).length() > aquireRadius) {
            target = null;
        }

        // aquire new target
        if (target == null) {
            AquireTarget();
        }

        if (target != null) {
            RotateTowardsTarget();
        }

        if (target != null) {
            CheckForFiring();
        }
    }


    void AquireTarget() {
        Set<Base> objs = ability.player.game.grid.retrieve(position, aquireRadius);
        for (Base o : objs) {
            if (o instanceof Player) {
                if (o != ability.player) {
                    if (Physics.circleInCircle(position.x, position.y, aquireRadius, o.position.x, o.position.y, o.radius)) {
                        target = (Player)o;
                    }
                }
            }
        }
    }


    void RotateTowardsTarget() {
        rotation = Physics.slowlyRotateToward(position, rotation, target.position, 5);
        needsUpdate = true;
    }


    void CheckForFiring() {
        if (lastFired + fireCooldown < ability.player.game.tickStartTime) {
            FireAtTarget();
        }
    }


    void FireAtTarget() {
        new BlasterBullet(ability, position.x, position.y, rotation, 6, bulletDamage, shieldDamageMultiplier, bulletColor);
        lastFired = ability.player.game.tickStartTime;
    }


    @Override
    public void Contact(Base otherObject) {
        if (otherObject instanceof Obstacle) {
            Physics.resolveCollision(this, (Obj)otherObject);
        } else if (otherObject instanceof Player) {
            Physics.resolveCollision(this, (Obj)otherObject);
        } else if (otherObject instanceof Box) {
            Destroy();
        }
    }


    public void TakeDamage(double damage, double shieldDamageMultiplier, Player otherPlayer) {
        if (!game.isStarted) return;
        if (damage <= 0) return;
        
        health -= Math.min(health, damage);
        otherPlayer.AddDamageDealt(Math.min(health, damage));

        needsUpdate = true;

        if (health <= 0) {
            Destroy();
        }
    }


    @Override
    public void Destroy() {
        super.Destroy();
        new Explosion(ability.player.game, position.x, position.y, radius*3, 50, 1, "0xff4444", ability.player);
    }
}