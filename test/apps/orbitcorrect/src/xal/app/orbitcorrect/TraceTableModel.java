/*
 * TraceTableModel.java
 *
 * Created on Wed Jan 07 13:02:36 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.orbitcorrect;

import xal.extension.widgets.plot.*;

import javax.swing.table.*;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;


/**
 * TraceTableModel
 *
 * @author  tap
 */
public class TraceTableModel extends AbstractTableModel implements ChartModelListener {
    
    private static final long serialVersionUID = 1L;
    
	// table column enumerators
	protected static final int NAME_COLUMN = 0;
	protected static final int DISPLAY_COLUMN = 1;
	
	/** chart model */
	protected ChartModel _chartModel;
	
	/** the trace sources */
	protected List<TraceSource> _traceSources;
	
	
	/**
	 * Primary constructor
	 * @param chartModel the chart model whose trace sources will be displayed in the table
	 */
	public TraceTableModel(ChartModel chartModel) {
		_traceSources = new ArrayList<TraceSource>();
		setChartModel(chartModel);
	}
	
	
	/**
	 * Set the chart model whose trace sources are displayed through this table model.
	 * @param chartModel the chart model providing the trace sources.
	 */
	public void setChartModel(final ChartModel chartModel) {
		if ( _chartModel != null ) {
			_chartModel.removeChartModelListener(this);
		}
		_chartModel = chartModel;
		if ( _chartModel != null ) {
			_chartModel.addChartModelListener(this);
		}
		updateTraceSources();
	}
	
	
	/**
	 * Update the trace sources to those in the chart model.
	 */
	public void updateTraceSources() {
		List<TraceSource> traceSources = (_chartModel != null) ? _chartModel.getTraceSources() : Collections.<TraceSource>emptyList();
		synchronized(_traceSources) {
			_traceSources.clear();
			_traceSources.addAll(traceSources);
		}
		fireTableDataChanged();
	}
	
	
	/**
	 * Get the number of columns to display.
	 * @return the number of columns to display
	 */
	public int getColumnCount() {
		return 2;
	}
	
	
	/**
	 * Get the number of rows of data in the table.
	 * @return the number of trace sources
	 */
	public int getRowCount() {
		synchronized(_traceSources) {
			return _traceSources.size();
		}
	}
	
	
	/**
	 * Get the name of the specified column.
	 * @param column the index of the column
	 * @return the name of the specified column
	 */
	public String getColumnName(int column) {
		switch(column) {
			case NAME_COLUMN:
				return "Trace";
			case DISPLAY_COLUMN:
				return "Display";
			default:
				return null;
		}
	}
	
	
	/**
	 * Get the class for the data in the specified column.
	 * @param column the index of the column
	 * @return the class of the data in the column
	 */
	public Class<?> getColumnClass(int column) {
		switch(column) {
			case DISPLAY_COLUMN:
				return Boolean.class;
			default:
				return String.class;
		}
	}
	
	
	/**
	 * Get the value of the data in the cell at the specified row and column.
	 * @param row the row index of the cell
	 * @param column the column index of the cell
	 * @return the data in the cell
	 */
	public Object getValueAt(int row, int column) {
		final TraceSource traceSource;
		synchronized(_traceSources) {
			traceSource = _traceSources.get(row);
		}
		
		switch(column) {
			case NAME_COLUMN:
				return traceSource.getLabel();
			case DISPLAY_COLUMN:
				return new Boolean( traceSource.isEnabled() );
			default:
				return null;
		}
	}
	
	
	/**
	 * Determine if the cell at the specified row and column is editable.
	 * @param row the cell's row index
	 * @param column the cell's column index
	 * @return true if the cell is editable and false if not
	 */
	public boolean isCellEditable(int row, int column) {
		switch(column) {
			case DISPLAY_COLUMN:
				return true;
			default:
				return false;
		}
	}
	
	
	/**
	 * Set the value to display in the cell at the specified row and column.
	 * @param value the new value for the cell
	 * @param row the cell's row index
	 * @param column the cell's column index
	 */
	public void setValueAt(Object value, int row, int column) {
		final TraceSource traceSource;
		synchronized(_traceSources) {
			traceSource = _traceSources.get(row);
		}
		
		switch(column) {
			case DISPLAY_COLUMN:
				traceSource.setEnabled( ((Boolean)value).booleanValue() );
				break;
			default:
				break;
		}
		
		fireTableCellUpdated(row, column);
	}
	
	
	/**
	 * An event indicating that the chart model's trace sources have changed.
	 * @param model the chart model which posted the event
	 * @param traceSources the chart model's new trace sources
	 */
	public void traceSourcesChanged(ChartModel model, List<TraceSource> traceSources) {
		updateTraceSources();
	}
	
	
	/**
	 * An event indicating that the chart's properties have changed.
	 * @param model the chart model which posted the event
	 * @param chart the chart whose properties have changed
	 */
	 public void chartPropertiesChanged(ChartModel model, FunctionGraphsJPanel chart) {
	 }
}

