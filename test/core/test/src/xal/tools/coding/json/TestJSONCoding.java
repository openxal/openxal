//
//  TestJSONDecoding.java
//  xal
//
//  Created by Tom Pelaia on 2/17/2011.
//  Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.coding.json;

import org.junit.*;
import java.util.*;
import java.lang.reflect.Array;
import java.io.*;


/** test the complex number class */
public class TestJSONCoding {
    @Test
    public void testNullEncoding() {
        Assert.assertTrue( "null".equals( JSONCoder.defaultEncode( (Object)null ) ) );
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
        Assert.assertTrue( null == JSONCoder.defaultDecode( "null" ) );
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
	public void testNumericDecoding() {
		checkDecodingEquality( "{\"__XALTYPE\": \"java.lang.Integer\", \"value\": 2}", 2 );
		checkDecodingEquality( "{\"__XALTYPE\": \"int\", \"value\": 2}", 2 );
		checkDecodingEquality( "{\"__XALTYPE\": \"long\", \"value\": 2}", 2L );
		checkDecodingEquality( "{\"__XALTYPE\": \"java.lang.Double\", \"value\": 3.14159}", 3.14159 );
		checkDecodingEquality( "{\"__XALTYPE\": \"double\", \"value\": 3.14159}", 3.14159 );
		checkDecodingEquality( "{\"__XALTYPE\": \"java.lang.Double\", \"value\": 35}", 35.0 );
		checkDecodingEquality( "{\"__XALTYPE\": \"double\", \"value\": 35}", 35.0 );
	}


    @Test
    public void testNumericEncodingDecoding() {
        checkEncodingDecoding( (byte)89 );
        checkEncodingDecoding( (short)7456 );
        checkEncodingDecoding( 509674 );
        checkEncodingDecoding( 325822043801L );
        checkEncodingDecoding( (float)56.4 );
        checkEncodingDecoding( 56.4 );
		checkEncodingDecoding( -5.3E5 );
		checkEncodingDecoding( -17.8 );
		checkEncodingDecoding( 5.3 );
		checkEncodingDecoding( 2.5E17 );
		checkEncodingDecoding( 6.2E-23 );
		checkEncodingDecoding( 5.3 );
		checkEncodingDecoding( 0.0 );
		checkEncodingDecoding( -100.0 );
		checkEncodingDecoding( -17.2976 );
		checkEncodingDecoding( -32.698E53 );
		checkEncodingDecoding( 7.5E-102 );
		checkEncodingDecoding( 0L );
		checkEncodingDecoding( 5L );
		checkEncodingDecoding( -17L );
		checkEncodingDecoding( -100L );
		checkEncodingDecoding( 100L );
		checkEncodingDecoding( 127444986353848L );
		checkEncodingDecoding( -142015908198098L );
		checkEncodingDecoding( 3268249075299837591L );
		checkEncodingDecoding( -751510751908751578L );
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
		checkArrayEncodingDecoding( new Object[] { "Hello", "World" } );    // Object array with standard types
		checkArrayEncodingDecoding( new Object[] {} );		// Empty object array
		checkArrayEncodingDecoding( new Object[] { "Hello, World", 2.0 } );    // Object array with standard types
        checkArrayEncodingDecoding( new Object[] { "Hello, World", 2.0, 5000L } );    // Object array with standard types
        checkArrayEncodingDecoding( new Object[] { "Hello, World", 25, new Date() } );    // Object array with extended types
        checkArrayEncodingDecoding( new String[] { "Hello", "World", "This is just a test!" } );    // standard type array
        checkArrayEncodingDecoding( new double[] { 4.78, Math.PI, -17.6, 5.4E23, 8.719E-32 } );     // standard primitive array
        checkArrayEncodingDecoding( new int[] { 2, 3, 5, 7, 11 } );         // extended type primitive array
        checkArrayEncodingDecoding( new Byte[] { 105, 74, 43, 45 });        // extended type primitive array using wrapper
        checkArrayEncodingDecoding( new Date[] { new Date(), new Date( new Date().getTime() - 1000 ), new Date( new Date().getTime() + 1000 ) } );      // extended type array
    }
    
    
    @Test
    @SuppressWarnings( "unchecked" )    // need to cast decoded object
    public void testMultidimensionalArrayEncodingDecoding() {
        final int[][] controlArray = { { 2, 3, 5 }, { 7, 11, 13, 17, 19 } };
        final String coding = JSONCoder.defaultEncode( controlArray );
        final int[][] testArray = (int[][])JSONCoder.defaultDecode( coding );
        for ( int row = 0 ; row < controlArray.length ; row++ ) {
            final int[] controlColumnArray = controlArray[row];
            final int[] testColumnArray = testArray[row];
            for ( int column = 0 ; column < controlColumnArray.length ; column++ ) {
                final int controlItem = controlColumnArray[ column ];
                final int testItem = testColumnArray[ column ];
                assertEquality( controlItem, testItem );
            }
        }
    }
    
    
    @Test
    public void testDateEncodingDecoding() {
        checkEncodingDecoding( new Date() );
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

		// test empty dictionary
		checkEncodingDecoding( new HashMap<String,Object>() );
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
    public void testSerializationEncodingDecoding() {
        checkEncodingDecoding( new java.math.BigInteger( "123456789012345678901234567890" ) );  // BigInteger is Serializable and not a directly supported type
    }
    
    
    @Test
    public void testRuntimeExceptionEncodingDecoding() {
        try {
            final Object nullObject = null;
            nullObject.toString();  // should always throw an exception
        }
        catch ( Exception exception ) {
            final RuntimeException controlValue = new RuntimeException( exception );
            final String coding = JSONCoder.defaultEncode( controlValue );
            final RuntimeException testValue = (RuntimeException)JSONCoder.defaultDecode( coding );
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
                
        final String json = JSONCoder.defaultEncode( testMap );
        
        final Map<String,Object> control = (Map<String,Object>)JSONCoder.defaultDecode( json );
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
        final String testCoding = JSONCoder.defaultEncode( value );
        assertEquality( controlCoding, testCoding );
    }
    
    /** check whether the decoder can decode values */
    static private <DataType> void checkValueEquality( final DataType controlValue ) {
        final Object testValue = JSONCoder.defaultDecode( String.valueOf( controlValue ) );
        assertEquality( controlValue, testValue );
    }
    
    
    /** check whether the decoder can decode strings */
    static private void checkStringEquality( final String controlValue, final String testCoding ) {
        final Object testValue = JSONCoder.defaultDecode( testCoding );
        assertEquality( controlValue, testValue );
    }
    
    
    /** check whether a decoded encoding matches the original value */
    static private void checkEncodingDecoding( final Object controlValue ) {
        final String coding = JSONCoder.defaultEncode( controlValue );
        final Object testValue = JSONCoder.defaultDecode( coding );
        assertEquality( controlValue, testValue );
    }
    
    
    /** check whether a decoded encoding matches the original value */
    static private void checkArrayEncodingDecoding( final Object controlArray ) {
        final String coding = JSONCoder.defaultEncode( controlArray );
        final Object testArray = JSONCoder.defaultDecode( coding );
        
        final int count = Array.getLength( controlArray );
        assertEquality( count, Array.getLength( testArray ) );
        for ( int index = 0 ; index < count ; index++ ) {
            assertEquality( Array.get( controlArray, index ), Array.get( testArray, index ) );
        }
    }


	/** check whether the coder can decode the json coding to match the specified control value */
	static private <DataType> void checkDecodingEquality( final String coding, final DataType controlValue ) {
		final Object testValue = JSONCoder.defaultDecode( coding );
		assertEquality( controlValue, testValue );
	}


    /** Assert whether the control value equals the test value */
    static private void assertEquality( final Object controlValue, final Object testValue ) {
        Assert.assertTrue( controlValue == testValue || controlValue.equals( testValue ) );
    }
}