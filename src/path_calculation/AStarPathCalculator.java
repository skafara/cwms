package path_calculation;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

import path_calculation.UndirectedGraph.Link;
import simulation.Node;

/**
 * Represents an A* path calculation algorithm.
 * 
 * @author Jakub Krizanovsky
 */
public class AStarPathCalculator extends APathCalculator {
	
	private final Node[] nodes;
	
	private final UndirectedGraph graph;
	
	/**
	 * Constructs an A* path calculator.
	 * @param nodes Nodes.
	 * @param paths Paths between nodes.
	 */
	public AStarPathCalculator(Node[] nodes, Path[] paths) {
		this.nodes = nodes;
		this.graph = new UndirectedGraph(nodes.length);
		initGraphEdges(paths);
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
		PriorityQueue<Entry> openSet = new PriorityQueue<Entry>();
		int[] cameFrom = new int[nodes.length];
		Arrays.fill(cameFrom, -1);
		
	
		double[] gScore = new double[nodes.length];
		double[] fScore = new double[nodes.length];
		Arrays.fill(gScore, Double.POSITIVE_INFINITY);
		Arrays.fill(fScore, Double.POSITIVE_INFINITY);
		
		
		gScore[i] = 0;
		fScore[i] = hCost(i, j);
		
		openSet.add(new Entry(i, fScore[i]));
		
		while(!openSet.isEmpty()) {
			int current = openSet.poll().n;
			if(current == j) {
				return reconstructPath(cameFrom, current);
			}
			
			Link neighborLink = graph.getNeighbours(current);
			while(neighborLink != null) {
				int neighbor = neighborLink.neighbour;
				double tentativeGScore = gScore[current] + nodes[current].getCoords().airDistanceTo(nodes[neighbor].getCoords());
				if(tentativeGScore < gScore[neighbor]) {
					cameFrom[neighbor] = current;
					gScore[neighbor] = tentativeGScore;
					fScore[neighbor] = tentativeGScore + hCost(neighbor, j);
					
					boolean contains = false;
					for (Iterator<Entry> iterator = openSet.iterator(); iterator.hasNext();) {
						if(iterator.next().n == current) {
							contains = true;
							break;
						}
					}
					
					if(!contains) {
						openSet.add(new Entry(neighbor, fScore[neighbor]));
					}
					
					neighborLink = neighborLink.getNext();
				}
			}
		}
		
		System.out.println("Path from: " + i + ", to: " + j + " not found.");
		return new PathDescriptor(new LinkedList<>(), Double.POSITIVE_INFINITY, new LinkedList<>());
	}
	
	private PathDescriptor reconstructPath(int[] cameFrom, int currentIndex) {
		LinkedList<Integer> path = new LinkedList<Integer>();
		LinkedList<Double> partialDistances = new LinkedList<Double>();
		int current = currentIndex;
		path.add(current);
		while(cameFrom[current] != -1) {
			int last = current;
			current = cameFrom[current];
			path.addFirst(current);
			partialDistances.addFirst(nodes[current].getCoords().airDistanceTo(nodes[last].getCoords()));
		}
		
		return new PathDescriptor(new LinkedList<Integer>(), current, new LinkedList<Double>());
	}
	
	private double hCost(int n, int goal) {
		return nodes[n].getCoords().airDistanceTo(nodes[goal].getCoords());
	}
	
	class Entry implements Comparable<Entry> {
		public final int n;
		public final double fScore;
		
		public Entry(int n, double fScore) {
			this.n = n;
			this.fScore = fScore;
		}

		@Override
		public int compareTo(Entry o) {
			return (int)Math.signum(this.fScore - o.fScore);
		}
	}
	
	private void initGraphEdges(Path[] paths) {
		for (Path path : paths) {
			double distance = nodes[path.u].getCoords().airDistanceTo(nodes[path.v].getCoords());
			graph.addEdge(path.u, path.v, distance);
		}
	}
}
