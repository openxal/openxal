//
//  Main.java
//  xal
//
//  Created by Thomas Pelaia on 2/2/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import xal.extension.application.*;
import xal.extension.application.smf.*;


/**
 * Main is the ApplicationAdaptor for the Energy Manager application.
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
        return new String[] {"enman"};
    }
    
    
    /**
	 * Returns the text file suffixes of files this application can write.
     * @return Suffixes of writable files
     */
    public String[] writableDocumentTypes() {
        return new String[] {"enman"};
    }
    
    
    /**
	 * Implement this method to return an instance of my custom document.
     * @return An instance of my custom document.
     */
    public XalDocument newEmptyDocument() {
        return new EnergyManagerDocument();
    }
    
    
    /**
	 * Implement this method to return an instance of my custom document 
     * corresponding to the specified URL.
     * @param url The URL of the file to open.
     * @return An instance of my custom document.
     */
    public XalDocument newDocument( final java.net.URL url ) {
        return new EnergyManagerDocument( url );
    }
    
    
    // --------- Global application management ---------------------------------
    
    
    /**
	 * Specifies the name of my application.
     * @return Name of my application.
     */
    public String applicationName() {
        return "Energy Manager";
    }
    
    
    
    // --------- Application events --------------------------------------------
    
    /** 
	* Capture the application launched event and print it.  This is an optional
	* hook that can be used to do something useful at the end of the application launch.
	*/
    public void applicationFinishedLaunching() {
        System.out.println("Application has finished launching!");
    }
    
    
    /** Constructor */
    public Main() {
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

