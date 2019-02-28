package arenaworker.lib;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import arenaworker.Base;

public class GridTest {

    @Test
    public void GridRetrieveTest() {

        Grid grid = new Grid(1000, 100);

        for (int i = 0; i < 100; i++) {
            double x = Math.random() * 1000;
            double y = Math.random() * 1000;
            double radius = 20;
            
            Base obj1 = new Base(null, x, y, radius, 0, false);
            grid.insert(obj1);
            Base obj2 = new Base(null, x + radius * 2, y, radius, 0, false);
            grid.insert(obj2);

            Set<Base> found = grid.retrieve(obj1.position, obj1.radius);

            Assert.assertEquals(2, found.size());

            grid.remove(obj1);
            grid.remove(obj2);
        }

        for (int i = 0; i < 100; i++) {
            double x = Math.random() * 1000;
            double y = Math.random() * 1000;
            double radius = 20;
            
            Base obj1 = new Base(null, x, y, radius, 0, false);
            grid.insert(obj1);
            Base obj2 = new Base(null, x + radius * 3, y, radius, 0, false);
            grid.insert(obj2);

            Set<Base> found = grid.retrieve(obj1.position, obj1.radius);

            Assert.assertEquals(1, found.size());

            grid.remove(obj1);
            grid.remove(obj2);
        }
    }
}