//
//  TestJSONDecoding.java
//  xal
//
//  Created by Tom Pelaia on 2/17/2011.
//  Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.text;

import org.junit.*;


/** test the ScientificNumberFormat class */
public class TestScientificNumberFormat {
	@Test
	public void testPositiveSimpleFormats() {
		assertFormat( "4", 1, 4.32 );
		assertFormat( "-5", 1, -5.13 );
		assertFormat( "3.1416", 5, 3.14159265 );
	}


	@Test
	public void testNegativeSimpleFormats() {
		assertFormat( "-3.1416", 5, -3.14159265 );
	}


	@Test
	public void testPositiveExponentialFormats() {
		assertFormat( "1.000E1", 4, 10.0 );
		assertFormat( "3.00E2", 3, 300.0 );
		assertFormat( "5.24E250", 3, 5.2395E+250 );
		assertFormat( "1.000E-1", 4, 0.1 );
		assertFormat( "9.900E-1", 4, 0.99 );
	}


	@Test
	public void testNegativeExponentialFormats() {
		assertFormat( "-1.000E1", 4, -10.0 );
		assertFormat( "-1.000E-1", 4, -0.1 );
		assertFormat( "-9.900E-1", 4, -0.99 );
		assertFormat( "-3.00E2", 3, -300.0 );
		assertFormat( "-5.2395E250", 5, -5.2395E+250 );
		assertFormat( "-5.2395E-250", 5, -5.2395E-250 );
	}


	@Test
	public void testPositiveSimpleFixedFormats() {
		assertFixedWidthFormat( "    4", 1, 5, 4.32 );
		assertFixedWidthFormat( "   -5", 1, 5, -5.13 );
		assertFixedWidthFormat( "    3.1416", 5, 10, 3.14159265 );
	}


	@Test
	public void testNegativeSimpleFixedFormats() {
		assertFixedWidthFormat( "   -3.1416", 5, 10, -3.14159265 );
	}


	@Test
	public void testPositiveExponentialFixedFormats() {
		assertFixedWidthFormat( "   1.000E1", 4, 10, 10.0 );
		assertFixedWidthFormat( "    3.00E2", 3, 10, 300.0 );
		assertFixedWidthFormat( "  5.24E250", 3, 10, 5.2395E+250 );
		assertFixedWidthFormat( "  1.000E-1", 4, 10, 0.1 );
		assertFixedWidthFormat( "  9.900E-1", 4, 10, 0.99 );
	}


	@Test
	public void testNegativeExponentialFixedFormats() {
		assertFixedWidthFormat( "  -1.000E1", 4, 10, -10.0 );
		assertFixedWidthFormat( " -1.000E-1", 4, 10, -0.1 );
		assertFixedWidthFormat( " -9.900E-1", 4, 10, -0.99 );
		assertFixedWidthFormat( "   -3.00E2", 3, 10, -300.0 );
		assertFixedWidthFormat( "  -5.2395E250", 5, 13, -5.2395E+250 );
		assertFixedWidthFormat( " -5.2395E-250", 5, 13, -5.2395E-250 );
	}


	@Test
	public void testRoundingFixedFormats() {
		assertFixedWidthFormat( "    1.49", 3, 8, 1.494999 );
		assertFixedWidthFormat( "    1.50", 3, 8, 1.495 );
	}


	@Test
	public void testFormattedFixedOutput() {
		assertFixedWidthFormattedOutput( "      1.49,      3.14,     -2.60,     -5.00,    5.50E3,   7.30E17, -1.24E-19", 3, 10, ",",   1.494999, 3.14159, -2.6, -5, 5496.48, 7.298534E17, -1.2359E-19 );
	}


	/** Assert whether the fixed width formatted value matches the specified reference */
	static private void assertFixedWidthFormat( final String reference, final int significantDigits, final int width, final double value ) {
		final ScientificNumberFormat format = new ScientificNumberFormat( significantDigits, width );
		final String output = format.format( value );
		Assert.assertEquals( "Failed fixed width format equality for number: " + value + " with output: " + output + " of length: " + output.length(), reference, output );
	}


	/** Assert whether the formatted value matches the specified reference */
	static private void assertFormat( final String reference, final int significantDigits, final double value ) {
		final ScientificNumberFormat format = new ScientificNumberFormat( significantDigits );
		final String output = format.format( value );
		Assert.assertEquals( "Failed format equality for number: " + value + " with output: " + output + " of length: " + output.length(), reference, output );
	}


	/** Assert whether the fixed width formatted output matches the specified reference */
	static private void assertFixedWidthFormattedOutput( final String reference, final int significantDigits, final int width, final CharSequence separator, final double ... values ) {
		final ScientificNumberFormat format = new ScientificNumberFormat( significantDigits, width );
		final StringBuffer buffer = new StringBuffer();
		format.appendTo( buffer, separator, values );
		final String output = buffer.toString();
		Assert.assertEquals( "Failed fixed width formatted output equality for values with output >>" + output + "<< and expected result of >>" + reference + "<<", reference, output );
	}
}