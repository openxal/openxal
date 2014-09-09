/*
 *  MonitoredArrayPV.java
 *
 *  Created on July 7, 2005, 10:25 AM
 */
package xal.extension.scan;

import java.util.*;
import java.awt.event.*;

import xal.ca.*;

/**
 *  This class keeps a reference to the PV with array data and listens to the
 *  data change.
 *
 *@author     shishlo
 *created    October 31, 2005
 *@version    July 29, 2005
 */

public class MonitoredArrayPV {

	private Object syncObj = new Object();

	private double[] vals = new double[0];

	private MonitoredPV mpv = null;

	private ActionListener updateListener = null;

	private static int nextIndex = 0;

	private volatile boolean switchOn = true;


	/**
	 *  Constructor for the MonitoredArrayPV object.
	 */
	public MonitoredArrayPV() {
		updateListener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					synchronized(syncObj) {

						MonitoredPVEvent mpvEvt = (MonitoredPVEvent) e.getSource();

						if(!switchOn) {
							if(vals.length != 0) {
								vals = new double[0];
							}
							return;
						}

						if(mpv != null && mpv.isGood()) {
							ChannelRecord record = mpvEvt.getChannelRecord();
							double[] localVals = new double[0];

							if(record != null) {
								localVals = record.doubleArray();
							}

							if(localVals.length != vals.length) {
								vals = new double[localVals.length];
							}
							for(int i = 0; i < localVals.length; i++) {
								vals[i] = localVals[i];
							}
						} else {
							vals = new double[0];
						}
					}
				}
			};

		mpv = MonitoredPV.getMonitoredPV("MonitoredArrayPV_" + nextIndex);
		mpv.addValueListener(updateListener);
		mpv.addStateListener(updateListener);
		nextIndex++;
	}


	/**
	 *  Returns the array listener that is one of the inner MonitoredPV object
	 *  listeners
	 *
	 *@return    The array listener object
	 */
	public ActionListener getArrayListener() {
		return updateListener;
	}


	/**
	 *  Returns the reference to the data array. The operations with this array
	 *  should be synchronized by syncObj that can be received by using
	 *  getSyncObject() method.
	 *
	 *@return    The data array
	 */
	public double[] getValues() {
		return vals;
	}


	/**
	 *  Sets the channel name.
	 *
	 *@param  chanName  The new channel name.
	 */
	public void setChannelName(String chanName) {
		mpv.setChannelName(chanName);
	}


	/**
	 *  Sets the channel name without creating the monitor.
	 *
	 *@param  chanName  The new channel name.
	 */
	public void setChannelNameQuietly(String chanName) {
		mpv.setChannelNameQuietly(chanName);
	}


	/**
	 *  Returns the channel name
	 *
	 *@return    The channel name.
	 */
	public String getChannelName() {
		return mpv.getChannelName();
	}


	/**
	 *  Sets the channel.
	 *
	 *@param  chIn  The new channel.
	 */
	public void setChannel(Channel chIn) {
		mpv.setChannel(chIn);
	}


	/**
	 *  Sets the channel without creating the monitor.
	 *
	 *@param  chIn  The new channel.
	 */
	public void setChannelQuietly(Channel chIn) {
		mpv.setChannelQuietly(chIn);
	}


	/**
	 *  Returns the channel.
	 *
	 *@return    The channel.
	 */
	public Channel getChannel() {
		return mpv.getChannel();
	}


	/**
	 *  Returns the syncObj reference. It is used for synchronization.
	 *
	 *@return    The syncObj reference
	 */
	public Object getSyncObject() {
		return syncObj;
	}


	/**
	 *  Sets the syncObject attribute of the MonitoredArrayPV object. This method
	 *  has to be called from UpdatingController class only.
	 *
	 *@param  syncObj  The new syncObject value
	 */
	protected void setSyncObject(Object syncObj) {
		synchronized(syncObj) {
			synchronized(this.syncObj) {
				this.syncObj = syncObj;
			}
		}
	}


	/**
	 *  Returns true if the update monitor is working. By default it is On.
	 *
	 *@return    true if the update monitor is working, false otherwise.
	 */
	public boolean getSwitchOn() {
		return switchOn;
	}


	/**
	 *  Sets the switch on key for monitoring.
	 *
	 *@param  switchOn  The new switchOn value
	 */
	public void setSwitchOn(boolean switchOn) {
		this.switchOn = switchOn;
		synchronized(syncObj) {
			if(!switchOn) {
				if(vals.length != 0) {
					vals = new double[0];
				}
				mpv.stopMonitor();
			}
		}
		if(switchOn) {
			mpv.startMonitor();
		}
	}


	/**
	 *  Gets the monitoredPV attribute of the MonitoredArrayPV object
	 *
	 *@return    The monitoredPV value
	 */
	public MonitoredPV getMonitoredPV() {
		return mpv;
	}


	/**
	 *  Removes the monitored PV.
	 * @throws Throwable 
	 */
	protected void finalize() throws Throwable {
		try {
			MonitoredPV.removeMonitoredPV(mpv);
		}
		finally {
			super.finalize();
		}
	}

}

