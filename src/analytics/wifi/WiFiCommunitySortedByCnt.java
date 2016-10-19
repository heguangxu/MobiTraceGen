package analytics.wifi;

import java.util.Comparator;

/**
* Comparator based on cnt of community patterns
* @author xhu2
*/
public class WiFiCommunitySortedByCnt implements Comparator<WiFiCommunity> {
	public int compare(WiFiCommunity cm1, WiFiCommunity cm2) {
        return cm1.getCount() - cm2.getCount();
    }
}
