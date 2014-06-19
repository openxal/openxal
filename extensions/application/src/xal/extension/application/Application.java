/*
 * Application.java
 *
 * Created on March 17, 2003, 3:48 PM
 */

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
import java.nio.channels.FileChannel;
import java.util.prefs.Preferences;

import xal.extension.application.platform.*;
import xal.tools.StringJoiner;
import xal.tools.apputils.files.*;
import xal.tools.messaging.MessageCenter;
import xal.extension.service.*;
import xal.tools.URLReference;
import xal.tools.apputils.ApplicationSupport;


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
	final private Date LAUNCH_TIME;
	
    // static variables
    static private Application _application;
    
    // instance variables
    protected AbstractApplicationAdaptor _applicationAdaptor;  // custom application adaptor
    protected List<XalAbstractDocument> _openDocuments;         // list of open documents
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
    
    /** location of the next document to open */
    private Point _nextDocumentOpenLocation;
    
    /** template folder */
    private File _templateFolder;
    
    /** default folder */
    private File _defaultDocumentFolder;
    
	
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
        _nextDocumentOpenLocation = new Point( 0, 0 );
        
		LAUNCH_TIME = new Date();
        
        _applicationAdaptor = adaptor;
        _openDocuments = new LinkedList<XalAbstractDocument>();
        
        // assign the global application instance before the setup since it is referenced there (among other places).
        Application._application = this;
		
        setup( urls );
    }
	
	
	/** Load the user's custom properties and set them as the defaults, but do not override existing properties. */
	static private void loadUserProperties() {
		final Preferences prefs = xal.tools.apputils.Preferences.nodeForPackage( Application.class );
		final String propertiesPath = prefs.get( "UserPropertiesFile", "" );
		
		if ( propertiesPath == null || propertiesPath.isEmpty() )  return;
				
		try {
			final FileInputStream propertiesStream = new FileInputStream( propertiesPath );
			final Properties defaultProperties = System.getProperties();
			// must create properties from the default properties to keep Java Web Start happy
			final Properties userProperties = new Properties( defaultProperties );
			userProperties.clear();
			
			userProperties.load( propertiesStream );
			propertiesStream.close();
						
			// don't override existing system properties since they may have been passed at the command line
			final Set<String> propertyNames = userProperties.stringPropertyNames();
			for ( final String name : propertyNames ) {
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
	 * @return The launch time
	 */
	public Date getLaunchTime() {
		return LAUNCH_TIME;
	}
    
    
    /**
     * Get the application commander that manages commands for the entire application.
     * @return the application commander
     */
    public Commander getCommander() {
        return _commander;
    }
    
    
    /** Determine whether this application can open documents */
    protected boolean canOpenDocuments() {
        return _applicationAdaptor.canOpenDocuments();
    }
    
    
    /** Indicates whether the welcome dialog should be displayed at launch */
    protected boolean showsWelcomeDialogAtLaunch() {
        return _applicationAdaptor.showsWelcomeDialogAtLaunch();
    }
    
    
    /** show the welcome dialog which offers to open a new document, template or existing document. */
    protected void showWelcomeDialog() {
        // todo: should get the position from a Java property (e.g. passed by the launcher)
        new WelcomeController( getNextDocumentOpenLocation() );
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
				Logger.getLogger( "xal.extension.application" ).log( Level.INFO, "Registered application services..." );
			}
			catch(Exception exception) {
				exception.printStackTrace();
				System.err.println("Service registration failed due to " + exception);
				Logger.getLogger( "xal.extension.application" ).log( Level.SEVERE, "Service registration failed...", exception );
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
    
    
    /** get the location of the next document to open */
    protected Point getNextDocumentOpenLocation() {
        return _nextDocumentOpenLocation;
    }
    
    
    /** Set the next document open location */
    protected void setNextDocumentOpenLocation( final Point location ) {
        _nextDocumentOpenLocation = location;
    }
    
    
    /** Set the location of the next document to open based on the location of the active application document window */
    protected void updateNextDocumentOpenLocation() {
        final Window activeWindow = getActiveWindow();
		final XalWindow selectedWindow = activeWindow instanceof XalWindow ? (XalWindow)activeWindow : null;
		// offset this window relative to the active window if any
		if ( selectedWindow != null && selectedWindow.isVisible() ) {
			final java.awt.Container contentPane = selectedWindow.getContentPane();
			final int offset = (int) ( 1.5 * ( contentPane.getLocationOnScreen().y - selectedWindow.getLocationOnScreen().y ) );
			final Point location = new Point( selectedWindow.getLocationOnScreen() );
			location.translate( offset, offset );
            setNextDocumentOpenLocation( location );
		}
    }
    
    
    /** update the next location offset from document */
    private void updateNextDocumentOpenLocationOffsetFrom( final XalAbstractDocument document ) {
        if ( document instanceof XalDocument ) {
            final XalWindow window = ((XalDocument)document).getMainWindow();
            if ( window != null ) {
                updateNextDocumentOpenLocationOffsetFrom( window );
            }
        }
    }
    
    
    /** update the next location offset from window */
    private void updateNextDocumentOpenLocationOffsetFrom( final XalWindow window ) {
        final java.awt.Container contentPane = window.getContentPane();
        final int offset = (int) ( 1.5 * ( contentPane.getLocationOnScreen().y - window.getLocationOnScreen().y ) );
        final Point location = new Point( window.getLocationOnScreen() );
        location.translate( offset, offset );
        setNextDocumentOpenLocation( location );
    }
    
    
    // --------- Event registration --------------------------------------------
    
    /** 
     * Register the instance as a provider for ApplictionListener events.
     * Register the application adaptor as an ApplicationListener.
     */
    protected void registerEvents() {
        _messageCenter = new MessageCenter();
        _noticeProxy = _messageCenter.registerSource( this, ApplicationListener.class );
        
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
     * Get the unmodifiable list of all open documents.
     * @return An immutable list of the open documents.
     */
    public List<XalAbstractDocument> getDocuments() {
        return Collections.unmodifiableList( _openDocuments );
    }
    
    /**
     * Get a copy of the list of all open documents.
     * @return An immutable list of the open documents.
     */
    @SuppressWarnings( "unchecked" )    // suppress unchecked casting to DocumentType since there is not way around it
    public <DocumentType extends XalAbstractDocument> List<DocumentType> getDocumentsCopy() {
        final List<XalAbstractDocument> documents = getDocuments();
        final List<DocumentType> documentsCopy = new ArrayList<DocumentType>( documents.size() );
        for ( final XalAbstractDocument document : documents ) {
            documentsCopy.add( (DocumentType)document );
        }
        
        return documentsCopy;
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
    
    
    /** Create a new document based on a user selected document */
    protected void newDocumentFromTemplate() {
        updateNextDocumentOpenLocation();
        
        final File defaultFolder = getDefaultDocumentFolder();
        final File templateFolder = getTemplateFolder();
        final File chooserFolder = templateFolder != null && templateFolder.exists() ? templateFolder : defaultFolder;
        final JFileChooser templateChooser = new JFileChooser( chooserFolder );
        FileFilterFactory.applyFileFilters( templateChooser, _applicationAdaptor.readableDocumentTypes() );
        templateChooser.setMultiSelectionEnabled( true );
        templateChooser.setDialogTitle( "Open Template" );
        templateChooser.setApproveButtonText( "Open Template" );
        templateChooser.setApproveButtonToolTipText( "Open new copies of the selected templates" );
        
        openDocuments( templateChooser, true, false, false );
    }
	
    
    /** Show the file chooser */
    private int showOpenFileChooser( final JFileChooser fileChooser ) {
        return fileChooser.showOpenDialog( getActiveWindow() );
    }
    
	
	/**
	 * Show the save file chooser.
	 * @return the user's option (e.g. cancel, approve, error) for the file chooser.
	 */
	protected int showOpenFileChooser() {
		final int status = showOpenFileChooser( _openFileChooser );
		
		// reconcile current directory for open and save file choosers
		_saveFileChooser.setCurrentDirectory( _openFileChooser.getCurrentDirectory() );
		
		return status;
	}
    
    
    /**
     * Handle the "Open" action by opening a new document.
     */
    protected void openDocument() {
        updateNextDocumentOpenLocation();
        openDocuments( _openFileChooser, false, true, true );
    }
    
    
    /**
     * Handle the "Open" action by opening a new document.
     * @param fileChooser the file chooser to use for file selection
     * @param copyDocument indicates whether to copy the document (e.g. as in opening a template)
     * @param syncSaveChooser synchronize the current directory of the save file chooser with that of the specified file chooser
     * @param trackRecent indicates whether to track the document for recent activity 
     */
    private void openDocuments( final JFileChooser fileChooser, final boolean copyDocument, final boolean syncSaveChooser, final boolean trackRecent ) {
        final int status = showOpenFileChooser( fileChooser );
        
        if ( syncSaveChooser ) {
            _saveFileChooser.setCurrentDirectory( fileChooser.getCurrentDirectory() );
        }
        
        switch( status ) {
            case JFileChooser.CANCEL_OPTION:
                break;
            case JFileChooser.APPROVE_OPTION:
                File[] fileSelections = fileChooser.getSelectedFiles();
                openFiles( fileSelections, copyDocument, trackRecent );
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
        openURL( urlSpec, false, true );
    }
    
    
    /**
     * Support method for opening a new document with the URL specification
     * @param urlSpec The URL specification of the file to open.
     * @param copySource indicates whether to make a fresh copy of the source (e.g. as if opening from a template)
     * @param trackRecent indicates whether to track the document for recent activity
     */
    private void openURL( final String urlSpec, final boolean copySource, final boolean trackRecent ) {
        try {
            final URL url = new URL( urlSpec );
            openDocument( url, copySource, trackRecent );
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
        openFile( file, false, true );
    }
    
    
    /**
     * Support method for opening a new document given a file.
     * @param file The file to open.
     * @param copySource indicates whether to make a fresh copy of the source (e.g. as if opening from a template)
     * @param trackRecent indicates whether to track the document for recent activity 
     * @see #openFiles
     */
    private void openFile( final File file, final boolean copySource, final boolean trackRecent ) {
        try {
            final URL url = file.toURI().toURL();
            openDocument( url, copySource, trackRecent );
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
        openFiles( files, false, true );
    }
    
    
    /**
     * Support method for opening an array of files.
     * @param files The files to open.
     * @param copySource indicates whether to make a fresh copy of the source (e.g. as if opening from a template)
     * @param trackRecent indicates whether to track the document for recent activity 
     * @see #openDocument()
     */
    private void openFiles( final File[] files, final boolean copySource, final boolean trackRecent ) {
        for ( int index = 0 ; index < files.length ; index++ ) {
            File file = files[index];
            openFile( file, copySource, trackRecent );
        }
    }
    
    
    /**
     * Support method for opening a document with the specified URL.
     * @param url The URL of the file to open.
     * @see #openURL
     * @see #openFile
     */
    public void openDocument( final URL url ) {
        openDocument( url, false, true );
    }
    
    
    /**
     * Support method for opening a document with the specified URL.
     * @param url The URL of the file to open.
     * @param copyDocument indicates whether to make a new independent copy of the document at the specified URL (e.g. as if opening from a template)
     * @param trackRecent indicates whether to track the document for recent activity 
     * @see #openURL
     * @see #openFile
     */
    private void openDocument( final URL url, final boolean copyDocument, final boolean trackRecent ) {
        try {
            XalAbstractDocument document = _applicationAdaptor.generateDocument( url );
            if ( copyDocument )  document.setSource( null );    // mark the document as independent form the source URL (e.g. opened from template)
			produceDocument( document );
            if ( trackRecent && !URLReference.isRootedIn( getTemplateFolderURL(), url ) )  registerRecentURL( url );    // never track files under the template folder regardless of the flag
            updateNextDocumentOpenLocationOffsetFrom( document );
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
        final LinkedList<XalAbstractDocument> docList = new LinkedList<XalAbstractDocument>( _openDocuments );
	
		for( final XalAbstractDocument document : docList ) {
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
            saveDocumentVersion( document );
        }
        else {
            saveAsDocument( document );
        }        
    }
    
    
    /** present the user with a dialog box to open a version of the specified document */
    protected void openDocumentVersion( final XalAbstractDocument document ) {
        updateNextDocumentOpenLocation();

        final FileVersionInfo versionInfo = getSourceVersionInfo( document );
        if ( versionInfo != null ) {
            final File currentFolder = versionInfo.getCurrentFolder();
            final File latestFolder = currentFolder.exists() ? currentFolder : currentFolder.getParentFile();
            if ( latestFolder.exists() ) {
                // present a file chooser and open the document selected by the user
                final JFileChooser versionChooser = new JFileChooser( latestFolder );
                FileFilterFactory.applyFileFilters( versionChooser, _applicationAdaptor.readableDocumentTypes() );
                versionChooser.setMultiSelectionEnabled( true );
                openDocuments( versionChooser, false, false, false );
            }
            else {
                document.displayWarning( "Can't open version", "No versions exist for this document." );
            }
        }
        else {
            document.displayWarning( "Can't open version", "Nothing to open." );
        }
    }
    
    
    /** get the source file version info for the document */
    private FileVersionInfo getSourceVersionInfo( final XalAbstractDocument document ) {
        try {
            final Date now = new Date();
            final URL sourceURL = document.getSource();
            if ( sourceURL != null ) {
                final File sourceFile = new File( sourceURL.toURI() );
                final File defaultFolder = getDefaultDocumentFolder();
                if ( defaultFolder != null ) {
                    return new FileVersionInfo( sourceFile, defaultFolder, now );
                }
                else {
                    System.err.println( "Can't get source version info because no default documents directory has been specified!" );
                    return null;
                }
            }
            else {
                return null;
            }
        }
        catch( Exception exception ) {
            exception.printStackTrace();
            throw new RuntimeException( "Exception generating source version info for document.", exception );
        }
    }
    
    
    /** save a copy of the document to the versions folder: documents/appname/.versions/basename/year/basename_@timestamp.extension */
    private void saveDocumentVersion( final XalAbstractDocument document ) {
        try {
            final FileVersionInfo versionInfo = getSourceVersionInfo( document );
            if ( versionInfo != null ) {
                final File yearFolder = versionInfo.getCurrentFolder();
                if ( yearFolder != null && ( yearFolder.exists() || yearFolder.mkdirs() ) ) {
                    final String baseName = versionInfo.getBaseName();
                    final String targetName = baseName.replaceFirst( "\\.", new java.text.SimpleDateFormat( "_@yyyyMMdd'T'HHmmss@" ).format( versionInfo.getTimestamp() ) + "." );;
                    final File targetFile = new File( yearFolder, targetName );
                    targetFile.createNewFile();
                    copyFile( versionInfo.getSourceFile(), targetFile );
                    targetFile.setReadOnly();
                }
            }
        }
        catch( Exception exception ) {
            System.err.println( "Exception saving document version..." );
            exception.printStackTrace();
        }
    }
    
    
    /**
     * Handle the "Save As" action by saving the specified document to the location chosen by the user.  Displays a dialog box to allow the user to select a location.
     * @param document The document to save.
     */
    protected void saveAsDocument( final XalAbstractDocument document ) {
		final String defaultName = document.getFileNameForSaving();
		final File defaultFolder = _saveFileChooser.getCurrentDirectory();
		final File defaultFile = new File( defaultFolder, defaultName );
		
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
        for ( final XalAbstractDocument document : _openDocuments ) {
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
            final URL url = file.toURI().toURL();
            document.saveDocumentAs( url );
            document.setSource( url );
            if ( !URLReference.isRootedIn( getTemplateFolderURL(), url ) ) {
                registerRecentURL( url );
            }
            saveDocumentVersion( document );
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
		final List<XalAbstractDocument> documents = getDocuments();
		for ( final XalAbstractDocument document : documents ) {
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
		final List<XalAbstractDocument> documents = getDocuments();
		for ( final XalAbstractDocument document : documents ) {
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
        for ( final XalAbstractDocument document : _openDocuments ) {
            document.showDocument();
        }
    }
    
    
    /**
     * Handle the "Hide All" action by hiding all main windows corresponding 
     * to the open documents.
     */
    protected void hideAllWindows() {
        Console.hide();     // hide the console
        
        for ( final XalAbstractDocument document : _openDocuments ) {
            document.hideDocument();
        }
    }
	
	
	/** show the about box */
	static public void showAboutBox() {
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
    
    
    /** Get this application's template folder creating it if possible and necessary */
    private File getTemplateFolder() {
        if ( _templateFolder == null ) {
            final File defaultFolder = getDefaultDocumentFolder();
            final File templateFolder = defaultFolder != null && defaultFolder.exists() ? new File( defaultFolder, "Templates" ) : null;
            
            // attempt to make the template folder if it doesn't already exist but the default folder does
            if ( templateFolder != null && !templateFolder.exists() ) {
                if ( defaultFolder.canWrite() ) {
                    templateFolder.mkdir();
                }
            }
            
            _templateFolder = templateFolder;
        }
        
        return _templateFolder;
    }
    
    
    /** Get the URL to this application's template folder creating it if possible and necessary */
    private URL getTemplateFolderURL() {
        final File templateFolder = getTemplateFolder();
        try {
            return templateFolder != null ? templateFolder.toURI().toURL() : null;
        }
        catch( Exception exception ) {
            exception.printStackTrace();
            throw new RuntimeException( "Exception getting the template URL", exception );
        }
    }
    
    
	/**
	 * Get the default document folder.
	 * @return the default folder for documents or null if none has been set.
	 */
	public File getDefaultDocumentFolder() {
        if ( _defaultDocumentFolder == null ) {
            _defaultDocumentFolder = _defaultFolderAccessory.getDefaultFolder();
        }
        
        return _defaultDocumentFolder;
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
			// get the document URLs passed at the command line
			final URL[] docURLs = AbstractApplicationAdaptor.getDocURLs();
            if ( docURLs.length > 0 ) {
                launch( adaptor, docURLs );
			}
        } 
        catch ( NullPointerException exception ) {
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
		return ApplicationSupport.getActiveWindow();
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
    
    
    /** Copy the source file to the target file */
    static private void copyFile( final File sourceFile, final File targetFile ) {
        try {
            final FileChannel sourceChannel = new FileInputStream( sourceFile ).getChannel();
            final FileChannel targetChannel = new FileOutputStream( targetFile ).getChannel();
            
            sourceChannel.transferTo( 0, sourceChannel.size(), targetChannel );
            
            sourceChannel.close();
            targetChannel.close();
        }
        catch( Exception exception ) {
            exception.printStackTrace();
            throw new RuntimeException( "Exception attempting to copy the source file to the target file.", exception );
        }
    }
	
	
	/**
	 * Display a confirmation dialog with a title and message
	 * @param title The title of the dialog
	 * @param message The message to display
	 * @return YES_OPTION or NO_OPTION 
	 */
	static public int displayConfirmDialog( final String title, final String message ) {
		return ApplicationSupport.displayConfirmDialog( title, message );
	}
	
    
    /**
     * Display a warning dialog box with information about the exception.
     * @param exception The exception about which the warning dialog is displayed.
     */
    static public void displayWarning( final Exception exception ) {
		ApplicationSupport.displayWarning( exception );
    }

    
    /**
     * Display a warning dialog box.
     * @param title Title of the warning dialog box.
     * @param message The warning message to appear in the warning dialog box.
     */
    static public void displayWarning( final String title, final String message ) {
		displayWarning( title, message );
    }
    
    
    /**
     * Display a warning dialog box with information about the exception.  This method allows
     * clarification about the consequences of the exception (e.g. "Save Failed:").
     * @param title Title of the warning dialog box.
     * @param prefix Text that should appear in the dialog box before the exception messasge.
     * @param exception The exception about which the warning dialog is displayed.
     */
    static public void displayWarning( final String title, final String prefix, final Exception exception ) {
		ApplicationSupport.displayWarning( title, prefix, exception );
    }

    
    /**
     * Display an error dialog box.
     * @param title Title of the warning dialog box.
     * @param message The warning message to appear in the warning dialog box.
     */
    static public void displayError( final String title, final String message ) {
		ApplicationSupport.displayError( title, message );
    }
    
    
    /**
     * Display an error dialog box with information about the exception.
     * @param exception The exception about which the warning dialog is displayed.
     */
    static public void displayError( final Exception exception ) {
		ApplicationSupport.displayError( exception );
    }
    
    
    /**
     * Display an error dialog box with information about the exception.  This method allows
     * clarification about the consequences of the exception (e.g. "Save Failed:").
     * @param title Title of the warning dialog box.
     * @param prefix Text that should appear in the dialog box before the exception messasge.
     * @param exception The exception about which the warning dialog is displayed.
     */
    static public void displayError( final String title, final String prefix, final Exception exception ) {
		ApplicationSupport.displayError( title, prefix, exception );
    }
    
    
    /**
     * Display an error dialog box with information about the exception.  This method allows
     * clarification about the consequences of the exception (e.g. "Save Failed:").
     * @param title Title of the warning dialog box.
     * @param prefix Text that should appear in the dialog box before the exception messasge.
     * @param exception The exception about which the warning dialog is displayed.
     */
    static public void displayApplicationError( final String title, final String prefix, final Exception exception ) {
		displayError( title, prefix, exception );
    }
    
    
    
    /** manage the welcome dialog */
    private class WelcomeController {
        /** Open a new empty document */
        final private int NEW_MODE = 0;
        
        /** Open a document for read/write */
        final private int DOCUMENT_MODE = NEW_MODE + 1;
        
        /** Create a new document based on a template */
        final private int TEMPLATE_MODE = DOCUMENT_MODE + 1;
        
        /** Open a recently viewed document */
        final private int RECENT_MODE = TEMPLATE_MODE + 1;
        
        /** DOCUMENT_CHOOSER for the display */
        final private JFileChooser DOCUMENT_CHOOSER;
        
        /** indicates the mode for which to open a document */
        private int _openMode;
        
        
        /** Constructor */
        private WelcomeController( final Point location ) {            
            final Box accessory = new Box( BoxLayout.Y_AXIS );
            
            final JButton newButton = new JButton( "New Empty" );
            newButton.setToolTipText( "Create a new, empty document" );
            
            final JButton openButton = new JButton( "Documents..." );
            openButton.setToolTipText( "Display documents to open for editing" );
            
            final JButton templateButton = new JButton( "Templates..." );
            templateButton.setToolTipText( "Display documents for which to open new copies" );
            
            final JButton recentButton = new JButton( "Recent..." );
            recentButton.setToolTipText( "Display recently opened documents to open for editing" );
            
            accessory.add( newButton );
            accessory.add( openButton );
            accessory.add( templateButton );
            accessory.add( recentButton );
            
            final URLReference[] recentURLReferences = getValidRecentURLReferences();
            if ( recentURLReferences == null || recentURLReferences.length == 0 ) recentButton.setEnabled( false );
            
            DOCUMENT_CHOOSER = new WelcomeFileChooser( location );
            DOCUMENT_CHOOSER.setAccessory( accessory );
            DOCUMENT_CHOOSER.setMultiSelectionEnabled( true );
            DOCUMENT_CHOOSER.setDialogTitle( "Select " + getAdaptor().applicationName() + " documents to open" );
            FileFilterFactory.applyFileFilters( DOCUMENT_CHOOSER, _applicationAdaptor.readableDocumentTypes() );
            
            final File templateFolder = getTemplateFolder();
            final File documentFolder = getDefaultDocumentFolder();
            
            setOpenMode( TEMPLATE_MODE );
            
            if ( templateFolder != null && templateFolder.exists() && templateFolder.isDirectory() && templateFolder.list().length > 0 ) {
                DOCUMENT_CHOOSER.setCurrentDirectory( templateFolder );
                setOpenMode( TEMPLATE_MODE );
            }
            else if ( documentFolder != null && documentFolder.exists() && documentFolder.isDirectory() ) {
                DOCUMENT_CHOOSER.setCurrentDirectory( documentFolder );
                setOpenMode( DOCUMENT_MODE );
            }
            else {
                setOpenMode( DOCUMENT_MODE );
            }
            
            newButton.addActionListener( new ActionListener() {
                public void actionPerformed( final ActionEvent event ) {
                    setOpenMode( NEW_MODE );
                    DOCUMENT_CHOOSER.approveSelection();
                }
            });
            
            openButton.addActionListener( new ActionListener() {
                public void actionPerformed( final ActionEvent event ) {
                    DOCUMENT_CHOOSER.setCurrentDirectory( documentFolder );
                    setOpenMode( DOCUMENT_MODE );
                }
            });
            
            templateButton.addActionListener( new ActionListener() {
                public void actionPerformed( final ActionEvent event ) {
                    DOCUMENT_CHOOSER.setCurrentDirectory( templateFolder );
                    setOpenMode( TEMPLATE_MODE );
                }
            });
            
            recentButton.addActionListener( new ActionListener() {
                public void actionPerformed( final ActionEvent event ) {
                    final URLReference selection = (URLReference)JOptionPane.showInputDialog( DOCUMENT_CHOOSER, "Open the selected document", "Recent Documents", JOptionPane.PLAIN_MESSAGE, null, recentURLReferences, null );
                    if ( selection != null ) {
                        setOpenMode( RECENT_MODE );
                        DOCUMENT_CHOOSER.approveSelection();
                        openURL( selection.getFullURLSpec() );
                    }
                }
            });
            
            int status = DOCUMENT_CHOOSER.showOpenDialog( null );
            
            switch( status ) {
                case JFileChooser.CANCEL_OPTION:
                    System.exit( 0 );
                    break;
                case JFileChooser.APPROVE_OPTION:
                    processSelections( DOCUMENT_CHOOSER.getSelectedFiles() );
                    break;
                default:
                    newDocument();
                    break;
            }
        }
        
        
        /** Set the open mode */
        private void setOpenMode( final int mode ) {
            _openMode = mode;
            
            switch( mode ) {
                case TEMPLATE_MODE:
                    DOCUMENT_CHOOSER.setApproveButtonText( "Open Template" );
                    DOCUMENT_CHOOSER.setApproveButtonToolTipText( "Open new copies of the selected templates" );
                    break;
                case DOCUMENT_MODE:
                    DOCUMENT_CHOOSER.setApproveButtonText( "Open" );
                    DOCUMENT_CHOOSER.setApproveButtonToolTipText( "Open the documents for editing" );
                    break;
                default:
                    break;
            }
        }
        
        
        /** perform the operation indicated by the mode */
        private void processSelections( final File[] selections ) {            
            if ( _openMode != RECENT_MODE && ( selections == null || selections.length == 0 ) ) {
                newDocument();
                return;
            }
            
            switch( _openMode ) {
                case NEW_MODE:
                    newDocument();
                    break;
                case TEMPLATE_MODE:
                    openFiles( selections, true, false );   // open templates
                    break;
                case DOCUMENT_MODE:
                    openFiles( selections );    // open documents
                    break;
                default:
                    break;
            }
        }
        
        
        /** get those recent URL specs which are valid */
        private URLReference[] getValidRecentURLReferences() {
            return URLReference.getValidReferences( getDefaultDocumentFolderURL(), getRecentURLSpecs() );
        }
    }
    
    
    
    /** custom file chooser for the welcome window */
    private class WelcomeFileChooser extends JFileChooser {
        /** serialization ID */
        private static final long serialVersionUID = 1L;
        
        /** initial location for the dialog */
        final Point INITIAL_LOCATION;
        
        
        /** constructor */
        public WelcomeFileChooser( final Point location ) {
            super();
            
            INITIAL_LOCATION = location;
        }
        
        
        /** Create the dialog */
        protected JDialog createDialog( final java.awt.Component parent ) throws java.awt.HeadlessException {
            final JDialog dialog = super.createDialog( parent );
            dialog.setLocation( INITIAL_LOCATION );
            
            dialog.addComponentListener( new java.awt.event.ComponentAdapter() {
                public void componentMoved( final java.awt.event.ComponentEvent event ) {
                    setNextDocumentOpenLocation( dialog.getLocationOnScreen() );
                }
            });
            
            return dialog;
        }
    }
}




/** constainer of file versions info */
class FileVersionInfo {
    final private String BASE_NAME;
    final private File CURRENT_FOLDER;
    final private Date TIMESTAMP;
    final private File SOURCE_FILE;
    
    /** Constructor */
    public FileVersionInfo( final File sourceFile, final File defaultFolder, final Date timestamp ) {
        TIMESTAMP = timestamp;
        SOURCE_FILE = sourceFile;
        
        if ( sourceFile.exists() && sourceFile.canRead() ) {
            BASE_NAME = getBaseNameStrippingTimestamp( sourceFile );
            if ( defaultFolder != null && defaultFolder.exists() ) {
                final File versionsFolder = new File( defaultFolder, ".versions" );
                final File baseFolder = new File( versionsFolder, BASE_NAME );
                
                final String yearString = new java.text.SimpleDateFormat( "yyyy" ).format( timestamp );
                CURRENT_FOLDER = new File( baseFolder, yearString );
            }
            else {
                CURRENT_FOLDER = null;
            }
        }
        else {
            BASE_NAME = null;
            CURRENT_FOLDER = null;
        }
    }
    
    
    /** get the source file */
    public File getSourceFile() {
        return SOURCE_FILE;
    }
    
    
    /** versions base name for the source file */
    public String getBaseName() {
        return BASE_NAME;
    }
    
    
    /** current folder to hold the latest version */
    public File getCurrentFolder() {
        return CURRENT_FOLDER;
    }
    
    
    /** get the timestamp */
    public Date getTimestamp() {
        return TIMESTAMP;
    }
    
    
    /** Get the base name of a file stripping timestamp tags if any */
    private String getBaseNameStrippingTimestamp( final File file ) {
        return file.getName().replaceFirst( "_@.*@", "" );
    }
}