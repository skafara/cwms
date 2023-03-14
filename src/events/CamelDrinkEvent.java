package events;

import camels.Camel;
import simulation.Map;

/**
 * Represents an event of camel drinking.
 * 
 * @author Stanislav Kafara, Jakub Krizanovsky
 * @version 1 06-10-22
 */
public class CamelDrinkEvent extends ACamelEvent {
	
	private static final Map MAP = Map.getInstance();
	
	private final int nodeIndex;

	/**
	 * Constructs an event of camel drinking.
	 * @param time Time of drinking.
	 * @param camel Camel.
	 * @param nodeIndex Index of the node, where the camel is going to drink.
	 */
	public CamelDrinkEvent(double time, Camel camel, int nodeIndex) {
		super(time, PRIORITY, camel);
		this.nodeIndex = nodeIndex;
	}

	/**
	 * Camel drinks.
	 */
	@Override
	public void process() {
		String ow;
		int indexPlusOne;
		if (MAP.isOasisIndex(nodeIndex)) {
			ow = "Oaza";
			indexPlusOne = MAP.nodeToOasisIndex(nodeIndex)+1;
		}
		else {
			ow = "Sklad";
			indexPlusOne = nodeIndex+1;
		}
		System.out.format(
				"Cas: %.0f, Velbloud: %d, %s: %d, Ziznivy %s, Pokracovani mozne v: %.0f%n",
				time,
				camel.getIndexPlusOne(),
				ow,
				indexPlusOne,
				camel.getType().getName(),
				time + camel.getType().getDrinkTime()
		);
	}

}
