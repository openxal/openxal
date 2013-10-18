/*
 * BrowserModelListener.java
 *
 * Created on Fri Feb 20 10:14:28 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.service.pvlogger.apputils.browser;

import xal.service.pvlogger.ChannelGroup;
import xal.service.pvlogger.MachineSnapshot;


/**
 * BrowserModelListener is a notification interface for browser model events.
 *
 * @author  tap
 */
public interface BrowserModelListener {
	/**
	 * The model's connection has changed
	 * @param model The model whose connection changed
	 */
	public void connectionChanged(BrowserModel model);
	
	
	/**
	 * event indicating that the selected channel group changed
	 * @param model the source sending this notice
	 * @param newGroup the newly selected channel group
	 */
	public void selectedChannelGroupChanged(BrowserModel model, ChannelGroup newGroup);
	
	
	/**
	 * event indicating that machine snapshots have been fetched.
	 * @param model the source of this event
	 * @param snapshots the fetched snapshots
	 */
	public void machineSnapshotsFetched(BrowserModel model, MachineSnapshot[] snapshots);
}

