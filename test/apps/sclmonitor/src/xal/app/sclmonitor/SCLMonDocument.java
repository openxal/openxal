/*
 * SCLMonDocument.java
 *
 * Created on Fri Oct 10 14:08:21 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.sclmonitor;

import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.AcceleratorNode;
import xal.extension.application.smf.*;
import xal.smf.data.XMLDataManager;
import xal.extension.application.*;
import xal.service.pvlogger.*;
import xal.tools.database.*;
import xal.service.pvlogger.apputils.browser.PVLogSnapshotChooser;
import xal.ca.*;
import xal.smf.impl.SCLCavity;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Date;

import java.awt.event.ActionEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import javax.swing.JOptionPane;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JDialog;

/**
 * SCLMonDocument
 * 
 * @author somebody
 */
class SCLMonDocument extends AcceleratorDocument {

	List<SCLCavity> rfCavs = null;

	// private PVLoggerForm pvlogger;

	private MachineSnapshot snapshot;

	private PVLogger pvLogger;
	
	protected Date startTime;

	private PVLogSnapshotChooser plsc;

	private JDialog pvLogSelector;

	private long pvLoggerID = 0;

	protected Action snapAction;

	int[] hom0States, hom1States;

	Channel[] ch1, ch2;

	boolean finished1 = false;
	boolean finished2 = false;
	boolean finished3 = false;

	/** Create a new empty document */
	public SCLMonDocument() {
		this(null);

	}

	/**
	 * Create a new document loaded from the URL file
	 * 
	 * @param url
	 *            The URL of the file to load into the new document.
	 */
	public SCLMonDocument(java.net.URL url) {
		setSource(url);
	}

