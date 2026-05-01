/*
 * LoggerSessionHandler.java
 *
 * Created on Tue Jun 01 16:17:44 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.pvlogger;

import xal.service.pvlogger.*;
import xal.tools.data.*;
import xal.tools.dispatch.DispatchQueue;
import xal.extension.service.*;
import xal.tools.messaging.MessageCenter;

import java.util.*;


/**
 * LoggerSessionHandler handles requests for a single remote logger session model.
 * @author  tap
 * @since Jun 01, 2004
 */
public class LoggerSessionHandler {
	protected String _groupType;
	protected RemoteLogging _remoteProxy;

	/** serial queue on which updates are scheduled */
	private final DispatchQueue UPDATE_QUEUE;

	// messaging
	protected final MessageCenter _messageCenter;
	protected final LoggerSessionListener _postProxy;
	
	// logger state
	protected String _latestSnapshotDump;
	protected Date _latestSnapshotTimestamp;
	protected List<ChannelRef> _channelRefs;
	protected volatile double _loggingPeriod;
	protected volatile boolean _isLogging;
	
	// event timestamps
	protected Date _lastLoggerEventTime;
	protected Date _lastChannelEventTime;
	
	
	/**
	 * Constructor
	 */
	public LoggerSessionHandler( final String groupType, final RemoteLogging remoteProxy ) {
		_groupType = groupType;
		_remoteProxy = remoteProxy;

		UPDATE_QUEUE = DispatchQueue.createSerialQueue( "Update Queue" );
		
		_messageCenter = new MessageCenter( "Logger Session" );
		_postProxy = _messageCenter.registerSource( this, LoggerSessionListener.class );
		
		_latestSnapshotTimestamp = null;
		_latestSnapshotDump = "";
		
		_lastLoggerEventTime = new Date(0);
		_lastChannelEventTime = new Date(0);
		
		_channelRefs = new ArrayList<ChannelRef>();
		_isLogging = false;
		_loggingPeriod = 0;
	}


