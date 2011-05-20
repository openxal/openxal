/**
 * DataItem.java
 *
 * @author Christopher K. Allen
 * @since  May 19, 2011
 *
 */

/**
 * DataItem.java
 *
 * @author  Christopher K. Allen
 * @since	May 19, 2011
 */
package xal.sim.cfg;

import java.util.HashMap;
import java.util.Map;

import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.data.IArchive;

/**
 * <p>
 * This class models an XML data node on the structure of
 * a Java enumeration class.  The objective is to conveniently
 * load and store the data node, defined by an <code>enum</code>
 * class, using the <code>{@link IArchive}</code> interface.
 * </p>
 * <p>
 * For example, suppose we define the enumeration class
 * <br/>
 * <br/>
 * <code>
 * &nbsp; &nbsp; public enum test_node {
 * <br/>&nbsp; &nbsp; &nbsp; &nbsp; attr1,
 * <br/>&nbsp; &nbsp; &nbsp; &nbsp; attr2,
 * <br/>&nbsp; &nbsp; &nbsp; &nbsp; attr3;
 * <br/>&nbsp; &nbsp;  }
 * <br/>
 * <br/>
 * </code>
 * then the XML element would have the form
 * <br/>
 * <br/>
 * <code>
 * &nbsp; &nbsp; &lt;test_node attr1="" attr2="" attr3=""/>
 * <br/>
 * <br/>
 * </code>
 * Note that the template (generic) parameter <code>E</code> is required to be an
 * enumeration type.  To defined the data node it is necessary to 
 * pass the class object of the enumeration type <code>E</code> 
 * to the constructor (see {@link #DataItem(Class)}). 
 * </p>
 * 
 * 
 *
 * @author Christopher K. Allen
 * @since   May 19, 2011
 */
public class DataItem<E extends Enum<E>> implements IArchive {


    
    /*
     * Global Methods
     */
    
    /**
     * Returns the name of the enumeration class given the
     * Java class object of the enumeration.  The returned name is 
     * the "given name", created by removing all the package and
     * other context prefixes.
     *
     * @param clsEnumAttrs  Java class type of the enumeration
     * 
     * @return  Name of enumeration
     *
     * @author Christopher K. Allen
     * @since  May 20, 2011
     */
    static public <E extends Enum<E>> String  extractEnumName(Class<E> clsEnumAttrs) {
        String      strClsNm = clsEnumAttrs.getName();
        
        int         indEnd = strClsNm.length();
        int         indBeg = strClsNm.lastIndexOf("$") + 1;
        if (indBeg == 0)
            indBeg = strClsNm.lastIndexOf(".") + 1;

        String      strEnmNm = strClsNm.substring(indBeg, indEnd);
        
        return strEnmNm;
    }
    
    
    /*
     * Local Attributes
     */
    
    /** The XML element name for the data node */
    final private String                strElemNm;

    /** Enumeration of XML attribute names where the data value is located */
    final private E[]                   ArrEnmAttrs;
    
    /** The data values indexed by attribute name */
    final private Map<E,String>         mapVals;
    
    
    
    /*
     * Initialization
     */
    

    /**
     * Create a new <code>DataItem</code> object configured to load and
     * store values defined by the given enumeration class.  
     * Note the data value are not yest set.
     * 
     * @param clsEnmAttrs  Java type class for the enumeration defining the data node
     *
     * @author  Christopher K. Allen
     * @since   May 19, 2011
     */
    public DataItem(Class<E> clsEnmAttrs) {
        this.strElemNm   = DataItem.extractEnumName(clsEnmAttrs);
        this.ArrEnmAttrs = clsEnmAttrs.getEnumConstants();
        
        this.mapVals   = new HashMap<E,String>();
    }

    /**
     * Create a new <code>DataItem</code> object configured to load and
     * store values defined by the given enumeration class.  
     * The data values are initialized from the
     * data source exposing the <code>{@link DataAdaptor}</code>
     * interface.
     * 
     * @param clsEnmAttrs  Java type class for the enumeration defining the data node
     * @param daSource     Data source containing the data values
     *
     * @throws  DataFormatException  unreadable data format in the data source
     * 
     * @author  Christopher K. Allen
     * @since   May 19, 2011
     */
    public DataItem(Class<E> clsEnmAttrs, DataAdaptor daSource) throws DataFormatException {
        this(clsEnmAttrs);
        
        this.load(daSource);
    }

    
    /**
     * Sets the value of the given attribute in this data set.
     *
     * @param enmAttr   data attribute 
     * @param strValue  attribute value
     *
     * @author Christopher K. Allen
     * @since  May 20, 2011
     */
    public void setValue(E enmAttr, String strValue) {
        this.mapVals.put(enmAttr, strValue);
    }
    
    /**
     * Sets the value of the given attribute in this data node.
     * The numerical value is first converted to an appropriate
     * string representation.
     *
     * @param enmAttr   data attribute
     * @param numValue  attribute value (as a number)
     *
     * @author Christopher K. Allen
     * @since  May 20, 2011
     */
    public void setValue(E enmAttr, Number numValue) {
        String  strValue = numValue.toString();
        
        this.setValue(enmAttr, strValue);
    }
    
