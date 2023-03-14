package path_calculation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import simulation.Node;

/**
 * Path and distance calculator using Floyd Warshall algorithm
 * 
 * @author Jakub Krizanovsky, Stanislav Kafara
 * @version 1 22-10-22
 */
public class FloydWarshallPathCalculator extends APathCalculator {
	
	private final double[][] distMatrix;
	
	private final int[][] nextMatrix;
	
	/**
	 * Constructs the path calculator using Floyd-Warshall algorithm.
	 * @param nodes Nodes.
	 * @param paths Paths between nodes.
	 */
	public FloydWarshallPathCalculator(Node[] nodes, Path[] paths) {
		distMatrix = new double[nodes.length][nodes.length];
		nextMatrix = new int[nodes.length][nodes.length];
		initMatrices(nodes.length);
		generateDistMatrix(nodes, paths);
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
		List<Integer> path = getPathFromTo(i, j);
		List<Double> partialDistances = getPartialDistancesFromTo(path);
		return new PathDescriptor(
				path,
				distMatrix[i][j],
				partialDistances
		);
	}
	
	private void initMatrices(int nodeCount) {
		for(int i = 0; i < nodeCount; i++) {
			for(int j = 0; j < nodeCount; j++) {
				distMatrix[i][j] = Double.POSITIVE_INFINITY;
				nextMatrix[i][j] = -1;
			}
		}
		
	}
	
	private void generateDistMatrix(Node[] nodes, Path[] paths) {
		for(Path path : paths) {
			distMatrix[path.u][path.v] = nodes[path.u].getCoords().airDistanceTo(nodes[path.v].getCoords());
			distMatrix[path.v][path.u] = distMatrix[path.u][path.v];
			nextMatrix[path.u][path.v] = path.v;
			nextMatrix[path.v][path.u] = path.u;
		}
		for(int i = 0; i < nodes.length; i++) {
			distMatrix[i][i] = 0;
			nextMatrix[i][i] = i;
		}
		
		for(int k = 0; k < nodes.length; k++) {
			for(int i = 0; i < nodes.length; i++) {
				for(int j = 0; j < nodes.length; j++) {
					if(distMatrix[i][j] > distMatrix[i][k] + distMatrix[k][j]) {
						distMatrix[i][j] = distMatrix[i][k] + distMatrix[k][j];
						nextMatrix[i][j] = nextMatrix[i][k];
					}
				}
			}
		}
	}
	
	private List<Integer> getPathFromTo(int i, int j) {
		LinkedList<Integer> path = new LinkedList<Integer>();
		
		if(nextMatrix[i][j] == -1) {
			return path;
		}
		path.add(i);
		/*while(i != j) {
			i = nextMatrix[i][j];
			path.add(i);
		}*/
		int k = i;
		while(k != j) {
			k = nextMatrix[k][j];
			path.add(k);
		}
		
		return path;	
	}
	
	private List<Double> getPartialDistancesFromTo(List<Integer> path) {
		LinkedList<Double> partialDistances = new LinkedList<>();
		int i, j;
		if (path.size() == 0) {
			return partialDistances;
		}
		
		Iterator<Integer> it = path.iterator();
		i = it.next();
		while (it.hasNext()) {
			j = it.next();
			
			partialDistances.add(distMatrix[i][j]);
			
			i = j;
		}
		
		return partialDistances;
	}

}
