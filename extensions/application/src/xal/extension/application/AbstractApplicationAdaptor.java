//
//  AbstractApplicationAdaptor.java
//  xal
//
//  Created by Thomas Pelaia on 3/29/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.extension.application;

import xal.tools.apputils.files.*;
import xal.tools.bricks.WindowReference;

import java.util.logging.*;
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
    final protected boolean canOpenDocuments() {
        final String[] documentTypes = readableDocumentTypes();
        return documentTypes != null && documentTypes.length > 0;
    }
    
    
    /** Indicates whether the welcome dialog should be displayed at launch. By default, this returns true if the application can open documents. */
    protected boolean showsWelcomeDialogAtLaunch() {
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
    protected void customizeCommands(Commander commander) {
    }
    
	
    /**
	 * Define some flags for launching the application, such as pre-load a default accelerator.
     * todo: this code needs to be reviewed to determine its value and logic
     */
    protected static void setOptions( String[] args ){
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
		final String path = getGUIDefinitionPath();
		final URL url = getClass().getResource( path );
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
	 * Subclasses should override this method if the resources folder path is in a location other than the default one.  The default file path is a subfolder called <code>resources</code> 
     * located within the same folder where the runtime subclass of ApplicationAdaptor resides. Java package path notation is used. For example "xal.app.Myapp.resources".
     * @return The resource path in classpath notation
     */
    public String getResourcesPath() {
        String packageName = getClass().getPackage().getName();
        return packageName + ".resources";
    }
	
    
    /**
	 * Get the path to the specified named resource which resides in the the resource folder path as specified by the getResourcesPath() method.  For example if 
	 * the resources path is xal.app.Myapp.resources and we wish to get the resource path to the menudef.properties file, the resourceName to specify 
	 * would simply be "menudef" and the method would return "xal.app.Myapp.resources.menudef".
	 * @param resourceName The name of the resource for which to get the path
     * @return The resource definition properties file path in classpath notation
	 * @see #getResourcesPath
     */
    final public String getPathToResource( final String resourceName ) {
        return getResourcesPath() + "." + resourceName;
    }
	
	
	/** 
	 * Get the URL to the specified resource file residing within the resources folder.
	 * @param resourceName file name of the resource to reference
	 * @return the URL to the specified resource
	 */
	public URL getResourceURL( final String resourceName ) {
		return getClass().getResource( "resources/" + resourceName );
	}
	
    
    /**
	 * Subclasses should override this method if the menudefinition properties file is in a location other than the default one.  The default file path is 
     * a properties file called <code>"menudef.properties"</code> located in the resources folder.  Java package path notation is used.  The file's suffix is excluded from the path.
     * For example "xal.app.Myapp.resources.menudef".
     * @return The menu definition properties file path in classpath notation
     */
    public String getMenuDefinitionPath() {
        return getPathToResource( "menudef" );
    }
    
    
    /**
	 * Subclasses should override this method if the application information properties (displayed in the "About" box) file is in a location other than the default one.  
     * The default file path is a properties file called <code>"About.properties"</code> located in the resources folder.  Java package path notation is used.  The file's 
     * suffix is excluded from the path. For example "xal.app.Myapp.resources.About".
     * @return The application information properties file path in classpath notation
     */
    public String getApplicationInfoPath() {
        return getPathToResource( "About" );
    }
    
    
    /**
	 * Subclasses should override this method if the application Help file is in a location other than the default one.  
     * The default file is an HTML file called <code>"Help.html"</code> located in the resources folder.  Standard relative URL notation is used. For example "resources/Help.html".
     * @return the relative application help path in posix notation
     */
    public String getApplicationHelpPath() {
        return "resources/Help.html";
    }	
    
    
    /**
	 * Subclasses may override this method if the GUI definition file has a different name.  
     * @return the relative application GUI definition file
     */
    public String getGUIDefinitionPath() {
        return "resources/gui.bricks";
    }	
}
