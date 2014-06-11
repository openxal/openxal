//
//  Main.java
//  xal
//
//  Created by Thomas Pelaia on 9/13/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.app.knobs;

import xal.extension.application.*;
import xal.extension.application.smf.*;

import java.net.URL;


/** Knobs application adaptor. */
public class Main extends ApplicationAdaptor {
    // --------- Document management -------------------------------------------
    
    /**
	 * Returns the text file suffixes of files this application can open.
     * @return Suffixes of readable files
     */
    public String[] readableDocumentTypes() {
        return new String[] { "knobs" };
    }
    
    
    /**
	 * Returns the text file suffixes of files this application can write.
     * @return Suffixes of writable files
     */
    public String[] writableDocumentTypes() {
        return new String[] { "knobs" };
    }
    
    
    /**
	 * Implement this method to return an instance of my custom document.
     * @return An instance of my custom document.
     */
    public XalDocument newEmptyDocument() {
        return new KnobsDocument();
    }
    
    
    /**
	 * Implement this method to return an instance of my custom document corresponding to the specified URL.
     * @param url The URL of the file to open.
     * @return An instance of my custom document.
     */
    public XalDocument newDocument( java.net.URL url ) {
        return new KnobsDocument( url );
    }
    
    
    // --------- Global application management ---------------------------------
    
    
    /**
	 * Specifies the name of my application.
     * @return Name of my application.
     */
    public String applicationName() {
        return "Knobs";
    }
    
    
    
    // --------- Application events --------------------------------------------
    
    /** 
	* Capture the application launched event and print it.  This is an optional
	* hook that can be used to do something useful at the end of the application launch.
	*/
    public void applicationFinishedLaunching() {
        System.out.println( "Application has finished launching!" );
    }
    
    
    /** Constructor */
    public Main() {
    }
    
    
    /** The main method of the application. */
    static public void main(String[] args) {
        try {
            System.out.println( "Starting application..." );
			URL[] urls = new URL[args.length];
			for ( int index = 0 ; index < args.length ; index++ ) {
				urls[index] = new java.io.File( args[index] ).toURI().toURL();
			}
            AcceleratorApplication.launch( new Main(), urls );
        }
        catch( Exception exception ) {
            System.err.println( exception.getMessage() );
            exception.printStackTrace();
			Application.displayApplicationError( "Launch Exception", "Launch Exception", exception );
			System.exit( -1 );
        }
    }
	
}
