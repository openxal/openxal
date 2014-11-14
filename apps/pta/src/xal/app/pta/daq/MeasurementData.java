/**
 * MeasurementData.java
 *
 *  Created	: Mar 19, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.daq;

import xal.app.pta.MainApplication;
import xal.ca.BadChannelException;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.extension.application.Application;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;
import xal.service.pvlogger.PvLoggerException;
import xal.smf.AcceleratorNode;
import xal.smf.impl.WireHarp;
import xal.smf.impl.WireScanner;
import xal.smf.impl.profile.ProfileDevice.IProfileData;
import xal.smf.impl.profile.ProfileDevice;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates all the data and peripheral information
 * acquired from a profile scan involving multiple
 * profile devices. The class maintains a collection of data structures
 * (type <code>ScannerData</code>) of acquisition data, one data
 * structure for each acquisition device used in the measurement.
 * 
 * <p>
 * <b>Ported from XAL on Jul 16, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Mar 19, 2010
 * @author Christopher K. Allen
 */
public class MeasurementData implements DataListener {

    /**
     * Check that all the data acquisition channels are available for
     * all the given devices.   
     *
     * @param lstDevs   set of hardware devices under test
     * @param dblTmOut  maximum time allowed to make connections (in seconds)
     * 
     * @return          <code>true</code> if all connections are available,
     *                  <code>false</code> otherwise
     *                  
     * @throws BadChannelException  there exists an unbound channel handle in the given
     *                              set of SCADA field descriptors
     *
     *
     * @author Christopher K. Allen
     * @since  Mar 16, 2011
     */
    public static boolean testConnection(List<WireScanner> lstDevs, double dblTmOut) 
        throws BadChannelException 
    {

        boolean     bolResult = true;
        
        for (AcceleratorNode smfDev : lstDevs) {
            
            // Check that we have a wire scanner
            if ( !(smfDev instanceof WireScanner) )
                continue;
            
            WireScanner     ws       = (WireScanner)smfDev;

            if (ScannerData.testConnection(ws, dblTmOut) == false) { 
                MainApplication.getEventLogger().
                    logWarning(MeasurementData.class, "Could not connect with device " + ws.getId());
                
                bolResult = false;
            }
        }
        
        return bolResult;
    }

    /**
     * <p>
     * Create a new <code>MeasurementData</code> object by acquiring data
     * from the given list of profile devices.
     * </p> 
     * <p>
     * We connect to each device in turn, acquiring the measurement data
     * from their buffers.  (The measurement data was taken from the preceding
     * scan.) Note that if anything goes wrong we throw
     * a channel access exception.
     * </p>
     *
     * @param lstDevs    list of profile devices to acquire data and populate data set.
     *
     * @return  data structure containing all the acquired measurement data 
     *          for the given device list
     *  
     * @throws PvLoggerException        could not take PV Logger machine snapshot 
     * @throws ConnectionException      could not connect to a profile device
     * @throws GetException             could not read data from a profile device
     * 
     * 
     * @since  Mar 19, 2010
     * @author Christopher K. Allen
     */
    public static MeasurementData acquire(List<ProfileDevice> lstDevs) 
        throws PvLoggerException, ConnectionException, GetException
    {
        return new MeasurementData(lstDevs);
    }
    
    /**
     * Create a new <code>MeasurementData</code> object but reading the
     * data from the storage behind the <code>DataAdaptor</code>
     * interface.  A <code>IllegalArgumentException</code> is thrown
     * for general data read errors, kind of forced into this since
     * we can only throw runtime errors if we want to employ the
     * <code>DataListener</code> interface.
     *
     * @param daptSrc   data source from which to populate this data set
     * 
     * @return  data structure containing acquired measurement data 
     *          stored in the given data source
     *  
     * @throws IllegalArgumentException  unknown data format 
     * 
     * @since  Mar 19, 2010
     * @author Christopher K. Allen
     */
    public static MeasurementData load(DataAdaptor daptSrc) throws IllegalArgumentException {
        return new MeasurementData(daptSrc);
    }
    
    
    
    
    /*
     * Global Constants
     */

