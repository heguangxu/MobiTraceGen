package analytics.mobitrace;

import java.util.ArrayList;
import java.util.HashMap;

import analytics.mobitrace.geomtypes.Point;

public class TT {
	private HashMap<String, Integer> map;  
	
	public TT(HashMap<String, Integer> _map) {
		map = new HashMap<String, Integer>();
		map.clear();
		map = _map;
		map.put("One", 1);
		map.put("Two", 2);
	}
	
	
}
