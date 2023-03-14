package ui;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import camels.Camel;
import events.EventManager;
import requests.Request;
import requests.RequestManager;
import requests.RequestState;

/**
 * Helper class for class UserInterface
 * Takes care of request controlling
 * 
 * @version 29.11.2022
 * @author Jakub Krizanovsky
 */
public class UIRequestControls {

	private static final RequestManager REQUEST_MANAGER = RequestManager.getInstance();
	private static final EventManager EVENT_MANAGER = EventManager.getInstance();
	private static final simulation.Map MAP = simulation.Map.getInstance();
	private final UserInterface ui;
	
	/**
	 * Constructor for this class
	 * Sets the ui
	 * @param ui User interface
	 */
	public UIRequestControls(UserInterface ui) {
		this.ui = ui;
	}
	
	/**
	 * Cancels a request
	 * @param commandArr array of user input (0 - command, 1 - request index)
	 */
	public void cancelRequest(String[] commandArr) {
		if(commandArr.length != 2) {
			System.out.println("Invalid arguments");
			return;
		}
		
		if(!ui.isDataLoaded()) {
			System.out.println("No data loaded");
			return;
		}
		
		int requestIndex = -1;
		try {
			requestIndex = Integer.parseInt(commandArr[1]);
		} catch(NumberFormatException e) {
			System.out.println("Request index not formatted correctly.");
			return;
		}
		
		Map<Request, RequestState> requests = REQUEST_MANAGER.getAllRequests();
		
		if(requestIndex >= requests.size()) {
			System.out.println("Request with that index does not exist.");
			return;
		}
		
		cancelRequest(requestIndex, requests);
	}

	//Split because of pmd
	private void cancelRequest(int requestIndex, Map<Request, RequestState> requests) {
		Set<Entry<Request, RequestState>> requestsEntrySet = requests.entrySet();
		int i = 0;
		for(Entry<Request, RequestState> entry : requestsEntrySet) {
			if(i == requestIndex) {
				if(entry.getValue() == RequestState.Active
						|| entry.getValue() == RequestState.Delivered
						|| entry.getValue() == RequestState.Completed
						|| entry.getValue() == RequestState.Cancelled) {
					System.out.println("Cannot cancel an active, delivered, completed or cancelled request.");
					return;
				}
				REQUEST_MANAGER.cancelRequest(entry.getKey());
				System.out.println("Request #" + requestIndex + " cancelled.");
				return;
			}
			i++;
		}
		
	}

	/**
	 * Lists requests
	 * If request state is not set, list all requests, else lists all requests of given state
	 * @param commandArr array of user input (0 - command, [1 - request state])
	 */
	public void listRequests(String[] commandArr) {
		if(commandArr.length > 2) {
			System.out.println("Invalid arguments");
			return;
		} 
		
		if(!ui.isDataLoaded()) {
			System.out.println("No data loaded");
			return;
		}
		
		
		RequestState state = null;
		if(commandArr.length == 2) {
			//Capitalize first char if not done by user
			commandArr[1] = commandArr[1].substring(0, 1).toUpperCase() + commandArr[1].substring(1);
			
			try {
				state = RequestState.valueOf(commandArr[1]);
				System.out.print(state + " ");
			} catch (IllegalArgumentException e) {
				System.out.println("Unknown request state: " + commandArr[1]);
				return;
			}
		}
		
		listRequests(state);
	}
	
	//Split because of pmd
	private void listRequests(RequestState state) {
		Map<Request, RequestState> requests = REQUEST_MANAGER.getAllRequests();
		
		System.out.println("Requests:");
		
		Set<Entry<Request, RequestState>> requestsEntrySet = requests.entrySet();
		int i = 0;
		int count = 0;
		for(Entry<Request, RequestState> entry : requestsEntrySet) {
			if(state == null || entry.getValue().equals(state)) {
				System.out.printf(" #%d: %s: %s\n", i, entry.getKey(), entry.getValue());
				count++;
			}
			i++;
		}
		
		System.out.println("Count: " + count);
		
	}

