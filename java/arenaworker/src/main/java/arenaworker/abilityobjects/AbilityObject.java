package arenaworker.abilityobjects;

import arenaworker.Base;
import arenaworker.abilities.Ability;


public class AbilityObject extends Base {

    public Ability ability;
    
    
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