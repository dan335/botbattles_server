package arenaworker.abilityobjects;

import arenaworker.abilities.Ability;

public class BlasterBullet extends Projectile {
    
    public BlasterBullet(Ability ability, double rotation) {
        super(ability, ability.player.position.x, ability.player.position.y, 12, rotation);
        initialUpdateName = "blasterBulletInitial";
        updateName = "blasterBulletUpdate";
        destroyUpdateName = "blasterBulletDestroy";
        speed = 1;
        mass = 0.4;
        damage = 2;
        SendInitialToAll();
    }
}