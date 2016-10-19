package analytics.events;

import java.io.*;
import java.text.*;
import java.util.*;

import analytics.utility.ConvertEpochTime;
import analytics.utility.StatCalc;


public class EventsStatOneDay {
	// the four types of states each sample might be in
    public static final String OFF = "OFF";
    public static final String GREEN = "GREEN";
    public static final String WIFI = "WIFI";
    
    // 12 types of event / status transition between two adjacent samples
    private HashMap<String, Integer> event_index_mapping;
    
    // private attributes 
	private final int MINUTES = 24*60;  // total number of the minutes in a day 
	//private final int NODE_CNT = 201;	// maximum number of mobile nodes + 1
	
	private int min_bin_size = 0;       // the min duration should be 5 minutes
	private int bin_size = 0;           // sampling duration in minutes
	private int max_bin_cnt = 0;        // the max number of bins
	private int bin_cnt = 0;            // number of sampling bins 
	private int event_type = 0;
	
	private HashMap<String, boolean[]> deviceOn;    // node socsID : deviceOn[timeIndex], true indicates on at given timeIndex
	private HashMap<String, int[]> socsEventsCnt;   // nodeID : associate event 
	private HashMap<String, double[]> socsEventsPercent; //
	private HashMap<String, ArrayList<String>[]> node_ap_proximityMap; 
	private HashMap<String, String[]> node_ap_communityMap;
	
	public EventsStatOneDay(int _duration) {
    	min_bin_size = 5; 
    	bin_size = _duration;
    	max_bin_cnt = MINUTES / min_bin_size;
    	bin_cnt = MINUTES / bin_size;
    	event_type = 10;
    	
    	event_index_mapping = new HashMap<String, Integer>();
    	event_index_mapping.clear();
    	event_index_mapping.put("OFF_OFF", 0);
    	event_index_mapping.put("OFF_GREEN", 1);
    	event_index_mapping.put("OFF_WIFI", 2);
    	event_index_mapping.put("GREEN_OFF", 3);
    	event_index_mapping.put("GREEN_GREEN", 4);
    	event_index_mapping.put("GREEN_WIFI", 5);
    	event_index_mapping.put("WIFI_OFF", 6);
    	event_index_mapping.put("WIFI_GREEN", 7);
    	event_index_mapping.put("WIFI_WIFI_SAME", 8);
    	event_index_mapping.put("WIFI_WIFI_DIFF", 9);
    	
    	deviceOn = new HashMap<String, boolean[]>(); 
    	deviceOn.clear();
    	
    	socsEventsCnt = new HashMap<String, int[]>();
    	socsEventsCnt.clear();   	
    	
    	socsEventsPercent = new HashMap<String, double[]>();
    	socsEventsPercent.clear();
    	
    	node_ap_proximityMap = new HashMap<String, ArrayList<String>[]>();
    	node_ap_proximityMap.clear();
    	
    	node_ap_communityMap = new HashMap<String, String[]>();
    	node_ap_communityMap.clear();
	}
	
	public void createDeviceOnMap(String fileName) {  // parse the BT data file
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
                long timeStamp = Long.parseLong(dataArray[1]);  // sample time stamp
                //short sigStr = Short.parseShort(dataArray[4]);  // rssi value
                
                if(socsID.contains("socs")) {
                	ConvertEpochTime cet = new ConvertEpochTime();
                    int binIndex = cet.getTimeIndex(timeStamp, min_bin_size);
                    //System.out.println("socs index: " + j + " " + k);
                    
                    // fill in the device_on map
                    if(!deviceOn.containsKey(socsID)) {
                    	boolean[] isOn = new boolean[max_bin_cnt];
                    	for(int i = 0; i < max_bin_cnt; i++) {
                    		isOn[i] = false;
                    	}
                    	deviceOn.put(socsID, isOn);
                    }
                    	
                    deviceOn.get(socsID)[binIndex] = true;
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
	

	public void createWiFiProximityMap(String fileName) {
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
							.println("Error: The data file is corrupted, exit!");
					System.exit(0);
				}

				String socsID = dataArray[0];   // the socs id number
				long timeStamp = Long.parseLong(dataArray[1]); // time stamp
				String network = dataArray[2];     // wifi type: ND-Secure, nomad, etc
				String apMac = dataArray[3];      // the mac address of AP
				// short sigStr = Short.parseShort(dataArray[4]); // rssi value

				if (socsID.contains("socs") && !network.contains("dummy") && !apMac.contains("dummy")) {
                	ConvertEpochTime cet = new ConvertEpochTime();
                    int binIndex = cet.getTimeIndex(timeStamp, min_bin_size);

					// fill in the node_ap proximity map
					if (!node_ap_proximityMap.containsKey(socsID)) {
						ArrayList<String>[] visibleAPs = new ArrayList[max_bin_cnt];
						for (int i = 0; i < max_bin_cnt; i++) {
							visibleAPs[i] = new ArrayList<String>();
							visibleAPs[i].clear();
						}
						node_ap_proximityMap.put(socsID, visibleAPs);
					}
					if (!node_ap_proximityMap.get(socsID)[binIndex]
							.contains(apMac)) {
						node_ap_proximityMap.get(socsID)[binIndex].add(apMac);
					}
				}
				dataRow = file.readLine(); // Read next line of data.
			}
			// Close the file once all data has been read.
			file.close();
		} catch (FileNotFoundException e) {
			// Inform user that file was not found.
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Error: " + e);
		}
		
