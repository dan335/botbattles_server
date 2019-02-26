package arenaworker.abilities;

import arenaworker.Player;

public class BulletTime extends Ability {
    
    public BulletTime(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 8000L;
    }

    @Override
    public void BulletTimeEnded() {
        super.BulletTimeEnded();
        player.shipEngineSpeed = player.game.settings.shipEngineSpeed;
    }

    @Override
    public void Fire() {
        super.Fire();

        player.game.StartBulletTime();
        player.shipEngineSpeed = player.game.settings.shipEngineSpeed * 2;
    }
}