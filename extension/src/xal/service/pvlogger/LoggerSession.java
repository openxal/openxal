/*
 * LoggerSession.java
 *
 * Created on Thu Dec 04 09:01:51 EST 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.service.pvlogger;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.ca.Channel;
import xal.ca.ChannelTimeRecord;
import xal.tools.messaging.MessageCenter;



/**
 * LoggerSession manages a session of logging machine state.  One can create an instance to log the current machine
 * state either on demand or periodically.
 *
 * @author  tap
 */
public class LoggerSession {
	/** initial timer delay */
	protected final int INITIAL_DELAY = 1000;
	
	/** default logging period in seconds */
	static protected final double DEFAULT_LOGGING_PERIOD = 5.0;
	
	/** publisher of snapshots to the persistent store */
	protected SnapshotPublisher SNAPSHOT_PUBLISHER;
	
	/** latest snapshot taken which may or may not have been published */
	protected volatile MachineSnapshot _latestMachineSnapshot;
	
	// messaging
	final protected MessageCenter MESSAGE_CENTER;
	final protected LoggerChangeListener EVENT_PROXY;
	
	// state variables
	protected ChannelGroup _group;
	final protected Timer LOG_TIMER;
	protected TimerTask _logTask;
	protected double _loggingPeriod;	// logging period in seconds
	protected boolean _enabled;
	
	
	/**
	 * LoggerSession constructor
	 * @param group Group of channels to log. 
	 * @param publisher The snapshot publisher.
	 */
	public LoggerSession( final ChannelGroup group, final SnapshotPublisher publisher ) {
		MESSAGE_CENTER = new MessageCenter( "PV Logger" );
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, LoggerChangeListener.class );
		
		SNAPSHOT_PUBLISHER = publisher;
		
		LOG_TIMER = new Timer();
		
