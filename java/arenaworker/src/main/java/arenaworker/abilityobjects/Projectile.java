package arenaworker.abilityobjects;

import org.json.JSONObject;

import arenaworker.Obj;
import arenaworker.abilities.Ability;
import arenaworker.lib.Vector2;


public class Projectile extends Obj {

    public Ability ability;
    public double speed = 0.1;
    public double damage = 10;
    
    
    public Projectile(Ability ability, double rotation) {
        super(ability.player.game, ability.player.position.x, ability.player.position.y, 1);
        this.ability = ability;
        ability.projectiles.add(this);
        this.rotation = rotation;
    }


    public void Tick() {
        // move projectile
        velocity = new Vector2( Math.cos(rotation), Math.sin(rotation) ).scale(speed);

        SetPosition(position.add(velocity.scale(game.deltaTime)));

        SendUpdate();
    }


    public void Destroy() {
        ability.projectiles.remove(this);
        
        JSONObject json = new JSONObject();
        json.put("t", destroyUpdateName);
        json.put("id", id);
        
        game.SendJsonToClients(json);
    }
}