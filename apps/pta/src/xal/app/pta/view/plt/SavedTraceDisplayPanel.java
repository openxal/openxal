/**
 * SavedTraceDisplayPanel.java
 *
 * @author Christopher K. Allen
 * @since  Mar 30, 2011
 *
 */

/**
 * SavedTraceDisplayPanel.java
 *
 * @author  Christopher K. Allen
 * @since	Mar 30, 2011
 */
package xal.app.pta.view.plt;

import xal.app.pta.MainApplication;
import xal.app.pta.rscmgt.AppProperties;
import xal.app.pta.rscmgt.DeviceProperties;
import xal.app.pta.tools.ca.CaResponseHandler;
import xal.app.pta.tools.ca.SmfPvMonitor;
import xal.app.pta.tools.ca.SmfPvMonitorPool;
import xal.app.pta.tools.logging.IEventLogger;
import xal.app.pta.view.plt.MultiGraphDisplayPanel.LAYOUT;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.MonitorException;
import xal.ca.PutException;
import xal.tools.math.Interval;
import xal.tools.math.MathException;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.smf.NoSuchChannelException;
import xal.smf.impl.WireScanner;
import xal.smf.impl.WireScanner.CMD;
import xal.smf.impl.WireScanner.CMDARG;
import xal.smf.impl.WireScanner.CmdPck;
import xal.smf.scada.XalPvDescriptor;
import xal.smf.scada.ScadaFieldDescriptor;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

/**
 * Displays the wire scanner traces currently stored on the requested device.
 * The scan index may be selected. The data processing window is also displayed
 * and the user may move the window to different locations.  Current this class
 * is set up to restrict the movement to the limits specified by the current
 * device parameters.  The developer is able to receive notifications of the
 * user processing window manipulations.  This class only modifies 
 * the processing parameters through the method
 * <code>{@link #configureHardwareFromGraph(xal.smf.impl.WireScanner.ANGLE)}</code>.  So
 * developers wishing to set the processing parameters from the window displayed
 * on the graph must call this method, for example, from an event response
 * object.  Other than that, this class only reads processing window parameters
 * in order to display the window. 
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author Christopher K. Allen
 * @since   Mar 30, 2011
 */
public class SavedTraceDisplayPanel extends JPanel {

    
    
    /*
     * Inner Classes
     */
    
    
    /**
     * Response class for the <i>dragged vertical line</i> event
     * thrown by the <code>{@link FunctionGraphsJPanel}</code> internal
     * component.
     *
     * @author Christopher K. Allen
     * @since   Mar 31, 2011
     */
    private class DraggedVerLineAction implements ActionListener {

        
        /*
         * Local Attributes
         */
        
        /** The graph firing the event */
        private WireScanner.ANGLE      angGraph;
        
        
        /**
         * Creates a new <code>DraggedVerLineAction<code> response
         * object for the given graph.
         * 
         * @param angGraph  graph firing the event this responds to
         *
         * @author  Christopher K. Allen
         * @since   Mar 31, 2011
         */
        public DraggedVerLineAction(WireScanner.ANGLE angGraph) {
            this.angGraph = angGraph;
        }
        
        /**
         * Calls the event handler attached to the main class.
         * 
         * @since Mar 31, 2011
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         * 
         * @see SavedTraceDisplayPanel#draggedLineHandler(xal.smf.impl.WireScanner.ANGLE) 
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            SavedTraceDisplayPanel.this.draggedLineHandler(this.angGraph);
        }
        
    }
    
    
    
    
    /*
     * Global Constants
     */

    /** Serialization version identifier */
    private static final long serialVersionUID = 1L;

    
    
    /** Line color used for the trace curve */
    private static final Color          CLR_LN_TRACE = AppProperties.PLT.CLR_TRC_CRV.getValue().asColor();
    
    /** Line color used to draw the processing window */
    private static final Color          CLR_LN_PRC_WND = AppProperties.PLT.CLR_TRC_WND.getValue().asColor();
    
    /** Index of the vertical line showing the beginning of the processing window */
    private static final int            IND_BEG_PRC_WND = 0;
    
    /** Index of the vertical line showing the end of the processing window */
    private static final int            IND_END_PRC_WND = 1;
    
    
    /** 
     * The scaling factor used to convert between trace abscissa units and 
     * the processing window location units.  Scale from seconds to micro-seconds. 
     * */
    private static final double         DBL_SCALE_PRCG_WND = 1.0e6;  

        
    /*
     * Local Attributes
     */
    
