package arenaworker.abilityobjects;

import org.json.JSONObject;

import arenaworker.Base;
import arenaworker.Box;
import arenaworker.Obj;
import arenaworker.Obstacle;
import arenaworker.Player;
import arenaworker.abilities.Ability;
import arenaworker.lib.Physics;
import arenaworker.other.Explosion;

public class StunGunBullet extends Projectile {

    String color;
    long stunDuration;
    
    public StunGunBullet(Ability ability, double x, double y, double rotation, double radius, long stunDuration, String color) {
        super(ability, x, y, radius, rotation, false);
        initialUpdateName = "stunGunBulletInitial";
        updateName = "stunGunBulletUpdate";
        destroyUpdateName = "stunGunBulletDestroy";
        speed = 1;
        mass = 0.4;
        this.stunDuration = stunDuration;
        this.color = color;
        SendInitialToAll();
    }


    @Override
    public void Contact(Base otherObject) {
        if (otherObject instanceof Obstacle) {
            Physics.resolveCollision(this, (Obj)otherObject);
            Destroy();
        } else if (otherObject instanceof Player) {
            if (ability.player != otherObject) {
                Physics.resolveCollision(this, (Obj)otherObject);
                Player player = (Player) otherObject;
                player.Stun(stunDuration);
                Destroy();
            }
        } else if (otherObject instanceof Box) {
            Destroy();
        } else if (otherObject instanceof ShieldBubble) {
            if (((ShieldBubble)otherObject).ability.player != ability.player) {
                Destroy();
            }
        }
    }


    @Override
    public void Destroy() {
        super.Destroy();
        new Explosion(ability.player.game, position.x, position.y, radius * 4, 0, 0, color, ability.player);
    }


    @Override
    public JSONObject InitialData() {
        JSONObject json = super.InitialData();
        json.put("color", color);
        return json;
    }
}