/**
 * MeasurementConfiguration.java
 *
 * @author Christopher K. Allen
 * @since  Apr 24, 2012
 *
 */
package xal.app.pta.daq;

import xal.app.pta.MainApplication;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;
import xal.service.pvlogger.PvLoggerException;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.WireHarp;
import xal.smf.impl.WireScanner;
import xal.smf.impl.profile.ProfileDevice;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class stores the configuration parameters of a given set of measurement devices.
 * It represents a snapshot of the hardware devices at an instance in time.  
 * The configuration snapshot can be saved to disk or recovered from disk and restored
 * to the wire scanners, using the (using the <code>{@link DataListener}</code> interface). 
 * 
 * <p>
 * <b>Ported from XAL on Jul 16, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author Christopher K. Allen
 * @since   Apr 24, 2012
 */
public class MachineConfig implements DataListener {

    
    /*
     * Global Constants
     */

    /**  Data format version */
    private static final double         DBL_VER_FMT = 1.0;

    
    /** The data node label for device configuration data - used in <code>DataListener</code> interface */
    private static final String         STR_LBL_CFG_DEV = "device";
    
    /** The attribute label for the device configuration data type */
    private static final String         STR_ATTR_CFG_TYPE = "type";
    

      
//    /**
//     * Check that all the configuration parameter channels are available for
//     * all the given devices.   
//     *
//     * @param lstDevs   set of hardware devices under test
//     * @param dblTmOut  maximum time allowed to make connections (in seconds)
//     * 
//     * @return          <code>true</code> if all connections are available,
//     *                  <code>false</code> otherwise
//     *                  
//     * @throws BadChannelException  there exists an unbound channel handle in the given
//     *                              set of SCADA field descriptors
//     *
//     *
//     * @author Christopher K. Allen
//     * @since  Mar 16, 2011
//     */
//    public static boolean testConnection(List<ProfileDevice> lstDevs, double dblTmOut) 
//        throws BadChannelException 
//    {
//
//        boolean     bolResult = true;
//        
//        for (ProfileDevice smfDev : lstDevs) {
//            for (DeviceConfig cfg : this)
//
//            if (smfDev.testConnection(dblTmOut) == false) { 
//                MainApplication.getEventLogger().
//                    logWarning(MachineConfig.class, "Could not connect with device " + ws.getId());
//                
//                bolResult = false;
//            }
//        }
//        
//        return bolResult;
//    }

    /**
     * <p>
     * Create a new <code>MeasurementConfiguration</code> object by acquiring the configuration
     * data from the given list of profile devices and packaging them up.
     * </p> 
     * <p>
     * We connect to each device in turn, acquiring the configuration parameters
     * from their buffers.  If anything goes wrong we throw
     * a channel access exception.
     * </p>
     *
     * @param lstDevs    list of profile devices to acquire configuration parameters 
     *
     * @return  data structure containing all the acquired configuration parameters
     *          for the given device list
     *  
     * @throws PvLoggerException        could not take PV Logger machine snapshot 
     * @throws ConnectionException      could not connect to a profile device
     * @throws GetException             could not read data from a profile device
     * 
     * @since  Mar 19, 2010
     * @author Christopher K. Allen
     */
    public static MachineConfig acquire(List<? extends ProfileDevice> lstDevs) 
        throws PvLoggerException, ConnectionException, GetException
    {
        return new MachineConfig(lstDevs);
    }
    
    /**
     * Create a new <code>MeasurementConfiguration</code> object but reading the
     * configuration information from the storage behind the <code>DataAdaptor</code>
     * interface.  A <code>IllegalArgumentException</code> is thrown
     * for general read errors, kind of forced into this since
     * we can only throw runtime errors if we want to employ the
     * <code>DataListener</code> interface.
     *
     * @param daptSrc   data source from which to populate this configuration information
     * 
     * @return  data structure containing configuration information
     *          stored in the given data source
     *  
     * @throws IllegalArgumentException  unknown data format 
     * 
     * @since  Mar 19, 2010
     * @author Christopher K. Allen
     */
    public static MachineConfig load(DataAdaptor daptSrc) throws IllegalArgumentException {
        return new MachineConfig(daptSrc);
    }
    
    
    
    
    
    /*
     * Global Attributes
     */
    
    /** Formats the time stamps to and from string representation */
    private static DateFormat           FMT_TMSTMP = DateFormat.getInstance();
    
    
    
    /*
     * Local Attributes
     */
    
    /** PV Logger snapshot ID of the machine state at data acquisition */
    private long                    lngPvLogId;
    
    /** The time at which the measurement data was taken */
    private Date                    datTmStmp;
    
    /** Notes and comments on the measurement */
    private String                  strNotes;
    
    /** Map of device identifier to configuration data for the device */
    private final Map<String, DeviceConfig> mapMchnCfg;
    

    
    /*
     * Initialization
     */
    
