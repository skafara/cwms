package simulation;

/**
 * Represents a node of the map.
 * @author Jakub Krizanovsky
 */
public class Node {
	
	private final Coordinates coords;

	/**
	 * Constructs a node.
	 * @param coords Node coordinates.
	 */
	public Node(Coordinates coords) {
		this.coords = coords;
	}

	/**
	 * Returns the node coordinates.
	 * @return Node coordinates.
	 */
	public Coordinates getCoords() {
		return coords;
	}
}
