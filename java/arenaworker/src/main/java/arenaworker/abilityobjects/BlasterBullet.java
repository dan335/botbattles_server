package arenaworker.abilityobjects;

import arenaworker.Base;
import arenaworker.Box;
import arenaworker.Obj;
import arenaworker.Obstacle;
import arenaworker.Player;
import arenaworker.abilities.Ability;
import arenaworker.lib.Physics;

public class BlasterBullet extends Projectile {
    
    public BlasterBullet(Ability ability, double rotation) {
        super(ability, ability.player.position.x, ability.player.position.y, 6, rotation, false);
        initialUpdateName = "blasterBulletInitial";
        updateName = "blasterBulletUpdate";
        destroyUpdateName = "blasterBulletDestroy";
        speed = 1;
        mass = 0.4;
        damage = 10;
        shieldDamageMultiplier = 1;
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
                player.ProjectileHit(this);
                Destroy();
            }
        } else if (otherObject instanceof Box) {
            Destroy();
        }
    }


    @Override
    public void Destroy() {
        super.Destroy();
        new Explosion(ability, position.x, position.y, radius * 2, 0);
    }
}