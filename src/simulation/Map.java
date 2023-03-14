package simulation;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import camels.CamelFactory;
import events.EventManager;
import path_calculation.APathCalculator;
import path_calculation.AStarPathCalculator;
import path_calculation.CentrePathCalculator;
import path_calculation.DijkstraPathCalculator;
import path_calculation.FloydWarshallPathCalculator;
import path_calculation.Path;
import path_calculation.PathDescriptor;
import requests.Request;
import requests.RequestManager;
import requests.RequestState;

/**
 * Represent the map of the simulation.
 * 
 * @author Stanislav Kafara, Jakub Krizanovsky
 */
public class Map {
	
	
	
	private static RequestManager REQUEST_MANAGER = RequestManager.getInstance();
	private static final EventManager EVENT_MANAGER = EventManager.getInstance();
	private static final CamelFactory CAMEL_FACTORY = CamelFactory.getInstance();
	
	/** Maximum matrix size for Floyd-Warshall to be able to get selected */
	private static final int MAX_FLOYD_WARSHALL_MB = 1024; // 1 GB
	/** Minimum graph density for Floyd-Warshall to be selected */
	private static final double MIN_FLOYD_WARSHALL_DENSITY = 0.2; // 1 GB
	
	/** Maximum number of warehouses to consider while processing a request */
	private static final int MAX_WAREHOUSES_DURING_PROCESS = 10;
	/** Whether A* should be selected insted of Dijkstra */
	private static final boolean USE_ASTAR_INSTEAD_OF_DIJKSTRA = false;
	
	private Warehouse[] warehouses;
	private Oasis[] oases;
	private Node[] nodes;
	
	/** Selected path calculator */
	private APathCalculator pathCalculator;
	
	/** The one and only instance of this class (singleton) */
	private static final Map INSTANCE = new Map();
	/** Private constructor (singleton) */
	private Map() {}
	/**
	 * Method to get the single instance of Map
	 * @return the single instance of Map
	 */
	public static Map getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Sets the map data and path calculator.
	 * @param warehouses Warehouses.
	 * @param oases Oases.
	 * @param paths Paths between nodes.
	 */
	public void setMap(Warehouse[] warehouses, Oasis[] oases, Path[] paths) {
		this.warehouses = warehouses;
		this.oases = oases;
		
		fillNodes();
		Path[] preprocessedPaths = preprocessPaths(paths);
		setPathCalculators(warehouses, oases, preprocessedPaths);
		
		REQUEST_MANAGER = RequestManager.getInstance();
	}
	
	/**
	 * Returns the warehouses.
	 * @return Warehouses.
	 */
	public Warehouse[] getWarehouses() {
		return warehouses;
	}
	
	
	private void setPathCalculators(Warehouse[] warehouses, Oasis[] oases, Path[] paths) {
		int vertices = warehouses.length + oases.length;
		int edges = 2 * paths.length;
		double density = edges / (vertices*(vertices-1.0));
		
		long matricesMiB = (2L * 8L * vertices * vertices) / 1048576L;
		
		System.out.println("Vertices: " + vertices);
		System.out.println("Edges: " + edges);
		System.out.println("Matrix size: " + matricesMiB + " MB");
		System.out.println("Density: " + density);
		
		int centreNodeIndex = CentrePathCalculator.isApplicable(nodes, paths);
		if (centreNodeIndex >= 0) {
			System.out.println("%%%%%%%%%%%%%%%%%%%%%  CENTRE  %%%%%%%%%%%%%%%%%%%%%%%%");
			pathCalculator = new CentrePathCalculator(nodes, centreNodeIndex);
			sortWarehousesForCentre();
			
		} else if (matricesMiB <= MAX_FLOYD_WARSHALL_MB && density >= MIN_FLOYD_WARSHALL_DENSITY) {
			System.out.println("%%%%%%%%%%%%%%%%  FLOYD WARSHALL  %%%%%%%%%%%%%%%%%%%%%");
			pathCalculator = new FloydWarshallPathCalculator(nodes, paths);
		} else if (USE_ASTAR_INSTEAD_OF_DIJKSTRA) {
			System.out.println("%%%%%%%%%%%%%%%%%%%%%%  A*  %%%%%%%%%%%%%%%%%%%%%%%%%%%");
			pathCalculator = new AStarPathCalculator(nodes, paths);
		} else {
			System.out.println("%%%%%%%%%%%%%%%%%%%  DIJKSTRA  %%%%%%%%%%%%%%%%%%%%%%%%");
			pathCalculator = new DijkstraPathCalculator(nodes, paths);
		}
	}

