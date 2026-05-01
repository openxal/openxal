package xal.app.diagtiming;

import xal.ca.*;

public class ComboBoxPVCell extends InputPVTableCell implements IEventSinkValue {
	int m_row, m_col;

	Channel m_chan;

	boolean isUpdating = true;

	String cellValue = "";
	
	BPMPane myPane;

	public ComboBoxPVCell(Channel p_channel, int p_row, int p_col, BPMPane bpmPane) {
		m_row = p_row;
		m_col = p_col;
		m_chan = p_channel;
		myPane = bpmPane;
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

		if (recordValue == null) {
			return "null";
		}
//		System.out.println(String.valueOf(recordValue.intValue()) + " : " +myPane.trigMap1.get(new Integer(recordValue.intValue())));
		if (m_col == 3)
			return myPane.trigMap1.get(new Integer(recordValue.intValue()));	
		else if (m_col == 5 || m_col == 8 || m_col == 11 || m_col == 14)
			return myPane.gainMap1.get(new Integer(recordValue.intValue()));
		else if (m_col == 6 || m_col == 9 || m_col == 12 || m_col == 15)
			return myPane.analysisMap1.get(new Integer(recordValue.intValue()));
		else if (m_col == 16)
			return myPane.operModeMap1.get(new Integer(recordValue.intValue()));
		
		return "null";
	}
}
