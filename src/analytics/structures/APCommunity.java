package analytics.structures;

public class APCommunity {
	/** attributes **/
	private String combination;   // ap mac address combination
	private int count;   // count of visible cases during a given time period 
	
    public APCommunity(String _combination, int _cnt) {
    	combination = _combination;
		count = _cnt;	
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
}
