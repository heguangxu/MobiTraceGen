package analytics;

import java.io.*;
import java.util.*;

import analytics.utility.*;

public class DataParser {
    private final int MINUTES = 24*60;    // total number of the minutes in a day 
	private final int FREQUENCY = 3;      // the specified sampling frequency (in Min)
	
    private String bt_dir;        // Bluetooth data directory
    private String wf_dir;        // WiFi data directory
    private int day_num;          // # of days to process
	private int time_instances;   // # of sampling bins 
    
	private HashMap<String, ArrayList<String>[]> node_ap_proximity;   // node socsID : visible AP list[time_index]
	private HashMap<String, ArrayList<String>[]> node_node_proximity; // node socsID : visible node list[time_index]
	
    public DataParser(String btDir, String wfDir, int dayNum) {
    	bt_dir = btDir;
    	wf_dir = wfDir;
    	day_num = dayNum;
    	
    	time_instances = dayNum * MINUTES / FREQUENCY;
    	
    	node_ap_proximity = new HashMap<String, ArrayList<String>[]>();
    	node_ap_proximity.clear();
    	
    	node_node_proximity = new HashMap<String, ArrayList<String>[]>();
    	node_node_proximity.clear();
    }
    
	public void parseDataMultiDays() {
		File btPath = new File(bt_dir);
		File wfPath = new File(wf_dir);
    	File[] btFiles = btPath.listFiles();
    	File[] wfFiles = wfPath.listFiles();
    	System.out.println("# of files: " + btFiles.length);
    	for (int i = 0; i < day_num; i++) {
    		String btFile = btPath.getPath() + "/" + btFiles[i].getName();
    		String wfFile = wfPath.getPath() + "/" + wfFiles[i].getName();
    		
    		if(!btFiles[i].getName().equals(wfFiles[i].getName())) {
    			System.out.println("BT and WiFi sample dates don't match.");
    			System.exit(0);
    		}
    		processBTProximityOneDay(btFile, i);
    		processWiFiProximityOneDay(wfFile, i);
    	}
	}
	
	private void processBTProximityOneDay(String fileName, int dayIndex) {
		try {
	        BufferedReader file = new BufferedReader(new FileReader(fileName));
	        String dataRow = file.readLine();  // read first line
	        // the while checks to see if the data is null, if
	        // it is, we've hit the end of the file; if not, then
	        // process the data
	        
	        while (dataRow != null) {
                String[] dataArray = dataRow.split(",");
                
                if(dataArray.length != 5) {
                    System.out.println("The BT file: " + fileName + " is corrupted, exit.");
                    System.exit(0);
                }
                
                String socsID1 = dataArray[0];                  // the 1st socs
                String socsID2 = dataArray[2];                  // the socs seen by the 1st socs
                long timeStamp = Long.parseLong(dataArray[1]);  // time stamp
                short sigStr = Short.parseShort(dataArray[4]);  // rssi value
                
                if(socsID1.contains("socs") && socsID2.contains("socs") && sigStr >= -80) {
                	ConvertEpochTime epTime = new ConvertEpochTime();
                	int binIndex = epTime.getTimeIndex(timeStamp, FREQUENCY) + dayIndex * (MINUTES/FREQUENCY);
                	
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
	
	private void processWiFiProximityOneDay(String fileName, int dayIndex) {
		try {
            BufferedReader file = new BufferedReader(new FileReader(fileName));
            String dataRow = file.readLine(); // Read first line.
            // The while checks to see if the data is null. If   
            // it is, we've hit the end of the file. If not,   
            // process the data.  
            
            while(dataRow != null) {   
                String[] dataArray = dataRow.split(",");
                
                if(dataArray.length != 5) {
                    System.out.println("The WiFi file: " + fileName + " is corrupted, exit.");
                    System.exit(0);
                }
                
                String socsID = dataArray[0];                   // the socs id number
                long timeStamp = Long.parseLong(dataArray[1]);  // time stamp
                String network = dataArray[2];                  // network: ND-Secure, nomad, etc
                String mac = dataArray[3];                      // the mac address of AP       
                short sigStr = Short.parseShort(dataArray[4]);  // rssi value
                         
                if(socsID.contains("socs") && network.equals("ND-secure") && sigStr >= -80) {            	
                	ConvertEpochTime epTime = new ConvertEpochTime();
                	int binIndex = epTime.getTimeIndex(timeStamp, FREQUENCY) + dayIndex * (MINUTES/FREQUENCY);

                    // fill in the node_ap proximity map
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
	
	public HashMap<String, ArrayList<String>[]> getWFProximity() {
		return node_ap_proximity;
	}
	
	public HashMap<String, ArrayList<String>[]> getBTProximity() {
		return node_node_proximity;
	}
	
	public int getTimeInstanceCnt() {
		return time_instances;
	}
}
