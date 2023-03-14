package simulation;

/**
 * Represents an oasis of the map.
 * @author Jakub Krizanovsky
 */
public class Oasis extends Node {

	private static int instanceCounter = 0;
	
	private final int index;

	/**
	 * Constructs an oasis.
	 * @param coords Oasis coordinates.
	 */
	public Oasis(Coordinates coords) {
		super(coords);
		index = instanceCounter++;
	}

	/**
	 * Returns the index of the oasis plus one.
	 * @return Index of the oasis plus one.
	 */
	public int getIndexPlusOne() {
		return index + 1;
	}
}
