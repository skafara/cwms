/**
 * 
 */
package events;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import camels.Camel;
import requests.Request;
import requests.RequestManager;
import requests.RequestState;
import simulation.Simulation;
import simulation.Statistics;

/**
 * Represents an event of sucessful end of a simulation.
 * 
 * @author Jakub Krizanovsky
 * @version 2 02-12-22
 */
public class SimulationEndEvent extends AEvent {

	private static final int PRIORITY = 0;
	
	/**
	 * Constructs an event of successful end of simulation.
	 * @param time Time.
	 */
	public SimulationEndEvent(double time) {
		super(time, PRIORITY);
	}

	/**
	 * Successfully ends the simulation.
	 */
	@Override
	public void process() {
		System.out.printf("Cas: %.0f, Vse splneno, ukonceni simulace\n", time);
		
		int requestCount = 0;
		int sumBaskets = 0;
		
		Map<Request, RequestState> allRequests = RequestManager.getInstance().getAllRequests();
		Set<Entry<Request, RequestState>> requestsEntrySet = allRequests.entrySet();
		for(Entry<Request, RequestState> entry : requestsEntrySet) {
			if(!entry.getValue().equals(RequestState.Completed) && !entry.getValue().equals(RequestState.Cancelled)) {
				throw new IllegalStateException("Ending simulation, but " + entry.getKey() + " is in state: " + entry.getValue());
			}
			Request r = entry.getKey();
			requestCount++;
			sumBaskets += r.getBasketCount();
		}
		
		System.out.println("\nPocet dorucenych kosu: " + Simulation.basketsDelivered + " / " + sumBaskets);
		System.out.println("Pocet obslouzenych pozadavku: " + Simulation.requestsFulfilled + " / " + requestCount);
		System.out.println("Pocet pouzitych velbloudu: " + Camel.getTotalCamelCount());
		System.out.printf("Doba behu: %.1f s\n" , (System.nanoTime() - Simulation.startTime)*1e-9);
		
		Statistics.getInstance().generateStatistics();
		System.exit(0);

	}

}
