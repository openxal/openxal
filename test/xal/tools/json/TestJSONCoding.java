//
//  TestJSONDecoding.java
//  xal
//
//  Created by Tom Pelaia on 2/17/2011.
//  Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.json;

import org.junit.*;
import java.util.*;
import java.io.*;


/** test the complex number class */
public class TestJSONCoding {
    @Test
    public void testNullCoding() {
        Assert.assertTrue( "null" == JSONCoder.encode( (Object)null ) );
    }
    
    
    @Test
    public void testDoubleCoding() {
        checkCodingEquality( -5.3E5 );
        checkCodingEquality( -17.8 );
        checkCodingEquality( 5.3 );
        checkCodingEquality( 2.5E17 );
        checkCodingEquality( 6.2E-23 );
    }
    
    
    @Test
    public void testBooleanCoding() {
        checkCodingEquality( true );
        checkCodingEquality( false );
    }
    
    
    @Test
    public void testStringCoding() {
        checkCodingEquality( "\"Hello, World\"", "Hello, World" );
        checkCodingEquality( "\"String with an \\\"internal\\\" string.\"", "String with an \"internal\" string." );
    }
    
    
    /** check whether the coder can encode values */
    static private <DataType> void checkCodingEquality( final DataType value ) {
        final String controlCoding = String.valueOf( value );
        checkCodingEquality( controlCoding, value );
    }
    
    
    /** check whether the coder can encode values */
    static private <DataType> void checkCodingEquality( final String controlCoding, final DataType value ) {
        final String testCoding = JSONCoder.encode( value );
        assertEquality( controlCoding, testCoding );
    }
    
    
    /** Assert whether the control value equals the test value */
    static private void assertEquality( final Object controlValue, final Object testValue ) {
        Assert.assertTrue( controlValue.equals( testValue ) );
    }
}