package analytics.mobitrace;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import analytics.mobitrace.geomtypes.*;
import analytics.utility.*;


public class InterContactPreservation {
	private final int BT_R = 5;       // the bluethooth proxmity range
	
	private HashMap<String, ArrayList<String>[]> preTrace_btProximity;          //
	private HashMap<String, ArrayList<String>[]> posTrace_btProximity;          // 
	private HashMap<String, Point[]> node_traces;                   //
	private HashMap<String, ArrayList<Integer>> preTrace_contacts;  // contact between two nodes at time spots in the orignial data
	private HashMap<String, ArrayList<Integer>> posTrace_contacts;  // contact between two nodes at time spots in the generated traces
	private ArrayList<Integer> preTrace_interContact;               // store all the inter-contact time cases for original proximity
	private ArrayList<Integer> posTrace_interContact;               // store all the inter-contact time cases for generated proxmity
	
	private int time_instances;
	
	public InterContactPreservation(HashMap<String, ArrayList<String>[]> btProximity,
			                        HashMap<String, Point[]> traces, 
			                        int timeInstances) {
		preTrace_btProximity = new HashMap<String, ArrayList<String>[]>();
		preTrace_btProximity.clear();
		preTrace_btProximity = btProximity;
		
		posTrace_btProximity = new HashMap<String, ArrayList<String>[]>();
		posTrace_btProximity.clear();
		
		node_traces = new HashMap<String, Point[]>();
		node_traces.clear();
		node_traces = traces;
		
		preTrace_contacts = new HashMap<String, ArrayList<Integer>>();
		preTrace_contacts.clear();
		
		posTrace_contacts = new HashMap<String, ArrayList<Integer>>();
		posTrace_contacts.clear();
		
		preTrace_interContact = new ArrayList<Integer>();
		preTrace_interContact.clear();
		
		posTrace_interContact = new ArrayList<Integer>();
		posTrace_interContact.clear();
		
		time_instances = timeInstances;
	}
	
	public void calcInterContactTimes() {
		generatePostTraceBTProximity();
		summarizeContacts();
		for (String key : preTrace_contacts.keySet()) {
			ArrayList<Integer> timespots = preTrace_contacts.get(key);
			Collections.sort(timespots);
			for (int i = 1; i < timespots.size(); i++) {
				int interTime = Math.abs(5 * (timespots.get(i) - timespots.get(i-1) - 1));
				if (interTime > 5)
				    preTrace_interContact.add(interTime);
			}
		}
		
		for (String key : posTrace_contacts.keySet()) {
			ArrayList<Integer> timespots = posTrace_contacts.get(key);
			Collections.sort(timespots);
			for (int i = 1; i < timespots.size(); i++) {
				int interTime = Math.abs(5 * (timespots.get(i) - timespots.get(i-1) - 1));
				if (interTime > 5)
				    posTrace_interContact.add(interTime);
			}
		}
		printInterContactTimes();
	}
	
	private void summarizeContacts() {
		for (String key : node_traces.keySet()) {
			ArrayList<String>[] ndsByTime1 = preTrace_btProximity.get(key);
			ArrayList<String>[] ndsByTime2 = posTrace_btProximity.get(key);
			for (int i = 0; i < time_instances; i++) {
				// process the original proximity
				ArrayList<String> nds1 = ndsByTime1[i];
				for (String nd : nds1) {
					String twoIDs = key + nd;
					if (!preTrace_contacts.containsKey(twoIDs)) {
						ArrayList<Integer> times = new ArrayList<Integer>();
						times.clear();
						preTrace_contacts.put(twoIDs, times);
					} 
					preTrace_contacts.get(twoIDs).add(i);
				}
				// process the post-trace proximity
				ArrayList<String> nds2 = ndsByTime2[i];
				for (String nd : nds2) {
					String twoIDs = key + nd;
					if (!posTrace_contacts.containsKey(twoIDs)) {
						ArrayList<Integer> times = new ArrayList<Integer>();
						times.clear();
						posTrace_contacts.put(twoIDs, times);
					} 
					posTrace_contacts.get(twoIDs).add(i);
				}
			}
		}
	}
	
	// Calculate the Bluetooth proximity using generated traces
	private void generatePostTraceBTProximity() {
		Iterator<String> iter = node_traces.keySet().iterator();
		while (iter.hasNext()) {
			String socsID = iter.next();
			ArrayList<String>[] nearbyNodes = new ArrayList[time_instances];
			Point[] traces = node_traces.get(socsID);
			
			for (int i = 0; i < time_instances; i++) {
				Point position = traces[i];
				nearbyNodes[i] = new ArrayList<String>();
				nearbyNodes[i].clear();
				
				if (position == null) {
					nearbyNodes[i] = preTrace_btProximity.get(socsID)[i];
				} else {
					Iterator<String> iter2 = node_traces.keySet().iterator();
					while (iter2.hasNext()) {
						String nodeID = iter2.next();
						Point nodePosition = node_traces.get(nodeID)[i];
						if (!socsID.equals(nodeID) && 
							nodePosition != null &&	
							position.distanceTo(nodePosition) <= BT_R &&
							!nearbyNodes[i].contains(nodeID)) {
							nearbyNodes[i].add(nodeID);
						}
					}
				}
			}
			if (!posTrace_btProximity.containsKey(socsID)) {
				posTrace_btProximity.put(socsID, nearbyNodes);
			}
		}
	}
	
	private void printInterContactTimes() {
		try {
			BufferedWriter writer1 = new BufferedWriter(new FileWriter(
					"./preInterContact.txt"));
			
			for (int i = 0; i < preTrace_interContact.size(); i++) {
				writer1.write(preTrace_interContact.get(i) + "\n");
			}
			writer1.flush();
			writer1.close();
			
			BufferedWriter writer2 = new BufferedWriter(new FileWriter(
					"./posInterContact.txt"));

			for (int i = 0; i < posTrace_interContact.size(); i++) {
				writer2.write(posTrace_interContact.get(i) + "\n");
			}
			writer2.flush();
			writer2.close();		
		} catch (IOException e) {
			System.err.println("Error: " + e);
		}		
	}
}
