/*
 * ElogUtility.java
 *
 * Created on Wed Feb 18 17:07:43 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.extension.logbook;


import xal.tools.database.*;

import java.util.*;
import java.sql.*;


/**
 * ElogUtility provides convenience methods for interacting with the Elog database.
 *
 * @author  tap
 */
public class ElogUtility {
	/** Default maximum allowed size of the main body text */
	static final public int DEFAULT_MAX_BODY_SIZE = 4000;
	
	/** Default maximum allowed size of the title size */
	static final public int DEFAULT_MAX_TITLE_SIZE = 120;
	
	/** Controls logbook name */
	static final public String CONTROLS_LOGBOOK = "Controls";
	
	/** Operations logbook name */
	static final public String OPERATIONS_LOGBOOK = "Operations";
	
	/** connection dictionary */
	final protected ConnectionDictionary _connectionDictionary;
	
	/** database adaptor */
	final protected DatabaseAdaptor _databaseAdaptor;
	
	/** Map of binary types keyed by extension */
	protected Map< String, BinaryType > _binaryTypes;
	
	
	/**
	 * ElogUtility constructor
	 * Uses the default database adaptor.
	 * @param dictionary The connection dictionary.
	 * @throws gov.sns.tools.database.DatabaseException if an exception occurs while connecting to the database
	 */
	public ElogUtility( final ConnectionDictionary dictionary ) throws DatabaseException {
		_connectionDictionary = dictionary;
		
		final DatabaseAdaptor adaptor = dictionary.getDatabaseAdaptor();
		_databaseAdaptor = adaptor != null ? adaptor : DatabaseAdaptor.getInstance();			
	}
	
	
	/**
	 * Get the default elog utility which uses the default connection dictionary and the default database adaptor.
	 * @return The elog utility if the default connection dictionary exists and null if the connection dictionary cannot be found
	 * @throws gov.sns.tools.database.DatabaseException if an exception occurs while connecting to the database
	 */
	static public ElogUtility defaultUtility() throws DatabaseException {
		final ConnectionDictionary dictionary = newConnectionDictionary();
		return ( dictionary != null ) ? new ElogUtility( dictionary ) : null;
	}
	
	
	/** generate a new connection dictionary appropriate for publishing new logbook entries */
	static public ConnectionDictionary newConnectionDictionary() {
		// use the elog account if available, otherwise use the default account
		return ConnectionDictionary.getPreferredInstance( "elog" );
	}
	
	
	/**
	 * Make a new database connection.
	 * @return a new database connection
	 */
	private Connection newConnection() throws DatabaseException {
		try {
			final Connection connection = _databaseAdaptor.getConnection( _connectionDictionary );
			connection.setAutoCommit( false );
			return connection;			
		}
		catch ( SQLException exception ) {
			throw new DatabaseException( "ElogUtility exception at database connection.", _databaseAdaptor, exception );
		}
	}
	
	
	/**
	 * Close the specified database connection.
	 * @param connection the connection to close
	 */
	private void closeConnection( final Connection connection ) throws DatabaseException {
		try {
			if ( connection != null && !connection.isClosed() ) {
				connection.close();
			}
		}
		catch ( SQLException exception ) {
			throw new DatabaseException( "Exception while attempting to close a database exception.", _databaseAdaptor, exception );
		}
	}
	
	
	/** get the maximum allowed size of the main body text */
	public int getMaxBodySize() {
		return DEFAULT_MAX_BODY_SIZE;		// in the future we should get this from the database itself
	}
	
	
	/**
	 * Post an entry to a logbook using the account of the user logged in.
	 * @param logbook The name of the logbook to which we are posting the entry
	 * @param title The title of the entry to post
	 * @param content The content of the entry to post
	 * @throws gov.sns.tools.database.DatabaseException if a database exception occurs while posting an entry
	 */
	public void postEntry( final String logbook, final String title, final String content ) throws DatabaseException {
		final Connection connection = newConnection();
		try {
			postEntry( connection, getUserBadgeNumber( connection ), logbook, title, content );			
		}
		catch ( DatabaseException exception ) {
			throw exception;
		}
		finally {
			closeConnection( connection );
		}
	}
	
	
	/**
	 * Post an entry to a logbook.  This method is private so that a user who is connected can post
	 * an entry only under their own account.
	 * @param badgeNumber The badge number of the user posting the entry
	 * @param logbook The name of the logbook to which we are posting the entry
	 * @param title The title of the entry to post
	 * @param content The content of the entry to post
	 * @throws gov.sns.tools.database.DatabaseException if a database exception occurs while posting an entry
	 */
	private void postEntry( final Connection connection, final String badgeNumber, final String logbook, final String title, final String content ) throws DatabaseException {
		postEntry( connection, badgeNumber, logbook, null, title, content );
	}
	
	
	/**
	 * Post an entry to a logbook.  This method is private so that a user who is connected can post
	 * an entry only under their own account.
	 * @param connection a database connection
	 * @param badgeNumber The badge number of the user posting the entry
	 * @param logbook The name of the logbook to which we are posting the entry
	 * @param categories An array of category IDs to associate with the entry (e.g. "OP1", "OP2", etc.).
	 * @param title The title of the entry to post
	 * @param content The content of the entry to post
	 * @throws gov.sns.tools.database.DatabaseException if a database exception occurs while posting an entry
	 */
	private void postEntry( final Connection connection, final String badgeNumber, final String logbook, final String[] categories, final String title, final String content ) throws DatabaseException {
		try {
			final CallableStatement postEntry = connection.prepareCall( "call logbook.logbook_pkg.insert_logbook_entry (?, ?, ?, ?, ?)" );
			final Array categoryArray = _databaseAdaptor.getArray( "LOGBOOK.LOGBOOK_CAT_TAB_TYP", connection, categories );
			
			postEntry.setString( 1, badgeNumber );
			postEntry.setString( 2, logbook );
			postEntry.setString( 3, title );
			postEntry.setArray( 4, categoryArray );			// array of categories
			postEntry.setString( 5, content);
			postEntry.execute();
			
			connection.commit();
		}
		catch ( SQLException exception ) {
			throw new DatabaseException( "Exception while posting entry to Elog.", _databaseAdaptor, exception );				
		}
	}
	
	
	/**
	 * Post an entry to a logbook using the account of the user logged in.
	 * @param logbook The name of the logbook to which we are posting the entry
	 * @param title The title of the entry to post
	 * @param content The content of the entry to post
	 * @param dataName The name of the associatiated image or attachment data.
	 * @param formatName The format of the image or attachment data to associate with this entry (e.g. "jpg", "pdf", "txt").
	 * @param data The image or attachment data to associate with the entry.
	 * @throws gov.sns.tools.database.DatabaseException if a database exception occurs while posting an entry
	 */
	public void postEntry( final String logbook, final String title, final String content, final String dataName, final String formatName, final byte[] data ) throws DatabaseException {
		final Connection connection = newConnection();
		postEntry( connection, getUserBadgeNumber( connection ), logbook, title, content, dataName, formatName, data );
	}
		
	
	/**
	 * Post an entry to a logbook.  This method is private so that a user who is connected can post
	 * an entry only under their own account.
	 * @param connection The database connection
	 * @param badgeNumber The badge number of the user posting the entry
	 * @param logbook The name of the logbook to which we are posting the entry
	 * @param title The title of the entry to post
	 * @param content The content of the entry to post
	 * @param dataName The name of the associatiated image or attachment data.
	 * @param formatName The format of the image or attachment data to associate with this entry (e.g. "jpg", "pdf", "txt").
	 * @param data The image or attachment data to associate with the entry.
	 * @throws gov.sns.tools.database.DatabaseException if a database exception occurs while posting an entry
	 */
	private void postEntry( final Connection connection, final String badgeNumber, final String logbook, final String title, final String content, final String dataName, final String formatName, final byte[] data ) throws DatabaseException {
		
		fetchBinaryTypesIfNeeded();
		if ( !_binaryTypes.containsKey( formatName ) ) {
			throw new IllegalArgumentException( "\"" + formatName + "\" is not a valid binary data type" );
		}
		
		final BinaryType binaryType = _binaryTypes.get( formatName );
		final Blob blob = _databaseAdaptor.newBlob( connection );
		
		try {
			int count = blob.setBytes( 1L, data );
			
			final CallableStatement postDataEntry = connection.prepareCall( "call logbook.logbook_pkg.insert_logbook_entry (?, ?, ?, ?, ?, ?, ?, ?)" );
			
			postDataEntry.setString( 1, badgeNumber );
			postDataEntry.setString( 2, logbook );
			postDataEntry.setString( 3, title );
			postDataEntry.setString( 4, content );
			postDataEntry.setString( 5, binaryType.getTypeCode() );
			postDataEntry.setString( 6, dataName );
			postDataEntry.setLong( 7, binaryType.getID() );
			postDataEntry.setBlob( 8, blob );
			postDataEntry.execute();
			
			connection.commit();
		}
		catch( SQLException exception ) {
			throw new DatabaseException( "Exception while posting entry to Elog.", _databaseAdaptor, exception );
		}
	}
	
	
	/**
	 * Get the badge number of the user who is logged in.
	 * @return the badge number of the user who is logged in.
	 */
	private String getUserBadgeNumber( final Connection connection ) {
		return getBadgeNumber( connection );
	}
	
	
	/**
	 * Get the badge number for the user in the connection dictionary
	 * @param connection the database connection
	 * @return the badge number for the specified user or null if none was found
	 * @throws gov.sns.tools.database.DatabaseException if a database exception occurs while fetching the badge number
	 */
	private String getBadgeNumber( final Connection connection ) {
		try {
			final String userID = _connectionDictionary.getUser().toUpperCase();
			final PreparedStatement statement = connection.prepareStatement( "select bn from OPER.EMPLOYEE_V where user_id = ?" );
			statement.setString( 1, userID );
			statement.execute();
			connection.commit();
			final ResultSet result = statement.getResultSet();
			return ( result.next() ) ? result.getString( "bn" ) : null;
		}
		catch( SQLException exception ) {
			throw new DatabaseException( "Exception while fetching the badge number.", _databaseAdaptor, exception );
		}
	}
	
	
	/**
	 * Get the binary types.
	 */
	public Map< String, BinaryType > getBinaryTypes() {
		fetchBinaryTypesIfNeeded();
		return _binaryTypes;
	}
	
	
	/** Fetch the image and attachment binary types if needed. */
	private void fetchBinaryTypesIfNeeded() throws DatabaseException {
		if ( _binaryTypes == null ) {
			_binaryTypes = new HashMap< String, BinaryType >();
			final Connection connection = newConnection();
			try {
				fetchImageTypes( connection );
				fetchAttachmentTypes( connection );
			}
			finally {
				closeConnection( connection );
			}
		}
	}
	
	
	/** Fetch the available image types. */
	private void fetchImageTypes( final Connection connection ) throws DatabaseException {
		try {
			final Statement statement = connection.createStatement();
			final ResultSet result = statement.executeQuery( "select * from LOGBOOK.IMAGE_TYPE" );
			
			connection.commit();
			
			while ( result.next() ) {
				long ID = result.getLong( "image_type_id" );
				String extension = result.getString( "file_extension" );
				_binaryTypes.put( extension, new BinaryType( ID, extension ) );
			}
		}
		catch( SQLException exception ) {
			throw new DatabaseException( "Exception while fetching the image types.", _databaseAdaptor, exception );
		}
	}
	
	
	/** Fetch the available attachment types. */
	private void fetchAttachmentTypes( final Connection connection ) throws DatabaseException {
		try {
			final Statement statement = connection.createStatement();
			final ResultSet result = statement.executeQuery( "select * from LOGBOOK.ATTACHMENT_TYPE" );
			
			connection.commit();
			
			while ( result.next() ) {
				final long ID = result.getLong( "attachment_type_id" );
				final String extension = result.getString( "file_extension" );
				_binaryTypes.put( extension, new BinaryType( ID, extension ) );
			}
		}
		catch( SQLException exception ) {
			throw new DatabaseException( "Exception while fetching the attachment types.", _databaseAdaptor, exception );
		}
	}
}

