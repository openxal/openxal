/*
 * @(#)GenDocument.java          0.1 06/16/2003
 *
 * Copyright (c) 2001-2002 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 */

package xal.app.rocs;

import java.util.*;
import java.net.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import javax.swing.event.*;

import xal.extension.smf.application.*;
import xal.smf.impl.MagnetMainSupply;
import xal.extension.application.*;
import xal.ca.*;
import xal.tools.xml.*;
import xal.tools.data.*;
import xal.tools.messaging.*;

import java.net.URL;
/**
 * GenDocument is a custom XALDocument for ring optic applications 
 *
 * @version   0.1 06/16/2003
 * @author  cp3
 * @author  Sarah Cousineau
 */
 
public class GenDocument extends AcceleratorDocument implements SettingListener,DataListener{    

    
    /**
     * The document for the text pane in the main window.
     */
    protected PlainDocument textDocument;
    
    //      protected Lattice lattice = null;
    
    /** the name of the xml file containing the accelerator */
    protected String theProbeFile;
    
    // define an accelerator
    //  Accelerator             accel = new Accelerator();
    
    /** Create a new empty document */
    public GenDocument() {
	this(null);
	
    }
    
    /** 
     * Create a new document loaded from the URL file 
     * @param url The URL of the file to load into the new document.
     */
    public GenDocument(java.net.URL url) {
        setSource(url);
		opticsProxy=messageCenter.registerSource(this, OpticsListener.class);
		if ( url != null ) {
            try {
                System.out.println("Opening document: " + url.toString());
                DataAdaptor documentAdaptor = 
				XmlDataAdaptor.adaptorForUrl(url, false);
                update(documentAdaptor.childAdaptor("GenDocument"));
				
				quad_k[0]=Double.parseDouble((documentAdaptor.childAdaptor
											  ("GenDocument").stringValue("quad0")));
				quad_k[1]=Double.parseDouble((documentAdaptor.childAdaptor
											  ("GenDocument").stringValue("quad1")));
				quad_k[2]=Double.parseDouble((documentAdaptor.childAdaptor
											  ("GenDocument").stringValue("quad2")));
				quad_k[3]=Double.parseDouble((documentAdaptor.childAdaptor
											  ("GenDocument").stringValue("quad3")));
				quad_k[4]=Double.parseDouble((documentAdaptor.childAdaptor
											  ("GenDocument").stringValue("quad4")));
				quad_k[5]=Double.parseDouble((documentAdaptor.childAdaptor
											  ("GenDocument").stringValue("quad5")));
				
				sext_k[0]=Double.parseDouble((documentAdaptor.childAdaptor
											  ("GenDocument").stringValue("sext0")));
				sext_k[1]=Double.parseDouble((documentAdaptor.childAdaptor
											  ("GenDocument").stringValue("sext1")));
				sext_k[2]=Double.parseDouble((documentAdaptor.childAdaptor
											  ("GenDocument").stringValue("sext2")));
				sext_k[3]=Double.parseDouble((documentAdaptor.childAdaptor
											  ("GenDocument").stringValue("sext3")));
				
				xtune=Double.parseDouble((documentAdaptor.childAdaptor
										  ("GenDocument").stringValue("tunex")));
				ytune=Double.parseDouble((documentAdaptor.childAdaptor
										  ("GenDocument").stringValue("tuney")));
				xchrom=Double.parseDouble((documentAdaptor.childAdaptor
										   ("GenDocument").stringValue("chromx")));
				ychrom=Double.parseDouble((documentAdaptor.childAdaptor
										   ("GenDocument").stringValue("chromy")));
				
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
		if( getAccelerator() == null ) {
			System.out.println("No accelerator specified. Will attempt to load the default accelerator.");
			loadDefaultAccelerator();		
		}		
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
        adaptor.setValue("date", new Date().toString());
	adaptor.setValue("quad0",quad_k[0]);
	adaptor.setValue("quad1",quad_k[1]);
	adaptor.setValue("quad2",quad_k[2]);
	adaptor.setValue("quad3",quad_k[3]);
	adaptor.setValue("quad4",quad_k[4]);
	adaptor.setValue("quad5",quad_k[5]);
	adaptor.setValue("sext0",quad_k[0]);
	adaptor.setValue("sext1",quad_k[1]);
	adaptor.setValue("sext2",quad_k[2]);
	adaptor.setValue("sext3",quad_k[3]);
	adaptor.setValue("tunex",xtune);
	adaptor.setValue("tuney",ytune);
	adaptor.setValue("chromx",xchrom);
	adaptor.setValue("chromy",ychrom);
    }
     
    //set up a messaging center

    protected MessageCenter messageCenter = new MessageCenter("Optics Message");
    /** Add a OpticsListener */
    public void addOpticsListener(OpticsListener listener) {
        messageCenter.registerTarget(listener, this, OpticsListener.class);
    } 

    //Internal methods and variables of application
  
    public double[] quad_k = new double[6];
    public double[] quad_k_llimit = new double[6];
    public double[] quad_k_ulimit = new double[6];
    public double[] sext_k = new double[4];
    public double[] sext_k_llimit = new double[4];
    public double[] sext_k_ulimit = new double[4];
  
    public double xtune, ytune;
    public double xchrom, ychrom;
    public double xphase, yphase;
    protected OpticsListener opticsProxy;  
    public double brho_nom;
    public double mass;
    public double c;

    public ChannelAgent[] quad_ch = new ChannelAgent[6];
    public ChannelAgent[] sext_ch = new ChannelAgent[4];
   
    public int i;
    public int nQuads = 6;
    public int nSexts = 4;
    
   
	
    //Here is the channel setup portion.  Also defines constants.
    public void Setup(){
		if( accelerator == null ) {
			System.out.println("No accelerator specified. Will attempt to load the default accelerator.");
			loadDefaultAccelerator();		
		}		
		
        //First, set up all channels and limits		
        quad_ch[0] = fieldSetChannelAgentForSupply( "Ring_Mag:PS_QV03a05a07" );
        quad_ch[1] = fieldSetChannelAgentForSupply( "Ring_Mag:PS_QH02a08" );
        quad_ch[2] = fieldSetChannelAgentForSupply( "Ring_Mag:PS_QH04a06" );
        quad_ch[3] = fieldSetChannelAgentForSupply( "Ring_Mag:PS_QV01a09" );
        quad_ch[4] = fieldSetChannelAgentForSupply( "Ring_Mag:PS_QV11a12" );
        quad_ch[5] = fieldSetChannelAgentForSupply( "Ring_Mag:PS_QH10a13" );
		
		sext_ch[0] = fieldSetChannelAgentForSupply( "Ring_Mag:PS_SV03a07" );
        sext_ch[1] = fieldSetChannelAgentForSupply( "Ring_Mag:PS_SH04" );
        sext_ch[2] = fieldSetChannelAgentForSupply( "Ring_Mag:PS_SV05" );
        sext_ch[3] = fieldSetChannelAgentForSupply( "Ring_Mag:PS_SH06" );

		brho_nom = 5.65737;
		mass = 0.938272310;
		c = 2.99792458e8;
    }
	
    
    /**
     * Hook for handling the accelerator change event.  Subclasses should override
     * this method to provide custom handling.  The default handler does nothing.
     */
    public void acceleratorChanged() {
		Setup();
    }
	
	
	/** Get the field set channel agent for the specified magnet power supply */
	private ChannelAgent fieldSetChannelAgentForSupply( final String supplyID ) {
		return new ChannelAgent( accelerator.getMagnetMainSupply( supplyID ).getChannel( MagnetMainSupply.FIELD_SET_HANDLE ) );
	}

    //Methods for the tune setting tab:
   
    CalcSettings calcsettings = new CalcSettings();
    public void callTuneCalc(URL url, double ke, double x, double y){
	double[] temp;
	double brho;
	int i;
	xtune = x;
	ytune = y;
	brho= 1.e9*Math.sqrt(ke*(ke+2*mass))/c;
	try{
	    temp=calcsettings.getMags(url,x, y);
	    for(i=0; i<temp.length; i++) quad_k[i] = brho/brho_nom * temp[i];
	}
	catch(Exception e){
	}
	
	opticsProxy.updateQuadK(this, quad_k);
    }
    
    public void setQuadChannelAccess(){
	if((quad_k[0] >= quad_k_llimit[0] && quad_k[0] <= quad_k_ulimit[0])
	   &&(quad_k[1] >= quad_k_llimit[1] && quad_k[1] <= quad_k_ulimit[1])
	   &&(quad_k[2] >= quad_k_llimit[2] && quad_k[2] <= quad_k_ulimit[2])
	   &&(quad_k[3] >= quad_k_llimit[3] && quad_k[3] <= quad_k_ulimit[3])
	   &&(quad_k[4] >= quad_k_llimit[4] && quad_k[4] <= quad_k_ulimit[4])
	   &&(quad_k[5] >= quad_k_llimit[5] && quad_k[5] <= quad_k_ulimit[5]))
	    {
	       for(i=0; i<=nQuads-1; i++) quad_ch[i].setValue(quad_k[i]); 
	       this.setMessage("Qaudrupole strengths submitted to machine.");
	    }
	else{
	    this.setMessage("One or more magnet settings out of range. No assignment made.");
	}	
    }

    public double[] getQuadK(){
	    return quad_k;
    }

    public void setQuadK(double[] local_k){
	    quad_k=local_k;
	    opticsProxy.updateQuadK(this, quad_k);
    }

    public void setTunes(double X, double Y){
	    xtune = X;
	    ytune = Y;
	    opticsProxy.updateTunes(this, xtune, ytune);
}

 
    //Methods for the chromaticity tab:
    public void callChromCalc(URL url, double ke, double x, double y){
	double[] temp;
	double brho;
	int i;
	xchrom=x;
	ychrom=y;
	brho= 1.e9*Math.sqrt(ke*(ke+2*mass))/c;
	try{
	    temp=calcsettings.getMags(url,x, y);
	    for(i=0; i<temp.length; i++) sext_k[i] = brho/brho_nom * (temp[i]*brho_nom);
	}
	catch(Exception e){
	}
	System.out.println("SV03a07=" + sext_k[0] + " T/m^2");
	System.out.println("SH04=" + sext_k[1] + " T/m^2");
	System.out.println("SV05=" + sext_k[2] + " T/m^2");
	System.out.println("SH06=" + sext_k[3] + " T/m^2");
	//For now, convert to Amps
	sext_k[0] = -sext_k[0]/0.0434*0.317;
	sext_k[1] = sext_k[1]/0.0261*0.33;
	sext_k[2] = -sext_k[2]/0.0434*0.317;
	sext_k[3] = sext_k[3]/0.0261*0.33;
	opticsProxy.updateSextK(this, sext_k);
		
    }

    public double[] getPercentRange(double[] local_k){	
	double range[] = new double[2];
	double min=-20;
	double max=20;
	int imin=0;
	int i;

	range[0]=min;

	min=Math.abs(sext_k_llimit[0]-local_k[0]);
	for(i=1; i<=3; i++)
	    if(Math.abs(sext_k_llimit[i]-local_k[i]) < min){
		imin=i;
		min=Math.abs(sext_k_llimit[i]-local_k[i]);
	    }
	for(i=0; i<=3; i++)
	    if(Math.abs(sext_k_ulimit[i]-local_k[i]) < min){
		imin=i;
		min=Math.abs(sext_k_ulimit[i]-local_k[i]);
	    }
	range[1] = Math.rint(100*Math.abs(min/local_k[imin]));

	if(range[1] <= max) return range;
	else{
	    range[1]=max;
	    return range;
	}
    }    

    public void setSextChannelAccess(){
	if((sext_k[0] >= sext_k_llimit[0] && sext_k[0] <= sext_k_ulimit[0])
	   &&(sext_k[1] >= sext_k_llimit[1] && sext_k[1] <= sext_k_ulimit[1])
	   &&(sext_k[2] >= sext_k_llimit[2] && sext_k[2] <= sext_k_ulimit[2])
	   &&(sext_k[3] >= sext_k_llimit[3] && sext_k[3] <= sext_k_ulimit[3]))
	    {
		for(i=0; i<=nSexts-1; i++) sext_ch[i].setValue(sext_k[i]);
		this.setMessage("Sextupole strengths submitted to machine.");
	    }
	else{
	    this.setMessage("One or more magnet settings out of range. No assignment made.");
	}
    }

    public double[] getSextK(){
	return sext_k;
    }

    public void setSextK(double[] local_k){
	sext_k=local_k;
	opticsProxy.updateSextK(this, sext_k);
    }

    public void setChroms(double X, double Y){
	xchrom = X;
	ychrom = Y;
	opticsProxy.updateChroms(this, xchrom, ychrom);
    }

    //Methods for the arc phase tab:

    public void callPhaseCalc(URL url, double ke, double x, double y){
	double[] temp;
	double brho;
	int i;
	brho= 1.e9*Math.sqrt(ke*(ke+2*mass))/c;
	xphase = x;
	yphase = y;
	try{
	    temp=calcsettings.getMags(url,x, y);
	    for(i=0; i<temp.length; i++) quad_k[i] = brho/brho_nom * temp[i];
	}
	catch(Exception e){
	}

	opticsProxy.updateQuadK(this, quad_k);
    }   

    public void setPhases(double X, double Y){
	xphase = X;
	yphase = Y;
	opticsProxy.updatePhases(this, xphase, yphase); 
    }
    
    public void setMessage(String message){
	this.myWindow().messagetext.setText(message);
	//JOptionPane frame = new JOptionPane();
	//JOptionPane.showMessageDialog(frame, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }
}
   








