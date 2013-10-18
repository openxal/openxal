/*
 * RFDocument.java
 *
 * Created on March 15, 2006, 2:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package xal.app.rfsimulator;

/**
 *
 * @author y32
 */

import java.net.*;
import java.io.*;
import java.lang.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import xal.extension.application.*;
import xal.extension.application.Commander;

import xal.tools.xml.*;
import xal.tools.apputils.*;
import xal.tools.swing.*;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;

import xal.smf.*;
import xal.extension.smf.application.*;

public class RFDocument extends AcceleratorDocument {
   /*
    private XmlDataAdaptor xmlRead, xmlWrite;    
   
    static String ROOT = "RingRF";
    static String LLRF = "llrf";
    static String BEAM = "beam";
    static String CAV1 = "cav1";
    static String CAV2 = "cav2";
    static String AMP = "amp";
    static String PHS = "phase";
    */
    RFMonitor rfmonitor;
    RFController controller;
    BeamMonitor beammonitor;
    
    //String[] cav = {"cav #1", "cav #2"};
    protected int pulsenumber;
    protected double lossrate;
    protected double bandwidth;
    protected double loopq;
    //protected double loopf;
    
    // Tracing 50000 partilces         
    protected double[] bmdp;
    protected double[] bmde;
    
    protected double[] kik1;
    protected double[] kik2;    
    protected double[] kk;    
    
    // 2 X 128 points separatrix curve   
    protected double[] sep1;
    protected double[] sep2;    
    
    // last turn profiles (3X#1 + #2) 128 samples     
    protected double[] phase;    
    protected double[] voltage1; 
    protected double[] voltage2; 
    protected double[] power1; 
    protected double[] axis; 
    
    // turn by turn RF power
    protected double[] count;    
    protected double[] rfpower;
    protected double[] bmct;
    protected double[] phs;
    protected double[] amp;
    
    // 10 step setpoints
    protected double[] turn;    
    protected double[] gain;    
    protected double[][] ampset;
    protected double[][] phsset;
    protected double[][] detune;
    
    // 50 X 1024 buffer
    protected Signal[][] ei;
    protected Signal[][] ep;
    
    // 1000 turn display 
    protected double[] erra;
    protected double[] errp;
    protected double[] picka;
    protected double[] pickp;
    
    // #1 and #2 cavity 
    protected double k;
    protected double ki;
    protected double kp;   
    
    protected double[] rott;    
    protected double[] frequency;
    
    protected double pulsestart;
    protected double beamcurrent;    
    protected double beamenergy;    
    
    protected double step;    
    protected double de;
    protected double dz;
    protected double dc;
    
    protected double period;        
    protected double chopper;
    protected double kicker;
    
    protected double cabledelay;   
    protected double affdelay;
    
    protected double ramp;
    protected double start;
   
    protected boolean beamdetune = false; 
    private boolean stop = true;        
    private boolean aff = false;
    private boolean fbk = false;
            
    public RFDocument() {      
        this(null);
        init();
    }
       
    public RFDocument(URL url) {
	if (url == null)
            return; 
 	setSource(url);        
        init();
    } 
    
    public void setstop(boolean b) {
        stop = b;
    }
            
    public boolean getstop() {
        return stop;
    }
    
    public void setfbk(boolean b) {
        fbk = b;
    }
            
    public boolean getfbk() {
        return fbk;
    }

    public void setaff(boolean b) {
        aff = b;
    }
            
    public boolean getaff() {
        return aff;
    }
    
    private void init() {                
        de   = 0.;
        dz   = 0.; 
        
        kik1 = new double[2];
        kik2 = new double[2];          
        kk = new double[2];          
        
        phase = new double[129];
        sep1 = new double[129];
        sep2 = new double[129];          
        voltage1 = new double[129]; 
        voltage2 = new double[129]; 
        power1 = new double[129]; 
        axis = new double[129]; 
        
        ei  = new Signal[128][1024];        
        ep  = new Signal[128][1024];    
    
        rfpower = new double[1024];
        count = new double[1024];
        bmct = new double[1024];
        picka = new double[1024];
        pickp = new double[1024];        
        erra  = new double[1024];
        errp  = new double[1024];
        amp  = new double[1024];
        phs  = new double[1024];
        
        bmdp  = new double[50000];
        bmde  = new double[50000];
        
        turn = new double[11];                
        gain = new double[11];        
        ampset = new double[2][11];
        phsset = new double[2][11];
        detune = new double[2][11];
                
        frequency = new double[2];
        rott = new double[2];
        
        k  = 0.;
        kp = 0.;
        ki = 0.;
        
        kk[0] = -0.015;
        kk[1] =  0.015;
        
        for (int j = 0; j < 2; j++) {
            frequency[j] = (1.+j)*1.05E6;
            rott[j]= 0.;
        }
        
        for (int j = 0; j < 1024; j++) {
            count[j] = j;
        }
        
        for (int j = 0; j < 129; j++) {
            phase[j] = -180. + j*2.8125;
        }
        
        lossrate = 0.0;
        pulsenumber = 0;
    }
    
    //AffDocument
    public void saveDocumentAs(URL url) {
	writeTo(url);        
	setHasChanges(false);          
    }  
    
