package path_calculation;

/**
 * Abstract class for path and distance calculation.
 * 
 * @author Stanislav Kafara, Jakub Krizanovsky
 * @version 1 22-10-22
 */
public abstract class APathCalculator {
	
	/**
	 * Constructs an abstract path calculator.
	 */
	public APathCalculator() {
		// PMD
	}
	
	/**
	 * Returns a descriptor of the shortest path between i and j.
	 * 
	 * @param i Node i.
	 * @param j Node j.
	 * @return Descriptor of the shortest path between i and j.
	 */
	public abstract PathDescriptor getShortestPath(int i, int j);

}
