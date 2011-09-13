/*
 * VolatileDataAdaptor.java
 *
 * Created on March 5, 2003, 9:23 PM
 */

package xal.tools.data;


import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.PatternSyntaxException;



/**
 * A volatile implementation of the <code>DataAdaptor</code> interface.  
 * <code>TransientDataAdaptor</code> is intended as a convenience class
 * for passing data using the </code>DataAdaptor</code> interface.  Note, 
 * however, class supports no persistence.  Once an object of class 
 * <code>TransientDataAdaptor</code> is destroyed all internal data is lost.
 *
 * @author  Christopher K. Allen
 */
public class TransientDataAdaptor implements DataAdaptor {
    
    
    /*
     *  Local Attributes
     */
    
    /** label of data store */
    private final String      m_strLabel;
    
    /** map of all the attribute-value pairs */
    private final HashMap<String, String>           m_mapAttrs = new HashMap<String, String>();
    
    /** list of all child adaptors */
    private final LinkedList<DataAdaptor>  m_lstKids = new LinkedList<DataAdaptor>();
    
    
    
    /*
     *  Initialization
     */
    
    /** 
     *  Create a new instance of VolatileDataAdaptor 
     *
     *  @param  strLabel    label for new data node
     */
    public TransientDataAdaptor(String strLabel) {
        m_strLabel = strLabel;
    }
    
    
    
    
    /*
     *  DataAdaptor Interface
     */
    
    /** 
     *  Get the label for this data node.
     *
     *  @return         name for the particular node in the data tree 
     */
    public String name() {
        return m_strLabel;
    }
    
    /** 
     *  Get all the attribute names in the data node.
     *
     * @return  array of all attributes names
     */
    public String[] attributes() {
        
        // Allocate the string array
        int                 nAttrs;     // number of attributes
        String[]            arrNames;   // returned array attribute names

        nAttrs   = m_mapAttrs.size();
        arrNames = new String[nAttrs];
        
        
        // Build the string array
        int                 iName;      // index of current attribute name
        Set<String>        setNames;   // set of attribute names
        Iterator<String>   iter;       // name set iterator
        
        setNames = m_mapAttrs.keySet();
        iter     = setNames.iterator();
        iName    = 0;
        while (iter.hasNext())  {
            arrNames[iName] = iter.next();
            
            iName++;
        }
            
        return arrNames;
    }
    
    /** 
     *  Test whether or not an attribute is present in the data node.
     *
     *  @param  strAttrName     attribute name
     *
     *  @return                 true if specified attribute is present, false otherwise
     */
    public boolean hasAttribute(String strAttrName) {
        return m_mapAttrs.containsKey(strAttrName);
    }
    
    
    /** 
     *  Get the value of an attribute.
     *
     *  @param  strAttrName     name of attribute
     *
     *  @return                 value of attribute as boolean
     */
    public String stringValue(String strAttrName) {
        String  strValue = m_mapAttrs.get(strAttrName);
        
        return strValue;
    }
    
    /** 
     *  Get the value of an attribute.
     *
     *  @param  strAttrName     name of attribute
     *
     *  @return                 value of attribute as boolean
     *
     *  @exception  NumberFormatException   unable to parse value as boolean
     */
    public boolean booleanValue(String strAttrName) throws NumberFormatException {
        String strValue = stringValue(strAttrName);
        
        return Boolean.valueOf(strValue).booleanValue();
    }
    
    /** 
     *  Get the value of an attribute.
     *
     *  @param  strAttrName     name of attribute
     *
     *  @return                 value of attribute as int
     *
     *  @exception  NumberFormatException   unable to parse value as int
     */
    public int intValue(String strAttrName) {
        String strValue = stringValue(strAttrName);
        
        return Integer.valueOf(strValue).intValue();
    }
    
    /** 
     *  Get the value of an attribute.
     *
     *  @param  strAttrName     name of attribute
     *
     *  @return                 value of attribute as long
     *
     *  @exception  NumberFormatException   unable to parse value as long
     */
    public long longValue(String strAttrName) {
        String strValue = stringValue(strAttrName);
        
        return Long.valueOf(strValue).longValue();
    }
    
