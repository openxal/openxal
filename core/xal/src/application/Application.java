/*
 * Application.java
 *
 * Created on March 17, 2003, 3:48 PM
 */

package xal.application;

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
import xal.tools.services.*;


/**
 * The Application class handles defines the core of an application.  It is often 
 * the first handler of application wide events and typically forwards those events
 * to the custom application adaptor for further processing.  Every application has 
 * exactly one instance of this class.  The static method <code>getApp()</code> 
 * provides access to that instance.  Every application has one custom 
 * application adaptor.  The adaptor acts as a delegate for handling events 
 * specific to the custom application.  The Application, however, handles events 
 * common to all multi-document applications.
 *
 * @author  t6p
 */
abstract public class Application {
	// public static constants for confirmation dialogs
	final static public int YES_OPTION = JOptionPane.YES_OPTION;
	final static public int NO_OPTION = JOptionPane.NO_OPTION;
	
	// private constants
	final private double _launchTime;
	
    // static variables
    static private Application _application;
    
    // instance variables
    protected AbstractApplicationAdaptor _applicationAdaptor;  // custom application adaptor
    protected List _openDocuments;         // list of open documents
	protected Commander _commander;
	
    private JFileChooser _openFileChooser;   // file chooser for open 
    private JFileChooser _saveFileChooser;   // file chooser for save
	/** cache and retrieve recently accessed files */
	private RecentFileTracker _recentFileTracker;
	
	/** keep track of the application's default documents folder */
	private DefaultFolderAccessory _defaultFolderAccessory;
    
    // messaging instance variables
    private MessageCenter _messageCenter;        // local message center
    protected ApplicationListener _noticeProxy;    // proxy for broadcasting ApplicationListener events
    
	
	/** static initializer */
	static {
		LoggerBuffer.setupRootLogger();
		
		final String osName = System.getProperty( "os.name" ).toLowerCase();
		if ( osName.startsWith( "mac os x" ) ) {
			MacAdaptor.initialize();
		}
		
		loadUserProperties();
		setupDoubleBufferingMode();
	}
	
    
    /** 
	 * Application constructor. 
	 * @param adaptor The application adaptor used for customization.
	 */
    protected Application( final AbstractApplicationAdaptor adaptor ) {
		this( adaptor, new URL[]{} );
    }
    
    
    /** 
	 * Application constructor. 
	 * @param adaptor The application adaptor used for customization.
	 * @param urls An array of document URLs to open upon startup. 
	 */
    protected Application( final AbstractApplicationAdaptor adaptor, final URL[] urls ) {
		_launchTime = ( (double)new Date().getTime() ) / 1000;
        _applicationAdaptor = adaptor;
        _openDocuments = new LinkedList();
        
        // assign the global application instance before the setup since it is referenced there (among other places).
        Application._application = this;
		
        setup( urls );
    }
	
	
	/** Load the user's custom properties and set them as the defaults, but do not override existing properties. */
	static private void loadUserProperties() {
		final Preferences prefs = Preferences.userNodeForPackage( Application.class );
		final String propertiesPath = prefs.get( "UserPropertiesFile", "" );
		
		if ( propertiesPath == null || propertiesPath == "" )  return;
				
		try {
			final FileInputStream propertiesStream = new FileInputStream( propertiesPath );
			final Properties defaultProperties = System.getProperties();
			// must create properties from the default properties to keep Java Web Start happy
			final Properties userProperties = new Properties( defaultProperties );
			userProperties.clear();
			
			userProperties.load( propertiesStream );
			propertiesStream.close();
						
			// don't override existing system properties since they may have been passed at the command line
			final Enumeration propertyEnum = userProperties.propertyNames();
			while ( propertyEnum.hasMoreElements() ) {
				final String name = (String)propertyEnum.nextElement();
				if ( System.getProperty( name ) == null ) {
					System.setProperty( name, userProperties.getProperty( name ) );
				}
			}
			System.setProperties( userProperties );
			Logger.getLogger("global").log( Level.INFO, "Applied user properties from file: " + propertiesPath );
		}
		catch( FileNotFoundException exception ) {
			exception.printStackTrace();
			Logger.getLogger("global").log( Level.WARNING, "Failed to load user properties from file: " + propertiesPath, exception );
		}
		catch( IOException exception ) {
			exception.printStackTrace();
			Logger.getLogger("global").log( Level.WARNING, "Failed to load user properties from file: " + propertiesPath, exception );
		}
		catch( SecurityException exception ) {
			exception.printStackTrace();
			Logger.getLogger("global").log( Level.WARNING, "Failed to load user properties from file: " + propertiesPath, exception );
		}
		catch( Exception exception ) {
			exception.printStackTrace();
			Logger.getLogger("global").log( Level.WARNING, "Failed to load user properties from file: " + propertiesPath, exception );
		}
	}
	
	
	/**
	 * Check to see if the user has indicated that double buffering should be disabled
	 * by having set the "DisableDoubleBuffering" property to true.  This may be useful for
	 * remote X display.  If the the property is true then disable double buffering.
	 */
	static private void setupDoubleBufferingMode() {
		final boolean disableDoubleBuffering = Boolean.getBoolean( "DisableDoubleBuffering" );
		if ( disableDoubleBuffering ) {
			javax.swing.RepaintManager.currentManager( null ).setDoubleBufferingEnabled( false );
			Logger.getLogger("global").log( Level.CONFIG, "Double buffering disabled..." );
			System.out.println( "Double buffering disabled..." );
		}
	}
	
	
	/**
	 * Get the launch time which is the time at which the Application instance was instantiated.
	 * @return The launch time in seconds since the epoch (midnight GMT, January 1, 1970)
	 */
	double getLaunchTime() {
		return _launchTime;
	}
    
    
    /**
     * Get the application commander that manages commands for the entire application.
     * @return the application commander
     */
    public Commander getCommander() {
        return _commander;
    }
    
    
    // --------- Application Initializers --------------------------------------
    
