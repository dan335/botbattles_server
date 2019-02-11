package arenaworker.abilityobjects;

import arenaworker.abilities.Ability;
import arenaworker.abilities.Blasters;

public class BlasterBullet extends Projectile {
    
    public BlasterBullet(Ability blasters, double rotation) {
        super(blasters, rotation);
        initialUpdateName = "blasterBulletInitial";
        updateName = "blasterBulletUpdate";
        destroyUpdateName = "blasterBulletDestroy";
        speed = 1;
        radius = 5;
        mass = 0.4;
        damage = 2;
        SendInitialToAll();
    }
}