	/**
	 * Prints info about a requests
	 * @param commandArr array of user input (0 - command, 1 - request index)
	 */
	public void printRequestInfo(String[] commandArr) {
		if(commandArr.length != 2) {
			System.out.println("Invalid arguments");
			return;
		}
		
		if(!ui.isDataLoaded()) {
			System.out.println("No data loaded");
			return;
		}
		
		int requestIndex = -1;
		try {
			requestIndex = Integer.parseInt(commandArr[1]);
		} catch(NumberFormatException e) {
			System.out.println("Request index not formatted correctly.");
			return;
		}
		
		Map<Request, RequestState> requests = REQUEST_MANAGER.getAllRequests();
		if(requestIndex >= requests.size()) {
			System.out.println("Request with that index does not exist.");
			return;
		}
		
		printRequestInfo(requestIndex, requests);
	}
	
	//Split because of pmd
	private void printRequestInfo(int requestIndex, Map<Request, RequestState> requests) {
		Request request = null;
		Set<Entry<Request, RequestState>> requestsEntrySet = requests.entrySet();
		int i = 0;
		for(Entry<Request, RequestState> entry : requestsEntrySet) {
			if(i == requestIndex) {
				request = entry.getKey();
				break;
			}
			i++;
		}
		
		System.out.println(" " + request);
		RequestState requestState = requests.get(request);
		System.out.println(" State: " + requestState);
		if(requestState == RequestState.Active || requestState == RequestState.Delivered) {
			List<Camel> camels = request.getCamels();
			System.out.println(" Camels:");
			for(Camel camel : camels) {
				System.out.println("  " + camel);
			}
		}
	}

	/**
	 * Adds a request
	 * @param commandArr array of user input (0 - command, 1 - request time, 2 - oasis index (in input data format), 3 - basket count)
	 */
	public void addRequest(String[] commandArr) {
		if(commandArr.length != 5) {
			System.out.println("Not enough arguments");
			return;
		}
		
		if(!ui.isDataLoaded()) {
			System.out.println("No data loaded");
			return;
		}
		
		double requestTime = getRequestTime(commandArr);
		
		int oasisIndex = getOasisIndex(commandArr);
		
		int basketCount = getBasketCount(commandArr);
		
		
		double deliveryTime = -1;
		try {
			deliveryTime = Double.parseDouble(commandArr[4]);
		} catch (Exception e) {
			System.out.println("Delivery time count not formatted correctly");
		}
		
		if(requestTime < 0 || oasisIndex < 0 || basketCount < 0 || deliveryTime < 0) {
			System.out.println("Adding request failed");
			return;
		}
		
		Request request = new Request(requestTime, oasisIndex, basketCount, deliveryTime);
		REQUEST_MANAGER.addRequest(request);
		System.out.println("Request succesfully added.");
	}
	
	private int getBasketCount(String[] commandArr) {
		int basketCount = -1;
		try {
			basketCount = Integer.parseInt(commandArr[3]);
		} catch (Exception e) {
			System.out.println("Basket count not formatted correctly");
			return -1;
		}
		
		if(basketCount <= 0) {
			System.out.println("Basket count must be a positive integer");
			return -1;
		}
		
		return basketCount;
	}

	private double getRequestTime(String[] commandArr) {
		double requestTime = -1;
		if(commandArr[1].equals("now")) {
			requestTime = EVENT_MANAGER.getSimulationTime();
		} else {
			try {
				requestTime = Double.parseDouble(commandArr[1]);
			} catch (Exception e) {
				System.out.println("Time not formatted correctly");
				return -1;
			}
		}
		
		if(requestTime < EVENT_MANAGER.getSimulationTime()) {
			System.out.println("Time cannot be lesser than current simulation time");
			return -1;
		}
		
		return requestTime;
	}

	private int getOasisIndex(String[] commandArr) {
		int oasisIndex = -1;
		try {
			oasisIndex = Integer.parseInt(commandArr[2]) - 1;
		} catch (Exception e) {
			System.out.println("Oasis index not formatted correctly");
			return -1;
		}
		
		if(oasisIndex < 0 || oasisIndex > MAP.getOasisCount()) {
			System.out.println("Oasis with index: " + oasisIndex+1 + " does not exist");
			return -1;
		}
		
		
		return oasisIndex;
	}
}
