/*
 * Main.java
 *
 * Created on Wed Dec 3 15:00:00 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.pvlogger;

import xal.extension.application.*;
import xal.extension.application.smf.*;
import xal.service.pvlogger.*;


/**
 * Main is the ApplicationAdaptor for the history application.
 * @author  tap
 */
public class Main extends ApplicationAdaptor {
	/** main model */
	private final LoggerModel SERVICES_MODEL;
	
	
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
		return newEmptyDocument( "Services" );
    }
	
    
    /**
	 * Implement this method to return an instance of my custom document.
	 * @param type the type of document to open
     * @return an instance of a document corresponding to the specified type.
     */
    public XalDocument newEmptyDocument( final String type ) {
		if ( type.equals( "Services" ) ) {
			return new PVLoggerDocument( SERVICES_MODEL );
		}
		else if ( type.equals( "Configuration" ) ) {
			return new ConfigurationDocument();
		}
		else if ( type.equals( "Browsing" ) ) {
			return new BrowserDocument();
		}
		else {
			return newEmptyDocument();
		}
    }
	
    
    /**
     * Implement this method to return an instance of my custom document corresponding to the specified URL.
     * @param url The URL of the file to open.
     * @return an instance of my custom document.
     */
    public XalDocument newDocument( final java.net.URL url ) {
        return new PVLoggerDocument( url, SERVICES_MODEL );
    }
    
    
    // --------- Global application management ---------------------------------
    
    
    /**
     * Specifies the name of my application.
     * @return Name of my application.
     */
    public String applicationName() {
        return "PV Logger";
    }
    
    
    
    // --------- Application events --------------------------------------------
    
    /** 
     * Capture the application launched event and print it.  This is an optional
     * hook that can be used to do something useful at the end of the application launch.
     */
    public void applicationFinishedLaunching() {
        System.out.println("Application has finished launching!");
    }
	
	    
    /**
     * Override the inherited method to handle the pvlogger quitting
     */
    public void applicationWillQuit() {
		SERVICES_MODEL.dispose();
	}

    
    
    /**
     * Constructor
     */
    public Main() {
		SERVICES_MODEL = new LoggerModel();
    }
    
    
    /** The main method of the application. */
    static public void main(String[] args) {
        try {
            System.out.println("Starting application...");
            AcceleratorApplication.launch( new Main() );
        }
        catch(Exception exception) {
            System.err.println( exception.getMessage() );
            exception.printStackTrace();
			Application.displayApplicationError("Launch Exception", "Launch Exception", exception);
			System.exit(-1);
        }
    }
}

