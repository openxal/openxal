/*
 * DataListener.java
 *
 * Created on February 11, 2002, 2:32 PM
 */

package xal.tools.data;

/**
 * DataListener is a generic interface for an object that (receives/writes)
 * data (from/to) a generic DataAdaptor.
 * 
 * @author  tap
 */
public interface DataListener {
    /** 
     * dataLabel() provides the name used to identify the class in an 
     * external data source.
     * @return a tag that identifies the receiver's type
     */
    public String dataLabel();
    
    
    /**
     * Update the data based on the information provided by the data provider.
     * @param adaptor The adaptor from which to update the data
     */
    public void update(DataAdaptor adaptor);
    
    
    /**
     * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write(DataAdaptor adaptor);
}

