/**
 * ScannerConfig.java
 *
 *  Created	: Feb 23, 2012
 *  Author  : Christopher K. Allen 
 */
package xal.app.pta.daq;

import xal.app.pta.rscmgt.AppProperties;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.WireHarp;
import xal.smf.scada.BadStructException;
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
public class HarpConfig extends DeviceConfig implements DataListener, Serializable {


    
    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;
    
    
    
//    /** Collection of all SCADA field descriptors for channels containing device configuration parameters */
//    static final List<ScadaFieldDescriptor>     LST_FDS_CONFIG;
//
//    
//    static {
//        
//        ScadaFieldList  lstDevCfg  = new ScadaFieldList(WireHarp.DevConfig.class);
//        ScadaFieldList  lstDevStat = new ScadaFieldList(WireHarp.DevStatus.class);
//        ScadaFieldList  lstDaqCfg  = new ScadaFieldList(WireHarp.DaqConfig.class);
//        
//        LST_FDS_CONFIG = new LinkedList<ScadaFieldDescriptor>();
//        
//        LST_FDS_CONFIG.addAll( lstDevCfg );
//        LST_FDS_CONFIG.addAll( lstDevStat );
//        LST_FDS_CONFIG.addAll( lstDaqCfg );
//    }
    
    
    /**
     * Acquires are the measurement data from the given hardware device
     * and returns it in a new <code>ScannerConfig</code> data
     * structure.
     *
     * @param smfHarp        wire scanner device with measurement data on board
     * 
     * @return          data structure of measurement data
     * 
     * @throws GetException        unable to connect to a parameter read back channel
     * @throws ConnectionException general channel access get exception
     * 
     * @since  Feb 26, 2010
     * @author Christopher K. Allen
     */
    public static HarpConfig   acquire(WireHarp smfHarp) throws ConnectionException, GetException {
        return new HarpConfig(smfHarp);
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
    public static HarpConfig load(DataAdaptor daptSrc) {
        HarpConfig         data = new HarpConfig(daptSrc);
        
        return data;
    }

    
    
    

    
    /*
     * Local Attributes
     */
    

    //
    // Device Configuration 
    //

    /** Harp configuration parameters */
    public  final WireHarp.DevConfig        devConfig;

    /** Harp operational status parameters */
    public  final WireHarp.DevStatus        devStatus;
    
    /** Data acquisition configuration parameters */
    public  final WireHarp.DaqConfig        daqConfig;
    


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
    public HarpConfig() {
        super();
        
        this.devConfig = new WireHarp.DevConfig();
        this.devStatus = new WireHarp.DevStatus();
        this.daqConfig = new WireHarp.DaqConfig();
        
        this.daqConfig.cntWires = AppProperties.HARP.CNT_WIRES.getValue().asInteger();

        this.initScadaRecList();
    }
    
    /**
     * Create a new <code>ScannerConfig</code> object initialized to the
     * given argument values.
     * 
     * @param cfgActr       device actuator configuration
     * @param cfgPrcg       device processing parameters
     * @param devConfig       device scan configuration parameters
     * @param devStatus        device sampling setup
     * @param cfgTmg        device timing/triggering parameters
     *
     * @author  Christopher K. Allen
     * @since   Apr 17, 2012
     */
    public HarpConfig(
            WireHarp.DevConfig  devCfg, 
            WireHarp.DevStatus  devStat,
            WireHarp.DaqConfig datStat
            ) 
    {
        super();
        
        this.devConfig = devCfg;
        this.devStatus = devStat;
        this.daqConfig = datStat;
        
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
    public HarpConfig(DataAdaptor daptSrc) {
        this();
        this.update(daptSrc);

        this.initScadaRecList();
    }
    
    /**
     * Create a new <code>ScannerConfig</code> object and
     * populates it using data from the given device.
     *
     * @param smfHarp        source of measurement data
     *
     * @throws GetException        unable to connect to a parameter read back channel
     * @throws ConnectionException general channel access get exception
     *  
     * @since     Feb 3, 2010
     * @author    Christopher K. Allen
     */
    public HarpConfig(WireHarp smfHarp) throws ConnectionException, GetException {
        super(smfHarp.getId());
        
        this.devConfig = WireHarp.DevConfig.acquire(smfHarp);
        this.devStatus = WireHarp.DevStatus.acquire(smfHarp);
        this.daqConfig = WireHarp.DaqConfig.acquire(smfHarp);
        
        this.daqConfig.cntWires = AppProperties.HARP.CNT_WIRES.getValue().asInteger();
        
        this.initScadaRecList();
    }
    
    /*
     * Operations
     */
    
    /**
     * Sets the configuration of the given device to that represented
     * in this data structure.  Note that the configuration is applied 
     * to the device regardless of whether is the same device this configuration
     * was taken from (the device IDs not not necessary match).
     *
     * @param smfHarp    device to be configured
     * 
     * @throws BadStructException  data structure fields are ill-defined/incompatible 
     * @throws ConnectionException  unable to connect to PV channel
     * @throws PutException  general put exception (unable to set all parameter values)
     *
     * @author Christopher K. Allen
     * @since  May 1, 2012
     */
    public void     configureDevice(WireHarp   smfHarp) 
        throws BadStructException, ConnectionException, PutException
    {
        this.devConfig.setHardwareValues(smfHarp);
        this.devStatus.setHardwareValues(smfHarp);
        this.daqConfig.setHardwareValues(smfHarp);
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
        AcceleratorNode smfDev = smfSeq.getNodeWithId(this.getDeviceId());
        
        if (smfDev == null)
            return false;
        
        if ( !(smfDev instanceof WireHarp) )
            return false;
        
        WireHarp smfHarp = (WireHarp)smfDev;
        
        this.configureDevice(smfHarp);
        return true;
    }
    
    /*
     * DataListener Interface
     */
    
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

        // Create the data node for this object 
        DataAdaptor     daptDev = snkData.createChild( this.dataLabel() );
        
        // Write out base class data
        super.write(daptDev);
        
        this.devConfig.write(daptDev);
        this.devStatus.write(daptDev);
        this.daqConfig.write(daptDev);;
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

        // Grab the data node for this object
        DataAdaptor     daptDev = daptSrc.childAdaptor(this.dataLabel());
        if (daptDev == null)
            daptDev = daptSrc;
        
        // Load the base class data
        super.update(daptDev);

        // Load the version information, which is currently unused
        @SuppressWarnings("unused")
        long    lngVer = daptSrc.longValue(STR_ATTR_FMTVER);
        
        // Load the data for this class
        this.devConfig.update(daptDev);
        this.devConfig.update(daptDev);
        this.daqConfig.update(daptDev);
    }

    
    /*
     * Base Class Hooks
     */
    
    /**
     * Returns a container of all the SCADA records contained in this class, 
     * basically all the configuration parameter data structures.
     *
     * @see xal.app.pta.daq.DeviceConfig#getScadaRecords()
     *
     * @author Christopher K. Allen
     * @since  Apr 15, 2014
     */
    @Override
    protected List<ScadaRecord> getScadaRecords() {

        return this.lstRecCfg;
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

        this.lstRecCfg.add(this.devConfig);
        this.lstRecCfg.add(this.devStatus);
        this.lstRecCfg.add(this.daqConfig);
    }
}
