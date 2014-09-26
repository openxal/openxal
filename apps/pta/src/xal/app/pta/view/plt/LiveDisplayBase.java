/**
 * LiveAcquisitionDisplayPanel.java
 *
 *  Created	: Feb 18, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.view.plt;

import xal.app.pta.MainApplication;
import xal.app.pta.daq.MeasurementData;
import xal.app.pta.rscmgt.AppProperties;
import xal.app.pta.tools.logging.IEventLogger;
import xal.app.pta.view.plt.MultiGraphDisplayPanel.LAYOUT;
import xal.smf.AcceleratorNode;
import xal.smf.impl.profile.ProfileDevice.IProfileData;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * <p>
 * Base class for displays (i.e., GUI display) which plot data 
 * taken during an acquisition scan,
 * taken directly from the diagnostic devices, not the data stored
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
public abstract class LiveDisplayBase extends JPanel  {

    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;


    /*
     * Abstract Methods
     */
    
//    /**
//     * <p>
//     * Builds the pool of monitors for live data display. Child
//     * classes must fill the pool 
//     * <code>{@link LiveDisplayBase#mplLiveData}</code> with monitors 
//     * (of type <code>{@link xal.app.pta.tools.ca.SmfPvMonitor}</code> )
//     * whose actions update the graph 
//     * <code>{@link LiveDisplayBase#pltSignal}</code>.  
//     * </p>
//     * <p>
//     * The base class will manage the monitors within the pool.  The
//     * monitors maintain the graphs of live data.  What data is being
//     * displayed and the details of how it is displayed is the responsibility
//     * of the child classes.  This method is the hook.
//     * </p>
//     *
//     * @param   lstDevs list of devices we are going to monitor
//     * 
//     * @since  Feb 4, 2010
//     * @author Christopher K. Allen
//     * 
//     * @see LiveDisplayBase#getDisplayPlot
//     * @see LiveDisplayBase#getMonitorPool
//     * @see xal.app.pta.tools.ca.SmfPvMonitor
//     */
//    abstract protected void buildMonitorPool(List<WireScanner> lstDevs);
//
    
    
    /*
     * Internal Classes
     */

    /**
     * The display plot format options.
     *
     * @since  Apr 8, 2010
     * @author Christopher K. Allen
     */
    public enum FORMAT {
        
        /**  All data is displayed on a single graph */
        SINGLEGRAPH,

        /**  Data is display on several graphs, aligned horizontally, on for each projection plane */
        MULTIGRAPH_HOR,

        /**  Data is display on several graphs, aligned horizontally, on for each projection plane */
        MULTIGRAPH_VER;

        
        /**
         * Creates the appropriate plot display for the given format
         * constant.
         *
         * @return              a profile data display GUI component with appropriate format
         * 
         * @since  Apr 8, 2010
         * @author Christopher K. Allen
         */
        public GraphDisplayBase createDisplayPanel() {
            switch (this) {
            
            case SINGLEGRAPH:
                return new SingleGraphDisplayPanel();
                
            case MULTIGRAPH_HOR:
                return new MultiGraphDisplayPanel(LAYOUT.HOR);
                
            case MULTIGRAPH_VER:
                return new MultiGraphDisplayPanel(LAYOUT.VER);
                
             // this cannot happen - we need it to make the compiler happy
            default:    
                return null;
            }
        }
    }

    
    
    /*
     * Instance Attributes
     */
    
    
    //
    // Application Components
    //
    
    /** display format of the plot */
    private final FORMAT                fmtPlt;
    
    /** map of devices to their plot legend colors */
    private Map<AcceleratorNode, Color>     mapKeyColor;
    
    
    //
    // GUI Components
    //
    
    /** Use live data button */
    protected JRadioButton              butLiveData;
    
    /** Clear graphs button */
    private JButton                     butClrPlt;
    
    /** The acquisition trace data graph display */
    protected GraphDisplayBase          pltSignal;

    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>LiveScanDisplayPanel</code> object.
     *
     * @param fmtPlt    display format for the signal
     *
     * @since     Feb 18, 2010
     * @author    Christopher K. Allen
     */
    public LiveDisplayBase(FORMAT fmtPlt) {
        this.fmtPlt  = fmtPlt;

        this.mapKeyColor = new HashMap<AcceleratorNode,Color>();

        // Create the GUI
        this.createGuiComponents(); 
        this.layoutGui();         
    }
    
    /**
     * Set the map of wire scanner devices to their graph and legend key color. 
     *
     * @param mapKeyColor   map of (device,color) pairs used on the graph legend
     *
     * @author Christopher K. Allen
     * @since  Aug 23, 2012
     */
    public void setDeviceColorMap(Map<AcceleratorNode,Color> mapKeyColor) {
        this.mapKeyColor = mapKeyColor;
    }
    
    
    /*
     * Operations
     */

    /**
     * Clears the plots of all graphs.
     *
     * 
     * @since  Apr 5, 2010
     * @author Christopher K. Allen
     */
    public void clearGraphs() {
        this.pltSignal.clear();
    }
    
    /**
     * Adds the given action to be invoked whenever the <code>Live</code> data button
     * it toggled
     * 
     * @param actLiveButSel     the action to be invoked upon button toggle
     *
     * @author Christopher K. Allen
     * @since  Apr 30, 2014
     */
    public void addLiveDataSeledListener(ActionListener actLiveButSel) {
        this.butLiveData.addActionListener(actLiveButSel);
    }
    
    /**
     * Set the "Live Data" radio button programmatically.
     *
     * @param bolLive  do live data monitor if <code>true</code>
     *                 otherwise do nothing
     * 
     * @since  Apr 12, 2010
     * @author Christopher K. Allen
     */
    public void setLiveData(boolean bolLive) {
        this.butLiveData.setSelected(bolLive);
    }
    
    /**
     * Toggles the "Live Data" radio button visible or not.
     *
     * @param bolVisible        materializes the button if <code>true</code>
     * 
     * @since  Apr 12, 2010
     * @author Christopher K. Allen
     */
    public void setLiveDataButtonVisible(boolean bolVisible) {
        this.butLiveData.setVisible(bolVisible);
    }
    
    /**
     * Toggles the "Clear Data" radio button visible or not.
     *
     * @param bolVisible    set the button visible if <code>true</code>
     *                      invisible if <code>false</code>
     *
     * @author Christopher K. Allen
     * @since  Nov 14, 2011
     */
    public void setClearDataButton(boolean bolVisible) {
        this.butClrPlt.setVisible(bolVisible);
    }
    
    /**
     * Statically displays the given profile measurement set.
     *
     * @param datMsmt data set to display
     * 
     * @since  Apr 12, 2010
     * @author Christopher K. Allen
     */
    public void displayRawData(MeasurementData datMsmt) {
        this.getDisplayPlot().displayRawData(datMsmt);
    }
    
    /**
     * Statically displays the given profile measurement data
     * with graph colors corresponding to the given color map.
     *
     * @param datMsmt
     * @param mapDevClr
     *
     * @author Christopher K. Allen
     * @since  Aug 24, 2012
     */
    public void displayRawData(MeasurementData datMsmt, Map<String,Color> mapDevClr) {
        
        for (IProfileData datDev : datMsmt.getDataSet()) {
            String  strDevId = datDev.getDeviceId();
            Color   clrDevKey = mapDevClr.get(strDevId);
            
            this.getDisplayPlot().setCurveLabel(strDevId);
            this.getDisplayPlot().setCurvePoints(true);
            this.getDisplayPlot().setCurveColor(clrDevKey);
//            this.getDisplayPlot().setCurveThickness(SZ_CRV_RAW);
            this.getDisplayPlot().displayRawData(datDev);
            }
    }

    
    /*
     * Base Class Support
     */
    
    /**
     * Returns the application event logger. The reference
     * for the logger was taken from the main window.
     *
     * @return the event logger for the main application
     * 
     * @since  Nov 14, 2009
     * @author Christopher K. Allen
     */
    protected IEventLogger getLogger() {
        return MainApplication.getEventLogger();
    }

    /**
     * Returns the device color map used to draw the curves on this graph.
     * 
     * @return  map of device to its curve color on the graph
     *
     * @author Christopher K. Allen
     * @since  Apr 23, 2014
     */
    protected Map<AcceleratorNode, Color>  getDeviceColorMap() {
        return this.mapKeyColor;
    }
    
    /**
     * Returns the plot object used to display the
     * live acquisition data.
     *
     * @return  signal display plot of the GUI
     * 
     * @since  Apr 12, 2010
     * @author Christopher K. Allen
     */
    protected GraphDisplayBase  getDisplayPlot() {
        return this.pltSignal;
    }

    
    /*
     * GUI Support Methods
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
        
        
        // The signal graph
        this.pltSignal = fmtPlt.createDisplayPanel();

        
        // The display live acquisition data button
        this.butLiveData = new JRadioButton("Live");
        
        this.butLiveData.setSelected(true);
        this.butLiveData.setVisible(true);
        this.butLiveData.addActionListener(
                        new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                            }
                        }
        );
        
        // The clear graph button
        ImageIcon icnClear  = AppProperties.ICON.CLEAR.getValue().asIcon();
        this.butClrPlt = new JButton(" Clear ", icnClear);

        this.butClrPlt.addActionListener(
                        new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                LiveDisplayBase.this.clearGraphs();
                            }
                        }
        );
    }
        

    /**
     * Lays out and build the GUI using
     * the initialized components.
     *
     * 
     * @since  Aug 19, 2009
     * @author Christopher K. Allen
     */
    private void layoutGui(){

        // Setup up the layout manager for this pane
        this.setLayout( new GridBagLayout() );

        GridBagConstraints      gbcLayout = new GridBagConstraints();
        gbcLayout.insets = new Insets(0,0,0,0);

        // Live data monitoring button
        gbcLayout.gridx = 0;
        gbcLayout.gridy = 0;
        gbcLayout.weightx = 0.0;
        gbcLayout.weighty = 0.0;
        gbcLayout.fill = GridBagConstraints.NONE;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        gbcLayout.gridwidth = 1;
        this.add(this.butLiveData, gbcLayout);

        // Clear plot button
        gbcLayout.gridx = 1;
        gbcLayout.gridy = 0;
        gbcLayout.weightx = 0.0;
        gbcLayout.weighty = 0.0;
        gbcLayout.fill = GridBagConstraints.NONE;
        gbcLayout.anchor = GridBagConstraints.LINE_END;
        gbcLayout.gridwidth = 1;
        this.add(this.butClrPlt, gbcLayout);

        // Current data display plots 
        gbcLayout.gridx = 0;
        gbcLayout.gridy = 1;
        gbcLayout.gridwidth = 2;
        gbcLayout.weightx = 0.5;
        gbcLayout.weighty = 0.5;
        gbcLayout.fill = GridBagConstraints.BOTH;
        gbcLayout.anchor = GridBagConstraints.CENTER;
        this.add(this.pltSignal, gbcLayout);
    }


}

