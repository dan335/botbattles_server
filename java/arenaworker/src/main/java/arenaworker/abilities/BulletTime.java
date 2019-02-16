package arenaworker.abilities;

import arenaworker.Player;

public class BulletTime extends Ability {
    
    public BulletTime(Player player, int abilityNum, String abilityType) {
        super(player, abilityNum, abilityType);
        interval = 5000L;
    }

    @Override
    public void Fire() {
        super.Fire();

        player.game.StartBulletTime();
    }
}