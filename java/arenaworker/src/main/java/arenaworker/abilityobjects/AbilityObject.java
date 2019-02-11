package arenaworker.abilityobjects;

import org.json.JSONObject;

import arenaworker.Base;
import arenaworker.abilities.Ability;


public class AbilityObject extends Base {

    public Ability ability;
    public double speed = 0.1;
    public double damage = 10;
    
    
    public AbilityObject(Ability ability, double x, double y, double radius, double rotation) {
        super(ability.player.game, x, y, radius, rotation);
        this.ability = ability;
        ability.abilityObjects.add(this);
    }


    public void Tick() {
        SendUpdate();
    }


    @Override
    public void Destroy() {
        JSONObject json = new JSONObject();
        json.put("t", destroyUpdateName);
        json.put("id", id);
        game.SendJsonToClients(json);

        ability.abilityObjects.remove(this);
    }
}