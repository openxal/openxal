/*
 * Main.java
 *
 * Created on June 7, 2011
 *
 * Copyright (c) 2001-2011 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 */

package xal.app.rekit;

import java.util.*;
import java.io.IOException;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.net.*;

import xal.extension.application.*;
import xal.extension.application.smf.*;

/**
 * This is the main class for the extraction kicker restoration application.
 *
 * @version   1.0
 * @author  cp3, zoy
 * @author  Sarah Cousineau, Taylor Patterson
 */
 
public class Main extends ApplicationAdaptor {
    
    //-------------Constructors-------------
    public Main() {
	}

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
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
        return "REKiT";
    }
    
    public XalDocument newDocument(java.net.URL url) {
        return new REKiTDocument(url);
    }
    
    public XalDocument newEmptyDocument() {
        return new REKiTDocument();
    }
    
    public String[] writableDocumentTypes() {
        return new String[] {"text", "txt"};
    }
    
    public String[] readableDocumentTypes() {
        return new String[] {"text", "txt"};
    }
}