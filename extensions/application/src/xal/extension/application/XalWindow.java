/*
 * WindowAdaptor.java
 *
 * Created on March 17, 2003, 5:11 PM
 */

package xal.extension.application;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import java.io.*;

import xal.Info;
import xal.tools.messaging.MessageCenter;
import xal.tools.apputils.ImageCaptureManager;


/**
 * The base class for custom windows that are the main windows for documents.  Subclasses need to define their custom views.
 * @author  t6p
 */
public abstract class XalWindow extends JFrame implements XalDocumentView, XalDocumentListener {
	/** serial version ID required for Serializable */
	static final long serialVersionUID = 1L;

	// public static constants for confirmation dialogs
	final static public int YES_OPTION = JOptionPane.YES_OPTION;
	final static public int NO_OPTION = JOptionPane.NO_OPTION;
	    
    /** indicates whether to display a toolbar */
    private final boolean DISPLAYS_TOOLBAR;

	/** The toolbar associated with this window */
	private JToolBar _toolBar;
    
    /** The document corresponding to this main window */
    protected XalDocument document;
	    
    
    /** Creates a new instance of WindowAdaptor */
    public XalWindow( final XalDocument aDocument ) {
        this( aDocument, true );
    }
    
    
    public XalWindow( final XalDocument aDocument, final boolean displaysToolbar ) {
		positionWindow();
        registerEvents();
        
        document = aDocument;
        DISPLAYS_TOOLBAR = displaysToolbar;
        
        makeFrame();
    }
	
	
	/** position this window relative to the currently active window */
	private void positionWindow() {
        Application.getApp().updateNextDocumentOpenLocation();
        setLocation( Application.getApp().getNextDocumentOpenLocation() );
	}
    

