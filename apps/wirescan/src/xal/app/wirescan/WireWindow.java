/*
 * WireWindow.java
 */

package xal.app.wirescan;

import java.awt.Dimension;
import java.awt.Component;
import java.awt.BorderLayout;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;

import xal.extension.application.*;
import xal.ca.*;
import xal.tools.apputils.*;
import xal.service.pvlogger.RemoteLoggingCenter;
import xal.smf.*;
import xal.extension.application.smf.*;
import xal.smf.impl.*;
import xal.tools.correlator.*;   // for correlattion

/**
 * This class creates the main window that is used. A WireWindow contains most
 * methods related to the drawing of the window.
 * 
 * @author S. Bunch
 * @version 1.0
 * @see AcceleratorWindow
 */
public class WireWindow extends AcceleratorWindow {
    private static final long serialVersionUID = 1L;

	private JTable wireTable;

	private JTable postTable;

	private WireDoc theDoc;

	private JTabbedPane theTabbedPane;

	private JButton startButton, selectAll, unselectAll, abortAll, saveWOScan;
	protected JButton dumpHarp;

	private JComboBox<String> saveData;

	private JPanel mainPanel, controlPanel;

	private JScrollPane scrollPane;

	private JScrollPane postScrollPane;

	private EdgeLayout eLayout1, eLayout2;

	private EdgeConstraints eConstraints1, eConstraints2;

	private WirePanel[] wirePanels;

	//	private WirePanel wirePanel;
	private PVUpdaterDbl[] posUpdate;

	//	private PVUpdaterDblArry xDataUpdate, yDataUpdate, zDataUpdate,
	// posArrayUpdate, xFitUpdate, yFitUpdate, zFitUpdate;
	//	private PVUpdaterDblArry[] xDataUpdate, yDataUpdate, zDataUpdate,
	// posArrayUpdate, xFitUpdate, yFitUpdate, zFitUpdate;
	private PVUpdaterDblArry[] xDataUpdate, yDataUpdate, zDataUpdate;

	//        private PVUpdaterRTData[] dataUpdate;

	private ArrayList<AcceleratorNode> seriesList;

	protected ArrayList<WirePanel> panelList;

	private ArrayList<Integer> selectedRows;

	private SeriesRun seriesRun;

	private ThreadKiller killAllThreads;

	private ButtonGroup group;

	protected JTextField msgField;

	private int[] nSteps;

	/** The table model for the control table */
	protected WireTableModel tableModel;

	/** Progress bar updater */
	protected ProgProdder[] progProdder;

	/** Timestamp when a scan was started */
	protected Date startTime;

	/** Radio button for a series scan */
	protected JRadioButton seriesButton;

	/** Radio button for a parallel scan */
	protected JRadioButton parallelButton;

	protected PostTableModel postTableModel;
		
	/** remote PV Logger*/
	final private RemoteLoggingCenter REMOTE_PV_LOGGER;

	protected long pvLoggerId;

	protected boolean pvLogged = false;

	protected boolean firstScan = true;

	protected boolean tableChanged = false;

	protected ProfileMonitor[] thePM;

	/**
	 * Creates a new instance of WireWindow
	 * 
	 * @param wiredocument
	 *            The WireDoc that is used
	 */
	public WireWindow(WireDoc wiredocument) {
		super(wiredocument);
		theDoc = wiredocument;
		setSize(600, 800);
		makeContent();
		REMOTE_PV_LOGGER = new RemoteLoggingCenter();
	}

