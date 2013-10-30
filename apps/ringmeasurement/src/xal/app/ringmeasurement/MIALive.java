/*
 * MIALive.java
 *
 * Created on June 30, 2010
 *
 * Copyright (c) 2001-2010 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 */

package xal.app.ringmeasurement;

import java.io.*;                                                              
import java.util.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.Component;
import java.text.NumberFormat;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.*;
import javax.swing.table.*;

import java.text.ParseException;
import java.lang.Math.*;
import java.lang.String;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import xal.tools.apputils.EdgeLayout;
import xal.smf.impl.*;
import xal.smf.Ring;
import xal.tools.swing.DecimalField;
import xal.ca.*;
import xal.tools.plot.*;
import xal.tools.apputils.files.*;
import xal.service.pvlogger.*;
//import xal.tools.pvlogger.query.*;
import xal.tools.database.*;
import xal.tools.beam.Twiss;
import xal.tools.beam.calc.RingParameters;
import xal.model.*;
import xal.tools.fit.LinearFit;
import xal.model.probe.TransferMapProbe;
import xal.sim.scenario.ProbeFactory;
import xal.model.alg.TransferMapTracker;
import xal.sim.scenario.*;
import xal.model.probe.Probe;
import xal.model.probe.traj.*;
//import xal.model.probe.traj.BeamProbeState;
import xal.sim.sync.PVLoggerDataSource;
import xal.smf.*;
import xal.tools.xml.*;
import xal.tools.data.*;
import xal.smf.application.*;
import xal.smf.*;
import xal.smf.data.XMLDataManager;
import xal.application.*;


/**
 * @author cp3, tep
 * Class for calculating beta functions via MIA analysis from live BPM data.   
 */
 
/**
 * Class <code></code>.
 *
 *
 * @author Christopher K. Allen
 * @since  Oct 30, 2013
 */
public class MIALive extends JPanel{
    
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;

	/* Main display objects */
	EdgeLayout edgeLayout = new EdgeLayout();
	JPanel mainPanel= new JPanel();

	/* Buttons to begin and pause beta plotting */
	JButton startButton;
	JButton pauseButton;
	JLabel activeLabel;

	/* Components for exporting data to a file */
	JButton exportButton;
	JFileChooser exportFile;

	/* Components of the beta function scaling selector */
	JLabel xScaleElementLabel;
	JLabel yScaleElementLabel;
	JLabel xScaleValueLabel;
	JLabel yScaleValueLabel;
	private String[] xScaleElements = {"No Scaling"};
	private String[] yScaleElements = {"No Scaling"};
	private JComboBox<String> xScaleSelector = new JComboBox<String>(xScaleElements);
	private JComboBox<String> yScaleSelector = new JComboBox<String>(yScaleElements);
	DecimalField xScaleValue;
	DecimalField yScaleValue;
	NumberFormat numForm = NumberFormat.getNumberInstance();

	/* Components of the number of turns selector */
	JLabel numTurnsLabel;
	DecimalField numTurnsField;
	int numTurns;

	/* Components to select/deselect static design plots */
	JCheckBox xDesignCheck;
	JCheckBox yDesignCheck;
	Boolean xDesignFlag = false;
	Boolean yDesignFlag = false;

	/* Components to select/deselect live design plots */
	JCheckBox xLiveDesignCheck;
	JCheckBox yLiveDesignCheck;
	Boolean xLiveDesignFlag = false;
	Boolean yLiveDesignFlag = false;

	/* Components to select/deselect drawing lines */
	JCheckBox xLinesCheck;
	JCheckBox yLinesCheck;
	Boolean xLinesFlag = true;
	Boolean yLinesFlag = true;

	/* Components to select/deselect the use of averaging */
	JCheckBox xAvgCheck;
	JCheckBox yAvgCheck;
	Boolean xAvgFlag = false;
	Boolean yAvgFlag = false;

