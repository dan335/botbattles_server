package arenaworker;

import org.json.JSONObject;

import arenaworker.lib.Vector2;

public class ObjRectangle extends Obj {

    public Vector2 scale = new Vector2(10, 10);

    public ObjRectangle(Game game) {
        super(game);
    }
    

    @Override
    public JSONObject UpdateData() {
        JSONObject json = super.UpdateData();
        json.put("scaleX", scale.x);
        json.put("scaleY", scale.y);
        return json;
    }
}