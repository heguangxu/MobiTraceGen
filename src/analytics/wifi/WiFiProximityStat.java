package analytics.wifi;

import java.io.*;
import java.util.*;
import java.text.*;

import analytics.utility.ConvertEpochTime;
import analytics.utility.StatCalc;

/**
* Class used to capture the statistical 
* information of the wifi proximity
* 
* @author xhu2, June 2012
*/
public class WiFiProximityStat {
	private final int MINUTES = 24*60;  // total number of the minutes in a day 
	private final int NODE_CNT = 201;	// maximum number of mobile nodes + 1
	
	private int min_bin_size = 0;       // the min duration should be 5 minutes
	private int bin_size = 0;           // sampling duration in minutes
	private int threshold = 0;          // how many nodes associated with the specific AP
	private int max_bin_cnt = 0;        // the max number of bins
	private int bin_cnt = 0;            // number of sampling bins 
	
	private HashMap<String, boolean[][]> ap_node_proximityMap;  // map, AP Mac : proximity matrix[time_index][socs_index]
    
	private HashMap<String, ArrayList<String>[]> node_ap_proximityMap;   // node socsID : visible AP list[time_index], the map for the min bin size
	
	private HashMap<String, ArrayList<String>[]> node_ap_proximityMap2;  // node socsID : visible AP list[time_index], the map for specified bin size
	
	private HashMap<String, VisibleAPsList<VisibleAP>> node_ap_overallMap;  // node socsID : visible APs across a whole day 
	
	private HashMap<String, int[]> node_ap_cntMap; // for a socs node, the # of different APs it detected within each bin
	
    private HashMap<String, Double> ap_node_prmtySizePercentMap; 
    
    private HashMap<String, ArrayList<Double>> node_ap_prmtySizePercentMap;     // percent of time a socs node detected >= <threshold> APs across all bins
    
    private HashMap<String, WiFiCommunityList<WiFiCommunity>> communityListMap; 
    
    public WiFiProximityStat(int _duration, int _threshold) {
    	min_bin_size = 5; 
    	bin_size = _duration;
    	threshold = _threshold;
    	max_bin_cnt = MINUTES / min_bin_size;
    	bin_cnt = MINUTES / bin_size;
    
    	ap_node_proximityMap = new HashMap<String, boolean[][]>();
    	ap_node_proximityMap.clear();
    	
    	node_ap_proximityMap = new HashMap<String, ArrayList<String>[]>();
    	node_ap_proximityMap.clear();
    	
    	node_ap_proximityMap2 = new HashMap<String, ArrayList<String>[]>();
    	node_ap_proximityMap2.clear();
    		
    	node_ap_overallMap = new HashMap<String, VisibleAPsList<VisibleAP>>();
    	node_ap_overallMap.clear();
    	
    	node_ap_cntMap = new HashMap<String, int[]> ();
    	node_ap_cntMap.clear();
    	
    	ap_node_prmtySizePercentMap = new HashMap<String, Double>();
    	ap_node_prmtySizePercentMap.clear();
    	
    	node_ap_prmtySizePercentMap = new HashMap<String, ArrayList<Double>>();
    	node_ap_prmtySizePercentMap.clear();
    	
    	communityListMap = new HashMap<String, WiFiCommunityList<WiFiCommunity>>();
    	communityListMap.clear();
    }
    
    
    // Do a set of analysis
    public void perform() {
    	//getWeekAPCnt();
    	//getWeekGreenZoneCnt("socs127");
    	//createProximityMap("./socsData/socs_0411_WiFi.csv");
    	createProximityMap("./socsData/WiFi_2012_05/07.csv");
    	//getAvgCommunitySizePerHr("socs018");
    	createCommunityMap(true);
    	printCommunityCnt("socs018");
    	//printCommunityCnt("socs052");
    	//printCommunityCnt("socs061");
    	//printCommunityCnt("socs172");
    	printCommunityCnt("socs111");
    	
    	//createOverallNodeAPMap();
    	//createGranularityNodeAPMap("socs018", 5);
    	//createGranularityAPCntMap();
    	//printGranularityAPCntMap("socs018");
    	//createNodeAPProximitySizePercentMap();
    	//printNodeAPProximityPercentStat();
    	//createProximitySizePercentMap();
    	//printProximityPercentStat();
    	//System.out.println("Sample Size: " + proximityMap.size());
    }
    
