package camels;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import events.CamelDeliverEvent;
import events.CamelDrinkEvent;
import events.CamelReturnEvent;
import events.CamelWalkEvent;
import events.EventManager;
import path_calculation.PathDescriptor;
import requests.Request;
import simulation.Map;
import simulation.Node;
import simulation.Oasis;
import simulation.Statistics;
import simulation.Warehouse;

/**
 * Represents individual camels
 *
 * @author Jakub Krizanovsky, Stanislav Kafara
 * @version 2 02-12-22
 */
public class Camel {
	
	private static final Map MAP = Map.getInstance();
	private static final EventManager EVENT_MANAGER = EventManager.getInstance();
	private static final Statistics STATISTICS = Statistics.getInstance();
	
	/** Number of camels currently created */
	private static int instanceCounter = 0;
	
	private final int index;
	private final double generationTime;
	private final CamelType type;
	private final double movementSpeed;
	private final double distance;
	private final double drinkTime;
	private final int maxLoad;
	
	private Warehouse home;
	
	/** Baskets currently carrying */
	private int currentLoad = 0;
	/** Index of a node, where camel was last seen (last CamelWalkEvent) */
	private int locationIndex = -1;
	
	/**
	 * Creates a new camel
	 * Not used directly - camels are created in CamelFactory
	 * @param type type of camel to create
	 * @param movementSpeed movement speed of the camel
	 * @param distance max distance the camel can travel before needing to drink
	 * @param anonymous whether or not camel is really being created or just used for calculations
	 */
	public Camel(CamelType type, double movementSpeed, double distance, boolean anonymous) {
		if (!anonymous) {
			index = instanceCounter++;
		} else {
			index = -1;
		}
		this.generationTime = EVENT_MANAGER.getSimulationTime();
		this.type = type;
		this.movementSpeed = movementSpeed;
		this.distance = distance;
		this.drinkTime = type.getDrinkTime();
		this.maxLoad = type.getMaxLoad();
	}
	
	/**
	 * Method to get the total number of camels generated
	 * @return total number of camels generated
	 */
	public static int getTotalCamelCount() {
		return instanceCounter;
	}

	/**
	 * Method to test whether a camel can deliver a request in time
	 * @param request request to be delivered
	 * @param pathDescriptor description of the path to the oasis
	 * @param load number of baskets
	 * @return true if request can be delivered by this camel, false otherwise
	 */
	public boolean canDeliverInTime(Request request, PathDescriptor pathDescriptor, int load) {
		
		//Remaining time for delivery
		final double deliveryRemainingTime = request.getRequestTime() + request.getDeliveryTime() - EVENT_MANAGER.getSimulationTime();
		
		double time = 0;
		double distance = this.distance;
		
		time += 2 * load * home.getBasketManipulationTime(); // loading and unloading
		
		Iterator<Double> it = pathDescriptor.partialDistances.iterator(); 
		while(it.hasNext()) {
			if (time > deliveryRemainingTime) {
				return false;
			}
			
			double partialDistance = it.next();
			if(this.distance < partialDistance) { //Edge too long, cannot possibly make it
				return false;
			}
				
			if(distance - partialDistance < 0) { //Has to drink
				time += drinkTime;
				distance = this.distance;
			} 
			
			distance -= partialDistance;
			time += partialDistance/movementSpeed;
		}
		
		return time <= deliveryRemainingTime;
	}
	
