package arenaworker.abilityobjects;

import java.util.Set;

import arenaworker.Base;
import arenaworker.Box;
import arenaworker.Obj;
import arenaworker.Obstacle;
import arenaworker.Player;
import arenaworker.abilities.Ability;
import arenaworker.lib.Physics;
import arenaworker.lib.Vector2;

public class Explosion extends AbilityObjectPhysics {

    public double speed = 0.1;
    public double damage = 10;
    public double shieldDamageMultiplier = 1;
    
    public Explosion(Ability ability, double x, double y, double radius, double damage) {
        super(ability, x, y, radius, 0, false);
        initialUpdateName = "explosionInitial";
        updateName = "explosionUpdate";
        destroyUpdateName = "explosionDestroy";
        this.damage = damage;
        shieldDamageMultiplier = 1;
        
        SendInitialToAll();

        Set<Base> objs = ability.player.game.grid.retrieve(new Vector2(x, y), radius);
        for (Base o : objs) {
            if (o instanceof Player) {
                ApplyDamage((Player)o);
                ApplyForce((Obj)o);
            } else if (o instanceof Obstacle) {
                ApplyForce((Obj)o);
            }
        }

        Destroy();
    }


    void ApplyDamage(Player player) {
        if (damage == 0) return;
        double distance = position.subtract(player.position).length();
        double percent = 1 - distance / radius * 0.5;
        player.TakeDamage(damage * percent, shieldDamageMultiplier);
    }

    void ApplyForce(Obj obj) {
        Physics.PositionalCorrection(this, obj);
    }

    @Override
    public void Tick() {
        //SendUpdate(); // no update needed
    }


    @Override
    public void Contact(Base otherObject) {
        
    }
}