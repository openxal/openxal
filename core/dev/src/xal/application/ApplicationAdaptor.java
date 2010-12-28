/*
 * ApplicationAdaptor.java
 *
 * Created on March 17, 2003, 5:13 PM
 */

package xal.application;

import xal.tools.apputils.files.*;

import java.util.logging.*;
import java.net.*;


/**
 * ApplicationAdaptor is the abstract superclass of the custom applicaton 
 * adaptors each of which acts as a delegate for its corresponding application.
 * It contains hooks for handling application events.  It also provides 
 * application wide information about the application.
 *
 * @author  t6p
 */
abstract public class ApplicationAdaptor extends AbstractApplicationAdaptor {
	/** wildcard file extension */
	static public final String WILDCARD_FILE_EXTENSION = FileFilterFactory.WILDCARD_FILE_EXTENSION;
	
	
	/**
	 * Launch the application with the specified document URLs.
	 * @param urls The document URLs to open upon launching the application.
	 */
	void launchApplication( final URL[] urls ) {
		FrameApplication.launch( this, urls );
	}
    

    /**
     * Subclasses should implement this method to return an instance of their
     * custom subclass of XalDocument.
     * @return An instance of the custom subclass of XalDocument
     */
    abstract public XalDocument newEmptyDocument();
    
	
    /**
	 * Subclasses should implement this method to return an instance of their
     * custom subclass of XalDocument.
     * @return An instance of the custom subclass of XalDocument
     */
	public XalDocument newEmptyDocument( final String type ) {
		return newEmptyDocument();
	}
    
    
    /**
     * Subclasses should implement this method to return an instance of their
     * custom subclass of XalDocument loaded from the specified URL.
     * @return An instance of the custom subclass of XalDocument
     */
    abstract public XalDocument newDocument( URL url );
    
	
    /**
	 * Generate a new empty document.
     * @return an instance of the custom subclass of XalAbstractDocument
     */
    final public XalAbstractDocument generateEmptyDocument( final String type ) {
		return newEmptyDocument( type );
	}
    
    
    /**
	 * Generate a document from the specified URL.
     * @return An instance of the custom subclass of XalDocument
     */
    final XalAbstractDocument generateDocument( final URL url ) {
		return newDocument( url );
	}
    
    
    /**
     * Override this method to show your application's preference panel.  The 
     * preference panel may optionally be document specific or application wide 
     * depending on the application's specific implementation.
     * The default implementaion displays a warning dialog box that now preference panel exists.
     * @param document The document whose preferences are being changed.  Subclass may ignore.
     */
    public void editPreferences( final XalDocument document ) {
        document.displayWarning( "No Preference Panel", "This application has not implemented a preference panel." );
    }
}