    /**
     * <p>
     * Create a new <code>MeasurementConfiguration</code> object by requesting and storing the configuration
     * parameters from the given list of profile devices.
     * </p> 
     * <p>
     * We connect to each device in turn, then get the configuration information
     * from their buffers.  Note that if anything goes wrong we throw
     * a channel access exception.
     * </p>
     *
     * @param lstDevs    list of profile devices to request configuration information and populate data set.
     *
     * @throws PvLoggerException        could not take PV Logger machine snapshot 
     * @throws ConnectionException      could not connect to a profile device
     * @throws GetException             could not read data from a profile device
     * 
     * @since     Mar 19, 2010
     * @author    Christopher K. Allen
     */
    public MachineConfig(List<? extends ProfileDevice> lstDevs) 
        throws PvLoggerException, ConnectionException, GetException 
    {
        String strCmt = "DATA ACQUISITION: " + this.dataLabel();
        
        this.mapMchnCfg = this.acquireConfiguration(lstDevs);
        this.datTmStmp  = MainApplication.timeStamp();
        this.lngPvLogId = MainApplication.pvLoggerSnapshot(strCmt);
    }
    
    /**
     * Create a new <code>MachineConfig</code> object by reading the
     * data from the storage behind the <code>DataAdaptor</code>
     * interface.  A <code>IllegalArgumentException</code> is thrown
     * for general data read errors, kind of forced into this since
     * we can only throw runtime errors if we want to employ the
     * <code>DataListener</code> interface.
     *
     * @param daptSrc   data source from which to populate this data set
     * 
     * @throws IllegalArgumentException  unknown data format 
     *
     * @since     Mar 19, 2010
     * @author    Christopher K. Allen
     */
    public MachineConfig(DataAdaptor daptSrc) throws IllegalArgumentException {
        this();
        
        this.update(daptSrc);
    }
    
    /**
     * Creates an empty <code>MachineConfig</code> object.  Note that both the
     * time stamp and PV Logger ID are null since their is no data to point to.
     *
     * @author Christopher K. Allen
     * @since  Apr 17, 2014
     * 
     * @deprecated  Should not be necessary since all data has an ID
     */
    @Deprecated
    public MachineConfig() {
        this.datTmStmp  = null;
        this.lngPvLogId = -1;
        this.mapMchnCfg = new HashMap<String, DeviceConfig>();
    }
    
    
    /*
     * Operations
     */
    
    
    /**
     * Returns number of wire scanner devices composing the configuration.
     * 
     * @return  returns the number of devices configured in this <code>MachineConfig</code> instance
     *
     * @author Christopher K. Allen
     * @since  May 10, 2012
     */
    public int  getDeviceCount() {
        return this.mapMchnCfg.size();
    }
    
    /**
     * Returns the string identifiers of the devices composing this machine configuration.  If the
     * machine configuration has not yet been defined an empty set is returned (i.e., a set of 
     * size zero).
     *
     * @return  the identifiers of all devices included in this configuration set.
     *
     * @author Christopher K. Allen
     * @since  Jun 19, 2012
     */
    public Set<String> getDeviceIds() {
        
        if (this.getDeviceCount() > 0)
            return this.mapMchnCfg.keySet();
        
        return new TreeSet<String>();
    }
    
    /**
     * Returns the collection of all configuration data structures
     * for every device managed by this object.
     * 
     * @return  all device configurations currently managed by this object
     *
     * @author Christopher K. Allen
     * @since  Apr 16, 2014
     */
    public Collection<? extends DeviceConfig> getDeviceConfigurations() {
        if (this.getDeviceCount() > 0)
            return this.mapMchnCfg.values();
        
        return new TreeSet<DeviceConfig>();
    }
    
    /**
     * <p>
     * Applies the current device configuration information to the given machine
     * sector.  The method returns a collection of device IDs indicating all 
     * the devices that were not successfully configured.  Specifically, if the configuration 
     * update of a particular device fails its device ID is to the collection of returned device IDs.
     * </p>
     * <p>
     * <h4>NOTE</h4>
     * Only the wire scanners for which we have configuration information are affected in <var>smfSeq</var>.
     * </p> 
     *
     * @param smfSeq    the accelerator sequence whose wire scanners are to be configured.
     * 
     * @return          collection of wire scanner IDs whose configuration was successfully updated
     *
     * @author Christopher K. Allen
     * @since  May 1, 2012
     */
    public Collection<String> applyConfiguration(AcceleratorSeq smfSeq)  {
        List<String>    lstSuccessIds = new LinkedList<String>();
        
        // For each device in the configuration set
        for (Map.Entry<String, ? extends DeviceConfig> entry : this.mapMchnCfg.entrySet()) {
            String          strDevId  = entry.getKey();
            DeviceConfig    cfgDevice = entry.getValue();

            // Check if device is contained in the given accelerator sequence
            //  Skip if it is not
            if (smfSeq.getNodeWithId(strDevId) == null) 
                continue;
            
            try {
                // Send the device configuration
                if ( cfgDevice.appyConfigurationById(smfSeq) )
                    lstSuccessIds.add(strDevId);
            
            } catch (ConnectionException e) {
                String  strMsg = "Connection Error attemping to configure device " + strDevId + " in sequence " + smfSeq;
                MainApplication.getEventLogger().logException(this.getClass(), e, strMsg);
                
            } catch (PutException e) {
                String  strMsg = "Put Error attemping to configure device " + strDevId + " in sequence " + smfSeq;
                MainApplication.getEventLogger().logException(this.getClass(), e, strMsg);
                
            }
        }
        
        return lstSuccessIds;
    }
    
