/*
 *  SqlStateStore.java
 *
 *  Created on Fri Dec 05 16:11:32 EST 2003
 *
 *  Copyright (c) 2003 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.score;

import xal.tools.database.ConnectionDictionary;
import xal.tools.database.DatabaseAdaptor;
import xal.tools.database.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * SqlStateStore is an implementation of StateStore that provides persistent storage / retrival of machine
 * state to and from a SQL database.
 *
 * @author jdg
 */
public class SqlStateStore implements StateStore {
	// tables
	/** Description of the Field */
	protected final static String SCORE_SNAPSHOT_SIGNAL_TABLE = "score.score_snapshot_sgnl";
	/** Description of the Field */
	protected final static String SCORE_SNAPSHOT_GROUP_TABLE = "score.score_snapshot_grp";
	/** Description of the Field */
	protected final static String SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE = "score.score_snapshot_grp_sgnl";
	// types
	/** Description of the Field */
	protected final static String SGNL_VALUE_ARRAY_TYPE = "EPICS.SGNL_VAL_TYP";
	
	//SCORE_SNAPSHOT_SIGNAL_TABLE
	// columns
	protected final static String EQUIP_CAT_PK = "equip_cat_id";
	protected final static String DATE_PK = "mod_dte";	
	/** Description of the Field */
	protected final static String SP_PV_ID_COL = "set_pt_sgnl_id";
	/** Description of the Field */
	protected final static String RB_PV_ID_COL = "rb_sgnl_id";	
	protected final static String RB_PV_VAL_COL = "rb_sgnl_val";	
	protected final static String SP_PV_VAL_COL = "set_pt_sgnl_val";	

	//SCORE_SNAPSHOT__GROUP_SIGNAL_TABLE
	// columns
	protected final static String SYSTEM_COL = "sys_id";	
	protected final static String SUBSYSTEM_COL = "filter_id";
	protected final static String USE_RB_FOR_SP_COL = "use_rb_ind";
	protected final static String GROUP_SIGNAL_ACTIVE_COL = "active_ind";
	protected final static String PV_DATA_TYPE_COL = "pv_data_type";

	//SCORE_SNAPSHOT__GROUP_TABLE
	// columns
	protected final static String DESCR_COL = "descr";	
	protected final static String GOLDEN_COL = "pri_save_set_ind";	
	protected final static String USER_COL = "userid";
	protected final static String DATE_COL = "mod_dte";	
	// database adaptor
	/** Description of the Field */
	protected DatabaseAdaptor _databaseAdaptor;
	
	/** the database dictionary with connection information */
	protected ConnectionDictionary _dictionary;
	
	/** the connection information used to attempt reconnects */
	private final String _user;
	private final String _password;
	private final String _urlSpec;

	// prepared statements

	/** Description of the Field */
	protected PreparedStatement SCORE_SNAPSHOT_INSERT;
	/** Description of the Field */
	protected PreparedStatement SCORE_ROW_INSERT;
	/** Description of the Field */
	protected PreparedStatement SCORE_ROW_QUERY;
	/** Description of the Field */
	protected PreparedStatement SCORE_GROUP_QUERY_BY_GOLDEN;
	/** Description of the Field */
	protected PreparedStatement SCORE_GROUP_TYPE_QUERY;
	/** Description of the Field */		
	protected PreparedStatement SCORE_GROUP_QUERY_BY_TYPE_TIMERANGE;
	protected PreparedStatement SCORE_GROUP_QUERY_BY_TIMERANGE;
	protected PreparedStatement SCORE_GROUP_QUERY_BY_TYPE_TIME;
	protected PreparedStatement SCORE_ROW_QUERY_BY_TYPE_DATE;
	
	/** the error message handler */
	HandleErrorMessage _theDoc;

	// connection state
	/** Description of the Field */
	protected Connection _connection;

	/**
	 * Primary constructor
	 * @param dict properties needed for database connection 
	 * @param connection  A database connection
	 * @param doc error message handler (which is the data document)
	 */
	public SqlStateStore( final ConnectionDictionary dict, final Connection connection, HandleErrorMessage doc) {
		_dictionary = dict;
		DatabaseAdaptor adaptor = _dictionary.getDatabaseAdaptor();
		_databaseAdaptor = (adaptor != null ) ? adaptor : DatabaseAdaptor.getInstance();
		_connection = connection;
		_theDoc = doc;
		_user = _dictionary.getUser();
		_password = _dictionary.getPassword();
		_urlSpec = _dictionary.getURLSpec();
	}


