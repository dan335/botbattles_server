package arenaworker.abilityobjects;

import arenaworker.Obj;
import arenaworker.abilities.Ability;


public class AbilityObjectPhysics extends Obj {

    public Ability ability;
    
    
    public AbilityObjectPhysics(Ability ability, double x, double y, double radius, double rotation, boolean addToGrid) {
        super(ability.player.game, x, y, radius, rotation, addToGrid);
        this.ability = ability;
        ability.abilityObjects.add(this);
    }


    @Override
    public void Destroy() {
        super.Destroy();
        ability.abilityObjects.remove(this);
    }
}