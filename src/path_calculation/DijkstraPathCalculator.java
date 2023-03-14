package path_calculation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import path_calculation.UndirectedGraph.Link;
import simulation.Node;

/**
 * Path and distance calculator using Dijkstra algorithm
 * 
 * @author Stanislav Kafara, Jakub Krizanovsky
 * @version 1 22-10-22
 */
public class DijkstraPathCalculator extends APathCalculator {
	
	private static class AlgorithmResult {
		
		//private final double[] dist;
		
		private final int[] prev;
		
		public AlgorithmResult(int[] prev) {
			//this.dist = dist;
			this.prev = prev;
		}
		
	}
	
	private static class VertexDistance implements Comparable<VertexDistance> {
		
		public final int vertex;
		
		public final double distance;
		
		public VertexDistance(int vertex, double distance) {
			this.vertex = vertex;
			this.distance = distance;
		}

		@Override
		public int compareTo(VertexDistance o) {
			double difference = this.distance - o.distance;
			if (difference < 0) {
				return -1;
			}
			else if (difference > 0) {
				return +1;
			}
			else {
				return 0;
			}
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof VertexDistance)) {
				return false;
			}
			VertexDistance o = (VertexDistance) obj;
			
			return this.vertex == o.vertex && this.distance == o.distance;
		}
		
	}
	
	private final Node[] nodes;
	
	private final UndirectedGraph graph;
	
	/**
	 * Constructs a path calculator using the Dijkstra algorithm.
	 * @param nodes Nodes.
	 * @param paths Paths between the nodes.
	 */
	public DijkstraPathCalculator(Node[] nodes, Path[] paths) {
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
		AlgorithmResult result = dijkstra(i, j);
		List<Integer> path = getPathFromTo(i, j, result.prev);
		List<Double> partialDistances = getPartialDistances(path);
		double distance = 0;
		if (path.size() != 0) {
			for (Double pd : partialDistances) {
				distance += pd;
			}
		}
		else {
			distance = Double.POSITIVE_INFINITY;
		}
		return new PathDescriptor(
				path,
				distance,
				partialDistances
		);
	}
	
	private AlgorithmResult dijkstra(int i, int j) {
		PriorityQueue<VertexDistance> pQueue;
		VertexDistance minVertexDistance;
		Link neighbourLink;
		int[] mark;
		double[] dist;
		int[] prev;
		int u, v;
		double newDist;
		
		pQueue = new PriorityQueue<>();
		mark = new int[graph.verticesCount];
		dist = new double[graph.verticesCount];
		prev = new int[graph.verticesCount];
		for (v = 0; v < graph.verticesCount; v++) {
			dist[v] = Double.POSITIVE_INFINITY;
			prev[v] = -1;
			//pQueue.add(new VertexDistance(v, dist[v]));
		}
		
		mark[i] = 1;
		dist[i] = 0;
		pQueue.add(new VertexDistance(i, 0));
		while (!pQueue.isEmpty()) {
			minVertexDistance = pQueue.poll();
			u = minVertexDistance.vertex;
			
			if (u == j) {
				return new AlgorithmResult(prev);
			}
			
			neighbourLink = graph.getNeighbours(u);
			while (neighbourLink != null) {
				v = neighbourLink.neighbour;
				
				if (mark[v] != 2) {
					newDist = dist[u] + nodes[u].getCoords().airDistanceTo(nodes[v].getCoords());
					
					if (mark[v] == 0) {
						mark[v] = 1;
						dist[v] = newDist;
						prev[v] = u;
						pQueue.add(new VertexDistance(v, newDist));
					}
					else if (newDist < dist[v]) {
						pQueue.remove(new VertexDistance(v, dist[v]));
						dist[v] = newDist;
						prev[v] = u;
						pQueue.add(new VertexDistance(v, newDist));
					}
				}
				
				neighbourLink = neighbourLink.getNext();
			}
			
			mark[u] = 2;
		}
		
		return new AlgorithmResult(prev);
	}

	private void initGraphEdges(Path[] paths) {		
		for (Path path : paths) {
			double distance = nodes[path.u].getCoords().airDistanceTo(nodes[path.v].getCoords());
			graph.addEdge(path.u, path.v, distance);
		}
		

	}
	
	private List<Integer> getPathFromTo(int i, int j, int[] prev) {
		LinkedList<Integer> path = new LinkedList<>();
		if (prev[j] == -1) {
			return path;
		}
		
		path.addFirst(j);
		/*while (prev[j] != i) {
			path.addFirst(prev[j]);
			j = prev[j];
		}*/
		int k = j; // PMD
		while (prev[k] != i) {
			path.addFirst(prev[k]);
			k = prev[k];
		}
		path.addFirst(i);
		
		return path;
	}
	
	private List<Double> getPartialDistances(List<Integer> path) {
		LinkedList<Double> partialDistances = new LinkedList<>();
		int i, j;
		if (path.size() == 0) {
			return partialDistances;
		}
		
		Iterator<Integer> it = path.iterator();
		i = it.next();
		while (it.hasNext()) {
			j = it.next();
			
			partialDistances.add(nodes[i].getCoords().airDistanceTo(nodes[j].getCoords()));
			
			i = j;
		}
		
		return partialDistances;
	}
	
}

class UndirectedGraph {
	
	class Link {
		
		public final int neighbour;
		
		public final double distance;
		
		private final Link next;
		
		public Link(int neighbour, double distance, Link next) {
			this.neighbour = neighbour;
			this.distance = distance;
			this.next = next;
		}
		
		public Link getNext() {
			return this.next;
		}
		
	}
	
	public final int verticesCount;
	
	private final Link[] edges;
	
	public UndirectedGraph(int verticesCount) {
		this.verticesCount = verticesCount;
		this.edges = new Link[verticesCount];
	}
	
	public void addEdge(int i, int j, double distance) {
		edges[i] = new Link(j, distance, edges[i]);
		edges[j] = new Link(i, distance,  edges[j]);
	}
	
	public Link getNeighbours(int i) {
		return edges[i];
	}
	
}
