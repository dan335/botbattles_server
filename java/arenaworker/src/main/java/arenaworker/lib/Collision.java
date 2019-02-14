package arenaworker.lib;

import arenaworker.Base;
import arenaworker.Obj;
import arenaworker.ObjRectangle;

public class Collision {

    public Base a;
    public Base b;
    public double magnitude;

    public Collision(Obj a, Obj b, double magnitude) {
        this.a = a;
        this.b = b;
        this.magnitude = magnitude;
    }

    public Collision(Obj a, ObjRectangle b, double magnitude) {
        this.a = a;
        this.b = b;
        this.magnitude = magnitude;
    }
}