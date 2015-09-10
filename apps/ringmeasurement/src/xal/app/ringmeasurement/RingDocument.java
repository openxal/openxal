/*
 * RingDocument.java
 *
 * Copyright (c) 2001-2005 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 * Created on February 15, 2005, 11:36 AM
 */

package xal.app.ringmeasurement;

import java.util.*;
import java.awt.event.*;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.*;
import java.io.*;

import xal.extension.application.*;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;
import xal.extension.application.smf.*;
import xal.smf.data.XMLDataManager;
import xal.smf.*;
import xal.tools.apputils.files.*;

import xal.extension.application.smf.*;
import xal.smf.impl.*;
import xal.smf.data.*;
import xal.extension.application.*;
import xal.model.*;
import xal.tools.*;
import xal.tools.beam.*;
import xal.tools.xml.*;
import xal.tools.data.*;
import xal.tools.messaging.*;
import xal.tools.xml.XmlDataAdaptor;
 import xal.model.probe.traj.*;
import xal.model.probe.*; 
import xal.model.xml.*; 
import xal.sim.scenario.*;
//import xal.tools.optimizer.*;



/**
 * 
 * @author Paul Chu
 */


 
 
 
 public class RingDocument extends AcceleratorDocument {

	 
	 
	 
	 
	// for on/off-line mode selection
	ToggleButtonModel online = new ToggleButtonModel();

	ToggleButtonModel offline = new ToggleButtonModel();

	protected boolean isOnline = true;
	
    /** for data file */
    private RecentFileTracker _inpFileTracker;
    File inpFile;
    
    protected long bpmPVLogId = 0;
    protected long defPVLogId = 0;
    
	/** Creates a new instance of RingDocument */
	public RingDocument() {
		this(null);
	}

	/**
	 * Create a new document loaded from the URL file
	 * 
	 * @param url
	 *            The URL of the file to load into the new document.
	 */
	public RingDocument(java.net.URL url) {
		setSource(url);


		
        // inp file management
        _inpFileTracker = new RecentFileTracker(1, this.getClass(), "recent_data");

		if (url == null)
			return;
	
	
	
	}

	public void makeMainWindow() {
		mainWindow = new RingWindow(this);
				
		// restore from saved document
		if (getSource() != null) {
			XmlDataAdaptor xda = XmlDataAdaptor.adaptorForUrl(getSource(),
					false);
			DataAdaptor da = xda.childAdaptor("RingMeasurement");
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
			String selectedBPM = da.childAdaptor("SelectedBPM").stringValue(
					"BPM");
			myWindow().setSelectedBPM(selectedBPM);
			// set up the Fit/FFT configuration
			/*
			 * myWindow().tunePanel.A =
			 * da.childAdaptor("TuneConfig").doubleValue("A");
			 * myWindow().tunePanel.df1.setValue(myWindow().tunePanel.A);
			 * myWindow().tunePanel.c =
			 * da.childAdaptor("TuneConfig").doubleValue("c");
			 * myWindow().tunePanel.df2.setValue(myWindow().tunePanel.c);
			 * myWindow().tunePanel.w =
			 * da.childAdaptor("TuneConfig").doubleValue("w");
			 * myWindow().tunePanel.df3.setValue(myWindow().tunePanel.w);
			 * myWindow().tunePanel.b =
			 * da.childAdaptor("TuneConfig").doubleValue("b");
			 * myWindow().tunePanel.df4.setValue(myWindow().tunePanel.b);
			 * myWindow().tunePanel.d =
			 * da.childAdaptor("TuneConfig").doubleValue("d");
			 * myWindow().tunePanel.df5.setValue(myWindow().tunePanel.d);
			 */myWindow().tunePanel.maxTime = da.childAdaptor("TuneConfig")
					.intValue("MaxTime");
			myWindow().tunePanel.df6.setValue(myWindow().tunePanel.maxTime);
			myWindow().tunePanel.len = da.childAdaptor("TuneConfig").intValue(
					"TurnNo");
			myWindow().tunePanel.df7.setValue(myWindow().tunePanel.len);
			myWindow().tunePanel.fftSize = da.childAdaptor("TuneConfig")
					.intValue("FFTSize");
			myWindow().tunePanel.fftConf.setSelectedIndex(2);
		}
	}

	/**
	 * Convenience method for getting the main window cast to the proper
	 * subclass of XalWindow. This allows me to avoid casting the window every
	 * time I reference it.
	 * 
	 * @return The main window cast to its dynamic runtime class
	 */
	private RingWindow myWindow() {
		return (RingWindow) mainWindow;
	}

	public void saveDocumentAs(java.net.URL url) {
		try {
			XmlDataAdaptor documentAdaptor = XmlDataAdaptor
					.newEmptyDocumentAdaptor();
			DataAdaptor da = documentAdaptor.createChild("RingMeasurement");
			DataAdaptor daXMLFile = da.createChild("accelerator");
			// save the selected accelerator file
			daXMLFile.setValue("xmlFile", getAcceleratorFilePath());
			// save the selected BPM name for tune measurement
			DataAdaptor selectedBPM = da.createChild("SelectedBPM");
			selectedBPM.setValue("BPM", myWindow().getSelectedBPM());
			// save Fit/FFT configuration
			DataAdaptor tuneConfig = da.createChild("TuneConfig");
			/*
			 * tuneConfig.setValue("A", myWindow().tunePanel.A);
			 * tuneConfig.setValue("c", myWindow().tunePanel.c);
			 * tuneConfig.setValue("w", myWindow().tunePanel.w);
			 * tuneConfig.setValue("b", myWindow().tunePanel.b);
			 * tuneConfig.setValue("d", myWindow().tunePanel.d);
			 */tuneConfig.setValue("MaxTime", myWindow().tunePanel.maxTime);
			tuneConfig.setValue("TurnNo", myWindow().tunePanel.len);
			tuneConfig.setValue("FFTSize", myWindow().tunePanel.fftSize);

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
		ArrayList<AcceleratorSeq> ringSeqs = new ArrayList<AcceleratorSeq>();
		List<AcceleratorSeq> ring;
		if (accelerator != null) {
			// pre-define all 5 Ring sequences as the "ring" combo sequence
			// try {
			ringSeqs.add(accelerator.getSequence("Ring1"));
			ringSeqs.add(accelerator.getSequence("Ring2"));
			ringSeqs.add(accelerator.getSequence("Ring3"));
			ringSeqs.add(accelerator.getSequence("Ring4"));
			ringSeqs.add(accelerator.getSequence("Ring5"));

			ring = AcceleratorSeq.orderSequences(ringSeqs);

			setSelectedSequence(AcceleratorSeqCombo.getInstance("ring", ring));
			if (myWindow() != null) {
				myWindow().setTunePanel(selectedSequence);
				myWindow().setDispPanel(selectedSequence);
				myWindow().repaint();
				
//				myWindow().tunePanel.connectAll();
			}
			// } catch (Exception e) {
			// System.out.println("Missing ring sequence(s)!");
			// }
		}
	}

	public void selectedSequenceChanged() {
	}
	
	public void customizeCommands(Commander commander) {
		//online action
		online.setSelected(true);
		online.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				isOnline = true;
				myWindow().getTunePanel().setAppMode(isOnline);
			}
		});
		commander.registerModel("online", online);

		//offline (PV logger) action
		offline.setSelected(false);
		offline.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				isOnline = false;
				myWindow().getTunePanel().setAppMode(isOnline);
				// select data file
	            String	currentDirectory = _inpFileTracker.getRecentFolderPath();

	            JFrame frame = new JFrame();
	            JFileChooser fileChooser= new JFileChooser(currentDirectory);
	            fileChooser.addChoosableFileFilter(new InpFileFilter());

	            int status= fileChooser.showOpenDialog(frame);
	            if (status == JFileChooser.APPROVE_OPTION) {
	                _inpFileTracker.cacheURL(fileChooser.getSelectedFile());
	                inpFile= fileChooser.getSelectedFile();
	                
	                ReadDataFile rdf = new ReadDataFile(inpFile);
	                
	                bpmPVLogId = rdf.getBPMPVLogId();
	                defPVLogId = rdf.getDefPVLogId();
	            }                               
			}
		});
		commander.registerModel("offline", offline);

		
	}



 }

class InpFileFilter extends javax.swing.filechooser.FileFilter {
    //Accept xml files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        
        String extension = Utils1.getExtension(f);
        if (extension != null) {
            if (extension.equals(Utils1.dat) ) {
                return true;
            } else {
                return false;
            }
        }
        
        return false;
    }
    
    //The description of this filter
    public String getDescription() {
        return "RingMeasurement Data File";
    }
}

class Utils1 {
    
    public final static String dat = "dat";
    
    /*
     * Get the extension of a file.
     */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        
        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
}


