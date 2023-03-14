package path_calculation;

import java.util.LinkedList;

import simulation.Node;

/**
 * PathCalculator that is used for calculating paths in centre graphs - graphs where
 * all edges are from one centre node to other nodes and an this edge exists 
 * for each node other than the centre node.
 * 
 * @version 29.11.2022
 * @author Jakub Krizanovsky
 */
public class CentrePathCalculator extends APathCalculator {

	private final Node[] nodes;
	private final int centreNodeIndex;
	
	/**
	 * Constructor for class CentrePathCalculator
	 * @param nodes all nodes
	 * @param centreNodeIndex index of the centre node
	 */
	public CentrePathCalculator(Node[] nodes, int centreNodeIndex) {
		this.nodes = nodes;
		this.centreNodeIndex = centreNodeIndex;
	}

	/**
	 * Calculates and returns a descriptor of the shortest path between i and j.
	 * 
	 * @param i Node i.
	 * @param j Node j.
	 * @return Descriptor of the shortest path between i and j.
	 */
	@Override
	public PathDescriptor getShortestPath(int i, int j) {
		LinkedList<Integer> path = new LinkedList<Integer>();
		LinkedList<Double> partialDistances = new LinkedList<Double>();
		double totalDist = Double.POSITIVE_INFINITY;
		
		if(i != centreNodeIndex && j != centreNodeIndex) { //Path through centreNode
			path.add(i);
			path.add(centreNodeIndex);
			path.add(j);
			
			partialDistances.add(nodes[i].getCoords().airDistanceTo(nodes[centreNodeIndex].getCoords()));
			partialDistances.add(nodes[centreNodeIndex].getCoords().airDistanceTo(nodes[j].getCoords()));
			
			totalDist = partialDistances.get(0) + partialDistances.get(1);
		} else { //Is a direct path from i to j
			path.add(i);
			path.add(j);
			
			partialDistances.add(nodes[i].getCoords().airDistanceTo(nodes[j].getCoords()));
			
			totalDist = partialDistances.get(0);
		}
		return new PathDescriptor(path, totalDist, partialDistances);
	}
	
	
	/**
	 * Method to check whether CentrePathCalculator is applicable for the current graph problem
	 * @param nodes nodes of the graph
	 * @param paths paths of the graph
	 * @return index of the centre node if CentrePathCalculator is applicable, -1 otherwise
	 */
	public static int isApplicable(Node[] nodes, Path[] paths) {
		//If there are no nodes, CentrePathCalculator is not applicable (minimal example)
		if(nodes.length == 0) {
			return -1;
		}
		
		//Count node occurrences in paths
		int[] nodeCounts = new int[nodes.length];
		for(Path path : paths) {
			nodeCounts[path.u]++;
			nodeCounts[path.v]++;
		}
		
		//Find potential centre node
		int centreNode = 0;
		for(int i = 1; i < nodeCounts.length; i++) {
			if(nodeCounts[i] > nodeCounts[centreNode]) {
				centreNode = i;
			}
		}
		
		//Check if all paths lead there
		if(nodeCounts[centreNode] == paths.length) {
			if(nodeCounts[centreNode] != nodes.length - 1) {
				System.out.println("WARNING: Some nodes are inaccessible");
				return -1;
			}
			return centreNode;
		} 
		
		return -1;
	}


	/**
	 * Returns centre node index.
	 * @return Centre node index.
	 */
	public int getCentreNodeIndex() {
		return centreNodeIndex;
	}
	
	

}
