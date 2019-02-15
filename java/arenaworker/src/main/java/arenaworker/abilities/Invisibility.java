package arenaworker.abilities;

import org.json.JSONObject;

import arenaworker.Player;

public class Invisibility extends Ability {

    boolean isInvisible = false;
    long duration = 1500L;
    long start;
    
    public Invisibility(Player player, int abilityNum, String abilityType) {
        super(player, abilityNum, abilityType);
        interval = 5000L;
    }

    @Override
    public void Fire() {
        super.Fire();

        isInvisible = true;
        start = player.game.tickStartTime;

        JSONObject json = new JSONObject();
        json.put("t", "goInvisible");
        json.put("shipId", player.id);
        player.game.SendJsonToClients(json);
    }

    @Override
    public void PlayerTookDamage() {
        if (isInvisible) {
            LoseInvisibility();
        }
    }

    @Override
    public void PlayerStartedAnAbility(Ability a) {
        if (isInvisible) {
            if (a != this) {
                LoseInvisibility();
            }
        }
    }

    @Override
    public void Tick() {
        super.Tick();

        if (isInvisible) {
            if (start + duration < player.game.tickStartTime) {
                LoseInvisibility();
            }
        }
    }

    void LoseInvisibility() {
        isInvisible = false;
        JSONObject json = new JSONObject();
        json.put("t", "goVisible");
        json.put("shipId", player.id);
        player.game.SendJsonToClients(json);
    }
}