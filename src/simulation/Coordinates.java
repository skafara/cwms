package simulation;

/**
 * Messenger that represents 2D coordinates
 * 
 * @version 29.11.2022
 * @author Jakub Krizanovsky
 */
public class Coordinates {
	
	/** X coordinate */
	public final double x;
	/** Y coordinate */
	public final double y;
	
	/**
	 * Creates a new instance of coordinates
	 * @param x x coordinate
	 * @param y y coordinate
	 */
	public Coordinates(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Getter for x
	 * @return the x coordinate
	 */
	public double getX() {
		return x;
	}
	
	/**
	 * Getter for y
	 * @return the x coordinate
	 */
	public double getY() {
		return y;
	}
	
	/**
	 * Method to calculate the distance between coordinates
	 * @param other other coordinates to get the distance to
	 * @return the distance
	 */
	public double airDistanceTo(Coordinates other) {
		return  Math.sqrt((x - other.x)*(x - other.x) + (y - other.y)*(y - other.y)); 
	}

	@Override
	public String toString() {
		return "[x=" + x + ", y=" + y + "]";
	}
	
}
