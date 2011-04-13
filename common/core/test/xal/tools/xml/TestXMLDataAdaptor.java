//
//  TestComplex.java
//  xal
//
//  Created by Tom Pelaia on 2/17/2011.
//  Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.xml;

import xal.tools.data.*;

import java.util.List;
import java.net.URL;

import org.junit.*;


/** test the complex number class */
public class TestXMLDataAdaptor {
    @Test
    public void testAttributeAccessors() {
        final XmlDataAdaptor documentAdaptor = XmlDataAdaptor.newEmptyDocumentAdaptor( "TestDoc", null );
        final IDataAdaptor modelAdaptor = documentAdaptor.createChild( "model" );
        
        final IDataAdaptor pointNode = modelAdaptor.createChild( "point" );
        final double x = 2.1;
        final double y = 3.4;        
        pointNode.setValue( "x", x );
        pointNode.setValue( "y", y );        
        Assert.assertTrue( x == pointNode.doubleValue( "x" ) );
        Assert.assertTrue( y == pointNode.doubleValue( "y" ) );
        
        final IDataAdaptor boolNode = modelAdaptor.createChild( "status" );
        final boolean good = true;
        final boolean bad = false;
        boolNode.setValue( "good", good );
        boolNode.setValue( "bad", bad );
        Assert.assertTrue( good == boolNode.booleanValue( "good" ) );
        Assert.assertTrue( bad == boolNode.booleanValue( "bad" ) );
        
        final double[] array = new double[] { 2.3, 4.7, 10.8, 5.2 };
        final IDataAdaptor waveAdaptor = modelAdaptor.createChild( "waveform" );
        waveAdaptor.setValue( "wave", array );
        final double[] waveform = waveAdaptor.doubleArray( "wave" );
        for ( int index = 0 ; index < array.length ; index++ ) {
            Assert.assertTrue( array[index] == waveform[index] );
        }
    }
    
    
    @Test
    public void testFileReading() {
        final URL documentURL = this.getClass().getResource( "SampleData.xml" );
        
        try {
            final IDataAdaptor documentAdaptor = XmlDataAdaptor.adaptorForUrl( documentURL, false );
            final IDataAdaptor bookAdaptor = documentAdaptor.childAdaptor( "book" );
            
            // read escaped characters
            final IDataAdaptor peopleAdaptor = bookAdaptor.childAdaptor( "people" );
            Assert.assertTrue( peopleAdaptor.stringValue( "comment" ).equals( "\"This & that\"" ) );
            
            // read array
            final double[] vec = peopleAdaptor.doubleArray( "vec" );
            Assert.assertTrue( vec.length == 3 );
            Assert.assertTrue( vec[0] == 1.2 );
            Assert.assertTrue( vec[1] == -12.477 );
            Assert.assertTrue( vec[2] == 7.9 );
            
            final List<IDataAdaptor> personAdaptors = peopleAdaptor.childAdaptors( "person" );
            
            // read primitive types
            final IDataAdaptor einsteinAdaptor = personAdaptors.get( 0 );
            Assert.assertTrue( einsteinAdaptor.stringValue( "firstName" ).equals( "Albert" ) );
            Assert.assertTrue( einsteinAdaptor.stringValue( "lastName" ).equals( "Einstein" ) );
            Assert.assertTrue( einsteinAdaptor.intValue( "birthYear" ) == 1879 );
            
            final IDataAdaptor fermiAdaptor = personAdaptors.get( 1 );
            Assert.assertTrue( fermiAdaptor.stringValue( "firstName" ).equals( "Enrico" ) );
            Assert.assertTrue( fermiAdaptor.stringValue( "lastName" ).equals( "Fermi" ) );
            Assert.assertTrue( fermiAdaptor.intValue( "birthYear" ) == 1901 );
        }
        catch ( Exception exception ) {
            Assert.assertTrue( false );
        }
    }
}