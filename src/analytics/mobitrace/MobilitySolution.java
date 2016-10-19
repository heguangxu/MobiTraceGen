package analytics.mobitrace;

import java.util.*;

import analytics.*;
import analytics.mobitrace.geomtypes.Point;

public class MobilitySolution {
	static final int WF_R = 25;        // the WiFi  range
	static final int BT_R = 5;         // the Bluetooth range
    private String date;               // specify a date in format YYYY/MM/DD/
	private int sample_freq;           // sampling frequency
    
	private HashMap<String, ArrayList<String>[]> bt_proximity;    // node socsID : visible AP list[time_index]
	private HashMap<String, ArrayList<String>[]> wifi_proximity;  // node socsI
	private HashMap<String, VisibleEntitiesList[]> wf_proximity;    //
	private HashMap<String, Point> ap_coords;
	private HashMap<String, Point[]> node_traces;
	
	public MobilitySolution(String _date, int _sampleFreq) {
		date = _date;
		sample_freq = _sampleFreq;
		
	  	bt_proximity = new HashMap<String, ArrayList<String>[]>();
    	bt_proximity.clear();
    	
    	wifi_proximity = new HashMap<String, ArrayList<String>[]>();
    	wifi_proximity.clear();
    	
    	wf_proximity = new HashMap<String, VisibleEntitiesList[]>();
    	wf_proximity.clear();
    	
    	ap_coords = new HashMap<String, Point>();
    	ap_coords.clear();
    	
    	node_traces = new HashMap<String, Point[]>();
    	node_traces.clear();
	}
	
	public void generateSolution() {
		String btFile = "./socsData/Bluetooth/" + date + ".csv";
		String wfFile = "./socsData/WiFi/" + date + ".csv";
		
		// Process samples for a single day
		/*
		ParseDataOneDay parser = new ParseDataOneDay(sample_freq, btFile, wfFile);
		parser.createProximityOneDay();
		bt_proximity = parser.getBTProximityOneDay();
		wifi_proximity = parser.getWFProximityOneDay();
		int timeInstances = parser.getTimeInstanceCnt();
		*/
		
		
		DataParserOneDay p = new DataParserOneDay(sample_freq, btFile, wfFile);
		p.createProximityOneDay();
		bt_proximity = p.getBTProximityOneDay();
		wf_proximity = p.getWFRSSIProximityOneDay();
		wifi_proximity = p.getWFProximityOneDay();
		int timeInstances = p.getTimeInstanceCnt();
		
		
		// Process samples for multiple days
        /*
		DataParser mparser = new DataParser("./socsData/Bluetooth/2012/04", 
				"./socsData/WiFi/2012/04", 14);
		mparser.parseDataMultiDays();
		bt_proximity = mparser.getBTProximity();	
		wifi_proximity = mparser.getWFProximity();
		int timeInstances = mparser.getTimeInstanceCnt();
		*/
		
		// Place the APs
	    /*
		APDeployment apSolver = new APDeployment(wifi_proximity, date, true);
		apSolver.deployAPs();
		//apSolver.printNeighborhoodSize();
		ap_coords = apSolver.getAPCoords();
        */
		
		APPlacement apSolver = new APPlacement(wf_proximity, date, true);
		apSolver.deployAPs();
		ap_coords = apSolver.getAPCoords();
	    
		// Generate traces for mobile nodes
		/*
		TraceGenerator traceSolver = new TraceGenerator(timeInstances,
				                                        bt_proximity, 
				                                        wifi_proximity, 
				                                        ap_coords);
		
        traceSolver.generateTraces();
		node_traces = traceSolver.getTraces();
	    */
		//traceSolver.printTraces();
		//traceSolver.printTraceOneNode("socs039");
		
		
		// Inter-contact time preservation 
		/*
		InterContactPreservation ip = new InterContactPreservation(bt_proximity, 
				                                                   node_traces,
				                                                   timeInstances);
		ip.calcInterContactTimes();
		*/
		
		// Proximity preservation
		/*
		ProximityPreservation pp = new ProximityPreservation(bt_proximity, 
				                                             wifi_proximity,
				                                             ap_coords, 
				                                             node_traces,
				                                             timeInstances); 
	                                       
	    pp.calcPrxtyPresv();
	    //pp.calcAvgPrxtyPresv();
	    //pp.printWorstNodeInfo();
	    pp.calcAggrPrxtyPresv();
	    //pp.printMissCntPerAP();
	    //pp.printMissCntPerSlot();
		
		//pp.calVelocityPreservation();
		//pp.getVelocityPreservationStat();	
		//pp.getBTPrxtyErrStat();
		//pp.getWiFiPrxtyErrStat();
		
        //traceSolver.printn2nSize("socs039");
        */
	   
	}
	
}
