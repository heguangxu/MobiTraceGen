package analytics.wifi;

import java.util.*;

/**
* Comparator based on cnt of visible cases
* @author xhu2
*/
public class VisibleAPSortedByCnt implements Comparator<VisibleAP> {
	public int compare(VisibleAP ap1, VisibleAP ap2) {
        return ap1.getCount() - ap2.getCount();
    }
}
