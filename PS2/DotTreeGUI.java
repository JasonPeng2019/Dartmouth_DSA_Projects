import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Driver for interacting with a quadtree:
 * inserting points, viewing the tree, and finding points near a mouse press
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2015
 * @author CBK, Spring 2016, updated for dots
 * @author CBK, Fall 2016, generics, dots, extended testing
 */
public class DotTreeGUI extends DrawingGUI {
    private static final int width=800, height=600;		// size of the universe
    private static final int dotRadius = 5;				// to draw dot, so it's visible
    private static final Color[] rainbow = {Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA};
    // to color different levels differently

    private PointQuadtree<Dot> tree = null;			// holds the dots
    private char mode = 'a';						// 'a': adding; 'q': querying with the mouse
    private int mouseX, mouseY;						// current mouse location, when querying
    private int mouseRadius = 10;					// circle around mouse location, for querying
    private boolean trackMouse = false;				// if true, then print out where the mouse is as it moves
    private List<Dot> found = null;					// who was found near mouse, when querying

    public DotTreeGUI() {
        super("dottree", width, height);
    }

    /**
     * DrawingGUI method, here keeping track of the location and redrawing to show it
     */
    @Override
    public void handleMouseMotion(int x, int y) {
        if (mode == 'q') {
            mouseX = x; mouseY = y;
            repaint();
        }
        if (trackMouse) {
            System.out.println(x+","+y);
        }
    }

    /**
     * DrawingGUI method, here either adding a new point or querying near the mouse
     */
    @Override
    public void handleMousePress(int x, int y) {
        if (mode == 'a') {
            // Add a new dot at the point
            // TODO: YOUR CODE HERE
            // if null, make a new tree with the rectangle as the full screen
            if (this.tree == null) {
                Dot dot = new Dot(x, y);
                tree = new PointQuadtree<>(dot, 0, 0, width - 1, height - 1);
            }
            // else insert the point into the tree
            else {
            tree.insert(new Dot(x, y));}

        }
        else if (mode == 'q') {
            // Set "found" to what tree says is near the mouse press
            // TODO: YOUR CODE HERE
            found = tree.findInCircle(x, y, mouseRadius);
        }
        else {
            System.out.println("clicked at "+x+","+y);
        }
        // print the tree structure for debugging
//        tree.printTree("");
        repaint();
    }

    /**
     * A simple testing procedure, making sure actual is expected, and printing a message if not
     * @param x		query x coordinate
     * @param y		query y coordinate
     * @param r		query circle radius
     * @param expectedCircleRectangle	how many times Geometry.circleIntersectsRectangle is expected to be called
     * @param expectedInCircle			how many times Geometry.pointInCircle is expected to be called
     * @param expectedHits				how many points are expected to be found
     * @return  0 if passed; 1 if failed
     */
    private int testFind(int x, int y, int r, int expectedCircleRectangle, int expectedInCircle, int expectedHits) {
        Geometry.resetNumInCircleTests();
        Geometry.resetNumCircleRectangleTests();
        int errs = 0;
        int num = tree.findInCircle(x, y, r).size();
        String which = "("+x+","+y+")@"+r;
        if (Geometry.getNumCircleRectangleTests() != expectedCircleRectangle) {
            errs++;
            System.err.println(which+": wrong # circle-rectangle, got "+Geometry.getNumCircleRectangleTests()+" but expected "+expectedCircleRectangle);
        }
        if (Geometry.getNumInCircleTests() != expectedInCircle) {
            errs++;
            System.err.println(which+": wrong # in circle, got "+Geometry.getNumInCircleTests()+" but expected "+expectedInCircle);
        }
        if (num != expectedHits) {
            errs++;
            System.err.println(which+": wrong # hits, got "+num+" but expected "+expectedHits);
        }
        return errs;
    }

    /**
     * test tree 0 -- first three points from figure in handout
     * hardcoded point locations for 800x600
     */
    private void test0() {
        found = null;
        tree = new PointQuadtree<Dot>(new Dot(400,300), 0,0,800,600); // start with A
        tree.insert(new Dot(150,450)); // B
        tree.insert(new Dot(250,550)); // C
        int bad = 0;
        bad += testFind(0,0,900,3,3,3);		// rect for all; circle for all; find all
        bad += testFind(400,300,10,3,2,1);	// rect for all; circle for A,B; find A
        bad += testFind(150,450,10,3,3,1);	// rect for all; circle for all; find B
        bad += testFind(250,550,10,3,3,1);	// rect for all; circle for all; find C
        bad += testFind(150,450,200,3,3,2);	// rect for all; circle for all; find B, C
        bad += testFind(140,440,10,3,2,0);	// rect for all; circle for A,B; find none
        bad += testFind(750,550,10,2,1,0);	// rect for A,B; circle for A; find none
        if (bad==0) System.out.println("test 0 passed!");
    }

    /**
     * test tree 1 -- figure in handout
     * hardcoded point locations for 800x600
     */
    private void test1() {
        found = null;
        tree = new PointQuadtree<Dot>(new Dot(300,400), 0,0,800,600); // start with A
        tree.insert(new Dot(150,450)); // B
        tree.insert(new Dot(250,550)); // C
        tree.insert(new Dot(450,200)); // D
        tree.insert(new Dot(200,250)); // E
        tree.insert(new Dot(350,175)); // F
        tree.insert(new Dot(500,125)); // G
        tree.insert(new Dot(475,250)); // H
        tree.insert(new Dot(525,225)); // I
        tree.insert(new Dot(490,215)); // J
        tree.insert(new Dot(700,550)); // K
        tree.insert(new Dot(310,410)); // L
        int bad = 0;
        bad += testFind(150,450,10,6,3,1); 	// rect for A [D] [E] [B [C]] [K]; circle for A, B, C; find B
        bad += testFind(500,125,10,8,3,1);	// rect for A [D [G F H]] [E] [B] [K]; circle for A, D, G; find G
        bad += testFind(300,400,15,10,6,2);	// rect for A [D [G F H]] [E] [B [C]] [K [L]]; circle for A,D,E,B,K,L; find A,L
        bad += testFind(495,225,50,10,6,3);	// rect for A [D [G F H [I [J]]]] [E] [B] [K]; circle for A,D,G,H,I,J; find H,I,J
        bad += testFind(0,0,900,12,12,12);	// rect for all; circle for all; find all
        if (bad==0) System.out.println("test 1 passed!");
    }

