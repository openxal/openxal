/*
 * OracleDatabaseAdaptor.java
 *
 * Created on Wed Feb 18 14:02:59 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.tools.database;

import java.util.*;
import java.util.logging.*;
import java.sql.*;

import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;
import oracle.sql.BLOB;


/**
 * OracleDatabaseAdaptor is a concrete subclass of DatabaseAdaptor for implementing methods specifically for the Oracle database.
 *
 * @author  tap
 */
public class OracleDatabaseAdaptor extends DatabaseAdaptor {
	final private Map<String,ArrayDescriptor> ARRAY_DESCRIPTOR_TABLE;
	
	
	/**
	 * Public Constructor
	 */
	public OracleDatabaseAdaptor() {
		ARRAY_DESCRIPTOR_TABLE = new HashMap<String,ArrayDescriptor>();
	}
	
	
	/**
	 * Get a new database connection
	 * @param urlSpec The URL to which to connect
	 * @param user The user loggin into the database
	 * @param password the user's password
	 * @throws xal.tools.database.DatabaseException if a database exception is thrown
	 */
	public Connection getConnection(String urlSpec, String user, String password) throws DatabaseException {
		try {
			return DriverManager.getConnection(urlSpec, user, password);
		}
		catch(SQLException exception) {
			Logger.getLogger("global").log( Level.SEVERE, "Error connecting to the database at URL: \"" + urlSpec + "\" as user: " + user , exception );
			throw new DatabaseException("Exception connecting to the database.", this, exception);
		}
	}
	
	
	/**
	 * Fetch the list of nontrivial schemas.
	 * @param connection database connection
	 * @return list of nontrivial schema names
	 */
	public List<String> fetchNontrivialSchemas( final Connection connection ) {
		try {
			final PreparedStatement statement = connection.prepareStatement( "select owner from all_tables group by owner order by owner" );
			final ResultSet resultSet = statement.executeQuery();
			final List<String> schemas = new ArrayList<String>();
			while( resultSet.next() ) {
				final String schema = resultSet.getString( 1 );
				schemas.add( schema );
			}
			return schemas;
		}
		catch( SQLException exception ) {
			throw new DatabaseException( "Exception Fetching nontrivial schemas.", this, exception );
		}		
	}
	
	
	
	/**
	 * Instantiate an empty Blob.
	 * @param connection the database connection
	 * @return a new instance of a Blob appropriate for this adaptor.
	 */
	public Blob newBlob( final Connection connection ) {
		try {
			return BLOB.createTemporary( connection, true, BLOB.DURATION_SESSION );			
		}
		catch( SQLException exception ) {
			throw new DatabaseException( "Exception instantiating a Blob.", this, exception );
		}
	}
	
	
	/**
	 * Get an SQL Array given an SQL array type, connection and a primitive array
	 * @param type An SQL array type identifying the type of array
	 * @param connection An SQL connection
	 * @param array The primitive Java array
	 * @return the SQL array which wraps the primitive array
	 * @throws xal.tools.database.DatabaseException if a database exception is thrown
	 */
	public Array getArray( final String type, final Connection connection, final Object array ) throws DatabaseException {
		try {
			final ArrayDescriptor descriptor = getArrayDescriptor( type, connection );
			return new ARRAY( descriptor, connection, array );
		}
		catch(SQLException exception) {
			Logger.getLogger("global").log( Level.SEVERE, "Error instantiating an SQL array of type: " + type, exception );
			throw new DatabaseException("Exception generating an SQL array.", this, exception);
		}
	}
	
	
	/**
	 * Get the array descriptor for the specified array type
	 * @param type An SQL array type
	 * @param connection A database connection
	 * @return the array descriptor for the array type
	 * @throws java.sql.SQLException if a database exception is thrown
	 */
	private ArrayDescriptor getArrayDescriptor( final String type, final Connection connection ) throws SQLException {
		if ( ARRAY_DESCRIPTOR_TABLE.containsKey(type) ) {
			return ARRAY_DESCRIPTOR_TABLE.get( type );
		}
		else {
			final ArrayDescriptor descriptor = ArrayDescriptor.createDescriptor( type, connection );
			ARRAY_DESCRIPTOR_TABLE.put( type, descriptor );
			return descriptor;
		}		
	}
}

