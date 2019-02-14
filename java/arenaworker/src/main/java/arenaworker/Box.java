package arenaworker;

import org.json.JSONObject;

import arenaworker.lib.Physics;

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


    @Override
    public void Contact(Base otherObject) {
        if (otherObject instanceof Obstacle) {
            otherObject.Destroy();
        } else if (otherObject instanceof Player) {
            Physics.PositionalCorrection(this, (Obj)otherObject);
        } else if (otherObject instanceof Box) {
            return;
        }
    }


    @Override
    public void SetPosition(double x, double y) {
        if (position.x != x || position.y != y) {

            position.x = x;
            position.y = y;
            needsUpdate = true;
            if (isInGrid) {
                game.grid.update(this);
            }
        }
    }
}