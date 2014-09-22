/**
 * LiveHarpDisplayPanel.java
 *
 * Author  : Christopher K. Allen
 * Since   : Apr 23, 2014
 */
package xal.app.pta.view.plt;

import xal.app.pta.MainHarpController;
import xal.app.pta.MainScanController;
import xal.app.pta.daq.MeasurementData;
import xal.app.pta.tools.ca.SmfPvMonitor;
import xal.app.pta.tools.ca.SmfPvMonitorPool;
import xal.ca.ChannelRecord;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.MonitorException;
import xal.extension.widgets.plot.BasicGraphData;
import xal.smf.NoSuchChannelException;
import xal.smf.impl.WireHarp;
import xal.smf.impl.profile.ProfileDevice.ANGLE;
import xal.smf.impl.profile.ProfileDevice.IProfileData;
import xal.smf.impl.profile.ProfileDevice.IProfileDomain;
import xal.smf.scada.BadStructException;
import xal.smf.scada.XalPvDescriptor;
import xal.smf.scada.ScadaAnnotationException;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Class that displays live acquisition data for the wire harps.
 *
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author Christopher K. Allen
 * @since  Apr 23, 2014
 */
public class LiveHarpDisplayPanel extends LiveDisplayBase implements MainHarpController.IHarpControllerListener {

        
    /*
     * Inner Classes
     */
    
    /**
     * Plots the live data from the given list of 
     * DAQ devices.
     *
     * @since  Feb 4, 2010
     * @author Christopher K. Allen
     */
    public class SampleTakenAction implements SmfPvMonitor.IAction {


        /*
         * Local Attributes
         */

        /** The graph curve that we are updating */
        private final BasicGraphData            crvSig;

        /** The array of sample positions */
        private final double[]                  arrPos;


        /**
         * Create a new <code>SampleTakenAction</code> object.
         *
         * @param arrPos        array of sample positions (does not change)
         * @param crvSig        the graph curve of the profile signal 
         *
         * @since     Feb 4, 2010
         * @author    Christopher K. Allen
         */
        public SampleTakenAction(double[] arrPos, BasicGraphData crvSig) {
            this.arrPos = arrPos;
            this.crvSig = crvSig;
        }

        /**
         * Responds to a change in values available in the DAQ registers of the
         * harp.  Update the graph curve with the new data 
         * for this device wire.
         *
         * @since       Feb 4, 2010
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.ca.SmfPvMonitor.IAction#valueChanged(xal.ca.ChannelRecord, xal.app.pta.tools.ca.SmfPvMonitor)
         */
        @Override
        public void valueChanged(ChannelRecord val, SmfPvMonitor mon) {

            // Retrieve the live data from each device and build list of plot data
            double[]      arrVal = val.doubleArray();

//            LiveHarpDisplayPanel.super.getDisplayPlot().clear();
            this.crvSig.updateValues(this.arrPos, arrVal);
            
//            System.out.println("LiveHarpDisplayPanel.SampleTakenAction.valueChanged(ChannelRecord,SmfPvMonitor)");
//            System.out.println("  this.arrPos=" + LiveHarpDisplayPanel.this.arrayString(this.arrPos) );
//            System.out.println("  arrVal=" + LiveHarpDisplayPanel.this.arrayString(arrVal));
        }
        
    }

    
    /**
     * Data structure for maintaining data and parameters needed to draw the
     * data curve for a particular profile device.  Usually stored in a 
     * map and accessed by device.
     *
     * @author Christopher K. Allen
     * @since  Apr 30, 2014
     */
    private class CurveProperties {
        
        /*
         * Local Records
         */
        
        /** the profile device producing the data */
        public final    WireHarp        smfDev;
        
        /** the curve displaying the profile device data */
        public final    BasicGraphData  crvDat;
        
        /** the projection angle for the data */   
        public final    ANGLE           angPrj;
        
        /** the color of the curve */
        public final    Color           clrCrv;
        
        /** the sample positions for the data on the abscissa */ 
        public final    IProfileDomain  domCrv;
        
