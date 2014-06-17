/*
 *  BrowserModel.java
 *
 *  Created on Thu Feb 19 16:50:29 EST 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.dbbrowser;

import xal.tools.database.*;
import xal.tools.messaging.MessageCenter;

import java.sql.*;
import java.util.*;


/**
 * BrowserModel
 * @author   tap
 */
public class BrowserModel {
	/** database adaptor */
	protected DatabaseAdaptor _databaseAdaptor;
	
	/** current database connection */
	protected Connection _connection;

	/** message distribution center */
	protected MessageCenter _messageCenter;
	
	/** notification proxy */
	protected BrowserModelListener _proxy;
	
	/** list of schemas in the database */
	protected List<String> _schemas;
	
	/** current schema selection */
	protected String _schema;
	
	/** list of tables in the schema selection */
	protected List<String> _tables;
	
	/** current table selection */
	protected String _table;
	
	/** list of table attributes */
	protected List<TableAttribute> _tableAttributes;

	/** user associated with the database connection */
	protected String _user;
	
	/** URL of the database */
	protected String _databaseURL;

	/** indication of whether the database connection has been established */
	private boolean _hasConnected = false;


	/** BrowserModel constructor  */
	public BrowserModel() {
		_messageCenter = new MessageCenter( "Browser Model" );
		_proxy = _messageCenter.registerSource( this, BrowserModelListener.class );

		_user = "";
		_databaseURL = "";

		_databaseAdaptor = DatabaseAdaptor.getInstance();
		resetData();
	}


	/**
	 * Get the database adaptor for making connections.
	 * @return   the database adaptor for making connection.
	 */
	DatabaseAdaptor getDatabaseAdaptor() {
		return _databaseAdaptor;
	}


	/** Reset the data to what it is before any fetches  */
	protected void resetData() {
		_schema = null;
		_schemas = Collections.<String>emptyList();
		_table = null;
		_tables = Collections.<String>emptyList();
		_tableAttributes = Collections.<TableAttribute>emptyList();
	}


	/**
	 * Connect to the database with the default connection dictionary
	 * @throws DatabaseException  if the connection or schema fetch fails
	 */
	public void connect() throws DatabaseException {
		connect( ConnectionDictionary.getPreferredInstance( "reports" ) );
	}


	/**
	 * Connect to the database with the specified connection dictionary
	 * @param dictionary          The connection dictionary
	 * @throws DatabaseException  if the connection or schema fetch fails
	 */
	public void connect( final ConnectionDictionary dictionary ) throws DatabaseException {
		Connection connection = _databaseAdaptor.getConnection( dictionary );
		setDatabaseConnection( connection, dictionary );
	}
	
	
	/**
	 * Get the database connection.
	 * @return the database connection
	 */
	public Connection getDatabaseConnection() {
		return _connection;
	}


	/**
	 * Set the connection to the one supplied and also update the connection information from the supplied connection dictionary.
	 * @param connection  the new database connection
	 * @param dictionary  the dictionary supplying connection information
	 */
	public void setDatabaseConnection( Connection connection, ConnectionDictionary dictionary ) {
		_hasConnected = false;
		_user = null;
		_databaseURL = null;

		_connection = connection;
		_hasConnected = true;
		_user = dictionary.getUser();
		_databaseURL = dictionary.getURLSpec();

		resetData();
		_schemas = fetchSchemas();
		_proxy.connectionChanged( this );
	}


	/**
	 * Determine if we have successfully connected to the database. Note that this does not mean that the database connection is still valid.
	 * @return   true if we have successfully connected to the database and false if not
	 */
	public boolean hasConnected() {
		return _hasConnected;
	}


	/**
	 * Get the connected user
	 * @return   the connected user
	 */
	public String getUser() {
		return _user;
	}


	/**
	 * Get the connected database URL
	 * @return   the connected database
	 */
	public String getDatabaseURL() {
		return _databaseURL;
	}


	/**
	 * Add a listener of browser model events
	 * @param listener  A listener to receive browser model events
	 */
	public void addBrowserModelListener( BrowserModelListener listener ) {
		_messageCenter.registerTarget( listener, this, BrowserModelListener.class );
	}


	/**
	 * Remove the listener from receiving browser model events
	 * @param listener  The listener to remove from receiving events
	 */
	public void removeBrowserModelListener( BrowserModelListener listener ) {
		_messageCenter.removeTarget( listener, this, BrowserModelListener.class );
	}


	/**
	 * Set the database schema to be this model's selected schema and fetch its tables
	 * @param newSchema                                  The new database schema
	 * @exception DatabaseException                      Description of the Exception
	 * @throws xal.tools.database.DatabaseException  if the table fetch fails
	 */
	public void setSchema( final String newSchema ) throws DatabaseException {
		if ( newSchema != _schema ) {
			_schema = newSchema;
			_table = null;
			_tableAttributes = Collections.<TableAttribute>emptyList();
			_tables = fetchTables( _schema );
			_proxy.schemaChanged( this, _schema );
		}
	}


	/**
	 * Get this model's selected schema
	 * @return   This model's selected schema
	 */
	public String getSchema() {
		return _schema;
	}


	/**
	 * Set the database table to be this model's selected table and fetch its attributes
	 * @param newTable                                   The table to become this model's selected table
	 * @exception DatabaseException                      Description of the Exception
	 * @throws xal.tools.database.DatabaseException  if the attributes fetch fails
	 */
	public void setTable( String newTable ) throws DatabaseException {
		if ( newTable != _table && newTable != null ) {
			_table = newTable;
			setTableAttributes( fetchAttributes( _schema, _table ) );
			_proxy.tableChanged( this, _table );
		}
	}


