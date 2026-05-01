/**
 * ScannerConfig.java
 *
 *  Created	: Feb 23, 2012
 *  Author  : Christopher K. Allen 
 */
package xal.app.pta.daq;

import xal.app.pta.MainApplication;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;
import xal.smf.impl.WireScanner;
import xal.smf.scada.ScadaRecord;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Data structure containing the configuration information for one profile device.
 * 
 * <p>
 * <b>Ported from XAL on Jul 16, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Feb 23, 2012
 * @author Christopher K. Allen
 */
public class ScannerConfig extends DeviceConfig implements DataListener, Serializable {


    
    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;
    
    
//    /** The format version for persistent storage */
//    private static final long  LNG_VAL_FMTVER = 2;
//    
//    /** The data label for measurement data - used in <code>DataAdaptors</code> */
//    public static final String  STR_LBL_PARENT = ScannerConfig.class.getCanonicalName();
//    
//    /** The data format version attribute used in <code>DataListener</code> implementation */
//    private static final String STR_ATTR_FMTVER = "ver";
//    
//    /** The device ID attribute used in <code>DataListener</code> implementation */
//    private static final String STR_ATTR_DEVID = "dev";


//    /** List of all SCADA field descriptors for channels containing device configuration parameters */
//    private static final Collection<ScadaFieldDescriptor>     COL_SFD_CONFIG;
//
//    
//    static {
//        
//        ScadaFieldList  lstScan = new ScadaFieldList(WireScanner.ScanConfig.class);
//        ScadaFieldList  lstActr = new ScadaFieldList(WireScanner.ActrConfig.class);
//        ScadaFieldList  lstPrcg = new ScadaFieldList(WireScanner.PrcgConfig.class);
//        ScadaFieldList  lstTmg  = new ScadaFieldList(WireScanner.TrgConfig.class);
//        ScadaFieldList  lstSmpl = new ScadaFieldList(WireScanner.SmplConfig.class);
//        
//        COL_SFD_CONFIG = new LinkedList<ScadaFieldDescriptor>();
//        
//        COL_SFD_CONFIG.addAll( lstScan );
//        COL_SFD_CONFIG.addAll( lstActr );
//        COL_SFD_CONFIG.addAll( lstPrcg );
//        COL_SFD_CONFIG.addAll( lstTmg );
//        COL_SFD_CONFIG.addAll( lstSmpl );
//    }
    
    
//    /**
//     * Check that all the device configuration channels are available for
//     * the given device.   
//     *
//     * @param ws        hardware device under test
//     * @param dblTmOut  maximum time allowed to make connections (in seconds)
//     * 
//     * @return          <code>true</code> if all connections are available,
//     *                  <code>false</code> otherwise
//     *                  
//     * @throws BadChannelException  there exists an unbound channel handle in the given
//     *                              set of SCADA field descriptors
//     *
//     * @author Christopher K. Allen
//     * @since  Mar 16, 2011
//     */
//    public static boolean testConnection(WireScanner ws, double dblTmOut) throws BadChannelException {
//        
//        boolean bolResult = ws.testConnection(COL_SFD_CONFIG, dblTmOut);
//        
//        return bolResult;
//    }
    
    /**
     * Acquires are the measurement data from the given hardware device
     * and returns it in a new <code>ScannerConfig</code> data
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
    public static ScannerConfig   acquire(WireScanner ws) throws ConnectionException, GetException {
        return new ScannerConfig(ws);
    }
    
    /**
     * Create and load previously measured (and saved) data.  The
     * data is read from the given source exposing the </code>DataAdaptor</code>
     * interface.
     *
     * @param daptSrc   source of the (previously saved) measurement data
     * 
     * @return          new <code>ScannerConfig</code> object populated from the
     *                  given data source.
     * 
     * @since  Mar 17, 2010
     * @author Christopher K. Allen
     */
    public static ScannerConfig   load(DataAdaptor daptSrc) {
        ScannerConfig         data = new ScannerConfig(daptSrc);
        
        return data;
    }
    
    

    
    /*
     * Local Attributes
     */
    
    //
    //  Data Source
    //
    
//    /** The acquisition device */
//    public String                          strDevId;
//
//    /** The PV log ID of the machine state at measurement 
//     * @deprecated Not used.
//     */
//    public long                            lngPvLogId;
    
    
    //
    // Device Configuration 
    //

    /** Scan parameters */
    public  final WireScanner.ScanConfig    cfgScan;
    
    /** Data acquisition configuration parameters */
    public  final WireScanner.SmplConfig    cfgSmp;
    
    /** Scan actuator configuration parameters */
    public  final WireScanner.ActrConfig    cfgActr;
    
