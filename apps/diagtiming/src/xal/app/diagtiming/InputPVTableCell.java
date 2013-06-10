package xal.app.diagtiming;

import xal.ca.*;
import java.text.*;

public class InputPVTableCell implements IEventSinkValue {
	int m_row, m_col;

	Channel m_chan;

	boolean isUpdating = true;
	
	String cellValue = "";
	
	int typeInd;

	public InputPVTableCell() {
	}

	public InputPVTableCell(int typeInd, Channel p_channel, int p_row, int p_col) {
		this.typeInd = typeInd;
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
		if (isUpdating) 
			recordValue = newRecord;
	}

	public void setUpdating(boolean upDating) {
		isUpdating = upDating;
	}
	
	public void setCellValue (String val) {
		cellValue = val;
	}

	public String toString() {
		if (!isUpdating) {
			return cellValue;
		} 

		// System.out.println("row = " + m_row + " col = " + m_col);
		if (recordValue == null) {
			return "null";
		}

		// force the number display as the format of either "0.0000" or
		// "0.000E0" depending on the value
		NumberFormat fieldFormat = null;
		if (m_col == 7 || m_col == 10 ||
				m_col == 13 || m_col == 4)
			fieldFormat = new DecimalFormat("0");
		else if (m_col == 1 && typeInd != 0) {
			if (recordValue.doubleValue() > 0.0)
				return "Not OK";
			else 
				return "OK";
		}
		else {
		if (Math.abs(recordValue.doubleValue()) >= 10000.
				|| Math.abs(recordValue.doubleValue()) < 0.01)
			fieldFormat = new DecimalFormat("0.000000E0");
		else
			fieldFormat = new DecimalFormat("0.000000");
		}
		return fieldFormat.format(recordValue.doubleValue());
		
		
//		return String.valueOf(recordValue.doubleValue());
	}

}
