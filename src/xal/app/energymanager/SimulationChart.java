/*
 *  ChartModel.java
 *
 *  Created on Wed Jan 07 13:46:12 EST 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */

package xal.app.energymanager;

import javax.swing.*;
import java.awt.event.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.text.*;
import java.util.*;

import xal.extension.solver.*;
import xal.extension.widgets.plot.*;
import xal.tools.apputils.*;
import xal.smf.AcceleratorSeq;
import xal.extension.widgets.smf.XALSynopticPanel;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;


/**
 * SimulationChart
 * @author tap
 */
public class SimulationChart implements OpticsOptimizerListener {
	/** index of the X coordinate result */
	static final public int X = Simulation.X_INDEX;
	
	/** index of the Y coordinate result */
	static final public int Y = Simulation.Y_INDEX;
	
	/** index of the Z coordinate result */
	static final public int Z = Simulation.Z_INDEX;
	
	/** names of the coordinates */
	static final public String[] AXIS_LABELS;
	
	/** index of the beta data */
	static final public int BETA = 0;
	
	/** index of the beta error data */
	static final public int BETA_ERROR = 1;
	
	/** index of the chromatic dispersion data */
	static final public int ETA = 2;
	
	/** index of the alpha data */
	static final public int ALPHA = 3;
	
	/** index of the emittance data */
	static final public int EMITTANCE = 4;
	
	/** index of the kinetic energy scalar data */
	static final public int KINETIC_ENERGY = 0;
	
	/** name of the data to display */
	static final public String[] DATA_LABELS;
	
	/** name of the scalar data to display */
	static final public String[] SCALAR_DATA_LABELS;
	
	/** index of design simulation */
	static final public int DESIGN = 0;
	
	/** index of trial simulation */
	static final public int TRIAL = 1;
	
	/** names of the simulations */
	static final public String[] SIMULATION_LABELS;
	
	/** chart date format */
	final static protected DateFormat CHART_DATE_FORMAT;
	
	/** optimizer */
	protected OpticsOptimizer _optimizer;
	
	/** the simulations */
	protected Simulation[] _simulations;
	
	/** chart of traces */
	protected FunctionGraphsJPanel _chart;
	
	/** view of nodes */
	protected XALSynopticPanel _nodeView;
	
	/** display the beta and error data */
	protected boolean[] _showData;
	
	/** display the kinetic energy */
	protected boolean[] _showScalarData;
	
	/** display the design and trial traces */
	protected boolean[] _showSimulation;
	
	/** array indicating whether to display the axis corresponding to the index */
	protected boolean[] _showAxis;
	
	/** series data cache for data associated with axes */
	protected BasicGraphData[][][] _seriesCache;
	
