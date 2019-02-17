package arenaworker.abilities;

import arenaworker.Player;
import arenaworker.abilityobjects.PlayerSeekingMissle;

public class PlayerSeeker extends Ability {

    int numBullets = 10;
    double angleInDegrees = 30;
    String color = "0xff4444";
    
    public PlayerSeeker(Player player, int abilityNum, String abilityType) {
        super(player, abilityNum, abilityType);
        interval = 1000L;
    }

    @Override
    public void Fire() {
        super.Fire();

        new PlayerSeekingMissle(this, player.position.x, player.position.y, player.rotation, 8, 30, 1, color);
    }
}