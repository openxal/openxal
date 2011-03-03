/*
 * StringJoiner.java
 *
 * Created on March 1, 2002, 3:29 PM
 */

package xal.tools;

/**
 * String joiner is a utility class for joining items as strings with an arbitrary separator.  It contains several convenience methods for appending
 * a wide variety of objects and primitives.  Once you are done appending items simply call toString() to get the string with all items joined by the separator.
 *
 * @author  tap
 */
public class StringJoiner {
	/** Separator to use between consecutive items */
	final private String SEPARATOR;
	
	/** Buffer holding the joined string while it is being assembled */
	final private StringBuffer BUFFER;
	
	/** Object performing the joins which gets changed depending on the rule for joining. */
	private Joiner _joiner;
	
	
	/** Empty Constructor using ", " as the default separator */
	public StringJoiner() {
		this( ", " );
	}
	
	
	/** Constructor taking the separator to use for joining items */
	public StringJoiner( final String separator ) {
		SEPARATOR = separator;
		BUFFER = new StringBuffer();   // create an empty string buffer
		_joiner = new FirstJoiner();    // appropriate joiner for empty buffer
	}
	
	
	/** append an integer value */
	public void append( final int value ) {
		append( String.valueOf( value ) );
	}
	
	
	/** append a short value */
	public void append( final short value ) {
		append( String.valueOf( value ) );
	}
	
	
	/** append a long value */
	public void append( final long value ) {
		append( String.valueOf( value ) );
	}
	
	
	/** append a float value */
	public void append( final float value ) {
		append( String.valueOf( value ) );
	}
	
	
	/** append a double value */
	public void append( final double value ) {
		append( String.valueOf( value ) );
	}
	
	
	/** append a boolean value */
	public void append( final boolean value ) {
		append( String.valueOf( value ) );
	}
	
	
	/** append a char value */
	public void append( final char value ) {
		append( String.valueOf( value ) );
	}
	
	
	/** append a byte value */
	public void append( final byte value ) {
		append( String.valueOf( value ) );
	}
	
	
	/** append an array of integers */
	public void append( final int[] array ) {
		for ( int value : array ) {
			append( value );
		}
	}
	
	
	/** append an array of shorts */
	public void append( final short[] array ) {
		for ( short value : array ) {
			append( value );
		}
	}
	
	
	/** append an array of long integers */
	public void append( final long[] array ) {
		for ( long value : array ) {
			append( value );
		}
	}
	
	
	/** append an array of floats */
	public void append( final float[] array ) {
		for ( float value : array ) {
			append( value );
		}
	}
	
	
	/** append an array of doubles */
	public void append( final double[] array ) {
		for ( double value : array ) {
			append( value );
		}
	}
	
	
	/** append an array of booleans */
	public void append( final boolean[] array ) {
		for ( boolean value : array ) {
			append( value );
		}
	}
	
	
	/** append an array of chars */
	public void append( final char[] array ) {
		for ( char value : array ) {
			append( value );
		}
	}
	
	
	/** append an array of bytes */
	public void append( final byte[] array ) {
		for ( byte value : array ) {
			append( value );
		}
	}
	
	
	/** append an array of Object items */
	public void append( final Object[] array ) {
		for ( Object value : array ) {
			append( value );
		}
	}
	
	
	/** append an Object item */
	public void append( final Object anObject ) {
		final String stringValue = (anObject == null) ? "" : anObject.toString();
		append( stringValue );
	}
	
	
	/** append a String item */
	public void append( final String string ) {
		_joiner.append( string );
	}
	
	
	/** get the joined items as a String */
	public String toString() {
		return BUFFER.toString();
	}
	
	
	
	/** 
	 * Joiner is an abstract class for the object that joins strings.
	 * StringJoiner converts all items to strings and sends them to a
	 * concrete subclass of Joiner to join the strings.  Joiner is abstract
	 * since it is never appropriate to instantiate.
	 */
	abstract private class Joiner {
		public void append( final String string ) {
			BUFFER.append( string );
		}
	}
	
	
	
	/**
	 * When we join an item to an empty string buffer, we simply add the item
	 * without using the separator.  Thereafter, we must join items with
	 * a lead separator.
	 */
	private class FirstJoiner extends Joiner {
		public void append( final String string ) {
			super.append( string );
			_joiner = new ConsecutiveJoiner();  // for future joins
		}
	}
	
	
	
	/**
	 * Join consecutive items with a lead separator.  ConsecutiveJoiner is
	 * used when the buffer already has one or more items in it.
	 */
	private class ConsecutiveJoiner extends Joiner {
		public void append( final String string ) {
			BUFFER.append( SEPARATOR );
			super.append( string );
		}
	}
}