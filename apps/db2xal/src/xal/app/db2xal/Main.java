/*
 * @(#)Main.java          0.9 02/13/2004
 *
 * Copyright (c) 2001-2004 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 */
package xal.app.db2xal;

import java.util.*;
import java.io.IOException;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.net.*;

import xal.extension.application.*;
import xal.extension.application.smf.*;

/**
 * This is the main class for the database query application.  
 *
 * @version   0.9  13 Feb 2004
 * @author  Paul Chu
 */
 
public class Main extends ApplicationAdaptor {

    private URL url;
    
    //-------------Constructors-------------
    public Main() {url = null;}

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
	    setOptions(args);
            AcceleratorApplication.launch( new Main() );
        }
        catch(Exception exception) {
            System.err.println( exception.getMessage() );
            exception.printStackTrace();
            JOptionPane.showMessageDialog(null, exception.getMessage(), exception.getClass().getName(), JOptionPane.WARNING_MESSAGE);
        }
    }

    public String applicationName() {
        return "db2xal";
    }
    
    
    /** Indicates whether the welcome dialog should be displayed at launch. */
    protected boolean showsWelcomeDialogAtLaunch() {
        return false;
    }    
    
    
    public XalDocument newDocument(java.net.URL url) {
        return new Db2XalDocument(url);
    }
    
    public XalDocument newEmptyDocument() {
        return new Db2XalDocument();
    }
    
    public String[] writableDocumentTypes() {
        return new String[] {"d2x", "db", "xml"};
    }
    
    public String[] readableDocumentTypes() {
        return new String[] {"d2x", "db", "xml"};
    }
    
}
