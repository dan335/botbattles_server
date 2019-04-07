package arenaworker.abilities;

import java.util.concurrent.ConcurrentSkipListSet;

import arenaworker.Player;
import arenaworker.abilityobjects.TurretObject;
import arenaworker.lib.Vector2;

public class Turret extends Ability {

    public ConcurrentSkipListSet<TurretObject> turrets = new ConcurrentSkipListSet<TurretObject>();
    int maxTurrets = 4;
    
    public Turret(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 12000L;
    }

    @Override
    public void Fire() {
        if (turrets.size() > maxTurrets - 1) {
            turrets.last().Destroy();
        }
        super.Fire();

        Vector2 pos = player.game.map.GetEmptyPos(40, player.position.x - 100, player.position.y - 100, player.position.x + 100, player.position.y + 100, 20);
        if (pos != null) {
            turrets.add(new TurretObject(this, 0, pos.x, pos.y));
        }
    }
}