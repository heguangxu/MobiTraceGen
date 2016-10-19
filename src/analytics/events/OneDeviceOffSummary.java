package analytics.events;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class OneDeviceOffSummary {

	private int type_cnt = 0;
	private int start_hr = 0;
	private int end_hr = 24;
	private int bin_size = 5;
	private String date = "2012/07/01";

	private ArrayList<String> targetDevices; 
	
	private HashMap<String, double[]> socsStatesPercent; 
	private HashMap<String, ArrayList<Long>> socsStamps;
	private HashMap<String, Integer> socsSampleCnt;
	private HashMap<String, int[]> socsSamplesDistr;
	
	
	public OneDeviceOffSummary(int _startHr, int _endHr, int _binSize, String _date) {
		type_cnt = 3;
		start_hr = _startHr;
		end_hr = _endHr;
		bin_size = _binSize;
        date = _date; 
        
        targetDevices = new ArrayList<String>();
        targetDevices.clear();
        
        socsStatesPercent = new HashMap<String, double[]>();
        socsStatesPercent.clear();
        
    	socsStamps = new HashMap<String, ArrayList<Long>>();
    	socsStamps.clear();
    	
    	socsSampleCnt = new HashMap<String, Integer>();
    	socsSampleCnt.clear();
    	
    	socsSamplesDistr = new HashMap<String, int[]>();
    	socsSamplesDistr.clear();
	}
	
	public void identifyTopDevics(double threshold) {
		String btFile = "./socsData/Bluetooth/" + date + ".csv";
		String wfFile = "./socsData/WiFi/" + date + ".csv";
		StatesSummaryOneDay oneDayStat = new StatesSummaryOneDay(start_hr, end_hr, bin_size, true);
		oneDayStat.createStatesMapBT(btFile);
		oneDayStat.createStatesMapWiFi(wfFile);
		oneDayStat.countStates();
		
		//HashMap<String, String[]> socsStates = oneDayStat.getStatesMap();
		socsStatesPercent = oneDayStat.getStatesPercent();
		socsStamps = oneDayStat.getTimeStamps();
		socsSampleCnt = oneDayStat.getSampleCnt();
		socsSamplesDistr = oneDayStat.getSampleDistr();
		
		Iterator<String> iter = socsStatesPercent.keySet().iterator();
		while (iter.hasNext()) {
			String socsID = iter.next();
			double noWiFiPercent = socsStatesPercent.get(socsID)[1];
			if (noWiFiPercent >= threshold) {
				targetDevices.add(socsID);
				System.out.println(socsID + " : " +  noWiFiPercent + " has no WiFi.");
			}
		}
	}

	public void printDevicesSampleDetails() {
		try {
			String[] day = date.split("/");
			BufferedWriter writer = new BufferedWriter(new FileWriter(day[0] + "_" + day[1] + "_" + day[2] + "_DeviceSampleDetails.csv"));
			
    		for (int i = 0; i < targetDevices.size(); i++) {
    			String deviceID = targetDevices.get(i);
    			double percent = socsStatesPercent.get(deviceID)[1];
    			ArrayList<Long> stamps = socsStamps.get(deviceID);
    			if (stamps!=null && !stamps.isEmpty())
    			    Collections.sort(stamps);
    			String startTime = getEpochTime(stamps.get(0));
    			String endTime = getEpochTime(stamps.get(stamps.size()-1));
    			int sampleCnt = socsSampleCnt.get(deviceID);
    			
    			String line = deviceID + "," + percent + "," + startTime + "," + endTime + "," + sampleCnt;
    			writer.write(line + "\n");
    		}
    		
        	writer.flush();
        	writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void printAllDevicesSampleDistribution() {
		for (int i = 0; i < targetDevices.size(); i++) {
			printDeviceSampleDistribution(targetDevices.get(i));
		}
	}
	
	public void printDeviceSampleDistribution(String socsID) {
		try {
			int[] distri = socsSamplesDistr.get(socsID);
			String[] day = date.split("/");
			BufferedWriter writer = new BufferedWriter(new FileWriter("./Sample Dist/" + day[0] + day[1] + day[2] + "_" + socsID + "SampleDist.csv"));
			
    		for (int i = 0; i < distri.length; i++) {
    			String line = (i+1) + "," + distri[i]; 
    			writer.write(line + "\n");
    		}
        	writer.flush();
        	writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	private String getEpochTime(Long stamp) {
		String date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
		.format(new Date(stamp * 1000));
		String time = date.substring(11);
		return time;
	}
}