    //
    // Hardware Components
    /** The current hardware device whose traces we are using */
    private WireScanner                 smfDev;
    
    
    //
    // Event Response
    /** Hardware (proc. window) parameters modification flag */
    private boolean                     bolModHware;
    
    /** Set of monitors for the processing window PVs */
    private final SmfPvMonitorPool      mplPrcgParms;
    
    
    /** The list of processing window parameter change event response objects */
    private final List<ActionListener>  lstParmChgAct;
    
    /** The lists of event response handlers for line dragging, organized by graph */
    private final Map<WireScanner.ANGLE, List<ActionListener>>   mapEvtHdlr;
    
    
    //
    // GUI Components
    
    /** Plot of the trace at the current actuator position */
    private MultiGraphDisplayPanel      pltTrace;

    
    /** The maximum index of the set of traces */
    private int                         indTrcMax;
    
    /** Interval of allowed values for the processing window start (scaled to micro-seconds) */
    private Interval                    ivlAvgBgn;
    
    /** Interval of allowed values for the processing window length (scaled to micro-seconds) */
    private Interval                    ivlAvgLng;
    
 
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>ProcessWindowSelectorPanel</code> object and
     * use the given profile device for trace display.
     *
     * @param smfDev    device from which the traces are taken
     *
     * @since     May 21, 2010
     * @author    Christopher K. Allen
     */
    public SavedTraceDisplayPanel(WireScanner smfDev)   {
        
        this.lstParmChgAct = new LinkedList<ActionListener>();
        this.mapEvtHdlr    = new HashMap<WireScanner.ANGLE, List<ActionListener>>();
        for (WireScanner.ANGLE angGraph : WireScanner.ANGLE.values()) 
            this.mapEvtHdlr.put(angGraph, new LinkedList<ActionListener>());

        this.bolModHware  = false;
        this.mplPrcgParms = new SmfPvMonitorPool();

        this.buildGuiComponents();
        this.layoutGuiComponents();
        this.setDevice(smfDev);
    }
    
    /**
     * Create a new <code>TraceSelectionPanel</code> object.
     *
     *
     * @since     May 21, 2010
     * @author    Christopher K. Allen
     */
    public SavedTraceDisplayPanel() {
        this(null);
    }
    
    /**
     * Sets the ability to modify the processing window
     * parameters on the device hardware according to the user
     * actions.
     * 
     * @param bolModHware   <code>true</code allows the hardware parameters to be modified,
     *                      <code>false</code> and user actions do not affect hardware.
     *
     * @author Christopher K. Allen
     * @since  Mar 31, 2011
     */
    public void setModifyHardwareParameters(boolean bolModHware) {
        this.bolModHware = bolModHware;
    }
    
    /**
     * Add and event response object that will receive notifications
     * when this class modifies the window processing parameters.
     *
     * @param lsnParmChg    event response object
     *
     * @author Christopher K. Allen
     * @since  Apr 1, 2011
     */
    public void addParameterChangeListener(ActionListener lsnParmChg) {
        this.lstParmChgAct.add(lsnParmChg);
    }
    
    /**
     * Removes the given event response object from the list of objects
     * receiving notifications when this class modifies the window 
     * processing parameters.
     *
     * @param lsnParmChg    event response object to be removed
     *
     * @author Christopher K. Allen
     * @since  Apr 1, 2011
     */
    public void removeParameterChangeListener(ActionListener lsnParmChg) {
        this.lstParmChgAct.remove(lsnParmChg);
    }
    
