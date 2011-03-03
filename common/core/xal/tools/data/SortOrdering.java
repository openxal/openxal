/*
 * SortOrdering.java
 *
 * Created on Fri Aug 15 16:42:50 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.tools.data;

import java.util.*;


/**
 * SortOrdering can be used in DataTable queries to order records based on their values associated with the sort ordering keys.  These associated values must be comparable (i.e. their class must
 * implement the Comparable interface).  The comparison is made starting with the first key and considers successive keys as necessary until the records can be ordered or all keys are
 * exhausted resulting in equality.
 * @author  tap
 */
public class SortOrdering implements Comparator<KeyedRecord> {
	protected String[] _keys;
	
	
	/**
	 * Empty constructor with now sort keys specified.
	 */
	public SortOrdering() {
		this( new String[0] );
	}
	
	
	/**
	 * Constructor with a single sort key.
	 * @param key The lone key used for sorting.
	 */
	public SortOrdering( final String key ) {
		this( new String[] {key} );
	}
	
	
	/**
	 * Constructor with an array of sort keys.
	 * @param newKeys The array of keys used for the sort ordering.
	 */
	public SortOrdering( final String[] newKeys ) {
		_keys = new String[newKeys.length];
		System.arraycopy(newKeys, 0, _keys, 0, newKeys.length);
	}
	
	
	/**
	 * Add a new sort key to the end of the sort keys.
	 * @param newKey The new sort key.
	 * @return this for convenience in adding sorting criteria
	 */
	public SortOrdering addKey( final String newKey ) {
		String[] newKeys = new String[_keys.length + 1];
		System.arraycopy( _keys, 0, newKeys, 0, _keys.length );
		newKeys[_keys.length] = newKey;
		_keys = newKeys;
		
		return this;
	}
	
	
	/**
	 * Get the sort ordering keys.
	 * @return The sort ordering keys.
	 */
	public String[] getKeys() {
		return _keys;
	}
	
	
	/**
	 * Compare two records.  The sort ordering compares records based on their values associated with 
	 * the sort ordering keys.  These associated values must be comparable (i.e. their class must
	 * implement the Comparable interface).  The comparison is made starting with the first key
	 * and considers successive keys as necessary until the records can be ordered or all keys are
	 * exhausted resulting in equality.
	 * @param record1 The first record of the comparison.
	 * @param record2 The second record of the comparison.
	 * @return 0 if the records are equal, negative if record2 > record1 and positive if record1 > record2
	 */
	public int compare( final KeyedRecord record1, final KeyedRecord record2 ) {
		int comparison = 0;
		
		for ( int index = 0 ; index < _keys.length ; index++ ) {
			String key = _keys[index];
			comparison = ((Comparable)record1.valueForKey( key )).compareTo( (Comparable)record2.valueForKey( key ) );
			if ( comparison != 0 )  return comparison; 
		}
		return comparison;
	}
}

