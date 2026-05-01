/*
 * @(#)GenDocument.java          0.1 7/7/11
 *
 * Copyright (c) 2001-2002 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 */

package xal.app.beam_matcher;

import java.util.*;
import javax.swing.text.*;
import java.util.HashMap;

import xal.extension.application.smf.*;
import xal.smf.*;
import xal.smf.data.*;
import xal.extension.application.*;
import xal.model.*;
import xal.model.alg.*;
import xal.model.probe.EnvelopeProbe;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.*;
import xal.tools.xml.*;
import xal.tools.data.*;

/**
 * GenDocument is a custom XALDocument for loss viewing application
 *
 * @version   0.1 12/1/2003
 * @author  cp3
 * @author  Sarah Cousineau
 */

public class GenDocument extends AcceleratorDocument{
    private static final String STR_ID = ("HEBT1");
    private Accelerator     accl;
    private Scenario		mWSD;
    private AcceleratorSeqCombo   seq;
    private ArrayList<AcceleratorSeq> seqlist = new ArrayList<AcceleratorSeq>();
    Scenario model;
    
    
    /**
     * The document for the text pane in the main window.
     */
    protected PlainDocument textDocument;
    
    protected Lattice lattice = null;
    
    //public HashMap allagentsmap = new HashMap();
    
    
    //Unused
    //public HashMap masterDataMap = new HashMap();
    //public HashMap resultMap = new HashMap();
    
    /** the name of the xml file containing the accelerator */
    protected String theProbeFile;
    
    /** Create a new empty document */
    public GenDocument() {
        this(null);
        init();
        //this.seq = accl.getSequence(STR_ID);
        this.seqlist.add(accl.getSequence("SCLHigh"));
        this.seqlist.add(accl.getSequence("HEBT1"));
        this.seq = new AcceleratorSeqCombo("HEBTCombo", seqlist);
        
        //EnvTrackerAdapt etracker = new EnvTrackerAdapt();
        
        IAlgorithm etracker = null;
        
        try {
            
            etracker = AlgorithmFactory.createEnvTrackerAdapt( seq );
            
        } catch ( InstantiationException exception ) {
            System.err.println( "Instantiation exception creating tracker." );
            exception.printStackTrace();
        }
        
        EnvelopeProbe probe = ProbeFactory.getEnvelopeProbe(STR_ID, seq, etracker);
        
        
        
        
        try {
            model = Scenario.newScenarioFor(seq);
            model.setProbe(probe);
            
            model.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
        } catch (ModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    private static GenDocument onlyOnce = null;
    
    public static GenDocument getInstance() {
        if(GenDocument.onlyOnce == null) {
            onlyOnce = new GenDocument();
            
            return onlyOnce;
            
        }else {
            return onlyOnce;
        }
    }
    
    /**
     * Create a new document loaded from the URL file
     * @param url The URL of the file to load into the new document.
     */
    public  GenDocument(java.net.URL url) {
        accl = XMLDataManager.loadDefaultAccelerator();
        //		accl = getAccelerator();
        
        
        setSource(url);
        if ( url != null ) {
            try {
                System.out.println("Opening document: " + url.toString());
                DataAdaptor documentAdaptor =
                XmlDataAdaptor.adaptorForUrl(url, false);
                update(documentAdaptor.childAdaptor("GenDocument"));
                setHasChanges(false);
                
                
                //				this.accl = XMLDataManager.loadDefaultAccelerator();
                //
                //				if (this.accl == null)
                //					throw new RuntimeException("The default accelerator did not load");
                //
                
                
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
     * @url The URL to which the document should be saved.
     */
    public void saveDocumentAs(java.net.URL url) {
        try {
            XmlDataAdaptor documentAdaptor = XmlDataAdaptor.newEmptyDocumentAdaptor();
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
     * getAccelerator() gets the accelerator object generated in the
     * GenDocument class.
     * @return Accelerator accl
     */
    
    public Accelerator getAccelerator() {
        return accl;
    }
    /**
     * getModel() gets the model object generated in the
     * GenDocument class.
     * @return Scenario mWSD
     */
    public Scenario getModel() {
        return model;
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
    
    //Begin declarations and methods specific to the application
    /*public DataTable masterdatatable;
     public DataTable resultsdatatable;
     public HashMap masterpvloggermap;
     public Integer currentpvloggerid;
     */
    public void init(){
        
        ArrayList<DataAttribute> attributes = new ArrayList<DataAttribute>();
        //attributes.add(new DataAttribute("file", String.class, true) );
        attributes.add(new DataAttribute("file", String.class, true));
        //	masterdatatable = new DataTable("DataTable", attributes);
        //	resultsdatatable = new DataTable("ResultsTable", attributes);
        //	masterpvloggermap = new HashMap();
    }
    
    
}
