package src;

import java.util.ArrayList;
import java.util.TreeMap;

public class Sketch {
    private TreeMap<Integer, Shape> shapes; // treemap for sketch
    private int size; // keep track of index

    public ArrayList<ArrayList<Integer>> grouped = new ArrayList<>();


    public Sketch() {
        this.shapes = new TreeMap<Integer, Shape>();
        size = 0;
    }

    /**
     * add a shape to the treemap
     * @param s
     */
    public void addShape(Shape s) {
        System.out.println("size right before ACTUAL add"+size);
        int lastidx = 0;
        for (int i: shapes.keySet()){
            lastidx = i;
        }
        shapes.put(lastidx + 1, s);
        size++;
    }

    public void addShape (int id, Shape s){
        shapes.put(id, s);
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
    public TreeMap<Integer, Shape> getShapeMap() {
        return shapes;
    }

    public void setShapeMap(TreeMap<Integer, Shape> inputMap){
        shapes = inputMap;
    }

    /**
     * getter for size of shape map
     * @return
     */
    public int getSize() {
        return size;
    }

    public int getActualSize() {return shapes.size();}

    public void setSize(int size) {this.size = size;}

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

    /**
     * deepCopy method for sketch's object
     * @return
     */
    public Sketch deepCopy(){
        Sketch deepCopy = new Sketch();
        for (int i: this.getShapeMap().keySet()) {
            deepCopy.shapes.put(i, this.shapes.get(i).deepCopy());
        }
        deepCopy.size = this.size;
        deepCopy.grouped = new ArrayList<>(grouped);
        return deepCopy;
    }
}
