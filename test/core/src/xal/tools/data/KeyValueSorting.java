//
//  KeyValueSorting.java
//  xal
//
//  Created by Tom Pelaia on 1/30/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.data;

import java.util.Comparator;
import java.util.List;


/** factory to support sorting using the key value adaptor */
public class KeyValueSorting {
	/** indicates ascending order */
	static final public int DESCENDING = -1;
	
	/** indicates ascending order */
	static final public int ASCENDING = 1;
	
	
	/** 
	 * Generate a comparator that compares records according to the method obtained by a key value adaptor generated internally.
	 * The keyed values being compared are assumed to be comparable.
	 * @param keyPath indicates the method used for comparison
	 * @param order either ASCENDING or DESCENDING
	 * @return a comparator which compares objects according to the keyed parameter
	 */
	static public <RecordType> Comparator<RecordType> comparatorForKeyPath( final String keyPath, final int order ) {
		return KeyValueSorting.<RecordType>comparatorForKeyPath( new KeyValueAdaptor(), keyPath, order );
	}
	
	
	/** 
	 * Generate a comparator that compares records according to the method obtained by the key value adaptor
	 * The keyed values being compared are assumed to be comparable.
	 * @param adaptor key value adaptor used for getting the method associated with the key
	 * @param keyPath indicates the method used for comparison
	 * @param order either ASCENDING or DESCENDING
	 * @return a comparator which compares objects according to the keyed parameter
	 */
	static public <RecordType> Comparator<RecordType> comparatorForKeyPath( final KeyValueAdaptor adaptor, final String keyPath, final int order ) {
		return new Comparator<RecordType>() {
			/** compares the two items for order */
            @SuppressWarnings( "unchecked" )    // no way to predetermine the value types
			public int compare( final RecordType record1, final RecordType record2 ) {
				final Comparable<Object> value1 = (Comparable<Object>)adaptor.valueForKeyPath( record1, keyPath );
				final Object value2 = adaptor.valueForKeyPath( record2, keyPath );
				return order * value1.compareTo( value2 );
			}
			
			/** the specified object is equal to this comparator */
			public boolean equals( final Object object ) {
				return object == this;
			}
		};
	}
	
	
	/**
	 * Coalesce the specified comparators to generate a compound comparator whose sub comparators are evaluated in the specified order.
	 * @param comparators array of parameters to coalesce taken in order (sort first by the first comparator and last by the last comparator)
	 * @return the compound comparator
	 */
	@SafeVarargs		// let the compiler know that heap pollution will not occur
	static public <RecordType> Comparator<RecordType> compoundComparator( final Comparator<RecordType> ... comparators ) {
		return new Comparator<RecordType>() {
			/** compares the two items for order */
            @SuppressWarnings( "unchecked" )    // no way to predetermine the item types
			public int compare( final RecordType record1, final RecordType record2 ) {
				for ( final Comparator<RecordType> comparator : comparators ) {
					final int ordering = comparator.compare( record1, record2 );
					if ( ordering != 0 ) return ordering;
				}
				return 0;
			}
			
			/** the specified object is equal to this comparator */
			public boolean equals( final Object object ) {
				return object == this;
			}
		};
	}
}
