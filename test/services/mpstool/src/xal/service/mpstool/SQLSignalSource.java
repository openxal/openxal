/*
 *  SQLSignalSource.java
 *
 *  Created on Thu Mar 04 16:08:12 EST 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.service.mpstool;

import java.util.*;
import java.util.logging.*;
import java.sql.*;
import java.lang.reflect.Array;

import xal.tools.database.*;

/**
 * SQLSignalSource is an implementation of SignalSource that uses an SQL database as the data
 * source.
 *
 * @author    tap
 */
public class SQLSignalSource implements SignalSource {
	/** database adaptor to use for the database source */
	protected DatabaseAdaptor _databaseAdaptor;
	
	/** database connection dictionary */
	protected ConnectionDictionary _connectionDictionary;
		

	/**
	 * Constructor to initialize using the default database adaptor
	 * @throws DatabaseException the database exception
	 */
	public SQLSignalSource() throws DatabaseException {
		// use the "firstfault" account if available, otherwise fallback to "monitor" and finally the default account
		this( ConnectionDictionary.getPreferredInstance( "firstfault", "monitor" ) );
	}
	
	
	/**
	 * Constructor to initialize using the default database adaptor
	 * @throws DatabaseException the database exception
	 */
	public SQLSignalSource( final ConnectionDictionary dictionary ) throws DatabaseException {
		_connectionDictionary = dictionary;
		_databaseAdaptor = dictionary.getDatabaseAdaptor();
	}
	

	/**
	 * Connect to the database with the default connection dictionary
	 * @return a new connection
	 * @throws DatabaseException  if the connection fails
	 */
	synchronized protected Connection newConnection() throws DatabaseException {
		try {
			final Connection connection = _databaseAdaptor.getConnection( _connectionDictionary );
			connection.setAutoCommit( false );
			return connection;			
		}
		catch ( SQLException exception ) {
			throw new DatabaseException( "Exception while making a new database connection.", _databaseAdaptor, exception );
		}
	}
	
	
	/**
	 * Close the specified database connection.
	 * @param connection the connection to close
	 */
	protected void closeConnection( final Connection connection ) throws DatabaseException {
		try {
			if ( connection != null && !connection.isClosed() ) {
				connection.close();
			}
		}
		catch ( SQLException exception ) {
			throw new DatabaseException( "Exception while attempting to close a database exception.", _databaseAdaptor, exception );
		}
	}
	

	/**
	 * Fetch MPS signals for the specified MPS latch type.
	 * @param type   The MPS signal latch type (e.g. "FPL" or "FPAR").
	 * @return    The array of MPS PVs
	 * @throws DatabaseException  if the fetch fails
	 */
	synchronized public String[] fetchMPSSignals( final String type ) throws DatabaseException {
		final Connection connection = newConnection();
		try {
			final String sql = "{? = call epics.epics_mps_pkg.mps_signals_to_monitor (?)}";
			final CallableStatement procedure = connection.prepareCall( sql );
			procedure.registerOutParameter( 1, Types.ARRAY, "EPICS.SGNL_ID_TAB" );
			procedure.setString( 2, type );
			procedure.executeUpdate();
			connection.commit();
			return (String[])procedure.getArray( 1 ).getArray();
		}
		catch ( SQLException exception ) {
			final String message = "Exception while fetching MPS signals of type, " + type;
			throw new DatabaseException( message, _databaseAdaptor, exception );
		}
		finally {
			closeConnection( connection );
		}
	}
	
	
	/**
	 * Fetch a map of input monitors keyed by their associated MPS signals.
	 * @param type  The MPS signal latch type (e.g. "FPL" or "FPAR").
	 * @return  The input monitor map keyed by MPS signal.
	 * @throws DatabaseException  if the fetch fails
	 */
	 synchronized public Map<String,InputMonitor> fetchInputMonitors( final String type ) throws DatabaseException {
		 final Connection connection = newConnection();
		 try {
			 final Map<String,InputMonitor> signalMap = new HashMap<String,InputMonitor>();
			 
			 final String sql = "{ ? = call epics.epics_mps_pkg.mps_trip_signals_to_monitor(?) }";
			 final CallableStatement procedure = connection.prepareCall( sql );
			 procedure.registerOutParameter( 1, Types.ARRAY, "EPICS.MPS_TRIPS_SGNL_TAB" );
			 procedure.setString( 2, type );
			 procedure.execute();
			 
			 final Object[] array = (Object[])procedure.getArray(1).getArray();
			 for ( int index = 0 ; index < array.length ; index++ ) {
				 final Struct element = (Struct)array[index];
				 final Object[] attributes = element.getAttributes();
				 if ( attributes.length > 3 ) {
					 final Object mpsAttribute = attributes[0];
					 final Object inputAttribute = attributes[1];
					 final Object okayInputAttribute = attributes[2];
					 if ( mpsAttribute != null && inputAttribute != null && okayInputAttribute != null ) {
						 try {
							 final String mpsSignal = mpsAttribute.toString();
							 final String inputSignal = inputAttribute.toString();
							 final int okayInputValue = Integer.parseInt( okayInputAttribute.toString() );
							 
							 signalMap.put( mpsSignal, new InputMonitor( mpsSignal, inputSignal, okayInputValue ) );
						 }
						 catch( Exception exception ) {
							 final String message = "Exception generating input info for MPS signal: " + mpsAttribute;
							 Logger.getLogger("global").log( Level.SEVERE, message, exception );
						 }
					 }
				 }
			 }
			 
			 return signalMap;
		 }
		 catch ( SQLException exception ) {
			 final String message = "Exception while fetching MPS input signals for MPS type, " + type;	
			 throw new DatabaseException( message, _databaseAdaptor, exception );
		 }
		 finally {
			 closeConnection( connection );
		 }
	 }


	/**
	 * Publish the MPS daily statistics to the database.
	 * @param day  The day for which the statistics were gathered
	 * @param statistics  The daily trip statistics
	 * @exception DatabaseException  if the publish attempt fails
	 */
	 synchronized public void publishDailyStatistics( final java.util.Date day, final Collection<TripStatistics> statistics ) throws DatabaseException {
		 final Connection connection = newConnection();
		 try {
			 final PreparedStatement STATS_INSERT = connection.prepareStatement( "INSERT INTO epics.mps_daily_stat ( sgnl_id, cur_dte, mps_inp_sgnl_id, mps_inp_trips, mps_trips, mps_first_hit ) VALUES (?, ?, ?, ?, ?, ?)" );
			 
			 for( final TripStatistics stats : statistics ) {
				 final String inputSignal = stats.getInputSignal();
				 				 
				 STATS_INSERT.setString( 1, stats.getMPSPV() );
				 STATS_INSERT.setDate( 2, new java.sql.Date( day.getTime() ) );
				 STATS_INSERT.setString( 3, stats.getInputSignal() );
				 STATS_INSERT.setInt( 4, stats.getInputTrips() );
				 STATS_INSERT.setInt( 5, stats.getMPSTrips() );
				 STATS_INSERT.setInt( 6, stats.getFirstHits() );
				 
				 STATS_INSERT.addBatch();
			 }
			 STATS_INSERT.executeBatch();
			 connection.commit();
		 }
		 catch( SQLException exception ) {
			 final String message = "Exception while publishing daily trip statistics";	
			 throw new DatabaseException( message, _databaseAdaptor, exception );
		 }
		 finally {
			 closeConnection( connection );
		 }
	 }
}

