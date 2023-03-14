package simulation;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import camels.Camel;
import camels.CamelFactory;
import events.BasketRefillEvent;
import events.CamelPrepareEvent;
import events.EventManager;
import path_calculation.PathDescriptor;
import requests.Request;

/**
 * Represents a warehouse of the map.
 * @author Stanislav Kafara, Jakub Krizanovsky
 */
public class Warehouse extends Node {
	
	private final int index;
	private final int basketRefillCount;
	private final double basketRefillTime;
	private final double basketManipulationTime;

	private int basketCount;
	
	private final Set<Camel> camelsInWarehouse = new HashSet<Camel>();
	private final Set<Camel> camelsDelivering = new HashSet<>();
	
	private static int instanceCouter = 0;
	
	private static final EventManager EVENT_MANAGER = EventManager.getInstance();
	private static final CamelFactory CAMEL_FACTORY = CamelFactory.getInstance();
	

	/**
	 * Constructs a warehouse.
	 * @param coords Warehouse coordinates.
	 * @param basketRefillCount Number of refilled baskets on a refill.
	 * @param basketRefillTime Time between refills.
	 * @param basketManipulationTime Time of manipulation with one basket.
	 */
	public Warehouse(Coordinates coords, int basketRefillCount, double basketRefillTime, double basketManipulationTime) {
		super(coords);
		this.basketRefillCount = basketRefillCount;
		this.basketRefillTime = basketRefillTime;
		this.basketManipulationTime = basketManipulationTime;
		this.index = instanceCouter++;
		basketCount = basketRefillCount;
		
		EVENT_MANAGER.addEvent(new BasketRefillEvent(basketRefillTime, this));
	}
	
	/**
	 * Distributes the baskests.
	 * @param request Request.
	 * @param basketCount Basket count.
	 * @param pathDescriptor Path descriptor.
	 */
	public void distribute(Request request, int basketCount, PathDescriptor pathDescriptor) {
		int basketAmount = basketCount;
		//Try use camels in warehouse
		Iterator<Camel> iterator = camelsInWarehouse.iterator();
		while(basketAmount > 0 && iterator.hasNext()) {
			Camel camel = iterator.next();
			int load = Math.min(camel.getMaxLoad(), basketAmount);
			if(camel.canDeliverInTime(request, pathDescriptor, load)) {
				EVENT_MANAGER.addEvent(new CamelPrepareEvent(EVENT_MANAGER.getSimulationTime(), camel, request, load, pathDescriptor));
				basketAmount -= load;
				deductDistributedBaskets(load);
				camelsDelivering.add(camel);
				iterator.remove();
			}
		}
		
		//Generate new camels
		while(basketAmount > 0) {
			Camel camel = CAMEL_FACTORY.getCamel();
			camel.setHome(this);
			
			int load = Math.min(camel.getMaxLoad(), basketAmount);
			if(camel.canDeliverInTime(request, pathDescriptor, load)) {
				EVENT_MANAGER.addEvent(new CamelPrepareEvent(EVENT_MANAGER.getSimulationTime(), camel, request, load, pathDescriptor));
				basketAmount -= load;
				deductDistributedBaskets(load);
				camelsDelivering.add(camel);
			} else {
				camelsInWarehouse.add(camel);
			}
		}
	} 
	
	/**
	 * Returns whether request is deliverable from the warehouse through the provided path with ideal camels.
	 * @param request Request.
	 * @param pathDescriptor Descriptor of the path the camels follow.
	 * @return True, if request is deliverable through the provided path with ideal camels.
	 */
	public boolean isDeliverable(Request request, PathDescriptor pathDescriptor) {
		Camel[] idealCamels = CAMEL_FACTORY.getIdealCamels();
		for(Camel idealCamel : idealCamels) {
			idealCamel.setHome(this);
			if(idealCamel.canDeliverInTime(request, pathDescriptor, 1)) {
				return true;
			}
		}
		
		return false;
	}
	
	private void deductDistributedBaskets(int basketCount) {
		if(this.basketCount < basketCount) {
			throw new IllegalStateException("Attempting to send baskets we don't have. Warehouse: " + index);
		}
		this.basketCount -= basketCount;
	}
	
	/**
	 * Refills the baskets and registers the future refilling.
	 */
	public void refillBaskets() {
		basketCount += basketRefillCount;
		EVENT_MANAGER.addEvent(new BasketRefillEvent(EVENT_MANAGER.getSimulationTime() + basketRefillTime, this));
	}

	/**
	 * Returns the baskets count in the warehouse.
	 * @return Number of baskets in the warehouse.
	 */
	public int getBasketCount() {
		return basketCount;
	}

	/**
	 * Returns the time between refills..
	 * @return Time between refills.
	 */
	public double getBasketRefillTime() {
		return basketRefillTime;
	}
	
	/**
	 * Returns the number of baskets that are added to the warehouse on refill.
	 * @return Number of baskets that are added to the warehouse on refill.
	 */
	public int getBasketRefillCount() {
		return basketRefillCount;
	}

	/**
	 * Returns the manipulation time with one basket.
	 * @return Manipulation time with one basket.
	 */
	public double getBasketManipulationTime() {
		return basketManipulationTime;
	}

	/**
	 * Return the warehouse index plus one.
	 * @return Warehouse index plus one.
	 */
	public int getIndexPlusOne() {
		return index + 1;
	}
	
	/**
	 * Returns the warehouse index.
	 * @return Warehouse index.
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * Returns the camel.
	 * @param camel Camel.
	 */
	public void returnCamel(Camel camel) {
		camelsDelivering.remove(camel);
		camelsInWarehouse.add(camel);
	}
	
	/**
	 * Returns all the camel possessed by the warehouse.
	 * @return All the camels possessed by the warehouse.
	 */
	public Set<Camel> getOwnedCamels() {
		Set<Camel> ownedCamels = new HashSet<>();
		ownedCamels.addAll(camelsInWarehouse);
		ownedCamels.addAll(camelsDelivering);
		return ownedCamels;
	}


	/**
	 * Returns the probable number of camels missing to fulfill the request.
	 * @param request Request.
	 * @param pathDescriptor Descriptor of the path the camels to follow.
	 * @return Probable number of camels missing to fulfill the request.
	 */
	public int getMissingCamelCount(Request request, PathDescriptor pathDescriptor) {
		
		int missingBasketsCount = request.getBasketCount();
		int missingCamelCount = 0;
		
		for (Camel camel : camelsInWarehouse) {
			missingBasketsCount -= camel.getType().getMaxLoad();
		}
		if (missingBasketsCount < 0) {
			return 0;
		}
		
		while (missingBasketsCount > 0) {
			// generate a camel, inc missing camelCount and if can deliver, decrease missing basketsCount
			Camel camel = CAMEL_FACTORY.getAnonymousCamel();
			camel.setHome(this);
			if (camel.canDeliverInTime(request, pathDescriptor, camel.getType().getMaxLoad())) {
				missingBasketsCount -= camel.getType().getMaxLoad();
			}
			missingCamelCount++;
		}
		
		return missingCamelCount;
	}
	
	/**
	 * Returns the hashcode of the warehouse.
	 * @return Haschode of the warehouse.
	 */
	@Override
	public int hashCode() {
		return index;
	}
	
	/**
	 * Checks whether the provide object is equal to the warehouse.
	 * @return True, if they are equal, else false.
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Warehouse)) {
			return false;
		}
		return this.index == ((Warehouse) obj).index;
	}

}