	/**
	 * Construct an SQL state store from the specified connection and use the default database
	 * adaptor.
	 *
	 * @param connection  A database connection
	 */
	public SqlStateStore( final Connection connection ) {
		this( null, connection, null);
	}

	/**
	 * Create a database connection to the persistent data storage.
	 *
	 * @param urlSpec                                      The url of the database
	 * @param user                                         The user to login
	 * @param password                                     The user's password for login
	 * @exception StateStoreException                      Description of the Exception
	 * @throws gov.sns.tools.pvlogger.StateStoreException  if a SQL exception is thrown
	 */
	protected void connect( String urlSpec, String user, String password ) throws StateStoreException {
		try {
			_connection = newConnection(_databaseAdaptor, urlSpec, user, password );
		}
		catch ( DatabaseException exception ) {
			throw new StateStoreException( "Error while connecting to the data source and preparing statements.", exception );
		}
	}

	/** 
	 * try to reestablish a database connection to the persistent data storage.
	 *
	 * @exception StateStoreException                      Description of the Exception
	 * @throws gov.sns.tools.pvlogger.StateStoreException  if a SQL exception is thrown
	 */
	protected boolean reconnect() throws StateStoreException {
		try {
			_connection = newConnection(_databaseAdaptor, _urlSpec, _user, _password );
			// force new prepared statements since there are connection dependent
			SCORE_SNAPSHOT_INSERT = null;
			SCORE_ROW_INSERT = null;
			SCORE_ROW_QUERY= null;
			SCORE_GROUP_QUERY_BY_GOLDEN = null;
			SCORE_GROUP_TYPE_QUERY = null;
			SCORE_GROUP_QUERY_BY_TYPE_TIMERANGE = null;
			SCORE_GROUP_QUERY_BY_TIMERANGE = null;
			SCORE_GROUP_QUERY_BY_TYPE_TIME = null;
			SCORE_ROW_QUERY_BY_TYPE_DATE = null;		
			return true;
		}
		catch ( DatabaseException exception ) {
			_theDoc.dumpErr("Cannot reestablish database connection");
			return false;
		}
	}
	
	/**
	 * Create a database connection to the persistent data storage.
	 *
	 * @param adaptor                                      the database adaptor
	 * @param urlSpec                                      The url of the database
	 * @param user                                         The user to login
	 * @param password                                     The user's password for login
	 * @return                                             a new database connection
	 * @exception StateStoreException                      Description of the Exception
	 * @throws gov.sns.tools.pvlogger.StateStoreException  if a SQL exception is thrown
	 */
	protected static Connection newConnection( DatabaseAdaptor adaptor, String urlSpec, String user, String password ) throws StateStoreException {
		try {
			return adaptor.getConnection( urlSpec, user, password );
		}
		catch ( DatabaseException exception ) {
			throw new StateStoreException( "Error while connecting to the data source and preparing statements.", exception );
		}
	}

	/**
	 * Create the prepared statement if it does not already exist.
	 *
	 * @return  the prepared statement for inserting a new machine snapshot
	 * @exception SQLException        Description of the Exception
	 * @throws java.sql.SQLException  if an exception occurs during a SQL evaluation
	 */
	protected PreparedStatement getScoreSnapshotInsertStatement() throws SQLException {
		if ( SCORE_SNAPSHOT_INSERT == null ) {
			SCORE_SNAPSHOT_INSERT = _connection.prepareStatement( "INSERT INTO " + SCORE_SNAPSHOT_GROUP_TABLE + "(" + EQUIP_CAT_PK + ", " + DATE_COL + ", " + DESCR_COL + ", " + USER_COL + "," + GOLDEN_COL + ")" + " VALUES (?, ?, ?, ?, ?)" );
		}
		return SCORE_SNAPSHOT_INSERT;
	}


