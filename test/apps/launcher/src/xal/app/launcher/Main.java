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

			// create an instance of this adaptor
			final Main applicationAdaptor = new Main();

			// URL of document to open (if any)
			URL documentURL = null;

			// see if the user has passed a Launcher document to open
			if ( args.length > 0 ) {
				final String documentSpec = args[0];
				System.out.println( "open document with spec: " + documentSpec );

				// first see if the user has passed a valid URL spec and if not then treat it as a file path
				try {
					// attempt to interpret document spec as a URL
					documentURL = new URL( documentSpec );
				} catch( MalformedURLException exception ) {
					// attempt to treat the document spec as a file path (either absolute or relative to the application's default document folder)
					if ( documentSpec.startsWith( File.pathSeparator ) ) {	// absolute path
						documentURL = new File( documentSpec ).toURI().toURL();
					} else {	// relative path (assume relative to this application's default folder)
						final URL defaultFolderURL = applicationAdaptor.getDefaultDocumentFolderURL();
						documentURL = new URL( defaultFolderURL, documentSpec );
					}
				}
			}
			else if ( !Boolean.getBoolean("SAFE_MODE") ) {		// unless we are in safe mode, attempt to open the default document if any
				documentURL = PreferenceController.getDefaultDocumentURL();
			}

			// open the document URL only after we have verified it exists and has content
			boolean openDocumentURL = false;
			if ( documentURL != null ) {
				try {
					// test to make sure the URL content really exists
					int contentLength = documentURL.openConnection().getContentLength();
					if ( !( contentLength > 0 ) ) {
						throw new RuntimeException("Contents of \"" + documentURL +"\" is missing.");
					}
					else {
						// document URL exists and has content
						openDocumentURL = true;
						System.out.println( "opening document at URL: " + documentURL );
					}
				}
				catch(MalformedURLException exception) {
					System.err.println(exception);
				}
				catch(Exception exception) {
					System.err.println(exception);
					System.err.println("Default document \"" + documentURL +"\" cannot be openned.");
					Application.displayApplicationError("Launch Exception", "Default document \"" + documentURL +"\" cannot be openned.", exception);
				}
			}

			// launch the application and open the document URL if any
			final URL[] urls = openDocumentURL ? new URL[] {documentURL} : new URL[] {};
			Application.launch( applicationAdaptor, urls );
        }
        catch(Exception exception) {
            System.err.println( exception.getMessage() );
            exception.printStackTrace();
			Application.displayApplicationError("Launch Exception", "Launch Exception", exception);
			System.exit(-1);
        }
    }
}

