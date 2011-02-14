/*
 * Main.java
 *
 * Created on Fri April 17 15:12:21 EDT 2006
 *
 * Copyright (c) 2006 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.bricks;

import xal.application.*;

import java.io.File;
import java.net.URL;


/**
 * Main is the ApplicationAdaptor for this application.
 * @author  tap
 */
public class Main extends ApplicationAdaptor {
    // --------- Document management -------------------------------------------
    
    /**
     * Returns the text file suffixes of files this application can open.
     * @return Suffixes of readable files
     */
    public String[] readableDocumentTypes() {
        return new String[] { "bricks" };
    }
    
    
    /**
     * Returns the text file suffixes of files this application can write.
     * @return Suffixes of writable files
     */
    public String[] writableDocumentTypes() {
        return new String[] { "bricks" };
    }
    
    
    /**
     * Implement this method to return an instance of my custom document.
     * @return An instance of my custom document.
     */
    public XalDocument newEmptyDocument() {
        return new BricksDocument();
    }
    
    
    /**
     * Implement this method to return an instance of my custom document 
     * corresponding to the specified URL.
     * @param url The URL of the file to open.
     * @return An instance of my custom document.
     */
    public XalDocument newDocument( final java.net.URL url ) {
        return new BricksDocument( url );
    }
    
    
    // --------- Global application management ---------------------------------
    
    
    /**
     * Specifies the name of my application.
     * @return Name of my application.
     */
    public String applicationName() {
        return "Bricks";
    }
    
    
    
    // --------- Application events --------------------------------------------
    
    /** 
     * Capture the application launched event and print it.  This is an optional
     * hook that can be used to do something useful at the end of the application launch.
     */
    public void applicationFinishedLaunching() {
        System.out.println( "Application has finished launching!" );
    }
    
    
    /**
     * Constructor
     */
    public Main() {
    }
    
    
    /** The main method of the application. */
    static public void main(String[] args) {
        try {
            System.out.println("Starting Bricks...");
			URL[] urls = new URL[args.length];
			for ( int index = 0 ; index < args.length ; index++ ) {
				urls[index] = new File( args[index] ).toURI().toURL();
			}
            Application.launch( new Main(), urls );
        }
        catch( Exception exception ) {
            System.err.println( exception.getMessage() );
            exception.printStackTrace();
			Application.displayApplicationError( "Launch Exception", "Launch Exception", exception );
			System.exit(-1);
        }
    }
}

