/*
 * MPSModelListener.java
 *
 * Created on Tue Feb 17 16:30:56 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.mpsclient;

import xal.tools.data.*;

import java.util.Date;


/**
 * MPSModelListener is the interface for listeners who want to receive MPS model events.
 *
 * @author  tap
 */
interface MPSModelListener {
	/**
	 * The status of MPS services have been updated.  Each record specifies information about 
	 * a single MPS service.
	 * @param model The MPS model posting the event
	 * @param records The records of every MPS service found on the local network.
	 */
	public void servicesChanged(MPSModel model, java.util.List<RemoteMPSRecord> records);
	
	
	/**
	 * The request handler associated with the specified record has checked for new status
	 * information from the remote service.
	 * @param record The request handler record for which the update has been made
	 * @param timestamp The timestamp of the check
	 */
	public void lastCheck(RemoteMPSRecord record, Date timestamp);
}

