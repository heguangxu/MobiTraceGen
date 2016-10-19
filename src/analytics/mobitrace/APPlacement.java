package analytics.mobitrace;

import java.io.*;
import java.util.*;

import analytics.mobitrace.geomtypes.*;
import analytics.mobitrace.graph.*;
import mdsj.MDSJ;

public class APPlacement {
	private boolean regen;                                           // flag to indicate whether to re-generate AP
	private String date;                                             // date of the processed data file
	private HashMap<String, Point> ap_coords;                        // the mac of the ap and associated coordinates
    private HashMap<String, ArrayList<String>> neighbor_lists;       // a simplified version of the "neighborhood" map
    private HashMap<String, VisibleEntitiesList[]> node_ap_proximity;  // map used to store the returned result from ParseWiFiOneDay
    private HashMap<String, Integer> relative_dist;                  // relative distance between 2 aps    
    private ArrayList<String> access_points; 
    
    public APPlacement(HashMap<String, VisibleEntitiesList[]> proximity, String _date, boolean _flag) {
    	date = "";
    	String[] dateArray = _date.split("/");
    	for (int i = 0; i < dateArray.length; i++) {
    		date += dateArray[i];
    	}
    	
    	regen = _flag;
		
		ap_coords = new HashMap<String, Point>();
		ap_coords.clear();

		neighbor_lists = new HashMap<String, ArrayList<String>>();
		neighbor_lists.clear();
				
		node_ap_proximity = new HashMap<String, VisibleEntitiesList[]>();
		node_ap_proximity = proximity;
		
		relative_dist  = new HashMap<String, Integer>();
		relative_dist.clear();
		
		access_points = new ArrayList<String>();
		access_points.clear();
    }
    
    // Deploy APs, i.e., assign x-y coordinates for each AP
    public void deployAPs() {
    	String path = "./AP Deployment/" + date + ".csv";
    	File file = new File(path);
    	if (file.exists() && regen == false) {
    		loadDeploymentFromFile(path);
    	} else {
    		if (file.exists()) {
    			file.delete();
    		}

        	//printCnt();
    		
    		deriveRelativeDist();
    		// D - distance matrix between each pair of APs
        	int numAPs = access_points.size();
        	ConnectedComponent cc = new ConnectedComponent(access_points, neighbor_lists);
        	ArrayList<Integer> ids = new ArrayList<Integer>();
        	ids.clear();
        	for (int m = 0; m < numAPs; m++) {
        		int id = cc.getID(m);
        		if (!ids.contains(id))  {
        			ids.add(id);
        			System.out.println("CC Size: " + cc.getSize(m));
        		}
        	}
   
    		double[][] D = new double[numAPs][numAPs];
    		D = buildDistanceMatrix();
    		//printDistPre(D);
    		//storeDistanceMatrix(D);
    		
    		// feed distance matrix into multidimensional scaling and get the coordinates
    		// output[0][i] - x-coordinate for AP i
    		// output[1][i] - y-coordinate for AP i
    		double[][] output = MDSJ.classicalScaling(D);
    		//printDistPos(output);
    		setAPCoords(output);
    		printAPCoords(); 
    	}
    }
    
    private void filterOutSmallCCs() {
    	for (String socsID : node_ap_proximity.keySet()) {
    		VisibleEntitiesList[] apsByBin = node_ap_proximity.get(socsID);
    		for (int i = 0; i < apsByBin.length; i++) {
    			VisibleEntitiesList aps = apsByBin[i];
    			if (aps.size() > 0) {
    				for (VisibleEntity anchor : aps) {
    					String anchorID = anchor.getID();
    			        if (!access_points.contains(anchorID)) {
    			        	access_points.add(anchorID);
    			        }
    				}
    			}
    		}
    	}
    }
    
