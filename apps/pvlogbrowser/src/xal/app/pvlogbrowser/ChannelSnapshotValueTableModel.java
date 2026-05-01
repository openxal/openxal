//
//  ChannelValuesTableModel.java
//  xal
//
//  Created by Tom Pelaia on 11/6/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

package xal.app.pvlogbrowser;



import xal.service.pvlogger.ChannelSnapshot;
import xal.tools.text.FormattedNumber;

import java.util.*;
import javax.swing.table.*;


/** table model to display a channel's snapshot value */
public class ChannelSnapshotValueTableModel extends AbstractTableModel {
	final static protected int INDEX_COLUMN = 0;
	final static protected int VALUE_COLUMN = 1;
	
	/** empty array value */
	final static private double[] EMPTY_ARRAY_VALUE;
	
	/** currently selected channel snapshot */
	protected ChannelSnapshot _channelSnapshot;
	
	/** array value of the channel snapshot */
	protected double[] _arrayValue;
	
	
	// static initializer
	static {
		EMPTY_ARRAY_VALUE = new double[0];
	}
	
	
	/** Primary constructor */
	public ChannelSnapshotValueTableModel() {
		_arrayValue = EMPTY_ARRAY_VALUE;
	}
	
	
	/**
	 * Set the channel snapshot whose filtered channel snapshots are to be displayed
	 * @param snapshot the machine snapshot
	 */
	public void setChannelSnapshot( final ChannelSnapshot snapshot ) {
		_channelSnapshot = snapshot;
		_arrayValue = snapshot != null ? snapshot.getValue() : EMPTY_ARRAY_VALUE;
		fireTableDataChanged();
	}
	
	
	/**
	 * Get the number of table rows to display
	 * @return the number of rows
	 */
	public int getRowCount() {
		return _arrayValue.length;
	}
	
	
	/**
	 * Get the number of table columns to display
	 * @return the number of columns
	 */
	public int getColumnCount() {
		return 2;
	}
	
	
	/**
	 * Get the value of the item to display in the specified table cell
	 * @param row the index of the cell's row
	 * @param column the index of the cell's column
	 * @return the value to display in the cell
	 */
	public Object getValueAt( final int row, final int column ) {
		final double[] arrayValue = _arrayValue;
		
		switch( column ) {
			case INDEX_COLUMN:
				return row;
			case VALUE_COLUMN:
				return arrayValue != null ? ( arrayValue.length > row ? new FormattedNumber( arrayValue[row] ) : null ) : null;
			default:
				return null;
		}
	}
	
	
	/**
	 * Get the name to display in the column's header
	 * @param column the index of the column
	 * @return the name to display in the column's header
	 */
	public String getColumnName( final int column ) {
		switch( column ) {
			case INDEX_COLUMN:
				return "Index";
			case VALUE_COLUMN:
				return "Value";
			default:
				return "";
		}
	}
	
	
	/** get the class associated with the specified column */
	public Class getColumnClass( final int column ) {
		switch ( column ) {
			case INDEX_COLUMN: case VALUE_COLUMN:
				return Number.class;
			default:
				return super.getColumnClass( column );
		}
	}	
}