	/**
	 * Creates a new table based on WireTableModel and adds it to the display.
	 * 
	 * @see WireTableModel
	 */
	protected void makeTable() {
		tableModel = new WireTableModel(theDoc);
		tableModel.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent evt) {
				tableChanged = true;
				firstScan = true;
			}
		});
		wireTable = new JTable(tableModel);
		wireTable.getColumn("Relative Wire Position").setCellRenderer(
				new ProgressRenderer());
		scrollPane = new JScrollPane(wireTable);
		scrollPane.setPreferredSize(new Dimension(550, 300));
		scrollPane.setVisible(true);
		eLayout2.setConstraints(scrollPane, 27, 0, 0, 0,
				EdgeLayout.TOP_LEFT_RIGHT, EdgeLayout.NO_GROWTH);
		controlPanel.add(scrollPane);
		theTabbedPane.add("Control", controlPanel);
		theTabbedPane.setVisible(true);
	}

	protected void doPostSigmaTable() {
		postTableModel = new PostTableModel(theDoc);
		postTable = new JTable(postTableModel);
		postScrollPane = new JScrollPane(postTable);
		postScrollPane.setPreferredSize(new Dimension(550, 225));
		postScrollPane.setVisible(true);
		eLayout2.setConstraints(postScrollPane, 350, 0, 0, 0,
				EdgeLayout.TOP_LEFT_RIGHT, EdgeLayout.NO_GROWTH);
		controlPanel.add(postScrollPane);
	}

	public void updatePostSigmaTable() {
		for (int i = 0; i < theDoc.selectedWires.size(); i++) {
			WireData wd = theDoc.wireDataMap.get(thePM[i].getId());
			postTableModel.setValueAt(new Double(wd.xsigmaf), i, 1);
			postTableModel.setValueAt(new Double(wd.ysigmaf), i, 2);
			postTableModel.setValueAt(new Double(wd.zsigmaf), i, 3);
		}
	}

	/**
	 * The routine used to draw the window and it's contents. Each window
	 * contains a control panel with a control table diplayed, a button to begin
	 * a scan, radio buttons to choose a series or parallel scan, an abort all
	 * button, and an export button.
	 */
	protected void makeContent() {
		mainPanel = new JPanel();
		mainPanel.setVisible(true);
		this.getContentPane().add(mainPanel, BorderLayout.CENTER);
		eLayout1 = new EdgeLayout();
		eConstraints1 = new EdgeConstraints();
		eLayout2 = new EdgeLayout();
		eConstraints2 = new EdgeConstraints();
		mainPanel.setLayout(eLayout1);
		theTabbedPane = new JTabbedPane();
		eLayout1.setConstraints(theTabbedPane, 0, 0, 0, 0,
				EdgeLayout.ALL_SIDES, EdgeLayout.GROW_BOTH);
		theTabbedPane.setVisible(true);
		mainPanel.add(theTabbedPane);
		controlPanel = new JPanel();
		controlPanel.setLayout(eLayout2);
		startButton = new JButton("Start");
		startButton.setText("Start Scan");
		eLayout2.setConstraints(startButton, 0, 5, 85, 0,
				EdgeLayout.LEFT_BOTTOM, EdgeLayout.NO_GROWTH);
		controlPanel.add(startButton);
		startButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				startButton.setEnabled(false);
				startAction();
				takeAndPublishPVLoggerSnapshot();
				//                                                              pvlogger.getLogger().takeSnapshot();
				Channel.flushIO();
			}
		});

		saveWOScan = new JButton("Save W/O Scan");
		eLayout2.setConstraints(saveWOScan, 0, 5, 50, 0,
				EdgeLayout.LEFT_BOTTOM, EdgeLayout.NO_GROWTH);
		controlPanel.add(saveWOScan);
		saveWOScan.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveWOScanAction();
				takeAndPublishPVLoggerSnapshot();
				exportAction();
				Channel.flushIO();
			}
		});
		
		dumpHarp = new JButton("Save Harp Data");
		eLayout2.setConstraints(dumpHarp, 0, 5, 15, 0,
				EdgeLayout.LEFT_BOTTOM, EdgeLayout.NO_GROWTH);
		controlPanel.add(dumpHarp);
		dumpHarp.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveHarpData();
			}
		});
		dumpHarp.setEnabled(false);		

		selectAll = new JButton();
		selectAll.setText("Select All");
		eLayout2.setConstraints(selectAll, 0, 200, 0, 0, EdgeLayout.TOP_LEFT,
				EdgeLayout.NO_GROWTH);
		controlPanel.add(selectAll);
		selectAll.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				selectAllAction();
			}
		});
		unselectAll = new JButton("Unselect All");
		eLayout2.setConstraints(unselectAll, 0, 300, 0, 0, EdgeLayout.TOP_LEFT,
				EdgeLayout.NO_GROWTH);
		controlPanel.add(unselectAll);
		unselectAll.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				unselectAllAction();
			}
		});

		String[] saveDataOptions = { "Export Data & PVLogger",
				"Export Data only" };
		// now we always have to save to the PVLogger.
