/**
 * IContextAware.java
 * 
 * Created 11/20/06
 * 
 */
package xal.tools.data;

/**
 * Interface specifying methods for saving/restore object data from
 * an <code>EditContext</code> object.
 * 
 * 
 * 
 * @author Christopher K. Allen
 *
 * @see xal.tools.data.EditContext
 */
public interface IContextAware {

    
    /**
     * Load the class's parameters from an <code>EditContext</code> object.
     * 
     * 
     * @param strPrimKeyVal     primary key value specifying the name of the record
     * @param ecTableData       EditContext containing table data
     * 
     * @throws DataFormatException  bad data format - error in reading
     */
    public void load(final String strPrimKeyVal, final EditContext ecTableData) throws DataFormatException;
}