    /**  Data format version */
    private static final long           LNG_VER_FMT = 3;  // or 1.3

    
    
    /*
     * Global Attributes
     */
    
    /** General time stamp format used for reading (version insensitive) */
    private static DateFormat           FMT_TM_STMP = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
    
    
    
    /*
     * Local Attributes
     */
    
    /** PV Logger snapshot ID of the machine state at data acquisition */
    private long                    lngPvLogId;
    
    /** The time at which the measurement data was taken */
    private Date                    datTmStmp;
    
    /** Notes and comments on the measurement */
    private String                  strNotes;
    
    /** List of acquired data for each hardware device */
    private final Map<String, IProfileData> mapMsmtData;
    

    
    /*
     * Initialization
     */
    
    /**
     * <p>
     * Create a new <code>MeasurementData</code> object by acquiring data
     * from the given list of profile devices.
     * </p> 
     * <p>
     * We connect to each device in turn, then get the measurement data
     * from their buffers.  (The measurement data was taken from a previous
     * scan.) Note that if anything goes wrong we throw
     * a channel access exception.
     * </p>
     *
     * @param lstDevs    list of profile devices to acquire data and populate data set.
     *
     * @throws PvLoggerException        could not take PV Logger machine snapshot 
     * @throws ConnectionException      could not connect to a profile device
     * @throws GetException             could not read data from a profile device
     * 
     * @since     Mar 19, 2010
     * @author    Christopher K. Allen
     */
    public MeasurementData(List<ProfileDevice> lstDevs) 
        throws PvLoggerException, ConnectionException, GetException 
    {
        String strCmt = "DATA ACQUISITION: " + this.dataLabel();
        
        this.mapMsmtData = this.acquireData(lstDevs);
        this.datTmStmp   = MainApplication.timeStamp();
        
//        System.out.println("MeasurementData(List<ProfileDevice>) - before PV Logger snapshot");
        
        this.lngPvLogId  = MainApplication.pvLoggerSnapshot(strCmt);
        
//        System.out.println("MeasurementData(List<ProfileDevice>) - after PV Logger snapshot");
    }
    
    /**
     * Create a new <code>MeasurementData</code> object by reading the
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
    public MeasurementData(DataAdaptor daptSrc) throws IllegalArgumentException {
        this.datTmStmp   = null;
        this.lngPvLogId  = -1;
        this.mapMsmtData = new HashMap<String, IProfileData>();
        
        this.update(daptSrc);
    }
    
    
    /**
     * Set any notes and/or comments associated with the measurement set. 
     *
     * @param strNotes  associated comments and notes
     *
     * @since  Apr 22, 2010
     * @author Christopher K. Allen
     */
    public void setNotes(String strNotes) {
        this.strNotes = strNotes;
    }
    
    /*
     * Operations
     */
    
    /**
     * Returns the set of profile devices used to 
     * obtain this measurement data.  There is one
     * complete data set for each device listed in 
     * the returned set.
     *
     * @return  set of unique profile device identifiers
     * 
     * @since  Apr 22, 2010
     * @author Christopher K. Allen
     */
    public Set<String> getDeviceIdSet() {
        return this.mapMsmtData.keySet();
    }
    
    /**
     * Get the measurement data for the given device.
     *
     * @param strDevId  profile data device identifier
     * 
     * @return  the acquisition data taken from the given device
     * 
     * @since  Mar 19, 2010
     * @author Christopher K. Allen
     */
    public IProfileData   getDataForDeviceId(String strDevId) {
        return this.mapMsmtData.get(strDevId);
    }
    
    /**
     * Returns all the measurement data as a collection of data structures,
     * one data structure for each acquisition device used in the measurement.
     *
     * @return  the collection of all acquisition for each device in this data set
     * 
     * @since  Mar 19, 2010
     * @author Christopher K. Allen
     */
    public Collection<IProfileData>       getDataSet() {
        return this.mapMsmtData.values();
    }
    
