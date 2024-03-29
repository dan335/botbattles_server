package arenaworker;

import org.json.JSONObject;

import arenaworker.lib.Vector2;

public class ObjRectangle extends Obj {

    public Vector2 scale = new Vector2(10, 10);

    public ObjRectangle(Game game, double x, double y, double scaleX, double scaleY, boolean addToGrid) {
        super(game, x, y, 0, 0, false);
        this.scale = new Vector2(scaleX, scaleY);

        if (addToGrid) {
            game.grid.insert(this);
            isInGrid = true;
        }
    }


    public void setScale(double x, double y) {
        if (scale.x != x || scale.x != y) {
            scale.x = x;
            scale.y = y;
            needsUpdate = true;
            game.grid.update(this);
        }
    }
    

    @Override
    public JSONObject UpdateData() {
        JSONObject json = super.UpdateData();
        json.put("scaleX", scale.x);
        json.put("scaleY", scale.y);
        return json;
    }
}