	/* Components of the beta plots */
	JLabel betaXPlotLabel;
	JLabel betaYPlotLabel;
	FunctionGraphsJPanel betaXGraphPanel;
	FunctionGraphsJPanel betaYGraphPanel;
	BasicGraphData xBetaLive;
	BasicGraphData yBetaLive;
	BasicGraphData xBetaDesign;
	BasicGraphData yBetaDesign;
	BasicGraphData xBetaLiveDesign;
	BasicGraphData yBetaLiveDesign;
	private double[] BPMPositionPlot;
	private double[] xBetaPlot;
	private double[] yBetaPlot;
	double[] posDesignPlot;
	double[] xBetaDesignPlot;
	double[] yBetaDesignPlot;
	double[] posLiveDesignPlot;
	double[] xBetaLiveDesignPlot;
	double[] yBetaLiveDesignPlot;

	/* Components of the BPM selector table */
	JScrollPane BPMSelectorPane;
	private JTable BPMTable;
	private DataTableModel BPMDataTableModel;
	private String[] BPMSelColNames = {"Name", "Position", "Use?"};

	/* Components for setting up the accelerator */
	public AcceleratorSeqCombo seq;
	Accelerator accl = new Accelerator();
	Probe probe;
	Scenario scenario;
	Trajectory traj;
	ProbeState state;
	Twiss[] twiss = new Twiss[2];

	/* Structures to store BPM data */
	ArrayList<AcceleratorNode> BPMList = new ArrayList<AcceleratorNode>();
	int numBPMs;
	double[][] xTBTArray;
	double[][] yTBTArray;
	BPMAgent[] BPMAgents;

	/* Components of the timer for updating the plots */
	int refreshRate;
	ActionListener tasksOnTimer;
	Timer updateTimer;
	
	/* Variables for the MIAanalysis method */
	Matrix xBPMMatrix;
	Matrix yBPMMatrix;
	SingularValueDecomposition xSVD;
	SingularValueDecomposition ySVD;
	double[] xSigma;
	double[] ySigma;
	Matrix xUMatrix;
	Matrix yUMatrix;
	Matrix xVMatrix;
	Matrix yVMatrix;
	double[] xBeta;
	double[] yBeta;
	double[] BPMPosition;
	double xScaleFactor;
	double yScaleFactor;


	/* Member function Constructor */
	public MIALive() {
		makeComponents();		/* Creation of all GUI components */
		addComponents();		/* Add all components to the layout and panels */
		acclSetup();			/* Read current accelerator, make lattice 
								 * sequence, and get BPM nodes 
								 */
		makeBPMConnections();	/* Create a monitor via BPMAgent to receive BPM
								 * data
								 */
		makeTimer();			/* Create the timer */
		makeScaleSelector();	/* Create the mechanism for selecting the scale
								 * of the beta plots
								 */
		makeBPMSelectorTable();	/* Create the BPM selection table */
		makeDesignData();		/* Get the data for the static design beta plots */
		makeLiveDesignData();	/* Get the data for the live design beta plots */
		setAction();			/* Set the action listeners */
	}