	/**
	 * Create the prepared statement if it does not already exist.
	 *
	 * @return the prepared statement for inserting a new channel snapshot
	 * @exception SQLException        Description of the Exception
	 * @throws java.sql.SQLException  if an exception occurs during a SQL evaluation
	 */
	protected PreparedStatement getScoreRowInsertStatement() throws SQLException {
		if ( SCORE_ROW_INSERT == null ) {
			SCORE_ROW_INSERT = _connection.prepareStatement( "INSERT INTO " + SCORE_SNAPSHOT_SIGNAL_TABLE + "(" + EQUIP_CAT_PK + ", " + DATE_PK + ", " + SP_PV_ID_COL + ", " + SP_PV_VAL_COL + ", " + RB_PV_ID_COL + ", " + RB_PV_VAL_COL + ") VALUES (?, ?, ?, ?, ?, ?)" );
		}
		return SCORE_ROW_INSERT;
	}


	/**
	 * Create the prepared statement if it does not already exist. Filter for only the group signals that are marked as active.
	 * @return the prepared statement for querying the table for the score rows
	 * @exception SQLException        Description of the Exception
	 * @throws java.sql.SQLException  if an exception occurs during a SQL evaluation
	 */
	protected PreparedStatement getScoreRowTemplateQueryStatement() throws SQLException {
		if ( SCORE_ROW_QUERY == null ) {
			SCORE_ROW_QUERY = _connection.prepareStatement( "SELECT * FROM " + SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + " WHERE " + EQUIP_CAT_PK + " = ? AND " + GROUP_SIGNAL_ACTIVE_COL + " = 'Y'" );
		}
		return SCORE_ROW_QUERY;
	}

	/**
	 * Create the prepared statement if it does not already exist.
	 *
	 * @return  the prepared statement to query forgroup by
	 *      golden indicator
	 * @exception SQLException        Description of the Exception
	 * @throws java.sql.SQLException  if an exception occurs during a SQL evaluation
	 */
	protected PreparedStatement getScoreGroupQueryByGoldenStatement() throws SQLException {
		if ( SCORE_GROUP_QUERY_BY_GOLDEN == null ) {
			SCORE_GROUP_QUERY_BY_GOLDEN = _connection.prepareStatement( "SELECT * FROM " + SCORE_SNAPSHOT_GROUP_TABLE + " WHERE " + GOLDEN_COL + " = 'Y' AND " +  EQUIP_CAT_PK + " = ?");
		}

		return SCORE_GROUP_QUERY_BY_GOLDEN;
	}
	
	/**
	 * Create the prepared statement if it does not already exist.
	 *
	 * @return the prepared statement to query for machine snapshots by
	 *      type and time range
	 * @exception SQLException        Description of the Exception
	 * @throws java.sql.SQLException  if an exception occurs during a SQL evaluation
	 */
	protected PreparedStatement getScoreGroupQueryByTypeTimerangeStatement() throws SQLException {
		if ( SCORE_GROUP_QUERY_BY_TYPE_TIMERANGE == null ) {
			SCORE_GROUP_QUERY_BY_TYPE_TIMERANGE = _connection.prepareStatement( "SELECT * FROM " + SCORE_SNAPSHOT_GROUP_TABLE + " WHERE " + EQUIP_CAT_PK + " = ? AND " + DATE_COL + " > ? AND " + DATE_COL + " < ? order by " + DATE_COL );
		}

		return SCORE_GROUP_QUERY_BY_TYPE_TIMERANGE;
	}


	/**
	 * Create the prepared statement if it does not already exist.
	 *
	 * @return  the prepared statement to query for machine snapshots by
	 *      time range
	 * @exception SQLException        Description of the Exception
	 * @throws java.sql.SQLException  if an exception occurs during a SQL evaluation
	 */
	protected PreparedStatement getScoreGroupQueryByTimerangeStatement() throws SQLException {
		if ( SCORE_GROUP_QUERY_BY_TIMERANGE == null ) {
			SCORE_GROUP_QUERY_BY_TIMERANGE = _connection.prepareStatement( "SELECT * FROM " + SCORE_SNAPSHOT_GROUP_TABLE + " WHERE " + DATE_COL + " > ? AND " + DATE_COL + " < ?" );
		}

		return SCORE_GROUP_QUERY_BY_TIMERANGE;
	}
	
	/**
	 * Create the prepared statement if it does not already exist.
	 *
	 * @return   the prepared statement to query for machine snapshot group by
	 *      time and type
	 * @exception SQLException        Description of the Exception
	 * @throws java.sql.SQLException  if an exception occurs during a SQL evaluation
	 */
	protected PreparedStatement getScoreGroupQueryByTypeTimeStatement() throws SQLException {
		if ( SCORE_GROUP_QUERY_BY_TYPE_TIME == null ) {
			SCORE_GROUP_QUERY_BY_TYPE_TIME = _connection.prepareStatement( "SELECT * FROM " + SCORE_SNAPSHOT_GROUP_TABLE + " WHERE " + EQUIP_CAT_PK + " = ? AND " + DATE_COL + " = ?" );
			
		}

		return SCORE_GROUP_QUERY_BY_TYPE_TIME;
	}

