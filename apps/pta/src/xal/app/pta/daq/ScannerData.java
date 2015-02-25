/**
 * ScannerData.java
 *
 *  Created	: Feb 3, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.daq;

import xal.app.pta.MainApplication;
import xal.ca.BadChannelException;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.tools.data.DataAdaptor;
import xal.smf.impl.WireScanner;
import xal.smf.impl.WireScanner.DataLivePt;
import xal.smf.impl.profile.ProfileDevice;
import xal.smf.impl.profile.ProfileDevice.ANGLE;
import xal.smf.impl.profile.SignalAttrSet;
import xal.smf.impl.profile.SignalSet;
import xal.smf.scada.ScadaAnnotationException;
import xal.smf.scada.ScadaFieldDescriptor;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

/**
 * Data structure containing the measurement data for one device
 * for one profile scan.
 * 
 * <p>
 * <b>Ported from XAL on Jul 16, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Feb 3, 2010
 * @author Christopher K. Allen
 */
public class ScannerData implements ProfileDevice.IProfileData, Serializable {


    
    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;
    
    
    /** The format version for persistent storage */
    private static final long  LNG_VAL_FMTVER = 3;
    
    /** The data label for measurement data - used in <code>DataAdaptors</code> */
    public static final String  STR_LBL_PARENT = ScannerData.class.getCanonicalName();
    
    /** The data format version attribute used in <code>DataListener</code> implementation */
    private static final String STR_ATTR_FMTVER = "ver";
    
    /** The device type ID attribute used in <code>DataListener</code> implementation */
    private static final String STR_ATTR_TYPID = "type";
    
    /** The device ID attribute used in <code>DataListener</code> implementation */
    private static final String STR_ATTR_DEVID = "dev";


    /** Collection of field descriptors for the device data channels */
    private static final Collection<ScadaFieldDescriptor>   COL_SFD_DATA;
    
    
    /*
     * Class Initialization
     */
    static {
//        List<ScadaFieldDescriptor>   lstFdLive = WireScanner.DataLivePt.getFieldDescriptorList();
//        List<ScadaFieldDescriptor>   lstFdRaw  = WireScanner.DataRaw.getFieldDescriptorList();
//        List<ScadaFieldDescriptor>   lstFdFit  = WireScanner.DataFit.getFieldDescriptorList();
//        List<ScadaFieldDescriptor>   lstFdTrc  = WireScanner.Trace.getFieldDescriptorList();
//      
//      Collection<ScadaFieldDescriptor> setFldDsc = new LinkedList<ScadaFieldDescriptor>();
//      
//      setFldDsc.addAll( lstFdLive );
//      setFldDsc.addAll( lstFdRaw );
//      setFldDsc.addAll( lstFdFit );
//      setFldDsc.addAll( lstFdTrc );

        List<ScadaFieldDescriptor>   lstFdLiv  = getFldDescrLst(DataLivePt.class);
        List<ScadaFieldDescriptor>   lstFdRaw  = getFldDescrLst(WireScanner.DataRaw.class);
        List<ScadaFieldDescriptor>   lstFdFit  = getFldDescrLst(WireScanner.DataFit.class);
        List<ScadaFieldDescriptor>   lstFdTrc  = getFldDescrLst(WireScanner.Trace.class);
        
        COL_SFD_DATA = new LinkedList<ScadaFieldDescriptor>();
        
        COL_SFD_DATA.addAll( lstFdLiv );
        COL_SFD_DATA.addAll( lstFdRaw );
        COL_SFD_DATA.addAll( lstFdFit );
        COL_SFD_DATA.addAll( lstFdTrc );
    }

    
    /*
     * Global Methods
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
    public static String dataLabel(long lngVersion) {
        String  strLblVer = STR_LBL_PARENT;
        
        if (lngVersion < 2) {
            strLblVer = MainApplication.convertPtaDataLabelToVer3(strLblVer);
            strLblVer = strLblVer.replace("ScannerData", "DeviceData");
        }
        
        return strLblVer;
    }
    
    /**
     * Check that all the data acquisition channels are available for
     * the given device.   
     *
     * @param ws        hardware device under test
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
    public static boolean testConnection(WireScanner ws, double dblTmOut) throws BadChannelException {
//        List<ScadaFieldDescriptor>   lstFdLiv  = getFldDescrLst(WireScanner.DataLivePt.class);
//        List<ScadaFieldDescriptor>   lstFdRaw  = getFldDescrLst(WireScanner.DataRaw.class);
//        List<ScadaFieldDescriptor>   lstFdFit  = getFldDescrLst(WireScanner.DataFit.class);
//        List<ScadaFieldDescriptor>   lstFdTrc  = getFldDescrLst(WireScanner.Trace.class);
//        
//        Collection<ScadaFieldDescriptor> setFldDsc = new LinkedList<ScadaFieldDescriptor>();
//        
//        setFldDsc.addAll( lstFdLive );
//        setFldDsc.addAll( lstFdRaw );
//        setFldDsc.addAll( lstFdFit );
//        setFldDsc.addAll( lstFdTrc );
//        
//        boolean bolResult = ws.testConnection(setFldDsc, dblTmOut);
        boolean bolResult = ws.testConnection(COL_SFD_DATA, dblTmOut);
        
        return bolResult;
    }
    
    /**
     * Acquires are the measurement data from the given hardware device
     * and returns it in a new <code>ScannerData</code> data
     * structure.
     *
     * @param ws        wire scanner device with measurement data on board
     * 
     * @return          data structure of measurement data
     * 
     * @throws GetException        unable to connect to a parameter read back channel
     * @throws ConnectionException general channel access get exception
     * 
     * @since  Feb 26, 2010
     * @author Christopher K. Allen
     */
    public static ScannerData   acquire(WireScanner ws) throws ConnectionException, GetException {
        return new ScannerData(ws);
    }
    
