package xal.app.diagtiming;

import java.util.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;

import xal.ca.*;

public class WSPane extends JPanel implements ConnectionListener,
ActionListener {
	static final long serialVersionUID = 0;

	final String[] columnNames = { "WS", "Delay (us)", "Delay_Set (turns)" };

	String[] wsNames;

	private HashMap<String, Vector<InputPVTableCell>> monitorQueues = new HashMap<String, Vector<InputPVTableCell>>();

	/** List of the monitors */
	final Vector<Monitor> mons = new Vector<Monitor>();

	/** ConnectionListener interface */
	public void connectionMade(Channel aChannel) {
		connectMons(aChannel);
	}

	/** ConnectionListener interface */
	public void connectionDropped(Channel aChannel) {
	}

	/** internal method to connect the monitors */
	private void connectMons(Channel p_chan) {
		Vector<InputPVTableCell> chanVec;

		try {
			chanVec = getChannelVec(p_chan);
			for (int i = 0; i < chanVec.size(); i++) {
				mons.add(p_chan.addMonitorValue( chanVec
						.elementAt(i), Monitor.VALUE));
			}
			chanVec.removeAllElements();

		} catch (ConnectionException e) {
			System.out.println("Connection Exception");
		} catch (MonitorException e) {
			System.out.println("Monitor Exception");
		}
	}

	/** get the list of table cells monitoring the prescibed channel */
	private Vector<InputPVTableCell> getChannelVec(Channel p_chan) {
		if (!monitorQueues.containsKey(p_chan.channelName()))
			monitorQueues.put(p_chan.channelName(), new Vector<InputPVTableCell>());

		return monitorQueues.get(p_chan.channelName());
	}

	public void actionPerformed(ActionEvent e) {
		
	}
}
