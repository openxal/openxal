/*
 *  LoggerBufferListener.java
 *
 *  Created on Wed Sep 15 09:14:25 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.application;

import java.util.*;
import java.util.logging.*;


/**
 * LoggerBufferListener
 *
 * @author   tap
 * @since    Sep 15, 2004
 */
interface LoggerBufferListener {
	/**
	 * Event indicating that the records in the logger buffer have changed.
	 * 
	 * @param buffer   the buffer whose records have changed
	 * @param records  the new records in the buffer
	 */
	public void recordsChanged( LoggerBuffer buffer, List<LogRecord> records );
}