    /**
     * Create and load previously measured (and saved) data.  The
     * data is read from the given source exposing the </code>DataAdaptor</code>
     * interface.
     *
     * @param daptSrc   source of the (previously saved) measurement data
     * 
     * @return          new <code>ScannerData</code> object populated from the
     *                  given data source.
     * 
     * @since  Mar 17, 2010
     * @author Christopher K. Allen
     */
    public static ScannerData   load(DataAdaptor daptSrc) {
        ScannerData         data = new ScannerData(daptSrc);
        
        return data;
    }
    
    
    /*
     * Local Attributes
     */
    
    //
    //  Data Source
    //
    
    /** The acquisition device type ID */
    public String                         strTypId;
    
    /** The acquisition device ID */
    public String                          strDevId;

    
    //
    // Measurement Device Configuration
    //
    
    /** The PV log ID of the machine state at measurement 
     * @deprecated  Not used
     */
    @Deprecated
    public long                            lngPvLogId;
    
    /** Configuration of the measurement device for the data set */
    public ScannerConfig                    cfgDevice;
    

    //
    // Measurement Data
    //
    
    /** The raw profile data acquired from diagnostics */
    private final WireScanner.DataRaw        datRaw;
    
    /** The fitted profile data from the diagnostics */
    private  final WireScanner.DataFit       datFit;
    
    /** The raw measurement trace generating measurement data */ 
    private final WireScanner.Trace          datTrace;
    
    
    //
    // Measurement Signal Properties
    //
    
    /** The direct statistical parameters of the profile signals */
    private final WireScanner.StatisticalAttrSet     sigStat;
    
    /** The Gaussian fit parameters for the profile signals */
    private final WireScanner.GaussFitAttrSet        sigGauss;
    
