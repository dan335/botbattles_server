package arenaworker;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class Settings {
    // how long after the 2nd player joins should we wait to start the game
    public final int numAbilities = 3;
    public final String[] defaultAbilityTypes = new String[]{"Blasters", "Shotgun", "GrenadeLauncher", "ForceField"};
    public final long gameWaitToStartTimeMs = 1000L * 5L;
    public final long tickIntervalMs = 16L;
    public final long updateIntervalMs = 50L;
    public JSONObject defaultMap = new JSONObject();
    public final long mapUpdateInterval = 1000L * 2L;
    public final double mapShrinkPerInterval = 0.008;
    public final double mapMinSize = 400;
    public final int gridDivisions = 10;
    public final double shipEngineSpeed = 0.035;
    public final double drag = -0.1;
    public final double wallWidth = 80;
    public final long playerHealDelay = 1000L * 3L;
    public final double playerHealPerInterval = 0.03;
    public final long maxReplayTime = 1000L * 60L * 3L;
    public final double maxHealth = 280;
    public final double maxShield = 100;
    public final double defaultRating = 1000;
    public final double playerDefaultMass = 1;
    public final Set<String> abilityNames = new HashSet<>(Arrays.asList(
        "Blasters",
        "BlastersHealth",
        "BlastersShield",
        "BombDropper",
        "Boost",
        "BulletTime",
        "Cannon",
        "Charger",
        "Dash",
        "Emp",
        "ForceField",
        "FreezeTrap",
        "GrenadeLauncher",
        "Heal",
        "MouseSeeker",
        "PlayerSeeker",
        "Rage",
        "Resurrection",
        "Shotgun",
        "Silencer",
        "Slam",
        "Slicer",
        "Smasher",
        "StunGun",
        "Teleport",
        "Turret",
        "Vacuum",
        "VortexLauncher"
    ));

    public Settings() {
        defaultMap.put("startSize", 1800);

        JSONArray obstacles = new JSONArray();

        int num = 8;
        double radius = 600;
        for (int i = 0; i < num; i++) {
            double angle = 2 * i * Math.PI / num ;
            double x = Math.cos(angle) * radius;
            double y = Math.sin(angle) * radius;
            JSONObject obstacle = new JSONObject();
            obstacle.put("x", x);
            obstacle.put("y", y);
            obstacle.put("radius", 70);
            obstacle.put("mass", 5);
            obstacle.put("shape", "circle");    // not used?
            obstacles.put(obstacle);
        }

        num = 6;
        radius = 300;
        for (int i = 0; i < num; i++) {
            double angle = 2 * i * Math.PI / num ;
            double x = Math.cos(angle) * radius;
            double y = Math.sin(angle) * radius;
            JSONObject obstacle = new JSONObject();
            obstacle.put("x", x);
            obstacle.put("y", y);
            obstacle.put("radius", 50);
            obstacle.put("mass", 1);
            obstacle.put("shape", "circle");    // not used?
            obstacles.put(obstacle);
        }

        JSONObject obstacle = new JSONObject();
        obstacle.put("x", 0);
        obstacle.put("y", 0);
        obstacle.put("radius", 80);
        obstacle.put("mass", 10);
        obstacle.put("shape", "circle");    // not used?
        obstacles.put(obstacle);

        defaultMap.put("obstacles", obstacles);
    }
}