//		String[] saveDataOptions = { "Export Data & PVLogger" };
		saveData = new JComboBox<String>(saveDataOptions);
		eLayout2.setConstraints(saveData, 0, 285, 25, 0,
				EdgeLayout.LEFT_BOTTOM, EdgeLayout.NO_GROWTH);
		controlPanel.add(saveData);
		saveData.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if (((String) (((JComboBox) evt.getSource()).getSelectedItem())).equals("Export Data Only")) {
					exportAction();
				} 
				else if (((String) (((JComboBox) evt.getSource()).getSelectedItem())).equals("Export Data & PVLogger")) {
					takeAndPublishPVLoggerSnapshot();
					exportAction();
				}
			}
		});
		abortAll = new JButton("Abort All");
		eLayout2.setConstraints(abortAll, 0, 470, 25, 0,
				EdgeLayout.LEFT_BOTTOM, EdgeLayout.NO_GROWTH);
		controlPanel.add(abortAll);
		abortAll.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				abortAllAction();
			}
		});
		seriesButton = new JRadioButton("Series Scan");
		eLayout2.setConstraints(seriesButton, 0, 145, 55, 0,
				EdgeLayout.LEFT_BOTTOM, EdgeLayout.NO_GROWTH);
		controlPanel.add(seriesButton);
		seriesButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				theDoc.series = Boolean.TRUE;
			}
		});
		parallelButton = new JRadioButton("Parallel Scan");
		parallelButton.setSelected(true);
		theDoc.series = new Boolean(false);
		theDoc.series = Boolean.FALSE;
		eLayout2.setConstraints(parallelButton, 0, 145, 25, 0,
				EdgeLayout.LEFT_BOTTOM, EdgeLayout.NO_GROWTH);
		controlPanel.add(parallelButton);
		parallelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				theDoc.series = Boolean.FALSE;
			}
		});
		group = new ButtonGroup();
		group.add(seriesButton);
		group.add(parallelButton);
		msgField = new JTextField();
		msgField.setEditable(false);
		//		msgField.setPreferredSize(new Dimension(600, 20));
		//		eLayout2.setConstraints(msgField, 0, 0, 0, 0, EdgeLayout.LEFT_BOTTOM,
		// EdgeLayout.NO_GROWTH);
		//		controlPanel.add(msgField);
		getContentPane().add(msgField, "South");

		seriesList = new ArrayList<AcceleratorNode>();
		panelList = new ArrayList<WirePanel>();
		selectedRows = new ArrayList<Integer>();
	}

	/**
	 * Action performed when the start scan button is clicked.
	 * <p>
	 * This is a quite intensive routine that starts up everything and sets
	 * everything up to do a scan.
	 */
	private void startAction() {
		clearAll();

		// check if selected wire scanners get changed
		if (tableChanged) {
			tableChanged = false;
		}

		// reset the wireDataMap to empty for new scan
		theDoc.resetWireDataMap();

		selectWires();
		String[] theId = new String[theDoc.selectedWires.size()];

		if (firstScan) {
			thePM = new ProfileMonitor[theDoc.selectedWires.size()];
			nSteps = new int[theDoc.selectedWires.size()];

			posUpdate = new PVUpdaterDbl[theDoc.selectedWires.size()];
			progProdder = new ProgProdder[theDoc.selectedWires.size()];

			wirePanels = new WirePanel[theDoc.selectedWires.size()];
			for (int i = 0; i < theDoc.selectedWires.size(); i++) {
				System.out.println("selectedwires size "
						+ theDoc.selectedWires.size());
				theId[i] = (theDoc.selectedWires.get(i))
						.getId();
				thePM[i] = (ProfileMonitor) theDoc.selectedWires.get(i);

				// Make connections
				connectArrays(thePM[i]);
				// Create WirePanel
				wirePanels[i] = new WirePanel(thePM[i], theDoc, this);
				// Create holder for WireData
				theDoc.wireDataMap.put(theId[i], new WireData(wirePanels[i]));

				panelList.add(wirePanels[i]);

				// the next 7 lines are for debugging purpose
				//                            theTabbedPane.add(theId[i], wirePanels[i]);
				//                            theTabbedPane.setVisible(true);
				//                    }
				//		    doPostSigmaTable();
				//                    firstScan = false;
				//                }
				//                startButton.setEnabled(true);

			}

			doPostSigmaTable();
			firstScan = false;

		} else {
			//		   killAllThreads.stop();
		}

		killAllThreads = new ThreadKiller(this);

		//                synchronized (this) {
		xDataUpdate = new PVUpdaterDblArry[theDoc.selectedWires.size()];
		yDataUpdate = new PVUpdaterDblArry[theDoc.selectedWires.size()];
		zDataUpdate = new PVUpdaterDblArry[theDoc.selectedWires.size()];
		//                    posArrayUpdate = new PVUpdaterDblArry[theDoc.selectedWires.size()];

		//                    dataUpdate = new PVUpdaterRTData[theDoc.selectedWires.size()];

		for (int i = 0; i < theDoc.selectedWires.size(); i++) {

			try {
				nSteps[i] = thePM[i].getNSteps();
			} catch (ConnectionException ce) {
				System.err.println(ce.getMessage());
				ce.printStackTrace();
			} catch (GetException ge) {
				System.err.println(ge.getMessage());
				ge.printStackTrace();
			}

			//theTabbedPane.add(theId[i], wirePanels[i]);

			//                            Channel[] rtChannels = new Channel[4];
			//                            rtChannels[0] = wirePanels[i].pm.PosC;
			//                            rtChannels[1] =
			// wirePanels[i].pm.getChannel(ProfileMonitor.V_REAL_DATA_HANDLE);
			//                            rtChannels[2] =
			// wirePanels[i].pm.getChannel(ProfileMonitor.D_REAL_DATA_HANDLE);
			//                            rtChannels[3] =
			// wirePanels[i].pm.getChannel(ProfileMonitor.H_REAL_DATA_HANDLE);

			try {
				tableModel.setMaxValueAt(thePM[i].getScanLength(),
						(selectedRows.get(i)).intValue());
				//                                dataUpdate[i] = new PVUpdaterRTData(rtChannels, theId[i],
				// theDoc, wirePanels[i], thePM[i].getScanLength());
			} catch (ConnectionException ce) {
				System.err.println(ce.getMessage());
				ce.printStackTrace();
			} catch (GetException ge) {
				System.err.println(ge.getMessage());
				ge.printStackTrace();
			}

			// Start the updates
			//                            startArrayUpdates(wirePanels[i], theId[i], nSteps[i]);
			xDataUpdate[i] = new PVUpdaterDblArry(wirePanels[i].pm
					.getChannel(ProfileMonitor.V_REAL_DATA_HANDLE), theId[i],
					theDoc, 0, wirePanels[i].pm.PosC, wirePanels[i]);
			yDataUpdate[i] = new PVUpdaterDblArry(wirePanels[i].pm
					.getChannel(ProfileMonitor.D_REAL_DATA_HANDLE), theId[i],
					theDoc, 1, wirePanels[i].pm.PosC, wirePanels[i]);
			zDataUpdate[i] = new PVUpdaterDblArry(wirePanels[i].pm
					.getChannel(ProfileMonitor.H_REAL_DATA_HANDLE), theId[i],
					theDoc, 2, wirePanels[i].pm.PosC, wirePanels[i]);
			//		posArrayUpdate[i] = new
			// PVUpdaterDblArry(wirePanels[i].pm.PosArrayC, theId[i], theDoc,
			// nSteps[i]);

			// Set up the progress bar monitor
			posUpdate[i] = new PVUpdaterDbl(thePM[i].PosC);
			progProdder[i] = new ProgProdder((JProgressBar) tableModel
					.getValueAt((selectedRows.get(i)).intValue(), 2),
					posUpdate[i], tableModel);

			// If series scan just add to list to scan...otherwise go ahead and
			// start the scan
			if (theDoc.series == Boolean.TRUE) {
				seriesList.add(theDoc.selectedWires.get(i));
			}
			if (theDoc.series == Boolean.FALSE) {
				int timeout = 0;
				// Make sure the wirescanner is ready
				while (timeout <= 10) {
					if (wirePanels[i].status.getText().startsWith("Ready")) {
						break;
					}
					if (wirePanels[i].status.getText().startsWith("Scan")) {
						break;
					}
					if (wirePanels[i].status.getText().startsWith("Found")) {
						break;
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ie) {
						System.out.println("Sleep interrupted for wirescanner "
								+ theId[i] + " while checking if ready");
						System.err.println(ie.getMessage());
						ie.printStackTrace();
					}
					timeout++;
				}
				// If it's ready then let's go
				if (timeout <= 10) {

					try {
						msgField.setText("Scanning...");
						thePM[i].doScan();
					} catch (ConnectionException ce) {
						System.err.println(ce.getMessage());
						ce.printStackTrace();
					} catch (PutException pe) {
						System.err.println(pe.getMessage());
						pe.printStackTrace();
					}

					startTime = new Date();
				}
				// If it's not ready yet then we can't do a scan right now
				else {
					System.out.println("Timeout expired for " + theId[i]
							+ " while waiting for ready");
					JOptionPane.showMessageDialog(this,
							"Timeout expired while waiting for ready",
							theId[i], JOptionPane.ERROR_MESSAGE);
				}
			}

			// start correlator
			//                 dataUpdate[i].startCorrelator();

			// Start the threads
			progProdder[i].start();
			theTabbedPane.setVisible(true);
		}
		//                }

		// If this is a series scan...let's start it up now
		if (theDoc.series == Boolean.TRUE) {
			seriesRun = new SeriesRun(seriesList, panelList);
			seriesRun.start();
			startTime = new Date();
		}

		// Start the thread killer monitor
		killAllThreads.start();

		Channel.flushIO();

	}
	
	/**
	 * Method saveHarpData: to save the RTBT harp data to a file
	 */
	private void saveHarpData() {
		System.out.println( "Saving Harp data..." );
		
		startTime = new Date();
		takeAndPublishPVLoggerSnapshot();

		if ( startTime != null ) {
			/* Set up the file name to be the timestamp of the run */
			SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd");
			String datePart = df.format(startTime);
			datePart = datePart.replaceAll(" ", "");
			datePart = datePart.replace('/', '.');
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			String timePart = sdf.format(startTime);
			timePart = timePart.replaceAll(" ", "");
			timePart = timePart.replace(':', '.');
			String fileString = datePart + "." + timePart + ".harp";
			/* Save the file in a pre-defined directory */
//			File filePath = new File("/ade/xal/docs/Wirescanner Application/");
			File filePath = Application.getApp().getDefaultDocumentFolder();
//			fileString = "/ade/xal/docs/Wirescanner Application/" + fileString;
			File file = new File(filePath, fileString);
			
			// x=hori., y=vert., z=diag.
			double[] x, y, z;
			double[] posX, posY, posZ;
			
			System.out.println( "Fetching correlated Harp data..." );
			Correlation<ChannelTimeRecord> correlation = theDoc.correlator.fetchCorrelationWithTimeout( 1.0 );
			if ( correlation != null ) {
				System.out.println( "Got correlated Harp data..." );
				x = ((ChannelRecord)correlation.getRecord(theDoc.ch_h.getId())).doubleArray();
				y = ((ChannelRecord)correlation.getRecord(theDoc.ch_v.getId())).doubleArray();
				z = ((ChannelRecord)correlation.getRecord(theDoc.ch_d.getId())).doubleArray();
				posX = ((ChannelRecord)correlation.getRecord(theDoc.ch_posH.getId())).doubleArray();
				posY = ((ChannelRecord)correlation.getRecord(theDoc.ch_posV.getId())).doubleArray();
				posZ = ((ChannelRecord)correlation.getRecord(theDoc.ch_posD.getId())).doubleArray();
				
				System.out.println( "Writing out Harp data..." );
				
				String line = "start time: " + startTime.toString() + "\n\n";
				
				line = line + "RTBT_Diag:Harp30\n\n";
				
				for (int i=0; i < x.length; i++) {
					line = line + posX[i] + "\t" +x[i] + "\t" + posY[i] + "\t" + y[i] + "\t" + posZ[i] + "\t" + z[i] + "\n";
				}
				// if also log the machine status to the PV Logger, attach the PV logger ID here
				if (pvLogged) {
					line = line + "\nPVLoggerID = " + pvLoggerId;
				}
				
				byte buf[] = line.getBytes();
				try {
					OutputStream f1 = new FileOutputStream(file);
					f1.write(buf);
					f1.close();
					msgField.setText("Saved File Successfully...File saved to..." + file.toString());
				} 
				catch (Exception e) {}
			}
			else {
				JOptionPane.showMessageDialog( this, "Failed to get correlated Harp data!", "No Data", JOptionPane.ERROR_MESSAGE );
			}
		} else {
			JOptionPane.showMessageDialog( this, "Nothing to export!\nPlease run a scan first!", "No Data", JOptionPane.ERROR_MESSAGE );
		}		
	}
	
	private void saveWOScanAction() {
		clearAll();

		// check if selected wire scanners get changed
		if (tableChanged) {
			tableChanged = false;
		}
		
		// reset the wireDataMap to empty for new scan
		theDoc.resetWireDataMap();
		startTime = new Date();
		selectWires();
		String[] theId = new String[theDoc.selectedWires.size()];

		wirePanels = new WirePanel[theDoc.selectedWires.size()];
		thePM = new ProfileMonitor[theDoc.selectedWires.size()];
		nSteps = new int[theDoc.selectedWires.size()];
		for (int i = 0; i < theDoc.selectedWires.size(); i++) {
			System.out.println("selectedwires size "
					+ theDoc.selectedWires.size());
			theId[i] = (theDoc.selectedWires.get(i))
					.getId();
			thePM[i] = (ProfileMonitor) theDoc.selectedWires.get(i);

			// Make connections
			connectArrays(thePM[i]);
			// Create WirePanel
			wirePanels[i] = new WirePanel(thePM[i], theDoc, this);
			// Create holder for WireData
			theDoc.wireDataMap.put(theId[i], new WireData(wirePanels[i]));

			panelList.add(wirePanels[i]);
			
			// aquire data
			wirePanels[i].getAmpls();
			wirePanels[i].getAreas();
			wirePanels[i].getMeans();
			wirePanels[i].getOffsets();
			wirePanels[i].getSigmas();
			wirePanels[i].getSlopes();
			wirePanels[i].doPostGraph();
		}
		
		doPostSigmaTable();
		
		Channel.flushIO();
	}

	private void connectArrays(ProfileMonitor dummyPM) {
		String theId = ((AcceleratorNode) dummyPM).getId();
		try {
			dummyPM.connectVData();
		} catch (ConnectionException ce) {
			System.out.println("Could not connect to " + theId + " XData");
			System.err.println(ce.getMessage());
			ce.printStackTrace();
		}
		try {
			dummyPM.connectDData();
		} catch (ConnectionException ce) {
			System.out.println("Could not connect to " + theId + " YData");
			System.err.println(ce.getMessage());
			ce.printStackTrace();
		}
		try {
			dummyPM.connectHData();
		} catch (ConnectionException ce) {
			System.out.println("Could not connect to " + theId + " ZData");
			System.err.println(ce.getMessage());
			ce.printStackTrace();
		}
		try {
			dummyPM.connectVDataArray();
		} catch (ConnectionException ce) {
			System.out.println("Could not connect to " + theId + " XDataArray");
			System.err.println(ce.getMessage());
			ce.printStackTrace();
		}
		try {
			dummyPM.connectDDataArray();
		} catch (ConnectionException ce) {
			System.out.println("Could not connect to " + theId + " YDataArray");
			System.err.println(ce.getMessage());
			ce.printStackTrace();
		}
		try {
			dummyPM.connectHDataArray();
		} catch (ConnectionException ce) {
			System.out.println("Could not connect to " + theId + " ZDataArray");
			System.err.println(ce.getMessage());
			ce.printStackTrace();
		}
		try {
			dummyPM.connectPosArray();
		} catch (ConnectionException ce) {
			System.out.println("Could not connect to " + theId + " PosArray");
			System.err.println(ce.getMessage());
			ce.printStackTrace();
		}
		try {
			dummyPM.connectStatArray();
		} catch (ConnectionException ce) {
			System.out.println("Could not connect to " + theId + " StatArray");
			System.err.println(ce.getMessage());
			ce.printStackTrace();
		}
		try {
			dummyPM.connectPos();
		} catch (ConnectionException ce) {
			System.out.println("Could not connect to " + theId + " Pos");
			System.err.println(ce.getMessage());
			ce.printStackTrace();
		}
		try {
			dummyPM.connectVFitArray();
		} catch (ConnectionException ce) {
			System.out.println("Could not connect to " + theId + " VFitArray");
			System.err.println(ce.getMessage());
			ce.printStackTrace();
		}
		try {
			dummyPM.connectDFitArray();
		} catch (ConnectionException ce) {
			System.out.println("Could not connect to " + theId + " DFitArray");
			System.err.println(ce.getMessage());
			ce.printStackTrace();
		}
		try {
			dummyPM.connectHFitArray();
		} catch (ConnectionException ce) {
			System.out.println("Could not connect to " + theId + " HFitArray");
			System.err.println(ce.getMessage());
			ce.printStackTrace();
		}
		
		Channel.flushIO();
	}

	private void clearAll() {
		if (tableChanged) {
			// Need to clear everything before starting a new scan
			//		theDoc.selectedWires.clear();
			theDoc.selectedWires = new ArrayList<AcceleratorNode>();
			//		seriesList.clear();
			seriesList = new ArrayList<AcceleratorNode>();
			//		panelList.clear();
			panelList = new ArrayList<WirePanel>();
			//		selectedRows.clear();
			selectedRows = new ArrayList<Integer>();
			//		theDoc.wireDataMap.clear();
			theDoc.wireDataMap = new HashMap<String, WireData>();

			// if selected wires changed, we need to initialize everything
			if (theTabbedPane.getTabCount() > 1) {
				controlPanel.remove(postScrollPane);
			}
			while (theTabbedPane.getTabCount() > 1) {
				theTabbedPane.remove(theTabbedPane.getTabCount() - 1);
			}
		}

		// clear all the dataViews in the graphs
		for (int i = 0; i < panelList.size(); i++) {
			(panelList.get(i)).clear();
		}

	}

	public void clearPVUpdaters() {

		// reset all data counter
		for (int i = 0; i < theDoc.selectedWires.size(); i++) {
			//                dataUpdate[i].resetData();
			xDataUpdate[i].reset();
			yDataUpdate[i].reset();
			zDataUpdate[i].reset();
			//                posArrayUpdate[i].reset();
			//                    xFitUpdate[i].reset();
			//                    yFitUpdate[i].reset();
			//                    zFitUpdate[i].reset();
		}

	}

	/**
	 * Adds the selected wires to an ArrayList in the WireDoc class for use
	 * later.
	 */
	protected void selectWires() {
		// reset properly first
		theDoc.selectedWires.clear();
		//            theDoc.selectedWires = new ArrayList();
		selectedRows.clear();
		//            selectedRows = new ArrayList();

		for (int i = 0; i < tableModel.getRowCount(); i++) {
			if (((Boolean) tableModel.getValueAt(i, 1)).booleanValue()) {
				theDoc.selectedWires.add(theDoc.wirescanners.get(i));
				selectedRows.add(new Integer(i));
			}
		}

	}

	private void selectAllAction() {
		for (int i = 0; i < tableModel.getRowCount(); i++) {
			tableModel.setValueAt(Boolean.TRUE, i, 1);
		}
	}

	private void unselectAllAction() {
		for (int i = 0; i < tableModel.getRowCount(); i++) {
			tableModel.setValueAt(Boolean.FALSE, i, 1);
		}

		clearAll();
	}

	private void abortAllAction() {
		for (int i = 0; i < panelList.size(); i++) {
			(panelList.get(i)).abortAction();
		}
	}

	/**
	 * Action taken when the export button is clicked.
	 * <p>
	 * It basically sets up the timestamp filename and sends it to the
	 * saveToFile() routine in the WireDoc class.
	 */
	protected void exportAction() {
		/* If a scan has been done then we can export */
		if (startTime != null) {
			/* Set up the file name to be the timestamp of the run */
			SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd");
			String datePart = df.format(startTime);
			datePart = datePart.replaceAll(" ", "");
			datePart = datePart.replace('/', '.');
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			String timePart = sdf.format(startTime);
			timePart = timePart.replaceAll(" ", "");
			timePart = timePart.replace(':', '.');
			String fileString = datePart + "." + timePart + ".txt";
			/* Save the file in a pre-defined directory */
			File filePath = Application.getApp().getDefaultDocumentFolder();
			File file = new File(filePath, fileString);
			try {
				theDoc.saveToFile(file);
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "Error Saving File",
						"Save Error", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(this,
					"Nothing to export!\nPlease run a scan first!", "No Data",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	protected void takeAndPublishPVLoggerSnapshot() {
		System.out.println( "Taking and publishing a PV Logger Snapshot..." );
		String comments = startTime.toString();
		comments = comments + "\n" + "For Wire Scanner Application with WSs:\n";

		for (int i = 0; i < theDoc.selectedWires.size(); i++) {
			comments = comments + " " + (theDoc.selectedWires.get(i)).getId();
		}
		
		pvLoggerId = REMOTE_PV_LOGGER.takeAndPublishSnapshot( "default", comments );
		if ( pvLoggerId > 0 ) {
			pvLogged = true;
			System.out.println( "Took and published a PV Logger snapshot with ID: " + pvLoggerId );
		}
		else if ( pvLoggerId == -1 ) {
			pvLogged = false;
			JOptionPane.showMessageDialog( this, "Could not locate the default PV Logger service.\nNo PV Logger snapshot will be logged.", "Snapshot Exception", JOptionPane.WARNING_MESSAGE );
		}
		else {
			pvLogged = false;
			JOptionPane.showMessageDialog( this, "Exception while taking a PV Logger snapshot.\nNo PV Logger snapshot will be logged.", "Snapshot Exception", JOptionPane.WARNING_MESSAGE );
		}

		//            JFrame frame = pvlogger.showFrame();
		//            frame.setLocationRelativeTo(this);
		//            frame.setVisible(true);
	}

	public void enableStartButton() {
		startButton.setEnabled(true);
	}

	public void stopProgProdders() {

		for (int i = 0; i < theDoc.selectedWires.size(); i++) {
			progProdder[i].stopT();
		}
	}

}
