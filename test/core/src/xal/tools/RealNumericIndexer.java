//
//  RealNumericIndexer.java
//  xal
//
//  Created by Thomas Pelaia on 6/23/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.tools;

import java.util.*;


/** Order objects by their specified numeric index and assign a corresponding integer index based on sequential order and provide mapping between the two indices. */
public class RealNumericIndexer<T> implements Iterable<T> {
	/** records of data */
	protected final List<NumericRecord<T>> _records;
	
	
	/** Constructor */
	public RealNumericIndexer() {
		_records = new ArrayList<NumericRecord<T>>();
	}
	
	
	/**
	 * Get the size
	 * @return the number of items indexed
	 */
	public int size() {
		return _records.size();
	}
	
	
	/**
	 * Get an indexer for the indexed items.
	 * @return an iterator over the indexed items
	 */
	public Iterator<T> iterator() {
		return new Iterator<T> () {
			int index = 0;
			
			public boolean hasNext() {
				return index < _records.size();
			}
			
			
			public T next() {
				return _records.get( index++ ).getValue();
			}
			
			
			public void remove() {
				throw new UnsupportedOperationException( "RealNumericIndexer does not support the remove() operation in its iterator." );
			}
		};
	}
	
	
	/**
	 * Generate a list of the values.
	 * @return a list of the values ordered according to the index
	 */
	public List<T> toList() {
		final List<T> list = new ArrayList<T>();
		final Iterator<T> iterator = iterator();
		while( iterator.hasNext() ) {
			list.add( iterator.next() );
		}
		return list;
	}
	
	
	/**
	 * Get the element at the specified index
	 * @return the value at the specified index
	 */
	public T get( final int index ) {
		return _records.get( index ).getValue();
	}
	
	
	/**
	 * Get the location corresponding to the specified index.
	 * @param index the index of the record to fetch
	 * @return the location corresponding to the specified index
	 */
	public double getLocation( final int index ) {
		return _records.get( index ).getLocation();
	}
	
	
	/**
	 * Get the index which is the greatest integer less than or equal to the index of the specified location.
	 * However, if there are several indices for the specified location, pick the least one.
	 * @param location the location for which to identify the array index
	 * @return the index which is the greatest integer less than or equal to the index of the specified location
	 */
	public int getLowerIndex( final double location ) {
		int lowerIndex = (int)Math.floor( findIndex( location ) );
		// now search for the smallest index corresponding to the specified location
		for ( int index = lowerIndex - 1 ; index >= 0 ; index-- ) {
			if ( getLocation( index ) < location )   break;
			lowerIndex = index;
		}
		
		return lowerIndex;
	}
	
	
	/**
	 * Get the index which is the least integer greater than or equal to the index of the specified location but the greatest of equals.
	 * However, if there are several indices for the specified location, pick the greatest one.
	 * @param location the location for which to identify the array index
	 * @return the index which is the least integer greater than or equal to the index of the specified location
	 */
	public int getUpperIndex( final double location ) {
		return getUpperIndex( location, 0 );
	}
	
	
	/**
	 * Get the index which is the least integer greater than or equal to the index of the specified location but the greatest of equals.
	 * However, if there are several indices for the specified location, pick the greatest one.
	 * @param location the location for which to identify the array index
	 * @param startIndex the initial index from which to start the upward search
	 * @return the index which is the least integer greater than or equal to the index of the specified location
	 */
	private int getUpperIndex( final double location, final int startIndex ) {
		final int count = size();
		int upperIndex = (int)Math.ceil( findIndex( location, startIndex, count - 1 ) );
		// now search for the largest index corresponding to the specified location
		for ( int index = upperIndex ; index < count ; index++ ) {
			if ( getLocation( index ) > location )   break;
			upperIndex = index;
		}
		
		return upperIndex;
	}
	
	
	/**
	 * Get the smallest range of indices whose location range contains the specified range inclusive.
	 * @param startLocation the starting location
	 * @param endLocation the ending location
	 * @return the indices corresponding to the location range or a range of -1s if none exists
	 */
	public int[] getIndicesWithinLocationRange( final double startLocation, final double endLocation ) {
		final int count = size();
		if ( count == 0 ) {
			return null;
		}
		else if ( startLocation == endLocation ) {
			final int index = (int)findIndex( startLocation );
			return getLocation( index ) == startLocation ? new int[] { index, index } : null;
		}
		else {
			int lowerIndex = getLowerIndex( startLocation );
			if ( getLocation( lowerIndex ) < startLocation )  ++lowerIndex;
			if ( lowerIndex >= count )  return null;
			int upperIndex = getUpperIndex( endLocation, lowerIndex );
			if ( getLocation( upperIndex ) > endLocation )  --upperIndex;
			if ( upperIndex >= lowerIndex ) {
				return new int[] { lowerIndex, upperIndex };
			}
			else {
				return null;
			}
		}
	}
	
	
	/**
	 * Get the index which is closest to the index of the specified location and round down if two are equally close.
	 * @param location the location for which to identify the array index
	 * @return the closest index
	 */
	public int getClosestIndex( final double location ) {
		return (int)Math.round( findIndex( location ) );
	}
	
	
	/**
	 * Get the index of the specified location.
	 * @param location the location for which to identify the array index
	 * @return the array index for the specified location
	 */
	public double findIndex( final double location ) {
		return findIndex( location, 0, size() - 1 );
	}
		
	
	/**
	 * Get the index of the specified location.
	 * @param location the location for which to identify the array index
	 * @return the array index for the specified location
	 */
	private double findIndex( final double location, final int startIndex, final int endIndex ) {
		if ( startIndex == endIndex )  return startIndex;
		if ( size() == 0 )  return Double.NaN;
		
		int nmin = startIndex;
		int nmax = endIndex;
		double xmin = getLocation( nmin );
		double xmax = getLocation( nmax );
		
		int iterations = 0;
		while ( nmax - nmin > 1 ) {
			iterations += 1;
			int index = (int)Math.round(estimateIndex( location, nmin, nmax, xmin, xmax ));
			if (index <= nmin) index = nmin+1; // this is needed to guarantee progress
			else if (index >= nmax) index = nmax-1;
			
			final double middle = getLocation( index );
			if ( location >= middle ) {
				xmin = middle;
				nmin = index;
			} else {
				xmax = middle;
				nmax = index;
			}
		}
		return location >= xmin ? nmax : nmin;
	}
	
	
	/**
	 * Estimate the index of the element at the specified location
	 * @param x the location for which to estimate the index
	 * @return the estimated index
	 */
	static private double estimateIndex( final double x, final int nmin, final int nmax, final double xmin, final double xmax ) {
		return ( nmin * ( xmax - x ) + nmax * ( x - xmin ) ) / ( xmax - xmin );
	}
	
	
	/**
	 * Get the minimum location.
	 * @return the minimum location
	 */
	private double getMinLocation() {
		return size() > 0 ? _records.get( 0 ).getLocation() : Double.NaN;
	}
	
	
	/**
	 * Get the maximum location.
	 * @return the maximum location
	 */
	private double getMaxLocation() {
		final int count = size();
		return count > 0 ? _records.get( count - 1 ).getLocation() : Double.NaN;
	}
	
	
	/**
	 * Add the specified value at the specified location.
	 * @param value the value to add
	 * @param location the location of the value
	 */
	public void add( final double location, final T value ) {
		final NumericRecord<T> newRecord = new NumericRecord<T>( value, location );
		final int NUM_RECORDS = _records.size();
		if ( NUM_RECORDS > 0 ) {
			final int index = getUpperIndex( location );
			if ( location < getMinLocation() ) {
				_records.add( 0, newRecord );
			}
			else if ( location >= getMaxLocation() ) {
				_records.add( newRecord );
			}
			else if ( index > 0 && index < NUM_RECORDS ) {
				_records.add( index, newRecord );
			}
			else {
				throw new RuntimeException( "RealNumericIndexer exception while adding a new record at location: " + location + " with index: " + index );
			}
		}
		else {
			_records.add( newRecord );
		}
	}
	
	
	/**
	 * Remove the specified item by index.
	 * @param index the index of the item to remove
	 */
	public T remove( final int index ) {
		final NumericRecord<T> record = _records.remove( index );
		return record != null ? record.getValue() : null;
	}
}



/** numeric record */
final class NumericRecord<T> implements Comparable<NumericRecord<T>> {
	protected T _value;
	protected double _location;
	
	
	/** constructor */
	public NumericRecord( final T value, final double location ) {
		_value = value;
		_location = location;
	}
	
	
	/** get the value */
	final public T getValue() {
		return _value;
	}
	
	
	/** get the location */
	final public double getLocation() {
		return _location;
	}
	
	
	/** compare this record to another */
	final public int compareTo( final NumericRecord<T> record ) {
		return _location < record._location ? -1 : _location > record._location ? 1 : 0;
	}
	
	
	/** string representation */
	@Override
    final public String toString() {
		return "location:  " + _location + ", value:  " + _value;
	}
}
