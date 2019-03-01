package arenaworker.abilities;

import arenaworker.Player;

public class Heal extends Ability {

    long stunDuration = 3000L;
    long stunStart;
    double healAmount = 150;
    boolean isCharging = false;
    
    public Heal(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 10000L;
    }


    @Override
    public void Tick() {
        super.Tick();

        if (isCharging && stunStart + stunDuration < player.game.tickStartTime) {
            player.StunEnd();
            player.health += healAmount;
            if (player.health > player.game.settings.maxHealth) {
                player.health = player.game.settings.maxHealth;
            }
            player.needsUpdate = true;
            isCharging = false;
        }
    }

    @Override
    public void Fire() {
        super.Fire();

        player.Stun(1000L * 60L * 60L);
        stunStart = player.game.tickStartTime;
        isCharging = true;
    }
}