package arenaworker;

import org.json.JSONObject;

// optimizations
// box cannot be rotated
// mass must always be 0 (infinite)

public class Box extends ObjRectangle {

    public Box(Game game) {
        super(game);
        game.boxes.add(this);
        this.mass = 0;

        SendInitialToAll("boxInitial");
    }


    @Override
    public void Tick() {
        super.Tick();
        SendUpdate("boxUpdate");
    }


    public void Destroy() {
        game.boxes.remove(this);

        JSONObject json = new JSONObject();
        json.put("t", "boxDestroy");
        json.put("id", id);
        
        for (Client c : game.clients) {
            c.SendJson(json.toString());
        }
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