package arenaworker.projectiles;

import arenaworker.abilities.*;
import arenaworker.Client;
import arenaworker.ObjCircle;

import org.json.JSONObject;
import java.util.UUID;

import arenaworker.lib.Vector2;
import java.util.Date;


public class Projectile extends ObjCircle {

    public Ability ability;
    public double speed = 0.1;
    
    
    public Projectile(Ability ability) {
        super(ability.player.game, ability.player.position.x, ability.player.position.y, 1);
        this.ability = ability;
        ability.projectiles.add(this);
        rotation = ability.player.rotation;
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