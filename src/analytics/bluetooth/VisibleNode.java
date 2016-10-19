package analytics.bluetooth;

public class VisibleNode {
	/** attributes **/
	private String id;   // socs number
	private int index;   // index in the proximity matrix
	private int count;   // count of visible cases during a given time period 
	
    public VisibleNode(String _id, int _index, int _cnt) {
		id = _id;
		index = _index;
		count = _cnt;
		
	}
    
    public String getID(){
    	return id;
    }
    
    public int getIndex(){
    	return index;
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
