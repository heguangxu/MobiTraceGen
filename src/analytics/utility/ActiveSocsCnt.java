package analytics.utility;

import java.io.*;
import java.text.*;
import java.util.*;

/**
* Class used to count the number of 
* distinct socs devices in the WiFi samples 
* for each day 
* @author xhu2, Oct 2012
*/
public class ActiveSocsCnt {

	private HashMap<String, Integer> dailyCnt; // date : # of devices has WiFi samples
	
	public ActiveSocsCnt() {
		
		dailyCnt = new HashMap<String, Integer>();
		dailyCnt.clear();
	}
	
	public void getDailyCnt() {
		try{
			File wfDir = new File("./inputDataFiles/WiFi/07/");
	    	File[] wfFiles = wfDir.listFiles();
	    	
	    	for (int i = 0; i < wfFiles.length; i++) {
	    	    String wfFile = "./inputDataFiles/WiFi/07/" + wfFiles[i].getName();
	    		BufferedReader file = new BufferedReader(new FileReader(wfFile));
	    		ArrayList<String> socsList = new ArrayList<String>();
	    		socsList.clear();
	    		
	    		String dataRow = file.readLine(); // Read first line.
	    		while(dataRow != null) {
	    			String[] dataArray = dataRow.split(",");
	                if(dataArray.length != 5) {
	                    System.out.println("Error: The bt data file is corrupted. exit");
	                    System.exit(0);
	                }
	                
	                String socsID = dataArray[0];   
					if (socsID.contains("574") && !socsList.contains(socsID)) {
						socsList.add(socsID);
					}
	                dataRow = file.readLine(); // Read next line of data.
	    		}
	    		
	    		String wfDate = wfFiles[i].getName().substring(0, 2);
	    		wfDate = "July-" + wfDate;
	    		if(!dailyCnt.containsKey(wfDate)) {
	    			dailyCnt.put(wfDate, socsList.size());
	    		}
	            // Close the file once all data has been read. 
	            file.close();
	    	}

        } catch(FileNotFoundException e) {
            // Inform user that file was not found.    
            e.printStackTrace();
        } catch(IOException e) {
            System.err.println("Error: " + e);
        }	
	}
	
	public void printDailyCnt() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("SOCSCnt_July.csv"));
			
			Object[] key = dailyCnt.keySet().toArray();
            Arrays.sort(key);
			
            for(int i = 0; i < key.length; i++) {
            	int cnt = dailyCnt.get(key[i]);
            	String line = key[i] + "," + cnt;
            	writer.write(line + "\n");
            	System.out.println(line);
            }
        	writer.flush();
        	writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
