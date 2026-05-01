package xal.app.diagtiming;

import java.util.*;

import xal.ca.Channel;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;
import xal.extension.application.smf.*;
import xal.smf.data.XMLDataManager;
import xal.smf.impl.qualify.*;
import xal.smf.*;
import xal.smf.AcceleratorNode;
import xal.smf.impl.BPM;
import xal.smf.impl.RingBPM;
import xal.smf.impl.WireScanner;
import xal.smf.impl.CurrentMonitor;


public class DiagTimingDocument extends AcceleratorDocument {

	List<AcceleratorNode> lbpms;

	List<AcceleratorNode> rbpms;
	
	List<AcceleratorNode> rtbtBpms;

	List<WireScanner> wss;
	
	List<CurrentMonitor> bcms;

	/** Creates a new instance of RingDocument */
	public DiagTimingDocument() {
		this(null);
	}

	/**
	 * Create a new document loaded from the URL file
	 * 
	 * @param url
	 *            The URL of the file to load into the new document.
	 */
	public DiagTimingDocument(java.net.URL url) {
		setSource(url);

		if (url == null)
			return;
	}

	public void makeMainWindow() {
		mainWindow = new DiagTimingWindow(this);
		// restore from saved document
		if (getSource() != null) {
			XmlDataAdaptor xda = XmlDataAdaptor.adaptorForUrl(getSource(),
					false);
			DataAdaptor da = xda.childAdaptor("DiagTiming");
			// get the accelerator file
			String acceleratorPath = da.childAdaptor("accelerator")
					.stringValue("xmlFile");
			if (acceleratorPath.length() > 0) {
				this.setAcceleratorFilePath(acceleratorPath);
				System.out.println("accelFile = "
						+ this.getAcceleratorFilePath());
				String accelUrl = "file://" + this.getAcceleratorFilePath();
				try {
					XMLDataManager dMgr = new XMLDataManager(accelUrl);
					this.setAccelerator(dMgr.getAccelerator(), this
							.getAcceleratorFilePath());
				} catch (Exception exception) {
					System.err.println(exception.getMessage());
					exception.printStackTrace();
				}
				this.acceleratorChanged();
			}
			// set up the selected BPM
			// String selectedBPM = da.childAdaptor("SelectedBPM").stringValue(
			// "BPM");
			/*
			 * myWindow().setSelectedBPM(selectedBPM); // set up the Fit/FFT
			 * configuration myWindow().tunePanel.maxTime =
			 * da.childAdaptor("TuneConfig").intValue("MaxTime");
			 * myWindow().tunePanel.df6.setValue(myWindow().tunePanel.maxTime);
			 * myWindow().tunePanel.len =
			 * da.childAdaptor("TuneConfig").intValue("TurnNo");
			 * myWindow().tunePanel.df7.setValue(myWindow().tunePanel.len);
			 * myWindow().tunePanel.fftSize =
			 * da.childAdaptor("TuneConfig").intValue("FFTSize");
			 * myWindow().tunePanel.fftConf.setSelectedIndex(2);
			 */}
	}

	/**
	 * Convenience method for getting the main window cast to the proper
	 * subclass of XalWindow. This allows me to avoid casting the window every
	 * time I reference it.
	 * 
	 * @return The main window cast to its dynamic runtime class
	 */
	private DiagTimingWindow myWindow() {
		return (DiagTimingWindow) mainWindow;
	}

