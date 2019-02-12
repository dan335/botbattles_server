package arenaworker.abilityobjects;

import arenaworker.Base;
import arenaworker.abilities.Ability;

public class ShieldBubble extends Base {
    
    public ShieldBubble(Ability ability) {
        super(ability.player.game, ability.player.position.x, ability.player.position.y, ability.player.radius + 15, 0, false);
        initialUpdateName = "shieldBubbleInitial";
        updateName = "shieldBubbleUpdate";
        destroyUpdateName = "shieldBubbleDestroy";
        SendInitialToAll();
    }
}