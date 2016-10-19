package analytics.events;

import java.io.*;
import java.text.*;
import java.util.*;

import analytics.utility.ConvertEpochTime;
import analytics.utility.StatCalc;

public class StatesSummaryOneDay {
	// the three types of states each sample might be in
    public static final String OFF = "Off";
    public static final String NOWIFI = "NoWiFi";
    public static final String WIFI = "WiFi";
    
    // private attributes 
	private final int MINUTES = 24*60;  // total number of the minutes in a day 
	
	private int bt_bin_size = 5; 
	private int bt_bin_cnt = 24*60 / bt_bin_size;
	private int min_bin_size = 0;       // the min duration should be 5 minutes
	private int bin_size = 0;           // sampling duration in minutes
	private int max_bin_cnt = 0;
	private int bin_cnt = 0;            // number of sampling bins 
	private int state_type = 0;
	private int start_hr = 0;
	private int end_hr = 24;
	private boolean rssi_filter = false;
	
	private HashMap<String, String[]> socsStates;        // node socsID : state[timeIndex]
	private HashMap<String, int[]> socsStatesCnt;        // nodeID : associate event 
	private HashMap<String, double[]> socsStatesPercent; //
	
	private HashMap<String, ArrayList<Long>> socsStamps; // nodeID : all time stamps in BT samples
	private HashMap<String, Integer> socsSampleCnt;        // nodeID : # of BT samples
	private HashMap<String, int[]> socsSamplesDistr;
	
    // 3 possible types of states for samples
    private HashMap<String, Integer> state_index_mapping;
    
	public StatesSummaryOneDay(int _startHr, int _endHr, int binSize, boolean filter) {
		start_hr = _startHr;
		end_hr = _endHr;
		rssi_filter = filter;
		min_bin_size = 5;
    	bin_size = binSize;
    	max_bin_cnt = MINUTES / min_bin_size;
    	bin_cnt = MINUTES / bin_size;
    	state_type = 3;
    	
    	socsStates = new HashMap<String, String[]>();
    	socsStates.clear();
    	
    	socsStatesCnt = new HashMap<String, int[]>();
    	socsStatesCnt.clear();   	
    	
    	socsStatesPercent = new HashMap<String, double[]>();
    	socsStatesPercent.clear();
    	
    	socsStamps = new HashMap<String, ArrayList<Long>>();
    	socsStamps.clear();
    	
    	socsSampleCnt = new HashMap<String, Integer>();
    	socsSampleCnt.clear();
    	
    	socsSamplesDistr = new HashMap<String, int[]>();
    	socsSamplesDistr.clear();
    	
    	state_index_mapping = new HashMap<String, Integer>();
    	state_index_mapping.clear();
    	state_index_mapping.put(OFF, 0);
    	state_index_mapping.put(NOWIFI, 1);
    	state_index_mapping.put(WIFI, 2);
	}
	
