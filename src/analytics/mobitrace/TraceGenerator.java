package analytics.mobitrace;

import java.util.*;

import analytics.mobitrace.geomtypes.*;

public class TraceGenerator {
	private int time_instances; 
	private HashMap<String, ArrayList<String>[]> bt_proximity;   // Bluetooth proximity
	private HashMap<String, ArrayList<String>[]> wifi_proximity; // WiFi proximity
	private HashMap<String, Point> ap_coords;                    // the AP deployment
	private ArrayList<String> nodes;                             // set of nodes that either has WiFi or BT samples
	private HashMap<String, Point[]> node_traces;                // map to store the traces
	
	public TraceGenerator(int timeInstances, 
			              HashMap<String, ArrayList<String>[]> btProximity, 
			              HashMap<String, ArrayList<String>[]> wfProximity, 
			              HashMap<String, Point> apCoords) {
		time_instances = timeInstances;
		
		bt_proximity = new HashMap<String, ArrayList<String>[]>();
		bt_proximity.clear();
		bt_proximity = btProximity;
		
		wifi_proximity = new HashMap<String, ArrayList<String>[]>();
		wifi_proximity.clear();
		wifi_proximity = wfProximity;
		
		ap_coords = new HashMap<String, Point>();
		ap_coords.clear();
		ap_coords = apCoords;

		nodes = new ArrayList<String>();
		nodes.clear();
		Iterator<String> iter1 = bt_proximity.keySet().iterator();
		while (iter1.hasNext()) {
			String socsID = iter1.next();
			if (!nodes.contains(socsID))
				nodes.add(socsID);
		}
		Iterator<String> iter2 = wifi_proximity.keySet().iterator();
		while (iter2.hasNext()) {
			String socsID = iter2.next();
			if (!nodes.contains(socsID))
				nodes.add(socsID);
		}
		
		node_traces = new HashMap<String, Point[]>();
		node_traces.clear();
	}
	
	// Generate mobility traces for each mobile node
	public void generateTraces() {
		for (String socsID : nodes) {
			// use dummies to fill proximity data if one node has BT
			// sample but not WiFi sample, or vice-versa
			ArrayList<String>[] dummies = new ArrayList[time_instances];
			for (int t = 0; t < time_instances; t++) {
				dummies[t] = new ArrayList<String>();
				dummies[t].clear();
			}
			
			if (!bt_proximity.containsKey(socsID)) {
				bt_proximity.put(socsID, dummies);
			}
			if (!wifi_proximity.containsKey(socsID)) {
				wifi_proximity.put(socsID, dummies);
			}
			
			if (!node_traces.containsKey(socsID)) {
				Point[] traces = new Point[time_instances];
				ArrayList<String>[] apArrays = wifi_proximity.get(socsID);
				for (int t = 0; t < time_instances; t++) {
					ArrayList<String> aps = apArrays[t];                      // the APs in proximity
					ArrayList<String> deployedAPs = new ArrayList<String>();  // the APs in proximity that have already been deployed
					deployedAPs.clear();
					
					for (String ap : aps) {
						if (ap_coords.containsKey(ap)) {
							deployedAPs.add(ap);		
						}
					}
					
					if (!deployedAPs.isEmpty()) {
					    traces[t] = getRandomPointInWiFiOverlap(deployedAPs);
					    if(Double.isNaN(traces[t].x()) || Double.isNaN(traces[t].y())) {
					    	System.out.println("Invalid WiFi Overlap for " + socsID + " at time " + t);
					    }
					} else {
						traces[t] = null;
					}
				}
				node_traces.put(socsID, traces);
			}
		}
		
		for (String socsID : nodes) {
		    ArrayList<String>[] mnArrays = bt_proximity.get(socsID);
		    Point[] traces = node_traces.get(socsID);
		    for (int t = 0; t < time_instances; t++) {
		    	ArrayList<String> mns = mnArrays[t];                      // mobile nodes in proxmity
				ArrayList<String> deployedMNs = new ArrayList<String>();  // mobile nodes in proximity that have been deployed
				deployedMNs.clear();
				
				if (traces[t] == null) {
					for (String mn : mns) {
						if (node_traces.containsKey(mn) && 
							node_traces.get(mn)[t] != null) {
							deployedMNs.add(mn);
						}
					}
					
					if (!deployedMNs.isEmpty()) {
					    traces[t] = getRandomPointInBTOverlap(deployedMNs, t);
					    if(Double.isNaN(traces[t].x()) || Double.isNaN(traces[t].y()) ||
					       Double.isInfinite(traces[t].x()) || Double.isInfinite(traces[t].y())) {
					    	System.out.println("Invalid BT Overlap for " + socsID + " at time " + t);
					    	System.out.println("Neighbors: ");
					    	for(String socs : deployedMNs) {
					    		System.out.println(socs + ": " + node_traces.get(socs)[t]);
					    	}
					    	System.out.println("*************");
					    }
					} 
				}
		    }
		    node_traces.put(socsID, traces);
		}
	 
	}
	