    /** 
	 * Initialize the Application and open the documents specified by the URL array.
	 * If the URL array is empty, then create one empty document.
	 * 
	 * @param urls An array of document URLs to open.
	 */
    abstract protected void setup( final URL[] urls );
    
    
    /**
     * Make an application commander
     * @return the commander that loads default and custom actions.
     */
    protected Commander makeCommander() {
        return new Commander( this );
    }
	
	
	/** Register the application status service so clients on the network can query the status of this application instance. */
	final protected void registerApplicationStatusService() {
		// check to see if the startup flag has disabled application services
		Boolean shouldRegister = Boolean.valueOf( System.getProperty("registerApplicationService", "true") );
		
		if ( shouldRegister.booleanValue() ) {
			try {
				ServiceDirectory.defaultDirectory().registerService( ApplicationStatus.class, _applicationAdaptor.applicationName(), new ApplicationStatusService() );
				System.out.println( "Registered application services..." );
				Logger.getLogger( "xal.application" ).log( Level.INFO, "Registered application services..." );
			}
			catch(Exception exception) {
				exception.printStackTrace();
				System.err.println("Service registration failed due to " + exception);
				Logger.getLogger( "xal.application" ).log( Level.SEVERE, "Service registration failed...", exception );
			}
		}
		else {
			Logger.getLogger("global").log( Level.CONFIG, "Application services disabled." );
			System.out.println( "Application services not registerd because of startup flag..." );
		}
	}
    
    
    /** Setup the console to capture standard output and standard error */
    protected void setupConsole() {
        if ( _applicationAdaptor.usesConsole() ) {
            Console.captureOutput();
            Console.captureErr();
        }
    }
	
	
	/**
	 * Get the file chooser with which the user interacts when saving a document.
	 * @return The file chooser with which the user interacts when saving a document.
	 */
	public JFileChooser getSaveFileChooser() {
		return _saveFileChooser;
	}
	
	
	/**
	 * Set the file chooser with which the user will interact when saving a document.
	 * @param fileChooser The file chooser with which the user will interact when saving a document.
	 */
	public void setSaveFileChooser( final JFileChooser fileChooser ) {
		_saveFileChooser = fileChooser;
		_defaultFolderAccessory.applyTo( _saveFileChooser );
	}
	
	
	/**
	 * Get the file chooser with which the user interacts when opening a document.
	 * @return The file chooser with which the user interacts when opening a document.
	 */
	public JFileChooser getOpenFileChooser() {
		return _openFileChooser;
	}
	
	
	/**
	 * Set the file chooser with which the user will interact when opening a document.
	 * @param fileChooser The file chooser with which the user will interact when opening a document.
	 */
	public void setOpenFileChooser( final JFileChooser fileChooser ) {
		_openFileChooser = fileChooser;
		_defaultFolderAccessory.applyTo( _openFileChooser );
	}
    
    
    /** Create a file chooser for opening and saving documents. */
    protected void makeFileChoosers() {
		_recentFileTracker = new RecentFileTracker( getAdaptor().getClass(), "recent_files" );
		_defaultFolderAccessory = new DefaultFolderAccessory( XalDocument.class, null, getAdaptor().applicationName() );
		
		setOpenFileChooser( new JFileChooser() );
		FileFilterFactory.applyFileFilters( _openFileChooser, _applicationAdaptor.readableDocumentTypes() );
        _openFileChooser.setMultiSelectionEnabled( true );
		
		setSaveFileChooser( new JFileChooser() );
		FileFilterFactory.applyFileFilters( _saveFileChooser, _applicationAdaptor.writableDocumentTypes() );
        _saveFileChooser.setMultiSelectionEnabled( false );
    }
    
    
    // --------- Event registration --------------------------------------------
    
