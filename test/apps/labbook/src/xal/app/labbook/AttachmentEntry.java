//
//  AttachmentEntry.java
//  xal
//
//  Created by Thomas Pelaia on 9/25/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.labbook;

import java.io.*;
import java.sql.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.awt.datatransfer.*;
import java.util.List;
import java.util.ArrayList;


/** Attachment Entry */
public class AttachmentEntry {
	/** file for the media */
	final protected File MEDIA_FILE;
	
	/** title */
	protected String _title;
	
	
	/** Primary Constructor */
	public AttachmentEntry( final File mediaFile, final String title ) {
		MEDIA_FILE = mediaFile;
		setTitle( title );
	}
	
	
	/** Constructor */
	public AttachmentEntry( final File mediaFile ) {
		this( mediaFile, mediaFile.getName() );
	}
	
	
	/** get a transferable for the specified files */
	static public Transferable getTransferable( final List<File> mediaFiles ) {
		final List<AttachmentEntry> entries = new ArrayList<AttachmentEntry>( mediaFiles.size() );
		for ( final File file : mediaFiles ) {
			entries.add( new AttachmentEntry( file ) );
		}
		
		return new AttachmentEntryTransferable( entries );
	}
	
	
	/** get a transferable for the specified media entries */
	static public Transferable getTransferableForEntries( final List<AttachmentEntry> entries ) {
		return new AttachmentEntryTransferable( entries );
	}
	
	
	/** get the media file */
	public File getMediaFile() {
		return MEDIA_FILE;
	}
	
	
	/**
	 * Determine the file's extension.
	 * @return the file's extension
	 */
	protected String getFileExtension() {
		final File file = MEDIA_FILE;
		final String name = file.getName().toLowerCase();
		final int extensionIndex = name.lastIndexOf('.');
		
		return ( extensionIndex < ( name.length() - 2 ) && extensionIndex >= 0 ) ? name.substring( extensionIndex + 1 ) : "";
	}
	
	
	/** get the title */
	public String getTitle() {
		return _title;
	}
	
	
	/** set a new title */
	public void setTitle( final String title ) {
		_title = title;
	}
	
	
	/** publish this image entry and associate it with the specified logbook entry */
	public void publish( final long logbookEntryID, final int order, final Publisher publisher ) {
		try {
			final int mediaTypeID = publisher.getAttachmentTypeID( getFileExtension() );
			publish( logbookEntryID, order, _title, mediaTypeID, new FileInputStream( MEDIA_FILE ), (int)MEDIA_FILE.length(), publisher );
		}
		catch ( Exception exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Exception publishing the attachment with title:  " + _title, exception );
		}
	}
	
	
	/** publish this image entry and associate it with the specified logbook entry */
	static public void publish( final long logbookEntryID, final int order, final String title, final int mediaTypeID, final InputStream mediaStream, final int mediaLength, final Publisher publisher ) {
		try {
			final long mediaID = publisher.nextAttachmentID();
			
			final PreparedStatement mediaInsertStatement = publisher.getAttachmentInsertStatement();
			mediaInsertStatement.setLong( 1, mediaID );
			mediaInsertStatement.setInt( 2, mediaTypeID );
			mediaInsertStatement.setString( 3, title );
			mediaInsertStatement.setBinaryStream( 4, mediaStream, mediaLength );
			mediaInsertStatement.executeUpdate();
			
			final PreparedStatement entryMediaAssociationStatement = publisher.getEntryAttachmentAssociationStatement();
			entryMediaAssociationStatement.setLong( 1, logbookEntryID );
			entryMediaAssociationStatement.setLong( 2, mediaID );
			entryMediaAssociationStatement.setInt( 3, order );
			entryMediaAssociationStatement.executeUpdate();
		}
		catch( Exception exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Exception publishing the attachment with title:  " + title, exception );
		}
	}
}
