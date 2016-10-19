package analytics.bluetooth;

import java.util.ArrayList;

/**
 * List of BT community patterns to a specific mobile node, during a given period of time
 * it extends an ArrayList and has certain methods specific to nodes. 
 * 
 * @author xhu2
 *
 */
public class BTCommunityList<E> extends ArrayList<BTCommunity> {
	/**
	 * returns the community pattern associated with the given pattern id.
	 * 
	 * @param id
	 * @return
	 */
	public BTCommunity getCommunityById(String id) {
		// iterate through list until the community with correct id is found
		for(BTCommunity pattern: this) {
			if(pattern.getID().equals(id)) 
				return pattern;
		}
		
		// if id was not found, return null
		return null;
	}
}
