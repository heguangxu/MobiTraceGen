package analytics.wifi;

public class VisibleAP {
	/** attributes **/
	private String id;   // access point mac address
	private int count;   // count of visible cases during a given time period 
	private int rssi;    // the associated rssi value
	
    public VisibleAP(String _id, int _cnt) {
		id = _id;
		count = _cnt;
		//rssi = _rssi;
		
	}
    
    public String getID() {
    	return id;
    }
    
    public int getCount() {
    	return count; 
    }
    
    public int getRSSI() {
    	return rssi; 
    }
    
    public void setCount(int cnt) {
    	count = cnt;
    }
    
    public void increaseCount() { 
    	count++;
    }
}
