package arenaworker.other;

import java.util.Set;

import org.json.JSONObject;

import arenaworker.Base;
import arenaworker.Game;
import arenaworker.Obj;
import arenaworker.Obstacle;
import arenaworker.Player;
import arenaworker.abilityobjects.Grenade;
import arenaworker.lib.Physics;
import arenaworker.lib.Vector2;

public class Explosion extends Obj {

    public double speed = 0.1;
    public double damage = 10;
    public double shieldDamageMultiplier = 1;
    double forceToApplyToObjects;
    String color;
    Player owner;
    
    public Explosion(Game game, double x, double y, double radius, double damage, double forceToApplyToObjects, String color, Player owner) {
        super(game, x, y, radius, 0, false);
        initialUpdateName = "explosionInitial";
        updateName = "explosionUpdate";
        destroyUpdateName = "explosionDestroy";
        this.damage = damage;
        shieldDamageMultiplier = 1;
        game.other.add(this);
        this.forceToApplyToObjects = forceToApplyToObjects;
        this.color = color;
        this.owner = owner;
        
        SendInitialToAll();

        Set<Base> objs = game.grid.retrieve(new Vector2(x, y), radius);
        for (Base o : objs) {
            if (o instanceof Player || o instanceof Obstacle) {
                if (Physics.circleInCircle(position.x, position.y, radius, o.position.x, o.position.y, o.radius)) {
                    if (o instanceof Player) {
                        ApplyDamage((Player)o);
                        ApplyForce((Obj)o);
                    } else if (o instanceof Obstacle) {
                        ApplyForce((Obj)o);
                    }
                }
            }
        }

        Destroy();
    }


    void ApplyDamage(Player player) {
        if (damage == 0) return;
        double distance = position.copy().subtract(player.position).length();
        double percent = 1 - distance / radius * 0.5;
        player.TakeDamage(damage * percent, shieldDamageMultiplier, owner);
    }

    void ApplyForce(Obj obj) {
        if (forceToApplyToObjects == 0) return;
        double distance = position.copy().subtract(obj.position).length() - obj.radius;
        double percent = 1 - distance / radius;
        obj.forces.add(obj.position.copy().subtract(position).normalize().scale(percent).scale(forceToApplyToObjects));
    }

    @Override
    public void Tick() {
        //SendUpdate(); // no update needed
    }


    @Override
    public void Contact(Base otherObject) {
        
    }


    @Override
    public void Destroy() {
        game.other.remove(this);
    }

    @Override
    public void SendUpdate() {
    }

    @Override
    public JSONObject InitialData() {
        JSONObject json = super.InitialData();
        json.put("color", color);
        return json;
    }
}