        /**
         * Create and initialize a new curve data structure.
         *
         * @param smfDev    the profile device producing the data
         * @param crvDat    the curve displaying the profile device data
         * @param angPrj    the projection angle for the data   
         * @param clrCrv    the color of the curve
         * @param arrPos    the sample positions for the data on the abscissa 
         *
         * @author Christopher K. Allen
         * @since  Apr 30, 2014
         */
        public CurveProperties(WireHarp smfDev, BasicGraphData crvDat, ANGLE angPrj, Color clrCrv, IProfileDomain domCrv) {
            this.smfDev = smfDev;
            this.crvDat = crvDat;
            this.angPrj = angPrj;
            this.clrCrv = clrCrv;
            this.domCrv = domCrv;
        }
    }
    
    /**
     * Container for organizing curve data records according to the DAQ device
     * and the projection plane of the data.
     *
     * @author Christopher K. Allen
     * @since  Apr 30, 2014
     */
    private class CurvePropertyMap  {
        
        /*
         * Local Attributes
         */
        
        /** Map structure that maintains the internal maps by profile device */
        private final Map<WireHarp, Map<ANGLE, CurveProperties>>    mapData;

        /*
         * Initialization
         */
        
        /**
         * Constructor for CurvePropertyMap.
         *
         * @author Christopher K. Allen
         * @since  Apr 30, 2014
         */
        public CurvePropertyMap() {
            this.mapData = new HashMap<WireHarp, Map<ANGLE, CurveProperties>>();
        }
        
        /*
         * Operations
         */

        /**
         * Returns a list of all <code>CurveProperties</code> objects managed
         * by this data structure.
         * 
         * @return  all the <code>CurveProperties</code> objects contained in the data structure
         *
         * @author Christopher K. Allen
         * @since  May 1, 2014
         */
        public List<CurveProperties>  curves() {
            List<CurveProperties>   lstProps = new LinkedList<CurveProperties>();
            
            for (Map<ANGLE, CurveProperties> map : this.mapData.values()) {
                lstProps.addAll(map.values());
            }
            
            return lstProps;
        }
        
        /**
         * Creates a new set of curve properties for each profile device in the
         * give list.  Note that the current data structure is not cleared, however,
         * if there are already entries for a repeated device it is clobbered. 
         * 
         * @param lstDevs   devices requesting new properties.
         *
         * @author Christopher K. Allen
         * @since  Apr 30, 2014
         */
        public void createPropertiesFor(List<WireHarp> lstDevs) {

            // Initialize the curve data
            for (WireHarp smfHarp : lstDevs) {

                try {

                    // Get the domain of the curves
                    IProfileDomain   domDaq = WireHarp.DaqConfig.acquire(smfHarp);
                    
                    Color            clrSig = LiveHarpDisplayPanel.this.getDeviceColorMap().get(smfHarp);

                    // Now for each projection angle
                    for (ANGLE angSig : ANGLE.values()) {

                        // Create the signal's graph object
                        BasicGraphData   crvSig = new BasicGraphData();
                        if (clrSig != null)
                            crvSig.setGraphColor(clrSig);

                        CurveProperties  datCrv = new CurveProperties(smfHarp, crvSig, angSig, clrSig, domDaq);

                        this.put(smfHarp, angSig, datCrv);
                    }

                } catch (NoSuchChannelException e) {
                    getLogger().logException(getClass(), e, "NO Channel Error when gathering device configuration data");

                } catch (ConnectionException e) {
                    getLogger().logException(getClass(), e, "Connection Error when gathering device configuration data");

                } catch (GetException e) {
                    getLogger().logException(getClass(), e, "Get Error when gathering device configuration data");

                }
            }
        }

        /**
         * Enters the curve data for the given (device,angle) pair into the
         * data structure.  
         * 
         * @param devKey    profile device key
         * @param angKey    project angle key
         * @param datVal    curve data value for the given (device,angle) key pair
         *
         * @author Christopher K. Allen
         * @since  Apr 30, 2014
         */
        public void put(WireHarp devKey, ANGLE angKey, CurveProperties datVal) {
            
            // Get the map of angles to curve data for this device
            Map<ANGLE, CurveProperties>   mapByAng = this.mapData.get(devKey);
            
            // An entry for the device has not be made yet
            if (mapByAng == null)  {
                mapByAng = new HashMap<ANGLE, CurveProperties>();
                this.mapData.put(devKey, mapByAng);
            }
            
            // Put the curve data entry for the angle into the map
            mapByAng.put(angKey, datVal);
        }
        
