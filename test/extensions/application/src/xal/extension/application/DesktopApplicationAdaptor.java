//
//  DesktopApplicationAdaptor.java
//  xal
//
//  Created by Thomas Pelaia on 3/29/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.application;

import xal.tools.apputils.files.*;

import java.util.logging.*;
import java.net.*;


/**
 * DesktopApplicationAdaptor is the abstract superclass of the custom applicaton adaptors each of which acts as a 
 * delegate for its corresponding desktop application.  It contains hooks for handling application events.  It also provides 
 * application wide information about the application.
 *
 * @author  t6p
 */
abstract public class DesktopApplicationAdaptor extends AbstractApplicationAdaptor {
	/**
	 * Launch the application with the specified document URLs.
	 * @param urls The document URLs to open upon launching the application.
	 */
	void launchApplication( final URL[] urls ) {
		DesktopApplication.launch( this, urls );
	}
	
	
    /**
	 * Subclasses should implement this method to return an instance of their
     * custom subclass of XalDocument.
     * @return An instance of the custom subclass of XalDocument
     */
    abstract public XalInternalDocument newEmptyDocument();
    
	
    /**
	 * Subclasses should implement this method to return an instance of their
     * custom subclass of XalDocument.
     * @return An instance of the custom subclass of XalDocument
     */
	public XalInternalDocument newEmptyDocument( final String type ) {
		return newEmptyDocument();
	}
    
    
    /**
	 * Subclasses should implement this method to return an instance of their
     * custom subclass of XalDocument loaded from the specified URL.
     * @return An instance of the custom subclass of XalDocument
     */
    abstract public XalInternalDocument newDocument( URL url );
    
	
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
	 * Override this method to show your application's preference panel.  The preference panel may optionally be 
	 * document specific or application wide depending on the application's specific implementation.
     * The default implementaion displays a warning dialog box that now preference panel exists.
     * @param document The document whose preferences are being changed.  Subclasses may ignore.
     */
    public void editPreferences( final XalInternalDocument document ) {
        document.displayWarning( "No Preference Panel", "This application has not implemented a preference panel." );
    }
	
	
	/**
	 * Determine whether to draw a document's contents when its window is dragged.  The default behavior is true.
	 * Subclasses should override this method if outline mode is desired.
	 * @return true to draw the contents on drag and false to draw an outline.
	 */
	public boolean drawsDocumentContentOnDrag() {
		return true;
	}
	
	
	/**
	 * Event indicating that the application will display the desktop pane.
	 * Subclasses may override this method to handle this event if needed.
	 */
	public void applicationWillDisplayDesktopPane() {}
}
