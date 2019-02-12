package arenaworker.abilities;

import arenaworker.Player;
import arenaworker.abilityobjects.Grenade;

public class GrenadeLauncher extends Ability {

    public Grenade grenade;
    double amountOfForce = 4;
    public long defaultInterval = 5000L;
    
    public GrenadeLauncher(Player player, int abilityNum) {
        super(player, abilityNum);
        interval = defaultInterval;
    }

    @Override
    public void Fire() {
        lastFired = player.game.tickStartTime;

        if (grenade == null) {
            grenade = new Grenade(this, player.rotation, 10, amountOfForce, 150);
            interval = 300L;
            SendCooldownMessage();
        } else {
            grenade.Explode();  
        }
    }
}