	public void createStatesMapBT(String fileName) { // parse the BT data file
		try {
			BufferedReader file = new BufferedReader(new FileReader(fileName));
			String dataRow = file.readLine(); // Read first line.

			// The while checks to see if the data is null. If
			// it is, we've hit the end of the file. If not,
			// process the data.
			while (dataRow != null) {
				String[] dataArray = dataRow.split(",");

				if (dataArray.length != 5) {
					//System.out
							//.println("Error: The BT data file is corrupted!");
					//System.out.println(dataRow);
					//continue;
				}

				String socsID = dataArray[0]; // the socs ID of current device
				long timeStamp = Long.parseLong(dataArray[1]); // sample time
																// stamp
				// short sigStr = Short.parseShort(dataArray[4]); // rssi value

				if (socsID.contains("socs")) {
                	ConvertEpochTime cet = new ConvertEpochTime();
                    int binIndex = cet.getTimeIndex(timeStamp, bin_size);
					// System.out.println("socs index: " + j + " " + k);

					// fill in the device_on map
					if (!socsStates.containsKey(socsID)) {
						String[] states = new String[bin_cnt];
						for (int i = 0; i < bin_cnt; i++) {
							states[i] = OFF;
						}
						socsStates.put(socsID, states);
					}
					socsStates.get(socsID)[binIndex] = NOWIFI;
					
					/*if (!socsBTStamps.containsKey(socsID)) {
						ArrayList<Long> stamps = new ArrayList<Long>();
						stamps.clear();
						socsBTStamps.put(socsID, stamps);
					}
					socsBTStamps.get(socsID).add(timeStamp);
					
					
					if (!socsBTSampleCnt.containsKey(socsID)) {
						socsBTSampleCnt.put(socsID, 0);
					}
					int cnt = socsBTSampleCnt.get(socsID);
					socsBTSampleCnt.put(socsID, ++cnt);
					
					if (!socsSamplesDistr.containsKey(socsID)) {
						int[] dist = new int[bt_bin_cnt];
						for (int i = 0; i < bt_bin_cnt; i++) {
							dist[i] = 0;
						}
						socsSamplesDistr.put(socsID, dist);
					}
					int index = convertEpochTime(timeStamp, bt_bin_size);
					socsSamplesDistr.get(socsID)[index]++; */
				}
				dataRow = file.readLine(); // Read next line of data.
			}
			// Close the file once all data has been read.
			file.close();
			removeBTFalseNegatives();
		} catch (FileNotFoundException e) {
			// Inform user that file was not found.
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Error: " + e);
		}
	}
	
	public void createStatesMapWiFi(String fileName) {
		try {
			BufferedReader file = new BufferedReader(new FileReader(fileName));
			String dataRow = file.readLine(); // Read first line.
			// The while checks to see if the data is null. If
			// it is, we've hit the end of the file. If not,
			// process the data.

			while (dataRow != null) {
				String[] dataArray = dataRow.split(",");
				if (dataArray.length != 5) {
					System.out
							.println("Error: The WiFi data file is corrupted!");
					System.out.println(dataRow);
					continue;
				}

				String socsID = dataArray[0];   // the socs id number
				long timeStamp = Long.parseLong(dataArray[1]); // time stamp
				String network = dataArray[2];     // wifi type: ND-Secure, nomad, etc
				String apMac = dataArray[3];      // the mac address of AP
				int sigStr = Integer.parseInt(dataArray[4]); // rssi value
				
				if (socsID.contains("socs")) {
                	ConvertEpochTime cet = new ConvertEpochTime();
                    int binIndex = cet.getTimeIndex(timeStamp, bin_size);
				
					if (!socsStates.containsKey(socsID)) {
						String[] states = new String[bin_cnt];
						for (int i = 0; i < bin_cnt; i++) {
							states[i] = NOWIFI;
						}
						socsStates.put(socsID, states);
					}
					
					if (socsStates.get(socsID)[binIndex] != OFF 
						&& !network.contains("dummy")	
						&& !apMac.contains("dummy")) {
						if (rssi_filter) {
							if (sigStr >= -80)
								socsStates.get(socsID)[binIndex] = WIFI;
						} else {
							socsStates.get(socsID)[binIndex] = WIFI;
						}
					} 
					
					if (!socsStamps.containsKey(socsID)) {
						ArrayList<Long> stamps = new ArrayList<Long>();
						stamps.clear();
						socsStamps.put(socsID, stamps);
					}
					socsStamps.get(socsID).add(timeStamp);

					if (!socsSampleCnt.containsKey(socsID)) {
						socsSampleCnt.put(socsID, 0);
					}
					int cnt = socsSampleCnt.get(socsID);
					socsSampleCnt.put(socsID, ++cnt);

					if (!socsSamplesDistr.containsKey(socsID)) {
						int[] dist = new int[bt_bin_cnt];
						for (int i = 0; i < bt_bin_cnt; i++) {
							dist[i] = 0;
						}
						socsSamplesDistr.put(socsID, dist);
					}
					int index = cet.getTimeIndex(timeStamp, bt_bin_size);
					socsSamplesDistr.get(socsID)[index]++;
				}
				dataRow = file.readLine(); // Read next line of data.
			}
			// Close the file once all data has been read.
			file.close();
			removeWiFiFalseNegatives();
		} catch (FileNotFoundException e) {
			// Inform user that file was not found.
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Error: " + e);
		}
	}
	
