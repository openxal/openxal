//
//  PersistentStore.java
//  xal
//
//  Created by Thomas Pelaia on 8/2/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.service.tripmonitor;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.sql.*;

import xal.tools.data.*;
import xal.tools.database.*;


/** contains information about the persistent storage */
public class PersistentStore {
	/** data label */
	public final static String DATA_LABEL = "PersistentStore";
	
	/** Trips table */
	final protected String TRIPS_TABLE;
	
	/** PV Column */
	final protected String PV_COLUMN;
	
	/** Timestamp Column */
	final protected String TIMESTAMP_COLUMN;
	
	/** SQL text for the insert statement */
	final protected String INSERT_SQL;
	
	
	/** Constructor */
	public PersistentStore( final DataAdaptor adaptor ) {
		TRIPS_TABLE = adaptor.stringValue( "table" );
		PV_COLUMN = adaptor.stringValue( "pvColumn" );
		TIMESTAMP_COLUMN = adaptor.stringValue( "timestampColumn" );
		
		INSERT_SQL = "INSERT INTO " + TRIPS_TABLE + " ( " + PV_COLUMN + ", " + TIMESTAMP_COLUMN + " ) VALUES (?, ?)";
	}
	
	
	/** get a new connection using the default dictionary */
	static public Connection connectionInstance() {
		// use the "tripmonitor" account if available, otherwise fallback to "monitor" and finally the default account
		final ConnectionDictionary dictionary = ConnectionDictionary.getPreferredInstance( "tripmonitor", "monitor" );
		final DatabaseAdaptor databaseAdaptor = dictionary.getDatabaseAdaptor();
		return databaseAdaptor.getConnection( dictionary );		
	}
	
	
	/** publish the trip records */
	public boolean publish( final Connection connection, final List<TripRecord> tripRecords ) {
		try {
			final PreparedStatement TRIPS_INSERT = connection.prepareStatement( INSERT_SQL );
			
			for ( final TripRecord record : tripRecords ) {
				TRIPS_INSERT.setString( 1, record.getPV() );
				TRIPS_INSERT.setTimestamp( 2, record.getSQLTimestamp() );
				
				TRIPS_INSERT.addBatch();
			}
			TRIPS_INSERT.executeBatch();
			connection.commit();
			
			return true;
		}
		catch( Exception exception ) {
			exception.printStackTrace();
			return false;
		}
	}
	
	
	/** fetch the trip records between the selected dates */
	public List<TripRecord> fetchTripRecordsBetween( final Connection connection, final Date startTime, final Date endTime ) {
		final String SELECT_SQL = "SELECT * FROM " + TRIPS_TABLE + " WHERE " + TIMESTAMP_COLUMN + " >= ? AND " + TIMESTAMP_COLUMN + " <= ? ORDER BY " + TIMESTAMP_COLUMN;
		
		try {
			final List<TripRecord> tripRecords = new ArrayList<TripRecord>();
			
			final PreparedStatement SELECT_STATEMENT = connection.prepareStatement( SELECT_SQL );
			SELECT_STATEMENT.setTimestamp( 1, new java.sql.Timestamp( startTime.getTime() ) );
			SELECT_STATEMENT.setTimestamp( 2, new java.sql.Timestamp( endTime.getTime() ) );
			final ResultSet resultSet = SELECT_STATEMENT.executeQuery();
			
			while ( resultSet.next() ) {
				final String pvName = resultSet.getString( PV_COLUMN );
				final Timestamp timeStamp = resultSet.getTimestamp( TIMESTAMP_COLUMN );
				
				tripRecords.add( new TripRecord( pvName, timeStamp ) );
			}
			
			return tripRecords;
		}
		catch( Exception exception ) {
			exception.printStackTrace();
			return null;
		}
	}
}

