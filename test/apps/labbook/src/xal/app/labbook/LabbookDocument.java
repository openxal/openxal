/*
 * LabbookDocument.java
 *
 * Created on Mon Sep 18 12:51:23 EDT 2006
 *
 * Copyright (c) 2006 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.labbook;

import xal.extension.application.*;
import xal.extension.bricks.WindowReference;
import xal.tools.database.*;
import xal.tools.messaging.MessageCenter;

import java.net.URL;
import java.awt.Color;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import java.util.*;
import java.util.Date;
import java.sql.Connection;


/**
 * LabbookDocument
 * @author  t6p
 */
class LabbookDocument extends XalDocument implements ControllerListener {
	/** document message center */
	protected MessageCenter MESSAGE_CENTER;
	
	/** reference to the main window */
	protected WindowReference _mainWindowReference;
	
	/** controller for managing the summary view */
	protected SummaryController _summaryController;
	
	/** controller for the details view */
	protected DetailsController _detailsController;
	
	/** controller for managing images */
	protected ImageController _imageController;
	
	/** controller for managing attachments */
	protected AttachmentController _attachmentController;
	
	/** label for displaying document messages */
	protected JLabel _mainMessageLabel;
	
	/** button to publish entries */
	protected JButton _publishButton;
	
	
	/** Create a new empty document */
    public LabbookDocument() {
		MESSAGE_CENTER = new MessageCenter( "Labbook Document Message Center" );
		MESSAGE_CENTER.registerTarget( this, ControllerListener.class );
		
        setSource( null );
    }
    
    
    /** Make a main window by instantiating the my custom window. */
    public void makeMainWindow() {
		final WindowReference windowReference = getDefaultWindowReference( "MainWindow", this );
		_mainWindowReference = windowReference;
		
        mainWindow = (XalWindow)windowReference.getWindow();
		
		_mainMessageLabel = (JLabel)windowReference.getView( "MainMessageLabel" );
				
		_summaryController = new SummaryController( MESSAGE_CENTER, windowReference );
		_detailsController = new DetailsController( MESSAGE_CENTER, windowReference );
		_imageController = new ImageController( MESSAGE_CENTER, windowReference );
		_attachmentController = new AttachmentController( MESSAGE_CENTER, windowReference );
		
		_publishButton = (JButton)windowReference.getView( "PublishButton" );
		_publishButton.setEnabled( false );
		_publishButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				publishEntry();
			}
		});
    }
    
    
    /**
	 * Register custom commands. 
     * @param commander The commander with which to register the custom commands.
     */
    public void customizeCommands( final Commander commander ) {
		DetailsController.registerCommands( commander );
	}
	
	
	/** publish the entry to the logbook */
	public void publishEntry() {
		Publisher publisher = null;
		
		try {			
			validate();
			
			publisher = requestPublisher();
			
			if ( publisher == null ) {
				displayWarning( "No Entry Made" , "No entry was made because the user cancelled the operation." );
				updateMessage( "User cancelled publishing of entry.", true );
				return;
			}
			
			final String userID = publisher.getConnectionDictionary().getUser();
			
			final List<String> logbookIDs = new ArrayList<String>();
			logbookIDs.add( "OP" );		// Operations logbook
			logbookIDs.add( "AP" );		// Accelerator Physics logbook
			
			final List<String> categoryIDs = new ArrayList<String>();
			categoryIDs.add( "AP" );	// Accelerator Physics category
			
			final LogbookEntry entry = new LogbookEntry( logbookIDs, categoryIDs );
			
			entry.setTitle( _summaryController.getTitle() );
			entry.setSummary( _summaryController.getSummary() );
			entry.setShiftSummary( _summaryController.isShiftSummary() );

			entry.setImageEntries( _imageController.getImageEntries() );
			
			final String detailsHTML = _detailsController.isEmpty() ? null : _detailsController.getHTML();
			entry.setDetailsContent( detailsHTML );
			
			entry.setAttachmentEntries( _attachmentController.getMediaEntries() );
			
			entry.publish( userID, publisher );
			
			final Connection connection = publisher.getConnection();
			publisher.commit();
			
			updateMessage( "Entry published." );
			setHasChanges( false );
			_publishButton.setEnabled( false );
		}
		catch( Exception exception ) {
			displayError( exception );
			updateMessage( "Failed attempt to publish entry.", true );
			if ( publisher != null )  publisher.rollback();
		}
		finally {
			if ( publisher != null )  publisher.closeConnection();
		}
	}
	
	
	/** validate all parts of the entry for publication */
	protected void validate() {
		if ( !_summaryController.validate() ) {
			throw new EntryValidationException( _summaryController.getValidationText() );
		}
		else if ( !_imageController.validate() ) {
			throw new EntryValidationException( _imageController.getValidationText() );
		}
		else if ( !_attachmentController.validate() ) {
			throw new EntryValidationException( _attachmentController.getValidationText() );
		}
	}
	
	
	/** update the displayed message indicating normal information */
	public void updateMessage( final String message ) {
		updateMessage( message, false );
	}
	
	
	/** update the displayed message */
	public void updateMessage( final String message, boolean isWarning ) {
		_mainMessageLabel.setText( message );
		_mainMessageLabel.setForeground( isWarning ? Color.RED : Color.GREEN.darker() );
	}
	
	
	/** 
	 * Display a connection dialog to the user and connect to the database using the resulting connection dictionary.
	 * @return a new publisher
	 */
	protected Publisher requestPublisher() {
		ConnectionDictionary dictionary = ConnectionDictionary.defaultDictionary();
		if ( dictionary == null ) {
			dictionary = new ConnectionDictionary();
		}
		else {
			dictionary.setUser( "" );
			dictionary.setPassword( "" );
		}
		final ConnectionDialog dialog = ConnectionDialog.getInstance( mainWindow, dictionary );
		final Connection connection = dialog.showConnectionDialog( dictionary.getDatabaseAdaptor() );
		dictionary = dialog.getConnectionDictionary();
		
		return connection != null ? new Publisher( dictionary, connection ) : null;
	}
	
    
    /**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs( final URL url ) {}
	
	
	/** indicates that the document has been modified */
	public void documentModified( final AbstractController source ) {
		setHasChanges( true );
		_publishButton.setEnabled( true );
	}
}