	// Select a random position within the 
	// common WiFi coverage area of multiple APs
	private Point getRandomPointInWiFiOverlap(ArrayList<String> aps) {
	    Point p; 
	    ArrayList<Point> apCenters = new ArrayList<Point>();
	    apCenters.clear();
	    for (String apMac : aps) {
	    	if (ap_coords.containsKey(apMac) && !apCenters.contains(ap_coords.get(apMac))) {
	    		apCenters.add(ap_coords.get(apMac));
	    	}
	    }
	    
		if (apCenters.size() == 1) {
			Point apPosition = apCenters.get(0);
			p = getRandomPointInCircle(apPosition, MobilitySolution.WF_R/5);
		} else {
			Point[] pts = getIntersectionPtsMCs(apCenters, MobilitySolution.WF_R);
			if (pts.length == 0 || pts == null) {  // no common area for all circles
				Point[] centers = new Point[apCenters.size()];
			    apCenters.toArray(centers);
			    Point c;
                if (centers.length == 2) {
				    c = new Point((centers[0].x() + centers[1].x())/2, (centers[0].y() + centers[1].y())/2);			
                } else {
                	Polygon poly = new Polygon(centers);
                	c = poly.centroid();
                }
                p = getRandomPointInCircle(c, MobilitySolution.WF_R/5);
			} else if (pts.length == 1){          // intersection area has one vertex
				p = pts[0];
			} else if (pts.length == 2) {         // intersection area has two vertices
				Segment seg = new Segment(pts[0], pts[1]);
				p = seg.sample();
			} else {                              // intersection area has three or more vertices
				Polygon poly = new Polygon(pts);
				p = poly.sample();
			}
		}
		return p;
	}
	
	// Select a random position within the 
	// common BT coverage area of multiple devices
	private Point getRandomPointInBTOverlap(ArrayList<String> mns, int timeIndex) {
		Point p;
		ArrayList<Point> mnCenters = new ArrayList<Point>();
		mnCenters.clear();
		
		for (String socsID : mns) {
			if (node_traces.containsKey(socsID) && 
				node_traces.get(socsID)[timeIndex] != null  && 
				!mnCenters.contains(node_traces.get(socsID)[timeIndex])) {
				mnCenters.add(node_traces.get(socsID)[timeIndex]);
			}
		}
		
		if (mnCenters.size() == 1) {
			Point mnPosition = mnCenters.get(0);
			p = getRandomPointInCircle(mnPosition, MobilitySolution.BT_R);
		}  else {
			Point[] pts = getIntersectionPtsMCs(mnCenters, MobilitySolution.BT_R);
			if (pts.length == 0 || pts == null) {
				Point[] centers = new Point[mnCenters.size()];
				mnCenters.toArray(centers);
				Point c;
                if (centers.length == 2) {
				    c = new Point((centers[0].x() + centers[1].x())/2, (centers[0].y() + centers[1].y())/2);			
                } else {
                	Polygon poly = new Polygon(centers);
                	c = poly.centroid();
                }
                p = getRandomPointInCircle(c, MobilitySolution.BT_R/5);	
			} else if (pts.length == 1){
				p = pts[0];
			} else if (pts.length == 2) {
				Segment seg = new Segment(pts[0], pts[1]);
				p = seg.sample();	
			} else {
				Polygon poly = new Polygon(pts);
				p = poly.sample();			
			}
		}
		return p;
	}

