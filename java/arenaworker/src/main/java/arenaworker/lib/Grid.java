package arenaworker.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import arenaworker.Obj;
import arenaworker.ObjCircle;
import arenaworker.ObjRectangle;
import arenaworker.Player;

public class Grid {
    
    private double g;  // optimization
    private ConcurrentHashMap<String, Set<Obj>> objects = new ConcurrentHashMap<>();  // TODO: needs to be different.  why?
    
    public Grid(double size, int divisions) {
        this.g = divisions / size;
    }
    
    
    public void update(Obj obj) {
        if (obj instanceof ObjCircle) {
            ObjCircle o = (ObjCircle)obj;
            if (!obj.grids.equals(getGridsObjCircleIsIn(o.position, o.radius))) {
                remove(o);
                insert(o);
            }
        } else if (obj instanceof ObjRectangle) {
            ObjRectangle o = (ObjRectangle)obj;
            if (!obj.grids.equals(getGridsObjRectangleIsIn(o.position, o.scale))) {
                remove(o);
                insert(o);
            }
        }
    }
    
    
    public void insert(Obj obj) {
        if (obj instanceof ObjCircle) {
            ObjCircle o = (ObjCircle)obj;
            obj.grids = getGridsObjCircleIsIn(o.position, o.radius);
        } else if (obj instanceof ObjRectangle) {
            ObjRectangle o = (ObjRectangle)obj;
            obj.grids = getGridsObjRectangleIsIn(o.position, o.scale);
        }
        
        for (String grid : obj.grids) {
            if (objects.containsKey(grid)) {
                objects.get(grid).add(obj);
            } else {
                objects.put(grid, new HashSet<Obj>(Arrays.asList(obj)));
            }
        }
    }
    
    
    public void remove(Obj obj) {
        for (String grid : obj.grids) {
            objects.get(grid).remove(obj);
        }
        obj.grids = new String[0];
    }
    
    
    
    public Set<Obj> retrieve(Vector2 pos, double radius) {
        Set<Obj> retrievedObjects = Collections.newSetFromMap(new ConcurrentHashMap<Obj, Boolean>());
        
        String[] grids = getGridsObjCircleIsIn(pos, radius);
        
        for (String grid : grids) {
            if (objects.containsKey(grid)) {
                
                Set<Obj> gridObjects = Collections.newSetFromMap(new ConcurrentHashMap<Obj, Boolean>());
                gridObjects = objects.get(grid);
                
                retrievedObjects.addAll(gridObjects);
            }
        }
        
        return retrievedObjects;
    }


    public Set<Obj> retrieve(Vector2 pos, Vector2 scale) {
        Set<Obj> retrievedObjects = Collections.newSetFromMap(new ConcurrentHashMap<Obj, Boolean>());

        String[] grids = getGridsObjRectangleIsIn(pos, scale);

        for (String grid : grids) {
            if (objects.containsKey(grid)) {
                
                Set<Obj> gridObjects = Collections.newSetFromMap(new ConcurrentHashMap<Obj, Boolean>());
                gridObjects = objects.get(grid);
                
                retrievedObjects.addAll(gridObjects);
            }
        }
        
        return retrievedObjects;
    }
    
    
    public Set<Obj> retrievePlayers(Vector2 pos, double radius) {
        Set<Obj> retrievedObjects = new HashSet<>();
        Set<Obj> tempObjects = new HashSet<>();
        
        String[] grids = getGridsObjCircleIsIn(pos, radius);
        
        for (String grid : grids) {
            if (objects.containsKey(grid)) {
                for (Obj o : objects.get(grid)) {
                    if (Player.class.isAssignableFrom(o.getClass())) {
                       tempObjects.add(o); 
                    }
                }
                retrievedObjects.addAll(tempObjects);
            }
        }

        return retrievedObjects;
    }
    
    
    private String[] getGridsObjCircleIsIn(Vector2 pos, double radius) {
        List<String> grids = new ArrayList<>();

        int[] min = positionToGrid(pos.x - radius, pos.y - radius);
        int[] max = positionToGrid(pos.x + radius, pos.y + radius);
        
        for (int x = min[0]; x <= max[0]; x++) {
            for (int y = min[1]; y <= max[1]; y++) {
                grids.add(x + "_" + y);
            }
        }
        
        return grids.toArray(new String[grids.size()]);
    }


    private String[] getGridsObjRectangleIsIn(Vector2 pos, Vector2 scale) {
        List<String> grids = new ArrayList<>();

        // https://stackoverflow.com/questions/622140/calculate-bounding-box-coordinates-from-a-rotated-rectangle
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

        return grids.toArray(new String[grids.size()]);
    }
    
    
    private int[] positionToGrid(double x, double y) {
        return new int[]{(int)Math.round(g * x), (int)Math.round(g * y)};
    }
}