    private void getWeekAPCnt() {
    	HashMap<String, Double> weekData = new HashMap<String, Double>();
    	weekData.clear();
    	
    	File inputDir = new File("./socsData/WiFi_2012_05");
    	File[] inputFiles = inputDir.listFiles();
    	
    	for (File inFile : inputFiles) {
    		String fileName = "./socsData/WiFi_2012_05/" + inFile.getName();
    		createProximityMap(fileName);
    		createNodeAPProximitySizePercentMap();
    		
    		String date = inFile.getName().substring(0, 2);
    	    String key = date;
    		if(!weekData.containsKey(key)) {
    			weekData.put(key, getNodeAPProximityPercentMean(1));
    		}
    		clearAllMaps();
    	}
    	printWeekData(weekData);
    }
    
    private void getWeekGreenZoneCnt(String socs) {
    	HashMap<String, Double> weekData = new HashMap<String, Double>();
    	weekData.clear();
    	
    	File inputDir = new File("./socsData/WiFi_1stweek");
    	File[] inputFiles = inputDir.listFiles();
    	
    	for (File inFile : inputFiles) {
    		String fileName = "./socsData/WiFi_1stweek/" + inFile.getName();
    		createProximityMap(fileName);
    		createCommunityMap(true);
    		
    		String date = inFile.getName().substring(0, 2);
    	    String key = date;
    		if(!weekData.containsKey(key)) {
    			weekData.put(key, getGreenZonCntDayTime(socs));
    		}
    		clearAllMaps();
    	}
    	printWeekData(weekData);
    	
    }
    
    private double getGreenZonCntDayTime(String socs) {
    	double percent = 0.0;
    	if(communityListMap.containsKey(socs)) {
    		WiFiCommunityList<WiFiCommunity> communityList = communityListMap.get(socs);
    		if(communityList.getCommunityById("") != null) {
    			return communityList.getCommunityById("").calcTimePercent();
    		} else {
    			return 0.0;
    		}
    	} else {
    		System.out.println("The specified socs has no ND networks connection in this day.");
    		return 0.0;
    	}
    }
    
    private void clearAllMaps() {
    	ap_node_proximityMap.clear();
    	node_ap_proximityMap.clear();   	
    	node_ap_proximityMap2.clear();  		
    	node_ap_overallMap.clear();  	
    	node_ap_cntMap.clear(); 	
    	ap_node_prmtySizePercentMap.clear();
    	node_ap_prmtySizePercentMap.clear();
    	communityListMap.clear();
    }
    
    public void printWeekData(HashMap<String, Double> weekData) {
    	try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("GreenZoneWeek.csv"));
			
			Iterator<String> it = weekData.keySet().iterator();
			while(it.hasNext()) {
				String date = it.next();
				double val = weekData.get(date);
				String line = date + "," + val + "\n";
				writer.write(line);
			}

