/*
 *  LoggerWindow.java
 *
 *  Created on Tue Sep 14 12:51:14 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.application;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.Component;
import java.util.*;
import java.util.logging.*;


/**
 * LoggerWindow
 *
 * @author   tap
 * @since    Sep 14, 2004
 */
class LoggerWindow extends JFrame {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
	/** default logger window */
	protected static LoggerWindow _defaultWindow;

	/** logger handler */
	protected LoggerBuffer _loggerHandler;

	/** indicates whether this window has ever been shown */
	protected boolean _neverShown;

	/** logger table model */
	protected LogTableModel _logTableModel;

	/** the selected log record */
	protected LogRecord _selectedRecord;

	/** text view for displaying the selected log record's message */
	protected JTextArea _selectedRecordMessageView;

	/** text view for displaying the selected log record's exception if any */
	protected JTextArea _selectedRecordExceptionView;


	/**
	 * Primary constructor
	 *
	 * @param handler  the handler for which to display the logged events
	 * @param title    the window's title
	 */
	public LoggerWindow( final String title, final LoggerBuffer handler ) {
		super( title );

		_logTableModel = new LogTableModel();

		setLoggerHandler( handler );
		_neverShown = true;

		makeView();
	}


	/** Constructor */
	public LoggerWindow() {
		this( "Event Log", LoggerBuffer.getRootHandler() );
	}


	/** Make the main view. */
	protected void makeView() {
		setSize( 500, 400 );
		
		setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE );
		
		Box mainView = new Box( BoxLayout.Y_AXIS );
		getContentPane().add( mainView );
		
		JSplitPane mainSplit = new JSplitPane( JSplitPane.VERTICAL_SPLIT, true, makeTable(), makeRecordInspector() );
		mainSplit.setResizeWeight( 0.5 );

		mainView.add( makeTopButtonBar() );
		mainView.add( mainSplit );
	}


	/**
	 * Make a button bar at the top of the window.
	 *
	 * @return   the button bar
	 */
	protected Component makeTopButtonBar() {
		Box bar = new Box( BoxLayout.X_AXIS );
		bar.setBorder( BorderFactory.createEtchedBorder() );
		
		bar.add( Box.createHorizontalGlue() );

		JButton clearButton = new JButton( "Clear" );
		bar.add( clearButton );
		clearButton.addActionListener(
			new ActionListener() {
				public void actionPerformed( final ActionEvent event ) {
					_loggerHandler.clear();
				}
			} );

		return bar;
	}


	/**
	 * Make the table view.
	 *
	 * @return   the table view
	 */
	protected Component makeTable() {
		Box view = new Box( BoxLayout.Y_AXIS );
		view.add( Box.createHorizontalStrut(10000) );		// force the table to fill horizontally
		final JTable table = new JTable( _logTableModel );
		JScrollPane scrollPane = new JScrollPane( table );
		view.add( scrollPane );

		table.getSelectionModel().addListSelectionListener(
			new ListSelectionListener() {
				public void valueChanged( ListSelectionEvent event ) {
					int selectedRow = table.getSelectedRow();
					if ( selectedRow >= 0 ) {
						LogTableModel tableModel = (LogTableModel)table.getModel();
						setSelectedRecord( tableModel.getRecord( selectedRow ) );
					}
					else {
						setSelectedRecord( null );
					}
				}
			} );

		return view;
	}


	/**
	 * Make an inspector to display the log record's message and associated exception if any.
	 *
	 * @return   a log record inspector
	 */
	protected Component makeRecordInspector() {
		_selectedRecordMessageView = new JTextArea();
		_selectedRecordExceptionView = new JTextArea();

		JTabbedPane tabbedView = new JTabbedPane();
		tabbedView.addTab( "Message", new JScrollPane( _selectedRecordMessageView ) );
		tabbedView.addTab( "Exception", new JScrollPane( _selectedRecordExceptionView ) );

		return tabbedView;
	}


	/**
	 * Get the default logger window.
	 *
	 * @return   The default logger window
	 */
	public static LoggerWindow getDefault() {
		if ( _defaultWindow == null ) {
			_defaultWindow = new LoggerWindow();
		}

		return _defaultWindow;
	}


	/**
	 * Show the logger window. If the window has never been shown before, place it relative to the
	 * sender, otherwise simply show it where it was last placed by the user.
	 *
	 * @param sender  The component relative to which the logger should be positioned
	 */
	public void showFirstTimeNear( final Component sender ) {
		if ( _neverShown ) {
			setLocationRelativeTo( sender );
			_neverShown = false;
		}
		setVisible( true );
	}


	/**
	 * Set the logger handler to the one specified.
	 *
	 * @param handler  The new logger handler
	 */
	public void setLoggerHandler( final LoggerBuffer handler ) {
		if ( _loggerHandler != null ) {
			_loggerHandler.removeLoggerBufferListener( _logTableModel );
		}

		_loggerHandler = handler;

		if ( handler != null ) {
			handler.addLoggerBufferListener( _logTableModel );
		}
	}


	/**
	 * Set the selected record to the value specified.
	 *
	 * @param record  the new selected record
	 */
	public void setSelectedRecord( final LogRecord record ) {
		if ( record != _selectedRecord ) {
			_selectedRecord = record;

			if ( record != null ) {
				_selectedRecordMessageView.setText( record.getMessage() );
				Throwable exception = record.getThrown();
				String exceptionText = exception != null ? exception.toString() : "";
				_selectedRecordExceptionView.setText( exceptionText );
			}
			else {
				_selectedRecordMessageView.setText( "" );
				_selectedRecordExceptionView.setText( "" );
			}
		}
	}
}


