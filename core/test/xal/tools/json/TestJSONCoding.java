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
import java.lang.reflect.Array;
import java.io.*;


/** test the complex number class */
public class TestJSONCoding {
    @Test
    public void testNullEncoding() {
        Assert.assertTrue( "null" == JSONCoder.encode( (Object)null ) );
    }
    
    
    @Test
    public void testDoubleEncoding() {
        checkEncodingEquality( -5.3E5 );
        checkEncodingEquality( -17.8 );
        checkEncodingEquality( 5.3 );
        checkEncodingEquality( 2.5E17 );
        checkEncodingEquality( 6.2E-23 );
    }
    
    
    @Test
    public void testLongEncoding() {
        checkEncodingEquality( 0L );
        checkEncodingEquality( -100L );
        checkEncodingEquality( 100L );
        checkEncodingEquality( 127444986353848L );
        checkEncodingEquality( -142015908198098L );
    }
    
    
    @Test
    public void testBooleanEncoding() {
        checkEncodingEquality( true );
        checkEncodingEquality( false );
    }
    
    
    @Test
    public void testStringEncoding() {
        checkEncodingEquality( "\"Hello, World\"", "Hello, World" );
        checkEncodingEquality( "\"String with an \\\"internal\\\" string.\"", "String with an \"internal\" string." );
    }
    
    
    @Test
    public void testNullDecoding() {
        Assert.assertTrue( null == JSONCoder.decode( "null" ) );
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
    public void testLongDecoding() {
        checkValueEquality( 5L );
        checkValueEquality( 0L );
        checkValueEquality( -100L );
        checkValueEquality( -17L );
        checkValueEquality( 3268249075299837591L );
        checkValueEquality( -751510751908751578L );
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
    public void testNumericEncodingDecoding() {
        checkEncodingDecoding( (byte)89 );
        checkEncodingDecoding( (short)7456 );
        checkEncodingDecoding( 509674 );
        checkEncodingDecoding( 325822043801L );
        checkEncodingDecoding( (float)56.4 );
        checkEncodingDecoding( 56.4 );
    }
    
    
    @Test
    public void testBooleanEncodingDecoding() {
        checkEncodingDecoding( true );
        checkEncodingDecoding( false );
    }
    
    
    @Test
    public void testNullEncodingDecoding() {
        checkEncodingDecoding( null );
    }
    
    
    @Test
    public void testStringEncodingDecoding() {
        checkEncodingDecoding( "Hello, World" );
    }
    
    
    @Test
    public void testArrayEncodingDecoding() {
        checkArrayEncodingDecoding( new double[] { 4.78, Math.PI, -17.6, 5.4E23, 8.719E-32 } );
        checkArrayEncodingDecoding( new String[] { "Hello", "World", "This is just a test!" } );
        checkArrayEncodingDecoding( new int[] { 2, 3, 5, 7, 11 } );
    }
    
    
    @Test
    public void testDateEncodingDecoding() {
        checkEncodingDecoding( new Date() );
    }
    
    
    @Test
    // arrays of a common, extended component type are efficiently encoded into an array of representations and a single type key
    public void testArrayCommonExtensionEncodingDecoding() {
        checkArrayEncodingDecoding( new Date[] { new Date(), new Date( new Date().getTime() - 1000 ), new Date( new Date().getTime() + 1000 ) } );
        checkArrayEncodingDecoding( new Byte[] { 105, 74, 43, 45 });
    }
    
    
    @Test
    public void testCharacterEncodingDecoding() {
        checkEncodingDecoding( 'a' );
    }
    
    
    @Test
    public void testListEncodingDecoding() {        
        final List<Object> simpleList = new ArrayList<Object>();
        simpleList.add( "Hello" );
        simpleList.add( "World" );
        simpleList.add( true );
        simpleList.add( false );
        simpleList.add( null );
        simpleList.add( "String with \"embedded\" string." );
        simpleList.add( new Double( -32.7 )  );
        checkEncodingDecoding( simpleList );
    }
    
    
    @Test
    public void testVectorEncodingDecoding() {        
        final Vector<Object> vector = new Vector<Object>();
        vector.add( "Hello" );
        vector.add( "World" );
        vector.add( true );
        vector.add( false );
        vector.add( null );
        vector.add( "String with \"embedded\" string." );
        vector.add( 745.89  );
        checkEncodingDecoding( vector );
    }
    
    
    @Test
    public void testMapEncodingDecoding() {        
        final Map<String,Object> simpleMap = new HashMap<String,Object>();
        simpleMap.put( "info", null );
        simpleMap.put( "x", 41.8 );
        simpleMap.put( "y", -2.6 );
        simpleMap.put( "comment", "Just a point" );
        checkEncodingDecoding( simpleMap );
    }
    
    
    @Test
    public void testTableEncodingDecoding() {        
        final Map<String,Object> simpleMap = new Hashtable<String,Object>();
        simpleMap.put( "x", 41.8 );
        simpleMap.put( "y", -2.6 );
        simpleMap.put( "text", "This is just a test string..." );
        checkEncodingDecoding( simpleMap );
    }
    
    
    @Test
    public void testRuntimeExceptionEncodingDecoding() {
        try {
            final Object nullObject = null;
            nullObject.toString();  // should always throw an exception
        }
        catch ( Exception exception ) {
            final RuntimeException controlValue = new RuntimeException( exception );
            final String coding = JSONCoder.encode( controlValue );
            final RuntimeException testValue = (RuntimeException)JSONCoder.decode( coding );
            assertEquality( testValue.getMessage(), controlValue.getMessage() );
            
            final StackTraceElement[] controlStackTrace = controlValue.getStackTrace();
            final StackTraceElement[] testStackTrace = testValue.getStackTrace();
            Assert.assertTrue( testStackTrace.length == controlStackTrace.length );
            
            for ( int index = 0 ; index < controlStackTrace.length ; index++ ) {
                assertEquality( testStackTrace[index], controlStackTrace[index] );
            }            
        }
    }
    
    
    @Test
    public void testCompoundEncodingDecoding() {        
        final List<Object> simpleList = new ArrayList<Object>();
        simpleList.add( "Hello" );
        simpleList.add( "World" );
        simpleList.add( true );
        simpleList.add( false );
        simpleList.add( null );
        simpleList.add( "String with \"embedded\" string." );
        simpleList.add( new Double( -32.7 )  );
        
        final Map<String,Object> simpleMap = new HashMap<String,Object>();
        simpleMap.put( "info", null );
        simpleMap.put( "x", 41.8 );
        simpleMap.put( "y", -2.6 );
        simpleMap.put( "comment", "Just a point" );
        
        final List<Object> compoundList = new ArrayList<Object>();
        compoundList.add( true );
        compoundList.add( simpleList );
        compoundList.add( simpleMap );
        compoundList.add( "Thing" );
        compoundList.add( 2.5 );
        checkEncodingDecoding( compoundList );
        
        final Map<String,Object> compoundMap = new HashMap<String,Object>();
        compoundMap.put( "z", 23.6 );
        compoundMap.put( "ready", true );
        compoundMap.put( "on", false );
        compoundMap.put( "simple map", simpleMap );
        compoundMap.put( "simple list", simpleList );
        checkEncodingDecoding( compoundMap );
    }
    
    
    @Test
    @SuppressWarnings( "unchecked" )
    public void testReferenceEncodingDecoding() {
        final List<Object> sharedList = new ArrayList<Object>();    // list to be shared using references
        sharedList.add( "Knoxville" );
        sharedList.add( "Oak Ridge" );
        sharedList.add( "Chattanooga" );
        sharedList.add( "Nashville" );
        sharedList.add( "Memphis" );
        
        final List<Object> otherList = new ArrayList<Object>( sharedList );     // list is equal to shared list but different instance
        
        final Map<String,Object> testMap = new HashMap<String,Object>();
        testMap.put( "share_0", sharedList );
        testMap.put( "other", otherList );
        testMap.put( "share_1", sharedList );
                
        final String json = JSONCoder.encode( testMap );
        
        final Map<String,Object> control = (Map<String,Object>)JSONCoder.decode( json );
        assertEquality( testMap, control );     // verify that we have regenerated the original map
                
        final Object shared_0 = control.get( "share_0" );
        final Object other = control.get( "other" );
        final Object shared_1 = control.get( "share_1" );
        Assert.assertTrue( shared_0 == shared_1 );      // verify that references are preserved (objects that share the same instance prior to encoding do so when regenerated)
        Assert.assertTrue( shared_0 != other );         // verify that different instances that are equal prior to encoding do not share the same instance after regeneration
    }
    
    
    /** check whether the coder can encode values */
    static private <DataType> void checkEncodingEquality( final DataType value ) {
        final String controlCoding = String.valueOf( value );
        checkEncodingEquality( controlCoding, value );
    }
    
    
    /** check whether the coder can encode values */
    static private <DataType> void checkEncodingEquality( final String controlCoding, final DataType value ) {
        final String testCoding = JSONCoder.encode( value );
        assertEquality( controlCoding, testCoding );
    }
    
    /** check whether the decoder can decode values */
    static private <DataType> void checkValueEquality( final DataType controlValue ) {
        final Object testValue = JSONCoder.decode( String.valueOf( controlValue ) );
        assertEquality( controlValue, testValue );
    }
    
    
    /** check whether the decoder can decode strings */
    static private void checkStringEquality( final String controlValue, final String testCoding ) {
        final Object testValue = JSONCoder.decode( testCoding );
        assertEquality( controlValue, testValue );
    }
    
    
    /** check whether a decoded encoding matches the original value */
    static private void checkEncodingDecoding( final Object controlValue ) {
        final String coding = JSONCoder.encode( controlValue );
        final Object testValue = JSONCoder.decode( coding );
        assertEquality( controlValue, testValue );
    }
    
    
    /** check whether a decoded encoding matches the original value */
    static private void checkArrayEncodingDecoding( final Object controlArray ) {
        final String coding = JSONCoder.encode( controlArray );
        final Object testArray = JSONCoder.decode( coding );
        
        final int count = Array.getLength( controlArray );
        assertEquality( count, Array.getLength( testArray ) );
        for ( int index = 0 ; index < count ; index++ ) {
            assertEquality( Array.get( controlArray, index ), Array.get( testArray, index ) );
        }
    }
    
    
    /** Assert whether the control value equals the test value */
    static private void assertEquality( final Object controlValue, final Object testValue ) {
        Assert.assertTrue( controlValue == testValue || controlValue.equals( testValue ) );
    }
}