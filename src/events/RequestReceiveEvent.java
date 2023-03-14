package events;

import requests.Request;
import simulation.Map;

/**
 * Represents an event of receiving a request.
 * 
 * @author Stanislav Kafara, Jakub Krizanovsky
 * @version 2 02-12-22
 */
public class RequestReceiveEvent extends ARequestEvent {
	
	private static final int PRIORITY = 10;
	
	private static final EventManager EVENT_MANAGER = EventManager.getInstance();
	
	private static final Map MAP = Map.getInstance();
	
	/**
	 * Constructs an event of receiving a request.
	 * @param time Time of receiving the request.
	 * @param request Received request.
	 */
	public RequestReceiveEvent(double time, Request request) {
		super(time, PRIORITY, request);
	}

	/**
	 * Tries to process the request.
	 */
	@Override
	public void process() {
		RequestFailEvent requestFailEvent = new RequestFailEvent(EVENT_MANAGER.getSimulationTime() + request.getDeliveryTime(), request);
		request.setRequestFailEvent(requestFailEvent);
		EVENT_MANAGER.addEvent(requestFailEvent);
		
		MAP.processRequest(request);
	}

}
