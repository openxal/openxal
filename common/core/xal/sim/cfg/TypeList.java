/**
 * TypeList.java
 *
 * @author Christopher K. Allen
 * @since  May 18, 2011
 *
 */

/**
 * TypeList.java
 *
 * @author  Christopher K. Allen
 * @since	May 18, 2011
 */
package xal.sim.cfg;

import java.util.LinkedList;
import java.util.List;

import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.data.IArchive;

/**
 * 
 *
 * @author Christopher K. Allen
 * @since   May 18, 2011
 */
public class TypeList  implements IArchive {

    
    /*
     * Global Constants
     */
    
    /** The XML attribute name used to identify the Java class name in the data node */
    final static private String     STR_ATTR_TYPE = "type";
    
    
    
    
    /*
     * Local Attributes
     */
    
    
    /** String id of the type collection */
    final private String            strId;
    
    /** Data node name (XML element name) of data nodes containing type information */
    final private String            strElemNm;
    
    /** List of the data types */
    final private List<String>      lstTypes;
    
    
    
    /*
     * Initialization
     */
    
    
    /**
     * Creates a new, empty <code>TypeList</code> object with the given
     * identifier.
     * 
     * @param strId         string identifier of this type list
     * @param strElemNm     element names for the nodes containing the type values
     *
     * @author  Christopher K. Allen
     * @since   May 18, 2011
     */
    public TypeList(String strId, String strElemNm) {
        this.strId     = strId;
        this.strElemNm = strElemNm;
        this.lstTypes  = new LinkedList<String>();
    }
    
    /**
     * Initializing constructor - both id and the full list of
     * types are defined.
     * 
     * @param strId     ID of type collection
     * @param strElemNm element names for the nodes containing the type values
     * @param lstTypes  list of type names (Java class names)
     *
     * @author  Christopher K. Allen
     * @since   May 18, 2011
     */
    public TypeList(String strId, String strElemNm, List<String> lstTypes) {
        super();
        this.strId     = strId;
        this.strElemNm = strElemNm;
        this.lstTypes  = lstTypes;
    }
    
    /**
     * 
     * @param strId
     * @param strElemNm     element names for the nodes containing the type values
     * @param daSource
     *
     * @author  Christopher K. Allen
     * @since   May 18, 2011
     */
    public TypeList(String strId, String strElemNm, DataAdaptor daSource) {
        this(strId, strElemNm);

        this.load(daSource);
    }
    
    

    /*
     * Operations
     */

    /**
     * Returns the ID of the Java class type collection.
     *
     * @return  type list string identifier
     *
     * @author Christopher K. Allen
     * @since  May 18, 2011
     */
    public String   getId() {
        return this.strId;
    }

    /**
     * Returns the list of Java class names managed by this
     * class (this is the basic state data).
     *
     * @return  list of class names constituting the type list
     *
     * @author Christopher K. Allen
     * @since  May 18, 2011
     */
    public List<String> getTypeList() {
        return this.lstTypes;
    }
    
    /**
     * Creates and returns the list of Java type classes 
     * (i.e., <code>{@link Class}</code> objects) for the
     * current type list.  
     *
     * @return  list of Java type classes, one for each type name in the current list
     * 
     * @throws ClassNotFoundException   a type name in the type list is invalid (unknown type)
     *
     * @author Christopher K. Allen
     * @since  May 18, 2011
     */
    public List< Class<?> > createClassList() throws ClassNotFoundException {
        List< Class<?> >    lstClsTypes = new LinkedList< Class<?> >();
        
        for (String strClsNm : this.lstTypes) {
            Class<?>    clsType = Class.forName(strClsNm);
            
            lstClsTypes.add(clsType);
        }
        
        return lstClsTypes;
    }
    
    
    /*
     * IArchive Interface
     */
    
    /**
     * Saves the list of class types to the given data archive with
     * the <code>{@link DataAdaptor}</code> interface.  The type values
     * are stored with the attribute name <code>{@link #STR_ATTR_TYPE}</code>
     * (which is {@value #STR_ATTR_TYPE}).  The attribute is part of the
     * (XML-like) element node with element name <code>strElemNm</code>.
     * 
     * @param daArchive     data archive receiving the type information of the list
     *
     * @author Christopher K. Allen
     * @since  May 18, 2011

     * @see xal.tools.data.IArchive#save(xal.tools.data.DataAdaptor)
     */
    @Override
    public void save(DataAdaptor daArchive) {
        for (String strType : this.lstTypes) {
            DataAdaptor daElem = daArchive.createChild(this.strElemNm);
            
            daElem.setValue(STR_ATTR_TYPE, strType);
        }
    }

    /**
     * Loads the list of class types from the given data source with the
     * <code>{@link DataAdaptor}</code> interface.  All the data elements
     * named "<code>strElemNm</code>" will be loaded and the attribute
     * with the value of <code>{@link #STR_ATTR_TYPE}</code> are read in
     * as type names.
     *
     * @param daSource      data source of type values
     *
     * @author Christopher K. Allen
     * @since  May 18, 2011
     *
     * @see xal.tools.data.IArchive#load(xal.tools.data.DataAdaptor)
     */
    @Override
    public void load(DataAdaptor daSource) throws DataFormatException {
        
        List<DataAdaptor>   lstDas = daSource.childAdaptors(this.strElemNm);

        this.lstTypes.clear();
        for (DataAdaptor da : lstDas) {
            String  strType = da.stringValue(STR_ATTR_TYPE);
            
            this.lstTypes.add(strType);
        }
    }
    
}
