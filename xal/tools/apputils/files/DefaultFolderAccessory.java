//
//  DefaultFolderAccessory.java
//  xal
//
//  Created by Thomas Pelaia on 1/24/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.apputils.files;

import java.io.*;
import java.beans.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.prefs.Preferences;
import java.net.*;

/** Manage the default folder for an application's documents */
public class DefaultFolderAccessory implements PropertyChangeListener {
	/** default preference ID */
	final static private String DEFAULT_ID = "DEFAULT_FOLDER";
	
	/** file tracker for the default folder */
	protected RecentFileTracker _folderTracker;
	
	/** optional subfolder name */
	protected String _subfolderName;
	
	/** the active file chooser */
	protected JFileChooser _activeFileChooser;
	
	
	/** Primary Constructor */
	public DefaultFolderAccessory( final Preferences prefs, final String preferenceID, final String subfolderName ) {
		_subfolderName = subfolderName;
		final String prefID = ( preferenceID != null ) ? preferenceID : DEFAULT_ID;
		_folderTracker = new RecentFileTracker( 1, prefs, prefID );
	}
	
	
	/** Constructor */
	public DefaultFolderAccessory( final Preferences prefs, final String preferenceID ) {				
		this( prefs, preferenceID, null );		
	}
	
	
	/** Constructor */
	public DefaultFolderAccessory( final Preferences prefs ) {
		this( prefs, DEFAULT_ID );
	}
	
	
	/** Constructor */
	public DefaultFolderAccessory( final Class<?> preferenceNode, final String preferenceID, final String subfolderName ) {
		this ( Preferences.userNodeForPackage( preferenceNode ), preferenceID, subfolderName );
	}
	
	
	/** Constructor */
	public DefaultFolderAccessory( final Class<?> preferenceNode, final String preferenceID ) {
		this ( Preferences.userNodeForPackage( preferenceNode ), preferenceID );
	}
	
	
	/** Constructor */
	public DefaultFolderAccessory( final Class<?> preferenceNode ) {
		this ( Preferences.userNodeForPackage( preferenceNode ), DEFAULT_ID );
	}
	
	
	/** Determine if the default folder has been specified. */
	public boolean defaultFolderSpecified() {
		return _folderTracker.getMostRecentFile() != null;
	}
	
	
	/** Get the default folder */
	public File getDefaultFolder() {
		final File recentFolder = _folderTracker.getMostRecentFile();
		
		if ( recentFolder != null && recentFolder.exists() ) {
			if ( _subfolderName != null ) {
				final File defaultFolder = new File( recentFolder, _subfolderName );
				if ( !defaultFolder.exists() ) {
					defaultFolder.mkdir();
				}
				return defaultFolder;
			}
			else {
				return recentFolder;
			}
		}
		
		return null;
	}
	
	
	/** Get the default folder URL */
	public URL getDefaultFolderURL() {
		try {
			final File defaultFolder = getDefaultFolder();
			return ( defaultFolder != null ) ? defaultFolder.toURI().toURL() : null;			
		}
		catch( MalformedURLException exception ) {
			throw new RuntimeException( "Exception getting the default document URL.", exception );
		}
	}
	
	
	/** register for events from the specified file chooser */
	public void applyTo( final JFileChooser fileChooser ) {
		fileChooser.setAccessory( new AccessoryView().getComponent() );
		applyDefaultFolder( fileChooser );
		fileChooser.addPropertyChangeListener( this );
	}
	
	
	/** Apply default folder to file chooser */
	public void applyDefaultFolder( final JFileChooser fileChooser ) {
		fileChooser.setCurrentDirectory( getDefaultFolder() );
	}
	
	
	/** Implement the propertyChange event handler for this listener */
	public void propertyChange( final PropertyChangeEvent event ) {
		_activeFileChooser = (JFileChooser)event.getSource();
	}
	
	
	/** view for displaying the buttons */
	private class AccessoryView {
		final private Box _view;
		
		/** Constructor */
		public AccessoryView() {
			_view = new Box( BoxLayout.Y_AXIS );
			makeContents();
		}
		
		
		/** get the component */
		public JComponent getComponent() {
			return _view;
		}
		
		
		/** handle the action event */
		protected void handleToDefaultFolderAction() throws Exception {
			if ( !defaultFolderSpecified() ) {
				String message = "A default folder has not been specified.  Would you like to specify one now?\n";
				if ( _subfolderName != null ) {
					message += "Note that you will be specifying the parent folder of " + _subfolderName + ".\n";
					message += _subfolderName + " under the selected folder will hold your files.";
				}
				final int confirm = JOptionPane.showConfirmDialog( _view, message, "Specify Default Folder", JOptionPane.YES_NO_OPTION );
				
				try {
					switch ( confirm ) {
						case JOptionPane.YES_OPTION:
							if ( !showDefaultFolderSelector() )  return;
							break;
						default:
							return;
					}					
				}
				catch ( Exception exception ) {
					exception.printStackTrace();
				}
			}
			
			applyDefaultFolder( _activeFileChooser );		
		}
		
		
		/**
		 * Show the selector for selecting the default folder.
		 * @return true if the user selected a default folder and false if not.
		 */
		protected boolean showDefaultFolderSelector() throws Exception {
			final JFileChooser selector = new JFileChooser( _activeFileChooser.getCurrentDirectory() );
			selector.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
			final String title = ( _subfolderName == null ) ? "Default Folder" : "Default Parent Folder of " + _subfolderName;
			selector.setDialogTitle( title );
			final int status = selector.showDialog( _view, "Make Default" );
			
			switch ( status ) {
				case JFileChooser.APPROVE_OPTION:
					final File defaultFolder = selector.getSelectedFile();
					if ( defaultFolder != null ) {
						_folderTracker.cacheURL( defaultFolder.toURI().toURL() );
						return true;						
					}
					else {
						return false;
					}
				default:
					return false;
			}
		}
		
		
		/** make the box view */
		protected void makeContents() {
			_view.add( makeDefaultFolderNavigationButton() );
			
			_view.add( Box.createVerticalGlue() );
		}
		
		
		/** Make the button for navigating to the default folder. */
		protected Component makeDefaultFolderNavigationButton() {
			final JButton goButton = new JButton( "Default Folder" );
			goButton.setToolTipText( "Navigate to the default folder." );
			goButton.addActionListener( new ActionListener() {
				public void actionPerformed( final ActionEvent event ) {
					try {
						handleToDefaultFolderAction();
					}
					catch( Exception exception ) {
						reportException( exception );
					}
				}
			});
			
			return goButton;
		}
		
		
		/** report exceptions */
		protected void reportException( final Exception exception ) {
			final String message = exception.getMessage();
			JOptionPane.showMessageDialog( _view, message, "Default Folder Error", JOptionPane.ERROR_MESSAGE );
		}
	}
}
