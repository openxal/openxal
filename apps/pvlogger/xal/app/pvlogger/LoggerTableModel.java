/*
 * LoggerTableModel.java
 *
 * Created on Fri Oct 10 16:41:01 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.pvlogger;

import xal.tools.data.*;

import javax.swing.table.*;
import java.util.*;
import java.text.*;


/**
 * LoggerTableModel is the table model for displaying the status of loggers on the network.
 *
 * @author  tap
 */
class LoggerTableModel extends AbstractTableModel implements LoggerModelListener, DataKeys {
	/** required UID for serialization */
	static final long serialVersionUID = 1L;

    // constants
    final protected DecimalFormat PERIOD_FORMAT = new DecimalFormat("#,##0.000");
	final protected SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("MMM d, yyyy HH:mm:ss zz");
    
    // column identifiers
    final protected int HOST_COLUMN = 0;
	final protected int LAUNCH_COLUMN = 1;
	final protected int LAST_CHECK_COLUMN = 2;
	final protected int SERVICE_OKAY_COLUMN = 3;

	/** main model */
	private final LoggerModel MODEL;

	/** data source */
	protected List<GenericRecord> _records;

    
    /**
     * Constructor of the application table model.
     */
    public LoggerTableModel( final LoggerModel aModel ) {
		MODEL = aModel;
		_records = new ArrayList<GenericRecord>();
		MODEL.addLoggerModelListener(this);
    }
    
    
    /**
     * Get the number of table rows.
     * @return The number of table rows.
     */
    public int getRowCount() {
        return _records.size();
    }
    
    
    /**
     * Get the number of table columns.
     * @return The number of table columns.
     */
    public int getColumnCount() {
        return 4;
    }
    
    
    /**
     * Get the value to display for the table cell at the specified row and column.
     * @param row The table cell row.
     * @param column The table cell column.
     * @return The value to display for the specified table cell.
     */
    public Object getValueAt(int row, int column) {
		GenericRecord record = _records.get(row);
		
        switch(column) {
            case HOST_COLUMN:
                return record.valueForKey(HOST_KEY);
			case LAUNCH_COLUMN:
				Object timestamp = record.valueForKey(LAUNCH_TIME_KEY);
				return (timestamp != null) ? TIME_FORMAT.format(timestamp) : "?";
			case LAST_CHECK_COLUMN:
				Object checkTime = record.valueForKey(LAST_CHECK_KEY);
				return (checkTime != null) ? TIME_FORMAT.format(checkTime) : "?";
			case SERVICE_OKAY_COLUMN:
				return record.booleanValueForKey(SERVICE_OKAY_KEY) ? "<html><body>Okay</body></html>" : "<html><body><font color =\"#ff0000\">Failed</font></body></html>";
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
            case HOST_COLUMN:
				return "Host";
			case LAUNCH_COLUMN:
				return "Launch Time";
			case LAST_CHECK_COLUMN:
				return "Last Check";
			case SERVICE_OKAY_COLUMN:
				return "Service Status";
            default:
                return "";
        }
    }
	
	
	/**
	 * Get the record displayed at the specified row
	 * @param row The row where the record is displayed
	 * @return the record displayed at the specified row
	 */
	public GenericRecord getRecord(int row) {
		return _records.get(row);
	}
	
	
	/**
	 * The status of a logger has been updated along with its client side record.
	 * @param source The source of the event
	 * @param record The record that has been updated.
	 */
	public void newLoggerStatus(LoggerModel source, GenericRecord record) {
		int row = _records.indexOf(record);
		if ( row >= 0 ) {
			fireTableRowsUpdated(row, row);
		}
	}
	
	
	/**
	 * The list of loggers has changed.
	 * @param source The source of the event
	 * @param newRecords The new logger records.
	 */
	public void loggersChanged(LoggerModel source, List<GenericRecord> newRecords) {
		_records = newRecords;
		fireTableDataChanged();
	}
	
	
	/** Publish snapshots on the PV Loggers corresponding to the specified rows */
	public void publishSnapshots( final int[] rows ) {
		for ( final int row : rows ) {
			final GenericRecord record = _records.get( row );
			MODEL.publishSnapshots( record );
		}
	}
	
	
	/**
	 * Identify the records corresponding to the rows selected in the table. Tell the MODEL to 
	 * restart the loggers corresponding to these records.
	 * @param rowSelections The table rows from the selected loggers.
	 */
	public void restartSelections(int[] rowSelections) {
		for ( int index = 0 ; index < rowSelections.length ; index++ ) {
			int row = rowSelections[index];
			final GenericRecord record = _records.get(row);
			MODEL.restartLogger(record);
		}
	}
	
	
	/**
	 * Identify the records corresponding to the rows selected in the table. Tell the MODEL to 
	 * shutdown the loggers corresponding to these records.
	 * @param rowSelections The table rows from the selected loggers.
	 */
	public void shutdownSelections(int[] rowSelections) {
		for ( int index = 0 ; index < rowSelections.length ; index++ ) {
			int row = rowSelections[index];
			final GenericRecord record = _records.get(row);
			MODEL.shutdownLogger(record);
		}
	}
	
	
	/**
	 * Identify the records corresponding to the rows selected in the table. Tell the model to 
	 * start the corresponding loggers logging.
	 * @param rowSelections The table rows from the selected loggers.
	 */
	public void resumeLoggingSelections(int[] rowSelections) {
		for ( int index = 0 ; index < rowSelections.length ; index++ ) {
			int row = rowSelections[index];
			final GenericRecord record = _records.get(row);
			MODEL.resumeLogging(record);
		}
	}
	
	
	/**
	 * Identify the records corresponding to the rows selected in the table. Tell the model to 
	 * stop the corresponding loggers from logging.
	 * @param rowSelections The table rows from the selected loggers.
	 */
	public void stopLoggingSelections(int[] rowSelections) {
		for ( int index = 0 ; index < rowSelections.length ; index++ ) {
			int row = rowSelections[index];
			final GenericRecord record = _records.get(row);
			MODEL.stopLogging(record);
		}
	}
}
