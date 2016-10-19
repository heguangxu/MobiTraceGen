package analytics.greenzone;

import java.io.*;
import java.util.*;
import java.text.*;

import analytics.utility.ConvertEpochTime;
import analytics.utility.StatCalc;

/**
* Class used to analyze the BT and WiFi data and 
* to calculate the statistical 
* information of the node-in-greenzone time
* in one day. i.e. process one input data file
* @author xhu2, Oct 2012
*/

public class GreenZoneStatOneDay {
	private final int MINUTES = 24*60;  // total number of the minutes in a day 
	private final int NODE_CNT = 201;	// maximum number of mobile nodes + 1
	
	private int min_bin_size = 0;       // the min duration should be 5 minutes
	private int bin_size = 0;           // sampling duration in minutes
	private int max_bin_cnt = 0;        // the max number of bins
	private int bin_cnt = 0;            // number of sampling bins 
	
	private OneDayStat gzStat;          // the stat record of green zone time, for all nodes across a day
	private OneDayStat onStat;          // the stat record of device on time, for all nodes across a day
	
	private HashMap<String, boolean[]> device_on;    // node socsID : deviceOn[timeIndex], true indicates on at given timeIndex
	private HashMap<String, boolean[]> in_greenzone; // node socsID : inGreenZone[timeIndex], true indicates in green zone at given timeIndex
	
	private HashMap<String, Double> deviceOnTime;
	private HashMap<String, Double> inGreenzoneTime; 
	private HashMap<String, OneDayStat> inGreenZoneWindowStat;  // the width of continuously in-green-zone time: average
	
	public GreenZoneStatOneDay(int _duration) {
    	min_bin_size = 5; 
    	bin_size = _duration;
    	max_bin_cnt = MINUTES / min_bin_size;
    	bin_cnt = MINUTES / bin_size;
    	
    	gzStat = new OneDayStat();
    	onStat = new OneDayStat();
    	
    	device_on = new HashMap<String, boolean[]>();
    	device_on.clear();
    	
    	in_greenzone = new HashMap<String, boolean[]>();
    	in_greenzone.clear();
    	
    	deviceOnTime = new HashMap<String, Double>();
    	deviceOnTime.clear();
    	
    	inGreenzoneTime = new HashMap<String, Double>();
    	inGreenzoneTime.clear();
    	
    	inGreenZoneWindowStat = new HashMap<String, OneDayStat>();
    	inGreenZoneWindowStat.clear();
    	
	}
	
	public void createDeviceOnMap(String fileName) {
		try{
            BufferedReader file = new BufferedReader(new FileReader(fileName));
            String dataRow = file.readLine(); // Read first line.
            // The while checks to see if the data is null. If   
            // it is, we've hit the end of the file. If not,   
            // process the data.  
            while(dataRow != null) {   
                String[] dataArray = dataRow.split(",");
                
                if(dataArray.length != 5) {
                    System.out.println("Error: The bt data file is corrupted. exit");
                    System.exit(0);
                }
                
                String socsID = dataArray[0];                   // the socs ID of current device
                long timeStamp = Long.parseLong(dataArray[1]);  // sample time 
                //short sigStr = Short.parseShort(dataArray[4]);  // rssi value
                
                if(socsID.contains("socs")) {
                	ConvertEpochTime ept = new ConvertEpochTime();
                    int binIndex = ept.getTimeIndex(timeStamp, min_bin_size);
                    //System.out.println("socs index: " + j + " " + k);
                    
                    // fill in the device_on map
                    if(!device_on.containsKey(socsID)) {
                    	boolean[] isOn = new boolean[max_bin_cnt];
                    	for(int m = 0; m < max_bin_cnt; m++) {
                    		isOn[m] = false;
                    	}
                    	device_on.put(socsID, isOn);
                    }
                    	
                    device_on.get(socsID)[binIndex] = true;
                }
                dataRow = file.readLine(); // Read next line of data.
            }  
            // Close the file once all data has been read. 
            file.close();
        } catch(FileNotFoundException e) {
            // Inform user that file was not found.    
            e.printStackTrace();
        } catch(IOException e) {
            System.err.println("Error: " + e);
        }	
	}
	
