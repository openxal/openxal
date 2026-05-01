/**
 * LiveAcquisitionDisplayPanel.java
 *
 *  Created	: Feb 18, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.view.plt;

import xal.app.pta.IDocView;
import xal.app.pta.MainDocument;
import xal.app.pta.MainScanController;
import xal.app.pta.daq.MeasurementData;
import xal.app.pta.daq.ScannerData;
import xal.app.pta.tools.ca.SmfPvMonitor;
import xal.ca.Channel;
import xal.ca.ChannelRecord;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.extension.widgets.plot.BasicGraphData;
import xal.smf.NoSuchChannelException;
import xal.smf.impl.WireHarp;
import xal.smf.impl.WireScanner;
import xal.smf.impl.profile.ProfileDevice.ANGLE;
import xal.smf.impl.profile.ProfileDevice.IProfileData;
import xal.smf.impl.profile.ProfileDevice.IProfileDomain;
import xal.smf.scada.XalPvDescriptor;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Displays (i.e., GUI display) the acquisition data taken 
 * directly from the diagnostic devices, not the data stored
 * within the application.  The displayed data may be taken 
 * live while the acquisition process is running (i.e., during
 * the scan), or from the post-acquisition data buffers within
 * the diagnostic devices.
 * </p>
 * <p>
 * NOTE: This class also responds to the 
 * <code>{@link IDocView#updateMeasurementData(MainDocument)}</code> by 
 * refreshing the display with the new measurement data.
 * </p>
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Feb 18, 2010
 * @author Christopher K. Allen
 * 
 */
public class LiveScanDisplayPanel extends LiveScanDisplayBase  {

    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;

        
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
    public class PosMonitorAction implements SmfPvMonitor.IAction {


        /*
         * Local Attributes
         */

        /** The wire scanner signal channel */
        private final Channel                   chnSig;

        /** The graph curve that we are updating */
        private final BasicGraphData            crvSig;

        /** The wire angle */
        private final ANGLE                     enmAng;


        /**
         * Create a new <code>PosMonitorAction</code> object.
         *
         * @param crvSig        the graph curve of the profile signal 
         * @param chnSig        the signal channel
         * @param enmAng        the wire which we are monitoring 
         *
         * @since     Feb 4, 2010
         * @author    Christopher K. Allen
         */
        public PosMonitorAction(ANGLE enmAng, BasicGraphData crvSig, Channel chnSig) {
            this.enmAng = enmAng;
            this.chnSig = chnSig;
            this.crvSig = crvSig;
        }

        /**
         * Responds to a change in actuator position.  Fires a 
         * <code>{@link LiveScanDisplayPanel.ProfileCurveUpdater}</code> 
         * thread to fetch the signal value and update the graph curve
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
            double      dblPos = val.doubleValue();

            ProfileCurveUpdater     thdUpdate = new ProfileCurveUpdater(dblPos, this.enmAng, this.crvSig, this.chnSig);
            thdUpdate.start();
        }
    }

    /**
     * Thread spawned from 
     * <code>{@link LiveScanDisplayPanel.PosMonitorAction}</code>
     * 
     *
     * @since  Feb 11, 2010
     * @author Christopher K. Allen
     */
    public class ProfileCurveUpdater extends Thread {

        /** 
         * Time delay before we update the graph (in milli-seconds)
         * 
         *  @deprecated not used, not necessary to delay thread spawn
         */
        @Deprecated
        public static final long       LNG_TM_DELAY = 10;


        /*
         * Instance attributes
         */

//        /** The plot containing the curve */
//        private final ANGLE             enmAng;

        /** the actuator position */
        private final double            dblPos;

        /** The graph curve we are updating */
        private final BasicGraphData    crvSig;

        /** the signal channel */
        private final Channel           chnSig;

        /**
         * Create a new <code>ProfileCurveUpdater</code> object.
         *
         * @param dblPos        position of the actuator
         * @param enmAng        the graph to be updated 
         * @param crvSig        graph curve to be updated
         * @param chnSig        channel containing signal value
         *
         * @since     Feb 11, 2010
         * @author    Christopher K. Allen
         */
        public ProfileCurveUpdater(double dblPos, ANGLE enmAng, BasicGraphData crvSig, Channel chnSig) {
//            this.enmAng = enmAng;
            this.crvSig = crvSig;
            this.dblPos = dblPos;
            this.chnSig = chnSig;
        }

