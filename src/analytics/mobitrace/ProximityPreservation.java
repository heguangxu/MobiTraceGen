package analytics.mobitrace;

import java.util.*;

import analytics.mobitrace.geomtypes.*;
import analytics.utility.*;

public class ProximityPreservation {
	private HashMap<String, ArrayList<String>[]> bt_proximity;          // actual Blutooth proximty
	private HashMap<String, ArrayList<String>[]> wifi_proximity;        // actual WiFi proximity 
	private HashMap<String, Point> ap_coords;                           // AP positions
	private HashMap<String, Point[]> node_traces;                       // mobility traces for each node
	private HashMap<String, double[][]> bt_error;                       // 
	private HashMap<String, double[][]> wifi_error;                     //
	private HashMap<String, double[][]> bt_presv;                   // BT presrevation for each node across time
	private HashMap<String, double[][]> wifi_presv;                 // WiFi preservation for each node across time
	private HashMap<String, Double> velocity_presv;                 // Velocity preservation
	private HashMap<String, Double> bt_presv_avg;                   // average f-score w.r.t. BT preservation for ecach node in the given duration
	private HashMap<String, Double> wifi_presv_avg;                 // average f-score w.r.t. WiFi preservation for each node in the given duration
	private HashMap<String, int[]> ap_missCnt;                      // total FP and FN per ap
	private HashMap<Integer, int[]> missCntPerSlot;                 // total FP and FN count in a given time slot
	private int time_instances;
	
	public ProximityPreservation(HashMap<String, ArrayList<String>[]> btProximity, 
			                     HashMap<String, ArrayList<String>[]> wfProximity, 
			                     HashMap<String, Point> apPositions, 
			                     HashMap<String, Point[]> traces, 
			                     int timeInstances) {
		bt_proximity = new HashMap<String, ArrayList<String>[]>();
		bt_proximity.clear();
		bt_proximity = btProximity;
		
		wifi_proximity = new HashMap<String, ArrayList<String>[]>();
		wifi_proximity.clear();
		wifi_proximity = wfProximity;
		
		ap_coords = new HashMap<String, Point>();
		ap_coords.clear();
		ap_coords = apPositions;
		
		node_traces = new HashMap<String, Point[]>();
		node_traces.clear();
		node_traces = traces;
	
		time_instances = timeInstances;
		
		bt_error = new HashMap<String, double[][]>();
		bt_error.clear();
		
		wifi_error = new HashMap<String, double[][]>();
		wifi_error.clear();
		
		bt_presv = new HashMap<String, double[][]>();
		bt_presv.clear();
		
		wifi_presv = new HashMap<String, double[][]>();
		wifi_presv.clear();
		
		velocity_presv = new HashMap<String, Double>();
		velocity_presv.clear();
		
		bt_presv_avg = new HashMap<String, Double>();
		bt_presv_avg.clear();
		
		wifi_presv_avg = new HashMap<String, Double>();
		wifi_presv_avg.clear();
		
		ap_missCnt = new HashMap<String, int[]>();
		ap_missCnt.clear();
		
		missCntPerSlot = new HashMap<Integer, int[]>();
		missCntPerSlot.clear();
	}
	
	// Extract the Bluetooth proximity using generated traces
	private HashMap<String, ArrayList<String>[]> extractResultedBTProximity() {
		HashMap<String, ArrayList<String>[]> result = new HashMap<String, ArrayList<String>[]>();
		result.clear();

		for (String socsID : node_traces.keySet()) {
			ArrayList<String>[] nearbyNodes = new ArrayList[time_instances];
			Point[] traces = node_traces.get(socsID);
			
			for (int i = 0; i < time_instances; i++) {
				Point position = traces[i];
				nearbyNodes[i] = new ArrayList<String>();
				nearbyNodes[i].clear();
				
				if (position == null) {
					nearbyNodes[i] = bt_proximity.get(socsID)[i];
				} else {
					for (String nodeID : node_traces.keySet()) {
						Point nodePosition = node_traces.get(nodeID)[i];
						if (!socsID.equals(nodeID) && 
							nodePosition != null &&	
							position.distanceTo(nodePosition) <= MobilitySolution.BT_R &&
							!nearbyNodes[i].contains(nodeID)) {
							nearbyNodes[i].add(nodeID);
						}
					}
				}
			}
			if (!result.containsKey(socsID)) {
				result.put(socsID, nearbyNodes);
			}
		}
		return result;
	}
	
