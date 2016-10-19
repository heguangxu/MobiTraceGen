package analytics.utility;

import java.io.*;
import java.util.*;

/**
* Class used to replace the device ID in data files
* with specified SOCS number
* 
* @author xhu2, June 2012
*/
public class DeviceIDReplacement {
	// The following HashMap Device ID : SOCS #
	private String data = "Bluetooth"; // only two valid values: Bluetooth or WiFi
	private String year = "2012";  // four digit for year
	private String month = "07";   // two digit in string - e.g. 02 == Feb.
	
    private HashMap<String, String> IDSocsMap;
    
    public DeviceIDReplacement(String _data, String _year, String _month) {
    	data = _data;
    	year = _year;
    	month = _month;
        initializeVariables();
    }
    
    public void initializeVariables() {
    	IDSocsMap = new HashMap<String, String>();
    	IDSocsMap.clear();
    }
    
    public void createMap(String fileDeviceInfo) {
    	try{
            BufferedReader mapFile = new BufferedReader(new FileReader(fileDeviceInfo));
            String dataRow = mapFile.readLine(); // Read first line.
            // The while checks to see if the data is null. If   
            // it is, we've hit the end of the file. If not,   
            int cnt = 0;
            // process the data.  
            while (dataRow != null){   
                String[] dataArray = dataRow.split(" ");
                
                if(dataArray.length < 2){
                    System.out.println("Error: The map file is not complete. exit");
                    System.exit(0);
                }
                
                String deviceID = dataArray[0];
                String socsID = dataArray[1];
                
                if(!IDSocsMap.containsKey(deviceID)) {
                	IDSocsMap.put(deviceID, socsID);
                	cnt++;
                }
                dataRow = mapFile.readLine(); // Read next line of data.
            }  
            // Close the file once all data has been read. 
            mapFile.close();
        } catch (FileNotFoundException e) {
            // Inform user that file was not found.    
            e.printStackTrace();
        } catch(IOException e) {
            System.err.println("Error: " + e);
        }	
    }
    
    public void replaceDeviceNumberWithSOCS() {
    	//File inputDir = new File("./inputDataFiles/" + data + "/" + year + "/" + month + "/");
    	File inputDir = new File("./0411/");
    	File[] inputFiles = inputDir.listFiles();
		
		for (File inFile : inputFiles) {
			try {
			    BufferedReader reader = new BufferedReader(new FileReader(inFile));
				
				String outFileName = inFile.getName();
				//BufferedWriter writer = new BufferedWriter(new FileWriter("./socsData/" + data + "/" + year + "/" + month + "/" + outFileName));
				BufferedWriter writer = new BufferedWriter(new FileWriter("./0411/"  + "cc_" + outFileName));
				String dataRow = reader.readLine(); 
				while(dataRow != null) {
				    String[] dataArray = dataRow.split(",");
					/*if(dataArray.length != 5) {
		                System.out.println("Error: The input data file is not complete, exit.");
		                System.out.println(dataRow);
		                System.exit(0);
		            }*/
					String outputLine = IDSocsMap.get(dataArray[0]);
					for(int i = 1; i < dataArray.length; i++) {
				        outputLine = outputLine + "," + dataArray[i];
					}
					writer.write(outputLine + "\n");
					dataRow = reader.readLine(); 
				}
				reader.close();
				
				writer.flush();
				writer.close();
			}catch (IOException e) {
				System.err.println("Error: " + e);
			} 
		}
    }
}
