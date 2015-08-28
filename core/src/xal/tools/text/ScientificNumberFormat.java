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


	/** 
	 * Designated constructor 
	 * @param significantDigits total number of significant digits to display
	 * @param fieldWidth field width of right justified text to display
	 */
	public ScientificNumberFormat( final int significantDigits, final int fieldWidth ) {
		setSignificantDigits( significantDigits );
		setFieldWidth( fieldWidth );
	}


	/**
	 * Convenience constructor. 
	 * The width is significant digits + 1 space + 1 sign + 1 decimal point + 5 for exponent field.
	 * @param significantDigits total number of significant digits to display
	 */
	public ScientificNumberFormat( final int significantDigits ) {
		this( significantDigits, significantDigits + 1 + 1 + 1 + 5 );
	}


	/** Empty convenience constructor defaulting to five significant digits. */
	public ScientificNumberFormat() {
		this( 5 );
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
	 * Get the field width for displaying the right justified output.
	 * @return the field width
	 */
	public int getFieldWidth() {
		return _fieldWidth;
	}


	/** 
	 * Set the field width 
	 * @param fieldWidth field width of right justified text to display
	 */
	public void setFieldWidth( final int fieldWidth ) {
		_fieldWidth = fieldWidth;
	}


	/** Implement the abstract format method by delegating to an internal number format */
	public StringBuffer format( final double number, final StringBuffer inputBuffer, final FieldPosition position ) {
		// get the absolute value
		final double absValue = Math.abs( number );

		// if the absolute value is between 1 inclusive and 10 exclusive, we can use a simple format otherwise we must use exponential notation
		final StringBuffer buffer;
		if ( absValue >= 1.0 && absValue < 10.0 ) {
			buffer = SIMPLE_FORMAT.format( number, inputBuffer, position );
		} else {
			buffer = EXPONENTIAL_FORMAT.format( number, inputBuffer, position );
		}

		// pad with spaces to the left of the number for a total width matching the field width
		final int spaces = _fieldWidth - buffer.length();
		if ( spaces > 0 ) {
			for( int space = 0 ; space < spaces ; space++ ) {
				buffer.insert( 0, " " );
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
	 * Append the formatted values to the output
	 * @param output the output to which to append the formatted values
	 * @param separator the characters to insert between each formatted value (e.g. this could be a comma)
	 * @param values the values to format and output
	 */
	public void appendTo( final Appendable output, final CharSequence separator,  final double ... values ) throws IOException {
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
	 * Print the formatted values
	 * @param separator the characters to insert between each formatted value (e.g. this could be a comma)
	 * @param terminator the characters to append to the end of the line of formatted values (e.g. this could be "\n")
	 * @param values the values to format and output
	 */
	final public void print( final CharSequence separator, final CharSequence terminator, final double ... values ) {
		try {
			appendTo( System.out, separator, values );
			System.out.append( terminator );
		}
		catch( IOException exception ) {
			System.err.println( "Error printing formatted values to the standard output." );
			exception.printStackTrace();
		}
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


