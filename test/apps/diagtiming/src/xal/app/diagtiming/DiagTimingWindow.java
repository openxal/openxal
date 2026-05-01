package xal.app.diagtiming;

import javax.swing.*;

import xal.ca.Channel;
import xal.extension.application.smf.*;
import xal.smf.AcceleratorNode;
import xal.smf.impl.BPM;
import xal.smf.impl.RingBPM;
import xal.smf.impl.WireScanner;
import xal.smf.impl.CurrentMonitor;


public class DiagTimingWindow extends AcceleratorWindow {
	static final long serialVersionUID = 0;

	private DiagTimingDocument myDocument;

	protected JTabbedPane tabbedPane;

	BPMPane lbpmPane = new BPMPane(0);

	BPMPane rbpmPane = new BPMPane(1);
	
	BPMPane rtbtPane = new BPMPane(2);

	JPanel wsPane = new JPanel();
	
	BCMPane bcmPane = new BCMPane();

	boolean windowSet = false;

	/** Creates a new instance of RingWindow */
	public DiagTimingWindow(DiagTimingDocument aDocument) {
		super(aDocument);
		myDocument = aDocument;
		setSize(1280, 700);
		makeContent();

		if (myDocument.getAccelerator() != null) {
			if (myDocument.getLinacBPMs().size() > 0) {
				createLBPMPane(myDocument.getLinacBPMs());
				lbpmPane.connectAll();
			}
			if (myDocument.getRingBPMs().size() > 0) {
				createRBPMPane(myDocument.getRingBPMs());
				rbpmPane.connectAll();
			}
			if (myDocument.getRTBTBPMs().size() > 0) {
				createRTBTBPMPane(myDocument.getRTBTBPMs());
				rtbtPane.connectAll();
			}
			if (myDocument.getBCMs().size() > 0) {
				createBCMPane(myDocument.getBCMs());
				bcmPane.connectAll();
			}
			Channel.flushIO();
		}

	}

	/**
	 * Create the main window subviews.
	 */
	protected void makeContent() {

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addTab("Linac BPMs", lbpmPane);
		tabbedPane.addTab("Ring BPMs", rbpmPane);
		tabbedPane.addTab("RTBT BPMs", rtbtPane);
		tabbedPane.addTab("Wire Scanners", wsPane);
		tabbedPane.addTab("BCMs", bcmPane);

		getContentPane().add(tabbedPane);

		// disable the parts we have not coded yet.
		// tabbedPane.setEnabledAt(1, false);
		tabbedPane.setEnabledAt(3, false);

//		if (myDocument.getAccelerator() != null)
//		{
//			createLBPMPane(myDocument.lbpms);
//			lbpmPane.connectAll();		
			
//			createRBPMPane(myDocument.rbpms);
//		}
	}

	protected void createLBPMPane(java.util.List<AcceleratorNode> nodes) {
		lbpmPane.initializeBPMPane(nodes);
	}

	protected void createRBPMPane(java.util.List<AcceleratorNode> nodes) {
		rbpmPane.initializeBPMPane(nodes);
	}
	
	protected void createRTBTBPMPane(java.util.List<AcceleratorNode> nodes) {
		rtbtPane.initializeBPMPane(nodes);
	}
	
	protected void createWSPane(java.util.List<WireScanner> nodes) {
		
	}
	
	protected void createBCMPane(java.util.List<CurrentMonitor> nodes) {
		bcmPane.initializeBCMPane(nodes);
	}

	protected DiagTimingDocument getDocument() {
		return myDocument;
	}

}
