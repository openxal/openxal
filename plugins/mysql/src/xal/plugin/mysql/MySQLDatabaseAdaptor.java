package xal.plugin.mysql;

import xal.tools.database.*;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MySQLDatabaseAdaptor extends DatabaseAdaptor {
	/** Table of cached array descriptors keyed by type. The value class is actually oracle.sql.ArrayDescriptor, but Object is used since the Oracle driver is reflected. */
	final private Map<String,Object> ARRAY_DESCRIPTOR_TABLE;

	/**
	 * Public Constructor
	 */
	public MySQLDatabaseAdaptor() {
		ARRAY_DESCRIPTOR_TABLE = new HashMap<String,Object>();
	}

	
	@Override
	public Array getArray(String type, Connection connection, Object array)
			throws DatabaseException {
		// TODO Auto-generated method stub
//		try {
//			final ArrayDescriptor descriptor = getArrayDescriptor(type, connection);
//			return new ARRAY(descriptor, connection, array);
//		}
//		catch(SQLException exception) {
//			Logger.getLogger("global").log( Level.SEVERE, "Error instantiating an SQL array of type: " + type, exception );
//			throw new DatabaseException("Exception generating an SQL array.", this, exception);
//		}
		
		return null;
	}

	@Override
	public Connection getConnection(String urlSpec, String user, String password)
			throws DatabaseException {
		try {
			return DriverManager.getConnection(urlSpec, user, password);
		}
		catch(SQLException exception) {
			Logger.getLogger("global").log( Level.SEVERE, "Error connecting to the database at URL: \"" + urlSpec + "\" as user: " + user , exception );
			throw new DatabaseException("Exception connecting to the database.", this, exception);
		}
	}

	
	/**
	 * Fetch all schemas from the connected database. MySQL adaptor returns catalogs instead of schemas.
	 * @return  list of all schemas in the database
	 * @exception DatabaseException
	 * @throws gov.sns.tools.database.DatabaseException  if the schema fetch fails
	 */
	public List<String> fetchAllSchemas( final Connection connection ) throws DatabaseException {
		try {
			final List<String> schemas = new ArrayList<String>();
			final DatabaseMetaData metaData = connection.getMetaData();
			final ResultSet result = metaData.getCatalogs();

			while ( result.next() ) {
				schemas.add( result.getString( "TABLE_CAT" ) );
			}
			result.close();
			return schemas;
		}
		catch ( SQLException exception ) {
			throw new DatabaseException( "Database exception while fetching schemas.", this, exception );
		}
	}


	/** Get the result set for tables for the specified meta data and schema. MySQL adaptor uses the catalog in place of schema. */
	public ResultSet getTablesResultSet( final DatabaseMetaData metaData, final String schema ) throws SQLException {
		return metaData.getTables( schema, null, null, null );
	}


	/** Get the result set of columns for the specified meta data, schema and table. MySQL adaptor uses the catalog in place of schema. */
	public ResultSet getColumnsResultSet( final DatabaseMetaData metaData, final String schema, final String table ) throws SQLException {
		return metaData.getColumns( schema, null, table, null );
	}


	/** Get the result set of primary keys for the specified meta data, schema and table. MySQL adaptor uses the catalog in place of schema. */
	public ResultSet getPrimaryKeysResultSet( final DatabaseMetaData metaData, final String schema, final String table ) throws SQLException {
		return metaData.getPrimaryKeys( schema, null, table );
	}

	
//	/**
//	 * Get the array descriptor for the specified array type
//	 * @param type An SQL array type
//	 * @param connection A database connection
//	 * @return the array descriptor for the array type
//	 * @throws java.sql.SQLException if a database exception is thrown
//	 */
//	private ArrayDescriptor getArrayDescriptor(final String type, final Connection connection) throws SQLException {
//		if ( arrayDescriptorMap.containsKey(type) ) {
//			return (ArrayDescriptor)arrayDescriptorMap.get(type);
//		}
//		else {
//			ArrayDescriptor descriptor = ArrayDescriptor.createDescriptor(type, connection);
//			arrayDescriptorMap.put(type, descriptor);
//			return descriptor;
//		}		
//	}
}
