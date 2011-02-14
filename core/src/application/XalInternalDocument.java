//
//  XalInternalDocument.java
//  xal
//
//  Created by Thomas Pelaia on 3/29/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.application;

import java.net.*;
import java.awt.print.*;
import java.awt.Dimension;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
import javax.swing.JOptionPane;
import javax.swing.event.*;
import javax.swing.JMenuBar;


/**
 * The base class for custom documents.  Subclasses of this class need to define the logic for their document.
 * Every document has a main window and a URL source that provides persistent storage.
 *
 * @author  t6p
 */
abstract public class XalInternalDocument extends XalAbstractDocument {
	// public static constants for confirmation dialogs
	final static public int YES_OPTION = XalAbstractDocument.YES_OPTION;
	final static public int NO_OPTION = XalAbstractDocument.NO_OPTION;
	
    /** this document's associated window */
    protected XalInternalWindow _mainWindow;     // The main window for the document
	
	/** this document's window event handler */
	protected WindowEventHandler _windowEventHandler;
	
	/** desktop menubar to display when this document is selected */
	private JMenuBar _desktopMenubar;
	    
    /** the document event proxy */
    protected XalInternalDocumentListener _documentListenerProxy;    // The proxy for document events
    
    
    /** Constructor for new documents */
    public XalInternalDocument() {
		super();
    }
    
    
    /** Register this document as a source of DocumentListener events. */
    protected void registerEvents() {
		super.registerEvents();
        _documentListenerProxy = (XalInternalDocumentListener)_messageCenter.registerSource( this, XalInternalDocumentListener.class );
    }
    
    
    /** Add the listener for events from this document. */
    public void addXalInternalDocumentListener( final XalInternalDocumentListener listener ) {
        _messageCenter.registerTarget( listener, this, XalInternalDocumentListener.class );
    }
    
    
    /** Remove the listener from event from this document. */
    public void removeXalInternalDocumentListener( final XalInternalDocumentListener listener ) {
        _messageCenter.removeTarget( listener, this, XalInternalDocumentListener.class );
    }
    
    
    /** Construct the main window and associate it with this document. */
	void setupMainWindow() {
        makeMainWindow();
		makeDesktopMenubar();
		_windowEventHandler = new WindowEventHandler();
		_mainWindow.addInternalFrameListener( _windowEventHandler );
        addXalInternalDocumentListener( _mainWindow );
        _mainWindow.titleChanged( this, title );
    }
	
	
	/** Generate the desktop menubar */
	private void makeDesktopMenubar() {
		final Commander commander = makeCommander();
		customizeDesktopCommands( commander );
		_desktopMenubar = commander.getMenubar();
	}
    
    
    /** Subclasses may override this method to provide a custom Commander. */
    protected Commander makeCommander() {
        // create a document commander
        return new Commander( Application.getApp().getCommander(), this );
    }
	
	
	/** 
	 * Get the document's desktop menubar.
	 * @return the desktop menubar to display when this document is selected.
	 */
	JMenuBar getDesktopMenubar() {
		return _desktopMenubar;
	}
    
    
    /**
	 * Set the document title.
     * @param newTitle The new title for this document.
     */
    public void setTitle( final String newTitle ) {
		super.setTitle( newTitle );
        _documentListenerProxy.titleChanged( this, title );
    }	
    
    
    /**
	 * Set the whether this document has changes.
     * @param changeStatus Status to set whether this document has changes that need saving.
     */
    public void setHasChanges( final boolean changeStatus ) {
        if ( changeStatus != hasChanges ) {
            hasChanges = changeStatus;
            _documentListenerProxy.hasChangesChanged( this, hasChanges );
        }
    }
	
	
    /**
	 * This method is a request to close a document.  It may be called when, for 
     * example, the user selects "Close" from the File menu, or when the user closes the 
     * window with the close button, or when the application quits.  This request
     * starts a series of events which closes the document.  Xal document 
     * listeners are notified that the document will close.  They may perform 
     * any cleanup as necessary before the document closes.  Then the listeners 
     * are informed that the document has closed.  The application removes 
     * the document from its list of open documents and informs its listeners 
     * that the document has been closed.  If there are any unsaved changes, the 
     * user is given an opportunity to not close the document so they can save 
     * the changes.
     */
    protected boolean closeDocument() {
		if ( warnUserOfUnsavedChangesWhenClosing() && hasChanges() ) {
			if ( !_mainWindow.userPermitsCloseWithUnsavedChanges() )  return false;
		}
		
        _documentListenerProxy.documentWillClose( this );
        willClose();
        _documentListenerProxy.documentHasClosed( this );
		
		freeResources();
		
        return true;
    }
	
	
	/** Free document resources. */
	final protected void freeResources() {
		super.freeResources();
		
		_mainWindow.removeInternalFrameListener( _windowEventHandler );
		
		_windowEventHandler = null;
		_documentListenerProxy = null;
		_mainWindow = null;
	}
    
    
    /**
	 * Get the main window for this document.
     * @return The main window for this document.
     */
    public XalInternalWindow getMainWindow() {
        return _mainWindow;
    }
    
    
    /**
		* Implement the method for XalAbstractDocument.
     * @return The main window for this document.
     */
    public XalDocumentView getDocumentView() {
        return _mainWindow;
    }
    
    
    /**
	 * Subclasses should override this method to register custom document commands (if any) for the desktop menu.  You do so by registering 
	 * actions with the commander.  Those action instances should have a reference to this document so the action is 
	 * executed on the document when the action is activated.  The default implementation of this method does nothing.
     * @param commander The commander that manages commands.
     * @see Commander#registerAction(Action)
     */
    protected void customizeDesktopCommands( final Commander commander ) {
    }
	

	/**
	 * Subclasses should override this method if this document should use a menu definition
	 * other than the default specified in application adaptor.  The document menu inherits the
	 * application menu definition.  This custom path allows the document to modify the
	 * application wide definitions for this document.  By default this method returns null.
	 * @return The menu definition properties file path in classpath notation
	 * @see ApplicationAdaptor#getPathToResource
	 */
	protected String getCustomInternalMenuDefinitionPath() {
		return null;
	}

	
	
	/** window event handler **/
	private class WindowEventHandler extends InternalFrameAdapter {
		/** Handle window closing events. */
		public void internalFrameClosing( final InternalFrameEvent event ) {
			closeDocument();
		}
		
		/** Handle the window being activated. */
		public void internalFrameActivated( final InternalFrameEvent event ) {
			_documentListenerProxy.documentActivated( XalInternalDocument.this );
		}
		
		/** Handle the window being deactivated. */
		public void internalFrameDeactivated( final InternalFrameEvent event ) {
			_documentListenerProxy.documentDeactivated( XalInternalDocument.this );
		}		
	}
}
