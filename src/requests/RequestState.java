package requests;

/**
 * Possible states that a request can be in
 *
 * @author Jakub Krizanovsky
 * @version 29.11.2022
 */
public enum RequestState {
	/** Request has not been received yet */
	Inactive, 
	/** Request has been received and is being delivered */
	Active, 
	/** Request has been received but no baskets could be sent yet */
	Unprocessed, 
	/** Request has been received, but only some and not all baskets have been sent yet */
	Unfinished, 
	/** All of the baskets of the request have been delivered, but camels are yet to return home */
	Delivered, 
	/** All baskets have been delivered and all camel have returned */
	Completed, 
	/** Request has been cancelled by the user */
	Cancelled
}
