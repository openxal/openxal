/*
 * MPSEventTableModel.java
 *
 * Created on Wed Apr 14 10:50:50 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.mpsclient;

import xal.tools.data.*;

import javax.swing.table.*;
import java.util.*;
import java.text.*;


/**
 * MPSEventTableModel
 *
 * @author  tap
 */
public class MPSEventTableModel extends AbstractTableModel {
    
    private static final long serialVersionUID = 1L;

    // column identifiers
	final protected int SIGNAL_COLUMN = 0;
	final protected int SIGNAL_TIMESTAMP_COLUMN = 1;
	
	/** data source */
	protected MPSEvent _mpsEvent;

    
    /**
     * Constructor
     */
    public MPSEventTableModel(MPSEvent event) {
		setEvent(event);
    }
	
	
	/**
	 * Set a new MPS event
	 * @param event the new MPS event
	 */
	synchronized public void setEvent(MPSEvent event) {
		_mpsEvent = event;
		fireTableDataChanged();
	}
    
    
    /**
     * Get the number of table rows.
     * @return The number of table rows.
     */
    public int getRowCount() {
        return (_mpsEvent != null) ? _mpsEvent.getSignalEventCount() : 0;
    }
    
    
    /**
     * Get the number of table columns.
     * @return The number of table columns.
     */
    public int getColumnCount() {
        return 2;
    }
    
    
    /**
     * Get the value to display for the table cell at the specified row and column.
     * @param row The table cell row.
     * @param column The table cell column.
     * @return The value to display for the specified table cell.
     */
    public Object getValueAt(int row, int column) {
		if ( _mpsEvent == null )  return "";
		SignalEvent event = _mpsEvent.getSignalEvent(row);
		
        switch(column) {
            case SIGNAL_COLUMN:
                return event.getSignal();
			case SIGNAL_TIMESTAMP_COLUMN:
				return event.getTimestamp();
            default:
                return "";
        }
    }
    
    
    /** 
     * Get the title of the specified column.
     * @param column The index of the column.
     * @return The title for the specified column.
     */
    public String getColumnName(int column) {
        switch(column) {
            case SIGNAL_COLUMN:
                return "Signal";
			case SIGNAL_TIMESTAMP_COLUMN:
				return "Timestamp";
            default:
                return "";
        }
    }
}

