package arenaworker.abilities;

import arenaworker.Player;
import arenaworker.lib.Vector2;

public class Teleport extends Ability {

    double distance = 400;
    double searchRadius = 80;
    
    public Teleport(Player player, int abilityNum) {
        super(player, abilityNum);
        interval = 5000L;
    }

    @Override
    public void Fire() {
        super.Fire();
        
        Vector2 teleportPos = new Vector2(
            player.position.x + Math.cos(player.rotation) * distance,
            player.position.y + Math.sin(player.rotation) * distance
        );

        // Vector2 teleportPos = player.forces.getNormalized().scale(distance);

        Vector2 emptyPos = player.game.map.GetEmptyPos(player.radius * 2, teleportPos.x-searchRadius, teleportPos.y-searchRadius, teleportPos.x+searchRadius, teleportPos.y+searchRadius, 20);
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
        }
    }
}