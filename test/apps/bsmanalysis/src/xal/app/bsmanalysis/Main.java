/*
 * @(#)Main.java          0.9 05/11/2008
 *
 * Copyright (c) 2001-2002 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 */
package xal.app.bsmanalysis;

import java.util.*;
import java.io.IOException;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.net.*;
import xal.extension.widgets.swing.*;
import xal.extension.application.*;
import xal.extension.application.smf.*;

/**
 * This is the main class for the loss viewer application.
 *
 * @version   0.1 12/1/2003
 * @author  cp3
 * @author  Sarah Cousineau
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
	    AcceleratorApplication.launch( new Main() );
        }
        catch(Exception exception) {
            System.err.println( exception.getMessage() );
            exception.printStackTrace();
            JOptionPane.showMessageDialog(null, exception.getMessage(), 
					  exception.getClass().getName(), 
					  JOptionPane.WARNING_MESSAGE);
        }
    }

    public String applicationName() {
        return "BSManalysis";
    }
    
    public XalDocument newDocument(java.net.URL url) {
        return new GenDocument(url);
    }
    
    public XalDocument newEmptyDocument() {
        return new GenDocument();
    }
    
    public String[] writableDocumentTypes() {
        return new String[] {"text", "txt"};
    }
    
    public String[] readableDocumentTypes() {
        return new String[] {"text", "txt"};
    }
    
    
}

