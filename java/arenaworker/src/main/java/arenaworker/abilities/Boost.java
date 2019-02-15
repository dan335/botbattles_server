package arenaworker.abilities;

import arenaworker.Player;
import arenaworker.lib.Vector2;

public class Boost extends Ability {

    boolean isActive = false;
    long duration = 2000L;
    long start;
    
    public Boost(Player player, int abilityNum, String abilityType) {
        super(player, abilityNum, abilityType);
        interval = 7000L;
    }

    @Override
    public void Fire() {
        super.Fire();

        isActive = true;
        start = player.game.tickStartTime;
        Activate();
    }

    @Override
    public void Tick() {
        if (isActive) {
            if (start + duration < player.game.tickStartTime) {
                isActive = false;
                Deactivate();
            }
        }
    }

    void Activate() {
        player.shipSpeedMultiplier += 1;
    }

    void Deactivate() {
        player.shipSpeedMultiplier -= 1;
    }
}