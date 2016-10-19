package analytics.wifi;

import java.util.ArrayList;

/**
 * List of visible access point to a specific mobile node, during a given period of time
 * it extends an ArrayList and has certain methods specific to nodes. 
 * 
 * @author xhu2
 *
 */
public class VisibleAPsList<E> extends ArrayList<VisibleAP> {
	/**
	 * returns the node object associated with the given id.
	 * 
	 * @param id
	 * @return
	 */
	public VisibleAP getVisibleAPById(String id) {
		// iterate through list until the ap with correct id is found
		for(VisibleAP ap: this) {
			if(ap.getID().equals(id)) 
				return ap;
		}
		
		// if id was not found, return null
		return null;
	}
}
