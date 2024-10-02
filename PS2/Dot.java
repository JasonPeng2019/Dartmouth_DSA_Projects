/**
 * A very simple implementation of a class implementing the Point2D interface
 * Called it a "Dot" to distinguish from Point
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2016
 */
public class Dot implements Point2D, Comparable<Dot> {
    private double x, y;

    public Dot(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String toString() {
        return "("+x+","+y+")";
    }

    /**
    equals method added for my Test 2, in which I want to make 1000 points and find them all using a HashSet.
     */

    public boolean equals(Object obj) {
        Dot d = (Dot)obj;
        if ((int)this.x == (int)d.getX() && (int)this.y == (int)d.y){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(Dot otherDot) {
        // Compare Dots based on their x-coordinates.
        int xComparison = Double.compare(this.x, otherDot.x);

        // If x-coordinates are equal, compare based on y-coordinates.
        if (xComparison == 0) {
            return Double.compare(this.y, otherDot.y);
        }

        return xComparison;
    }


    /**
     hashCode method added for my Test 2, in which I want to make 1000 points and find them all with a HashSet.
     */
    public int hashCode() {
        return (int)x;
    }

}
