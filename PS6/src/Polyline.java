package src;

import java.awt.*;
import java.util.ArrayList;

/**
 * A multi-segment Shape, with straight lines connecting "joint" points -- (x1,y1) to (x2,y2) to (x3,y3) ...
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2016
 * @author CBK, updated Fall 2016
 */
public class Polyline implements Shape {
	// TODO: YOUR CODE HERE
	private ArrayList<Point> path = new ArrayList<>(); // the list of points

	private Color color;

	public Polyline(int x, int y, Color color) {
		path.add(new Point(x, y));
		this.color = color;
	}

	/**
	 * add a point to the Polyline
	 *
	 * @param x
	 * @param y
	 */
	public void addPoint(int x, int y) {
		path.add(new Point(x, y));
	}

	@Override

	/**
	 * move the polyline shape by a distance x and distance y
	 */
	public synchronized void moveBy(int dx, int dy) {
		for (int i = 0; i < path.size(); i++) {
			int tempX = path.get(i).x;
			int tempY = path.get(i).y;
			path.set(i, new Point(tempX + dx, tempY + dy));
		}
	}

	/**
	 * getter method for color
	 *
	 * @return
	 */
	@Override
	public Color getColor() {
		return color;
	}

	/**
	 * setter method for color
	 *
	 * @param color The shape's color
	 */
	@Override
	public synchronized void setColor(Color color) {
		this.color = color;
	}

	/**
	 * function that determines if a point is on the polyline object
	 *
	 * @param x
	 * @param y
	 * @return
	 */
	@Override
	public boolean contains(int x, int y) {
		for (int i = 0; i < path.size() - 1; i++) { // go through each segment
			Segment tempSeg = new Segment(path.get(i).x, path.get(i).y, path.get(i + 1).x, path.get(i + 1).y, color);
			if (tempSeg.contains(x, y)) { // if any segment contains, return true
				return true;
			}
		}
		return false; // else return false
	}

	@Override
	/**
	 * draw the polyline shape
	 */
	public void draw(Graphics g) {
		g.setColor(color);
		int tX = path.get(0).x; // previous coordinates
		int tY = path.get(0).y;
		boolean first = true; // for first, so that it can iterate through
		for (Point p : path) {
			if (path.size() == 1) { // if just a point, draw a point
				g.drawLine(tX, tY, tX, tY);
			} else { // else, draw segment(s)
				g.drawLine(tX, tY, p.x, p.y); // draw segment from previous to current
				tX = p.x; // update previous
				tY = p.y;
			}
		}
	}

	/**
	 * return object data as a parseable string
	 *
	 * @return
	 */
	@Override
	public String toString() {
		String returnString = "polyline:";
		returnString += getColor().getRGB();
		for (int i = 0; i < path.size(); i++) {
			 returnString += "," + path.get(i).x + "," + path.get(i).y;
		}
		return returnString;
	}
}
