package analytics.mobitrace;

public class VisibleEntity {
	/** attributes **/
	private String id; // for AP it is the mac; for node it is the socsID
	private short rssi; // associated rssi value

	public VisibleEntity(String _id, short _rssi) {
		id = _id;
		rssi = _rssi;
	}
	
	public String getID() {
		return id;
	}

	public short getRSSI() {
		return rssi;
	}

	public void setRSSI(short _rssi) {
		rssi = _rssi;
	}
}
