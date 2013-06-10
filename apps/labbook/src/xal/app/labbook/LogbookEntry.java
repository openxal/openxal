//
//  LogbookEntry.java
//  xal
//
//  Created by Thomas Pelaia on 9/19/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.labbook;

import java.io.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;


/** description of a logbook entry to be published */
public class LogbookEntry {
	static public final int ENTRY_ID_COLUMN = 1;
	static public final int BADGE_NUMBER_COLUMN = ENTRY_ID_COLUMN + 1;
	static public final int TITLE_COLUMN = BADGE_NUMBER_COLUMN + 1;
	static public final int SUMMARY_COLUMN = TITLE_COLUMN + 1;
	static public final int HTML_INDICATOR_COLUMN = SUMMARY_COLUMN + 1;
	static public final int POST_TIME_COLUMN = HTML_INDICATOR_COLUMN + 1;
	static public final int EVENT_TIME_COLUMN = POST_TIME_COLUMN + 1;
	static public final int EMAIL_NOTIFICATION_COLUMN = EVENT_TIME_COLUMN + 1;
	static public final int ENTRY_TYPE_ID_COLUMN = EMAIL_NOTIFICATION_COLUMN + 1;
	static public final int PUBLISH_STATUS_ID_COLUMN = ENTRY_TYPE_ID_COLUMN + 1;
	
	static public final int LOGBOOK_ID_COLUMN = ENTRY_ID_COLUMN + 1;
	static public final int CATEGORY_ID_COLUMN = ENTRY_ID_COLUMN + 1;
	
	/** entry title */
	protected String _title;
	
	/** entry summary */
	protected String _summary;
	
	/** indicates whether this entry is a shift summary */
	protected boolean _isShiftSummary;
	
	/** logbook name */
	protected List<String> _logbookIDs;
	
	/** category name */
	protected List<String> _categoryIDs;
	
	/** image entries */
	protected List<ImageEntry> _imageEntries;
	
	/** attachment entries */
	protected List<AttachmentEntry> _attachmentEntries;
	
	/** details attachment content */
	protected String _detailsContent;	
	
	
	/** Constructor */
	public LogbookEntry( final List<String> logbookIDs, final List<String> categoryIDs ) {
		_isShiftSummary = true;
		_logbookIDs = logbookIDs;
		_categoryIDs = categoryIDs;
		_imageEntries = new ArrayList<ImageEntry>();
		_attachmentEntries = new ArrayList<AttachmentEntry>();
	}
	
	
	/** get the title */
	public String getTitle() {
		return _title;
	}
	
	
	/** set the title */
	public void setTitle( final String title ) {
		_title = title;
	}
	
	
	/** get the summary */
	public String getSummary() {
		return _summary;
	}
	
	
	/** set the summary */
	public void setSummary( final String summary ) {
		_summary = summary;
	}
	
	
	/** determine if this entry is a shift summary */
	public boolean isShiftSummary() {
		return _isShiftSummary;
	}
	
	
	/** sets whether this entry is a shift summary */
	public void setShiftSummary( final boolean shiftSummary ) {
		_isShiftSummary = shiftSummary;
	}
	
	
	/** set the image entries */
	public void setImageEntries( final List<ImageEntry> entries ) {
		_imageEntries = entries;
	}
	
	
	/** set the attachment entries */
	public void setAttachmentEntries( final List<AttachmentEntry> entries ) {
		_attachmentEntries = entries;
	}
	
	
	/** set details content */
	public void setDetailsContent( final String content ) {
		_detailsContent = content;
	}
	
	
	/** write the entry to the specified statement */
	public void publish( final String userID, final Publisher publisher ) {
		try {
			final long entryID = publisher.nextLogbookEntryID();
			final String badgeNumber = publisher.getBadgeNumber( userID );
			
			final boolean isShiftSummary = _isShiftSummary;
			final int typeID = isShiftSummary ? publisher.getShiftSummaryEntryTypeID() : 0;
			
			final Time now = new Time( new java.util.Date().getTime() );
			
			System.out.println( "posting at:  " + now );
			System.out.println( "Making entry with ID:  " + entryID + " for user with badge number:  " + badgeNumber );
			
			final PreparedStatement statement = publisher.getEntryInsertStatement();
			statement.setLong( ENTRY_ID_COLUMN, entryID );
			statement.setString( BADGE_NUMBER_COLUMN, badgeNumber );
			statement.setString( TITLE_COLUMN, _title );
			statement.setString( SUMMARY_COLUMN, _summary );
			statement.setString( HTML_INDICATOR_COLUMN, "N" );
			statement.setTime( POST_TIME_COLUMN, now );
			statement.setTime( EVENT_TIME_COLUMN, now );
			statement.setString( EMAIL_NOTIFICATION_COLUMN, "N" );
			statement.setString( PUBLISH_STATUS_ID_COLUMN, "P" );		// mark the entry as published
			
			if ( isShiftSummary ) {
				statement.setInt( ENTRY_TYPE_ID_COLUMN, typeID );
			}
			else {
				statement.setNull( ENTRY_TYPE_ID_COLUMN, Types.INTEGER );
			}
			
			statement.executeUpdate();
			
			final PreparedStatement logbookAssociationStatement = publisher.getLogbookEntryAssociationStatement();
			for ( final String logbookID : _logbookIDs ) {
				logbookAssociationStatement.setLong( ENTRY_ID_COLUMN, entryID );
				logbookAssociationStatement.setString( LOGBOOK_ID_COLUMN, logbookID );
				logbookAssociationStatement.executeUpdate();
			}
			
			final PreparedStatement categoryAssociationStatement = publisher.getCategoryEntryAssociationStatement();
			for ( final String categoryID : _categoryIDs ) {
				categoryAssociationStatement.setLong( ENTRY_ID_COLUMN, entryID );
				categoryAssociationStatement.setString( CATEGORY_ID_COLUMN, categoryID );
				categoryAssociationStatement.executeUpdate();
			}
			
			final List<ImageEntry> imageEntries = _imageEntries;
			int imageOrder = 0;
			for ( final ImageEntry imageEntry : imageEntries ) {
				imageEntry.publish( entryID, imageOrder++, publisher );
			}
			
			int attachmentOrder = 0;
			
			if ( _detailsContent != null && _detailsContent != "" ) {
				final int mediaTypeID = publisher.getAttachmentTypeID( "html" );
				final byte[] mediaData = _detailsContent.getBytes( "UTF-8" );
				final InputStream mediaStream = new ByteArrayInputStream( mediaData );
				AttachmentEntry.publish( entryID, attachmentOrder++, "details", mediaTypeID, mediaStream, mediaData.length, publisher );
			}
			
			final List<AttachmentEntry> attachmentEntries = _attachmentEntries;
			for ( final AttachmentEntry mediaEntry : attachmentEntries ) {
				mediaEntry.publish( entryID, attachmentOrder++, publisher );
			}
		}
		catch( Exception exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Exception writing the logbook entry.\n" + exception.getMessage(), exception );
		}
	}
}
