package arenaworker.abilityobjects;

import org.json.JSONObject;

import arenaworker.abilities.Ability;
import arenaworker.lib.Vector2;


public class Projectile extends AbilityObjectPhysics {

    public double speed = 0.1;
    public double damage = 10;
    public double shieldDamageMultiplier = 1;
    
    public Projectile(Ability ability, double x, double y, double radius, double rotation, boolean addToGrid) {
        super(ability, x, y, radius, rotation, addToGrid);
    }


    public void Tick() {
        velocity.x = Math.cos(rotation) * speed;
        velocity.y = Math.sin(rotation) * speed;
        
        SetPosition(position.copy().add(velocity.copy().scale(game.deltaTime)));
        SendUpdate();
    }
}