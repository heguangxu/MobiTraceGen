package analytics.greenzone;

public class OneDayStat {
    private double avg = 0.0; // mean value
    private double min = 0.0; // min value
    private double max = 0.0; // max value
    private double std = 0.0; // standard deviation
    
    public void setValues(double _avg, double _min, double _max, double _std) {
    	avg = _avg;
    	min = _min;
    	max = _max;
    	std = _std;
    }
    
    public double getAvg() {
    	return avg;
    }
    
    public double getMin() {
    	return min;
    }
    
    public double getMax() {
    	return max;
    }
    
    public double getStdev() {
    	return std;
    }
}
