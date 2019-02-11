package arenaworker.projectiles;

import arenaworker.abilities.Blasters;

public class BlasterBullet extends Projectile {
    
    public BlasterBullet(Blasters blasters) {
        super(blasters);
        initialUpdateName = "blasterBulletInitial";
        updateName = "blasterBulletUpdate";
        destroyUpdateName = "blasterBulletDestroy";
        speed = 1;
        radius = 5;
        mass = 0.4;
        SendInitialToAll();
    }
}