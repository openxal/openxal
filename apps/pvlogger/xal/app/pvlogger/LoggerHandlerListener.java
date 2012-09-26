/*
 * LoggerHandlerListener.java
 *
 * Created on Thu Mar 18 09:10:30 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.pvlogger;

import xal.tools.data.GenericRecord;

import java.util.*;


/**
 * LoggerHandlerListener is the interface for receiving logger events
 *
 * @author  tap
 */
public interface LoggerHandlerListener {
	/**
	 * Notification that the logger handler's record has been updated
	 * @param source the logger handler whose record has been updated
	 * @param record the updated record
	 */
	public void recordUpdated(LoggerHandler source, GenericRecord record);
}