    /** The Double Gaussian fit parameters of the profile signals */
    private final WireScanner.DblGaussFitAttrSet     sigDblGauss;

    
    
    
    /*
     * Initialization
     */
    
    
    /**
     * Create a new <code>ScannerData</code> object.  The
     * data structure is empty.
     *
     * @throws ScadaAnnotationException  either unable to access the field of a signal 
     *                                  or it is ill-defined w.r.t <code>ASignal</code>
     *                                  annotation
     *  
     * @since     Mar 17, 2010
     * @author    Christopher K. Allen
     */
    public ScannerData() throws ScadaAnnotationException {
        this.strTypId = null;
        this.strDevId = null;
        
        this.cfgDevice = new ScannerConfig();
        
        this.datRaw   = new WireScanner.DataRaw();
        this.datFit   = new WireScanner.DataFit();
        this.datTrace = new WireScanner.Trace();
            
        this.sigStat  = new WireScanner.StatisticalAttrSet();
        this.sigGauss = new WireScanner.GaussFitAttrSet();
        this.sigDblGauss = new WireScanner.DblGaussFitAttrSet();
    }
    
    /**
     * Create a new <code>ScannerData</code> object and initialize
     * it from existing measurement data taken from the given data source.
     *
     * @param daptSrc   persistent data source containing measurement data 
     *
     * @since     Mar 18, 2010
     * @author    Christopher K. Allen
     */
    public ScannerData(DataAdaptor daptSrc) {
        this();
        this.update(daptSrc);
////        DataAdaptor     daptDev = daptSrc.childAdaptor(this.dataLabel());
//        DataAdaptor     daptDev = daptSrc;
//        this.strDevId = daptDev.stringValue(STR_ATTR_DEVID);
//        
//        this.datRaw = new WireScanner.DataRaw(daptDev);
//        this.datFit = new WireScanner.DataFit(daptDev);
//        this.datTrace = new WireScanner.Trace(daptDev);
//        
//        this.sigStat = new WireScanner.StatisticalAttrSet(daptDev);
//        this.sigGauss = new WireScanner.GaussFitAttrSet(daptDev);
//        this.sigDblGauss = new WireScanner.DblGaussFitAttrSet(daptDev);
//        
//        this.cfgDevice = new ScannerConfig(daptSrc);
    }
    
    /**
     * Create a new <code>ScannerData</code> object and
     * populates it using data from the given device.
     *
     * @param ws        source of measurement data
     *
     * @throws GetException        unable to connect to a parameter read back channel
     * @throws ConnectionException general channel access get exception
     *  
     * @since     Feb 3, 2010
     * @author    Christopher K. Allen
     */
    public ScannerData(WireScanner ws) throws ConnectionException, GetException {
        this.strTypId = ws.getType(); 
        this.strDevId = ws.getId();

        // Attempt to collect the wire scanner configuration information
        //  Send a warning to the user if a failure occurs but continue
        //  on with the profile data.
        try {
            
            this.cfgDevice = ScannerConfig.acquire(ws);
            
        } catch (ConnectionException e) {
            String  strMsg = "Unable to acquire configuration data for " + ws.getId();

            MainApplication.getEventLogger().logWarning(this.getClass(), strMsg);
            JOptionPane.showMessageDialog(null, strMsg, "Warning", JOptionPane.WARNING_MESSAGE);

            this.cfgDevice = new ScannerConfig();
            
        } catch (GetException e) {
            String  strMsg = "Unable to acquire configuration data for " + ws.getId();

            MainApplication.getEventLogger().logWarning(this.getClass(), strMsg);
            JOptionPane.showMessageDialog(null, strMsg, "Warning", JOptionPane.WARNING_MESSAGE);

            this.cfgDevice = new ScannerConfig();
            
        }
        
        // Now acquire all the measurement data and the analysis
        //  data done by the wire scanner controller.
        //  If this fails then there is a big problem and we let it
        //  bubble up to the invoking methods.
        this.datRaw   = WireScanner.DataRaw.acquire(ws);
        this.datFit   = WireScanner.DataFit.acquire(ws);
        this.datTrace = WireScanner.Trace.acquire(ws);

        this.sigStat     = WireScanner.StatisticalAttrSet.acquire(ws);
        this.sigGauss    = WireScanner.GaussFitAttrSet.acquire(ws);
        this.sigDblGauss = WireScanner.DblGaussFitAttrSet.acquire(ws);
    }

    
    /*
     * Operations
     */

