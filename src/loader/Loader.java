package loader;

import java.util.Iterator;

import camels.CamelType;
import path_calculation.Path;
import requests.Request;
import simulation.Coordinates;
import simulation.Oasis;
import simulation.Warehouse;

/**
 * Provides useful methods for loading parsed data
 * 
 * @author Stanislav Kafara, Jakub Krizanovsky
 * @version 1 06-10-22
 */
public class Loader {
	
	private Loader() {}
	
	/**
	 * String to int conversion
	 * 
	 * @param s String
	 * @return int
	 */
	private static int s2i(String s) {
		return Integer.parseInt(s);
	}
	
	/**
	 * String to double conversion
	 * 
	 * @param s String
	 * @return double
	 */
	private static double s2d(String s) {
		return Double.parseDouble(s);
	}
	
	/**
	 * Loads a warehouse.
	 * @param iter Parsed data iterator pointing to a warehouse definition
	 * @return Warehouse
	 */
	private static Warehouse loadWarehouse(Iterator<String> iter) {
		return new Warehouse(
				new Coordinates(s2d(iter.next()), s2d(iter.next())),
				s2i(iter.next()),
				s2i(iter.next()),
				s2i(iter.next())
		);
	}

	/**
	 * Loads warehouses.
	 * @param iter Parsed data iterator pointing to warehouses definitions
	 * @return Array of warehouses
	 */
	public static Warehouse[] loadWarehouses(Iterator<String> iter) {
		int count = s2i(iter.next());
		Warehouse[] warehouses = new Warehouse[count];
		
		for (int i = 0; i < count; i++) {
			warehouses[i] = loadWarehouse(iter);
		}
		
		return warehouses;
	}
	
	/**
	 * Loads an oasis.
	 * @param iter Parsed data iterator pointing to a oasis definition
	 * @return Oasis
	 */
	private static Oasis loadOasis(Iterator<String> iter) {
		return new Oasis(
				new Coordinates(s2d(iter.next()), s2d(iter.next()))
		);
	}
	
	/**
	 * Loads oases.
	 * @param iter Parsed data iterator pointing to oases definitions
	 * @return Array of oases
	 */
	public static Oasis[] loadOases(Iterator<String> iter) {
		int count = s2i(iter.next());
		Oasis[] oases = new Oasis[count];
		
		for (int i = 0; i < count; i++) {
			oases[i] = loadOasis(iter);
		}
		
		return oases;
	}
	
	/**
	 * Loads a path.
	 * @param iter Parsed data iterator pointing to a path definition
	 * @return Path
	 */
	private static Path loadPath(Iterator<String> iter) {
		return new Path(s2i(iter.next()) - 1, s2i(iter.next()) - 1);
	}
	
	/**
	 * Loads paths.
	 * @param iter Parsed data iterator pointing to paths definitions
	 * @return Array of paths
	 */
	public static Path[] loadPaths(Iterator<String> iter) {
		int count = s2i(iter.next());
		Path[] paths = new Path[count];
		
		for (int i = 0; i < count; i++) {
			paths[i] = loadPath(iter);
		}
		
		return paths;
	}
	
	/**
	 * Loads a camel type.
	 * @param iter Parsed data iterator pointing to a camel type definition
	 * @return Camel type
	 */
	private static CamelType loadCamelType(Iterator<String> iter) {
		return new CamelType(
				iter.next(),
				s2d(iter.next()), s2d(iter.next()), // min, max speed
				s2d(iter.next()), s2d(iter.next()), // min, max distance
				s2d(iter.next()), s2i(iter.next()),
				s2d(iter.next())
		);
	}
	
	/**
	 * Loads camel types.
	 * @param iter Parsed data iterator pointing to camel types definitions
	 * @return Array of camel types
	 */
	public static CamelType[] loadCamelTypes(Iterator<String> iter) {
		int count = s2i(iter.next());
		CamelType[] camelTypes = new CamelType[count];
		
		for (int i = 0; i < count; i++) {
			camelTypes[i] = loadCamelType(iter);
		}
		
		return camelTypes;
	}
	
	/**
	 * Loads a request.
	 * @param iter Parsed data iterator pointing to a request definition
	 * @return Request
	 */
	private static Request loadRequest(Iterator<String> iter) {
		return new Request(
				s2d(iter.next()),
				s2i(iter.next()) - 1,
				s2i(iter.next()),
				s2d(iter.next())
		);
	}
	
	/**
	 * Loads requests.
	 * @param iter Parsed data iterator pointing to requests definitions
	 * @return Array of requests
	 */
	public static Request[] loadRequests(Iterator<String> iter) {
		int count = s2i(iter.next());
		Request[] requests = new Request[count];
		
		for (int i = 0; i < count; i++) {
			requests[i] = loadRequest(iter);
		}
		
		return requests;
	}
	
}
