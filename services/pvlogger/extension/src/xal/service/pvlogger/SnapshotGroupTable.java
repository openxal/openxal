//
//  SnapshotGroupTable.java
//  xal
//
//  Created by Pelaia II, Tom on 10/12/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.service.pvlogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import xal.tools.data.DataAdaptor;



/** represent the snapshot group (type) database table */
class SnapshotGroupTable {
	/** database table name */
	protected final String TABLE_NAME;
	
	/** primary key */
	protected final String PRIMARY_KEY;
	
	/** group description */
	protected final String DESCRIPTION_COLUMN;
	
	/** snapshot period column */
	protected final String PERIOD_COLUMN;
	
	/** snapshot retention column */
	protected final String RETENTION_COLUMN;
	
	/** service ID foreign key column */
	protected final String SERVICE_COLUMN;
	
	/** proxy to the table of channel - channel group relationships */
	protected SnapshotGroupChannelTable SNAPSHOT_GROUP_CHANNEL_TABLE;
	
	
	/** Constructor */
	public SnapshotGroupTable( final DataAdaptor tableAdaptor, final SnapshotGroupChannelTable groupChannelTable ) {
		this( DBTableConfiguration.getInstance( tableAdaptor ), groupChannelTable );
	}
	
	
	/** Constructor */
	public SnapshotGroupTable( final DBTableConfiguration configuration, final SnapshotGroupChannelTable groupChannelTable ) {
		SNAPSHOT_GROUP_CHANNEL_TABLE = groupChannelTable;
		
		TABLE_NAME = configuration.getTableName();
		
		PRIMARY_KEY = configuration.getColumn( "group" );
		
		DESCRIPTION_COLUMN = configuration.getColumn( "description" );
		PERIOD_COLUMN = configuration.getColumn( "period" );
		RETENTION_COLUMN = configuration.getColumn( "retention" );
		SERVICE_COLUMN = configuration.getColumn( "service" );
	}
	
	
	/** fetch all channel groups */
	public List<ChannelGroup> fetchChannelGroups( final Connection connection ) throws SQLException {
		final ArrayList<ChannelGroup> groups = new ArrayList<ChannelGroup>();
		final PreparedStatement groupsQueryStatement = getGroupsQueryStatement( connection );
		final ResultSet resultSet = groupsQueryStatement.executeQuery();
		while ( resultSet.next() ) {
			final ChannelGroup group = newChannelGroup( connection, resultSet );
			groups.add( group );
		}
		resultSet.close();
		groupsQueryStatement.close();
		return groups;
	}
	
	
	/**
	 * Fetch a channel group given the specified type
	 * @param connection database connection
	 * @param type channel group type
	 * @return channel group corresponding to the specified result set record
	 */
	public ChannelGroup fetchChannelGroup( final Connection connection, final String type ) throws SQLException {
		final PreparedStatement groupQueryStatement = getGroupQueryByNameStatement( connection );
		groupQueryStatement.setString( 1, type );
		
		final ResultSet resultSet = groupQueryStatement.executeQuery();
		try {
			return resultSet.next() ? newChannelGroup( connection, resultSet ) : null;
		} finally {
			resultSet.close();
			groupQueryStatement.close();
		}
	}
	
	
	/** produce a new channel group from the specified result set */
	private ChannelGroup newChannelGroup( final Connection connection, final ResultSet resultSet ) throws SQLException {
		final String groupID = resultSet.getString( PRIMARY_KEY );		// should match type
		final String description = resultSet.getString( DESCRIPTION_COLUMN );
		final double loggingPeriod = resultSet.getDouble( PERIOD_COLUMN );
		final double retention = resultSet.getDouble( RETENTION_COLUMN );
		final String serviceID = resultSet.getString( SERVICE_COLUMN );
		
		final String[] pvArray = SNAPSHOT_GROUP_CHANNEL_TABLE.fetchActivePVsByType( connection, groupID );
		
		return new ChannelGroup( groupID, serviceID, description, pvArray, loggingPeriod, retention );			
	}
	
	
	/**
	 * Fetch channel groups as an array of types
	 * @param connection database connection
	 * @return array of types corresponding to all of the channel groups
	 */
	public String[] fetchTypes( final Connection connection )  throws SQLException {
		final List<String> types = new ArrayList<String>();
		final ResultSet result = getGroupsQueryStatement( connection ).executeQuery();
		while ( result.next() ) {
			types.add( result.getString( PRIMARY_KEY ) );
		}
		result.close();
		return types.toArray( new String[types.size()] );		
	}
	
	
	/**
	 * Fetch the channel groups associated with the service ID as an array of types
	 * @param connection database connection
	 * @param serviceID service ID of groups to fetch
	 * @return array of types corresponding to channel groups with the specified service ID
	 */
	public String[] fetchTypes( final Connection connection, final String serviceID ) throws SQLException {
		final PreparedStatement statement = getGroupsQueryByServiceStatement( connection );
		statement.setString( 1, serviceID );
		
		final List<String> types = new ArrayList<String>();
		final ResultSet result = statement.executeQuery();
		while ( result.next() ) {
			types.add( result.getString( PRIMARY_KEY ) );
		}
		result.close();
		statement.close();
		return types.toArray( new String[types.size()] );		
	}
	
	
	/**
	 * Get a prepared statement to get the snapshot groups.
	 * @return the prepared statement to query for available snapshot types
	 * @throws java.sql.SQLException  if an exception occurs during a SQL evaluation
	 */
	protected PreparedStatement getGroupsQueryStatement( final Connection connection ) throws SQLException {
		return connection.prepareStatement( "SELECT * FROM " + TABLE_NAME );
	}
	
	
	/**
	 * Get a prepared statement to get the snapshot group by group name (i.e. primary key).
	 * @return the prepared statement to query for a snapshot type with the specified name
	 * @throws java.sql.SQLException  if an exception occurs during a SQL evaluation
	 */
	protected PreparedStatement getGroupQueryByNameStatement( final Connection connection ) throws SQLException {
		return connection.prepareStatement( "SELECT * FROM " + TABLE_NAME + " WHERE " + PRIMARY_KEY + " = ?" );
	}
	
	
	/**
	 * Get a prepared statement to get the snapshot groups associated with a service ID.
	 * @return the prepared statement to query for available snapshot types associated with a specified service ID
	 * @throws java.sql.SQLException  if an exception occurs during a SQL evaluation
	 */
	protected PreparedStatement getGroupsQueryByServiceStatement( final Connection connection ) throws SQLException {
		return connection.prepareStatement( "SELECT * FROM " + TABLE_NAME + " WHERE " + SERVICE_COLUMN + " = ?" );
	}
	
	
	
	
	/**
	 * Create a prepared statement for updating records in the snapshot group database table.
	 * @return the prepared statement for updating records
	 * @throws java.sql.SQLException  if an exception occurs during a SQL evaluation
	 */
	protected PreparedStatement getUpdateStatement( final Connection connection ) throws SQLException {
		return connection.prepareStatement( "UPDATE " + TABLE_NAME + " SET " + DESCRIPTION_COLUMN + " = ?, " + SERVICE_COLUMN + " = ?, " + PERIOD_COLUMN + " = ?, " + RETENTION_COLUMN + " = ? where " + PRIMARY_KEY + " = ?" );
	}	
	
	
	/**
	 * Insert the channels for the specified group.
	 * @param connection database connection
	 * @param channelNames PVs to insert
	 * @param groupID Channel Group ID
	 */
	public void publishGroupEdits( final Connection connection, final Set<ChannelGroupRecord> groupRecords ) throws SQLException {
		final PreparedStatement updateStatement = getUpdateStatement( connection );
		boolean needsUpdate = false;
		
		for ( final ChannelGroupRecord groupRecord : groupRecords ) {
			if ( groupRecord != null ) {
				try {
					updateStatement.setString( 1, groupRecord.getDescription() );
					updateStatement.setString( 2, groupRecord.getServiceID() );
					updateStatement.setDouble( 3, groupRecord.getDefaultLoggingPeriod() );
					updateStatement.setDouble( 4, groupRecord.getRetention() );
					updateStatement.setString( 5, groupRecord.getLabel() );
					
					updateStatement.addBatch();
					needsUpdate = true;
				}
				catch( Exception exception ) {
					System.err.println( "Exception publishing update for group:  " + groupRecord.getLabel() );
					System.err.println( exception );
				}
			}
		}
		
		if ( needsUpdate ) {
			updateStatement.executeBatch();
		}
		
		if( updateStatement != null ) {
			updateStatement.close();
		}
	}	
}
