package src;

import java.awt.Color;
import java.awt.Graphics;

/**
 * A rectangle-shaped Shape
 * Defined by an upper-left corner (x1,y1) and a lower-right corner (x2,y2)
 * with x1<=x2 and y1<=y2
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author CBK, updated Fall 2016
 */
public class Rectangle implements Shape {
	// TODO: YOUR CODE HERE
	private int x1, y1, x2, y2;		// upper left and lower right
	private Color color;

	// new, empty rectangle
	public Rectangle(int x1, int y1, Color color) {
		this.x1 = x1; this.x2 = x1;
		this.y1 = y1; this.y2 = y1;
		this.color = color;
	}
	// rectangle constructor with all sides
	public Rectangle(int x1, int y1, int x2, int y2, Color color) {
		setCorners(x1, y1, x2, y2);
		this.color = color;
	}
	/**
	 * Redefines the rectangle based on new corners
	 */
	public void setCorners(int x1, int y1, int x2, int y2) {
		// Ensure correct upper left and lower right
		this.x1 = Math.min(x1, x2);
		this.y1 = Math.min(y1, y2);
		this.x2 = Math.max(x1, x2);
		this.y2 = Math.max(y1, y2);
	}

	/**
	 * move the rectangle by a distance x and distance y
	 * @param dx
	 * @param dy
	 */
	@Override
	public synchronized void moveBy(int dx, int dy) {
		x1 += dx; y1 += dy;
		x2 += dx; y2 += dy;
	}

	/**
	 * getter method for color
	 * @return
	 */
	@Override
	public Color getColor() {
		return color;
	}

	/**
	 * setter method for color
	 * @param color The shape's color
	 */
	@Override
	public synchronized void setColor(Color color) {
		this.color = color;
	}

	/** returns if in the box
	 * @param x
	 * @param y
	 * @return
	 */
	@Override
	public boolean contains(int x, int y) {
		return x > x1 && y > y1 && x < x2 && y < y2;
	}

	/**
	 * draw the rectangle
	 * @param g
	 */
	@Override
	public void draw(Graphics g) {
		g.setColor(color);
		g.fillRect(x1, y1, x2-x1, y2-y1);
	}

	@Override
	public Shape deepCopy() {
		return new Rectangle(this.x1, this.y1, this.x2, this.y2, new Color(this.color.getRGB()));
	}

	/**
	 * return the object's data as a parseable string
 	 * @return
	 */
	public String toString() {
		return "rectangle:" +getColor().getRGB() + ","+ x1 + "," + y1 + "," + x2 + "," + y2;
	}
}
