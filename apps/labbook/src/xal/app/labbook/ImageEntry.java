//
//  ImageEntry.java
//  xal
//
//  Created by Thomas Pelaia on 9/21/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.labbook;

import java.io.*;
import java.sql.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.awt.Image;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
import java.awt.datatransfer.*;
import java.util.List;
import java.util.ArrayList;


/** image entry */
public class ImageEntry {
	/** file for the image */
	final protected File IMAGE_FILE;
	
	/** title */
	protected String _title;
	
	/** comment */
	protected String _comment;
	
	/** raw image icon */
	protected ImageIcon _rawIcon;
	
	
	/** Primary Constructor */
	public ImageEntry( final File imageFile, final String title ) {
		IMAGE_FILE = imageFile;
		setTitle( title );
	}
	
	
	/** Constructor */
	public ImageEntry( final File imageFile ) {
		this( imageFile, imageFile.getName() );
	}
	
	
	/** get a transferable for the specified files */
	static public ImageEntryTransferable getTransferable( final List<File> imageFiles ) {
		final List<ImageEntry> entries = new ArrayList<ImageEntry>( imageFiles.size() );
		for ( final File file : imageFiles ) {
			entries.add( new ImageEntry( file ) );
		}
		
		return new ImageEntryTransferable( entries );
	}
	
	
	/** get a transferable for the specified image entries */
	static public ImageEntryTransferable getTransferableForEntries( final List<ImageEntry> entries ) {
		return new ImageEntryTransferable( entries );
	}
	
	
	/** get the image file */
	public File getImageFile() {
		return IMAGE_FILE;
	}
	
	
	/**
	 * Determine the file's extension.
	 * @return the file's extension
	 */
	protected String getFileExtension() {
		final File file = IMAGE_FILE;
		final String name = file.getName().toLowerCase();
		final int extensionIndex = name.lastIndexOf('.');
		
		return ( extensionIndex < ( name.length() - 2 ) && extensionIndex >= 0 ) ? name.substring( extensionIndex + 1 ) : "";
	}
	
	
	/**
	 * Get the raw image
	 * @return the raw image
	 */
	public Image getImage() {
		return getIcon().getImage();
	}
	
	
	/**
	 * Get the raw icon
	 * @return the raw icon
	 */
	public ImageIcon getIcon() {
		if ( _rawIcon == null ) {
			final Image rawImage = java.awt.Toolkit.getDefaultToolkit().createImage( IMAGE_FILE.getAbsolutePath() );
			_rawIcon = new ImageIcon( rawImage );
		}
		
		return _rawIcon;
	}
	
	
	/** get the title */
	public String getTitle() {
		return _title;
	}
	
	
	/** set a new title */
	public void setTitle( final String title ) {
		_title = title;
	}
	
	
	/** get the comment */
	public String getComment() {
		return _comment;
	}
	
	
	/** set the comment */
	public void setComment( final String comment ) {
		_comment = comment;
	}
	
	
	/** publish this image entry and associate it with the specified logbook entry */
	public void publish( final long logbookEntryID, final int order, final Publisher publisher ) {
		try {
			final String imageFileExtension = getFileExtension();
			final int imageTypeID = publisher.getImageTypeID( imageFileExtension );
			
			if ( imageTypeID < 1 ) {
				throw new RuntimeException( "Image file extension for \"" + _title + "\" is not a supported image type based on file extension: \"" + getFileExtension() + "\"" );
			}
			
			final long imageID = publisher.nextImageID();
			
			final PreparedStatement imageInsertStatement = publisher.getImageInsertStatement();
			imageInsertStatement.setLong( 1, imageID );
			imageInsertStatement.setInt( 2, imageTypeID );
			imageInsertStatement.setString( 3, _title );
			final InputStream imageStream = new FileInputStream( IMAGE_FILE );
			imageInsertStatement.setBinaryStream( 4, imageStream, (int)IMAGE_FILE.length() );
			imageInsertStatement.executeUpdate();
			
			final PreparedStatement entryImageAssociationStatement = publisher.getEntryImageAssociationStatement();
			entryImageAssociationStatement.setLong( 1, logbookEntryID );
			entryImageAssociationStatement.setLong( 2, imageID );
			entryImageAssociationStatement.setInt( 3, order );
			entryImageAssociationStatement.setString( 4, _comment );
			entryImageAssociationStatement.executeUpdate();
		}
		catch( Exception exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Exception publishing the image with title:  " + _title + "\n" + exception.getMessage(), exception );
		}
	}
	
	
	/** equals compares two entries by file equality */
	public boolean equals( final Object entry ) {
		if ( entry instanceof ImageEntry ) {
			return ((ImageEntry)entry).IMAGE_FILE.equals( IMAGE_FILE );
		}
		else {
			return false;
		}
	}


	/** override hashCode() as required to be consistent with equals() */
	public int hashCode() {
		return IMAGE_FILE.hashCode();
	}
}
