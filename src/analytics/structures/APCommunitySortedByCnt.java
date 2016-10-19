package analytics.structures;

import java.util.Comparator;

public class APCommunitySortedByCnt implements Comparator< APCommunity> {
	public int compare(APCommunity cm1, APCommunity cm2) {
        return cm1.getCount() - cm2.getCount();
    }
}