    /** 
     *  Get the value of an attribute.
     *
     *  @param  strAttrName     name of attribute
     *
     *  @return                 value of attribute as double
     *
     *  @exception  NumberFormatException   unable to parse value as double
     */
    public double doubleValue(String strAttrName) {
        String strValue = stringValue(strAttrName);
        
        return Double.valueOf(strValue).doubleValue();
    }
    
    /**
     * Returns the value of an attribute as an array of doubles.  The
     * attribute value must be stored as a string of comma separated 
     * values (CSVs).  The values are parsed into doubles and packed into
     * the returned array.
     *
     * @param strAttr   the attribute name
     * 
     * @return  Array of double values as parsed from the value string.
     *          A <code>null</code> value is returned if the value string is empty.
     * 
     * @throws NumberFormatException    at least one value was malformed, 
     *                                  or the CSV string was malformed 
     * 
     * @since  Mar 12, 2010
     * @author Christopher K. Allen
     */
    public double[] doubleArray(final String strAttr) throws NumberFormatException {
        String strValues = stringValue(strAttr);
        
        if (strValues.length() == 0)
            return null;
        
        String strErrMsg = "Error parsing as double attribute: " + strAttr + 
                        ", from string: " + strValues + ", for XML node: " + name();
        
            try {
                String[]        arrTokens = strValues.split(",");
                double[]        arrVals = new double[arrTokens.length];
                
                int     index = 0;
                for (String strVal : arrTokens) {
                    double      dblVal = Double.parseDouble(strVal);
                    
                    arrVals[index] = dblVal;
                    index++;
                }
                
                return arrVals;
                
            } catch (PatternSyntaxException e) {
                throw new NumberFormatException(strErrMsg);
                
            } catch(java.lang.NumberFormatException e) {
                throw new NumberFormatException(strErrMsg);
            }
    }
    

    /** 
     *  Set the value of the specified attribute as a string.
     *
     *  @param  strAttrName     attribute name
     *  @param  strAttrVal      attribute value
     */
    public void setValue(String strAttrName, String strAttrVal) {
        m_mapAttrs.put(strAttrName, strAttrVal);
    }
    
    /** 
     *  Set the value of the specified attribute
     *
     *  @param  strAttrName     attribute name
     *  @param  objAttrVal      new attribute value  
     */
    public void setValue(String strAttrName, Object objAttrVal) {
        String  strAttrVal = objAttrVal.toString();
        
        this.setValue(strAttrName, strAttrVal);
    }
    
    /** 
     *  Set the value of the specified attribute
     *
     *  @param  strAttrName     attribute name
     *  @param  bolAttrVal      new attribute value  
     */
    public void setValue(String strAttrName, boolean bolAttrVal) {
        String  strAttrVal = String.valueOf(bolAttrVal);
        
        this.setValue(strAttrName, strAttrVal);
    }
    
    /** 
     *  Set the value of the specified attribute
     *
     *  @param  strAttrName     attribute name
     *  @param  intAttrVal      new attribute value  
     */
    public void setValue(String strAttrName, int intAttrVal) {
        String strAttrVal = String.valueOf(intAttrVal);
        
        this.setValue(strAttrName, strAttrVal);
    }
    
    /** 
     *  Set the value of the specified attribute
     *
     *  @param  strAttrName     attribute name
     *  @param  longAttrVal     new attribute value  
     */
    public void setValue(String strAttrName, long longAttrVal) {
        String strAttrVal = String.valueOf(longAttrVal);
        
        this.setValue(strAttrName, strAttrVal);
    }
    
    /** 
     *  Set the value of the specified attribute
     *
     *  @param  strAttrName     attribute name
     *  @param  dblAttrVal      new attribute value  
     */
    public void setValue(String strAttrName, double dblAttrVal) {
        String  strAttrVal = String.valueOf(dblAttrVal);
        
        this.setValue(strAttrName, strAttrVal);
    }
    
    /**
     * Sets the attribute (name,value) pair for the given arguments.  
     * The value here is a double array which is stored as a string 
     * of comma separated values.
     *
     * @since   Mar 11, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.tools.data.DataAdaptor#setValue(java.lang.String, double[])
     */
    @Override
    public void setValue(String strAttr, double[] arrVal) {
        StringBuffer    bufVals = new StringBuffer(arrVal.length);
        
        for (double dblVal : arrVal) {
            bufVals.append(dblVal);
            bufVals.append(", ");
        }
        
        this.setValue(strAttr, bufVals.toString());
    }
    
    
    /** 
     *  Get the number of child data nodes.
     *
     *  @return         number of child node adaptors  
     */
    public int nodeCount() {
        return m_lstKids.size();
    }
    
