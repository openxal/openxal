//
//  KeyValueTableModel.java
//  xal
//
//  Created by Tom Pelaia on 1/29/09.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.widgets.swing;

import xal.tools.data.KeyValueAdaptor;
import xal.tools.data.KeyValueRecordListener;
import xal.tools.messaging.MessageCenter;
import xal.tools.StringJoiner;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.regex.*;
import javax.swing.table.AbstractTableModel;


/** Table Model whose records are arbitrary objects and whose values are obtained through the Key-Value adaptor */
public class KeyValueTableModel<RecordType> extends AbstractTableModel {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
	/** pattern for matching a lower case letter followed immediately by an upper case letter */
	final static private Pattern LOWER_UPPER_PATTERN;
	
	/** message center for posting events */
	final private MessageCenter MESSAGE_CENTER;
	
	/** proxy for events to be forwarded to registered listeners */
	final private KeyValueRecordListener<KeyValueTableModel<RecordType>,RecordType> EVENT_PROXY;
	
	/** key value adaptor to get the value from a record (row) for the specified key path (column property) */
	final protected KeyValueAdaptor KEY_VALUE_ADAPTOR;
	
	/** column names keyed by key path */
	final private Map<String,String> COLUMN_NAME_MAP;
	
	/** column class keyed by key path */
	final private Map<String,Class<?>> COLUMN_CLASS_MAP;
	
	/** column edit indicator map keyed by key path */
	final private Map<String,ColumnEditRule<RecordType>> COLUMN_EDITABLE_MAP;
	
	/** list of records to display (one record for each table row) */
	private List<RecordType> _records;
	
	/** array of key paths to get the data to display (one key path for each column) */
	private String[] _keyPaths;
	
	
	// static initializer
	static {
		LOWER_UPPER_PATTERN = Pattern.compile( "(\\p{Lower}\\p{Upper})" );
	}
	
	
	/** 
	 * Primary Constructor
	 * @param records the list of objects (one record for each table row)
	 * @param keyPaths specifies the array of key paths to get the data to display (one key path for each column)
	 */
    @SuppressWarnings( "unchecked" )    // Java static fields don't support generics so we can't use them to strongly type KeyValueRecordListener.class
	public KeyValueTableModel( final List<RecordType> records, final String ... keyPaths ) {
		MESSAGE_CENTER = new MessageCenter( "KeyValueTableModel" );
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, KeyValueRecordListener.class );
		
		KEY_VALUE_ADAPTOR = new KeyValueAdaptor();
		
		COLUMN_NAME_MAP = new HashMap<String,String>();
		COLUMN_CLASS_MAP = new HashMap<String,Class<?>>();
		COLUMN_EDITABLE_MAP = new HashMap<String,ColumnEditRule<RecordType>>();
		
