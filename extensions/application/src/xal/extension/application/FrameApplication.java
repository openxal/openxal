//
//  FrameApplication.java
//  xal
//
//  Created by Thomas Pelaia on 3/28/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.application;

import java.awt.Point;
import java.awt.Window;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.Toolkit;

import java.util.*;
import java.util.logging.*;
import java.net.*;
import java.io.*;
import java.util.prefs.Preferences;

import xal.tools.StringJoiner;
import xal.tools.apputils.files.*;
import xal.tools.messaging.MessageCenter;
import xal.extension.service.*;


/** Application subclass for JFrame based applications. */
public class FrameApplication extends Application implements XalDocumentListener {
    private volatile int _retainCount;   // counts items that want to keep the application alive
	
	
    /** 
	* Constructor
	* @param adaptor The application adaptor used for customization.
	*/
    protected FrameApplication( final ApplicationAdaptor adaptor ) {
		this( adaptor, new URL[]{} );
    }
    
    
    /** 
	* Constructor 
	* @param adaptor The application adaptor used for customization.
	* @param urls An array of document URLs to open upon startup. 
	*/
    protected FrameApplication( final ApplicationAdaptor adaptor, final URL[] urls ) {
		super( adaptor, urls );
		
        _retainCount = 0;
    }
    
    /** 
	* Initialize the Application and open the documents specified by the URL array.
	* If the URL array is empty, then create one empty document.
	* 
	* @param urls An array of document URLs to open.
	*/
    protected void setup( final URL[] urls ) {		
        registerEvents();		
		
		try {
			SwingUtilities.invokeAndWait( new Runnable() {
				public void run() {
					setupConsole();
					
					// Make the open/save file choosers as early as possible since JFileChooser has a known
					// race condition bug.
					makeFileChoosers();
					
					// setup the application commander and load custom application commands
					_commander = makeCommander();
					_applicationAdaptor.customizeCommands( _commander );
					
					// notify listeners that the initial documents, if any, will be opened
					_noticeProxy.applicationWillOpenInitialDocuments();

					if ( urls == null || urls.length == 0 ) {
                        if ( showsWelcomeDialogAtLaunch() ) {
                            showWelcomeDialog();
                        }
                        else {
                            newDocument();
                        }
					}
					else {
						for ( int index = 0 ; index < urls.length ; index++ ) {
							openDocument( urls[index] );
						}
					}
					
					// if multiple documents are opened then cascade them
					if ( _openDocuments.size() > 1 ) {
						cascadeWindowsAbout( _openDocuments.get(0) );
					}					
				}
			});
		}
		catch ( InterruptedException exception ) {
			exception.printStackTrace();
			throw new RuntimeException( exception );
		}
		catch ( java.lang.reflect.InvocationTargetException exception ) {
			exception.printStackTrace();
			throw new RuntimeException( exception );
		}
		
		registerApplicationStatusService();   // comment out application service registration until it is developed -tap
		
        _applicationAdaptor.applicationFinishedLaunching();
    }
    
	
    /**
	 * Add a new document to this application and if makeVisible is true, show it
	 * @param document the document to produce
	 * @param makeVisible make the document visible
     */
    public void produceDocument( final XalAbstractDocument document, final boolean makeVisible ) {
        _openDocuments.add( document );
        ((XalDocument)document).addXalDocumentListener( this );
		document.initMainWindow();
		if ( makeVisible ) {
			document.showDocument();
		}
        _noticeProxy.documentCreated( (XalDocument)document );
    }

	
    /** Create and open a new empty document. */
    protected void newDocument() {
        updateNextDocumentOpenLocation();
		newDocument("");
    }
	
	
    /** 
	 * Create and open a new empty document of the specified type. 
	 * @param type the type of document to create.
	 */
    protected void newDocument( final String type ) {
        XalDocument document = (XalDocument)_applicationAdaptor.generateEmptyDocument( type );
		produceDocument( document );
    }
    
    
    /**
	 * Handle the "Revert To Saved" action by reverting the specified document 
     * to that of its source file.
     * @param document The document to revert.
     */
    protected void revertToSaved( final XalAbstractDocument document ) {
        // don't revert if there are no changes
        if ( !document.hasChanges() ) {
            document.displayWarning( "No revert!", "This document reports no changes from the original." );
            return;
        }
        
        URL source = document.getSource();
        
        if ( source == null ) {
            document.displayWarning( "No revert!", "There is no source to revert to." );
            return;
        }
        
        retainApp();
        try {
            if ( document.closeDocument() ) {
                openDocument( source );
            }
        }
        finally {   //do this regardless of thrown exceptions
            releaseApp();
        }
    }
    
    
    /** Handle the "Close All" action by closing all open documents and opening a new empty document. */
    protected void closeAllDocuments() {
		try {
			retainApp();
            updateNextDocumentOpenLocation();
            super.closeAllDocuments();
            showWelcomeDialog();
		}
		finally {
			releaseApp();
		}
    }
	
	
    /** Implement XalDocumentListener.  Empty implementation. */
    public void titleChanged( final XalDocument document, final String newTitle ) {
    }
    
    
    /** Implement XalDocumentListener.  Empty implementation. */
    public void hasChangesChanged( final XalDocument document, final boolean newHasChangesStatus ) {
    }
    
    
    /** Implement XalDocumentListener.  Empty implementation. */
    public void documentWillClose( final XalDocument document ) {
    }
    
    
    /** 
	* Implement XalDocumentListener.  When a document has closed, the application 
	* receives this event and removes the document from its open documents list.
	* If there are no documents remaining, the application quits.
	* @param document The document that has closed.
	*/
    public void documentHasClosed( final XalDocument document ) {
        document.removeXalDocumentListener( this );
        _openDocuments.remove( document );
        _noticeProxy.documentClosed( document );
        
        terminateOnStatus();
    }
    
	
    /**
	 * Increment the retain count.  The application will not quit while the 
     * retain count is at least 1.  This method gets called to keep the application
     * alive when other properties indicate the application should quit.
     * For example if the application has no open documents, the application can 
     * be prevented from terminating by incrementing the retain count.
     */
    synchronized private void retainApp() {
        ++_retainCount;
    }
    
    
    /**
	 * Decrement the retain count. The application will not quit while the 
     * retain count is at least 1.
     * @see retainApp
     */
    synchronized private void releaseApp() {
        --_retainCount;
        terminateOnStatus();
    }
    
    
    /**
	 * Check the status of the application to see if the application should quit.
     * Check whether there are any open documents or if the application retain 
     * count is greater than zero.  If these criteria are not met, terminate the 
     * application.
     */
    private void terminateOnStatus() {
        if ( _openDocuments.size() == 0 && _retainCount < 1 ) {
            quit();
        }
    }
    
    
    /**
	 * Handle the launching of the application by creating the application instance
     * and performing application initialization.
     * @param adaptor The custom application adaptor.
	 * @param urls The URLs of documents to open upon launching the application
     */
    static public void launch( final ApplicationAdaptor adaptor, final URL[] urls ) {
        new FrameApplication( adaptor, urls );
    }	
}
