/**
 * ArchiveItemList.java
 *
 * @author Christopher K. Allen
 * @since  May 18, 2011
 *
 */

/**
 * ArchiveItemList.java
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
 * <p>
 * Maintains a list of archiveable items, specifically,
 * objects that expose the <code>{@link IArchive}</code>
 * interface.  The exact class type of these items is given
 * as the template parameter <code>T</code>.
 * </p>
 * <p>
 * In order for the <code>{@link #load(DataAdaptor)}</code> method
 * to operate correctly, the class <code>T</code> must define a
 * nullary (zero-argument) constructor.  This constructor is used
 * when populating the list.
 * </p>
 *
 * @author Christopher K. Allen
 * @since   May 18, 2011
 */
public class ArchiveItemList<T extends IArchive> implements IArchive {

    
    /*
     * Local Attributes
     */
    
    
    /** The Java class type of the items */
    final private Class<T>  clsType;
    
    /** (XML) element name of data node containing the archiveable items */
    final private String    strNodeNm;
    
    /** (XML) element name of each archiveable item */
    final private String    strElemNm;
    
    /** The list of archiveable items */
    final private List<T>   lstItems;
    
    
    
    /**
     * Creates a new archiveable item list with the given
     * data node name and archiveable element name.
     * 
     * @param typItem       Java class type of the archiveable items
     * @param strNodeNm     name of the main data node
     * @param strElemNm     element name of each archiveable item
     *
     * @author  Christopher K. Allen
     * @since   May 18, 2011
     */
    public ArchiveItemList(Class<T> typItem, String strNodeNm, String strElemNm) {
        this.clsType   = typItem;
        this.strNodeNm = strNodeNm;
        this.strElemNm = strElemNm;
        this.lstItems  = new LinkedList<T>();
    }
    
    /*
     * Operations
     */
    
    /**
     * Returns the entire list of archiveable items. 
     *
     * @return  item list
     *
     * @author Christopher K. Allen
     * @since  May 19, 2011
     */
    public List<T> getItemList() {
        return this.lstItems;
    }
    
    /**
     * Set the entire list of archiveable items
     *
     * @param lstItems   new item list to maintain
     *
     * @author Christopher K. Allen
     * @since  May 19, 2011
     */
    public void     setList(List<T> lstItems) {
        this.lstItems.clear();
        this.lstItems.addAll(lstItems);
    }
    
    /**
     * Add an archiveable item to the item list.
     *
     * @param item  object to add to the (tail) of the item list
     *
     * @author Christopher K. Allen
     * @since  May 19, 2011
     */
    public void     addItem(T item) {
        this.lstItems.add(item);
    }
    
    
    /*
     * IArchive Interface
     */
    
    /**
     * Stores the list of archiveable items to the given data
     * storage location with the <code>DataAdaptor</code> interface.
     * 
     * @author Christopher K. Allen
     * @since May 18, 2011
     * 
     * @see xal.tools.data.IArchive#save(xal.tools.data.DataAdaptor)
     */
    @Override
    public void save(DataAdaptor daArchive) {
        DataAdaptor daNode = daArchive.createChild(this.strNodeNm);
        
        for (T item : this.lstItems)
            item.save(daNode);
    }

    /**
     * Loads the list of items from the given data source with the
     * <code>{@link DataAdaptor}</code> interface.  An item
     * (of type <code>T</code> is created from its zero constructor
     * then loaded with its {@link IArchive#load(DataAdaptor)}
     * method.
     *
     * @param daSource      data source of type values
     * @throws DataFormatException  Class <code>T</code> or its nullary constructor is not accessible,
     *                              or unreadable or bad data format in data source
     * 
     * 
     * @author Christopher K. Allen
     * @since May 18, 2011
     * 
     * @see xal.tools.data.IArchive#load(xal.tools.data.DataAdaptor)
     */
    @Override
    public void load(DataAdaptor daSource) throws DataFormatException {
        DataAdaptor daNode = daSource.childAdaptor(this.strNodeNm);
        
        List<DataAdaptor>   lstSrcDas = daNode.childAdaptors(this.strElemNm);
        for (DataAdaptor daSrc : lstSrcDas) {
            
            try {
                T   item = this.clsType.newInstance();
                
                item.load(daSrc);
                
                this.lstItems.add(item);
                
            } catch (InstantiationException e) {

            } catch (IllegalAccessException e) {

            }
            
        }
        
        
    }

    
    /*
     * Support Methods
     */
    
}
