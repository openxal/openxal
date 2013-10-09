//
//  QuadTableModel.java
//  xal
//
//  Created by Thomas Pelaia on 5/5/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import xal.tools.data.*;
import xal.tools.text.FormattedNumber;

import javax.swing.table.*;
import java.util.*;
import java.text.*;


/** Table of live parameters. */
public class LiveParameterTableModel extends AbstractTableModel implements LiveParameterListener, LiveParameterQualifierListener {
    
    private static final long serialVersionUID = 1L;
    
	static final public int NODE_ID_COLUMN = 0;
	static final public int POSITION_COLUMN = 1;
	static final public int VARIABLE_COLUMN = 2;
	static final public int NAME_COLUMN = 3;
	static final public int ACTIVE_SOURCE_COLUMN = 4;
	static final public int DESIGN_VALUE_COLUMN = 5;
	static final public int CONTROL_VALUE_COLUMN = 6;
	static final public int INITIAL_VALUE_COLUMN  = 7;
	static final public int LOWER_LIMIT_COLUMN = 8;
	static final public int UPPER_LIMIT_COLUMN = 9;
	static final public int NUM_COLUMNS = 10;
		
	/** formatter for displaying control values */
	static final private DecimalFormat CONTROL_VALUE_FORMAT;
	
	/** synchronization lock for the parameters list */
	final private Object PARAMETERS_LOCK;

