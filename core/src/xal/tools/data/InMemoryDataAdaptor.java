//
// InMemoryDataAdaptor.java: Source file for 'InMemoryDataAdaptor'
// Project xal
//
// Created by Tom Pelaia II on 5/24/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.data;

import java.util.*;


/** InMemoryDataAdaptor */
public class InMemoryDataAdaptor implements DataAdaptor {
    /** name for this adaptor */
    final private String NAME;
    
    /** hash map of properties keyed by attributes */
    final private Map<String,Object> PROPERTIES;
    
    /** map of subnode lists keyed by node name */
    final private Map<String,List<DataAdaptor>>SUBNODE_MAP;
    
    
	/** Constructor */
    public InMemoryDataAdaptor( final String name ) {
        NAME = name;
        PROPERTIES = new HashMap<String,Object>();
        SUBNODE_MAP = new HashMap<String,List<DataAdaptor>>();
    }
    
    
    /** name for the particular node in the data tree */
    public String name() {
        return NAME;
    }
    
    
    /** returns true iff the node has the specified attribute */
    public boolean hasAttribute( final String attribute ) {
        return PROPERTIES.containsKey( attribute );
    }
    
    
    /** string value associated with the specified attribute */
    public String stringValue( final String attribute ) {
    	Object value = PROPERTIES.get( attribute );
        return value != null ? value.toString() : null;
    }
    
    
    /** double value associated with the specified attribute */
    public double doubleValue( final String attribute ) {
        final Object rawValue = PROPERTIES.get( attribute );
        if ( rawValue instanceof Number ) {
            return ((Number)rawValue).doubleValue();
        }
        else if ( rawValue instanceof String ) {
            return Double.parseDouble( rawValue.toString() );
        }
        else {
            throw new NumberFormatException( "Invalid conversion to double from: " + rawValue );
        }
    }
    
	
    /** long value associated with the specified attribute */
    public long longValue( final String attribute ) {
        final Object rawValue = PROPERTIES.get( attribute );
        if ( rawValue instanceof Number ) {
            return ((Number)rawValue).longValue();
        }
        else if ( rawValue instanceof String ) {
            return Long.parseLong( rawValue.toString() );
        }
        else {
            throw new NumberFormatException( "Invalid conversion to long from: " + rawValue );
        }
    }
    
    
    /** integer value associated with the specified attribute */
    public int intValue( final String attribute ) {
        final Object rawValue = PROPERTIES.get( attribute );
        if ( rawValue instanceof Number ) {
            return ((Number)rawValue).intValue();
        }
        else if ( rawValue instanceof String ) {
            return Integer.parseInt( rawValue.toString() );
        }
        else {
            throw new NumberFormatException( "Invalid conversion to int from: " + rawValue );
        }
    }
    
    
    /** boolean value associated with the specified attribute */
    public boolean booleanValue( final String attribute ) {
        final Object rawValue = PROPERTIES.get( attribute );
        if ( rawValue instanceof Boolean ) {
            return ((Boolean)rawValue).booleanValue();
        }
        else if ( rawValue instanceof String ) {
            return Boolean.parseBoolean( rawValue.toString() );
        }
        else {
            throw new NumberFormatException( "Invalid conversion to boolean from: " + rawValue );
        }
    }
    
    
    /* Returns the value of an attribute as an array of doubles.  */
    public double[] doubleArray( final String attribute ) {
        throw new UnsupportedOperationException( "Arrays are not supported in the in memory adaptor." );
    }
    
    
    /** set the value of the specified attribute to the specified value */
    public void setValue( final String attribute, final String value ) {
        PROPERTIES.put( attribute, value );
    }
    
    
    /** set the value of the specified attribute to the specified value */
    public void setValue( final String attribute, final double value ) {
        PROPERTIES.put( attribute, value );
    }
    
    
    /** set the value of the specified attribute to the specified value */
    public void setValue( final String attribute, final long value ) {
        PROPERTIES.put( attribute, value );
    }
    
    
    /** set the value of the specified attribute to the specified value */    
    public void setValue( final String attribute, final int value ) {
        PROPERTIES.put( attribute, value );
    }
    
    
    /** set the value of the specified attribute to the specified value */    
    public void setValue( final String attribute, final boolean value ) {
        PROPERTIES.put( attribute, value );        
    }
    
    
    /** set the value of the specified attribute to the specified value */
    public void setValue( final String attribute, final Object value ) {
        PROPERTIES.put( attribute, value );        
    }
    
    
    /** Stores the value of the given <code>double[]</code> object in the data adaptor backing store. */
    public void setValue( final String attribute, final double[] value ) {
        throw new UnsupportedOperationException( "Arrays are not supported in the in memory adaptor." );
    }
    
    
    /** return the array of all attributes */
    public String[] attributes() {
        final Set<String> attributes = PROPERTIES.keySet();
        return attributes.toArray( new String[0] );
    }
    
    
    /** return the number of child node adaptors */
    public int nodeCount() {
        int count = 0;
        for ( final Collection<DataAdaptor> adaptors : SUBNODE_MAP.values() ) {
            count += adaptors.size();
        }
        return count;
    }
    
    
    /** return all child adaptors */
    public List<DataAdaptor> childAdaptors() {
        final List<DataAdaptor> subnodes = new ArrayList<DataAdaptor>();
        for ( final Collection<DataAdaptor> adaptors : SUBNODE_MAP.values() ) {
            subnodes.addAll( adaptors );
        }
        return subnodes;
    }
    
    
    /** return all child adaptors of the specified node name */
    public List<DataAdaptor> childAdaptors( final String label ) {
        return SUBNODE_MAP.get( label );
    }
    
    
    /** return an iterator of all child adaptors */
    public Iterator<DataAdaptor> childAdaptorIterator() {
        return childAdaptors().iterator();
    }
    
    
    /** return an iterator of all child adaptors of the specified name */
    public Iterator<DataAdaptor> childAdaptorIterator( final String label ) {
        return childAdaptors( label ).iterator();
    }
    
    
    /** Convenience method to get a single child adaptor when only one is expected */
    public DataAdaptor childAdaptor( final String label ) {
        final List<DataAdaptor> namedSubnodes = childAdaptors( label );
        return namedSubnodes.size() > 0 ? namedSubnodes.get( 0 ) : null;
    }
    
    
    /** Create an new empty child adaptor with label */
    public DataAdaptor createChild( final String label ) {
        final DataAdaptor child = new InMemoryDataAdaptor( label );
        List<DataAdaptor> subnodes = childAdaptors( label );
        if ( subnodes == null ) {
            subnodes = new ArrayList<DataAdaptor>();
            SUBNODE_MAP.put( label, subnodes );
        }
        subnodes.add( child );
        return child;
    }
    
    
    /** write the listener as a new node and append it to the data tree */
    public void writeNode( final DataListener listener ) {
        final String name = listener.dataLabel();
        final DataAdaptor adaptor = createChild( name );
        listener.write( adaptor );
    }
	
	
    /** 
     * Write the collection of listeners to new nodes and append them to the data tree.
	 * @param nodes the nodes to write
     */
    public void writeNodes( Collection<? extends DataListener> nodes ) {
        for ( final DataListener node : nodes ) {
            writeNode( node );
        }
    }
}