	protected void addComponents() {
		edgeLayout.setConstraints(mainPanel, 0, 0, 0, 0, EdgeLayout.ALL_SIDES,
								  EdgeLayout.GROW_BOTH);
		this.add(mainPanel);

		EdgeLayout newLayout = new EdgeLayout();
		mainPanel.setLayout(newLayout);

		// add start and pause buttons
		newLayout.add(startButton, mainPanel, 5, 5, EdgeLayout.LEFT);
		newLayout.add(pauseButton, mainPanel, 10, 35, EdgeLayout.LEFT);
		newLayout.add(activeLabel, mainPanel, 47, 63, EdgeLayout.LEFT);
		
		newLayout.add(exportButton, mainPanel, 145, 35, EdgeLayout.LEFT);

		// add scale factor mechanisms
		newLayout.add(xScaleElementLabel, mainPanel, 330, 10, EdgeLayout.LEFT);
		newLayout.add(xScaleSelector, mainPanel, 410, 5, EdgeLayout.LEFT);
		newLayout.add(xScaleValueLabel, mainPanel, 540, 10, EdgeLayout.LEFT);
		newLayout.add(xScaleValue, mainPanel, 600, 5, EdgeLayout.LEFT);
		newLayout.add(yScaleElementLabel, mainPanel, 330, 40, EdgeLayout.LEFT);
		newLayout.add(yScaleSelector, mainPanel, 410, 35, EdgeLayout.LEFT);
		newLayout.add(yScaleValueLabel, mainPanel, 540, 40, EdgeLayout.LEFT);
		newLayout.add(yScaleValue, mainPanel, 600, 35, EdgeLayout.LEFT);

		// add turn quantity selector
		newLayout.add(numTurnsLabel, mainPanel, 145, 10, EdgeLayout.LEFT);
		newLayout.add(numTurnsField, mainPanel, 255, 5, EdgeLayout.LEFT);

		// add line drawing check boxes
		newLayout.add(xLinesCheck, mainPanel, 0, 80, EdgeLayout.LEFT);
		newLayout.add(yLinesCheck, mainPanel, 0, 430, EdgeLayout.LEFT);

		// add static design display check boxes
		newLayout.add(xDesignCheck, mainPanel, 100, 80, EdgeLayout.LEFT);
		newLayout.add(yDesignCheck, mainPanel, 100, 430, EdgeLayout.LEFT);

		// add live design display check boxes
		newLayout.add(xLiveDesignCheck, mainPanel, 210, 80, EdgeLayout.LEFT);
		newLayout.add(yLiveDesignCheck, mainPanel, 210, 430, EdgeLayout.LEFT);

		// add averaging check boxes
		newLayout.add(xAvgCheck, mainPanel, 315, 80, EdgeLayout.LEFT);
		newLayout.add(yAvgCheck, mainPanel, 315, 430, EdgeLayout.LEFT);

		// add plots and associated labels
		newLayout.add(betaXPlotLabel, mainPanel, 465, 85, EdgeLayout.LEFT);
		newLayout.add(betaXGraphPanel, mainPanel, 10, 100, EdgeLayout.LEFT);
		newLayout.add(betaYPlotLabel, mainPanel, 465, 435, EdgeLayout.LEFT);
		newLayout.add(betaYGraphPanel, mainPanel, 10, 450, EdgeLayout.LEFT);

		// add BPM selector table
		newLayout.add(BPMSelectorPane, mainPanel, 670, 0, EdgeLayout.LEFT);
	}     
    
