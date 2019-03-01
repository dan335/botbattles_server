package arenaworker.lib;

import arenaworker.Base;
import arenaworker.Obj;
import arenaworker.ObjRectangle;

public class Physics {
    
    public static boolean pointInCircle(double x, double y, double cirX, double cirY, double r) {
        if (r == 0) { return false; }
        final double dx = cirX - x;
        final double dy = cirY - y;
        return dx * dx + dy * dy <= r * r;
    }
    
    
    public static boolean pointInRect(double x, double y, double rectX, double rectY, double rectW, double rectH) {
        return (
                rectX - rectW / 2 < x &&
                rectY - rectH / 2 < y &&
                rectX + rectW / 2 > x &&
                rectY + rectH / 2 > y
                );
    }    
    
    public static double distanceBetweenPoints(double aPosX, double aPosY, double bPosX, double bPosY){
        return Math.sqrt(((aPosX - bPosX) * (aPosX - bPosX)) + ((aPosY - bPosY) * (aPosY - bPosY)));
    }
    
    public static boolean circleInCircle(double aPosX, double aPosY, double aRadius, double bPosX, double bPosY, double bRadius) {
        return Math.sqrt(((aPosX - bPosX) * (aPosX - bPosX)) + ((aPosY - bPosY) * (aPosY - bPosY))) < aRadius + bRadius;
    }
    
    public static boolean circleInCircle(Base a, Base b) {
        return circleInCircle(a.position.x, a.position.y, a.radius, b.position.x, b.position.y, b.radius);
    }


    public static boolean circleInRectangle(Base a, ObjRectangle b) {
       return circleInRectangle(a.position, a.radius, b.position, b.scale);
    }


    // axis aligned rectangle
    static Vector2 circleDistance = new Vector2();
    public static boolean circleInRectangle(Vector2 posA, double radiusA, Vector2 posB, Vector2 scaleB) {
        circleDistance.x = Math.abs(posA.x - posB.x);
        circleDistance.y = Math.abs(posA.y - posB.y);
        
        if (circleDistance.x > (scaleB.x/2 + radiusA)) { return false; }
        if (circleDistance.y > (scaleB.y/2 + radiusA)) { return false; }

        if (circleDistance.x <= (scaleB.x/2)) { return true; } 
        if (circleDistance.y <= (scaleB.y/2)) { return true; }

        double cornerDistance_sq = Math.pow(circleDistance.x - scaleB.x/2, 2) + Math.pow(circleDistance.y - scaleB.y/2, 2);

        return cornerDistance_sq <= Math.pow(radiusA, 2);
    }




