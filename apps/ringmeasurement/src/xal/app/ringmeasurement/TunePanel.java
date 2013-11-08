/*
 * TunePanel.java
 *
 * Copyright (c) 2001-2005 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 * Created on February 21, 2005, 10:45 AM
 */

package xal.app.ringmeasurement;

import java.io.*;
import java.util.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.event.*;
import java.text.NumberFormat;

import javax.swing.*;
import javax.swing.event.*;

import java.text.ParseException;

import xal.tools.apputils.EdgeLayout;
import xal.smf.impl.*;
import xal.smf.Ring;
import xal.tools.math.r3.R3;
import xal.tools.swing.DecimalField;
import xal.ca.*;
import xal.tools.apputils.files.*;
import xal.tools.beam.calc.RingCalculations;
import xal.service.pvlogger.*;
import xal.service.pvlogger.query.*;
import xal.tools.database.*;
import xal.model.*;
import xal.model.probe.TransferMapProbe;
import xal.sim.scenario.ProbeFactory;
import xal.model.alg.TransferMapTracker;
import xal.sim.scenario.Scenario;
import xal.model.probe.traj.TransferMapTrajectory;
import xal.model.probe.traj.TransferMapState;
import xal.sim.sync.PVLoggerDataSource;

/**
 * 
 * @author Paul Chu
 */