		createAPCommunityMap();
	}
	
	private void createAPCommunityMap() {
		Iterator<String> iter = node_ap_proximityMap.keySet().iterator();
		while (iter.hasNext()) {
			String socsID = iter.next();

			ArrayList<String>[] apsByBin = node_ap_proximityMap.get(socsID);
			String[] comByBin = new String[max_bin_cnt];

			for (int i = 0; i < max_bin_cnt; i++) {
				ArrayList<String> aps = apsByBin[i];
				comByBin[i] = serializeVisibileList(aps);
			}
			if (!node_ap_communityMap.containsKey(socsID)) {
				node_ap_communityMap.put(socsID, comByBin);
			}
		}
	}
	
	public void countEvents() {
		Iterator<String> iter = deviceOn.keySet().iterator();
    	while (iter.hasNext()) {
    		String socsID = iter.next();
    		if(!socsEventsCnt.containsKey(socsID)) {
    			int[] eventsCnt = new int[event_type];
    			for(int i = 0; i < event_type; i++)
    				eventsCnt[i] = 0;
    			socsEventsCnt.put(socsID, eventsCnt);
    		}
    		
    		String pre_state = OFF;
    		String cur_state = "";
    		
    		String pre_community = "";
    		String cur_community = "";
    		
    		for(int i = 0; i < max_bin_cnt; i++) {
    			String event = "";
    			
    			if(deviceOn.get(socsID)[i] == false) { // the device is currently off
    				cur_state = OFF;
    			} else {                               // the device is on, so it's either in green zone or in wifi range
    				if(node_ap_communityMap.get(socsID) == null || node_ap_communityMap.get(socsID)[i] == "") {
    				    cur_state = GREEN;             // in greenzone since cannot detect any wifi
    				} else {
    					cur_state = WIFI;              // in wifi, and get the ap community at this time point
    					cur_community = node_ap_communityMap.get(socsID)[i];
    				}
    			}
    			
                // categorize the current event into a proper event type
    			if(pre_state == WIFI && cur_state == WIFI) {
    				if(pre_community.equals(cur_community)) {
    					event = "WIFI_WIFI_SAME";
    				} else {
    					event = "WIFI_WIFI_DIFF";
    				}
    				pre_community = cur_community;
    			} else {
    				event = pre_state + "_" + cur_state;
    			}
    			
    			socsEventsCnt.get(socsID)[event_index_mapping.get(event)]++;
    			pre_state = cur_state;
    		}
    	}
    	normalizeEventCnts();
	}
	
	private void normalizeEventCnts() {
		Iterator<String> iter = socsEventsCnt.keySet().iterator();
		while (iter.hasNext()) {
			String socsID = iter.next();
            if (!socsEventsPercent.containsKey(socsID)) {			
            	int[] cnts = socsEventsCnt.get(socsID);
            	double[] percts = new double[cnts.length];
            	for (int i = 0; i < cnts.length; i++) {
            		percts[i] = (double) cnts[i] / (double) max_bin_cnt;
            	}
            	socsEventsPercent.put(socsID, percts);
            }
		}
	}
	
	public double[] getOneDeviceEventsDist(String socsID) {
		if (socsEventsPercent.get(socsID) != null) {
			return socsEventsPercent.get(socsID);
		} else {
			return null; 
		}
	}
	
	public StatCalc[] getAllDevicesEventStat() {
		StatCalc[] statistics = new StatCalc[event_type];
		for (int m = 0; m < event_type; m++) 
			statistics[m]  = new StatCalc();
		Iterator<String> iter = socsEventsPercent.keySet().iterator();
		while (iter.hasNext()) {
			String socsID = iter.next();
			for (int i = 0; i < event_type; i++) {
				statistics[i].enter(socsEventsPercent.get(socsID)[i]);
			}
		}
		return statistics;
	}

	public void printStatForOne(String socsID) {
		if (socsEventsPercent.containsKey(socsID)) {
			double[] cnts = socsEventsPercent.get(socsID);
			for (int i = 0; i < cnts.length; i++)
				System.out.println(i + " : " + cnts[i]);
		} else {
			System.out.println("current socs invalid.");
		}
	}

	private String serializeVisibileList(ArrayList<String> array) {
		String result = "";
		Collections.sort(array);
		for (int i = 0; i < array.size(); i++) {
			result += array.get(i) + " ";
		}
		return result;
	}
}