    /** 
     * Register the instance as a provider for ApplictionListener events.
     * Register the application adaptor as an ApplicationListener.
     */
    protected void registerEvents() {
        _messageCenter = new MessageCenter();
        _noticeProxy = (ApplicationListener)_messageCenter.registerSource( this, ApplicationListener.class );
        
        addApplicationListener( _applicationAdaptor );
    }
    
    
    /**
     * Add the listener as a listener of Application events.
     * @param listener Object to register as a listener of application events.
     */
    public void addApplicationListener( final ApplicationListener listener ) {
        _messageCenter.registerTarget( listener, this, ApplicationListener.class );
    }
    
    
    /**
     * Remove the listener from listening to Application events.
     * @param listener Object to un-register as a listener of application events.
     */
    public void removeApplicationListener( final ApplicationListener listener ) {
        _messageCenter.removeTarget( listener, this, ApplicationListener.class );
    }
    
    
    
    // --------- accessors -----------------------------------------------------
    
    /**
     * Get the list of all open documents.
     * @return An immutable list of the open documents.
     */
    public List getDocuments() {
        return Collections.unmodifiableList( _openDocuments );
    }
    
    
    /**
     * Get the custom application adaptor.
     * @return The custom application adaptor.
     * @see #getAdaptor
     */
    public AbstractApplicationAdaptor getApplicationAdaptor() {
        return _applicationAdaptor;
    }
    
    
    
    // --------- File menu actions ---------------------------------------------
    
