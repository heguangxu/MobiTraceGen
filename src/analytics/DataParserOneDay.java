package analytics;

import java.io.*;
import java.util.*;

import analytics.mobitrace.*;
import analytics.utility.ConvertEpochTime;
import analytics.utility.StatCalc;

/**
* Class used to parse the BT and WiFi Sample data
* for one day, in order to set up the abstract 
* neighborhood relations node-to-node and node-to-ap
* @author xhu2, Nov 2012
*/
public class DataParserOneDay {
	private final int MINUTES = 24*60;  // total number of the minutes in a day 
	private int sample_freq = 0;        // the specified sampling frequency
	private int time_instances = 0;            // number of sampling bins 
	
	private String bt_file;
	private String wf_file; 
	
	private HashMap<String, VisibleEntitiesList[]> node_ap_proximityRSSI;    // node socsID : visible AP list[time_index]
	private HashMap<String, ArrayList<String>[]> node_ap_proximity;      
	private HashMap<String, ArrayList<String>[]> node_node_proximity;    // node socsID : visible node list[time_index]
    private HashMap<String, Short> ap_max_rssi;                          // maximum scanned RSSI associated with each AP
	
	public DataParserOneDay(int _freq, String btFile, String wfFile) {
		sample_freq = _freq;
		time_instances = MINUTES / sample_freq;
    	
    	bt_file = btFile;
    	wf_file = wfFile;
    	
    	node_ap_proximityRSSI = new HashMap<String, VisibleEntitiesList[]>();
    	node_ap_proximityRSSI.clear();
    	
    	node_ap_proximity = new HashMap<String, ArrayList<String>[]>();
    	node_ap_proximity.clear();
    	
    	node_node_proximity = new HashMap<String, ArrayList<String>[]>();
    	node_node_proximity.clear();
    	
    	ap_max_rssi = new HashMap<String, Short>();
    	ap_max_rssi.clear();
	}

	public void createProximityOneDay() {
		createBTProximityOneDay();
		createWFProximityOneDay();
	}
	
