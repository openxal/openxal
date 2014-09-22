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
import xal.smf.impl.WireHarp;
import xal.smf.impl.profile.ProfileDevice;
import xal.smf.impl.profile.ProfileDevice.IProfileData;
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
public class HarpData implements IProfileData,  Serializable {


    
    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;
    
    
    /** The format version for persistent storage */
    private static final long  LNG_VAL_FMTVER = 1;
    
    /** The data label for measurement data - used in <code>DataAdaptors</code> */
    public static final String  STR_LBL_PARENT = HarpData.class.getCanonicalName();
    
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

        List<ScadaFieldDescriptor>   lstFdRaw  = getFldDescrLst(WireHarp.DataRaw.class);
        List<ScadaFieldDescriptor>   lstFdFit  = getFldDescrLst(WireHarp.DataFit.class);
        
        COL_SFD_DATA = new LinkedList<ScadaFieldDescriptor>();
        
        COL_SFD_DATA.addAll( lstFdRaw );
        COL_SFD_DATA.addAll( lstFdFit );
    }

    
    /**
     * Check that all the data acquisition channels are available for
     * the given device.   
     *
     * @param smfHarp        hardware device under test
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
    public static boolean testConnection(WireHarp smfHarp, double dblTmOut) throws BadChannelException {
        boolean bolResult = smfHarp.testConnection(COL_SFD_DATA, dblTmOut);
        
        return bolResult;
    }
    
    /**
     * Acquires are the measurement data from the given hardware device
     * and returns it in a new <code>ScannerData</code> data
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
    public static HarpData   acquire(WireHarp smfHarp) throws ConnectionException, GetException {
        return new HarpData(smfHarp);
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
    public static HarpData   load(DataAdaptor daptSrc) {
        HarpData         data = new HarpData(daptSrc);
        
        return data;
    }
    
    
    /*
     * Local Attributes
     */
    
    //
    //  Data Source
    //
    
    /** The acquisition device type ID */
    public String       strTypId;
    
    /** The acquisition device */
    public String       strDevId;
    
    
    //
    // Measurement Device Configuration
    //
    
    /** Configuration of the measurement device for the data set */
    public HarpConfig cfgDevice;
    
    
    //
    // Measurement Data
    //
    
    /** The raw profile data acquired from diagnostics */
    public final WireHarp.DataRaw datRaw;
    
    /** The fitted profile data from the diagnostics */
    public final WireHarp.DataFit datFit;
    
    
    //
    // Measurement Signal Properties
    //
    
    /** The direct statistical parameters of the profile signals */
    public final WireHarp.FitAttrSet sigFitAttrs;

    
    
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
    public HarpData() throws ScadaAnnotationException {
        this.strTypId = null;
        this.strDevId = null;
        
        this.cfgDevice = new HarpConfig();
        
        this.datRaw  = new WireHarp.DataRaw();
        this.datFit  = new WireHarp.DataFit();
            
        this.sigFitAttrs  = new WireHarp.FitAttrSet();
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
    public HarpData(DataAdaptor daptSrc) {
        this();
        this.update(daptSrc);
    }
    
    /**
     * Create a new <code>ScannerData</code> object and
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
    public HarpData(WireHarp smfHarp) throws ConnectionException, GetException {
        this.strTypId = smfHarp.getType(); 
        this.strDevId = smfHarp.getId();

        // Attempt to collect the wire scanner configuration information
        //  Send a warning to the user if a failure occurs but continue
        //  on with the profile data.
        try {
            
            this.cfgDevice = HarpConfig.acquire(smfHarp);
            
        } catch (ConnectionException e) {
            String  strMsg = "Unable to acquire configuration data for " + smfHarp.getId();

            MainApplication.getEventLogger().logWarning(this.getClass(), strMsg);
            JOptionPane.showMessageDialog(null, strMsg, "Warning", JOptionPane.WARNING_MESSAGE);

            this.cfgDevice = new HarpConfig();
            
        } catch (GetException e) {
            String  strMsg = "Unable to acquire configuration data for " + smfHarp.getId();

            MainApplication.getEventLogger().logWarning(this.getClass(), strMsg);
            JOptionPane.showMessageDialog(null, strMsg, "Warning", JOptionPane.WARNING_MESSAGE);

            this.cfgDevice = new HarpConfig();
            
        }
        
        // Now acquire all the measurement data and the analysis
        //  data done by the wire scanner controller.
        //  If this fails then there is a big problem and we let it
        //  bubble up to the invoking methods.
        this.datRaw   = WireHarp.DataRaw.aquire(smfHarp);
        this.datFit   = WireHarp.DataFit.aquire(smfHarp);

        this.sigFitAttrs   = WireHarp.FitAttrSet.aquire(smfHarp);
    }

    
    /**
     * <p>
     * Performs an averaging operation with the given <code>HarpData</code> data set using the
     * given weighting factor.  The quantities within this data structure are averaged in
     * place with that of the given data structure.  Letting &lambda; denote the 
     * provided weighting factor, which is in the interval [0,1], the new values of any data
     * value, say <i>v'</i> are given by the formula
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>v'</i> = &lambda;<i>u</i> + (1 - &lambda;)<i>v</i>
     * <br/>
     * <br/>
     * where <i>v</i> is the previous value of <i>v'</i> and <i>u</i> is the new value
     * of <i>v</i> in argument <code>datAvg</code>. 
     * </p>  
     * <p>
     * <h4>NOTES:</h4>
     * &middot; Nothing is done to the position values of signals, they are unchanged of
     * current writing.
     * <br/>
     * &middot; Standard deviation quantities are weighted vectorally.
     * <br/>
     * &middot; The device configuration parameters remain unchanged
     * </p>
     * 
     * @param datAvg        harp data set <i>u</i> to average into this one 
     * @param dblFactor     weighting factor &lambda; &in; [0,1] for the new data set 
     * 
     * @throws IllegalArgumentException the provided data set is not the same size as this one
     *
     *
     * @author Christopher K. Allen
     * @since  May 1, 2014
     */
    public void average(HarpData datAvg, double dblWtFac) throws IllegalArgumentException {
        this.datRaw.average(datAvg.datRaw, dblWtFac);
        this.datFit.average(datAvg.datFit, dblWtFac);
        this.sigFitAttrs.average(datAvg.sigFitAttrs, dblWtFac);
    }
    
    /*
     * IProfileData Interface
     */
    
    /**
     * Returns the SMF device type identifier that uniquely identifies a hardware
     * a type of accelerator hardware. 
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
     * (see <code>{@link WireHarp.ScanConfig#stepCount}</code>).
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
        if (this.cfgDevice.devConfig == null)
            return -1;
        
        return this.cfgDevice.daqConfig.cntWires;
    }
    
    /**
     * Returns the raw signal data of this data set.
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
     * Returns the fitted signal data of this data set.  The fit
     * type is indicated in the data acquisition configuration
     * data structure.
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
     * are computed from the fitted signal data.
     *
     * @see xal.smf.impl.profile.IProfileData#getDataAttrs()
     *
     * @author Christopher K. Allen
     * @since  Apr 22, 2014
     */
    @Override
    public SignalAttrSet getDataAttrs() {
        return this.sigFitAttrs;
    }

    /*
     * Operations
     */
    
//    /**
//     * 
//     * @param angle     projection angle of the harp
//     * @param iWire     index of the wire within the harp (0 index origin)
//     * 
//     * @return          <code>true</code> if the wire produces valid data and 
//     *                  <code>false</code> otherwise
//     *
//     * @author Christopher K. Allen
//     * @since  Jul 1, 2014
//     */
//    public boolean  isValidWire(ProfileDevice.ANGLE angle, int iWire) {
//        return this.cfgDevice.daqConfig.validWire(angle, iWire);
//    }

    /**
     * Indicates whether or not the given wire with index <code>iWire</code> on the
     * harp for projection angle <code>angle</code> is producing valid data.
     * Uses the <code>WireHarp.DaqConfig</code> structure to determine of 
     * the given wire is valid, that is operating correctly and producing
     * good data.
     *
     * @see xal.smf.impl.profile.ProfileDevice.IProfileData#isValidWire(int)
     *
     * @author Christopher K. Allen
     * @since  Jul 2, 2014
     */
    @Override
    public boolean isValidWire(ProfileDevice.ANGLE angle, int iWire) {
        WireHarp.DaqConfig  cfgDaq    = cfgDevice.daqConfig;
        boolean             bolResult = cfgDaq.validWire(angle, iWire);
        
        return bolResult;
    }

    
    /*
     * DataListener Interface
     */
    
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
    @Override
    public void write(DataAdaptor snkData) {
        
        DataAdaptor     daptDev = snkData.createChild( this.dataLabel() );
        daptDev.setValue(STR_ATTR_TYPID, this.strTypId);
        daptDev.setValue(STR_ATTR_DEVID, this.strDevId);
        daptDev.setValue(STR_ATTR_FMTVER, LNG_VAL_FMTVER);
        
        this.cfgDevice.write(daptDev);
        
        this.datRaw.write(daptDev);
        this.datFit.write(daptDev);
        
        this.sigFitAttrs.write(daptDev);
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
//        DataAdaptor     daptDev = daptSrc.childAdaptor(this.dataLabel());
        DataAdaptor     daptDev = daptSrc;
        
        this.strDevId = daptDev.stringValue(STR_ATTR_DEVID);
        
        long    lngFmtVer = daptDev.longValue(STR_ATTR_FMTVER);
        
        if (lngFmtVer >= 1) {
            
            this.cfgDevice.update(daptDev);
            
            this.datRaw.update(daptDev);
            this.datFit.update(daptDev);
            
            this.sigFitAttrs.update(daptDev);
            
        } else {
            
            MainApplication.getEventLogger().logError(this.getClass(), "Data unreadable, has bad version format.");

        }

        if (lngFmtVer >= 2) 
            this.strTypId = daptDev.stringValue(STR_ATTR_TYPID);
        else
            this.strTypId = "unknown";
        
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
