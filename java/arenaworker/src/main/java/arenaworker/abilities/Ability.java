package arenaworker.abilities;

import arenaworker.Player;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import arenaworker.abilityobjects.*;


public class Ability {

    public Player player;
    long lastFired = 0L;
    public long interval = 1000L;
    boolean isOn = false;
    public Set<Projectile> projectiles = ConcurrentHashMap.newKeySet();

    public Ability(Player player) {
        this.player = player;
    }


    public void Tick() {
        if (isOn) {
            if (player.game.tickStartTime >= lastFired + interval) {
                Fire();
            }
        }

        for (Projectile p : projectiles) {
            p.Tick();
        }
    }


    public void Start() {
        isOn = true;
        if (player.game.tickStartTime >= lastFired + interval) {
            Fire();
        }
    }

    public void Stop() {
        isOn = false;
    }

    public void Fire() {
        lastFired = player.game.tickStartTime;
    }


    public void Destroy() {
        for (Projectile p : projectiles) {
            p.Destroy();
        }
    }


    public void PlayerPositionChanged() {

    }
}