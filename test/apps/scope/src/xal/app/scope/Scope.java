/*
 * Scope.java
 *
 * Created on December 17, 2002, 9:46 AM
 */

package xal.app.scope;

import xal.extension.application.*;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;


/**
 * Main controller of the application.
 *
 * @author  tap
 */
public class Scope extends ApplicationAdaptor {
    // --------- Document management -------------------------------------------
    
    /**
     * Returns the text file suffixes of files this application can open.
     * @return Suffixes of readable files
     */
    public String[] readableDocumentTypes() {
        return new String[] {"scope"};
    }
    
    
    /**
     * Returns the text file suffixes of files this application can write.
     * @return Suffixes of writable files
     */
    public String[] writableDocumentTypes() {
        return new String[] {"scope"};
    }
    
    
    /**
     * Implement this method to return an instance of my custom document.
     * @return An instance of my custom document.
     */
    public XalDocument newEmptyDocument() {
        return new ScopeDocument();
    }
    
    
    /**
     * Implement this method to return an instance of my custom document 
     * corresponding to the specified URL.
     * @param url The URL of the file to open.
     * @return An instance of my custom document.
     */
    public XalDocument newDocument(java.net.URL url) {
        return new ScopeDocument(url);
    }
    
    
    // --------- Global application management ---------------------------------
    
    
    /**
     * Specifies the name of my application.
     * @return Name of my application.
     */
    public String applicationName() {
        return "Scope";
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
    
    
    /**
     * Override this method to show your application's preference panel.  The 
     * preference panel may optionally be document specific or application wide 
     * depending on the application's specific implementation.
     * The default implementaion displays a warning dialog box that now preference panel exists.
     * @param document The document whose preferences are being changed.  Subclass may ignore.
     */
    public void editPreferences(XalDocument document) {
		((ScopeDocument)document).editPreferences();
    }
    
    
    // --------- Application events --------------------------------------------
    
    /** Capture the application launched event and print it */
    public void applicationFinishedLaunching() {		
    }
    
    
    /** The main method of the application. */
    static public void main(String[] args) {
        try {
            System.out.println("Starting Scope...");
			URL[] urls = new URL[args.length];
			for ( int index = 0 ; index < args.length ; index++ ) {
				urls[index] = new File(args[index]).toURI().toURL();
			}
            Application.launch( new Scope(), urls );
        }
        catch(Exception exception) {
            System.err.println( exception.getMessage() );
            exception.printStackTrace();
			Application.displayApplicationError("Launch Exception", "Launch Exception", exception);
			System.exit(-1);
        }
    }
}
