//
//  ChannelTableModel.java
//  xal
//
//  Created by Tom Pelaia on 12/9/08.
//  Copyright 2008 Oak Ridge National Lab. All rights reserved.
//

package xal.app.xyzcorrelator;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;


/** table model for displaying and editing the channels to correlate */
public class ChannelTableModel extends AbstractTableModel {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
	/** column indicating the monitor enable status of the channel */
	protected static final int ENABLE_COLUMN = 0;
	
	/** column indicating the plotting status of the channel */
	protected static final int PLOTTING_COLUMN = 1;
	
	/** column for the channel ID */
	protected static final int CHANNEL_COLUMN = 2;
	
	/** column for the channel source name */
	protected static final int SOURCE_COLUMN = 3;
	
	/** main model */
	final private CorrelationModel MODEL;
		
	
	/** Primary Constructor */
	public ChannelTableModel( final CorrelationModel model )  {
		MODEL = model;
	}
	
	
	/** get the number of columns */
	public int getColumnCount() {
		return 4;
	}
	
	
	/** get the column titles */
	public String getColumnName( final int column ) {
		switch ( column ) {
			case ENABLE_COLUMN:
				return "Monitor";
			case PLOTTING_COLUMN:
				return "Plot";
			case SOURCE_COLUMN:
				return "Source";
			case CHANNEL_COLUMN:
				return "Channel";
			default:
				return "";
		}
	}
	
	
	/** get the number of rows */
	public int getRowCount() {
		return MODEL.getChannelPlaceholderCount();
	}
	
	
	/** get the column class */
	public Class<?> getColumnClass( final int column ) {
		switch ( column ) {
			case SOURCE_COLUMN: case CHANNEL_COLUMN:
				return String.class;
			case ENABLE_COLUMN: case PLOTTING_COLUMN:
				return Boolean.class;
			default:
				return super.getColumnClass( column );
		}
	}
	
	
	/** determine whether the specified cell is editable */
	public boolean isCellEditable( final int row, final int column ) {
		return column != SOURCE_COLUMN && MODEL.getChannel( row ) != null;
	}
	
	
	/** get the value for the specified cell */
	public Object getValueAt( final int row, final int column ) {
		switch ( column ) {
			case SOURCE_COLUMN:
				return MODEL.getChannelSourceID( row );
			case CHANNEL_COLUMN:
				return MODEL.getChannelID( row );
			case ENABLE_COLUMN:
				return MODEL.isChannelEnabled( row );
			case PLOTTING_COLUMN:
				return MODEL.isChannelPlotting( row );
			default:
				return null;
		}
	}
	
	
	/** set the value for the specified cell */
	public void setValueAt( final Object value, final int row, final int column ) {
		switch( column ) {
			case CHANNEL_COLUMN:
				MODEL.setChannelPV( row, value.toString().trim() );
				break;
			case ENABLE_COLUMN:
				MODEL.setChannelEnable( row, (Boolean)value );
				break;
			case PLOTTING_COLUMN:
				MODEL.setChannelPlotting( row, (Boolean)value );
				break;
			default:
				break;
		}
	}
}



