package arenaworker.abilityobjects;

import org.json.JSONObject;

import arenaworker.Base;
import arenaworker.Obstacle;
import arenaworker.Player;
import arenaworker.abilities.Ability;

public class ShieldBubble extends AbilityObject {
    
    public ShieldBubble(Ability ability) {
        super(ability, ability.player.position.x, ability.player.position.y, ability.player.radius + 20, 0, false);
        initialUpdateName = "shieldBubbleInitial";
        updateName = "shieldBubbleUpdate";
        destroyUpdateName = "shieldBubbleDestroy";
        SendInitialToAll();
    }


    @Override
    public void Tick() {
        // don't need updates for Shield Bubble, it just follows player
        //SendUpdate();
    }


    @Override
    public JSONObject InitialData() {
        JSONObject json = super.InitialData();
        json.put("shipId", ability.player.id);
        return json;
    }


    @Override
    public void Contact(Base otherObject) {
        if (otherObject instanceof Projectile) {
            Projectile projectile = (Projectile)otherObject;
            if (projectile.ability.player != ability.player) {
                otherObject.Destroy();
            }
        }
    }
}