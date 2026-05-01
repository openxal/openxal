//
//  Publisher.java
//  xal
//
//  Created by Thomas Pelaia on 9/19/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.labbook;

import xal.tools.database.*;

import java.sql.*;
import java.util.Map;
import java.util.Hashtable;


/** publish entries to the logbook */
public class Publisher {
	/** get the entry type ID for shift summaries */
	static protected int _shiftSummaryEntryTypeID;
	
	/** table of image type IDs keyed by extension */
	static protected Map<String,Number> _imageTypes;
	
	/** table of attachment type IDs keyed by extension */
	static protected Map<String,Number> _attachmentTypes;
		
	/** database connection */
	protected Connection _connection;
	
	/** connection dictionary */
	protected ConnectionDictionary _connectionDictionary;
	
	
	// static initializer 
	static {
		_shiftSummaryEntryTypeID = -1;
	}
	
	
	/** Constructor */
	public Publisher( final ConnectionDictionary dictionary, final Connection connection ) {
		setConnectionDictionary( dictionary );
		setConnection( connection );
	}
	
	
	/** get the connection dictionary */
	public ConnectionDictionary getConnectionDictionary() {
		return _connectionDictionary;
	}
	
	
	/** set the connection dictionary */
	public void setConnectionDictionary( final ConnectionDictionary dictionary ) {
		_connectionDictionary = dictionary;
	}
	
	
	/** get the connection */
	public Connection getConnection() {
		return _connection;
	}
	
	
	/** set a new connection */
	public void setConnection( final Connection connection ) {
		_connection = connection;
		
		if ( connection != null ) {
			try {
				connection.setAutoCommit( false );
			}
			catch( SQLException exception ) {
				throw new RuntimeException( "connection auto commit exception...", exception );
			}			
		}
	}
	
	
	/** commit the transaction */
	public void commit() {
		try {
			_connection.commit();
		}
		catch ( Exception exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Exception attempting to commit the transaction...", exception );
		}
	}
	
	
	/** rollback the transaction */
	public void rollback() {
		try {
			_connection.rollback();
		}
		catch ( Exception exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Exception attempting to rollback...", exception );
		}
	}
	
	
	/** close the connection */
	public void closeConnection() {
		try {
			_connection.close();
		}
		catch ( Exception exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Exception attempting to close the database connection...", exception );
		}
	}
	
	
	/** get the shift summary entry type ID */
	public int getShiftSummaryEntryTypeID() {
		if ( _shiftSummaryEntryTypeID < 0 ) {
			try {
				final PreparedStatement statement = _connection.prepareStatement( "select type_id from logbook.LOG_ENTRY_TYPE where type_nm = \'Shift Summary\'" );
				final ResultSet resultSet = statement.executeQuery();
				_shiftSummaryEntryTypeID = ( resultSet.next() ) ? resultSet.getInt( 1 ) : 0;
			}
			catch( SQLException exception ) {
				exception.printStackTrace();
				throw new RuntimeException( "Exception fetching the shift summary entry type ID.", exception );
			}		
		}
		
		return _shiftSummaryEntryTypeID;
	}
	
	
	/** get the next logbook entry ID */
	public long nextLogbookEntryID() {
		try {
			final PreparedStatement statement = _connection.prepareStatement( "select logbook.log_entry_seq.nextval from dual" );
			final ResultSet resultSet = statement.executeQuery();
			return ( resultSet.next() ) ? resultSet.getLong( 1 ) : 0;
		}
		catch( SQLException exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Exception requesting the next entry ID.", exception );
		}
	}
	
	
	/** get the next image entry ID */
	public long nextImageID() {
		try {
			final PreparedStatement statement = _connection.prepareStatement( "select logbook.image_seq.nextval from dual" );
			final ResultSet resultSet = statement.executeQuery();
			return ( resultSet.next() ) ? resultSet.getLong( 1 ) : 0;
		}
		catch( SQLException exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Exception requesting the next image ID.", exception );
		}
	}
	
	
	/** get the next attachment entry ID */
	public long nextAttachmentID() {
		try {
			final PreparedStatement statement = _connection.prepareStatement( "select logbook.attachment_seq.nextval from dual" );
			final ResultSet resultSet = statement.executeQuery();
			return ( resultSet.next() ) ? resultSet.getLong( 1 ) : 0;
		}
		catch( SQLException exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Exception requesting the next attachment ID.", exception );
		}
	}
	
	
	/** get the user's badge number */
	public String getBadgeNumber( final String userID ) {
		try {
			System.out.println( "Get badge number for user ID:  " + userID );
			final PreparedStatement statement = _connection.prepareStatement( "select bn from OPER.EMPLOYEE_V where user_id = ?" );
			statement.setString( 1, userID.toUpperCase() );
			statement.execute();
			final ResultSet result = statement.getResultSet();
			return ( result.next() ) ? result.getString( "bn" ) : null;
		}
		catch( SQLException exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Exception fetching the user's badge number.", exception );
		}
	}
	
	
	/** Get the prepared statement for inserting a new entry */
	public PreparedStatement getEntryInsertStatement() {
		try {
			final String sql =  "INSERT INTO logbook.log_entry ( log_entry_id, bn, title, content, html_ind, orig_post, occur_dte, email_notif_ind, type_id, pub_stat_id ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
			return _connection.prepareStatement( sql );
		}
		catch( SQLException exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Exception getting an insert statement for inserting an entry record.", exception );
		}		
	}
	
	
	/** Get the prepared statement for associating a new entry with a logbook */
	public PreparedStatement getLogbookEntryAssociationStatement() {
		try {
			final String sql =  "INSERT INTO logbook.entry_logbook ( log_entry_id, logbook_id ) VALUES (?, ?)";
			return _connection.prepareStatement( sql );
		}
		catch( SQLException exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Exception getting an insert statement for inserting an entry-logbook record.", exception );
		}		
	}
	
	
	/** Get the prepared statement for associating a new entry with a category */
	public PreparedStatement getCategoryEntryAssociationStatement() {
		try {
			final String sql =  "INSERT INTO logbook.log_entry_categories ( log_entry_id, cat_id ) VALUES (?, ?)";
			return _connection.prepareStatement( sql );
		}
		catch( SQLException exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Exception getting an insert statement for inserting an entry-category record.", exception );
		}		
	}
	
	
	/** Get the image insert statement */
	public PreparedStatement getImageInsertStatement() {
		try {
			final String sql =  "INSERT INTO logbook.image ( image_id, image_type_id, image_nm, image_data ) VALUES (?, ?, ?, ?)";
			return _connection.prepareStatement( sql );
		}
		catch( SQLException exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Exception getting an insert statement for inserting an image record.", exception );
		}				
	}
	
	
	/** Get the entry image insert statement */
	public PreparedStatement getEntryImageAssociationStatement() {
		try {
			final String sql =  "INSERT INTO logbook.log_entry_image ( LOG_ENTRY_ID, IMAGE_ID, log_entry_image_ord, log_entry_image_cmnt ) VALUES (?, ?, ?, ?)";
			return _connection.prepareStatement( sql );
		}
		catch( SQLException exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Exception getting an insert statement for inserting an entry-image record.", exception );
		}
	}
	
	
	/** Get the image type ID corresponding to the specified extension */
	public int getImageTypeID( final String extension ) {
		final Number typeID = getImageTypeTable( _connection ).get( extension );
		return typeID != null ? typeID.intValue() : -1;
	}
	
	
	/** Get the image type table */
	static protected Map<String,Number> getImageTypeTable( final Connection connection ) {
		if ( _imageTypes == null ) {
			try {
				final Map<String,Number> imageTypes = new Hashtable<String,Number>();
				final PreparedStatement statement = connection.prepareStatement( "select * from logbook.image_type" );
				statement.execute();
				final ResultSet resultSet = statement.getResultSet();
				while( resultSet.next() ) {
					final String extension = resultSet.getString( "file_extension" );
					final int typeID = resultSet.getInt( "image_type_id" );
					imageTypes.put( extension, typeID );
				}
				_imageTypes = imageTypes;
			}
			catch( Exception exception ) {
				exception.printStackTrace();
				throw new RuntimeException( "Exception getting the image type table.", exception );
			}
		}
		
		return _imageTypes;
	}
	
	
	/** Get the image attachment statement */
	public PreparedStatement getAttachmentInsertStatement() {
		try {
			final String sql =  "INSERT INTO logbook.attachment ( attachment_id, attachment_type_id, attachment_nm, attachment_data ) VALUES (?, ?, ?, ?)";
			return _connection.prepareStatement( sql );
		}
		catch( SQLException exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Exception getting an insert statement for inserting an attachment record.", exception );
		}				
	}
	
	
	/** Get the entry attachment insert statement */
	public PreparedStatement getEntryAttachmentAssociationStatement() {
		try {
			final String sql =  "INSERT INTO logbook.LOG_ENTRY_ATTACHMENT ( LOG_ENTRY_ID, attachment_id, log_entry_attachment_ord ) VALUES (?, ?, ?)";
			return _connection.prepareStatement( sql );
		}
		catch( SQLException exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Exception getting an insert statement for inserting an entry-attachment record.", exception );
		}				
	}
	
	
	/** Get the attachment type ID corresponding to the specified extension */
	public int getAttachmentTypeID( final String extension ) {
		final Number typeID = getAttachmentTypeTable( _connection ).get( extension );
		return typeID != null ? typeID.intValue() : -1;
	}
	
	
	/** Get the attachment type table */
	static protected Map<String,Number> getAttachmentTypeTable( final Connection connection ) {
		if ( _attachmentTypes == null ) {
			try {
				final Map<String,Number> attachmentTypes = new Hashtable<String,Number>();
				final PreparedStatement statement = connection.prepareStatement( "select * from logbook.attachment_type" );
				statement.execute();
				final ResultSet resultSet = statement.getResultSet();
				while( resultSet.next() ) {
					final String extension = resultSet.getString( "file_extension" );
					final int typeID = resultSet.getInt( "attachment_type_id" );
					attachmentTypes.put( extension, typeID );
				}
				_attachmentTypes = attachmentTypes;
			}
			catch( Exception exception ) {
				exception.printStackTrace();
				throw new RuntimeException( "Exception getting the attachment type table.", exception );
			}
		}
		
		return _attachmentTypes;
	}
}




