package analytics.bluetooth;

import java.io.*;
import java.util.*;
import java.text.*;

import analytics.utility.StatCalc;

/**
* Class used to capture the statistical 
* information of the bluetooth proximity
* 
* @author xhu2, June 2012
*/
public class BluetoothProximityStat {
	private final int SAMPLE_CNT = 24*60;  // total number of the samples 
	private final int NODE_CNT = 201;	   // maximum number of mobile nodes + 1, index from 1
	
	private ArrayList<String> valid_socs;  // valid socs numbers given by the mapping file
   
	private boolean[][][] prmty;           // 3D array to indicate the proximity between
      						               // each pair of nodes across the sampling duration 
                                           // where 1D: time; 2D: node1; 3D: node 2
    
    private short[][][] rssi;         // 3D array to store the rssi value
    
    private int[] hrNoiseVol;         // the count of asymmetric noisy samples during an hour  
    
    private boolean remove_noise;     // flag to indicate whether to remove the data noise or not
    
    private int noise_cnt;            // number of noisy data in terms of symmetry proximity
    
    private StatCalc rssiCalcSym;     // statistics of rssi difference between two node seeing each other
    private StatCalc rssiCalcAsym;    // statistics of rssi value for the asymmetric samples 
    
    private HashMap<Short, Integer> rssiAsym_map; // map rssi : cnt of asymmetric samples
    private HashMap<Short, Integer> rssiSym_map;  // map rssi : cnt of symmetric samples
    
    private ArrayList<Short> rssiAsym_list;
    private ArrayList<Short> rssiSym_list;
    
    private HashMap<String, VisibleNodesList<VisibleNode>> visibleListMap;     // map, node : overall visible nodes 
    private HashMap<String, VisibleNodesList<VisibleNode>> symVisibleListMap;  // map, node : visible nodes for symmetric cases
    private HashMap<String, VisibleNodesList<VisibleNode>> asymVisibleListMap; // map, node : visible nodes for asymmetric cases
    
    private HashMap<String, ArrayList<Double>> visibleSizePercentMap;     // map, node : % of time it sees no less than {0..n} nodes
    private HashMap<String, ArrayList<Double>> symVisibleSizePercentMap;  // map, node : % of time it sees no less than {0..n} nodes, for the symmetric cases
  
    private HashMap<String, Double> visibleSizePercentDayTime;  // map, node : % of time it sees no less than 1 nodes in BT proximity with >= -80 dBm RSSI value
    
    private HashMap<String, BTCommunityList<BTCommunity>> communityListMap; 
    
    private HashMap<String, ArrayList<String>[]> communityPerBinMap;
    
    public BluetoothProximityStat(boolean flag) {
    	remove_noise = flag;
    	noise_cnt = 0;
    	
    	rssiCalcSym = new StatCalc();
    	rssiCalcAsym = new StatCalc();
    	
        prmty = new boolean[SAMPLE_CNT][NODE_CNT][NODE_CNT]; // allocate memory 
        rssi = new short[SAMPLE_CNT][NODE_CNT][NODE_CNT]; 
        for(int i = 0; i < SAMPLE_CNT; i++) {                // initialization
        	for(int j = 0; j < NODE_CNT; j++) {
        		for(int k = 0; k < NODE_CNT; k++) {
        			prmty[i][j][k] = false;
        			rssi[i][j][k] = 0;
        		}
        	}
        }
        
        hrNoiseVol = new int[24];
        for(int i=0; i < 24; i++) {
        	hrNoiseVol[i] = 0;
        }
        
        rssiAsym_list = new ArrayList<Short> ();
        rssiSym_list = new ArrayList<Short> ();
        rssiAsym_list.clear();
        rssiSym_list.clear();
        
        rssiAsym_map = new HashMap<Short, Integer>();
        rssiAsym_map.clear();
        rssiSym_map = new HashMap<Short, Integer>();
        rssiSym_map.clear();
        
        visibleListMap = new HashMap<String, VisibleNodesList<VisibleNode>>();
        visibleListMap.clear();
        symVisibleListMap = new HashMap<String, VisibleNodesList<VisibleNode>>();
        symVisibleListMap.clear();
        asymVisibleListMap = new HashMap<String, VisibleNodesList<VisibleNode>>();
        asymVisibleListMap.clear();
        
        visibleSizePercentMap = new HashMap<String, ArrayList<Double>>();
        visibleSizePercentMap.clear();
        symVisibleSizePercentMap = new HashMap<String, ArrayList<Double>>();
        symVisibleSizePercentMap.clear();
        
        visibleSizePercentDayTime = new HashMap<String, Double>();
        visibleSizePercentDayTime.clear();
        
        communityListMap = new HashMap<String, BTCommunityList<BTCommunity>>();
        communityListMap.clear();
        
        communityPerBinMap = new HashMap<String, ArrayList<String>[]>();
        communityPerBinMap.clear();
        
        initializeSOCSList("./mapping.csv");
    }
    
