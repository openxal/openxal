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
public class TestJSONDecoding {
    @Test
    public void testNullDecoding() {
        Assert.assertTrue( null == JSONDecoder.decode( "null" ) );
    }
    
    
    @Test
    public void testDoubleDecoding() {
        checkValueEquality( 5.3 );
        checkValueEquality( 0.0 );
        checkValueEquality( -100.0 );
        checkValueEquality( -17.2976 );
        checkValueEquality( -32.698E53 );
        checkValueEquality( 7.5E-102 );
    }
    
    
    @Test
    public void testBooleanDecoding() {
        checkValueEquality( true );
        checkValueEquality( false );
    }
    
    
    @Test
    public void testStringDecoding() {
        checkStringEquality( "Hello, World", "\"Hello, World\"" );
        checkStringEquality( "String with an \"internal\" string.", "\"String with an \\\"internal\\\" string.\"" );
    }
    
    
    /** check whether the decoder can decode values */
    static private <DataType> void checkValueEquality( final DataType controlValue ) {
        final Object testValue = JSONDecoder.decode( String.valueOf( controlValue ) );
        assertEquality( controlValue, testValue );
    }
    
    
    /** check whether the decoder can decode strings */
    static private void checkStringEquality( final String controlValue, final String testCoding ) {
        final Object testValue = JSONDecoder.decode( testCoding );
        assertEquality( controlValue, testValue );
    }
    
    
    /** Assert whether the control value equals the test value */
    static private void assertEquality( final Object controlValue, final Object testValue ) {
        Assert.assertTrue( controlValue.equals( testValue ) );
    }
}