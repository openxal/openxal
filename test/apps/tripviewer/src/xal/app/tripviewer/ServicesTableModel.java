//
//  ServicesTableModel.java
//  xal
//
//  Created by Thomas Pelaia on 8/10/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.tripviewer;

import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.text.SimpleDateFormat;

import xal.service.tripmonitor.*;


/** table model for managing the table of trip services */
public class ServicesTableModel extends AbstractTableModel {
    
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
    
	/** time stamp format */
	final static protected SimpleDateFormat TIMESTAMP_FORMAT;
	
	/** service name */
	static final public int NAME_COLUMN = 0;
	
	/** Host column */
	static final public int HOST_COLUMN = 1;
	
	/** launch time column */
	static final public int LAUNCH_TIME_COLUMN = 2;
	
	/** trip services to display */
	protected List<ServiceHandler> _serviceHandlers;
	
	
	// static initializer
	static {
		TIMESTAMP_FORMAT = new SimpleDateFormat( "MMM dd, yyyy HH:mm:ss.SSS" );
	}
	
	
	/** Constructor */
	public ServicesTableModel() {
		_serviceHandlers = new ArrayList<ServiceHandler>();
	}
	
	
	/** set the services */
	public void setServiceHandlers( final List<ServiceHandler> serviceHandlers ) {
		final ArrayList<ServiceHandler> handlers = new ArrayList<ServiceHandler>();
		handlers.addAll( serviceHandlers );
		
		synchronized( this ) {
			_serviceHandlers = handlers;
		}
		
		fireTableDataChanged();
	}
	
	
	/** get the column count */
	public int getColumnCount() {
		return 3;
	}
	
	
	/** get the row count */
	public int getRowCount() {
		int rowCount;
		
		synchronized( this ) {
			rowCount = _serviceHandlers.size();
		}
		
		return rowCount;
	}
	
	
	/** get the name for the specified column */
	public String getColumnName( final int column ) {
		switch( column ) {
			case NAME_COLUMN:
				return "Name";
			case HOST_COLUMN:
				return "Host";
			case LAUNCH_TIME_COLUMN:
				return "Launch Time";
			default:
				return "";
		}
	}
	
	
	/** get the value for the specified cell */
	public Object getValueAt( final int row, final int column ) {
		ServiceHandler serviceHandler;
		
		synchronized ( _serviceHandlers ) {
			if ( row < _serviceHandlers.size() ) {
				serviceHandler = _serviceHandlers.get( row );
			}
			else {
				return "";
			}
		}
		
		if ( serviceHandler == null )  return null;
		
		switch( column ) {
			case NAME_COLUMN:
				return serviceHandler.getID();
			case HOST_COLUMN:
				return serviceHandler.getHostName();
			case LAUNCH_TIME_COLUMN:
				return TIMESTAMP_FORMAT.format( serviceHandler.getLaunchTime() );
			default:
				return null;
		}
	}	
}
