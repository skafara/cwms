package events;

import java.util.PriorityQueue;

import requests.Request;

/**
 * Singleton that manages events
 * 
 * @version 29.11.2022
 * @author Jakub Krizanovsky, Stanislav Kafara
 */
public class EventManager {
	
	/** Whether additional debug info should be printed while processing events */
	private static final boolean DEBUG_MODE = false;
	/** Current simulation time */
	private double simulationTime = 0;
	/** Priority queue of events */
	private final PriorityQueue<AEvent> events = new PriorityQueue<AEvent>();
	
	/** The one and only instance of this class (singleton) */
	private static final EventManager INSTANCE = new EventManager();
	/** Private constructor (singleton) */
	private EventManager() {}
	/**
	 * Method to get the single instance (singleton)
	 * @return the single instance
	 */
	public static EventManager getInstance() {
		return INSTANCE;
	}

	/**
	 * Adds an event to the priority queue of events
	 * @param e event to add
	 */
	public void addEvent(AEvent e) {
		events.add(e);
	}
	
	/**
	 * Cancels an upcoming event
	 * @param e event to cancel
	 * @throws IllegalStateException if event was not in the priority queue of events
	 */
	public void cancelEvent(AEvent e) {
		if(DEBUG_MODE) {
			System.out.println("Cancelling event " + e);
		}
		boolean removed = events.remove(e);
		if(!removed) {
			throw new IllegalStateException("Cancelling a non existing event");
		}
	}
	
	/**
	 * Takes the next upcoming event a processes it
	 * @return the processed event
	 */
	public AEvent nextEvent() {
		AEvent e = events.poll();
		if(DEBUG_MODE) {
			System.out.println();
			System.out.println("===================================NEXT EVENT===================================");
			System.out.println(e);
			System.out.println("time: " + e.getTime());
			System.out.println("priority: " + e.getPriority());
			System.out.println("================================================================================");
		}
		simulationTime = e.getTime();
		e.process();
		return e;
	}

	/**
	 * Getter for simulation time
	 * @return the simulationTime
	 */
	public double getSimulationTime() {
		return simulationTime;
	}
	
	/**
	 * Cancels RequestReceiveEvent and RequestFailEvent for an event
	 * @param request request to cancel events for
	 */
	public void cancelRequestEvents(Request request) {
		//Remove RequestReceiveEvent
		for(AEvent event : events) {
			if(event instanceof RequestReceiveEvent && ((RequestReceiveEvent) event).getRequest() == request) {
				cancelEvent(event);
				break;
			}
		}
		
		//Remove RequestFailEvent
		for(AEvent event : events) {
			if(event instanceof RequestFailEvent && ((RequestFailEvent) event).getRequest() == request) {
				cancelEvent(event);
				break;
			}
		}
	}
}