	/**
	 * Tries to process a request.
	 * Sorts the warehouses by distance from the oasis.
	 * Calculates paths from specified number of warehouses.
	 * Distributes the request from satisfying warehouses.
	 * If request was not fully distributed, it is added to the unfinished requests
	 * and the process is tried again later.
	 * @param request Request.
	 */
	public void processRequest(Request request) {

		int oasisNodeIndex = oasisToNodeIndex(request.getOasisIndex());
		
		Warehouse[] warehousesSorted = warehouses;
		if(!(pathCalculator instanceof CentrePathCalculator)) {
			warehousesSorted = warehouses.clone();
			Arrays.sort(warehousesSorted, new Comparator<Warehouse>() {
				@Override
				public int compare(Warehouse o1, Warehouse o2) {
					return (int)Math.signum(o1.getCoords().airDistanceTo(nodes[oasisNodeIndex].getCoords()) - o2.getCoords().airDistanceTo(nodes[oasisNodeIndex].getCoords())); 
				}

			});
			
		}
		
		int warehouseCounter = 0;
		for (int w = 0; w < warehousesSorted.length; w++) {
			Warehouse warehouse = warehousesSorted[w];
			
			if (warehouse.getBasketCount() == 0) {
				continue;
			}
			
			if(warehouseCounter++ == MAX_WAREHOUSES_DURING_PROCESS) {
				break;	
			}
			
			PathDescriptor pathDescriptor = pathCalculator.getShortestPath(warehouse.getIndex(), oasisNodeIndex);
			
			int basketAmount = Math.min(warehouse.getBasketCount(), request.getBasketsRemaining());

			if (pathDescriptor.distance != Double.POSITIVE_INFINITY && warehouse.isDeliverable(request, pathDescriptor)) {
				warehouse.distribute(request, basketAmount, pathDescriptor);
				REQUEST_MANAGER.changeRequestState(request, RequestState.Active);
				request.reduceBasketsRemaining(basketAmount);
				if(request.getBasketsRemaining() == 0) {
					return;
				}
			}
		}
		
		addToUnfinished(request);
	}
	
	private void addToUnfinished(Request request) {
		//System.out.println("Adding to unfinished: " + request);
		REQUEST_MANAGER.addToUnfinishedRequests(request);
		if(request.getBasketsRemaining() == request.getBasketCount()) { //Couldn't distribute any
			REQUEST_MANAGER.changeRequestState(request, RequestState.Unprocessed);
		} else if(request.getBasketsRemaining() > 0) { //Distributed just some baskets, but not all
			REQUEST_MANAGER.changeRequestState(request, RequestState.Unfinished);
		}
	}
	
	/**
	 * Tries again to process unfinished requests.
	 * @param warehouse Warehouse from which to try to distribute the unfinished requests.
	 */
	public void tryProcessUnfinishedRequests(Warehouse warehouse) {
		
		Set<Request> unfinishedRequests = REQUEST_MANAGER.getUnfinishedRequests();
		if(unfinishedRequests.size() == 0) {
			return;
		}
		
		//System.out.println("Unfinished requests: " + unfinishedRequests.size());
		for (Iterator<Request> iterator = unfinishedRequests.iterator(); iterator.hasNext();) {
			Request request = iterator.next();
			
			//If you run out of baskets simply skip the rest
			if(warehouse.getBasketCount() == 0) {
				break;
			}
			
			//Skip if warehouse is too far away from the oasis
			Oasis oasis = oases[request.getOasisIndex()];
			double maxDistance = CamelFactory.getInstance().getMaxCamelMovementSpeed() * (request.getRequestTime() + request.getDeliveryTime() - EVENT_MANAGER.getSimulationTime());
			if(oasis.getCoords().airDistanceTo(warehouse.getCoords()) > maxDistance) {
				continue;
			}
			
			
			int basketAmount = Math.min(warehouse.getBasketCount(), request.getBasketsRemaining());
			
			int oasisNodeIndex = oasisToNodeIndex(request.getOasisIndex());
			int warehouseNodeIndex = warehouse.getIndex();

			PathDescriptor pathDescriptor = pathCalculator.getShortestPath(oasisNodeIndex, warehouseNodeIndex);
			
			if(pathDescriptor.distance != Double.POSITIVE_INFINITY && warehouse.isDeliverable(request, pathDescriptor)) {
				warehouse.distribute(request, basketAmount, pathDescriptor);
				REQUEST_MANAGER.changeRequestState(request, RequestState.Active);
				request.reduceBasketsRemaining(basketAmount);
				if(request.getBasketsRemaining() == 0) { //Request is done
					iterator.remove();
					continue;
				}
			}
			
			if(request.getBasketsRemaining() <= request.getBasketCount()) {
				REQUEST_MANAGER.changeRequestState(request, RequestState.Unfinished);
			}
		}
	
	}

