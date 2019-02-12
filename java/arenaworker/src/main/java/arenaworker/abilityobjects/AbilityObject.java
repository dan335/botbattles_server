package arenaworker.abilityobjects;

import org.json.JSONObject;

import arenaworker.Base;
import arenaworker.abilities.Ability;


public class AbilityObject extends Base {

    public Ability ability;
    public double speed = 0.1;
    public double damage = 10;
    public double shieldDamageMultiplier = 1;
    
    
    public AbilityObject(Ability ability, double x, double y, double radius, double rotation, boolean addToGrid) {
        super(ability.player.game, x, y, radius, rotation, addToGrid);
        this.ability = ability;
        ability.abilityObjects.add(this);
    }


    public void Tick() {
        SendUpdate();
    }


    @Override
    public void Destroy() {
        super.Destroy();
        ability.abilityObjects.remove(this);
    }
}