package analytics.bluetooth;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;


public class BTCommunityVariation {
	private String socsID;
    private HashMap<String, double[]> dateCommunitySizeMap;  // 
    
    public BTCommunityVariation(String _socsID) {
    	socsID = _socsID;
    	dateCommunitySizeMap = new HashMap<String, double[]>();
    	dateCommunitySizeMap.clear();
    }
    
    // fill in dateCommunitySizeMap
    public void createCommunitySizeMap() {
    	File inputDir = new File("./socsData/BT_1stWeek");
    	File[] inputFiles = inputDir.listFiles();
    	
    	for (File inFile : inputFiles) {
    		String fileName = "./socsData/BT_1stWeek/" + inFile.getName();
    		double[] sizeArray = {0.0, 0.0, 0.0, 0.0};
    		
    		BluetoothProximityStat bt = new BluetoothProximityStat(false);
    		bt.createProximity(fileName);
    
    		for (int i = 1; i < 5; i++) {
    			sizeArray[i-1] = bt.getCommunitySizeByTimeClass(i, socsID);
    		}
    		
    		String date = inFile.getName().substring(0, 2);
    		if(!dateCommunitySizeMap.containsKey(date)) {
    			dateCommunitySizeMap.put(date, sizeArray);
    		}
    	}
    }
    
    public void printCommunitySizeMap() {
    	try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(socsID + "_btcomm_size_var.csv"));
			
			String firstLine = "Dates," + "00~06," + "06~12," + "12~18," + "18~24,";
			writer.write(firstLine + "\n");
			
			Iterator<String> iter = dateCommunitySizeMap.keySet().iterator();
			while(iter.hasNext()) {
				String date = iter.next();
				String line = date + ",";
				for (int i = 0; i < 4; i++) {
					line += dateCommunitySizeMap.get(date)[i] + ",";
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
