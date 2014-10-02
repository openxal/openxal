//
//  AbstractApplicationAdaptor.java
//  xal
//
//  Created by Thomas Pelaia on 3/29/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.extension.application;

import xal.tools.ResourceManager;
import xal.tools.apputils.files.*;
import xal.extension.bricks.WindowReference;

import java.util.logging.*;
import java.io.File;
import java.net.*;


/**
 * AbstractApplicationAdaptor is the abstract superclass of the desktop and frame based application adaptors.  
 * It contains hooks for handling application events.  It also provides application wide information about the application.
 *
 * @author  t6p
 */
abstract public class AbstractApplicationAdaptor implements ApplicationListener {
	/** wildcard file extension */
	static public final String WILDCARD_FILE_EXTENSION = FileFilterFactory.WILDCARD_FILE_EXTENSION;

	/** name for the gui bricks resource which may or may not exist */
	static public final String GUI_BRICKS_RESOURCE = "gui.bricks";

	/** location of the resources directory */
	private ApplicationResourceManager _resourceManager;


	/** Constructor */
	public AbstractApplicationAdaptor() {
		// resources are located using the default resource manager
		setResourcesLocation( null );
	}

	
	/**
	 * Launch the application with the specified document URLs.
	 * @param urls The document URLs to open upon launching the application.
	 */
	abstract void launchApplication( final URL[] urls );
	
	
    // --------- Document management -------------------------------------------
    
    /** The URLs to open existing document(s) in the command-line. */
    public static URL[] docURLs;
    
	
    /**
	 * Subclasses should implement this method to return the array of file suffixes identifying the files that can be read by the application.
     * @return An array of file suffixes corresponding to readable files
     */
    abstract public String[] readableDocumentTypes();
    
    
    /**
	 * Subclasses should implement this method to return the array of file suffixes identifying the files that can be written by the application.
     * @return An array of file suffixes corresponding to writable files
     */
    abstract public String[] writableDocumentTypes();
    
    
    /** Determine whether this application can open documents */
    final public boolean canOpenDocuments() {
        final String[] documentTypes = readableDocumentTypes();
        return documentTypes != null && documentTypes.length > 0;
    }
    
    
    /** Indicates whether the welcome dialog should be displayed at launch. By default, this returns true if the application can open documents. */
    public boolean showsWelcomeDialogAtLaunch() {
        return canOpenDocuments();
    }
    
	
    /**
	 * Generate a new empty document of the specified type.
	 * @param type the type of document to create.
     * @return an instance of the custom subclass of XalAbstractDocument
     */
    abstract public XalAbstractDocument generateEmptyDocument( final String type );
    
    
    /**
	 * Generate a document from the specified URL.
     * @return an instance of the custom subclass of XalAbstractDocument
     */
    abstract XalAbstractDocument generateDocument( URL url );    
    
    
    // --------- Global application management ---------------------------------
    
    /**
	 * Subclasses must implement this method to return the name of their application.
     * @return The name of the application
     */
    abstract public String applicationName();
    
    
    /**
	 * Identifies whether the application sends standard output and standard error to the application's console or whether it should simply go to 
     * the terminal from which the application was launched.  The default is to return true thus indicating that the console should be used.
     * @return Whether the application's console should capture standard output and error
     */
    public boolean usesConsole() {
        return true;
    }
    
    
    /**
	 * Override this method to register custom application commands.
     * @param commander The commander with which to register commands.
     * @see Commander#registerAction(Action)
     */
    public void customizeCommands(Commander commander) {
    }
    
	
    /**
	 * Define some flags for launching the application, such as pre-load a default accelerator.
     * todo: this code needs to be reviewed to determine its value and logic
     */
    public static void setOptions( String[] args ){
        if (args.length > 0){
            
            final java.util.ArrayList<String> docPaths = new java.util.ArrayList<String>();
            for ( final String arg : args ) {
                if ( !arg.startsWith( "-" ) ) {
                    docPaths.add( arg );
                }
            }
            if ( docPaths.size() > 0 ) {
                docURLs = new URL[docPaths.size()];
                for ( int index = 0; index < docPaths.size(); index++ ) {
                    try {
                        docURLs[index] = new URL( "file://" + docPaths.get( index ) );
                    } 
                    catch ( MalformedURLException exception ) {
						Logger.getLogger("global").log( Level.WARNING, "Error setting the documents to open passed by the user.", exception );
                        System.err.println( exception );
                    }
                }
            }
        }
    }
    
	
    /**
	 * Get the document URLs.
	 * @return document URLs
     */
    public static URL[] getDocURLs() {
        return docURLs;
    }
	
	
	/** Get the window reference from the resource if any */
	public WindowReference getDefaultWindowReference( final String tag, final Object... parameters ) {
		final URL url = getResourceURL( GUI_BRICKS_RESOURCE );
		return new WindowReference( url, tag, parameters );
	}
    
    
    // --------- Application events --------------------------------------------
	
