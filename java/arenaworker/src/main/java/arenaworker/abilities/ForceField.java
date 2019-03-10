package arenaworker.abilities;

import arenaworker.Player;
import arenaworker.abilityobjects.ShieldBubble;

public class ForceField extends Ability {

    ShieldBubble shieldBubble;
    double duration = 2000L;
    double lastCreated;
    
    public ForceField(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 5000L;
    }

    @Override
    public void Fire() {
        super.Fire();

        shieldBubble = new ShieldBubble(this);
        lastCreated = player.game.tickStartTime;
    }


    @Override
    public void Tick() {
        super.Tick();

        if (shieldBubble != null) {
            if (lastCreated + duration < player.game.tickStartTime) {
                shieldBubble.Destroy();
                shieldBubble = null;
            }
        }
    }


    @Override
    public void PlayerPositionChanged() {
        if (shieldBubble != null) {
            shieldBubble.SetPosition(player.position);
        }
    }
}