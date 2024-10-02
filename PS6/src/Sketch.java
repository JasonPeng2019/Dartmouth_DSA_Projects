package src;

import java.util.Map;
import java.util.TreeMap;

public class Sketch {
    private Map<Integer, Shape> shapes; // treemap for sketch
    private int size; // keep track of index

    public Sketch() {
        this.shapes = new TreeMap<Integer, Shape>();
        size = 0;
    }

    /**
     * add a shape to the treemap
     * @param s
     */
    public void addShape(Shape s) {
        shapes.put(size, s);
        size++;
    }

    /**
     * remove a shape from the treemap based on an index
     * @param i
     */
    public void remove(int i) {
        shapes.remove(i);
    }

    /**
     * getter for the shape map
     * @return
     */
    public Map<Integer, Shape> getShapeMap() {
        return shapes;
    }

    /**
     * getter for size of shape map
     * @return
     */
    public int getSize() {
        return shapes.size();
    }

    /**
     * returns data as a parseable string
     * @return
     */
    public String getShapes() {
        String ans = "";
        for(Integer s : shapes.keySet()) {
            ans += s + "/";
            ans += shapes.get(s).toString() + "&&";
            // for sending data, when you want to split between shapes
        }
        return ans;
    }

    /**
     * toString method for data. Uses getShapes() to turn the data into parseable string
     * @return
     */
    @Override
    public String toString() {
        return getShapes();
    }
}