        	writer.flush();
        	writer.close();
	
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void createProximityMap(String fileName) {
       	try {
            BufferedReader file = new BufferedReader(new FileReader(fileName));
            String dataRow = file.readLine(); // Read first line.
            // The while checks to see if the data is null. If   
            // it is, we've hit the end of the file. If not,   
            // process the data.  
            
            while(dataRow != null) {   
                String[] dataArray = dataRow.split(",");
                
                if(dataArray.length != 5) {
                    System.out.println("Error: The data file is corrupted, exit!");
                    System.exit(0);
                }
                
                String socsID = dataArray[0];        // the socs id number
                long timeStamp = Long.parseLong(dataArray[1]);  // time stamp
                String wifi = dataArray[2];          // wifi type: ND-Secure, nomad, etc
                String mac = dataArray[3];           // the mac address of AP       
                //short sigStr = Short.parseShort(dataArray[4]);  // rssi value
                         
                if((wifi.equals("ND-secure")||wifi.equals("ND-guest")||wifi.equals("nomad")) && socsID.contains("socs")) {
                	ConvertEpochTime epTime = new ConvertEpochTime();
                	int binIndex = epTime.getTimeIndex(timeStamp, min_bin_size);
                    int socsIndex = Integer.parseInt(socsID.substring(4));
                    
                    // fill in the ap_node proximity map
                    if(!ap_node_proximityMap.containsKey(mac)) {
                    	boolean visibleMatrix[][] = new boolean[max_bin_cnt][NODE_CNT];
                    	
                    	for(int m = 0; m < max_bin_cnt; m++) {
                    		for(int n = 0; n < NODE_CNT; n++)
                    			visibleMatrix[m][n] = false;
                    	}
                    	ap_node_proximityMap.put(mac, visibleMatrix);
                    }
                    (ap_node_proximityMap.get(mac))[binIndex][socsIndex] = true;
                    
                    // fill in the node_ap proximity map
                    if(!node_ap_proximityMap.containsKey(socsID)) {
                    	ArrayList<String>[] visibleAPs =  new ArrayList[max_bin_cnt];
                    	for(int i = 0; i < max_bin_cnt; i++) {
                    		visibleAPs[i] = new ArrayList<String>();
                    		visibleAPs[i].clear();
                    	}
                    	node_ap_proximityMap.put(socsID, visibleAPs);
                    }
                    if(!node_ap_proximityMap.get(socsID)[binIndex].contains(mac)) {
                    	node_ap_proximityMap.get(socsID)[binIndex].add(mac);
                    } 
                    
                    // fill in the node_visabileAPsMap
                    int largerBinIndex = epTime.getTimeIndex(timeStamp, bin_size);
                    if(!node_ap_proximityMap2.containsKey(socsID)) {
                    	ArrayList<String>[] visibleAPs =  new ArrayList[bin_cnt];
                    	for(int i = 0; i < bin_cnt; i++) {
                    		visibleAPs[i] = new ArrayList<String>();
                    		visibleAPs[i].clear();
                    	}
                    	node_ap_proximityMap2.put(socsID, visibleAPs);
                    }
                    if(!node_ap_proximityMap2.get(socsID)[largerBinIndex].contains(mac)) {
                    	node_ap_proximityMap2.get(socsID)[largerBinIndex].add(mac);
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
    
    public void createCommunityMap(boolean dayTimeOnly) {
    	Iterator<String> iter = node_ap_proximityMap.keySet().iterator();
    	while (iter.hasNext()) {
    		String socsID = iter.next();
    	
    		if(!communityListMap.containsKey(socsID)) {
    			communityListMap.put(socsID, new WiFiCommunityList<WiFiCommunity>());
    		}
    		WiFiCommunityList<WiFiCommunity> communityList = communityListMap.get(socsID);
    		
    		ArrayList<String>[] apsByBin = node_ap_proximityMap.get(socsID);
    		
    		int startIndex = 0;
    		int endIndex = max_bin_cnt;
    		int binCnt = 288;
    		if (dayTimeOnly == true) {
    		    startIndex = 8 * (60 / min_bin_size);
    		    endIndex = 20 * (60 / min_bin_size);
    		    binCnt = 144;
    		}
    		
    		for(int i = startIndex; i < endIndex; i++) {
    			ArrayList<String> aps = apsByBin[i];
    			String sortedList = serializeVisibileList(aps);
    			
    			if (communityList.getCommunityById(sortedList) == null) {
		    		communityList.add(new WiFiCommunity(sortedList, 1, binCnt));
		    	} else {
		    		communityList.getCommunityById(sortedList).increaseCount();
		    	}	
    		}
    	}
    }
    
    public void printCommunityCnt(String socsID) {
    	String key = socsID;
    	try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(key + "_WiFiCommunityCnt.csv"));
			
			if (communityListMap.containsKey(key)) {
	    	    WiFiCommunityList<WiFiCommunity> communities = communityListMap.get(key);
	        	
	        	Comparator<WiFiCommunity> comparator;
	        	comparator = Collections.reverseOrder(new WiFiCommunitySortedByCnt());
	        	Collections.sort(communities, comparator);
	        	
	        	for (int i = 0; i < communities.size(); i++) {
	        		WiFiCommunity community = communities.get(i);
	        		//System.out.println(community.getID() + "," + community.getCount() + "," + community.calcTimePercent());
	        		writer.write(community.getID() + "," + community.getCount() + "," + community.calcTimePercent() + "\n");
	        	}
	        	writer.flush();
	        	writer.close();
	        	
	    	} else {
	    		System.out.println(key + " has no visible ND APs during the given time.");
	    	}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private String serializeVisibileList(ArrayList<String> array) { 
    	String result = "";
    	Collections.sort(array);
    	for(int i = 0; i < array.size(); i++) {
    		result += array.get(i) + " ";
    	}
    	return result;
    }
    
    // get the average ND-Secure community size in each hour 
    public void getAvgCommunitySizePerHr(String socsID) {
    	ArrayList<String>[] communities = new ArrayList[max_bin_cnt];
		communities = node_ap_proximityMap.get(socsID);
		double[] sizeArray = new double[24];
		if (communities == null){
			for (int i = 0; i < 24; i++) 
				sizeArray[i] = 0.0;
		} else {
			for (int i = 0; i < 24; i++) {
				int sum = 0;
				for (int j = i*12; j < (i+1)*12 ; j++) {
					sum += communities[j].size();
				}
				sizeArray[i] = sum/12.0;
				System.out.println(sizeArray[i]);
			}
		}
    }
    
    // get the average community size by chunks of a day based on flag
    // i.e. 0am - 6 am, 6 am - 12 pm, 12 pm - 6 pm, 6 pm - 12 am 
    // flag must be either 1, 2, 3, or 4.
    public double getCommunitySizeByTimeClass(int flag, String socsID) {
    	int startIndex = 12*6*(flag-1);
    	int endIndex = startIndex + 12*6;
		int sumSize = 0;
		double average = 0.0;
    
    	ArrayList<String>[] communities = new ArrayList[max_bin_cnt];
		communities = node_ap_proximityMap.get(socsID);
		if (communities != null) {
			for (int i = startIndex; i < endIndex; i++) {
				sumSize += communities[i].size();
			}
			average = sumSize/(double)(12*6);	
		}
   
    	return average;
    }
    
    public void createOverallNodeAPMap() {
    	Iterator<String> iter = node_ap_proximityMap.keySet().iterator();
    	while (iter.hasNext()) {
    		String socsID = iter.next();
    		if(!node_ap_overallMap.containsKey(socsID)) {
    			node_ap_overallMap.put(socsID, new VisibleAPsList<VisibleAP>());
    		}
    		
    		ArrayList<String>[] apsByBin = node_ap_proximityMap.get(socsID);
    		for(int i = 0; i < max_bin_cnt; i++) {
    			ArrayList<String> aps = apsByBin[i];
    			for(int j = 0; j < aps.size(); j++) {
    				String apID = aps.get(j);
    				if(node_ap_overallMap.get(socsID).getVisibleAPById(apID) == null) {
    					node_ap_overallMap.get(socsID).add(new VisibleAP(apID, 1));
    				} else {
    					node_ap_overallMap.get(socsID).getVisibleAPById(apID).increaseCount();
    				}
    			}
    		}
    	}
    }
    
    public void createGranularityAPCntMap() {
    	Iterator<String> iter = node_ap_proximityMap2.keySet().iterator();
    	while (iter.hasNext()) {
    		String socsID = iter.next();
    		if(!node_ap_cntMap.containsKey(socsID)) {
    			node_ap_cntMap.put(socsID, new int[bin_cnt]);
    		}
    		
    		for(int i = 0; i < bin_cnt; i++) {
    			node_ap_cntMap.get(socsID)[i] = node_ap_proximityMap2.get(socsID)[i].size();
    		}
    	}
    }
    
    public void printGranularityAPCntMap(String _socsID) {
    	if(node_ap_cntMap.containsKey(_socsID)) {
            int[] cntArray = node_ap_cntMap.get(_socsID);
            try {
    			BufferedWriter writer = new BufferedWriter(new FileWriter(_socsID + "_APCntDistr.csv"));
    			
    			for(int i = 0; i < bin_cnt; i++) {
    				String line = Integer.toString(i) + "," + cntArray[i];
    				writer.write(line + "\n");
    			}
            	writer.flush();
            	writer.close();
    	
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    public void createGranularityNodeAPMap(String _socsID, int _topN) {
    	if(node_ap_overallMap.containsKey(_socsID) && node_ap_proximityMap.containsKey(_socsID)) {
    		VisibleAPsList<VisibleAP> aps = node_ap_overallMap.get(_socsID);
    		ArrayList<String>[] apsByBin = node_ap_proximityMap.get(_socsID);
    		
    		Comparator<VisibleAP> comparator;
    		comparator = Collections.reverseOrder(new VisibleAPSortedByCnt());
        	Collections.sort(aps, comparator);
        	
        	HashMap<String, int[]> apCntMap = new HashMap<String, int[]>();
        	apCntMap.clear();
      
        	for(int i = 0; i < _topN; i++) {
        		String apID = aps.get(i).getID();
        		apCntMap.put(apID, new int[bin_cnt]); 
  
        		int tmp = max_bin_cnt/bin_cnt;
        	  	for(int j = 0; j < max_bin_cnt; j++) {
        			if(apsByBin[j].contains(apID)) {
        				apCntMap.get(apID)[j/tmp]++;
        			}
        		}
        	}
        	
        	printGranularityNodeAPMap(_socsID, apCntMap);
    	}
    }
    
    public void printGranularityNodeAPMap(String _socsID, HashMap<String, int[]> apCntMap) {
    	try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(_socsID + "_nodeTopAPsMap.csv"));
			
			String firstLine = "time_slot" + ",";
			Iterator<String> it = apCntMap.keySet().iterator();
			while(it.hasNext()) {
				String apID = it.next();
				firstLine += apID + ",";
			}
			writer.write(firstLine + "\n");
			
			for(int i = 0; i < bin_cnt; i++) {
				String line = Integer.toString(i) + ",";
				Iterator<String> iter = apCntMap.keySet().iterator();
				while(iter.hasNext()) {
					String apID = iter.next();
					line += apCntMap.get(apID)[i] + ",";
				}
				writer.write(line + "\n");
			}
        	writer.flush();
        	writer.close();
	
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void createNodeAPProximitySizePercentMap() {
    	Iterator<String> iter = node_ap_proximityMap.keySet().iterator();
    	while (iter.hasNext()) {
			String socs = iter.next();
			ArrayList<String>[] detectedAPs = node_ap_proximityMap.get(socs);
			
			int[] threshCnt = new int[] { 0, 0, 0, 0, 0, 0 };
			for(int i = 0; i < max_bin_cnt; i++) {  // scan for each node bin by bin
				for(int j = 0; j < 6; j++) {
					if(j <= detectedAPs[i].size())
						threshCnt[j]++;
				}				
			}

			if(!node_ap_prmtySizePercentMap.containsKey(socs)) {
				node_ap_prmtySizePercentMap.put(socs, new ArrayList<Double>());
				ArrayList<Double> freq = node_ap_prmtySizePercentMap.get(socs);
				
				for(int i = 0; i < 6; i++) {
					freq.add(i, (double)threshCnt[i]/max_bin_cnt);
				}
			}
		}
    }
    
    public double getNodeAPProximityPercentMean(int thresh) {
    	StatCalc stat = new StatCalc();
    	Iterator<String> iter = node_ap_prmtySizePercentMap.keySet().iterator();
		while(iter.hasNext()) {
			String socsID = iter.next();
			ArrayList<Double> probList = node_ap_prmtySizePercentMap.get(socsID);
			double prob = probList.get(thresh);
			stat.enter(prob);
		}
		
		return stat.getMean();
    }
    
    public void printNodeAPProximityPercentStat() {
    	StatCalc[] statList = new StatCalc[6];
		for (int i = 0; i < 6; i++) {
			statList[i] = new StatCalc();
		}
		
		Iterator<String> iter = node_ap_prmtySizePercentMap.keySet().iterator();
		while(iter.hasNext()) {
			String socsID = iter.next();
			ArrayList<Double> probList = node_ap_prmtySizePercentMap.get(socsID);
			for (int j = 0; j < probList.size(); j++) {
				double prob = probList.get(j);
				statList[j].enter(prob);
			}
		}
		
		for (int k = 0; k < 6; k++) {
			double min = statList[k].getMin();
			double max = statList[k].getMax();
			double mean = statList[k].getMean();
			double std_dev = statList[k].getStandardDeviation();
			System.out.println(min + "\t" + max + "\t" + mean + "\t" + std_dev);
		}
    }
    
    public void createAPNodeProximitySizePercentMap() {
    	Iterator<String> iter = ap_node_proximityMap.keySet().iterator();
    	while (iter.hasNext()) {
			String mac = iter.next();
			boolean[][] matrix = ap_node_proximityMap.get(mac);
			int colCnt = 0;
			for(int i = 0; i < max_bin_cnt; i++) {
				int rowCnt = 0;
        		for(int j = 0; j < NODE_CNT; j++) {
        			if(matrix[i][j] == true) 
        			    rowCnt++;
        		}
        		if(rowCnt >= threshold)
        			colCnt++;
        	}
			double percent = (double)colCnt / max_bin_cnt;
			if(!ap_node_prmtySizePercentMap.containsKey(mac)) {
				ap_node_prmtySizePercentMap.put(mac, percent);
			}
		}
    }
    
    public void printAPNodeProximityPercentStat() {
    	StatCalc stat = new StatCalc();
    	Iterator<String> iter = ap_node_prmtySizePercentMap.keySet().iterator();
    	while (iter.hasNext()) {
    		String mac = iter.next();
    		stat.enter(ap_node_prmtySizePercentMap.get(mac));
    	}
    	System.out.println("Overall statitistics, % of time at least " + threshold + " nodes in the range of the same AP: ");
    	System.out.println(stat.getMin() + "\t" + stat.getMax() + "\t" + stat.getMean() + "\t" + stat.getStandardDeviation());
    }
}
