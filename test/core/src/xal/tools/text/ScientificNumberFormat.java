/*
 *  ScientificNumberFormat.java
 *
 *  Created at August 27, 2015
 */

package xal.tools.text;

import java.io.IOException;
import java.text.*;


/**
 * Formats numbers in scientific notation using the specified number of signficant digits and a 
 * specified width for the space occupied by the right justified formatted output. The ouput always
 * has exactly one integer digit and displays the specified number of significant digits using 
 * exponential notation as needed.
 */
public class ScientificNumberFormat extends NumberFormat {
	/** serialization ID */
	private static final long serialVersionUID = 1L;

	/** internal simple format without exponent for numbers where:  1 <= abs(number) < 10  */
	private final DecimalFormat SIMPLE_FORMAT = new DecimalFormat("0.0000");

	/** internal simple format with exponent for numbers where:  abs(number) < 1 or abs(number) >= 10  */
	private final DecimalFormat EXPONENTIAL_FORMAT = new DecimalFormat("0.0000E0");

	/** total number of significant digits to display (includes digits left and right of decimal) */
	private int _significantDigits;

	/** field width of right justified text to display */
	private int _fieldWidth;

	/** format the number padding with spaces to the left (right justification) as needed to fill out the field width */
	private boolean _fixedLength;


	/**
	 * Designated constructor.
	 * @param significantDigits total number of significant digits to display
	 * @param fieldWidth field width of right justified text to display
	 * @param fixedLength specified whether to pad the formatted output to the left (right justification) as needed to fill out the field width
	 */
	public ScientificNumberFormat( final int significantDigits, final int fieldWidth, final boolean fixedLength ) {
		setSignificantDigits( significantDigits );
		setFieldWidth( fieldWidth );
		setFixedLength( fixedLength );
	}


	/** 
	 * Convenience constructor.
	 * By default, formats a value with padding to the field width (set fixedLength to change mode).
	 * @param significantDigits total number of significant digits to display
	 * @param fieldWidth field width of right justified text to display
	 */
	public ScientificNumberFormat( final int significantDigits, final int fieldWidth ) {
		this( significantDigits, fieldWidth, true );
	}


	/**
	 * Convenience constructor.
	 * The width is significant digits + 1 space + 1 sign + 1 decimal point + 5 for exponent field.
	 * By default, formats a value without padding to the field width (set fixedLength to change mode).
	 * @param significantDigits total number of significant digits to display
	 */
	public ScientificNumberFormat( final int significantDigits ) {
		this( significantDigits, significantDigits + 1 + 1 + 1 + 5, false );
	}


	/** 
	 * Empty convenience constructor defaulting to four significant digits. 
	 * By default, formats a value without padding to the field width (set fixedLength to change mode).
	 */
	public ScientificNumberFormat() {
		this( 4 );
	}


	/** 
	 * Determine whether the formatted output is padded with spaces to the left (right justification) as needed to fill out the fixed width.
	 * @return true if using fixed length mode and false if not
	 */
	public boolean isFixedLength() {
		return _fixedLength;
	}


	/** format values using a the field width and padding with spaces as needed */
	public void setFixedLength( final boolean fixedLength ) {
		_fixedLength = fixedLength;
	}


	/**
	 * Get the number of significant digits to output
	 * @return the number of significant digits to output
	 */
	public int getSignificantDigits() {
		return _significantDigits;
	}


	/** 
	 * Set the number of significant digits  
	 * @param significantDigits total number of significant digits to display
	 */
	public void setSignificantDigits( final int significantDigits ) {
		_significantDigits = significantDigits;

		final StringBuffer patternBuffer = new StringBuffer("0");
		if ( significantDigits > 1 ) {		// there are fractional digits to display
			// append a decimal point
			patternBuffer.append( "." );
			// append a zero for each digit to the right of the decimal point
			for ( int digit = 1; digit < significantDigits ; digit++ ) {
				patternBuffer.append( "0" );
			}
		}

		final String simplePattern = patternBuffer.toString();
		final String exponentialPattern = simplePattern + "E0";

		SIMPLE_FORMAT.applyPattern( simplePattern );
		EXPONENTIAL_FORMAT.applyPattern( exponentialPattern );
	}


