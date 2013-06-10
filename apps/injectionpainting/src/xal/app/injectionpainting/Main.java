/*
 * Main.java
 *
 * Created on October 1, 2007
 */

package xal.app.injectionpainting;

import javax.swing.*;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.event.*;
import java.awt.event.*;
import java.net.*;
import java.util.logging.*;

import xal.application.*;
import xal.tools.IconLib;


/**
 * Main is a demo concrete subclass of ApplicationAdaptor.  
 * @author  cp3
 */
public class Main extends ApplicationAdaptor {
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
    
    /** Indicates whether the welcome dialog should be displayed at launch. */
    protected boolean showsWelcomeDialogAtLaunch() {
        return false;
    }
    
    
    /**
     * Implement this method to return an instance of my custom document.
     * @return An instance of my custom document.
     */
    public XalDocument newEmptyDocument() {
        return new WaveformDocument();
    }
    
    
    /**
     * Implement this method to return an instance of my custom document 
     * corresponding to the specified URL.
     * @param url The URL of the file to open.
     * @return An instance of my custom document.
     */
    public XalDocument newDocument( final java.net.URL url ) {
        return new WaveformDocument( url );
    }
    
    
    // --------- Global application management ---------------------------------
    
    
    /**
     * Specifies the name of my application.
     * @return Name of my application.
     */
    public String applicationName() {
        return "Injection Painting";
    }
    
    
    /**
     * Register actions for the Special menu items.  These actions simply 
     * print to standard output or standard error.
     * This code demonstrates how to define custom actions for menus and the toolbar
     * This method is optional.  You may similarly define actions in the document class
     * if those actions are document specific.  Here the actions are application wide.
     * @param commander The commander with which to register the custom commands.
     */
    protected void customizeCommands(Commander commander) {
    }

    
    // --------- Application events --------------------------------------------
    
    /** 
     * Capture the application launched event and print it.  This is an optional
     * hook that can be used to do something useful at the end of the application launch.
     */
    public void applicationFinishedLaunching() {
        System.out.println("Application has finished launching!");
		Logger.getLogger("global").log( Level.INFO, "Application has finished launching." );
    }


    /**
     * Constructor
     */
    public Main() {}
    
    /** The main method of the application. */
    static public void main(String[] args) {
        try {
            System.out.println("Starting application...");
	    Logger.getLogger("global").log( Level.INFO, "Starting application..." );
            Application.launch( new Main() );
        }
        catch(Exception exception) {
	    Logger.getLogger("global").log( Level.SEVERE, "Error starting application.", exception );
            System.err.println( exception.getMessage() );
            exception.printStackTrace();
	    Application.displayApplicationError("Launch Exception", "Launch Exception", exception);
	    System.exit(-1);
        }
    }
}
