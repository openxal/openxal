//
//  OptimizerObjectivesTableModel.java
//  xal
//
//  Created by Thomas Pelaia on 6/14/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import xal.extension.solver.*;

import javax.swing.table.*;
import java.util.*;
import java.text.*;


/** table model for displaying optimizer objectives */
public class OptimizerObjectivesTableModel extends AbstractTableModel implements OpticsOptimizerListener {
    
    private final static long serialVersionUID = 1L;
    
	static final public int OBJECTIVE_COLUMN = 0;
	static final public int DESIGN_VALUE_COLUMN = 1;
	static final public int TRIAL_VALUE_COLUMN = 2;
	static final public int TRIAL_SATISFACTION_COLUMN = 3;
	
	
	/** solution lock */
	final protected Object SOLUTION_LOCK;
	
	/** formatter for displaying values */
	static final private DecimalFormat VALUE_FORMAT;
	
	/** formatter for displaying satisfaction */
	static final private DecimalFormat SATISFACTION_FORMAT;
	
	/** optimizer */
	protected OpticsOptimizer _optimizer;
	
	/** objectives to view */
	protected List<OpticsObjective> _objectives;
	
	/** trial from which to get the values for the objectives */
	protected Trial _solution;
	
	/** the simulation from which to get the values for the objectives */
	protected Simulation _designSimulation;
	
	
	// static initializer
	static {
		VALUE_FORMAT = new DecimalFormat( "#,##0.00" );
		SATISFACTION_FORMAT = new DecimalFormat( "#,##0.0000 %" );
	}
	
	
	/** Constructor */
	public OptimizerObjectivesTableModel( final OpticsOptimizer optimizer ) {
		SOLUTION_LOCK = new Object();
		
		setOptimizer( optimizer );
	}
	
	
	
	
	/**
		* Get the name of the specified column.
	 * @param column the index of the column for which to get the name.
	 * @return the name of the specified column
	 */
	public String getColumnName( final int column ) {
		switch ( column ) {
			case OBJECTIVE_COLUMN:
				return "Objective";
			case DESIGN_VALUE_COLUMN:
				return "Design Value";
			case TRIAL_VALUE_COLUMN:
				return "Trial Value";
			case TRIAL_SATISFACTION_COLUMN:
				return "Satisfaction";
			default:
				return "?";
		}
	}
	
	
	/**
	 * Get the data class for the specified column.
	 */
	public Class<String> getColumnClass( final int column ) {
		switch( column ) {
			default:
				return String.class;
		}
	}
	
	
	/**
	 * Determine if the specified cell is editable.
	 */
	public boolean isCellEditable( final int row, final int column ) {
		return false;
	}
	
	
	/**
	 * Get the number of rows to display.
	 * @return the number of rows to display.
	 */
	public int getRowCount() {
		synchronized ( SOLUTION_LOCK ) {
			return ( _objectives != null ) ? _objectives.size() : 0;
		}
	}
	
	
	/**
		* Get the number of columns to display.
	 * @return the number of columns to display.
	 */
	public int getColumnCount() {
		return 4;
	}
	
	
	/**
		* Get the value for the specified cell.
	 * @param row the row of the cell to update.
	 * @param column the column of the cell to update.
	 * @return the value to display in the specified cell.
	 */
	public Object getValueAt( final int row, final int column ) {
		synchronized( SOLUTION_LOCK ) {
			if ( row >= _objectives.size() )  return "";
			final OpticsObjective objective = _objectives.get( row );
			
			switch ( column ) {
				case OBJECTIVE_COLUMN:
					return objective.getLabel();
				case DESIGN_VALUE_COLUMN:
					return ( _designSimulation != null ) ? VALUE_FORMAT.format( objective.getDisplayValue( objective.getValue( null, _designSimulation, _designSimulation ) ) ) : "";
				case TRIAL_VALUE_COLUMN:
					return ( _solution != null ) ? VALUE_FORMAT.format( objective.getDisplayValue( _solution.getScore( objective ).getValue() ) ) : "";
				case TRIAL_SATISFACTION_COLUMN:
					return ( _solution != null ) ? SATISFACTION_FORMAT.format( _solution.getSatisfaction( objective ) ) : "";
				default:
					return "?";
			}			
		}
	}
	
	
	/** update the objectives */
	public void updateObjectives() {
		synchronized( SOLUTION_LOCK ) {
			if ( _optimizer != null ) {
				_objectives = _optimizer.getActiveSolverSession().getEnabledObjectives();
				_designSimulation = _optimizer.getActiveSolverSession().getDesignSimulation();
			}
			else {
				_objectives = Collections.emptyList();
				_designSimulation = null;
			}			
		}
	}
	
	
	/**
	 * Set the optimizer to display.
	 * @param optimizer the new optimizer
	 */
	public void setOptimizer( final OpticsOptimizer optimizer ) {
		synchronized( SOLUTION_LOCK ) {
			if ( _optimizer != null ) {
				_optimizer.removeOpticsOptimizerListener( this );
			}
			
			_optimizer = optimizer;
			_solution = null;
			
			if ( optimizer != null ) {
				optimizer.addOpticsOptimizerListener( this );				
			}
			
			updateObjectives();
		}
		
		fireTableDataChanged();
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
	public void newOptimalSolution( final OpticsOptimizer optimizer, final Trial solution ) {
		synchronized( SOLUTION_LOCK ) {
			_solution = solution;			
		}
		
		fireTableDataChanged();				
	}
	
	
	/**
	 * Event indicating that an optimization run has been started.
	 * @param optimizer the optimizer producing the event
	 */
	public void optimizationStarted( final OpticsOptimizer optimizer ) {
		synchronized ( SOLUTION_LOCK ) {
			_solution = null;
			updateObjectives();
		}
		
		fireTableDataChanged();
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