        /**
         * Returns the curve data entry associated with the (device,angle) key
         * pair. If there is no entry found for the key pair then a 
         * <code>null</code> value is returned.
         * 
         * @param devKey    profile data acquisition device (key)
         * @param angKey    angle of the projection data (key)
         * 
         * @return          the curve data for the given device and angle
         *                  or <code>null</code> if none is found
         *
         * @author Christopher K. Allen
         * @since  Apr 30, 2014
         */
        public CurveProperties    get(WireHarp devKey, ANGLE angKey) {
            Map<ANGLE, CurveProperties>   mapByAng = this.mapData.get(devKey);
            
            if (mapByAng == null)
                return null;
            
            CurveProperties   datCrv = mapByAng.get(angKey);
            
            return datCrv;
        }
        
        /**
         * Clears out the data structure of all entries, including the 
         * internal map structures.
         *
         * @author Christopher K. Allen
         * @since  Apr 30, 2014
         */
        public void clear() {
            for (Map<ANGLE,CurveProperties> mapValue : this.mapData.values() ) 
                mapValue.clear();
            
            this.mapData.clear();
        }
    }
    
    
    /*
     * Global Constants
     */
    
    /** Serialize version number  */
    private static final long serialVersionUID = 1L;
    

    /*
     * Local Attributes
     */
    
    /** List of the active scan position monitors */
    private final List<WireHarp>    lstActDevs;

    /** Map of monitored devices to the graph curve data */
    private final CurvePropertyMap          mapCrvDat;

    /** Live data monitoring */
    private final SmfPvMonitorPool  mplLiveData;


    
    /*
     * Initialization
     */
    
    /**
     * Constructor for LiveHarpDisplayPanel.
     *
     * @param fmtPlt    display format of the graphs 
     *
     * @author Christopher K. Allen
     * @since  Apr 23, 2014
     */
    public LiveHarpDisplayPanel(FORMAT fmtPlt) {
        super(fmtPlt);

      this.lstActDevs  = new LinkedList<WireHarp>();
      this.mapCrvDat   = new CurvePropertyMap();
      this.mplLiveData = new SmfPvMonitorPool();
      
      this.createGuiComponents();
    }
    

    /*
     * Operations
     */
    
    /**
     * Sets the list of profile device we are to monitor.
     * 
     * @param lstActDev     new list of profile devices for which to display data
     *
     * @author Christopher K. Allen
     * @since  Apr 30, 2014
     */
    public void setProfileDevices(List<WireHarp> lstDevs) {
        this.lstActDevs.clear();
        this.lstActDevs.addAll(lstDevs);
        
        this.mapCrvDat.clear();
        this.mapCrvDat.createPropertiesFor(lstDevs);
    }

    
    /*
     * Base Class Overrides
     */
    
    /**
     * Override the base class implementation so that we only display data for
     * harp devices.
     *
     * @see xal.app.pta.view.plt.LiveDisplayBase#displayRawData(xal.app.pta.daq.MeasurementData, java.util.Map)
     *
     * @author Christopher K. Allen
     * @since  Sep 22, 2014
     */
    @Override
    public void displayRawData(MeasurementData datMsmt, Map<String, Color> mapDevClr) {
        for (IProfileData datDev : datMsmt.getDataSet()) {
            
            // Make sure the data is from a harp device
            if ( datDev.getDeviceTypeId() != WireHarp.STR_TYPE_ID ) 
                continue;

            String  strDevId = datDev.getDeviceId();
            Color   clrDevKey = mapDevClr.get(strDevId);

            super.getDisplayPlot().setCurveLabel(strDevId);
            super.getDisplayPlot().setCurvePoints(true);
            super.getDisplayPlot().setCurveColor(clrDevKey);
            super.getDisplayPlot().displayRawData(datDev);
        }
    }


