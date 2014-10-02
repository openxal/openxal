//
//  XalInternalWindow.java
//  xal
//
//  Created by Thomas Pelaia on 3/29/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.extension.application;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.Point;
import java.awt.Toolkit;
import java.beans.PropertyVetoException;
import java.util.*;
import java.util.logging.*;
import java.io.*;

import xal.tools.messaging.MessageCenter;
import xal.tools.apputils.ImageCaptureManager;


/**
 * The base class for custom windows that are the main windows for documents.  
 * Subclasses need to define their custom views.
 *
 * @author  t6p
 */
abstract public class XalInternalWindow extends JInternalFrame implements XalDocumentView, XalInternalDocumentListener {
	// public static constants for confirmation dialogs
	final static public int YES_OPTION = JOptionPane.YES_OPTION;
	final static public int NO_OPTION = JOptionPane.NO_OPTION;
	
    //------------- instance variables -----------------------------------------
    
	/** The toolbar associated with this window */
	private JToolBar _toolBar;
	
    /** The document corresponding to this main window */
    protected XalInternalDocument _document;
	    
    
    /** Creates a new instance of WindowAdaptor */
    public XalInternalWindow( final XalInternalDocument aDocument ) {
		super( "", true, true, true, true );
		
		positionWindow();
        registerEvents();
        _document = aDocument;
        makeFrame();
    }
	
	
	/** position this window relative to the currently active window */
	private void positionWindow() {
		final XalInternalWindow selectedWindow = ((DesktopApplication)Application.getApp()).getSelectedWindow();
		// offset this window relative to the active window if any
		if ( selectedWindow != null && selectedWindow.isVisible() && !selectedWindow.isIcon() ) {
			final java.awt.Container contentPane = selectedWindow.getContentPane();
			final int offset = ( (int)1.5 * ( contentPane.getLocationOnScreen().y - selectedWindow.getLocationOnScreen().y ) );
			final Point location = new Point( selectedWindow.getLocation() );
			location.translate( offset, offset );
			setLocation( location );
		}		
	}
    
	
    /** Register the event handlers */
    public void registerEvents() {
    }
	
	
	/**
	 * Get the internal document
	 * @return this window's internal document
	 */
	XalInternalDocument getInternalDocument() {
		return _document;
	}
    
    
    /** Make the frame and populate the menubar and toolbar. */
    public void makeFrame() {
        setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
        makeLayout();
        
        Commander commander = makeCommander();
        customizeCommands( commander );
        _document.customizeCommands( commander );
		
		final JMenuBar menuBar = commander.getMenubar();
        
		if ( menuBar != null ) {
			setJMenuBar( commander.getMenubar() );			
		}
		
        if ( usesToolbar() )  { 
            _toolBar = commander.getToolbar();
            getContentPane().add( _toolBar, "North" );
        }
    }
    
    
    /** Subclasses should override this method to provide a custom Commander. */
    public Commander makeCommander() {
        // create a document commander off of the application commander and a document
        return new Commander( _document );
    }
	
	
	/**
	 * Get the toolbar associated with this window.
	 * @return This window's toolbar or null if none was added.
	 */
	public JToolBar getToolBar() {
		return _toolBar;
	}
	
	
    /** Override this method to register custom commands. */
    public void customizeCommands( final Commander commander ) {}
    
	
    /** Make the window layout. */
	private void makeLayout() {
        getContentPane().setLayout( new BorderLayout() );
    }
    
	
    /**
	 * Close this window.  Check to see if the document has unsaved changes and 
     * if so warn the user and allow them to cancel the close operation.
     */
    public void closeWindow() {
        releaseWindow();
    }
	
    
    /** Capture the window content as a PNG.  Present the user with a save dialog box so the image can be saved to a file. */
    public void captureAsImage() {
        try {
            ImageCaptureManager.defaultManager().saveSnapshot( this.getContentPane() );
        }
        catch( java.awt.AWTException exception ) {
			Logger.getLogger("global").log( Level.WARNING, "Failed to capture image.", exception ); 
            System.err.println( exception );
            displayWarning( exception );
        }
        catch( IOException exception ) {
			Logger.getLogger("global").log( Level.WARNING, "Failed to capture image.", exception ); 
            System.err.println( exception );
            displayWarning( exception );
        }
    }
	
    
    /** Show this window.  Make it visible (de-iconify if necessary) and bring it to the front. */
    public void showWindow() {
		try {
			setIcon( false );    // de-iconify this window
			setVisible( true );
			toFront();
			setSelected( true );
		}
		catch( PropertyVetoException exception ) {
			Application.displayError( "Document Exception", "Exception attempting to display document.", exception );
		}
    }
    
    
    /** Iconify this window. */
    public void hideWindow() {
		try {
			setIcon( true );     // iconify the window			
		}
		catch( PropertyVetoException exception ) {
			Application.displayError( "Document Exception", "Exception attempting to iconify document.", exception );
		}
    }
    
    
    /**
	 * Query the user to see if it is okay to close the document given that unsaved changes exist.
     * @return If the user allows the document to be closed.
     */
    public boolean userPermitsCloseWithUnsavedChanges() {
        String message = "Document has unsaved changes!\nDo you still want to close the document without saving changes?";
        int status = JOptionPane.showConfirmDialog(this, message, "Close Confirmation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        return status == JOptionPane.OK_OPTION;
    }
    
    
    /**
	 * Dispose of this window and remove its association with the document.
     */
    final public void releaseWindow() {
		freeCustomResources();
		
        dispose();
		
        _document.removeXalInternalDocumentListener( this );
		_document = null;
    }
    
	
	/**
	 * Dispose of custom window resources.  Subclasses should override this method
	 * to provide custom disposal of resources.  The default implementation does nothing.
	 */
	public void freeCustomResources() {
	}
	
	
	/**
	 * Generate the title on the title bar to reflect the document state.
	 * The title displays the application name, the document title if any and an asterisk
	 * if the document has unsaved changes.
	 */
	public void generateWindowTitle() {
		String windowTitle = "Untitled";
		final String documentTitle = _document.getTitle();
		
		if ( documentTitle != null && !documentTitle.isEmpty() ) {
			windowTitle = documentTitle;
			
			if ( _document.hasChanges() ) {
				windowTitle += "*";
			}
		}
		
		final String theTitle = windowTitle;
        
		// since this method often gets called from other threads we should take care to make it thread safe
		if ( SwingUtilities.isEventDispatchThread() ) {
			setTitle( theTitle );
		}
		else {
			try {
				SwingUtilities.invokeAndWait( new Runnable() {
					public void run() {
						setTitle( theTitle );
					}
				});
			}
			catch( Exception exception ) {
				exception.printStackTrace();
				throw new RuntimeException( "Exception updating the window title.", exception );
			}
			
		}
	}
	
    
    /**  
	 * Handle the document event indicating that the title has changed.
	 * Update the title on the title bar to reflect the new document title. 
	 * @param document The document initiating the title changed event.
	 * @param documentTitle The new document title.
	 */
    final public void titleChanged( final XalInternalDocument document, final String documentTitle ) {
		generateWindowTitle();
    }
    
    
    /** 
	* Update the title on the title bar to reflect whether the document has changes that need saving.
	* @param document The document initiating the event.
	* @param newHasChangesStatus The new status identifying whethe the document has changes to be saved
	* @see #titleChanged
	*/
    public void hasChangesChanged( final XalInternalDocument document, final boolean newHasChangesStatus ) {
        titleChanged( document, document.getTitle() );
    }
    
    
    /** Handle the event indicating that the document will close by closing the window in response. */
    public void documentWillClose( final XalInternalDocument document ) {
        closeWindow();
    }
    
    
    /** Handle document closed event.  Does nothing. */
    public void documentHasClosed( final XalInternalDocument document ) {}
	
	
	/**
	 * Handle the document activated event.
	 * @param document the document that has been activated.
	 */
	public void documentActivated( XalInternalDocument document ) {}
	
	
	/**
	 * Handle the document activated event.
	 * @param document the document that has been activated.
	 */
	public void documentDeactivated( XalInternalDocument document ) {}
    
    
    //----------- Methods subclasses might override ----------------------------
    
    /** Subclasses may override this method to create a toolbar. */
    public boolean usesToolbar() {
        return false;
    }
	
    
    //----------- Convenience methods ------------------------------------------
	
	
	/**
	 * Display a confirmation dialog with a title and message
	 * @param title The title of the dialog
	 * @param message The message to display
	 * @return YES_OPTION or NO_OPTION 
	 */
	public int displayConfirmDialog( final String title, final String message ) {
        Toolkit.getDefaultToolkit().beep();
        return JOptionPane.showInternalConfirmDialog( this, message, title, JOptionPane.YES_NO_OPTION );		
	}
	
    
    /**
	 * Display a warning dialog box and provide an audible alert.
     * @param aTitle Title of the warning dialog box.
     * @param message The warning message to appear in the warning dialog box.
     */
    public void displayWarning( final String aTitle, final String message ) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showInternalMessageDialog( this, message, aTitle, JOptionPane.WARNING_MESSAGE );
    }
	
    
    /**
	 * Display a warning dialog box showing information about an exception that 
     * has been thrown and provide an audible alert.
     * @param exception The exception whose description is being displayed.
     */
    public void displayWarning( final Exception exception ) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showInternalMessageDialog( this, exception.getMessage(), exception.getClass().getName(), JOptionPane.WARNING_MESSAGE );
    }    
    
    
    /**
	 * Display a warning dialog box with information about the exception and provide
     * an audible alert.  This method allows
     * clarification about the consequences of the exception (e.g. "Save Failed:").
     * @param aTitle Title of the warning dialog box.
     * @param prefix Text that should appear in the dialog box before the exception messasge.
     * @param exception The exception about which the warning dialog is displayed.
     */
    public void displayWarning( final String aTitle, final String prefix, final Exception exception ) {
        Toolkit.getDefaultToolkit().beep();
        String message = prefix + "\n" + "Exception: " + exception.getClass().getName() + "\n" + exception.getMessage();
        JOptionPane.showInternalMessageDialog( this, message, aTitle, JOptionPane.WARNING_MESSAGE );
    }
	
    
	
    
    /**
	 * Display an error dialog box and provide an audible alert.
     * @param aTitle Title of the warning dialog box.
     * @param message The warning message to appear in the warning dialog box.
     */
    public void displayError( final String aTitle, final String message ) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showInternalMessageDialog( this, message, aTitle, JOptionPane.ERROR_MESSAGE );
    }
    
    
    /**
	 * Display an error dialog box with information about the exception and 
     * provide an audible alert.
     * @param exception The exception about which the warning dialog is displayed.
     */
    public void displayError( final Exception exception ) {
        Toolkit.getDefaultToolkit().beep();
        String message = "Exception: " + exception.getClass().getName() + "\n" + exception.getMessage();
        JOptionPane.showInternalMessageDialog( this, message, exception.getClass().getName(), JOptionPane.ERROR_MESSAGE );
    }    
    
    
    /**
	 * Display an error dialog box with information about the exception and 
     * provide an audible alert.  This method allows
     * clarification about the consequences of the exception (e.g. "Save Failed:").
     * @param aTitle Title of the warning dialog box.
     * @param prefix Text that should appear in the dialog box before the exception messasge.
     * @param exception The exception about which the warning dialog is displayed.
     */
    public void displayError( final String aTitle, final String prefix, final Exception exception ) {
        Toolkit.getDefaultToolkit().beep();
        String message = prefix + "\n" + "Exception: " + exception.getClass().getName() + "\n" + exception.getMessage();
        JOptionPane.showInternalMessageDialog( this, message, aTitle, JOptionPane.ERROR_MESSAGE );
    }
}
