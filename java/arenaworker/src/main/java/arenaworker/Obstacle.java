package arenaworker;

import org.json.JSONObject;

import arenaworker.lib.Physics;
import arenaworker.other.Explosion;

public class Obstacle extends Obj {

    double defaultMass;

    public Obstacle(Game game, double x, double y, double radius, double mass) {
        super(game, x, y, radius, 0, true);
        game.obstacles.add(this);
        initialUpdateName = "obstacleInitial";
        updateName = "obstacleUpdate";
        destroyUpdateName = "obstacleDestroy";
        this.defaultMass = mass;
        this.mass = 0;   // start un-movable; set mass when game starts
        SendInitialToAll();
    }


    public void GameStarted() {
        mass = defaultMass;
    }



    @Override
    public void Destroy() {
        game.obstacles.remove(this);
        super.Destroy();
        new Explosion(game, position.x, position.y, radius * 3, 50, 1, "0xff4444", null);
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


    @Override
    public void Contact(Base otherObject) {
        if (otherObject instanceof Obstacle) {
            Physics.resolveCollision(this, (Obj)otherObject);
        } else if (otherObject instanceof Player) {
            Physics.resolveCollision(this, (Obj)otherObject);
        } else if (otherObject instanceof Box) {
            Destroy();
        }
    }
}