        /**
         * Sleeps for <code>{@link ProfileCurveUpdater#LNG_TM_DELAY}</code>
         * 
         *
         * @since 	Feb 11, 2010
         * @author  Christopher K. Allen
         *
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {

            // Retrieve the live data from each device and build list of plot data
            try {
                double  dblSig = this.chnSig.getValDbl();

                this.crvSig.addPoint(this.dblPos, dblSig);
                
            } catch (ConnectionException e) {
                getLogger().logException(this.getClass(), e, "CA connection exception on " + this.chnSig.getId());

            } catch (GetException e) {
                getLogger().logException(this.getClass(), e, "CA get exception on " + this.chnSig.getId());

            }  
        }

    }


    /*
     * Local Attributes
     */
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>LiveScanDisplayPanel</code> object.
     *
     * @param fmtDspl   format for displaying the plot data
     *
     * @since     Feb 18, 2010
     * @author    Christopher K. Allen
     */
    public LiveScanDisplayPanel(FORMAT fmtDspl) {
        super(fmtDspl);
    }
   
    
    
    /*
     *  MainScanController.IDaqControllerListener Interface
     */
    
    /**
     * We want to catch this event in order to auto-scale the display
     * before updating the plot with single data points.  
     * Otherwise the plot will auto-scale each update and the user does
     * not get an appreciation for the status of the scan. 
     * 
     * @since  Nov 14, 2009
     * @author Christopher K. Allen
     *
     * @see xal.app.pta.MainScanController.IScanControllerListener#scanInitiated(java.util.List, MainScanController.SCAN_MODE)
     */
    @Override
    public void scanInitiated(List<WireScanner> lstDevs, MainScanController.SCAN_MODE mode) {

        List<IProfileDomain>    lstDomain = new LinkedList<IProfileDomain>();

        for (WireScanner smfScan : lstDevs) {


            try {
                // Get the scan configuration for this device and look at scan range
                WireScanner.ScanConfig  cfgDaq = WireScanner.ScanConfig.acquire(smfScan);

                lstDomain.add(cfgDaq);

            } catch (ConnectionException e) {
                this.getLogger().logException(this.getClass(), e, "DevId = " + smfScan.getId()); //$NON-NLS-1$
                continue;

            } catch (GetException e) {
                this.getLogger().logException(this.getClass(), e, "DevId = " + smfScan.getId()); //$NON-NLS-1$
                continue;

            }
        }
        
        super.scanInitiated(lstDevs, mode);
        super.getDisplayPlot().scaleAbsissa(lstDomain);
    }

    
    /*
     * Base Class Overrides
     */

    /**
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
            if ( datDev.getDeviceTypeId() != WireScanner.s_strType ) 
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
     * Builds the pool of monitors for live data acquisition 
     * monitoring. Also adds the corresponding <code>BasicGraphData</code>
     * objects to the display panel (i.e., one for each device signal
     * we monitor).
     *
     * @param   lstDevs list of DAQ devices we are going to monitor
     * 
     * @since  Feb 4, 2010
     * @author Christopher K. Allen
     * 
     * @see LiveScanDisplayBase#buildMonitorPool(List)
     */
    @Override
    protected void buildMonitorPool(List<WireScanner> lstDevs) {

        // Else we start the live data monitoring
        for (WireScanner ws : lstDevs) {

            try {

                for (ANGLE angle : ANGLE.values()) {

                    // Create the signal's graph object
//                    Color            clrSig = this.mapKeyColor.get(ws);
                    Color            clrSig = this.getDeviceColorMap().get(ws);
                    BasicGraphData   crvSig = new BasicGraphData();
                    if (clrSig != null)
                        crvSig.setGraphColor(clrSig);
                    
                    // Create the wire scanner position changed action
                    XalPvDescriptor     dcrVal = angle.getSignalValFd(WireScanner.DataLivePt.class);
                    
                    Channel          chnSig = ws.getAndConnectChannel(dcrVal.getRbHandle());
                    PosMonitorAction actPos = new PosMonitorAction(angle, crvSig, chnSig);

                    // Create the wire scanner position changed monitor and add to monitor pool
                    XalPvDescriptor  dcrPos = angle.getSignalPosFd(WireScanner.DataLivePt.class);
                    this.getMonitorPool().createMonitor(ws, dcrPos, actPos);
                    
                    // Display the first point in the curve
                    super.getDisplayPlot().displayCurve(angle, crvSig);
                }

            } catch (NoSuchChannelException e) {
                getLogger().logException(getClass(), e, "Error in building live data monitoring");

            } catch (ConnectionException e) {
                getLogger().logException(getClass(), e, "Error in building live data monitoring");

            }
        }
    }

}

