
//
//  ChannelSnapshotTable.java
//  xal
//
//  Created by Pelaia II, Tom on 10/12/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.service.pvlogger;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.List;
import java.util.ArrayList;

import java.math.BigDecimal;

import xal.tools.database.DatabaseAdaptor;



/** represent the channel snapshot database table */
class ChannelSnapshotTable {
	/** database table name */
	protected final String TABLE_NAME;

	/** time stamp column */
	protected final String TIMESTAMP_COLUMN;

	/** machine snapshot primary key */
	protected final String MACHINE_SNAPSHOT_COLUMN;

	/** PV primary key */
	protected final String PV_COLUMN;

	/** value column */
	protected final String VALUE_COLUMN;

	/** status column */
	protected final String STATUS_COLUMN;

	/** severity column */
	protected final String SEVERITY_COLUMN;

	/** value array type (value holds an array of doubles) */
	protected final String VALUE_ARRAY_TYPE;


	/** Constructor */
	public ChannelSnapshotTable( final DBTableConfiguration configuration ) {
		TABLE_NAME = configuration.getTableName();

		MACHINE_SNAPSHOT_COLUMN = configuration.getColumn( "machineSnapshot" );
		PV_COLUMN = configuration.getColumn( "pv" );

		TIMESTAMP_COLUMN = configuration.getColumn( "timestamp" );
		VALUE_COLUMN = configuration.getColumn( "value" );
		STATUS_COLUMN = configuration.getColumn( "status" );
		SEVERITY_COLUMN = configuration.getColumn( "severity" );

		VALUE_ARRAY_TYPE = configuration.getDataType( "valueArray" );
	}


	/**
	 * Insert the channel snapshots.
	 * @param connection database connection
	 * @param channelSnapshots channel snapshots to insert
	 * @param machineSnapshotID machine snapshot ID
	 */
	public void insert( final Connection connection, final DatabaseAdaptor databaseAdaptor, final ChannelSnapshot[] channelSnapshots, final long machineSnapshotID ) throws SQLException {
		final PreparedStatement insertStatement = getInsertStatement( connection );
		boolean needsInsert = false;

		for ( final ChannelSnapshot channelSnapshot : channelSnapshots ) {
			if ( channelSnapshot != null ) {
				final Timestamp timeStamp = channelSnapshot.getTimestamp().getSQLTimestamp();
				try {
					final Array valueArray = databaseAdaptor.getArray( VALUE_ARRAY_TYPE, connection, channelSnapshot.getValue() );
					insertStatement.setLong( 1, machineSnapshotID );
					insertStatement.setString( 2, channelSnapshot.getPV() );
					insertStatement.setTimestamp( 3, timeStamp );

					insertStatement.setArray( 4, valueArray );

					insertStatement.setInt( 5, channelSnapshot.getStatus() );
					insertStatement.setInt( 6, channelSnapshot.getSeverity() );

					insertStatement.addBatch();
					needsInsert = true;
				}
				catch( Exception exception ) {
					System.err.println( "Exception publishing channel snapshot:  " + channelSnapshot );
					System.err.println( exception );
				}

			}
		}

		if ( needsInsert ) {
			insertStatement.executeBatch();
		}
		if(insertStatement != null) {
			insertStatement.close();
		}
	}


	/**
	 * Fetch the channel snapshots associated with a machine snapshot given by the machine snapshot's unique identifier.
	 * @param connection database connection
	 * @param machineSnapshotID machine snapshot primary key
	 * @return The channel snapshots associated with the machine snapshop
	 */
	public ChannelSnapshot[] fetchChannelSnapshotsForMachineSnapshotID( final Connection connection, final long machineSnapshotID ) throws SQLException {
		final List<ChannelSnapshot> snapshots = new ArrayList<ChannelSnapshot>();

		final PreparedStatement snapshotQuery = getQueryByMachineSnapshotStatement( connection );
		snapshotQuery.setLong( 1, machineSnapshotID );

		final ResultSet resultSet = snapshotQuery.executeQuery();
		while ( resultSet.next() ) {
			final String pv = resultSet.getString( PV_COLUMN );
			final Timestamp timestamp = resultSet.getTimestamp( TIMESTAMP_COLUMN );
			final Number[] bigValue = (Number[])resultSet.getArray( VALUE_COLUMN ).getArray();
			final double[] value = toDoubleArray( bigValue );
			final short status = resultSet.getShort( STATUS_COLUMN );
			final short severity = resultSet.getShort( SEVERITY_COLUMN );
			snapshots.add( new ChannelSnapshot( pv, value, status, severity, new xal.ca.Timestamp( timestamp ) ) );
		}
		if( snapshotQuery != null) {
			snapshotQuery.close();
		}
		resultSet.close();
		return snapshots.toArray( new ChannelSnapshot[snapshots.size()] );
	}


	/**
	 * Create a prepared statement for inserting new records into the channel snapshot database table.
	 * @return the prepared statement for inserting a new channel snapshot
	 * @throws java.sql.SQLException  if an exception occurs during a SQL evaluation
	 */
	protected PreparedStatement getInsertStatement( final Connection connection ) throws SQLException {
		return connection.prepareStatement( "INSERT INTO " + TABLE_NAME + "(" + MACHINE_SNAPSHOT_COLUMN + ", " + PV_COLUMN + ", " + TIMESTAMP_COLUMN + ", " + VALUE_COLUMN + ", " + STATUS_COLUMN + ", " + SEVERITY_COLUMN + ") VALUES (?, ?, ?, ?, ?, ?)" );
	}


	/**
	 * Create a prepared statement to query for channel snapshot records corresponding to a machine snapshot.
	 * @return the prepared statement to query for channel snapshots by machine snapshot
	 * @throws java.sql.SQLException  if an exception occurs during a SQL evaluation
	 */
	protected PreparedStatement getQueryByMachineSnapshotStatement( final Connection connection ) throws SQLException {
		return connection.prepareStatement( "SELECT * FROM " + TABLE_NAME + " WHERE " + MACHINE_SNAPSHOT_COLUMN + " = ?" );
	}


	/**
	 * Convert an array of numbers to an array of double values.
	 * @param numbers array of numbers to convert
	 * @return array of double values corresponding to the input array of numbers.
	 */
	static protected double[] toDoubleArray( final Number[] numbers ) {
		final double[] array = new double[numbers.length];

		for ( int index = 0; index < numbers.length; index++ ) {
			array[index] = numbers[index].doubleValue();
		}

		return array;
	}
}
