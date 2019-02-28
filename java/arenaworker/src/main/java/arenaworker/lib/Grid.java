package arenaworker.lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import arenaworker.Base;
import arenaworker.ObjRectangle;

public class Grid {
    
    private double g;  // optimization
    private ConcurrentHashMap<String, Set<Base>> objects = new ConcurrentHashMap<>();
    
    public Grid(double size, int divisions) {
        this.g = divisions / size;
    }
    
    
    public void update(Base obj) {
        if (obj instanceof ObjRectangle) {
            ObjRectangle o = (ObjRectangle)obj;
            if (!obj.grids.equals(getGridsObjRectangleIsIn(o.position, o.scale))) {
                remove(o);
                insert(o);
            }
        } else {
            if (!obj.grids.equals(getGridsObjCircleIsIn(obj.position, obj.radius))) {
                remove(obj);
                insert(obj);
            }
        }
    }
    
    
    public void insert(Base obj) {
        if (obj instanceof ObjRectangle) {
            ObjRectangle o = (ObjRectangle)obj;
            obj.grids = getGridsObjRectangleIsIn(o.position, o.scale);
        } else {
            obj.grids = getGridsObjCircleIsIn(obj.position, obj.radius);
        }
        
        for (String grid : obj.grids) {
            if (objects.containsKey(grid)) {
                objects.get(grid).add(obj);
            } else {
                Set<Base> temp = ConcurrentHashMap.newKeySet();
                temp.add(obj);
                objects.put(grid, temp);
            }
        }
    }
    
    
    public void remove(Base obj) {
        for (String grid : obj.grids) {
            objects.get(grid).remove(obj);
        }
        obj.grids.clear();
    }
    
    
    Set<Base> retrievedObjects = Collections.newSetFromMap(new ConcurrentHashMap<Base, Boolean>());
    public Set<Base> retrieve(Vector2 pos, double radius) {
        retrievedObjects.clear();

        Set<String> grids = getGridsObjCircleIsIn(pos, radius);
        
        for (String grid : grids) {
            if (objects.containsKey(grid)) {
                retrievedObjects.addAll(objects.get(grid));
            }
        }
        
        return retrievedObjects;
    }


    public Set<Base> retrieve(Vector2 pos, Vector2 scale) {
        retrievedObjects.clear();

        Set<String> grids = getGridsObjRectangleIsIn(pos, scale);

        for (String grid : grids) {
            if (objects.containsKey(grid)) {
                retrievedObjects.addAll(objects.get(grid));
            }
        }
        
        return retrievedObjects;
    }
    

    
    
    private Set<String> getGridsObjCircleIsIn(Vector2 pos, double radius) {
        Set<String> grids = new HashSet<>();

        int[] min = positionToGrid(pos.x - radius, pos.y - radius);
        int[] max = positionToGrid(pos.x + radius, pos.y + radius);
        
        for (int x = min[0]; x <= max[0]; x++) {
            for (int y = min[1]; y <= max[1]; y++) {
                grids.add(x + "_" + y);
            }
        }
        
        return grids;
    }


    private Set<String> getGridsObjRectangleIsIn(Vector2 pos, Vector2 scale) {
        Set<String> grids = new HashSet<>();

        // https://stackoverflow.com/questions/622140/calculate-bounding-box-coordinates-from-a-rotated-rectangle
        // TODO: remove rotation part to make faster - boxes are never rotated
        double x1 = -scale.x / 2;
        double x2 = scale.x / 2;
        double x3 = scale.x / 2;
        double x4 = -scale.x / 2;
        double y1 = scale.y / 2;
        double y2 = scale.y / 2;
        double y3 = -scale.y / 2;
        double y4 = -scale.y / 2;

        // rotation = 0 always
        double x11 = x1 * Math.cos(0) + y1 * Math.sin(0);
        double y11 = -x1 * Math.sin(0) + y1 * Math.cos(0);
        double x21 = x2 * Math.cos(0) + y2 * Math.sin(0);
        double y21 = -x2 * Math.sin(0) + y2 * Math.cos(0);
        double x31 = x3 * Math.cos(0) + y3 * Math.sin(0);
        double y31 = -x3 * Math.sin(0) + y3 * Math.cos(0);
        double x41 = x4 * Math.cos(0) + y4 * Math.sin(0);
        double y41 = -x4 * Math.sin(0) + y4 * Math.cos(0);

        double xMin = Math.min(Math.min(x11, x21), Math.min(x31, x41));
        double xMax = Math.max(Math.max(x11, x21), Math.max(x31, x41));

        double yMin = Math.min(Math.min(y11, y21), Math.min(y31, y41));
        double yMax = Math.max(Math.max(y11, y21), Math.max(y31, y41));

        int[] min = positionToGrid(pos.x + xMin, pos.y + yMin);
        int[] max = positionToGrid(pos.x + xMax, pos.y + yMax);

        for (int x = min[0]; x <= max[0]; x++) {
            for (int y = min[1]; y <= max[1]; y++) {
                grids.add(x + "_" + y);
            }
        }

        return grids;
    }
    
    
    private int[] positionToGrid(double x, double y) {
        return new int[]{(int)Math.round(g * x), (int)Math.round(g * y)};
    }
}