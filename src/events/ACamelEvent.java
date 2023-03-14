package events;

import camels.Camel;

/**
 * Represents an event with camel.
 * 
 * @author Stanislav Kafara, Jakub Krizanovsky
 * @version 1 06-10-22
 */
public abstract class ACamelEvent extends AEvent {
	
	/** Camel. */
	protected final Camel camel;

	/**
	 * Constructs an abstract camel event.
	 * @param time Time.
	 * @param priority Priority.
	 * @param camel Camel.
	 */
	public ACamelEvent(double time, int priority, Camel camel) {
		super(time, priority);
		this.camel = camel;
	}


}