    protected void makeMainWindow() {  
                       
        controller = new RFController(this);
        rfmonitor = new RFMonitor(this);        
        beammonitor = new BeamMonitor(this);           
	mainWindow = new RFWindow(this);
       /* 
	if (getSource() != null) {
		xmlRead = XmlDataAdaptor.adaptorForUrl(getSource(), false);
                readFrom();
        }
               
        */
        setHasChanges(false);        
    }  
    
    public void acceleratorChanged() {
	//setHasChanges(true);        
    }
    
    public void errormsg(String ms) {        
		myWindow().errorText.setText(ms);
		System.err.println(ms);	    
    }
    
    protected JPanel makeControlPanel() {
        return controller.makePanel();        
    } 
    
    protected JPanel makeRFPanel() {
        return rfmonitor.makePanel(); 
    }
    
    protected JPanel makeBeamPanel() {
        return beammonitor.makePanel();
    }
    
    protected RFController getController() {
        return controller;
    } 
    
    protected RFMonitor getMonitor() {
        return rfmonitor;
    } 
            
    protected BeamMonitor getBeam() {
	return beammonitor;
    }
   	    	
    protected RFWindow myWindow() {
        return (RFWindow) mainWindow;
    }
    
    protected void dumpdata() {
        
         try {                
                Formatter sp = new Formatter("param.txt"); 
                sp.format("%8.4f %8.4f %8.4f %8.4f\n", chopper*1E6, kicker*1E6, cabledelay*1E6, bandwidth/1E3);
                sp.format("%8.3f %8.3f %8.3f %8.3f\n", rott[0]*57.29578, rott[1]*57.29578, ramp*1E6, affdelay*1E6);
                sp.format("%8.3f %8.3f %8.3f %8.3f\n", start*1E6, k, kp, ki);
                sp.format("%8.2f %8.2f %8.2f\n", beamenergy, beamcurrent*1E3, pulsestart*1E6);
                sp.format("%8.3f %8.3f %8.3f\n", de, dz*57.29578, dc*1E3);
                                
                for (int i=0; i<turn.length; i++ ) {
                    sp.format("%8.2f %8.2f %8.2f %8.1f %8.2f %8.2f %8.2f %8.2f\n",               
                                turn[i], ampset[0][i], phsset[0][i], detune[0][i]*0.001,
                                ampset[1][i], phsset[1][i], detune[1][i]*0.001, gain[i]);                                    
                }
                
                sp.close();
                
                Formatter sf = new Formatter("wave.txt");
                
                sf.format("Last turn waveforms\ndegree    amp1       pw1      v1+v2\n");
                for (int i=0; i<axis.length; i++) {
                   sf.format("%7.3f  %7.2f  %7.2f  %7.2f\n",
                              phase[i], voltage1[i], power1[i], voltage2[i]);
                }
                sf.format("\nTurn by Turn data\nTurns     Ibeam      Amp1     Phs     RFpower\n");
                
                for (int i=0; i<count.length; i++) {
                   sf.format("%6.0f  %7.3f  %7.2f  %7.4f  %7.2f\n",
                              count[i], bmct[i], picka[i], pickp[i]*57.29578, rfpower[i]);
                }
                
                sf.close();
                
                Formatter sb = new Formatter("beam.txt"); 
                
                sb.format("Phase      dE/E\n");
                
                for (int i=0; i<bmde.length; i++) {
                   sb.format("%8.3f  %8.5f\n", bmdp[i], bmde[i]);
                }
                
                sb.close();
                
         } catch (IOException ie) {
                System.out.println(ie + " in saving data");
         }  
        
    }
    
    public void writeTo(URL url) {
      /*  
	xmlWrite = XmlDataAdaptor.newEmptyDocumentAdaptor();
	DataAdaptor xroot = xmlWrite.createChild(ROOT);	
	DataAdaptor cav1 = xroot.createChild(CAV1);
	DataAdaptor xbeam = xroot.createChild(BEAM);
	DataAdaptor llrf = xroot.createChild(LLRF);        
	DataAdaptor amp1 = cav1.createChild(AMP);
	DataAdaptor phase1 = cav1.createChild(PHS);
        
        for (int i=0; i<gain.length; i++) {
            amp1.setValue("a"+i, ampset[0][i]);
            phase1.setValue("p"+i, phsset[0][i]);
        }
        
        llrf.setValue("band", bandwidth);
        llrf.setValue("delay", cabledelay);
        llrf.setValue("choper", chopper);        
        llrf.setValue("kicker", kicker);
        xbeam.setValue("energy", beamenergy);
        xbeam.setValue("current", beamcurrent);
        xbeam.setValue("start", pulsestart);
        
	xmlWrite.writeToUrl(url);
        */
	setHasChanges(false);        
    }

    public void readFrom() { 
        /*
	DataAdaptor xroot = xmlRead.childAdaptor(ROOT);	
	
	DataAdaptor cav1 = xroot.childAdaptor(CAV1);
	DataAdaptor cav2 = xroot.childAdaptor(CAV2);
	DataAdaptor xbeam = xroot.childAdaptor(BEAM);
	DataAdaptor llrf = xroot.childAdaptor(LLRF);
        
	if(xbeam.hasAttribute("start"))
             pulsestart = xbeam.doubleValue("start");
	if(xbeam.hasAttribute("energy"))
             beamenergy = xbeam.doubleValue("energy");
	if(xbeam.hasAttribute("current"))
             beamcurrent = xbeam.doubleValue("current");        
       */
    }    
}
 