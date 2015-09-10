/**
 * TableFactory
 *
 * @version   2.0
 * @author    J. Galambos
 * @author    T. Pelaia
 */

package xal.app.score;

import xal.ca.*;
import xal.tools.data.*;
import xal.extension.widgets.swing.*;
import xal.tools.text.FormattedNumber;
import xal.tools.data.GenericRecord;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.text.*;
import javax.swing.*;
import javax.swing.SwingConstants;
import javax.swing.table.*;


/** Constructs and configures the Score Table and its model */
public class TableFactory {
	/** The table model used here */
	final private KeyValueFilteredTableModel<ScoreRecord> TABLE_MODEL;
	
	/** the Table */
	private JTable theTable;
	
	
	/** Constructor */
	public TableFactory( final ScoreDocument document ) {
		TABLE_MODEL = new KeyValueFilteredTableModel<ScoreRecord>();
		TABLE_MODEL.setKeyPaths( "system", "signalType", "setpointPV", "savedSetpointAsString", "liveSetPointAsString", "readbackPV", "savedReadbackAsString", "liveReadbackAsString", "formattedSetpointRelativeError", "formattedReadbackRelativeError" );
		TABLE_MODEL.setMatchingKeyPaths( "system", "signalType", "setpointPV", "readbackPV", "formattedSetpointRelativeError", "formattedReadbackRelativeError" );
		
		TABLE_MODEL.setColumnClass( "formattedSetpointRelativeError", FormattedNumber.class );
		TABLE_MODEL.setColumnClass( "formattedReadbackRelativeError", FormattedNumber.class );
		
		TABLE_MODEL.setColumnName( "signalType", "Signal Type" );
		TABLE_MODEL.setColumnName( "setpointPV", "Setpoint PV" );
		TABLE_MODEL.setColumnName( "savedSetpointAsString", "Saved Setpoint" );
		TABLE_MODEL.setColumnName( "liveSetPointAsString", "Live Setpoint" );
		TABLE_MODEL.setColumnName( "readbackPV", "Readback PV" );
		TABLE_MODEL.setColumnName( "savedReadbackAsString", "Saved Readback" );
		TABLE_MODEL.setColumnName( "liveReadbackAsString", "Live Readback" );
		TABLE_MODEL.setColumnName( "formattedSetpointRelativeError", "Setpoint Error(%)" );
		TABLE_MODEL.setColumnName( "formattedReadbackRelativeError", "Readback Error(%)" );
		
		theTable = new JTable( TABLE_MODEL );
		theTable.setAutoCreateRowSorter( true );
		
		theTable.setShowGrid( true );
		theTable.setGridColor( new Color( 0.9f, 0.9f, 1.0f ) );	// pale blue grid
		
		final TableColumnModel columnModel = theTable.getColumnModel();
		final TableCellRenderer valueRenderer = new ScoreValueTableCellRenderer( document );
		columnModel.getColumn( ScoreColumns.SYSTEM_COLUMN ).setCellRenderer( valueRenderer );
		columnModel.getColumn( ScoreColumns.TYPE_COLUMN ).setCellRenderer( valueRenderer );
		columnModel.getColumn( ScoreColumns.SETPOINT_SAVED_VALUE_COLUMN ).setCellRenderer( valueRenderer );
		columnModel.getColumn( ScoreColumns.SETPOINT_LIVE_VALUE_COLUMN ).setCellRenderer( valueRenderer );
		columnModel.getColumn( ScoreColumns.READBACK_SAVED_VALUE_COLUMN ).setCellRenderer( valueRenderer );
		columnModel.getColumn( ScoreColumns.READBACK_LIVE_VALUE_COLUMN ).setCellRenderer( valueRenderer );
	}
	
	
	/** update the table for the systems and types */
	public void updateTable( final DataTable dataTable, final List<String> systems, final List<String> types ) {
		// an order to get data ordered by a) sp-name , then b) rb-name:
		final String [] keyOrder = { PVData.spNameKey, PVData.rbNameKey };
		final SortOrdering ordering = new SortOrdering( keyOrder );
		
		// get all the records for the specified systems with specified types
		final List<ScoreRecord> records = new ArrayList<ScoreRecord>();
		if ( systems.size() > 0 ) {
			for ( final String system : systems ) {
				final Map<String, String> bindings = new HashMap<String, String>();
				bindings.put( PVData.systemKey, system );
				if( types.size() > 0 ) {
					for ( final String type : types ) {
						bindings.put( PVData.typeKey, type );
						final List<ScoreRecord> theRecords = toScoreRecords(dataTable.getRecords( bindings, ordering ));
						records.addAll( theRecords );
					}
				}
			}
		}
		TABLE_MODEL.setRecords( records );
	}
    
    
    /** cast the generic records to score records as a new list */
    private static List<ScoreRecord> toScoreRecords( List<GenericRecord> records ) {
        final List<ScoreRecord> scoreRecords = new ArrayList<ScoreRecord>();
        for ( final GenericRecord record : records ) {
            scoreRecords.add( (ScoreRecord)record );
        }
        return scoreRecords;
    }
	
	
	/** construct the table from the PV Set */
	public JTable makeTable() {		
		// Set default columnWidths:
		theTable.getColumnModel().getColumn( ScoreColumns.SYSTEM_COLUMN ).setPreferredWidth(40);
		theTable.getColumnModel().getColumn( ScoreColumns.TYPE_COLUMN ).setPreferredWidth(40);
		theTable.getColumnModel().getColumn( ScoreColumns.SETPOINT_NAME_COLUMN ).setPreferredWidth(150);
		theTable.getColumnModel().getColumn( ScoreColumns.SETPOINT_SAVED_VALUE_COLUMN ).setPreferredWidth(100);
		theTable.getColumnModel().getColumn( ScoreColumns.SETPOINT_LIVE_VALUE_COLUMN ).setPreferredWidth(100);
		theTable.getColumnModel().getColumn( ScoreColumns.READBACK_NAME_COLUMN ).setPreferredWidth(150);
		theTable.getColumnModel().getColumn( ScoreColumns.READBACK_SAVED_VALUE_COLUMN ).setPreferredWidth(40);
		theTable.getColumnModel().getColumn( ScoreColumns.READBACK_LIVE_VALUE_COLUMN ).setPreferredWidth(40);
		theTable.getColumnModel().getColumn( ScoreColumns.SETPOINT_RELATIVE_ERROR_COLUMN ).setPreferredWidth(40);
		theTable.getColumnModel().getColumn( ScoreColumns.READBACK_RELATIVE_ERROR_COLUMN ).setPreferredWidth(40);
		
		theTable.setRowSelectionAllowed( true );
		theTable.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
		return theTable;
	}
}



