package analytics.greenzone;

import java.io.*;
import java.util.*;
import java.text.*;

/**
* Class used to analyze the WiFi data and 
* calculate the statistical 
* information of the node-in-greenzone time
* for multiple days
* @author xhu2, Oct 2012
*/
public class GreenZoneStatMultiDays {

	private HashMap<String, Double> oneDeviceOnTime;                 // for one device  - date : 
	private HashMap<String, Double> oneDeviceGreenzoneTime;          // for one device
    private HashMap<String, OneDayStat> oneDeviceGreenzoneWindowStat;         // for one device
    
	private HashMap<String, OneDayStat> allDevicesOnTimeStat;        // for all devices 
	private HashMap<String, OneDayStat> allDevicesGreenzoneTimeStat; // for all devices
    
	public GreenZoneStatMultiDays() {
		oneDeviceOnTime = new HashMap<String, Double>();
		oneDeviceOnTime.clear();
		
		oneDeviceGreenzoneTime = new HashMap<String, Double>();
		oneDeviceGreenzoneTime.clear();
		
		oneDeviceGreenzoneWindowStat = new HashMap<String, OneDayStat>();
		oneDeviceGreenzoneWindowStat.clear();
		
		allDevicesOnTimeStat = new HashMap<String, OneDayStat>();
		allDevicesOnTimeStat.clear();
		
		allDevicesGreenzoneTimeStat = new HashMap<String, OneDayStat>();
		allDevicesGreenzoneTimeStat.clear();
		
	}
	
	public void extractMultiDayStat(String socsID) { 
		File btDir = new File("./socsData/Bluetooth/2012_04/");
    	File[] btFiles = btDir.listFiles();
    	
    	File wfDir = new File("./socsData/WiFi/2012_04/");
    	File[] wfFiles = wfDir.listFiles();
    	
    	if(btFiles.length != wfFiles.length) {
    		System.out.println("BT and WiFi directory files don't match.");
    		return;
    	}
    	
    	for (int i = 0; i < btFiles.length && i < wfFiles.length; i++) {
    		String btFile = "./socsData/Bluetooth/2012_04/" + btFiles[i].getName();
    		String wfFile = "./socsData/WiFi/2012_04/" + wfFiles[i].getName();
    		
    		GreenZoneStatOneDay oneDayStat = new GreenZoneStatOneDay(5);
    		
    		//
    		oneDayStat.createDeviceOnMap(btFile);
    		double onTimePercent = 0.0;
    		if(oneDayStat.calcOneDeviceOnTime().get(socsID) != null)
    			onTimePercent = oneDayStat.calcOneDeviceOnTime().get(socsID);
    		
    		//
    		oneDayStat.createInGreenzoneMap(wfFile);
    		double inGreenzoneTimePercent =0.0;
    		if(oneDayStat.calcOneDeviceGreenzoneTime().get(socsID) != null)
    			inGreenzoneTimePercent = oneDayStat.calcOneDeviceGreenzoneTime().get(socsID);
    		
    		OneDayStat gzWindowStat = new OneDayStat();
    		if(oneDayStat.calcOneDeviceGreenzoneWindowStat().get(socsID) != null)
    			gzWindowStat = oneDayStat.calcOneDeviceGreenzoneWindowStat().get(socsID);
    		
    		OneDayStat allDevicesOnTime = new OneDayStat();
    		allDevicesOnTime = oneDayStat.getAllDevicesOnTimeStat();
    		
    		OneDayStat allDeviceGreenzoneTime = new OneDayStat();
    		allDeviceGreenzoneTime = oneDayStat.getAllDevicesGreenzoneTimeStat();
    		
    		String btDate = btFiles[i].getName().substring(0, 2);
    		String wfDate = wfFiles[i].getName().substring(0, 2);
    		
    		if(!btDate.equals(wfDate)) {
    			System.out.println("BT and WiFi  files don't pair.");
    			return;
    		}
    		
    		if(!oneDeviceOnTime.containsKey(btDate)) {
    			oneDeviceOnTime.put(btDate, onTimePercent);
    		}
    		
    		if(!oneDeviceGreenzoneTime.containsKey(wfDate)) {
    			oneDeviceGreenzoneTime.put(wfDate, inGreenzoneTimePercent);
    		}
    		
    		if(!oneDeviceGreenzoneWindowStat.containsKey(wfDate)) {
    			oneDeviceGreenzoneWindowStat.put(wfDate, gzWindowStat);
    		}
    		
    		if(!allDevicesOnTimeStat.containsKey(btDate)) {
    			allDevicesOnTimeStat.put(btDate, allDevicesOnTime);
    		}
    		
    		if(!allDevicesGreenzoneTimeStat.containsKey(wfDate)) {
    			allDevicesGreenzoneTimeStat.put(wfDate, allDeviceGreenzoneTime);
    		}
    	}
	}
	
	public void printOneDeviceStat() {
    	try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("DeviceOnandGreenzone.csv"));
			
			Iterator<String> it = oneDeviceOnTime.keySet().iterator();
			while(it.hasNext()) {
				String date = it.next();
				double onTime = oneDeviceOnTime.get(date);
				double gzTime = oneDeviceGreenzoneTime.get(date);
				String line = date + "," + onTime + "," + gzTime + "\n";
				writer.write(line);
			}
        	writer.flush();
        	writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			BufferedWriter writer1 = new BufferedWriter(new FileWriter("DeviceGreenZoneWindow.csv"));
			
			Iterator<String> it1 = oneDeviceGreenzoneWindowStat.keySet().iterator();
			while(it1.hasNext()) {
				String date = it1.next();
				OneDayStat windowStat = oneDeviceGreenzoneWindowStat.get(date);
				String line = date + "," + windowStat.getAvg() + "," + windowStat.getMin() + "," + windowStat.getMax() + "\n";
				writer1.write(line);
			}
        	writer1.flush();
        	writer1.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void printAllDevicesStat() {
    	try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("AllDevicesOnStat.csv"));
			
			Iterator<String> it = allDevicesOnTimeStat.keySet().iterator();
			while(it.hasNext()) {
				String date = it.next();
				OneDayStat stat = allDevicesOnTimeStat.get(date);
				String line = date + "," + stat.getAvg() + "," + stat.getMin() + "," + stat.getMax() + "\n";
				writer.write(line);
			}
        	writer.flush();
        	writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			BufferedWriter writer1 = new BufferedWriter(new FileWriter("AllDevicesGreenzoneStat.csv"));
			
			Iterator<String> it1 = allDevicesGreenzoneTimeStat.keySet().iterator();
			while(it1.hasNext()) {
				String date = it1.next();
				OneDayStat stat = allDevicesGreenzoneTimeStat.get(date);
				String line = date + "," + stat.getAvg() + "," + stat.getMin() + "," + stat.getMax() + "\n";
				writer1.write(line);
			}
        	writer1.flush();
        	writer1.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
