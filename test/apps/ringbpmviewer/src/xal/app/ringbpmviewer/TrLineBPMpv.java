package xal.app.ringbpmviewer;

import java.awt.*;
import java.util.*;
import java.awt.event.*;

import xal.ca.*;
import xal.extension.widgets.plot.*;
import xal.extension.scan.MonitoredPV;
import xal.extension.scan.MonitoredPVEvent;
import xal.extension.scan.UpdatingEventController;

import xal.extension.widgets.plot.barchart.*;

/**
 *  The average transfer line BPM (x,y, or amplitude) values stack. It
 *  implements the BarColumn interface to show data on a bar chart.
 *
 *@author     shishlo
 */

public class TrLineBPMpv implements BarColumn {

	private Color barColor = null;

	private Vector<Double> dataStack = new Vector<Double>();
	private volatile int stackCapacity = 5;

	private volatile boolean switchedOn = false;

	private String trLineBPM_name = "None";

	private MonitoredPV mpv = null;

	private boolean listenToEPICS = false;

	static int trLineBPMsCounter = 0;

	//update controller for the redrawing the bar charts
	private UpdatingEventController uc = null;

	//update controller for the changing the set of bar columns in charts
	private UpdatingEventController ucContent = null;


	/**
	 *  Constructor for the TrLineBPMpv object
	 *
	 *@param  ucIn         The update controller for the redrawing the bar charts
	 *@param  ucContentIn  The update controller for the changing the set of bar
	 *      columns in charts
	 */
	public TrLineBPMpv(UpdatingEventController ucIn, UpdatingEventController ucContentIn) {
		uc = ucIn;
		ucContent = ucContentIn;

		mpv = MonitoredPV.getMonitoredPV("trLineBPM_" + trLineBPMsCounter);
		trLineBPMsCounter++;

		ActionListener updateListener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					measure((MonitoredPVEvent) e);
				}
			};
		mpv.addValueListener(updateListener);
		mpv.addStateListener(updateListener);
	}

	/**
	 *  Sets the switched-On attribute of the TrLineBPMpv object
	 *
	 *@param  switchedOn  The new switched-On value
	 */
	public void setSwitchedOn(boolean switchedOn) {
		this.switchedOn = switchedOn;
		if(ucContent != null) {
			ucContent.update();
		}
	}


	/**
	 *  Returns the switched-On boolean value
	 *
	 *@return    The switched-On value
	 */
	public boolean getSwitchedOn() {
		return switchedOn;
	}


	/**
	 *  This method calculates average value over the TBT array inside the certain
	 *  limits and updates the stack with history data.
	 *
	 *@param  mpvEvnt  The Parameter
	 */
	private void measure(MonitoredPVEvent mpvEvnt) {

		if(!listenToEPICS) {
			return;
		}
		double avgValue = 0.;

		ChannelRecord record = mpvEvnt.getChannelRecord();

		if(record != null) {
			avgValue = record.doubleValue();
		}

		dataStack.add(new Double(avgValue));

		if(dataStack.size() > stackCapacity) {
			for(int i = 0, n = dataStack.size() - stackCapacity; i < n; i++) {
				Object obj = dataStack.firstElement();
				dataStack.removeElement(obj);
			}
		}
		uc.update();
	}


	/**
	 *  Returns the stack capacity
	 *
	 *@return    The stack capacity value
	 */
	public int getStackCapacity() {
		return stackCapacity;
	}


	/**
	 *  Sets the stack capacity
	 *
	 *@param  stackCapacity  The new stack capacity value
	 */
	public void setStackCapacity(int stackCapacity) {
		this.stackCapacity = stackCapacity;
		if(dataStack.size() > stackCapacity) {
			for(int i = 0, n = dataStack.size() - stackCapacity; i < n; i++) {
				Object obj = dataStack.lastElement();
				dataStack.removeElement(obj);
			}
			uc.update();
		}
	}


	/**
	 *  Clear the memory data stack
	 */
	public void clearStack() {
		dataStack.clear();
		uc.update();
	}


	/**
	 *  Adds an external data value to the stack
	 *
	 *@param  value  The feature to be added to the Data attribute
	 */
	public void addValue(double value) {
		dataStack.add(new Double(value));
		if(dataStack.size() > stackCapacity) {
			for(int i = 0, n = dataStack.size() - stackCapacity; i < n; i++) {
				Object obj = dataStack.firstElement();
				dataStack.removeElement(obj);
			}
		}
	}

	/**
	 *  Returns the BPM name
	 *
	 *@return    The BPM name
	 */
	public String getBPMName() {
		return trLineBPM_name;
	}


	/**
	 *  Sets the BPM name
	 *
	 *@param  trLineBPM_name  The new BPM name
	 */
	public void setBPMName(String trLineBPM_name) {
		this.trLineBPM_name = trLineBPM_name;
	}


	/**
	 *  Returns the name of PV
	 *
	 *@return    The name of PV
	 */
	public String getNamePV() {
		return mpv.getChannelName();
	}


	/**
	 *  Sets the name of PV
	 *
	 *@param  pv_name  The new name of PV
	 */
	public void setNamePV(String pv_name) {
		mpv.setChannelNameQuietly(pv_name);
	}


	/**
	 *@return    Returns the history stack size.
	 */
	public int size() {
		int size = dataStack.size();
		size = Math.min(size, stackCapacity);
		return size;
	}


	/**
	 *  Returns true if user wants to see the line with this index
	 *
	 *@param  index  The index of the line inside the bar
	 *@return        True (or false) if user (does not ) wants to see the line with
	 *      this index
	 */
	public boolean show(int index) {
		return true;
	}


	/**
	 *  Returns true if user wants to see this column at all
	 *
	 *@return    True (or false) if user (does not ) wants to see the column
	 */
	public boolean show() {
		return switchedOn;
	}


	/**
	 *  Returns the value for the line hight inside the bar
	 *
	 *@param  index  The index of the line inside the bar
	 *@return        The value for the line hight inside the bar
	 */
	public double value(int index) {
		if(index < dataStack.size()) {
			Double dI = dataStack.get(index);
			return dI.doubleValue();
		}
		return 0.;
	}


	/**
	 *  Returns a marker for this bar in the bar chart
	 *
	 *@return    The string with marker
	 */
	public String marker() {
		return trLineBPM_name;
	}


	/**
	 *  Sets the "listen to EPICS" attribute
	 *
	 *@param  listenToEPICS  The new listen to EPICS boolean value
	 */
	public void setListenToEPICS(boolean listenToEPICS) {
		this.listenToEPICS = listenToEPICS;

		if(listenToEPICS) {
			mpv.startMonitor();
		} else {
			mpv.stopMonitor();
		}
	}


	/**
	 *  Returns the color for the line inside the bar
	 *
	 *@param  index  The index of the line inside the bar
	 *@return        The value for the line hight inside the bar
	 */
	public Color getColor(int index) {
		return barColor;
	}


	/**
	 *  Sets the color attribute of the TrLineBPMpv object
	 *
	 *@param  barColor  The new color value
	 */
	public void setColor(Color barColor) {
		this.barColor = barColor;
	}

	/**
	 *  Removes the monitored PV.
	 */
	protected void finalize() {
		MonitoredPV.removeMonitoredPV(mpv);
	}

}