    /**
     * Returns the signal properties as computed directly from the raw data.
     * <br/><br/>
     * 
     * <b>NOTE</b>
     * <br/>
     * &middot; This values are extremely sensitive to noise.
     * 
     * @return  properties of the raw data signals 
     *
     * @author Christopher K. Allen
     * @since  Apr 22, 2014
     */
    public SignalAttrSet    getStatisticalAttributes() {
        return  this.sigStat;
    }
    
    /**
     * Return the signal properties computed from a double Gaussian fit
     * of the raw profile data.
     * 
     * @return  signal properties of a double Gaussian fit
     *
     * @author Christopher K. Allen
     * @since  Apr 22, 2014
     */
    public SignalAttrSet    getDoubleGaussianAttributes() {
        return this.sigDblGauss;
    }

    
    /*
     * IProfileData Interface
     */

    /**
     * Returns the SMF device type identifier used to uniquely identifier
     * the hardware. 
     *
     * @see xal.smf.impl.profile.ProfileDevice.IProfileData#getDeviceTypeId()
     *
     * @author Christopher K. Allen
     * @since  Sep 22, 2014
     */
    @Override
    public String getDeviceTypeId() {
        return this.strTypId;
    }

    /**
     * Returns the identifier of the data acquisition device producing
     * this data set.
     *
     * @see xal.smf.impl.profile.IProfileData#getDeviceId()
     *
     * @author Christopher K. Allen
     * @since  Apr 22, 2014
     */
    @Override
    public String   getDeviceId() {
        return this.strDevId;
    }
    
    /**
     * <p>
     * Returns the number of valid data values in the data arrays contained in this
     * data structure (and the position arrays).  
     * </p>
     * <p>
     * The data array has the length of the <code>EPICS_MAX_ARRAY_SIZE</code> 
     * environment variable.  However, only the initial part of the array contains
     * actual data, the length of which is specified in the configuration parameters
     * (see <code>{@link WireScanner.ScanConfig#stepCount}</code>).
     * </p>
     *
     * @return the number of valid data values in the data arrays of this structure
     *
     * @see xal.smf.impl.profile.IProfileData#getDataSize()
     *
     * @author Christopher K. Allen
     * @since  Apr 17, 2012
     */
    @Override
    public int  getDataSize() {
        if (this.cfgDevice == null)
            return -1;
        if (this.cfgDevice.cfgScan == null)
            return -1;
        
        return this.cfgDevice.cfgScan.stepCount;
    }
    
    /**
     *
     * @see xal.smf.impl.profile.IProfileData#getRawData()
     *
     * @author Christopher K. Allen
     * @since  Apr 22, 2014
     */
    @Override
    public SignalSet getRawData() {
        return this.datRaw;
    }

    /**
     * Returns the fitted signal data of this data set.  This method returns
     * the Gaussian fitted data.
     *
     * @see xal.smf.impl.profile.IProfileData#getFitData()
     *
     * @author Christopher K. Allen
     * @since  Apr 22, 2014
     */
    @Override
    public SignalSet getFitData() {
        return this.datFit;
    }

    /**
     * Returns the signal properties of this data set.  The signal properties
     * are computed from the Gaussian fitted signal data.
     *
     * @see xal.smf.impl.profile.IProfileData#getDataAttrs()
     *
     * @author Christopher K. Allen
     * @since  Apr 22, 2014
     */
    @Override
    public SignalAttrSet getDataAttrs() {
        return this.sigGauss;
    }

    /**
     * This method returns <code>true</code> regardless of the arguments
     * since there is, as of yet, no hardware method of determining whether
     * or not the device wire is faulty.
     *
     * @see xal.smf.impl.profile.ProfileDevice.IProfileData#isValidWire(xal.smf.impl.profile.ProfileDevice.ANGLE, int)
     *
     * @author Christopher K. Allen
     * @since  Jul 2, 2014
     */
    @Override
    public boolean isValidWire(ANGLE angle, int iWire) {
        return true;
    }

    
    /*
     * DataListener Interface
     */

