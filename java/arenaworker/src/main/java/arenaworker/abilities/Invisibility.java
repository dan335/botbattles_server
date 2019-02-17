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

        player.GoInvis(duration);
    }
}