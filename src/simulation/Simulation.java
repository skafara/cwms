package simulation;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import camels.CamelFactory;
import loader.Loader;
import loader.Parser;
import path_calculation.Path;
import requests.Request;
import requests.RequestManager;
import ui.UserInterface;

/**
 * Represents the application, the simulation.
 * @author Stanislav Kafara, Jakub Krizanovsky
 * @version 2 02-12-22
 */
public class Simulation {

	private static final RequestManager REQUEST_MANAGER = RequestManager.getInstance();
	
	private static Map MAP = Map.getInstance();
	
	/** Number of delivered baskets. */
	public static int basketsDelivered = 0;
	/** Number of fulfilled requests. */
	public static int requestsFulfilled = 0;
	/** Time of the simulation start. */
	public static long startTime;
	
	/**
	 * Application entry point.
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		UserInterface ui = new UserInterface();
		while (true) {
			ui.readAndProcessInput();
		}
	}
	
	private Simulation() {}
	
	/**
	 * Loads the data into simulation.
	 * @param filename Name of the file from which to load the data.
	 * @throws IOException If there is any problem regarding files.
	 */
	public static void loadData(String filename) throws IOException {
		try {
			Iterator<String> iter = Parser.parse(filename).iterator();
			
			Warehouse[] warehouses = Loader.loadWarehouses(iter);
			
			Oasis[] oases = Loader.loadOases(iter);
			Path[] paths = Loader.loadPaths(iter);
			CamelFactory.getInstance().setCamelTypes(Loader.loadCamelTypes(iter));
			MAP.setMap(
					warehouses,
					oases,
					paths
			);
			
			Request[] requests = Loader.loadRequests(iter);

			for (Request request : requests) {
				REQUEST_MANAGER.addRequest(request);
			}
			
			System.out.println("Request count: " + requests.length);
			System.out.println("Sum baskets: " + Arrays.stream(requests).mapToInt(r -> r.getBasketCount()).sum());
		}
		catch (IOException e) {
			throw e;
		}
	}
}
