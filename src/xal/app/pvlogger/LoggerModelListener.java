/*
 * LoggerModelListener.java
 *
 * Created on Fri Oct 10 17:09:56 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.pvlogger;

import java.util.List;


/**
 * LoggerModelListener is the interface for listeners who want to receive logger model events.
 *
 * @author  tap
 */
interface LoggerModelListener {
	/**
	 * The status of a logger has been updated along with its client side record.
	 * @param source The source of the event
	 * @param record The record that has been updated.
	 */
	public void newLoggerStatus( LoggerModel source, RemoteLoggerRecord record );
	
	
	/**
	 * The list of loggers has changed.
	 * @param model The source of the event
	 * @param records The new logger records.
	 */
	public void loggersChanged( LoggerModel model, List<RemoteLoggerRecord> records );
}