	/**
	 * Create the prepared statement if it does not already exist. Filter for only the group signals that are marked as active.
	 * @return                        the prepared statement to query for a specified score group snapshot
	 * @exception SQLException        Description of the Exception
	 * @throws java.sql.SQLException  if an exception occurs during a SQL evaluation
	 */
	protected PreparedStatement getScoreRowQueryByTypeDateStatement() throws SQLException {
		if ( SCORE_ROW_QUERY_BY_TYPE_DATE == null ) {
			
			String mainQuery = "SELECT " + SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + "."+ EQUIP_CAT_PK +", " + SCORE_SNAPSHOT_SIGNAL_TABLE + "."+ DATE_COL +", " + SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + "."+ SP_PV_ID_COL +", " + SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + "."+ RB_PV_ID_COL +", " + SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + "."+ SYSTEM_COL + ", " + SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + "."+ SUBSYSTEM_COL + ", " + SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + "." + PV_DATA_TYPE_COL + ", " + SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + "." + USE_RB_FOR_SP_COL + ", " + SCORE_SNAPSHOT_SIGNAL_TABLE + "." + RB_PV_VAL_COL + ", " + SCORE_SNAPSHOT_SIGNAL_TABLE + "." + SP_PV_VAL_COL + " FROM " +  SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + ", " + SCORE_SNAPSHOT_SIGNAL_TABLE + " WHERE " + SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + "." + EQUIP_CAT_PK + "= ? AND " + SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + "." + GROUP_SIGNAL_ACTIVE_COL + " = 'Y' AND " + SCORE_SNAPSHOT_SIGNAL_TABLE + "." + DATE_COL + " = ? AND " + SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + "." + EQUIP_CAT_PK + "=" + SCORE_SNAPSHOT_SIGNAL_TABLE + "." + EQUIP_CAT_PK + " AND " + SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + "." + SP_PV_ID_COL + "  = " + SCORE_SNAPSHOT_SIGNAL_TABLE + "." + SP_PV_ID_COL + " AND " + SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + "." + RB_PV_ID_COL + " = " + SCORE_SNAPSHOT_SIGNAL_TABLE + "." + RB_PV_ID_COL;

			String unionQuery = " UNION SELECT " + SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + "."+ EQUIP_CAT_PK +", "+ "TO_DATE('', 'mm/dd/yyyy hh:mm:ss') " + DATE_COL + " , " + SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + "." + SP_PV_ID_COL + " , " + SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + "." + RB_PV_ID_COL + " , " + SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + "." + SYSTEM_COL +  ", " + SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + "." + SUBSYSTEM_COL +  ", " + SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + "." + PV_DATA_TYPE_COL + ", " + SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + "." + USE_RB_FOR_SP_COL + ", '' " + RB_PV_VAL_COL +  " , '' " + SP_PV_VAL_COL + " FROM " +  SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + " WHERE " + SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + "." + EQUIP_CAT_PK + "= ? AND " + SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + "." + GROUP_SIGNAL_ACTIVE_COL + " = 'Y' AND (" + SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + "." + SP_PV_ID_COL + ", " + SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + "." + RB_PV_ID_COL + ") NOT IN ";
			
			String subQuery = "( SELECT " + SCORE_SNAPSHOT_SIGNAL_TABLE + "." + SP_PV_ID_COL + " , " + SCORE_SNAPSHOT_SIGNAL_TABLE + "." + RB_PV_ID_COL + " FROM " + SCORE_SNAPSHOT_SIGNAL_TABLE + " WHERE " + SCORE_SNAPSHOT_SIGNAL_TABLE + "." + EQUIP_CAT_PK + " = ? AND " + SCORE_SNAPSHOT_SIGNAL_TABLE + "." + DATE_COL + " = ? " + ")";		

			SCORE_ROW_QUERY_BY_TYPE_DATE = _connection.prepareStatement(mainQuery + unionQuery+ subQuery);
		}
		
		return SCORE_ROW_QUERY_BY_TYPE_DATE;
	}

