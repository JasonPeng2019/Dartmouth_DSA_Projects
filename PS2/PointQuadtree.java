import java.util.ArrayList;
import java.util.List;

/**
 * A point quadtree: stores an element at a 2D position, 
 * with children at the subdivided quadrants.
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2015.
 * @author CBK, Spring 2016, explicit rectangle.
 * @author CBK, Fall 2016, generic with Point2D interface.
 * @author JASON PENG, Fall 2023, Full QuadTree with all features, Dartmouth CS 10.
 * 
 */
public class PointQuadtree<E extends Point2D> {
	private E point;							// the point anchoring this node
	private int x1, y1;							// upper-left corner of the region
	private int x2, y2;							// bottom-right corner of the region
	private PointQuadtree<E> c1, c2, c3, c4;	// children

	/**
	 * Initializes a leaf quadtree, holding the point in the rectangle
	 */
	public PointQuadtree(E point, int x1, int y1, int x2, int y2) {
		this.point = point;
		this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
	}

	// Getters
	
	public E getPoint() {
		return point;
	}

	public int getX1() {
		return x1;
	}

	public int getY1() {
		return y1;
	}

	public int getX2() {
		return x2;
	}

	public int getY2() {
		return y2;
	}

	/**
	 * Returns the child (if any) at the given quadrant, 1-4
	 * @param quadrant	1 through 4
	 */
	public PointQuadtree<E> getChild(int quadrant) {
		if (quadrant==1) return c1;
		if (quadrant==2) return c2;
		if (quadrant==3) return c3;
		if (quadrant==4) return c4;
		return null;
	}

	/**
	 * Returns whether there is a child at the given quadrant, 1-4
	 * @param quadrant	1 through 4
	 */
	public boolean hasChild(int quadrant) {
		return (quadrant==1 && c1!=null) || (quadrant==2 && c2!=null) || (quadrant==3 && c3!=null) || (quadrant==4 && c4!=null);
	}

	/**
	 * Inserts the point into the tree
	 */
	public void insert(E p2) {
		// TODO: YOUR CODE HERE
		int x = (int)(this.point.getX());
		int y = (int)(this.point.getY());
		if (p2.getX() == this.point.getX() && p2.getY() == this.point.getY()){return;}
		// if on left top corner: insert c1
		if (p2.getX() <= this.point.getX() && p2.getY() <= this.point.getY()){
			if (this.hasChild(1)){
				c1.insert(p2);
			}
			// else if leaf, make new node.
			else{
				c1 = new PointQuadtree<E>(p2, this.getX1(), this.getY1(), x, y);
			}
		// if on right top corner: insert c2
		} else if (p2.getX() > this.point.getX() && p2.getY() <= this.point.getY()) {
			if (this.hasChild(2)){
				c2.insert(p2);
			}
			// else if leaf, make new node.
			else{
				c2 = new PointQuadtree<E>(p2, x, this.getY1(), this.getX2(), y);
			}
		}
		// if on right bottom corner: insert c3
		else if (p2.getX() > this.point.getX() && p2.getY() > this.point.getY()){
			if (this.hasChild(3)){
				c3.insert(p2);
			}
			// else if leaf, make new node.
			else{
				c3 = new PointQuadtree<E>(p2, x, y, this.getX2(), this.getY2());
			}
		}
		// if on left bottom corner: insert c4
		else if (p2.getX() <= this.point.getX() && p2.getY() > this.point.getY()) {
			if (this.hasChild(4)){
				c4.insert(p2);
			}
			// else if leaf, make new node.
			else{
				c4 = new PointQuadtree<>(p2, this.getX1(), y, x, this.getY2());
			}

		}
	}
	
	/**
	 * Finds the number of points in the quadtree (including its descendants)
	 */
	public int size() {
		int count = 1;
		// TODO: YOUR CODE HERE
		for (int i = 1; i<5; i++) {
			if (hasChild(i)) {
				count += this.getChild(i).size();
			}
		} return count;
	}
	
	/**
	 * Builds a list of all the points in the quadtree (including its descendants)
	 */
	public List<E> allPoints() {
		List<E> points = new ArrayList<E>();
		// TODO: YOUR CODE HERE
		// add this point to the list
		points.add(this.point);
			//traverse the tree
		for (int i =1; i < 5; i ++){
			if (this.hasChild(i)) {
				points.addAll(getChild(i).allPoints());
			}
		}
		return points;
	}	

	/**
	 * Uses the quadtree to find all points within the circle
	 * @param cx	circle center x
	 * @param cy  	circle center y
	 * @param cr  	circle radius
	 * @return    	the points in the circle (and the qt's rectangle)
	 */
	public List<E> findInCircle(double cx, double cy, double cr) {
		// TODO: YOUR CODE HERE
		/*
		System.out.println(
				"Checking circle at (" + cx + "," + cy + ") with radius "
						+ cr + " against rectangle with boundaries (" +
						this.getX1() + "," + this.getY1() + "," + this.getX2() +
						"," + this.getY2() + ")"
		);
		*/

		ArrayList<E> pointsInCircle = new ArrayList<E>();
		// define the points px and py which are closest on the rectangle to center of the circle

		// find if any of the points closest on the rectangle are within the circle. If the circle
		// is inside the rectangle, the closest point to the center will be the center:
		if (Geometry.circleIntersectsRectangle(cx, cy, cr, this.getX1(), this.getY1(), this.getX2(), this.getY2())) {
				//if the point at this node is within the circle: add it to the list
			if (Geometry.pointInCircle(this.point.getX(), this.point.getY(), cx, cy, cr)) {
				pointsInCircle.add(this.point);}
			// search through the children
			for (int i = 1; i<5; i++){
				if (this.hasChild(i)){
					pointsInCircle.addAll(this.getChild(i).findInCircle(cx, cy, cr));
					}
				}
			}

		return pointsInCircle;
	}

	// TODO: YOUR CODE HERE for any helper methods.

	/**
	 * Helper method for debugging. Prints entire tree so when called and symbolizes depth
	 * 			with length of "--"
	 * @param prefix
	 */
	public void printTree(String prefix) {
		System.out.println(prefix + this.point + ",X1 " + this.getX1()+ ",X2 " + this.getX2() + ",Y1 "
				+ this.getY1() + ",Y2 " + this.getY2());
		if (this.hasChild(1)) c1.printTree(prefix + "--");
		if (this.hasChild(2)) c2.printTree(prefix + "--");
		if (this.hasChild(3)) c3.printTree(prefix + "--");
		if (this.hasChild(4)) c4.printTree(prefix + "--");
	}

}
