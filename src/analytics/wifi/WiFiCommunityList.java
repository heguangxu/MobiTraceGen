package analytics.wifi;

import java.util.ArrayList;

/**
 * List of AP community patterns to a specific mobile node, during a given period of time
 * it extends an ArrayList and has certain methods specific to nodes. 
 * 
 * @author xhu2
 *
 */
public class WiFiCommunityList<E> extends ArrayList<WiFiCommunity> {
	/**
	 * returns the community pattern associated with the given pattern id.
	 * 
	 * @param id
	 * @return
	 */
	public WiFiCommunity getCommunityById(String id) {
		// iterate through list until the community with correct id is found
		for(WiFiCommunity pattern: this) {
			if(pattern.getID().equals(id)) 
				return pattern;
		}
		
		// if id was not found, return null
		return null;
	}
}
