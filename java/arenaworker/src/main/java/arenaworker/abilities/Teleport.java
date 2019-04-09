package arenaworker.abilities;

import org.json.JSONObject;

import arenaworker.Player;
import arenaworker.lib.Vector2;

public class Teleport extends Ability {

    double distance = 600;
    double searchRadius = 30;
    int tries = 6;
    boolean isWaitingToTeleport = false;
    Vector2 telportToPos = null;
    long teleportDelay = 500L;
    long teleportStart;
    long defaultCooldown = 4000L;
    
    public Teleport(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = defaultCooldown;
    }


    @Override
    public void Fire() {
        Vector2 emptyPos;

        if (Vector2.distance(player.position, player.mousePosition) <= distance) {
            double testSize = 1;
            emptyPos = player.game.map.GetEmptyPos(player.radius, player.mousePosition.x-testSize, player.mousePosition.y-testSize, player.mousePosition.x+testSize, player.mousePosition.y+testSize, 1);

            if (emptyPos == null) {
                emptyPos = player.game.map.GetEmptyPos(player.radius, player.mousePosition.x-searchRadius, player.mousePosition.y-searchRadius, player.mousePosition.x+searchRadius, player.mousePosition.y+searchRadius, tries);
            }
            
        } else {
            Vector2 teleportPos = new Vector2(
                player.position.x + Math.cos(player.GetRotation()) * distance,
                player.position.y + Math.sin(player.GetRotation()) * distance
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

            player.Freeze(teleportDelay);

            telportToPos = emptyPos.copy();
            isWaitingToTeleport = true;
            teleportStart = player.game.tickStartTime;

            JSONObject json = new JSONObject();
            json.put("t", "teleportTelegraph");
            json.put("shipId", player.id);
            json.put("x", telportToPos.x);
            json.put("y", telportToPos.y);
            json.put("radius", player.radius);
            json.put("timeout", teleportDelay);
            player.game.SendJsonToClients(json);

            cooldown = teleportDelay;
            super.Fire();
        }
    }


    @Override
    public void Tick() {
        if (isWaitingToTeleport) {
            if (teleportStart + teleportDelay < player.game.tickStartTime) {
                DoTeleport();
                isWaitingToTeleport = false;
            }
        }

        super.Tick();
    }


    public void DoTeleport() {
        if (telportToPos == null) return;

        // make sure new position is empty
        Vector2 emptyPos = player.game.map.GetEmptyPos(player.radius, telportToPos.x-1, telportToPos.y-1, telportToPos.x+1, telportToPos.y+1, 1);

        // if it's not empty choose another
        if (emptyPos == null) {
            emptyPos = player.game.map.GetEmptyPos(player.radius, telportToPos.x-searchRadius, telportToPos.y-searchRadius, telportToPos.x+searchRadius, telportToPos.y+searchRadius, tries);
        }

        if (emptyPos == null) return;

        JSONObject json = new JSONObject();
        json.put("t", "teleportInitial");
        json.put("shipId", player.id);
        json.put("x", player.position.x);
        json.put("y", player.position.y);
        json.put("radius", player.radius);
        player.game.SendJsonToClients(json);

        player.SetPosition(telportToPos);
        player.teleportToNextPosition = true;

        cooldown = defaultCooldown;
        super.Fire();
    }
}