/**
 * 
 */
package events;

import requests.Request;
import requests.RequestManager;
import requests.RequestState;
import simulation.Simulation;

/**
 * Event that should be processed a request gets fulfilled (all baskets are delivered)
 * 
 * @version 29.11.2022
 * @author Jakub Krizanovsky, Stanislav Kafara
 *
 */
public class RequestFulfilledEvent extends ARequestEvent {
	
	private static final int PRIORITY = 100; //Was 40
	
	private static final EventManager EVENT_MANAGER = EventManager.getInstance();
	private static final RequestManager REQUEST_MANAGER = RequestManager.getInstance();

	/**
	 * Constructor for RequestFulfilledEvent
	 * @param time event time
	 * @param request request that is fulfilled
	 */
	public RequestFulfilledEvent(double time, Request request) {
		super(time, PRIORITY, request);
	}

	@Override
	public void process() {
		EVENT_MANAGER.cancelEvent(request.getRequestFailEvent());
		REQUEST_MANAGER.changeRequestState(request, RequestState.Delivered);
		Simulation.requestsFulfilled++;
		request.setDeliveredTime(time);
	}

}
