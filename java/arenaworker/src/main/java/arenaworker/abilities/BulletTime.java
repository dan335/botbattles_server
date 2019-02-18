package arenaworker.abilities;

import arenaworker.Player;

public class BulletTime extends Ability {
    
    public BulletTime(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 5000L;
    }

    @Override
    public void Fire() {
        super.Fire();

        player.game.StartBulletTime();
    }
}