    /**
     * Returns the time at which the measurement data was 
     * acquired.
     *
     * @return  time stamp for the data set
     * 
     * @since  Mar 19, 2010
     * @author Christopher K. Allen
     */
    public Date         getTimeStamp() {
        return this.datTmStmp;
    }
    
    /**
     * Returns the PV Logger snapshot ID of the machine snapshot
     * taken at the time of data acquisition.
     *
     * @return  PV Logger ID of the machine state at acquisition
     * 
     * @since  Mar 19, 2010
     * @author Christopher K. Allen
     */
    public long         getPvLoggerId() {
        return this.lngPvLogId;
    }

    /**
     * Returns any comments associated with the measurement
     * set or <code>null</code> if none. 
     *
     * @return  measurement set comments and notes, 
     *          or <code>null</code> if none
     *
     * @since  Apr 22, 2010
     * @author Christopher K. Allen
     */
    public String getNotes() {
        return strNotes;
    }

    
    
    /*
     * DataListener Interface
     */
    
//    /**
//     * Returns the data label used to store data for the given
//     * version number.  The current label  will be returned for
//     * all version greater than or equal to the current version number. 
//     * 
//     * @param lngVersion    storage version number for the parent data node
//     * 
//     * @return              data label used for the given storage format version
//     *
//     * @author Christopher K. Allen
//     * @since  Oct 13, 2014
//     */
//    public String dataLabel(long lngVersion) {
//        String  strLblVer = this.dataLabel();
//        
//        if (lngVersion < 2) {
//            strLblVer = MainApplication.convertPtaDataLabelToVer3(strLblVer);
//        }
//        
//        return strLblVer;
//    }
    
    /** 
     * dataLabel() provides the name used to identify the class in an 
     * external data source.
     * 
     * @return The tag for this data node.
     */
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
     */
    public void update(DataAdaptor daptSrc) throws IllegalArgumentException {
        
        // THis is the parent PTA level data adaptor
        // Get the data adaptor node for the measurements
        String  strLblMsmts = this.dataLabel();
        
        DataAdaptor     daptMsmts = daptSrc.childAdaptor(strLblMsmts);
        
        // Check to make sure we have the current data label and 
        //  bail out if we don't.  Needed because of an inconsistency in versioning
        //  introduced when porting to Open XAL
        if (daptMsmts == null) {
            
            strLblMsmts = MainApplication.convertPtaDataLabelToVer3(strLblMsmts);
            daptMsmts   = daptSrc.childAdaptor(strLblMsmts);
        }
        
        if (daptMsmts == null)
            throw new IllegalArgumentException("Unreadable data - Bad version number on data node " + this.dataLabel());
        
        
        // Get the version information
        //  Must account for the earlier mistake of using decimal versions (i.e., non-integer)
        long         lngVerMsmt = 0;
        
        if (daptMsmts.hasAttribute("ver")) {
            String  strVerMsmt = daptSrc.stringValue("ver");

            try { 

                lngVerMsmt = Long.parseLong(strVerMsmt);

            } catch (NumberFormatException e) {

                lngVerMsmt = 2;  // for "1.2"
            };
        }
        
        if (lngVerMsmt > LNG_VER_FMT)
            throw new IllegalArgumentException("Unknown data format (format version is unknown)");
        
        // Get the PV Logger snapshot ID
        if (daptMsmts.hasAttribute("pvlogid"))
            this.lngPvLogId = daptMsmts.longValue("pvlogid");
        
        // Get the time stamp
        if (daptMsmts.hasAttribute("time")) {
            String      strTime = daptMsmts.stringValue("time");
            
            try {
                if (lngVerMsmt < 1) 
                    this.datTmStmp = DateFormat.getDateTimeInstance().parse(strTime);
                else
                    this.datTmStmp = FMT_TM_STMP.parse(strTime);
                
            } catch (ParseException e) {

                this.datTmStmp = new Date(0L);
                System.err.println("Data file format error - Bad time stamp; " + e.getMessage());
                Application.displayWarning("Bad Time Stamp", "Unable to parse data set time stamp", e);
            }
        }
        
        // Get Comment string
        if (daptMsmts.hasAttribute("comment")) 
            this.strNotes = daptMsmts.stringValue("comment");
        

        // Get the measurement data for each wire scanner
        //  First check the data labels for each version
        //  If no data is found either the format is bad or there is not wire scanner
        //  data.  In the later can just create an empty list of data adaptors
        //  to keep flow control simple.
        String  strLblScanData = ScannerData.STR_LBL_PARENT;
        
        List<DataAdaptor>      lstScanDapts = daptMsmts.childAdaptors(strLblScanData);

        // Again, check to make sure we have the right version of data label
        if (lstScanDapts.size() == 0) {
            strLblScanData = MainApplication.convertPtaDataLabelToVer3(strLblScanData);
            strLblScanData = strLblScanData.replace("ScannerData", "DeviceData");
        
            lstScanDapts = daptMsmts.childAdaptors(strLblScanData);
        }
//        if (lstScanDapts.size() == 0) { 
//        
//            Application.displayWarning("Warning", "No wire scanner data found in file");
//
//        }
        for (DataAdaptor daptMsmt : lstScanDapts) {
            ScannerData  datMsmt = ScannerData.load(daptMsmt);
            
            String           strDevId = datMsmt.strDevId;
            this.mapMsmtData.put(strDevId, datMsmt);
        }
        
        // Get the measurement data for each harp
        //  Again, check the data label for each version and proceed accordingly
        String  strLblHarpData = HarpData.STR_LBL_PARENT;
        
        List<DataAdaptor>   lstHarpDapts = daptMsmts.childAdaptors(strLblHarpData);
        
        if (lstHarpDapts.size() == 0) {
            strLblHarpData = MainApplication.convertPtaDataLabelToVer3(strLblHarpData);

            lstHarpDapts = daptMsmts.childAdaptors(strLblHarpData);
        }
        
        for (DataAdaptor daptMsmt : lstHarpDapts) {
            HarpData    datMsmt = HarpData.load(daptMsmt);
            
            String      strDevId = datMsmt.strDevId;
            this.mapMsmtData.put(strDevId, datMsmt);
        }
    }
    