    private void deriveRelativeDist() {
    	for (String socsID : node_ap_proximity.keySet()) {
    		VisibleEntitiesList[] apsByBin = node_ap_proximity.get(socsID);
    		for (int i = 0; i < apsByBin.length; i++) {
    			VisibleEntitiesList aps = apsByBin[i];
    			if (aps.size() > 0) {
    				for (VisibleEntity anchor : aps) {
    					int dist2anchor = rssi2Dist(anchor.getRSSI());
    					String anchorID = anchor.getID();
    					if (!access_points.contains(anchorID)) {
    						access_points.add(anchorID);
    					}
    					if (!neighbor_lists.containsKey(anchorID)) {
    					    ArrayList<String> nbs = new ArrayList<String>();
    					    nbs.clear();
    					    neighbor_lists.put(anchorID, nbs);
    					}
    					for (VisibleEntity neighbor : aps) {
    					    int dist2neighbor = rssi2Dist(neighbor.getRSSI());
    					    String neighborID = neighbor.getID();
    					    if (anchorID != neighborID) {
    					    	if (!neighbor_lists.get(anchorID).contains(anchorID)) { 
    					    		neighbor_lists.get(anchorID).add(neighborID);
    					    	}
    					    	String pair = anchorID + neighborID;
    					        int dist = dist2anchor + dist2neighbor;
    					        if (!relative_dist.containsKey(pair)) {    
    					        	relative_dist.put(pair, dist);
    					        } else {
    					        	if (dist < relative_dist.get(pair))
    					        		relative_dist.put(pair, dist);
    					        }
    					    }
    					}
    				}
    			}
    		}
    	}
    }
    
    // Maps RSSI values to physical relative distance
    private int rssi2Dist(short rssi) {
    	int dist = MobilitySolution.WF_R;
    	if (rssi >= -85)
    		dist -= 5;
    	if (rssi >= -80)
    		dist -= 5;
    	if (rssi >= -70)
    		dist -= 5;
    	if (rssi >= -55)
    		dist -= 5;
    		
    	return dist;
    }
    
    private void storeDistanceMatrix(double[][] D) {
    	try {
    		String path = "./Distances/" + date + ".txt";
    		BufferedWriter writer = new BufferedWriter(
					new FileWriter(path));
    		int size = D[0].length;
    		
        	for (int i = 0; i < size; i++) {
        		String line="";
        		for (int j = 0; j < size; j++) {
        			line += D[i][j];
        			if(j < size-1)
        				line += " ";
        		}
        		writer.write(line + "\n");
        	}
        	writer.flush();
			writer.close();
    	} catch (IOException e) {
			System.err.println("Error: " + e);
		}
    }
    
