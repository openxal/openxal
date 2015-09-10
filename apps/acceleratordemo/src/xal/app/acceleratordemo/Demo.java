/*
 * Main.java
 *
 * Created on March 19, 2003, 1:28 PM
 */

package xal.app.acceleratordemo;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.net.*;
import java.util.logging.*;

import xal.extension.application.*;
import xal.extension.application.smf.*;

/**
 * Demo is a demo concrete subclass of ApplicationAdaptor.  This demo application
 * is a simple accelerator viewer that demonstrates how to build a simple 
 * accelerator based application using the accelerator application framework.
 *
 * @author  t6p
 */
public class Demo extends ApplicationAdaptor {
    // --------- Document management -------------------------------------------
    
    /**
     * Returns the text file suffixes of files this application can open.
     * @return Suffixes of readable files
     */
    public String[] readableDocumentTypes() {
        return new String[] {"txt", "text"};
    }
    
    
    /**
     * Returns the text file suffixes of files this application can write.
     * @return Suffixes of writable files
     */
    public String[] writableDocumentTypes() {
        return new String[] {"txt", "text"};
    }
    
    
    /**
     * Implement this method to return an instance of my custom document.
     * @return An instance of my custom document.
     */
    public XalDocument newEmptyDocument() {
        return new DemoDocument();
    }
    
    
    /**
     * Implement this method to return an instance of my custom document 
     * corresponding to the specified URL.
     * @param url The URL of the file to open.
     * @return An instance of my custom document.
     */
    public XalDocument newDocument(java.net.URL url) {
        return new DemoDocument(url);
    }
    
    
    // --------- Global application management ---------------------------------
    
    
    /**
     * Specifies the name of my application.
     * @return Name of my application.
     */
    public String applicationName() {
        return "DemoAcceleratorApplicaton";
    }
    
    
    /**
     * Specifies whether I want to send standard output and error to the console.
     * I don't need to override the superclass adaptor to return true (the default), but
     * it is sometimes convenient to disable the console while debugging.
     * @return Name of my application.
     */
    public boolean usesConsole() {
        String usesConsoleProperty = System.getProperty("usesConsole");
        if ( usesConsoleProperty != null ) {
            return Boolean.valueOf(usesConsoleProperty).booleanValue();
        }
        else {
            return true;
        }
    }
    
    
    // --------- Application events --------------------------------------------
    
    /** Capture the application launched event and print it */
    public void applicationFinishedLaunching() {
        System.out.println("Application finished launching...");
		Logger.getLogger("global").log( Level.INFO, "Application finished launching." );
    }
    
    
    /** The main method of the application. */
    static public void main(String[] args) {
        try {
            System.out.println("Launching application...");
			Logger.getLogger("global").log( Level.INFO, "Launching the application..." );
            AcceleratorApplication.launch( new Demo() );
        }
        catch(Exception exception) {
            System.err.println( exception.getMessage() );
			Logger.getLogger("global").log( Level.SEVERE, "Error launching the application." , exception );
            exception.printStackTrace();
            JOptionPane.showMessageDialog(null, exception.getMessage(), exception.getClass().getName(), JOptionPane.WARNING_MESSAGE);
        }
    }
}
