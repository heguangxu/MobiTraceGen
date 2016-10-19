package analytics.events;

import java.util.*;
import java.io.*;

import analytics.utility.StatCalc;

public class StatesSummaryMultiDays {
	private int type_cnt = 0;
	private int start_hr = 0;
	private int end_hr = 24;
	private int bin_size = 5;
	private String year = "2012";
	private String month = "07"; 
	private boolean rssi_filter = false;
	
	private HashMap<String, double[]> oneDeviceStates;   // date : statistics
	private HashMap<String, StatCalc[]> allDevicesStates;  // date : statistics (mean)
	
	public StatesSummaryMultiDays(int _startHr, int _endHr, int _binSize, String _year, String _month, boolean _rssi_filter) {
		type_cnt = 3;
		start_hr = _startHr;
		end_hr = _endHr;
		bin_size = _binSize;
		year = _year;
		month = _month;
		rssi_filter = _rssi_filter;
		
		oneDeviceStates = new HashMap<String, double[]>();
		oneDeviceStates.clear();
		
		allDevicesStates = new HashMap<String, StatCalc[]>();
		allDevicesStates.clear();
	}
	
	public void extractMultiDayStat(String socsID) {
		File btDir = new File("./socsData/Bluetooth/" + year + "/" + month + "/");
    	File[] btFiles = btDir.listFiles();
    	
    	File wfDir = new File("./socsData/WiFi/" + year + "/" + month + "/");
    	File[] wfFiles = wfDir.listFiles();
    	
    	if (btFiles.length != wfFiles.length) {
    		System.out.println("BT and WiFi directory files don't match.");
    		return;
    	}
    	
    	for (int i = 0; i < btFiles.length && i < wfFiles.length; i++) {
    		String btFile = btDir.getPath() + "/" + btFiles[i].getName();
    		String wfFile = wfDir.getPath() + "/" + wfFiles[i].getName();
    		
    		StatesSummaryOneDay oneDayStat = new StatesSummaryOneDay(start_hr, end_hr, bin_size, rssi_filter);
    		
    		oneDayStat.createStatesMapBT(btFile);
    		oneDayStat.createStatesMapWiFi(wfFile);
    		oneDayStat.countStates();
    		
    		double[] oneDeviceDist = new double[type_cnt];
    		if (oneDayStat.getOneDeviceStatesDist(socsID) != null) {
    			oneDeviceDist = oneDayStat.getOneDeviceStatesDist(socsID);
    		} 
    		
    		StatCalc[] allDevicesDist = new StatCalc[type_cnt];
    		allDevicesDist = oneDayStat.getAllDevicesStatesStat();
    		
    		String btDate = btFiles[i].getName().substring(0, 2);
    		String wfDate = wfFiles[i].getName().substring(0, 2);
    		
    		if (!btDate.equals(wfDate)) {
    			System.out.println("BT and WiFi files don't pair.");
    			System.exit(0);
    		}
    		
    		if (!oneDeviceStates.containsKey(btDate)) {
    			oneDeviceStates.put(btDate, oneDeviceDist);
    		}
    		
    		if (!allDevicesStates.containsKey(wfDate)) {
    			allDevicesStates.put(wfDate, allDevicesDist);
    		}
    	}
	}
	
	public void printOneDeviceStatesDist() {
		try {
			String flag = new Boolean(rssi_filter).toString();
			BufferedWriter writer = new BufferedWriter(new FileWriter(year + month + "_" + start_hr + "-" + end_hr + "_" + flag +  "_OneDeviceStates.csv"));
			
			Object[] key = oneDeviceStates.keySet().toArray();
            Arrays.sort(key);
			
            for(int i = 0; i < key.length; i++) {
            	double[] percents = oneDeviceStates.get(key[i]);
            	String line = month + "/" + key[i] + ",";
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
	
	public void printAllDevicesStatesStat() {
		try {
			String flag = new Boolean(rssi_filter).toString();
			BufferedWriter writer = new BufferedWriter(new FileWriter(year + month + "_" + start_hr + "-" + end_hr + "_" + flag + "_AllDevicesStates.csv"));
			
			Object[] key = allDevicesStates.keySet().toArray();
            Arrays.sort(key);
			
            for(int i = 0; i < key.length; i++) {
            	StatCalc[] stats = allDevicesStates.get(key[i]);
            	String line = month + "/" + key[i]+ ",";
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