    /**
     * <p>
     * Adds the given event response object to the list of objects receiving
     * notifications when a user drags a vertical line across the plot screen.
     * The listener object responds only to the graph indicated by the first
     * argument.
     * </p>
     * <p>
     * Note that current the underlying plotting object, 
     * of type <code>{@link FunctionGraphsJPanel}<code>, only supports a single
     * event handler for this dragging operation.  So repeated calls to this
     * method will resplace the previous event handler with the new one, rather
     * than have multiple event handlers.
     * </p>
     *
     * @param angGraph      the graph to which the event handler is attached
     * @param lsnDragAct    the event handler object
     *
     * @author Christopher K. Allen
     * @since  Mar 30, 2011
     */
    public void addDraggedVerLineListener(WireScanner.ANGLE angGraph, ActionListener lsnDragAct) {
        
        this.mapEvtHdlr.get(angGraph).add(lsnDragAct);
        this.pltTrace.setVerticalLineDragging(angGraph, true);
    }
    
    
    /**
     * Sets the processing window displayed on the given trace plot.  The 
     * minimum and maximum value provided are checked against the parameter
     * restriction of the current device.  If the given values fall outside
     * these limit, the given values are modified to so maximum values are
     * displayed.
     *
     * @param angGraph  graph whose processing window is modified
     * @param dblMin    the minimum time value of the processing window (in micro-seconds)
     * @param dblMax    the maximum time value of the processing window (in micro-seconds)
     *
     * @author Christopher K. Allen
     * @since  Mar 30, 2011
     */
    public void     setProcessingWindowDisplay(WireScanner.ANGLE angGraph, double dblMin, double dblMax) {
        
        this.pltTrace.setVerticalLineDragging(false);

        // If the lines are mixed up we'll just bail out
        if (dblMin > dblMax)
            return;
        
        double  dblTimMin = dblMin;
        double  dblTimMax = dblMax;
        
        // Get the window start value and check against limits
        if (dblTimMin > this.ivlAvgBgn.getMax()) {
            
            dblTimMin = this.ivlAvgBgn.getMax();
            
        } else if (dblTimMin < this.ivlAvgBgn.getMin()) {
            
            dblTimMin = this.ivlAvgBgn.getMin();
        }
            
        this.pltTrace.setVerticalLine(angGraph, IND_BEG_PRC_WND, dblTimMin);
        
            
        // Get the window duration value and check against limits
        double  dblDurLng = dblTimMax - dblTimMin;
        
        if (dblDurLng > this.ivlAvgLng.getMax()) {
            
            dblDurLng = this.ivlAvgLng.getMax();
            
        } else if (dblDurLng < this.ivlAvgLng.getMin()) {
            
            dblDurLng = this.ivlAvgLng.getMin();
        }
        dblTimMax = dblTimMin + dblDurLng;

        this.pltTrace.setVerticalLine(angGraph, IND_END_PRC_WND, dblTimMax);
        this.pltTrace.setVerticalLineDragging(true);
        this.pltTrace.refreshPlots();
    }

    /**
     * Sets the processing window displayed on all the traces plots.  The 
     * minimum and maximum value provided are checked against the parameter
     * restriction of the current device.  If the given values fall outside
     * these limit, the given values are modified to so maximum values are
     * displayed.
     *
     * @param dblMin    the minimum time value of the processing window
     * @param dblMax    the maximum time value of the processing window
     *
     * @author Christopher K. Allen
     * @since  Mar 30, 2011
     */
    public void     setProcessingWindowDisplay(double dblMin, double dblMax) {
        
        this.pltTrace.setVerticalLineDragging(false);

        double  dblTimMax = dblMax;
        double  dblTimMin = dblMin;
        
        // Get the window start value and check against limits
        if (dblTimMin > this.ivlAvgBgn.getMax()) {
            
            dblTimMin = this.ivlAvgBgn.getMax();
            
        } else if (dblTimMin < this.ivlAvgBgn.getMin()) {
            
            dblTimMin = this.ivlAvgBgn.getMin();
        }
            
        this.pltTrace.setVerticalLine(IND_BEG_PRC_WND, dblTimMin);
        
            
        // Get the window duration value and check against limits
        double  dblDurLng = dblTimMax - dblTimMin;
        
        if (dblDurLng > this.ivlAvgLng.getMax()) {
            
            dblDurLng = this.ivlAvgLng.getMax();
            
        } else if (dblDurLng < this.ivlAvgLng.getMin()) {
            
            dblDurLng = this.ivlAvgLng.getMin();
        }
        dblTimMax = dblTimMin + dblDurLng;

        this.pltTrace.setVerticalLine(IND_END_PRC_WND, dblTimMax);
        this.pltTrace.setVerticalLineDragging(true);
        this.pltTrace.refreshPlots();
    }
    

    /*
     * Attributes
     */
    
    /**
     * Returns the processing window starting time for the given graph.
     * Of course, all processing windows will be the same for a given
     * device, however, the display could be arranged differently.
     *
     * @param angGraph  the processing window parameters are taken from this graph
     * 
     * @return          the starting time (left-most position) of the processing window
     *
     * @author Christopher K. Allen
     * @since  Mar 30, 2011
     */
    public double   getProcessWindStartTime(WireScanner.ANGLE angGraph) {
        return this.pltTrace.getVerticalLinePosition(angGraph, IND_BEG_PRC_WND);
    }
    
