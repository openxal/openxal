/*
 * DataAdaptor.java
 *
 * Created on February 11, 2002, 2:26 PM
 */

package xal.tools.data;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * DataAdaptor is a generic interface to an external data source.  A specific
 * data adaptor (e.g. XmlDataAdaptor) will implement this interface to 
 * advertise its data in a generic way for reading and writing to a specific
 * data source (e.g. XML file).  DataAdaptor is intended to be a generic
 * wrapper for specific data adaptors.  Data adaptors do not have any knowledge
 * about the objects that read and write data.  Such data receivers and writers
 * must implement the DataListener interface.
 *
 * A DataAdaptor instance is generated for each node in a hierarchical data
 * tree and advertises information about the node and provides accessors
 * to its children.  Information about a node is in the form of attributes
 * which represent key/value pairs of primitive data.
 *
 * @author  tap
 */
public interface DataAdaptor {
    /** name for the particular node in the data tree */
    public String name();
    
    
    /** returns true iff the node has the specified attribute */
    public boolean hasAttribute( String attribute );
    
    /** string value associated with the specified attribute */
    public String stringValue( String attribute );
    
    /** double value associated with the specified attribute */
    public double doubleValue( String attribute );
	
    /** long value associated with the specified attribute */
    public long longValue( String attribute );
    
    /** integer value associated with the specified attribute */
    public int intValue( String attribute );
    
    /** boolean value associated with the specified attribute */
    public boolean booleanValue( String attribute );
      
    /**
     * Returns the value of an attribute as an array of doubles.  
     *
     * @param strAttr   the attribute name
     * 
     * @return  Array of double values,
     *          a <code>null</code> value is returned if the value string is empty.
     * 
     * @since  Mar 12, 2010
     * @author Christopher K. Allen
     */
    public double[] doubleArrayValue(final String strAttr);

        
    /** set the value of the specified attribute to the specified value */
    public void setValue( String attribute, String value );

    /** set the value of the specified attribute to the specified value */
    public void setValue( String attribute, double value );

    /** set the value of the specified attribute to the specified value */
    public void setValue( String attribute, long value );

    /** set the value of the specified attribute to the specified value */    
    public void setValue( String attribute, int value );

    /** set the value of the specified attribute to the specified value */    
    public void setValue( String attribute, boolean value );
    
    /** set the value of the specified attribute to the specified value */
    public void setValue( String attribute, Object value );
    
    /**
     * Stores the value of the given <code>double[]</code> object
     * in the data adaptor backing store.
     *
     * @param strAttr   attribute name
     * @param arrVal    attribute value
     * 
     * @since  Mar 11, 2010
     * @author Christopher K. Allen
     */
    public void setValue(String strAttr, double[] arrVal);
    
    /** return the array of all attributes */
    public String[] attributes();
    
    
    /** return the number of child node adaptors */
    public int nodeCount();
    
    /** return all child adaptors */
    public List<DataAdaptor> childAdaptors();
    
    /** return all child adaptors of the specified node name */
    public List<DataAdaptor> childAdaptors( String label );
    
    /** return an iterator of all child adaptors */
    public Iterator<DataAdaptor> childAdaptorIterator();
    
    /** return an iterator of all child adaptors of the specified name */
    public Iterator<DataAdaptor> childAdaptorIterator( String label );
    
    /** Convenience method to get a single child adaptor when only one is expected */
    public DataAdaptor childAdaptor( String label );
    
    
    /** Create an new empty child adaptor with label */
    public DataAdaptor createChild( String label );
    
    
    /** write the listener as a new node and append it to the data tree */
    public void writeNode( DataListener listener );
	
	
    /** 
     * Write the collection of listeners to new nodes and append them to the data tree.
	 * @param nodes the nodes to write
     */
    public void writeNodes( Collection<? extends DataListener> nodes );
}