	private void fillNodes() {
		nodes = new Node[warehouses.length + oases.length];
		for(int i = 0; i < warehouses.length; i++) {
			nodes[i] = warehouses[i];
		}
		for(int i = 0; i < oases.length; i++) {
			nodes[i + warehouses.length] = oases[i];
		}
	}
	
	/**
	 * Removes all duplicit paths, paths from i to i and paths that are too long for any camel use
	 * @param paths paths to preprocess
	 * @return preprocessed paths
	 */
	private Path[] preprocessPaths(Path[] paths) {
		Set<Path> preprocessed = new HashSet<Path>();
		
		for(Path path : paths) {
			if((path.u == path.v)
					|| (nodes[path.u].getCoords().airDistanceTo(nodes[path.v].getCoords()) >= CAMEL_FACTORY.getMaxCamelDistance())
					|| (preprocessed.contains(path) || preprocessed.contains(path.inverse()))) {
				continue;
			}
		
			
			preprocessed.add(path);
		}
		
		Path[] arr = new Path[preprocessed.size()];
		int i = 0;
		for (Iterator<Path> iterator = preprocessed.iterator(); iterator.hasNext();) {
			arr[i++] = iterator.next();
		}
		
		return arr;
	}
	
	/**
	 * Sorts warehouses by distance from centre node when using centrePathCalculator
	 * Used during preprocessing
	 */
	private void sortWarehousesForCentre() {
		if(!(pathCalculator instanceof CentrePathCalculator)) {
			throw new IllegalArgumentException("Cannot sort warehouses for centre when not using centrePathCalculator");
		}
		Node centreNode = nodes[((CentrePathCalculator)pathCalculator).getCentreNodeIndex()];
		Arrays.sort(warehouses, new Comparator<Warehouse>() {
			@Override
			public int compare(Warehouse o1, Warehouse o2) {
				return (int)Math.signum(o1.getCoords().airDistanceTo(centreNode.getCoords()) - o2.getCoords().airDistanceTo(centreNode.getCoords())); 
			}
		});
	}
	
	/**
	 * Returns whether provided index is index of an oasis.
	 * @param index Index.
	 * @return True, if provided index is index of an oasis, else false.
	 */
	public boolean isOasisIndex(int index) {
		return index >= warehouses.length;
	}
	
	/**
	 * Returns a transformed index to node index.
	 * @param oasisIndex Index of an oasis.
	 * @return Oasis node index.
	 */
	public int oasisToNodeIndex(int oasisIndex) {
		return oasisIndex + warehouses.length;
	}
	
	/**
	 * Returns a transformed index to oasis index.
	 * @param nodeIndex Oasis node index.
	 * @return Oasis index.
	 */
	public int nodeToOasisIndex(int nodeIndex) {
		return nodeIndex - warehouses.length;
	}
	
	/**
	 * Returns a node at provided index.
	 * @param nodeIndex Index of the node.
	 * @return Node at provided index.
	 */
	public Node getNodeAtIndex(int nodeIndex) {
		return nodes[nodeIndex];
	}
	
	/**
	 * Method to get the number of oases
	 * @return the number of oases
	 */
	public int getOasisCount() {
		return oases.length;
	}
}