    // rectangle cannot be rotated
    public static Collision resolveCollision(Obj a, ObjRectangle b) {
        if (b.rotation != 0) {
            System.out.println("Error: Rectangle cannot be rotated");
        }

        Vector2 relativeVelocity = new Vector2(
            b.velocity.x - a.velocity.x,
            b.velocity.y - a.velocity.y
        );

        // length for damage caused
        final double magnitude = Math.sqrt(relativeVelocity.x * relativeVelocity.x + relativeVelocity.y * relativeVelocity.y);
        
        Vector2 closest = new Vector2();
        closest.x = Math.min(Math.max(a.position.x, b.position.x - b.scale.x / 2), b.position.x + b.scale.x / 2);
        closest.y = Math.min(Math.max(a.position.y, b.position.y - b.scale.y / 2), b.position.y + b.scale.y / 2);

        boolean isInside = false;

        if (
            closest.x > b.position.x - b.scale.x/2 &&
            closest.x < b.position.x + b.scale.x/2 &&
            closest.y > b.position.y - b.scale.y/2 &&
            closest.y < b.position.y + b.scale.y/2
        ){
            isInside = true;

            // Find closest axis
            double distancePositiveX = Math.abs(a.position.x - (b.position.x + b.scale.x));
            double distanceNegativeX = Math.abs(a.position.x - (b.position.x - b.scale.x));
            double distancePositiveY = Math.abs(a.position.y - (b.position.y + b.scale.y));
            double distanceNegativeY = Math.abs(a.position.y - (b.position.y - b.scale.y));

            double smallest = Math.min(Math.min(distancePositiveX, distanceNegativeX), Math.min(distancePositiveY, distanceNegativeY));

            if (distancePositiveX == smallest) {
                closest.x = b.position.x + b.scale.x / 2;
            } else if (distanceNegativeX == smallest) {
                closest.x = b.position.x - b.scale.x / 2;
            } else if (distancePositiveY == smallest) {
                closest.y = b.position.y + b.scale.y / 2;
            } else {
                closest.y = b.position.y - b.scale.y / 2;
            }
        }

        Vector2 collisionNormal = closest.subtract(a.position);

        if (isInside) {
            collisionNormal.scale(-1);
        }
        
        collisionNormal.normalize();

        // calculate relative velocity in terms of the normal direction
        final double velocityAlongNormal = relativeVelocity.dotProduct(collisionNormal);
        
        // do not resolve if velocities are separating
        if (velocityAlongNormal > 0) {
            return null; 
        }

        // calculate resitution
        final double restitution = Math.min(a.restitution, b.restitution);
        
        // mass = 0 is infinite mass
        // double aInverseMass, bInverseMass;
        // if (a.mass == 0) {
        //     aInverseMass = 0;
        // } else {
        //     aInverseMass = 1 / a.mass;
        // }
        
        // bInverseMass = 0;

        // calculate impulse scalar j
        double j = -(1 + restitution) * velocityAlongNormal;
        //j /= aInverseMass + bInverseMass; // not sure why but works better commented out

        // apply impulse
        final Vector2 impulse = new Vector2(
            j * collisionNormal.x,
            j * collisionNormal.y
        );
        
        double massSum = a.mass + b.mass;
        double ratio;

        ratio = a.mass / massSum;
        a.Collision(b, magnitude, new Vector2(ratio * impulse.x * -1, ratio * impulse.y * -1));
        
        a.velocity = a.velocity.subtract(impulse.scale(ratio));

        ratio = b.mass / massSum;
        b.Collision(a, magnitude, new Vector2(ratio * impulse.x, ratio * impulse.y));

        // b.velocity = b.velocity.add(impulse.scale(ratio));

        //a.needsUpdate = true;
        //b.needsUpdate = true;

        PositionalCorrection(a, b, collisionNormal, isInside);

        return new Collision(a, b, magnitude);
    }

    
    // https://gamedevelopment.tutsplus.com/tutorials/how-to-create-a-custom-2d-physics-engine-the-basics-and-impulse-resolution--gamedev-6331
    
    

    public static Collision resolveCollision(Obj a, Obj b) {
        Vector2 relativeVelocity = new Vector2(
            b.velocity.x - a.velocity.x,
            b.velocity.y - a.velocity.y
        );
        
        // length for damage caused
        final double magnitude = Math.sqrt(relativeVelocity.x * relativeVelocity.x + relativeVelocity.y * relativeVelocity.y);
        
        Vector2 collisionNormal = new Vector2(
            b.position.x - a.position.x,
            b.position.y - a.position.y
        );
        
        collisionNormal.normalize();
        
        // calculate relative velocity in terms of the normal direction
        final double velocityAlongNormal = relativeVelocity.dotProduct(collisionNormal);
        
        // do not resolve if velocities are separating
        if (velocityAlongNormal > 0) {
            return null; 
        }
        
        // calculate resitution
        final double restitution = Math.min(a.restitution, b.restitution);
        
        // mass = 0 is infinite mass
        double aInverseMass, bInverseMass;
        if (a.mass == 0) {
            aInverseMass = 0;
        } else {
            aInverseMass = 1 / a.mass;
        }
        
        if (b.mass == 0) {
            bInverseMass = 0;
        } else {
            bInverseMass = 1 / b.mass;
        }
        
        // calculate impulse scalar j
        double j = -1 * (1 + restitution) * velocityAlongNormal;
        j /= (aInverseMass + bInverseMass);

        // apply impulse
        final Vector2 impulse = new Vector2(
            j * collisionNormal.x,
            j * collisionNormal.y
        );
        
        double massSum = aInverseMass + bInverseMass;
        double ratio;
        
        ratio = aInverseMass / massSum;
        a.Collision(b, magnitude, new Vector2(ratio * impulse.x * -1, ratio * impulse.y * -1));
        
        a.velocity = a.velocity.subtract(impulse.copy().scale(ratio));

        ratio = bInverseMass / massSum;
        b.Collision(a, magnitude, new Vector2(ratio * impulse.x, ratio * impulse.y));

        b.velocity.add(impulse.copy().scale(ratio));

        // a.needsUpdate = true;
        // b.needsUpdate = true;

        PositionalCorrection(a, b);

        return new Collision(a, b, magnitude);
    }



