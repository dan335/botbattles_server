package arenaworker.abilityobjects;

import arenaworker.abilities.Ability;

public class Mine extends Grenade {

    public Mine(Ability ability, double rotation, double radius, double amountOfForce, double damage) {
        super(ability, rotation, radius, amountOfForce, damage, true);

        initialUpdateName = "mineInitial";
        updateName = "mineUpdate";
        destroyUpdateName = "mineDestroy";

        SendInitialToAll();
    }
}