    /** 
     *  Convenience method to get a single child adaptor when only one is expected.
     *
     *  @param  strLabel    data label of child node
     *
     *  @return             first child node with label strLabel, null if none exist
     */
    public DataAdaptor childAdaptor(String strLabel) {
        Iterator<DataAdaptor>    iter = this.childAdaptorIterator(strLabel);
        
        if ( !iter.hasNext() ) return null;
        else                   return iter.next(); 
    }
    
    /** 
     *  Get all the child data nodes of this adaptor.
     *
     *  @return             all child adaptors  
     */
    public List<DataAdaptor> childAdaptors() {
        return m_lstKids;
    }
    
    /** 
     *  Get all the child data nodes of a particular data label.
     *
     *  @param  strLabel    data label for child nodes
     *
     *  @return             all child adaptors with specified label 
     */
    public List<DataAdaptor> childAdaptors(String strLabel) {
        LinkedList<DataAdaptor>  lst  = new LinkedList<DataAdaptor>();
        Iterator<DataAdaptor>    iter = this.childAdaptorIterator();
        
        while (iter.hasNext())  {
            DataAdaptor daptChild = iter.next();
            
            if ( daptChild.name().equals(strLabel) )
                lst.add(daptChild);
        }
        
        return lst;
    }
    
    /** 
     *  Get an iterator containing all child adaptors of this node.
     *
     *  @return                 iterator of all child adaptors  
     */
    public Iterator<DataAdaptor> childAdaptorIterator() {
        return m_lstKids.iterator();
    }
    
    /** 
     *  Get an iterator for all child data nodes having a given data label.
     *
     *  @param  strLabel    data label for child nodes
     *
     *  @return             iterator of child adaptors with specified label 
     */
    public Iterator<DataAdaptor> childAdaptorIterator(String strLabel) {
        return this.childAdaptors(strLabel).iterator();
    }
    
    /** 
     *  Create a new empty child adaptor with the specified label.
     *
     *  @param  strLabel        data label for the child node
     *
     *  @return                 new child data node attached to this  
     */
    public DataAdaptor createChild(String strLabel) {
        TransientDataAdaptor     daptChild = new TransientDataAdaptor(strLabel);
        
        this.m_lstKids.add(daptChild);
        
        return daptChild;
    }
    
    
    
    
    /** 
     *  Write out the listener data as a new node then append it as a child node
     *  in the data tree.
     *
     *  @param  ifcSrc
     */
    public void writeNode(DataListener ifcSrc) {
        String strLabel = ifcSrc.dataLabel();
        
        DataAdaptor daptChild = this.createChild(strLabel);
        ifcSrc.write(daptChild);
    }
    
    /**
     *  write the collection of listeners to new nodes and append them
     *  to the data tree.
     *  
     * @param colSrcs   collection data sources exposing <code>DataListener</code> interface
     *  
     */
    public void writeNodes(Collection<? extends DataListener> colSrcs) {
        Iterator<? extends DataListener> iterSrcs = colSrcs.iterator();
        
        while ( iterSrcs.hasNext() ) {
            DataListener ifcSrc = iterSrcs.next();
            writeNode(ifcSrc);
        }
    }

    
    
    /*
     *  Testing and Debugging
     */

    /**
     * Print out the contents of this adaptor.
     *
     * @param os        output stream
     * 
     * @since  Mar 12, 2010
     * @author Christopher K. Allen
     */
    public void print(PrintStream os) {
        String[]    arrAttrNames = this.attributes();
        int         nAttrs = arrAttrNames.length;
        int         iAttr  = 0;
        
        os.println(this.name());
        
        for (iAttr=0; iAttr<nAttrs; iAttr++)    {
            String  strAttrName = arrAttrNames[iAttr];
            String  strAttrVal = this.stringValue(strAttrName);
            
            os.println("  " + strAttrName + ": " + strAttrVal);
        }
    }
    
}