	/**
	 * Event indicating that the application will open any initial documents.  These documents may include a new empty document if appropriate or any documents passed at the command line.
	 * Subclasses may override this method to handle this event if needed.
	 */
	public void applicationWillOpenInitialDocuments() {}
	
    
    /** Subclasses may override this method to provide custom handling upon completion of the application having launched.  The default implementation does nothing. */
    public void applicationFinishedLaunching() {}
    
	
    /** Implement ApplicationListener.  Subclasses may implement this method to handle a document closed event at the application level.  The default implementation does nothing. */
    public void documentClosed( final XalAbstractDocument document ) {}
    
    
    /** Implement ApplicationListener.  Subclasses may implement this method to handle a document created event at the application level.  The default implementation does nothing. */
    public void documentCreated( final XalAbstractDocument document ) {}
    
    
    /** Implement ApplicationListener.  Subclasses may implement this method to handle an "application will quit" event at the application level.  The default implementation does nothing. */
    public void applicationWillQuit() {}
    
    
    
    // --------- Application resources -----------------------------------------

	/** 
	 * Subclasses can set the location of the resources directory. This is really used for script based applications that don't reside in a jar file. 
	 * @param resourcesDirectory normal file system directory specifying the location of the resources directory
	 */
	private void setResourcesDirectory( final File resourcesDirectory ) {
		try {
			setResourcesLocation( resourcesDirectory.toURI().toURL() );
		}
		catch ( MalformedURLException exception ) {
			throw new RuntimeException( "Bad URL to the application resource specified with the directory: " + resourcesDirectory, exception );
		}
	}


	/**
	 * Convenience method to set the location of the resources directory by specifying the parent directory of resources. The resources directory is assumed to be named "resources". This is really used for script based applications that don't reside in a jar file.
	 * @param resourcesParentDirectory normal file system directory specifying the location of the parent directory of the resources
	 */
	public void setResourcesParentDirectory( final File resourcesParentDirectory ) {
		if ( resourcesParentDirectory != null ) {
			setResourcesDirectory( new File( resourcesParentDirectory, "resources" ) );
		}
		else {
			setResourcesLocation( null );
		}
	}


	/**
	 * Convenience method to set the location of the resources directory by specifying the parent directory of resources. The resources directory is assumed to be named "resources". This is really used for script based applications that don't reside in a jar file.
	 * @param resourcesParentDirectoryPath full file system directory path specifying the location of the parent directory of the resources
	 */
	public void setResourcesParentDirectoryWithPath( final String resourcesParentDirectoryPath ) {
		if ( resourcesParentDirectoryPath != null ) {
			setResourcesParentDirectory( new File( resourcesParentDirectoryPath ) );
		}
		else {
			setResourcesLocation( null );
		}
	}


	/** Subclasses can set the location of the resources directory. Setting it to null will use the default resource manager. */
	public void setResourcesLocation( final URL resourcesLocation ) {
		if ( resourcesLocation != null ) {
			_resourceManager = new LocationApplicationResourceManager( resourcesLocation );
		}
		else {
			_resourceManager = ApplicationResourceManager.getDefaultInstance();
		}
	}


	/**
	 * Get the URL to the specified resource residing within the resources directory.
	 * @param resourceSpec specification of the resource relative to the resources URL
	 * @return the full URL to the specified resource
	 */
	public URL getResourceURL( final String resourceSpec ) {
		return _resourceManager.getResourceURL( this, resourceSpec );
	}
}



/** abstract resource manager for applications */
abstract class ApplicationResourceManager {
	/** get the named resource for the specified application */
	abstract public URL getResourceURL( final AbstractApplicationAdaptor adaptor, final String resourceSpec );


	/** get the singleton instance */
	static public DefaultApplicationResourceManager getDefaultInstance() {
		return DefaultApplicationResourceManager.getInstance();
	}
}



/** resource manager for applications that uses the default resource manager */
class DefaultApplicationResourceManager extends ApplicationResourceManager {
	/** singleton resource manager */
	final static private DefaultApplicationResourceManager DEFAULT_RESOURCE_MANAGER;


	// static initializer
	static {
		DEFAULT_RESOURCE_MANAGER = new DefaultApplicationResourceManager();
	}


	/** get the singleton instance */
	static public DefaultApplicationResourceManager getInstance() {
		return DEFAULT_RESOURCE_MANAGER;
	}


	/** get the named resource for the specified application */
	public URL getResourceURL( final AbstractApplicationAdaptor adaptor, final String resourceSpec ) {
		return ResourceManager.getResourceURL( adaptor.getClass(), resourceSpec );
	}
}


/** resource manager for applications that uses a specific location to search for resources (suitable for script based applications) */
class LocationApplicationResourceManager extends ApplicationResourceManager {
	/** location of the resources directory */
	final private URL RESOURCES_LOCATION;


	/** Constructor */
	public LocationApplicationResourceManager( final URL resourcesLocation ) {
		RESOURCES_LOCATION = resourcesLocation;
	}


	/** get the named resource for the specified application */
	public URL getResourceURL( final AbstractApplicationAdaptor adaptor, final String resourceSpec ) {
		try {
			return new URL( RESOURCES_LOCATION, resourceSpec );
		}
		catch( MalformedURLException exception ) {
			throw new RuntimeException( "Bad URL to the application resource: " + resourceSpec, exception );
		}
	}
}
