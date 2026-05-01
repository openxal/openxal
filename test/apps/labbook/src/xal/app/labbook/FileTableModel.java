//
//  FileTableModel.java
//  xal
//
//  Created by Thomas Pelaia on 9/21/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.labbook;

import javax.swing.*;
import javax.swing.table.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;


/** table model for listing files */
public class FileTableModel extends AbstractTableModel {
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
	/** default date format */
	final static SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat( "MMM dd, yyyy HH:mm:ss" );
	
	/** file name column */
	final static public int FILE_NAME_COLUMN = 0;
	
	/** modification date column */
	final static public int MODIFICATION_DATE_COLUMN = FILE_NAME_COLUMN + 1;
	
	/** list of files to display in the table */
	protected List<File> _files;
	
	
	/** Primary Constructor */
	public FileTableModel( final List<File> files ) {
		setFiles( files );
	}
	
	
	/** Constructor */
	public FileTableModel() {
		this( new ArrayList<File>() );
	}
	
	
	/** get the file at the specified row */
	public File getFile( final int row ) {
		final List<File> files = _files;
		return files.size() > row ? files.get( row ) : null;
	}
	
	
	/** set the list of files to display */
	public void setFiles( final List<File> files ) {
		_files = files;
		fireTableDataChanged();
	}
	
	
	/** get the column title */
	public String getColumnName( final int column ) {
		switch( column ) {
			case FILE_NAME_COLUMN:
				return "File Name";
			case MODIFICATION_DATE_COLUMN:
				return "Modified";
			default:
				return null;
		}		
	}
	
	
	/** get the row count */
	public int getRowCount() {
		final List<File> files = _files;
		return files.size();
	}
	
	
	/** get the column count */
	public int getColumnCount() {
		return 2;
	}
	
	
	/** get the value for the specified cell */
	public Object getValueAt( final int row, final int column ) {
		final List<File> files = _files;
		
		if ( row >= files.size() )  return null;
		
		final File file = files.get( row );
		
		switch( column ) {
			case FILE_NAME_COLUMN:
				return file.getName();
			case MODIFICATION_DATE_COLUMN:
				final Date modificationDate = new Date( file.lastModified() );
				return DEFAULT_DATE_FORMAT.format( modificationDate );
			default:
				return null;
		}
	}
}