		_enabled = false;
		setChannelGroup( group );
	}
	
	
	/**
	 * Add a logger change listener to receive logger change events.
	 * @param listener The listener of the logger change events.
	 */
	public void addLoggerChangeListener( final LoggerChangeListener listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, LoggerChangeListener.class );
	}
	
	
	/**
	 * Remove a logger change listener from receiving logger change events.
	 * @param listener The listener of the logger change events.
	 */
	public void removeLoggerChangeListener( final LoggerChangeListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, LoggerChangeListener.class );
	}
	
	
	/** Resume periodic logging with the most recent settings if enabled. */
	public void resumeLogging() {
		if ( isEnabled() ) {
			disposeLoggingTask();
			_logTask = newLoggingTask();
			final long delay = (long)( _loggingPeriod * 1000 );
			LOG_TIMER.schedule( _logTask, delay, delay );
			EVENT_PROXY.stateChanged( this, LoggerChangeListener.LOGGING_CHANGED );
		}
	}
	
	
	/** Start periodically logging machine state to the persistent storage. */
	public void startLogging() {
		final double loggingPeriod = getLoggingPeriod();
		final String message = "Start logging \"" + _group.getLabel() + "\" with period " + loggingPeriod + " seconds";
		System.out.println( message );
		Logger.getLogger("global").log( Level.INFO, message );
		resumeLogging();
	}
	
	
	/**
	 * Start periodically logging machine state to the persistent storage.
	 * @param period The period in seconds between events where we take and store machine snapshots.
	 */
	public void startLogging( final double period ) {
		setLoggingPeriod( period );
		resumeLogging();
	}
	
	
	/** Stop the periodic machine state logging. */
	public void stopLogging() {
		if ( _logTask != null ) {
			disposeLoggingTask();
			EVENT_PROXY.stateChanged( this, LoggerChangeListener.LOGGING_CHANGED );
		}
	}
	
	
	/**
	 * Reveal whether the logger is scheduled to run periodically
	 * @return true if the logger is scheduled to run periodically or false otherwise
	 */
	public boolean isLogging() {
		return _logTask != null;
	}
	
	
	/**
	 * Set the period between events where we take and store machine snapshots.
	 * @param period The period in seconds between events where we take and store machine snapshots.
	 */
	public void setLoggingPeriod( final double period ) {
		setEnabled( period > 0 );
		boolean isRunning = isLogging();
		if ( period != _loggingPeriod ) {
			_loggingPeriod = period;
			if ( isRunning && _logTask != null ) {
				disposeLoggingTask();
				resumeLogging();
			}
			EVENT_PROXY.stateChanged( this, LoggerChangeListener.LOGGING_PERIOD_CHANGED );
		}
	}
	
	
	/**
	 * Get the loggin period.
	 * @return The period in seconds between events where we take and store machine snapshots.
	 */
	public double getLoggingPeriod() {
		return _loggingPeriod;
	}
	
	
	/**
	 * Determine whether this logger session is enabled
	 * @return true if this logger session is enabled and false if not
	 */
	public boolean isEnabled() {
		return _enabled;
	}
	
	
	/**
	 * Set whether this session should be enabled
	 * @param enable true to enable this session and false to disable it 
	 */
	protected void setEnabled( final boolean enable ) {
		_enabled = enable;
		if ( isLogging() )  stopLogging();
		EVENT_PROXY.stateChanged( this, LoggerChangeListener.ENABLE_CHANGED );
	}
	
	
	/**
	 * Get the active channel group for this session
	 * @return the channel group
	 */
	public ChannelGroup getChannelGroup() {
		return _group;
	}
	
	
	/**
	 * Set the channel group for this logger session
	 * @param group the new channel group for this logger session
	 */
	public void setChannelGroup( final ChannelGroup group ) {
		final boolean shouldLog = isLogging();
		if ( shouldLog )  stopLogging();
		
		final ChannelGroup oldGroup = _group;
		if ( oldGroup != null && group != oldGroup ) {
			oldGroup.dispose();
		}
		else if ( group != null ) {
			group.requestConnections();
			setLoggingPeriod( group.getDefaultLoggingPeriod() );
		}
		
		if ( group == null ) {
			setLoggingPeriod( DEFAULT_LOGGING_PERIOD );
		}
		
		_group = group;
		if ( shouldLog )  resumeLogging();
		EVENT_PROXY.stateChanged( this, LoggerChangeListener.GROUP_CHANGED );
	}
	
	
	/**
	 * Get the channels which we are attempting to monitor and log
	 * @return a collection of the channels we wish to monitor and log
	 */
	public Collection<Channel> getChannels() {
		return _group.getChannels();
	}
	
	
	/**
	 * Get the latest machine snapshot which may or may not have been published
	 * @return the latest machine snapshot
	 */
	public MachineSnapshot getLatestMachineSnapshot() {
		return _latestMachineSnapshot;
	}
	
	
	/**
	 * Take a snapshot and publish it immediately
	 * @return the published snapshot
	 */
	final public MachineSnapshot takeAndPublishSnapshot() {
		return takeAndPublishSnapshot( "" );
	}
	
	
	/**
	 * Take a snapshot and publish it immediately
	 * @param comment machine snapshot comment
	 * @return the published snapshot
	 */
	final public MachineSnapshot takeAndPublishSnapshot( final String comment ) {
		final MachineSnapshot machineSnapshot = takeSnapshot();
		machineSnapshot.setComment( comment );
		publishSnapshot( machineSnapshot );
		return machineSnapshot;
	}
	
	
	/**
	 * Take a snapshot and schedule it for publication
	 * @return the scheduled snapshot
	 */
	final protected MachineSnapshot takeAndScheduleSnapshotForPublication() {
		final MachineSnapshot machineSnapshot = takeSnapshot();
		SNAPSHOT_PUBLISHER.scheduleSnapshotPublication( machineSnapshot );
		return machineSnapshot;
	}
	
	
	/**
	 * Take a snapshot of the current machine state.
	 * @return A snapshot of the current machine state.
	 */
	final public MachineSnapshot takeSnapshot() {
		final ChannelWrapper[] channelWrappers = _group.getChannelWrappers();
		MachineSnapshot machineSnapshot = new MachineSnapshot( channelWrappers.length );
		machineSnapshot.setType( _group.getLabel() );
		for ( int index = 0 ; index < channelWrappers.length ; index++ ) {
			ChannelWrapper channelWrapper = channelWrappers[index];
			if ( channelWrapper == null )  continue;
			ChannelTimeRecord record = channelWrapper.getRecord();
			if ( record == null )  continue;
			ChannelSnapshot snapshot = new ChannelSnapshot( channelWrapper.getPV(), record );
			machineSnapshot.setChannelSnapshot( index, snapshot );
		}
		
		_latestMachineSnapshot = machineSnapshot;
		EVENT_PROXY.snapshotTaken( this, machineSnapshot );
		return machineSnapshot;
	}
	
	
	/**
	 * Publish the machine snapshot to the persistent storage.
	 * @param machineSnapshot The machine snapshot to publish.
	 */
	final public void publishSnapshot( final MachineSnapshot machineSnapshot ) {
		SNAPSHOT_PUBLISHER.scheduleSnapshotPublication( machineSnapshot );
		SNAPSHOT_PUBLISHER.publishSnapshots();
		EVENT_PROXY.snapshotPublished( this, machineSnapshot );
	}
	
	
	/** dispose of the logging task */
	protected void disposeLoggingTask() {
		if ( _logTask != null ) {
			_logTask.cancel();
		}
		LOG_TIMER.purge();
		_logTask = null;
	}
	
	
	/** get a new timer task for periodic logging */
	final protected TimerTask newLoggingTask() {
		return new TimerTask() {
			final public void run() {
				// must catch exceptions to avoid the timer stopping
				try {
					final MachineSnapshot machineSnapshot = takeSnapshot();
					SNAPSHOT_PUBLISHER.scheduleSnapshotPublication( machineSnapshot );
				}
				catch( Exception exception ) {
					Logger.getLogger( "global" ).log( Level.WARNING, "Error publishing snapshot: ", exception );
					System.err.println( exception );
				}				
			}
		};
	}
}

