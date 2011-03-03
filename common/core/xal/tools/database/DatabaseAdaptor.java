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
	abstract public Array getArray(String type, Connection connection, Object array) throws DatabaseException;
	
	
	/**
	 * Get a new database connection
	 * @param urlSpec The URL to which to connect
	 * @param user The user loggin into the database
	 * @param password the user's password
	 * @throws xal.tools.database.DatabaseException if a database exception is thrown
	 */
	abstract public Connection getConnection(String urlSpec, String user, String password) throws DatabaseException ;
	
	
	/**
	 * Get a new database connection
	 * @param dictionary A connection dictionary
	 * @throws xal.tools.database.DatabaseException if a database exception is thrown
	 */
	public Connection getConnection(final ConnectionDictionary dictionary) throws DatabaseException {
		return getConnection(dictionary.getURLSpec(), dictionary.getUser(), dictionary.getPassword());
	}
	
	
	/**
	 * Get a new database adaptor using the default database adaptor
	 * @return A new instance of the default database adaptor
	 */
	static public DatabaseAdaptor getInstance() {
		return new OracleDatabaseAdaptor();
	}
	
	
	/**
	 * Fetch the list of nontrivial schemas.
	 * @param connection database connection
	 * @return list of nontrivial schema names
	 */
	abstract public List<String> fetchNontrivialSchemas( final Connection connection );
}

