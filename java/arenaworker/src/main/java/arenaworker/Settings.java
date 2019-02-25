package arenaworker;

import org.json.JSONArray;
import org.json.JSONObject;

public class Settings {
    // how long after the 2nd player joins should we wait to start the game
    public int numAbilities = 3;
    public String[] defaultAbilityTypes = new String[]{"Blasters", "Shotgun", "GrenadeLauncher", "ForceField"};
    public long gameWaitToStartTimeMs = 1000L * 5L;
    public long tickIntervalMs = 16L;
    public long updateIntervalMs = 40L;
    public JSONObject defaultMap = new JSONObject();
    public long mapUpdateInterval = 1000L * 2L;
    public double mapShrinkPerInterval = 0.008;
    public double mapMinSize = 400;
    public int gridDivisions = 10;
    public double shipEngineSpeed = 0.075;
    public double drag = -0.2;
    public double wallWidth = 80;
    public long playerHealDelay = 1000L * 3L;
    public double playerHealPerInterval = 0.03;
    public long maxReplayTime = 1000L * 60L * 5L;
    public double maxHealth = 300;
    public double maxShield = 120;
    public double defaultRating = 1000;

    public Settings() {
        defaultMap.put("startSize", 1800);

        JSONArray obstacles = new JSONArray();

        int num = 10;
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

        num = 5;
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

        defaultMap.put("obstacles", obstacles);
    }
}