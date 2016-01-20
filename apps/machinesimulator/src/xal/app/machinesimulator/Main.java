/*
 * Main.java
 *
 * Created on Feb 11, 2009, 1:32 PM
 */

package xal.app.machinesimulator;

import javax.swing.*;
import java.util.logging.*;

import xal.extension.application.*;
import xal.extension.application.smf.*;

/**
 * Application adaptor for the Machine Simulator application.
 * @author  t6p
 */
public class Main extends ApplicationAdaptor {    
    /**
     * Returns the text file suffixes of files this application can open.
     * @return Suffixes of readable files
     */
    public String[] readableDocumentTypes() {
        return writableDocumentTypes();
    }
    
    
    /**
     * Returns the text file suffixes of files this application can write.
     * @return Suffixes of writable files
     */
    public String[] writableDocumentTypes() {
        return new String[] {"msim"};
    }
    
    
    /**
     * Implement this method to return an instance of my custom document.
     * @return An instance of my custom document.
     */
    public XalDocument newEmptyDocument() {
        return new MachineSimulatorDocument();
    }
    
    
    /**
     * Implement this method to return an instance of my custom document corresponding to the specified URL.
     * @param url The URL of the file to open.
     * @return An instance of my custom document.
     */
    public XalDocument newDocument(java.net.URL url) {
        return new MachineSimulatorDocument( url );
    }
    
    
    /**
     * Specifies the name of my application.
     * @return Name of my application.
     */
    public String applicationName() {
        return "Machine Simulator";
    }
    
    
    /** Capture the application launched event and print it */
    public void applicationFinishedLaunching() {
        System.out.println( "Application finished launching..." );
		Logger.getLogger( "global" ).log( Level.INFO, "Application finished launching." );
    }
    
    
    /** The main method of the application. */
    static public void main(String[] args) {
        try {
            System.out.println( "Launching Machine Simulator application..." );
			Logger.getLogger( "global" ).log( Level.INFO, "Launching the application..." );
            AcceleratorApplication.launch( new Main() );
        }
        catch(Exception exception) {
            System.err.println( exception.getMessage() );
			Logger.getLogger( "global" ).log( Level.SEVERE, "Error launching the application." , exception );
            exception.printStackTrace();
            JOptionPane.showMessageDialog( null, exception.getMessage(), exception.getClass().getName(), JOptionPane.WARNING_MESSAGE );
        }
    }
}