    // print out the ap-to-ap distances after deployment
    private void printDistPos(double[][] coords) {
		try {
			BufferedWriter writer = new BufferedWriter(
					new FileWriter("pos.txt"));
			writer.write("dist\n");
			for (int i = 0; i < coords[0].length; i++) {
				Point p1 = new Point(coords[0][i], coords[1][i]);
				for (int j = i+1; j < coords[0].length; j++) {
					Point p2 = new Point(coords[0][j], coords[1][j]);
					String line = p1.distanceTo(p2) + "\n";
					writer.write(line);
				}
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			System.err.println("Error: " + e);
		}
    }
    
    // print out the ap-to-ap distances before deployment
    private void printDistPre(double[][] D) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					"pre.txt"));
			writer.write("dist\n");
			for (int i = 0; i < D[0].length; i++) {
				for (int j = i+1; j < D[1].length; j++) {
					String line = D[i][j] + "\n";
					writer.write(line);
				}
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			System.err.println("Error: " + e);
		}
	}
    
    // Load AP coordinates directly from 
    // existing file instead of re-generating it
    private void loadDeploymentFromFile(String fileName) {
    	try {
			BufferedReader file = new BufferedReader(new FileReader(fileName));
			String dataRow = file.readLine(); // Read first line.
			while (dataRow != null) {
				String[] dataArray = dataRow.split(",");
				String mac = dataArray[0];
				double x = Double.parseDouble(dataArray[1]);
				double y = Double.parseDouble(dataArray[2]);
				Point pt = new Point(x, y);
				if (!ap_coords.containsKey(mac)) {
					ap_coords.put(mac, pt);
				}
				dataRow = file.readLine(); // Read next line of data.
            }
            // Close the file once all data has been read. 
            file.close();
    	} catch(FileNotFoundException e) {
            e.printStackTrace();
    	} catch(IOException e) {
            System.err.println("Error: " + e);
        }	
    }
	
	private void printNeighborCnt() {
		int cnt = 0;
		for (String mac : neighbor_lists.keySet()) {
			ArrayList<String> neighbors = neighbor_lists.get(mac);
			System.out.println(neighbors.size());
			if(neighbors.size() > 0)
				cnt++;
		}
		System.out.println("total # of APs having neighbors: "+cnt);
	}
	
	// Build the distance constraints between each pair of APs
	// based on the given neighborhood information
	private double[][] buildDistanceMatrix() {	
		// initialize the vertex array
		int numVertices = access_points.size();
		Vertex[] vertices = new Vertex[numVertices];   
		for (int i = 0; i < numVertices; i++) {
			vertices[i] = new Vertex(access_points.get(i), i);
		}
		
	 	// initialize the edge array
    	int numEdges = 0;
    	Edge[] edges;
    	ArrayList<String>[] neighborhoods = new ArrayList[neighbor_lists.values().size()];
    	neighbor_lists.values().toArray(neighborhoods);
    	for (int i = 0; i < neighborhoods.length; i++) {
    		numEdges += neighborhoods[i].size();
    	}
    	edges = new Edge[numEdges];
 
    	int j = 0;
    	for (String mac : neighbor_lists.keySet()) {
    		ArrayList<String> neighbors = neighbor_lists.get(mac);
    		for (String neighborMac : neighbors) {
    			int weight = relative_dist.get(mac+neighborMac);
    			edges[j++] = new Edge(vertices[access_points.indexOf(mac)], 
    					              vertices[access_points.indexOf(neighborMac)], 
    					              weight);
    		}
    	}
    	
    	// fill the shortest distance matrix 
		double[][] D = new double[numVertices][numVertices];
		FloydWarshall fw = new FloydWarshall(vertices, edges);
		D = fw.getDistanceMatrix();
		
		// D is the upper-bound matrix of distance, so the actual
		// distance can be randomly assigned it within an interval 
		Random random = new Random(150);
		for (int m = 0; m < numVertices; m++) {
			D[m][m] = 0;
			for (int n = m+1; n < numVertices; n++) {
				D[m][n] = Math.min(D[m][n], D[n][m]);
			}
		}
		return D;
	}
	
	// Store the coordinates of APs into ap_coords map
	private void setAPCoords(double[][] p) {
		for (int i = 0; i < p[0].length; i++) {
			double x = p[0][i];
			double y = p[1][i]; 
			Point pt = new Point(x, y);
			String apMac = access_points.get(i);
				
			if (!ap_coords.containsKey(apMac)) {
				ap_coords.put(apMac, pt);
			}
		}
	}
	
	// 
	private void setAPCoordsFromFile(String name) {
		try {
			BufferedReader file = new BufferedReader(new FileReader(name));
	        String dataRow = file.readLine(); 
	        int index = 0;
	        while (dataRow != null) {
                String[] dataArray = dataRow.split(",");
                
                if(dataArray.length != 2) {
                    System.out.println("The coord file: " + name + " is corrupted, exit.");
                    System.exit(0);
                }
                double x = Double.parseDouble(dataArray[0]);                  // the 1st socs
                double y = Double.parseDouble(dataArray[0]);      
                Point pt = new Point(x, y);
                String mac = access_points.get(index);
                if(!ap_coords.containsKey(mac))
                	ap_coords.put(mac, pt);
                dataRow = file.readLine(); // Read next line of data.
                index++;
	        }
	        
		} catch(FileNotFoundException e) {
            // Inform user that file was not found.    
            e.printStackTrace();
        } catch(IOException e) {
            System.err.println("Error: " + e);
        }	
	}
	
	// Print the AP coordinates into file
	private void printAPCoords() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					"./AP Deployment/" + date + ".csv"));
			Iterator<String> iter = ap_coords.keySet().iterator();
			while (iter.hasNext()) {
				String apMac = iter.next();
				String line = apMac + "," + ap_coords.get(apMac).x() + "," + ap_coords.get(apMac).y() + "\n";
				writer.write(line);
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			System.err.println("Error: " + e);
		}
	}
	
	// Get the coordinates for each AP, use hash map as storage
	public HashMap<String, Point> getAPCoords() { 
		return ap_coords;
	}
	
	public void printNeighborhoodSize() {
		for (String key : neighbor_lists.keySet()) {
			System.out.println(neighbor_lists.get(key).size());
		}
	}

}