    /**
     * Returns the processing window ending time for the given graph.
     * Of course, all processing windows will be the same for a given
     * device, however, the display could be arranged differently.
     *
     * @param angGraph  the processing window parameters are taken from this graph
     * 
     * @return          the ending time (right-most position) of the processing window
     *
     * @author Christopher K. Allen
     * @since  Mar 30, 2011
     */
    public double   getProcessWindEndTime(WireScanner.ANGLE angGraph) {
        return this.pltTrace.getVerticalLinePosition(angGraph, IND_END_PRC_WND);
    }
    
    
    /*
     * Operations
     */
    
    /**
     * Clears the GUI display of device information
     * and releases the current device. 
     *
     * 
     * @since  May 27, 2010
     * @author Christopher K. Allen
     */
    public void clearDevice() {
        this.smfDev = null;
        
        this.pltTrace.clear();
    }
    
    /**
     * Refreshes the display by clearing the plots, acquiring the current
     * data from the current hardware device and plotting everything 
     * (including the processing window). 
     *
     * @author Christopher K. Allen
     * @since  Mar 30, 2011
     */
    public void refresh() {

        try {
            
            // Read the trace buffer and processing parameters
            WireScanner.Trace      trcNew  = WireScanner.Trace.acquire(this.smfDev);
            
            // Display the trace waveform
            this.pltTrace.clear();
            this.pltTrace.displayProfile(trcNew);
            
            
            // Draw the processing window
            this.updateProcessingWindowHandler();
            
        } catch (ConnectionException e) {
            getLogger().logException(this.getClass(), e, "Unable to connect to " + smfDev.getId());
            
        } catch (GetException e) {
            getLogger().logException(this.getClass(), e, "Unable to acquire trace from " + smfDev.getId());
            
        } catch (NoSuchChannelException e) {
            getLogger().logException(this.getClass(), e, "Bad trace channel for " + smfDev.getId());
            
        }
        
    }
    
    
    /**
     * Set the source of the display traces to the given
     * hardware device.
     *
     * @param ws        source of the traces displayed
     * 
     * @since  May 21, 2010
     * @author Christopher K. Allen
     */
    public synchronized void setDevice(WireScanner ws) {

        // Check for valid device
        if (ws == null) {
            return;
        }

        this.smfDev = ws;

        try {
            
            // Get the scan configuration parameters in order 
            //  to set trace index bounds
            WireScanner.ScanConfig cfgScan = WireScanner.ScanConfig.acquire(ws);

            this.indTrcMax = cfgScan.stepCount;

            
            // Just pick the trace at the middle step index
            int     indInit = this.indTrcMax/2;
            
            this.setTraceIndex(indInit);

            // Draw the plots
            this.refresh();
            
            // Start monitoring the values of the processing window parameters
            this.buildMonitorPool(ws);
            this.mplPrcgParms.begin(true);
            
            
        } catch (ConnectionException e) {
            getLogger().logException(getClass(), e, "Unable to connect with " + ws.getId());
            
        } catch (GetException e) {
            getLogger().logException(getClass(), e, "Unable to read from " + ws.getId());
            
        } catch (NoSuchChannelException e) {
            getLogger().logException(getClass(), e, "Missing channel for trace processing PV monitors in " + ws.getId());
            
        } catch (MonitorException e) {
            getLogger().logException(getClass(), e, "Unable to start trace processing PV monitors for " + ws.getId());

        }
    }
    
