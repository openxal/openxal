/*
 *  LoggerBuffer.java
 *
 *  Created on Tue Sep 14 12:58:35 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.application;

import java.util.logging.*;
import java.util.*;

import xal.tools.messaging.MessageCenter;

/**
 * LoggerBuffer
 *
 * @author   tap
 * @since    Sep 14, 2004
 */
class LoggerBuffer extends Handler {
	/** root handler */
	protected static LoggerBuffer _rootHandler;

	/** list of captured records */
	protected List<LogRecord> _records;

	/** message center for dispatching messages to registered listeners */
	protected MessageCenter _messageCenter;

	/** proxy which forwards events to registered listeners */
	protected LoggerBufferListener _eventProxy;

	/** static constructor */
	static {
		setupRootLogger();
	}


	/** Constructor */
	public LoggerBuffer() {
		_messageCenter = new MessageCenter( "Logger Buffer" );
		_eventProxy = _messageCenter.registerSource( this, LoggerBufferListener.class );

		_records = new ArrayList<LogRecord>();
	}


	/**
	 * Add the specified listener to receive LoggerBufferListener events from this instance.
	 *
	 * @param listener  The listener to register to receive events from this instance
	 */
	public void addLoggerBufferListener( final LoggerBufferListener listener ) {
		_messageCenter.registerTarget( listener, this, LoggerBufferListener.class );
		synchronized( _records ) {
			listener.recordsChanged( this, new ArrayList<LogRecord>( _records ) );
		}
	}


	/**
	 * Remove the specified listener from receiving LoggerBufferListener events from this instance.
	 *
	 * @param listener  The listener to remove from receiving events from this instance.
	 */
	public void removeLoggerBufferListener( final LoggerBufferListener listener ) {
		_messageCenter.removeTarget( listener, this, LoggerBufferListener.class );
	}


	/**
	 * Initialize the root logger by creating a logger buffer handler and adding it to the root
	 * logger.
	 */
	public static void setupRootLogger() {
		if ( _rootHandler == null ) {
			_rootHandler = new LoggerBuffer();
			_rootHandler.setLevel( Level.FINEST );
			Logger.getLogger( "" ).addHandler( _rootHandler );
		}
	}


	/**
	 * Get the root handler.
	 *
	 * @return   The rootHandler value
	 */
	public static LoggerBuffer getRootHandler() {
		return _rootHandler;
	}


	/** Flush the buffer. Presently does nothing. */
	public void flush() {
	}
	
	
	/** Clear the log */
	public void clear() {
		synchronized( _records ) {
			_records.clear();
			_eventProxy.recordsChanged( this, Collections.<LogRecord>emptyList() );
		}
	}


	/**
	 * Close the buffer. Presently does nothing.
	 *
	 * @exception SecurityException  presently doesn't get thrown
	 */
	public void close() throws SecurityException {
	}


	/**
	 * Record the new log record.
	 *
	 * @param record  the new log record
	 */
	public void publish( final LogRecord record ) {
		synchronized ( _records ) {
			_records.add( record );
			_eventProxy.recordsChanged( this, new ArrayList<LogRecord>(_records) );
		}
	}
}

