//
//  OptimizerWindow.java
//  xal
//
//  Created by Thomas Pelaia on 6/14/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import xal.extension.solver.*;
import xal.tools.apputils.NumericCellRenderer;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.awt.Component;
import java.awt.Rectangle;
import java.util.*;
import java.text.*;


/** Window for displaying the optimization status */
public class OptimizerWindow extends JFrame implements OpticsOptimizerListener {
    
    private final static long serialVersionUID = 1L;
    
	/** optimizer */
	protected OpticsOptimizer _optimizer;
	
	/** table of objective values */
	final protected JTable _objectivesTable;
	
	/** objectives table model */
	final protected OptimizerObjectivesTableModel _objectivesTableModel;
	
	
	/** variable table */
	final protected JTable _variableTable;
	
	/** variables table model */
	final protected VariableValueTableModel _variableTableModel;
	
	/** fixed parameter table */
	final protected JTable _fixedParameterTable;
	
	/** fixed parameters table model */
	final protected FixedCustomParameterTableModel _fixedParameterTableModel;
	
	/** optimization progress bar */
	final protected JProgressBar _progressBar;
	
	/** The view which allows the user to change the duration of the solving run */
	protected JSpinner _durationView;
	
	/** the simulation chart */
	final protected SimulationChart _simulationChart;
	
	/** split pane */
	protected JSplitPane _splitPane;
	
	/** split pane over tables */
	protected JSplitPane _tableSplitPane;
	
	/** split pane for variables */
	protected JSplitPane _parametersSplitPane;
	
	/** split pane set indicator */
	protected boolean _splitPaneSet;
	
	
	/** Constructor */
	public OptimizerWindow( final OpticsOptimizer optimizer ) {
		super( "Optimization Status" );
		
		_objectivesTableModel = new OptimizerObjectivesTableModel( null );
		_objectivesTable = new JTable( _objectivesTableModel );
		setupObjectivesTable();
		
		_variableTableModel = new VariableValueTableModel( null );
		_variableTable = new JTable( _variableTableModel );
		setupVariablesTable();
		
		_fixedParameterTableModel = new FixedCustomParameterTableModel( null );
		_fixedParameterTable = new JTable( _fixedParameterTableModel );
		setupFixedParametersTable();
		
		_simulationChart = new SimulationChart( optimizer );
		
		_progressBar = new JProgressBar();
		_progressBar.setMinimum( 0 );
		_progressBar.setString( "Optimization Time" );
	
		makeContentView();
		
		setOptimizer( optimizer );
		updateMaximumProgressTime();
	}
	
	
	/** Make the content view */
	protected void makeContentView() {
		setSize( 900, 800 );
		final Box mainView = new Box( BoxLayout.Y_AXIS );
		getContentPane().add( mainView );
		
		_splitPaneSet = false;
		
		_parametersSplitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, new JScrollPane( _variableTable ), new JScrollPane( _fixedParameterTable ) );
		_parametersSplitPane.setResizeWeight( 0.6 );
		