    /** Create and open a new empty document. */
    abstract protected void newDocument();
	
	
    /** 
	 * Create and open a new empty document of the specified type. 
	 * @param type the type of document to create.
	 */
    abstract protected void newDocument( final String type );
	
	
	/**
	 * Show the save file chooser.
	 * @return the user's option (e.g. cancel, approve, error) for the file chooser.
	 */
	protected int showOpenFileChooser() {
		final int status = _openFileChooser.showOpenDialog( getActiveWindow() );
		
		// reconcile current director for open and save file choosers
		_saveFileChooser.setCurrentDirectory( _openFileChooser.getCurrentDirectory() );
		
		return status;
	}
    
    
    /**
     * Handle the "Open" action by opening a new document.
     */
    protected void openDocument() {
        final int status = showOpenFileChooser();
        
        switch( status ) {
            case JFileChooser.CANCEL_OPTION:
                break;
            case JFileChooser.APPROVE_OPTION:
                File[] fileSelections = _openFileChooser.getSelectedFiles();
                openFiles( fileSelections );
                break;
            case JFileChooser.ERROR_OPTION:
                break;
        }
    }
    
    
    /**
     * Support method for opening a new document with the URL specification
     * @param urlSpec The URL specification of the file to open.
     */
    protected void openURL( final String urlSpec ) {
        try {
            URL url = new URL( urlSpec );
            openDocument( url );
        }
        catch(MalformedURLException exception) {
			Logger.getLogger("global").log( Level.WARNING, "Error opening URL: " + urlSpec, exception );
            System.err.println( exception );
            displayError( exception );
        }
    }
    
    
    /**
     * Support method for opening a new document given a file.
     * @param file The file to open.
     * @see #openFiles
     */
    protected void openFile( final File file ) {
        try {
            URL url = file.toURI().toURL();
            openDocument( url );
        }
        catch( MalformedURLException exception ) {
			Logger.getLogger("global").log( Level.WARNING, "Error opening file: " + file, exception );
            System.err.println( exception );
            displayError( exception );
        }
    }
    
    
    /**
     * Support method for opening an array of files.
     * @param files The files to open.
     * @see #openDocument()
     */
    protected void openFiles( final File[] files ) {
        for ( int index = 0 ; index < files.length ; index++ ) {
            File file = files[index];
            openFile( file );
        }
    }
    
    
    /**
     * Support method for opening a document with the specified URL.
     * @param url The URL of the file to open.
     * @see #openURL
     * @see #openFile
     */
    public void openDocument( final URL url ) {
        try {
            XalAbstractDocument document = _applicationAdaptor.generateDocument( url );
			produceDocument( document );
            registerRecentURL( url );
        }
        catch(Exception exception) { 
			Logger.getLogger("global").log( Level.WARNING, "Error opening document: " + url, exception );
            System.err.println( "Open failed due to an internal exception: " + exception );
			exception.printStackTrace();
            displayError( "Open Failed!", "Open failed due to an internal exception!", exception );
        }
    }
    
    
    /**
     * Handle the "Close" action by closing the specified document.
     * @param document The document to close.
     */
    protected void closeDocument( final XalAbstractDocument document ) {
        document.closeDocument();
    }
    
    
    /** Handle the "Close All" action by closing all open documents and opening a new empty document. */
    protected void closeAllDocuments() {
        LinkedList docList = new LinkedList( _openDocuments );
        ListIterator iterator = docList.listIterator();
	
		while( iterator.hasNext() ) {
			XalAbstractDocument document = (XalAbstractDocument)iterator.next();
			closeDocument( document );
		}
    }
	
	
	/**
	 * Show the save file chooser.
	 * @return the user's option (e.g. cancel, approve, error) for the file chooser.
	 */
	protected int showSaveFileChooser( final XalAbstractDocument document ) {
        final int status = _saveFileChooser.showSaveDialog( (java.awt.Container)document.getDocumentView() );
		
		// reconcile current directory between open and save file choosers
		_openFileChooser.setCurrentDirectory( _saveFileChooser.getCurrentDirectory() );
		
		return status;
	}
    
    
    /**
     * Handle the "Save" action by saving the specified document.  If the 
     * document has a an existing file source, the document is saved to that 
     * source.  Otherwise, the user is shown a dialog box to select a file 
     * location to which the document will be saved.
     * @param document The document to save.
     */
    protected void saveDocument( final XalAbstractDocument document ) {
        if ( !document.hasChanges() ) {
            document.displayWarning( "Nothing Saved!", "This document reports no changes to save." );
            return;
        }
        
        if ( document.getSource() != null ) {
            document.saveDocument();
        }
        else {
            saveAsDocument( document );
        }
    }
    
    
    /**
     * Handle the "Save As" action by saving the specified document to the 
     * location chosen by the user.  Displays a dialog box to allow the user
     * to select a location.
     * @param document The document to save.
     */
    protected void saveAsDocument( final XalAbstractDocument document ) {
		String defaultName = document.getFileNameForSaving();
		File defaultFolder = _saveFileChooser.getCurrentDirectory();
		File defaultFile = new File( defaultFolder, defaultName );
		
		_saveFileChooser.setSelectedFile( defaultFile );
        final int status = showSaveFileChooser( document );
        
        switch( status ) {
            case JFileChooser.CANCEL_OPTION:
                break;
            case JFileChooser.APPROVE_OPTION:
                File fileSelection = _saveFileChooser.getSelectedFile();
				if ( fileSelection.exists() ) {
					int confirm = document.displayConfirmDialog( "Overwrite Confirmation", "The selected file:  " + fileSelection + " already exists! \n Overwrite selection?" );
					if ( confirm == NO_OPTION ) {
						saveAsDocument( document );	// offer a new selection
						return;
					}
				}
                saveDocumentToFile( document, fileSelection );
                break;
            case JFileChooser.ERROR_OPTION:
                break;
        }
    }
    
    
    /**
     * Handle the "Save All" action by saving all open documents.
     */
    protected void saveAllDocuments() {
        Iterator iterator = _openDocuments.iterator();
        
        while ( iterator.hasNext() ) {
            XalAbstractDocument document = (XalAbstractDocument)iterator.next();
            saveDocument( document );
        }
    }
    
    
    /**
     * Support method for saving a document to a file.
     * @param document The document to save.
     * @param file The file to which the document will be saved.
     */
    protected void saveDocumentToFile( final XalAbstractDocument document, final File file ) {
        try {
            URL url = file.toURI().toURL();
            document.saveDocumentAs( url );
            document.setSource( url );
            registerRecentURL( url );
        }
        catch( MalformedURLException exception ) {
			Logger.getLogger("global").log( Level.WARNING, "Failed to save document to file: " + file, exception );
            System.err.println( exception );
			document.displayError( "Save Error" , "Error attempting to save the document." , exception );
        }
    }
    
    
    /**
     * Handle the "Revert To Saved" action by reverting the specified document to that of its source file.
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
        
		if ( document.closeDocument() ) {
			openDocument( source );
		}
    }
    
    
    /** Handle the "Quit" action by quitting the application. */
    public void quit() {
		boolean warnUnsavedChanges = false;
		Iterator documentIter = getDocuments().iterator();
		while ( documentIter.hasNext() ) {
			XalAbstractDocument document = (XalAbstractDocument)documentIter.next();
			warnUnsavedChanges |= ( document.warnUserOfUnsavedChangesWhenClosing() && document.hasChanges() );
		}
		
		if ( warnUnsavedChanges ) {
			try {
				int status = JOptionPane.showConfirmDialog( getActiveWindow(), "Some documents have unsaved changes.  Continue Quitting?", "Unsaved Changes", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
				if ( status == JOptionPane.NO_OPTION ) {
					return;
				}
			}
			catch( java.awt.HeadlessException exception ) {
				Logger.getLogger("global").log( Level.SEVERE, "Exception while quitting the application.", exception );
				System.err.println( exception );
				exception.printStackTrace();
			}
		}
        
        _noticeProxy.applicationWillQuit();
        System.exit(0);
    }
    
    
    
    // --------- Window menu actions -------------------------------------------

    
    /**
     * Handle the "Cascade Windows" action by cascading all document windows about the target document.
     * @param targetDocument The document about whose window all document windows should cascade
     */
    protected void cascadeWindowsAbout( final XalAbstractDocument targetDocument ) {
        final Point windowOrigin = targetDocument.getDocumentView().getLocation();
        final Iterator documentIter = getDocuments().iterator();
        
        while ( documentIter.hasNext() ) {
            final XalAbstractDocument document = (XalAbstractDocument)documentIter.next();
            final XalDocumentView window = document.getDocumentView();
			try {	// iconified windows will throw exceptions
				window.setVisible( true );
				final java.awt.Container contentPane = window.getContentPane();
				final int offset = window.isVisible() ? (int)( 1.5 * ( contentPane.getLocationOnScreen().y - window.getLocationOnScreen().y ) ) : 50;
				
				window.setVisible( false );   // must do this so we can force the window to move ???
				window.setLocation( windowOrigin );
				window.setVisible( true );    // restore the window to visible
				document.showDocument();
				windowOrigin.translate( offset, offset );    // prepare for next window
			}
			catch( Exception exception ) {}
        }
    }
    
    
    /**
     * Handle the "Show All" action by showing all main windows corresponding 
     * to the open documents.  The windows are brought to the front and 
     * un-collapsed as necessary.
     */
    protected void showAllWindows() {
        Iterator iterator = _openDocuments.iterator();
        
        while ( iterator.hasNext() ) {
            XalAbstractDocument document = (XalAbstractDocument)iterator.next();
            document.showDocument();
        }
    }
    
    
    /**
     * Handle the "Hide All" action by hiding all main windows corresponding 
     * to the open documents.
     */
    protected void hideAllWindows() {
        Console.hide();     // hide the console
        
        Iterator iterator = _openDocuments.iterator();
        while ( iterator.hasNext() ) {
            XalAbstractDocument document = (XalAbstractDocument)iterator.next();
            document.hideDocument();
        }
    }
	
	
	/** show the about box */
	static void showAboutBox() {
		AboutBox.showNear( getActiveWindow() );
	}
	
    
    // --------- Manage History ------------------------------------------------
	
    /**
     * Register the URL of a document that has recently been opened or saved.  These URLs 
     * appear in the "Open Recent" submenu of the File menu.  These items get saved in 
     * the user's preferences for this application as identified by the custom 
     * application adaptor class.
     * @param url The URL to register.
     */
    void registerRecentURL( final URL url ) {
		// if the url is from inside a jar file then don't cache it
		if ( url.getProtocol().equalsIgnoreCase( "jar" ) )  return;
		
		_recentFileTracker.cacheURL( url );
    }
    
    
    /**
     * Get the array of URLs corresponding to recently opened or saved documents.
     * Fetch the recent items from the list saved in the user's preferences for 
     * this application.
     * @return The array of recent URLs.
     */
    String[] getRecentURLSpecs() {
		return _recentFileTracker.getRecentURLSpecs();
    }
    
    
    /**
     * Get the most recently visited folder saved in the user's preferences for 
     * this application.
     * @return The most recently visited folder.
     */
    private File getRecentFolder() {
		return _recentFileTracker.getRecentFolder();
    }
    
    
    /**
     * Handle the "Clear" event associated with the list of recent items.  Clear 
     * the list of URLs corresponding to the recently opened or saved documents.
     * Clear the list in the user's preferences for this application.
     */
    void clearRecentItems() {
		_recentFileTracker.clearCache();
    }
    
	
	/**
	 * Get the default document folder.
	 * @return the default folder for documents or null if none has been set.
	 */
	public File getDefaultDocumentFolder() {
		return _defaultFolderAccessory.getDefaultFolder();
	}
    
	
	/**
	 * Get the default document folder as a URL.
	 * @return the default folder for documents as a URL or null if none has been set.
	 */
	public URL getDefaultDocumentFolderURL() {
		return _defaultFolderAccessory.getDefaultFolderURL();
	}
    
    
    // --------- Application Management ----------------------------------------
    
    
    /**
     * Handle the launching of the application by creating the application instance
     * and performing application initialization.
     * @param adaptor The custom application adaptor.
     */
    static public void launch( final AbstractApplicationAdaptor adaptor ) {
        try {
            if ( adaptor.getDocURLs().length > 0 ) {
                launch( adaptor, adaptor.getDocURLs() );				
			}
        } catch ( NullPointerException exception ) {
            launch( adaptor, new URL[]{} );
        }
    }
    
    
    /**
     * Handle the launching of the application by creating the application instance
     * and performing application initialization.
     * @param adaptor The custom application adaptor.
	 * @param urls The URLs of documents to open upon launching the application
     */
    static public void launch( final AbstractApplicationAdaptor adaptor, final URL[] urls ) {
		adaptor.launchApplication( urls );
    }
    
    
    /**
     * Convenience method for getting the custom application adaptor.  There is 
     * only one such adaptor for the entire application.
     * @return The custom application adaptor.
     * @see #getApplicationAdaptor
     */
    static public AbstractApplicationAdaptor getAdaptor() {
        return _application.getApplicationAdaptor();
    }
    
    
    /**
     * Get the application instance.  There is only one application instance 
     * per application.
     * @return The application instance.
     */
    static public Application getApp() {
        return _application;
    }
    
    
    /**
     * Get the active window which is in focus for this application.  It is typically a good  
     * window relative to which you can place application warning dialog boxes.
     * @return The active window
     */
    static public Window getActiveWindow() {
        return java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
    }
    
    
    /**
     * Add a new document to this application and show it
	 * @param document the document to produce
     */
    public void produceDocument( final XalAbstractDocument document ) {
		produceDocument( document, true );
    }
    
    
    /**
     * Add a new document to this application and if makeVisible is true, show it
	 * @param document the document to produce
	 * @param makeVisible make the document visible
     */
    abstract public void produceDocument( final XalAbstractDocument document, final boolean makeVisible );
    
    
    //------------------- Convenience methods -----------------------------------
	
	
	/**
	 * Display a confirmation dialog with a title and message
	 * @param title The title of the dialog
	 * @param message The message to display
	 * @return YES_OPTION or NO_OPTION 
	 */
	static public int displayConfirmDialog( final String title, final String message ) {
        Toolkit.getDefaultToolkit().beep();
        return JOptionPane.showConfirmDialog( getActiveWindow(), message, title, JOptionPane.YES_NO_OPTION );		
	}
	
    
    /**
     * Display a warning dialog box with information about the exception.
     * @param exception The exception about which the warning dialog is displayed.
     */
    static public void displayWarning( final Exception exception ) {
        Toolkit.getDefaultToolkit().beep();
        String message = "Exception: " + exception.getClass().getName() + "\n" + exception.getMessage();
        displayWarning( exception.getClass().getName(), message );
    }

    
    /**
     * Display a warning dialog box.
     * @param title Title of the warning dialog box.
     * @param message The warning message to appear in the warning dialog box.
     */
    static public void displayWarning( final String title, final String message ) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog( getActiveWindow(), message, title, JOptionPane.WARNING_MESSAGE );
    }
    
    
    /**
     * Display a warning dialog box with information about the exception.  This method allows
     * clarification about the consequences of the exception (e.g. "Save Failed:").
     * @param title Title of the warning dialog box.
     * @param prefix Text that should appear in the dialog box before the exception messasge.
     * @param exception The exception about which the warning dialog is displayed.
     */
    static public void displayWarning( final String title, final String prefix, final Exception exception ) {
        Toolkit.getDefaultToolkit().beep();
        String message = prefix + "\n" + "Exception: " + exception.getClass().getName() + "\n" + exception.getMessage();
        JOptionPane.showMessageDialog( getActiveWindow(), message, title, JOptionPane.WARNING_MESSAGE );
    }

    
    /**
     * Display an error dialog box.
     * @param title Title of the warning dialog box.
     * @param message The warning message to appear in the warning dialog box.
     */
    static public void displayError( final String title, final String message ) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog( getActiveWindow(), message, title, JOptionPane.ERROR_MESSAGE );
    }
    
    
    /**
     * Display an error dialog box with information about the exception.
     * @param exception The exception about which the warning dialog is displayed.
     */
    static public void displayError( final Exception exception ) {
        Toolkit.getDefaultToolkit().beep();
        String message = "Exception: " + exception.getClass().getName() + "\n" + exception.getMessage();
        displayError( exception.getClass().getName(), message );
    }    
    
    
    /**
     * Display an error dialog box with information about the exception.  This method allows
     * clarification about the consequences of the exception (e.g. "Save Failed:").
     * @param title Title of the warning dialog box.
     * @param prefix Text that should appear in the dialog box before the exception messasge.
     * @param exception The exception about which the warning dialog is displayed.
     */
    static public void displayError( final String title, final String prefix, final Exception exception ) {
        Toolkit.getDefaultToolkit().beep();
        String message = prefix + "\n" + "Exception: " + exception.getClass().getName() + "\n" + exception.getMessage();
        JOptionPane.showMessageDialog( getActiveWindow(), message, title, JOptionPane.ERROR_MESSAGE );
    }
    
    
    /**
     * Display an error dialog box with information about the exception.  This method allows
     * clarification about the consequences of the exception (e.g. "Save Failed:").
     * @param title Title of the warning dialog box.
     * @param prefix Text that should appear in the dialog box before the exception messasge.
     * @param exception The exception about which the warning dialog is displayed.
     */
    static public void displayApplicationError( final String title, final String prefix, final Exception exception ) {
        Toolkit.getDefaultToolkit().beep();
        String message = prefix + "\n" + "Exception: " + exception.getClass().getName() + "\n" + exception.getMessage();
        JOptionPane.showMessageDialog( getActiveWindow(), message, title, JOptionPane.ERROR_MESSAGE );
    }
}
