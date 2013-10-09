/*
 * DataAdaptor.java
 *
 * Created on February 11, 2002, 2:26 PM
 */

package xal.tools.data;

import java.util.Collection;
import java.util.List;


/**
 * <p>
 * <code>DataAdaptor</code> is a generic interface to an external data source.  
 * A specific data adaptor (e.g. <code>XmlDataAdaptor</code>) will implement this interface to 
 * advertise its data in a generic way for reading and writing to a specific
 * data source (e.g. XML file).  <code>DataAdaptor</code> is intended to be a generic
 * wrapper for specific data sources and sinks.  Data adaptors do not have any knowledge
 * about the objects that read and write data.  Such data receivers and writers
 * should implement the <code>{@link DataListener}</code> interface (or the 
 * <code>{@link xal.tools.data.IArchive}</code> interface for model persistence).
 * </p>
 * <p>
 * A <code>DataAdaptor</code> instance is generated for each node in a hierarchical data
 * tree and advertises information about the node and provides accessors
 * to its children.  Information about a node is in the form of attributes
 * which represent key/value pairs of primitive data.  The node also contains
 * a text string which represents the data node "content".
 * </p>
 * <p>
 * The general structure of the data should be tree based, analogous to that of
 * the XML DOM structure.  Data is distributed by nodes in a tree, each node having
 * a data label (see <code>{@link DataAdaptor#name()}</code>), attributes, and possibly some 
 * type of string content (see <code>DataAdaptor#getContent()</code> - <em>currently unimplimented</em>).
 * There are potentially child data nodes of each node (see <code>{@link DataAdaptor#createChild(String)}</code> 
 * and <code>{@link DataAdaptor#childAdaptor(String)}</code>).  
 * <p>
 * </p>
 * Any object that is able to read and write from <code>DataAdaptor</code> interfaces
 * can implement the <code>DataListener</code> interface.  The idea is that such an 
 * object should know, itself, how to load and save data from this interface, and expect
 * to receive the appropriate <code>DataAdaptor</code> object for doing so.  Finally, 
 * note that the <code>update(DataAdaptor)</code> and <code>write(DataAdaptor)</code> 
 * methods of the 
 * <code>IDataAware</code> interface expect <code>IDataAdaptors</code> in different
 * positions on the data tree.  This must be the case for proper logistical implementation.
 * The <code>update()</code> method expects to be synchronized to the current data node while
 * the <code>write()</code> method expects to be given the parent of the data node it is to
 * populate.  See the documentation for the <code>{@link DataListener}</code> interface for more
 * information.
 * 
 * @author  Tom Pelaia
 * @author  Christopher K. Allen
 * 
 */
public interface DataAdaptor {
    /** name for the particular node in the data tree */
    public String name();
    
    
    /** returns true iff the node has the specified attribute */
    public boolean hasAttribute( final String attribute );
    
    
    /** string value associated with the specified attribute */
    public String stringValue( final String attribute );
    
    
    /** double value associated with the specified attribute */
    public double doubleValue( final String attribute );
    
	
    /** long value associated with the specified attribute */
    public long longValue( final String attribute );
    
    
    /** integer value associated with the specified attribute */
    public int intValue( final String attribute );
    
    
    /** boolean value associated with the specified attribute */
    public boolean booleanValue( final String attribute );
    
    
    /**
     * Returns the value of an attribute as an array of doubles.  
     * @param attribute   the attribute name
     * @return  Array of double values, a <code>null</code> value is returned if the value string is empty.
     */
    public double[] doubleArray( final String attribute );
    
        
    /** set the value of the specified attribute to the specified value */
    public void setValue( final String attribute, final String value );
    

    /** set the value of the specified attribute to the specified value */
    public void setValue( final String attribute, final double value );
    

    /** set the value of the specified attribute to the specified value */
    public void setValue( final String attribute, final long value );

    
    /** set the value of the specified attribute to the specified value */    
    public void setValue( final String attribute, final int value );

    
    /** set the value of the specified attribute to the specified value */    
    public void setValue( final String attribute, final boolean value );
    
    
    /** set the value of the specified attribute to the specified value */
    public void setValue( final String attribute, final Object value );
    
    
    /**
     * Stores the value of the given <code>double[]</code> object in the data adaptor backing store.
     * @param attribute   attribute name
     * @param array    attribute value
     */
    public void setValue( final String attribute, final double[] array );
    
    
    /** return the array of all attributes */
    public String[] attributes();
    
    
    /** return all child adaptors */
    public List<DataAdaptor> childAdaptors();
    
    
    /** return all child adaptors of the specified node name */
    public List<DataAdaptor> childAdaptors( String label );
    
    
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

