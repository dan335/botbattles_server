package arenaworker;

public class ObjCircle extends Obj {

    public double radius = 1;
    
    public ObjCircle(Game game, double x, double y, double radius) {
        super(game, x, y);
        this.radius = radius;
    }


    public void SetRadius(double radius) {
        this.radius = radius;
    }
}