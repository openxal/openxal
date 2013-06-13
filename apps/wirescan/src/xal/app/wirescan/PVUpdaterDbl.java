/*
 * PVUpdaterDbl.java
 */

package xal.app.wirescan;

import xal.ca.*;

/**
 * This class monitors a channel that returns a double for updates.
 * A PVUpdaterDbl connects to a channel and returns a new
 * record as a double and then converts it to a Double.
 * It then returns the Double.
 *
 * @author	S. Bunch
 * @version	1.0
 * @see PVUpdater
 */
public class PVUpdaterDbl extends PVUpdater
{
	private Double recordValue;

	/**
	 * The PVUpdaterDbl constructor takes a Channel type
	 *
	 * @param channel	The channel to be used
	 */
	public PVUpdaterDbl(Channel channel){
		theChannel = channel;
		theChannel.addConnectionListener(this);
		if(!theChannel.isConnected()) {
			theChannel.connectAndWait();
		}
		else makeMonitor(theChannel);
	}

	/**
	 * The eventValue function is overloaded for this specific double.
	 * Each time the ChannelRecord is updated, the double is retrieved
	 * and stored as a new Double
	 *
	 * @param newRecord		The value that is checked for changes.
	 * @param chan		The channel that is being monitored.
	 * @see ChannelRecord
	 * @see Channel
	 * @see Double
	 */
	public void eventValue(ChannelRecord newRecord, Channel chan) {
		/* Get the new double and store it as a Double */
		recordValue = new Double(newRecord.doubleValue());
	}

	/**
	 * Method used to return the Double.
	 *
	 * @return		Double value of the double.
	 * @see Double
	 */
	protected Double getValue() {
		/* If null, then return 0.0 */
		if(recordValue == null) return new Double(0.0);
		return recordValue;
	}
}
