package analytics.mobitrace;

import java.util.*;

public class VisibleEntitiesList extends ArrayList<VisibleEntity> {
	/**
	 * returns the visible neighbor object associated with the given id.
	 * 
	 * @param id
	 * @return
	 */
	public VisibleEntity getVisibleEntityById(String id) {
		// iterate through list until the entity with correct id is found
		for(VisibleEntity e: this) {
			if(e.getID().equals(id)) 
				return e;
		}
		
		// if id was not found, return null
		return null;
	}
}
