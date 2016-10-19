package analytics.events;

import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

import analytics.structures.*;
import analytics.utility.ConvertEpochTime;

public class APCommunityTransOneDay {
	// the three types of states each sample might be in
    public static final String INDEGREE = "InDegree";
    public static final String TO_NO_WIFI = "ToNoWiFi";
    public static final String TO_DIFF_WIFI = "ToDiffWiFi";
	
	private final int MINUTES = 24*60;  // total number of the minutes in a day 
	
	private int bin_size = 5;           // sampling duration in minutes
	private int threshold = 0;          // how many nodes associated with the specific AP
	private int bin_cnt = 0;            // number of sampling bins 
	
	private HashMap<String, ArrayList<String>[]> ap_proximityMap;  // node socsID : visible AP list[time_index]
	private HashMap<String, String[]> ap_communitiesMap;           // transfer the  ap list into a string
	private HashMap<String, APCommunityList<APCommunity>> communityListMap; 
	private HashMap<String, HashMap<String, int[]>> communityTransCnt; // socs ID : <community_id : trans_probabilities>
    // 3 possible types of state transitions: 
    private HashMap<String, Integer> trans_index_mapping;
	
	public APCommunityTransOneDay(int _bin_size, int _threshold) {
    	bin_size = _bin_size;
    	threshold = _threshold;
    	bin_cnt = MINUTES / bin_size;
    	
    	ap_proximityMap = new HashMap<String, ArrayList<String>[]>();
    	ap_proximityMap.clear();
    	
    	ap_communitiesMap = new HashMap<String, String[]>();
    	ap_communitiesMap.clear();
    	
    	communityListMap = new HashMap<String, APCommunityList<APCommunity>>();
    	communityListMap.clear();
    	
    	communityTransCnt = new HashMap<String, HashMap<String, int[]>>();
    	communityTransCnt.clear();
    	
        // 3 possible types of states transitions
    	trans_index_mapping = new HashMap<String, Integer>();
    	trans_index_mapping.clear();
    	trans_index_mapping.put(INDEGREE, 0);
    	trans_index_mapping.put(TO_NO_WIFI, 1);
    	trans_index_mapping.put(TO_DIFF_WIFI, 2);
	}
	
	public void createAPProximity(String fileName) {
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
					if(!ap_proximityMap.containsKey(socsID)) {
                    	ArrayList<String>[] visibleAPs =  new ArrayList[bin_cnt];
                    	for(int i = 0; i < bin_cnt; i++) {
                    		visibleAPs[i] = new ArrayList<String>();
                    		visibleAPs[i].clear();
                    	}
                    	ap_proximityMap.put(socsID, visibleAPs);
                    }
                    if(!ap_proximityMap.get(socsID)[binIndex].contains(apMac) && sigStr >= -80) {
                    	ap_proximityMap.get(socsID)[binIndex].add(apMac);
                    } 
				}
				dataRow = file.readLine(); // Read next line of data.
			}
			// Close the file once all data has been read.
			file.close();
		} catch (Exception e) {
			// Inform user that file was not found.
			e.printStackTrace();
		} 
	}
	
	public void createCommunitiesByTime() {
		Iterator<String> iter = ap_proximityMap.keySet().iterator();
		while (iter.hasNext()) {
    		String socsID = iter.next();
    		ArrayList<String>[] apsByBin = ap_proximityMap.get(socsID);
    		String[] comms = new String[bin_cnt];
    		for (int i = 0; i < bin_cnt; i++) {
    			ArrayList<String> aps = apsByBin[i];
    			String sortedList = serializeVisibileList(aps);
    			comms[i] = sortedList;
    			
    			if(!communityListMap.containsKey(socsID)) {
        			communityListMap.put(socsID, new APCommunityList<APCommunity>());
        		}
        		APCommunityList<APCommunity> communityList = communityListMap.get(socsID);
        		
    			if (communityList.getCommunityById(sortedList) == null) {
		    		communityList.add(new APCommunity(sortedList, 1));
		    	} else {
		    		communityList.getCommunityById(sortedList).increaseCount();
		    	}
    			
                
    		}
    		if(!ap_communitiesMap.containsKey(socsID)) {
				ap_communitiesMap.put(socsID, comms);
			}
		}
	}
	
	public void summarizeTransCnt() {
		Iterator<String> iter = ap_communitiesMap.keySet().iterator();
		while (iter.hasNext()) {
			String socsID = iter.next();
			String[] comms = ap_communitiesMap.get(socsID);
			
			if (!communityTransCnt.containsKey(socsID)) {
				HashMap<String, int[]> trans = new HashMap<String, int[]>();
				communityTransCnt.put(socsID, trans);
			}
			
			for (int i = 0; i < bin_cnt; i++) {
				String prev_comm, curr_comm, next_comm;
				if (i == 0) {
					prev_comm = "";
				} else {
					prev_comm = comms[i-1];
				}
				
				curr_comm = comms[i];
				
				if (i == bin_cnt-1) {
					next_comm = "";
				} else {
					next_comm = comms[i+1];
				}
				
				if (curr_comm != "") {
					if(!(communityTransCnt.get(socsID).containsKey(curr_comm))) {
						int[] tranCnts = new int[3];
						for (int m = 0; m < 3; m++)
							tranCnts[m] = 0;
						communityTransCnt.get(socsID).put(curr_comm, tranCnts);
					}
					int[] cnts = communityTransCnt.get(socsID).get(curr_comm);
					
					if (next_comm == "") {
						cnts[trans_index_mapping.get(TO_NO_WIFI)]++;
					} else {
						if (!curr_comm.equals(next_comm))
						    cnts[trans_index_mapping.get(TO_DIFF_WIFI)]++;
						else 
							cnts[trans_index_mapping.get(INDEGREE)]++;
					}
					communityTransCnt.get(socsID).put(curr_comm, cnts);
				}
			}
		}
	}
	
	public double[] getOneDeviceCommTrans(String socsID) {
		double[] results = new double[4];
		for (int i = 0; i < 4; i++)
			results[i] = 0.0;
		
		if (!communityTransCnt.containsKey(socsID) || !communityListMap.containsKey(socsID)) {
			System.out.println("SOCS ID not contained in current sample file");
		} else {
			APCommunityList<APCommunity> communities = communityListMap.get(socsID);
			Comparator<APCommunity> comparator;
        	comparator = Collections.reverseOrder(new APCommunitySortedByCnt());
        	Collections.sort(communities, comparator);
        	
        	int index = 0;
        	APCommunity community = communities.get(index);
        	String communityName = community.getID();
        	while (communityName == "" && (index + 1) < communities.size()) {
        		community = communities.get(++index);
        		communityName = community.getID();
        	}
        	//System.out.println("most comm: " + community.getID());
        	int[] cnts = communityTransCnt.get(socsID).get(communityName);
        	if (cnts != null) {
				for (int i = 0; i < 3; i++) {
					results[i] = (double) cnts[i]
							/ (double) community.getCount();
				}
				results[3] = (double) community.getCount() / (double) bin_cnt;
        	}
		}
		return results;
	}
	
    private String serializeVisibileList(ArrayList<String> array) { 
    	String result = "";
    	Collections.sort(array);
    	for(int i = 0; i < array.size(); i++) {
    		result += array.get(i) + " ";
    	}
    	return result;
    }
}