    /**
     * Respond to the user event of specifying a new
     * trace index.  A request is sent to the wire scanner
     * requesting that the trace buffer be updated with the
     * data for the given step number.
     * 
     * @param indTrc    index (i.e., the step number) of the desired trace
     * 
     * @since  May 25, 2010
     * @author Christopher K. Allen
     */
    public synchronized void setTraceIndex(int indTrc) {
        
        // Build the "load trace buffer" command for the requested step
        int cntRpt  = 0;
        WireScanner.CmdPck cmdPck = new CmdPck(CMD.UPDATE, CMDARG.TRACE, indTrc, cntRpt);
        
        try {
            // Load the requested trace into the trace buffer 
            this.smfDev.runCommand(cmdPck);
            this.smfDev.runCommand(WireScanner.CMD.RESET);
            this.smfDev.runCommand(CMD.REANALYZE);

            this.refresh();

            
        } catch (ConnectionException e) {
            getLogger().logException(this.getClass(), e, "Unable to connect to " + smfDev.getId());
            
        } catch (PutException e) {
            getLogger().logException(this.getClass(), e, "Unable to send command to " + smfDev.getId());
            
        } catch (IllegalArgumentException e) {
            getLogger().logException(this.getClass(), e, "Bad device command " + 
                            cmdPck.toString() + " for " + smfDev.getId()
                            );
            
        } catch (InterruptedException e) {
            getLogger().logException(this.getClass(), e, "Command thread interrupted for " + smfDev.getId());
            
        } catch (NoSuchChannelException e) {
            getLogger().logException(this.getClass(), e, "Bad trace channel for " + smfDev.getId());
            
        }
    }
    
    /**
     * <p>
     * This method is called from action listeners attached to the trace display
     * plots.  Whenever the user moves a vertical line marking an endpoint of the
     * processing window, the listener invokes this method passing the identity
     * of the graph being modified.
     * </p>
     * <p>
     * Once invoked, we get the current values of the processing window markers
     * from the modified graph.  The processing parameters are retrieved from the
     * current hardware device and the parameters defining the processing window
     * are modified and send back to the device.
     * </p>
     *
     * @param angGraph  the graph that had its window parameters adjusted
     *
     * @author Christopher K. Allen
     * @since  Mar 29, 2011
     */
    public synchronized void configureHardwareFromGraph(WireScanner.ANGLE angGraph) {
        
        this.pltTrace.setVerticalLineDragging(false);

        // Get the window start value and check against limits
        double  dblBeg = pltTrace.getVerticalLinePosition(angGraph, IND_BEG_PRC_WND);
        
        if (dblBeg > this.ivlAvgBgn.getMax()) {
            dblBeg = this.ivlAvgBgn.getMax();

            this.pltTrace.setVerticalLine(IND_BEG_PRC_WND, dblBeg);
        }
            
        // Get the window duration value and check against limits
        double  dblEnd = pltTrace.getVerticalLinePosition(angGraph, IND_END_PRC_WND);
        double  dblLng = dblEnd - dblBeg;
        
        if (dblLng > this.ivlAvgLng.getMax()) {
            dblLng = this.ivlAvgLng.getMax();
            dblEnd = dblBeg + dblLng;
            
            this.pltTrace.setVerticalLine(IND_END_PRC_WND, dblEnd);
        }
        
        
        try {
            WireScanner.PrcgConfig  cfgPrcg = WireScanner.PrcgConfig.acquire(smfDev);
            
            cfgPrcg.avgBgn = dblBeg / DBL_SCALE_PRCG_WND;
            cfgPrcg.avgLng = dblLng / DBL_SCALE_PRCG_WND;
            
            smfDev.configureHardware(cfgPrcg);
//            smfDev.runCommand(CMD.RESET);
//            smfDev.runCommand(CMD.UPDATE);
            
            // Notify all the action listeners of the change in parameter values
            ActionEvent         evt = new ActionEvent(this, 0, "SavedTraceDisplayPanel#configureHardwareFromGraph");
            for (ActionListener act : this.lstParmChgAct) 
                act.actionPerformed(evt);
            
        } catch (ConnectionException e) {
            getLogger().logException(getClass(), e, "Unable to connect with " + smfDev.getId());
            
        } catch (GetException e) {
            getLogger().logException(getClass(), e, "Unable to acquire from " + smfDev.getId());
            
        } catch (PutException e) {
            getLogger().logException(getClass(), e, "Unable to send to " + smfDev.getId());
            
//        } catch (InterruptedException e) {
//            getLogger().logException(getClass(), e, "Command buffer thread interrupted for " + smfDev.getId());
//            
        }
        
        this.pltTrace.setVerticalLineDragging(true);
    }

    
    
