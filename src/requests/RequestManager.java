package	requests;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import events.EventManager;
import events.RequestReceiveEvent;
import events.SimulationEndEvent;

/**
 * Singleton that manages requests
 * 
 * @version 29.11.2022
 * @author Jakub Krizanovsky, Stanislav Kafara
 */
public class RequestManager {
	
	private static final EventManager EVENT_MANAGER = EventManager.getInstance();
	
	/** All requests with their state */
	private final Map<Request, RequestState> requests = new TreeMap<Request, RequestState>();
	/** Requests that are unprocessed or unfinished (could not be distributed yet) sorted by remaining time to deliver them */
	private final Set<Request> unfinishedRequests = new TreeSet<Request>(new RequestTimeLeftComparator());
	
	/** The one and only instance of this class (singleton) */
	private static final RequestManager INSTANCE = new RequestManager();
	/** Private constructor (singleton) */
	private RequestManager() {}
	/**
	 * Method to get the single instance (singleton)
	 * @return the single instance
	 */
	public static RequestManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Adds a new request and creates a RequestReceiveEvent for it
	 * @param request request to add
	 */
	public void addRequest(Request request) {
		requests.put(request, RequestState.Inactive);
		EVENT_MANAGER.addEvent(new RequestReceiveEvent(request.getRequestTime(), request));
	}
	
	/**
	 * Adds a request to unfinished to try and distribute it again
	 * @param request request to add to unfinished
	 */
	public void addToUnfinishedRequests(Request request) {
		unfinishedRequests.add(request);
	}
	
	/**
	 * Changes the state of a request
	 * @param request request to change the state of
	 * @param newState state to change to
	 */
	public void changeRequestState(Request request, RequestState newState) {
		requests.put(request, newState);
	}
	
	/**
	 * Method to get all requests
	 * @return all requests with their state
	 */
	public Map<Request, RequestState> getAllRequests() {
		return requests;
	}
	
	/**
	 * Method to get unfinished requests in order to try to process them again
	 * @return Unfinished requests to be later processed.
	 */
	public Set<Request> getUnfinishedRequests() {
		return unfinishedRequests;
	}

	/**
	 * Cancels a request (when user requests it from ui)
	 * @param request request to cancel
	 */
	public void cancelRequest(Request request) {
		requests.put(request, RequestState.Cancelled);
		unfinishedRequests.remove(request);

		EVENT_MANAGER.cancelRequestEvents(request);
	}
	
	/**
	 * Test whether all events are completed or cancelled and ends the simulation if they are
	 */
	public void testSimulationEnd() {
		Set<Entry<Request, RequestState>> requestEntrySet = requests.entrySet();
		for(Entry<Request, RequestState> entry : requestEntrySet) {
			if(!(entry.getValue().equals(RequestState.Completed) || entry.getValue().equals(RequestState.Cancelled))) {
				return;
			}
		}

		EVENT_MANAGER.addEvent(new SimulationEndEvent(EVENT_MANAGER.getSimulationTime()));
	}
	
}
