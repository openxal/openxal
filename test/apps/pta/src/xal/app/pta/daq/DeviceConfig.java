/**
 * DeviceConfig.java
 *
 * Author  : Christopher K. Allen
 * Since   : Apr 14, 2014
 */
package xal.app.pta.daq;

import xal.app.pta.MainApplication;
import xal.ca.BadChannelException;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;
import xal.tools.xml.XmlDataAdaptor;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.profile.ProfileDevice;
import xal.smf.scada.BadStructException;
import xal.smf.scada.ScadaFieldDescriptor;
import xal.smf.scada.ScadaRecord;

import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * Base class for profile device configuration data structures.  Contains
 * common operations necessary for this type of data structure and any
 * non-specific attributes.
 * 
 * <p>
 * <b>Ported from XAL on Jul 16, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author Christopher K. Allen
 * @since  Apr 14, 2014
 */
public abstract class DeviceConfig implements DataListener {

    
    /*
     * Global Constants
     */
    
    
    /** The device ID attribute used in <code>DataListener</code> implementation */
    protected static final String STR_ATTR_DEVID = "dev";

    
    /** The format version for persistent storage */
    protected static final long  LNG_VAL_FMTVER = 3;

    /** The data format version attribute used in <code>DataListener</code> implementation */
    protected static final String STR_ATTR_FMTVER = "ver";
     

   /*
    * Global Operations
    */
    

    /*
     * Local Attributes
     */
    
    //
    //  Data Source
    //
    
    /** The acquisition device */
    private String                          strDevId;
    
//    /** List of SCADA field descriptors describing connections maintained by this object */
//    private final ScadaRecord[]             arrRecCfg;
//
//    /** List of SCADA field descriptors describing connections maintained by this object */
//    private final List<ScadaFieldDescriptor> lstFds;

    /*
     * Initialization
     */
    
    /**
     * Initializing constructor which takes all the field descriptors
     * for the channels this object will manage.  The profile device is
     * left unidentified.
     *
     * @param   arrRecCfg  array of managed SCADA field descriptor objects
     *
     * @author Christopher K. Allen
     * @since  Apr 14, 2014
     */
    public DeviceConfig() {
        this("");
    }

    /**
     * Initializing constructor which accepts the string identifier of the 
     * device whose configuration parameters are being maintained.
     *
     * @param   strDevId    string identifier for the profile device under configuration management
     *
     * @author Christopher K. Allen
     * @since  Apr 14, 2014
     */
    public DeviceConfig(String strDevId) {
        this.strDevId  = strDevId;
//        this.arrRecCfg = arrRecCfg;
//        this.lstFds    = new LinkedList<ScadaFieldDescriptor>();
//        
//        // Load the list of all scada field descriptor records from all the 
//        //  given scada records
//        for (ScadaRecord rec : this.arrRecCfg) {
//            List<ScadaFieldDescriptor>  lstFdsRec = rec.getFieldDescriptors();
//            
//            this.lstFds.addAll(lstFdsRec);
//        }
    }
    
//    /**
//     * Constructor for DeviceConfig.
//     *
//     * @param smfDev
//     *
//     * @author Christopher K. Allen
//     * @since  Apr 15, 2014
//     */
//    public DeviceConfig(ProfileDevice smfDev) {
//        this(smfDev.getId());
//        this.acquireConfiguration(smfDev);
//    }
    
    /*
     * Properties
     */
    
    /**
     * Returns the string identifier to the device under configuration
     * management.
     * 
     * @return  profile device whose configuration management information is stored here
     *
     * @author Christopher K. Allen
     * @since  Apr 15, 2014
     */
    public String   getDeviceId() {
        return this.strDevId;
    }
    
    /**
     * Returns the current data format version for this class and all child
     * classes.  This is used in the <code>{@link #update(DataAdaptor)}</code>
     * and <code>{@link #write(DataAdaptor)}</code>
     * methods exposed by the <code>DataListener</code> interface.
     *  
     * @return  version number of the current <code>DataListener</code> data format
     *
     * @author Christopher K. Allen
     * @since  Apr 15, 2014
     */
    public long     getDataFormatVersion() {
        return LNG_VAL_FMTVER;
    }
    
