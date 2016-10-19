package analytics.bluetooth;

import java.util.ArrayList;

/**
 * List of visible mobile nodes to a specific mobile node during a given period of time,
 * it extends an ArrayList and has certain methods specific to nodes. 
 * 
 * @author xhu2
 *
 */
public class VisibleNodesList<E> extends ArrayList<VisibleNode> {
	/**
	 * Returns the node object associated with the given id.
	 * 
	 * @param id
	 * @return
	 */
	public VisibleNode getVisibleNodeById(String id) {
		// iterate through list until the node with correct id is found
		for(VisibleNode n: this) {
			if(n.getID().equals(id)) 
				return n;
		}
		
		// if id was not found, return null
		return null;
	}
}
