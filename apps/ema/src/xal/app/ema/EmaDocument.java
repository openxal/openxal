/*
 * PastaDocument.java
 *
 * Created on March 5, 2005
 */

package xal.app.ema;

import java.util.*;
import java.net.*;
import java.io.*;
import java.awt.Toolkit;

import xal.ca.*;
import xal.extension.application.*;
import xal.extension.application.smf.*;
import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.impl.qualify.*;
import xal.sim.scenario.*;
import xal.model.alg.ParticleTracker;
import xal.model.probe.*;

/**
 * This class contains the primary internal working objects of the
 * slacs application. It provides the top level accelerator view to
 * allow selection of the components to tune and stores the
 *  selected parts of the accelerator that are being used.
 *
 * @author  jdg
 */
public class EmaDocument extends AcceleratorDocument {
    
    /** the helper class to save and open documents to/from xml files */
    //   private SaveOpen saveOpen;
    
    /** the sequence, note since this app only deals with the scl,
     * this will always be the SCL
     */
    protected AcceleratorSeq theSeq;
    
    /** a probe to use to get beam mass, charge, ...*/
    protected ParticleProbe theProbe;
    
    /** the bpm calculation controller */
    protected BPMController bpmController;
    
    /** list of linac BPMs the user can choose from  for TOF calcs */
    protected Collection<BPM> linacBPMs;
    
    /** workaround to avoid jca context initialization exception */
    static{
        ChannelFactory.defaultFactory().init();
    }
    
    /** Create a new empty document */
    public EmaDocument() {
		this( null );
    }
    
    /**
     * Create a new document loaded from the URL file
     * @param url The URL of the file to load into the new document.
     */
    public EmaDocument( final java.net.URL url ) {
		bpmController = new BPMController(this);
		System.out.println("controller done");
		bpmController.makeBPMResultsTable();
        
        if ( url == null )  {
			return;
		}
        else {
            System.out.println("Opening document: " + url.toString());
            setSource(url);
        }
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
        
        mainWindow = new EmaWindow(this);
        // now that we have a window, let's read in the input file + set it up
        //if(getSource() != null ) saveOpen.readSetupFrom(getSource());
        myWindow().presetBPMList.setListData(bpmController.getDefaultPairs().toArray());
        myWindow().updateListData();
        //bpmController.updateBPMTable();
    }
    
    
    /**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs(URL url) {
        //saveOpen.saveTo(url);
        setHasChanges(false);
    }
    
    
    /**
     * Convenience method for getting the main window cast to the proper subclass of XalWindow.
     * This allows me to avoid casting the window every time I reference it.
     * @return The main window cast to its dynamic runtime class
     */
    protected EmaWindow myWindow() {
        return (EmaWindow) mainWindow;
    }
    
    /**
     * Handle the accelerator changed event by displaying the elements of the
     * accelerator in the main window.
     */
    public void acceleratorChanged() {
        System.out.println("accelerator path: " + acceleratorFilePath);
        ArrayList<AcceleratorSeq> seqs = new ArrayList<AcceleratorSeq>();
        seqs.add(this.getAccelerator().getSequence("SCLMed"));
        seqs.add(this.getAccelerator().getSequence("SCLHigh"));
        theSeq =  new AcceleratorSeqCombo("linac", seqs);
        linacBPMs = theSeq.<BPM>getAllNodesOfType("BPM");
        // need a probe to get beam mass, charge, etc.
        
        ParticleTracker alg = null;
        try {
            alg = AlgorithmFactory.createParticleTracker( theSeq );
            
        } catch ( InstantiationException exception ) {
            System.err.println( "Instantiation exception creating tracker." );
            exception.printStackTrace();
        }
        
        theProbe = ProbeFactory.getParticleProbe("SCLMed", theSeq, alg);
        bpmController.initialize();
        
    }
    
    /**
     * Handle the selected sequence changed event by displaying the elements of the
     * selected sequence in the main window.
     */
    public void selectedSequenceChanged() {
    }
    
    /** get the Controller */
    public BPMController getBPMController() { return bpmController;}
    
    /** method to dump an error message and sound the alarm */
    public void dumpErr(String msg) {
		Toolkit.getDefaultToolkit().beep();
		myWindow().errorText.setText(msg);
		System.err.println(msg);	    
    }
}
