/*
 * Main.java
 *
 * Created on 8/12/2003, 1:28 PM
 */

package xal.app.score;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.net.*;

import xal.extension.application.*;

/**
 * Main is the main program for the score app.
 *
 * @author  jdg
 */
public class Main extends ApplicationAdaptor {
	/** logbook to which to post messages */
	final static public String DEFAULT_LOGBOOK = "Automated Entries";
	
	
    /**
     * Returns the text file suffixes of files this application can open.
     * @return Suffixes of readable files
     */
    public String[] readableDocumentTypes() {
        return new String[] {"scr", "SCR"};
    }
    
    
    /**
     * Returns the text file suffixes of files this application can write.
     * @return Suffixes of writable files
     */
    public String[] writableDocumentTypes() {
        return new String[] {"scr", "SCR"};
    }
    
    
    /** Indicates whether the welcome dialog should be displayed at launch. By default, this returns true if the application can open documents. */
    public boolean showsWelcomeDialogAtLaunch() {
        return false;
    }
    
    
    /**
     * Implement this method to return an instance of my custom document.
     * @return An instance of my custom document.
     */
    public XalDocument newEmptyDocument() {
        return new ScoreDocument();
    }
    
    
    /**
     * Implement this method to return an instance of my custom document 
     * corresponding to the specified URL.
     * @param url The URL of the file to open.
     * @return An instance of my custom document.
     */
    public XalDocument newDocument(java.net.URL url) {
        return new ScoreDocument(url);
    }
    
    
    // --------- Global application management ---------------------------------
    
    
    /**
     * Specifies the name of my application.
     * @return Name of my application.
     */
    public String applicationName() {
        return "Score";
    }
    
    
    
    /**
     * Specifies whether I want to send standard output and error to the console
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
        System.out.println("Score application has finished launching!");
    }
    
    
    /**
     * Constructor
     */
    public Main() {
    } 
    
    /** The main method of the application. */
    static public void main(String[] args) {
        try {
            System.out.println("Starting score application...");
            Application.launch( new Main() );
        }
        catch(Exception exception) {
            System.err.println( exception.getMessage() );
            exception.printStackTrace();
            JOptionPane.showMessageDialog(null, exception.getMessage(), exception.getClass().getName(), JOptionPane.WARNING_MESSAGE);
        }
    }
    
     public void editPreferences(XalDocument document) {
        ((ScoreDocument) document).editPreferences();
    }   
}
