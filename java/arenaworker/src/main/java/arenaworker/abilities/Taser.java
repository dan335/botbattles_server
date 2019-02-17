package arenaworker.abilities;

import arenaworker.Player;
import arenaworker.abilityobjects.TaserBullet;

public class Taser extends Ability {

    long stunDuration = 1000L;
    String color = "0x888888";
    double size = 6;
    
    public Taser(Player player, int abilityNum, String abilityType) {
        super(player, abilityNum, abilityType);
        interval = 3000L;
    }

    @Override
    public void Fire() {
        super.Fire();

        new TaserBullet(this, player.position.x, player.position.y, player.rotation, size, stunDuration, color);
    }
}