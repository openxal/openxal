/*
 * AnalysisPanel.java
 *
 * Created on June 7, 2011
 *
 * Copyright (c) 2011 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 */


package xal.app.rekit;

import java.io.*;                                                              
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.lang.Math.*;
import java.lang.String;
import java.text.DecimalFormat;

import xal.tools.apputils.EdgeLayout;
import xal.tools.beam.*;
import xal.tools.solver.*;
import xal.tools.solver.algorithm.*;
import xal.tools.swing.DecimalField;
import xal.model.*;
import xal.model.probe.*;
import xal.model.alg.ParticleTracker;
import xal.model.probe.traj.*;
import xal.sim.scenario.*;
import xal.smf.*;
import xal.smf.data.*;
import xal.smf.impl.*;
import xal.smf.proxy.*;
import xal.tools.plot.*;

/**
 * This is the display panel for the extraction kicker restoration application.
 *
 * @version   1.0
 * @author  cp3, zoy
 * @author  Sarah Cousineau, Taylor Patterson
 */


public class AnalysisPanel extends JPanel{
	
    private static final long serialVersionUID = 1L;
    
	/* Main display objects */
	EdgeLayout edgeLayout = new EdgeLayout();
	JPanel mainPanel= new JPanel();
		
	/* Components of the EKicker selector table */
	JScrollPane EKickSelectorPane;
	private JTable EKickTable;
	private DataTableModel EKickDataTableModel;
	private String[] EKickSelColNames = {"Kicker Name", "Set Voltage",
		"Field Value", "Trial Value", "In Use?", "Vary?", "Lower Limit",
		"Upper Limit"};
	private FunctionGraphsJPanel plotPanel;
	GridLimits limits = new GridLimits();
	
	JButton resetButton;
	JButton copyButton;
	ButtonGroup readbackButtonGroup;
	JRadioButton readbackRB;
	JRadioButton setRB;
	
	/* Components of the single pass portion */
	JLabel singlePassLabel;
	ButtonGroup spButtonGroup;
	JRadioButton setPointsRB;
	JRadioButton trialPointsRB;
	JButton runSPButton;
	JLabel atSeptumLabel;
	JLabel ySingleLabel;
	DecimalField ySingleField;
	JLabel pySingleLabel;
	DecimalField pySingleField;
	
	/* Componets of the solver portion */
	JLabel solverLabel;
	JLabel yGoalLabel;
	DecimalField yGoalField;
	JLabel pyGoalLabel;
	DecimalField pyGoalField;
	JLabel ySolutionLabel;
	DecimalField ySolutionField;
	JLabel pySolutionLabel;
	DecimalField pySolutionField;
	JButton solveButton;
	JScrollPane solutionPane;
	private JTable solutionTable;
	private DataTableModel solutionDataTableModel;
	private String[] solutionColNames = {"Kicker Name", "Trial Value"};
	JLabel solveTimeLabel;
	DecimalField solveTimeField;
	JButton sendButton;
	NumberFormat numForm = NumberFormat.getInstance();
	DecimalFormat decForm = new DecimalFormat("0.000000");
	
	/* Components for setting up the accelerator */
	public AcceleratorSeqCombo seq;
	Accelerator accl = new Accelerator();
	ParticleProbe probe;
	Scenario scenario;
	ParticleTrajectory traj;
	ParticleProbeState state;
	PhaseVector coordinates;
	
	/* Data structures for EKicker data */
	ArrayList<AcceleratorNode> nodeList = new ArrayList<AcceleratorNode>();
	int numNodes;
	EKickAgent[] nodeArray;
	double[] limitArray = {0.026061, 0.021593, 0.021593, 0.017523, 0.017523,
						   0.014768, 0.014768, 0.019235, 0.019235, 0.019235,
						   0.019235, 0.020650, 0.020650, 0.020650};
	
	/* Components of the solver */
	ArrayList<Variable> variables = new ArrayList<Variable>();
	ArrayList<Objective> objectives = new ArrayList<Objective>();
	Evaluator1 evaluator;
	Problem problem;
	Solver solver;


