//
//  FixedCustomParameterTableModel.java
//  xal
//
//  Created by Thomas Pelaia on 6/16/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//




package xal.app.energymanager;

import xal.extension.solver.*;

import javax.swing.table.*;
import java.util.*;
import java.text.*;


/** table model for displaying optimizer objectives */
public class FixedCustomParameterTableModel extends AbstractTableModel implements OpticsOptimizerListener {
    
    private static final long serialVersionUID = 1L;
    
	static final public int NAME_COLUMN = 0;
	static final public int INITIAL_VALUE_COLUMN = 1;
	
	
	/** solution lock */
	final protected Object SOLUTION_LOCK;
	
	/** formatter for displaying values */
	static final private DecimalFormat VALUE_FORMAT;
	
	/** optimizer */
	protected OpticsOptimizer _optimizer;
	
	/** variables */
	protected List<CoreParameter> _parameters;
		
	
	// static initializer
	static {
		VALUE_FORMAT = new DecimalFormat( "0.0000" );
	}
	
	
	/** Constructor */
	public FixedCustomParameterTableModel( final OpticsOptimizer optimizer ) {
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
			case NAME_COLUMN:
				return "Fixed Parameter";
			case INITIAL_VALUE_COLUMN:
				return "Custom Value";
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
			return ( _parameters != null ) ? _parameters.size() : 0;
		}
	}
	
	
	/**
	 * Get the number of columns to display.
	 * @return the number of columns to display.
	 */
	public int getColumnCount() {
		return 2;
	}
	
	
	/**
	 * Get the value for the specified cell.
	 * @param row the row of the cell to update.
	 * @param column the column of the cell to update.
	 * @return the value to display in the specified cell.
	 */
	public Object getValueAt( final int row, final int column ) {
		synchronized( SOLUTION_LOCK ) {
			if ( row >= _parameters.size() )  return null;
			
			final CoreParameter parameter = _parameters.get( row );
			
			switch ( column ) {
				case NAME_COLUMN:
					return parameter.getName();
				case INITIAL_VALUE_COLUMN:
					return VALUE_FORMAT.format( parameter.getInitialValue() );
				default:
					return "?";
			}
		}
	}
	
	
	/** update the parameters from the optimizer */
	protected void updateParameters() {
		synchronized ( SOLUTION_LOCK ) {
			if ( _optimizer != null ) {
				_parameters = _optimizer.getActiveSolverSession().getFixedCustomParameters();
			}
			else {
				_parameters = Collections.<CoreParameter>emptyList();
			}
			System.out.println( "Fixed parameters: " + _parameters );
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
			
			if ( optimizer != null ) {
				_optimizer.addOpticsOptimizerListener( this );				
			}
			
			updateParameters();
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
	}
	
	
	/**
	 * Event indicating that an optimization run has been started.
	 * @param optimizer the optimizer producing the event
	 */
	public void optimizationStarted( final OpticsOptimizer optimizer ) {
		System.out.println( "*********** Optimization started ************" );
		updateParameters();
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
