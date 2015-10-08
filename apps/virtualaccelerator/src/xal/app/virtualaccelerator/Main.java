/*
 * @(#)Main.java          0.9 02/26/2004
 *
 * Copyright (c) 2001-2004 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 */
package xal.app.virtualaccelerator;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import xal.extension.application.Application;
import xal.extension.application.ApplicationAdaptor;
import xal.extension.application.XalDocument;
import xal.extension.application.smf.AcceleratorApplication;

/**
 * This is the main class and Application adapter for Virtual accelerator. It provides entry point for the program and
 * information along with some callback for other parts of application.
 * 
 * @version 0.2 13 Jul 2015
 * @author Paul Chu
 * @author Blaz Kranjc <blaz.kranjc@cosylab.com>
 */
public class Main extends ApplicationAdaptor {
    /**
     * Variable indicating wheather or not application should start virtual accelerator after loading is done.False by
     * default.
     */
    private static boolean runOnFinishedLaunching = false;
    
    //-------------Constructors-------------
    public Main() {
    }


    /** The main method of the application. */
    static public void main(String[] args) {
        try {
            System.out.println("Starting application...");

            // set command-line option(s) for opening existing document(s)
            setOptions( args );
            AcceleratorApplication.launch( new Main() );
        }
        catch(Exception exception) {
            System.err.println( exception.getMessage() );
            exception.printStackTrace();
            JOptionPane.showMessageDialog( null, exception.getMessage(), exception.getClass().getName(), JOptionPane.WARNING_MESSAGE );
        }
    }
    
    /**
     * Loads command line options.
     * 
     * @param args
     *            arguments given by main method.
     */
    public static void setOptions(String[] args) {

        final java.util.ArrayList<String> docPaths = new java.util.ArrayList<String>();
        for (final String arg : args) {
            if (!arg.startsWith("-")) {
            	docPaths.add(arg);// We add any filepaths. 
            } else {
                switch (arg) {
                case "-r":
                case "--run":                
                    runOnFinishedLaunching = true;
                    break;
                case "-h":
                case "--help":
                    printHelp();
                    System.exit(0);
                }

            }
        }
        if (docPaths.size() > 0) {
            docURLs = new URL[docPaths.size()];
            for (int index = 0; index < docPaths.size(); index++) {
                try {
                    docURLs[index] = new URL("file://" + docPaths.get(index));
                } catch (MalformedURLException exception) {
                    Logger.getLogger("global").log(Level.WARNING,
                            "Error setting the documents to open passed by the user.", exception);
                    System.err.println(exception);
                }
            }
        }
    }
    
    
    /** Convenient method for printing command line options help for user. */
    private static void printHelp() {
        System.out.println("Usage:virtualaccelerator [options] [files]\r\n"
                         + "        Runs Virtual Accelerator.\r\n"
                         + "  options:\r\n"
                         + "        -r,--run                        run accelerator after loading.\r\n"
                         + "        -h,--help                       print this help.\r\n"
                         + "  files:\r\n"
                         + "    path(s) to virtual accelerator[.va] file we want to open.\r\n");
    }


    /**
     * Callback method to start virtual accelerator if {@link #runOnFinishedLaunching} is true.
     */
    @Override
    public void applicationFinishedLaunching() {
        if (runOnFinishedLaunching) {
            for(VADocument document:Application.getApp().<VADocument>getDocumentsCopy()){
                document.commander.getAction("run-va").actionPerformed(new ActionEvent(this, 0, "Run"));    
            }      
        }

    }
    
    /**
     * Callback method to destroy all servers when application is exiting.
     */
    public void applicationWillQuit() {
		try {
			final List<VADocument> documents = Application.getApp().<VADocument>getDocumentsCopy();
            for ( final VADocument document : documents ) {
				try {
					document.destroyServer();
				}
				catch( Exception exception ) {
					System.err.println( exception.getMessage() );					
				}
			}
		} 
		catch ( Exception exception ) {
			System.err.println( exception.getMessage() ); 
		}
    }
	
    public String applicationName() {
        return "Virtual Accelerator";
    }
    
    public XalDocument newDocument(java.net.URL url) {
        return new VADocument(url);
    }
    
    public XalDocument newEmptyDocument() {
        return new VADocument();
    }
    
    public String[] writableDocumentTypes() {
        return new String[] {"va"};
    }
    
    public String[] readableDocumentTypes() {
        return new String[] {"va", "xml"};
    }

    
    public boolean usesConsole() {
		return true;
    }    
}