    /**
     * <p>
     * Applies the current device configuration information to all the applicable devices
     * in the given accelerator.  The method returns a collection of device IDs indicating all 
     * the devices that were not successfully configured, but have configuration information
     * contained in this data structure.  Specifically, if the configuration 
     * update of a particular device fails its device ID is added to the collection of returned device IDs.
     * </p>
     * <p>
     * <h4>NOTE</h4>
     * Only the wire scanners for which we have configuration information herein are affected in <var>smfSeq</var>.
     * </p> 
     * 
     * @param  smfAccel the accelerator containing devices to be updated with this configuration information
     * 
     * @return          collection of wire scanner IDs whose configuration was successfully updated
     *
     * @author Christopher K. Allen
     * @since  May 4, 2012
     */
    public Collection<String>  applyConfiguration(Accelerator smfAccel) {
        List<String>    lstSuccessIds = new LinkedList<String>();
        
        // Get each sequence in the accelerator and apply the configuration
//        List<AcceleratorSeq>    lstSmfSeqs = smfAccel.getAllSeqs();
        List<AcceleratorSeq>    lstSmfSeqs = smfAccel.getSequences();
        for (AcceleratorSeq smfSeq : lstSmfSeqs) {
            Collection<String>  lstDevIds = this.applyConfiguration(smfSeq);
            
            lstSuccessIds.addAll(lstDevIds);
        }

        return lstSuccessIds;
    }

    
    
    /*
     * DataListener Interface - File Persistence
     */
    
    /**
     * #dataLabel() provides the name used to identify the class in an 
     * external data source.
     * 
     * @return The tag for this data node.
     * 
     * @since Apr 24, 2012
     * @see xal.tools.data.DataListener#dataLabel()
     */
    @Override
    public String dataLabel() {
        return this.getClass().getName();
    }

    
    /**
     * Instructs the receiver to update its data based on the given adaptor.
     * 
     * @param daptSrc   data source populating this data structure
     * 
     * @throws IllegalArgumentException  the format version of the given data source is unknown 
     * 
     * @since Apr 24, 2012
     * @see xal.tools.data.DataListener#update(xal.tools.data.DataAdaptor)
     */
    @Override
    public  void update(DataAdaptor daptSrc) throws IllegalArgumentException {
        DataAdaptor     daptDataSet = daptSrc.childAdaptor(this.dataLabel());
//        DataAdaptor         daptDataSet = daptSrc;
        
        // Get the version information
        double          dblVer = 0;
        if (daptDataSet.hasAttribute("ver"))
            dblVer = daptDataSet.doubleValue("ver");
        if (dblVer > DBL_VER_FMT)
            throw new IllegalArgumentException("Unknown data format (format version is unknown)");
        
        // Get the PV Logger snapshot ID
        if (daptDataSet.hasAttribute("pvlogid"))
            this.lngPvLogId = daptDataSet.longValue("pvlogid");
        
        // Get the time stamp
        if (daptDataSet.hasAttribute("time")) {
            String      strTime = daptDataSet.stringValue("time");
            
            try {
                this.datTmStmp = FMT_TMSTMP.parse(strTime);
                
            } catch (ParseException e) {
                throw new IllegalArgumentException("Bad time stamp", e);
            }
        }
        
        // Get Comment string
        if (daptDataSet.hasAttribute("comment")) 
            this.strNotes = daptDataSet.stringValue("comment");
        

        // Get the configuration data for each device
        List<DataAdaptor>      lstDapts = daptDataSet.childAdaptors(STR_LBL_CFG_DEV);
        for (DataAdaptor daptCfg : lstDapts) {
            if (!daptCfg.hasAttribute(STR_ATTR_CFG_TYPE))
                throw new IllegalArgumentException("Device configuration node has no data type: " + daptCfg);
            
            String  strType = daptCfg.stringValue(STR_ATTR_CFG_TYPE);
            try {
                @SuppressWarnings("unchecked")
                Class<DeviceConfig>       clsType  = (Class<DeviceConfig>) Class.forName(strType);
                Constructor<DeviceConfig> ctorType = clsType.getConstructor();
                DeviceConfig              devCfg   = ctorType.newInstance();
                
                devCfg.update(daptCfg);
                String  strDevId = devCfg.getDeviceId();
                
                this.mapMchnCfg.put(strDevId, devCfg);
                
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Unable to create device configuration structure of type " + strType);
                
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Unable to create device configuration structure of type " + strType);
                
            } catch (SecurityException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Unable to create device configuration structure of type " + strType);

            } catch (InstantiationException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Unable to create device configuration structure of type " + strType);
                
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Unable to create device configuration structure of type " + strType);
                
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Unable to create device configuration structure of type " + strType);
                
            }
            
        }
    }

