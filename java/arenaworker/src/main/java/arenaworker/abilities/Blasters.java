package arenaworker.abilities;

import arenaworker.Player;
import arenaworker.abilityobjects.BlasterBullet;
import arenaworker.lib.Vector2;

public class Blasters extends Ability {

    boolean left = true;
    double fireOffset = 8;
    String color = "0xff4444";
    double damage = 15;
    double shieldDamageMultiplier = 1;
    
    public Blasters(Player player, int abilityNum) {
        super(player, abilityNum);
        cooldown = 250L;
    }

    @Override
    public void Fire() {
        super.Fire();

        //Vector2 pos = player.FirePosition();
        // if (left) {
        //     pos = new Vector2(
        //         player.position.x + Math.cos(player.rotation + Math.PI/2) * fireOffset,
        //         player.position.y + Math.sin(player.rotation + Math.PI/2) * fireOffset
        //         );
        //     left = false;
        // } else {
        //     pos = new Vector2(
        //         player.position.x + Math.cos(player.rotation - Math.PI/2) * fireOffset,
        //         player.position.y + Math.sin(player.rotation - Math.PI/2) * fireOffset
        //         );
        //     left = true;
        // }

        new BlasterBullet(this, player.FirePositionX(), player.FirePositionY(), player.rotation, 6, damage, shieldDamageMultiplier, color, 1.05, true);
    }
}