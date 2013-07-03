//
//  TripsTableModel.java
//  xal
//
//  Created by Thomas Pelaia on 8/7/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.tripviewer;

import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.text.SimpleDateFormat;

import xal.service.tripmonitor.*;


/** table model for displaying trip records */
public class TripsTableModel extends AbstractTableModel {
    
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
	/** time stamp format */
	final static protected SimpleDateFormat TIMESTAMP_FORMAT;
	
	/** timestamp column */
	static final public int TIMESTAMP_COLUMN = 0;
	
	/** PV column */
	static final public int PV_COLUMN = 1;
	
	/** trip records to display */
	protected List<TripRecord> _tripRecords;
	
	
	// static initializer
	static {
		TIMESTAMP_FORMAT = new SimpleDateFormat( "MMM dd, yyyy HH:mm:ss.SSS" );
	}
	
	
	/** constructor */
	public TripsTableModel() {
		_tripRecords = new ArrayList<TripRecord>();
	}
	
	
	/** set the list of trip records */
	public void setTripRecords( final List<TripRecord> tripRecords ) {
		synchronized( this ) {
			_tripRecords = tripRecords;
		}
		fireTableDataChanged();
	}
	
	
	/** get the column count */
	public int getColumnCount() {
		return 2;
	}
	
	
	/** get the row count */
	public int getRowCount() {
		synchronized( this ) {
			return _tripRecords.size();
		}
	}
	
	
	/** get the name for the specified column */
	public String getColumnName( final int column ) {
		switch( column ) {
			case TIMESTAMP_COLUMN:
				return "Time Stamp";
			case PV_COLUMN:
				return "PV";
			default:
				return "";
		}
	}
	
	
	/** get the value for the specified cell */
	public Object getValueAt( final int row, final int column ) {
		TripRecord tripRecord;
		synchronized( this ) {
			tripRecord = _tripRecords.get( row );
		}
		if ( tripRecord == null )  return null;
		
		switch( column ) {
			case TIMESTAMP_COLUMN:
				return TIMESTAMP_FORMAT.format( tripRecord.getDate() );
			case PV_COLUMN:
				return tripRecord.getPV();
			default:
				return null;
		}
	}
}