    /** Post acquisition data analysis configuration */
    public  final WireScanner.PrcgConfig    cfgPrcg;
    
//    /** Acquisition timing parameters */
//    public  final WireScanner.TrgConfig     cfgTmg;

    
    //
    //  Class Data Management 
    //
    
    /** List of this SCADA record attributes maintained by this object */
    private List<ScadaRecord>               lstRecCfg;

    
    /*
     * Initialization
     */
    
    
    /**
     * Create a new <code>ScannerConfig</code> object.  The
     * data structure is empty.
     *
     * @since     Mar 17, 2010
     * @author    Christopher K. Allen
     */
    public ScannerConfig() {
        super();
        
        this.cfgActr = new WireScanner.ActrConfig();
        this.cfgPrcg = new WireScanner.PrcgConfig();
        this.cfgSmp  = new WireScanner.SmplConfig();
        this.cfgScan = new WireScanner.ScanConfig();
//        this.cfgTmg  = new WireScanner.TrgConfig();
        
        this.initScadaRecList();
    }
    
    /**
     * Create a new <code>ScannerConfig</code> object initialized to the
     * given argument values.
     * 
     * @param cfgActr       device actuator configuration
     * @param cfgPrcg       device processing parameters
     * @param cfgScan       device scan configuration parameters
     * @param cfgSmp        device sampling setup
     * @param cfgTmg        device timing/triggering parameters
     *
     * @author  Christopher K. Allen
     * @since   Apr 17, 2012
     */
    public ScannerConfig(
            WireScanner.ActrConfig cfgActr, 
            WireScanner.PrcgConfig cfgPrcg, 
            WireScanner.ScanConfig cfgScan, 
            WireScanner.SmplConfig cfgSmp, 
            WireScanner.TrgConfig cfgTmg
            ) 
    {
        super();
        
        this.cfgActr = cfgActr;
        this.cfgPrcg = cfgPrcg;
        this.cfgScan = cfgScan;
        this.cfgSmp  = cfgSmp;
//        this.cfgTmg  = cfgTmg;
        
        this.initScadaRecList();
    }
    
    /**
     * Create a new <code>ScannerConfig</code> object and initialize
     * it from existing measurement data taken from the given data source.
     *
     * @param daptSrc   persistent data source containing measurement data 
     *
     * @since     Mar 18, 2010
     * @author    Christopher K. Allen
     */
    public ScannerConfig(DataAdaptor daptSrc) {
        this();
        this.update(daptSrc);
        
        this.initScadaRecList();
    }
    
    /**
     * Create a new <code>ScannerConfig</code> object and
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
    public ScannerConfig(WireScanner ws) throws ConnectionException, GetException {
        super(ws.getId());
        
        this.cfgScan = WireScanner.ScanConfig.acquire(ws);
        this.cfgActr = WireScanner.ActrConfig.aquire(ws);
        this.cfgPrcg = WireScanner.PrcgConfig.acquire(ws);
        this.cfgSmp  = WireScanner.SmplConfig.acquire(ws);
//        this.cfgTmg  = WireScanner.TrgConfig.acquire(ws);
        
        this.initScadaRecList();
    }
    
    
    /*
     * Operations
     */
    
//    /**
//     * Sets the configuration of the given device to that represented
//     * in this data structure.  Note that the configuration is applied 
//     * to the device regardless of whether is the same device this configuration
//     * was taken from (the device IDs not not necessary match).
//     *
//     * @param ws    device to be configured
//     * 
//     * @throws BadStructException  data structure fields are ill-defined/incompatible 
//     * @throws ConnectionException  unable to connect to PV channel
//     * @throws PutException  general put exception (unable to set all parameter values)
//     *
//     * @author Christopher K. Allen
//     * @since  May 1, 2012
//     */
//    public void     configureDevice(WireScanner   ws) 
//        throws BadStructException, ConnectionException, PutException
//    {
//        this.cfgActr.setHardwareValues(ws);
//        this.cfgPrcg.setHardwareValues(ws);
//        this.cfgScan.setHardwareValues(ws);
//        this.cfgSmp.setHardwareValues(ws);
//        this.cfgTmg.setHardwareValues(ws);
//    }

//    /**
//     * Applies the configuration represented in this data structure to the 
//     * device in the given accelerator sequence with the same device ID. If
//     * there is no such device then the method returns false.
//     *
//     * @param smfSeq    this data structure instance should contain the configuration information for a device
//     *                  in this accelerator sequence, a device with the proper ID
//     *                  
//     * @return          <code>true</code> if the target device was found and its configuration updated,
//     *                  <code>false</code> otherwise
//     * 
//     * @throws BadStructException  data structure fields are ill-defined/incompatible 
//     * @throws ConnectionException  unable to connect to PV channel
//     * @throws PutException  general put exception (unable to set all parameter values)
//     *
//     * @author Christopher K. Allen
//     * @since  May 1, 2012
//     */
//    public boolean  configureDeviceById(AcceleratorSeq smfSeq) 
//        throws BadStructException, ConnectionException, PutException
//    {
//        AcceleratorNode smfDev = smfSeq.getNodeWithId(this.strDevId);
//        
//        if (smfDev == null)
//            return false;
//        
//        if ( !(smfDev instanceof WireScanner) )
//            return false;
//        
//        WireScanner ws = (WireScanner)smfDev;
//        
//        this.configureDevice(ws);
//        return true;
//    }
    