		_tableSplitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT, new JScrollPane( _objectivesTable ), _parametersSplitPane );
		_tableSplitPane.setResizeWeight( 0.5 );
		_tableSplitPane.setOneTouchExpandable( true );
		
		_splitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT, _tableSplitPane, makeBottomView() );
		_splitPane.setResizeWeight( 0.4 );
		_splitPane.setOneTouchExpandable( true );
		
		mainView.add( _splitPane );
	}
	
	
	/**
	 * Override this method to set the divider location of the split pane.
	 */
	public void setVisible ( final boolean visible ) {
		super.setVisible( visible );
		
		if ( !_splitPaneSet ) {		// only set the divider location the first time it is shown
			_splitPane.setDividerLocation( 0.4 );
			_tableSplitPane.setDividerLocation( 0.5 );
			_parametersSplitPane.setDividerLocation( 0.6 );
			_splitPaneSet = true;
		}
	}
	
	
	/**
	 * Make the bottom view.
	 */
	protected Component makeBottomView() {
		final Box view = new Box( BoxLayout.Y_AXIS );
		
		view.add( makeChartView() );
		view.add( makeProgressView() );
		
		return view;
	}
	
	
	/**
	 * Make the progress view.
	 * @return the progress view.
	 */
	protected Component makeProgressView() {
		final int SPACE = 25;
		final Box view = new Box( BoxLayout.X_AXIS );
		
		view.add( Box.createHorizontalGlue() );
		
		view.add( new JLabel( "Elapsed Time: " ) );
	
		_progressBar.setMaximumSize( _progressBar.getPreferredSize() );
		view.add( _progressBar );
						  
		view.add( Box.createHorizontalStrut( SPACE ) );
		
		view.add( new JLabel( "Duration (sec): " ) );
		
		_durationView = new JSpinner( new SpinnerNumberModel( 0.0, 0.0, 3600.0, 1.0 ) );
		_durationView.setMaximumSize( _durationView.getPreferredSize() );
		view.add( _durationView );
		_durationView.addChangeListener( new ChangeListener() {
			public void stateChanged( final ChangeEvent event ) {
				if ( _optimizer != null ) {
					final double duration = ((Number)_durationView.getValue()).doubleValue();
					_optimizer.setSolvingDuration( duration );
					updateMaximumProgressTime();					
				}
			}
		});
						  
		view.add( Box.createHorizontalStrut( SPACE ) );

		final JButton stopButton = new JButton( "Stop" );
		view.add( stopButton );
		stopButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				if ( _optimizer != null ) {
					_optimizer.stopSolving();					
				}
			}
		});
		
		view.add( Box.createHorizontalStrut( SPACE ) );
		
		view.setBorder( BorderFactory.createTitledBorder( "Solving Time Progress" ) );
		view.setMaximumSize( new java.awt.Dimension( 10000, view.getPreferredSize().height ) );
		
		return view;
	}
	
	
	/**
	 * Make the chart view.
	 */
	protected Component makeChartView() {
		final Box view = new Box( BoxLayout.Y_AXIS );
		
		view.add( makeChartButtonsView() );
		view.add( _simulationChart.getChart() );
				
		return view;
	}
	
	
	/**
	 * Make the chart view.
	 * @return the chart button view
	 */
	protected Component makeChartButtonsView() {
		final int SPACE = 10;
		final Box view = new Box( BoxLayout.X_AXIS );
		
		view.setBorder( BorderFactory.createTitledBorder( "Chart Controls" ) );
		
		final JCheckBox showDesignButton = new JCheckBox( SimulationChart.SIMULATION_LABELS[ SimulationChart.DESIGN ] );
		showDesignButton.setSelected( _simulationChart.isSimulationEnabled( SimulationChart.DESIGN ) );
		showDesignButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				_simulationChart.enableSimulation( SimulationChart.DESIGN, showDesignButton.isSelected() );
			}
		});
		view.add( showDesignButton );
		
		final JCheckBox showTrialButton = new JCheckBox( SimulationChart.SIMULATION_LABELS[ SimulationChart.TRIAL ] );
		showTrialButton.setSelected( _simulationChart.isSimulationEnabled( SimulationChart.TRIAL ) );
		showTrialButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				_simulationChart.enableSimulation( SimulationChart.TRIAL, showTrialButton.isSelected() );
			}
		});
		view.add( showTrialButton );
		
		view.add( Box.createHorizontalStrut( SPACE ) );
		
		// add checkboxes for enabling/disabling scalar chart data
		addSimulationScalarDataDisplayCheckbox( view, SimulationChart.KINETIC_ENERGY, "Kinetic Energy (MeV)" );
				
		view.add( Box.createHorizontalStrut( SPACE ) );
		
		// add checkboxes for enabling/disabling vector chart data
		addSimulationDataDisplayCheckbox( view, SimulationChart.BETA, "Beta (m)" );		
		addSimulationDataDisplayCheckbox( view, SimulationChart.BETA_ERROR, "Beta Error (%)" );
		addSimulationDataDisplayCheckbox( view, SimulationChart.ETA, "Chromatic Dispersion (m)" );		
		addSimulationDataDisplayCheckbox( view, SimulationChart.ALPHA, "Alpha" );		
		addSimulationDataDisplayCheckbox( view, SimulationChart.EMITTANCE, "Emittance" );		
		
		view.add( Box.createHorizontalStrut( SPACE ) );
		
		// add checkboxes for enabling/disabling coordinates
		addCoordinateDisplayCheckbox( view, SimulationChart.X );
		addCoordinateDisplayCheckbox( view, SimulationChart.Y );
		addCoordinateDisplayCheckbox( view, SimulationChart.Z );
				
		view.add( Box.createHorizontalStrut( SPACE ) );
		
		final JCheckBox legendButton = new JCheckBox( "Legend" );
		legendButton.setSelected( _simulationChart.isLegendVisible() );
		legendButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				_simulationChart.setLegendVisible( legendButton.isSelected() );
			}
		});
		view.add( legendButton );
		
		final JCheckBox gridButton = new JCheckBox( "Grid" );
		gridButton.setSelected( _simulationChart.isGridVisible() );
		gridButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				_simulationChart.setGridVisible( gridButton.isSelected() );
			}
		});
		view.add( gridButton );
		
		
		view.setMaximumSize( new java.awt.Dimension( 10000, view.getPreferredSize().height ) );
		
		return view;
	}
	
	
	/** add to the specified view, a checkbox to enable and disable the specified simulation data parameter */
	private void addSimulationDataDisplayCheckbox( final java.awt.Container view, final int dataIndex, final String tooltip ) {
		final JCheckBox button = new JCheckBox( SimulationChart.DATA_LABELS[ dataIndex ] );
		button.setToolTipText( tooltip );
		button.setSelected( _simulationChart.isDataEnabled( dataIndex ) );
		button.addActionListener( new ActionListener() {
			 public void actionPerformed( final ActionEvent event ) {
				_simulationChart.enableData( dataIndex, button.isSelected() );
			 }
		});
		view.add( button );		
	}
	
	
	/** add to the specified view, a checkbox to enable and disable the specified simulation scalar data parameter */
	private void addSimulationScalarDataDisplayCheckbox( final java.awt.Container view, final int dataIndex, final String tooltip ) {
		final JCheckBox button = new JCheckBox( SimulationChart.SCALAR_DATA_LABELS[ dataIndex ] );
		button.setToolTipText( tooltip );
		button.setSelected( _simulationChart.isDataEnabled( dataIndex ) );
		button.addActionListener( new ActionListener() {
			 public void actionPerformed( final ActionEvent event ) {
				_simulationChart.enableScalarData( dataIndex, button.isSelected() );
			 }
		});
		view.add( button );		
	}
	
	
	/** add to the specified view, a checkbox to enable and disable the specified coordinate */
	private void addCoordinateDisplayCheckbox( final java.awt.Container view, final int axis ) {
		final JCheckBox button = new JCheckBox( SimulationChart.AXIS_LABELS[ axis ] );
		button.setSelected( _simulationChart.isAxisEnabled( axis ) );
		button.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				_simulationChart.enableAxis( axis, button.isSelected() );
			}
		});
		view.add( button );		
	}
	
	
	/**
	 * Set the optimizer to display.
	 * @param optimizer the new optimizer
	 */
	public void setOptimizer( final OpticsOptimizer optimizer ) {
		if ( _optimizer == optimizer )  return;
		
		if ( _optimizer != null ) {
			_optimizer.removeOpticsOptimizerListener( this );
		}
		
		_optimizer = optimizer;
		
		if ( optimizer != null ) {
			_simulationChart.setOptimizer( optimizer );
			_objectivesTableModel.setOptimizer( optimizer );
			_variableTableModel.setOptimizer( optimizer );
			_fixedParameterTableModel.setOptimizer( optimizer );
			
			optimizer.addOpticsOptimizerListener( this );
		}
	}
	
	
	/** setup the parameter table  */
	private void setupObjectivesTable() {
		_objectivesTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		
		_objectivesTable.getColumnModel().getColumn( OptimizerObjectivesTableModel.DESIGN_VALUE_COLUMN ).setCellRenderer( new NumericCellRenderer( _objectivesTable ) );
		_objectivesTable.getColumnModel().getColumn( OptimizerObjectivesTableModel.TRIAL_VALUE_COLUMN ).setCellRenderer( new NumericCellRenderer( _objectivesTable ) );
		_objectivesTable.getColumnModel().getColumn( OptimizerObjectivesTableModel.TRIAL_SATISFACTION_COLUMN ).setCellRenderer( new NumericCellRenderer( _objectivesTable ) );
	}
	
	
	/** setup the parameter table  */
	private void setupVariablesTable() {
		_variableTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		_variableTable.getColumnModel().getColumn( VariableValueTableModel.INITIAL_VALUE_COLUMN ).setCellRenderer( new NumericCellRenderer( _variableTable ) );
		_variableTable.getColumnModel().getColumn( VariableValueTableModel.TRIAL_VALUE_COLUMN ).setCellRenderer( new NumericCellRenderer( _variableTable ) );
	}
	
	
	/** setup the parameter table  */
	private void setupFixedParametersTable() {
		_fixedParameterTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		_fixedParameterTable.getColumnModel().getColumn( FixedCustomParameterTableModel.INITIAL_VALUE_COLUMN ).setCellRenderer( new NumericCellRenderer( _fixedParameterTable ) );
	}
	
	
	/** update the maximum progress time */
	protected void updateMaximumProgressTime() {
		if ( _optimizer != null ) {
			final SolverSession session = _optimizer.getActiveSolverSession();
			final double maximumDuration = session.getMaxSolveTime();
			_progressBar.setMaximum( (int)maximumDuration );
			_durationView.setValue( new Double( maximumDuration ) );			
		}
	}
	
	
	/**
	 * Event indicating that a new trial has been evaluated.
	 * @param optimizer the optimizer producing the event
	 * @param trial the trial which was scored
	 */
	public void trialScored( final OpticsOptimizer optimizer, final Trial trial ) {
		_progressBar.setValue( (int)optimizer.getElapsedTime() );
	}
	
	
	/**
	 * Event indicating that a new optimal solution has been found
	 * @param optimizer the optimizer producing the event
	 * @param solution the new optimal solution
	 */
	public void newOptimalSolution( final OpticsOptimizer optimizer, final Trial solution ) {
		
	}
	
	
	/**
	 * Event indicating that an optimization run has been started.
	 * @param optimizer the optimizer producing the event
	 */
	public void optimizationStarted( final OpticsOptimizer optimizer ) {
		updateMaximumProgressTime();
	}
	
	
	/**
	 * Event indicating that an optimization run has stopped.
	 * @param optimizer the optimizer producing the event
	 */
	public void optimizationStopped( final OpticsOptimizer optimizer ) {
		_progressBar.setValue( 0 );
	}
	
	
	/**
	 * Event indicating that an optimization run has failed.
	 * @param optimizer the optimizer producing the event
	 * @param exception the exception thrown during optimization
	 */
	public void optimizationFailed( final OpticsOptimizer optimizer, final Exception exception ) {
		setVisible( true );
		JOptionPane.showMessageDialog( this, exception.getMessage(), "Optimizer Failed", JOptionPane.ERROR_MESSAGE );
	}
	
	
	/**
	 * Event indicating that optimizer settings have changed.
	 * @param optimizer the optimizer producing the event
	 */
	public void optimizerSettingsChanged( final OpticsOptimizer optimizer ) {}
}







