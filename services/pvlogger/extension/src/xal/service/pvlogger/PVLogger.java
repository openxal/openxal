//
//  PVLogger.java
//  xal
//
//  Created by Pelaia II, Tom on 10/18/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.service.pvlogger;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import xal.tools.ResourceManager;
import xal.tools.data.DataAdaptor;
import xal.tools.database.ConnectionDictionary;
import xal.tools.database.DBConfiguration;
import xal.tools.xml.XmlDataAdaptor;


/** Provides a public interface to the PV Logger package */
public class PVLogger {
	/** database store */
	final protected PersistentStore PERSISTENT_STORE;

	/** snapshot publisher */
	final protected SnapshotPublisher SNAPSHOT_PUBLISHER;

	/** connection dictionary */
	protected ConnectionDictionary _connectionDictionary;

	/** logger sessions keyed by channel group ID */
	protected Map<String,LoggerSession> LOGGER_SESSIONS;

	/** current database connection */
	protected Connection _connection;


	/** Primary Constructor */
	public PVLogger( final ConnectionDictionary connectionDictionary ) {
		LOGGER_SESSIONS = new HashMap<String,LoggerSession>();

		URL configurationURL = null;
		DBConfiguration dbConfig = DBConfiguration.getInstance();
		if (dbConfig != null) configurationURL = dbConfig.getSchemaURL("pvlogger");
		if (configurationURL == null) configurationURL = ResourceManager.getResourceURL( getClass(), "configuration.xml" );
		final DataAdaptor configurationAdaptor = XmlDataAdaptor.adaptorForUrl( configurationURL, false ).childAdaptor( "Configuration" );

		final DataAdaptor persistentStoreAdaptor = configurationAdaptor.childAdaptor( "persistentStore" );
		PERSISTENT_STORE = new PersistentStore( persistentStoreAdaptor );

		final DataAdaptor publisherAdaptor = configurationAdaptor.childAdaptor( "publisher" );
		SNAPSHOT_PUBLISHER = new SnapshotPublisher( publisherAdaptor, PERSISTENT_STORE, connectionDictionary );

		setConnectionDictionary( connectionDictionary );
	}
	
	
	/** Constructor */
	public PVLogger() {
		this( newLoggingConnectionDictionary() );
	}
	
	
	/** get an instance for browsing the PV Logger data */
	static public PVLogger getBrowsingInstance() {
		final ConnectionDictionary dictionary = newBrowsingConnectionDictionary();
		return dictionary != null ? new PVLogger( dictionary ) : null;
	}
	
	
	/** get an instance for logging PV data to the database */
	static public PVLogger getLoggingInstance() {
		return new PVLogger();
	}
	
	
	/** generate a new connection dictionary appropriate for logging */
	static public ConnectionDictionary newLoggingConnectionDictionary() {
		return ConnectionDictionary.getInstance( "pvlogger" );
	}
	
	
	/** generate a new connection dictionary appropriate for browsing logged data */
	static public ConnectionDictionary newBrowsingConnectionDictionary() {
		// use the reports account if available, otherwise use the default account
		return ConnectionDictionary.getPreferredInstance( "pvlogger-reports", "reports" );
	}
	
	
	/** get the connection dictionary */
	public ConnectionDictionary getConnectionDictionary() {
		return _connectionDictionary;
	}
	
	
	/** set the connection dictionary */
	public void setConnectionDictionary( final ConnectionDictionary dictionary ) {
		_connectionDictionary = dictionary;
		SNAPSHOT_PUBLISHER.setConnectionDictionary( dictionary );
	}
	
	
	/**
	 * Get the logger session with the specified groupID
	 * @param groupID the channel group ID
	 * @return the logger session with the specified group ID or null if none exists
	 */
	public LoggerSession getLoggerSession( final String groupID ) {
		synchronized( LOGGER_SESSIONS ) {
			return LOGGER_SESSIONS.get( groupID );
		}
	}
	
	
	/**
	 * Get all logger sessions managed by this PV Logger
	 * @return the collection of logger sessions
	 */
	public Collection<LoggerSession> getLoggerSessions() {
		synchronized( LOGGER_SESSIONS ) {
			return LOGGER_SESSIONS.values();
		}
	}
	
	
	/** remove all logger sessions */
	public void removeAllLoggerSessions() {
		synchronized ( LOGGER_SESSIONS ) {
			final Collection<LoggerSession> loggerSessions = new HashSet<LoggerSession>( getLoggerSessions() );
			for ( final LoggerSession session : loggerSessions ) {
				removeLoggerSession( session.getChannelGroup().getLabel() );
			}
		}
	}
	
	
	/**
	 * Stop the logger session with the specified group ID and remove it from the PV Logger
	 * @param groupID group ID of the logger session to remove
	 */
	public void removeLoggerSession( final String groupID ) {
		synchronized ( LOGGER_SESSIONS ) {
			final LoggerSession session = getLoggerSession( groupID );
			if ( session != null ) {
				session.setEnabled( false );
				LOGGER_SESSIONS.remove( groupID );
			}
		}
	}
	
	
	/**
	 * Determine if a logger session exists for the specified group
	 * @param groupID group ID of the logger session for which to look
	 * @return true if a session exists for the group and false if not
	 */
	public boolean hasLoggerSession( final String groupID ) {
		synchronized ( LOGGER_SESSIONS ) {
			return LOGGER_SESSIONS.containsKey( groupID );
		}
	}
	
	
	/**
	 * Request enabled logger sessions for the specified service
	 * @param serviceID service name
	 * @return the list of logger sessions
	 */
	public List<LoggerSession> requestEnabledLoggerSessionsForService( final String serviceID ) throws SQLException {
		final String[] types = fetchTypes( serviceID );
		final List<LoggerSession> sessions = new ArrayList<LoggerSession>( types.length );
		final Connection connection = getDatabaseConnection();
		for ( final String groupID : types ) {
			final ChannelGroup group = PERSISTENT_STORE.fetchChannelGroup( connection, groupID );
			if ( group.getDefaultLoggingPeriod() > 0 ) {
				sessions.add( requestLoggerSession( groupID ) );
			}
		}

		return sessions;
	}
	
	
	/**
	 * Request logger sessions for the specified service
	 * @param serviceID service name
	 * @return the list of logger sessions
	 */
	public List<LoggerSession> requestLoggerSessionsForService( final String serviceID ) throws SQLException {
		final String[] types = fetchTypes( serviceID );
		final List<LoggerSession> sessions = new ArrayList<LoggerSession>( types.length );
		for ( final String groupID : types ) {
			sessions.add( requestLoggerSession( groupID ) );
		}

		return sessions;
	}
	
	
	/**
	 * If a logger session already exists for the channel group, get it otherwise create a new one
	 * @param groupID the name of the channel group
	 * @return an existing logger session if one exists otherwise a new logger session or null if one could not be created
	 */
	public LoggerSession requestLoggerSession( final String groupID ) throws SQLException {
		synchronized( LOGGER_SESSIONS ) {
			if ( LOGGER_SESSIONS.containsKey( groupID ) ) {
				return getLoggerSession( groupID );
			}

			final Connection connection = getDatabaseConnection();
			if ( connection == null )  return null;
			final ChannelGroup group = PERSISTENT_STORE.fetchChannelGroup( connection, groupID );
			if ( group != null ) {
				final LoggerSession session = new LoggerSession( group, SNAPSHOT_PUBLISHER );
				LOGGER_SESSIONS.put( groupID, session );
				return session;
			}
			else {
				return null;
			}
		}
	}
	
	
	/**
	 * Reload the logger session for the specified channel group
	 * @param groupID the name of the channel group
	 * @return the corresponding logger session or null if a corresponding logger session cannot be found or generated
	 */
	public LoggerSession reloadLoggerSession( final String groupID ) throws SQLException {
		synchronized( LOGGER_SESSIONS ) {
			if ( LOGGER_SESSIONS.containsKey( groupID ) ) {
				final Connection connection = getDatabaseConnection();
				if ( connection == null )  return null;
				final ChannelGroup group = PERSISTENT_STORE.fetchChannelGroup( connection, groupID );
				final LoggerSession session = getLoggerSession( groupID );
				session.setChannelGroup( group );
				return session;
			}
			else {
				return requestLoggerSession( groupID );
			}			
		}
	}
	
	
	/** determine if the snapshot publisher is publishing snapshots periodically */
	public boolean isPublishing() {
		return SNAPSHOT_PUBLISHER.isPublishing();
	}
	
	
	/** start logging sessions and publishing snapshots */
	public void start() {
		SNAPSHOT_PUBLISHER.start();

		synchronized( LOGGER_SESSIONS ) {
			final Collection<LoggerSession> sessions = LOGGER_SESSIONS.values();
			for ( final LoggerSession session : sessions ) {
				if ( !session.isLogging() ) {
					session.startLogging();
				}
			}
		}
	}
	
	
	/** restart logging sessions and publishing snapshots */
	public void restart() {
		SNAPSHOT_PUBLISHER.start();

		synchronized( LOGGER_SESSIONS ) {
			final Collection<LoggerSession> sessions = LOGGER_SESSIONS.values();
			for ( final LoggerSession session : sessions ) {
				if ( !session.isLogging() ) {
					session.resumeLogging();
				}
			}
		}
	}
	
	
	/** stop logging sessions and publishing snapshots but publish any scheduled snapshots */
	public void stop() {
		SNAPSHOT_PUBLISHER.stop();
		synchronized( LOGGER_SESSIONS ) {
			final Collection<LoggerSession> sessions = LOGGER_SESSIONS.values();
			for ( final LoggerSession session : sessions ) {
				session.stopLogging();
			}
		}
		SNAPSHOT_PUBLISHER.publishSnapshots();
	}
	
	
	/** publish any scheduled snapshots remaining in the queue */
	public void publishSnapshots() {
		SNAPSHOT_PUBLISHER.publishSnapshots();
	}
	
	
	/**
	 * Get the publishing period
	 * @return publishing period in seconds
	 */
	public double getPublishingPeriod() {
		return SNAPSHOT_PUBLISHER.getPublishingPeriod();
	}
	
	
	/**
	 * Set the publishing period
	 * @param period publishing period in seconds
	 */
	public void setPublishingPeriod( final double period ) {
		SNAPSHOT_PUBLISHER.setPublishingPeriod( period );
	}
	
	
	/**
	 * Fetch the machine snapshot corresponding to the specified snasphot ID
	 * @param snapshotID machine snaspshot ID
	 * @return machine snapshot corresponding to the specified ID
	 */
	public MachineSnapshot fetchMachineSnapshot( final long snapshotID ) throws SQLException {
		final Connection connection = getDatabaseConnection();
		return PERSISTENT_STORE.fetchMachineSnapshot( connection, snapshotID );
	}
	
	
	/**
	 * Fetch the machine snapshots within the specified time range. If the type is not null, then restrict the machine snapshots to those of the specified type.
	 * The machine snapshots do not include the channel snapshots. A complete snapshot can be obtained using the fetchMachineSnapshot(id) method.
	 * @param type The type of machine snapshots to fetch or null for no restriction
	 * @param startTime The start time of the time range
	 * @param endTime The end time of the time range
	 * @return An array of machine snapshots meeting the specified criteria
	 */
	public MachineSnapshot[] fetchMachineSnapshotsInRange( final String type, final Date startTime, final Date endTime ) throws SQLException {
		final Connection connection = getDatabaseConnection();
		return PERSISTENT_STORE.fetchMachineSnapshotsInRange( connection, type, startTime, endTime );
	}
	
	
	/**
	 * Fetch the channel snapshots from the data source and populate the machine snapshot
	 * @param machineSnapshot The machine snapshot for which to fetch the channel snapshots and load them
	 * @return the machineSnapshot which is the same as the parameter returned for convenience
	 */
	public MachineSnapshot loadChannelSnapshotsInto( final MachineSnapshot machineSnapshot ) throws SQLException {
		final Connection connection = getDatabaseConnection();
		return PERSISTENT_STORE.loadChannelSnapshotsInto( connection, machineSnapshot );
	}
	
	
	/**
	 * Fetch channel groups as an array of types
	 * @return array of types corresponding to all of the channel groups
	 */
	public String[] fetchTypes()  throws SQLException {
		final Connection connection = getDatabaseConnection();
		return PERSISTENT_STORE.fetchTypes( connection );
	}
	
	
	/**
	 * Fetch the channel groups associated with the service ID as an array of types
	 * @param serviceID service ID of groups to fetch
	 * @return array of types corresponding to channel groups with the specified service ID
	 */
	public String[] fetchTypes( final String serviceID ) throws SQLException {
		final Connection connection = getDatabaseConnection();
		return PERSISTENT_STORE.fetchTypes( connection, serviceID );
	}
	
	
	/**
	 * Get the channel group corresponding to the specified type.
	 * @param type channel group type
	 */
	public ChannelGroup getChannelGroup( final String type ) throws SQLException {
		final Connection connection = getDatabaseConnection();
		return PERSISTENT_STORE.getChannelGroup( connection, type );
	}
	
	
	/** get the current database connection creating it if necessary */
	protected Connection getDatabaseConnection() {
		if ( _connection == null || !testConnection( _connection ) ) {
			closeConnection();
			_connection = getNewDatabaseConnection();
		}

		return _connection;
	}
	
	
	/** make a new database connection */
	protected Connection getNewDatabaseConnection() {
		try {
			Connection con = _connectionDictionary.hasRequiredInfo() ? PersistentStore.connectionInstance( _connectionDictionary ) : null; 
			System.out.println("Connection is "+ con == null ? "null" : con.toString());
			return con;
		}
		catch( Exception exception ) {
			exception.printStackTrace();
			return null;
		}
	}
	
	
	/** close the database connection if a connection exists and set the connection to null */
	public void closeConnection() {
		try {
			if ( _connection != null ) {
				_connection.close();
			}
		}
		catch ( Exception exception ) {
			exception.printStackTrace();
		}
		finally {
			_connection = null;
		}
		
	}
	
	
	/**
	 * Test whether the connection is good
	 * @param connection the connection to test
	 * @return true if the connection is good and false if not
	 */
	static protected boolean testConnection( final Connection connection ) {
		try {
			return !connection.isClosed();
		}
		catch( SQLException exception ) {
			return false;
		}
	}
	
	
	/** sql connections should be closed manually 
	 * @throws Throwable */
	protected void finalize() throws Throwable{
		try {
			closeConnection();
		}
		finally {
			super.finalize();
		}
	}
}