	/**
	 * Departs the camel.
	 * Calculates the times of events happening and provides them to the manager.
	 * @param pathDescriptor Descriptor of the path camel will follow.
	 * @param request Request which is going to be fulfilled (at least partially).
	 */
	public void depart(PathDescriptor pathDescriptor, Request request) {
		double time = EVENT_MANAGER.getSimulationTime();
		double currentDistance = distance;
		
		double loadTime = currentLoad * home.getBasketManipulationTime();
		time += loadTime;
		double timeDepart = time;
		
		List<Statistics.Drinking> drinking = new ArrayList<>();
		
		Iterator<Integer> itPath = pathDescriptor.path.iterator();
		Iterator<Double> itPartialDistances = pathDescriptor.partialDistances.iterator();
		int i = itPath.next();
		while(itPath.hasNext()) {
			int j = itPath.next();
			
			double distIJ = itPartialDistances.next();
		
			boolean logWalk = itPath.hasNext();
			
			if(distIJ > currentDistance) {
				EVENT_MANAGER.addEvent(new CamelDrinkEvent(time, this, i));
				drinking.add(STATISTICS.createDrinkingRecord(time, i));
				time += drinkTime;
				currentDistance = distance;
			}
			
			if(!MAP.isOasisIndex(j)) {
				logWalk = false;
			}
			
			time += distIJ/movementSpeed;
			currentDistance -= distIJ;
			EVENT_MANAGER.addEvent(new CamelWalkEvent(time, this, j, logWalk)); //Log only if not warehouse, not drinking or not final destination
			
			i = j;
		}
		
		EVENT_MANAGER.addEvent(new CamelDeliverEvent(time, this, request));
		//has to unload first (per basket)
		double timeDeliver = time;
		time += loadTime; // unloaded, now can go home
		
		itPath = ((LinkedList<Integer>)pathDescriptor.path).descendingIterator();
		itPartialDistances = ((LinkedList<Double>)pathDescriptor.partialDistances).descendingIterator();
		i = itPath.next();
		while(itPath.hasNext()) {
			int j = itPath.next();
			
			double distIJ = itPartialDistances.next();
		
			boolean logWalk = itPath.hasNext();
			
			if(distIJ > currentDistance) {
				EVENT_MANAGER.addEvent(new CamelDrinkEvent(time, this, i));
				drinking.add(STATISTICS.createDrinkingRecord(time, i));
				time += drinkTime;
				currentDistance = distance;
				logWalk = false;
			}
			
			if(!MAP.isOasisIndex(j)) {
				logWalk = false;
			}
			
			time += distIJ/movementSpeed;
			currentDistance -= distIJ;
			EVENT_MANAGER.addEvent(new CamelWalkEvent(time, this, j, logWalk)); //Log only if not warehouse, not drinking or not final destination
			
			i = j;
		}
		EVENT_MANAGER.addEvent(new CamelReturnEvent(time, this, request));
		STATISTICS.addCamelDelivery(this, request, currentLoad, timeDepart, timeDeliver, time, pathDescriptor.path, drinking);
	}
	
	/**
	 * Gets the name of the location where camel was last seen 
	 * @return name of the location
	 */
	private String getLocationName() {
		Node location = MAP.getNodeAtIndex(locationIndex);
		if(location instanceof Oasis) {
			return "Oasis #" + MAP.nodeToOasisIndex(locationIndex) + 1;
		} else {
			return "Warehouse #" + locationIndex + 1;
		}
	}
	
	/**
	 * Returns the index of camel.
	 * @return Index of the camel.
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * Returns the index of camel plus one.
	 * @return Index of camel plus one.
	 */
	public int getIndexPlusOne() {
		return index + 1;
	}
	
	/**
	 * Returns the time of camel generation.
	 * @return Time of camel generation.
	 */
	public double getGenerationTime() {
		return generationTime;
	}
	
	/**
	 * Getter for camel type
	 * @return camel type
	 */
	public CamelType getType() {
		return type;
	}

	/**
	 * Returns camel home warehouse.
	 * @return Home warehouse.
	 */
	public Warehouse getHome() {
		return home;
	}

	/**
	 * Sets camel home warehouse.
	 * @param home Home warehouse.
	 */
	public void setHome(Warehouse home) {
		this.home = home;
		this.locationIndex = home.getIndex();
	}

	/**
	 * Returns number of baskets the camel is carrying.
	 * @return Number of carried baskets.
	 */
	public int getCurrentLoad() {
		return currentLoad;
	}

	/**
	 * Sets number of baskets the camel is carrying.
	 * @param currentBaskets Number of baskets carried by the camel.
	 */
	public void setCurrentLoad(int currentBaskets) {
		this.currentLoad = currentBaskets;
	}

	/**
	 * Returns max load of camel.
	 * @return Max load.
	 */
	public int getMaxLoad() {
		return maxLoad;
	}

	/**
	 * Returns the index of the node where the camel is.
	 * @return Index of the node where the camel is.
	 */
	public int getLocationIndex() {
		return locationIndex;
	}

	/**
	 * Sets node index where the camel is.
	 * @param locationIndex Index of the node where the camel is.
	 */
	public void setLocationIndex(int locationIndex) {
		this.locationIndex = locationIndex;
	}

	/**
	 * Returns the distance the camel can cover without drinking.
	 * @return Distance the camel can cover withou drinking.
	 */
	public double getDistance() {
		return distance;
	}

	/**
	 * Returns the movement speed.
	 * @return Movement speed.
	 */
	public double getMovementSpeed() {
		return movementSpeed;
	}
	
	/**
	 * Returns camel string representation.
	 * @return Camel string representation.
	 */
	@Override
	public String toString() {
		return "Camel [index=" + getIndexPlusOne() + ", type=" + type.getName() + ", home=" + home.getIndexPlusOne()
				+ ", currentLoad=" + currentLoad + ", lastSeen=" + getLocationName() + "]";
	}
	
	/**
	 * Returns camel hashcode.
	 * @return Camel hashcode.
	 */
	@Override
	public int hashCode() {
		return index;
	}
	
	/**
	 * Checks whether provided object is equal to the camel.
	 * @param obj Object to compare with the camel.
	 * @return True, if obj is equal to the camel, else false.
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Camel)) {
			return false;
		}
		return this.index == ((Camel) obj).index;
	}
	
}