    /**
     * Instructs the receiver to update its data based on the given adaptor.
     * 
     * @param daptSrc   data source populating this data structure
     * 
     * @throws IllegalArgumentException  the format version of the given data source is unknown 
     * 
     */
    public void update_old(DataAdaptor daptSrc) throws IllegalArgumentException {
        
        DataAdaptor     daptDataSet = daptSrc.childAdaptor(this.dataLabel());
        
        // Get the version information
        double          dblVer = 0;
        if (daptDataSet.hasAttribute("ver"))
            dblVer = daptDataSet.doubleValue("ver");
        if (dblVer > LNG_VER_FMT)
            throw new IllegalArgumentException("Unknown data format (format version is unknown)");
        
        // Get the PV Logger snapshot ID
        if (daptDataSet.hasAttribute("pvlogid"))
            this.lngPvLogId = daptDataSet.longValue("pvlogid");
        
        // Get the time stamp
        if (daptDataSet.hasAttribute("time")) {
            String      strTime = daptDataSet.stringValue("time");
            
            try {
                if (dblVer < LNG_VER_FMT) 
                    this.datTmStmp = DateFormat.getDateTimeInstance().parse(strTime);
                else
                    this.datTmStmp = FMT_TM_STMP.parse(strTime);
//                  this.datTmStmp = MainApplication.getTimeStampFormat().parse(strTime);
                
            } catch (ParseException e) {
                this.datTmStmp = new Date(0L);
                System.err.println("Data file format error - Bad time stamp; " + e.getMessage());
//                throw new IllegalArgumentException("Bad time stamp", e);
            }
        }
        
        // Get Comment string
        if (daptDataSet.hasAttribute("comment")) 
            this.strNotes = daptDataSet.stringValue("comment");
        

        // Get the measurement data for each wire scanner
        List<DataAdaptor>      lstScanDapts = daptDataSet.childAdaptors(ScannerData.STR_LBL_PARENT);
        for (DataAdaptor daptMsmt : lstScanDapts) {
            ScannerData  datMsmt = ScannerData.load(daptMsmt);
            
            String           strDevId = datMsmt.strDevId;
            this.mapMsmtData.put(strDevId, datMsmt);
        }
        
        // Get the measurement data for each harp
        List<DataAdaptor>   lstHarpDapts = daptDataSet.childAdaptors(HarpData.STR_LBL_PARENT);
        for (DataAdaptor daptMsmt : lstHarpDapts) {
            HarpData    datMsmt = HarpData.load(daptMsmt);
            
            String      strDevId = datMsmt.strDevId;
            this.mapMsmtData.put(strDevId, datMsmt);
        }
    }
    