	// Extract WiFi proximity using generated traces
	private HashMap<String, ArrayList<String>[]> extractResultedWiFiPrxomity() {
		HashMap<String, ArrayList<String>[]> result = new HashMap<String, ArrayList<String>[]>();
		result.clear();

		for (String socsID : node_traces.keySet()) {
			ArrayList<String>[] nearbyAPs = new ArrayList[time_instances];
			Point[] traces = node_traces.get(socsID);
			for (int i = 0; i < time_instances; i++) {
				Point position = traces[i];
				nearbyAPs[i] = new ArrayList<String>();
				nearbyAPs[i].clear();
				
				if (position == null) {
					nearbyAPs[i] = wifi_proximity.get(socsID)[i];
				} else {
					for (String apMac : ap_coords.keySet()) {
						Point apPosition = ap_coords.get(apMac);
						if (position.distanceTo(apPosition) <= MobilitySolution.WF_R && 
							!nearbyAPs[i].contains(apMac)) {
							nearbyAPs[i].add(apMac);
						}
					}
				}	
			}
			if (!result.containsKey(socsID)) {
				result.put(socsID, nearbyAPs);
			}			
		}
		return result;
	}
	
	// Calculate the BT and WiFi proximity preservation for all nodes
	public void calcPrxtyPresv() {
		HashMap<String, ArrayList<String>[]> resultBT = extractResultedBTProximity();
		HashMap<String, ArrayList<String>[]> resultWF = extractResultedWiFiPrxomity();
		Iterator<String> iter = node_traces.keySet().iterator();
		while (iter.hasNext()) {
			String socsID = iter.next();
			ArrayList<String>[] ndsByTime = resultBT.get(socsID);
			ArrayList<String>[] apsByTime = resultWF.get(socsID);
			// Bluetooth proximity preservation 
			double[][] presvBT = calcBTPrxtyPresvPerNode(socsID, ndsByTime); 
			if (!bt_presv.containsKey(socsID)) {
				bt_presv.put(socsID, presvBT);
			}
			
			// Bluetooth proximity error
			double[][] errsBT = calcBTPrxtyErrPerNode(socsID, ndsByTime);
			if (!bt_error.containsKey(socsID)) {
				bt_error.put(socsID, errsBT);
			}
			
			// WiFi proximity preservation
			double[][] presvWF = calcWiFiPrxtyPresvPerNode(socsID, apsByTime);
			if (!wifi_presv.containsKey(socsID)) {
				wifi_presv.put(socsID, presvWF);
			}
			
			// WiFi proxmity error
			double[][] errsWF = calcWiFiPrxtyErrPerNode(socsID, apsByTime);
			if (!wifi_error.containsKey(socsID)) {
				wifi_error.put(socsID, errsWF);
			}
		}
	}
	
	// Calculate the Bluetooth proximity preservation for each node
	private double[][] calcBTPrxtyPresvPerNode (String socsID, ArrayList<String>[] nodesByTime) {
		double[][] presv = new double[time_instances][2];
		for (int i = 0; i < time_instances; i++) {
			presv[i][0] = 1.0;
			presv[i][1] = 1.0;
		}
		
		for (int i = 0; i < time_instances; i++) {
			ArrayList<String> nodes = nodesByTime[i];
			ArrayList<String> actualNodes = bt_proximity.get(socsID)[i];
			Point nodePosition = node_traces.get(socsID)[i];
			if (nodePosition == null) {
				continue;
			}
			
			int correctCnt = 0; 
			int recallCnt = 0;
			int sizeObtained = nodes.size();
			int sizeActual = actualNodes.size();
			// Precision
			if (sizeObtained == 0) {
			    presv[i][0] = 1.0;
			} else {
				for (String node : nodes) {
					if (actualNodes.contains(node)) {
						correctCnt++;
					}
				}
				presv[i][0] = (double)correctCnt/(double)sizeObtained;
			}
			
			// Recall
			if (sizeActual == 0) {
				presv[i][1] = 1.0;
			} else {
				for (String node : actualNodes) {
					if (nodes.contains(node)) {
						recallCnt++;
					}
				}
				presv[i][1] = (double)recallCnt/(double)sizeActual;
			}
		}
		return presv;
	}