	/**
	 * Create the prepared statement if it does not already exist. Filter for only the group signals that are marked as active.
	 * @return                        the prepared statement to query for available snapshot types
	 * @exception SQLException        Description of the Exception
	 * @throws java.sql.SQLException  if an exception occurs during a SQL evaluation
	 */
	protected PreparedStatement getScoreGroupTypeQueryStatement() throws SQLException {
		if ( SCORE_GROUP_TYPE_QUERY == null ) {
			SCORE_GROUP_TYPE_QUERY = _connection.prepareStatement( "SELECT " + EQUIP_CAT_PK + " FROM " + SCORE_SNAPSHOT_GROUP_SIGNAL_TABLE + " WHERE " + GROUP_SIGNAL_ACTIVE_COL + " = 'Y' GROUP BY " +  EQUIP_CAT_PK + " ORDER BY " + EQUIP_CAT_PK );
		}

		return SCORE_GROUP_TYPE_QUERY;
	}

	/**
	 * Publish the channel snapshot and associate it with the machine snapshot given by the
	 * machine snapshop id.
	 *
         * @param row scoreRow data
         * @param groupId    The unique id of the associated machine snapshot
         * @param time  The timestamp when the snapshot was taken.
         * 
	 * @exception StateStoreException                      Description of the Exception
	 * @throws gov.sns.apps.score.StateStoreException  if a SQL exception is thrown
	 */
	public void publish( final ScoreRow row, final String groupId,  final Timestamp time) throws StateStoreException {
		String rbName = row.getRBName();
		if(rbName == null) rbName = "NA";
		String spName = row.getSPName();
		if(spName == null) spName = "NA";		

		try {
			getScoreRowInsertStatement();
			SCORE_ROW_INSERT.setString( 1, groupId );
			SCORE_ROW_INSERT.setTimestamp( 2, time );
			SCORE_ROW_INSERT.setString( 3, spName);
			SCORE_ROW_INSERT.setString( 4, row.getSPValueAsString());
			SCORE_ROW_INSERT.setString( 5, rbName );
			SCORE_ROW_INSERT.setString( 6, row.getRBValueAsString());
			SCORE_ROW_INSERT.addBatch();
		}
		// reconnect attempt not needed. This is only called from publish(snapshot) which forces reconnect if needed.
		catch ( SQLException exception ) {
			_theDoc.dumpErr("Database error - try reconnecting");
			throw new StateStoreException( "Error publishing a channel snapshot.", exception );
		}
	}


	/**
	 * Publish the machine snapshot.
	 *
	 * @param machineSnapshot   -The machine snapshot to publish.
         * @return true if no exception thrown
         *    
	 * @exception StateStoreException                      Description of the Exception
	 * @throws gov.sns.apps.score.StateStoreException  if a SQL exception is thrown
	 */
	public boolean publish( final ScoreSnapshot machineSnapshot ) throws StateStoreException {
		Timestamp time = machineSnapshot.getTimestamp();
		String type = machineSnapshot.getType();
		String user = _dictionary.getUser();

		try {   
			getScoreSnapshotInsertStatement();
			getScoreRowInsertStatement();

			SCORE_SNAPSHOT_INSERT.setString( 1, type );
			SCORE_SNAPSHOT_INSERT.setTimestamp( 2, time );
			SCORE_SNAPSHOT_INSERT.setString( 3, machineSnapshot.getComment() );
			SCORE_SNAPSHOT_INSERT.setString( 4, user);
			SCORE_SNAPSHOT_INSERT.setObject( 5, "N", java.sql.Types.CHAR);
			SCORE_SNAPSHOT_INSERT.executeUpdate();
			
			final ScoreRow[] rows  = machineSnapshot.getScoreRows();
			for ( int index = 0; index < rows.length; index++ ) {
				ScoreRow row  = rows[index];
				if ( row != null ) {
					publish(row, type, time);
				}
			}
			SCORE_ROW_INSERT.executeBatch();
			_connection.commit();
			return true;
		}
		catch ( SQLException exception ) {
			switch ( exception.getErrorCode() ) {
				case 2396:
					_theDoc.dumpErr("Database error - try reconnecting");
					if ( reconnect() ) {	// attempt to reconnect once
						_theDoc.dumpErr( "Reconnection successful" );
						publish( machineSnapshot );
					}
					else {
						_theDoc.dumpErr("Reconnection unsuccessful");
						throw new StateStoreException( "Error fetching the machine snapshots in range.", exception );
					}
					break;
				case 1031:
					_theDoc.dumpErr( "Insufficient database privilege for this user" );
					throw new StateStoreException( "Insufficient database privilege for this user.", exception );
				default:
					_theDoc.dumpErr("Database problem on snapshot save ");
					throw new StateStoreException( "Error publishing a machine snapshot.", exception );
			}
			
			 return false;
		}
	}


