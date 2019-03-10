package arenaworker.abilityobjects;

import org.json.JSONObject;

import arenaworker.Base;
import arenaworker.Box;
import arenaworker.Obj;
import arenaworker.Obstacle;
import arenaworker.Player;
import arenaworker.abilities.Ability;
import arenaworker.abilities.FreezeTrap;
import arenaworker.lib.Physics;

public class FreezeTrapGrenade extends AbilityObjectPhysics implements Comparable<FreezeTrapGrenade> {

    public double speed = 0.1;
    public double damage = 10;
    public double shieldDamageMultiplier = 1;
    long stunDuration = 2000L;
    double vortexRadius = 250;
    String color;
    
    public FreezeTrapGrenade(Ability ability, double rotation, double radius, String color) {
        super(ability, ability.player.position.x, ability.player.position.y, radius, rotation, true);
        initialUpdateName = "mineInitial";
        updateName = "mineUpdate";
        destroyUpdateName = "mineDestroy";
        mass = 0.6;
        this.color = color;
        SendInitialToAll();
    }


    @Override
    public void Contact(Base otherObject) {
        if (otherObject instanceof Obstacle) {

            Physics.resolveCollision(this, (Obj)otherObject);

        } else if (otherObject instanceof ShieldBubble) {

            if (((ShieldBubble)otherObject).ability.player != ability.player) {
                Destroy();
            }

        } else if (otherObject instanceof Player) {

            if (ability.player != otherObject) {
                ((Player)otherObject).Freeze(stunDuration);
                Destroy();
            }

        } else if (otherObject instanceof TurretObject) {

            Destroy();

        } else if (otherObject instanceof Box) {

            Destroy();
            
        }
    }

    @Override
    public void Destroy() {
        if (ability instanceof FreezeTrap) {
            ((FreezeTrap)ability).traps.remove(this);
        }
        super.Destroy();
    }


    public int compareTo(FreezeTrapGrenade o) {
        return id.compareTo(o.id);
    }

    @Override
    public JSONObject InitialData() {
        JSONObject json = super.InitialData();
        json.put("color", color);
        return json;
    }
}