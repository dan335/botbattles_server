package arenaworker.abilities;

import arenaworker.Player;
import arenaworker.abilityobjects.PlayerSeekingMissle;

public class PlayerSeeker extends Ability {

    double angleInDegrees = 30;
    String color = "0xff4444";
    double damage = 25;
    
    public PlayerSeeker(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 1000L;
    }

    @Override
    public void Fire() {
        super.Fire();

        new PlayerSeekingMissle(this, player.FirePositionX(), player.FirePositionY(), player.GetRotation(), 8, damage, 1, color);
    }
}