	/** series data cache for scalar data */
	protected BasicGraphData[][] _scalarSeriesCache;
	
	
	/* static initializer */
	static {
		CHART_DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");
		AXIS_LABELS = new String[] { "X", "Y", "Z" };
		SIMULATION_LABELS = new String[] { "Design", "Trial" };
		DATA_LABELS = new String[] { "Beta", "Beta Error", "Eta", "Alpha", "Emittance" };
		SCALAR_DATA_LABELS = new String[] { "Kinetic Energy" };
	}
	
	
	/**
	 * Constructor
	 * @param  optimizer  the optics optimizer
	 */
	public SimulationChart( final OpticsOptimizer optimizer ) {		
		_chart = createChart();
		SimpleChartPopupMenu.addPopupMenuTo( _chart );
		
		//createIconicNodeView();
		
		_showData = new boolean[] { true, true, true, false, false };
		_showScalarData = new boolean[] { true };
		_showAxis = new boolean[] { true, true, true };
		_showSimulation = new boolean[] { true, true };
		_simulations = new Simulation[] { null, null };
		_seriesCache = new BasicGraphData[ _showSimulation.length ][ _showData.length ][ _showAxis.length ];
		_scalarSeriesCache = new BasicGraphData[ _showSimulation.length ][ _showScalarData.length ];
		
		setOptimizer( optimizer );
		//synchronizeNodeView();
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
			_simulations[ DESIGN ] = optimizer.getActiveSolverSession().getDesignSimulation();
			optimizer.addOpticsOptimizerListener( this );
		}
		else {
			_simulations[ TRIAL ] = null;
			_simulations[ DESIGN ] = null;
		}			
	}
	
	
	/**
	 * Get this model's chart.
	 * @return    this model's chart
	 */
	public FunctionGraphsJPanel getChart() {
		return _chart;
	}
	
	
	/** Get the node view */
	public JComponent getNodeView() {
		return _nodeView;
	}
	
	
	/**
	 * Determine whether the specified axis is enabled.
	 * @param axis the index of the axis for which to get the enable state (X, Y or Z)
	 * @return true if it is enabled and false if not
	 */ 
	synchronized public boolean isAxisEnabled( final int axis ) {
		return _showAxis[ axis ];
	}
	
	
	/**
	 * Set whether or not to display the specified axis.
	 * @param axis the index of the axis to enable/disable (X, Y or Z)
	 * @param enable true to enable the axis and false to disable it
	 */
	synchronized public void enableAxis( final int axis, final boolean enable ) {
		_showAxis[ axis ] = enable;
		updateTraces();
	}
	
	
	/**
	 * Determine whether the specified simulation is enabled.
	 * @param source the index of the source for which to get the enable state (DESIGN or TRIAL)
	 * @return true if it is enabled and false if not
	 */ 
	synchronized public boolean isSimulationEnabled( final int source ) {
		return _showSimulation[ source ];
	}
	
	
	/**
	 * Set whether or not to display the simulation.
	 * @param source the index of the source to enable/disable (DESIGN or TRIAL)
	 * @param enable true to enable the simulation and false to disable it
	 */
	synchronized public void enableSimulation( final int source, final boolean enable ) {
		_showSimulation[ source ] = enable;
		updateTraces();
	}
	
	
	/**
	 * Determine whether the specified data is enabled.
	 * @param dataID the ID of the data to display/hide (beta, beta error or eta)
	 * @return true if the data is enabled and false if not
	 */
	synchronized public boolean isDataEnabled( final int dataID ) {
		return _showData[ dataID ];
	}
	
	
	/**
	 * Set whether or not to display the specified data.
	 * @param dataID the ID of the data to display/hide (beta, beta error or eta)
	 * @param enable true to enable the data and false to disable it
	 */
	synchronized public void enableData( final int dataID, final boolean enable ) {
		_showData[ dataID ] = enable;
		updateTraces();
	}
	
	
	/**
	 * Determine whether the specified scalar data is enabled.
	 * @param dataID the ID of the data to display/hide (kinetic energy)
	 * @return true if the data is enabled and false if not
	 */
	synchronized public boolean isScalarDataEnabled( final int dataID ) {
		return _showScalarData[ dataID ];
	}
	
	
	/**
	 * Set whether or not to display the specified scalar data.
	 * @param dataID the ID of the data to display/hide (kinetic energy)
	 * @param enable true to enable the data and false to disable it
	 */
	synchronized public void enableScalarData( final int dataID, final boolean enable ) {
		_showScalarData[ dataID ] = enable;
		updateTraces();
	}
	
	
	/**
	 * Get the number of traces to display on the chart.
	 * @return the number of traces to display on the chart.
	 */
	synchronized public int getNumTraces() {
		return getEnabledCount( _showSimulation ) * ( getEnabledCount( _showAxis ) * getEnabledCount( _showData ) + getEnabledCount( _showScalarData ) );
	}
	
	
	/**
	 * Determine the number of enabled items in the array.
	 * @param array the boolean array of items to consider
	 * @return the number of enabled items in the array
	 */
	static protected int getEnabledCount( final boolean[] array ) {
		int count = 0;
		
		for ( int index = 0 ; index < array.length ; index++ ) {
			count += ( array[ index ] ? 1 : 0 );
		}
		
		return count;
	}
	
	
	/** Update the traces to display. */
	synchronized public void updateTraces() {
		if ( _optimizer == null )  return;
		
		final int numTraces = getNumTraces();
		final Vector<BasicGraphData> series = new Vector<>( numTraces );
		
		for ( int simulation = 0 ; simulation < _showSimulation.length ; simulation++ ) {
			if ( !_showSimulation[ simulation ] )  continue;
			for ( int dataID = 0 ; dataID < _showData.length ; dataID++ ) {
				if ( !_showData[ dataID ] )  continue;
				for ( int axis = 0 ; axis < _showAxis.length ; axis++ ) {
					if ( _showAxis[ axis ] ) {
						final BasicGraphData graphData = _seriesCache[ simulation ][ dataID ][ axis ];
						if ( graphData != null ) {
							series.add( graphData );
						}
					}
				}
			}
			for ( int scalarDataID = 0 ; scalarDataID < _showScalarData.length ; scalarDataID++ ) {
				if ( !_showScalarData[ scalarDataID ] )  continue;
				final BasicGraphData graphData = _scalarSeriesCache[ simulation ][ scalarDataID ];
				if ( graphData != null ) {
					series.add( graphData );
				}
			}
		}
		
		_chart.removeAllGraphData();
		_chart.addGraphData( series );
	}
	
	
	/** Generate the graph data to display */
	protected void generateGraphData() {
		for ( int simulationIndex = 0 ; simulationIndex < _simulations.length ; simulationIndex++ ) {
			final Simulation simulation = _simulations[ simulationIndex ];
			if ( simulation == null )  continue;
			
			// vector data (one value per coordinate per element)
			final double[][] betaArray = simulation.getBeta();
			final double[][] betaErrorArray = simulation.getPercentBetaError( _simulations[DESIGN] );
			final double[][] etaArray = simulation.getEta();
			final double[][] alphaArray = simulation.getAlpha();
			final double[][] emittanceArray = simulation.getEmittance();
			
			// scalar data (one value per element)
			final double[] kineticEnergyArray = simulation.getKineticEnergy();
			
			generateGraphData( simulationIndex, BETA, betaArray );
			generateGraphData( simulationIndex, BETA_ERROR, betaErrorArray );
			generateGraphData( simulationIndex, ETA, etaArray );
			generateGraphData( simulationIndex, ALPHA, alphaArray );
			generateGraphData( simulationIndex, EMITTANCE, emittanceArray );
			
			generateGraphData( simulationIndex, KINETIC_ENERGY, kineticEnergyArray );
		}
		
		updateTraces();
	}
	
	
	/**
	 * Generate the graph data for the specified data type and the given array.
	 * @param simulationIndex the simulation source index
	 * @param dataID the data identifier
	 * @param array the array of data indexed by axis and the element number
	 */
	protected void generateGraphData( final int simulationIndex, final int dataID, final double[][] array ) {
		final int NUM_AXES = _showAxis.length;
		final Simulation simulation = _simulations[ simulationIndex ];
		
		for ( int axis = 0 ; axis < NUM_AXES ; axis++ ) {
			double[] positions = simulation.getPositions();
			double[] values = array[ axis ];
			final BasicGraphData graphData = new BasicGraphData();
			graphData.addPoint( positions, values );
			graphData.setGraphColor( IncrementalColors.getColor( NUM_AXES * simulationIndex + axis ) );
			graphData.setGraphProperty( _chart.getLegendKeyString(), SIMULATION_LABELS[ simulationIndex ] + ": " + DATA_LABELS[ dataID ] + " " + AXIS_LABELS[ axis ] );
			
			_seriesCache[ simulationIndex ][ dataID ][ axis ] = graphData;
		}		
	}
	
	
	/**
	 * Generate the graph data for the specified data type and the given array of scalar data.
	 * @param simulationIndex the simulation source index
	 * @param scalarDataID the scalar data identifier
	 * @param array the array of data indexed by the element number
	 */
	protected void generateGraphData( final int simulationIndex, final int scalarDataID, final double[] array ) {
		final int NUM_AXES = _showAxis.length;
		final Simulation simulation = _simulations[ simulationIndex ];
		
		double[] positions = simulation.getPositions();
		final BasicGraphData graphData = new BasicGraphData();
		graphData.addPoint( positions, array );
		graphData.setGraphColor( IncrementalColors.getColor( NUM_AXES * simulationIndex ) );
		graphData.setGraphProperty( _chart.getLegendKeyString(), SIMULATION_LABELS[ simulationIndex ] + ": " + SCALAR_DATA_LABELS[ scalarDataID ] );
		
		_scalarSeriesCache[ simulationIndex ][ scalarDataID ] = graphData;
	}
	
	
	/** Update the chart with new data. */
	protected void updateChart() {
		generateGraphData();
		updateTraces();
	}
	
	
	/**
	 * Create the chart for displaying traces.
	 * @return    a new chart for displaying traces.
	 */
	protected FunctionGraphsJPanel createChart() {
		FunctionGraphsJPanel chart = new FunctionGraphsJPanel();
		chart.setBackground( Color.gray );
		
		// labels
		chart.setName( "Beta Function" );
		chart.setAxisNameX( "Position from sequence start (m)" );
		chart.setAxisNameY( "Parameter Value" );
		
		chart.setNumberFormatX( new DecimalFormat( "0.00E0" ) );
		chart.setNumberFormatY( new DecimalFormat( "0.00E0" ) );
		
		// add the horizontal and vertical guides
		chart.addVerticalLine( chart.getCurrentMinX(), Color.black );
		chart.addVerticalLine( chart.getCurrentMaxX(), Color.black );
		chart.addHorizontalLine( chart.getCurrentMinY(), Color.black );
		chart.addHorizontalLine( chart.getCurrentMaxY(), Color.black );
		
		// Hide buttons on the plot since they are handled by a dialog box
		chart.setChooseModeButtonVisible( false );
		chart.setHorLinesButtonVisible( false );
		chart.setVerLinesButtonVisible( false );
		
		// Show the horizontal and vertical guides
		chart.setDraggingHorLinesGraphMode( true );
		chart.setDraggingVerLinesGraphMode( true );
		
		// add legend support
		chart.setLegendPosition( FunctionGraphsJPanel.LEGEND_POSITION_ARBITRARY );
		chart.setLegendKeyString( "Legend" );
		chart.setLegendBackground( Color.lightGray );
		chart.setLegendColor( Color.black );
		chart.setLegendVisible( true );
		
		return chart;
	}
	
	
	/** Create iconic node view */
	protected void createIconicNodeView() {
		_nodeView = new XALSynopticPanel();
		
		_chart.addHorLimitsListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				synchronizeNodeView();
			}
		});
	}
	
	
	/** Synchronize the node view with the chart's horizontal axis. */
	protected void synchronizeNodeView() {
        final double start = _chart.getCurrentMinX();
        final double end = _chart.getCurrentMaxX();
        
        final int left = _chart.getScreenX( start );
        final int right = _nodeView.getWidth() - _chart.getScreenX( end );
        
        if ( _nodeView.getMargin().left != left || _nodeView.getMargin().right != right || start != _nodeView.getStartPosition() || end != _nodeView.getEndPosition() ) {
            _nodeView.setMargin( new Insets(5, left, 5, right) );
			if ( start < _nodeView.getEndPosition() ) {
				_nodeView.setStartPosition( start );
				_nodeView.setEndPosition( end );
			}
			else {
				_nodeView.setEndPosition( end );
				_nodeView.setStartPosition( start );
			}
            _nodeView.repaint();
        }
	}
    
    
    /** 
     * Returns the visibility of the grid.
     * @return True if the grid is visible and False if the grid is not visible.
     */
    public boolean isGridVisible() {
        return _chart.getGridLinesVisibleX() || _chart.getGridLinesVisibleY();
    }
    
    
    /** 
     * Sets the visibility of the grid.
     * @param isVisible True if the grid should be made visible and False if it should be hidden.
     */
    public void setGridVisible(boolean isVisible) {
		if ( isVisible == isGridVisible() )  return;	// nothing to do
		
        _chart.setGridLinesVisibleX(isVisible);
        _chart.setGridLinesVisibleY(isVisible);
    }
    
    
    /** 
     * Toggle the visibility of the chart grid.
     */
    public void toggleGridVisible() {
        setGridVisible( !isGridVisible() );
    }
    
    
    /** 
     * Returns the visibility of the legend.
     * @return True if the legend is visible and False if the legend is not visible.
     */
    public boolean isLegendVisible() {
		return _chart.isLegendVisible();
    }
    
    
    /** 
     * Sets the visibility of the legend.
     * @param isVisible True if the legend should be made visible and False if it should be hidden.
     */
    public void setLegendVisible( final boolean isVisible ) {
		_chart.setLegendVisible(isVisible);
    }
    
    
    /** 
     * Toggle the visibility of the chart legend.
     */
    public void toggleLegendVisible() {
        _chart.setLegendVisible( !_chart.isLegendVisible() );
    }
	
	
	/**
	 * Event indicating that a new trial has been evaluated.
	 * @param optimizer the optimizer producing the event
	 * @param trial the trial which was scored
	 */
	public void trialScored( final OpticsOptimizer optimizer, final Trial trial ) {}
	
	
	/**
	 * Event indicating that a new optimal solution has been found
	 * @param optimizer the optimizer producing the event
	 * @param solution the new optimal solution
	 */
	synchronized public void newOptimalSolution( final OpticsOptimizer optimizer, final Trial solution ) {
		_simulations[ TRIAL ] = (Simulation)solution.getCustomInfo();
		updateChart();
	}
	
	
	/**
	 * Event indicating that an optimization run has been started.
	 * @param optimizer the optimizer producing the event
	 */
	synchronized public void optimizationStarted( final OpticsOptimizer optimizer ) {
		_simulations[ DESIGN ] = optimizer.getActiveSolverSession().getDesignSimulation();
		_simulations[ TRIAL ] = null;
		updateChart();
	}
	
	
	/**
	 * Event indicating that an optimization run has stopped.
	 * @param optimizer the optimizer producing the event
	 */
	public void optimizationStopped( final OpticsOptimizer optimizer ) {}	
	
	
	/**
	 * Event indicating that an optimization run has failed.
	 * @param optimizer the optimizer producing the event
	 * @param exception the exception thrown during optimization
	 */
	public void optimizationFailed( final OpticsOptimizer optimizer, final Exception exception ) {}
	
	
	/**
	 * Event indicating that optimizer settings have changed.
	 * @param optimizer the optimizer producing the event
	 */
	public void optimizerSettingsChanged( OpticsOptimizer optimizer ) {}
}