    private void initializeSOCSList(String mappingFile){
    	valid_socs = new ArrayList<String>();
    	valid_socs.clear();
    	
    	try{
            BufferedReader reader = new BufferedReader(new FileReader(mappingFile));
            String dataRow = reader.readLine(); // Read first line.
            
            // The while checks to see if the data is null. If   
            // it is, we've hit the end of the file. If not,   
            // process the data.  
            while (dataRow != null){ 
                String[] dataArray = dataRow.split(" ");         
                if(dataArray.length != 2){
                    System.out.println("Error: The map file is not complete, exit!");
                    System.exit(0);
                }
                
                String socsID = dataArray[1];   
                valid_socs.add(socsID);
                
                dataRow = reader.readLine(); // Read next line of data.
            }  
            // Close the file once all data has been read. 
            reader.close();
        } catch(FileNotFoundException e) {
            // Inform user that file was not found.    
            e.printStackTrace();
        } catch(IOException e) {
            System.err.println("Error: " + e);
        }	
    }
    
    // Do a set of analysis
    public void perform() {
    	createProximity("./socsData/BT_2012_05/02.csv");
    	
    	//generateRSSIMap();
    	//printRSSIMap();
    	//printRSSIValues();
    	//getRSSIStat();
    	//printRSSIStat();
    	
    	//generateVisibleListMap();
    	//printVisibleListMap("socs012");
    	
    	//generateVisibleSizePercentMap();
    	//printVisibleSizePercentMap("socs092");
    	//printVisibleSizePercentStat();
    	//printVisibleSizePercentDistribution(1);
    	
    	generateVisibleSizePercentDayTime();
    	printVisiblePercentDayTimeDistribution();
    	
    	//createCommunityMap();
    	//printCommunityCnt("socs092");
    	//String[] socsList = {"socs092"};
    	//getCommunityVariation(socsList);
    	//getAvgCommunitySizePerHr("socs092"); 	
    }
    
