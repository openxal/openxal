/*
 * IArchive.java
 *
 * Created on March 5, 2003, 11:21 AM
 */

package xal.tools.data;


/**
 *  Interface for storing and restoring the state of an object via
 *  a data archive exposing the DataAdaptor interface.
 *
 * @author  Christopher Allen
 */
public interface IArchive {
    
    
    /**
     *  Save the state of the object to a data adaptor for later reconstruction.
     *
     *  @param  daArchive     DataAdaptor interface to a data archive
     */
    public void save(DataAdaptor daArchive);
    

    /**
     *  Recover an object's state from an archived data source.
     *
     *  @param  daSource     DataAdaptor interface to a data source
     *
     *  @exception  DataFormatException     data format of the adaptor is corrupt
     */
    public void load(DataAdaptor daSource) throws DataFormatException;
    
}
