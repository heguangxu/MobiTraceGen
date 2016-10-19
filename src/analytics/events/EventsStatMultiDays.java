package analytics.events;

import java.util.*;
import java.io.*;

import analytics.utility.StatCalc;

/**
* Class used to analyze the WiFi data and 
* calculate the statistical 
* distribution of the state transitions
* for multiple days
* @author xhu2, Oct 2012
*/
public class EventsStatMultiDays {

	private int bin_cnt = 0;
	private HashMap<String, double[]> oneDeviceEvents;   // date : statistics
	private HashMap<String, StatCalc[]> allDevicesEvents;  // date : statistics (mean)
	
	public EventsStatMultiDays(int binCnts) {
		bin_cnt = binCnts;
		
		oneDeviceEvents = new HashMap<String, double[]>();
		oneDeviceEvents.clear();
		
		allDevicesEvents = new HashMap<String, StatCalc[]>();
		allDevicesEvents.clear();
	}
	
	public void extractMultiDayStat(String socsID) {
		File btDir = new File("./socsData/Bluetooth/2012_07/");
    	File[] btFiles = btDir.listFiles();
    	
    	File wfDir = new File("./socsData/WiFi/2012_07/");
    	File[] wfFiles = wfDir.listFiles();
    	
    	if (btFiles.length != wfFiles.length) {
    		System.out.println("BT and WiFi directory files don't match.");
    		return;
    	}
    	
    	for (int i = 0; i < btFiles.length && i < wfFiles.length; i++) {
    		String btFile = "./socsData/Bluetooth/2012_07/" + btFiles[i].getName();
    		String wfFile = "./socsData/WiFi/2012_07/" + wfFiles[i].getName();
    		
    		EventsStatOneDay oneDayStat = new EventsStatOneDay(5);
    		
    		//
    		oneDayStat.createDeviceOnMap(btFile);
    		oneDayStat.createWiFiProximityMap(wfFile);
    		oneDayStat.countEvents();
    		
    		double[] oneDeviceDist = new double[bin_cnt];
    		if (oneDayStat.getOneDeviceEventsDist(socsID) != null) {
    			oneDeviceDist = oneDayStat.getOneDeviceEventsDist(socsID);
    		} 
    		
    		StatCalc[] allDevicesDist = new StatCalc[bin_cnt];
    		allDevicesDist = oneDayStat.getAllDevicesEventStat();
    		
    		String btDate = btFiles[i].getName().substring(0, 2);
    		String wfDate = wfFiles[i].getName().substring(0, 2);
    		
    		if (!btDate.equals(wfDate)) {
    			System.out.println("BT and WiFi  files don't pair.");
    			return;
    		}
    		
    		if (!oneDeviceEvents.containsKey(btDate)) {
    			oneDeviceEvents.put(btDate, oneDeviceDist);
    		}
    		
    		if (!allDevicesEvents.containsKey(wfDate)) {
    			allDevicesEvents.put(wfDate, allDevicesDist);
    		}
    	}
	}
	
	public void printOneDeviceEventsDist() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("OnDeviceEventsJuly.csv"));
			
			Object[] key = oneDeviceEvents.keySet().toArray();
            Arrays.sort(key);
			
            for(int i = 0; i < key.length; i++) {
            	double[] percents = oneDeviceEvents.get(key[i]);
            	String line = key[i] + ",";
            	for (int j = 0; j < percents.length; j++) {
            		line += percents[j] + ",";
            	}
            	writer.write(line + "\n");
            }
        	writer.flush();
        	writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void printAllDevicesEventsStat() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("AllDeviceEventsJuly.csv"));
			
			Object[] key = allDevicesEvents.keySet().toArray();
            Arrays.sort(key);
			
            for(int i = 0; i < key.length; i++) {
            	StatCalc[] stats = allDevicesEvents.get(key[i]);
            	String line = key[i] + ",";
            	for (int j = 0; j < stats.length; j++) {
            		line += stats[j].getMean() + ",";
            	}
            	writer.write(line + "\n");
            }
        	writer.flush();
        	writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
