/**
 * 
 */
package ui;

import events.AEvent;

/**
 * Event that pauses the simulation and lets user make inputs again
 *
 * @author Jakub Krizanovsky
 * @version 29.11.2022
 */
public class PauseEvent extends AEvent {

	private static final int PRIORITY = 100;
	private final UserInterface ui;
	
	/**
	 * Constructor for PauseEvent
	 * @param time time when event should be processed
	 * @param ui user interface
	 */
	public PauseEvent(double time, UserInterface ui) {
		super(time, PRIORITY);
		this.ui = ui;
	}

	@Override
	public void process() {
		ui.pause();
	}

}