    /**
     * <p>
     * Returns the set of all SCADA field descriptors describing the
     * data acquisition channels connected to this object.  
     * Since this is an active data structure
     * these channels are used internally to populate the data fields,
     * which is profile data taken from the hardware.
     * <h4>NOTE</h4>
     * &middot; This method calls <code>{@link #getScadaRecords()}</code>.  
     * Do not call this method during construction!
     * </p>
     *  
     * @return  set of all channel field descriptors maintained by this data structure
     *
     * @author Christopher K. Allen
     * @since  Apr 15, 2014
     */
    public List<ScadaFieldDescriptor> getFieldDescriptors() {
        
        // Create the container to be returned
        final List<ScadaFieldDescriptor> lstFdsAll = new LinkedList<ScadaFieldDescriptor>();
        
        // Fill the container with the field descriptors of each SCADA record
        for (ScadaRecord rec : this.getScadaRecords()) {
            List<ScadaFieldDescriptor>  lstFdsRec = rec.getFieldDescriptors();
            
            lstFdsAll.addAll(lstFdsRec);
        }
        
        return lstFdsAll;
    }
    

    /*
     * Operations
     */
    
    /**
     * Check that all the device configuration channels are available for
     * the given device.   
     *
     * @param smfHarp   hardware device under test
     * @param dblTmOut  maximum time allowed to make connections (in seconds)
     * 
     * @return          <code>true</code> if all connections are available,
     *                  <code>false</code> otherwise
     *                  
     * @throws BadChannelException  there exists an unbound channel handle in the given
     *                              set of SCADA field descriptors
     *
     * @author Christopher K. Allen
     * @since  Mar 16, 2011
     */
    public synchronized boolean testConnection(ProfileDevice smfHarp, double dblTmOut) throws BadChannelException {

        boolean bolResult = smfHarp.testConnection(this.getFieldDescriptors(), dblTmOut);

        return bolResult;
    }

    
    /**
     * <p>
     * Load the hardware configuration parameters of the given device into this
     * data structure.  The device type <code>T</code> should be appropriate for the
     * derived class.
     * <h4>NOTE</h4>
     * &middot; This method calls <code>{@link #getScadaRecords()}</code>.  
     * Do not call this method during construction!
     * </p> 
     *  
     * @param devSmf    hardware from which configuration information is acquired
     *
     * @throws BadStructException  SCADA data structure fields are ill-defined/incompatible 
     * @throws ConnectionException  unable to connect to PV channel
     * @throws GetException  general get exception (unable to get all parameter values)
     * 
     * @author Christopher K. Allen
     * @since  Apr 16, 2014
     */
    public void acquireConfiguration(ProfileDevice smfDev) throws ConnectionException, GetException, BadStructException {
        for (ScadaRecord rec : this.getScadaRecords()) {
            rec.loadHardwareValues(smfDev);
        }
    }

    /**
     * <p>
     * Sets the configuration of the given device to that represented
     * in this data structure.  Note that the configuration is applied 
     * to the device regardless of whether is the same device this configuration
     * was taken from (the device IDs not not necessary match).
     * <h4>NOTE</h4>
     * &middot; This method calls <code>{@link #getScadaRecords()}</code>.  
     * Do not call this method during construction!
     * </p> 
     *
     * @param smfDev    device to be configured
     * 
     * @throws BadStructException  SCADA data structure fields are ill-defined/incompatible 
     * @throws ConnectionException  unable to connect to PV channel
     * @throws PutException  general put exception (unable to set all parameter values)
     *
     * @author Christopher K. Allen
     * @since  May 1, 2012
     */
    public void     applyConfiguration(ProfileDevice   smfDev) 
        throws BadStructException, ConnectionException, PutException
    {
        for (ScadaRecord rec : this.getScadaRecords()) {
            rec.setHardwareValues(smfDev);
        }
    }
    
