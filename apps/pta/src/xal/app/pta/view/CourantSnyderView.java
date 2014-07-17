/**
 * CourantSynderView.java
 *
 * @author Christopher K. Allen
 * @since  Dec 15, 2011
 *
 */

/**
 * CourantSynderView.java
 *
 * @author  Christopher K. Allen
 * @since	Dec 15, 2011
 */
package xal.app.pta.view;

import xal.app.pta.IConfigView;
import xal.app.pta.IDocView;
import xal.app.pta.MainConfiguration;
import xal.app.pta.MainDocument;
import xal.app.pta.daq.MeasurementData;
import xal.app.pta.view.cmn.DeviceSelectorList;
import xal.app.pta.view.cmn.DeviceSelectorPanel;
import xal.app.pta.view.cmn.DeviceSelectorPanel.IDeviceSelectionListener;
import xal.tools.plot.FunctionGraphsJPanel;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Panel for computing and displaying the Courant-Snyder parameters.  These
 * parameters are computed from wire scanner data.  The measurement sets used
 * to compute the Courant-Snyder parameters, and the reconstruction location, 
 * are specified here.
 * 
 * TODO: Finish!
 *
 * @author Christopher K. Allen
 * @since   Dec 15, 2011
 */
public class CourantSynderView extends JPanel implements IDocView, IConfigView, IDeviceSelectionListener, ListSelectionListener {

    /*
     * Global Constants 
     */
    
    /** Serialization version */
    private static final long serialVersionUID = 1L;

    
    
    
    /*
     * Local Attributes
     */
    
    /** The application data document */
    private final MainDocument          docMain;
    
    
    //
    // Application Resources
    
    /** The measurement data set being displayed */
    private MeasurementData             setMsmt;
    
    
    //
    // GUI Components
    
    /** The reconstruction location selector (at which device) */
    private DeviceSelectorPanel         pnlDevLoc;
    
    /** The data selector (i.e., from which device) */
    private DeviceSelectorList          lbxDevDat;

    
    /** The profile signal plots */
    private FunctionGraphsJPanel        pltEnvs;
    
    
    
    

    /*
     * Initialization
     */
    
    /**
     * Creates a new <code>CourantSnyderView</code> panel attached to
     * the given data document.
     * 
     * @param docMain   main application document
     *
     * @author  Christopher K. Allen
     * @since   Dec 16, 2011
     */
    public CourantSynderView(MainDocument docMain) {
        super();
        this.docMain = docMain;
    }
    
    
    
    /*
     * ListSelectionListener Interface
     */
    
    /**
     * @since Dec 15, 2011
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    @Override
    public void valueChanged(ListSelectionEvent arg0) {
        // TODO Auto-generated method stub

    }

    
    /*
     * IConfigView Interface
     */
    /**
     * Reset the new accelerator in the display.
     * 
     * @since Dec 15, 2011
     * @see xal.app.pta.IConfigView#updateAccelerator(xal.app.pta.MainConfiguration)
     */
    @Override
    public void updateAccelerator(MainConfiguration cfgMain) {
        this.clearAll();

        Accelerator smfAccel = cfgMain.getAccelerator();
        
        this.pnlDevLoc.resetAccelerator(smfAccel);
    }

    /**
     * Nothing to do.
     * 
     * @since Jul 12, 2012
     * @see xal.app.pta.IConfigView#updateConfiguration(xal.app.pta.MainConfiguration)
     */
    @Override
    public void updateConfiguration(MainConfiguration cfgMain) {
    }

    
    /*
     * IDocView
     */

    /**
     * Populate the device selection list with the IDs of the devices
     * used in the new measurement set. 
     *
     * @since Dec 15, 2011
     * @see xal.app.pta.IDocView#updateMeasurementData(xal.app.pta.MainDocument)
     */
    @Override
    public void updateMeasurementData(MainDocument docMain) {
        this.clearAll();
        
        this.setMsmt = docMain.getMeasurementData();
        
        if (this.setMsmt != null) 
            this.lbxDevDat.setDeviceList( this.setMsmt.getDeviceIdSet() );
    }

    
    
    /*
     * IDeviceSelectionListener Interface
     */
    
    /**
     * @since Dec 15, 2011
     * @see xal.app.pta.view.cmn.DeviceSelectorPanel.IDeviceSelectionListener#newDeviceSelection(java.util.List)
     */
    @Override
    public void newDeviceSelection(List<AcceleratorNode> lstDevs) {
        // TODO Auto-generated method stub
        
    }

    
    /*
     * Support Methods
     */
    
    /**
     * Creates the individual GUI components.
     * 
     * @since  Dec 13, 2011
     * @author Christopher K. Allen
     */
    @SuppressWarnings({ "unchecked", "unused" })
    private void guiBuildComponents() {
        this.pnlDevLoc = new DeviceSelectorPanel(this.docMain.getAccelerator(), AcceleratorNode.class);
        this.pnlDevLoc.registerDeviceSelectedListener(this);

        this.lbxDevDat = new DeviceSelectorList();
        this.lbxDevDat.registerSelectionListener(this);
        
        this.pltEnvs = new FunctionGraphsJPanel();
        this.pltEnvs.setLegendVisible(true);
        this.pltEnvs.setLegendKeyString("");
        
    }
    
    /**
     * Lay out all the GUI components to make the user interface.
     *
     * @since  Dec 13, 2011
     * @author Christopher K. Allen
     */
    @SuppressWarnings("unused")
    private void guiLayoutComponents() {
        this.setLayout( new GridBagLayout() );
        
        GridBagConstraints       gbcLayout = new GridBagConstraints();

        gbcLayout.insets = new Insets(0,0,5,5);
        
        // The envelope plots
        gbcLayout.gridx = 0;
        gbcLayout.gridy = 0;
        gbcLayout.gridwidth  = 3;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.BOTH;
        gbcLayout.weightx = 0.5;
        gbcLayout.weighty = 0.9;
        gbcLayout.anchor = GridBagConstraints.CENTER;
        this.add( this.pltEnvs, gbcLayout );
        
        // Reconstruction location
        gbcLayout.gridx = 0;
        gbcLayout.gridy = 1;
        gbcLayout.gridwidth  = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.BOTH;
        gbcLayout.weightx = 0.5;
        gbcLayout.weighty = 0.1;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        this.add( this.pnlDevLoc, gbcLayout );

        // Device data selection list
        gbcLayout.gridx = 1;
        gbcLayout.gridy = 1;
        gbcLayout.gridwidth  = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.BOTH;
        gbcLayout.weightx = 0.0;
        gbcLayout.weighty = 0.1;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        this.add( this.lbxDevDat, gbcLayout );
        
    }
    
    /**
     * Initializes the GUI to the current
     * measurement data (if there is any).
     * 
     * @since  Apr 23, 2010
     * @author Christopher K. Allen
     */
    @SuppressWarnings("unused")
    private void guiInitialize() {
        if (this.setMsmt != null)
            this.lbxDevDat.setDeviceList( this.setMsmt.getDeviceIdSet() );
    }
    
    /**
     * Clears out all data in the GUI display.
     * 
     * @since  Apr 27, 2010
     * @author Christopher K. Allen
     */
    private void clearAll() {
        this.lbxDevDat.clear();
        
        this.pltEnvs.removeAllGraphData();
        
    }

}
