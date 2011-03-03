/*
 * Main.java
 *
 * Created on Thu Feb 19 15:16:57 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.dbbrowser;

import xal.application.*;
import xal.tools.database.*;


/**
 * Main is the ApplicationAdaptor for the database browser application.
 *
 * @author  t6p
 */
public class Main extends ApplicationAdaptor {
    // --------- Document management -------------------------------------------
    
    /**
     * Returns the text file suffixes of files this application can open.
     * @return Suffixes of readable files
     */
    public String[] readableDocumentTypes() {
		return new String[] {"sql"};
    }
    
    
    /**
     * Returns the text file suffixes of files this application can write.
     * @return Suffixes of writable files
     */
    public String[] writableDocumentTypes() {
		return new String[] {"sql"};
    }
    
    
    /**
     * Implement this method to return an instance of my custom document.
     * @return An instance of my custom document.
     */
    public XalDocument newEmptyDocument() {
        return new BrowserDocument();
    }
    
    
    /**
	 * Implement this method to return an instance of my custom document.
     * @return An instance of my custom document.
     */
    public XalDocument newEmptyDocument( final String type ) {
		if ( type.equals( "Browser" ) ) {
			return new BrowserDocument();
		}
		else if ( type.equals( "Query" ) ) {
			return new QueryDocument();
		}
		else {
			return newEmptyDocument();
		}
    }
    
    
    /**
     * Implement this method to return an instance of my custom document 
     * corresponding to the specified URL.
     * @param url The URL of the file to open.
     * @return An instance of my custom document.
     */
    public XalDocument newDocument(java.net.URL url) {
		if ( url.getPath().endsWith( ".sql" ) ) {
			return new QueryDocument( url, null );
		}
		else {
			return null;
		}
    }
    
    
    // --------- Global application management ---------------------------------
    
    
    /**
     * Specifies the name of my application.
     * @return Name of my application.
     */
    public String applicationName() {
        return "Database Browser";
    }
    
    
    
    /**
     * Edit application preferences.
     * @param document The document where to show the preference panel.
     */
    public void editPreferences(XalDocument document) {
        ConnectionPreferenceController.displayPathPreferenceSelector( document.getMainWindow() );
    }
    
    
    // --------- Application events --------------------------------------------
    
    /** 
     * Capture the application launched event and print it.  This is an optional
     * hook that can be used to do something useful at the end of the application launch.
     */
    public void applicationFinishedLaunching() {
        System.out.println("Database Browser has finished launching!");
    }
    
    
    /**
     * Constructor
     */
    public Main() {
    }
    
    
    /** The main method of the application. */
    static public void main(String[] args) {
        try {
            System.out.println("Starting application...");
            Application.launch( new Main() );
        }
        catch(Exception exception) {
            System.err.println( exception.getMessage() );
            exception.printStackTrace();
			Application.displayApplicationError("Launch Exception", "Launch Exception", exception);
			System.exit(-1);
        }
    }
}

