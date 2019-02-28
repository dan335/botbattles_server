package arenaworker.abilities;

import arenaworker.Player;

public class Boost extends Ability {

    boolean isActive = false;
    long duration = 2000L;
    long start;
    
    public Boost(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 7000L;
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