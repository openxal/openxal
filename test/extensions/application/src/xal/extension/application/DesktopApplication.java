//
//  DesktopApplication.java
//  xal
//
//  Created by Thomas Pelaia on 3/29/05.
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

import xal.Info;
import xal.tools.StringJoiner;
import xal.tools.apputils.files.*;
import xal.tools.messaging.MessageCenter;
import xal.extension.service.*;


/** Application subclass for JDesktopPane based applications. */
public class DesktopApplication extends Application implements XalInternalDocumentListener {
	/** desktop pane that contains the document windows. */
	private JFrame _desktopFrame;
	
	/** default desktop menubar */
	private JMenuBar _defaultDesktopMenu;
	
	
    /** 
	* Constructor 
	* @param adaptor The application adaptor used for customization.
	*/
    protected DesktopApplication( final DesktopApplicationAdaptor adaptor ) {
		this( adaptor, new URL[]{} );
    }
    
    
    /** 
	* Constructor 
	* @param adaptor The application adaptor used for customization.
	* @param urls An array of document URLs to open upon startup. 
	*/
    protected DesktopApplication( final DesktopApplicationAdaptor adaptor, final URL[] urls ) {
		super( adaptor, urls );
    }
	
	
    /** 
	* Initialize the Application and open the documents specified by the URL array.
	* If the URL array is empty, then create one empty document.
	* @param urls An array of document URLs to open.
	*/
    protected void setup( final URL[] urls ) {
		try {
			SwingUtilities.invokeAndWait( new Runnable() {
				public void run() {
					createDesktopFrame();
					
					registerEvents();		
					
					setupConsole();
					
					// Make the open/save file choosers as early as possible since JFileChooser has a known race condition bug.
					makeFileChoosers();
					
					// setup the application commander and load custom application commands
					_commander = makeCommander();
					_applicationAdaptor.customizeCommands( _commander );
					setupMenuBar( _commander );
					
					// notify the adaptor that the desktop frame will be displayed
					((DesktopApplicationAdaptor)_applicationAdaptor).applicationWillDisplayDesktopPane();
					
					_desktopFrame.setVisible( true );
					_desktopFrame.toFront();
					
					// notify listeners that the initial documents, if any, will be opened
					_noticeProxy.applicationWillOpenInitialDocuments();
					
					if ( urls.length > 0 ) {
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
			throw new RuntimeException( exception );
		}
		catch ( java.lang.reflect.InvocationTargetException exception ) {
			throw new RuntimeException( exception );
		}
		
		//registerApplicationStatusService();   // comment out application service registration until it is developed -tap
		
        _applicationAdaptor.applicationFinishedLaunching();
    }
    
    
    /**
	 * Make an application commander
     * @return the commander that loads default and custom actions.
     */
    protected Commander makeCommander() {
        return new Commander( this );
    }
	
	
	/** Setup the menubar */
	private void setupMenuBar( final Commander commander ) {
		_defaultDesktopMenu = commander.getMenubar();
		_desktopFrame.setJMenuBar( _defaultDesktopMenu );
	}
	
	
	/** Create the top level desktop frame */
	private void createDesktopFrame() {
		final JDesktopPane desktop = new JDesktopPane();
		desktop.setDragMode( ((DesktopApplicationAdaptor)_applicationAdaptor).drawsDocumentContentOnDrag() ? JDesktopPane.LIVE_DRAG_MODE : JDesktopPane.OUTLINE_DRAG_MODE );
		
		_desktopFrame = new JFrame( Info.getLabel() + " - " + _applicationAdaptor.applicationName() );
		_desktopFrame.setSize( 1024, 768 );
		_desktopFrame.setContentPane( desktop );
		
		_desktopFrame.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
		_desktopFrame.addWindowListener( newDesktopWindowHandler() );
	}
	
	
	/** Get the desktop pane */
	private JDesktopPane getDesktopPane() {
		return (JDesktopPane)_desktopFrame.getContentPane();
	}
	
	
	/**
	 * Get the selected internal window.
	 * @return the selected internal window.
	 */
	XalInternalWindow getSelectedWindow() {
		final JInternalFrame selectedFrame = getDesktopPane().getSelectedFrame();
		return ( selectedFrame instanceof XalInternalWindow ) ? (XalInternalWindow)selectedFrame : null;
	}
	
	
	/**
	 * Get the selected internal window.
	 * @return the selected internal window.
	 */
	XalInternalDocument getSelectedDocument() {
		final XalInternalWindow selectedWindow = getSelectedWindow();
		return ( selectedWindow != null ) ? selectedWindow.getInternalDocument() : null;
	}
    
    
    /** Create a new window listener. */
    private WindowListener newDesktopWindowHandler() {
        return new WindowAdapter() {
            public void windowClosing( final WindowEvent event ) {
                quit();
            }
        };
    }
	
	
    /**
	 * Add a new document to this application and if makeVisible is true, show it
	 * @param document the document to produce
	 * @param makeVisible make the document visible
     */
    public void produceDocument( final XalAbstractDocument document, final boolean makeVisible ) {
        _openDocuments.add( document );
        ((XalInternalDocument)document).addXalInternalDocumentListener( this );
		document.initMainWindow();
		getDesktopPane().add( (XalInternalWindow)document.getDocumentView() );
		if ( makeVisible ) {
			document.showDocument();
		}
        _noticeProxy.documentCreated( document );
    }
	
	
    /** Create and open a new empty document. */
    protected void newDocument() {
		newDocument("");
    }
	
	
    /** 
	 * Create and open a new empty document of the specified type. 
	 * @param type the type of document to create.
	 */
	protected void newDocument( final String type ) {
        final XalInternalDocument document = (XalInternalDocument)_applicationAdaptor.generateEmptyDocument( type );
		produceDocument( document );		
	}
	
    
    /**
	 * Handle the launching of the application by creating the application instance
     * and performing application initialization.
     * @param adaptor The custom application adaptor.
	 * @param urls The URLs of documents to open upon launching the application
     */
    static public void launch( final DesktopApplicationAdaptor adaptor, final URL[] urls ) {
        new DesktopApplication( adaptor, urls );
    }
	
	
    /** Handle document title change event.  Empty implementation. */
    public void titleChanged( final XalInternalDocument document, final String newTitle ) {}
    
    
    /** Handle document change event.  Empty implementation. */
    public void hasChangesChanged( final XalInternalDocument document, final boolean newHasChangesStatus ) {}
    
    
    /** Handle document closing event.  Empty implementation. */
    public void documentWillClose( final XalInternalDocument document ) {}
    
    
    /** 
	 * When a document has closed, the application receives this event and removes the document from its open documents list.
	 * If there are no documents remaining, the application quits.
	 * @param document The document that has closed.
	 */
    public void documentHasClosed( final XalInternalDocument document ) {
        document.removeXalInternalDocumentListener( this );
        _openDocuments.remove( document );
        _noticeProxy.documentClosed( document );
    }
	
	
	/**
	 * Handle the document activated event.
	 * @param document the document that has been activated.
	 */
	public void documentActivated( final XalInternalDocument document ) {
		_desktopFrame.setJMenuBar( document.getDesktopMenubar() );
		_desktopFrame.validate();
	}
	
	
	/**
	 * Handle the document activated event.
	 * @param document the document that has been activated.
	 */
	public void documentDeactivated( final XalInternalDocument document ) {
		_desktopFrame.setJMenuBar( _defaultDesktopMenu );
		_desktopFrame.validate();
	}
}