	/**
	 * Fetch an array of logger types
	 *
	 * @return  an array of available logger types
	 * @exception StateStoreException                      Description of the Exception
	 * @throws gov.sns.apps.score.StateStoreException  if a SQL exception is thrown
	 */
	public String[] fetchTypes() throws StateStoreException {
		try {   
			getScoreGroupTypeQueryStatement();

			List<String> types = new ArrayList<String>();
			ResultSet result = SCORE_GROUP_TYPE_QUERY.executeQuery();
			while ( result.next() ) {
				types.add( result.getString( EQUIP_CAT_PK) );
			}
			return types.toArray( new String[types.size()] );
		}
		catch ( SQLException exception ) {
			// check for connection timeout:
			if(exception.getErrorCode() == 2396) {
				_theDoc.dumpErr("Database error - try reconnecting");
				// attempt 1 reconnect
				if (reconnect()) {
					_theDoc.dumpErr("Reconnection successful");
					return fetchTypes();
				}
				else {
					_theDoc.dumpErr("Reconnection unsuccessful");
					throw new StateStoreException( "Error fetching the machine snapshots in range.", exception );
				}
			}
			 else {
				 throw new StateStoreException( "Error fetching score types.", exception );
			 }
		}
	}


	/**
	 * Fetch an empty ScoreGroup by name. T
	 * his can be used to construct the underlying structure (PVData) 
	 * used in score)
	 *
	 * @param type     the score type
	 * @return         a score group 
	 * @exception StateStoreException       Description of the Exception
	 * @throws gov.sns.apps.score.StateStoreException  if a SQL exception is thrown
	 */
	public ScoreGroup fetchGroup( final String type ) throws StateStoreException {
		try {
			getScoreRowTemplateQueryStatement();
			
			final List<ScoreRow> scoreRows = new ArrayList<ScoreRow>();
			SCORE_ROW_QUERY.setString( 1, type );
			final ResultSet result = SCORE_ROW_QUERY.executeQuery();
			while ( result.next() ) {
				String spName = result.getString( SP_PV_ID_COL);
				if(spName.equals("NA") ) spName = null;
				String rbName = result.getString( RB_PV_ID_COL);				
				if(rbName.equals("NA") ) rbName = null;
				final String dataTypeID = result.getString( PV_DATA_TYPE_COL );
				final DataTypeAdaptor dataTypeAdaptor = DataTypeAdaptor.adaptorForType( dataTypeID );
				final String system = result.getString( SYSTEM_COL );
				final String subsystem = result.getString( SUBSYSTEM_COL );
				final String useReadbackForSetpointFlag = result.getString( USE_RB_FOR_SP_COL );
				
				final ScoreRow row = new ScoreRow( system, subsystem, dataTypeAdaptor, rbName, spName, useReadbackForSetpointFlag );
				scoreRows.add(row);
			}			
			
			return new ScoreGroup( type, scoreRows.toArray(new ScoreRow[scoreRows.size()]) );
			
		}
		catch ( SQLException exception ) {
			// check for connection timeout:
			if(exception.getErrorCode() == 2396) {
				_theDoc.dumpErr("Database error - try reconnecting");
				// attempt 1 reconnect
				if (reconnect()) {
					_theDoc.dumpErr("Reconnection successful");
					return fetchGroup(type);
				}
				else {
					_theDoc.dumpErr("Reconnection unsuccessful");
					throw new StateStoreException( "Error fetching the machine snapshots in range.", exception );
				}
			}
			 else {			
				 throw new StateStoreException( "Error fetching pvlogger group for the specified type.", exception );
			 }
		}
	}


