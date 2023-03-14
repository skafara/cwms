package events;

import camels.Camel;
import requests.Request;
import simulation.Simulation;

/**
 * Represents an event of camel delivering baskets to an oasis.
 * 
 * @author Stanislav Kafara, Jakub Krizanovsky
 * @version 2 02-12-22
 */
public class CamelDeliverEvent extends ACamelRequestEvent {
	
	private static final int PRIORITY = 60;

	private static final EventManager EVENT_MANAGER = EventManager.getInstance();
	
	/**
	 * Constructs an event of camel delivering baskets to an oasis.
	 * @param time Time of delivery.
	 * @param camel Camel.
	 * @param request Request to be delivered (at least partially).
	 */
	public CamelDeliverEvent(double time, Camel camel, Request request) {
		super(time, PRIORITY, camel, request);
		
	}

	/**
	 * Deliveres the carried baskets.
	 */
	@Override
	public void process() {
		System.out.format(
				"Cas: %.0f, Velbloud: %d, Oaza: %d, Vylozeno kosu: %d, Vylozeno v: %.0f, Casova rezerva: %.0f%n",
				time,
				camel.getIndexPlusOne(),
				request.getOasisIndexPlusOne(),
				camel.getCurrentLoad(),
				time + camel.getCurrentLoad()*camel.getHome().getBasketManipulationTime(),
				request.getRequestTime() + request.getDeliveryTime() - time
		);
		
		
		Simulation.basketsDelivered += camel.getCurrentLoad();
				
		request.addDeliveredBaskets(camel.getCurrentLoad());
		camel.setCurrentLoad(0);
		
		if(request.getBasketCount() == request.getDeliveredBasketsCount()) {
			EVENT_MANAGER.addEvent(new RequestFulfilledEvent(time, request));
		}
	}

}
