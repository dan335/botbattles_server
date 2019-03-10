package arenaworker.abilities;

import org.json.JSONObject;

import arenaworker.Player;

public class Resurrection extends Ability {

    boolean isInvisible = false;
    long duration = 100L;
    long start;
    
    public Resurrection(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 1000L * 90L;
    }


    @Override
    public void Init() {
        //Fire();
    }

    @Override
    public void Fire() {
        super.Fire();
        JSONObject json = new JSONObject();
        json.put("t", "reviveInitial");
        json.put("shipId", player.id);
        player.game.SendJsonToClients(json);
    }


    @Override
    public void Start() {
        isOn = true;
    }
}