package arenaworker.abilities;

import arenaworker.Player;
import arenaworker.abilityobjects.BlasterBullet;
import arenaworker.lib.Vector2;

public class Blasters extends Ability {

    boolean left = true;
    double fireOffset = 10;
    String color = "0xff4444";
    double damage = 20;
    double shieldDamageMultiplier = 1;
    
    public Blasters(Player player, int abilityNum, String abilityType) {
        super(player, abilityNum, abilityType);
        interval = 300L;
    }

    @Override
    public void Fire() {
        super.Fire();

        Vector2 pos;
        if (left) {
            pos = new Vector2(
                player.position.x + Math.cos(player.rotation + Math.PI/2) * fireOffset,
                player.position.y + Math.sin(player.rotation + Math.PI/2) * fireOffset
                );
            left = false;
        } else {
            pos = new Vector2(
                player.position.x + Math.cos(player.rotation - Math.PI/2) * fireOffset,
                player.position.y + Math.sin(player.rotation - Math.PI/2) * fireOffset
                );
            left = true;
        }

        new BlasterBullet(this, pos.x, pos.y, player.rotation, 6, damage, shieldDamageMultiplier, color);
    }
}