	/**
	 * Fetch the specific snapshot associated with a time and equipment_id
	 *
	 * @param type   - the score group label
	 * @param time - the timestamp  for the desired snapshot
	 * @return      The score snapshop read from the
	 *      persistent store.
	 * @exception StateStoreException  - Description of the Exception
	 * @throws gov.sns.apps.score.StateStoreException  if a SQL exception is thrown
	 */
	public ScoreSnapshot fetchScoreSnapshot( final String type, final Timestamp time ) throws StateStoreException {
		// get the group information first
		String comment;
		String spValString, rbValString;			//SCORE_GROUP_QUERY_BY_TYPE_TIME.setTimestamp( 2, time);
                Time time2 = new Time(time.getTime());
                //System.out.println(time + "  " + time2);
		comment = "";
		try {
			getScoreGroupQueryByTypeTimeStatement();
			SCORE_GROUP_QUERY_BY_TYPE_TIME.setString(1,type);
			SCORE_GROUP_QUERY_BY_TYPE_TIME.setTimestamp( 2, time);
            //            SCORE_GROUP_QUERY_BY_TYPE_TIME.setTime( 2, time2);
			ResultSet snapshotResult = SCORE_GROUP_QUERY_BY_TYPE_TIME.executeQuery();
			if ( snapshotResult.next() ) {
				comment = snapshotResult.getString(DESCR_COL);
			}
		}
		catch ( SQLException exception ) {
			// check for connection timeout:
			if(exception.getErrorCode() == 2396) {
				_theDoc.dumpErr("Database error - try reconnecting");
				// attempt 1 reconnect
				if (reconnect()) {
					_theDoc.dumpErr("Reconnection successful");
					return fetchScoreSnapshot(type, time);
				}
				else {
					_theDoc.dumpErr("Reconnection unsuccessful");
					throw new StateStoreException( "Error fetching the machine snapshots in range.", exception );
				}
			}
			 else {			
				 throw new StateStoreException( "Error fetching the machine goup info for a snapshot.", exception );
			 }
		}
		
		// now get the associated rows
		try {
			getScoreRowQueryByTypeDateStatement();
			SCORE_ROW_QUERY_BY_TYPE_DATE.clearParameters();
			SCORE_ROW_QUERY_BY_TYPE_DATE.setFetchSize(1000);
			SCORE_ROW_QUERY_BY_TYPE_DATE.setString(1,type);
			//SCORE_ROW_QUERY_BY_TYPE_DATE.setTime( 2, time2);
			SCORE_ROW_QUERY_BY_TYPE_DATE.setTimestamp( 2, time);
			SCORE_ROW_QUERY_BY_TYPE_DATE.setString(3,type);
			SCORE_ROW_QUERY_BY_TYPE_DATE.setString(4,type);
			//SCORE_ROW_QUERY_BY_TYPE_DATE.setTime( 5, time2);
			SCORE_ROW_QUERY_BY_TYPE_DATE.setTimestamp( 5, time);
			
			ResultSet result = SCORE_ROW_QUERY_BY_TYPE_DATE.executeQuery();
			
			final List<ScoreRow> scoreRows = new ArrayList<ScoreRow>();
			while ( result.next() ) {
				final String sys = result.getString( SYSTEM_COL);
				final String subSys = result.getString( SUBSYSTEM_COL);
				String spName = result.getString(SP_PV_ID_COL);
				if(spName.equals("NA")) spName = null;
				final String dataTypeID = result.getString( PV_DATA_TYPE_COL );
				final DataTypeAdaptor dataTypeAdaptor = DataTypeAdaptor.adaptorForType( dataTypeID );
				String rbName = result.getString(RB_PV_ID_COL);
				if(rbName.equals("NA")) rbName = null;				
				final String useRB = result.getString(USE_RB_FOR_SP_COL);
				rbValString = result.getString(RB_PV_VAL_COL);
				spValString = result.getString(SP_PV_VAL_COL);
				final ScoreRow row = ScoreRow.getInstanceWithStringRepValues( sys, subSys, dataTypeAdaptor, rbName, rbValString, spName, spValString, useRB );
				scoreRows.add( row );
			}
			System.out.println("size = " + scoreRows.size());
			return new ScoreSnapshot( type, time, comment, scoreRows.toArray( new ScoreRow[scoreRows.size()]) );
			
		}
		// unlikely  a reconnect needed here:
		catch ( SQLException exception ) {
			_theDoc.dumpErr("Database error - try reconnecting");			
			throw new StateStoreException( "Error fetching the machine snapshot rows.", exception );
		}
	}
	
