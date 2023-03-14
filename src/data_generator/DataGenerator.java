/**
 * 
 */
package data_generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import path_calculation.Path;
import simulation.Coordinates;

/**
 * Library class that is used for generating new random data sets
 * 
 * @version 29.11.2022
 * @author Jakub Krizanovsky
 */
public class DataGenerator {
	
	private static final Random R = new Random();
	private static PrintWriter out;
	
	private static final double MAP_MAX_SIZE = 3000;
	
	private static final int MIN_WAREHOUSE_COUNT = 1;
	private static final int MAX_WAREHOUSE_COUNT = 750;
	private static final int MIN_WAREHOUSE_BASKET_COUNT = 1;
	private static final int MAX_WAREHOUSE_BASKET_COUNT = 10;
	private static final int MIN_WAREHOUSE_BASKET_REFILL_TIME = 10;
	private static final int MAX_WAREHOUSE_BASKET_REFILL_TIME  = 100;
	private static final int MIN_WAREHOUSE_BASKET_MANIPULATION_TIME = 1;
	private static final int MAX_WAREHOUSE_BASKET_MANIPULATION_TIME  = 5;
	
	private static final int MIN_OASIS_COUNT = 1;
	private static final int MAX_OASIS_COUNT = 2000;
	
	private static final double MIN_PATH_RATIO = 0.3;
	private static final double MAX_PATH_RATIO = 0.6;
	
	private static final int MIN_CAMEL_TYPE_COUNT = 1;
	private static final int MAX_CAMEL_TYPE_COUNT = 5;
	private static final double MIN_CAMEL_SPEED = 1;
	private static final double MAX_CAMEL_SPEED = 100;
	private static final double MIN_CAMEL_DISTANCE = 10;
	private static final double MAX_CAMEL_DISTANCE = 1000;
	private static final double MIN_CAMEL_DRINK_TIME = 1;
	private static final double MAX_CAMEL_DRINK_TIME = 10;
	private static final int MIN_CAMEL_MAX_LOAD = 1;
	private static final int MAX_CAMEL_MAX_LOAD = 20;
	
	private static final int MIN_REQUEST_COUNT = 1;
	private static final int MAX_REQUEST_COUNT = 5000;
	
	private static final double MIN_REQUEST_DELAY = 0;
	private static final double MAX_REQUEST_DELAY = 3;
	private static final int MIN_REQUEST_BASKET_COUNT = 1;
	private static final int MAX_REQUEST_BASKET_COUNT = 50;
	private static final double MIN_REQUEST_DELIVERY_TIME = 100;
	private static final double MAX_REQUEST_DELIVERY_TIME = 1000;
	
	/**
	 * Constructs a data generator.
	 */
	public DataGenerator() {
		//No actions
	}
	
	/**
	 * Generates simulation data and outputs them to a file
	 * @param file file to output the data to
	 */
	public static void generateData(File file) {
		
		try {
			out = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Failed to generate data");
			return;
		}
		
		//Warehouses
		out.println("🐪 Pocet skladu S: 🏜");
		int warehouseCount = getRandomInt(MIN_WAREHOUSE_COUNT, MAX_WAREHOUSE_COUNT);
		out.println(warehouseCount);
		
		generateWarehouses(out, warehouseCount);
		
		//Oases
		out.println("🐪 Pocet oaz O: 🏜");
		int oasisCount = getRandomInt(MIN_OASIS_COUNT, MAX_OASIS_COUNT);
		out.println(oasisCount);
		
		generateOases(out, oasisCount);
		
		//Paths
		out.println("🐪 Pocet cest C: 🏜");
		int combinations = combinationCount(warehouseCount + oasisCount);
		int pathCount = getRandomInt((int)(combinations*MIN_PATH_RATIO), (int)(combinations*MAX_PATH_RATIO));
		out.println(pathCount);
		
		generatePaths(pathCount, warehouseCount, oasisCount, out);
		
		//Camels
		out.println("🐪 Pocet druhu velbloudu D: 🏜");
		int camelTypeCount = getRandomInt(MIN_CAMEL_TYPE_COUNT, MAX_CAMEL_TYPE_COUNT);
		out.println(camelTypeCount);
		
		generateCamelTypes(camelTypeCount, out);
		
		//Requests
		out.println("🐪 Pocet pozadavku P: 🏜");
		int requestCount = getRandomInt(MIN_REQUEST_COUNT, MAX_REQUEST_COUNT);
		out.println(requestCount);
		
		generateRequests(requestCount, oasisCount, out);
		
		out.close();
	}


	private static void generateWarehouses(PrintWriter out, int warehouseCount) {
		out.println("🐪 Definice skladu (souradnice x,y, pocet kosu ks, doba doplneni ts a doba nalozeni tn) 🏜");
		for(int i = 0; i < warehouseCount; i++) {
			Coordinates coords = getRandomCoords();
			int basketCount = getRandomInt(MIN_WAREHOUSE_BASKET_COUNT, MAX_WAREHOUSE_BASKET_COUNT);
			int basketRefillTime = getRandomInt(MIN_WAREHOUSE_BASKET_REFILL_TIME, MAX_WAREHOUSE_BASKET_REFILL_TIME);
			int basketManipulationTime = getRandomInt(MIN_WAREHOUSE_BASKET_MANIPULATION_TIME, MAX_WAREHOUSE_BASKET_MANIPULATION_TIME);
			out.println(coords.x + " " + coords.y + " " + basketCount + " " + basketRefillTime + " " + basketManipulationTime);
		}
	}
	
