package arenaworker.abilityobjects;

import arenaworker.Base;
import arenaworker.Obj;
import arenaworker.Obstacle;
import arenaworker.Box;
import arenaworker.Player;
import arenaworker.abilities.Ability;
import arenaworker.lib.Physics;

public class BlasterBullet extends Projectile {
    
    public BlasterBullet(Ability ability, double rotation) {
        super(ability, ability.player.position.x, ability.player.position.y, 12, rotation, false);
        initialUpdateName = "blasterBulletInitial";
        updateName = "blasterBulletUpdate";
        destroyUpdateName = "blasterBulletDestroy";
        speed = 1;
        mass = 0.4;
        damage = 2;
        SendInitialToAll();
    }


    @Override
    public void Contact(Base otherObject) {
        if (otherObject instanceof Obstacle) {
            Physics.resolveCollision(this, (Obj)otherObject);
            Destroy();
        } else if (otherObject instanceof Player) {
            Physics.resolveCollision(this, (Obj)otherObject);
            Destroy();
        } else if (otherObject instanceof Box) {
            Destroy();
        }
    }
}