    /*
     * Event Handlers
     */
    
    
    /**
     * This method does the actual response to a dragged vertical
     * line event.  The event handler calls this method from a
     * spawned thread in order to release EPICS Channel Access.
     *
     * @param angGraph  the graph generating the event
     *
     * @author Christopher K. Allen
     * @since  Mar 30, 2011
     */
    public synchronized void draggedLineHandler(WireScanner.ANGLE angGraph) {
        
        // Get the window start value and check against limits
        double  dblBeg = pltTrace.getVerticalLinePosition(angGraph, IND_BEG_PRC_WND);
        double  dblEnd = pltTrace.getVerticalLinePosition(angGraph, IND_END_PRC_WND);

        this.setProcessingWindowDisplay(dblBeg, dblEnd);
        

        if (this.bolModHware)
            this.configureHardwareFromGraph(angGraph);
        
        // Notify all event listeners
        ActionEvent         evt = new ActionEvent(this, angGraph.getIndex(), "SavedTraceDisplayPanel#dragLineHandler");
        for (ActionListener lsnDrag : this.mapEvtHdlr.get(angGraph)) 
            lsnDrag.actionPerformed(evt);
    }
    
    /**
     * <p>
     * Handler invoked (by proxy) which retrieves the current trace 
     * processing parameters
     * then moves the processing window displayed on the
     * trace plots to correspond with those values.
     * </p>
     * <p>
     * Responds to the user-selection of new processing window
     * parameters.  This action is 
     * called by threads spawned by the PV monitors in the monitor pool.
     * </p>
     *
     * 
     * @since  Jun 3, 2010
     * @author Christopher K. Allen
     */
    public synchronized void updateProcessingWindowHandler() {
        
        // Check for selected device
        if (this.smfDev == null)
            return;
        
        // Get the signal processing parameters and profile data
        WireScanner.PrcgConfig  cfgPrcg;
        try {
            // Get the new trace process parameters and display processing window
            cfgPrcg = WireScanner.PrcgConfig.acquire(this.smfDev);
            
            double      dblPosBgn = DBL_SCALE_PRCG_WND * cfgPrcg.avgBgn;
            double      dblPosEnd = DBL_SCALE_PRCG_WND * (cfgPrcg.avgBgn + cfgPrcg.avgLng);
            
            
            // Set the processing window on the trace screen
            this.pltTrace.setVerticalLineDragging(false);
            
            for (WireScanner.ANGLE angle : WireScanner.ANGLE.values()) {
                
                // Set the processing window start and end markers
                int cntVerLns = this.pltTrace.getVerticalLineCount(angle);
                
                if (cntVerLns == 0) {
                    this.pltTrace.addVerticalLine(angle, dblPosBgn, CLR_LN_PRC_WND);
                    this.pltTrace.addVerticalLine(angle, dblPosEnd, CLR_LN_PRC_WND);
                    
                } else {    // The markers already exist, just reposition
                    
                    this.pltTrace.setVerticalLine(angle, IND_BEG_PRC_WND, dblPosBgn);
                    this.pltTrace.setVerticalLine(angle, IND_END_PRC_WND, dblPosEnd);
                }
            }

            this.pltTrace.setVerticalLineDragging(true);
            this.pltTrace.refreshPlots();
            
        } catch (ConnectionException e) {
            getLogger().logException(this.getClass(), e, "Unable to connect to " + smfDev.getId());
            
        } catch (GetException e) {
            getLogger().logException(this.getClass(), e, "Unable to acquire parameters from " + smfDev.getId());

        }
    }

    
    
