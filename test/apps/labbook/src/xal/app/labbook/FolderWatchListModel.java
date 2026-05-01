//
//  FolderWatchListModel.java
//  xal
//
//  Created by Thomas Pelaia on 9/21/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.labbook;

import javax.swing.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;


/** list model for a folder watch list */
public class FolderWatchListModel extends AbstractListModel<String> {
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
	/** folder watch list */
	protected List<File> _folders;
	
	
	/** Primary Constructor */
	public FolderWatchListModel( final List<File> folders ) {
		setFolders( folders );
	}
	
	
	/** Constructor */
	public FolderWatchListModel() {
		this( new ArrayList<File>() );
	}
	
	
	/** set the folders to the new list */
	public void setFolders( final List<File> folders ) {
		_folders = folders;
		fireContentsChanged( this, 0, folders.size() - 1 );
	}
	
	
	/** get the count of elements */
	public int getSize() {
		final List<File> folders = _folders;
		return folders.size();
	}
	
	
	/** get the element at the specified index */
	public String getElementAt( final int index ) {
		final List<File> folders = _folders;
		return folders.size() > index ? folders.get( index ).getAbsolutePath() : null;
	}
}
