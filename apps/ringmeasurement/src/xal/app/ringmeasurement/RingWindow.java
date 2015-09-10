/*
 * RingWindow.java
 *
 * Copyright (c) 2001-2005 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 * Created on February 15, 2005, 11:48 AM
 */

package xal.app.ringmeasurement;

import java.util.*;

import javax.swing.*;

import xal.extension.application.smf.*;
import xal.smf.*;
import xal.smf.impl.Quadrupole;
import xal.smf.impl.MagnetMainSupply;
import xal.smf.impl.BPM;

/**
 * 
 * @author Paul Chu
 */
public class RingWindow extends AcceleratorWindow {

	static final long serialVersionUID = 0;

	private RingDocument myDocument;

	protected JTabbedPane tabbedPane;

	protected TunePanel tunePanel;

	protected DispersionPane dispPanel;

	JPanel twissPanel = new JPanel();
	
	TwissFace twissFace = new TwissFace();
	
	JPanel miaPanel = new JPanel(); 
	
	MIALive miaLive = new MIALive();
	
	JPanel tunePane = new JPanel();

	JScrollPane energyPane = new JScrollPane();

	JScrollPane chromPane = new JScrollPane();

	JScrollPane dispPane = new JScrollPane();

	/** Creates a new instance of RingWindow */
	public RingWindow(RingDocument aDocument) {
		super(aDocument);
		myDocument = aDocument;
		setSize(1000, 900);
		makeContent();
	}

	/**
	 * Create the main window subviews.
	 */
	protected void makeContent() {
	    
	    twissPanel.add(twissFace);
		miaPanel.add(miaLive);
	    tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	    tabbedPane.addTab("Tune/Quad Corr.", tunePane);
	    tabbedPane.addTab("Twiss Measurement", twissPanel);
		tabbedPane.addTab("MIA Live", miaPanel);
	    tabbedPane.addTab("Energy", energyPane);
	    tabbedPane.addTab("Chromaticity", chromPane);
	    tabbedPane.addTab("Dispersion", dispPane);

	    getContentPane().add(tabbedPane);

	    // disable the parts we have not coded yet.
	    tabbedPane.setEnabledAt(3, false);
	    tabbedPane.setEnabledAt(4, false);
	    if (myDocument.getSelectedSequence() != null) {
		setTunePanel(myDocument.getSelectedSequence());
		setDispPanel(myDocument.getSelectedSequence());	
		//tunePanel.connectAll();
	    }
	}

	protected void setTunePanel(AcceleratorSeq seq) {
		tunePane.removeAll();
		List<AcceleratorNode> bpms = seq.getAllNodesOfType("BPM");
		bpms = AcceleratorSeq.filterNodesByStatus(bpms, true);
		Iterator<AcceleratorNode> bpmIt = bpms.iterator();
		ArrayList<BPM> bpmLst = new ArrayList<BPM>();
		while(bpmIt.hasNext()) {
			bpmLst.add((BPM)bpmIt.next());
		}
		
		List<AcceleratorNode> quads = seq.getAllNodesOfType("Q");
		
		tunePanel = new TunePanel(myDocument);
		tunePane.add(tunePanel);
		
		ArrayList<MagnetMainSupply> qPSs = new ArrayList<MagnetMainSupply>();
		HashMap<MagnetMainSupply, Double> designMap = new HashMap<MagnetMainSupply, Double>();
		
		Iterator<AcceleratorNode> it = quads.iterator();
		while (it.hasNext()) {
			Quadrupole quad = (Quadrupole) it.next();
			MagnetMainSupply mps = quad.getMainSupply();
			if (!qPSs.contains(mps)) {
				qPSs.add(mps);
				designMap.put(mps, new Double(quad.getDesignField()));
			}
		}
		
//		tunePanel.connectAll();

		tunePanel.initTables(bpmLst, qPSs, designMap);
		
//		tunePanel.connectAll();
	}

	protected void setDispPanel(AcceleratorSeq seq) {
		dispPane.removeAll();
		List<BPM> bpms = seq.getAllNodesOfType("BPM");
		dispPanel = new DispersionPane(bpms, myDocument);
		dispPane.add(dispPanel);
	}

	protected String getSelectedBPM() {
		return tunePanel.getSelectedBPM();
	}

	protected void setSelectedBPM(String BPM) {
		tunePanel.setSelectedBPM(BPM);
	}
	
	protected TunePanel getTunePanel() {
		return tunePanel;
	}
	
}