	/**
	 * Make a main window by instantiating the my custom window.
	 */
	public void makeMainWindow() {
		mainWindow = new SCLMonWindow(this);

		// for PV Logger snapshot chooser
		plsc = new PVLogSnapshotChooser(mainWindow);
		plsc.setGroup("SCL HOM");

		if (getSource() != null) {
			XmlDataAdaptor xda = XmlDataAdaptor.adaptorForUrl(getSource(),
					false);
			DataAdaptor da1 = xda.childAdaptor("AcceleratorApplicationSCLMon");

			// restore accelerator file
			this.setAcceleratorFilePath(da1.childAdaptor("accelerator")
					.stringValue("xalFile"));

			String accelUrl = this.getAcceleratorFilePath();
			try {
				this.setAccelerator(XMLDataManager
						.acceleratorWithPath(accelUrl), this
						.getAcceleratorFilePath());
			} catch (Exception exception) {
				JOptionPane
						.showMessageDialog(
								null,
								"Hey - I had trouble parsing the accelerator input xml file you fed me",
								"AOC error", JOptionPane.ERROR_MESSAGE);
			}
			this.acceleratorChanged();

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
		DataAdaptor daLevel1 = xda.createChild("SCLMon");
		// save accelerator file
		DataAdaptor daXMLFile = daLevel1.createChild("accelerator");
		try {
			daXMLFile.setValue("xalFile",
					new URL(this.getAcceleratorFilePath()).getPath());
		} catch (java.net.MalformedURLException e) {
			daXMLFile.setValue("xalFile", this.getAcceleratorFilePath());
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
		ArrayList<AcceleratorSeq> sclSeqs = new ArrayList<AcceleratorSeq>();
		List<AcceleratorSeq> scl;
		if (accelerator != null) {
			sclSeqs.add(accelerator.getSequence("SCLMed"));
			sclSeqs.add(accelerator.getSequence("SCLHigh"));

			scl = AcceleratorSeq.orderSequences(sclSeqs);

			setSelectedSequence(AcceleratorSeqCombo.getInstance("scl", scl));

			rfCavs = getSelectedSequence().getAllNodesOfType("SCLCavity");

			if (myWindow() != null)
				myWindow().createRFPane();

			// setHasChanges(true);

			if (snapAction != null)
				snapAction.setEnabled(true);
		}
		
		// initialize PVLogger
		ConnectionDictionary dict = PVLogger.newLoggingConnectionDictionary();
		if (dict != null) {
			pvLogger = new PVLogger(dict);
		} else {
			ConnectionPreferenceController.displayPathPreferenceSelector();
			dict = PVLogger.newLoggingConnectionDictionary();
			pvLogger = new PVLogger(dict);
		}

	}

	protected SCLMonWindow myWindow() {
		return (SCLMonWindow) mainWindow;
	}

	private synchronized void saveHOMStates() {
		ch1 = new Channel[rfCavs.size()];
		ch2 = new Channel[rfCavs.size()];
		hom0States = new int[rfCavs.size()];
		hom1States = new int[rfCavs.size()];

		ChannelFactory cf = ChannelFactory.defaultFactory();

		Iterator<SCLCavity> it = rfCavs.iterator();
		int i = 0;

		while (it.hasNext()) {
			// get HOM monitor states
			String cavName = ((AcceleratorNode) it.next()).getId();
			String chName0 = cavName.replaceAll(
					"RF:Cav", "LLRF:HPM").concat(":HBADC0_Ctl");
			String chName1 = cavName.replaceAll(
					"RF:Cav", "LLRF:HPM").concat(":HBADC1_Ctl");
			ch1[i] = cf.getChannel(chName0);
			ch2[i] = cf.getChannel(chName1);
			
			try {
				hom0States[i] = Integer.parseInt(ch1[i].getValueRecord().stringValue());
				hom1States[i] = Integer.parseInt(ch2[i].getValueRecord().stringValue());
			} catch (ConnectionException ce) {
				System.out.println("Cannot connect to a channel!");
			} catch (GetException ge) {
				System.out.println("Cannot get a channel value!");
			}
			
			i++;
		}
		finished1 = true;
		this.notify();
	}

	private synchronized void setNewHOMStates() {
		while (!finished1) {
			try {
				this.wait();
			} catch (InterruptedException ie) {
				// do nothing
			}
		}

		// set HOM waveform displays to 4/5
		for (int i = 0; i < hom0States.length; i++) {
			try {
				ch1[i].putRawValCallback(4, new PutListener() {
					public void putCompleted(Channel ch) {
						
					}
				});

				ch2[i].putRawValCallback(5, new PutListener() {
					public void putCompleted(Channel ch) {
						
					}
				});
			} catch (ConnectionException ce) {
				
			} catch (PutException pe) {
				
			}
		}
		
		finished1 = false;
		finished2 = true;
		this.notify();
	}

	private synchronized void saveSnapshot() {
		while (!finished2) {
			try {
				this.wait();
			} catch (InterruptedException ie) {
				// do nothing
			}
		}

		try {
			LoggerSession loggerSession = pvLogger.requestLoggerSession( "SCL HOM" );
			
			// give 5 seconds for channel connection
			try {
				Thread.sleep(5000);
			} catch (InterruptedException ie) {
				// do nothing
			}
			
			snapshot = loggerSession.takeSnapshot();
			startTime = new Date();
			
			// attach the date to the snapshot comment field
			String comments = startTime.toString();
			comments = comments + "\n" + "For SCL Monitor Application";
			snapshot.setComment(comments);
			loggerSession.publishSnapshot(snapshot);
		}
		catch( Exception exception ) {
			throw new RuntimeException( exception );
		}

		finished2 = false;
		finished3 = true;
		this.notify();		
	}

	private synchronized void step4() {
		while (!finished3) {
			try {
				this.wait();
			} catch (InterruptedException ie) {
				// do nothing
			}
		}

		// set HOM waveform states back to originals
		for (int i = 0; i < hom0States.length; i++) {
			try {
				ch1[i].putRawValCallback(hom0States[i], new PutListener() {
					public void putCompleted(Channel ch) {
						
					}
				});

				ch2[i].putRawValCallback(hom1States[i], new PutListener() {
					public void putCompleted(Channel ch) {
						
					}
				});
			} catch (ConnectionException ce) {
				
			} catch (PutException pe) {
				
			}
		}
		
		finished3 = false;
		
	}

	public void customizeCommands(Commander commander) {
		// action for snapshot SCL HOM waveforms to the PV Logger
		snapAction = new AbstractAction() {
			static final long serialVersionUID = 100;

			public void actionPerformed(ActionEvent event) {

				// first, save present settings for all the HOM waveform
				// monitors
				saveHOMStates();

				// 2nd, set all HOM waveforms to 4 or 5
				setNewHOMStates();

				// 3rd, take snapshot
				saveSnapshot();

				// 4th, restore the waveforms back to their original states
				step4();
			}
		};
		snapAction.putValue(Action.NAME, "snap");
		snapAction.setEnabled(false);
		commander.registerAction(snapAction);

		// action for snapshot SCL HOM waveforms to the PV Logger
		Action loadAction = new AbstractAction() {
			static final long serialVersionUID = 101;

			public void actionPerformed(ActionEvent event) {
				// open PV Logger Chooser for selecting PV Logger ID
				if (pvLogSelector == null) {
					pvLogSelector = plsc.choosePVLogId();
					pvLogSelector.addWindowListener(new WindowListener() {
						public void windowClosed(WindowEvent e) {
							// do nothing
						}

						public void windowActivated(WindowEvent e) {
							// do nothing
						}

						public void windowClosing(WindowEvent e) {
							// do nothing
						}

						public void windowDeiconified(WindowEvent e) {
							// do nothing
						}

						public void windowIconified(WindowEvent e) {
							// do nothing
						}

						public void windowDeactivated(WindowEvent e) {
							pvLoggerID = plsc.getPVLogId();
							if (pvLoggerID != 0
									&& myWindow().getLLRFPane() != null) {
								System.out.println("PV Logger ID = "
										+ pvLoggerID);
								myWindow().getLLRFPane().setPVLoggerID(
										pvLoggerID);
							}
						}

						public void windowOpened(WindowEvent e) {
							// do nothing
						}
					});
				} else
					pvLogSelector.setVisible(true);

			}
		};
		loadAction.putValue(Action.NAME, "load");
		commander.registerAction(loadAction);
	}

	protected long getPVLoggerID() {
		return pvLoggerID;
	}
}