    /**
     * Must stop monitoring before we clear the graphs.
     *
     * @see xal.app.pta.view.plt.LiveDisplayBase#clearGraphs()
     *
     * @author Christopher K. Allen
     * @since  Apr 30, 2014
     */
    @Override
    public void clearGraphs() {
        this.endMonitoring();
        this.setLiveData(false);
        super.clearGraphs();
    }
    

    /* 
     * MainHarpController.IHarpControllerListener Interface
     */
    
    /**
     * We initialize the graphs and sample-taken monitor pool (if the live data
     * option has been selected).  If we are monitoring live data then the 
     * monitor pool is started.
     * 
     * @see xal.app.pta.MainHarpController.IHarpControllerListener#daqInitiated(java.util.List, int)
     *
     * @author Christopher K. Allen
     * @since  Apr 25, 2014
     */
    @Override
    public void daqInitiated(List<WireHarp> lstDevs, int cntSamples) {

        this.setProfileDevices(lstDevs);
        this.setLiveData(false);
        this.endMonitoring();
        
        this.pltSignal.clear();
        this.initializeGraphs();
        

        // Display each curve on the graph
        for (CurveProperties prpCrv : this.mapCrvDat.curves() ) {
            ANGLE           angSig = prpCrv.angPrj;
            BasicGraphData  crvSig = prpCrv.crvDat;
            
            super.getDisplayPlot().displayCurve(angSig, crvSig);
        }

//        this.beginMonitoring();
        
//        // Make sure there is something to do
//        if (lstDevs.size() == 0)
//            return;
//
//        // Remember the devices
//        this.setProfileDevices(lstDevs);
//        
//        // Initialize the graphs
//        this.pltSignal.clear();
//        this.initializeGraphs(lstDevs);
////      this.pltTraces.scaleAbsissa(lstDevs);
//        
//        // See if we are monitoring live data
//        if (!this.butLiveData.isSelected()) 
//            return;
//
//        try {
//            this.mplLiveData.emptyPool();
//            this.buildMonitorPool(lstDevs);
//            this.mplLiveData.begin(true);
//            
//        } catch (ConnectionException e) {
//            getLogger().logException(getClass(), e, "Unable to start monitor pool");
//            
//        } catch (MonitorException e) {
//            getLogger().logException(getClass(), e, "Unable to start monitor pool");
//            
//        } catch (NoSuchChannelException e) {
//            getLogger().logException(getClass(), e, "Unable to start monitor pool");
//            
//        }
    }

    /**
     * Nothing to do.  We are catching this event with a Channel Access monitor directly
     * and (re)drawing graphs with newly acquired data.
     *
     * @see xal.app.pta.MainHarpController.IHarpControllerListener#daqSampled(xal.smf.impl.WireHarp, int)
     *
     * @author Christopher K. Allen
     * @since  Apr 25, 2014
     */
    @Override
    public void daqSampled(WireHarp smfHarp, int cntSample) {

//        System.out.println("LiveHarpDisplay.daqSampled(WireHarp,int) - I've been called with argument " + cntSample);
        try {

            WireHarp.DataRaw datRaw = WireHarp.DataRaw.aquire(smfHarp);

            for (ANGLE angSig : ANGLE.values()) {
                CurveProperties   recCrvData = this.mapCrvDat.get(smfHarp, angSig);

                if (recCrvData == null)
                    continue;

                BasicGraphData      crvDev = recCrvData.crvDat;
                IProfileDomain      domCrv = recCrvData.domCrv;
                ANGLE               angPrj = recCrvData.angPrj;
                double[]            arrPos = domCrv.getSamplePositions(angPrj);
                double[]            arrVal = datRaw.getSignal(angPrj).val;
                
//                System.out.println("  I'm trying to draw a graph :");
//                System.out.println("  this.arrPos=" + LiveHarpDisplayPanel.this.arrayString(arrPos) );
//                System.out.println("  arrVal=" + LiveHarpDisplayPanel.this.arrayString(arrVal));

                crvDev.updateValues(arrPos, arrVal);
            }

        } catch (NoSuchChannelException e) {
            getLogger().logException(getClass(), e, "NO Channel Error in acquiring new data on sample event");

        } catch (ConnectionException e) {
            getLogger().logException(getClass(), e, "Connection Error in acquiring new data on sample event");

        } catch (GetException e) {
            getLogger().logException(getClass(), e, "Get Error in acquiring new data on sample event");

        } catch (ScadaAnnotationException e) {
            getLogger().logException(getClass(), e, "Get Error in acquiring new data on sample event");

        } catch (BadStructException e) {
            getLogger().logException(getClass(), e, "Get Error in acquiring new data on sample event");

        }
    }

