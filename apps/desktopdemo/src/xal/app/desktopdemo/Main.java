//
//  Main.java
//  xal
//
//  Created by Thomas Pelaia on 3/31/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.app.desktopdemo;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.net.*;
import java.util.logging.*;

import xal.extension.application.*;


/** demonstration desktop application adaptor. */
public class Main extends DesktopApplicationAdaptor {
    // simple instance variable to hold state for the Demo application
    private boolean isRunning;
    private boolean isPaused;
    private Action startAction;
    private Action stopAction;
    private Action pauseAction;
	
	
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
    public XalInternalDocument newEmptyDocument() {
        return new DemoDocument();
    }
    
    
    /**
	 * Implement this method to return an instance of my custom document corresponding to the specified URL.
     * @param url The URL of the file to open.
     * @return An instance of my custom document.
     */
    public XalInternalDocument newDocument( final URL url ) {
        return new DemoDocument( url );
    }
    
    
    /**
	 * Specifies the name of my application.
     * @return Name of my application.
     */
    public String applicationName() {
        return "Desktop Demo";
    }

	
    /** 
	* Capture the application launched event and print it.  This is an optional
	* hook that can be used to do something useful at the end of the application launch.
	*/
    public void applicationFinishedLaunching() {
        System.out.println("Application has finished launching!");
		Logger.getLogger("global").log( Level.INFO, "Application has finished launching." );
    }
    
    
    /**
	 * Register actions for the Special menu items.  These actions simply 
     * print to standard output or standard error.
     * This code demonstrates how to define custom actions for menus and the toolbar
     * This method is optional.  You may similarly define actions in the document class
     * if those actions are document specific.  Here the actions are application wide.
     * @param commander The commander with which to register the custom commands.
     */
    public void customizeCommands(Commander commander) {
        // define the "start run" demo action
        startAction = new AbstractAction("start-run") {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent event) {
                isRunning = true;
                isPaused = false;
                updateActions();        // update the menu item enable state for user feedback
                System.out.println("Starting the run...");
				Logger.getLogger("global").log( Level.INFO, "Starting the run." );
            }
        };
        commander.registerAction(startAction);
        
        // define the "pause run" demo action
        pauseAction = new AbstractAction("pause-run") {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent event) {
                isRunning = false;
                isPaused = true;
                updateActions();        // update the menu item enable state for user feedback
                System.out.println("Pausing the run...");
				Logger.getLogger("global").log( Level.INFO, "Pausing the run." );
            }
        };        
        commander.registerAction(pauseAction);
        
        
        // define the "stop run" demo action
        stopAction = new AbstractAction("stop-run") {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent event) {
                isRunning = false;
                isPaused = false;
                updateActions();        // update the menu item enable state for user feedback
                System.err.println("Stopping the run...");
				Logger.getLogger("global").log( Level.INFO, "Stopping the run." );
            }
        };        
        commander.registerAction(stopAction);
	}
    
    
    /**
	 * Update the action enable states.  This method demonstrates how to enable
     * or disable menu or toolbar items.  Often this is unnecessary, but it is
     * included to demonstrate how to do this if you wish to provide this kind
     * of feedback to the user.
     */
    protected void updateActions() {
        startAction.setEnabled( !isRunning );
        pauseAction.setEnabled( isRunning );
        stopAction.setEnabled( isRunning || isPaused );
    }
	
    
    /** Constructor */
    public Main() {
		isRunning = false;
		isPaused = false;
    }
    
    
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
