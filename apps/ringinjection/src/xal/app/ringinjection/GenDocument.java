/*
 * @(#)GenDocument.java          0.1 06/16/2003
 *
 * Copyright (c) 2001-2002 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 */

package xal.app.ringinjection;

import java.lang.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.io.File;
import javax.swing.text.*;

import xal.extension.application.smf.*;
import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.data.*;
import xal.extension.application.*;
import xal.model.Lattice;
import xal.tools.*;
import xal.tools.xml.*;
import xal.tools.data.*;
import java.util.HashMap;
import xal.tools.xml.XmlDataAdaptor;


/**
 * GenDocument is a custom XALDocument for loss viewing application 
 *
 * @version   0.1 12/1/2003
 * @author  cp3
 * @author  Sarah Cousineau
 */
 
public class GenDocument extends AcceleratorDocument implements SettingListener,DataListener{    

    
    /**
     * The document for the text pane in the main window.
     */
    protected PlainDocument textDocument;
    
    protected Lattice lattice = null;
    
    /** the name of the xml file containing the accelerator */
    protected String theProbeFile;
    
    /** Create a new empty document */
    public GenDocument() {
	this(null);
	BPMFactory();
    }
    
    /** 
     * Create a new document loaded from the URL file 
     * @param url The URL of the file to load into the new document.
     */
    public GenDocument(java.net.URL url) {
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
    public GenWindow myWindow() {
        return (GenWindow)mainWindow;
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
    
    public double[] inj_params = new double[4];

    private Accelerator accl = new Accelerator();
  
    public ArrayList<BpmAgent> bpmagents;
    //public AcceleratorSeqCombo ringseq;
    public int nbpmagents;
    
    
    public void BPMFactory(){
	
	this.loadDefaultAccelerator();
	accl = this.getAccelerator();
	
	AcceleratorSeq ringseq=(AcceleratorSeq)accl.getRings().get(0);
	
	createAgents(ringseq);
	nbpmagents=bpmagents.size();
	

    }
    
    

	public ArrayList<BpmAgent> createAgents(AcceleratorSeq asequence){

		final List<BPM> bpms = asequence.<BPM>getNodesOfType("BPM");
		bpmagents = new ArrayList<>();

		for ( final BPM bpm : bpms ) {
			BpmAgent agent = new BpmAgent( asequence, bpm );
			if( agent.isOkay() ) {
				bpmagents.add((BpmAgent)agent);
			}

			System.out.println("This is BPM is " + agent.name() + " in sequence " + asequence + ", and status is " + agent.isOkay());
		}

		return bpmagents;
	}
    
    public void setInjSpot(double[] params){
	inj_params = params;
    }
    
    public double[] getInjSpot(){
	return inj_params;
    }
	  	
}
