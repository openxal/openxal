package xal.app.ringmeasurement;

import xal.ca.*;
import java.text.*;

public class InputPVTableCell implements IEventSinkValue {
	int m_row, m_col;

	Channel m_chan;

//	boolean isUpdating = true;
	
	String cellValue = "";

	public InputPVTableCell() {
	}

	public InputPVTableCell(Channel p_channel, int p_row, int p_col) {
		m_row = p_row;
		m_col = p_col;
		m_chan = p_channel;
		// System.out.println("Created inputPV callback on channel " +
		// p_channel.channelName());

	}

	public boolean isConnected() {
		return m_chan.isConnected();
	}

	protected ChannelRecord recordValue;

	public void eventValue(ChannelRecord newRecord, Channel chan) {
			recordValue = newRecord;
	}

	public String toString() {
		//	System.out.println("row = " + m_row + " col = " + m_col);
		if(recordValue == null) return "null";

	        // force the number display as the format of either "0.0000" or "0.000E0" depending on the value
	        NumberFormat fieldFormat = null;        
	        if (Math.abs(recordValue.doubleValue()) > 10000. || Math.abs(recordValue.doubleValue()) < 0.0001)
	            fieldFormat = new DecimalFormat("0.000E0");
	        else 
	            fieldFormat = new DecimalFormat("0.0000");
	        return fieldFormat.format(recordValue.doubleValue());
	}

}