	public void saveDocumentAs(java.net.URL url) {
		try {
			XmlDataAdaptor documentAdaptor = XmlDataAdaptor
					.newEmptyDocumentAdaptor();
			
			  DataAdaptor da = documentAdaptor.createChild("DiagTiming");
			  DataAdaptor daXMLFile = da.createChild("accelerator"); 
			  // save the selected accelerator file 
			  daXMLFile.setValue("xmlFile", getAcceleratorFilePath()); 
			  // save the selected BPM name for tune measurement 
			  DataAdaptor lbpm = da.createChild("Linac");
			  DataAdaptor rbpm = da.createChild("Ring");
			  DataAdaptor rtbpm = da.createChild("RTBT");
			  
			  // for all Linac BPMs
			  for (int i=0; i<myWindow().lbpmPane.bpmNames.length; i++) {
				  DataAdaptor bpm = lbpm.createChild("BPM");
				  bpm.setValue("trigDelay", myWindow().lbpmPane.bpmTableModel.getValueAt(i, 1));
				  bpm.setValue("noOfPls", myWindow().lbpmPane.bpmTableModel.getValueAt(i, 2));
				  bpm.setValue("BPM_width", myWindow().lbpmPane.bpmTableModel.getValueAt(i, 3));
				  bpm.setValue("samp_pts", myWindow().lbpmPane.bpmTableModel.getValueAt(i, 4));				  
			  }
			  
			  // for all Ring BPMs
			  for (int i=0; i<myWindow().rbpmPane.bpmNames.length; i++) {
				  DataAdaptor bpm = rbpm.createChild("BPM");
				  bpm.setValue("trigDelay", myWindow().rbpmPane.bpmTableModel.getValueAt(i, 1).toString());
				  bpm.setValue("trigEvt", myWindow().rbpmPane.bpmTableModel.getValueAt(i, 2).toString());
				  bpm.setValue("s1Trns", myWindow().rbpmPane.bpmTableModel.getValueAt(i, 3).toString());
				  bpm.setValue("s1Gain", myWindow().rbpmPane.bpmTableModel.getValueAt(i, 4).toString());				  
				  bpm.setValue("s1Mthd", myWindow().rbpmPane.bpmTableModel.getValueAt(i, 5).toString());				  
				  bpm.setValue("s2Trns", myWindow().rbpmPane.bpmTableModel.getValueAt(i, 6).toString());
				  bpm.setValue("s2Gain", myWindow().rbpmPane.bpmTableModel.getValueAt(i, 7).toString());				  
				  bpm.setValue("s2Mthd", myWindow().rbpmPane.bpmTableModel.getValueAt(i, 8));				  
				  bpm.setValue("s3Trns", myWindow().rbpmPane.bpmTableModel.getValueAt(i, 9).toString());
				  bpm.setValue("s3Gain", myWindow().rbpmPane.bpmTableModel.getValueAt(i, 10).toString());				  
				  bpm.setValue("s3Mthd", myWindow().rbpmPane.bpmTableModel.getValueAt(i, 11).toString());				  
				  bpm.setValue("s4Trns", myWindow().rbpmPane.bpmTableModel.getValueAt(i, 12).toString());
				  bpm.setValue("s4Gain", myWindow().rbpmPane.bpmTableModel.getValueAt(i, 13).toString());				  
				  bpm.setValue("s4Mthd", myWindow().rbpmPane.bpmTableModel.getValueAt(i, 14).toString());				  
				  bpm.setValue("freqMode", myWindow().rbpmPane.bpmTableModel.getValueAt(i, 15).toString());				  
				  bpm.setValue("directFreq", myWindow().rbpmPane.bpmTableModel.getValueAt(i, 16).toString());				  
				  bpm.setValue("directBeta", myWindow().rbpmPane.bpmTableModel.getValueAt(i, 17).toString());				  
				  bpm.setValue("firstTrnDly", myWindow().rbpmPane.bpmTableModel.getValueAt(i, 18).toString());				  
			  }
			 
			  // for all RTBT BPMs
			  for (int i=0; i<myWindow().rtbtPane.bpmNames.length; i++) {
				  DataAdaptor bpm = rtbpm.createChild("BPM");
				  bpm.setValue("trigDelay", myWindow().rtbtPane.bpmTableModel.getValueAt(i, 1).toString());
				  bpm.setValue("trigEvt", myWindow().rtbtPane.bpmTableModel.getValueAt(i, 2));
				  bpm.setValue("s1Trns", myWindow().rtbtPane.bpmTableModel.getValueAt(i, 3).toString());
				  bpm.setValue("s1Gain", myWindow().rtbtPane.bpmTableModel.getValueAt(i, 4));				  
				  bpm.setValue("s1Mthd", myWindow().rtbtPane.bpmTableModel.getValueAt(i, 5));				  
				  bpm.setValue("s2Trns", myWindow().rtbtPane.bpmTableModel.getValueAt(i, 6).toString());
				  bpm.setValue("s2Gain", myWindow().rtbtPane.bpmTableModel.getValueAt(i, 7));				  
				  bpm.setValue("s2Mthd", myWindow().rtbtPane.bpmTableModel.getValueAt(i, 8));				  
				  bpm.setValue("s3Trns", myWindow().rtbtPane.bpmTableModel.getValueAt(i, 9).toString());
				  bpm.setValue("s3Gain", myWindow().rtbtPane.bpmTableModel.getValueAt(i, 10));				  
				  bpm.setValue("s3Mthd", myWindow().rtbtPane.bpmTableModel.getValueAt(i, 11));				  
				  bpm.setValue("s4Trns", myWindow().rtbtPane.bpmTableModel.getValueAt(i, 12).toString());
				  bpm.setValue("s4Gain", myWindow().rtbtPane.bpmTableModel.getValueAt(i, 13));				  
				  bpm.setValue("s4Mthd", myWindow().rtbtPane.bpmTableModel.getValueAt(i, 14));				  
				  bpm.setValue("freqMode", myWindow().rtbtPane.bpmTableModel.getValueAt(i, 15));				  
				  bpm.setValue("directFreq", myWindow().rtbtPane.bpmTableModel.getValueAt(i, 16).toString());				  
				  bpm.setValue("directBeta", myWindow().rtbtPane.bpmTableModel.getValueAt(i, 17).toString());				  
				  bpm.setValue("firstTrnDly", myWindow().rtbtPane.bpmTableModel.getValueAt(i, 18).toString());				  
			  }
			  
			documentAdaptor.writeToUrl(url);
			setHasChanges(false);
		} catch (XmlDataAdaptor.WriteException exception) {
			exception.printStackTrace();
			displayError("Save Failed!",
					"Save failed due to an internal write exception!",
					exception);
		} catch (Exception exception) {
			exception.printStackTrace();
			displayError("Save Failed!",
					"Save failed due to an internal exception!", exception);
		}
	}

