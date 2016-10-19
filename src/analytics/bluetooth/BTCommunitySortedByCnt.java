package analytics.bluetooth;

import java.util.Comparator;

/**
* Comparator based on cnt of community patterns
* @author xhu2
*/
public class BTCommunitySortedByCnt implements Comparator<BTCommunity>{
	public int compare(BTCommunity cm1, BTCommunity cm2) {
        return cm1.getCount() - cm2.getCount();
    }
}
