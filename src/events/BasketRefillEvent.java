package events;

import simulation.Map;
import simulation.Statistics;
import simulation.Warehouse;

/**
 * Represents a warehouse basket refill event.
 * 
 * @author Stanislav Kafara, Jakub Krizanovsky
 * @version 2 02-12-22
 */
public class BasketRefillEvent extends AEvent {
	
	/** Priority. */
	protected static final int PRIORITY = 70;
	private static final Map MAP = Map.getInstance();
	
	private final Warehouse warehouse;

	/**
	 * Constructs the warehouse basket refill event.
	 * @param time Time of refill.
	 * @param warehouse Warehouse.
	 */
	public BasketRefillEvent(double time, Warehouse warehouse) {
		super(time, PRIORITY);
		this.warehouse = warehouse;
	}

	/** Refills the baskets in warehouse and tries to process unfinished requests. */
	@Override
	public void process() {
		Statistics.getInstance().addWarehouseRefill(warehouse, time, warehouse.getBasketCount());
		warehouse.refillBaskets();
		MAP.tryProcessUnfinishedRequests(warehouse);
	}

}