    /**
     * Applies the configuration represented in this data structure to the 
     * device in the given accelerator sequence with the same device ID. If
     * there is no such device then the method returns false.
     *
     * @param smfSeq    this data structure instance should contain the configuration information for a device
     *                  in this accelerator sequence, a device with the proper ID
     *                  
     * @return          <code>true</code> if the target device was found and its configuration updated,
     *                  <code>false</code> otherwise
     * 
     * @throws BadStructException  data structure fields are ill-defined/incompatible 
     * @throws ConnectionException  unable to connect to PV channel
     * @throws PutException  general put exception (unable to set all parameter values)
     *
     * @author Christopher K. Allen
     * @since  May 1, 2012
     */
    public boolean  appyConfigurationById(AcceleratorSeq smfSeq) 
        throws BadStructException, ConnectionException, PutException
    {
        AcceleratorNode smfDev = smfSeq.getNodeWithId(this.strDevId);
        
        if (smfDev == null)
            return false;
        
        if ( !(smfDev instanceof ProfileDevice) )
            return false;
        
        ProfileDevice smfDevPrf = (ProfileDevice)smfDev;
        
        this.applyConfiguration(smfDevPrf);
        
        return true;
    }

    
    /*
     * DataListener Interface
     */

    /**
     * Returns the data label used to store data for the given
     * version number.  The current label  will be returned for
     * all version greater than or equal to the current version number. 
     * 
     * @param lngVersion    storage version number
     * 
     * @return              data label used for the given storage format version
     *
     * @author Christopher K. Allen
     * @since  Oct 13, 2014
     */
    public String dataLabel(long lngVersion) {
        String  strLblVer = this.dataLabel();
        
        if (lngVersion < 3) {
            strLblVer = MainApplication.convertPtaDataLabelToVer3(strLblVer);
            strLblVer = strLblVer.replace("ScannerConfig", "DeviceConfig");
        }
        
        return strLblVer;
    }
    
    /**
     * Returns the string label used to identify
     * stored data for this data structure.
     *
     * @since   Mar 17, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.tools.data.DataListener#dataLabel()
     */
    @Override
    public String dataLabel() {
        return this.getClass().getCanonicalName();
    }


    /**
     * Load the contents of this data structure from the given 
     * data source exposing the <code>DataAdaptor</code> 
     * interface.
     * 
     * @param   daptSrc         data source used to populate this data structure
     *
     * @see xal.tools.data.DataListener#update(xal.tools.data.DataAdaptor)
     *
     * @author Christopher K. Allen
     * @since  Apr 14, 2014
     */
    @Override
    public void update(DataAdaptor daptSrc) {

        // Get the device ID of the configuration data
        //  (The version information is not used
        this.strDevId = daptSrc.stringValue(STR_ATTR_DEVID);
        @SuppressWarnings("unused")
        long    lngVer = daptSrc.longValue(STR_ATTR_FMTVER);
    }


    /**
     * Save the contents of this data structure to the
     * data sink behind the <code>DataAdaptor</code>
     * interface.
     *
     * @param snkData   persistent data store
     *
     * @see xal.tools.data.DataListener#write(xal.tools.data.DataAdaptor)
     *
     * @author Christopher K. Allen
     * @since  Apr 14, 2014
     */
    @Override
    public void write(DataAdaptor snkData) {
        
        // Write out device id and the data format version number
        snkData.setValue( STR_ATTR_DEVID, this.getDeviceId() );
        snkData.setValue( STR_ATTR_FMTVER, LNG_VAL_FMTVER );
    }
    
    
    /*
     * Object Overrides
     */

    /**
     * Uses an <code>XmlDataAdaptor</code> to write out the contents of this
     * configuration to a memory buffer and returns it.
     *
     * @see java.lang.Object#toString()
     *
     * @author Christopher K. Allen
     * @since  Apr 17, 2014
     */
    @Override
    public String toString() {
        
//        InMemoryDataAdaptor daptBuf = new InMemoryDataAdaptor();
        XmlDataAdaptor  daptBuf = XmlDataAdaptor.newEmptyDocumentAdaptor();
        this.write(daptBuf);
        
        StringWriter    wtrBuf  = new StringWriter();
        daptBuf.writeTo(wtrBuf);
        
        return wtrBuf.toString();
    }

    
    
    /*
     * Support Methods
     */
    
    /**
     * <p>
     * Returns the collection of SCADA records for configuration parameters
     * contained in this data structure.  
     * <h4>NOTE</h4>
     * &middot; Do not call this method during construction!
     * </p> 
     *  
     * @return  all the SCADA records managed by this data structure
     *
     * @author Christopher K. Allen
     * @since  Apr 15, 2014
     */
    protected abstract List<ScadaRecord> getScadaRecords(); 
 
}