	// sample a random point within a given circle
	private Point getRandomPointInCircle(Point center, int radius) {
		Random r = new Random();
		double angle = Math.toRadians(r.nextDouble() * 360);
		double dist = r.nextDouble() * radius;
		double x = center.x() + dist * Math.cos(angle);
		double y = center.y() + dist * Math.sin(angle);
	    return new Point(x, y);
	    
	}
	
	// Get the intersection pts (vertices) of common area of multiple circles
	// if the returned array is empty, then there doesn't exist an common area
	private Point[] getIntersectionPtsMCs(ArrayList<Point> centers, int radius) {
		ArrayList<Point> pts = new ArrayList<Point>();
		pts.clear();
		// get all the intersection points among those circles
		for (int i = 0; i < centers.size(); i++) {
			for (int j = i+1; j < centers.size(); j++) {
				Point[] interPts = getIntersectionPts2Cs(centers.get(i), centers.get(j), radius); 
				for (Point pt : interPts) {
			    	if(!pts.contains(pt))
			    		pts.add(pt);
			    }
			}
		}
		
		// remove the intersection points that are not in the 
		// common area of all circles
		for (int i = 0; i < pts.size(); i++) {
			if (!inCommonArea(pts.get(i), centers, radius))
				pts.remove(i);
		}
		
		Point[] result = new Point[pts.size()]; 
		pts.toArray(result);
		
		return result;	
	}
	
	// Test if the given point is a vertex of the common area of multiple circles
	private boolean inCommonArea(Point pt, ArrayList<Point> centers, int radius) {
		boolean flag = true;
		for (Point c : centers) {
			if (pt.distanceTo(c) > radius)
				return false;
		}
		return flag; 
	}
	
	// Get the intersection point(s) of the coverage of two 
	// APs, which could be represented by two circles
	// return null if no intersection
	public Point[] getIntersectionPts2Cs(Point cA, Point cB, int radius) {
		double d = cA.distanceTo(cB);
		ArrayList<Point> pts = new ArrayList<Point>();
		pts.clear();
	
		if (d > 2*radius) {         // no intersection
			
		} else if (d == 2*radius) { // intersect at one point
			Point p = new Point((cA.x()+cB.x())/2, (cA.y()+cB.y())/2);
			pts.add(p);
		} else {                    // intersect at two points
			double h = Math.sqrt(radius*radius - d*d/4);
			Point mid = new Point((cA.x()+cB.x())/2, (cA.y()+cB.y())/2);
			double p1x = mid.x() + h*(cB.y() - cA.y())/d;
			double p1y = mid.y() - h*(cB.x() - cA.x())/d;
			double p2x = mid.x() - h*(cB.y() - cA.y())/d;
			double p2y = mid.y() + h*(cB.x() - cA.x())/d;

			Point p1 = new Point(p1x, p1y);
			Point p2 = new Point(p2x, p2y);
			pts.add(p1);
			pts.add(p2);
		}
		Point[] points = new Point[pts.size()];
		pts.toArray(points);
		return points;
	}
	
	public HashMap<String, Point[]> getTraces() {
		return node_traces;
	}
	
	public void printTraceOneNode(String socsID) {
		if (!node_traces.containsKey(socsID)) {
			System.out.println(socsID + " cannot be found.");
			return;
		}
		Point[] traces = node_traces.get(socsID);
		for (int i = 0; i < traces.length; i++) {
		    System.out.println(traces[i]);
		}
	}
	
	public void printn2nSize(String socsID) {
			ArrayList<String>[] aps = bt_proximity.get(socsID);
			for (int i = 0; i < 288; i++) {
				System.out.println(aps[i].size());
			}
	}
	
	public void printTraces() {
		Iterator<String> iter = node_traces.keySet().iterator();
		while (iter.hasNext()) {
			String socsID = iter.next();
			Point p = node_traces.get(socsID)[144];
			if (p != null) {
			    System.out.println(p.x() + ", " + p.y());
			}
		}
	}

}