    /**
     * Instructs the receiver to write its data to the adaptor for external
     * storage.
     * 
     * @param daptSink The data adaptor corresponding to this object's data 
     * node.
     * 
     * @since Apr 24, 2012
     * @see xal.tools.data.DataListener#write(xal.tools.data.DataAdaptor)
     */
    @Override
    public void write(DataAdaptor daptSink) {
//        DataAdaptor     daptData = daptSink.createChild(this.dataLabel());
        DataAdaptor     daptMach = daptSink;
        
        daptMach.setValue("ver", DBL_VER_FMT);
        daptMach.setValue("pvlogid", this.lngPvLogId);
        daptMach.setValue("time", FMT_TMSTMP.format(this.datTmStmp));
        daptMach.setValue("comment", this.strNotes);
        
        for (Map.Entry<String, ? extends DeviceConfig> entry : this.mapMchnCfg.entrySet()) {
            DataAdaptor     daptDev = daptMach.createChild(STR_LBL_CFG_DEV);

            DeviceConfig    cfgDev  = entry.getValue();
            String          strType = cfgDev.getClass().getName();
            
            daptDev.setValue(STR_ATTR_CFG_TYPE, strType);
            cfgDev.write(daptDev);
        }
    }
    
    
    /*
     * Object Overrides
     */
    
    /**
     * Writes out all the machine configuration data structures to 
     * a string and returns it.
     *
     * @see java.lang.Object#toString()
     *
     * @author Christopher K. Allen
     * @since  Apr 17, 2014
     */
    @Override
    public String toString() {
        StringBuffer    bufOut = new StringBuffer();

        bufOut.append("PVLogger ID: " + this.lngPvLogId);
        bufOut.append("\n");
        bufOut.append("Data: " + this.datTmStmp);
        bufOut.append("\n");
        bufOut.append("Notes: " + this.strNotes);
        bufOut.append("\n");
        bufOut.append("__Device Configurations__");
        bufOut.append("\n");
        for (DeviceConfig cfgDev : this.mapMchnCfg.values())
            bufOut.append(cfgDev.toString());

        return bufOut.toString();
    }

    
    
    
    /*
     * Support Methods
     */
    
    /**
     * <p>
     * Requests the configuration parameters from the given list of devices.
     * The list is expected to contain profile diagnostic devices
     * which will serve up their configuration.
     * </p>
     * <p>
     * We connect to each device in turn, then get the configuration parameters
     * from their buffers.  Note that if anything goes wrong we throw
     * a channel access exception.
     * </p>
     *
     * @param lstDevs   list of profile devices where configuration information is acquired
     * 
     * @return  a map containing all the configuration parameters for each device, as a collection
     *                  of pairs (devId, devData)
     * 
     * @throws ConnectionException      could not connect to a profile device
     * @throws GetException             could not read data from a profile device
     * 
     * @since  Mar 19, 2010
     * @author Christopher K. Allen
     */
    private Map<String,DeviceConfig> acquireConfiguration(List<? extends ProfileDevice> lstDevs) throws ConnectionException, GetException {
        
        Map<String, DeviceConfig> mapMmtCfg = new HashMap<String, DeviceConfig>();

        // Acquire the configuration parameters from each device
        for (ProfileDevice smfDev : lstDevs) {
            
            String          strDevId = smfDev.getId();
            
            if (smfDev instanceof WireScanner) {
                WireScanner       ws     = (WireScanner)smfDev;
                ScannerConfig    cfgDev = ScannerConfig.acquire(ws);
                
                mapMmtCfg.put(strDevId, cfgDev);

            } else if (smfDev instanceof WireHarp) {
                WireHarp        wh     = (WireHarp)smfDev;
                HarpConfig     cfgDev = HarpConfig.acquire(wh);
                
                mapMmtCfg.put(strDevId, cfgDev);

            } else {
                
                MainApplication.getEventLogger().logWarning(this.getClass(), "Unknown profile device type: " + smfDev.getClass() );
                
            }
        }
        
        return mapMmtCfg;
    }

}
