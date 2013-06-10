/*
 * AppViewerListener.java
 *
 * Created on Fri Oct 10 17:09:56 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.appviewer;

import xal.tools.data.GenericRecord;


/**
 * AppViewerListener is the interface for listeners who want to receive application viewer events.
 *
 * @author  tap
 */
interface AppViewerListener {
	/**
	 * The list of applications has changed.
	 * @param source the model posting the event
	 * @param records The records of every application found on the local network.
	 */
	public void applicationsChanged(AppViewerModel source, java.util.List<GenericRecord> records);
	
	
	/**
	 * An application's record has been updated
	 * @param source the model posting the event
	 * @param record the updated record
	 */
	public void applicationUpdated(AppViewerModel source, GenericRecord record);
	
	
	/**
	 * Notification that a remote message exception has occurred
	 * @param source the model posting the event
	 * @param handler the handler for which the remote exception occurred
	 * @param exception the remote exception that occurred
	 */
	public void remoteException(AppViewerModel source, AppHandler handler, Exception exception);
}

