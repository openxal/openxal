/*
 * @(#)TimeStampDocument.java          0.9 05/21/2003
 *
 * Copyright (c) 2001-2002 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 */

package xal.app.timestamptest;

import java.text.NumberFormat;
import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.text.*;
import java.awt.event.*;
import java.awt.Color;
import javax.swing.event.*;
import javax.swing.JToggleButton.ToggleButtonModel;

import xal.extension.application.smf.*;
import xal.extension.application.*;
import xal.smf.*;
import xal.extension.widgets.plot.BasicGraphData;
import xal.tools.xml.*;
import xal.tools.apputils.files.RecentFileTracker;
import xal.tools.data.*;

/**
 * TimeStampDocument is a custom AcceleratorDocument for PV timestamp checking
 * application.
 * 
 * @version 0.9 21 May 2003
 * @author t6p
 * @author Paul Chu
 */

public class TimeStampDocument extends AcceleratorDocument {
	/**
	 * The document for the text pane in the main window.
	 */
	protected PlainDocument textDocument;

	protected CAMonitor[] cam;

	private int nPV = 0;

	ToggleButtonModel startModel = new ToggleButtonModel();

	ToggleButtonModel stopModel = new ToggleButtonModel();

	ArrayList<String> myPVList;
	
	int bufferSize, timeRange;

	private RecentFileTracker _savedFileTracker;

	/** Create a new empty document */
	public TimeStampDocument() {
		this(null);
	}

	/**
	 * Create a new document loaded from the URL file
	 * 
	 * @param url
	 *            The URL of the file to load into the new document.
	 */
	public TimeStampDocument(java.net.URL url) {
		setSource(url);
		makeTextDocument();

		_savedFileTracker = new RecentFileTracker(1, this.getClass(),
				"recent_saved_file");

		if (url == null)
			return;
	}

	/**
	 * Make a main window by instantiating the my custom window. Set the text
	 * pane to use the textDocument variable as its document.
	 */
	public void makeMainWindow() {
		mainWindow = new TimeStampWindow(this);

		if (getSource() != null) {
			XmlDataAdaptor xda = XmlDataAdaptor.adaptorForUrl(getSource(),
					false);
			DataAdaptor da1 = xda.childAdaptor("timestamptest");

			// restore previously selected PVs
			List<DataAdaptor> pvs = da1.childAdaptor("pvList").childAdaptors("pv");

			if (pvs.isEmpty())
				return;

			myPVList = new ArrayList<String>();
			Iterator<DataAdaptor> pvitr = pvs.iterator();
			while (pvitr.hasNext()) {
				DataAdaptor da = pvitr.next();
				myPVList.add(da.stringValue("name"));
			}
			
			try {
				// restore buffer size
				bufferSize = (new Integer( da1.childAdaptor("bufferSize").stringValue("size"))).intValue();
				myWindow().setBufferSize(bufferSize);
				//restore display time range
				timeRange = (new Integer( da1.childAdaptor("timeRange").stringValue("time"))).intValue();
				myWindow().setTimeRange(timeRange);
			} catch (NullPointerException e) {
				// do nothing, because we already have default buffer size set
			}
			
			myWindow().setPVList(myPVList);
			myWindow().restore();
		}
		setHasChanges(false);
	}

	/**
	 * Save the document to the specified URL.
	 * 
	 * @param url
	 *            The URL to which the document should be saved.
	 */
	public void saveDocumentAs(URL url) {

		XmlDataAdaptor xda = XmlDataAdaptor.newEmptyDocumentAdaptor();
		DataAdaptor daLevel1 = xda.createChild("timestamptest");

		// save all selected PV names
		DataAdaptor pvList = daLevel1.createChild("pvList");
		for (int i = 0; i < nPV; i++) {
			DataAdaptor pvName = pvList.createChild("pv");
			if (cam[i] != null)
				pvName.setValue("name", cam[i].getPVName());
		}
		
		// save buffer size
		DataAdaptor buffSize = daLevel1.createChild("bufferSize");
		buffSize.setValue("size", myWindow().getBufferSize());
		// save display time range
		DataAdaptor timeRange = daLevel1.createChild("timeRange");
		timeRange.setValue("time", myWindow().timeInSeconds);
		
		xda.writeToUrl(url);

		setHasChanges(false);
	}

	/**
	 * Convenience method for getting the main window cast to the proper
	 * subclass of XalWindow. This allows me to avoid casting the window every
	 * time I reference it.
	 * 
	 * @return The main window cast to its dynamic runtime class
	 */
	protected TimeStampWindow myWindow() {
		return (TimeStampWindow) mainWindow;
	}

	/**
	 * Instantiate a new PlainDocument that servers as the document for the text
	 * pane. Create a handler of text actions so we can determine if the
	 * document has changes that should be saved.
	 */
	private void makeTextDocument() {
		textDocument = new PlainDocument();
		textDocument.addDocumentListener(new DocumentListener() {
			public void changedUpdate(javax.swing.event.DocumentEvent evt) {
				setHasChanges(true);
			}

			public void removeUpdate(DocumentEvent evt) {
				setHasChanges(true);
			}

			public void insertUpdate(DocumentEvent evt) {
				setHasChanges(true);
			}
		});
	}