    /**
     * Test 2: create quadtree, insert 1000 random points, and use find() to locate them
     */
    public void test2() {
        //create new tree & a new list that will hold points & a list that will hold all found points
        tree = null;
        Set<Dot> points = new HashSet<>();
        Set<Dot> returnPoints = new HashSet<>();
        Set<Dot> returnPoints2 = new TreeSet<>();

        // create 1000 points, adding them both to the list and to the tree as you go.
        for (int i = 0; i < 1000; i++) {
            int x = (int)((Math.random())*width);
            int y = (int)((Math.random())*height);
            Dot p1 = new Dot(x, y);
            points.add(p1);
            // if tree is null, make a new tree
            if (tree == null) {
                //System.out.println("TEST1");
                tree = new PointQuadtree<Dot>(p1, 0, 0, width-1, height-1);
            }
            // else add it to the tree
            else {
                //System.out.println("INSERTING");
                tree.insert(p1);
                //tree.printTree("");
            }
        }
        // loop through the list of points you created and try to find it, adding found to a new set
        for (Dot p: points) {
            returnPoints.addAll(tree.findInCircle(p.getX(), p.getY(), 2));
            returnPoints2.addAll(tree.findInCircle(p.getX(), p.getY(), 2));
        }

        // if the 'found' set = 'created' set, the find works
        if (points.equals(returnPoints)){
            System.out.println("Pass");
        } else {
            System.out.println("Fail! ):");
        }
         boolean pass = true;
        for (Dot p:points){
            if (!returnPoints.contains(p)){ // why does contains not work? What's going on?
                pass = false;
            }
        }
        System.out.println(pass);
        //
        System.out.println(points + "\n" + returnPoints);
        if (returnPoints.containsAll(points) && points.containsAll(returnPoints)) {
            System.out.println("TreeSet test passed!");
        } else {
            System.out.println("TreeSet test failed!!! ):");
        }
        System.out.println(points.size() +","+ returnPoints.size());
        Set<Dot> temp = returnPoints;
        returnPoints.removeAll(points);
        points.removeAll(temp);

        System.out.println("Elements in returnPoints not in points: " + returnPoints);
        System.out.println("Elements in points not in returnPoints: " + points);
    }



    /**
     * DrawingGUI method, here toggling the mode between 'a' and 'q'
     * and increasing/decresing mouseRadius via +/-
     */
    @Override
    public void handleKeyPress(char key) {
        if (key=='a' || key=='q') mode = key;
        else if (key=='+') {
            mouseRadius += 10;
        }
        else if (key=='-') {
            mouseRadius -= 10;
            if (mouseRadius < 0) mouseRadius=0;
        }
        else if (key=='m') {
            trackMouse = !trackMouse;
        }
        else if (key=='0') {
            test0();
        }
        else if (key=='1') {
            test1();
        }
        // TODO: YOUR CODE HERE -- your test cases
        if (key == '0'){
            test0();
            repaint();
        }
        if (key == '1'){
            test1();
            repaint();
        }

        if (key == '2'){
            test2();
            repaint();
        }

        repaint();
    }

    /**
     * DrawingGUI method, here drawing the quadtree
     * and if in query mode, the mouse location and any found dots
     */
    @Override
    public void draw(Graphics g) {
        if (tree != null) drawTree(g, tree, 0);
        if (mode == 'q') {
            g.setColor(Color.BLACK);
            g.drawOval(mouseX-mouseRadius, mouseY-mouseRadius, 2*mouseRadius, 2*mouseRadius);
            if (found != null) {
                g.setColor(Color.BLACK);
                for (Dot d : found) {
                    g.fillOval((int)d.getX()-dotRadius, (int)d.getY()-dotRadius, 2*dotRadius, 2*dotRadius);
                }
            }
        }
    }

    /**
     * Draws the dot tree
     * @param g		the graphics object for drawing
     * @param tree	a dot tree (not necessarily root)
     * @param level	how far down from the root qt is (0 for root, 1 for its children, etc.)
     */
    public void drawTree(Graphics g, PointQuadtree<Dot> tree, int level) {
        // Set the color for this level
        g.setColor(rainbow[level % rainbow.length]);
        // Determine x and y for the node
        int x = (int)(tree.getPoint().getX());
        int y = (int)(tree.getPoint().getY());
        // Draw this node's dot and lines through it
        // TODO: YOUR CODE HERE
        g.drawOval(x-dotRadius, y-dotRadius, 2*dotRadius, 2*dotRadius);
        g.drawLine(tree.getX1(), y, tree.getX2(), y);
        g.drawLine(x, tree.getY1(), x, tree.getY2());
        // Recurse with children
        // TODO: YOUR CODE HERE
        for (int i = 1; i<5; i++){
            if (tree.hasChild(i)) {
                drawTree(g, tree.getChild(i), level + 1);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new DotTreeGUI();
            }
        });
    }
}