		setDataSource( records, keyPaths );
	}
	
	
	/** Empty Constructor */
	public KeyValueTableModel() {
		this( new java.util.ArrayList<RecordType>(), "toString" );
	}
	
	
	/**
	 * Add the specified listener as a receiver of record modification events from this instance.
	 * @param listener object to receive events
	 */
	public void addKeyValueRecordListener( final KeyValueRecordListener<? extends KeyValueTableModel<RecordType>,RecordType> listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, KeyValueRecordListener.class );
	}
	
	
	/**
	 * Remove the specified listener from receiving record modification events from this instance.
	 * @param listener object to be removed from receiving events
	 */
	public void removeKeyValueRecordListener( final KeyValueRecordListener<KeyValueTableModel<RecordType>,RecordType> listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, KeyValueRecordListener.class );
	}
	
	
	/** 
	 * Set the records (row) and key paths (column accessors)
	 * @param records the list of objects (one record for each table row)
	 * @param keyPaths specifies the array of key paths to get the data to display (one key path for each column)
	 */
	public void setDataSource( final List<RecordType> records, final String ... keyPaths ) {
		setRecords( records );
		setKeyPaths( keyPaths );
	}
	
	
	/** Get the record at the specified row index */
	public RecordType getRecordAtRow( final int row ) {
		return _records.get( row );
	}


	/** Get the records indexed by row */
	public List<RecordType> getRowRecords() {
		return _records;
	}
	
	
	/** 
	 * Set the records (row)
	 * @param records the list of objects (one record for each table row)
	 */
	public void setRecords( final List<RecordType> records ) {
		_records = records;
		fireTableDataChanged();
	}


	/**
	 * Get the key path for the specified model column
	 * @param column model column for which to get the key path
	 * @return key path for the specified model column or null if the column is out of bounds
	 */
	public String getKeyPathForColumn( final int column ) {
		return column >= 0 && column < _keyPaths.length ? _keyPaths[column] : null;
	}


	/** 
	 * Get the model column for the specified key path
	 * @param keyPath key path to the specified column
	 * @return model column index for the specified key path or -1 if no match
	 */
	public int getColumnForKeyPath( final String keyPath ) {
		if ( keyPath == null )  return -1;	// nothing to match

		for ( int column = 0 ; column < _keyPaths.length ; column++  ) {
			if ( keyPath.equals( _keyPaths[column] ) )  return column;
		}

		// no match was found
		return -1;
	}
	
	
	/**
	 * Set the key paths (column accessors)
	 * @param keyPaths specifies the array of key paths to get the data to display (one key path for each column)
	 */
	public void setKeyPaths( final String ... keyPaths ) {
		_keyPaths = keyPaths;
		if ( keyPaths != null ) {
			for ( final String keyPath : keyPaths ) {
                // generate a default column name if one has not already been set
                if ( !COLUMN_NAME_MAP.containsKey( keyPath ) ) {
                    final String name = toColumnName( keyPath );
                    setColumnName( keyPath, name );
                }
			}
		}
		fireTableStructureChanged();
	}
	
	
	/** Generate a column name from the specified key path */
	static private String toColumnName( final String keyPath ) {
		final StringJoiner nameBuffer = new StringJoiner( " " );
		final String[] keys = keyPath.split( "\\." );
		for ( final String key : keys ) {
			final String keyTitle = toTitle( key );
			nameBuffer.append( keyTitle );
		}
		return nameBuffer.toString();
	}
	
	
	/** Capitalize the first word and split the text into words where word boundaries are identified by a lower case letter followed by an upper case letter. */
	static private String toTitle( final String text ) {
		final Matcher matcher = LOWER_UPPER_PATTERN.matcher( text );
		final StringJoiner titleBuffer = new StringJoiner( " " );
		int lastLocation = 0;
		// look for matches of a lower case character followed immediately by an upper case character and insert a space between them
		while ( matcher.find( lastLocation ) ) {
			final int location = matcher.start();
			titleBuffer.append( text.substring( lastLocation, location + 1 ) );
			lastLocation = location + 1;
		}
		if ( lastLocation < text.length() )  titleBuffer.append( text.substring( lastLocation, text.length() ) );
		return capitalizeFirstLetter( titleBuffer.toString() );
	}
	
	
	/** capitalize the first letter of the text */
	static private String capitalizeFirstLetter( final String text ) {
		return text.substring( 0, 1 ).toUpperCase() + ( text.length() > 1 ? text.substring( 1 ) : "" );
	}
	
	
	/**
	 * Set the name of the specified column
	 * @param keyPath key path for which to assign the name
	 * @param name the new name to assign the column
	 */
	public void setColumnName( final String keyPath, final String name ) {
		COLUMN_NAME_MAP.put( keyPath, name );
		fireTableStructureChanged();
	}
	
	
	/** Get the name of the specified column */
	public String getColumnName( final int column ) {
		return COLUMN_NAME_MAP.get( _keyPaths[column] );
	}
	
	
	/**
	 * Set the column class for the specified column
	 * @param keyPath key path for which to assign the class
	 * @param columnClass the new class to assign the column
	 */
	public void setColumnClass( final String keyPath, final Class<?> columnClass ) {
        setColumnClassForKeyPaths( columnClass, keyPath );
	}
	
	
	/**
	 * Set the column class for the columns specified by the key paths
	 * @param columnClass the new class to assign the column
	 * @param keyPaths key paths for which to assign the class
	 */
	public void setColumnClassForKeyPaths( final Class<?> columnClass, final String ... keyPaths ) {
        for ( final String keyPath : keyPaths ) {
            COLUMN_CLASS_MAP.put( keyPath, columnClass );
        }
		fireTableStructureChanged();
	}
	
	
	/** Get the data class for the specified column */
	public Class<?> getColumnClass( final int column ) {
		final Class<?> customClass = COLUMN_CLASS_MAP.get( _keyPaths[column] );
		return customClass != null ? customClass : super.getColumnClass( column );
	}
	
	
	/** Get the number of rows to display */
	public int getRowCount() {
		return _records != null ? _records.size() : 0;
	}
	
	
	/** Get the number of columns to display */
	public int getColumnCount() {
		return _keyPaths != null ? _keyPaths.length : 0;
	}
	
	
	/** Set whether the column associated with the specified key path is editable */
	public void setColumnEditable( final String keyPath, final boolean allowsEdit ) {
		COLUMN_EDITABLE_MAP.put( keyPath, new SimpleColumnEditRule<RecordType>( allowsEdit ) );
		fireTableDataChanged();
	}
	
	
	/** 
	 * Set whether a cell associated with the specified column key path is editable based on the cell's record value corresponding to the specified edit key path 
	 * @param columnKeyPath key path of the column for which the edit rule applies
	 * @param editKeyPath key path (applied to the row's record) whose corresponding value determines whether the cell (of the row's record) is editable
	 */
	public void setColumnEditKeyPath( final String columnKeyPath, final String editKeyPath ) {
		setColumnEditKeyPath( columnKeyPath, editKeyPath, false );
	}
	
	
	/** 
	 * Set whether a cell associated with the specified column key path is editable based on the cell's record value corresponding to the specified edit key path 
	 * @param columnKeyPath key path of the column for which the edit rule applies
	 * @param editKeyPath key path (applied to the row's record) whose corresponding value determines whether the cell (of the row's record) is editable
	 * @param negation indicates whether to reverse the boolean indication of the edit column
	 */
	public void setColumnEditKeyPath( final String columnKeyPath, final String editKeyPath, final boolean negation ) {
		COLUMN_EDITABLE_MAP.put( columnKeyPath, new KeyedColumnEditRule<RecordType>( editKeyPath, negation ) );
		fireTableDataChanged();
	}
	
	
	/** Determine whether the cell is editable */
	public boolean isCellEditable( final int row, final int column ) {
		final String keyPath = _keyPaths[column];
		final ColumnEditRule<RecordType> editRule = COLUMN_EDITABLE_MAP.get( keyPath );
		final List<RecordType> records = _records;
		if ( row < records.size() ) {
			final RecordType record = records.get( row );
			return editRule != null && editRule.isCellEditable( record );
		}
		else {
			return false;
		}
	}
	
	
	/** get the value for the specified cell */
	public Object getValueAt( final int row, final int column ) {
		final List<RecordType> records = _records;
		if ( row < records.size() ) {
			final RecordType record = records.get( row );
			return KEY_VALUE_ADAPTOR.valueForKeyPath( record, _keyPaths[column] );
		}
		else {
			return null;
		}
	}
	
	
	/** Set the value of the specified cell */
	public void setValueAt( final Object value, final int row, final int column ) {
		final List<RecordType> records = _records;
		if ( row < records.size() ) {
			final RecordType record = records.get( row );
			KEY_VALUE_ADAPTOR.setValueForKeyPath( record, _keyPaths[column], value );
			EVENT_PROXY.recordModified( this, record, _keyPaths[column], value );
		}
	}
	
	
	
	/** rule to determine whether a column's cell is editable for a specific record */
	private interface ColumnEditRule<RecordType> {
		/** indicates whether the column's cell for the specified record is editable */
		public boolean isCellEditable( final RecordType record );
	}
	
	
	
	/** rule to determine whether a column's cells are editable regardless of the record */
	private class SimpleColumnEditRule<RecordType> implements ColumnEditRule<RecordType> {
		/** indicates whether the column's cells are editable */
		final private boolean EDITABLE;
		
		/** Constructor */
		public SimpleColumnEditRule( final boolean editable ) {
			EDITABLE = editable;
		}
		
		/** indicates whether the column's cell for the specified record is editable */
		public boolean isCellEditable( final RecordType record ) {
			return EDITABLE;
		}
	}
	
	
	
	/** rule to determine whether a column's cells are editable based on a specified keyed value of the record */
	private class KeyedColumnEditRule<RecordType> implements ColumnEditRule<RecordType> {
		/** record key whose corresponding value determines whether a cell is editable */
		final private String EDIT_KEYPATH;
		
		/** indicates whether to reverse the edit value */
		final private boolean NEGATION;
		
		/** Constructor */
		public KeyedColumnEditRule( final String editKeypath, final boolean negation ) {
			EDIT_KEYPATH = editKeypath;
			NEGATION = negation;
		}
		
		/** indicates whether the column's cell for the specified record is editable */
		public boolean isCellEditable( final RecordType record ) {
			try {
				final Object value = KEY_VALUE_ADAPTOR.valueForKeyPath( record, EDIT_KEYPATH );
				return value != null && value instanceof Boolean ? NEGATION ^ ((Boolean)value).booleanValue() : false;
			}
			catch( Exception exception ) {
				return false;
			}
		}
	}
}
