package arenaworker.abilityobjects;

import org.json.JSONObject;

import arenaworker.abilities.Ability;
import arenaworker.lib.Vector2;


public class Projectile extends AbilityObjectPhysics {

    public Ability ability;
    public double speed = 0.1;
    public double damage = 10;
    
    
    public Projectile(Ability ability, double x, double y, double radius, double rotation, boolean addToGrid) {
        super(ability, x, y, radius, rotation, addToGrid);
    }


    public void Tick() {
        velocity = new Vector2( Math.cos(rotation), Math.sin(rotation) ).scale(speed);
        SetPosition(position.add(velocity.scale(game.deltaTime)));
        SendUpdate();
    }
}