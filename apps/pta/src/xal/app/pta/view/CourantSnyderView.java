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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import xal.app.pta.IConfigView;
import xal.app.pta.IDocView;
import xal.app.pta.MainConfiguration;
import xal.app.pta.MainDocument;
import xal.app.pta.daq.MeasurementData;
import xal.app.pta.rscmgt.AppProperties;
import xal.app.pta.rscmgt.PtaResourceManager;
import xal.app.pta.view.analysis.CsFixedPtControlPanel;
import xal.app.pta.view.analysis.Twiss3PlaneDisplayPanel;
import xal.app.pta.view.cmn.DeviceSelectorList;
import xal.app.pta.view.cmn.DeviceSelectorPanel;
import xal.app.pta.view.cmn.DeviceSelectorPanel.IDeviceSelectionListener;
import xal.extension.twissobserver.Measurement;
import xal.extension.twissobserver.MeasurementCurve;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.impl.profile.ProfileDevice.IProfileData;

/**
 * Panel for computing and displaying the Courant-Snyder parameters.  These
 * parameters are computed from wire scanner data.  The measurement sets used
 * to compute the Courant-Snyder parameters, and the reconstruction location, 
 * are specified here.
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 * 
 * @author Christopher K. Allen
 * @since   Dec 15, 2011
 * @version Sep 30, 2014
 */
public class CourantSnyderView extends JPanel implements IDocView, IConfigView, IDeviceSelectionListener, ListSelectionListener {

    
    /*
     * Internal Classes
     */
    
    
    
    
    
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
    
    /** The list of measurements we are using for reconstruction */
    private List<Measurement>           lstMmtsRec;
    
    
    //
    // GUI Components
    
    /** The reconstruction location selector (at which device) */
    private DeviceSelectorPanel         pnlRecLoc;
    
    /** The data selector (i.e., from which device) */
    private DeviceSelectorList          lbxMmtData;
    
    /** The fixed point reconstruction algorithm interface */
    private CsFixedPtControlPanel       pnlFxdPtCltr;

    
    /** The computed Courant-Snyder parameters display */
    private Twiss3PlaneDisplayPanel     pnlTws3d;
    
    /** The profile signal plots */
    private FunctionGraphsJPanel        pltEnvs;
    
    /** The measurement data plots */
    private MeasurementCurve            pltMmts;
    
    
    
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
    public CourantSnyderView(MainDocument docMain) {
        super();
        this.docMain = docMain;
        
        this.lstMmtsRec = new LinkedList<>();
        
        this.guiBuildComponents();
        this.guiBuildActions();
        this.guiLayoutComponents();
        this.guiInitialize();
    }
    
    
    /*
     * IDeviceSelectionListener Interface
     */
    
    /**
     * Want to catchs the events that come from our own device selection listener (from the
     * user).  This is the device defining the reconstruction location.
     * 
     * @since Dec 15, 2011
     * @see xal.app.pta.view.cmn.DeviceSelectorPanel.IDeviceSelectionListener#newDeviceSelection(java.util.List)
     */
    @Override
    public void newDeviceSelection(List<AcceleratorNode> lstDevs) {
        // TODO Auto-generated method stub
        
    }

    
    /*
     * ListSelectionListener Interface
     */
    
    /**
     * The event caught here indicates that the user has selected a set of measurements
     * from which to reconstruction the Courant-Snyder parameters.
     * 
     * @since Dec 15, 2011
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    @Override
    public void valueChanged(ListSelectionEvent arg0) {
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
        
        this.pnlRecLoc.resetAccelerator(smfAccel);
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
     * Populate the measurement selection list with the IDs of the devices
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
            this.lbxMmtData.setDeviceList( this.setMsmt.getDeviceIdSet() );
    }

    
    /*
     * Support Methods
     */
    
    /**
     * From the data chosen by the user and specified on the GUI components,
     * we compute the Courant-Snyder parameters at the reconstruction location
     * (also chosen by the user and indicated on the GUI face).
     *
     * @author Christopher K. Allen
     * @since  Sep 30, 2014
     */
    private void computeCourantSnyder() {
        
    }
    
    private ArrayList<Measurement>  processMsmtData() {
        
        List<String>    lstDevIds = this.lbxMmtData.getSelectedDevices();

//        this.lstMmtsRec.clear();
        
        ArrayList<Measurement>   lstMsmts = new ArrayList<>();
        for (String strDevId : lstDevIds) {
            
            IProfileData    datMsmt = this.setMsmt.getDataForDeviceId(strDevId);
            
            Measurement msmt = new Measurement();
            msmt.strDevId  = strDevId;
            msmt.dblSigHor = datMsmt.getDataAttrs().hor.stdev;
            msmt.dblSigVer = datMsmt.getDataAttrs().ver.stdev;
            msmt.dblSigLng = 0.0;
  
            lstMsmts.add(msmt);
//            this.lstMmtsRec.add(mmt);
        }
        
        return lstMsmts;
    }
    
