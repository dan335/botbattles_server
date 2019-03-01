package arenaworker.abilities;

import org.json.JSONObject;

import arenaworker.Player;

public class BulletTime extends Ability {
    
    public BulletTime(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 11000L;
    }

    @Override
    public void BulletTimeEnded() {
        super.BulletTimeEnded();
        player.shipEngineSpeed = player.game.settings.shipEngineSpeed;
    }

    @Override
    public void Fire() {
        super.Fire();

        player.game.StartBulletTime();
        player.shipEngineSpeed = player.game.settings.shipEngineSpeed * 2;

        JSONObject json = new JSONObject();
        json.put("t", "bulletTimeInitial");
        json.put("shipId", player.id);
        player.game.SendJsonToClients(json);
    }
}