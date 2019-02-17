package arenaworker.lib;

import org.json.JSONObject;

public class Vector2 {
    public double x;
    public double y;

    public Vector2() {
        this.x = 0.0;
        this.y = 0.0;
    }

    public Vector2 (double x, double y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return x + "," + y;
    }

    public Vector2 copy() {
        return new Vector2(x, y);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("x", x);
        json.put("y", y);
        return json;
    }

    public Vector2 add(Vector2 other) {
        x += other.x;
        y += other.y;
        return (this);
    }

    public Vector2 subtract(Vector2 other) {
        return new Vector2(x - other.x, y - other.y);
    }


    public Vector2 scale(double num) {
        x *= num;
        y *= num;
        return this;
    }
    
    
    public boolean equals(Vector2 other) {
        return (this.x == other.x && this.y == other.y);
    }
    
    public static double distance(Vector2 a, Vector2 b) {
        double v0 = b.x - a.x;
        double v1 = b.y - a.y;
        return Math.sqrt(v0 * v0 + v1 * v1);
    }

    public double distance(Vector2 other) {
        double v0 = other.x - this.x;
        double v1 = other.y - this.y;
        return Math.sqrt(v0 * v0 + v1 * v1);
    }
    
    public Vector2 normalize() {
        double length = length();
        
        if (length != 0.0) {
            double s = 1.0f / length;
            x = x * s;
            y = y * s;
        }

        return this;
    }

    // same as normalize() but return result instead
    public Vector2 getNormalized() {
        double length = length();
        
        if (length == 0f) {
            return this;
        }
        
        double s = 1.0 / length;
        
        return new Vector2(x * s, y * s);
    }

    public double dotProduct(Vector2 v) {
        return x * v.x + y * v.y;
    }
    
    public static double dotProduct(Vector2 a, Vector2 b) {
        return a.x * b.x + a.y * b.y;
    }
    
    public double length() {
        return Math.sqrt(x * x + y * y);
    }
    
    public Vector2 rotateVector(double degrees) {
        double radians = -1 * degrees * (Math.PI / 180);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vector2(
            x * cos - y * sin,
            x * sin + y * cos
        );
    }
}