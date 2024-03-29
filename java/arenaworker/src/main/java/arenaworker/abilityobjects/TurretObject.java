package arenaworker.abilityobjects;

import java.util.Set;

import org.json.JSONObject;

import arenaworker.Base;
import arenaworker.Box;
import arenaworker.Obj;
import arenaworker.Obstacle;
import arenaworker.Player;
import arenaworker.abilities.Ability;
import arenaworker.abilities.Turret;
import arenaworker.lib.Physics;
import arenaworker.lib.Vector2;
import arenaworker.other.Explosion;

public class TurretObject extends AbilityObjectPhysics implements Comparable<TurretObject> {

    Obj target;
    long lastFired = 0L;
    long fireCooldown = 500L;
    double health = 90;
    double aquireRadius = 700;
    double bulletDamage = 8;
    double shieldDamageMultiplier = 1;
    String bulletColor = "0xff4444";
    Vector2 interceptPos = new Vector2();
    double bulletSpeed = 0.9;


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
            FindInterceptPos();
            
            // intercept pos is sometimes NaN for some reason
            if (!Double.isNaN(interceptPos.x)) {
                RotateTowardsTarget();
            }
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
                        target = (Obj)o;
                    }
                }
            }
        }

        if (target == null) {
            for (Base o : objs) {
                if (o instanceof TurretObject) {
                    if (((TurretObject)o).ability.player != ability.player) {
                        if (Physics.circleInCircle(position.x, position.y, aquireRadius, o.position.x, o.position.y, o.radius)) {
                            target = (Obj)o;
                        }
                    }
                }
            }
        }
    }

    // http://danikgames.com/blog/how-to-intersect-a-moving-target-in-2d/
    void FindInterceptPos() {
        // find the vector AB
        double ABx = target.position.x - position.x;
        double ABy = target.position.y - position.y;
        
        // Normalize it
        final double ABmag = Math.sqrt(ABx * ABx + ABy * ABy);
        ABx /= ABmag;
        ABy /= ABmag;
        
        // Project u onto AB
        final double uDotAB = ABx * target.velocity.x + ABy * target.velocity.y;
        final double ujx = uDotAB * ABx;
        final double ujy = uDotAB * ABy;
        
        // subtract uj from u to get ui
        final double uix = target.velocity.x - ujx;
        final double uiy = target.velocity.y - ujy;
        
        // calculate the magnitude of vj
        final double viMag = Math.sqrt(uix * uix + uiy * uiy);
        final double vjMag = Math.sqrt(bulletSpeed * bulletSpeed - viMag * viMag);
        
        // get vj by multiplying it's magnitude with the unit vector AB
        final double vjx = ABx * vjMag;
        final double vjy = ABy * vjMag;
        
        // add vj and vi to get v
        interceptPos.x = position.x + vjx + uix;
        interceptPos.y = position.y + vjy + uiy;
    }


    void RotateTowardsTarget() {
        SetRotation(Math.atan2(interceptPos.y - position.y, interceptPos.x - position.x));
    }


    void CheckForFiring() {
        if (lastFired + fireCooldown < ability.player.game.tickStartTime) {
            FireAtTarget();
        }
    }


    void FireAtTarget() {
        new BlasterBullet(ability, FirePositionX(), FirePositionY(), GetRotation(), 6, bulletDamage, shieldDamageMultiplier, bulletColor, bulletSpeed, true);
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

        } else if (otherObject instanceof TurretObject) {
            Physics.resolveCollision(this, (Obj)otherObject);

        } else if (otherObject instanceof ShieldBubble) {
            Destroy();
        }
    }


    // position where bullets should spawn from
    public double FirePositionX() {
        return this.position.x + Math.cos(GetRotation()) * this.radius;
    }


    public double FirePositionY() {
        return this.position.y + Math.sin(GetRotation()) * this.radius;
    }


    public void TakeDamage(double damage, Player otherPlayer) {
        if (!game.isStarted) return;
        if (damage <= 0) return;
        
        health -= Math.min(health, damage);
        if (otherPlayer != null) {
            otherPlayer.AddDamageDealt(Math.min(health, damage));
        }

        needsUpdate = true;

        if (health <= 0) {
            Destroy();
        }
    }


    @Override
    public void Destroy() {
        super.Destroy();
        new Explosion(ability.player.game, position.x, position.y, radius*3, 20, 1, "0xff4444", ability.player);
        ((Turret)ability).turrets.remove(this);
    }


    @Override
    public JSONObject InitialData() {
        JSONObject json = super.InitialData();
        json.put("health", health);
        return json;
    }


    @Override
    public JSONObject UpdateData() {
        JSONObject json = super.UpdateData();
        json.put("health", health);
        return json;
    }


    public int compareTo(TurretObject o) {
        return id.compareTo(o.id);
    }
}