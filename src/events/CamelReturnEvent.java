package events;

import camels.Camel;
import requests.Request;
import requests.RequestManager;
import requests.RequestState;

/**
 * Represents an event of camel returning to home warehouse.
 * 
 * @author Stanislav Kafara, Jakub Krizanovsky
 * @version 2 02-12-22
 */
public class CamelReturnEvent extends ACamelRequestEvent {
	
	private static final int PRIORITY = 50;
	private static final RequestManager REQUEST_MANAGER = RequestManager.getInstance();	

	/**
	 * Constructs an event of camel returning to home warehouse.
	 * @param time Time of return.
	 * @param camel Camel.
	 * @param request Request that was fulfilled (at least partially).
	 */
	public CamelReturnEvent(double time, Camel camel, Request request) {
		super(time, PRIORITY, camel, request);
	}

	/**
	 * Returns the camel to the home warehouse and checks whether the request was completed
	 * and whether the simulation should end successfully.
	 */
	@Override
	public void process() {
		System.out.format(
				"Cas: %.0f, Velbloud: %d, Navrat do skladu: %d%n",
				getTime(),
				camel.getIndexPlusOne(),
				camel.getHome().getIndexPlusOne()
		);
		
		camel.getHome().returnCamel(camel);
		
		request.removeCamel(camel);
		
		if(request.getCamels().size() == 0) {
			REQUEST_MANAGER.changeRequestState(request, RequestState.Completed);
		}
		
		//Test whether simulation should be ended
		REQUEST_MANAGER.testSimulationEnd();
	}

}