    /**
     *
     * @see xal.app.pta.MainHarpController.IHarpControllerListener#daqCompleted(java.util.List)
     *
     * @author Christopher K. Allen
     * @since  Apr 25, 2014
     */
    @Override
    public void daqCompleted(List<WireHarp> lstDevs) {
        this.setLiveData(false);
        this.endMonitoring();
    }

    /**
     *
     * @see xal.app.pta.MainHarpController.IHarpControllerListener#daqAborted()
     *
     * @author Christopher K. Allen
     * @since  Apr 25, 2014
     */
    @Override
    public void daqAborted() {
        this.setLiveData(false);
        this.endMonitoring();
    }

    /**
     *
     * @see xal.app.pta.MainHarpController.IHarpControllerListener#daqDeviceFailure(xal.smf.impl.WireHarp)
     *
     * @author Christopher K. Allen
     * @since  Apr 25, 2014
     */
    @Override
    public void daqDeviceFailure(WireHarp smfDev) {
    }

    
    /*
     * Support Methods
     */
    
    /**
     * Initializes all the components of the
     * GUI display.
     *
     * 
     * @since  Aug 19, 2009
     * @author Christopher K. Allen
     */
    private void createGuiComponents(){
        
        this.butLiveData.addActionListener(
                        new ActionListener() {
                            public void actionPerformed(ActionEvent e) {

                                // See if we are monitoring live data
                                if (LiveHarpDisplayPanel.this.butLiveData.isSelected()) 
                                    LiveHarpDisplayPanel.this.beginMonitoring();
                                else 
                                    LiveHarpDisplayPanel.this.endMonitoring();
                            }
                        }
        );
        
    }
        
    /**
     * Begin the monitoring of current profile devices.  New data from the
     * data monitors is displayed on the graphs via the <code>SampleTakenAction</code>
     * actions.  Once action is created for each device then registered into the
     * monitor pool.
     *
     * @author Christopher K. Allen
     * @since  Apr 30, 2014
     */
    private void beginMonitoring() {
            
            // Make sure there is something to do
            if (this.lstActDevs.size() == 0)
                return;
    
            // Initialize the graphs
            this.pltSignal.clear();
            this.initializeGraphs();
    //      this.pltTraces.scaleAbsissa(lstDevs);
            
            try {
                this.mplLiveData.emptyPool();
                this.buildMonitorPool();
                this.mplLiveData.begin(true);
                
            } catch (ConnectionException e) {
                getLogger().logException(getClass(), e, "Unable to start monitor pool");
                
            } catch (MonitorException e) {
                getLogger().logException(getClass(), e, "Unable to start monitor pool");
                
            } catch (NoSuchChannelException e) {
                getLogger().logException(getClass(), e, "Unable to start monitor pool");
                
            }
        }
    
    /**
     * Stop and terminate all profile device data monitoring.
     *
     * @author Christopher K. Allen
     * @since  Apr 30, 2014
     */
    private void endMonitoring() {
        this.mplLiveData.stopActive();
        this.mplLiveData.emptyPool();
    }