	// Calculate the WiFi proximity preservation for each node
	private double[][] calcWiFiPrxtyPresvPerNode(String socsID, ArrayList<String>[] apsByTime) {
		double[][] presv = new double[time_instances][2];
		for (int i = 0; i < time_instances; i++) {
			presv[i][0] = 1.0;
			presv[i][1] = 1.0;
			
		}
		for (int i = 0; i < time_instances; i++) {
			ArrayList<String> aps = apsByTime[i];
			ArrayList<String> actualAPs = wifi_proximity.get(socsID)[i];
			Point nodePosition = node_traces.get(socsID)[i];
			if (nodePosition == null) {
				continue;
			}
			
			int correctCnt = 0; 
			int recallCnt = 0;
			int sizeObtained = aps.size();
			int sizeActual = actualAPs.size();
			// Precision
			if (sizeObtained == 0) {
				presv[i][0] = 1.0;
			} else {
				for (String ap : aps) {
					if (actualAPs.contains(ap)) {
						correctCnt++;
					} else {  // do the false positive counting
						if(!ap_missCnt.containsKey(ap)){
							int[] cnts = new int[2];
							cnts[0] = 0;   // sotre false positive count
							cnts[1] = 0;   // sotre false negative count
							ap_missCnt.put(ap, cnts);
						}
						ap_missCnt.get(ap)[0]++;
						
						if(!missCntPerSlot.containsKey(i)) {
							int[] cnts = new int[2];
							cnts[0] = 0;
							cnts[1] = 0;
							missCntPerSlot.put(i,cnts);
						}
						missCntPerSlot.get(i)[0]++;
					}
				}
				presv[i][0] = (double)correctCnt/(double)sizeObtained;
				
				if(sizeActual == 0) {  // this is because of WiFi turned off
					presv[i][0] = 1;
				}
			}
			
			// Recall
			if (sizeActual == 0) {
				presv[i][1] = 1.0;
			} else {
				for (String ap : actualAPs) {
					if (aps.contains(ap)) {
						recallCnt++;
					} else {           // do the false negative count
						if(!ap_missCnt.containsKey(ap)){
							int[] cnts = new int[2];
							cnts[0] = 0;   // sotre false positive count
							cnts[1] = 0;   // sotre false negative count
							ap_missCnt.put(ap, cnts);
						}
						ap_missCnt.get(ap)[1]++;
						
						if(!missCntPerSlot.containsKey(i)) {
							int[] cnts = new int[2];
							cnts[0] = 0;
							cnts[1] = 0;
							missCntPerSlot.put(i,cnts);
						}
						missCntPerSlot.get(i)[1]++;
					}
				}
				presv[i][1] = (double)recallCnt/(double)sizeActual;
			}
		}
		return presv;
	}
	
	// Print out the total # of misses w.r.t. each AP
	public void printMissCntPerAP() {
		int i = 0;
		for (String key : ap_missCnt.keySet()) {
			int[] cnt = ap_missCnt.get(key);
			System.out.println(i++ + "," + cnt[0] + "," + cnt[1]);
		}
	}
	
	// Print out the overall # of missess across time
	public void printMissCntPerSlot() {
		for (int i = 0; i < time_instances; i++) {
			int[] cnt = missCntPerSlot.get(i);
			System.out.println(i + "," + cnt[0] + "," + cnt[1]);
		}
	}
	
