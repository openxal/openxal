/*
 * AppTableModel.java
 *
 * Created on Fri Oct 10 16:41:01 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.appviewer;

import xal.tools.data.*;

import javax.swing.table.*;
import javax.swing.SwingUtilities;
import java.util.*;
import java.text.*;


/**
 * AppTableModel is the table model for displaying the status of applications on the network.
 *
 * @author  tap
 */
class AppTableModel extends AbstractTableModel implements AppViewerListener, DataKeys {
    
    private static final long serialVersionUID = 1L;

    // constants
    final protected DecimalFormat memoryFormat = new DecimalFormat("#,##0.000");
	final protected SimpleDateFormat timeFormat = new SimpleDateFormat("MMM d, yyyy HH:mm:ss zz");
    
    // column identifiers
    final protected int NAME_COLUMN = 0;
    final protected int HOST_COLUMN = 1;
	final protected int LAUNCH_COLUMN = 2;
    final protected int TOTAL_MEMORY_COLUMN = 3;
    final protected int FREE_MEMORY_COLUMN = 4;
	final protected int SERVICE_OKAY_COLUMN = 5;
	
	/** data source */
	protected List<GenericRecord> records;
	
	/** main model */
	protected AppViewerModel model;
	
    
    /**
     * Constructor of the application table model.
     */
    public AppTableModel( final AppViewerModel aModel ) {
		model = aModel;
		records = new ArrayList<GenericRecord>();
		model.addAppViewerListener( this );
    }
    
    
    /**
     * Get the number of table rows.
     * @return The number of table rows.
     */
    public int getRowCount() {
        return records.size();
    }
    
    
    /**
     * Get the number of table columns.
     * @return The number of table columns.
     */
    public int getColumnCount() {
        return 6;
    }
    
    
    /**
     * Get the value to display for the table cell at the specified row and column.
     * @param row The table cell row.
     * @param column The table cell column.
     * @return The value to display for the specified table cell.
     */
    public Object getValueAt( final int row, final int column ) {
		GenericRecord record = records.get(row);
		
        switch(column) {
            case NAME_COLUMN:
                return record.valueForKey(APPLICATION_KEY);
            case HOST_COLUMN:
                return record.valueForKey(HOST_KEY);
			case LAUNCH_COLUMN:
				Object timestamp = record.valueForKey(LAUNCH_TIME_KEY);
				return (timestamp != null) ? timeFormat.format(timestamp) : "?";
            case TOTAL_MEMORY_COLUMN:
				Object totalMemory = record.valueForKey(TOTAL_MEMORY_KEY);
                return (totalMemory != null) ? memoryFormat.format(totalMemory) : "?";
            case FREE_MEMORY_COLUMN:
				Object freeMemory = record.valueForKey(FREE_MEMORY_KEY);
                return (freeMemory != null) ? memoryFormat.format(freeMemory) : "?";
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
    public String getColumnName( final int column ) {
        switch(column) {
            case NAME_COLUMN:
                return "Application";
            case HOST_COLUMN:
				return "Host";
			case LAUNCH_COLUMN:
				return "Launch Time";
            case TOTAL_MEMORY_COLUMN:
				return "Total Memory (kB)";
            case FREE_MEMORY_COLUMN:
				return "Free Memory (kB)";
			case SERVICE_OKAY_COLUMN:
				return "Service Status";
            default:
                return "";
        }
    }
	
	
	/**
	 * The list of applications has changed.
	 * @param source the model posting the event
	 * @param records The records of every application found on the local network.
	 */
	public void applicationsChanged( final AppViewerModel source, final java.util.List<GenericRecord> newRecords ) {
		try {
			SwingUtilities.invokeLater( new Runnable() {
				public void run() {
					records = newRecords;
					fireTableDataChanged();					
				}
			});
		}
		catch ( Exception exception ) {
			throw new RuntimeException( exception );
		}
	}
	
	
	/**
	 * An application's record has been updated
	 * @param source the model posting the event
	 * @param record the updated record
	 */
	public void applicationUpdated( final AppViewerModel source, final GenericRecord record ) {
		try {
			SwingUtilities.invokeLater( new Runnable() {
				public void run() {
					int row = records.indexOf( record );
					if ( row >= 0 ) {
						fireTableRowsUpdated( row, row );
					}
				}
			});
		}
		catch ( Exception exception ) {
			throw new RuntimeException( exception );
		}
	}
	
	
	/**
	 * Notification that a remote message exception has occurred
	 * @param source the model posting the event
	 * @param handler the handler for which the remote exception occurred
	 * @param exception the remote exception that occurred
	 */
	public void remoteException(AppViewerModel source, AppHandler handler, Exception exception) {}
	
	
	/**
	 * Identify the records corresponding to the rows selected in the table. Tell the model to 
	 * run the garbage collector on the applications corresponding to these records.
	 */
	public void collectGarbageOnSelections( final int[] rowSelections ) {
		for ( int index = 0 ; index < rowSelections.length ; index++ ) {
			int row = rowSelections[index];
			GenericRecord record = (GenericRecord)records.get( row );
			model.collectGarbageOnApplication( record );
		}
	}
	
	
	/**
	 * Identify the records corresponding to the rows selected in the table. Tell the model to 
	 * quit the applications corresponding to these records.
	 */
	public void quitSelections( final int[] rowSelections ) {
		for ( int index = 0 ; index < rowSelections.length ; index++ ) {
			int row = rowSelections[index];
			GenericRecord record = (GenericRecord)records.get( row );
			model.quitApplication( record );
		}
	}
	
	
	/**
	 * Identify the records corresponding to the rows selected in the table. Tell the model to force
	 * quit the applications corresponding to these records.
	 */
	public void forceQuitSelections( final int[] rowSelections ) {
		for ( int index = 0 ; index < rowSelections.length ; index++ ) {
			int row = rowSelections[index];
			GenericRecord record = (GenericRecord)records.get( row );
			model.forceQuitApplication( record );
		}
	}
	
	
	/** Reveal the applications corresponding to the selections  */
	public void revealSelectedApplications( final int[] rowSelections ) {
		for ( int index = 0 ; index < rowSelections.length ; index++ ) {
			int row = rowSelections[index];
			GenericRecord record = (GenericRecord)records.get( row );
			model.revealApplication( record );
		}
	}
}