    /**
     * We want to catch this event in order to auto-scale the display
     * before updating the plot with single data points.  
     * Otherwise the plot will auto-scale each update and the user does
     * not get an appreciation for the status of the scan. 
     * 
     * @since  April 23, 2014
     * @author Christopher K. Allen
     *
     * @see xal.app.pta.MainScanController.IScanControllerListener#scanInitiated(java.util.List, MainScanController.SCAN_MODE)
     */
    private void initializeGraphs(/*List<WireHarp> lstDevs*/) {
        
        List<IProfileDomain>    lstDomain = new LinkedList<IProfileDomain>();
    
        for (WireHarp smfHarp : this.lstActDevs) {

            // The IProfileDomain object for each angle is the same, just choose one
            CurveProperties   prpHor = this.mapCrvDat.get(smfHarp, ANGLE.HOR);
    
            lstDomain.add(prpHor.domCrv);
            
//            try {
//                // Get the scan configuration for this device and look at scan range
//                WireHarp.DaqConfig  cfgDaq = WireHarp.DaqConfig.acquire(smfHarp);
//    
//                lstDomain.add(cfgDaq);
//    
//            } catch (ConnectionException e) {
//                this.getLogger().logException(this.getClass(), e, "DevId = " + smfHarp.getId()); //$NON-NLS-1$
//                continue;
//    
//            } catch (GetException e) {
//                this.getLogger().logException(this.getClass(), e, "DevId = " + smfHarp.getId()); //$NON-NLS-1$
//                continue;
//    
//            }
        }
        
        super.getDisplayPlot().scaleAbsissa(lstDomain);
    }

    /**
     * Builds the pool of monitors for live data acquisition 
     * monitoring. Also adds the corresponding <code>BasicGraphData</code>
     * objects to the display panel (i.e., one for each device signal
     * we monitor).
     *
     * @since  Feb 4, 2010
     * @author Christopher K. Allen
     */
    private void buildMonitorPool() {

        // Else we start the live data monitoring
        for (WireHarp smfHarp : this.lstActDevs) {

//            try {
//
                for (ANGLE angle : ANGLE.values()) {

                    // Create the signal's graph object
//                    Color            clrSig = this.getDeviceColorMap().get(smfHarp);
//                    BasicGraphData   crvSig = new BasicGraphData();
//                    if (clrSig != null)
//                        crvSig.setGraphColor(clrSig);
//                    
//                    // Create the wire scanner position changed action
//                    IProfileDomain   domDaq = WireHarp.DaqConfig.acquire(smfHarp);
//                    
//                    double[]         arrPos = domDaq.getSamplePositions(angle);
////                    XalPvDescriptor     dcrVal = angle.getSignalValFd(WireHarp.DataRaw.class);
////                    
////                    Channel          chnSig = smfHarp.getAndConnectChannel(dcrVal.getRbHandle());
                    CurveProperties prpCrv = this.mapCrvDat.get(smfHarp, angle);
                    BasicGraphData  crvSig = prpCrv.crvDat;
                    IProfileDomain  domSig = prpCrv.domCrv;
                    ANGLE           angCrv = prpCrv.angPrj;
                    double[]        arrPos = domSig.getSamplePositions(angCrv);
                    SampleTakenAction actPos = new SampleTakenAction(arrPos, crvSig);

                    // Create the harp data changed monitor and add to monitor pool
                    XalPvDescriptor  dcrPos = angle.getSignalValFd(WireHarp.DataRaw.class);
                    this.mplLiveData.createMonitor(smfHarp, dcrPos, actPos);
                    
                    // Initialize the plot display if there is any data
                    super.getDisplayPlot().displayCurve(angle, crvSig);
                }

//            } catch (NoSuchChannelException e) {
//                getLogger().logException(getClass(), e, "NO Channel Error in building live data monitoring");
//
//            } catch (ConnectionException e) {
//                getLogger().logException(getClass(), e, "Connection Error in building live data monitoring");
//
//            } catch (GetException e) {
//                getLogger().logException(getClass(), e, "Get Error in building live data monitoring");
//                
//            }
        }
    }

    /**
     * Make a string representation of the given double array.
     * 
     * @param arrDbl    double array of values
     * 
     * @return          string representation of the double array
     *
     * @author Christopher K. Allen
     * @since  Apr 29, 2014
     */
    private String  arrayString(double[] arrDbl) {
        StringBuffer    buf = new StringBuffer();

        buf.append("(");
        for (double dbl : arrDbl) {
            buf.append(dbl);
            buf.append(",");
        }
        buf.append(")");
        
        return buf.toString();
    }
}
