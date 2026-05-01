/*
 * REKiTDocument.java
 *
 * Created on June 8, 2011
 *
 * Copyright (c) 2001-2011 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 */

package xal.app.rekit;

import java.util.*;
import java.net.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.HashMap;

import xal.extension.application.smf.*;
import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.data.*;
import xal.extension.application.*;
import xal.model.*;
import xal.ca.*;
import xal.tools.xml.*;
import xal.tools.data.*;
import xal.tools.messaging.*;
import xal.smf.impl.qualify.*;

/**
 * REKiTDocument is a custom XALDocument for the extraction kicker restoration 
 *		application. 
 *
 * @version  0.1 06/08/2011
 * @author  cp3, zoy
 * @author  Sarah Cousineau, Taylor Patterson
 */
 
public class REKiTDocument extends AcceleratorDocument{    

    
    /**
     * The document for the text pane in the main window.
     */
    protected PlainDocument textDocument;
    protected Lattice lattice = null;
    public Accelerator accl;
    
    // UNUSED
    //public HashMap masterDataMap = new HashMap();
    //public HashMap resultMap = new HashMap();
    
    /** the name of the xml file containing the accelerator */
    protected String theProbeFile;
    
    /** Create a new empty document */
    public REKiTDocument() {
	this(null);
	init();
	}
    
    /** 
     * Create a new document loaded from the URL file 
     * @param url The URL of the file to load into the new document.
     */
    public REKiTDocument(java.net.URL url) {
		loadDefaultAccelerator();
		accl = getAccelerator();
		setSource(url);
		if ( url != null ) {
			try {
				System.out.println("Opening document: " + url.toString());
                DataAdaptor documentAdaptor = 
					XmlDataAdaptor.adaptorForUrl(url, false);
                update(documentAdaptor.childAdaptor("REKiTDocument"));
                setHasChanges(false);
            }
            catch(Exception exception) {
                exception.printStackTrace();
                displayError("Open Failed!", 
			     "Open failed due to an internal exception!", 
			     exception);
            }
        }
	if ( url == null )  return;
	}
    
    /**
     * Make a main window by instantiating the my custom window.  Set the text 
     * pane to use the textDocument variable as its document.
     */
    public void makeMainWindow() {
        mainWindow = new REKiTWindow(this);
    }

    /**
     * Convenience method for getting the main window cast to the proper 
     * subclass of XalWindow.  This allows me to avoid casting the window 
     * every time I reference it.
     * @return The main window cast to its dynamic runtime class
     */
    private REKiTWindow myWindow() {
        return (REKiTWindow)mainWindow;
    }
        
    /** 
     * Customize any special button commands.
     */
    public void customizeCommands(Commander commander) {
    }

    /**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs(java.net.URL url) {
        try {
            XmlDataAdaptor documentAdaptor = 
				XmlDataAdaptor.newEmptyDocumentAdaptor();
            //documentAdaptor.writeNode(this);
            documentAdaptor.writeToUrl(url);
            setHasChanges(false);
        }
        catch(XmlDataAdaptor.WriteException exception) {
            exception.printStackTrace();
            displayError("Save Failed!", 
			 "Save failed due to an internal write exception!", 
			 exception);
        }
        catch(Exception exception) {
            exception.printStackTrace();
            displayError("Save Failed!", 
			 "Save failed due to an internal exception!"
			 , exception);
        }
    }
    
    /** 
     * dataLabel() provides the name used to identify the class in an 
     * external data source.
     * @return The tag for this data node.
     */
    public String dataLabel() {
        return "REKiTDocument";
    }

    /**
     * Instructs the receiver to update its data based on the given adaptor.
     * @param adaptor The data adaptor corresponding to this object's data 
     * node.
     */
    public void update(DataAdaptor adaptor) {
    }
    
     /**
     * When called this method indicates that a setting has changed in 
     * the source.
     * @param source The source whose setting has changed.
     */
    public void settingChanged(Object source) {
        setHasChanges(true);
    }

   /**
     * Instructs the receiver to write its data to the adaptor for external
     * storage.
     * @param adaptor The data adaptor corresponding to this object's data 
     * node.
     */
    public void write(DataAdaptor adaptor) {
    }
    
    //Begin declarations and methods specific to the application
    //public DataTable masterdatatable;
    //public DataTable resultsdatatable;
    //public HashMap masterpvloggermap;
    public Integer currentpvloggerid; 
    
    public void init(){
	
	//ArrayList attributes = new ArrayList();
	//attributes.add(new DataAttribute("file", String.class, true) );
	//attributes.add(new DataAttribute("file", String.class, true));
	//masterdatatable = new DataTable("DataTable", attributes);
	//resultsdatatable = new DataTable("ResultsTable", attributes);
	//masterpvloggermap = new HashMap();
    }

    
}
