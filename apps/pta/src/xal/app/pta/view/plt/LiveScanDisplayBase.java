/**
 * LiveScanDisplayBase.java
 *
 * Author  : Christopher K. Allen
 * Since   : Apr 23, 2014
 */
package xal.app.pta.view.plt;

import xal.app.pta.MainScanController;
import xal.app.pta.tools.ca.SmfPvMonitorPool;
import xal.ca.ConnectionException;
import xal.ca.MonitorException;
import xal.smf.NoSuchChannelException;
import xal.smf.impl.WireScanner;

import java.util.List;

/**
 * Adds common features for wire scanner live operation display.
 *
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author Christopher K. Allen
 * @since  Apr 23, 2014
 */
public abstract class LiveScanDisplayBase extends LiveDisplayBase implements MainScanController.IScanControllerListener {

    
    /*
     * Global Constants
     */
    
    /** Serialization version number */
    private static final long serialVersionUID = 1L;

    
    /*
     * Instance Attributes
     */
    
    //
    // Event Tools
    //
    
//    /** List of the active scan position monitors */
//    private final List<AcceleratorNode>     lstActDevs;
    
    /** Live data monitoring */
    private final SmfPvMonitorPool          mplLiveData;

    
    /*
     * Initialization
     */
    
    /**
     * Constructor for LiveScanDisplayBase.
     *
     * @param fmtPlt
     *
     * @author Christopher K. Allen
     * @since  Apr 23, 2014
     */
    public LiveScanDisplayBase(FORMAT fmtPlt) {
        super(fmtPlt);
        
//        this.lstActDevs  = new LinkedList<AcceleratorNode>();
        this.mplLiveData = new SmfPvMonitorPool();
    }

    
    /*
     * Abstract Methods
     */
    
    /**
     * <p>
     * Builds the pool of monitors for live data display. Child
     * classes must fill the pool 
     * <code>{@link LiveDisplayBase#mplLiveData}</code> with monitors 
     * (of type <code>{@link xal.app.pta.tools.ca.SmfPvMonitor}</code> )
     * whose actions update the graph 
     * <code>{@link LiveDisplayBase#pltSignal}</code>.  
     * </p>
     * <p>
     * The base class will manage the monitors within the pool.  The
     * monitors maintain the graphs of live data.  What data is being
     * displayed and the details of how it is displayed is the responsibility
     * of the child classes.  This method is the hook.
     * </p>
     *
     * @param   lstDevs list of devices we are going to monitor
     * 
     * @since  Feb 4, 2010
     * @author Christopher K. Allen
     * 
     * @see LiveDisplayBase#getDisplayPlot
     * @see LiveDisplayBase#getMonitorPool
     * @see xal.app.pta.tools.ca.SmfPvMonitor
     */
    abstract protected void buildMonitorPool(List<WireScanner> lstDevs);


    
    /*
     * Operations
     */
    
    /**
     * Returns the monitor pool containing the
     * PV monitors that update the graph display.
     *
     * @return  monitor pool maintained by this class
     * 
     * @since  Apr 12, 2010
     * @author Christopher K. Allen
     */
    protected SmfPvMonitorPool getMonitorPool() {
        return this.mplLiveData;
    }

    
    
    /*
     * MainScanController.IScanControllerListener Interface
     */
    
    /**
     * Responds to the scan initiated event from the
     * DAQ controller.
     *
     * 
     * @since  Nov 14, 2009
     * @author Christopher K. Allen
     *
     * @see xal.app.pta.MainScanController.IScanControllerListener#scanInitiated(java.util.List, MainScanController.SCAN_MODE)
     */
    @Override
    public void scanInitiated(List<WireScanner> lstDevs, MainScanController.SCAN_MODE mode) {
    
            // Make sure there is something to do
            if (lstDevs.size() == 0)
                return;
    
            // Set the currently active devices
//            this.lstActDevs.clear();
//            this.lstActDevs.addAll(lstDevs);
    
            // Reset the plots
            this.pltSignal.clear();
    //        this.pltTraces.scaleAbsissa(lstDevs);
            
            
            // See if we are monitoring live data
            if (!this.butLiveData.isSelected()) 
                return;
    
            try {
                this.mplLiveData.emptyPool();
                this.buildMonitorPool(lstDevs);
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
     * Responds to the scan completed event from the
     * DACQ controller panel.
     *
     * @param lstDevs       list of all the acquisition devices that 
     *                      have completed successfully.
     *
     * 
     * @since  Nov 14, 2009
     * @author Christopher K. Allen
     */
    @Override
    public void scanCompleted(List<WireScanner> lstDevs) {
        this.mplLiveData.stopAll();
        this.mplLiveData.emptyPool();  // CKA April 25, 2014
    }

    /**
     * Nothing to do here.
     *
     * @since   Dec 11, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.MainScanController.IScanControllerListener#scanActuatorsParked()
     */
    @Override
    public void scanActuatorsParked() {
    }

    /**
     * Responds to the scan aborted event from the
     * DAQ controller panel.  We stop the active
     * monitoring.  
     * 
     * @since  Nov 14, 2009
     * @author Christopher K. Allen
     *
     * @see xal.app.pta.MainScanController.IScanControllerListener#scanActuatorsParked()
     */
    @Override
    public void scanAborted() {
        this.mplLiveData.stopAll();
        this.mplLiveData.emptyPool();  // CKA April 25, 2014
    }

    /**
     * Nothing to do here.
     *
     * @since 	Apr 1, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.MainScanController.IScanControllerListener#scanActuatorsStopped()
     */
    @Override
    public void scanActuatorsStopped() {
    }

    /**
     * Nothing done here.
     *
     * @since   Dec 2, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.MainScanController.IScanControllerListener#scanDeviceFailure(WireScanner)
     */
    @Override
    public void scanDeviceFailure(WireScanner smfDev) {
    }

}
