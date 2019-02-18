package arenaworker.abilities;

import arenaworker.Player;

public class Resurrection extends Ability {

    boolean isInvisible = false;
    long duration = 100L;
    long start;
    
    public Resurrection(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 1000L * 60L;
    }


    @Override
    public void Init() {
        Fire();
    }


    @Override
    public void Start() {
        isOn = true;
    }
}