	private void createBTProximityOneDay() {
		try {
	        BufferedReader file = new BufferedReader(new FileReader(bt_file));
	        String dataRow = file.readLine();  // read first line
	        // the while checks to see if the data is null, if
	        // it is, we've hit the end of the file; if not, then
	        // process the data
	        
	        while (dataRow != null) {
                String[] dataArray = dataRow.split(",");
                
                if(dataArray.length != 5) {
                    System.out.println("The BT file: " + bt_file + " is corrupted, exit.");
                    System.exit(0);
                }
                
                String socsID1 = dataArray[0];                  // the 1st socs
                String socsID2 = dataArray[2];                  // the socs seen by the 1st socs
                long timeStamp = Long.parseLong(dataArray[1]);  // time stamp
                short sigStr = Short.parseShort(dataArray[4]);  // rssi value
                
                if(socsID1.contains("socs") && socsID2.contains("socs") && sigStr >= -80) {
                	ConvertEpochTime epTime = new ConvertEpochTime();
                	int binIndex = epTime.getTimeIndex(timeStamp, sample_freq);
                	
                    // fill in the node_to_node proximity map
                    if (!node_node_proximity.containsKey(socsID1)) {
                    	ArrayList<String>[] visibleNodes1 =  new ArrayList[time_instances];
                    	for(int i = 0; i < time_instances; i++) {
                    		visibleNodes1[i] = new ArrayList<String>();
                    		visibleNodes1[i].clear();
                    	}
                    	node_node_proximity.put(socsID1, visibleNodes1);
                    }
                    if (!node_node_proximity.get(socsID1)[binIndex].contains(socsID2)) {
                    	node_node_proximity.get(socsID1)[binIndex].add(socsID2);
                    } 
                    
                    if (!node_node_proximity.containsKey(socsID2)) {
                    	ArrayList<String>[] visibleNodes2 = new ArrayList[time_instances];
                    	for(int i = 0; i < time_instances; i++) {
                    		visibleNodes2[i] = new ArrayList<String>();
                    		visibleNodes2[i].clear();
                    	}
                    	node_node_proximity.put(socsID2, visibleNodes2);
                    }
                    if (!node_node_proximity.get(socsID2)[binIndex].contains(socsID1)) {
                    	node_node_proximity.get(socsID2)[binIndex].add(socsID1);
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
	
	private void createWFProximityOneDay() {
		try {
            BufferedReader file = new BufferedReader(new FileReader(wf_file));
            String dataRow = file.readLine(); // Read first line.
            // The while checks to see if the data is null. If   
            // it is, we've hit the end of the file. If not,   
            // process the data.  
            
            while(dataRow != null) {   
                String[] dataArray = dataRow.split(",");
                
                if(dataArray.length != 5) {
                    System.out.println("The WiFi file: " + wf_file + " is corrupted, exit.");
                    System.exit(0);
                }
                
                String socsID = dataArray[0];                   // the socs id number
                long timeStamp = Long.parseLong(dataArray[1]);  // time stamp
                String network = dataArray[2];                  // network: ND-Secure, nomad, etc
                String mac = dataArray[3];                      // the mac address of AP       
                short rssi = Short.parseShort(dataArray[4]);  // rssi value
                         
				if (socsID.contains("socs") && network.equals("ND-secure")) {
					if (!ap_max_rssi.containsKey(mac)) {
						ap_max_rssi.put(mac, rssi);
					} else {
						if (rssi > ap_max_rssi.get(mac))
							ap_max_rssi.put(mac, rssi);
					}
				}
                
                if(socsID.contains("socs") && network.equals("ND-secure")) {            	
                	ConvertEpochTime epTime = new ConvertEpochTime();
                	int binIndex = epTime.getTimeIndex(timeStamp, sample_freq);
                	
                	// fill in the node_ap proximity map without RSSI
                    if(!node_ap_proximity.containsKey(socsID)) {
                    	ArrayList<String>[] visibleAPs =  new ArrayList[time_instances];
                    	for(int i = 0; i < time_instances; i++) {
                    		visibleAPs[i] = new ArrayList<String>();
                    		visibleAPs[i].clear();
                    	}
                    	node_ap_proximity.put(socsID, visibleAPs);
                    }
                    if(!node_ap_proximity.get(socsID)[binIndex].contains(mac)) {
                    	node_ap_proximity.get(socsID)[binIndex].add(mac);
                    } 
                	
                    // fill in the node_ap proximity map with RSSI
                    if (!node_ap_proximityRSSI.containsKey(socsID)) {
                    	VisibleEntitiesList[] visibleAPs =  new VisibleEntitiesList[time_instances];
                    	for(int i = 0; i < time_instances; i++) {
                    		visibleAPs[i] = new VisibleEntitiesList();
                    		visibleAPs[i].clear();
                    	}
                    	node_ap_proximityRSSI.put(socsID, visibleAPs);
                    }
                    
                    if (node_ap_proximityRSSI.get(socsID)[binIndex].getVisibleEntityById(mac) == null) {
                    	node_ap_proximityRSSI.get(socsID)[binIndex].add(new VisibleEntity(mac, rssi));
                    } else {
                    	short oldRSSI = node_ap_proximityRSSI.get(socsID)[binIndex].getVisibleEntityById(mac).getRSSI();
                    	if (oldRSSI < rssi) {
                    		node_ap_proximityRSSI.get(socsID)[binIndex].getVisibleEntityById(mac).setRSSI(rssi);
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
	
	public HashMap<String, VisibleEntitiesList[]> getWFRSSIProximityOneDay() {
		return node_ap_proximityRSSI;
	}
	
	public HashMap<String, ArrayList<String>[]> getWFProximityOneDay() {
		return node_ap_proximity;
	}
	
	public HashMap<String, ArrayList<String>[]> getBTProximityOneDay() {
		return node_node_proximity;
	}
	
	public int getTimeInstanceCnt() {
		return time_instances;
	}
	
	public void printAPMaxRSSI() {
		StatCalc stat  =new StatCalc();
		for (String key : ap_max_rssi.keySet()) {
			short rssi = ap_max_rssi.get(key);
			System.out.println(key + ":" + rssi);
			stat.enter(rssi);
		}
		System.out.println("Stat for rssi value ** " +
		    "Max:" + (short)stat.getMax() + "|" + "Min:" + (short)stat.getMin() + "|" + "Avg:" + (short)stat.getMean() + "|" + "Dev:" + (short)stat.getStandardDeviation());
	}
}
