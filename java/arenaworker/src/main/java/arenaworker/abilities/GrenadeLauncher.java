package arenaworker.abilities;

import arenaworker.Player;
import arenaworker.abilityobjects.Grenade;

public class GrenadeLauncher extends Ability {

    public Grenade grenade;
    double amountOfForce = 5;
    public long defaultInterval = 5000L;
    double damage = 100;
    
    public GrenadeLauncher(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = defaultInterval;
    }

    @Override
    public void Fire() {
        lastFired = player.game.tickStartTime;

        if (grenade == null) {
            grenade = new Grenade(this, player.rotation, 12, amountOfForce, damage);
            cooldown = 300L;
            SendCooldownMessage();
        } else {
            grenade.Explode();  
        }
    }
}