    /**
     * Creates the individual GUI components.
     * 
     * @since  Dec 13, 2011
     * @author Christopher K. Allen
     */
    private void guiBuildComponents() {
        
        // The reconstruction location GUI
        this.pnlRecLoc = new DeviceSelectorPanel(this.docMain.getAccelerator(), AcceleratorNode.class);
        this.pnlRecLoc.registerDeviceSelectedListener(this);
        this.pnlRecLoc.setSingleSelectionMode(true);
        this.pnlRecLoc.setDeviceTableVisible(false);

        this.lbxMmtData = new DeviceSelectorList();
        this.lbxMmtData.setMultiSelectionMode(true);
        this.lbxMmtData.registerSelectionListener(this);
        
        this.pnlFxdPtCltr = new CsFixedPtControlPanel();
        
        this.pltEnvs = new FunctionGraphsJPanel();
        this.pltEnvs.setLegendVisible(true);
        this.pltEnvs.setLegendKeyString("");
        
        Border  bdrTws3d = new EtchedBorder();
//        Border  bdrTws3d = BorderFactory.createTitledBorder("Computed Courant-Snyder Parameters");
        this.pnlTws3d = new Twiss3PlaneDisplayPanel();
        this.pnlTws3d.setEditable(false);
        this.pnlTws3d.setBorder(bdrTws3d);

    }
    
    /**
     * Define the actions of the interactive GUI components.
     *
     * @author Christopher K. Allen
     * @since  Sep 30, 2014
     */
    private void guiBuildActions() {

        // Respond to the "compute" button in the CsFixedPtControlPanel
        ActionListener  lsnCmp = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                CourantSnyderView.this.computeCourantSnyder();
            }
        };
        
        this.pnlFxdPtCltr.addComputeButtonListener(lsnCmp);
    }
    
    /**
     * Lay out all the GUI components to make the user interface.
     *
     * @since  Dec 13, 2011
     * @author Christopher K. Allen
     */
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
        
        // Courant-Snyder parameters display panel
        String      strUrlIcon = AppProperties.ICON.TWS_ANALYZE.getValue().asString();
        ImageIcon   imgTitle = PtaResourceManager.getImageIcon(strUrlIcon);
        JLabel      lblTwiss = new JLabel("Computed Courant-Snyder Parameters", imgTitle, SwingConstants.LEADING);
        lblTwiss.setAlignmentX(Component.LEFT_ALIGNMENT);

        Box boxTwiss = Box.createVerticalBox();
        boxTwiss.add(lblTwiss);
        boxTwiss.add(Box.createVerticalStrut(10));
        boxTwiss.add(this.pnlTws3d);
        
        gbcLayout.gridx = 3;
        gbcLayout.gridy = 0;
        gbcLayout.gridwidth  = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.NONE;
        gbcLayout.weightx = 0.0;
        gbcLayout.weighty = 0.1;
        gbcLayout.anchor = GridBagConstraints.LINE_END;
        this.add( boxTwiss, gbcLayout );
        
        // Device data selection list
        Box     boxReconData = Box.createVerticalBox();
        JLabel  lblReconData = new JLabel("Reconstruction Data");
        
        boxReconData.add(lblReconData);
        boxReconData.add(Box.createVerticalStrut(10));
        boxReconData.add(this.lbxMmtData);
        
        gbcLayout.gridx = 0;
        gbcLayout.gridy = 1;
        gbcLayout.gridwidth  = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.VERTICAL;
        gbcLayout.weightx = 0.0;
        gbcLayout.weighty = 0.1;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        this.add( boxReconData, gbcLayout );
        
        // Reconstruction location
        Box     boxReconLoc = Box.createVerticalBox();
        JLabel  lblReconLoc = new JLabel("Reconstruction Location");
        
        boxReconLoc.add(lblReconLoc);
        boxReconLoc.add(Box.createVerticalStrut(10));
        boxReconLoc.add(this.pnlRecLoc);
        
        gbcLayout.gridx = 1;
        gbcLayout.gridy = 1;
        gbcLayout.gridwidth  = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.VERTICAL;
        gbcLayout.weightx = 0.0;
        gbcLayout.weighty = 0.1;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        this.add( boxReconLoc, gbcLayout );

        // The Courant-Snyder computation controller
        gbcLayout.gridx = 3;
        gbcLayout.gridy = 1;
        gbcLayout.gridwidth  = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.BOTH;
        gbcLayout.weightx = 0.0;
        gbcLayout.weighty = 0.1;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        this.add( this.pnlFxdPtCltr, gbcLayout );

        
//        // Compute button
//        gbcLayout.gridx = 3;
//        gbcLayout.gridy = 2;
//        gbcLayout.gridwidth  = 1;
//        gbcLayout.gridheight = 1;
//        gbcLayout.fill    = GridBagConstraints.NONE;
//        gbcLayout.weightx = 0.1;
//        gbcLayout.weighty = 0.0;
//        gbcLayout.anchor = GridBagConstraints.CENTER;
//        this.add( this.butRecon, gbcLayout );
//        
    }
    
    /**
     * Initializes the GUI to the current
     * measurement data (if there is any).
     * 
     * @since  Apr 23, 2010
     * @author Christopher K. Allen
     */
    private void guiInitialize() {
        
        if (this.setMsmt != null)
            this.lbxMmtData.setDeviceList( this.setMsmt.getDeviceIdSet() );
    }
    
    /**
     * Clears out all data in the GUI display.
     * 
     * @since  Apr 27, 2010
     * @author Christopher K. Allen
     */
    private void clearAll() {
        this.lbxMmtData.clear();
        this.lstMmtsRec.clear();
        this.pltEnvs.removeAllGraphData();
        
    }

}
