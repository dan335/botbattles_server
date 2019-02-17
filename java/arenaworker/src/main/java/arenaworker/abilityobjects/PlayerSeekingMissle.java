package arenaworker.abilityobjects;

import java.util.Set;

import org.json.JSONObject;

import arenaworker.Base;
import arenaworker.Box;
import arenaworker.Obj;
import arenaworker.Obstacle;
import arenaworker.Player;
import arenaworker.abilities.Ability;
import arenaworker.lib.Physics;
import arenaworker.lib.Vector2;
import arenaworker.other.Explosion;

public class PlayerSeekingMissle extends AbilityObjectPhysics {

    String color;
    public double damage = 10;
    public double shieldDamageMultiplier = 1;
    double searchRadius = 600;
    double speed = 0.15;
    
    public PlayerSeekingMissle(Ability ability, double x, double y, double rotation, double radius, double damage, double shieldDamageMultiplier, String color) {
        super(ability, x, y, radius, rotation, false);
        initialUpdateName = "playerSeekingMissleInitial";
        updateName = "playerSeekingMissleUpdate";
        destroyUpdateName = "playerSeekingMissleDestroy";
        this.color = color;
        mass = 0.4;
        this.damage = damage;
        this.shieldDamageMultiplier = shieldDamageMultiplier;
        SendInitialToAll();
    }


    @Override
    public void Tick() {
        Player closestPlayer = null;
        double distance = 999999999;

        Set<Base> objs = ability.player.game.grid.retrieve(position, searchRadius);
        for (Base o : objs) {
            if (o instanceof Player) {
                if (o != ability.player) {
                    if (Physics.circleInCircle(position.x, position.y, searchRadius, o.position.x, o.position.y, o.radius)) {
                        double dist = position.subtract(o.position).length();
                        if (dist < distance) {
                            closestPlayer = (Player)o;
                            distance = dist;
                        }
                    }
                }
            }
        }

        if (closestPlayer == null) {
            forces = new Vector2(
                Math.cos(rotation) * speed,
                Math.sin(rotation) * speed
            );
        } else {
            Vector2 straight = new Vector2(
                Math.cos(rotation),
                Math.sin(rotation)
            );

            Vector2 towardsPlayer = closestPlayer.position.subtract(position).getNormalized();

            forces = straight.add(towardsPlayer.scale(0.08)).getNormalized().scale(speed);

            this.rotation = Math.atan2(forces.y, forces.x);
        }
         
        super.Tick();
    }


    @Override
    public void Contact(Base otherObject) {
        if (otherObject instanceof Obstacle) {
            Physics.resolveCollision(this, (Obj)otherObject);
            Destroy();
        } else if (otherObject instanceof Player) {
            if (ability.player != otherObject) {
                Physics.resolveCollision(this, (Obj)otherObject);
                Player player = (Player) otherObject;
                player.ProjectileHit(this);
                Destroy();
            }
        } else if (otherObject instanceof Box) {
            Destroy();
        } else if (otherObject instanceof ShieldBubble) {
            if (((ShieldBubble)otherObject).ability.player != ability.player) {
                Destroy();
            }
        }
    }


    @Override
    public void Destroy() {
        super.Destroy();
        new Explosion(ability.player.game, position.x, position.y, radius * 4, 0, 0, color);
    }


    @Override
    public JSONObject InitialData() {
        JSONObject json = super.InitialData();
        json.put("color", color);
        return json;
    }
}