package arenaworker.abilityobjects;

import org.json.JSONObject;

import arenaworker.Obj;
import arenaworker.abilities.Ability;


public class AbilityObjectPhysics extends Obj {

    public Ability ability;
    public double speed = 0.1;
    public double damage = 10;
    public double shieldDamageMultiplier = 1;
    
    
    public AbilityObjectPhysics(Ability ability, double x, double y, double radius, double rotation, boolean addToGrid) {
        super(ability.player.game, x, y, radius, rotation, addToGrid);
        this.ability = ability;
        ability.abilityObjects.add(this);
    }


    // override to remove grid
    @Override
    public void SetPosition(double x, double y) {
        if (position.x != x || position.y != y) {
            position.x = x;
            position.y = y;
            needsUpdate = true;
        }
    }


    @Override
    public void Destroy() {
        super.Destroy();
        ability.abilityObjects.remove(this);
    }
}