    public static void PositionalCorrection( Obj a, ObjRectangle b) {
        Vector2 closest = new Vector2();
        closest.x = Math.min(Math.max(a.position.x, b.position.x - b.scale.x / 2), b.position.x + b.scale.x / 2);
        closest.y = Math.min(Math.max(a.position.y, b.position.y - b.scale.y / 2), b.position.y + b.scale.y / 2);

        boolean isInside = false;

        if (
            closest.x > b.position.x - b.scale.x/2 &&
            closest.x < b.position.x + b.scale.x/2 &&
            closest.y > b.position.y - b.scale.y/2 &&
            closest.y < b.position.y + b.scale.y/2
        ){
            isInside = true;

            // Find closest axis
            double distancePositiveX = Math.abs(a.position.x - (b.position.x + b.scale.x));
            double distanceNegativeX = Math.abs(a.position.x - (b.position.x - b.scale.x));
            double distancePositiveY = Math.abs(a.position.y - (b.position.y + b.scale.y));
            double distanceNegativeY = Math.abs(a.position.y - (b.position.y - b.scale.y));

            double smallest = Math.min(Math.min(distancePositiveX, distanceNegativeX), Math.min(distancePositiveY, distanceNegativeY));

            if (distancePositiveX == smallest) {
                closest.x = b.position.x + b.scale.x / 2;
            } else if (distanceNegativeX == smallest) {
                closest.x = b.position.x - b.scale.x / 2;
            } else if (distancePositiveY == smallest) {
                closest.y = b.position.y + b.scale.y / 2;
            } else {
                closest.y = b.position.y - b.scale.y / 2;
            }
        }

        Vector2 collisionNormal = closest.subtract(a.position);

        if (isInside) {
            collisionNormal.scale(-1);
        }
        
        collisionNormal.normalize();

        PositionalCorrection(a, b, collisionNormal, isInside);
    }



    public static void PositionalCorrection( Obj a, ObjRectangle b, Vector2 collisionNormal, boolean isInside) {
        double penetrationDepth;

        if (isInside) {
            penetrationDepth = Math.abs(a.radius + collisionNormal.length());
        } else {
            penetrationDepth = Math.abs(a.radius - collisionNormal.length());
        }

        collisionNormal.normalize();

        a.SetPosition(a.position.subtract(collisionNormal.scale(penetrationDepth * 0.5)));
    }




    public static void PositionalCorrection( Obj a, Obj b ) {
        double distance = Vector2.distance(a.position, b.position);
        double penetrationDepth = a.radius + b.radius - distance;
        
        if (penetrationDepth < 0) {
            return;
        }

        // mass = 0 is infinite mass
        double aInverseMass, bInverseMass;
        if (a.mass == 0) {
            aInverseMass = 0;
        } else {
            aInverseMass = 1 / a.mass;
        }
        
        if (b.mass == 0) {
            bInverseMass = 0;
        } else {
            bInverseMass = 1 / b.mass;
        }

        Vector2 collisionNormal = new Vector2(
            b.position.x - a.position.x,
            b.position.y - a.position.y
        );
        
        collisionNormal.normalize();

        double percent = 0.8; // usually 20% to 80%
        double slop = 0.01;
        double amount = Math.max( penetrationDepth - slop, 0) / (aInverseMass + bInverseMass) * percent;

        Vector2 correction = new Vector2(
            amount * collisionNormal.x,
            amount * collisionNormal.y
        );

        a.SetPosition(a.position.copy().subtract(correction.copy().scale(aInverseMass)));
        b.SetPosition(b.position.copy().add(correction.copy().scale(bInverseMass)));
    }
    
    
    
    
    
    // slowly rotate towards target
    public static double slowlyRotateToward(Vector2 pos, double rotation, Vector2 targetPos, double maxRotation) {
        double angle = Math.atan2(targetPos.y - pos.y, targetPos.x - pos.x);
        
        // flipp angle if difference > 3 or < -2
        if (angle - rotation < Math.PI / 2 * -1) {
            angle += Math.PI * 2;
        }
        
        if (angle - rotation > Math.PI / 2) {
            angle -= Math.PI * 2;
        }
        
        double newRotation = rotation + Math.max(maxRotation * -1, Math.min(maxRotation, angle - rotation));
        
        // wrap between PI
        newRotation = newRotation + (2f * Math.PI) * Math.floor((Math.PI - newRotation) /(2f * Math.PI));

        return newRotation;
    }
}
