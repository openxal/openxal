/*
 * @(#)GenDocument.java          0.1 06/16/2003
 *
 * Copyright (c) 2001-2002 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 */

package xal.app.wireanalysis;

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
 * GenDocument is a custom XALDocument for loss viewing application
 *
 * @version   0.1 12/1/2003
 * @author  cp3
 * @author  Sarah Cousineau
 */

public class GenDocument extends AcceleratorDocument implements DataListener{
    
    
    /**
     * The document for the text pane in the main window.
     */
    protected PlainDocument textDocument;
    
    protected Lattice lattice = null;
    
    //public HashMap allagentsmap = new HashMap();
    
    public Accelerator accl;
    public AcceleratorSeq LEBT;
    public AcceleratorSeq RFQ;
    public AcceleratorSeq MEBT;
    public AcceleratorSeq DTL1;
    public AcceleratorSeq DTL2;
    public AcceleratorSeq DTL3;
    public AcceleratorSeq DTL4;
    public AcceleratorSeq DTL5;
    public AcceleratorSeq DTL6;
    public AcceleratorSeq CCL;
    public AcceleratorSeq CCL2;
    public AcceleratorSeq CCL3;
    public AcceleratorSeq CCL4;
    public AcceleratorSeq SCLMed;
    public AcceleratorSeq SCLHigh;
    public AcceleratorSeq HEBT1;
    public AcceleratorSeq LDmp;
    public AcceleratorSeq HEBT2;
    public AcceleratorSeq IDmpm;
    public AcceleratorSeq IDmpp;
    public AcceleratorSeq Ring1;
    public AcceleratorSeq Ring2;
    public AcceleratorSeq Ring3;
    public AcceleratorSeq Ring4;
    public AcceleratorSeq Ring5;
    public AcceleratorSeq RTBT1;
    public AcceleratorSeq RTBT2;
    public AcceleratorSeq EDmp;
    
    /** the name of the xml file containing the accelerator */
    protected String theProbeFile;
    
    /** Create a new empty document */
    public GenDocument() {
        this(null);
        init();
    }
    
    /**
     * Create a new document loaded from the URL file
     * @param url The URL of the file to load into the new document.
     */
    public GenDocument(java.net.URL url) {
        loadDefaultAccelerator();
        accl = getAccelerator();
        
        /*LEBT = accl.getSequence("LEBT");
         RFQ = accl.getSequence("RFQ");
         MEBT = accl.getSequence("MEBT");
         DTL1 = accl.getSequence("DTL1");
         DTL2 = accl.getSequence("DTL2");
         DTL3 = accl.getSequence("DTL3");
         DTL4 = accl.getSequence("DTL4");
         DTL5 = accl.getSequence("DTL5");
         DTL6 = accl.getSequence("DTL6");
         CCL = accl.getSequence("CCL");
         CCL2 = accl.getSequence("CCL2");
         CCL3 = accl.getSequence("CCL3");
         CCL4 = accl.getSequence("CCL4");
         SCLMed = accl.getSequence("SCLMed");
         SCLHigh = accl.getSequence("SCLHigh");
         HEBT1 = accl.getSequence("HEBT1");
         LDmp = accl.getSequence("LDmp");
         HEBT2 = accl.getSequence("HEBT2");
         IDmpm = accl.getSequence("IDmp");
         IDmpp = accl.getSequence("IDmp+");
         Ring1 = accl.getSequence("Ring1");
         Ring2 = accl.getSequence("Ring2");
         Ring3 = accl.getSequence("Ring3");
         Ring4 = accl.getSequence("Ring4");
         Ring5 = accl.getSequence("Ring5");
         RTBT1 = accl.getSequence("RTBT1");
         RTBT2 = accl.getSequence("RTBT2");
         EDmp = accl.getSequence("EDmp");*/
        
        setSource(url);
        if ( url != null ) {
            try {
                System.out.println("Opening document: " + url.toString());
                DataAdaptor documentAdaptor =
                XmlDataAdaptor.adaptorForUrl(url, false);
                update(documentAdaptor.childAdaptor("GenDocument"));
                
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
        mainWindow = new GenWindow(this);
    }
    
    /**
     * Convenience method for getting the main window cast to the proper
     * subclass of XalWindow.  This allows me to avoid casting the window
     * every time I reference it.
     * @return The main window cast to its dynamic runtime class
     */
    private GenWindow myWindow() {
        return (GenWindow)mainWindow;
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
            XmlDataAdaptor documentAdaptor = XmlDataAdaptor.newEmptyDocumentAdaptor();
            documentAdaptor.writeNode(this);
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
        return "GenDocument";
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
    
    
    /** The root locatin of xaldev directory **/
    public File workDir = new File("/home2/scousine");
    
    //Begin declarations and methods specific to the application
    public DataTable masterdatatable;
    public DataTable resultsdatatable;
    public HashMap<String, Integer> masterpvloggermap;
    public Integer currentpvloggerid;
    
    //    public void makeallAgentsMap(ArrayList blmagentlist, String section, String label){
    //        if(blmagentlist.size() > 0){
    //            String name = new String(section + label);
    //            allagentsmap.put(new String(name), blmagentlist);
    //        }
    //    }
    
    public void init(){
        
        ArrayList<DataAttribute> attributes = new ArrayList<DataAttribute>();
        //attributes.add(new DataAttribute("file", String.class, true) );
        attributes.add(new DataAttribute("file", String.class, true));
        masterdatatable = new DataTable("DataTable", attributes);
    	resultsdatatable = new DataTable("ResultsTable", attributes);
        masterpvloggermap = new HashMap<String, Integer>();
    }
    
    
}
