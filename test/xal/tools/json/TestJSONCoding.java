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
    
    
    @Test
    public void testArrayCoding() {
        assertEquality( "[1.2, -17.6, 5.4E23, 1.2E-6]", JSONCoder.encode( new Double[] { 1.2, -17.6, 5.4E23, 1.2E-6 } ) );
        checkCodingEquality( "[\"Hello\", \"World\"]", new String[] { "Hello", "World" } );
        checkCodingEquality( "[\"Testing\", \"\\\"internal\\\" string.\"]", new String[] { "Testing", "\"internal\" string." } );
    }
    
    
    @Test
    public void testListCoding() {
        final List testList = new ArrayList();
        testList.add( "Hello" );
        testList.add( "World" );
        testList.add( 1.5 );
        testList.add( 2.7 );
        testList.add( -324.2 );
        assertEquality( "[\"Hello\", \"World\", 1.5, 2.7, -324.2]", JSONCoder.encode( testList ) );
    }
    
    
    @Test
    public void testMapCoding() {
        final Map testMap = new TreeMap();  // need to guarantee ordering so we can test against our control
        testMap.put( "message", "Hello, World" );
        testMap.put( "value", 5.4 );
        testMap.put( "cities", new String[] { "Knoxville", "Oak Ridge" } );
        // make sure the control coding lists keys in alphabetical ordering (corresponding to TreeMap)
        final String controlCoding = "{\"cities\": [\"Knoxville\", \"Oak Ridge\"], \"message\": \"Hello, World\", \"value\": 5.4}";
//        System.out.println( controlCoding );
//        System.out.println( JSONCoder.encode( testMap ) );
        assertEquality( controlCoding, JSONCoder.encode( testMap ) );
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