	/* Member function Constructor */
	public AnalysisPanel() {
		
		makeComponents();		/* Creation of all GUI components */
		addComponents();		/* Add all components to the layout and panels */
		acclSetup();			/* Read current accelerator, make lattice 
								 * sequence, and get BPM nodes 
								 */
		makeTables();			/* Create the primary display table */
		setAction();			/* Set the action listeners */
		
	}
	
	
	protected void makeComponents() {
		
		/* Make main panel */
		mainPanel = new JPanel();
		mainPanel.setPreferredSize(new Dimension(960, 800));

		/* Make EKicker selector table */
		EKickDataTableModel = new DataTableModel(EKickSelColNames, 0);
		EKickTable = new JTable(EKickDataTableModel);
		EKickSelectorPane = new JScrollPane(EKickTable,
											JScrollPane.VERTICAL_SCROLLBAR_NEVER,
											JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		EKickSelectorPane.setPreferredSize(new Dimension(550, 250));
		resetButton = new JButton("Reset Table");
		copyButton = new JButton("Copy Field Values to Trial Values");
		readbackButtonGroup = new ButtonGroup();
		readbackRB = new JRadioButton("Use B Readback", true);
		readbackButtonGroup.add(readbackRB);
		setRB = new JRadioButton("Use B Set", false);
		readbackButtonGroup.add(setRB);
		
		/* Make components of single pass display */
		singlePassLabel = new JLabel("Single Pass:");
		spButtonGroup = new ButtonGroup();
		setPointsRB = new JRadioButton("Use Field Values", true);
		spButtonGroup.add(setPointsRB);
		trialPointsRB = new JRadioButton("Use Trial Values", false);
		spButtonGroup.add(trialPointsRB);
		runSPButton = new JButton("Run Single Pass");
		atSeptumLabel = new JLabel("Beam at Extraction Septum:");
		ySingleLabel = new JLabel("y:");
		ySingleField = new DecimalField(0.0, 6, decForm);
		pySingleLabel = new JLabel("py:");
		pySingleField = new DecimalField(0.0, 6, decForm);
		
		/* Make componets of the solver display */
		solverLabel = new JLabel("Solver:");
		yGoalLabel = new JLabel("Desired y:");
		yGoalField = new DecimalField(-0.150, 6, decForm);
		pyGoalLabel = new JLabel("Desired py:");
		pyGoalField = new DecimalField(-0.013, 6, decForm);
		ySolutionLabel = new JLabel("Solution y:");
		ySolutionField = new DecimalField(0.0, 6, decForm);
		pySolutionLabel = new JLabel("Solution py:");
		pySolutionField = new DecimalField(0.0, 6, decForm);
		solveButton = new JButton("Solve");
		solutionDataTableModel = new DataTableModel(solutionColNames, 0);
		solutionTable = new JTable(solutionDataTableModel);
		solutionPane = new JScrollPane(solutionTable,
									   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
									   JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		solutionPane.setPreferredSize(new Dimension(200, 175));
		solveTimeLabel = new JLabel("Time Limit:");
		solveTimeField = new DecimalField(10.0, 6);
		sendButton = new JButton("Send to Machine");
		
		/* Make components of plot display */
		plotPanel = new FunctionGraphsJPanel();
		plotPanel.setPreferredSize(new Dimension(800, 250));
		plotPanel.setGraphBackGroundColor(Color.WHITE);
		
	}
	
	
	protected void addComponents() {
		
		edgeLayout.setConstraints(mainPanel, 0, 0, 0, 0, EdgeLayout.ALL_SIDES,
								  EdgeLayout.GROW_BOTH);
		this.add(mainPanel);
		
		EdgeLayout newLayout = new EdgeLayout();
		mainPanel.setLayout(newLayout);
		
		/* Add primary EKick data display */
		newLayout.add(EKickSelectorPane, mainPanel, 0, 5, EdgeLayout.LEFT);
		newLayout.add(resetButton, mainPanel, 0, 260, EdgeLayout.LEFT);
		newLayout.add(copyButton, mainPanel, 110, 260, EdgeLayout.LEFT);
		newLayout.add(readbackRB, mainPanel, 355, 260, EdgeLayout.LEFT);
		newLayout.add(setRB, mainPanel, 355, 280, EdgeLayout.LEFT);
		
		/* Add single pass display components */
		newLayout.add(singlePassLabel, mainPanel, 560, 5, EdgeLayout.LEFT);
		newLayout.add(setPointsRB, mainPanel, 585, 25, EdgeLayout.LEFT);
		newLayout.add(trialPointsRB, mainPanel, 585, 45, EdgeLayout.LEFT);
		newLayout.add(runSPButton, mainPanel, 585, 70, EdgeLayout.LEFT);
		newLayout.add(atSeptumLabel, mainPanel, 760, 25, EdgeLayout.LEFT);
		newLayout.add(ySingleLabel, mainPanel, 795, 50, EdgeLayout.LEFT);
		newLayout.add(ySingleField, mainPanel, 815, 45, EdgeLayout.LEFT);
		newLayout.add(pySingleLabel, mainPanel, 795, 75, EdgeLayout.LEFT);
		newLayout.add(pySingleField, mainPanel, 815, 70, EdgeLayout.LEFT);
		
		/* Add solver display components */
		newLayout.add(solverLabel, mainPanel, 560, 105, EdgeLayout.LEFT);
		newLayout.add(yGoalLabel, mainPanel, 585, 125, EdgeLayout.LEFT);
		newLayout.add(yGoalField, mainPanel, 660, 120, EdgeLayout.LEFT);
		newLayout.add(pyGoalLabel, mainPanel, 585, 150, EdgeLayout.LEFT);
		newLayout.add(pyGoalField, mainPanel, 660, 145, EdgeLayout.LEFT);
		newLayout.add(solveTimeLabel, mainPanel, 585, 175, EdgeLayout.LEFT);
		newLayout.add(solveTimeField, mainPanel, 660, 170, EdgeLayout.LEFT);
		newLayout.add(solveButton, mainPanel, 620, 195, EdgeLayout.LEFT);
		newLayout.add(ySolutionLabel, mainPanel, 585, 225, EdgeLayout.LEFT);
		newLayout.add(ySolutionField, mainPanel, 660, 220, EdgeLayout.LEFT);
		newLayout.add(pySolutionLabel, mainPanel, 585, 250, EdgeLayout.LEFT);
		newLayout.add(pySolutionField, mainPanel, 660, 245, EdgeLayout.LEFT);
		newLayout.add(sendButton, mainPanel, 585, 270, EdgeLayout.LEFT);
		newLayout.add(solutionPane, mainPanel, 760, 120, EdgeLayout.LEFT);

		/* Add the plot display */
		newLayout.add(plotPanel, mainPanel, 50, 400, EdgeLayout.LEFT);
	}
	
	
	public void acclSetup() {
		
		/* Set up the model */
		try {
			accl = XMLDataManager.loadDefaultAccelerator();
			seq = accl.getComboSequence("RTBT");
            //-
            try {
                ParticleTracker particleTracker = AlgorithmFactory.createParticleTracker(seq);
                
                probe = ProbeFactory.createParticleProbe(seq, particleTracker);
            }
            catch (Exception e) {
                System.err.println(e);
                e.printStackTrace();
            }

			scenario = Scenario.newScenarioFor(seq);
			scenario.setSynchronizationMode(Scenario.SYNC_MODE_RF_DESIGN);
			scenario.setStartElementId("Ring_Mag:EKick01");
			scenario.setStopElementId("RTBT_Mag:ExSptm");
			scenario.setProbe(probe);
			scenario.resync();
		}
		catch (ModelException e) {
			System.out.println(e);
		}
		
		/* Put the EKickers into an array of EKickAgents */
		nodeList = (ArrayList<AcceleratorNode>)seq.getNodesOfType("EKick");
		numNodes = nodeList.size();
		nodeArray = new EKickAgent[numNodes];
		for (int i = 0; i < numNodes; i++) {
			nodeArray[i] = new EKickAgent(seq, nodeList.get(i),true);
		}
		
	}
	
	
	public void makeTables(){
		
		/* Create the solver data table */
		solutionTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		solutionTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		solutionTable.setPreferredScrollableViewportSize(solutionTable.getPreferredSize());
		solutionTable.setRowSelectionAllowed(false);
		solutionTable.setColumnSelectionAllowed(false);
		solutionTable.setCellSelectionEnabled(false);
		solutionDataTableModel.fireTableDataChanged();
		
		/* Create the main data table */
		EKickTable.getColumnModel().getColumn(0).setPreferredWidth(75);
		EKickTable.getColumnModel().getColumn(1).setPreferredWidth(75);
		EKickTable.getColumnModel().getColumn(2).setPreferredWidth(75);
		EKickTable.getColumnModel().getColumn(3).setPreferredWidth(75);
		EKickTable.getColumnModel().getColumn(4).setPreferredWidth(50);
		EKickTable.getColumnModel().getColumn(5).setPreferredWidth(50);
		EKickTable.getColumnModel().getColumn(6).setPreferredWidth(75);
		EKickTable.getColumnModel().getColumn(7).setPreferredWidth(75);
		EKickTable.setPreferredScrollableViewportSize(EKickTable.getPreferredSize());
		EKickTable.setRowSelectionAllowed(false);
		EKickTable.setColumnSelectionAllowed(false);
		EKickTable.setCellSelectionEnabled(false);
		EKickDataTableModel.fireTableDataChanged();
		
		/* Popular the main table with data */
		for (int i = 0; i < numNodes; i++) {
			ArrayList<Object> tableData = new ArrayList<Object>();
			tableData.add(nodeArray[i].name().substring(9));
			tableData.add(numForm.format(nodeArray[i].getVoltage()));
			tableData.add(decForm.format(nodeArray[i].getValue()));
			tableData.add("");
			tableData.add(nodeArray[i].isOkay());
			tableData.add(false);
			tableData.add(decForm.format(0.0));
			tableData.add(decForm.format(nodeArray[i].upperLimit(limitArray[i])));
			EKickDataTableModel.addTableData(tableData);
		}
		EKickDataTableModel.fireTableDataChanged();
				
	}
	
	
	protected void setAction() {
		
		/* Button for starting a single pass */
		runSPButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					scenario.resync();
				}
				catch (ModelException e) {
					System.out.println(e);
				}
				runSinglePass();
			}
		});
		
		/* Button for starting the solver */
		solveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				solutionDataTableModel.clearAllData();
				runSolver();
			}
		});
		
		/* Button for returning the main table to its state on startup */
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				EKickDataTableModel.clearAllData();
				for (int i = 0; i < numNodes; i++) {
					ArrayList<Object> tableData = new ArrayList<Object>();
					tableData.add(nodeArray[i].name().substring(9));
					tableData.add(numForm.format(nodeArray[i].getVoltage()));
					tableData.add(decForm.format(nodeArray[i].getValue()));
					tableData.add("");
					tableData.add(nodeArray[i].isOkay());
					tableData.add(false);
					tableData.add(decForm.format(0.0));
					tableData.add(decForm.format(nodeArray[i].upperLimit(limitArray[i])));
					EKickDataTableModel.addTableData(tableData);
				}
				EKickDataTableModel.fireTableDataChanged();
				
				solutionDataTableModel.clearAllData();
				solutionDataTableModel.fireTableDataChanged();
			}
		});
		
		/* Copy the field value column into the trial value column */
		copyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				for (int i = 0; i < numNodes; i++) {
					EKickTable.setValueAt(EKickTable.getValueAt(i, 2), i, 3);
				}
				EKickDataTableModel.fireTableDataChanged();
			}
		});
		
		/* Select to use readback field values */
		readbackRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				for (int i = 0; i < numNodes; i++) {
					nodeArray[i].setUseReadback(true);
				}
				resetButton.doClick();
			}
		});
		
		/* Select to use the set field values */
		setRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				for (int i = 0; i < numNodes; i++) {
					nodeArray[i].setUseReadback(false);
				}
				resetButton.doClick();
			}
		});
		
		/* Make the values in the trial column the machine settings */
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				for (int i = 0; i < numNodes; i++) {
					if(((Boolean)EKickTable.getValueAt(i, 5)).booleanValue()){
						if (setRB.isSelected()) {
							nodeArray[i].setValue(Double.parseDouble(EKickTable.getValueAt(i, 3).toString()));
						}
						else {
							setValueForKnownReadback(nodeArray[i],
							Double.parseDouble(EKickTable.getValueAt(i, 3).toString()));
						}
					}
				}
			}
		});
		
	}
	
	
	protected void runSinglePass() {
		
		/* Set EKicker field values based on user selections */
		for (int i = 0; i < numNodes; i++) {
			double value;
			/* Use the trial points column */
			if (trialPointsRB.isSelected()) {
				if (EKickTable.getValueAt(i, 3).toString().isEmpty()) {
					//Replaced with JOptionPane.showMessageDialog
                        //JOptionPane errorAlert = new JOptionPane();
					JOptionPane.showMessageDialog(null, "No trial points generated.",
												 "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				value = Double.parseDouble(EKickTable.getValueAt(i, 3).toString());
			}
			/* Use the field values column */
			else {
				value = Double.parseDouble(EKickTable.getValueAt(i, 2).toString());
			}
			scenario.setModelInput(seq.getNodeWithId(nodeArray[i].name()),
								   ElectromagnetPropertyAccessor.PROPERTY_FIELD,
								   value);
			/* Set field to 0.0 if not in use */
			if (((Boolean)EKickTable.getValueAt(i, 4)).booleanValue() == false) {
				scenario.setModelInput(seq.getNodeWithId(nodeArray[i].name()),
									   ElectromagnetPropertyAccessor.PROPERTY_FIELD,
									   0.0);
			}
		}
		
		/* Run the model */
		try {
			probe.reset();
			scenario.resyncFromCache();
			scenario.run();
		}
		catch (ModelException e) {
			System.out.println(e);
		}
		
		ParticleTrajectory traj = (ParticleTrajectory)probe.getTrajectory();
		state = (ParticleProbeState)traj.stateForElement("RTBT_Mag:ExSptm");
		coordinates = state.getPhaseCoordinates();
		
		/* Display the resulting y and py */
		ySingleField.setValue(coordinates.gety());
		pySingleField.setValue(coordinates.getyp());

		plot(traj);
		
	}
	
	
	protected void runSolver() {
		
		/* Resync to flush any cached field values */
		try {
			scenario.resync();
		}
		catch (ModelException e) {
			System.out.println(e);
		}
		
		/* Determine which EKickers will be variables for the solver */
		variables.clear();
		for (int i = 0; i < numNodes; i++) {
			if (((Boolean)EKickTable.getValueAt(i, 5)).booleanValue()) {
				Variable var = new Variable(nodeArray[i].name(), nodeArray[i].getValue(),
											Double.parseDouble(EKickTable.getValueAt(i, 6).toString()),
											Double.parseDouble(EKickTable.getValueAt(i, 7).toString()));
				variables.add(var);
			}
		}
		
		/* Alert the user that no variables have been selected */
		if (variables.isEmpty()) {
			//Replace with JOptionPane.showMessageDialog
                //JOptionPane errorAlert = new JOptionPane();
			JOptionPane.showMessageDialog(null, "Must select magnets to vary.",
										 "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		/* Set solver objective */
		objectives.add(new TargetObjective("Target Error", 0.0));
		
		/* Run the solver */
		evaluator = new Evaluator1(objectives, variables);
		problem = new Problem(objectives, variables, evaluator);
		Stopper maxSolutionStopper = SolveStopperFactory.minMaxTimeSatisfactionStopper(1,
										solveTimeField.getDoubleValue(), 0.999999);
		solver = new Solver(new RandomShrinkSearch(), maxSolutionStopper);
		solver.solve(problem);
		
		/* Set the model to the best solution */
		Trial best = solver.getScoreBoard().getBestSolution();
		calcError(variables, best);

		/* Display the best solution in the fields */
		ySolutionField.setValue(coordinates.gety());
		pySolutionField.setValue(coordinates.getyp());
		
		/* Update the tables */
		Iterator<Variable> itr = variables.iterator();
		while(itr.hasNext()){
			Variable variable = itr.next();
			double value = best.getTrialPoint().getValue(variable);
			String name = variable.getName();
			for (int i = 0; i < numNodes; i++) {
				if (name.equals(nodeArray[i].name())) {
					ArrayList<Object> tableData = new ArrayList<Object>();
					tableData.add(name.substring(9));
					tableData.add(decForm.format(value));
					EKickTable.setValueAt(decForm.format(value), i, 3);
					solutionDataTableModel.addTableData(tableData);
				}
			}
		}
		solutionDataTableModel.fireTableDataChanged();
		
		for (int i = 0; i < numNodes; i++) {
			if (((Boolean)EKickTable.getValueAt(i, 5)).booleanValue() == false) {
				EKickTable.setValueAt(EKickTable.getValueAt(i, 2), i, 3);
			}
			if (((Boolean)EKickTable.getValueAt(i, 4)).booleanValue() == false) {
				EKickTable.setValueAt(0.000000, i, 3);
			}
		}
		EKickDataTableModel.fireTableDataChanged();
		
		plot(traj);

	}
	
	
	/* Method for determining the field set value to acheive the desired readback value */
	private void setValueForKnownReadback(EKickAgent kicker, double desired) {
		
		
		int iteration = 0;
		double error = 1.0;
		double trial = desired;
		String name = kicker.name();
		
		while (error > 0.05 && iteration < 4) {
			kicker.setValue(trial);
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				System.out.println(e);
			}
			error = Math.abs((kicker.getValue()-desired)/desired);
			trial = (desired*desired)/kicker.getValue();
			for (int i = 0; i < numNodes; i++) {
				if (name.equals(EKickTable.getValueAt(i, 0).toString())) {
					if (trial > Double.parseDouble(EKickTable.getValueAt(i, 7).toString())) {
						trial = Double.parseDouble(EKickTable.getValueAt(i, 7).toString());
					}
				}
			}
			iteration++;
		}
		return;
	}
	
	
	/* Method for evaluating the accuracy of the solver */
	public double calcError(ArrayList<Variable> vars, Trial trial) {
		
		double error;
		
		updateVariables(vars, trial);
		
		try {
			probe.reset();
			scenario.resyncFromCache();
			scenario.run();
		}
		catch (ModelException e) {
			System.out.println(e);
		}

		traj = (ParticleTrajectory)probe.getTrajectory();
		state = (ParticleProbeState)traj.stateForElement("RTBT_Mag:ExSptm");
		coordinates = state.getPhaseCoordinates();
		error = ((coordinates.gety()-yGoalField.getDoubleValue())*(coordinates.gety()-yGoalField.getDoubleValue()))
				+10.0*((coordinates.getyp()-pyGoalField.getDoubleValue())*(coordinates.getyp()-pyGoalField.getDoubleValue()));
		return Math.sqrt(10000.0*error);
		
	}
	
	
	/* Method for updating variables for the solver */
	void updateVariables(ArrayList<Variable> vars, Trial trial) {
		
		Iterator<Variable> itr = vars.iterator();
		while(itr.hasNext()){
			Variable variable = itr.next();
			double value = trial.getTrialPoint().getValue(variable);
			String name = variable.getName();
			for (int i = 0; i < numNodes; i++) {
				if (name.equals(nodeArray[i].name())) {
					scenario.setModelInput(seq.getNodeWithId(nodeArray[i].name()),
										   ElectromagnetPropertyAccessor.PROPERTY_FIELD,
										   value);
				}
				if (((Boolean)EKickTable.getValueAt(i, 4)).booleanValue() == false) {
					scenario.setModelInput(seq.getNodeWithId(nodeArray[i].name()),
										   ElectromagnetPropertyAccessor.PROPERTY_FIELD,
										   0.0);
				}
			}
		}
		
	}


	void plot(Trajectory traj){
		
		plotPanel.removeAllGraphData();
		BasicGraphData ygraphdata = new BasicGraphData();
		BasicGraphData pygraphdata = new BasicGraphData();
		BasicGraphData bpmgraphdata = new BasicGraphData();
		
		ArrayList<Double> sdata = new ArrayList<Double>();
		ArrayList<Double> ydata = new ArrayList<Double>();
		ArrayList<Double> pydata = new ArrayList<Double>();
		
		Iterator<ProbeState> iterState= traj.stateIterator();
		
		while(iterState.hasNext()){
			state = (ParticleProbeState)iterState.next();
			coordinates = state.getPhaseCoordinates();
			double s = state.getPosition();
			double y =  1000.0*coordinates.gety();
			double py =  1000.0*coordinates.getyp();
			sdata.add(s);
			ydata.add(y);
			pydata.add(py);
		}
		
		double exsptmpos = ((ParticleProbeState)traj.stateForElement("RTBT_Mag:ExSptm")).getPosition();
		int size = ydata.size() - 1;
		ParticleProbeState bpmc10state = (ParticleProbeState)traj.stateForElement("Ring_Mag:QTH_C10");
		double bpmc10pos = bpmc10state.getPosition();
		double bpmc10y = 1000.0*(bpmc10state.getPhaseCoordinates()).gety();
		
		
		double[] s = new double[size];
		double[] y = new double[size];
		double[] py = new double[size];
		
		for(int i=0; i<size; i++){
			s[i]=(sdata.get(i)).doubleValue();
			y[i]=(ydata.get(i)).doubleValue();
			py[i]=(pydata.get(i)).doubleValue();
		}
		
		ygraphdata.addPoint(s, y);
		ygraphdata.setDrawPointsOn(false);
		ygraphdata.setDrawLinesOn(true);
		ygraphdata.setGraphProperty("Legend", new String("Horizontal"));
		ygraphdata.setGraphColor(Color.RED);
		pygraphdata.addPoint(s, py);
		pygraphdata.setDrawPointsOn(false);
		pygraphdata.setDrawLinesOn(true);
		pygraphdata.setGraphProperty("Legend", new String("Vertical"));
		pygraphdata.setGraphColor(Color.BLUE);
		bpmgraphdata.addPoint(bpmc10pos, bpmc10y);
		bpmgraphdata.setDrawPointsOn(true);
		bpmgraphdata.setDrawLinesOn(false);

		//limits.setSmartLimits();
		limits.setXmax(exsptmpos);
		plotPanel.setExternalGL(limits);
		plotPanel.addGraphData(ygraphdata);
		plotPanel.addGraphData(pygraphdata);
		plotPanel.addGraphData(bpmgraphdata);
}		

/* Evaluator class for the solver */
class Evaluator1 implements Evaluator{
		
	protected ArrayList<Variable> _variables;
	protected ArrayList<Objective> _objectives;

	public Evaluator1(final ArrayList<Objective> objectives, final ArrayList<Variable> variables) {
		_objectives = objectives;
		_variables = variables;
	}
		
	public void evaluate(final Trial trial){
		double error = 0.0; 
		Iterator<Objective> itr = _objectives.iterator();
		while(itr.hasNext()){
			TargetObjective objective = (TargetObjective)itr.next();
			error = calcError(_variables, trial);
			trial.setScore(objective, error);
		}
			
	}
}
	
	
/* Objective class for the solver */
class TargetObjective extends Objective{
	
	protected final double _target;
	
	public TargetObjective(final String name, final double target){
		super(name);
		_target=target;
	}
		
	public double satisfaction(double value){
		double error = _target - value;
		return 1.0/(1+error*error);
	}
}

}