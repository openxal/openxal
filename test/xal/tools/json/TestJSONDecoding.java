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
    
    
    @Test
    public void testCodingDecoding() {
        checkCodingDecoding( null );
        checkCodingDecoding( 56.4 );
        checkCodingDecoding( true );
        checkCodingDecoding( false );
        checkCodingDecoding( "Hello, World" );
        
        final List<Object> simpleList = new ArrayList<Object>();
        simpleList.add( "Hello" );
        simpleList.add( "World" );
        simpleList.add( true );
        simpleList.add( false );
        simpleList.add( null );
        simpleList.add( "String with \"embedded\" string." );
        simpleList.add( new Double( -32.7 )  );
        checkCodingDecoding( simpleList );
        
        final Map<String,Object> simpleMap = new HashMap<String,Object>();
        simpleMap.put( "info", null );
        simpleMap.put( "x", 41.8 );
        simpleMap.put( "y", -2.6 );
        simpleMap.put( "comment", "Just a point" );
        checkCodingDecoding( simpleMap );
        
        final List<Object> compoundList = new ArrayList<Object>();
        compoundList.add( true );
        compoundList.add( simpleList );
        compoundList.add( simpleMap );
        compoundList.add( "Thing" );
        compoundList.add( 2.5 );
        checkCodingDecoding( compoundList );
        
        final Map<String,Object> compoundMap = new HashMap<String,Object>();
        compoundMap.put( "z", 23.6 );
        compoundMap.put( "ready", true );
        compoundMap.put( "on", false );
        compoundMap.put( "simple map", simpleMap );
        compoundMap.put( "simple list", simpleList );
        checkCodingDecoding( compoundMap );
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
    
    
    /** check whether a decoded encoding matches the original value */
    static private void checkCodingDecoding( final Object controlValue ) {
        final String coding = JSONCoder.encode( controlValue );
        final Object testValue = JSONDecoder.decode( coding );
        assertEquality( controlValue, testValue );
    }
    
    
    /** Assert whether the control value equals the test value */
    static private void assertEquality( final Object controlValue, final Object testValue ) {
        Assert.assertTrue( controlValue == testValue || controlValue.equals( testValue ) );
    }
}