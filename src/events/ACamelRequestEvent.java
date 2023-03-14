package events;

import camels.Camel;
import requests.Request;

/**
 * Represents an event with camel and request.
 * 
 * @author Stanislav Kafara, Jakub Krizanovsky
 * @version 1 06-10-22
 */
public abstract class ACamelRequestEvent extends ACamelEvent {
	
	/** Request. */
	protected final Request request;

	/**
	 * Constructs an abstract event with camel and request.
	 * @param time Time.
	 * @param priority Priority.
	 * @param camel Camel.
	 * @param request Request.
	 */
	public ACamelRequestEvent(double time, int priority, Camel camel, Request request) {
		super(time, priority, camel);
		this.request = request;
	}
}
