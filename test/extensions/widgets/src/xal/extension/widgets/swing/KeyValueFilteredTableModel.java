//
//  KeyValueFilteredTableModel.java
//  xal
//
//  Created by Tom Pelaia on 2/5/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.widgets.swing;

import xal.tools.FreshProcessor;
import xal.tools.data.*;

import java.util.List;
import javax.swing.text.*;
import javax.swing.event.*;


/** Key Value Table Model with built in support for filtering through a text input document */
public class KeyValueFilteredTableModel<T> extends KeyValueTableModel<T> {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
	/** handles the input events and filters the table records accordingly */
	final private InputFilterHandler INPUT_FILTER_HANDLER;
	
	/** record filter */
	final private KeyValueListFilter<T> RECORD_FILTER;
	
	/** input document used to specify filtering text */
	private Document _inputFilterDocument;
	
	/** list of all records to filter */
	private List<T> _allRecords;
	
	
	/** 
	 * Primary Constructor
	 * @param records the list of objects (one record for each table row)
	 * @param keyPaths specifies the array of key paths to get the data to display (one key path for each column)
	 */
	public KeyValueFilteredTableModel( final List<T> records, final String ... keyPaths ) {
		super( records, keyPaths );
		
		RECORD_FILTER = new KeyValueListFilter<T>( KEY_VALUE_ADAPTOR, _allRecords, keyPaths );
		
		INPUT_FILTER_HANDLER = new InputFilterHandler();
		setInputFilterDocument( null );
		
		filterRecords();
	}
	
	
	/** Empty Constructor */
	public KeyValueFilteredTableModel() {
		this( new java.util.ArrayList<T>(), "toString" );
	}
	
	
	/** Set the value of the specified cell */
	public void setValueAt( final Object value, final int row, final int column ) {
		super.setValueAt( value, row, column );
		final T record = getRecordAtRow( row );
		if ( record != null ) {
			RECORD_FILTER.reIndexRecord( record );
			filterRecords();
		}
	}
	
	
	/** set the specified text input document as the text source for filtering table records */
	public void setInputFilterDocument( final Document document) {		
		if ( _inputFilterDocument != null ) {
			_inputFilterDocument.removeDocumentListener( INPUT_FILTER_HANDLER );
			INPUT_FILTER_HANDLER.clear();	// clear pending requests if any
		}
		
		_inputFilterDocument = document;
		
		if ( document != null ) {
			document.addDocumentListener( INPUT_FILTER_HANDLER );
		}
	}
	
	
	/** set the specified text component as the text source for filtering table records */
	public void setInputFilterComponent( final JTextComponent component ) {
		setInputFilterDocument( component != null ? component.getDocument() : null );
	}
	
	
	/** set the key paths to use for matching */
	public void setMatchingKeyPaths( final String ... keyPaths ) {
		RECORD_FILTER.setMatchingKeyPaths( keyPaths );
		filterRecords();
	}
	
	
	/** 
	 * Overrides the inherited method to set all of the records (before filtering)
	 * @param records the list of objects
	 */
	public void setRecords( final List<T> records ) {
		setAllRecords( records );
	}
	
	
	/** 
	 * Set all of the records (before filtering)
	 * @param records the list of objects
	 */
	private void setAllRecords( final List<T> records ) {
		_allRecords = records;
		if ( RECORD_FILTER != null ) {
			RECORD_FILTER.setAllRecords( records );
			filterRecords();
		}
		fireTableDataChanged();
	}
	
	
	/** set the filtered records to display in the table */
	private void setFilteredRecords( final List<T> records ) {
		super.setRecords( records );
	}
	
	
	/** apply the filter to all records */
	private void filterRecords() {
		final Document document = _inputFilterDocument;
		if ( document != null ) {
			final String text = getText( document );
			filterRecords( text );
		}
		else {
			setFilteredRecords( _allRecords );
		}
	}
	
	
	/** get the text from the specified document */
	private String getText( final Document document ) {
		try {
			return document.getText( 0, document.getLength() );
		}
		catch( BadLocationException exception ) {
			exception.printStackTrace();
			return "";
		}		
	}
	
	
	/** apply the filter to all records */
	private void filterRecords( final String text ) {
		if ( RECORD_FILTER != null ) {
			setFilteredRecords( RECORD_FILTER.filterRecords( text ) );
		}
		else {
			setFilteredRecords( _allRecords );
		}
	}
	
	
	
	/** listens for text input changes and filters the table records accordingly */
	private class InputFilterHandler implements DocumentListener {
		/** processor for filtering records */
		final private FreshProcessor FILTER_PROCESSOR;
		
		
		/** Constructor */
		public InputFilterHandler() {
			FILTER_PROCESSOR = new FreshProcessor();
		}
		
		
		/** clear pending requests */
		public void clear() {
			FILTER_PROCESSOR.clear();
		}
		
		
		/** handle the input change event */
		public void changedUpdate( final DocumentEvent event ) {
			recordsNeedsFiltering( event );
		}
		
		/** handle the input insert event */
		public void insertUpdate( final DocumentEvent event ) {
			recordsNeedsFiltering( event );
		}
		
		/** handle the input remove event */
		public void removeUpdate( final DocumentEvent event ) {
			recordsNeedsFiltering( event );
		}
		
		/** filter the records based upon the latest input text */
		private void recordsNeedsFiltering( final DocumentEvent event ) {
			final Document document = event.getDocument();
			final String text = getText( document );
			FILTER_PROCESSOR.post( new FilterRecordsRequest( text ) );
		}
	}
	
	
	
	/** request to filter records in the table model according to the specified text */
	private class FilterRecordsRequest implements Runnable {
		/** text with which to filter the records */
		final private String FILTER_TEXT;
		
		
		/** Constructor */
		public FilterRecordsRequest( final String text ) {
			FILTER_TEXT = text;
		}
		
		
		/** filter the records in the table */
		public void run() {
			filterRecords();
		}
	}
}
