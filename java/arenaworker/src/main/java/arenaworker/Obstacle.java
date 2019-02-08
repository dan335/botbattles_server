package arenaworker;

import org.json.JSONObject;

public class Obstacle extends ObjCircle {

    public Obstacle(Game game) {
        super(game);
        game.obstacles.add(this);

        SendInitialToAll("obstacleInitial");
    }


    @Override
    public void Tick() {
        super.Tick();
        SendUpdate("obstacleUpdate");
    }


    


    @Override
    public JSONObject InitialData() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("x", position.x);
        json.put("y", position.y);
        json.put("rotation", rotation);
        json.put("radius", radius);
        return json;
    }
}