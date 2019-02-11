package arenaworker;

import org.json.JSONObject;

public class Obstacle extends ObjCircle {

    public Obstacle(Game game, double x, double y, double radius) {
        super(game, x, y, radius);
        game.obstacles.add(this);
        initialUpdateName = "obstacleInitial";
        updateName = "obstacleUpdate";
        destroyUpdateName = "obstacleDestroy";
        game.grid.insert(this);
        SendInitialToAll();
    }



    @Override
    public void Destroy() {
        game.obstacles.remove(this);
        super.Destroy();
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