    // Fill in the proximity matrix based on the data file
    public void createProximity(String fileName) {
    	try{
            BufferedReader file = new BufferedReader(new FileReader(fileName));
            String dataRow = file.readLine(); // Read first line.
            // The while checks to see if the data is null. If   
            // it is, we've hit the end of the file. If not,   
            // process the data.  
            while (dataRow != null) {   
                String[] dataArray = dataRow.split(",");
                
                if(dataArray.length != 5) {
                    System.out.println("Error: The map file is corrupted. exit");
                    System.exit(0);
                }
                
                String socsID1 = dataArray[0];                  // the 1st socs
                String socsID2 = dataArray[2];                  // the socs seen by the 1st socs
                long timeStamp = Long.parseLong(dataArray[1]);  // time 
                short sigStr = Short.parseShort(dataArray[4]);  // rssi value
                
                if(socsID1.contains("socs") && socsID2.contains("socs")) {
                    int i = convertEpochTime(timeStamp);
                    int j = Integer.parseInt(socsID1.substring(4));
                    int k = Integer.parseInt(socsID2.substring(4));
                    //System.out.println("socs index: " + j + " " + k);
                    prmty[i][j][k] = true;
                    rssi[i][j][k] = sigStr;
                    
                    // fill in the communityPerBinMap
                    if(!communityPerBinMap.containsKey(socsID1)) {
                    	ArrayList<String>[] communities =  new ArrayList[SAMPLE_CNT];
                    	for(int m = 0; m < SAMPLE_CNT; m++) {
                    		communities[m] = new ArrayList<String>();
                    		communities[m].clear();
                    	}
                    	communityPerBinMap.put(socsID1, communities);
                    }
                    if(!communityPerBinMap.get(socsID1)[i].contains(socsID2)) {
                    	communityPerBinMap.get(socsID1)[i].add(socsID2);
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
    	
    	if(remove_noise == true) {
    		noise_cnt = symmetrizeSamples();
    	}
    }
    
    public void createCommunityMap() {
    	for (int j = 1; j < NODE_CNT; j++) {
    		String key = "socs" + String.format("%03d", j);
    		if(valid_socs.contains(key)) {
        		if (!communityListMap.containsKey(key)) {
        			communityListMap.put(key, new BTCommunityList<BTCommunity>());
        		}
        		BTCommunityList<BTCommunity> communityList = communityListMap.get(key);
        		
    		    for (int i = 0; i < SAMPLE_CNT; i++) {
    		    	ArrayList<String> visbileList = new ArrayList<String>();
    		    	visbileList.clear();
    		    	for (int k = 1; k < NODE_CNT; k++) {
    		    		if (prmty[i][j][k]) {
    		    			String socsID = "socs" + String.format("%03d", k);
    		    			visbileList.add(socsID);
    		    		}
    		    	}
    		    	
    		    	String sortedList = serializeVisibileList(visbileList);	    
    		    	if (communityList.getCommunityById(sortedList) == null) {
    		    		communityList.add(new BTCommunity(sortedList, 1));
    		    	} else {
    		    		communityList.getCommunityById(sortedList).increaseCount();
    		    	}		    	
    		    }
    		}
    	}
    }
    
    public void printCommunityCnt(String socsID) {
    	String key = socsID;
    	try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(key + "_BTCommunity.csv"));
			
			if (communityListMap.containsKey(key)) {
	    	    BTCommunityList<BTCommunity> communities = communityListMap.get(key);
	        	
	        	Comparator<BTCommunity> comparator;
	        	comparator = Collections.reverseOrder(new BTCommunitySortedByCnt());
	        	Collections.sort(communities, comparator);
	        	
	        	for (int i = 0; i < communities.size(); i++) {
	        		BTCommunity community = communities.get(i);
	        		System.out.println(community.getID() + "," + community.getCount());
	        		writer.write(community.getID() + "," + community.getCount() + "\n");
	        	}
	        	writer.flush();
	        	writer.close();
	        	
	    	} else {
	    		System.out.println(key + " has no visible nodes during the given time.");
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
    
    // Return the community variation bin by bin
    public void getCommunityVariation(String[] socsIDs) {
    	for (int i = 0; i < socsIDs.length; i++) {
    		String socsID = socsIDs[i];
    		double[] variation = new double[SAMPLE_CNT];
    		if (valid_socs.contains(socsID)) {
    			ArrayList<String>[] communities = new ArrayList[SAMPLE_CNT];
    			communities = communityPerBinMap.get(socsID);
    			
    			//variation[0] = 0.0;
    			for (int j = 0; j < SAMPLE_CNT; j++) {
    				variation[j] = communities[j].size();// calcVariation(communities[j-1], communities[j]);
    			}
    			printCommunityVariation(variation);
    		} else {
    			System.out.println("The given socsID is not valid.");
    		}
    		
    	}
    }
    
    private double calcVariation(ArrayList<String> list1, ArrayList<String> list2) {
    	int cnt = 0;
    	if(list1.equals(list2)) {
    		return 0;
    	} else {
    	    for (int i = 0; i < list2.size(); i++) {
    		    if (!list1.contains(list2.get(i))) {
    			    cnt++;
    		    }
    	    }
    	    return cnt/(double)list1.size();
    	}
    }
    
    private void printCommunityVariation(double[] array) {
    	for (int i = 0; i < array.length; i++) {
    		System.out.println(i + " " + array[i]);
    	}
    	
    }
    
    // get the average community size in each hour and 
    public void getAvgCommunitySizePerHr(String socsID) {
		ArrayList<String>[] communities = new ArrayList[SAMPLE_CNT];
		communities = communityPerBinMap.get(socsID);
    	double[] sizeArray = new double[24];
    	if (communities == null) {
    		for (int i = 0; i < 24; i++) 
				sizeArray[i] = 0.0;
    	} else {
    		for (int i = 0; i < 24; i++) {
    			int sum = 0;
    			for (int j = i*60; j < (i+1)*60 ; j++) {
    				sum += communities[j].size();
    			}
    			sizeArray[i] = sum/60.0;
    			System.out.println(sizeArray[i]);
    		}
    	}
    }
    
    
    // get the average community size by chunks of a day based on flag
    // e.g. 0am - 6 am, 6 am - 12 pm, 12 pm - 6 pm, 6 pm - 12 am 
    // flag must be either 1, 2, 3, or 4.
    public double getCommunitySizeByTimeClass(int flag, String socsID) {
    	int startIndex = 60*6*(flag-1);
    	int endIndex = startIndex + 60*6;
		int sumSize = 0;
		double average = 0.0;
    	if (valid_socs.contains(socsID)) {
    		ArrayList<String>[] communities = new ArrayList[SAMPLE_CNT];
			communities = communityPerBinMap.get(socsID);
			if (communities != null) {
				for (int i = startIndex; i < endIndex; i++) {
					sumSize += communities[i].size();
				}
				average = sumSize/(double)(60*6);	
			}
    	} else {
			System.out.println("The given socsID is not valid.");
		}
    	return average;
    }
    
	// Calculate the statistics of rssi values
	public void getRSSIStat() {
		for (int i = 0; i < SAMPLE_CNT; i++) {
			for (int j = 1; j < NODE_CNT; j++) {
				for (int k = j + 1; k < NODE_CNT; k++) {
					if (prmty[i][j][k] || prmty[i][k][j]) {
						if (prmty[i][j][k] == prmty[i][k][j]) {
							rssiCalcSym.enter(Math.abs(rssi[i][j][k] - rssi[i][k][j]));
							//rssiCalcSym.enter((rssi[i][j][k] + rssi[i][k][j])/2);
						} else {
							rssiCalcAsym.enter(rssi[i][j][k] + rssi[i][k][j]);
						}
					}
				}
			}
		}
	}
    
    // Print out the statitics of rssi values
    public void printRSSIStat() {
    	System.out.println("For Symmetric Samples: " + rssiCalcSym.getMin() + " " + rssiCalcSym.getMax() + " " + rssiCalcSym.getMean() + " " + rssiCalcSym.getStandardDeviation());
    	System.out.println("For Asymmetric Samples: " + rssiCalcAsym.getMin() + " " + rssiCalcAsym.getMax() + " " + rssiCalcAsym.getMean() + " " + rssiCalcAsym.getStandardDeviation());
    }
    
    // Generate the maps: visibleSizePercentMap and symVisibleSizePercentMap
	public void generateVisibleSizePercentMap() {
		for (int j = 1; j < NODE_CNT; j++) {
			String key = "socs" + String.format("%03d", j);
			if (valid_socs.contains(key)) {
				int[] threshCnt = new int[] { 0, 0, 0, 0, 0, 0 };
				int[] symThreshCnt = new int[] { 0, 0, 0, 0, 0, 0 };
				for (int i = 0; i < SAMPLE_CNT; i++) {
					int visibleCnt = 0;
					int symVisibleCnt = 0;
					for (int k = 1; k < NODE_CNT; k++) {
						if (prmty[i][j][k])
							visibleCnt++;

						if (prmty[i][j][k] && prmty[i][k][j])
							symVisibleCnt++;
					}
					for (int m = 0; m < 6; m++) {
						if (m <= visibleCnt)
							threshCnt[m]++;

						if (m <= symVisibleCnt)
							symThreshCnt[m]++;
					}
				}

				if (!visibleSizePercentMap.containsKey(key)) {
					visibleSizePercentMap.put(key, new ArrayList<Double>());
					ArrayList<Double> freq = visibleSizePercentMap.get(key);
					for (int index = 0; index < 6; index++) {
						freq.add(index, (double) threshCnt[index] / SAMPLE_CNT);
					}
				}

				if (!symVisibleSizePercentMap.containsKey(key)) {
					symVisibleSizePercentMap.put(key, new ArrayList<Double>());
					ArrayList<Double> symFreq = symVisibleSizePercentMap.get(key);
					for (int index = 0; index < 6; index++) {
						symFreq.add(index, (double) symThreshCnt[index] / SAMPLE_CNT);
					}
				}
			}
		}
	}
	
	// Generate the maps: visibleSizePercentDayTime
	public void generateVisibleSizePercentDayTime() {
		for (int j = 1; j < NODE_CNT; j++) {
			String key = "socs" + String.format("%03d", j);
			if (valid_socs.contains(key)) {
				int totalCnt = 0;
				for (int i = 0; i < SAMPLE_CNT; i++) {
					int visibleCnt = 0;
					for (int k = 1; k < NODE_CNT; k++) {
						// a. visible; b. rssi > -80; c. choose samples from 8 am - 8 pm
						if (prmty[i][j][k] && (rssi[i][j][k] > -80) && (i > 479 && i < 1200))
							visibleCnt++;
					}
					if (visibleCnt > 0) {
						totalCnt ++; 
					}
				}

				if (!visibleSizePercentDayTime.containsKey(key)) {
					visibleSizePercentDayTime.put(key, (double) totalCnt * 3 / SAMPLE_CNT);
				}
			}
		}
	}
    
	// Print out the maps: visibleSizePercentMap and symVisibleSizePercentMap for
	// a given mobile node
	public void printVisibleSizePercentMap(String _key) {
		String key = _key;
		if (visibleSizePercentMap.containsKey(key)) {
			ArrayList<Double> probList = visibleSizePercentMap.get(key);
			for (int i = 0; i < probList.size(); i++) {
				double prob = probList.get(i);
				System.out.println("% of time seeing at least " + i
						+ " node(s) is: " + prob);
			}
		} else {
			System.out.println(key
					+ " has no visible nodes during the given duration.");
		}
	}
    
	// Print out the statistics based on the content of visibleSizePercentMap
	// and symVisibleSizePercentMap of all nodes
	public void printVisibleSizePercentStat() {
		StatCalc[] statList = new StatCalc[6];
		for (int i = 0; i < 6; i++) {
			statList[i] = new StatCalc();
		}

		for (int i = 1; i < NODE_CNT; i++) {
			String key = "socs" + String.format("%03d", i);
			if (visibleSizePercentMap.containsKey(key)) {
				ArrayList<Double> probList = visibleSizePercentMap.get(key);
				if(probList.get(1)> 0.5)
					System.out.println("Interesting node: " + key);
				for (int j = 0; j < probList.size(); j++) {
					double prob = probList.get(j);
					statList[j].enter(prob);
				}
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

	// For each node, print the % of time it sees at least <threshold> nodes
	public void printVisibleSizePercentDistribution(int threshold) {
		int thd = threshold;
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					"visibility_distr.csv"));
			Iterator<String> iter = visibleSizePercentMap.keySet().iterator();
			while (iter.hasNext()) {
				String socsNumber = iter.next();
				double val = visibleSizePercentMap.get(socsNumber).get(thd);
				writer.write(socsNumber + "," + val + "\n");
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			System.err.println("Error: " + e);
		}
	}
	
	// For each node, print the % of time it sees at least 1 node from 8am to 8pm with rssi > -80
	public void printVisiblePercentDayTimeDistribution() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					"dayTime_visibility_distr.csv"));
			Iterator<String> iter = visibleSizePercentDayTime.keySet().iterator();
			while (iter.hasNext()) {
				String socsNumber = iter.next();
				double val = visibleSizePercentDayTime.get(socsNumber);
				writer.write(socsNumber + "," + val + "\n");
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			System.err.println("Error: " + e);
		}
	}
    
	// Generate the visible lists for all mobile nodes, overall, symmetric and
	// asymmetric samples
	public void generateVisibleListMap() {
		for (int j = 1; j < NODE_CNT; j++) {
			String key = "socs" + String.format("%03d", j);
			if (valid_socs.contains(key)) {
				for (int k = 1; k < NODE_CNT; k++) {
					String nodeID = "socs" + String.format("%03d", k);
					for (int i = 0; i < SAMPLE_CNT; i++) {
						if (prmty[i][j][k] == true) {
							// fill the overall map
							if (!visibleListMap.containsKey(key)) {
								visibleListMap.put(key, new VisibleNodesList<VisibleNode>());
							}

							VisibleNodesList<VisibleNode> nodes = visibleListMap.get(key);

							if (nodes.getVisibleNodeById(nodeID) == null) {
								nodes.add(new VisibleNode(nodeID, k, 1));
							} else {
								VisibleNode node = nodes.getVisibleNodeById(nodeID);
								node.increaseCount();
							}
							// fill the symmetric and asymmetric map
							if (prmty[i][j][k] == prmty[i][k][j]) {
								if (!symVisibleListMap.containsKey(key)) {
									symVisibleListMap.put(key, new VisibleNodesList<VisibleNode>());
								}

								VisibleNodesList<VisibleNode> symNodes = symVisibleListMap.get(key);

								if (symNodes.getVisibleNodeById(nodeID) == null) {
									symNodes.add(new VisibleNode(nodeID, k, 1));
								} else {
									VisibleNode symNode = symNodes.getVisibleNodeById(nodeID);
									symNode.increaseCount();
								}
							} else {
								if (!asymVisibleListMap.containsKey(key)) {
									asymVisibleListMap.put(key, new VisibleNodesList<VisibleNode>());
								}

								VisibleNodesList<VisibleNode> asymNodes = asymVisibleListMap.get(key);

								if (asymNodes.getVisibleNodeById(nodeID) == null) {
									asymNodes.add(new VisibleNode(nodeID, k, 1));
								} else {
									VisibleNode asymNode = asymNodes.getVisibleNodeById(nodeID);
									asymNode.increaseCount();
								}
							}
						}
					}
				}
			}
		}
	}
    
	public void printRSSIValues(){
		try {
			// print to file for asymmetric rssi
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					"rssi_asym_values.csv"));
			for(int i = 0; i < rssiAsym_list.size(); i++) {
				writer.write(rssiAsym_list.get(i) + "\n");
			}
			writer.flush();
			writer.close();

			// print to file for symmetric average rssi diff
			BufferedWriter writer1 = new BufferedWriter(new FileWriter(
					"rssi_sym_values.csv"));
			for(int i = 0; i < rssiSym_list.size(); i++) {
				writer1.write(rssiSym_list.get(i) + "\n");
			}
			writer1.flush();
			writer1.close();
		} catch (IOException e) {
			System.err.println("Error: " + e);
		}
	}
	
	
    // Print out the visible list of a specific node
    public void printVisibleListMap(String _key) {
    	String key = _key;
    	try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(key + "_vis_map.csv"));
			
			if (visibleListMap.containsKey(key)) {
	    	    VisibleNodesList<VisibleNode> nodes = visibleListMap.get(key);
	        	
	        	Comparator<VisibleNode> comparator;
	        	comparator = Collections.reverseOrder(new VisibleNodeSortedByCnt());
	        	Collections.sort(nodes, comparator);
	        	
	        	for (int i = 0; i < nodes.size(); i++) {
	        		VisibleNode node = nodes.get(i);
	        		System.out.println(node.getID() + "\t" + node.getCount());
	        		writer.write(node.getID() + "," + node.getCount() + "\n");
	        	}
	        	writer.flush();
	        	writer.close();
	        	
	    	} else {
	    		System.out.println(key + " has no visible nodes during the given time.");
	    	}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
	// Generate the statistics of rssi value and corresponding count
	public void generateRSSIMap() {
		for (int i = 0; i < SAMPLE_CNT; i++) {
			for (int j = 1; j < NODE_CNT; j++) {
				for (int k = j + 1; k < NODE_CNT; k++) {
					if (prmty[i][j][k] != prmty[i][k][j]) {
						// we know in asymmetric samples, either rssi[i][j][k] or rssi[i][k][j] is 0
						short rssiValue = (short) (rssi[i][j][k] + rssi[i][k][j]);
				
						if (!rssiAsym_map.containsKey(rssiValue)) {
							rssiAsym_map.put(rssiValue, 1);
						} else {
							int cnt = rssiAsym_map.get(rssiValue);
							cnt++;
							rssiAsym_map.put(rssiValue, cnt);
						}
						rssiAsym_list.add(rssiValue);
					}

					if (prmty[i][j][k] && prmty[i][k][j]) {
						short rssiValue = (short) (Math.abs(rssi[i][j][k] - rssi[i][k][j]));
						if (!rssiSym_map.containsKey(rssiValue)) {
							rssiSym_map.put(rssiValue, 1);
						} else {
							int cnt = rssiSym_map.get(rssiValue);
							cnt++;
							rssiSym_map.put(rssiValue, cnt);
						}
						rssiSym_list.add(rssiValue);
					}
				}
			}
		}
	}
    
	// Print out the contents of rssiAsym_map.
	public void printRSSIMap() {
		try {
			// print to file for asymmetric rssi
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					"rssi_asym_map.csv"));
			Iterator<Short> iter = rssiAsym_map.keySet().iterator();
			while (iter.hasNext()) {
				short rssiVal = Short.parseShort(iter.next().toString());
				int cnt = rssiAsym_map.get(rssiVal);
				writer.write(rssiVal + "," + cnt + "\n");
			}
			writer.flush();
			writer.close();

			// print to file for symmetric average rssi
			BufferedWriter writer1 = new BufferedWriter(new FileWriter(
					"rssi_sym_map.csv"));
			Iterator<Short> iter1 = rssiSym_map.keySet().iterator();
			while (iter1.hasNext()) {
				short rssiVal = Short.parseShort(iter1.next().toString());
				int cnt = rssiSym_map.get(rssiVal);
				writer1.write(rssiVal + "," + cnt + "\n");
			}
			writer1.flush();
			writer1.close();
		} catch (IOException e) {
			System.err.println("Error: " + e);
		}
	}
    
    // Convert the UNIX time into human-readable format
    // and calculate the correct index for the 1st dimension 
    // of the 3D proximity matrix
    private int convertEpochTime(long timeStamp) {
    	int index = 0;
    	String date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(timeStamp*1000));
    	int hr = Integer.parseInt(date.substring(11, 13));  // extract the hour part
    	int mi = Integer.parseInt(date.substring(14, 16));  // extract the minute part
    	index = 60*hr + mi;
    	return index;
    }
    
    // Proximity matrix symmetrization, i.e., at time x, if node1 was in proximity of node2, 
    // then it must also hold that node2 is in proximity of node1. But the raw data may not
    // have such symmetry proximity for a node. Symmetrization helps to remove such kind of noise
    private int symmetrizeSamples() {
    	int noiseCnt = 0; 
    	for (int i = 0; i < SAMPLE_CNT; i++) {         // initialization
        	for( int j = 1; j < NODE_CNT; j++) {
        		for (int k = j+1; k < NODE_CNT; k++) {
        			if (prmty[i][j][k] != prmty[i][k][j]) {
        				noiseCnt++;
                        short uniformRSSI = (short) (rssi[i][j][k] + rssi[j][i][k]);
                        
        			    prmty[i][j][k] =  prmty[i][k][j] = true;
        			    rssi[i][j][k] = rssi[j][i][k] = uniformRSSI;
        			    hrNoiseVol[i/60]++;
        			}
        		}
        	}
    	}
    	return noiseCnt;
    }
    
    public int getSymmetryNoiseCnt(){
    	return noise_cnt;
    }
    
    // Print out the count of asymmetric samples for each hour
    public void printHourNoiseCnt() {
    	for (int i=0; i < 24; i++) {
    		System.out.println("Hour " + i + "-" + (i+1) + ": " + hrNoiseVol[i]);
    	}
    }       
}