   /**
     * Instructs the receiver to write its data to the adaptor for external
     * storage.
     * 
     * @param daptSink The data adaptor corresponding to this object's data 
     * node.
     */
    public void write(DataAdaptor daptSink) {
        DataAdaptor     daptData = daptSink.createChild(this.dataLabel());
        
        daptData.setValue("ver", LNG_VER_FMT);
        daptData.setValue("pvlogid", this.lngPvLogId);
//        daptData.setValue("time", MainApplication.getTimeStampFormat().format(this.datTmStmp));
        daptData.setValue("time", FMT_TM_STMP.format(this.datTmStmp));
        daptData.setValue("comment", this.strNotes);
        for (Map.Entry<String, IProfileData> entry : this.mapMsmtData.entrySet()) {
            IProfileData datMsmt = entry.getValue();

            datMsmt.write(daptData);
        }
    }
    
    
    /*
     * Support Methods
     */
    
//    /**
//     * Used to convert an XML label within a PTA data file to version 2
//     * from version 1 format.
//     * 
//     * @param strLblVer1    a PTA data file XML label in the version 1 format
//     * 
//     * @return              the corresponding PTA data file label in version 2 format
//     *
//     * @author Christopher K. Allen
//     * @since  Oct 8, 2014
//     */
//    private String  convertVersionedXmlLabel(String strLblVer1) {
//        String  strLblVer2 = strLblVer1.replace("xal.app", "gov.sns.apps");
//        
//        return strLblVer2;
//    }
    
    /**
     * <p>
     * Requests measurement data from the given list of devices.
     * The list is expected to contain profile diagnostic devices
     * which have measurement data in their buffers.
     * </p>
     * <p>
     * We connect to each device in turn, then get the measurement data
     * from their buffers.  Note that if anything goes wrong we throw
     * a channel access exception.
     * </p>
     *
     * @param lstDevs   list of profile devices where data is acquired
     * 
     * @return  a map containing all the measurement data as a collection
     *                  of pairs (devId, devData)
     * 
     * @throws ConnectionException      could not connect to a profile device
     * @throws GetException             could not read data from a profile device
     * 
     * @since  Mar 19, 2010
     * @author Christopher K. Allen
     */
    private Map<String,IProfileData> acquireData(List<ProfileDevice> lstDevs) throws ConnectionException, GetException {
        
        Map<String, IProfileData> mapMmtData = new HashMap<String, IProfileData>();

        // Acquire the measurement data from each device
        for (AcceleratorNode smfDev : lstDevs) {

            // Check that we have a wire scanner
            if ( smfDev instanceof WireScanner ) {

                WireScanner     smfScan  = (WireScanner)smfDev;
                String          strDevId = smfScan.getId();
                ScannerData     datDev   = ScannerData.acquire(smfScan);

                mapMmtData.put(strDevId, datDev);
                
            } else if ( smfDev instanceof WireHarp) {
                
                WireHarp    smfHarp  = (WireHarp)smfDev;
                String      strDevId = smfHarp.getId();
                HarpData    datDev   = HarpData.acquire(smfHarp);
                
                mapMmtData.put(strDevId, datDev);
                
            } else {
                
                MainApplication.getEventLogger().logWarning(this.getClass(), "Unknown profile device type: " + smfDev.getClass() );
                
            }
        }
        
        return mapMmtData;
    }
    
}
