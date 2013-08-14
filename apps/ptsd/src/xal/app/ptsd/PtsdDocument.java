/*
 * Document.java
 *
 * Created on July, 2008
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 * 
 * Christopher K. Allen
 */

package xal.app.ptsd;

import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.application.AcceleratorDocument;
import xal.smf.data.XMLDataManager;
import xal.smf.AcceleratorSeq;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;


/**
 * Document component for the application <code>PTSD</code>.  Maintains and 
 * manages all the
 * data for the application.
 *
 * @author  Christopher K. Allen
 */
class PtsdDocument extends AcceleratorDocument {

    
    /*
     * Local Attributes
     */
    
    /** Consolidated BPM data source */
    private BpmDataManager                mgrBpmData;
    
    

    
    
    /*
     * Initialization
     */
    
    
    /** 
     * Create a new, initialized document component for the <code>PTSD</code>
     * application.
     */
    public PtsdDocument() {
        this(null);
    }


    /**
     * <p> 
     * Create a new document component with data loaded 
     * from the file identified by the given URL.
     * </p>
     * 
     * <p>
     * Currently this implementation does nothing but set
     * the name of the application to that of the file
     * with the given URL.
     * </p>
     *  
     * @param url The URL of the file to load into the new document.
     */
    public PtsdDocument(java.net.URL url) {
        setSource(url);
        
        //
        // Do initialization stuff here
        //
    }


    /**
     * <p>
     * Create and display the main window of this document component.  
     * The main window maintains all the views on the document data and
     * consequently, behaves as a controller component for the application.
     * </p>
     */
    @Override
    public void makeMainWindow() {
        mainWindow = new PtsdWinMain(this);
        if (getSource() != null) {
            XmlDataAdaptor xda = XmlDataAdaptor.adaptorForUrl(getSource(),
                            false);
            DataAdaptor da1 = xda.childAdaptor("AcceleratorApplicationTemplate");

            //restore accelerator file
            this.setAcceleratorFilePath(da1.childAdaptor("accelerator")
                            .stringValue("xalFile"));

            String accelUrl = this.getAcceleratorFilePath();
            try {
                this.setAccelerator(XMLDataManager.acceleratorWithPath(accelUrl), this
                                .getAcceleratorFilePath());
            } catch (Exception exception) {
                JOptionPane
                .showMessageDialog(
                                null,
                                "Hey - I had trouble parsing the accelerator input xml file you fed me",
                                "AOC error", JOptionPane.ERROR_MESSAGE);
            }
            this.acceleratorChanged();

            // set up the right sequence combo from selected primaries:
            List<DataAdaptor> temp = da1.childAdaptors("sequences");
            if (temp.isEmpty())
                return; // bail out, nothing left to do

            ArrayList<AcceleratorSeq> seqs = new ArrayList<AcceleratorSeq>();
            DataAdaptor da2a = da1.childAdaptor("sequences");
            String seqName = da2a.stringValue("name");

            temp = da2a.childAdaptors("seq");
            Iterator<DataAdaptor> itr = temp.iterator();
            while (itr.hasNext()) {
                DataAdaptor da = itr.next();
                seqs.add(getAccelerator().getSequence(da.stringValue("name")));
            }
            setSelectedSequence(new AcceleratorSeqCombo(seqName, seqs));

        }
        setHasChanges(false);
    }


    
    /*
     * Attribute Query
     */

    
    /**
     * Return the BPM data source object. This is the
     * main data object of the <code>PTSD</code> 
     * application.
     *
     * @return The BPM data source object.
     *
     * @since  Jul 29, 2008
     * @author Christopher K. Allen
     */
    public BpmDataManager getDataSource() {
        return this.mgrBpmData;
    }


    
    /*
     * Operations
     */
    

    /**
     * <p>
     * Save the document to the specified URL.
     * </p>
     * <p>
     * The current implementation is just a shell which writes out
     * the selected sequence (or comb-sequence) to the given URL.
     * </p> 
     * 
     * @param url       URL of the file where document da
     */
    @Override
    public void saveDocumentAs(URL url) {
        
        XmlDataAdaptor xda = XmlDataAdaptor.newEmptyDocumentAdaptor();
        DataAdaptor daLevel1 = xda.createChild("PtsdApplicationData");
        
        //save accelerator file
        DataAdaptor daXMLFile = daLevel1.createChild("accelerator");
        try {
            daXMLFile.setValue("xalFile", new URL(this.getAcceleratorFilePath()).getPath());
            
        } catch (java.net.MalformedURLException e) {
            daXMLFile.setValue("xalFile",this.getAcceleratorFilePath());
            
        }
        
        // save selected sequences
        List<String>            seqs;
        if (getSelectedSequence() != null) {
            DataAdaptor daSeq = daLevel1.createChild("sequences");
            daSeq.setValue("name", getSelectedSequence().getId());
            
            // Check for combo-sequenc and get their IDs 
            if (getSelectedSequence().getClass() == AcceleratorSeqCombo.class) {
                AcceleratorSeqCombo asc = (AcceleratorSeqCombo) getSelectedSequence();
                seqs = asc.getConstituentNames();
                
            } else {
                seqs = new ArrayList<String>();
                seqs.add(getSelectedSequence().getId());
                
            }

            // Write out the child combo-sequence IDs (if they exist)
            Iterator<String> itr = seqs.iterator();
            while (itr.hasNext()) {
                DataAdaptor daSeqComponents = daSeq.createChild("seq");
                daSeqComponents.setValue("name", itr.next());
            }
        }

        // write to the document file
        xda.writeToUrl(url);
        setHasChanges(false);
    }

    
    
    /*
     * Standard Event Hooks
     */
    
    
    /**
     * <p>
     * Currently this method is not built up since we do not expect the
     * accelerator to change much.
     * </p>
     *
     * @since 	Jul 23, 2008
     * @author  Christopher K. Allen
     *
     * @see gov.sns.xal.smf.application.AcceleratorDocument#acceleratorChanged()
     */
    @Override
    public void acceleratorChanged() {
        if (accelerator != null) {
            String      strMsg = "New accelerator selected: " + accelerator.getId();
            
            System.out.println(strMsg);
            PtsdMain.getAppLogger().info(strMsg);
            setHasChanges(true);
        }
    }

    /**
     *
     * @since 	Jul 23, 2008
     * @author  Christopher K. Allen
     *
     * @see gov.sns.xal.smf.application.AcceleratorDocument#selectedSequenceChanged()
     */
    @Override
    public void selectedSequenceChanged() {
        
        // Check for valid sequence
        if (selectedSequence != null) {
            String      strMsg = "New sequence selected: " + selectedSequence.getId();
            
            System.out.println(strMsg);
            PtsdMain.getAppLogger().info(strMsg);
            setHasChanges(true);
            
            this.mgrBpmData = new BpmDataManager(this.selectedSequence);
            
        } else {
            String      strMsg = "Invalid sequence selected";
            
            System.out.println(strMsg);
            PtsdMain.getAppLogger().warning(strMsg);
            return;
            
        }
    }

}




