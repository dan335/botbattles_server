package arenaworker;

import arenaworker.lib.Vector2;


// Obj is Base with physics

public class Obj extends Base {
    public Vector2 velocity = new Vector2();
    public Vector2 forces = new Vector2(); // forces to be applied this tick
    public double restitution = 0.5;
    public double mass = 1;

    public Obj (Game game, double x, double y, double radius, double rotation, boolean addToGrid) {
        super(game, x, y, radius, rotation, addToGrid);
    }


    public void Collision(Obj obj, double magnitude, Vector2 force) {}


    public void Tick() {
        final Vector2 priorForces = new Vector2(game.settings.drag * velocity.x, game.settings.drag * velocity.y);
        final Vector2 acceleration = new Vector2(priorForces.x + forces.x, priorForces.y + forces.y);

        if (acceleration.x != 0 || acceleration.y != 0 || velocity.x != 0 || velocity.y != 0) {
            velocity.x = velocity.x + acceleration.x;
            velocity.y = velocity.y + acceleration.y;

            SetPosition(position.add(velocity.scale(game.deltaTime)));
        }

        super.Tick();
    }
}