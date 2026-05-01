/**
 * LiveAcquisitionDisplayPanel.java
 *
 *  Created	: Feb 18, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.view.plt;

import xal.app.pta.tools.ca.SmfPvMonitor;
import xal.ca.Channel;
import xal.ca.ChannelRecord;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.extension.widgets.plot.BasicGraphData;
import xal.smf.AcceleratorNode;
import xal.smf.NoSuchChannelException;
import xal.smf.impl.WireScanner;
import xal.smf.impl.profile.ProfileDevice.ANGLE;
import xal.smf.scada.XalPvDescriptor;

import java.util.List;


/**
 * <p>
 * Displays (i.e., GUI display) the acquisition trace taken 
 * directly from the diagnostic devices, not the data stored
 * within the application.  
 * </p>
 * <p>
 * NOTE: 
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
public class LiveTraceDisplayPanel extends LiveScanDisplayBase {

    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;

        
    /*
     * Inner classes
     */

    /**
     * Plots the live data from the given list of 
     * DAQ devices.
     *
     * @since  Feb 4, 2010
     * @author Christopher K. Allen
     */
    public class TraceMonitorAction implements SmfPvMonitor.IAction {


        /*
         * Local Attributes
         */

        /** The wire angle */
        private final ANGLE                     enmAng;

        /** The position coordinate step size */
        private final double                    dblDel;
        
        /** The graph curve that we are updating */
        private final GraphDisplayBase          pltTrace;

        /** The array of position values */
        private double[]                        arrTime;

        /**
         * Create a new <code>TraceMonitorAction</code> object.
         *
         * @param enmAng        the wire which we are monitoring 
         * @param dblDel        the sample position step length
         * @param pltTrace      the graph of the trace signal 
         *
         * @since     Feb 4, 2010
         * @author    Christopher K. Allen
         */
        public TraceMonitorAction(ANGLE enmAng, double dblDel, GraphDisplayBase pltTrace) {
            this.enmAng = enmAng;
            this.dblDel = dblDel;
            this.pltTrace = pltTrace;
            
            this.arrTime = null;
        }

        /**
         * Responds to a change in actuator position.  Updates the graph curve
         * for this device wire.
         *
         * @since       Feb 4, 2010
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.ca.SmfPvMonitor.IAction#valueChanged(xal.ca.ChannelRecord, xal.app.pta.tools.ca.SmfPvMonitor)
         */
        @Override
        public void valueChanged(ChannelRecord val, SmfPvMonitor mon) {

            // Retrieve the trace data and refresh the plot
            double[]        arrTrc = val.doubleArray();
            double[]        arrTm  = this.timeArray(arrTrc.length);
            BasicGraphData  crvTrc = new BasicGraphData();

            crvTrc.addPoint(arrTm, arrTrc);

            pltTrace.clear(this.enmAng);
            pltTrace.displayCurve(enmAng, crvTrc);
        }

        
        /**
         * Create the abscissa vector for the trace signal. These
         * values do not change and may be created once.
         *
         * @param cntPts    number of value in the array
         * 
         * @return          vector of time instances 
         * 
         * @since  Apr 5, 2010
         * @author Christopher K. Allen
         */
        private double[]    timeArray(int cntPts) {

            // Check if we have already computed time vector
            if (this.arrTime != null)
                return this.arrTime;
            
            // Compute the time vector
            double      dblTm  = 0.0;
            this.arrTime = new double[cntPts];
            for (int i=0; i<cntPts; i++) { 
                this.arrTime[i] = dblTm;
                dblTm += this.dblDel;
            }
            
            return this.arrTime;
        }
    }



    
    /*
     * Initialization
     */
    
    
    /**
     * Create a new <code>LiveScanDisplayPanel</code> object.
     *
     * @param fmtPlt    the display format of the signal plots
     *
     * @since     Feb 18, 2010
     * @author    Christopher K. Allen
     */
    public LiveTraceDisplayPanel(FORMAT fmtPlt) {
        super(fmtPlt);
    }
    

    
    /*
     * LiveDisplayBase Abstract Methods
     */
    
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
     */
    @Override
    protected void buildMonitorPool(List<WireScanner> lstDevs) {

        // Else we start the live data monitoring
        for (AcceleratorNode smfDev : lstDevs) {
            if ( !(smfDev instanceof WireScanner) )
                continue;

            try {
                WireScanner             ws        = (WireScanner)smfDev;
                
                for (ANGLE angle : ANGLE.values()) {
                    
                    XalPvDescriptor     pvdPos = angle.getSignalPosFd(WireScanner.Trace.class); 
                    Channel          chnPos = ws.getAndConnectChannel(pvdPos.getRbHandle());
                    double           dblDel = chnPos.getValDbl();
                    
                    XalPvDescriptor    pvdTrc = angle.getSignalValFd(WireScanner.Trace.class);
                    TraceMonitorAction actTrc = new TraceMonitorAction(angle, dblDel, super.getDisplayPlot());

                    super.getMonitorPool().createMonitor(ws, pvdTrc, actTrc);
                }

            } catch (NoSuchChannelException e) {
                getLogger().logException(getClass(), e, "Error in building live data monitoring");

            } catch (ConnectionException e) {
                getLogger().logException(getClass(), e, "Error in building live data monitoring");

            } catch (GetException e) {
                getLogger().logException(getClass(), e, "Error in building live data monitoring");

            }
        }
    }

}

