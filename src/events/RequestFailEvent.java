package events;

import requests.Request;
import simulation.Statistics;

/**
 * Represents an event of request delivery failure.
 * 
 * @author Stanislav Kafara, Jakub Krizanovsky
 * @version 2 02-12-22
 */
public class RequestFailEvent extends ARequestEvent {

	private static final int PRIORITY = 30;
	
	/**
	 * Constructs an event of request delivery failure.
	 * @param time Time of delivery failure.
	 * @param request Request failed to deliver.
	 */
	public RequestFailEvent(double time, Request request) {
		super(time, PRIORITY, request);
	}

	@Override
	public void process() {
		System.out.format(
				"Cas: %.0f, Oaza: %d, Vsichni vymreli, Harpagon zkrachoval, Konec simulace%n",
				time,
				request.getOasisIndexPlusOne()
		);
		Statistics.getInstance().generateStatistics();
		Statistics.getInstance().appendErrorMessage(
				String.format(
						"Vsichni vymreli. V case %.2f se nepodarilo dorucit pozadavek #%d.%n- %s",
						EventManager.getInstance().getSimulationTime(),
						request.getIndex() + 1,
						request.getErrorMessage()
				)
		);
		System.exit(0);
	}

}
