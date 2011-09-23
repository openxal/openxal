//
//  FileWatcherController.java
//  xal
//
//  Created by Tom Pelaia on 5/5/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//

package xal.app.launcher;

import xal.tools.bricks.WindowReference;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.*;
import javax.swing.*;


/** manage the File Watcher interface */
public class FileWatcherController {
	/** The main model of this document */
	final private LaunchModel MODEL;
	
	/** file watcher */
	final private FileWatcher FILE_WATCHER;
	
	/** list of watched folders */
	final private JList WATCH_FOLDER_LIST;
	
	/** file chooser for selecting folders to watch */
	final private JFileChooser FOLDER_CHOOSER;
	
	
	/** Constructor */
	public FileWatcherController( final LaunchModel model, final WindowReference windowReference ) {
		MODEL = model;
		FILE_WATCHER = model.getFileWatcher();
		
		FOLDER_CHOOSER = new JFileChooser();
		FOLDER_CHOOSER.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
		FOLDER_CHOOSER.setMultiSelectionEnabled( true );
		
		WATCH_FOLDER_LIST = (JList)windowReference.getView( "WatchPathList" );
		
		final JButton deleteWatchPathButton = (JButton)windowReference.getView( "DeleteWatchPathButton" );
		deleteWatchPathButton.addActionListener( deleteSelectedWatchFoldersAction() );
		
		final JButton addWatchPathButton = (JButton)windowReference.getView( "AddWatchPathButton" );
		addWatchPathButton.addActionListener( addNewWatchFolderAction() );
		
		refreshView();
	}
	
	
	/** refresh the view with the model data */
	private void refreshView() {
		final List<File> folders = FILE_WATCHER.getFolders();
		WATCH_FOLDER_LIST.setListData( new Vector<File>( folders ) );
	}
	
	
	/** action to add a new watch folder */
	private AbstractAction addNewWatchFolderAction() {
		return new AbstractAction() {
			public void actionPerformed( final ActionEvent event ) {
				final int status = FOLDER_CHOOSER.showOpenDialog( WATCH_FOLDER_LIST );
				switch( status ) {
					case JFileChooser.APPROVE_OPTION:
						final File[] folders = FOLDER_CHOOSER.getSelectedFiles();
						FILE_WATCHER.watchFolders( folders );
						break;
					default:
						return;
				}
				refreshView();
			}
		};
	}
	
	
	/** action to delete the selected watch folders */
	private AbstractAction deleteSelectedWatchFoldersAction() {
		return new AbstractAction() {
			public void actionPerformed( final ActionEvent event ) {
				final Object[] selections = WATCH_FOLDER_LIST.getSelectedValues();
				for ( final Object selection : selections ) {
					if ( selection instanceof File ) {
						FILE_WATCHER.ignoreFolder( (File)selection );
					}
				}
				refreshView();
			}
		};
	}	
}
