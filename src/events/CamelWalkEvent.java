package events;

import camels.Camel;
import simulation.Map;

/**
 * Represents an event of camel walking through.
 * 
 * @author Stanislav Kafara, Jakub Krizanovsky
 * @version 02-12-22
 */
public class CamelWalkEvent extends ACamelEvent {
	
	private static final Map MAP = Map.getInstance();
	
	private final int nodeIndex;
	private boolean log;

	/**
	 * Constructs an event of camel walking through.
	 * @param time Time.
	 * @param camel Camel.
	 * @param nodeIndex Node index.
	 * @param log True, if verbose mode, else false.
	 */
	public CamelWalkEvent(double time, Camel camel, int nodeIndex, boolean log) {
		this(time, camel, nodeIndex);
		this.log = log;
	}
	
	/**
	 * Constructs an event of camel walking through.
	 * @param time Time.
	 * @param camel Camel.
	 * @param nodeIndex Node index.
	 */
	public CamelWalkEvent(double time, Camel camel, int nodeIndex) {
		super(time, PRIORITY, camel);
		this.nodeIndex = nodeIndex;
	}

	/**
	 * Camel walks through.
	 */
	@Override
	public void process() {
		if(log) {
			System.out.format(
					"Cas: %.0f, Velbloud: %d, Oaza: %d, Kuk na velblouda%n",
					time,
					camel.getIndexPlusOne(),
					MAP.nodeToOasisIndex(nodeIndex)+1
			);
		}
		
		camel.setLocationIndex(nodeIndex);
		
	}

	/**
	 * Returns whether verbose mode is enabled.
	 * @return True, if verbose mode, else false.
	 */
	public boolean isLog() {
		return log;
	}
	
	

}
