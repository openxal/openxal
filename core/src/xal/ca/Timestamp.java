/*
 * Timestamp.java
 *
 * Created on Tue Dec 09 15:22:55 EST 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.ca;


import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Timestamp
 * @author  tap
 */
public class Timestamp implements Comparable<Timestamp> {
	/** date formatter */
    static final SimpleDateFormat TIME_FORMATTER;
	
	/** format an integer to have nine digits including leading zeros as necessary   */
	static final DecimalFormat NANOSECOND_FORMATTER;
	
	/** constant for 1000 */
	static final BigDecimal THOUSAND = new BigDecimal( 1000 );
	
	/** the timestamp information */
	protected BigDecimal _timestamp;
	
    
	// static initializer
    static {
        TIME_FORMATTER = new SimpleDateFormat( "MMM d, yyyy HH:mm:ss" );
		NANOSECOND_FORMATTER = new DecimalFormat( "000000000" );
    }
	
	
	/**
	 * Primary constructor from the full precision seconds since the Java epoch.
	 * @param timestamp the number of seconds since the Java epoch
	 */
	public Timestamp( final BigDecimal timestamp ) {
		_timestamp = timestamp;
	}
	
	
	/**
	 * Construct a Timestamp from a java.sql.Timestamp
	 * @param timestamp an SQL timestamp
	 */
	public Timestamp( final java.sql.Timestamp timestamp ) {
		this( toBigDecimal( timestamp ) );
	}
    
    
    /**
     * Get the times tamp as a Java Date.
     * Some precision is lost since the Date class only supports millisecond resolution.
     * @return The time stamp as a Date.
     */
    public Date getDate() {
        return new Date( getTime() );
    }
    
    
    /**
     * Get the timestamp in milliseconds relative to the Java epoch.
     * @return The time in milliseconds since the Java epoch.
     */
    public long getTime() {
        return ( _timestamp.multiply(THOUSAND) ).longValue();        
    }
	
	
	/**
	 * Get the time in seconds since the Java epoch.
	 * @return the time in seconds since the Java epoch.
	 */
	public double getSeconds() {
		return _timestamp.doubleValue();
	}
	
	
	/**
	 * Get the time in seconds with full precision since the Java epoch.
	 * @return the time in seconds since the Java epoch.
	 */
	public BigDecimal getFullSeconds() {
		return _timestamp;
	}
	
	
	/**
	 * Get the SQL Timestamp equivalent of this instance.
	 * @return the SQL Timestamp equivalent of this instance.
	 */
	public java.sql.Timestamp getSQLTimestamp() {
		java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp( getTime() );
		int nanoseconds = _timestamp.subtract( _timestamp.setScale(0, BigDecimal.ROUND_DOWN) ).movePointRight(9).intValue();
		sqlTimestamp.setNanos(nanoseconds);
		
		return sqlTimestamp;
	}
	
	
	/**
	 * Convert a java.sql.Timestamp to BigDecimal time.
	 * @param timestamp The timestamp as a java.sql.Timestamp
	 * @return the timestamp as a BigDecimal in seconds since the Java epoch
	 */
	static private BigDecimal toBigDecimal(java.sql.Timestamp timestamp) {
		BigDecimal seconds = new BigDecimal( timestamp.getTime() / 1000 );
		BigDecimal nanoSeconds = new BigDecimal( timestamp.getNanos() );
		return seconds.add( nanoSeconds.movePointLeft(9) ).setScale( 9, BigDecimal.ROUND_HALF_UP );
	}
	
	
	/** 
	 * Generate a string representation of this timestamp using the specified time format for the time format up to seconds. Subsecond time is appended using a decimal point. 
	 * @param timeFormat format for which to generate the string
	 * @return formatted string representation
	 */
	public String toString( final DateFormat timeFormat ) {
		final long nanoseconds = _timestamp.subtract( _timestamp.setScale( 0, BigDecimal.ROUND_DOWN ) ).movePointRight(9).longValue();
		return timeFormat.format( getDate() ) + "." + NANOSECOND_FORMATTER.format( nanoseconds );
	}
	
	
	/**
	 * Get a string representation of the Timestamp.
	 * @return a string representation of the Timestamp
	 */
	public String toString() {
		return toString( TIME_FORMATTER );
	}
	
	
	/**
	 * Determine if the specified timestamp equals this one.
	 * @return true if they are equals and false if not
	 */
	public boolean equals( final Object timestamp ) {
		if ( timestamp != null && timestamp instanceof Timestamp ) {
			return ((Timestamp)timestamp)._timestamp.equals( _timestamp );
		}
		else {
			return false;
		}
	}


	/** Override the hashcode as required when overriding equals. Equality implies equality of the underlying _timestamp instance variables. */
	public int hashCode() {
		return _timestamp.hashCode();
	}
	
	
	/**
	 * Compare this timestamp with another timestamp.
	 * @param otherTimestamp The timestamp with which to compare this one
	 * @return 0 if the timestamps are the same, negative if this is earlier than the supplied one and positive otherwise
	 */
	public int compareTo( final Timestamp otherTimestamp ) {
		return _timestamp.compareTo( otherTimestamp._timestamp );
	}
}