        /**
         * Fetch the golden snapshot associated with an equipment_id
         *
         * @param type   - the score group label
         * @return      The score snapshop read from the persistent store.
         *    or null if there is no golden set yet.
         * @exception StateStoreException  - Description of the Exception
         * @throws gov.sns.apps.score.StateStoreException  if a SQL exception is thrown
         */
        public ScoreSnapshot fetchGoldenSnapshot( final String type) throws StateStoreException {
                // get the group information first
//              String comment, sys, subSys, spName, rbName, useRB;
                String comment;
//              String spValString, rbValString;
                ArrayList<String> al = new ArrayList<String>();
                comment = "";
                Timestamp time;
                try {
                        getScoreGroupQueryByGoldenStatement() ;
                        SCORE_GROUP_QUERY_BY_GOLDEN.setString(1,type);
                        ResultSet snapshotResult = SCORE_GROUP_QUERY_BY_GOLDEN.executeQuery();
                        if ( snapshotResult.next() ) {
                                comment = snapshotResult.getString(DESCR_COL);
                                time = snapshotResult.getTimestamp(DATE_COL);
                        }
                        else {
                                return null;
                        }
                }
                catch ( SQLException exception ) {
                        // check for connection timeout:
                        if(exception.getErrorCode() == 2396) {
                                _theDoc.dumpErr("Database error - try reconnecting");
                                // attempt 1 reconnect
                                if (reconnect()) {
                                        _theDoc.dumpErr("Reconnection successful");
                                        return fetchGoldenSnapshot(type);
                                }
                                else {
                                        _theDoc.dumpErr("Reconnection unsuccessful");
                                        throw new StateStoreException( "Error fetching the machine snapshots in range.", exception );
                                }
                        }
                         else {                 
                                 throw new StateStoreException( "Error fetching the machine goup info for a snapshot.", exception );
                         }
                }
                
                try {
                        return fetchScoreSnapshot(type, time);
                }
                catch ( Exception exception ) {         
                        throw new StateStoreException( "Error fetching the machine snapshot rows.", exception );
                }
        }

	/**
	 * Fetch the machine snapshots 
	 *
	 * @param type    the score group label
	 * @param date1  initial time of time interval
	 * @param date2  final time of time interval
	 * @return The score snapshop read from the persistent store.
	 *      
	 * @throws gov.sns.apps.score.StateStoreException  if a SQL exception is thrown
	 */ 
	public List<ScoreSnapshot> fetchScoreSnapshotsInRange( final String type, final java.util.Date date1, final java.util.Date date2 ) throws StateStoreException {
		// get the group information first
		String comment, user;
//	        String comment;
		java.util.Date date;
		Timestamp time1 = new Timestamp( date1.getTime());
		Timestamp time2 = new Timestamp( date2.getTime());
		Timestamp time;
		final List<ScoreSnapshot> snapshots = new ArrayList<ScoreSnapshot>();
		
		try {
			getScoreGroupQueryByTypeTimerangeStatement();
			SCORE_GROUP_QUERY_BY_TYPE_TIMERANGE.setString(1,type);
			SCORE_GROUP_QUERY_BY_TYPE_TIMERANGE.setTimestamp( 2, time1);
			SCORE_GROUP_QUERY_BY_TYPE_TIMERANGE.setTimestamp( 3, time2);			
			ResultSet snapshotResult = SCORE_GROUP_QUERY_BY_TYPE_TIMERANGE.executeQuery();
			while ( snapshotResult.next() ) {
				comment = snapshotResult.getString(DESCR_COL);
				user = snapshotResult.getString(USER_COL);
				time = snapshotResult.getTimestamp(DATE_COL);
				snapshots.add( new ScoreSnapshot( type, time, comment ) );
			}
			return snapshots;		
			
		}
		catch ( SQLException exception ) {
			// check for connection timeout:
			if(exception.getErrorCode() == 2396) {
				_theDoc.dumpErr("Database error - try reconnecting");
				// attempt 1 reconnect
				if (reconnect()) {
					_theDoc.dumpErr("Reconnection successful");
					return fetchScoreSnapshotsInRange(type, date1, date2);
				}
				else {
					_theDoc.dumpErr("Reconnection unsuccessful");
					throw new StateStoreException( "Error fetching the machine snapshots in range.", exception );
				}
			}
			 else {
					throw new StateStoreException( "Error fetching the machine snapshots in range.", exception );
				}
		}
	}
}