	/**
	 * Generetes oases
	 * @param out output file PrintWriter
	 * @param oasisCount number of oases to generate
	 */
	private static void generateOases(PrintWriter out, int oasisCount) {
		out.println("🐪 Definice oaz (souradnice x,y) 🏜");
		for(int i = 0; i < oasisCount; i++) {
			Coordinates coords = getRandomCoords();
			out.println(coords.x + " " + coords.y);
		}
	}
	
	/**
	 * Generate paths
	 * @param pathCount number of paths to generate
	 * @param warehouseCount number of warehouses
	 * @param oasisCount number of oases
	 * @param out output file PrintWriter
	 */
	private static void generatePaths(int pathCount, int warehouseCount, int oasisCount, PrintWriter out) {
		out.println("🐪 Definice cest 🏜");
		Set<Path> paths = new HashSet<Path>();
		while(paths.size() < pathCount) {
			Path path = getRandomPath(warehouseCount + oasisCount);
			if(!paths.contains(path)) {
				paths.add(path);
				out.println(path.u + " " + path.v);
			}
		}
	}
	
	/**
	 * Generates camel types
	 * @param camelTypeCount number of types to generate
	 * @param out output file PrintWriter
	 */
	private static void generateCamelTypes(int camelTypeCount, PrintWriter out) {
		out.println("🐪 Definice velblouda (nazev, minimalni rychlost, maximalni rychlost, minimalni vzdalenost, maximalni vzdalenost, doba piti, maximalni zatizeni a procentualni pomer druhu velblouda)  🏜");
		for(int i = 1; i <= camelTypeCount; i++) {
			String typeName = "Type" + i;
			double minSpeed = getRandomDouble(MIN_CAMEL_SPEED, MAX_CAMEL_SPEED);
			double maxSpeed = getRandomDouble(MIN_CAMEL_SPEED, MAX_CAMEL_SPEED);
			if(minSpeed > maxSpeed) { //Swap them if they're the other way around
				double temp = minSpeed;
				minSpeed = maxSpeed;
				maxSpeed = temp;
			}
			
			double minDistance = getRandomDouble(MIN_CAMEL_DISTANCE, MAX_CAMEL_DISTANCE);
			double maxDistance = getRandomDouble(MIN_CAMEL_DISTANCE, MAX_CAMEL_DISTANCE);
			if(minDistance > maxDistance) { //Swap them if they're the other way around
				double temp = minDistance;
				minDistance = maxDistance;
				maxDistance = temp;
			}
			
			double drinkTime = getRandomDouble(MIN_CAMEL_DRINK_TIME, MAX_CAMEL_DRINK_TIME);
			int maxĹoad = getRandomInt(MIN_CAMEL_MAX_LOAD, MAX_CAMEL_MAX_LOAD);
			double perc = 1.0/camelTypeCount;
			out.println(typeName + " " + minSpeed + " " + maxSpeed + " " + minDistance + " " + maxDistance + " " + drinkTime + " " + maxĹoad + " " + perc);
		}
	}
	
	/**
	 * Generates requests
	 * @param requestCount number of requests to generate
	 * @param oasisCount number of oases
	 * @param out output file PrintWriter
	 */
	private static void generateRequests(int requestCount, int oasisCount, PrintWriter out) {
		out.println("🐪 Definice pozadavku (cas prichodu, index oazy, pocet kosu, cas na doruceni) 🏜");
		double reqiestTime = 0;
		for(int i = 0; i < requestCount; i++) {
			reqiestTime += getRandomDouble(MIN_REQUEST_DELAY, MAX_REQUEST_DELAY);
			int oasisIndex = getRandomInt(1, oasisCount);
			int basketCount = getRandomInt(MIN_REQUEST_BASKET_COUNT, MAX_REQUEST_BASKET_COUNT);
			double deliveryTime = getRandomDouble(MIN_REQUEST_DELIVERY_TIME, MAX_REQUEST_DELIVERY_TIME);
			out.println(reqiestTime + " " + oasisIndex + " " + basketCount + " " + deliveryTime);
		}
	}
	
	/**
	 * Generates new coordinates
	 * @return the newly generated coordinates
	 */
	private static Coordinates getRandomCoords() {
		return new Coordinates(R.nextDouble()*MAP_MAX_SIZE - MAP_MAX_SIZE/2, R.nextDouble()*MAP_MAX_SIZE - MAP_MAX_SIZE/2);
	}
	
	/**
	 * Generates random path
	 * @param nodeCount number of nodes
	 * @return the generated path
	 */
	private static Path getRandomPath(int nodeCount) {
		int u = getRandomInt(1, nodeCount);
		int v = getRandomInt(1, nodeCount);
		if(u == v)  {
			return getRandomPath(nodeCount);
		}
					
		Path path = new Path(u, v);
		return path;
	}
	
	/**
	 * Generates random integer between min and max values
	 * @param min minimal value
	 * @param max maximal value
	 * @return the generated integer
	 */
	private static int getRandomInt(int min, int max) {
		if(min == max) { //This is here because Random.nextInt() can't take 0 as a parameter
			return min;
		}
		return R.nextInt(max - min) + min;
	}
	
	/**
	 * Generates random double between min and max values
	 * @param min minimal value
	 * @param max maximal value
	 * @return the generated double
	 */
	private static double getRandomDouble(double min, double max) {
		return R.nextDouble()*(max - min) + min;
	}
	
	/**
	 * Calculates maximal number of path combinations in a graph
	 * @param c number of nodes
	 * @return number of possible path combinations
	 */
	private static int combinationCount(int c) {
		// = c! / (c - 2)! = c * (c-1)
		return c*(c-1); 
	}
}
