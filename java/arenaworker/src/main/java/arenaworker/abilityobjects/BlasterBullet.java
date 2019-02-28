package arenaworker.abilityobjects;

import org.json.JSONObject;

import arenaworker.Base;
import arenaworker.Box;
import arenaworker.Obj;
import arenaworker.Obstacle;
import arenaworker.Player;
import arenaworker.abilities.Ability;
import arenaworker.lib.Physics;
import arenaworker.other.Explosion;

public class BlasterBullet extends Projectile {

    String color;
    boolean playSound;
    
    public BlasterBullet(Ability ability, double x, double y, double rotation, double radius, double damage, double shieldDamageMultiplier, String color, double speed, boolean playSound) {
        super(ability, x, y, radius, rotation, false);
        initialUpdateName = "blasterBulletInitial";
        updateName = "blasterBulletUpdate";
        destroyUpdateName = "blasterBulletDestroy";
        this.color = color;
        this.speed = speed;
        mass = 0.4;
        this.damage = damage;
        this.shieldDamageMultiplier = shieldDamageMultiplier;
        this.playSound = playSound;
        SendInitialToAll();
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
        json.put("playSound", playSound);
        return json;
    }
}