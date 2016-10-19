package analytics.utility;

import java.text.SimpleDateFormat;
import java.util.Date;

/** Convert the UNIX time into human-readable format
 * and calculate the correct index for the 1st dimension 
 * of the 3D proximity matrix
 */

 public class ConvertEpochTime {
	 public int getTimeIndex(long time_stamp, int bin_size) {
		int index = 0;
		String date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
				.format(new Date(time_stamp * 1000));
		int hr = Integer.parseInt(date.substring(11, 13)); // extract the hour
															// part
		int mi = Integer.parseInt(date.substring(14, 16)); // extract the minute
															// part
		index = (60 * hr + mi) / bin_size;
		return index;
	 }
}
