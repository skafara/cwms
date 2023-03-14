package path_calculation;

import java.util.List;

/**
 * Path descriptor
 * 
 * @author Stanislav Kafara, Jakub Krizanovsky
 * @version 1 22-10-22
 */
public class PathDescriptor {
	
	/** Sequence of nodes */
	public final List<Integer> path;
	
	/** Distance between i and j */
	public final double distance;
	
	/**
	 * Partial distances between i and j
	 * - 0: i and x1
	 * - 1: x1 and x2
	 * - 2: x2 and x3
	 * - ...
	 * - n-1: x(n-2) and x(n-1)
	 * - n: x(n-1) and j
	 */
	public final List<Double> partialDistances;
	
	/**
	 * Constructs path descriptor
	 * 
	 * @param path Path.
	 * @param distance Path length.
	 * @param partialDistances Partial paths lengths.
	 */
	public PathDescriptor(List<Integer> path, double distance, List<Double> partialDistances) {
		this.path = path;
		this.distance = distance;
		this.partialDistances = partialDistances;
	}
	
}