	private void removeBTFalseNegatives() {
		Iterator<String> iter = socsStates.keySet().iterator();
		while (iter.hasNext()) {
			String socsID = iter.next();
			for (int i = 1; i < bin_cnt-1; i++) {
				if(socsStates.get(socsID)[i-1] != OFF && socsStates.get(socsID)[i+1] != OFF)
					socsStates.get(socsID)[i] = NOWIFI;
			}
		}
	}
	
	private void removeWiFiFalseNegatives() {
		Iterator<String> iter = socsStates.keySet().iterator();
		while (iter.hasNext()) {
			String socsID = iter.next();
			for (int i = 1; i < bin_cnt-1; i++) {
				if(socsStates.get(socsID)[i-1] == WIFI && socsStates.get(socsID)[i+1] == WIFI)
					socsStates.get(socsID)[i] = WIFI;
			}
		}
	}
	
	public HashMap<String, String[]> getStatesMap() {
		return socsStates;
	}
	
	public HashMap<String, double[]> getStatesPercent() {
		return socsStatesPercent;
	}
	
	public HashMap<String, ArrayList<Long>> getTimeStamps() {
		return socsStamps;
	}
	
	public HashMap<String, Integer> getSampleCnt() {
		return socsSampleCnt;
	}
	
	public HashMap<String, int[]> getSampleDistr(){
		return socsSamplesDistr;
	}
	
	public void countStates() {
		Iterator<String> iter = socsStates.keySet().iterator();
		while (iter.hasNext()) {
    		String socsID = iter.next();
    		if(!socsStatesCnt.containsKey(socsID)) {
    			int[] statesCnt = new int[state_type];
    			for(int i = 0; i < state_type; i++)
    				statesCnt[i] = 0;
    			socsStatesCnt.put(socsID, statesCnt);
    		}
    		
    		int startIndex = start_hr * (60 / bin_size);
    		int endIndex = end_hr * (60 / bin_size);
    		
    		for(int i = startIndex; i < endIndex; i++) {
    			String state = socsStates.get(socsID)[i];
    			socsStatesCnt.get(socsID)[state_index_mapping.get(state)]++;
    		}
		}
		normalizeStateCnts();
	}
	
	private void normalizeStateCnts() {
		int totalCnt = (end_hr - start_hr) * (60 / bin_size);
		Iterator<String> iter = socsStatesCnt.keySet().iterator();
		while (iter.hasNext()) {
			String socsID = iter.next();
            if (!socsStatesPercent.containsKey(socsID)) {			
            	int[] cnts = socsStatesCnt.get(socsID);
            	double[] percts = new double[cnts.length];
            	for (int i = 0; i < cnts.length; i++) {
            		percts[i] = (double) cnts[i] / (double) totalCnt;
            	}
            	socsStatesPercent.put(socsID, percts);
            }
		}	
	}
	
	public double[] getOneDeviceStatesDist(String socsID) {
		return socsStatesPercent.get(socsID);
	}
	
	public StatCalc[] getAllDevicesStatesStat() {
		StatCalc[] statistics = new StatCalc[state_type];
		for (int i = 0; i < state_type; i++) 
			statistics[i]  = new StatCalc();
		
		Iterator<String> iter = socsStatesPercent.keySet().iterator();
		while (iter.hasNext()) {
			String socsID = iter.next();
			for (int i = 0; i < state_type; i++) {
				statistics[i].enter(socsStatesPercent.get(socsID)[i]);
			}
		}
		return statistics;
	}
	
	public void printStatForOne(String socsID) {
		if (socsStatesPercent.containsKey(socsID)) {
			double[] cnts = socsStatesPercent.get(socsID);
			for (int i = 0; i < cnts.length; i++)
				System.out.println(i + " : " + cnts[i]);
		} else {
			System.out.println("Current socs invalid.");
		}
	}
}