public class TunePanel extends JPanel implements ConnectionListener,
		ActionListener {
	static final long serialVersionUID = 0;

	RingDocument myDoc;

	EdgeLayout edgeLayout = new EdgeLayout();

	JTable bpmTable, quadTable;

	ArrayList<BPM> allBPMs;

	ArrayList<Integer> badBPMs = new ArrayList<Integer>();

	// ArrayList allQuads;

	ArrayList<MagnetMainSupply> qPSs = new ArrayList<MagnetMainSupply>();

	JPanel bpmPane = new JPanel();

	private TuneMeasurement[] tuneMeasurement;

	JScrollPane bpmChooserPane, quadPane;

	JTabbedPane plotDisplayPane;

	JPanel phasePlotPane = new JPanel();

	JPanel posPlotPane = new JPanel();

	JPanel phaseDiffPlotPane = new JPanel();

	BpmTableModel bpmTableModel;

	QuadTableModel quadTableModel;

	private String selectedBPM = "";

	// private JTextField selectedBPMName = new JTextField(20);

	private JComboBox<String> selectBPM;

	private JDialog configDialog = new JDialog();

	BPMPlotPane xBpmPlotPane, yBpmPlotPane, xPhasePlotPane, yPhasePlotPane;

	BPMPlotPane xPhDiffPlotPane, yPhDiffPlotPane;

	double[] xTune, yTune;

	double[] xPhase, yPhase, xPhaseDiff, yPhaseDiff, xDiffPlot, yDiffPlot,
			posArray, goodPosArry;

	JTextField dfXTune, dfYTune;

	NumberFormat numberFormat = NumberFormat.getNumberInstance();

	// protected DecimalField df1, df2, df3, df4, df5;
	protected DecimalField df6, df7;

	/*
	 * double A = 17;
	 * 
	 * double c = 0.05;
	 * 
	 * double w = 0.21;
	 * 
	 * double b = 0.3;
	 * 
	 * double d = 0;
	 */
	int maxTime = 100;

	int fftSize = 64;

	int len = 40;

	protected JComboBox<String> fftConf;

	boolean hasTune = false;

	JButton quadCorrBtn, setQuadBtn;

	JProgressBar progBar;

	double[] qSetVals;

	Channel[] setPVChs, rbPVChs;

	/** List of the monitors */
	final Vector<Monitor> mons = new Vector<Monitor>();

	private HashMap<String, Vector<InputPVTableCell>> monitorQueues = new HashMap<String, Vector<InputPVTableCell>>();

	JButton dumpData = new JButton("Save Fit Data");

	/** for data dump file */
	private RecentFileTracker _datFileTracker;

	File datFile;

	// private PVLoggerForm pvlogger;
	private LoggerSession loggerSession, loggerSession1;

	private MachineSnapshot snapshot, snapshot1;

	protected long pvLoggerId, pvLoggerId1;

	/** Timestamp when a scan was started */
	protected Date startTime;

	InputPVTableCell setPVCell[], rbPVCell[];

	HashMap<MagnetMainSupply, Double> designMap;

	CalcQuadSettings cqs;

	JLabel xTuneAvg, yTuneAvg;
	
	// get track of "good" BPMs
	ArrayList<String> goodBPMs;
	
    /** for on/off line mode */
    private boolean isOnline = true;
    	
	private long bpmPVLogId = 0;
	private long defPVLogId = 0;
	
	private boolean quadTableInit = false;

	public TunePanel(RingDocument doc) {

		myDoc = doc;
		_datFileTracker = new RecentFileTracker(1, this.getClass(),
				"recent_saved_file");

		// initialize PVLogger
		try {
			PVLogger pvLogger = null;
			final ConnectionDictionary defaultDictionary = PVLogger.newLoggingConnectionDictionary();
			if ( defaultDictionary != null && defaultDictionary.hasRequiredInfo() ) {
				pvLogger = new PVLogger( defaultDictionary );
			}
			else {
				ConnectionPreferenceController.displayPathPreferenceSelector();
				final ConnectionDictionary dictionary = PVLogger.newLoggingConnectionDictionary();
				if ( dictionary != null && dictionary.hasRequiredInfo() ) {
					pvLogger = new PVLogger( dictionary );
				}
			}
			
			if ( pvLogger != null ) {
				loggerSession = pvLogger.requestLoggerSession( "default" );			
				loggerSession1 = pvLogger.requestLoggerSession( "Ring BPM Test" );
			}		
		}
		catch ( Exception exception ) {
			exception.printStackTrace();
		}
	}

	protected void initTables(ArrayList<BPM> bpms, ArrayList<MagnetMainSupply> quads,
			HashMap<MagnetMainSupply, Double> map) {
		allBPMs = bpms;
		qPSs = quads;
		designMap = map;

		tuneMeasurement = new TuneMeasurement[allBPMs.size()];
		xTune = new double[allBPMs.size()];
		yTune = new double[allBPMs.size()];
		xPhase = new double[allBPMs.size()];
		yPhase = new double[allBPMs.size()];
//		xPhaseDiff = new double[allBPMs.size()];
//		yPhaseDiff = new double[allBPMs.size()];
//		xDiffPlot = new double[allBPMs.size()];
//		yDiffPlot = new double[allBPMs.size()];
		posArray = new double[allBPMs.size()];

		this.setSize(960, 850);

		setLayout(edgeLayout);
		String[] bpmColumnNames = { "BPM", "XTune", "XPhase", "YTune",
				"YPhase", "Ignore" };
		bpmTableModel = new BpmTableModel(allBPMs, bpmColumnNames, this);

		String[] quadColumnNames = { "Quad PS", "Set Pt.", "Readback",
				"fitted Field", "new Set Pt." };
		quadTableModel = new QuadTableModel(qPSs, quadColumnNames);

		EdgeLayout edgeLayout1 = new EdgeLayout();
		bpmPane.setLayout(edgeLayout1);
		JLabel label = new JLabel("Select a BPM for Tune Measurement:");
		edgeLayout.setConstraints(label, 0, 0, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		bpmPane.add(label);

		// bpmTableModel.setValueAt(new Boolean(true), 0, 5);
		// setSelectedBPM(bpmTableModel.getRowName(0));

		bpmTable = new JTable(bpmTableModel);
		bpmTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		bpmTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ListSelectionModel rowSM = bpmTable.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;

				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty()) {
					// do nothing
				} else {
					int selectedRow = lsm.getMinSelectionIndex();
					setSelectedBPM(( allBPMs.get(selectedRow)).getId());
					if (!badBPMs.contains(new Integer(selectedRow))) {
						plotBPMData(selectedRow);
					}
				}
			}
		});

		bpmChooserPane = new JScrollPane(bpmTable);

		bpmChooserPane.setPreferredSize(new Dimension(450, 300));
		bpmChooserPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		edgeLayout1.setConstraints(bpmChooserPane, 20, 0, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		bpmPane.add(bpmChooserPane);
		JPanel selection = new JPanel();
		selection.setLayout(new GridLayout(2, 2));
		selection.setPreferredSize(new Dimension(330, 60));
		// selection.add(selectedBPMName);
		String[] options = { "Get tune (fit)", "Get tune (FFT)" };
		selectBPM = new JComboBox<String>(options);
		selectBPM.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				System.out.println("App mode is " + isOnline);
				quadTableModel.setAppMode(isOnline);
				if (((String) (((JComboBox) evt.getSource()).getSelectedItem()))
						.equals("Get tune (fit)")) {
					tuneByFit();
				} else if (((String) (((JComboBox) evt.getSource())
						.getSelectedItem())).equals("Get tune (FFT)")) {
					tuneByFFT();
				}
				// save to PV Logger
				if (isOnline) {
					snapshot = loggerSession.takeSnapshot();
					snapshot1 = loggerSession1.takeSnapshot();
					startTime = new Date();
				}
			}
		});
		selectBPM.setPreferredSize(new Dimension(60, 10));

		JButton config = new JButton("Config. FFT/fit");
		config.setActionCommand("configuration");
		config.setPreferredSize(new Dimension(80, 10));
		config.addActionListener(this);
		selection.add(config);
		JLabel dummy = new JLabel("");
		selection.add(dummy);
		selection.add(selectBPM);
		configDialog.setBounds(300, 300, 330, 300);
		configDialog.setTitle("Config. fit/FFT parameters...");
		numberFormat.setMaximumFractionDigits(6);

		dumpData.setActionCommand("dumpData");
		dumpData.addActionListener(this);
		dumpData.setEnabled(false);
		selection.add(dumpData);

		xTuneAvg = new JLabel("avg. x tune = ");
		edgeLayout1.setConstraints(xTuneAvg, 440, 0, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		bpmPane.add(xTuneAvg);
		yTuneAvg = new JLabel("avg. y tune = ");
		edgeLayout1.setConstraints(yTuneAvg, 460, 0, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		bpmPane.add(yTuneAvg);

		quadTable = new JTable(quadTableModel);
		quadTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		quadPane = new JScrollPane(quadTable);

		quadPane.setPreferredSize(new Dimension(450, 200));
		quadPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		edgeLayout1.setConstraints(quadPane, 550, 0, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		bpmPane.add(quadPane);

		quadCorrBtn = new JButton("Find Quad Error");
		quadCorrBtn.setActionCommand("findQuadError");
		quadCorrBtn.addActionListener(this);
		quadCorrBtn.setEnabled(false);
		edgeLayout.setConstraints(quadCorrBtn, 650, 500, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		add(quadCorrBtn);

		progBar = new JProgressBar();
		progBar.setMinimum(0);
		edgeLayout.setConstraints(progBar, 680, 500, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		add(progBar);

		setQuadBtn = new JButton("Set Quads");
		setQuadBtn.setActionCommand("setQuads");
		setQuadBtn.addActionListener(this);
		setQuadBtn.setEnabled(false);
		edgeLayout.setConstraints(setQuadBtn, 720, 500, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		add(setQuadBtn);

		JPanel paramConf = new JPanel();
		// paramConf.setLayout(new GridLayout(6,1));
		JLabel fitFunction = new JLabel(
				"Fit function: A*exp(-c*x) * sin(2PI*(w*x + b)) + d");
		JPanel ampPane = new JPanel();
		ampPane.setLayout(new GridLayout(1, 2));

		/*
		 * JLabel label1 = new JLabel("A = "); df1 = new DecimalField(A, 9,
		 * numberFormat); ampPane.add(label1); ampPane.add(df1);
		 * paramConf.add(ampPane); JPanel expPane = new JPanel();
		 * expPane.setLayout(new GridLayout(1, 2)); JLabel label2 = new
		 * JLabel("c = "); df2 = new DecimalField(c, 9, numberFormat);
		 * expPane.add(label2); expPane.add(df2); paramConf.add(expPane); JPanel
		 * tunePane = new JPanel(); tunePane.setLayout(new GridLayout(1, 2));
		 * JLabel label3 = new JLabel("w = "); df3 = new DecimalField(w, 9,
		 * numberFormat); tunePane.add(label3); tunePane.add(df3);
		 * paramConf.add(tunePane); JPanel phiPane = new JPanel();
		 * phiPane.setLayout(new GridLayout(1, 2)); JLabel label4 = new
		 * JLabel("b = "); df4 = new DecimalField(b, 9, numberFormat);
		 * phiPane.add(label4); phiPane.add(df4); paramConf.add(phiPane); JPanel
		 * offsetPane = new JPanel(); offsetPane.setLayout(new GridLayout(1,
		 * 2)); JLabel label5 = new JLabel("d = "); df5 = new DecimalField(d, 9,
		 * numberFormat); offsetPane.add(label5); offsetPane.add(df5);
		 * paramConf.add(offsetPane);
		 */
		JPanel maxTimePane = new JPanel();
		maxTimePane.setLayout(new GridLayout(1, 2));
		JLabel label6 = new JLabel("Max. no of iterations: ");
		df6 = new DecimalField(maxTime, 9, numberFormat);
		maxTimePane.add(label6);
		maxTimePane.add(df6);
		paramConf.add(maxTimePane);
		JPanel fitLengthPane = new JPanel();
		fitLengthPane.setLayout(new GridLayout(1, 2));
		JLabel label7 = new JLabel("fit up to turn number:");
		numberFormat.setMaximumFractionDigits(0);
		df7 = new DecimalField(len, 4, numberFormat);
		fitLengthPane.add(label7);
		fitLengthPane.add(df7);
		paramConf.add(fitLengthPane);

		JPanel fftPane = new JPanel();
		fftPane.setLayout(new GridLayout(1, 2));
		JLabel label8 = new JLabel("FFT array size: ");
		String[] fftChoice = { "16", "32", "64", "128", "256" };
		fftConf = new JComboBox<String>(fftChoice);
		fftConf.setSelectedIndex(2);
		fftConf.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if (((String) (((JComboBox) evt.getSource()).getSelectedItem()))
						.equals("16")) {
					fftSize = 16;
				} else if (((String) (((JComboBox) evt.getSource())
						.getSelectedItem())).equals("32")) {
					fftSize = 32;
				} else if (((String) (((JComboBox) evt.getSource())
						.getSelectedItem())).equals("64")) {
					fftSize = 64;
				} else if (((String) (((JComboBox) evt.getSource())
						.getSelectedItem())).equals("128")) {
					fftSize = 128;
				} else if (((String) (((JComboBox) evt.getSource())
						.getSelectedItem())).equals("256")) {
					fftSize = 256;
				}
			}
		});
		fftConf.setPreferredSize(new Dimension(30, 18));
		fftPane.add(label8);
		fftPane.add(fftConf);
		paramConf.add(fftPane);

		JPanel paramConfBtn = new JPanel();
		EdgeLayout edgeLayout3 = new EdgeLayout();
		paramConfBtn.setLayout(edgeLayout3);
		JButton done = new JButton("OK");
		done.setActionCommand("paramsSet");
		done.addActionListener(this);
		edgeLayout3.setConstraints(done, 0, 50, 0, 0, EdgeLayout.LEFT_BOTTOM,
				EdgeLayout.NO_GROWTH);
		paramConfBtn.add(done);
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("cancelConf");
		cancel.addActionListener(this);
		edgeLayout3.setConstraints(cancel, 0, 170, 0, 0,
				EdgeLayout.LEFT_BOTTOM, EdgeLayout.NO_GROWTH);
		paramConfBtn.add(cancel);
		configDialog.getContentPane().setLayout(new BorderLayout());
		configDialog.getContentPane().add(fitFunction, BorderLayout.NORTH);
		configDialog.getContentPane().add(paramConf, BorderLayout.CENTER);
		configDialog.getContentPane().add(paramConfBtn, BorderLayout.SOUTH);

		edgeLayout1.setConstraints(selection, 350, 10, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		bpmPane.add(selection);
		// selectedBPMName.setText(selectedBPM);

		edgeLayout.setConstraints(bpmPane, 10, 10, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		add(bpmPane);

		// show results
		plotDisplayPane = new JTabbedPane();
		plotDisplayPane.setPreferredSize(new Dimension(430, 600));
		plotDisplayPane.addTab("Phase", phasePlotPane);
		plotDisplayPane.addTab("Pos", posPlotPane);
		plotDisplayPane.addTab("phase diff.", phaseDiffPlotPane);
		edgeLayout.setConstraints(plotDisplayPane, 0, 480, 0, 0,
				EdgeLayout.TOP, EdgeLayout.NO_GROWTH);

		EdgeLayout el2 = new EdgeLayout();
		phasePlotPane.setLayout(el2);
		xPhasePlotPane = new BPMPlotPane(2);
		el2.setConstraints(xPhasePlotPane, 20, 20, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		phasePlotPane.add(xPhasePlotPane);
		yPhasePlotPane = new BPMPlotPane(3);
		el2.setConstraints(yPhasePlotPane, 245, 20, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		phasePlotPane.add(yPhasePlotPane);

		xBpmPlotPane = new BPMPlotPane(0);
		EdgeLayout el1 = new EdgeLayout();
		posPlotPane.setLayout(el1);
		el1.setConstraints(xBpmPlotPane, 20, 20, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		posPlotPane.add(xBpmPlotPane);
		JPanel xTunePanel = new JPanel();
		xTunePanel.setLayout(new GridLayout(1, 2));
		JLabel xTuneLabel = new JLabel("X Tune:");
		numberFormat.setMaximumFractionDigits(6);
		dfXTune = new JTextField(15);
		dfXTune.setForeground(Color.RED);
		xTunePanel.add(xTuneLabel);
		xTunePanel.add(dfXTune);
		el1.setConstraints(xTunePanel, 245, 20, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		posPlotPane.add(xTunePanel);

		yBpmPlotPane = new BPMPlotPane(1);
		el1.setConstraints(yBpmPlotPane, 275, 20, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		posPlotPane.add(yBpmPlotPane);
		JPanel yTunePanel = new JPanel();
		yTunePanel.setLayout(new GridLayout(1, 2));
		JLabel yTuneLabel = new JLabel("Y Tune:");
		dfYTune = new JTextField(15);
		dfYTune.setForeground(Color.RED);
		yTunePanel.add(yTuneLabel);
		yTunePanel.add(dfYTune);
		el1.setConstraints(yTunePanel, 500, 20, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		posPlotPane.add(yTunePanel);

		xPhDiffPlotPane = new BPMPlotPane(4);
		phaseDiffPlotPane.add(xPhDiffPlotPane);
		yPhDiffPlotPane = new BPMPlotPane(5);
		phaseDiffPlotPane.add(yPhDiffPlotPane);

		// edgeLayout.setConstraints(posPlotPane, 15, 55, 0, 0, EdgeLayout.TOP,
		// EdgeLayout.NO_GROWTH);
		add(plotDisplayPane);

		for (int i = 0; i < allBPMs.size(); i++) {
			bpmTableModel.addRowName((allBPMs.get(i)).getId(), i);
			bpmTableModel.setValueAt("0", i, 1);
			bpmTableModel.setValueAt("0", i, 2);
			bpmTableModel.setValueAt("0", i, 3);
			bpmTableModel.setValueAt("0", i, 4);
			bpmTableModel.setValueAt(new Boolean(false), i, 5);
		}

		// InputPVTableCell rbPVCell;
		// ChannelFactory caF = ChannelFactory.defaultFactory();

		setPVChs = new Channel[qPSs.size()];
		rbPVChs = new Channel[qPSs.size()];

		setPVCell = new InputPVTableCell[qPSs.size()];
		rbPVCell = new InputPVTableCell[qPSs.size()];

		for (int i = 0; i < qPSs.size(); i++) {
			MagnetMainSupply mps = qPSs.get(i);
			setPVChs[i] = mps.getChannel(MagnetMainSupply.FIELD_SET_HANDLE);
			rbPVChs[i] = mps.getChannel(MagnetMainSupply.FIELD_RB_HANDLE);
			quadTableModel.addRowName(mps.getId(), i);
			// quadTableModel.setValueAt("0", i, 1);
			// quadTableModel.setValueAt("0", i, 2);
			quadTableModel.setValueAt("0", i, 3);
			
			
		}
	}

	protected void connectAll() {
		for (int i = 0; i < qPSs.size(); i++) {

			ConnectPV connectPV1 = new ConnectPV(setPVChs[i], this);
			Thread thread1 = new Thread(connectPV1);
			thread1.start();

			ConnectPV connectPV2 = new ConnectPV(rbPVChs[i], this);
			Thread thread2 = new Thread(connectPV2);
			thread2.start();

			getChannelVec(setPVChs[i]).add(setPVCell[i]);
			getChannelVec(rbPVChs[i]).add(rbPVCell[i]);

			Channel.flushIO();
		}

		final TableProdder prodder = new TableProdder(quadTableModel);
		prodder.start();
		
	}

	private void tuneByFit() {
		 goodBPMs = new ArrayList<String>();
		// prepare for quad table
		if (isOnline) {
			if (!quadTableInit) {
				for (int i = 0; i < qPSs.size(); i++) {
					setPVCell[i] = new InputPVTableCell(setPVChs[i], i, 1);
					quadTableModel.addPVCell(setPVCell[i], i, 1);
					
					rbPVCell[i] = new InputPVTableCell(rbPVChs[i], i, 2);
					quadTableModel.addPVCell(rbPVCell[i], i, 2);
				}
				
			}
			quadTableInit = true;
		}

		HashMap<String, double[][]> bpmMap = null;
		quadTableModel.setAppMode(isOnline);
		
		if (!isOnline) {
			bpmPVLogId = myDoc.bpmPVLogId;
			defPVLogId = myDoc.defPVLogId;
			RingBPMTBTPVLog pvLog = new RingBPMTBTPVLog(bpmPVLogId);
			bpmMap = pvLog.getBPMMap();
			
		} else {
			connectAll();
		}
		
		// run online model once here for BPM phase plot
		TransferMapProbe myProbe = ProbeFactory.getTransferMapProbe(myDoc
				.getSelectedSequence(), new TransferMapTracker());
		Scenario scenario;

		HashMap<String, Double[]> goodBPMdata = new HashMap<String, Double[]>();

		try {
			scenario = Scenario.newScenarioFor(myDoc.getSelectedSequence());
			scenario.setProbe(myProbe);
			scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
			scenario.resetProbe();
			scenario.resync();
			scenario.run();

			TransferMapTrajectory traj = (TransferMapTrajectory) scenario
					.getTrajectory();

			double xSum = 0.;
			double ySum = 0.;
			double xAvgTune = 0.;
			double yAvgTune = 0.;
			int counter = 0;
			ArrayList<Double> xList = new ArrayList<Double>();
			ArrayList<Double> yList = new ArrayList<Double>();
			
			for (int i = 0; i < allBPMs.size(); i++) {
				// BPM theBPM = (BPM) (myDoc.getSelectedSequence()
				// .getNodeWithId(selectedBPM));

				BPM theBPM =  allBPMs.get(i);
				posArray[i] = myDoc.getSelectedSequence().getPosition(theBPM);
				// calculate the tune
				tuneMeasurement[i] = new TuneMeasurement();
				
				// for PV Logger data
				if (!isOnline) {
					tuneMeasurement[i].setBPMData(bpmMap.get(theBPM.getId()));
				}
								
				tuneMeasurement[i].setBPM(theBPM);
				// tuneMeasurement.setFitParameters(A, c, w, b, d, maxTime,
				// len);
				tuneMeasurement[i].setFitParameters(maxTime, len);
				if (!badBPMs.contains(new Integer(i))) {
					Thread thread = new Thread(tuneMeasurement[i]);
					thread.start();

					try {
						thread.join();

						xTune[i] = tuneMeasurement[i].getXTune();
						yTune[i] = tuneMeasurement[i].getYTune();

						if (tuneMeasurement[i].getXPhase() > -100.
								&& tuneMeasurement[i].getXPhase() < 0.) {
							xPhase[i] = tuneMeasurement[i].getXPhase()
									- Math.floor(tuneMeasurement[i].getXPhase()
											/ Math.PI / 2.) * Math.PI * 2.;
						} else if (tuneMeasurement[i].getXPhase() < 100.
								&& tuneMeasurement[i].getXPhase() >= 0.) {
							xPhase[i] = Math.ceil(tuneMeasurement[i]
									.getXPhase()
									/ Math.PI / 2.)
									* Math.PI
									* 2.
									- tuneMeasurement[i].getXPhase();
						} else {
							xPhase[i] = 0.;
						}
						if (tuneMeasurement[i].getYPhase() > -100.
								&& tuneMeasurement[i].getYPhase() < 0.) {
							yPhase[i] = tuneMeasurement[i].getYPhase()
									- Math.floor(tuneMeasurement[i].getYPhase()
											/ Math.PI / 2.) * Math.PI * 2.;
						} else if (tuneMeasurement[i].getXPhase() < 100.
								&& tuneMeasurement[i].getXPhase() >= 0.) {
							yPhase[i] = Math.ceil(tuneMeasurement[i]
									.getYPhase()
									/ Math.PI / 2.)
									* Math.PI
									* 2.
									- tuneMeasurement[i].getYPhase();
						} else {
							yPhase[i] = 0.;
						}
						
						// exclude bad fit BPMs
						if (xPhase[i] != 0. && yPhase[i] != 0. && xTune[i] > 0.08 
								&& yTune[i] > 0.08 && xTune[i] < 0.45 && yTune[i] < 0.45) {
							xSum += xTune[i];
							ySum += yTune[i];
							xList.add(new Double(xTune[i]));
							yList.add(new Double(yTune[i]));
							counter++;
							
							Double[] xyPair = new Double[2];
							xyPair[0] = new Double(xPhase[i]);
							xyPair[1] = new Double(yPhase[i]);
							goodBPMdata.put(theBPM.getId(), xyPair);
							goodBPMs.add(theBPM.getId());
						}

					} catch (InterruptedException ie) {
						System.out.println("tune calculation for "
								+ theBPM.getId() + " did not exit normally!");

						xTune[i] = 0.;
						yTune[i] = 0.;
						xPhase[i] = 0.;
						yPhase[i] = 0.;
					}
				} else {
					xTune[i] = 0.;
					yTune[i] = 0.;
				}

				numberFormat.setMaximumFractionDigits(4);
				bpmTableModel.setValueAt(numberFormat.format(xTune[i]), i, 1);
				bpmTableModel.setValueAt(numberFormat.format(xPhase[i]), i, 2);
				bpmTableModel.setValueAt(numberFormat.format(yTune[i]), i, 3);
				bpmTableModel.setValueAt(numberFormat.format(yPhase[i]), i, 4);

			}


			xPhaseDiff = new double[goodBPMs.size()];
			yPhaseDiff = new double[goodBPMs.size()];
			goodPosArry = new double[goodBPMs.size()];
			xDiffPlot = new double[goodBPMs.size()];
			yDiffPlot = new double[goodBPMs.size()];
			
			double[] xModelPhase = new double[goodBPMs.size()];
			double[] yModelPhase = new double[goodBPMs.size()];
			
			double xPhaseDiff0 = goodBPMdata.get(goodBPMs.get(0))[0].doubleValue();
			double yPhaseDiff0 = goodBPMdata.get(goodBPMs.get(0))[1].doubleValue();
			
	        // CKA - Create a machine parameter processor for the ring to replace
	        //    the machine parameters that were previously processed in the
	        //    simulation data itself.
	        RingCalculations     cmpRingParams = new RingCalculations(traj);
	        
	        // CKA - This
	        R3   vecPhaseModel0 = cmpRingParams.ringBetatronPhaseAdvance();
	        
	        double xModelPhase0 = vecPhaseModel0.getx();
	        double yModelPhase0 = vecPhaseModel0.gety();
	        
	        // Replaces this
	        
//			// get the 1st good BPM betatron phase as the reference
//			TransferMapState state0 = (TransferMapState) traj
//					.stateForElement(goodBPMs.get(0));
//			double xModelPhase0 = state0.getBetatronPhase().getx();
//			double yModelPhase0 = state0.getBetatronPhase().gety();

	        
			for (int i=0; i<goodBPMs.size(); i++) {
				// xPhaseDiff[i] = (xPhase[i] - xPhase[0]);
				// yPhaseDiff[i] = (yPhase[i] - yPhase[0]);
				goodPosArry[i] = myDoc.getSelectedSequence().getPosition(myDoc.getSelectedSequence().getNodeWithId(goodBPMs.get(i)));
				xPhaseDiff[i] = goodBPMdata.get(goodBPMs.get(i))[0].doubleValue() - xPhaseDiff0;
				yPhaseDiff[i] = goodBPMdata.get(goodBPMs.get(i))[1].doubleValue() - yPhaseDiff0;
				if (xPhaseDiff[i] < 0.)
					xPhaseDiff[i] = xPhaseDiff[i] + 2. * Math.PI;
				if (yPhaseDiff[i] < 0.)
					yPhaseDiff[i] = yPhaseDiff[i] + 2. * Math.PI;
				
				// get model BPM phase difference
				TransferMapState state = (TransferMapState) traj
				.stateForElement(goodBPMs.get(i));
				
				// CKA - This
				R3  vecPhaseBpm = cmpRingParams.computeBetatronPhase(state);
				
				xModelPhase[i] = vecPhaseBpm.getx() - xModelPhase0;
				yModelPhase[i] = vecPhaseBpm.gety() - yModelPhase0;

				// Replaces this
//				xModelPhase[i] = state.getBetatronPhase().getx()
//				- xModelPhase0;
//				yModelPhase[i] = state.getBetatronPhase().gety()
//				- yModelPhase0;
				
				if (xModelPhase[i] < 0.)
					xModelPhase[i] = xModelPhase[i] + 2. * Math.PI;
				if (yModelPhase[i] < 0.)
					yModelPhase[i] = yModelPhase[i] + 2. * Math.PI;
				
				// calculate diff between measured difference and model
				// predicted difference
				xDiffPlot[i] = xPhaseDiff[i] - xModelPhase[i];
				yDiffPlot[i] = yPhaseDiff[i] - yModelPhase[i];
				if (xDiffPlot[i] > 4.)
					xDiffPlot[i] = xDiffPlot[i] - 2.*Math.PI;
				if (xDiffPlot[i] < -4.)
					xDiffPlot[i] = xDiffPlot[i] + 2.*Math.PI;
				if (yDiffPlot[i] > 4.)
					yDiffPlot[i] = yDiffPlot[i] - 2.*Math.PI;
				if (yDiffPlot[i] < -4.)
					yDiffPlot[i] = yDiffPlot[i] + 2.*Math.PI;
				
			}
			
			if (counter != 0) {
				xAvgTune = xSum / counter;
				yAvgTune = ySum / counter;
			}

			double xSig = 0.;
			double ySig = 0.;

			for (int i = 0; i < xList.size(); i++) {
				xSig = xSig + (xList.get(i).doubleValue() - xAvgTune)
						* (xList.get(i).doubleValue() - xAvgTune);
				ySig = ySig + (yList.get(i).doubleValue() - yAvgTune)
						* (yList.get(i).doubleValue() - yAvgTune);
			}
			xSig = Math.sqrt(xSig) / xList.size();
			ySig = Math.sqrt(ySig) / yList.size();

			xTuneAvg.setForeground(Color.blue);
			yTuneAvg.setForeground(Color.blue);
			
			numberFormat.setMaximumFractionDigits(4);
			
			xTuneAvg.setText("avg. x tune = " + numberFormat.format(xAvgTune)
					+ "+/-" + numberFormat.format(xSig));
			yTuneAvg.setText("avg. y tune = " + numberFormat.format(yAvgTune)
					+ "+/-" + numberFormat.format(ySig));
			System.out.println("Got " + xList.size() + " sets of BPM data.");

			xPhasePlotPane.setDataArray(posArray, xPhase);
//			xPhasePlotPane.setDataArray(goodPosArry, xPhaseDiff);
			xPhasePlotPane.plot();
			yPhasePlotPane.setDataArray(posArray, yPhase);
//			yPhasePlotPane.setDataArray(goodPosArry, yPhaseDiff);
			yPhasePlotPane.plot();
			xPhDiffPlotPane.setDataArray(goodPosArry, xDiffPlot);
//			xPhDiffPlotPane.setDataArray(goodPosArry, xModelPhase);
			xPhDiffPlotPane.plot();
			yPhDiffPlotPane.setDataArray(goodPosArry, yDiffPlot);
//			yPhDiffPlotPane.setDataArray(goodPosArry, yModelPhase);
			yPhDiffPlotPane.plot();

		} catch (ModelException e) {
			System.out.println(e);
		}


		hasTune = true;

		// take a snapshot of the quad settings, if it's for live machine
		if (isOnline) {
			qSetVals = new double[qPSs.size()];
			for (int i = 0; i < qPSs.size(); i++) {
				try {
					qSetVals[i] = qPSs.get(i).getFieldSetting();
					// quadTableModel.setValueAt(numberFormat.format(qSetVals[i]),
					// i, 1);
				} catch (ConnectionException ce) {
					System.out.println(ce);
				} catch (GetException ge) {
					System.out.println(ge);
				}
			}
			// calculate best quad settings
			cqs = new CalcQuadSettings((Ring) myDoc.getSelectedSequence(), goodBPMs, this);
		}

		quadCorrBtn.setEnabled(true);
		dumpData.setEnabled(true);

		
	}

	private void tuneByFFT() {
		
		HashMap<String, double[][]> bpmMap = null;
		quadTableModel.setAppMode(isOnline);
		
		if (!isOnline) {
			bpmPVLogId = myDoc.bpmPVLogId;
			defPVLogId = myDoc.defPVLogId;
			RingBPMTBTPVLog pvLog = new RingBPMTBTPVLog(bpmPVLogId);
			bpmMap = pvLog.getBPMMap();
			
		} else {
			connectAll();
		}
		
		double xSum = 0.;
		double ySum = 0.;
		double xAvgTune = 0.;
		double yAvgTune = 0.;
		int xCounter = 0;
		int yCounter = 0;
		ArrayList<Double> xList = new ArrayList<Double>();
		ArrayList<Double> yList = new ArrayList<Double>();
		
		for (int i = 0; i < allBPMs.size(); i++) {
			// BPM theBPM = (BPM) (myDoc.getSelectedSequence()
			// .getNodeWithId(selectedBPM));

			BPM theBPM =  allBPMs.get(i);
			posArray[i] = myDoc.getSelectedSequence().getPosition(theBPM);
			// calculate the tune
			tuneMeasurement[i] = new TuneMeasurement();
			
			// for PV Logger data
			if (!isOnline) {
				tuneMeasurement[i].setBPMData(bpmMap.get(theBPM.getId()));
			}
			
			tuneMeasurement[i].setFFTArraySize(fftSize);
			tuneMeasurement[i].setTuneFromFit(false);
			tuneMeasurement[i].setBPM(theBPM);

			if (!badBPMs.contains(new Integer(i))) {
				Thread thread = new Thread(tuneMeasurement[i]);
				thread.start();

				try {
					thread.join();
					
					xTune[i] = tuneMeasurement[i].getXTune();
					xPhase[i] = 0.;
					yTune[i] = tuneMeasurement[i].getYTune();
					yPhase[i] = 0.;
					
					if (xTune[i] > 0.08 && xTune[i] < 0.45) {
						xSum += xTune[i];
						xList.add(new Double(xTune[i]));
						xCounter++;						
					}
					
					if (yTune[i] > 0.08 && xTune[i] < 0.45) {
						ySum += yTune[i];
						yList.add(new Double(yTune[i]));
						yCounter++;						
					}
					
					numberFormat.setMaximumFractionDigits(4);
					bpmTableModel.setValueAt(numberFormat.format(xTune[i]), i,
							1);
					bpmTableModel.setValueAt(numberFormat.format(xPhase[i]), i,
							2);
					bpmTableModel.setValueAt(numberFormat.format(yTune[i]), i,
							3);
					bpmTableModel.setValueAt(numberFormat.format(yPhase[i]), i,
							4);
				} catch (InterruptedException ie) {

				}
			}
		}

		
		if (xCounter != 0) {
			xAvgTune = xSum / xCounter;
		}
		if (yCounter != 0) {
			yAvgTune = ySum / yCounter;
		}
		

		double xSig = 0.;
		double ySig = 0.;

		for (int i = 0; i < xList.size(); i++) {
			xSig = xSig + (xList.get(i).doubleValue() - xAvgTune)
					* (xList.get(i).doubleValue() - xAvgTune);
		}
		for (int i = 0; i < yList.size(); i++) {
			ySig = ySig + (yList.get(i).doubleValue() - yAvgTune)
			* (yList.get(i).doubleValue() - yAvgTune);			
		}
		
		xSig = Math.sqrt(xSig) / xList.size();
		ySig = Math.sqrt(ySig) / yList.size();

		xTuneAvg.setForeground(Color.blue);
		yTuneAvg.setForeground(Color.blue);
		xTuneAvg.setText("avg. x tune = " + numberFormat.format(xAvgTune)
				+ "+/-" + numberFormat.format(xSig));
		yTuneAvg.setText("avg. y tune = " + numberFormat.format(yAvgTune)
				+ "+/-" + numberFormat.format(ySig));
		System.out.println("Got " + xList.size() + " sets of BPM x data.");
		System.out.println("Got " + yList.size() + " sets of BPM y data.");
				
		hasTune = true;
		
	}

	protected void plotBPMData(int ind) {
		// plot for the selected BPM
		// plot BPM data
		xBpmPlotPane.setDataArray(tuneMeasurement[ind].getXArray());
		xBpmPlotPane.setFittedData(tuneMeasurement[ind].getXFittedData());
		xBpmPlotPane.plot();
		yBpmPlotPane.setDataArray(tuneMeasurement[ind].getYArray());
		yBpmPlotPane.setFittedData(tuneMeasurement[ind].getYFittedData());
		yBpmPlotPane.plot();

		// update form text
		numberFormat.setMaximumFractionDigits(4);
		String xtune = numberFormat.format(xTune[ind]) + " +/- "
				+ numberFormat.format(tuneMeasurement[ind].getXTuneError());
		dfXTune.setText(xtune);
		String ytune = numberFormat.format(yTune[ind]) + " +/- "
				+ numberFormat.format(tuneMeasurement[ind].getYTuneError());
		dfYTune.setText(ytune);
	}

	public void actionPerformed(ActionEvent ev) {
		// pop-up dialog for changing fit/FFT parameters
		if (ev.getActionCommand().equals("configuration")) {
			configDialog.setVisible(true);
		} else if (ev.getActionCommand().equals("paramsSet")) {
			/*
			 * A = df1.getValue(); c = df2.getValue(); w = df3.getValue(); b =
			 * df4.getValue(); d = df5.getValue();
			 */maxTime = Math.round((int) df6.getDoubleValue());
			len = Math.round((int) df7.getDoubleValue());
			configDialog.setVisible(false);
		} else if (ev.getActionCommand().equals("cancelConf")) {
			configDialog.setVisible(false);
		} else if (ev.getActionCommand().equals("findQuadError")) {
			progBar.setValue(0);

			if (!isOnline) {													
					PVLoggerDataSource plds = new PVLoggerDataSource(defPVLogId);
					
					final Map<String, Double> quadMap = plds.getMagnetPSMap();
					
					// take a snapshot of the quad settings
					qSetVals = new double[qPSs.size()];
					for (int i = 0; i < qPSs.size(); i++) {
						qSetVals[i] = quadMap.get(qPSs.get(i).getChannel(MagnetMainSupply.FIELD_SET_HANDLE).getId());
						quadTableModel.setValueAt(numberFormat.format(qSetVals[i]), i, 1);						
					}
					
					// calculate best quad settings
					cqs = new CalcQuadSettings((Ring) myDoc.getSelectedSequence(), goodBPMs, this);
					
					Thread thread = new Thread(cqs);
					thread.start();					
			} else {
			
				Thread thread = new Thread(cqs);
				thread.start();

				// setQuadBtn.setEnabled(true);
			}
		} else if (ev.getActionCommand().equals("setQuads")) {
			try {
				for (int i = 0; i < qPSs.size(); i++) {
					qPSs.get(i).setField(
							numberFormat.parse(
									(String) quadTableModel.getValueAt(i, 4))
									.doubleValue());
				}
			} catch (PutException pe) {
				System.out.println(pe);
			} catch (ConnectionException ce) {
				System.out.println(ce);
			} catch (ParseException pe) {
				System.out.println(pe);
			}
		} else if (ev.getActionCommand().equals("dumpData")) {
			String currentDirectory = _datFileTracker.getRecentFolderPath();

			JFileChooser fileChooser = new JFileChooser(currentDirectory);

			int status = fileChooser.showSaveDialog(this);
			if (status == JFileChooser.APPROVE_OPTION) {
				_datFileTracker.cacheURL(fileChooser.getSelectedFile());
				File file = fileChooser.getSelectedFile();

				try {
					FileWriter fileWriter = new FileWriter(file);
					NumberFormat nf = NumberFormat.getNumberInstance();
					nf.setMaximumFractionDigits(5);
					nf.setMinimumFractionDigits(5);

					// write BPM data
					fileWriter.write("BPM_Id\t\t\t" + "s\t\t" + "xTune\t"
							+ "xPhase\t" + "yTune\t" + "yPhase" + "\n");

					// numberFormat.setMaximumFractionDigits(6);

					for (int i = 0; i < xTune.length; i++) {
						fileWriter.write(( allBPMs.get(i)).getId()
								+ "\t"
								+ numberFormat.format(myDoc
										.getSelectedSequence().getPosition(
												 allBPMs.get(i))) + "\t"
								+ numberFormat.format(xTune[i]) + "\t"
								+ numberFormat.format(xPhase[i]) + "\t"
								+ numberFormat.format(yTune[i]) + "\t"
								+ numberFormat.format(yPhase[i]) + "\n");
					}

					String comments = startTime.toString();
					comments = comments + "\n"
							+ "For Ring Measurement Application";
					snapshot.setComment(comments);
					snapshot1.setComment(comments);
					loggerSession.publishSnapshot(snapshot);
					loggerSession1.publishSnapshot(snapshot1);
					pvLoggerId = snapshot.getId();
					pvLoggerId1 = snapshot1.getId();

					fileWriter.write("PVLoggerID = " + pvLoggerId
							+ "\tPVLoggerId = " + pvLoggerId1 + "\n");

					fileWriter.close();

				} catch (IOException ie) {
					JFrame frame = new JFrame();
					JOptionPane.showMessageDialog(frame, "Cannot open the file"
							+ file.getName() + "for writing", "Warning!",
							JOptionPane.PLAIN_MESSAGE);

					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				}
			}
		}

	}

	protected void setSelectedBPM(String theBPM) {
		selectedBPM = theBPM;
		System.out.println("Selected BPM = " + selectedBPM);
	}

	protected String getSelectedBPM() {
		return selectedBPM;
	}

	/** ConnectionListener interface */
	public void connectionMade(Channel aChannel) {
		connectMons(aChannel);
	}

	/** ConnectionListener interface */
	public void connectionDropped(Channel aChannel) {
	}

	/** internal method to connect the monitors */
	private void connectMons(Channel p_chan) {
		Vector<InputPVTableCell> chanVec;

		try {
			chanVec = getChannelVec(p_chan);
			for (int i = 0; i < chanVec.size(); i++) {
				mons.add(p_chan.addMonitorValue( chanVec.elementAt(i), Monitor.VALUE));
			}
			chanVec.removeAllElements();

		} catch (ConnectionException e) {
			System.out.println("Connection Exception");
		} catch (MonitorException e) {
			System.out.println("Monitor Exception");
		}
	}

	/** get the list of table cells monitoring the prescibed channel */
	private Vector<InputPVTableCell> getChannelVec(Channel p_chan) {
		if (!monitorQueues.containsKey(p_chan.channelName())) {
			monitorQueues.put(p_chan.channelName(),
					new Vector<InputPVTableCell>());
		}

		return monitorQueues.get(p_chan.channelName());
	}

    protected void setAppMode(boolean isOn) {
        isOnline = isOn;
    }
    
}