	public void customizeCommands(Commander commander) {

		// action for starting PV monitoring
		startModel.setEnabled(true);
		startModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				PVPanel[] pvPanels = myWindow().getPVPanels();
				nPV = pvPanels.length;

				if (cam != null && cam.length == nPV) {
					// do nothing, just simply resume the PV monitors
					for (int i = 0; i < nPV; i++) {
						cam[i].startMon();
					}
				} else {
					cam = new CAMonitor[nPV];
					myWindow().getPlotPanel().removeAllGraphData();
					for (int i = 0; i < nPV; i++) {
						cam[i] = new CAMonitor(pvPanels[i]);
						cam[i].setMaxLength(myWindow().buffSize);
						cam[i].setTSDocument(myWindow().myDocument);
						BasicGraphData bgd = cam[i].getGraphData();
//						bgd.setGraphColor(colors[i - (int) (i / colors.length)]);
						bgd.setGraphProperty("Legend", pvPanels[i].getPVName());
						myWindow().addLegendButton(pvPanels[i].getPVName(), i);
						myWindow().getPlotPanel().addGraphData(bgd);
						myWindow().setConfigYDialog(i);
						cam[i].reset();
						cam[i].startMon();
					}
					myWindow().addResetButton();
				}

				myPVList = myWindow().getPVList();
				
				stopModel.setEnabled(true);
				startModel.setEnabled(false);
			}
		});
		commander.registerModel("prn-ts", startModel);

		// action for stopping PV monitoring
		stopModel.setEnabled(false);
		stopModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				stopAllMonitors();

				startModel.setEnabled(true);
				stopModel.setEnabled(false);
			}
		});
		commander.registerModel("stop-ts", stopModel);

		Action saveDataAction = new AbstractAction() {
			static final long serialVersionUID = 0;
			
			public void actionPerformed(ActionEvent event) {
				String currentDirectory = _savedFileTracker
						.getRecentFolderPath();

				JFrame frame = new JFrame();
				JFileChooser fileChooser = new JFileChooser(currentDirectory);

				int status = fileChooser.showSaveDialog(frame);
				if (status == JFileChooser.APPROVE_OPTION) {
					_savedFileTracker.cacheURL(fileChooser.getSelectedFile());

					File file = fileChooser.getSelectedFile();
					try {
						FileWriter fileWriter = new FileWriter(file);
						NumberFormat nf = NumberFormat.getNumberInstance();
						nf.setMaximumFractionDigits(6);
						nf.setMinimumFractionDigits(6);

						double[][][] data = new double[nPV][2][cam[0].getData()[0].length];
						for (int i = 0; i < nPV; i++) {
							data[i] = cam[i].getData();
						}

						// write labels
						for (int j=0; j<nPV; j++) {
							fileWriter.write(cam[j].getPVName() + "_t \t"
									+ cam[j].getPVName() + "\t");
						}
						// next line
						fileWriter.write("\n");
						
						for (int i = 0; i < cam[0].getData()[0].length; i++) {
							for (int j = 0; j < nPV; j++) {
								fileWriter.write(nf.format(data[j][0][i])
										+ "\t" + nf.format(data[j][1][i])
										+ "\t\t");
							}
							// next line
							fileWriter.write("\n");
						}

						fileWriter.close();
					} catch (IOException ie) {
						JFrame frame1 = new JFrame();
						JOptionPane.showMessageDialog(frame1,
								"Cannot open the file" + file.getName()
										+ "for writing", "Warning!",
								JOptionPane.PLAIN_MESSAGE);

						frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

					}

					myWindow().getTextField().setForeground(Color.BLACK);
					myWindow().getTextField().setText(
							"Data saved to: " + file.getPath());
				}
			}
		};
		saveDataAction.putValue(Action.NAME, "save-data");
		commander.registerAction(saveDataAction);

		Action configAction = new AbstractAction() {
			static final long serialVersionUID = 0;
			public void actionPerformed(ActionEvent event) {
				myWindow().configDialog.setVisible(true);
			}
		};
		configAction.putValue(Action.NAME, "configuration");
		commander.registerAction(configAction);
	}

	public void stopAllMonitors() {
		for (int i = 0; i < nPV; i++) {
			cam[i].stopMon();
		}
	}

	public ArrayList<String> getPVList() {
		return myPVList;
	}

	public void acceleratorChanged() {
		if (accelerator != null) {
			StringBuffer description = new StringBuffer(
					"Selected Accelerator: " + accelerator.getId() + '\n');
			description.append("Sequences:\n");
			Iterator<AcceleratorSeq> sequenceIter = accelerator.getSequences().iterator();
			while (sequenceIter.hasNext()) {
				AcceleratorSeq sequence = sequenceIter.next();
				description.append('\t' + sequence.getId() + '\n');
			}

			setHasChanges(true);
		}
	}

	public void selectedSequenceChanged() {
		if (selectedSequence != null) {
			StringBuffer description = new StringBuffer("Selected Sequence: "
					+ selectedSequence.getId() + '\n');
			description.append("Nodes:\n");
			Iterator<AcceleratorNode> nodeIter = selectedSequence.getNodes().iterator();
			while (nodeIter.hasNext()) {
				AcceleratorNode node = nodeIter.next();
				description.append('\t' + node.getId() + '\n');
			}

			setHasChanges(true);
		}
	}

	protected CAMonitor[] getCAMonitors() {
		return cam;
	}

	public void willClose() {
		stopAllMonitors();	
		myWindow().removeAll();
	}

}