	/**
	 * Get this model's selected database table name
	 * @return   this model's selected database table name
	 */
	public String getTable() {
		return _table;
	}


	/**
	 * Get the list of database table names for this model's selected schema
	 * @return   the list of database table names for this model's selected schema
	 */
	public List<String> getTables() {
		return _tables;
	}


	/**
	 * Set the table attributes for this model's selected table
	 * @param tableAttributes  The table attributes
	 */
	protected void setTableAttributes( final List<TableAttribute> tableAttributes ) {
		_tableAttributes = tableAttributes;
	}


	/**
	 * Get the table attributes for this model's selected table
	 * @return   the table attributes for this model's selected table
	 */
	public List<TableAttribute> getTableAttributes() {
		return _tableAttributes;
	}


	/**
	 * Get the list of schemas for this model's database connection
	 * @return   the list of schemas
	 */
	public List<String> getSchemas() {
		return _schemas;
	}
	
	
	/**
	 * Fetch nontrivial schemas from the connected database
	 * @return  list of nontrivial schemas in the database
	 * @exception DatabaseException                      Description of the Exception
	 * @throws xal.tools.database.DatabaseException  if the schema fetch fails
	 */
	public List<String> fetchSchemas() throws DatabaseException {
		return _databaseAdaptor.fetchNontrivialSchemas( _connection );
	}
	

	/**
	 * Fetch all schemas from the connected database
	 * @return  list of all schemas in the database
	 * @exception DatabaseException
	 * @throws xal.tools.database.DatabaseException  if the schema fetch fails
	 */
	public List<String> fetchAllSchemas() throws DatabaseException {
		return _databaseAdaptor.fetchAllSchemas( _connection );
	}


	/**
	 * Fetch tables for the specified schema
	 * @param schema                                     The schema for which to fetch the tables
	 * @return                                           a list of tables associated with the specified schema
	 * @exception DatabaseException                      Description of the Exception
	 * @throws xal.tools.database.DatabaseException  if the schema fetch fails
	 */
	public List<String> fetchTables( final String schema ) throws DatabaseException {
		return _databaseAdaptor.fetchTables( _connection, schema );
	}


	/**
	 * Fetch table attributes for the database table within the schema
	 * @param schema                 The schema to use
	 * @param table                  The table for which to fetch the columns
	 * @return                       a list of tables associated with the specified schema
	 * @exception DatabaseException  Description of the Exception
	 */
	public List<TableAttribute> fetchAttributes( final String schema, final String table ) throws DatabaseException {
		try {
			final List<TableAttribute> attributes = new ArrayList<TableAttribute>();
			final DatabaseMetaData metaData = _connection.getMetaData();
			final ResultSet result = _databaseAdaptor.getColumnsResultSet( metaData, schema, table );
			while ( result.next() ) {
				final TableAttribute attribute = new TableAttribute();
				attribute.name = result.getString( "COLUMN_NAME" );
				attribute.dataType = result.getInt( "DATA_TYPE" );
				attribute.type = result.getString( "TYPE_NAME" );
				attribute.width = result.getInt( "COLUMN_SIZE" );
				attribute.nullable = result.getString( "IS_NULLABLE" );
				attributes.add( attribute );
			}
			result.close();

			final List<String> primaryKeys = fetchPrimaryKeys( schema, table );
			for ( final TableAttribute attribute : attributes ) {
				attribute.isPrimaryKey = primaryKeys.contains( attribute.name );
			}

			return attributes;
		}
		catch ( SQLException exception ) {
			throw new DatabaseException( "Database exception while fetching schemas.", _databaseAdaptor, exception );
		}
	}


	/**
	 * Fetch the primary keys for a specified table in a specified schema
	 * @param schema                 The schema to use
	 * @param table                  The table for which to fetch the primary keys
	 * @return                       a list of the primary keys as column names
	 * @exception DatabaseException  Description of the Exception
	 */
	protected List<String> fetchPrimaryKeys( final String schema, final String table ) throws DatabaseException {
		return _databaseAdaptor.fetchPrimaryKeys( _connection, schema, table );
	}


	/**
	 * Fetch records from the present schema-table and key the record's values by the column names
	 * @return   The list of records
	 */
	public List<Map<String,Object>> fetchRecords() {
		try {
			final List<Map<String,Object>> records = new ArrayList<Map<String,Object>>();
			final Statement statement = _connection.createStatement();
			final String fullTableName = _schema + "." + _table;
			final ResultSet resultSet = statement.executeQuery( "select * from " + fullTableName );
			final ResultSetMetaData metaData = resultSet.getMetaData();
			
			final int columnCount = metaData.getColumnCount();
			final int[] columnTypes = new int[columnCount];
			
			for ( int column = 0 ; column < columnCount ; column++ ) {
				columnTypes[column] = metaData.getColumnType( column + 1 );
			}
			
			final List<TableAttribute> attributes = getTableAttributes();
			while ( resultSet.next() ) {
				final Map<String,Object> record = new HashMap<String,Object>();
				for ( int column = 1; column <= columnCount; column++ ) {
					final TableAttribute attribute = attributes.get( column - 1 );
					final String columnName = attribute.name;
					record.put( attribute.name, QueryModel.getValue( resultSet, column, columnTypes[column-1] ) );
				}
				records.add( record );
			}
			resultSet.close();
			statement.close();

			return records;
		}
		catch ( SQLException exception ) {
			throw new DatabaseException( "Database exception while fetching primary keys.", _databaseAdaptor, exception );
		}
	}
}

