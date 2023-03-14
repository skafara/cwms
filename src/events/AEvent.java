package events;

/**
 * Represents an abstract simulation event.
 * 
 * @author Stanislav Kafara
 * @version 1 06-10-22
 */
public abstract class AEvent implements Comparable<AEvent> {

	/** Time. */
	protected final double time;
	/** Default (low) priority. */
	protected static final int PRIORITY = 1;
	
	private final int priority;

	/**
	 * Constructs an abstract event.
	 * @param time Time.
	 * @param priority Priority.
	 */
	public AEvent(double time, int priority) {
		this.time = time;
		this.priority = priority;
	}

	/**
	 * Event processing.
	 */
	public abstract void process();
	
	/**
	 * Returns time of an event.
	 * @return Time.
	 */
	public double getTime() {
		return time;
	}

	/**
	 * Compares provided events.
	 * @param o Event.
	 */
	@Override
	public int compareTo(AEvent o) {
		int result = (int) Math.signum(time - o.time);
		
		if(result == 0) {
			result = o.priority - this.priority;
		}


		return result;
	}

	/**
	 * Returns priority.
	 * @return Priority
	 */
	public int getPriority() {
		return priority;
	}
	
	
}
