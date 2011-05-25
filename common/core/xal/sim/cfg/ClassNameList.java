/**
 * ClassNameList.java
 *
 * @author Christopher K. Allen
 * @since  May 18, 2011
 *
 */

/**
 * ClassNameList.java
 *
 * @author  Christopher K. Allen
 * @since	May 18, 2011
 */
package xal.sim.cfg;

import java.util.Collection;
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
public class ClassNameList  implements IArchive {

    
    /**
     * Enumeration for {@link DataItem} class that defines
     * the data node structure.
     *
     * @author Christopher K. Allen
     * @since   May 23, 2011
     */
    public enum ATTRS {
        
        /** attribute name for the Java class name value */
        type;
    }
    
    
    
    /*
     * Local Attributes
     */
    
    
    /** Data node name (XML element name) of data nodes containing type information */
    final private String                    strElemNm;
    
    /** List of the data types */
    final private List<DataItem<ATTRS>>     lstItems;
    
    
    
    /*
     * Initialization
     */
    
    
    /**
     * Creates a new, empty <code>ClassNameList</code> object with the given
     * identifier.
     * 
     * @param strElemNm     element names for the nodes containing the type values
     *
     * @author  Christopher K. Allen
     * @since   May 18, 2011
     */
    public ClassNameList(String strElemNm) {
        this.strElemNm = strElemNm;
        this.lstItems  = new LinkedList< DataItem<ATTRS> >();
    }
    
    /**
     * Initializing constructor - both element name and the full list of
     * types are defined.
     * 
     * @param strElemNm   element names for the nodes containing the type values
     * @param lstTypeNms  list of type names (Java class names)
     *
     * @author  Christopher K. Allen
     * @since   May 18, 2011
     */
    public ClassNameList(String strElemNm, Collection<String> lstTypeNms) {
        this(strElemNm);
        
        for (String strTypeNm : lstTypeNms) {
            DataItem<ATTRS> datNode = new DataItem<ATTRS>(ATTRS.class, strElemNm);
            
            datNode.setValue(ATTRS.type, strTypeNm);
            
            this.lstItems.add(datNode);
        }
    }
    
    /**
     * Initializing constructor - both element name and the full list of
     * types are defined.
     * 
     * @param strElemNm   element names for the nodes containing the type values
     * @param lstTypeCls  list of type classes (Java <code>Class</code> objects)
     *
     * @author  Christopher K. Allen
     * @since   May 18, 2011
     */
    public ClassNameList(String strElemNm, List< Class<?> > lstTypeCls) {
        this(strElemNm);
        
        for (Class<?> clsType : lstTypeCls) {
            DataItem<ATTRS> datNode   = new DataItem<ATTRS>(ATTRS.class, strElemNm);
            String          strTypeNm = clsType.getName();
            
            datNode.setValue(ATTRS.type, strTypeNm);
            
            this.lstItems.add(datNode);
        }
    }
    
    /**
     * Create a new class name list and initialize it from the given
     * data source.
     * 
     * @param strElemNm     element names for the nodes containing the type values
     * @param daSource      data source containing class name list
     *
     * @author  Christopher K. Allen
     * @since   May 18, 2011
     */
    public ClassNameList(String strElemNm, DataAdaptor daSource) {
        this(strElemNm);

        this.load(daSource);
    }
    
    

    /*
     * Operations
     */

    /**
     * Returns the list of Java class names managed by this
     * class (this is the basic state data).
     *
     * @return  list of class names constituting the type list
     *
     * @author Christopher K. Allen
     * @since  May 18, 2011
     */
    public List<String> createTypeNameList() {
        List<String>    lstTypeNms = new LinkedList<String>();
        
        for (DataItem<ATTRS> item : this.lstItems) {
            String  strTypeNm = item.getValString(ATTRS.type);
            
            lstTypeNms.add(strTypeNm);
        }
            
        return lstTypeNms;
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
        
        for (DataItem<ATTRS> item : this.lstItems) {
            Class<?>    clsType = item.getValClass(ATTRS.type);
            
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
     * are stored with the attribute name <code>{@link ATTRS}</code>
     * (which is {@value ATTRS#type}).  The attribute is part of the
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
        for (DataItem<ATTRS> datItem : this.lstItems) {
            
            datItem.save(daArchive);
        }
    }

    /**
     * Loads the list of class types from the given data source with the
     * <code>{@link DataAdaptor}</code> interface.  All the data elements
     * named "<code>strElemNm</code>" will be loaded and the attribute
     * with the value of <code>{@link ATTRS}</code> are read in
     * as type names.
     *
     * @param daSource      data source of type values
     * 
     * @throws DataFormatException  unreadable or bad data format in data source 
     *
     * @author Christopher K. Allen
     * @since  May 18, 2011
     *
     * @see xal.tools.data.IArchive#load(xal.tools.data.DataAdaptor)
     */
    @Override
    public void load(DataAdaptor daSource) throws DataFormatException {
        
        List<DataAdaptor>   lstDas = daSource.childAdaptors(this.strElemNm);

        this.lstItems.clear();
        for (DataAdaptor da : lstDas) {

            DataItem<ATTRS> item = new DataItem<ATTRS>(ATTRS.class, this.strElemNm, da);
            
            this.lstItems.add(item);
        }
    }
    
}
