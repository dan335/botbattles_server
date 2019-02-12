package arenaworker.abilityobjects;

import arenaworker.Base;
import arenaworker.Box;
import arenaworker.Obj;
import arenaworker.Obstacle;
import arenaworker.Player;
import arenaworker.abilities.Ability;
import arenaworker.abilities.GrenadeLauncher;
import arenaworker.lib.Physics;
import arenaworker.lib.Vector2;

public class Grenade extends AbilityObjectPhysics {

    public double speed = 0.1;
    public double damage = 10;
    public double shieldDamageMultiplier = 1;
    
    public Grenade(Ability ability, double rotation, double radius, double amountOfForce, double damage) {
        super(ability, ability.player.position.x, ability.player.position.y, radius, rotation, false);
        initialUpdateName = "grenadeInitial";
        updateName = "grenadeUpdate";
        destroyUpdateName = "grenadeDestroy";
        mass = 0.4;
        shieldDamageMultiplier = 1;
        this.damage = damage;
        forces = new Vector2(
            Math.cos(rotation) * amountOfForce,
            Math.sin(rotation) * amountOfForce
        );

        SendInitialToAll();
    }


    @Override
    public void Contact(Base otherObject) {
        if (otherObject instanceof Obstacle) {
            Physics.resolveCollision(this, (Obj)otherObject);
        } else if (otherObject instanceof Player) {
            if (ability.player != otherObject) {
                Physics.resolveCollision(this, (Obj)otherObject);
                Player player = (Player) otherObject;
                Explode();
            }
        } else if (otherObject instanceof Box) {
            Explode();
        }
    }


    public void Explode() {
        GrenadeLauncher launcher = (GrenadeLauncher) ability;
        launcher.interval = launcher.defaultInterval; 
        launcher.SendCooldownMessage();
        launcher.grenade = null;
        new Explosion(ability, position.x, position.y, 100, damage);
        Destroy();
    }
}