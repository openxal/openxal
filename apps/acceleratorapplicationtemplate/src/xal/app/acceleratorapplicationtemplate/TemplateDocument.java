/*
 * TemplateDocument.java
 *
 * Created on Fri Oct 10 14:08:21 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.acceleratorapplicationtemplate;

import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;
import xal.model.probe.EnvelopeProbe;
import xal.model.xml.ParsingException;
import xal.model.xml.ProbeXmlParser;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.extension.application.smf.*;
import xal.smf.data.XMLDataManager;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.net.URL;

import javax.swing.JOptionPane;


/**
 * TemplateDocument
 *
 * @author  somebody
 */
class TemplateDocument extends AcceleratorDocument {
	/** Create a new empty document */
    public TemplateDocument() {
        this(null);
    }
    
    
    /** 
     * Create a new document loaded from the URL file 
     * @param url The URL of the file to load into the new document.
     */
    public TemplateDocument(java.net.URL url) {
        setSource(url);
    }
    
    
    /**
     * Make a main window by instantiating the my custom window.
     */
    public void makeMainWindow() {
        mainWindow = new TemplateWindow(this);
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

    
    /**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs(URL url) {
		XmlDataAdaptor xda = XmlDataAdaptor.newEmptyDocumentAdaptor();
		DataAdaptor daLevel1 = xda.createChild("AcceleratorApplicationTemplate");
		//save accelerator file
		DataAdaptor daXMLFile = daLevel1.createChild("accelerator");
		try {
			daXMLFile.setValue("xalFile", new URL(this.getAcceleratorFilePath()).getPath());
		} catch (java.net.MalformedURLException e) {
			daXMLFile.setValue("xalFile",this.getAcceleratorFilePath());
		}
		// save selected sequences
		ArrayList<String> seqs;
		if (getSelectedSequence() != null) {
			DataAdaptor daSeq = daLevel1.createChild("sequences");
			daSeq.setValue("name", getSelectedSequence().getId());
			if (getSelectedSequence().getClass() == AcceleratorSeqCombo.class) {
				AcceleratorSeqCombo asc = (AcceleratorSeqCombo) getSelectedSequence();
				seqs = (ArrayList<String>) asc.getConstituentNames();
			} else {
				seqs = new ArrayList<String>();
				seqs.add(getSelectedSequence().getId());
			}

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
    
	public void acceleratorChanged() {
		if (accelerator != null) {

			setHasChanges(true);
		}
	}

	public void selectedSequenceChanged() {
		if (selectedSequence != null) {
			System.out.println("Sequence selected: " + selectedSequence.getId());
			setHasChanges(true);
		}
	}

}




