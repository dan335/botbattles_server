package arenaworker.abilityobjects;

import arenaworker.Base;
import arenaworker.Player;
import arenaworker.abilities.Ability;
import arenaworker.lib.Vector2;


public class Projectile extends AbilityObjectPhysics {

    public double speed = 0.1;
    public double damage = 10;
    public double shieldDamageMultiplier = 1;
    public Player ricochetedFrom = null;
    
    public Projectile(Ability ability, double x, double y, double radius, double rotation, boolean addToGrid) {
        super(ability, x, y, radius, rotation, addToGrid);
    }


    public void Tick() {
        velocity.x = Math.cos(GetRotation()) * speed;
        velocity.y = Math.sin(GetRotation()) * speed;
        
        SetPosition(position.copy().add(velocity.copy().scale(game.deltaTime)));
        SendUpdate();
    }


    public void Ricochet(Base object) {
        Vector2 vec = position.copy().subtract(object.position);
        double angle = Math.atan2(vec.y, vec.x);
        double diff = GetRotation() - Math.PI - angle;
        SetRotation(GetRotation() + Math.PI - diff);
        //teleportToNextPosition = true;
    }
}