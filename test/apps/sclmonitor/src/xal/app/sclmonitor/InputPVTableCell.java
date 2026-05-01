package xal.app.sclmonitor;

import xal.ca.*;
import java.text.*;
import java.util.*;

public class InputPVTableCell implements IEventSinkValue {
	int m_row, m_col;

	Channel m_chan;

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

		// System.out.println("row = " + m_row + " col = " + m_col);
		if (recordValue == null) {
			return "null";
		}

		// convert rep rate index to String
		HashMap<Integer, String> repRateMap = new HashMap<Integer, String>(8);
		repRateMap.put(new Integer(0), "off");
		repRateMap.put(new Integer(1), "1 Hz");
		repRateMap.put(new Integer(2), "2 Hz");
		repRateMap.put(new Integer(3), "5 Hz");
		repRateMap.put(new Integer(4), "10 Hz");
		repRateMap.put(new Integer(5), "20 Hz");
		repRateMap.put(new Integer(6), "30 Hz");
		repRateMap.put(new Integer(7), "60 Hz");

		if (m_col == 1) {
			return repRateMap.get(new Integer(recordValue.intValue()));
		} else {
			// force the number display as the format of either "0.0000" or
			// "0.000E0" depending on the value
			NumberFormat fieldFormat = null;
			if (Math.abs(recordValue.doubleValue()) >= 10000.
					|| Math.abs(recordValue.doubleValue()) < 0.01)
				fieldFormat = new DecimalFormat("0.000000E0");
			else
				fieldFormat = new DecimalFormat("0.000000");

			return fieldFormat.format(recordValue.doubleValue());
		}

		// return String.valueOf(recordValue.doubleValue());
	}

}
