package analytics.bluetooth;

import java.util.*;

/**
* Comparator based on cnt of visible cases
* @author xhu2
*/
public class VisibleNodeSortedByCnt implements Comparator<VisibleNode> {
	public int compare(VisibleNode node1, VisibleNode node2) {
        return node1.getCount() - node2.getCount();
    }
}
