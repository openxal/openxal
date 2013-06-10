//
//  NodesTableModel.java
//  xal
//
//  Created by Tom Pelaia on 10/22/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

package xal.app.opticseditor;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;


/** table model for nodes */
public class NodesTableModel extends AbstractTableModel {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
	/** name column */
	static private final int NAME_COLUMN = 0;
	
	/** sequence column */
	static private final int SEQUENCE_COLUMN = 1;
	
	/** comment column */
	static private final int COMMENT_COLUMN = 2;
	
	/** status column */
	static private final int STATUS_COLUMN = 3;
	
	/** exclude column */
	static private final int EXCLUDE_COLUMN = 4;
	
	/** modified column */
	static private final int MODIFY_COLUMN = 5;
	
	/** unfiltered records */
	private List<NodeRecord> _allRecords;
	
	/** list of node records to display */
	private List<NodeRecord> _records;
	
	/** filter to apply to records */
	private NodeRecordFilter _recordFilter;
	
	
	/** Constructor */
	public NodesTableModel( final List<NodeRecord> records, final NodeRecordFilter recordFilter ) {
		_allRecords = records;
		_recordFilter = recordFilter;
		refreshRecords();
	}
	
	
	/** apply the filter to all records and regenerate the list of records to display */
	public void refreshRecords() {
		final List<NodeRecord> allRecords = _allRecords;
		final List<NodeRecord> records = new ArrayList<NodeRecord>( allRecords.size() );
		
		for ( final NodeRecord record : allRecords ) {
			if ( _recordFilter.accept( record ) ) {
				records.add( record );
			}
		}
		
		_records = records;
		
		fireTableDataChanged();
	}
	
	
	/** get the title of the specified column */
	public String getColumnName( final int column ) {
		switch ( column ) {
			case NAME_COLUMN:
				return "Node";
			case SEQUENCE_COLUMN:
				return "Sequence";
			case STATUS_COLUMN:
				return "Status";
			case EXCLUDE_COLUMN:
				return "Excluded";
			case MODIFY_COLUMN:
				return "Modified";
			case COMMENT_COLUMN:
				return "Comment";
			default:
				return "?";
		}
	}
	
	
	/** get the class of the value for the specified column */
	public Class<?> getColumnClass( final int column ) {
		return super.getColumnClass( column );
	}
	
	
	/** get the number of columns */
	public int getColumnCount() {
		return 6;
	}
	
	
	/** get the number of rows */
	public int getRowCount() {
		return _records.size();
	}
	
	
	/** get the records corresponding to the specified rows */
	public List<NodeRecord> getRecordsForRows( final int[] rows ) {
		final List<NodeRecord> records = new ArrayList<NodeRecord>( rows.length );
		for ( final int row : rows ) {
			records.add( _records.get( row ) );
		}
		return records;
	}
	
	
	/** get the value to display in the specified cell */
	public Object getValueAt( final int row, final int column ) {
		final NodeRecord record = _records.get( row );
		
		if ( record != null ) {
			switch ( column ) {
				case NAME_COLUMN:
					return record.getNodeID();
				case SEQUENCE_COLUMN:
					return record.getSequenceID();
				case STATUS_COLUMN:
					return toStatusString( record );
				case EXCLUDE_COLUMN:
					return toExcludeString( record );
				case MODIFY_COLUMN:
					return toModifyString( record );
				case COMMENT_COLUMN:
					return record.getModificationComment();
				default:
					return null;
			}			
		}
		else {
			return null;
		}		
	}
	
	
	/** Determine whether the cell is editable */
	public boolean isCellEditable( final int row,  final int column ) {
		return column == COMMENT_COLUMN;
	}
	
	
	/** set the value of the specified cell */
	public void setValueAt( final Object value, final int row, final int column ) {
		final NodeRecord record = _records.get( row );
		
		if ( record != null ) {
			switch ( column ) {
				case COMMENT_COLUMN:
					record.setModificationComment( value != null ? value.toString() : null );
					break;
				default:
					break;
			}
		}
	}
	
	
	/** get styled status string */
	static private String toStatusString( final NodeRecord record ) {
		return record.getStatus() ? "<html><body><span style='color: green;'>Enabled</span></body></html>" : "<html><body><span style='color: red;'>Disabled</span></body></html>";
	}
	
	
	/** get styled exlusion string */
	static private String toExcludeString( final NodeRecord record ) {
		return record.isExcluded() ? "<html><body><span style='color: red;'>Excluded</span></body></html>" : "";
	}
	
	
	/** get styled modify string */
	static private String toModifyString( final NodeRecord record ) {
		return record.isModified() ? "<html><body><span style='color: yellow;'>Mofified</span></body></html>" : "";
	}
}