	/** list of live parameters */
	volatile protected List<LiveParameter> _parameters;
	volatile protected List<LiveParameter> _allParameters;
	protected LiveParameterQualifier _qualifier;
	
	
	// static initializer
	static {
		CONTROL_VALUE_FORMAT = new DecimalFormat( "0.00000" );
	}
	
	
	/** Constructor */
	public LiveParameterTableModel() {
		PARAMETERS_LOCK = new Object();
		
		_allParameters = new ArrayList<LiveParameter>();
		_parameters = new ArrayList<LiveParameter>();
		
		setQualifier( new LiveParameterQualifier() );
	}
	
	
	/**
	 * Set the parameters to qualify for display.
	 * @param parameters the parameters to display in the table.
	 */
	public void setParameters( final List<LiveParameter> parameters ) {
		for ( LiveParameter parameter : _allParameters ) {
			parameter.removeLiveParameterListener( this );
		}
		
		synchronized( PARAMETERS_LOCK ) {
			_allParameters = ( parameters != null ) ? parameters : new ArrayList<LiveParameter>();			
		}
		
		for ( LiveParameter parameter : parameters ) {
			parameter.addLiveParameterListener( this );
		}
		
		filterParameters();
	}
	
	
	/**
	 * Get the parameters that have been filtered by the qualifier.
	 */
	public List<LiveParameter> getFilteredParameters() {
		return _parameters;
	}
	
	
	/**
	 * Get the qualifier.
	 */
	public LiveParameterQualifier getQualifier() {
		return _qualifier;
	}
	
	
	/**
	 * Set the qualifier to the one specified.
	 */
	public void setQualifier( final LiveParameterQualifier qualifier ) {
		if ( qualifier != _qualifier ) {
			if ( _qualifier != null ) {
				_qualifier.removeLiveParameterQualifierListener( this );				
			}
			
			_qualifier = qualifier;
			
			if ( qualifier != null ) {
				qualifier.addLiveParameterQualifierListener( this );				
			}
			else {
				filterParameters();
			}
			
		}
	}
	
	
	/** Filter the parameters with the qualifier. */
	public void filterParameters() {
		final List<LiveParameter> parameters = new ArrayList<>();
		synchronized ( PARAMETERS_LOCK ) {
			if ( _qualifier == null ) {
				parameters.addAll( _allParameters );
			}
			else {
				final int NUM_PARAMETERS = _allParameters.size();
				for ( int param = 0 ; param < NUM_PARAMETERS ; param++ ) {
					final LiveParameter parameter = _allParameters.get( param );
					if ( _qualifier.matches( parameter ) )  parameters.add( parameter );
				}
			}
		}
		
		_parameters = parameters;
		
		fireTableDataChanged();
	}
	
	
	/**
	 * Get the name of the specified column.
	 * @param column the index of the column for which to get the name.
	 * @return the name of the specified column
	 */
	public String getColumnName( final int column ) {
		switch ( column ) {
			case VARIABLE_COLUMN:
				return "Variable";
			case NODE_ID_COLUMN:
				return "Node";
			case POSITION_COLUMN:
				return "Position";
			case NAME_COLUMN:
				return "Parameter";
			case DESIGN_VALUE_COLUMN:
				return "Design Value";
			case CONTROL_VALUE_COLUMN:
				return "Control Value";
			case ACTIVE_SOURCE_COLUMN:
				return "Active Source";
			case INITIAL_VALUE_COLUMN:
				return "Initial Value";
			case LOWER_LIMIT_COLUMN:
				return "Lower Limit";
			case UPPER_LIMIT_COLUMN:
				return "Upper Limit";
			default:
				return "?";
		}
	}
	
	
	/**
	 * Get the data class for the specified column.
	 */
	public Class<?> getColumnClass( final int column ) {
		switch( column ) {
			case VARIABLE_COLUMN:
				return Boolean.class;
			case POSITION_COLUMN:
				return Number.class;
			case DESIGN_VALUE_COLUMN:
				return Number.class;
			case CONTROL_VALUE_COLUMN:
				return Number.class;
			case INITIAL_VALUE_COLUMN:
				return Number.class;
			case LOWER_LIMIT_COLUMN:
				return Number.class;
			case UPPER_LIMIT_COLUMN:
				return Number.class;
			default:
				return String.class;
		}
	}
	
	
	/**
	 * Determine if the specified cell is editable.
	 */
	public boolean isCellEditable( final int row, final int column ) {
		switch( column ) {
			case VARIABLE_COLUMN:
				return false;
			default:
				return false;
		}
	}
	
	
	/**
	 * Get the number of rows to display.
	 * @return the number of rows to display.
	 */
	public int getRowCount() {
		synchronized ( PARAMETERS_LOCK ) {
			return _parameters.size();
		}
	}
	
	
	/**
	 * Get the number of columns to display.
	 * @return the number of columns to display.
	 */
	public int getColumnCount() {
		return NUM_COLUMNS;
	}
	
	
	/**
	 * Get the value for the specified cell.
	 * @param row the row of the cell to update.
	 * @param column the column of the cell to update.
	 * @return the value to display in the specified cell.
	 */
	public Object getValueAt( final int row, final int column ) {
		synchronized( PARAMETERS_LOCK ) {
			if ( row >= _parameters.size() )  return null;
			final LiveParameter parameter = _parameters.get( row );
			
			switch ( column ) {
				case VARIABLE_COLUMN:
					return new Boolean( parameter.isVariable() );
				case NODE_ID_COLUMN:
					return parameter.getNodeAgent().getID();
				case POSITION_COLUMN:
					return new FormattedNumber( CONTROL_VALUE_FORMAT, parameter.getPosition() );
				case NAME_COLUMN:
					return getHTMLConnectionText( parameter, parameter.getName() );
				case DESIGN_VALUE_COLUMN:
					return new FormattedNumber( CONTROL_VALUE_FORMAT, parameter.getDesignValue() );
				case ACTIVE_SOURCE_COLUMN:
					return parameter.getActiveSourceName();
				case CONTROL_VALUE_COLUMN:
					return new FormattedNumber( CONTROL_VALUE_FORMAT, parameter.getLatestControlValue() );
				case INITIAL_VALUE_COLUMN:
					return new FormattedNumber( CONTROL_VALUE_FORMAT, parameter.getInitialValue() );
				case LOWER_LIMIT_COLUMN:
					return new FormattedNumber( CONTROL_VALUE_FORMAT, parameter.getLowerLimit() );
				case UPPER_LIMIT_COLUMN:
					return new FormattedNumber( CONTROL_VALUE_FORMAT, parameter.getUpperLimit() );
				default:
					return null;
			}			
		}
	}
	
	
	/**
	 * Set the value of the specified cell to the value specified.
	 */
	public void setValueAt( final Object value, final int row, final int column ) {
		final LiveParameter parameter = _parameters.get( row );
		
		switch ( column ) {
			case VARIABLE_COLUMN:
				parameter.setIsVariable( ((Boolean)value).booleanValue() );
				break;
			default:
				break;
		}
	}
	
	
	/**
	 * Get the parameter at the specified row.
	 * @return the live parameter displayed at the specified row.
	 */
	final public LiveParameter getParameter( final int row ) {
		return ( row >= 0 && row < getRowCount() ) ? _parameters.get( row ) : null;
	}
	
	
	/**
	 * Get the HTML to colorize the text based on the parameter's connection status.
	 * @return HTML representation of the text.
	 */
	static private String getHTMLConnectionText( final LiveParameter parameter, final String text ) {
		final String color = parameter.isConnected() ? "GREEN>" : "RED>";
		return "<html><body><font color=" + color + text + "</font></body></html>";
	}
	
	
	/**
	 * Update the row for the specified parameter.
	 * @param parameter the parameter whose row should be updated
	 * @param column the column of the cell to update
	 */
	protected void updateCell( final LiveParameter parameter, final int column ) {
		synchronized( PARAMETERS_LOCK ) {
			final int row = _parameters.indexOf( parameter );
			if ( row >= 0 ) {
				fireTableCellUpdated( row, column );								
			}
		}		
	}
	
	
	/**
	 * Handle the event indicating that parameter's control channel connection has changed.
	 * @param parameter the live parameter whose connection has changed.
	 * @param isConnected true if the channel is now connected and false if it is disconnected
	 */
	public void controlConnectionChanged( final LiveParameter parameter, final boolean isConnected ) {
		updateCell( parameter, NAME_COLUMN );
	}
	
	
	/** 
		* Handle the event indicating that parameter's readback channel connection has changed.
		* @param parameter the live parameter whose connection has changed.
		* @param isConnected true if the channel is now connected and false if it is disconnected
		*/
	public void readbackConnectionChanged( final LiveParameter parameter, final boolean isConnected ) {
	}
	
	
	/** 
		* Handle the event indicating that parameter's control value has changed.
		* @param parameter the live parameter whose value has changed.
		* @param value the new control value of the parameter
		*/
	public void controlValueChanged( final LiveParameter parameter, final double value ) {
		updateCell( parameter, CONTROL_VALUE_COLUMN );
	}
	
	
	/**
	 * Handle the event indicating that parameter's readback value has changed.
	 * @param parameter the live parameter whose value has changed.
	 * @param value the new readback value of the parameter
	 */
	public void readbackValueChanged( final LiveParameter parameter, final double value ) {
	}
	
	
	/**
	 * Handle the event in which the parameter's variable status has changed.
	 * @param parameter the live parameter whose value has changed.
	 * @param isVariable indicates whether the parameter is now variable or not
	 */
	public void variableStatusChanged( final LiveParameter parameter, final boolean isVariable ) {
		updateCell( parameter, VARIABLE_COLUMN );
	}
	
	
	/**
	 * Handle the event in which the parameter's custom value has changed.
	 * @param parameter the core parameter whose value has changed.
	 * @param value the new custom value
	 */
	public void customValueChanged( LiveParameter parameter, double value ) {}
	
	
	/**
	 * Handle the event in which the parameter's custom limits have changed.
	 * @param parameter the core parameter whose limits have changed.
	 * @param limits the new custom limits
	 */
	public void customLimitsChanged( LiveParameter parameter, double[] limits ) {}
	
	
	/**
	 * Handle the event in which the parameter's initial variable value has changed.
	 * @param parameter the live parameter whose value has changed.
	 * @param value the new initial variable value
	 */
	public void initialValueChanged( LiveParameter parameter, double value ) {
		updateCell( parameter, INITIAL_VALUE_COLUMN );
	}
	
	
	/**
	 * Handle the event in which the parameter's lower variable limit has changed.
	 * @param parameter the live parameter whose value has changed.
	 * @param limit the new lower variable limit
	 */
	public void lowerLimitChanged( LiveParameter parameter, double limit ) {
		updateCell( parameter, LOWER_LIMIT_COLUMN );
	}
	
	
	/**
	 * Handle the event in which the parameter's upper variable limit has changed.
	 * @param parameter the live parameter whose value has changed.
	 * @param limit the new upper variable limit
	 */
	public void upperLimitChanged( LiveParameter parameter, double limit ) {
		updateCell( parameter, UPPER_LIMIT_COLUMN );
	}
	
	
	/**
	 * Handle the event in which the parameter's variable source has changed.
	 * @param parameter the live parameter whose variable source has changed.
	 * @param source the indicator of the parameter's variable source
	 */
	public void variableSourceChanged( LiveParameter parameter, int source ) {
		updateCell( parameter, ACTIVE_SOURCE_COLUMN );
		updateCell( parameter, INITIAL_VALUE_COLUMN );
		updateCell( parameter, LOWER_LIMIT_COLUMN );
		updateCell( parameter, UPPER_LIMIT_COLUMN );
	}
	
	
	/**
	 * Event indicating that the parameter qualifier has changed.
	 * @param qualifier the qualifier which has changed.
	 */
	public void qualifierChanged( LiveParameterQualifier qualifier ) {
		filterParameters();
	}
}


