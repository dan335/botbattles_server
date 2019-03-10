package arenaworker.abilityobjects;

import arenaworker.abilities.Ability;

public class Mine extends Grenade {

    public Mine(Ability ability, double rotation, double radius, double amountOfForce, double damage, String color) {
        super(ability, rotation, radius, amountOfForce, damage, false, color);

        initialUpdateName = "mineInitial";
        updateName = "mineUpdate";
        destroyUpdateName = "mineDestroy";

        SendInitialToAll();
    }
}