	// Calculate the Bluetooth proximity error for each node
	public double[][] calcBTPrxtyErrPerNode(String socsID, ArrayList<String>[] nodesByTime) {
		double[][] errs = new double[time_instances][2];
		for (int i = 0; i < time_instances; i++) {
			errs[i][0] = 0.0;
			errs[i][1] = 0.0;
		}
		for (int i = 0; i < nodesByTime.length; i++) {
			ArrayList<String> nodes = nodesByTime[i];
			ArrayList<String> actualnodes = bt_proximity.get(socsID)[i];
			Point nodePosition = node_traces.get(socsID)[i];
			if (nodePosition == null) {
				continue;
			}
			
			double errSumFP = 0.0;
			int errCntFP = 0; 
			for (String node : nodes) {
				if (!actualnodes.contains(node)) {  // false positive, shouldn't detect this ap but actualy did
					Point position = node_traces.get(node)[i];
					if (position != null) {
					    errSumFP += Math.abs((position.distanceTo(nodePosition) - MobilitySolution.BT_R));
					    errCntFP++;
				    }
				}
			}
			if (errCntFP > 0) {
				errs[i][0] = errSumFP/errCntFP;
			}
			
			double errSumFN = 0.0;
			int errCntFN = 0;
			for (String node : actualnodes) {    // false negative, should detect this ap but actually didn't 
				if (!nodes.contains(node)) {
					Point position = node_traces.get(node)[i];
					if (position != null) {
						errSumFN += Math.abs((position.distanceTo(nodePosition) - MobilitySolution.BT_R));
					    errCntFN++;
					}
				}
			}		
			if (errCntFN > 0) {
				errs[i][1] = errSumFN/errCntFN;
			}
		}
		return errs;
	}
	
	// Calculate the WiFi proximity error for each node
	public double[][] calcWiFiPrxtyErrPerNode(String socsID, ArrayList<String>[] apsByTime) {
		double[][] errs = new double[time_instances][4];
		for (int i = 0; i < time_instances; i++) {
			errs[i][0] = 0.0;
			errs[i][1] = 0.0;
			errs[i][2] = 0;
			errs[i][3] = 0;
		}
		for (int i = 0; i < time_instances; i++) {
			ArrayList<String> aps = apsByTime[i];
			ArrayList<String> actualAPs = wifi_proximity.get(socsID)[i];
			Point nodePosition = node_traces.get(socsID)[i];
			if (nodePosition == null) {
				continue;
			}
			
			double errSumFP = 0.0;
			int errCntFP = 0; 
			for (String ap : aps) {
				if (!actualAPs.contains(ap)) {  // false positive, shouldn't detect this ap but actualy did
					Point apPosition = ap_coords.get(ap);
					errSumFP += Math.abs((apPosition.distanceTo(nodePosition) - MobilitySolution.WF_R));
					errCntFP++;
				}
			}
			if (errCntFP > 0) {
				errs[i][0] = errSumFP/errCntFP;
			}
			
			double errSumFN = 0.0;
			int errCntFN = 0; 
			for (String ap : actualAPs) {    // false negative, should detect this ap but actually didn't 
				if (!aps.contains(ap)) {
					Point apPosition = ap_coords.get(ap);
					errSumFN += Math.abs((apPosition.distanceTo(nodePosition) - MobilitySolution.WF_R));
					errCntFN++;
				}
			}
			if (errCntFN > 0) {
				errs[i][1] = errSumFN/errCntFN;
			}
			errs[i][2] = errCntFP;
			errs[i][3] = errCntFN;
		}
		return errs;
	}
	
	// 
	public void calcAvgPrxtyPresv() {
		Iterator<String> iter = node_traces.keySet().iterator();
		while (iter.hasNext()) {
			String socsID = iter.next();
			StatCalc[] statBT = new StatCalc[2];
			StatCalc[] statWF = new StatCalc[2];
			for (int i = 0; i < 2; i++) {
				statBT[i] = new StatCalc();
				statWF[i] = new StatCalc();
			}
			for (int i = 0; i < time_instances; i++) {
				// Bluetooth
				double btp = bt_presv.get(socsID)[i][0];
				double btr = bt_presv.get(socsID)[i][1];
				statBT[0].enter(btp);
				statBT[1].enter(btr);
				// WiFi
				double wfp = wifi_presv.get(socsID)[i][0];
				double wfr = wifi_presv.get(socsID)[i][1];
				statWF[0].enter(wfp);
				statWF[1].enter(wfr);
			}
			// Bluetooth
			double avgBTp = statBT[0].getMean();
			double avgBTr = statBT[1].getMean();
			double avgBTf = 2*avgBTp*avgBTr / (avgBTp+avgBTr);
			if (!bt_presv_avg.containsKey(socsID)) {
				bt_presv_avg.put(socsID, avgBTf);
			}
			// WiFi
			double avgWFp = statWF[0].getMean();
			double avgWFr = statWF[1].getMean();
			double avgWFf = 2*avgWFp*avgWFr / (avgWFp+avgWFr);
			if (!wifi_presv_avg.containsKey(socsID)) {
				wifi_presv_avg.put(socsID, avgWFf);
			}
		}
		
		/*System.out.println("User ID, BT, WiFi, Combined");
		for (String key : node_traces.keySet()) {
			double btf = btPresvAvg.get(key);
			double wff = wfPresvAvg.get(key);
			double combf = combPresvAvg.get(key);
			System.out.println(key + "," + btf + "," + wff + "," + combf);
		}*/
	}
	
