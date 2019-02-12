package arenaworker;

import org.json.JSONObject;

// optimizations
// box cannot be rotated
// mass must always be 0 (infinite)

public class Box extends ObjRectangle {

    public Box(Game game, double x, double y, double scaleX, double scaleY) {
        super(game, x, y, scaleX, scaleY, true);
        game.boxes.add(this);
        this.mass = 0;
        initialUpdateName = "boxInitial";
        updateName = "boxUpdate";
        destroyUpdateName = "boxDestroy";
        SendInitialToAll();
    }



    public void Destroy() {
        game.boxes.remove(this);
        super.Destroy();
    }


    @Override
    public JSONObject InitialData() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("x", position.x);
        json.put("y", position.y);
        json.put("scaleX", scale.x);
        json.put("scaleY", scale.y);
        return json;
    }
}