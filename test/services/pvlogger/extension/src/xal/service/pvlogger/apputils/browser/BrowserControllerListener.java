/*
 * BrowserControllerListener.java
 *
 * Created on Fri Apr 30 11:26:20 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.service.pvlogger.apputils.browser;

import xal.service.pvlogger.*;

import java.util.*;


/**
 * BrowserControllerListener is the interface for receivers of events from the controller.
 *
 * @author  tap
 */
public interface BrowserControllerListener {
	/** 
	 * event indicating that a snapshot has been selected
	 * @param controller The controller managing selection state
	 * @param snapshot The snapshot that has been selected
	 */
	public void snapshotSelected(BrowserController controller, MachineSnapshot snapshot);
	
	
	/**
	 * event indicating that the selected channel group changed
	 * @param source the browser controller sending this notice
	 * @param newGroup the newly selected channel group
	 */
	public void selectedChannelGroupChanged(BrowserController source, ChannelGroup newGroup);
	
	
	/**
	 * Event indicating that the selected signals have changed
	 * @param source the controller sending the event
	 * @param selectedSignals the new collection of selected signals
	 */
	public void selectedSignalsChanged(BrowserController source, Collection<String> selectedSignals);
}