	public void acceleratorChanged() {
		if (accelerator != null) {
			// parepare a combo sequence for the entire linac
			// ArrayList linacSeqs = new ArrayList();
			// List linac;
			// try {
			/*
			 * linacSeqs.add(accelerator.getSequence("MEBT"));
			 * linacSeqs.add(accelerator.getSequence("DTL1"));
			 * linacSeqs.add(accelerator.getSequence("DTL2"));
			 * linacSeqs.add(accelerator.getSequence("DTL3"));
			 * linacSeqs.add(accelerator.getSequence("DTL4"));
			 * linacSeqs.add(accelerator.getSequence("DTL5"));
			 * linacSeqs.add(accelerator.getSequence("DTL6"));
			 * linacSeqs.add(accelerator.getSequence("CCL1"));
			 * linacSeqs.add(accelerator.getSequence("CCL2"));
			 * linacSeqs.add(accelerator.getSequence("CCL3"));
			 * linacSeqs.add(accelerator.getSequence("CCL4"));
			 * linacSeqs.add(accelerator.getSequence("SCLMed"));
			 * linacSeqs.add(accelerator.getSequence("SCLHigh"));
			 * linacSeqs.add(accelerator.getSequence("HEBT1"));
			 * linacSeqs.add(accelerator.getSequence("LDmp"));
			 * linacSeqs.add(accelerator.getSequence("HEBT2"));
			 * linacSeqs.add(accelerator.getSequence("IDmp-"));
			 */
			// List linac = AcceleratorSeq.orderSequences(linacSeqs);
			// AcceleratorSeqCombo linacs = AcceleratorSeqCombo.getInstance(
			// "linac", linac);
			AndTypeQualifier typeQualifier = new AndTypeQualifier();
			NotTypeQualifier nTypeQualifier = new NotTypeQualifier("RBPM");
			typeQualifier.and("BPM");
			typeQualifier.and(nTypeQualifier);
			typeQualifier.and(QualifierFactory.getStatusQualifier(true));
			lbpms = accelerator.getAllNodesWithQualifier(typeQualifier);

			// lbpms = linacs.getAllNodesOfType("BPM");

			// } catch (Exception e) {
			// System.out.println(e);
			// System.out.println("Missing sequence(s)!");
			// }

			// prepare a combo sequence for the ring
			ArrayList<AcceleratorSeq> ringSeqs = new ArrayList<AcceleratorSeq>();
			List<AcceleratorSeq> ring;
			// try {
			// ringSeqs.add(accelerator.getSequence("IDmp+"));
			ringSeqs.add(accelerator.getSequence("Ring1"));
			ringSeqs.add(accelerator.getSequence("Ring2"));
			ringSeqs.add(accelerator.getSequence("Ring3"));
			ringSeqs.add(accelerator.getSequence("Ring4"));
			ringSeqs.add(accelerator.getSequence("Ring5"));
			// ringSeqs.add(accelerator.getSequence("RTBT1"));
			// ringSeqs.add(accelerator.getSequence("RTBT2"));
			// ringSeqs.add(accelerator.getSequence("EDmp"));

			// if (ringSeqs != null) {
			ring = AcceleratorSeq.orderSequences(ringSeqs);
			AcceleratorSeqCombo rings = AcceleratorSeqCombo.getInstance(
					"ringType", ring);
			AndTypeQualifier typeQualifier1 = new AndTypeQualifier();
			typeQualifier1.and("RBPM");
			typeQualifier1.and(QualifierFactory.getStatusQualifier(true));

			rbpms = rings.getAllNodesWithQualifier(typeQualifier1);
			
			// for RTBT BPMs
			ArrayList<AcceleratorSeq> rtbtSeqs = new ArrayList<AcceleratorSeq>();
			List<AcceleratorSeq> rtbt;
			rtbtSeqs.add(accelerator.getSequence("RTBT1"));
			rtbtSeqs.add(accelerator.getSequence("RTBT2"));
			rtbt = AcceleratorSeq.orderSequences(rtbtSeqs);
			AcceleratorSeqCombo rtbts = AcceleratorSeqCombo.getInstance(
					"rtbtType", rtbt);
			
			rtbtBpms = rtbts.getAllNodesWithQualifier(typeQualifier1);

			wss = accelerator.getAllNodesOfType("WS");
			
			bcms = accelerator.getAllNodesOfType("BCM");

			if (myWindow() != null) {
				myWindow().createLBPMPane(lbpms);
				myWindow().lbpmPane.connectAll();

				myWindow().createRBPMPane(rbpms);
				myWindow().rbpmPane.connectAll();
				
				myWindow().createRTBTBPMPane(rtbtBpms);
				myWindow().rtbtPane.connectAll();

				myWindow().createWSPane(wss);
				
				myWindow().createBCMPane(bcms);
				myWindow().bcmPane.connectAll();
				
				Channel.flushIO();
			}

			// } catch (Exception e) {
			// System.out.println(e);
			// System.out.println("Missing sequence(s)!");
			// } finally {
			// don't do anything
			// }

		}
	}

	public void selectedSequenceChanged() {
	}

	protected List<AcceleratorNode> getLinacBPMs() {
		return lbpms;
	}

	protected List<AcceleratorNode> getRingBPMs() {
		return rbpms;
	}

	protected List<AcceleratorNode> getRTBTBPMs() {
		return rtbtBpms;
	}
	
	protected List<CurrentMonitor> getBCMs() {
		return bcms;
	}

	protected List<WireScanner> getRingWSs() {
		return wss;
	}
}
