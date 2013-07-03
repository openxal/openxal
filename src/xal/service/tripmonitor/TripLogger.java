//
//  TripLogger.java
//  xal
//
//  Created by Thomas Pelaia on 8/2/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.service.tripmonitor;

import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ArrayList;
import java.sql.*;

import xal.tools.data.*;


/** logs trips to persistent storage */
public class TripLogger {
	/** data label identifying the trip logger */
	final static public String DATA_LABEL = "TripLogger";
	
	/** timer which signals a log operation */
	final protected Timer LOG_TIMER;
	
	/** handles timer events */
	final protected TimerHandler TIMER_HANDLER;
	
	/** trip monitor filters */
	final protected List<TripMonitor> TRIP_MONITORS;
	
	
	/** Constructor */
	public TripLogger( final DataAdaptor adaptor, final List<TripMonitor> tripMonitors ) {
		TRIP_MONITORS = tripMonitors;
		
		final double loggingPeriod = adaptor.doubleValue( "loggingPeriod" );
		
		TIMER_HANDLER = new TimerHandler();
		final int delay = (int)toMillisecondsFromSeconds( loggingPeriod );
		LOG_TIMER = new Timer( delay, TIMER_HANDLER );
		LOG_TIMER.setRepeats( true );
	}
	
	
	/** determine if the logger is logging */
	public boolean isLogging() {
		return LOG_TIMER.isRunning();
	}
	
	
	/** start the logging */
	public void start() {
		LOG_TIMER.start();
	}
	
	
	/** stop the logging */
	public void stop() {
		LOG_TIMER.stop();
	}
	
	
	/** publish trips to the persistent storage */
	synchronized public void publishTrips() {
		try {
			final Connection connection = PersistentStore.connectionInstance();
			
			try {
				connection.setAutoCommit( false );				
				publishTrips( connection );
			}
			finally {
				connection.close();
			}
		}
		catch( Exception exception ) {
			exception.printStackTrace();
		}
	}
	
	
	/** publish trips to the persistent storage */
	protected void publishTrips( final Connection connection ) {
		try {
			for ( final TripMonitor tripMonitor : TRIP_MONITORS ) {
				final List<TripRecord> tripRecords = tripMonitor.getTripHistory();
				if ( tripRecords.size() > 0 ) {
					final PersistentStore persistentStore = tripMonitor.getPersistentStore();
					if ( persistentStore.publish( connection, tripRecords ) ) {
						tripMonitor.clearTripRecords( tripRecords );
					}
				}
			}
		}
		catch( Exception exception ) {
			exception.printStackTrace();
		}
	}
	
	
	/** convert milliseconds to seconds */
	private static double toSecondsFromMilliseconds( final long milliseconds ) {
		return ((double)milliseconds) / 1000.0;
	}
	
	
	/** convert seconds to milliseconds */
	private static long toMillisecondsFromSeconds( final double seconds ) {
		return (long)( 1000 * seconds );
	}
	
	
	
	/** class to handle timer events */
	protected class TimerHandler implements ActionListener {
		/** handle the event */
		public void actionPerformed( final ActionEvent event ) {
			publishTrips();
		}
	}
}
