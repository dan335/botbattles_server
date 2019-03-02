package arenaworker.abilities;

import arenaworker.Player;
import arenaworker.abilityobjects.VortexGrenade;

public class VortexLauncher extends Ability {

    public VortexGrenade grenade;
    double amountOfForce = 2.5;
    public long defaultInterval = 5000L;
    
    public VortexLauncher(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = defaultInterval;
    }

    @Override
    public void Fire() {
        lastFired = player.game.tickStartTime;

        if (grenade == null) {
            grenade = new VortexGrenade(this, player.rotation, 12, amountOfForce);
            cooldown = 300L;
            SendCooldownMessage();
        } else {
            grenade.Explode();  
        }
    }
}