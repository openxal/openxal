/**
 * ParameterSet.java
 *
 * Author  : Christopher K. Allen
 * Since   : Mar 21, 2014
 */
package xal.smf.impl.profile;

import java.util.MissingResourceException;

import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.tools.data.DataAdaptor;
import xal.smf.scada.BadStructException;
import xal.smf.scada.ScadaRecord;

/**
 * This is a base class to used to narrow
 * the type of <code>{@link ScadaRecord}</code>.
 * Specifically, data structures derived from this
 * type can be used directly as parameters for
 * <code>ProfileDevice</code> objects.
 * 
 * <p>
 * <b>Ported from XAL on Jul 15, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 * 
 * @since  Dec 18, 2009
 * @author Christopher K. Allen
 */
public abstract class ParameterSet extends ScadaRecord {


    
    /*
     * DataListener Interface
     */

//    /**
//     * Returns the data label used to store data for the given
//     * version number.  The current label  will be returned for
//     * all version greater than or equal to the current version number. 
//     * 
//     * @param lngVersion    storage version number
//     * 
//     * @return              data label used for the given storage format version
//     *
//     * @author Christopher K. Allen
//     * @since  Oct 13, 2014
//     */
//    public String dataLabelOld() {
//        String  strLblVer = "gov.sns." + this.dataLabel();
//        
////            strLblVer = strLblVer.replace("ScannerConfig", "DeviceConfig");
//        return strLblVer;
//    }
    
    /**
     * Label used for parameter set identification. 
     *
     * @since       Mar 4, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.tools.data.DataListener#dataLabel()
     */
    @Override
    public String dataLabel() {
        return this.getClass().getCanonicalName();
    }

//    /**
//     *
//     * @see xal.smf.scada.ScadaRecord#update(xal.tools.data.DataAdaptor)
//     *
//     * @author Christopher K. Allen
//     * @since  Oct 14, 2014
//     */
//    @Override
//    public void update(DataAdaptor daptSrc) throws MissingResourceException,BadStructException {
//
//        String strLblVer = this.dataLabel();
//        
//        DataAdaptor daptDev = daptSrc.childAdaptor(strLblVer);
//        if (daptDev == null) {
//            strLblVer = this.dataLabelOld();
//            daptDev = daptSrc.childAdaptor(strLblVer);
//        }
//        
//        super.update(daptSrc);
//    }


    /*
     * Initialization
     */

    /**
     * Create a new <code>WireScanner.ParameterSet</code> object.
     *
     * @since     Dec 18, 2009
     * @author    Christopher K. Allen
     */
    protected ParameterSet() {
        super();
    }

    /**
     * Create a new <code>ParameterSet</code> object initialized
     * from the given data source. 
     *
     * @param daptSrc       data source containing data structure fields
     *
     * @since     Mar 17, 2010
     * @author    Christopher K. Allen
     */
    protected ParameterSet(DataAdaptor daptSrc) {
        super(daptSrc);
    }

    /**
     * Create a new <code>WireScanner.ParameterSet</code> object.
     *
     * @param smfDev        Connects the parameter set to this device and loads values
     *
     * @throws ConnectionException  unable to connect to the given device 
     * @throws GetException         unable to acquire data from the given device 
     *  
     * @since     Dec 18, 2009
     * @author    Christopher K. Allen
     */
    protected ParameterSet(ProfileDevice smfDev) throws ConnectionException, GetException {
        super(smfDev);
    }
}
