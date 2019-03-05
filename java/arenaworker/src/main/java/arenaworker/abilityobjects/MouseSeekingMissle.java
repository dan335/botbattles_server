package arenaworker.abilityobjects;

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

public class MouseSeekingMissle extends AbilityObjectPhysics {

    String color;
    public double damage = 45;
    public double shieldDamageMultiplier = 1;
    double searchRadius = 600;
    double speed = 0.075;
    long lifetime = 1000L * 10L;
    long created;
    
    public MouseSeekingMissle(Ability ability, double x, double y, double rotation, double radius, double damage, double shieldDamageMultiplier, String color) {
        super(ability, x, y, radius, rotation, false);
        initialUpdateName = "playerSeekingMissleInitial";
        updateName = "playerSeekingMissleUpdate";
        destroyUpdateName = "playerSeekingMissleDestroy";
        this.color = color;
        mass = 0.4;
        this.damage = damage;
        this.shieldDamageMultiplier = shieldDamageMultiplier;
        created = ability.player.game.tickStartTime;
        SendInitialToAll();
    }


    @Override
    public void Tick() {
        if (created + lifetime < ability.player.game.tickStartTime) {
            Destroy();
            return;
        }

        if (ability.player.mousePosition == null) {
            forces = new Vector2(
                Math.cos(GetRotation()) * speed,
                Math.sin(GetRotation()) * speed
            );
        } else {
            Vector2 straight = new Vector2(
                Math.cos(GetRotation()),
                Math.sin(GetRotation())
            );

            Vector2 towardsMouse = ability.player.mousePosition.subtract(position).getNormalized();

            towardsMouse.scale(0.1);

            forces = straight.add(towardsMouse).normalize().scale(speed);

            SetRotation(Math.atan2(forces.y, forces.x));
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
                player.TakeDamage(damage, shieldDamageMultiplier, ability.player);
                Destroy();
            }

        } else if (otherObject instanceof Box) {
            Destroy();

        } else if (otherObject instanceof ShieldBubble) {
            if (((ShieldBubble)otherObject).ability.player != ability.player) {
                Destroy();
            }

        } else if (otherObject instanceof TurretObject) {
            TurretObject turret = (TurretObject)otherObject;
            if (turret.ability.player != ability.player) {
                turret.TakeDamage(damage, ability.player);
                Destroy();
            }
            
        }
    }


    @Override
    public void Destroy() {
        super.Destroy();
        new Explosion(ability.player.game, position.x, position.y, radius * 4, 0, 0, color, ability.player);
    }


    @Override
    public JSONObject InitialData() {
        JSONObject json = super.InitialData();
        json.put("color", color);
        return json;
    }
}