    /**
     * Returns the string label used to identify
     * stored data for this data structure for the current version.
     *
     * @since   Mar 17, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.tools.data.DataListener#dataLabel()
     */
    @Override
    public String dataLabel() {
        return STR_LBL_PARENT;
    }
    
    /**
     * Save the contents of this data structure to the
     * data sink behind the <code>DataAdaptor</code>
     * interface.
     *
     * @param snkData   persistent data store
     * 
     * @since  Mar 4, 2010
     * @author Christopher K. Allen
     */
    public void write(DataAdaptor snkData) {
        
        DataAdaptor     daptDev = snkData.createChild( this.dataLabel() );
        daptDev.setValue(STR_ATTR_TYPID, this.strTypId);
        daptDev.setValue(STR_ATTR_DEVID, this.strDevId);
        daptDev.setValue(STR_ATTR_FMTVER, LNG_VAL_FMTVER);
        
        this.cfgDevice.write(daptDev);
        
        this.datRaw.write(daptDev);
        this.datFit.write(daptDev);
        this.datTrace.write(daptDev);
        
        this.sigStat.write(daptDev);
        this.sigGauss.write(daptDev);
        this.sigDblGauss.write(daptDev);
    }

    /**
     * Load the contents of this data structure from the given 
     * data source exposing the <code>DataAdaptor</code> 
     * interface.
     * 
     * @param   daptSrc         data source used to populate this data structure
     *
     * @since 	Mar 17, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.tools.data.DataListener#update(xal.tools.data.DataAdaptor)
     */
    @Override
    public void update(DataAdaptor daptSrc) {
        DataAdaptor     daptDev = daptSrc;
        
        this.strDevId = daptDev.stringValue(STR_ATTR_DEVID);
        
        long    lngFmtVer = daptDev.longValue(STR_ATTR_FMTVER);
        
        if (lngFmtVer == 1) {
            
            WireScanner.ActrConfig  cfgActr = new WireScanner.ActrConfig(daptDev);
            WireScanner.PrcgConfig  cfgPrcg = new WireScanner.PrcgConfig(daptDev);
            WireScanner.ScanConfig  cfgScan = new WireScanner.ScanConfig(daptDev);
            WireScanner.SmplConfig  cfgSmp  = new WireScanner.SmplConfig(daptDev);
            WireScanner.TrgConfig   cfgTmg  = new WireScanner.TrgConfig(daptDev);
            
            this.cfgDevice = new ScannerConfig(cfgActr, cfgPrcg, cfgScan, cfgSmp, cfgTmg);
            
        } else {
            
            this.cfgDevice.update(daptDev);
            
        }
        
        if (lngFmtVer >= 3)
            this.strTypId = daptDev.stringValue(STR_ATTR_TYPID);
        else
            this.strTypId = "unknown";
        
        this.datRaw.update(daptDev);
        this.datFit.update(daptDev);
        this.datTrace.update(daptDev);
        
        this.sigStat.update(daptDev);
        this.sigGauss.update(daptDev);
        this.sigDblGauss.update(daptDev);
    }
    
    
    
    /*
     * Support Methods
     */
    
    /**
     * Convenience method for extracting Scada Field Descriptors from the class
     * type.  This method is a proxy to <code>SignalSet{@link #getFldDescrList(Class)}</code>.
     * 
     * @param clsData   <code>SignalSet</code>-derived class type containing the 
     *                  <code>ASignal.ASet</code> annotation data
     *                  
     * @return          list of all the Scada Field Descriptors contained in the class type
     *
     * @author Christopher K. Allen
     * @since  Mar 25, 2014
     */
    private static List<ScadaFieldDescriptor>   getFldDescrLst(Class<? extends SignalSet> clsData) {
        return SignalSet.getFieldDescriptorList(clsData);
    }

}
