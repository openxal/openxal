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
		assertFormat( "    3.1416", 5, 10, 3.14159265 );
	}


	@Test
	public void testNegativeSimpleFormats() {
		assertFormat( "   -3.1416", 5, 10, -3.14159265 );
	}


	@Test
	public void testPositiveExponentialFormats() {
		assertFormat( "   1.000E1", 4, 10, 10.0 );
		assertFormat( "    3.00E2", 3, 10, 300.0 );
		assertFormat( "  5.24E250", 3, 10, 5.2395E+250 );
		assertFormat( "  1.000E-1", 4, 10, 0.1 );
		assertFormat( "  9.900E-1", 4, 10, 0.99 );
	}


	@Test
	public void testNegativeExponentialFormats() {
		assertFormat( "  -1.000E1", 4, 10, -10.0 );
		assertFormat( " -1.000E-1", 4, 10, -0.1 );
		assertFormat( " -9.900E-1", 4, 10, -0.99 );
		assertFormat( "   -3.00E2", 3, 10, -300.0 );
		assertFormat( "  -5.2395E250", 5, 13, -5.2395E+250 );
		assertFormat( " -5.2395E-250", 5, 13, -5.2395E-250 );
	}


	@Test
	public void testRoundingFormats() {
		assertFormat( "    1.49", 3, 8, 1.494999 );
		assertFormat( "    1.50", 3, 8, 1.495 );
	}


	/** Assert whether the formatted output matches the specified reference */
	static private void assertFormat( final String reference, final int significantDigits, final int width, final double value ) {
		final ScientificNumberFormat format = new ScientificNumberFormat( significantDigits, width );
		final String output = format.format( value );
		Assert.assertEquals( "Failed format equality for number: " + value + " with output: " + output + " of length: " + output.length(), reference, output );
	}
}