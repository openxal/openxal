/*
 * Main.java
 *
 * Created on Tue Feb 17 16:30:56 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.mpsclient;

import xal.extension.application.*;


/**
 * Main is the ApplicationAdaptor for the history application.
 *
 * @author  tap
 */
public class Main extends ApplicationAdaptor {
	/** main model */
	protected MPSModel _model;
	
	
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
        return new MPSDocument(_model);
    }
    
    
    /**
     * Implement this method to return an instance of my custom document 
     * corresponding to the specified URL.
     * @param url The URL of the file to open.
     * @return An instance of my custom document.
     */
    public XalDocument newDocument(java.net.URL url) {
        return new MPSDocument(url, _model);
    }
    
    
    // --------- Global application management ---------------------------------
    
    
    /**
     * Specifies the name of my application.
     * @return Name of my application.
     */
    public String applicationName() {
        return "MPS First Faults";
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
     * Override the inherited method to handle the MPS client quitting
     */
    public void applicationWillQuit() {
		_model.dispose();
	}

    
    
    /**
     * Constructor
     */
    public Main() {
		_model = new MPSModel();
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

