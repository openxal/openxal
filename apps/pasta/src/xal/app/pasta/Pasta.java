/*
 * Pasta.java
 *
 * Created June 12, 2004
 */

package xal.app.pasta;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.net.*;

import xal.extension.application.*;
import xal.extension.application.smf.*;

/**
 * An application to perform "signature matching" on cavities by performing
 * phase scans and comparing downstream BPM phase responses to model
 * predictions.
 * @author  jdg
 */
public class Pasta extends ApplicationAdaptor {
    // --------- Document management -------------------------------------------
    
    /**
     * Returns the text file suffixes of files this application can open.
     * @return Suffixes of readable files
     */
    public String[] readableDocumentTypes() {
        return new String[] {"pst"};
    }
    
    /**
     * Returns the text file suffixes of files this application can write.
     * @return Suffixes of writable files
     */
    public String[] writableDocumentTypes() {
        return new String[] {"pst"};
    }
    
    
    /**
     * Implement this method to return an instance of my custom document.
     * @return An instance of my custom document.
     */
    public XalDocument newEmptyDocument() {
        return new PastaDocument();
    }
    
    
    /**
     * Implement this method to return an instance of my custom document 
     * corresponding to the specified URL.
     * @param url The URL of the file to open.
     * @return An instance of my custom document.
     */
    public XalDocument newDocument(java.net.URL url) {
        return new PastaDocument(url);
    }
    
    
    // --------- Global application management ---------------------------------
    
    
    /**
     * Specifies the name of my application.
     * @return Name of my application.
     */
    public String applicationName() {
        return "Pasta";
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
    }
    
    
    /** The main method of the application. */
    static public void main(String[] args) {
        try {
            System.out.println("Launching application...");
            AcceleratorApplication.launch( new Pasta() );
        }
        catch(Exception exception) {
            System.err.println( exception.getMessage() );
            exception.printStackTrace();
            JOptionPane.showMessageDialog(null, exception.getMessage(), exception.getClass().getName(), JOptionPane.WARNING_MESSAGE);
        }
    }
}
