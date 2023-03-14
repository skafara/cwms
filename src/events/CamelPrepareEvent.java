package events;

import camels.Camel;
import path_calculation.PathDescriptor;
import requests.Request;

/**
 * Represents an event of camel preparing for departure.
 * 
 * @author Stanislav Kafara, Jakub Krizanovsky
 * @version 2 02-12-22
 */
public class CamelPrepareEvent extends ACamelRequestEvent {
	
	private static final int PRIORITY = 20;
	
	private final int basketCount;
	private final PathDescriptor pathDescriptor;

	/**
	 * Constructs an event of camel preparing for departure.
	 * @param time Time of preparing for a departure.
	 * @param camel Camel.
	 * @param request Request to fulfill (at least partially).
	 * @param basketCount Basket count carrying.
	 * @param pathDescriptor Descriptor of the path the camel is going to follow.
	 */
	public CamelPrepareEvent(double time, Camel camel, Request request, int basketCount, PathDescriptor pathDescriptor) {
		super(time, PRIORITY, camel, request);
		this.basketCount = basketCount;
		this.pathDescriptor = pathDescriptor;
	}

	/**
	 * Departs the camel.
	 */
	@Override
	public void process() {
		camel.setCurrentLoad(basketCount);
		System.out.format(
				"Cas: %.0f, Velbloud: %d, Sklad: %d, Nalozeno kosu: %d, Odchod v: %.0f%n",
				time,
				camel.getIndexPlusOne(),
				camel.getHome().getIndexPlusOne(),
				basketCount,
				time + camel.getHome().getBasketManipulationTime()*basketCount
		);
		camel.depart(pathDescriptor, request);
		request.addCamel(camel);
	}

}
