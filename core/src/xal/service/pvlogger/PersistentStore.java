//
//  PersistentStore.java
//  xal
//
//  Created by Pelaia II, Tom on 10/10/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.service.pvlogger;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;
import java.sql.*;

import xal.tools.data.*;
import xal.tools.database.*;


/** contains information about the persistent storage */
class PersistentStore {
	/** proxy to a database table of snapshot group channels */
	final private SnapshotGroupChannelTable SNAPSHOT_GROUP_CHANNEL_TABLE;
	
	/** proxy to a database table of snapshot groups */
	final protected SnapshotGroupTable SNAPSHOT_GROUP_TABLE;
	
	/** channel snapshot tables keyed by service ID */
	final protected Map<String,ChannelSnapshotTable> CHANNEL_SNAPSHOT_TABLES;
	
	/** machine snapshot table */
	final protected MachineSnapshotTable MACHINE_SNAPSHOT_TABLE;
	
	/** map of channel groups keyed by group ID */
	final protected Map<String,ChannelGroup> CHANNEL_GROUPS;
	
	
	/** Constructor */
	public PersistentStore( final DataAdaptor storeAdaptor ) {
		final Map<String,DBTableConfiguration> tableConfigurations = loadTableConfigurations( storeAdaptor );
		SNAPSHOT_GROUP_CHANNEL_TABLE = new SnapshotGroupChannelTable( tableConfigurations.get( "SnapshotGroupChannel" ) );
		SNAPSHOT_GROUP_TABLE = new SnapshotGroupTable( tableConfigurations.get( "SnapshotGroup" ), SNAPSHOT_GROUP_CHANNEL_TABLE );
		MACHINE_SNAPSHOT_TABLE = new MachineSnapshotTable( tableConfigurations.get( "MachineSnapshot" ) );
		
		CHANNEL_SNAPSHOT_TABLES = loadChannelSnapshotTables( storeAdaptor );
		
		CHANNEL_GROUPS = new HashMap<String,ChannelGroup>();
	}
	
	
	/** get the table configurations from the configuration */
	static private Map<String,DBTableConfiguration> loadTableConfigurations( final DataAdaptor storeAdaptor ) {
		final List<DataAdaptor> tableAdaptors = storeAdaptor.childAdaptors( "dbtable" );
		final Map<String,DBTableConfiguration> tableConfigurations = new HashMap<String,DBTableConfiguration>(2);
		for ( final DataAdaptor tableAdaptor : tableAdaptors ) {
			final String entity = tableAdaptor.stringValue( "entity" );
			tableConfigurations.put( entity, new DBTableConfiguration( tableAdaptor ) );
		}
		
		return tableConfigurations;
	}
	
	
	/** load the machine snapshot tables from the configuration */
	static private Map<String,ChannelSnapshotTable> loadChannelSnapshotTables( final DataAdaptor storeAdaptor ) {
		final Map<String,ChannelSnapshotTable> channelSnapshotTables = new HashMap<String,ChannelSnapshotTable>();
		final List<DataAdaptor> serviceAdaptors = storeAdaptor.childAdaptors( "service" );
		for ( final DataAdaptor serviceAdaptor : serviceAdaptors ) {
			final String serviceID = serviceAdaptor.stringValue( "name" );
			final DataAdaptor tableAdaptor = serviceAdaptor.childAdaptor( "dbtable" );
			final ChannelSnapshotTable channelSnapshotTable = new ChannelSnapshotTable( new DBTableConfiguration( tableAdaptor ) );
			channelSnapshotTables.put( serviceID, channelSnapshotTable );
		}
		
		return channelSnapshotTables;
	}
	
	
	/** get a new connection using the specified connection dictionary */
	static public Connection connectionInstance( final ConnectionDictionary dictionary ) throws SQLException {
		final DatabaseAdaptor databaseAdaptor = dictionary.getDatabaseAdaptor();
		final Connection connection = databaseAdaptor.getConnection( dictionary );
		connection.setAutoCommit( false );
		
		return connection;
	}
	
	
	/**
	 * Fetch the machine snapshot corresponding to the specified snasphot ID
	 * @param connection database connection
	 * @param snapshotID machine snaspshot ID
	 * @return machine snapshot corresponding to the specified ID
	 */
	public MachineSnapshot fetchMachineSnapshot( final Connection connection, final long snapshotID ) throws SQLException {
		final MachineSnapshot machineSnapshot = MACHINE_SNAPSHOT_TABLE.fetchMachineSnapshot( connection, snapshotID );
		final ChannelSnapshotTable channelSnapshotTable = getChannelSnapshotTable( connection, machineSnapshot );
		
		MACHINE_SNAPSHOT_TABLE.loadChannelSnapshotsInto( connection, channelSnapshotTable, machineSnapshot );
		
		return machineSnapshot;
	}
	
	
	/**
	 * Fetch the machine snapshots within the specified time range. If the type is not null, then restrict the machine snapshots to those of the specified type. 
	 * The machine snapshots do not include the channel snapshots. A complete snapshot can be obtained using the fetchMachineSnapshot(id) method.
	 * @param connection database connection
	 * @param type The type of machine snapshots to fetch or null for no restriction
	 * @param startTime The start time of the time range
	 * @param endTime The end time of the time range
	 * @return An array of machine snapshots meeting the specified criteria
	 */
	public MachineSnapshot[] fetchMachineSnapshotsInRange( final Connection connection, final String type, final java.util.Date startTime, final java.util.Date endTime ) throws SQLException {
		return MACHINE_SNAPSHOT_TABLE.fetchMachineSnapshotsInRange( connection, type, startTime, endTime );
	}
	
	
	/**
	 * Fetch the channel snapshots from the data source and populate the machine snapshot
	 * @param connection database connection
	 * @param machineSnapshot The machine snapshot for which to fetch the channel snapshots and load them
	 * @return the machineSnapshot which is the same as the parameter returned for convenience
	 */
	public MachineSnapshot loadChannelSnapshotsInto( final Connection connection, final MachineSnapshot machineSnapshot ) throws SQLException {
		final ChannelSnapshotTable channelSnapshotTable = getChannelSnapshotTable( connection, machineSnapshot );
		return MACHINE_SNAPSHOT_TABLE.loadChannelSnapshotsInto( connection, channelSnapshotTable, machineSnapshot );
	}
	
	
	/**
	 * Fetch channel groups as an array of types
	 * @param connection database connection
	 * @return array of types corresponding to all of the channel groups
	 */
	public String[] fetchTypes( final Connection connection )  throws SQLException {
		return SNAPSHOT_GROUP_TABLE.fetchTypes( connection );		
	}
	
	
	/**
	 * Fetch the channel groups associated with the service ID as an array of types
	 * @param connection database connection
	 * @param serviceID service ID of groups to fetch
	 * @return array of types corresponding to channel groups with the specified service ID
	 */
	public String[] fetchTypes( final Connection connection, final String serviceID ) throws SQLException {
		return SNAPSHOT_GROUP_TABLE.fetchTypes( connection, serviceID );
	}
	
	
	/**
	 * Get the channel group corresponding to the specified type.
	 * @param connection database connection
	 * @param type channel group type
	 */
	public ChannelGroup getChannelGroup( final Connection connection, final String type ) throws SQLException {
		synchronized( CHANNEL_GROUPS ) {
			if ( !CHANNEL_GROUPS.containsKey( type ) ) {
				fetchChannelGroup( connection, type );
			}
			return CHANNEL_GROUPS.get( type );
		}
	}
	
	
	/**
	 * Fetch the channel group for the specified type from the database
	 * @param connection database connection
	 * @param type channel group type
	 * @return list of all channel groups
	 */
	protected ChannelGroup fetchChannelGroup( final Connection connection, final String type ) throws SQLException {
		final ChannelGroup channelGroup = SNAPSHOT_GROUP_TABLE.fetchChannelGroup( connection, type );
		CHANNEL_GROUPS.put( type, channelGroup );
		return channelGroup;
	}
	
	
	/**
	 * Insert the channel snapshots.
	 * @param connection database connection
	 * @param channelNames PVs to insert
	 * @param groupID Channel Group ID
	 */
	public void insertChannels( final Connection connection, final List<String> channelNames, final String groupID ) throws SQLException {
		SNAPSHOT_GROUP_CHANNEL_TABLE.insertChannels( connection, channelNames, groupID );
	}
	
	
	/** Publish the channel group edits */
	public void publishGroupEdits( final Connection connection, final Set<ChannelGroupRecord> records ) throws SQLException {
		SNAPSHOT_GROUP_TABLE.publishGroupEdits( connection, records );
	}
	
	
	/** get the channel snapshot table for the specified machine snapshot */
	protected ChannelSnapshotTable getChannelSnapshotTable( final Connection connection, final MachineSnapshot machineSnapshot ) throws SQLException {
		final String groupID = machineSnapshot.getType();
		final ChannelGroup group = getChannelGroup( connection, groupID );
		final String serviceID = group.getServiceID();
		
		return CHANNEL_SNAPSHOT_TABLES.get( serviceID );		
	}
	
	
	/** 
	 * Publish the machine snapshots to the database
	 * @param connection database connection
	 * @param machineSnapshots machine snapshots to publish to the database
	 * @return machine snapshots successfully published to the database
	 */
	public List<MachineSnapshot> publish( final Connection connection, final DatabaseAdaptor databaseAdaptor, final List<MachineSnapshot> machineSnapshots ) {
		if ( machineSnapshots.size() == 0 )  return null;
		
		final List<MachineSnapshot> successfulSnapshots = new ArrayList<MachineSnapshot>( machineSnapshots.size() );
		for ( final MachineSnapshot machineSnapshot : machineSnapshots ) {
			if ( publish( connection, databaseAdaptor, machineSnapshot ) ) {
				successfulSnapshots.add( machineSnapshot );
			}
		}
		
		return successfulSnapshots;
	}
	
	
	/**
	 * Publish the specified machine snapshot
	 * @param connection database connection
	 * @param machineSnapshot machine snapshot to publish
	 */
	protected boolean publish( final Connection connection, final DatabaseAdaptor databaseAdaptor, final MachineSnapshot machineSnapshot ) {
		try {
			final ChannelSnapshotTable channelSnapshotTable = getChannelSnapshotTable( connection, machineSnapshot );
			MACHINE_SNAPSHOT_TABLE.insert( connection, databaseAdaptor, channelSnapshotTable, machineSnapshot );
			
			return true;
		}
		catch( SQLException exception ) {
			exception.printStackTrace();
			return false;
		}
	}
}