	public void createInGreenzoneMap(String fileName) {
		try {
            BufferedReader file = new BufferedReader(new FileReader(fileName));
            String dataRow = file.readLine(); // Read first line.
            // The while checks to see if the data is null. If   
            // it is, we've hit the end of the file. If not,   
            // process the data.  
            
            while(dataRow != null) {   
                String[] dataArray = dataRow.split(",");
                
                if(dataArray.length != 5) {
                    System.out.println("Error: The wifi data file is corrupted, exit!");
                    System.exit(0);
                }
                
                String socsID = dataArray[0];        // the socs id number
                long timeStamp = Long.parseLong(dataArray[1]);  // time stamp
                String wifi = dataArray[2];          // wifi type: ND-Secure, nomad, etc
                //String mac = dataArray[3];           // the mac address of AP       
                //short sigStr = Short.parseShort(dataArray[4]);  // rssi value
                
                if(socsID.contains("socs")) {
                	ConvertEpochTime ept = new ConvertEpochTime();
                	int binIndex = ept.getTimeIndex(timeStamp, min_bin_size);
                	if(!in_greenzone.containsKey(socsID)) {
                		boolean[] inGreenZone =  new boolean[max_bin_cnt];
                    	for(int m = 0; m < max_bin_cnt; m++) {
                    		inGreenZone[m] = true;
                    	}
                    	in_greenzone.put(socsID, inGreenZone); 
                	}
                	
                	if(device_on.get(socsID) != null) {
                	if(device_on.get(socsID)[binIndex] == false) { // at this time bin the device is off
                		in_greenzone.get(socsID)[binIndex] = false;
                	} 
                	
                	if((device_on.get(socsID)[binIndex] == true) && (wifi.equals("ND-secure")||wifi.equals("ND-guest")||wifi.equals("nomad"))) {
                		in_greenzone.get(socsID)[binIndex] = false;
                	}
                	}
                }
                dataRow = file.readLine(); // Read next line of data.
            }  
            // Close the file once all data has been read. 
            file.close();
        } catch(FileNotFoundException e) {
            // Inform user that file was not found.    
            e.printStackTrace();
        } catch(IOException e) {
            System.err.println("Error: " + e);
        }	
	}
	
	public HashMap<String, Double> calcOneDeviceOnTime() {
		Iterator<String> iter = device_on.keySet().iterator();
		while (iter.hasNext()) {
			String socsID = iter.next();
			boolean[] deviceOn = device_on.get(socsID);
			int onBinCnt = 0;
			for(int i = 0; i < deviceOn.length; i++) {
				if(deviceOn[i] == true) {
					onBinCnt++;
				}
			}
			double percent = ((double)onBinCnt) / ((double)deviceOn.length);
		    if(!deviceOnTime.containsKey(socsID)) {
		    	deviceOnTime.put(socsID, percent);
		    }
		}
		
		return deviceOnTime;
	}
	
	public HashMap<String,Double> calcOneDeviceGreenzoneTime() {
		Iterator<String> iter = in_greenzone.keySet().iterator();
		while (iter.hasNext()) {
			String socsID = iter.next();
			boolean[] inGreenzone = in_greenzone.get(socsID);
			int inGreenCnt = 0;
			for(int i = 0; i < inGreenzone.length; i++) {
				if(inGreenzone[i] == true) {
					inGreenCnt++;
				}
			}
			double percent = ((double)inGreenCnt) / ((double)inGreenzone.length);
		    if(!inGreenzoneTime.containsKey(socsID)) {
		    	inGreenzoneTime.put(socsID, percent);
		    }
		}
		
		return inGreenzoneTime;
	}

	public OneDayStat getAllDevicesOnTimeStat() {
		StatCalc stat = new StatCalc();
		Iterator<String> iter = deviceOnTime.keySet().iterator();
		while (iter.hasNext()) {
    		String socsID = iter.next();
    		stat.enter(deviceOnTime.get(socsID));
    	}
		
		onStat.setValues(stat.getMean(), stat.getMin(), stat.getMax(), stat.getStandardDeviation());
		return onStat;
	}
	
	public OneDayStat getAllDevicesGreenzoneTimeStat() {
		StatCalc stat = new StatCalc();
		Iterator<String> iter = inGreenzoneTime.keySet().iterator();
		while (iter.hasNext()) {
    		String socsID = iter.next();
    		stat.enter(inGreenzoneTime.get(socsID));
    	}
		
		gzStat.setValues(stat.getMean(), stat.getMin(), stat.getMax(), stat.getStandardDeviation());
	    return gzStat;
	}
	
    public HashMap<String,OneDayStat> calcOneDeviceGreenzoneWindowStat() {
    	Iterator<String> iter = in_greenzone.keySet().iterator();
		while (iter.hasNext()) {
			String socsID = iter.next();
			boolean[] inGreenzone = in_greenzone.get(socsID);
			
			StatCalc stat = new StatCalc();
			int contWindowWidth = 0;
			for(int i = 0; i < inGreenzone.length; i++) {
				if(inGreenzone[i] == true) {
					contWindowWidth++;
				} else {// current in-green-zone continuous window ends
					    // add the  window size for statistics, reset window width to be 0
					if (contWindowWidth > 0)
						stat.enter(contWindowWidth*min_bin_size);
					contWindowWidth = 0;
				}
			}
			
			double avg = stat.getMean();
			double min = stat.getMin();
			double max = stat.getMax();
			double stdev = stat.getStandardDeviation();
			OneDayStat onedaystat = new OneDayStat();
			onedaystat.setValues(avg, min, max, stdev);
			
			if(!inGreenZoneWindowStat.containsKey(socsID)) {
				inGreenZoneWindowStat.put(socsID, onedaystat);
			}
		}
		return inGreenZoneWindowStat;
	}
}