/** column definitions for the Score table */
interface ScoreColumns {
	/** system column */
	static public final int SYSTEM_COLUMN = 0;
	
	/** type column */
	static public final int TYPE_COLUMN = 1;
	
	/** setpoint name column */
	static public final int SETPOINT_NAME_COLUMN = 2;
	
	/** setpoint saved value column */
	static public final int SETPOINT_SAVED_VALUE_COLUMN = 3;
	
	/** setpoint live value column */
	static public final int SETPOINT_LIVE_VALUE_COLUMN = 4;
	
	/** readback name column */
	static public final int READBACK_NAME_COLUMN = 5;
	
	/** readback saved value column */
	static public final int READBACK_SAVED_VALUE_COLUMN = 6;
	
	/** readback live value column */
	static public final int READBACK_LIVE_VALUE_COLUMN = 7;
	
	/** setpoint relative error column */
	static public final int SETPOINT_RELATIVE_ERROR_COLUMN = 8;
	
	/** readback relative error column */
	static public final int READBACK_RELATIVE_ERROR_COLUMN = 9;
}



/** implements a table cell renderer that aligns text to the right */
class ScoreValueTableCellRenderer extends DefaultTableCellRenderer {
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
	final private ScoreDocument DOCUMENT;
	
	
	/** Constructor */
	public ScoreValueTableCellRenderer( final ScoreDocument document ) {
		super();
		DOCUMENT = document;
	}
	
	
	/** get the cell renderer with the right alignment */
    @SuppressWarnings( "unchecked")
    //Suppressed warning on table.getModel() because it cannot be cast as KeyValueFilteredTableModel<ScoreRecord>
	public Component getTableCellRendererComponent( final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column ) {
		final KeyValueFilteredTableModel<ScoreRecord> model = (KeyValueFilteredTableModel<ScoreRecord>)table.getModel();
		final int modelRow = table.convertRowIndexToModel( row );
		final ScoreRecord record = model.getRecordAtRow( modelRow );
		
		final JLabel label = (JLabel)super.getTableCellRendererComponent( table,  value, isSelected, hasFocus, row, column );
		final int alignment =  record != null ? getHorizontalAlignment( table, column, record ) : SwingConstants.LEFT;
		label.setHorizontalAlignment( alignment );
		final Color foreground = getForeground( table, column, record );
		setForeground( foreground );
		configureFont( label, table, column );
		
		return label;
	}
	
	
	/** set the label's font according to the column */
	private void configureFont( final JLabel label, final JTable table, final int column ) {
		final Font baseFont = label.getFont();
		final int modelColumn = table.convertColumnIndexToModel( column );
		switch ( modelColumn ) {
			case ScoreColumns.SYSTEM_COLUMN: case ScoreColumns.TYPE_COLUMN:
				label.setFont( label.getFont().deriveFont( Font.PLAIN ).deriveFont( Font.ITALIC ) );
				break;
			default:
				label.setFont( label.getFont().deriveFont( Font.PLAIN ) );
				break;
		}
	}
	
	
	/** determine the foreground color for the table cell */
	private Color getForeground( final JTable table, final int column, final ScoreRecord record ) {
		if ( record != null ) {
			final double tolerance = DOCUMENT.cuttoffFraction;
			final int modelColumn = table.convertColumnIndexToModel( column );
			switch ( modelColumn ) {
				case ScoreColumns.SETPOINT_LIVE_VALUE_COLUMN:
					return record.isSetpointWithinTolerance( tolerance ) ? Color.BLACK : Color.RED;
				case ScoreColumns.READBACK_LIVE_VALUE_COLUMN:
					return record.isReadbackWithinTolerance( tolerance ) ? Color.BLACK : Color.RED;
				default:
					return Color.BLACK;
			}
		}
		else {
			return Color.BLACK;
		}
	}
	
	
	/** get the horizontal alignment for the specified row and column */
	private int getHorizontalAlignment( final JTable table, final int column, final ScoreRecord record ) {
		final int modelColumn = table.convertColumnIndexToModel( column );
		switch ( modelColumn ) {
			case ScoreColumns.SETPOINT_SAVED_VALUE_COLUMN: case ScoreColumns.SETPOINT_LIVE_VALUE_COLUMN: case ScoreColumns.READBACK_SAVED_VALUE_COLUMN: case ScoreColumns.READBACK_LIVE_VALUE_COLUMN:
				if ( record != null ) {
					final DataTypeAdaptor dataTypeAdaptor = record.getDataTypeAdaptor();
					return dataTypeAdaptor != null ? dataTypeAdaptor.getHorizontalAlignment() : SwingConstants.LEFT;
				}
				else {
					return SwingConstants.LEFT;
				}
			default:
				return SwingConstants.LEFT;
		}
	}
}
