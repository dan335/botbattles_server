package arenaworker;

import org.json.JSONArray;
import org.json.JSONObject;

public class Settings {
    // how long after the 2nd player joins should we wait to start the game
    public long gameWaitToStartTimeMs = 1000L * 5L;
    public long updateIntervalMs = 33L;
    public JSONObject defaultMap = new JSONObject();
    public long mapUpdateInterval = 1000L * 2L;
    public double mapShrinkPerSecond = 0.01f;
    public int gridDivisions = 10;
    public double shipEngineSpeed = 0.1;
    public double drag = -0.1;
    public double wallWidth = 80;

    public Settings() {
        defaultMap.put("startSize", 2000);

        JSONObject obstacle = new JSONObject();
        obstacle.put("x", 0);
        obstacle.put("y", 0);
        obstacle.put("radius", 50);
        obstacle.put("mass", 5);
        obstacle.put("shape", "circle");

        JSONObject obstacle2 = new JSONObject();
        obstacle2.put("x", -300);
        obstacle2.put("y", -300);
        obstacle2.put("radius", 50);
        obstacle2.put("mass", 0.5);
        obstacle2.put("shape", "circle");

        JSONObject rect = new JSONObject();
        rect.put("x", 300);
        rect.put("y", 300);
        rect.put("scaleX", 20);
        rect.put("scaleY", 200);
        rect.put("mass", 0);
        rect.put("shape", "rectangle");
        
        JSONArray obstacles = new JSONArray();
        obstacles.put(obstacle);
        obstacles.put(obstacle2);
        obstacles.put(rect);
        defaultMap.put("obstacles", obstacles);
    }
}