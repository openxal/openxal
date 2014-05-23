/*
 * @(#)Main.java          0.9 02/26/2004
 *
 * Copyright (c) 2001-2004 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 */
package xal.app.virtualaccelerator;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.net.*;

import xal.extension.application.*;
import xal.extension.application.smf.*;

/**
 * This is the main class for the external lattice file generation application.  
 *
 * @version   0.1  26 Feb 2004
 * @author  Paul Chu
 */
 
public class Main extends ApplicationAdaptor {

    private URL url;
    
    //-------------Constructors-------------
    public Main() {
        url = null;
    }

    public Main(String str) {

	    try{
            url = new URL(str);
	    }
	    catch (MalformedURLException exception) {
            System.err.println(exception);
	    }
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