	protected void makeComponents() {
		mainPanel = new JPanel();
		mainPanel.setPreferredSize(new Dimension(950,900));

		// make buttons
		startButton = new JButton("Start Plotting");
		pauseButton = new JButton("Pause Plots");
		activeLabel = new JLabel("");
		exportButton = new JButton("Export Data to File");

		// make beta function plots and labels
		betaXGraphPanel = new FunctionGraphsJPanel();
		betaYGraphPanel = new FunctionGraphsJPanel();
		betaXGraphPanel.setPreferredSize(new Dimension(930, 325));
		betaXGraphPanel.setGraphBackGroundColor(Color.WHITE);
		betaYGraphPanel.setPreferredSize(new Dimension(930, 325));
		betaYGraphPanel.setGraphBackGroundColor(Color.WHITE);
		betaXPlotLabel = new JLabel("Beta X");
		betaYPlotLabel = new JLabel("Beta Y");
		xBetaLive = new BasicGraphData();
		yBetaLive = new BasicGraphData();
		xBetaDesign = new BasicGraphData();
		yBetaDesign = new BasicGraphData();
		xBetaLiveDesign = new BasicGraphData();
		yBetaLiveDesign = new BasicGraphData();

		// make scale factor components
		xScaleElementLabel = new JLabel("Beta X Scale: ");
		yScaleElementLabel = new JLabel("Beta Y Scale: ");
		xScaleValueLabel = new JLabel("to Value: ");
		yScaleValueLabel = new JLabel("to Value: ");
		xScaleValue = new DecimalField(15, 4, numForm);
		yScaleValue = new DecimalField(15, 4, numForm);

		// make turn quantity selector
		numTurnsLabel = new JLabel("Number of turns: ");
		numTurnsField = new DecimalField(200, 4, numForm);
		numTurns = (int)numTurnsField.getDoubleValue();
		numForm.setMinimumFractionDigits(1);

		// make static design check boxes
		xDesignCheck = new JCheckBox("Static Design");
		yDesignCheck = new JCheckBox("Static Design");

		// make live design check boxes
		xLiveDesignCheck = new JCheckBox("Live Design");
		yLiveDesignCheck = new JCheckBox("Live Design");

		// make check boxes for toggling line drawing
		xLinesCheck = new JCheckBox("Draw Lines");
		xLinesCheck.setSelected(true);
		yLinesCheck = new JCheckBox("Draw Lines");
		yLinesCheck.setSelected(true);

		// make check boxes for averaging the beta functions
		xAvgCheck = new JCheckBox("Use Averaging");
		yAvgCheck = new JCheckBox("Use Averaging");

		// make BPM selector table
		BPMDataTableModel = new DataTableModel(BPMSelColNames, 0);
		BPMTable = new JTable(BPMDataTableModel);
		BPMSelectorPane = new JScrollPane(BPMTable,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		BPMSelectorPane.setPreferredSize(new Dimension(275, 100));
		
		exportFile = new JFileChooser();
	}


	protected void setAction() {
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				activeLabel.setText("Active");
				SwingUtilities.updateComponentTreeUI(activeLabel);
				updateTimer.start();
			}
		});
		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				updateTimer.stop();
				activeLabel.setText("Paused");
				SwingUtilities.updateComponentTreeUI(activeLabel);
			}
		});
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				updateTimer.stop();
				activeLabel.setText("Paused");
				SwingUtilities.updateComponentTreeUI(activeLabel);

				if (exportFile.showSaveDialog(MIALive.this)
						== JFileChooser.APPROVE_OPTION) {
					File file = exportFile.getSelectedFile();
					try {
						exportData(file);
					}
					catch (IOException e) {
						System.out.println(e);
					}
				}
			}
		});
		xLinesCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (xLinesFlag) {
					xLinesFlag = false;
				}
				else {
					xLinesFlag = true;
				}

			}
		});
		yLinesCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (yLinesFlag) {
					yLinesFlag = false;
				}
				else {
					yLinesFlag = true;
				}
				
			}
		});
		xDesignCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (xDesignFlag) {
					xDesignFlag = false;
				}
				else {
					xDesignFlag = true;
				}
				
			}
		});
		yDesignCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (yDesignFlag) {
					yDesignFlag = false;
				}
				else {
					yDesignFlag = true;
				}
				
			}
		});
		xLiveDesignCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (xLiveDesignFlag) {
					xLiveDesignFlag = false;
				}
				else {
					makeLiveDesignData();
					xLiveDesignFlag = true;
				}
				
			}
		});
		yLiveDesignCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (yDesignFlag) {
					yDesignFlag = false;
				}
				else {
					makeLiveDesignData();
					yDesignFlag = true;
				}
				
			}
		});
		xAvgCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (xAvgFlag) {
					xAvgFlag = false;
				}
				else {
					for (int i = 0; i < numBPMs; i++) {
						BPMAgents[i].clearXAvg();
					}
					xAvgFlag = true;
				}
				
			}
		});
		yAvgCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (yAvgFlag) {
					yAvgFlag = false;
				}
				else {
					for (int i = 0; i < numBPMs; i++) {
						BPMAgents[i].clearYAvg();
					}
					yAvgFlag = true;
				}
				
			}
		});
	}


	public void acclSetup() {
		accl = XMLDataManager.loadDefaultAccelerator();
		seq = accl.getComboSequence("Ring");
		BPMList = (ArrayList<AcceleratorNode>)seq.getNodesOfType("BPM");
		numBPMs = BPMList.size();
	}


	public void	makeBPMConnections() {
		BPMAgents = new BPMAgent[numBPMs];
		for (int i = 0; i < numBPMs; i++) {
			BPMAgents[i] = new BPMAgent(seq, (BPM)BPMList.get(i), numTurns);
		}
	}


	public void makeScaleSelector(){
		xScaleSelector.removeAllItems();
		yScaleSelector.removeAllItems();
		xScaleSelector.addItem(new String("No Scaling"));
		yScaleSelector.addItem(new String("No Scaling"));
		for (int i = 0; i < numBPMs; i++) {
			if (BPMAgents[i].isOkay()) {
				xScaleSelector.addItem(BPMList.get(i).toString().substring(10));
				yScaleSelector.addItem(BPMList.get(i).toString().substring(10));

			}
		}
	}


	public void makeBPMSelectorTable(){
		BPMTable.getColumnModel().getColumn(0).setWidth(100);
		BPMTable.getColumnModel().getColumn(1).setWidth(100);
		BPMTable.getColumnModel().getColumn(2).setWidth(75);
		BPMTable.setPreferredScrollableViewportSize(BPMTable.getPreferredSize());
		BPMTable.setRowSelectionAllowed(false);
		BPMTable.setColumnSelectionAllowed(false);
		BPMTable.setCellSelectionEnabled(false);
		BPMSelectorPane.getHorizontalScrollBar().setValue(1);
		BPMDataTableModel.fireTableDataChanged();

		String BPMName;
		for (int i = 0; i < numBPMs; i++) {
			ArrayList<Object> tableData = new ArrayList<Object>();
			tableData.add(BPMAgents[i].name().substring(14));
			tableData.add(BPMAgents[i].getPosition());
			if (BPMAgents[i].isOkay()) {
				tableData.add(new Boolean(true));
			} else {
				tableData.add(new Boolean(false));
			}

			BPMDataTableModel.addTableData(tableData);
		}
		BPMDataTableModel.fireTableDataChanged();
	}


	/**
	 * <p>
	 * No comments were provided.
	 * </p>
	 * <p>
	 * <h4>CKA NOTES:</h4>
	 * &middot; I have refactored some of the machine parameter calculations
	 * to work with the new machine parameter calculation component of the
	 * the online model.  This are marked in the code.
	 * <br/>
	 * <br/>
	 * &middot; I assume the processing done in this method is for ring data.  That is the way
	 * I treated it.
	 * <br/>
	 * <br/>
	 * &middot; I was forced to make downcasts since several of the variables
	 * were open typed, but I did not indicate any such exception as they should not break.
	 * </p>
	 *
	 * @author cp3  
	 * @author Christopher K. Allen
	 * @since  Jun 30, 2010
	 * @version  Oct 30, 2013
	 */
	public void makeDesignData() {
	    int numSelected = numberSelected();
	    posDesignPlot = new double[numSelected];
	    xBetaDesignPlot = new double[numSelected];
	    yBetaDesignPlot = new double[numSelected];

	    try {

	        try {


	            probe = ProbeFactory.getTransferMapProbe(seq, AlgorithmFactory.createTransferMapTracker( seq ));


	        } catch ( InstantiationException exception ) {
	            System.err.println( "Instantiation exception creating probe." );
	            exception.printStackTrace();
	        }

	        scenario = Scenario.newScenarioFor(seq);
	        scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
	        scenario.setStartElementId("Ring_Inj:Foil");
	        scenario.setProbe(probe);
	        scenario.resync();
	        scenario.run();
	        traj = probe.getTrajectory();

	        // CKA - Down cast the simulation trajectory results to the proper type then
	        //   create a ring parameter calculation engine for processing
	        TransferMapTrajectory  trjSimulation = (TransferMapTrajectory)traj;
	        RingParameters         cmpRingParams = new RingParameters(trjSimulation);

	        int j = 0;
	        for (int i = 0; i < BPMTable.getRowCount(); i++) {
	            if (((Boolean)BPMTable.getValueAt(i, 2)).booleanValue()) {
	                state = traj.stateForElement(BPMAgents[i].name());
	                
	                // CKA - Down cast the trajectory state to the proper type
	                //     then calculation the matched Twiss parameters at
	                //     the state location.
	                TransferMapState staXfer = (TransferMapState)state;
	                Twiss[]          arrTws  = cmpRingParams.getTwiss(staXfer);
	                
//	                twiss = ((TransferMapState)state).getTwiss();
	                twiss = arrTws;
	                
	                posDesignPlot[j] = state.getPosition();
	                xBetaDesignPlot[j] = twiss[0].getBeta();
	                yBetaDesignPlot[j] = twiss[1].getBeta();
	                j++;
	            }
	        }
	    }
	    catch (ModelException e) {
	        System.out.println(e);
	    }
	}


    /**
     * <p>
     * No comments were provided.
     * </p>
     * <p>
     * <h4>CKA NOTES:</h4>
     * &middot; The method reproduces most of the code in <code>{@link #makeDesignData()}</code>
     * so I will not repeat the above comments, other to say that I need to repeat the
     * same code as well.
     * <br/>
     * </p>
     *
     * @author cp3  
     * @author Christopher K. Allen
     * @since  Jun 30, 2010
     * @version  Oct 30, 2013
     */
	public void	makeLiveDesignData() {
		int numSelected = numberSelected();
		posLiveDesignPlot = new double[numSelected];
		xBetaLiveDesignPlot = new double[numSelected];
		yBetaLiveDesignPlot = new double[numSelected];
		
		try {
			
            try {
                
          
            probe = ProbeFactory.getTransferMapProbe(seq, AlgorithmFactory.createTransferMapTracker( seq ));
            
        } catch ( InstantiationException exception ) {
            System.err.println( "Instantiation exception creating probe." );
            exception.printStackTrace();
        }
        
			scenario = Scenario.newScenarioFor(seq);
			scenario.setSynchronizationMode(Scenario.SYNC_MODE_RF_DESIGN);
			scenario.setStartElementId("Ring_Inj:Foil");
			scenario.setProbe(probe);
			scenario.resync();
			scenario.run();
			traj = probe.getTrajectory();
			
            // CKA - Down cast the simulation trajectory results to the proper type then
            //   create a ring parameter calculation engine for processing
            TransferMapTrajectory  trjSimulation = (TransferMapTrajectory)traj;
            RingParameters         cmpRingParams = new RingParameters(trjSimulation);

			int j = 0;
			for (int i = 0; i < BPMTable.getRowCount(); i++) {
				if (((Boolean)BPMTable.getValueAt(i, 2)).booleanValue()) {
					state = traj.stateForElement(BPMAgents[i].name());
                    // CKA - Down cast the trajectory state to the proper type
                    //     then calculation the matched Twiss parameters at
                    //     the state location.
                    TransferMapState staXfer = (TransferMapState)state;
                    Twiss[]          arrTws  = cmpRingParams.getTwiss(staXfer);
                    
//                  twiss = ((TransferMapState)state).getTwiss();
                    twiss = arrTws;
                    
					posDesignPlot[j] = state.getPosition();
					xBetaLiveDesignPlot[j] = twiss[0].getBeta();
					yBetaLiveDesignPlot[j] = twiss[1].getBeta();
					j++;
				}
			}
		}
		catch (ModelException e) {
			System.out.println(e);
		}
	}


	public void	makeTimer() {
		refreshRate = 1000;
		tasksOnTimer = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				getTBTData();
				MIAAnalysis();
				plotBetas();
			}
		};
		updateTimer = new Timer(refreshRate, tasksOnTimer);
	}


	public void getTBTData() {
		/* Intitialize the TBTArrays */
		numTurns = (int)numTurnsField.getDoubleValue();
		xTBTArray = new double[numTurns][numBPMs];
		yTBTArray = new double[numTurns][numBPMs];
		
		/* Create local arrays to store arrays received via .getXTBT and
		 * .getYTBT, Use these local arrays to fill the global xTBTArray
		 * and yTBTArray
		 */
		double[] xTBT = new double[numTurns];
		double[] yTBT = new double[numTurns];
		for (int i = 0; i < numBPMs; i++) {
			if (BPMAgents[i].isOkay()) {
				System.arraycopy(BPMAgents[i].getXTBT(), 0, xTBT, 0,
								 xTBT.length);
				System.arraycopy(BPMAgents[i].getYTBT(), 0, yTBT, 0,
								 yTBT.length);

				for (int j = 0; j < numTurns; j++) {
					xTBTArray[j][i] = xTBT[j];
					yTBTArray[j][i] = yTBT[j];
				}
			}		
		}
	}


	/* Method for determining the number of good/selected by user BPMs */
	public int numberSelected() {
		int numSelected = 0;
		for (int i = 0; i < BPMTable.getRowCount(); i++) {
			if (((Boolean)BPMTable.getValueAt(i, 2)).booleanValue()) {
				numSelected++;
			}
		}
		return numSelected;
	}


	/* Method for creating the beta function plots */
	public void plotBetas () {
		int numSelected = numberSelected();

		/* Arrays of only BPM data selected by the user via the BPM table */
		BPMPositionPlot = new double[numSelected];
		xBetaPlot = new double[numSelected];
		yBetaPlot = new	double[numSelected];
		int j = 0;
		for (int i = 0; i < BPMTable.getRowCount(); i++) {
			if (((Boolean)BPMTable.getValueAt(i, 2)).booleanValue()) {
				BPMPositionPlot[j] = BPMPosition[i];
				xBetaPlot[j] = xBeta[i];
				yBetaPlot[j] = yBeta[i];
				j++;
			}
			
		}

		betaXGraphPanel.removeAllGraphData();
		betaYGraphPanel.removeAllGraphData();

		xBetaLive.addPoint(BPMPositionPlot, xBetaPlot);
		yBetaLive.addPoint(BPMPositionPlot, yBetaPlot);
		xBetaDesign.addPoint(posDesignPlot, xBetaDesignPlot);
		yBetaDesign.addPoint(posDesignPlot, yBetaDesignPlot);
		xBetaLiveDesign.addPoint(posLiveDesignPlot, xBetaLiveDesignPlot);
		yBetaLiveDesign.addPoint(posLiveDesignPlot, yBetaLiveDesignPlot);
		
		xBetaLive.setDrawLinesOn(xLinesFlag);
		xBetaLive.setDrawPointsOn(true);
		xBetaLive.setGraphColor(Color.RED);
		xBetaLive.setGraphProperty("Legend", "Measured");

		yBetaLive.setDrawLinesOn(yLinesFlag);
		yBetaLive.setDrawPointsOn(true);
		yBetaLive.setGraphColor(Color.RED);
		yBetaLive.setGraphProperty("Legend", "Measured");

		xBetaDesign.setDrawLinesOn(xLinesFlag);
		xBetaDesign.setDrawPointsOn(true);
		xBetaDesign.setGraphColor(Color.BLUE);
		xBetaDesign.setGraphProperty("Legend", "Static");

		yBetaDesign.setDrawLinesOn(yLinesFlag);
		yBetaDesign.setDrawPointsOn(true);
		yBetaDesign.setGraphColor(Color.BLUE);
		yBetaDesign.setGraphProperty("Legend", "Static");

		xBetaLiveDesign.setDrawLinesOn(xLinesFlag);
		xBetaLiveDesign.setDrawPointsOn(true);
		xBetaLiveDesign.setGraphColor(Color.LIGHT_GRAY);
		xBetaLiveDesign.setGraphProperty("Legend", "Live");
		
		yBetaLiveDesign.setDrawLinesOn(yLinesFlag);
		yBetaLiveDesign.setDrawPointsOn(true);
		yBetaLiveDesign.setGraphColor(Color.LIGHT_GRAY);
		yBetaLiveDesign.setGraphProperty("Legend", "Live");
		
		betaXGraphPanel.addGraphData(xBetaLive);
		if (xDesignFlag) betaXGraphPanel.addGraphData(xBetaDesign);
		if (xLiveDesignFlag) betaXGraphPanel.addGraphData(xBetaLiveDesign);
		betaXGraphPanel.setAxisNames("BPM Position", "Beta Value");
		betaXGraphPanel.setLegendButtonVisible(true);

		betaYGraphPanel.addGraphData(yBetaLive);
		if (yDesignFlag) betaYGraphPanel.addGraphData(yBetaDesign);
		if (yLiveDesignFlag) betaYGraphPanel.addGraphData(yBetaLiveDesign);
		betaYGraphPanel.setAxisNames("BPM Position", "Beta Value");
		betaYGraphPanel.setLegendButtonVisible(true);
	}


	/* Method for completing the MIA analysis of BPM data */
	public void MIAAnalysis() {
	
		/* Calculate and subtract off mean offsets. */
		for (int i = 0; i < numBPMs; i++) {
			double xsum = 0.0; 
			double ysum = 0.0; 
			double xoffset = 0.0; 
			double yoffset = 0.0; 
			for (int j = 0; j < numTurns; j++) {
				xsum += xTBTArray[j][i];
				ysum += yTBTArray[j][i];
			}
			xoffset = xsum/numTurns;
			for (int j = 0; j < numTurns; j++) {
				xTBTArray[j][i] -= xoffset;
				yTBTArray[j][i] -= yoffset;
			}
		 }
		
		/* Convert TBT arrays to Matrix data types for SVD operations */
		xBPMMatrix = new Matrix(xTBTArray);
		yBPMMatrix = new Matrix(yTBTArray);

		/* Perform SVD */
		xSVD = new SingularValueDecomposition(xBPMMatrix);
		ySVD = new SingularValueDecomposition(yBPMMatrix);

		/* Get singular values */
		xSigma = xSVD.getSingularValues();
		ySigma = ySVD.getSingularValues();

		/* Get U and V singular matrices */
		xUMatrix = xSVD.getU();
		yUMatrix = ySVD.getU();
		xVMatrix = xSVD.getV();
		yVMatrix = ySVD.getV();

		/* Convert the V matrices to 2D arrays */
		double[][] xVArray = xVMatrix.getArray();
		double[][] yVArray = yVMatrix.getArray();

		/* Initialize the the BPM position and beta arrays */
		BPMPosition = new double[numBPMs];
		xBeta = new double[numBPMs];
		yBeta = new double[numBPMs];

		/* Calculate unscaled beta values and fill BPM position array */
		String BPMName;
		for (int i = 0; i < xVArray.length; i++) {
			if (BPMAgents[i].isOkay()) {
				BPMPosition[i] = BPMAgents[i].getPosition();
				xBeta[i] = (xSigma[0]*xVArray[i][0])*(xSigma[0]*xVArray[i][0])
						+(xSigma[1]*xVArray[i][1])*(xSigma[1]*xVArray[i][1]);
				yBeta[i] = (ySigma[0]*yVArray[i][0])*(ySigma[0]*yVArray[i][0])
						+(ySigma[1]*yVArray[i][1])*(ySigma[1]*yVArray[i][1]);
			}
		}

		/* Add the beta values to their respective running averages
		 * and then save the mean back into the beta data arrays
		 */
		if (xAvgFlag) {
			for (int i = 0; i < numBPMs; i++) {
				BPMAgents[i].addXPoint(xBeta[i]);
				xBeta[i] = BPMAgents[i].getXMean();
			}
		}
		if (yAvgFlag) {
			for (int i = 0; i < numBPMs; i++) {
				BPMAgents[i].addYPoint(yBeta[i]);
				yBeta[i] = BPMAgents[i].getYMean();
			}
		}
		
		/* Scale the beta functions according to user specifications */
		if (xScaleSelector.getSelectedIndex() > 0) {
			xScaleFactor = xScaleValue.getDoubleValue()
							/xBeta[xScaleSelector.getSelectedIndex()-1];
			for (int i = 0; i < numBPMs; i++) {
				xBeta[i] *= xScaleFactor;
			}
		}
		if (yScaleSelector.getSelectedIndex() > 0) {
			yScaleFactor = yScaleValue.getDoubleValue()
							/yBeta[yScaleSelector.getSelectedIndex()-1];
			for (int i = 0; i < numBPMs; i++) {
				yBeta[i] *= yScaleFactor;
			}
		}
	}


	/* Method for saving data to a file */
	public void exportData(File file) throws IOException {
		FileOutputStream fOut = new FileOutputStream(file);
		String str = new String("#BPM Name \t\tPosition \t\tBeta X \t\t\t"
								+ "Beta Y \n");
		for (int i = 0; i < numBPMs; i++) {
			str = str + BPMAgents[i].name() + "\t" + BPMPosition[i] + "\t\t"
					+ xBeta[i] + "\t\t" + yBeta[i] + "\n";
		}
		fOut.write(str.getBytes());
		fOut.close();
	}
}
