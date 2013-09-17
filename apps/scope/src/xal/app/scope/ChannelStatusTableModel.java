/*
 * ChannelStatusTableModel.java
 *
 * Created on Wed Apr 14 12:38:02 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.scope;

import xal.ca.Channel;

import javax.swing.table.*;
import java.util.*;
import java.text.*;


/**
 * ChannelStatusTableModel
 *
 * @author  tap
 */
public class ChannelStatusTableModel extends AbstractTableModel implements ChannelModelListener {
	/** constant required to keep serializable happy */
	static final private long serialVersionUID = 1L;

	// column identifiers
	static final public int LABEL_COLUMN = 0;
	static final private int SIGNAL_COLUMN = 1;
	
	// row identifiers
	static final protected int WAVEFORM_ROW = 0;
	static final protected int DELAY_ROW = 1;
	static final protected int SAMPLE_PERIOD_ROW = 2;
	
	
	/** data source */
	protected ChannelModel _channelModel;

    
    /**
     * Constructor
     */
    public ChannelStatusTableModel(ChannelModel model) {
		setChannelModel(model);
    }
	
	
	/**
	 * Set the channel model to monitor
	 * @param model the new channel model
	 */
	public void setChannelModel(ChannelModel model) {
		if ( _channelModel != null ) {
			_channelModel.removeChannelModelListener(this);
		}
		
		_channelModel = model;
		if ( _channelModel != null ) {
			_channelModel.addChannelModelListener(this);
		}
		fireTableDataChanged();
	}
    
    
    /**
     * Get the number of table rows.
     * @return The number of table rows.
     */
    public int getRowCount() {
        return 3;
    }
    
    
    /**
     * Get the number of table columns.
     * @return The number of table columns.
     */
    public int getColumnCount() {
        return 2;
    }
	
	
	/**
	 * Determine whether the specified cell is editable.
     * @param row The table cell row.
     * @param column The table cell column.
     * @return true if the cell is editable and false if not.
	 */
	public boolean isCellEditable( final int row, final int column ) {
		return column == SIGNAL_COLUMN && !_channelModel.isSettingChannel();
	}
    
    
    /**
     * Get the value to display for the table cell at the specified row and column.
     * @param row The table cell row.
     * @param column The table cell column.
     * @return The value to display for the specified table cell.
     */
    public Object getValueAt( final int row, final int column ) {
		final Channel channel = getChannel( row );
		
        switch( column ) {
			case LABEL_COLUMN:
				return getConnectionText( channel, getRowLabel( row ) );
            case SIGNAL_COLUMN:
                return ( channel != null ) ? channel.channelName() : "";
            default:
                return "";
        }
    }
	
	
	/**
	 * Get the label for the specified row.
	 * @param row the row for which to get the label
	 * @return the row label
	 */
	public String getRowLabel( final int row ) {
		switch( row ) {
			case WAVEFORM_ROW:
				return "Waveform";
			case DELAY_ROW:
				return "Delay";
			case SAMPLE_PERIOD_ROW:
				return "Sampling";
			default:
				return "";
		}
	}
	
	
	/**
	 * Set the value to that specified.
	 * @param value the new value
     * @param row The table cell row.
     * @param column The table cell column.
	 */
	public void setValueAt( final Object value, final int row, final int column ) {
		if ( column == SIGNAL_COLUMN ) {
			final String PV = stripIllegalCharacters( value.toString() );
			switch( row ) {
				case WAVEFORM_ROW:
					new Thread() {
						public void run() {
							_channelModel.setChannel( PV );
						}
					}.start();
					break;
				case DELAY_ROW:
					_channelModel.setDelayChannel( PV );
					break;
				case SAMPLE_PERIOD_ROW:
					_channelModel.setSamplePeriodChannel( PV );
					break;
				default:
					return;
			}
		}
	}
	
	
	/** 
	 * Strip the specified PV string of illegal characters.
	 * @param value the initial string
	 * @return the stripped string
	 */
	static private String stripIllegalCharacters( final String value ) {
		final StringBuffer buffer = new StringBuffer( value.length() );
		
		final int length = value.length();
		for ( int index = 0 ; index < length ; index++ ) {
			final char character = value.charAt( index );
            if ( Character.isLetterOrDigit( character ) || character == ':' || character == '_' || character == '-' || character == '.') {
                buffer.append( character );
            }
			else {
				java.awt.Toolkit.getDefaultToolkit().beep();
			}
		}
		
		return buffer.toString();
	}
	
	
	/**
	 * Get the HTML text to display the connection status.
	 * @param channel channel for which to display the connection status
	 * @return HTML text indicating the connection status
	 */
	static private String getConnectionText( final Channel channel, final String text ) {
		final boolean isConnected = channel != null && channel.isConnected();
		final String prefix = "<html><body> ";
		final String suffix = " </font></body></html>";
		final String font = "<font color=\"" + ((isConnected) ? "#00aa00" : "#ff0000") + "\">";
		return prefix + font + text + suffix;
	}
	
	
	/**
	 * Get the channel to display at the specified row
	 * @param row the table row
	 * @return the channel to display
	 */
	private Channel getChannel(int row) {
		if ( _channelModel == null )  return null;
		
		switch(row) {
			case WAVEFORM_ROW:
				return _channelModel.getChannel();
			case DELAY_ROW:
				return _channelModel.getDelayChannel();
			case SAMPLE_PERIOD_ROW:
				return _channelModel.getSamplePeriodChannel();
			default:
				return null;
		}
	}
    
    
    /** 
     * Get the title of the specified column.
     * @param column The index of the column.
     * @return The title for the specified column.
     */
    public String getColumnName(int column) {
        switch(column) {
			case LABEL_COLUMN:
				return "Field";
            case SIGNAL_COLUMN:
                return "PV";
            default:
                return "";
        }
    }
	
	
	/**
	 * Event indicating that the specified channel is being enabled.
	 * @param source ChannelModel posting the event.
	 * @param channel The channel being enabled.
	 */
	public void enableChannel(ChannelModel source, Channel channel) {}
	
	
	/**
	 * Event indicating that the specified channel is being disabled.
	 * @param source ChannelModel posting the event.
	 * @param channel The channel being disabled.
	 */
	public void disableChannel(ChannelModel source, Channel channel) {}
	
	
	/**
	 * Event indicating that the channel model has a new channel.
	 * @param source ChannelModel posting the event.
	 * @param channel The new channel.
	 */
	public void channelChanged(ChannelModel source, Channel channel) {
		fireTableDataChanged();
	}
	
	
	/**
	 * Event indicating that the channel model has a new array of element times.
	 * @param source ChannelModel posting the event.
	 * @param elementTimes The new element times array measured in turns.
	 */
	public void elementTimesChanged(ChannelModel source, final double[] elementTimes) {}
}

