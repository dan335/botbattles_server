package arenaworker.abilities;

import arenaworker.Player;
import arenaworker.abilityobjects.StunGunBullet;

public class StunGun extends Ability {

    long stunDuration = 1500L;
    String color = "0x888888";
    double size = 15;
    
    public StunGun(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 2000L;
    }

    @Override
    public void Fire() {
        super.Fire();

        new StunGunBullet(this, player.FirePositionX(), player.FirePositionY(), player.rotation, size, stunDuration, color);
    }
}