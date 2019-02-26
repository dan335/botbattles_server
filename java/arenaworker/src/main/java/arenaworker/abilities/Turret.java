package arenaworker.abilities;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import arenaworker.Player;
import arenaworker.abilityobjects.TurretObject;
import arenaworker.lib.Vector2;

public class Turret extends Ability {

    public Set<TurretObject> turrets = ConcurrentHashMap.newKeySet();
    
    public Turret(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 13000L;
    }

    @Override
    public void Fire() {
        super.Fire();

        Vector2 pos = player.game.map.GetEmptyPos(40, player.position.x - 100, player.position.y - 100, player.position.x + 100, player.position.y + 100, 20);
        turrets.add(new TurretObject(this, player.rotation, pos.x, pos.y));
    }
}