    /** Register the event handlers */
    public void registerEvents() {
        addWindowListener( newWindowHandler() );
    }
    
    
    /** Make the frame and populate the menubar and toolbar. */
    public void makeFrame() {
        setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
        makeLayout();
        
        Commander commander = makeCommander();
        customizeCommands( commander );
        document.customizeCommands( commander );
        
        setJMenuBar( commander.getMenubar() );
        if ( usesToolbar() )  { 
            _toolBar = commander.getToolbar();
			if ( _toolBar != null ) {
				getContentPane().add( _toolBar, "North" );
			}
        }
    }
    
    
    /** Subclasses should override this method to provide a custom Commander. */
    public Commander makeCommander() {
        // create a document commander
        return new Commander( Application.getApp().getCommander(), document );
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
			final String applicationName = Application.getApp().getApplicationAdaptor().applicationName();
			final Date now = new Date();
			final String imageName = applicationName.replaceAll( " ", "" ) + "_" + new SimpleDateFormat("yyyyMMdd'T'HHmmss").format( now );
            ImageCaptureManager.defaultManager().saveSnapshot( this.getContentPane(), imageName );
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
    
    
    /** Create a new window listener. */
    public WindowListener newWindowHandler() {
        return new WindowAdapter() {
            public void windowClosing( final WindowEvent event ) {
                document.closeDocument();
            }
        };
    }
        
    
    /** Show this window.  Make it visible (de-iconify if necessary) and bring it to the front. */
    public void showWindow() {
        setState( java.awt.Frame.NORMAL );    // de-iconify this window
        setVisible( true );
        toFront();
    }
    
    
    /**
     * Iconify this window.
     */
    public void hideWindow() {
        setState( java.awt.Frame.ICONIFIED );     // iconify the window
    }
    
    
    /**
     * Query the user to see if it is okay to close the document given that 
     * unsaved changes exist.
     * @return If the user allows the document to be closed.
     */
    public boolean userPermitsCloseWithUnsavedChanges() {
        String message = "Document has unsaved changes!\nDo you still want to close the document without saving changes?";
        int status = JOptionPane.showConfirmDialog(this, message, "Close Confirmation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        return status == JOptionPane.OK_OPTION;
    }
    
    
    /** Dispose of this window and remove its association with the document. */
    final public void releaseWindow() {
		freeCustomResources();
        dispose();
        document.removeXalDocumentListener( this );
		document = null;
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
		// prefix the window title with the label for this version of Open XAL
		final StringBuffer windowTitle = new StringBuffer( "[" + Info.getLabel() +  "] - " );

		// append the application name
		windowTitle.append( Application.getApp().getApplicationAdaptor().applicationName() );

		// append the document title
		String documentTitle = document.getTitle();
		if ( documentTitle != null && !documentTitle.isEmpty() ) {
			windowTitle.append( " - " + documentTitle );
			
			final boolean documentModified = document.hasChanges();
			if ( documentModified ) {
				windowTitle.append( "*" );
			}
			getRootPane().putClientProperty( "Window.documentModified", documentModified );
		}
		        
		// since this method often gets called from other threads we should take care to make it thread safe
		if ( SwingUtilities.isEventDispatchThread() ) {
			setTitle( windowTitle.toString() );
		}
		else {
			try {
				SwingUtilities.invokeAndWait( new Runnable() {
					public void run() {
						setTitle( windowTitle.toString() );
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
	 * Implement XalDocumentListener and handle the event where the title has changed. Update the title on the title bar to reflect the new document title. 
	 * @param document The document initiating the title changed event.
	 * @param documentTitle The new document title.
	 */
    final public void titleChanged( final XalDocument document, final String documentTitle ) {
		generateWindowTitle();
		
		// update the document's title bar icon
		final java.net.URL source = document.getSource();
		if ( source != null ) {
			final String protocol = source.getProtocol();
			
			if ( protocol != null && protocol.equals( "file" ) ) {
                try { 
                    final File file = new File( source.toURI() );

                    if ( file.exists() ) {
                        getRootPane().putClientProperty( "Window.documentFile", file );
                    }
                }
                catch( Exception exception ) {
                    exception.printStackTrace();
                }
			}
		}
    }
    
    
    /** 
     * Implement XalDocumentListener.  Update the title on the title bar to reflect whether the document has changes that need saving.
     * @param document The document initiating the event.
     * @param newHasChangesStatus The new status identifying whethe the document has changes to be saved
     * @see #titleChanged
     */
    public void hasChangesChanged( final XalDocument document, final boolean newHasChangesStatus ) {
        titleChanged( document, document.getTitle() );
    }
    
    
    /** 
     * Implement XalDocumentListener.  Event indicating that the document will close.
     * Closes the window in response.
     */
    public void documentWillClose( final XalDocument document ) {
        closeWindow();
    }
    
    
    /** Implement XalDocumentListener.  Does nothing. */
    public void documentHasClosed( final XalDocument document ) {
    }
    
    
    //----------- Methods subclasses might override ----------------------------
    
    /**
     * Subclasses may override this method to not create the toolbar.
     */
    public boolean usesToolbar() {
        return DISPLAYS_TOOLBAR;
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
        return JOptionPane.showConfirmDialog( this, message, title, JOptionPane.YES_NO_OPTION );		
	}
	
    
    /**
     * Display a warning dialog box and provide an audible alert.
     * @param aTitle Title of the warning dialog box.
     * @param message The warning message to appear in the warning dialog box.
     */
    public void displayWarning( final String aTitle, final String message ) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog( this, message, aTitle, JOptionPane.WARNING_MESSAGE );
    }

    
    /**
     * Display a warning dialog box showing information about an exception that 
     * has been thrown and provide an audible alert.
     * @param exception The exception whose description is being displayed.
     */
    public void displayWarning( final Exception exception ) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog( this, exception.getMessage(), exception.getClass().getName(), JOptionPane.WARNING_MESSAGE );
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
        JOptionPane.showMessageDialog( this, message, aTitle, JOptionPane.WARNING_MESSAGE );
    }

    
     
    
    /**
     * Display an error dialog box and provide an audible alert.
     * @param aTitle Title of the warning dialog box.
     * @param message The warning message to appear in the warning dialog box.
     */
    public void displayError( final String aTitle, final String message ) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog( this, message, aTitle, JOptionPane.ERROR_MESSAGE );
    }
    
    
    /**
     * Display an error dialog box with information about the exception and 
     * provide an audible alert.
     * @param exception The exception about which the warning dialog is displayed.
     */
    public void displayError( final Exception exception ) {
        Toolkit.getDefaultToolkit().beep();
        String message = "Exception: " + exception.getClass().getName() + "\n" + exception.getMessage();
        JOptionPane.showMessageDialog( this, message, exception.getClass().getName(), JOptionPane.ERROR_MESSAGE );
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
        JOptionPane.showMessageDialog( this, message, aTitle, JOptionPane.ERROR_MESSAGE );
    }
}