/**
 * 
 */
package requests;

import java.util.Comparator;

/**
 * Comparator that compares two request by the time left to deliver them
 *
 * @author Jakub Krizanovsky
 * @version 29.11.2022
 */
public class RequestTimeLeftComparator implements Comparator<Request> {
	
	/**
	 * Constructs the object.
	 */
	public RequestTimeLeftComparator() {
		//No actions
	}

	/**
	 * Compares the two request based on the time left to deliver them
	 * @param o1 request 1
	 * @param o2 request 2
	 */
	@Override
	public int compare(Request o1, Request o2) {
		int timeLeftDiff = (int)Math.signum((o1.getDeliveryTime() - o1.getRequestTime()) - (o2.getDeliveryTime() - o2.getRequestTime()));
		if(timeLeftDiff != 0) {
			return timeLeftDiff;
		}
		
		return o1.compareTo(o2);
	}

}
