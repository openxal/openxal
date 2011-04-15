/*
 * IArchive.java
 *
 * Created on March 5, 2003, 11:21 AM
 */

package xal.model;

import xal.tools.data.IDataAdaptor;
import xal.tools.data.DataFormatException;

/**
 *  Interface for storing and restoring the state of an object via
 *  a data archive exposing the IDataAdaptor interface.
 *
 * @author  Christopher Allen
 */
public interface IArchive {
    
    
    /**
     *  Save the state of the object to a data adaptor for later reconstruction.
     *
     *  @param  daptArchive     IDataAdaptor interface to an archive
     */
    public void save(IDataAdaptor daptArchive);
    

    /**
     *  Recover an object's state from an archived data source.
     *
     *  @param  daptArchive     IDataAdaptor interface to an archive
     *
     *  @exception  DataFormatException     data format of the adaptor is corrupt
     */
    public void load(IDataAdaptor daptArchive) throws DataFormatException;
    
}
