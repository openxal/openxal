/*
 * DatabaseAdaptor.java
 *
 * Created on Wed Feb 18 13:50:00 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.tools.database;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.*;


/**
 * DatabaseAdaptor provides a generic adaptor to wrap database specific code.  For example, making
 * a SQL array requires database specific code.
 *
 * @author  tap
 */
public abstract class DatabaseAdaptor {
	/**
	 * Instantiate an empty Blob.
	 * @param connection the database connection
	 * @return a new instance of a Blob appropriate for this adaptor.
	 */
	public Blob newBlob( final Connection connection ) {
		return new ConcreteBlob();
	}
	
	
	/**
	 * Get an SQL Array given an SQL array type, connection and a primitive array
	 * @param type An SQL array type identifying the type of array
	 * @param connection An SQL connection
	 * @param array The primitive Java array
	 * @return the SQL array which wraps the primitive array
	 * @throws xal.tools.database.DatabaseException if a database exception is thrown
	 */
	abstract public Array getArray( final String type, final Connection connection, final Object array ) throws DatabaseException;
	
	
	/**
	 * Get a new database connection
	 * @param urlSpec The URL to which to connect
	 * @param user The user loggin into the database
	 * @param password the user's password
	 * @throws xal.tools.database.DatabaseException if a database exception is thrown
	 */
	public Connection getConnection( final String urlSpec, final String user, final String password ) throws DatabaseException {
		try {
			return DriverManager.getConnection( urlSpec, user, password );
		}
		catch( SQLException exception ) {
			Logger.getLogger("global").log( Level.SEVERE, "Error connecting to the database at URL: \"" + urlSpec + "\" as user: " + user , exception );
			throw new DatabaseException( "Exception connecting to the database.", this, exception );
		}
	}
	
	
	/**
	 * Get a new database connection
	 * @param dictionary A connection dictionary
	 * @throws xal.tools.database.DatabaseException if a database exception is thrown
	 */
	public Connection getConnection( final ConnectionDictionary dictionary ) throws DatabaseException {
		return getConnection( dictionary.getURLSpec(), dictionary.getUser(), dictionary.getPassword() );
	}
	
	
	/**
	 * Get a new database adaptor using the default database adaptor. Site specific code should provide an implementation of this method using the site specific database drivers.
	 * @return return a new instance of the default database adaptor (OracleDatabaseAdaptor)
	 */
	static public DatabaseAdaptor getInstance() {
		final DBConfiguration dbConfiguration = DBConfiguration.getInstance();
		return dbConfiguration != null ? dbConfiguration.getDefaultDatabaseAdaptor() : null;
	}

	
	/**
	 * Fetch all schemas from the connected database
	 * @return  list of all schemas in the database
	 * @exception DatabaseException
	 * @throws xal.tools.database.DatabaseException  if the schema fetch fails
	 */
	public List<String> fetchAllSchemas( final Connection connection ) throws DatabaseException {
		try {
			final List<String> schemas = new ArrayList<String>();
			final DatabaseMetaData metaData = connection.getMetaData();
			final ResultSet result = metaData.getSchemas();
			while ( result.next() ) {
				schemas.add( result.getString( "TABLE_SCHEM" ) );
			}
			result.close();
			return schemas;
		}
		catch ( SQLException exception ) {
			throw new DatabaseException( "Database exception while fetching schemas.", this, exception );
		}
	}
	
	
	/**
	 * Fetch the list of nontrivial schemas. By default, fetch all schemas. Driver specific subclasses should provide a more accurate implementation.
	 * @param connection database connection
	 * @return list of nontrivial schema names
	 */
	public List<String> fetchNontrivialSchemas( final Connection connection ) throws DatabaseException {
		return fetchAllSchemas( connection );
	}


	/**
	 * Fetch tables for the specified schema
	 * @param connection         The database connection
	 * @param schema             The schema for which to fetch the tables
	 * @return                   a list of tables associated with the specified schema
	 * @exception DatabaseException            
	 * @throws xal.tools.database.DatabaseException  if the schema fetch fails
	 */
	public List<String> fetchTables( final Connection connection, final String schema ) throws DatabaseException {
		try {
			final List<String> tables = new ArrayList<String>();
			final DatabaseMetaData metaData = connection.getMetaData();
			final ResultSet result = getTablesResultSet( metaData, schema );
			while ( result.next() ) {
				String name = result.getString( "TABLE_NAME" );
				tables.add( name );
			}
			result.close();
			return tables;
		}
		catch ( SQLException exception ) {
			throw new DatabaseException( "Database exception while fetching schemas.", this, exception );
		}
	}


	/** Get the result set of tables for the specified meta data and schema */
	public ResultSet getTablesResultSet( final DatabaseMetaData metaData, final String schema ) throws SQLException {
		return metaData.getTables( null, schema, null, null );
	}


	/** Get the result set of columns for the specified meta data, schema and table */
	public ResultSet getColumnsResultSet( final DatabaseMetaData metaData, final String schema, final String table ) throws SQLException {
		return metaData.getColumns( null, schema, table, null );
	}


	/**
	 * Fetch the primary keys for a specified table in a specified schema
	 * @param connection         The database connection
	 * @param schema             The schema to use
	 * @param table              The table for which to fetch the primary keys
	 * @return                    a list of the primary keys as column names
	 * @exception DatabaseException  Description of the Exception
	 */
	public List<String> fetchPrimaryKeys( final Connection connection, final String schema, final String table ) throws DatabaseException {
		try {
			final List<String> primaryKeys = new ArrayList<String>();
			final DatabaseMetaData metaData = connection.getMetaData();
			final ResultSet result = getPrimaryKeysResultSet( metaData, schema, table );
			while ( result.next() ) {
				final String column = result.getString( "COLUMN_NAME" );
				primaryKeys.add( column );
			}
			result.close();
			return primaryKeys;
		}
		catch ( SQLException exception ) {
			throw new DatabaseException( "Database exception while fetching primary keys.", this, exception );
		}
	}


	/** Get the result set of primary keys for the specified meta data, schema and table */
	public ResultSet getPrimaryKeysResultSet( final DatabaseMetaData metaData, final String schema, final String table ) throws SQLException {
		return metaData.getPrimaryKeys( null, schema, table );
	}
}

