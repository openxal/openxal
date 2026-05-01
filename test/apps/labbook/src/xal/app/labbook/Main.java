/*
 * Main.java
 *
 * Created on Mon Sep 18 12:51:45 EDT 2006
 *
 * Copyright (c) 2006 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.labbook;

import xal.extension.application.*;


/**
 * Main is the ApplicationAdaptor for the Labbook.
 * @author  t6p
 */
public class Main extends ApplicationAdaptor {
    // --------- Document management -------------------------------------------
    
    /**
     * Returns the text file suffixes of files this application can open.
     * @return Suffixes of readable files
     */
    public String[] readableDocumentTypes() {
        return new String[0];
    }
    
    
    /**
     * Returns the text file suffixes of files this application can write.
     * @return Suffixes of writable files
     */
    public String[] writableDocumentTypes() {
        return new String[0];
    }
    
    
    /**
     * Implement this method to return an instance of my custom document.
     * @return An instance of my custom document.
     */
    public XalDocument newEmptyDocument() {
        return new LabbookDocument();
    }
    
    
    /**
     * Implement this method to return an instance of my custom document corresponding to the specified URL.
     * @param url The URL of the file to open.
     * @return An instance of my custom document.
     */
    public XalDocument newDocument( final java.net.URL url ) {
        return newEmptyDocument();
    }
    
    
    // --------- Global application management ---------------------------------
    
    
    /**
     * Specifies the name of my application.
     * @return Name of my application.
     */
    public String applicationName() {
        return "Lab Book";
    }
    
    
    
    // --------- Application events --------------------------------------------
    
    /** Capture the application launched event and print it. */
    public void applicationFinishedLaunching() {
        System.out.println( "Lab Book has finished launching!" );
    }
    
    
    /** Constructor */
    public Main() {
    }
    
    
    /** The main method of the application. */
    static public void main( final String[] args ) {
        try {
            System.out.println( "Starting Lab Book..." );
            Application.launch( new Main() );
        }
        catch(Exception exception) {
            System.err.println( exception.getMessage() );
            exception.printStackTrace();
			Application.displayApplicationError( "Launch Exception", "Launch Exception", exception );
			System.exit( -1 );
        }
    }
}

