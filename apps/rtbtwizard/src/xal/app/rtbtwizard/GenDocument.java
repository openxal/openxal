/*
 * @(#)GenDocument.java          0.1 06/16/2003
 *
 * Copyright (c) 2001-2002 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 */

package xal.app.rtbtwizard;

import java.lang.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.io.File;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import javax.swing.event.*;

import xal.smf.application.*;
import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.data.*;
import xal.application.*;
import xal.model.*;
import xal.ca.*;
import xal.tools.*;
import xal.tools.beam.*;
import xal.tools.xml.*;
import xal.tools.data.*;
import xal.tools.messaging.*;
import java.util.HashMap;
import xal.tools.xml.XmlDataAdaptor;

import xal.model.probe.traj.*;
import xal.model.probe.*;
import xal.model.xml.*;
import xal.sim.scenario.*;

//import xal.tools.optimizer.*;


/**
 * GenDocument is a custom XALDocument for loss viewing application
 *
 * @version   0.1 12/1/2003
 * @author  cp3
 * @author  Sarah Cousineau
 */

public class GenDocument extends AcceleratorDocument implements DataListener{
    /** Create a new empty document */
    public GenDocument() {
		this( null );
		init();
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
     * Customize any special button commands.
     */
    protected void customizeCommands(Commander commander) {
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
    
    public DataTable wiredatabase;
    public DataTable wireresultsdatabase;
    public HashMap<String, Double> beamarearatios;
    public HashMap<String, Double> windowarearatios;
    private Accelerator accl = new Accelerator();
    
    //public ArrayList diagagents;
    //public AcceleratorSeqCombo ringseq;
    public int ndiagagents;
    public ArrayList<BpmAgent> bpmagents;
    Channel modeCh;
    CurrentMonitor theBCM;
    String bcmName="RTBT_Diag:BCM25";
    
    Monitor modeChMon;
    String mode;
    
    Monitor bcmMonitor;
    Marker harp;
    Channel harpxch;
    Channel harpych;
    Channel repratech;
    Channel energych;
	
    
    public double charge = 0.;
    public double xsize = 0;
    public double ysize = 0;
    public double xpos = 0;
    public double ypos = 0;
    public double tdensity = 0;
    public double wdensity = 0;
    public boolean lastfitSuperGauss = true;
    
    public void init(){
		ArrayList<DataAttribute> attributes = new ArrayList<DataAttribute>();
		//attributes.add(new DataAttribute("file", String.class, true) );
		attributes.add(new DataAttribute("file", String.class, true));
		wiredatabase = new DataTable("DataTable", attributes);
		wireresultsdatabase = new DataTable("ResultsTable", attributes);
		
		DiagFactory();
    }
    
    
    public void DiagFactory(){
		this.loadDefaultAccelerator();
		accl = this.getAccelerator();
		
		AcceleratorSeq rtbtseq= accl.getSequence("RTBT2");
		
		createAgents(rtbtseq);
		//nbpmagents=rtbtagents.size();
		harp = (Marker)(((ArrayList)rtbtseq.getNodesOfType("Harp")).get(0));
		harpxch=harp.getChannel("xRMS");
		harpych=harp.getChannel("yRMS");
		harpxch.requestConnection();
		harpych.requestConnection();
		repratech = accl.getTimingCenter().getChannel( "repRate" );
		energych = accl.getTimingCenter().getChannel( "ringEnergy" );
		repratech.requestConnection();
		energych.requestConnection();
		Channel.flushIO();
    }
	
	
	/** get the BPM agents */
	public java.util.List<BpmAgent> getBPMAgents() {
		return bpmagents;
	}
    
    
    public ArrayList<BpmAgent> createAgents(AcceleratorSeq asequence){
		ArrayList<AcceleratorNode> bpms = (ArrayList<AcceleratorNode>)asequence.getNodesOfType("BPM");
		bpmagents = new ArrayList<BpmAgent>();
		Iterator<AcceleratorNode> itr = bpms.iterator();
		
		while(itr.hasNext()){
			BpmAgent agent = new BpmAgent(asequence, (BPM)itr.next());
			if(agent.isOkay())
				bpmagents.add(agent);
			
			System.out.println("This is BPM is " + agent.name() + " in sequence " +
							   asequence + ", and status is " + agent.isOkay());
		}
		
		return bpmagents;
    }
    
    /*
     public ArrayList createAgents(AcceleratorSeq asequence){
     
     ArrayList bpms = (ArrayList)asequence.getNodesOfType("BPM");
     bpmagents = new ArrayList();
     Iterator itr = bpms.iterator();
     
     while(itr.hasNext()){
     BpmAgent agent = new BpmAgent(asequence, (BPM)itr.next());
     if(agent.isOkay())
     bpmagents.add((BpmAgent)agent);
     
     System.out.println("This is BPM is " + agent.name() + " in sequence " +
     asequence + ", and status is " + agent.isOkay());
     }
     
     return bpmagents;
     }
     */
    public void initBCM(String bcmchoice){
		bcmName=bcmchoice;
    	modeCh = ChannelFactory.defaultFactory().getChannel("ICS_Tim:MPS_Mode:MachMode");
		try {
			modeChMon = modeCh.addMonitorValTime(new IEventSinkValTime() {
				public void eventValue(ChannelTimeRecord newRecord, Channel chan) {
					mode = newRecord.stringValue();
					theBCM = (CurrentMonitor) accl.getNodeWithId(bcmName);
				}
			}, Monitor.VALUE);
			
		} catch (ConnectionException ce) {
			System.out.println("Cannot connect to " + modeCh.getId());
		} catch (MonitorException me) {
			System.out.println("Cannot monitor to " + modeCh.getId());
		}
    }
	
    public void startBCMMonitor() {
    	Channel bcmChargeCh = ChannelFactory.defaultFactory().getChannel(bcmName+":Q");
		try {
			bcmMonitor = bcmChargeCh.addMonitorValTime(new IEventSinkValTime() {
				public void eventValue(ChannelTimeRecord newRecord, Channel chan) {
					// convert mA to A
					charge = Math.abs(newRecord.doubleValue());
					if (charge == 0.) charge = 0.00001;
					//System.out.println("multiplier = " + 1./charge);
				}
			}, Monitor.VALUE);
			
		} catch (ConnectionException ce) {
			System.out.println("Cannot connect to " + bcmChargeCh.getId());
		} catch (MonitorException me) {
			System.out.println("Cannot monitor to " + bcmChargeCh.getId());
		}
    }
    
    public void stopBCMMonitor() {
		if (bcmMonitor != null) {
			bcmMonitor.clear();
			bcmMonitor = null;
		}
    }
	
}