    /*
     * Base Class Hooks
     */
    
    /**
     * Returns a container of all the SCADA records contained in this class, 
     * basically all the configuration parameter data structures.
     *
     * @see xal.apps.pta.daq.DeviceConfig#getScadaRecords()
     *
     * @author Christopher K. Allen
     * @since  Apr 15, 2014
     */
    @Override
    public List<ScadaRecord> getScadaRecords() {
        
        return this.lstRecCfg;
    }

//    /**
//     *
//     * @see xal.apps.pta.daq.DeviceConfig#acquireConfiguration(xal.smf.impl.profile.ProfileDevice)
//     *
//     * @author Christopher K. Allen
//     * @since  Apr 16, 2014
//     */
//    @Override
//    public <T extends ProfileDevice> void acquireConfiguration(T smfDev) {
//        // TODO Auto-generated method stub
//
//    }

    
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
//    public String dataLabel(long lngVersion) {
//        String  strLblVer = this.dataLabel();
//        
//        if (lngVersion < 3) {
//            strLblVer = MainApplication.convertPtaDataLabelToVer3(strLblVer);
//            strLblVer = strLblVer.replace("ScannerConfig", "DeviceConfig");
//        }
//        
//        return strLblVer;
//    }
//    
//    /**
//     * Returns the string label used to identify
//     * stored data for this data structure.
//     *
//     * @since   Mar 17, 2010
//     * @author  Christopher K. Allen
//     *
//     * @see xal.tools.data.DataListener#dataLabel()
//     */
//    @Override
//    public String dataLabel() {
//        return STR_LBL_PARENT;
//    }
    
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
        super.write(daptDev);
//        daptDev.setValue(STR_ATTR_DEVID, this.strDevId);
//        daptDev.setValue(STR_ATTR_FMTVER, LNG_VAL_FMTVER);
        
        this.cfgActr.write(daptDev);
        this.cfgPrcg.write(daptDev);
        this.cfgSmp.write(daptDev);
        this.cfgScan.write(daptDev);
//        this.cfgTmg.write(daptDev);
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

        // Get the version number
        long        lngVersion = 0;
        if (daptSrc.hasAttribute("ver"))
            lngVersion = daptSrc.intValue("ver");
        
        // Get the correct the data label for back versions
        String      strLblCfg = this.dataLabel();
        
        DataAdaptor     daptDev = daptSrc.childAdaptor(strLblCfg);
        if (daptDev == null) {
          strLblCfg = MainApplication.convertPtaDataLabelToVer3(strLblCfg);
          strLblCfg = strLblCfg.replace("ScannerConfig", "DeviceConfig");
          
          daptDev = daptSrc.childAdaptor(strLblCfg);
        }
        // Account for the first version of the ScannerData class where ScannerConfig
        //  was not a separate class (thus, no child node here)
        if (daptDev == null)
            daptDev = daptSrc;
        
        super.update(daptDev);
        
        this.cfgActr.update(daptDev);
        this.cfgPrcg.update(daptDev);
        this.cfgSmp.update(daptDev);
        this.cfgScan.update(daptDev);
//        this.cfgTmg.update(daptDev);
    }

    
    
    /*
     * Support Methods
     */
    
    /**
     *  Initializes the list of all SCADA records maintained by
     *  this class. This list is a convenience for the <code>DeviceConfig</code>
     *  required method <code>{@link #getScadaRecords()}</code>. 
     *
     * @author Christopher K. Allen
     * @since  Apr 15, 2014
     */
    private void initScadaRecList() {
        
        this.lstRecCfg = new LinkedList<ScadaRecord>();

        this.lstRecCfg.add(this.cfgActr);
        this.lstRecCfg.add(this.cfgPrcg);
        this.lstRecCfg.add(this.cfgScan);
        this.lstRecCfg.add(this.cfgSmp);
//        this.lstRecCfg.add(this.cfgTmg);
    }

}
