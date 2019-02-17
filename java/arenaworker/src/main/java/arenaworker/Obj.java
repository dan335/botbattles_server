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

    Vector2 priorForces = new Vector2();
    Vector2 acceleration = new Vector2();
    public void Tick() {
        priorForces.x = game.settings.drag * velocity.x;
        priorForces.y = game.settings.drag * velocity.y;

        acceleration.x = priorForces.x + forces.x;
        acceleration.y = priorForces.y + forces.y;

        forces.x = 0;
        forces.y = 0;

        if (acceleration.x != 0 || acceleration.y != 0 || velocity.x != 0 || velocity.y != 0) {
            velocity.x = velocity.x + acceleration.x;
            velocity.y = velocity.y + acceleration.y;

            SetPosition(
                position.x + velocity.x * game.deltaTime,
                position.y + velocity.y * game.deltaTime
            );
        }

        super.Tick();
    }
}