	public void printWorstNodeInfo() {
		Map<String, Double> unsorted = wifi_presv_avg;
		Map<String, Double> sorted = sortMapByValue(unsorted);
		Iterator<String> it = sorted.keySet().iterator();
		it.next();
		it.next();
		String socs = it.next();
		HashMap<String, ArrayList<String>[]> resultWF = extractResultedWiFiPrxomity();
		System.out.println(socs);
		for (int i = 0; i < time_instances; i++) {
			double wfp = wifi_presv.get(socs)[i][0];
			double wfr = wifi_presv.get(socs)[i][1];
			int obtainedSize = resultWF.get(socs)[i].size();
			int actualSize = wifi_proximity.get(socs)[i].size();
			System.out.println("WiFi pre, obt size, act size: " + wfp + ", " + wfr + ", "  + obtainedSize + ", " +actualSize);
		}
	}
	
	// Sort a passed in map according to values
	private Map sortMapByValue(Map unsortMap) {
		List list = new LinkedList(unsortMap.entrySet());
		 
		// sort list based on comparator
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue())
                                       .compareTo(((Map.Entry) (o2)).getValue());
			}
		});
 
		// put sorted list into map again
                //LinkedHashMap make sure order in which keys were inserted
		Map sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
		
	}
	
	// Calculate the overall proximity preservation considering all nodes
	public void calcAggrPrxtyPresv() {
		StatCalc aggBTStat[] = new StatCalc[2];
		StatCalc aggWFStat[] = new StatCalc[2];
		for (int i = 0; i < 2; i++) {
			aggBTStat[i] = new StatCalc();
			aggWFStat[i] = new StatCalc();
		}
		
		StatCalc[][] btstat = new StatCalc[time_instances][2];
		StatCalc[][] wfstat = new StatCalc[time_instances][2];
		for (int i = 0; i < time_instances; i++) {
			for (int j = 0; j < 2; j++) {
				btstat[i][j] = new StatCalc();
				wfstat[i][j] = new StatCalc();
			}
		}
		
		for (int i = 0; i < time_instances; i++) {
			// Bluetooth
			Iterator<String> iter1 = bt_presv.keySet().iterator();
			while (iter1.hasNext()) {
				String socsID = iter1.next();
				double[] val = new double[2];
				val[0] = bt_presv.get(socsID)[i][0]; // precision
				val[1] = bt_presv.get(socsID)[i][1]; // recall
				btstat[i][0].enter(val[0]);
				btstat[i][1].enter(val[1]);
			}
			double btp = btstat[i][0].getMean();
			double btr = btstat[i][1].getMean();
			aggBTStat[0].enter(btp);
			aggBTStat[1].enter(btr);
			
			// WiFi
			Iterator<String> iter2 = wifi_presv.keySet().iterator();
			while (iter2.hasNext()) {
				String socsID = iter2.next();
				double[] val = new double[2];
				val[0] = wifi_presv.get(socsID)[i][0]; // precision
				val[1] = wifi_presv.get(socsID)[i][1]; // recall
				wfstat[i][0].enter(val[0]);
				wfstat[i][1].enter(val[1]);
			}
			double wfp = wfstat[i][0].getMean();
			double wfr = wfstat[i][1].getMean();
			aggWFStat[0].enter(wfp);
			aggWFStat[1].enter(wfr);
		}
		
		// aggregate BT preservation result
		double aggre_btp = aggBTStat[0].getMean();
		double aggre_btr = aggBTStat[1].getMean();
		double aggre_btf = 2*aggre_btp*aggre_btr / (aggre_btp+aggre_btr);
		System.out.println("BT Proxmity Preservation Aggregate: ");
		System.out.println(aggre_btp + ", " + aggre_btr + ", " + aggre_btf);
		System.out.println("************************************");
		
		// aggregate WF preservation result
		double aggre_wfp = aggWFStat[0].getMean();
		double aggre_wfr = aggWFStat[1].getMean();
		double aggre_wff = 2*aggre_wfp*aggre_wfr / (aggre_wfp+aggre_wfr);
		System.out.println("WiFi Proxmity Preservation Aggregate: ");
		System.out.println(aggre_wfp + ", " + aggre_wfr + ", " + aggre_wff);
		System.out.println("************************************");
	}
	
	// Get the overall Bluetooth proximity error stat
	public void getBTPrxtyErrStat() {
		System.out.println("BT Proxmity Err Stat.");
		for (int i = 0; i < time_instances; i++) {
			StatCalc[] stat = new StatCalc[2];
			stat[0] = new StatCalc();
			stat[1] = new StatCalc();

			for (String socsID : bt_error.keySet()) {
				double val2 = bt_error.get(socsID)[i][0];
				double val1 = bt_error.get(socsID)[i][1];
				stat[0].enter(val1);
				stat[1].enter(val2);
			}
			System.out.println(i + ", " + stat[0].getMean() + ", " + stat[1].getMean());
		}
	}
	
	// Get the overall WiFi proximity error stat
	public void getWiFiPrxtyErrStat() {
		System.out.println("WiFi Proxmity Err Stat.");
		for (int i = 0; i < time_instances; i++) {
			StatCalc[] stat = new StatCalc[4];
			stat[0] = new StatCalc();
			stat[1] = new StatCalc();
			stat[2] = new StatCalc();
			stat[3] = new StatCalc();
			
			for (String socsID : wifi_error.keySet()) {
				double val1 = wifi_error.get(socsID)[i][0];
				double val2 = wifi_error.get(socsID)[i][1];
				double val3 = wifi_error.get(socsID)[i][2];
				double val4 = wifi_error.get(socsID)[i][3];
				stat[0].enter(val1);
				stat[1].enter(val2);
				if(val3 > 0)
				stat[2].enter(val3);
				if(val4 > 0)
				stat[3].enter(val4);
			}
			System.out.println(i + ", " + stat[0].getMean() + ", " + stat[1].getMean() + ", " + stat[2].getMean() + ", " + stat[3].getMean());
		}
	}
	
	// 
	public void calVelocityPreservation() {
		Iterator<String> iter = node_traces.keySet().iterator();
		while (iter.hasNext()) {
			String socsID  = iter.next();
			int satCnt = 0;
			Point[] traces  = node_traces.get(socsID);
			for (int i = 1; i < time_instances; i++) {
				Point p1 = traces[i-1];
				Point p2 = traces[i];
				if (p1 == null || p2 == null) {
					satCnt++;
				}
				if (p1 != null && p2 != null) {
					double dist = p1.distanceTo(p2);
					double v = (double)dist/(double)300;
					if (v <= 1.5)
						satCnt++;
				}
			}
			double value = (double)(++satCnt)/(double)time_instances;	
			if (!velocity_presv.containsKey(socsID)) {
				velocity_presv.put(socsID, value);
			}
		}
	}
	
	public void getVelocityPreservationStat() {
		Iterator<String> iter = velocity_presv.keySet().iterator();
		StatCalc stat = new StatCalc();
		while (iter.hasNext()) {
			String socsID  = iter.next();
			stat.enter(velocity_presv.get(socsID));
		}
		System.out.println("****************************");
		System.out.println("Velocity constraints perservation");
		System.out.println(stat.getMin() + ", " + stat.getMax() + ", " + stat.getMean() + ", " + stat.getStandardDeviation());
	}
}