	/** 
	 * Get the field width for displaying the right justified output when used with fixed length mode.
	 * @return the field width
	 */
	public int getFieldWidth() {
		return _fieldWidth;
	}


	/** 
	 * Set the field width (used with fixed length mode to format output with fixed width padding with spaces as needed)
	 * @param fieldWidth field width of right justified text to display
	 */
	public void setFieldWidth( final int fieldWidth ) {
		_fieldWidth = fieldWidth;
	}


	/** Implement the abstract format method by delegating to an internal number format */
	public StringBuffer format( final double number, final StringBuffer inputBuffer, final FieldPosition position ) {
		// get the absolute value
		final double absValue = Math.abs( number );

		// if the absolute value is between 1 inclusive and 10 exclusive or if it is identically zero, we can use a simple format otherwise we must use exponential notation
		final StringBuffer buffer;
		if ( ( absValue >= 1.0 && absValue < 10.0 ) || absValue == 0.0 ) {
			buffer = SIMPLE_FORMAT.format( number, inputBuffer, position );
		} else {
			buffer = EXPONENTIAL_FORMAT.format( number, inputBuffer, position );
		}

		// if fixed length mode, pad with spaces to the left of the number for a total width matching the field width
		if ( _fixedLength ) {
			final int spaces = _fieldWidth - buffer.length();
			if ( spaces > 0 ) {
				for( int space = 0 ; space < spaces ; space++ ) {
					buffer.insert( 0, " " );
				}
			}
		}

		return buffer;
	}


	/** Implement the abstract format method by delegating to an internal number format */
	public StringBuffer format( final long number, final StringBuffer inputBuffer, final FieldPosition position ) {
		return format( (double)number, inputBuffer, position );
	}


	/** Implement the abstract parse method by delegating to an internal number format */
	public Number parse( final String source, final ParsePosition position ) {
		return SIMPLE_FORMAT.parse( source, position );
	}


	/**
	 * Append the formatted values to the output throwing any internal IOException from the output.
	 * Use this method when you want to handle IO Exceptions
	 * @param output the output to which to append the formatted values
	 * @param separator the characters to insert between each formatted value (e.g. this could be a comma)
	 * @param values the values to format and output
	 */
	public void appendToIO( final Appendable output, final CharSequence separator,  final double ... values ) throws IOException {
		if ( values == null || values.length == 0 )  return;	//nothing to append

		// append the first formatted value
		output.append( format(values[0]) );

		// append the remaining formatted values with the leading separator
		for ( int index = 1 ; index < values.length ; index++ ) {
			output.append( separator );
			output.append( format( values[index] ) );
		}
	}


	/**
	 * Convenience method to append the formatted values to the output suppressing IOException and instead dumping the stack trace upon exception.
	 * This is useful when calling with outputs that don't through IOException (e.g. StringBuffer and StringBuilder)
	 * @param output the output to which to append the formatted values
	 * @param separator the characters to insert between each formatted value (e.g. this could be a comma)
	 * @param values the values to format and output
	 */
	final public void appendTo( final Appendable output, final CharSequence separator,  final double ... values ) {
		try {
			appendToIO( output, separator, values );
		}
		catch( IOException exception ) {
			System.err.println( "Error appending formatted values to an appendable output." );
			exception.printStackTrace();
		}
	}


	/**
	 * Print the formatted values
	 * @param separator the characters to insert between each formatted value (e.g. this could be a comma)
	 * @param terminator the characters to append to the end of the line of formatted values (e.g. this could be "\n")
	 * @param values the values to format and output
	 */
	final public void print( final CharSequence separator, final CharSequence terminator, final double ... values ) {
		appendTo( System.out, separator, values );
		System.out.append( terminator );
	}


	/**
	 * Convenience method to print the formatted values with the system's trailing line terminator
	 * @param separator the characters to insert between each formatted value (e.g. this could be a comma)
	 * @param values the values to format and output
	 */
	final public void println( final CharSequence separator, final double ... values ) {
		this.print( separator, System.getProperty("line.separator"), values );
	}
}


