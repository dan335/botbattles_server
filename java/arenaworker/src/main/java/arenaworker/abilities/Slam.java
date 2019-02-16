package arenaworker.abilities;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import arenaworker.Base;
import arenaworker.Player;
import arenaworker.lib.Physics;

public class Slam extends Ability {

    double radius = 120;
    long stunStart;
    boolean isStunned = false;
    long stunDuration = 1500L;
    Set<Player> stunnedPlayers = ConcurrentHashMap.newKeySet();
    
    public Slam(Player player, int abilityNum, String abilityType) {
        super(player, abilityNum, abilityType);
        interval = 5000L;
    }

    @Override
    public void Fire() {
        super.Fire();

        Set<Base> objs = player.game.grid.retrieve(player.position, player.radius);
        for (Base o : objs) {
            if (o instanceof Player) {
                if (Physics.circleInCircle(player.position.x, player.position.y, radius, o.position.x, o.position.y, o.radius)) {
                    if (o != player) {
                        Player p = (Player) o;
                        stunnedPlayers.add(p);
                        p.isStunned = true;

                        for (int i = 0; i < p.abilities.length; i++) {
                            if (p.abilities[i].isOn) {
                                p.abilities[i].Stop();
                            }
                        }
                    }
                }
            }
        }

        isStunned = true;
        stunStart = player.game.tickStartTime;

        JSONObject json = new JSONObject();
        json.put("t", "slamInitial");
        json.put("shipId", player.id);
        json.put("radius", radius);
        player.game.SendJsonToClients(json);
    }


    @Override
    public void Tick() {
        super.Tick();

        if (isStunned) {
            if (stunStart + stunDuration < player.game.tickStartTime) {
                for (Player p : stunnedPlayers) {
                    p.isStunned = false;
                }
                isStunned = false;
            }
        }
    }
}