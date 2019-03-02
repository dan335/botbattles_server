package arenaworker;

import org.json.JSONArray;
import org.json.JSONObject;

public class Settings {
    // how long after the 2nd player joins should we wait to start the game
    public int numAbilities = 3;
    public String[] defaultAbilityTypes = new String[]{"Blasters", "Shotgun", "GrenadeLauncher", "ForceField"};
    public long gameWaitToStartTimeMs = 1000L * 5L;
    public long tickIntervalMs = 16L;
    public long updateIntervalMs = 32L;
    public JSONObject defaultMap = new JSONObject();
    public long mapUpdateInterval = 1000L * 2L;
    public double mapShrinkPerInterval = 0.008;
    public double mapMinSize = 400;
    public int gridDivisions = 10;
    public double shipEngineSpeed = 0.035;
    public double drag = -0.1;
    public double wallWidth = 80;
    public long playerHealDelay = 1000L * 3L;
    public double playerHealPerInterval = 0.03;
    public long maxReplayTime = 1000L * 60L * 5L;
    public double maxHealth = 280;
    public double maxShield = 100;
    public double defaultRating = 1000;
    public double playerDefaultMass = 1;

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