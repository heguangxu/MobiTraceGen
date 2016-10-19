package analytics.events;
import java.util.*;
import java.io.*;

public class APCommunityTransMultiDays {
	private int type_cnt = 0;
	//private int start_hr = 0;
	//private int end_hr = 24;
	private int bin_size = 5;
	private String year = "2012";
	private String month = "04"; 
	private String socs_id = "";
	
	private HashMap<String, double[]> oneDeviceAPTrans;   // date : statistics
	
	public APCommunityTransMultiDays(int _binSize, String _year, String _month, String _socs_id) {
		type_cnt = 4;
		bin_size = 5; 
		year = _year;
		month = _month;
		socs_id = _socs_id;
		
		oneDeviceAPTrans = new HashMap<String, double[]>();
		oneDeviceAPTrans.clear();
	}
	
	public void extractMultiDaysTrans() {
		File wfDir = new File("./socsData/WiFi/" + year + "/" + month + "/");
    	File[] wfFiles = wfDir.listFiles();
    	
    	for (int i = 0; i < wfFiles.length; i++) {
    		String wfFile = wfDir.getPath() + "/" + wfFiles[i].getName();
    		APCommunityTransOneDay oneDay = new APCommunityTransOneDay(bin_size, 0);
    		
    		oneDay.createAPProximity(wfFile);
    		oneDay.createCommunitiesByTime();
    		oneDay.summarizeTransCnt();
    		double[] results = new double[4];
    		results = oneDay.getOneDeviceCommTrans(socs_id);
    		
    		String wfDate = wfFiles[i].getName().substring(0, 2);
    		if (!oneDeviceAPTrans.containsKey(wfDate)) {
    			oneDeviceAPTrans.put(wfDate, results);
    		}
    	}
	}
	
	public void printOneDeviceMultiDaysTrans() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(year + month + "_OneDeviceTrans.csv"));
			
			Object[] key = oneDeviceAPTrans.keySet().toArray();
            Arrays.sort(key);
			
            for(int i = 0; i < key.length; i++) {
            	double[] results = oneDeviceAPTrans.get(key[i]);
            	String line = month + "/" + key[i] + ",";
            	for (int j = 0; j < results.length; j++) {
            		line += results[j] + ",";
            	}
            	writer.write(line + "\n");
            }
        	writer.flush();
        	writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
