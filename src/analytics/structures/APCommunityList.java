package analytics.structures;

import java.util.ArrayList;

/**
 * List of AP community patterns to a specific mobile node, during a given period of time
 * it extends an ArrayList and has certain methods specific to nodes. 
 * 
 * @author xhu2
 *
 */
public class APCommunityList<E> extends ArrayList<APCommunity>  {
	/**
	 * returns the community pattern associated with the given pattern id.
	 * 
	 * @param id
	 * @return
	 */
	public APCommunity getCommunityById(String id) {
		// iterate through list until the community with correct id is found
		for(APCommunity pattern: this) {
			if(pattern.getID().equals(id)) 
				return pattern;
		}
		
		// if id is not found, return null
		return null;
	}
}