    /**
     * Set the value of the given attribute to the given
     * boolean value.  If the <code>Boolean</code> argument
     * have value <code>true</code> then the attribute value is
     * stored as the string "<i>true</i>".  The <code>false</code>
     * value is stored as the string "<i>false</i>". 
     *
     * @param enmAttr   data attribute
     * @param bolValue  new value of the attribute (as a boolean)
     *
     * @author Christopher K. Allen
     * @since  May 20, 2011
     */
    public void setValue(E enmAttr, Boolean bolValue) {
        String  strValue = bolValue.toString();
        
        this.setValue(enmAttr, strValue);
    }
    
    
    
    /*
     * Attributes
     */
    
    /**
     * Returns the value of the attribute with the given name.
     *
     * @param enmAttr   data attribute 
     * 
     * @return          value of the attribute
     *
     * @author Christopher K. Allen
     * @since  May 20, 2011
     */
    public String   getValString(E enmAttr) {
        return this.mapVals.get(enmAttr);
    }
    
    /**
     * Returns the value of the given attribute as an
     * <code>Integer</code> type.
     *
     * @param enmAttr   data attribute
     * 
     * @return          value of attribute
     * 
     * @throws NumberFormatException    String representation of data value does not support
     *                                  <code>Integer</code> representation.
     *
     * @author Christopher K. Allen
     * @since  May 20, 2011
     */
    public Integer  getValInteger(E enmAttr) throws NumberFormatException {
        String      strVal = this.getValString(enmAttr);
        Integer     intVal = Integer.valueOf(strVal);
        
        return intVal;
    }
    
    /**
     * Returns the value of the given attribute as a
     * <code>Long</code> type.
     *
     * @param enmAttr   data attribute
     * 
     * @return          value of attribute
     * 
     * @throws NumberFormatException    String representation of data value does not support
     *                                  <code>Long</code> representation.
     *
     * @author Christopher K. Allen
     * @since  May 20, 2011
     */
    public Long     getValLong(E enmAttr) throws NumberFormatException {
        String      strVal = this.getValString(enmAttr);
        Long        lngVal = Long.valueOf(strVal);
        
        return lngVal;
    }
    
    /**
     * Returns the value of the given attribute as a
     * <code>Double</code> type.
     *
     * @param enmAttr   data attribute
     * 
     * @return          value of attribute
     * 
     * @throws NumberFormatException    String representation of data value does not support
     *                                  <code>Double</code> representation.
     *
     * @author Christopher K. Allen
     * @since  May 20, 2011
     */
    public Double   getValDouble(E enmAttr) throws NumberFormatException {
        String      strVal = this.getValString(enmAttr);
        Double      dblVal = Double.valueOf(strVal);
        
        return dblVal;
    }
   
    /**
     * Returns the value of the given attribute as a
     * <code>Boolean</code> type.  The returned value is
     * <code>true</code> if and only if the attribute has
     * value <tt>"true"</tt>, ignoring case.  Otherwise the
     * returned value is <code>false</code>.
     *
     * @param enmAttr   data attribute
     * 
     * @return          <code>true</code> if the attribute value is <i>"true"</i>,
     *                  <code>false</code> otherwise.
     *
     * @author Christopher K. Allen
     * @since  May 20, 2011
     */
    public Boolean  getValBoolean(E enmAttr) {
        String      strVal = this.getValString(enmAttr);
        Boolean     bolVal = Boolean.valueOf(strVal);
        
        return bolVal;
    }
    
    
    
    
    /*
     * IArchive Interface
     */
    
    /**
     * Saves this data node to the given data archive.
     * 
     * @param   daArchive   archive to receive this data node
     * 
     * @author Christopher K. Allen
     * @since May 19, 2011
     * 
     * @see xal.tools.data.IArchive#save(xal.tools.data.DataAdaptor)
     */
    @Override
    public void save(DataAdaptor daArchive) {
        DataAdaptor daNode = daArchive.createChild(this.strElemNm);
        
        for (Map.Entry<E, String> entry : this.mapVals.entrySet()) {
            String  strAttrNm  = entry.getKey().name();
            String  strAttrVal = entry.getValue();
            
            daNode.setValue(strAttrNm, strAttrVal);
        }
    }

    /**
     * Loads the data of this data node from the given data source.
     * 
     * @param   daSource    contains a data node mirrored by the enumeration class
     * 
     * @throws  DataFormatException  unreadable data format in the data source
     * 
     * @author Christopher K. Allen
     * @since May 19, 2011
     * 
     * @see xal.tools.data.IArchive#load(xal.tools.data.DataAdaptor)
     */
    @Override
    public void load(DataAdaptor daSource) throws DataFormatException {
        
        // Check if the given data source is already of our node type 
        //      which it should be
        String      strSrcNm = daSource.name();
        
        if ( !strSrcNm.equals(this.strElemNm) )
            daSource = daSource.childAdaptor(this.strElemNm);
            // If not then we look for our node type one deeper

        for (E attr : this.ArrEnmAttrs) {
            String  strAttrNm = attr.name();
            
            if ( daSource.hasAttribute(strAttrNm) ) {
                String  strAttrVal = daSource.stringValue(strAttrNm);
                
                this.mapVals.put(attr, strAttrVal);
            }
        }            
    }
    
    
    /*
     * Support Methods
     */
    
}