    /*
     * Support Methods
     */
    
    
    /**
     * Instantiate the visible components on this
     * panel.
     * 
     * @since  May 21, 2010
     * @author Christopher K. Allen
     */
    private void buildGuiComponents() {
        
        // Setup the processing window length value domain
        ScadaFieldDescriptor fdAvgLng = WireScanner.PrcgConfig.FLD_MAP.get("avgLng");
        
        double  dblMin = DBL_SCALE_PRCG_WND * DeviceProperties.getMinLimit(fdAvgLng).asDouble();
        double  dblMax = DBL_SCALE_PRCG_WND * DeviceProperties.getMaxLimit(fdAvgLng).asDouble();
        double  dblIni = DBL_SCALE_PRCG_WND * DeviceProperties.getInitialValue(fdAvgLng).asDouble();
        try {
            
			this.ivlAvgLng = new Interval(dblMin, dblMax);
			
		} catch (IllegalArgumentException e) {
		    
			e.printStackTrace();
			MainApplication.getEventLogger().logError(this.getClass(), "Unable to create trace averaging interval");
		}

        // Setup the processing window starting value domain
        ScadaFieldDescriptor fdAvgBgn = WireScanner.PrcgConfig.FLD_MAP.get("avgBgn");
        
        dblMin = DBL_SCALE_PRCG_WND * DeviceProperties.getMinLimit(fdAvgBgn).asDouble();
        dblMax = DBL_SCALE_PRCG_WND * DeviceProperties.getMaxLimit(fdAvgBgn).asDouble();
        try {
            
			this.ivlAvgBgn = new Interval(dblMin, dblMax);
			
		} catch (IllegalArgumentException e) {

		    e.printStackTrace();
            MainApplication.getEventLogger().logError(this.getClass(), "Unable to create trace averaging interval");
            
		}

        
        // Set up the trace plots 
        //  Build the plots and set the properties and event handlers
        this.pltTrace = new MultiGraphDisplayPanel(LAYOUT.HOR);
        this.pltTrace.setCurveColor(CLR_LN_TRACE);
        this.pltTrace.setVerticalLineDragging(false);

        //  Setup the processing window
        this.pltTrace.addVerticalLine(this.ivlAvgBgn.getMin(), Color.RED);
        this.pltTrace.addVerticalLine(this.ivlAvgBgn.getMin()+dblIni, Color.RED);

        //  Setup the processing window modification event responses       
        for (WireScanner.ANGLE angGraph : WireScanner.ANGLE.values()) {
            DraggedVerLineAction    actDrgLn = new DraggedVerLineAction(angGraph);
            
            this.pltTrace.addDraggedVerLinesListener(angGraph, actDrgLn);
        }
    }
    
    /**
     *  Arrange the GUI components on this panel.
     * 
     * @since  May 21, 2010
     * @author Christopher K. Allen
     */
    private void layoutGuiComponents() {
        this.setLayout( new GridBagLayout() );

        GridBagConstraints      gbcLayout = new GridBagConstraints();
//        gbcLayout.insets  = new Insets(0, 5, 0, 5);


        gbcLayout.gridx = 0;
        gbcLayout.gridy = 0;
        gbcLayout.gridwidth = 1;
        gbcLayout.gridheight = 1;
//        gbcLayout.insets.right = 5;
        gbcLayout.weightx = 1;
        gbcLayout.weighty = 1;
        gbcLayout.anchor = GridBagConstraints.CENTER;
        gbcLayout.fill  = GridBagConstraints.BOTH;
        this.add( this.pltTrace, gbcLayout);
        
//        this.add( this.pltTrace );
    }
    
    /**
     * Creates the PV monitors for the processing window
     * parameters, then adds them to the 
     *
     * @param ws        hardware that we are monitoring
     * 
     * @since  Jun 3, 2010
     * @author Christopher K. Allen
     */
    private void buildMonitorPool(WireScanner ws) {
        
        // Terminate a previously started pool 
        this.mplPrcgParms.emptyPool();
        
        try {
            // Monitor changes in the processing window start time PV
            XalPvDescriptor      pvdAvgBgn = WireScanner.PrcgConfig.FLD_MAP.get("avgBgn");
            CaResponseHandler    actAvgBgn = new CaResponseHandler(this, "updateProcessingWindowHandler");
            SmfPvMonitor         monAvgBgn = new SmfPvMonitor(ws, pvdAvgBgn);
            actAvgBgn.setLogger(getLogger());
            monAvgBgn.addAction(actAvgBgn);
            this.mplPrcgParms.addMonitor(monAvgBgn);

            // Monitor changes in the processing window duration PV
            XalPvDescriptor      pvdAvgLng = WireScanner.PrcgConfig.FLD_MAP.get("avgLng");
            CaResponseHandler    actAvgLng = new CaResponseHandler(this, "updateProcessingWindowHandler");
            SmfPvMonitor         monAvgLng = new SmfPvMonitor(ws, pvdAvgLng);
            actAvgLng.setLogger(getLogger());
            monAvgLng.addAction(actAvgLng);
            this.mplPrcgParms.addMonitor(monAvgLng);

        } catch (SecurityException e){
            getLogger().logError(getClass(), "Unable to create ProcessingPvUpdateAction, method access error");
            
        }
    }


    /**
     * Returns the singleton event logger used by the
     * main application. 
     *
     * @return      application event logger
     * 
     * @since  May 25, 2010
     * @author Christopher K. Allen
     */
    private IEventLogger getLogger() {
        return MainApplication.getEventLogger();
    }

}