	/** dispose of the update queue */
	protected void finalize() throws Throwable {
		try {
			UPDATE_QUEUE.dispose();
		}
		finally {
			super.finalize();
		}
	}
	
	
	/**
	 * Add a listener of logger session handler events from this handler
	 * @param listener the listener to add
	 */
	public void addLoggerSessionListener(LoggerSessionListener listener) {
		_messageCenter.registerTarget(listener, this, LoggerSessionListener.class);
	}
	
	
	/**
	 * Remove the listener of logger session handler events from this handler
	 * @param listener the listener to remove
	 */
	public void removeLoggerSessionListener(LoggerSessionListener listener) {
		_messageCenter.removeTarget(listener, this, LoggerSessionListener.class);
	}
	
	
	/**
	 * Get the remote proxy managed by this handler
	 * @return the remote proxy
	 */
	public RemoteLogging getRemoteProxy() {
		return _remoteProxy;
	}
	
	
	/**
	 * Update the record with the current information from the remote application.  Try to get a lock
	 * for updating data from the remote application.  If the lock is unsuccessful simply return 
	 * false, otherwise perform the update.
	 * @return true if the data was successfully updated and false if not.
	 */
	public boolean update() {
		final List<Map<String,Object>> channelTables = new ArrayList<>();

		UPDATE_QUEUE.dispatchSync( new Runnable() {
			public void run() {
				try {
					Date lastLoggerEventTime = _remoteProxy.getLastLoggerEventTime(_groupType);
					if ( !lastLoggerEventTime.equals( _lastLoggerEventTime ) ) {
						boolean hasUpdate = false;

						final boolean isLogging = _remoteProxy.isLogging(_groupType);
						if ( isLogging != _isLogging ) {
							_isLogging = isLogging;
							hasUpdate = true;
						}

						final double loggingPeriod = _remoteProxy.getLoggingPeriod( _groupType );
						if ( loggingPeriod != _loggingPeriod ) {
							_loggingPeriod = loggingPeriod;
							hasUpdate = true;
						}

						if (hasUpdate) {
							_postProxy.loggerSessionUpdated( LoggerSessionHandler.this );
						}

						_latestSnapshotTimestamp = _remoteProxy.getTimestampOfLastPublishedSnapshot( _groupType );
						_latestSnapshotDump = _remoteProxy.getLastPublishedSnapshotDump( _groupType );
						_lastLoggerEventTime = lastLoggerEventTime;
						_postProxy.snapshotPublished( LoggerSessionHandler.this, _latestSnapshotTimestamp, _latestSnapshotDump );
					}

					final Date lastChannelEventTime = _remoteProxy.getLastChannelEventTime( _groupType );
					if ( !lastChannelEventTime.equals(_lastChannelEventTime) ) {
						channelTables.addAll( _remoteProxy.getChannels( _groupType ) );
						_lastChannelEventTime = lastChannelEventTime;
						processChannels( channelTables );
					}
				}
				catch(Exception exception) {
					System.err.println( "Got an update exception..." );
					System.err.println( exception );
				}
			}
		});

		// these postings need to happen outside of the blocking update queue to avoid deadlock due to callbacks
		if ( !channelTables.isEmpty() ) {
			_postProxy.channelsChanged( this, _channelRefs );
		}
		_postProxy.loggerSessionUpdated( this );

		return true;
	}
	
	
	/**
	 * Process the channel information.  The channelTables is a list of tables each of which
	 * provides the channel PV and channel connection status.  For convenience a channel ref 
	 * is constructed for each such channel table.
	 * @param channelTables the list of channel information about the remote logger's channels
	 */
	private void processChannels( final List<Map<String,Object>> channelTables ) {
		_channelRefs = new ArrayList<ChannelRef>( channelTables.size() );

		for ( final Map<String,Object> channelTable : channelTables ) {
			String pv = (String)channelTable.get( RemoteLogging.CHANNEL_SIGNAL );
			Boolean connected = (Boolean)channelTable.get( RemoteLogging.CHANNEL_CONNECTED );
			_channelRefs.add( new ChannelRef( pv, connected ) );
		}
	}
	
	
	/** get the group associated with this logging session */
	public String getGroupType() {
		return _groupType;
	}
	
	
	/**
	 * Get the list of channel references which carry the channel PV and channel connection
	 * statuse information about the remote logger's channels.
	 * @return the list of channel references
	 */
	public List<ChannelRef> getChannelRefs() {
		return _channelRefs;
	}
	
	
	/**
	 * Get the dump of the last published snapshot
	 * @return the dump of the last published snapshot
	 */
	public String getLastPublishedSnapshotDump() {
		return _latestSnapshotDump;
	}
	
	
	/**
	 * Get the timestamp of the last published snapshot
	 * @return the timestamp of the last published snapshot
	 */
	public Date getTimestampOfLastPublishedSnapshot() {
		return _latestSnapshotTimestamp;
	}
	
	
	/**
	 * Determine if the logger is running.
	 * @return true if the logger is running and false if not.
	 */
	public boolean isLogging() {
		return _isLogging;
	}
	
	
	/**
	 * Get the logging period.
	 * @return the logging period in units of seconds
	 */
	public double getLoggingPeriod() {
		return _loggingPeriod;
	}
	
	
	/**
	 * Set the logging period of the remote logger
	 * @param period The period in seconds to set for remote logging
	 */
	public void setLoggingPeriod(double period) {
		if ( _remoteProxy != null ) {
			try {
				_remoteProxy.setLoggingPeriod(_groupType, period);
			}
			catch(RemoteMessageException exception) {
				System.err.println( "Remote exception while trying to set the logging period..." );
				exception.printStackTrace();
			}
		}
	}
	
	
	/** reload the corresponding logger session from the database */
	public boolean reloadFromDatabase() {
		if ( _remoteProxy != null ) {
			try {
				final boolean status = _remoteProxy.reloadLoggerSession( _groupType );
				update();
				return status;
			}
			catch(RemoteMessageException exception) {
				System.err.println( "Remote exception while trying to set the logging period..." );
				exception.printStackTrace();
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	
	/** take and publish a snapshot for this session's group and with the specified comment */
	public int takeAndPublishSnapshot( final String comment ) {
		if ( _remoteProxy != null ) {
			try {
				return _remoteProxy.takeAndPublishSnapshot( _groupType, comment );
			}
			catch(RemoteMessageException exception) {
				System.err.println( "Remote exception while trying to set the logging period..." );
				exception.printStackTrace();
				return 0;
			}
		}
		else {
			return 0;
		}
	}
}

