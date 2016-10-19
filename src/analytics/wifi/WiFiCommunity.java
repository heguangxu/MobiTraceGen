package analytics.wifi;

public class WiFiCommunity {
	/** attributes **/
	private String combination;   // ap mac address combination
	private int count;   // count of visible cases during a given time period 
	private int time_slots; // the total number of time slots 
	
    public WiFiCommunity(String _combination, int _cnt, int _time_slots) {
    	combination = _combination;
		count = _cnt;	
		time_slots = _time_slots;
	}
    
    public String getID(){
    	return combination;
    }
    
    public int getCount(){
    	return count; 
    }
    
    public void setCount(int cnt) {
    	count = cnt;
    }
    
    public void increaseCount() { 
    	count++;
    }
    
    public double calcTimePercent() {
    	return (double)count/(double)time_slots;
    }
}
