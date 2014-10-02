/*
 * Main.java
 *
 * Created on Fri March 5 9:15:32 EDT 2004
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.launcher;

import xal.extension.application.*;

import java.io.*;
import java.net.*;


/**
 * Main is the ApplicationAdaptor for the Launcher application.
 *
 * @author t6p
 */
public class Main extends ApplicationAdaptor {
    // --------- Document management -------------------------------------------
    
    /**
     * Returns the text file suffixes of files this application can open.
     * @return Suffixes of readable files
     */
    public String[] readableDocumentTypes() {
        return new String[] {"launch"};
    }
    
    
    /**
     * Returns the text file suffixes of files this application can write.
     * @return Suffixes of writable files
     */
    public String[] writableDocumentTypes() {
        return new String[] {"launch"};
    }

    
    /** Indicates whether the welcome dialog should be displayed at launch. */
    public boolean showsWelcomeDialogAtLaunch() {
        return false;
    }
    
    
    /**
     * Implement this method to return an instance of my custom document.
     * @return An instance of my custom document.
     */
    public XalDocument newEmptyDocument() {
        return new LaunchDocument();
    }
    
    
    /**
     * Implement this method to return an instance of my custom document 
     * corresponding to the specified URL.
     * @param url The URL of the file to open.
     * @return An instance of my custom document.
     */
    public XalDocument newDocument(java.net.URL url) {
        return new LaunchDocument(url);
    }
    
    
    // --------- Global application management ---------------------------------
    
    
    /**
     * Specifies the name of my application.
     * @return Name of my application.
     */
    public String applicationName() {
        return "Launcher";
    }
    
    
    
    // --------- Application events --------------------------------------------
    
    /** 
     * Capture the application launched event and print it.  This is an optional
     * hook that can be used to do something useful at the end of the application launch.
     */
    public void applicationFinishedLaunching() {
        System.out.println("Launcher running...");
    }
    
    
    /**
     * Constructor
     */
    public Main() {
    }
    
    
    /**
     * Edit application preferences.
     * @param document The document where to show the preference panel.
     */
    public void editPreferences(XalDocument document) {
        PreferenceController.displayPathPreferenceSelector( document.getMainWindow() );
    }
	
    
    /** The main method of the application. */
    static public void main(String[] args) {
        try {
            System.out.println("Starting Launcher...");
			boolean openDefaultDocument = !Boolean.getBoolean("SAFE_MODE");
			URL url = null;
			if (openDefaultDocument) {
				try {
					url = PreferenceController.getDefaultDocumentURL();
					openDefaultDocument = url != null;
					// test to make sure the URL content really exists
					if ( openDefaultDocument ) {
						int contentLength = url.openConnection().getContentLength();
						openDefaultDocument = contentLength > 0;
						if (!openDefaultDocument) {
							throw new RuntimeException("Contents of \"" + url +"\" is missing.");
						}
					}
				}
				catch(MalformedURLException exception) {
					openDefaultDocument = false;
					System.err.println(exception);
				}
				catch(Exception exception) {
					openDefaultDocument = false;
					System.err.println(exception);
					System.err.println("Default document \"" + url +"\" cannot be openned.");
					Application.displayApplicationError("Launch Exception", "Default document \"" + url +"\" cannot be openned.", exception);
				}
			}
			URL[] urls = (openDefaultDocument) ? new URL[] {url} : new URL[] {};
			Application.launch( new Main(), urls );
        }
        catch(Exception exception) {
            System.err.println( exception.getMessage() );
            exception.printStackTrace();
			Application.displayApplicationError("Launch Exception", "Launch Exception", exception);
			System.exit(-1);
        }
    }
}