/** LogTableModel is a table model for displaying the log records in a table. */
class LogTableModel extends AbstractTableModel implements LoggerBufferListener {
    /** serialization ID */
    private static final long serialVersionUID = 1L;

	final static int LEVEL_COLUMN = 0;
	final static int TIMESTAMP_COLUMN = 1;
	final static int CLASS_COLUMN = 2;
	final static int METHOD_COLUMN = 3;
	final static int MESSAGE_COLUMN = 4;
	final static int EXCEPTION_COLUMN = 5;

	/** log records */
	protected List<LogRecord> _records;

	/** Map of level colors keyed by level */
	protected static Map<Level,String> _levelColors;

	/** static initializer */
	static {
		populateLevelColors();
	}


	/** Constructor */
	public LogTableModel() {
		_records = new ArrayList<LogRecord>();
	}


	/** Populate the map of HTML colors corresponding to each log level. */
	protected static void populateLevelColors() {
		_levelColors = new HashMap<Level,String>();

		_levelColors.put( Level.CONFIG, "purple" );
		_levelColors.put( Level.FINE, "blue" );
		_levelColors.put( Level.FINER, "aqua" );
		_levelColors.put( Level.FINEST, "lime" );
		_levelColors.put( Level.INFO, "black" );
		_levelColors.put( Level.WARNING, "ff8800" );
		_levelColors.put( Level.SEVERE, "red" );
		_levelColors.put( null, "black" );
	}


	/**
	 * Get the table row count
	 *
	 * @return   the table row count
	 */
	public int getRowCount() {
		synchronized ( _records ) {
			return _records.size();
		}
	}


	/**
	 * Get the table column count
	 *
	 * @return   the table column count
	 */
	public int getColumnCount() {
		return 6;
	}


	/**
	 * Get the name for the specified column
	 *
	 * @param column  the column for which to get the name
	 * @return        the name for the column
	 */
	public String getColumnName( final int column ) {
		switch ( column ) {
			case LEVEL_COLUMN:
				return "Level";
			case TIMESTAMP_COLUMN:
				return "Timestamp";
			case CLASS_COLUMN:
				return "Source Class";
			case METHOD_COLUMN:
				return "Source Method";
			case MESSAGE_COLUMN:
				return "Message";
			case EXCEPTION_COLUMN:
				return "Exception";
			default:
				return "";
		}
	}


	/**
	 * Get the value to display in the cell at the specified row and column.
	 *
	 * @param row     the cell's row
	 * @param column  the cell's column
	 * @return        the value to display in the cell
	 */
	public Object getValueAt( final int row, final int column ) {
		final LogRecord record = getRecord( row );
		if ( record == null ) {
			return null;
		}

		final Level level = record.getLevel();
		final String color = getColor( level );
		Object value;

		switch ( column ) {
			case LEVEL_COLUMN:
				value = level;
				break;
			case TIMESTAMP_COLUMN:
				value = new Date( record.getMillis() );
				break;
			case CLASS_COLUMN:
				value = record.getSourceClassName();
				break;
			case METHOD_COLUMN:
				value = record.getSourceMethodName();
				break;
			case MESSAGE_COLUMN:
				value = record.getMessage();
				break;
			case EXCEPTION_COLUMN:
				value = record.getThrown();
				break;
			default:
				value = "";
				break;
		}

		value = value != null ? value : "";
		return getCellCode( color, value );
	}


	/**
	 * Get the HTML code for the table cell which sets the font color of the text describing the
	 * value for the cell.
	 *
	 * @param color  HTML color
	 * @param value  value of the cell
	 * @return       The HTML describing the cell's value with the proper font color
	 */
	protected static String getCellCode( final String color, final Object value ) {
		return "<html><body><font color=" + color + ">" + value + "</font></body></html>";
	}


	/**
	 * Get the HTML color for the specified log level.
	 *
	 * @param level  the level for which to get the color
	 * @return       The HTML color to use for the specified level
	 */
	protected static String getColor( final Level level ) {
		return _levelColors.get( level );
	}


	/**
	 * Get the log record at the specified index.
	 *
	 * @param index  the index of the record to get
	 * @return       the log record at the specified index
	 */
	public LogRecord getRecord( final int index ) {
		synchronized ( _records ) {
			try {
				return _records.get( index );
			}
			catch ( ArrayIndexOutOfBoundsException exception ) {
				return null;
			}
		}
	}


	/**
	 * Event indicating that the records in the logger buffer have changed.
	 *
	 * @param buffer   the buffer whose records have changed
	 * @param records  the new records in the buffer
	 */
	public void recordsChanged( LoggerBuffer buffer, List<LogRecord> records ) {
		synchronized ( _records ) {
			_records.clear();
			_records.addAll( records );
			fireTableDataChanged();
		}
	}
}

