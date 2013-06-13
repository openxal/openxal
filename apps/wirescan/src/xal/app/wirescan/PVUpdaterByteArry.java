/*
 * PVUpdaterByteArry.java
 */

package xal.app.wirescan;

import xal.ca.*;

/**
 * This class monitors a channel that returns a byte array for updates.
 * A PVUpdaterByeArry connects to a channel and returns a new
 * record as a byte array and then converts it to a String
 * and trims off any excess whitespace.  It then returns
 * the String.
 *
 * @author	S. Bunch
 * @version	1.0
 * @see PVUpdater
 */
public class PVUpdaterByteArry extends PVUpdater
{
	private String recordValue;

	/**
	 * The PVUpdaterByteArry constructor takes a Channel type
	 *
	 * @param channel	The channel to be used
	 */
	public PVUpdaterByteArry(Channel channel){
		theChannel = channel;
		theChannel.addConnectionListener(this);
		if(!theChannel.isConnected()) {
			theChannel.connectAndWait();
		}
		else makeMonitor(theChannel);
	}

	/**
	 * The eventValue function is overloaded for this specific Byte Array.
	 * Each time the ChannelRecord is updated, the byte array is retrieved
	 * and stored as a new String
	 *
	 * @param newRecord		The value that is checked for changes.
	 * @param chan		The channel that is being monitored.
	 * @see ChannelRecord
	 * @see Channel
	 * @see String
	 */
	public void eventValue(ChannelRecord newRecord, Channel chan) {
		/* Get new byte array and store it as a String */
		recordValue = new String(newRecord.byteArray());
	}

	/**
	 * Method used to return the byte array as a string and trim off
	 * any excess whitespace.
	 *
	 * @return		String value of the byte array with trimmed whitespace
	 * @see String
	 */
	protected String getValue() {
		/* If null then return a pre-defined string */
		if(recordValue == null) return "Not Available";
		/* Delete excess whitespace from string */
		return recordValue.trim();
	}
}
