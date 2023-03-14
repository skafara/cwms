package requests;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import camels.Camel;
import events.RequestFailEvent;

/**
 * Represents a request of baskets to an oasis
 *
 * @author Jakub Krizanovsky
 * @version 29.11.2022
 */
public class Request implements Comparable<Request> {
	
	private static int instanceCounter = 0;
	
	private final int index;
	private final int oasisIndex;
	private final int basketCount;
	private final double requestTime;
	private final double deliveryTime;

	private double timeDelivered;
	
	/** Remaining baskets that are neither delivered nor on their way */
	private int basketsRemaining;
	/** Baskets that have been delivered */
	private int deliveredBasketsCount = 0;
	/** List of camels delivering the request */
	private final List<Camel> camels = new LinkedList<Camel>();
	
	private final Set<Camel> servingCamels = new HashSet<>();

	private RequestFailEvent requestFailEvent;
	
	/**
	 * Creates a new event
	 * 
	 * @param oasisIndex index of the oasis that baskets should be delivered to
	 * @param basketCount number of baskets to be delivered
	 * @param requestTime time when request should be received
	 * @param deliveryTime time for delivery of the request since the time of receiving
	 */
	public Request(double requestTime, int oasisIndex, int basketCount, double deliveryTime) {
		this.index = instanceCounter++;
		this.requestTime = requestTime;
		this.oasisIndex = oasisIndex;
		this.basketCount = basketCount;	
		this.deliveryTime = deliveryTime;
		basketsRemaining = basketCount;
	}

	/**
	 * Returns the index of oasis that created the request.
	 * @return Index of the oasis that created the request.
	 */
	public int getOasisIndex() {
		return oasisIndex;
	}

	/**
	 * Returns the requested basket count.
	 * @return Requested basket count.
	 */
	public int getBasketCount() {
		return basketCount;
	}

	/**
	 * Returns the time of the request creation.
	 * @return Time of the request creation.
	 */
	public double getRequestTime() {
		return requestTime;
	}

	/**
	 * Returns the maximal time of delivery.
	 * @return Maximal time of delivery.
	 */
	public double getDeliveryTime() {
		return deliveryTime;
	}
	
	/**
	 * Returns the time when request was fulfilled.
	 * @return Time when the request was fulfilled.
	 */
	public double getDeliveredTime() {
		return timeDelivered;
	}

	/**
	 * Sets the time when request was fulfilled.
	 * @param time Time when the request was fulfilled.
	 */
	public void setDeliveredTime(double time) {
		timeDelivered = time;
	}

	/**
	 * Returns the already delivered baskets count.
	 * @return Already delivered baskets count.
	 */
	public int getDeliveredBasketsCount() {
		return deliveredBasketsCount;
	}

	/**
	 * Adds already delivered baskets count.
	 * @param deliveredBasketsCount Delivered baskets count.
	 */
	public void addDeliveredBaskets(int deliveredBasketsCount) {
		this.deliveredBasketsCount += deliveredBasketsCount;
	}

	/**
	 * Returns the associated event of delivery failure.
	 * @return Associated event of delivery failure.
	 */
	public RequestFailEvent getRequestFailEvent() {
		return requestFailEvent;
	}

	/**
	 * Sets the event of delivery failure.
	 * @param requestFailEvent Associated event of delivery failure.
	 */
	public void setRequestFailEvent(RequestFailEvent requestFailEvent) {
		this.requestFailEvent = requestFailEvent;
	}

	/**
	 * Method to get index of the oasis plus one used for printing
	 * @return index of oasis + 1
	 */
	public int getOasisIndexPlusOne() {
		return oasisIndex+1;
	}

	/**
	 * Returns the camels participating in the delivery at the moment.
	 * @return The camels participating in the delivery at the moment.
	 */
	public List<Camel> getCamels() {
		return camels;
	}
	
	/**
	 * Returns all the camels that participated in the delivery.
	 * @return All the camels that participated in the delivery.
	 */
	public Set<Camel> getServingCamels() {
		return servingCamels;
	}
	
	/**
	 * Adds camel to the camels participating in the delivery.
	 * @param camel Camel.
	 */
	public void addCamel(Camel camel) {
		camels.add(camel);
		servingCamels.add(camel);
	}
	
	/**
	 * Removes camel from camels participating in the delivery at the moment.
	 * @param camel Camel to remove.
	 * @throws IllegalStateException if removed camel was not a camel delivering the request
	 */
	public void removeCamel(Camel camel) {
		boolean removed = camels.remove(camel);
		if(!removed) {
			throw new IllegalStateException("Trying to remove a camel that is not handling the event");
		}
	}

	/**
	 * Returns the baskets count that need to be delivered.
	 * @return Remaining baskets count to be delivered.
	 */
	public int getBasketsRemaining() {
		return basketsRemaining;
	}
	
	/**
	 * Reduces remaining baskets by an amount when baskets are sent
	 * @param reduceAmount amount to reduce by
	 */
	public void reduceBasketsRemaining(int reduceAmount) {
		basketsRemaining -= reduceAmount;
	}
	
	/**
	 * Returns the index of the request.
	 * @return Index of the request.
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * Returns the string representation of request delivery failure error message.
	 * @return String representation of request delivery failure.
	 */
	public String getErrorMessage() {
		return "Pozadavek [oaza=" + (oasisIndex + 1) + ", pocet_kosu=" + basketCount + ", cas_vzniku=" + requestTime
				+ ", limit_doruceni=" + deliveryTime + "]";
	}
	
	/**
	 * Return the string representation of the request.
	 * @return String representation of the request.
	 */
	@Override
	public String toString() {
		return "Request [oasisIndex=" + oasisIndex + ", basketCount=" + basketCount + ", requestTime=" + requestTime
				+ ", deliveryTime=" + deliveryTime + "]";
	}
	
	/**
	 * Compares the requests times of creation and oasis indices.
	 * @return 1 if the first is greater, -1 if the first is less, 0 if they are equal.
	 */
	@Override
	public int compareTo(Request o) {
		if(this==o) {
			return 0;
		}
		
		int timeDiff = (int)Math.signum(this.requestTime - o.requestTime);
		if(timeDiff != 0) {
			return timeDiff;
		}
		
		int oasisIndexDiff = this.oasisIndex - o.oasisIndex;
		if(oasisIndexDiff != 0) {
			return oasisIndexDiff;
		}
		
		return 1;
	}
}
