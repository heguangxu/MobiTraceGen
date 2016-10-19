package analytics;

import java.util.Random;

import analytics.events.*;
import analytics.greenzone.GreenZoneStatMultiDays;
import analytics.mobitrace.Test;
import analytics.utility.ActiveSocsCnt;
import analytics.utility.DeviceIDReplacement;
import analytics.wifi.WiFiProximityStat;

public class Main {
	/**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    	
    	//DeviceIDReplacement d = new DeviceIDReplacement("WiFi", "2012", "02");
    	//d.createMap("./mapping.csv");
    	//d.replaceDeviceNumberWithSOCS();
        
    	//BluetoothProximityStat bt = new BluetoothProximityStat(false);
    	//bt.perform();
    	
    	//WiFiProximityStat wf = new WiFiProximityStat(60, 2);
    	//wf.perform();
    	
    	//WiFiCommunityVariation wv = new WiFiCommunityVariation("socs018");
    	//wv.createCommunitySizeMap();
    	//wv.printCommunitySizeMap();
    	
    	//BTCommunityVariation bv = new BTCommunityVariation("socs092");
    	//bv.createCommunitySizeMap();
    	//bv.printCommunitySizeMap();
    	
    	//GreenZoneStatMultiDays gzm = new GreenZoneStatMultiDays();
    	//gzm.extractMultiDayStat("socs018");
    	//gzm.printOneDeviceStat();
    	//gzm.printAllDevicesStat();
    	
    	//EventsStatMultiDays es = new EventsStatMultiDays(288);
    	//es.extractMultiDayStat("socs018");
    	//es.printOneDeviceEventsDist();
    	//es.printAllDevicesEventsStat();	
    	
    	//StatesSummaryMultiDays ss = new StatesSummaryMultiDays(0, 8, 5, "2012", "02", true);
    	//ss.extractMultiDayStat("socs018");
    	//ss.printOneDeviceStatesDist();
    	//printAllDevicesStatesStat();	

        //APCommunityTransOneDay o = new APCommunityTransOneDay(5, 0);
        //o.createAPProximity("./socsData/WiFi/2012/04/01.csv");
        //o.createCommunitiesByTime();
        //o.summarizeTransCnt();
        //o.OneDeviceCommTrans("socs018");
        
       // APCommunityTransMultiDays mult = new APCommunityTransMultiDays(5, "2012", "02", "socs018");
    	//mult.extractMultiDaysTrans();
    	//mult.printOneDeviceMultiDaysTrans();
    	//ActiveSocsCnt ac = new ActiveSocsCnt();
    	//ac.getDailyCnt();
    	//ac.printDailyCnt();
    
    	Test t = new Test();
    	t.test();
    	/*double min = 1.0;
    	double max = 10.0;
    	for (int i = 0; i < 100; i++) {
    	Random random = new Random();
        System.out.println( min + random.nextDouble()*(max - min));
    	}*/
    }
}
