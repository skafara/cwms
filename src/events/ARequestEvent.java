package events;

import requests.Request;

/**
 * Represent an abstract request event.
 * 
 * @author Stanislav Kafara, Jakub Krizanovsky
 * @version 1 06-10-22
 */
public abstract class ARequestEvent extends AEvent {
	
	/** Request. */
	protected final Request request;

	/**
	 * Constructs an abstract event with request.
	 * @param time Time.
	 * @param priority Priority.
	 * @param request Request.
	 */
	public ARequestEvent(double time, int priority, Request request) {
		super(time, priority);
		this.request = request;
	}
	
	/**
	 * Returns the request.
	 * @return Request.
	 */
	public Request getRequest() {
		return request;
	}

}
