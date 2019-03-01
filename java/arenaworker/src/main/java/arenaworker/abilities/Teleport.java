package arenaworker.abilities;

import org.json.JSONObject;

import arenaworker.Player;
import arenaworker.lib.Vector2;

public class Teleport extends Ability {

    double distance = 600;
    double searchRadius = 30;
    int tries = 6;
    
    public Teleport(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 4000L;
    }

    @Override
    public void Fire() {
        super.Fire();

        Vector2 emptyPos;

        if (Vector2.distance(player.position, player.mousePosition) <= distance) {
            emptyPos = player.game.map.GetEmptyPos(player.radius, player.mousePosition.x-searchRadius, player.mousePosition.y-searchRadius, player.mousePosition.x+searchRadius, player.mousePosition.y+searchRadius, tries);
        } else {
            Vector2 teleportPos = new Vector2(
                player.position.x + Math.cos(player.rotation) * distance,
                player.position.y + Math.sin(player.rotation) * distance
            );

            emptyPos = player.game.map.GetEmptyPos(player.radius, teleportPos.x-searchRadius, teleportPos.y-searchRadius, teleportPos.x+searchRadius, teleportPos.y+searchRadius, tries);
        }
        
        if (emptyPos != null) {
            // make sure it's not outside map
            if (
                emptyPos.x <= -player.game.map.size/2 ||
                emptyPos.x >= player.game.map.size/2 ||
                emptyPos.y <= -player.game.map.size/2 ||
                emptyPos.y >= player.game.map.size/2
            ) {
                return;
            }

            player.SetPosition(emptyPos);
            player.teleportToNextPosition = true;

            JSONObject json = new JSONObject();
            json.put("t", "teleportInitial");
            json.put("shipId", player.id);
            player.game.SendJsonToClients(json);
        }
    }
}