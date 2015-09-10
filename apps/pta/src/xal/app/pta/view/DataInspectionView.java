/**
 * DataInspectionView.java
 *
 *  Created	: Apr 22, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.view;

import xal.app.pta.IConfigView;
import xal.app.pta.IDocView;
import xal.app.pta.MainApplication;
import xal.app.pta.MainConfiguration;
import xal.app.pta.MainDocument;
import xal.app.pta.daq.ScannerData;
import xal.app.pta.daq.MeasurementData;
import xal.app.pta.tools.logging.IEventLogger;
import xal.app.pta.view.analysis.ProfileAttrsDisplayPanel;
import xal.app.pta.view.cmn.DeviceSelectorList;
import xal.app.pta.view.plt.MultiGraphDisplayPanel;
import xal.smf.impl.profile.ProfileDevice.IProfileData;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Displays the measurement data taken from the profile devices. 
 * Includes graphs of the data and the fit along with the 
 * statistics computed by the device control software.
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Apr 22, 2010
 * @author Christopher K. Allen
 */
public class DataInspectionView extends JPanel implements IDocView, IConfigView, ListSelectionListener {

    
    /*
     * Internal Classes
     */
    
    
    /**  Serialization Version */
    private static final long serialVersionUID = 1L;

    
    /** Color of the Raw data curve */
    private static final Color  CLR_CRV_RAW = Color.BLACK;
    
    /** Size of the raw data curve */
    private static final int    SZ_CRV_RAW = 1;
    

    /** Color of the fitted data curve */
    private static final Color  CLR_CRV_FIT = Color.RED;
    
    /** Size of the fitted data curve */
    private static final int    SZ_CRV_FIT = 2;
    
    
    
    /*
     * Local Attributes
     */

    
    //
    // Application Resources
    
    /** The measurement data set being displayed */
    private MeasurementData              setMsmt;
    
    
    //
    // GUI Components
    
    /** The data selector (i.e., from which device) */
    private DeviceSelectorList          lbxDevSel;
    
    
    /** Label for the signal parameters */
    private JLabel                      lblSigPrps;
    
    
    /** The profile signal plots */
    private MultiGraphDisplayPanel      pltSignals;
    
    /** The profile signal properties for a Gaussian fit */
    private ProfileAttrsDisplayPanel    pnlPrpsGauss;
    
    /** The profile signal properties for a double Gaussian fit */
    private ProfileAttrsDisplayPanel    pnlPrpsDblGauss;
    
    /** The profile signal properties for a direct statistical calculation */
    private ProfileAttrsDisplayPanel    pnlPrpsStat;
    

    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>DataInspectionView</code> object.
     *
     * @param docMain   the application measurement data
     *
     * @since     Apr 23, 2010
     * @author    Christopher K. Allen
     */
    public DataInspectionView(MainDocument docMain) {
//        this.winMain = winMain;
        this.setMsmt = docMain.getMeasurementData();
        
        this.buildGuiComponents();
        this.layoutGuiComponents();
        this.initGui();
    }
    
    /*
     * Operations
     */
    
    /**
     * Convenience method returning the main application's 
     * event logger.
     *
     * @return  event logger used by application
     * 
     * @since  Apr 23, 2010
     * @author Christopher K. Allen
     */
    public IEventLogger getLogger() {
        return MainApplication.getEventLogger();
    }
    
    
    
    /* 
     * IDocView Interface
     */
    
    /**
     * Populate the device selection list with the IDs of the devices
     * used in the new measurement set. 
     *
     * @since 	Apr 22, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.IDocView#updateMeasurementData(xal.app.pta.MainDocument)
     */
    @Override
    public void updateMeasurementData(MainDocument docMain) {
        this.clearAll();
        
        this.setMsmt = docMain.getMeasurementData();
        
        if (this.setMsmt != null) 
            this.lbxDevSel.setDeviceList( this.setMsmt.getDeviceIdSet() );
    }
    
    /* 
     * IConfigView Interface
     */

    /**
     * We simply clear the GUI here.
     * 
     * @since   Apr 22, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.IConfigView#updateAccelerator(xal.app.pta.MainConfiguration)
     */
    @Override
    public void updateAccelerator(MainConfiguration cfgMain) {
        this.clearAll();
    }


    
    /*
     * ListSelectionListener Interface
     */

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
     * ListSelectionListener Interface
     */
    
    /**
     * Display the graph and signal properties of the
     * newly selected device.
     *
     * @since   Apr 23, 2010
     * @author  Christopher K. Allen
     *
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        String          strDevId = this.lbxDevSel.getSelectedDevice();
        IProfileData datDev   = this.setMsmt.getDataForDeviceId(strDevId); 
        
        this.pltSignals.clear();
        
        if (datDev == null)
            return;
        
        this.pltSignals.setCurveLabel("raw");
        this.pltSignals.setCurveColor(CLR_CRV_RAW);
        this.pltSignals.setCurvePoints(true);
        this.pltSignals.setCurveThickness(SZ_CRV_RAW);
        this.pltSignals.displayRawData(datDev);
        
        this.pltSignals.setCurveLabel("fit");
        this.pltSignals.setCurveColor(CLR_CRV_FIT);
        this.pltSignals.setCurvePoints(false);
        this.pltSignals.setCurveThickness(SZ_CRV_FIT);
        this.pltSignals.displayFittedData(datDev);
        
        this.pnlPrpsGauss.display(datDev.getDataAttrs());
        
        if (datDev instanceof ScannerData) {
            ScannerData    datScan = (ScannerData)datDev;
            
            this.pnlPrpsStat.display(datScan.getStatisticalAttributes());
            this.pnlPrpsDblGauss.display(datScan.getDoubleGaussianAttributes());
        }
    }

    
    
    /*
     * Support Methods
     */
    
    /**
     * Creates the individual GUI components.
     *
     * 
     * @since  Apr 23, 2010
     * @author Christopher K. Allen
     */
    private void buildGuiComponents() {
        this.lbxDevSel = new DeviceSelectorList();
        this.lbxDevSel.registerSelectionListener(this);
        
        MultiGraphDisplayPanel.LAYOUT   lytGraph = MultiGraphDisplayPanel.LAYOUT.HOR;
        ProfileAttrsDisplayPanel.LAYOUT lytDspl  = ProfileAttrsDisplayPanel.LAYOUT.HOR;
        
        this.pltSignals = new MultiGraphDisplayPanel(lytGraph);
        this.pltSignals.setLegendVisible(true);
        this.pltSignals.setLegendKey("");
        
        Font     fntLbl = this.getFont().deriveFont((float)16);
        this.lblSigPrps = new JLabel("Device provided properties");
        this.lblSigPrps.setFont(fntLbl);
        
        this.pnlPrpsStat = new ProfileAttrsDisplayPanel(lytDspl);
        this.pnlPrpsStat.setBorder( new TitledBorder("Statistical") );
        
        this.pnlPrpsGauss = new ProfileAttrsDisplayPanel(lytDspl);
        this.pnlPrpsGauss.setBorder( new TitledBorder("Gaussian Fit") );
        
        this.pnlPrpsDblGauss = new ProfileAttrsDisplayPanel(lytDspl);
//        this.pnlPrpsDblGauss.setName("Double Gaussian Fit");
        this.pnlPrpsDblGauss.setBorder( new TitledBorder("Double Gaussian Fit") );
    }
    
    /**
     * Lay out all the GUI components to make the user interface.
     *
     * 
     * @since  Apr 23, 2010
     * @author Christopher K. Allen
     */
    private void layoutGuiComponents() {
        this.setLayout( new GridBagLayout() );
        
        GridBagConstraints       gbcLayout = new GridBagConstraints();

        gbcLayout.insets = new Insets(0,0,5,5);
        
        // Device selection list
        gbcLayout.gridx = 0;
        gbcLayout.gridy = 0;
        gbcLayout.gridwidth  = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.VERTICAL;
        gbcLayout.weightx = 0.0;
        gbcLayout.weighty = 0.1;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        this.add( this.lbxDevSel, gbcLayout );
        
        // The profile plots
        gbcLayout.gridx = 1;
        gbcLayout.gridy = 0;
        gbcLayout.gridwidth  = 2;
        gbcLayout.gridheight = 2;
        gbcLayout.fill    = GridBagConstraints.BOTH;
        gbcLayout.weightx = 0.5;
        gbcLayout.weighty = 0.9;
        gbcLayout.anchor = GridBagConstraints.CENTER;
        this.add( this.pltSignals, gbcLayout );
        
//        // The signal properties label
//        gbcLayout.gridx = 0;
//        gbcLayout.gridy = 2;
//        gbcLayout.gridwidth  = 1;
//        gbcLayout.gridheight = 1;
//        gbcLayout.fill    = GridBagConstraints.NONE;
//        gbcLayout.weightx = 0.5;
//        gbcLayout.weighty = 0.9;
//        gbcLayout.anchor = GridBagConstraints.LINE_START;
//        this.add( this.lblSigPrps, gbcLayout );
//        
        // Signal properties
        gbcLayout.gridx = 1;
        gbcLayout.gridy = 2;
        gbcLayout.gridwidth  = 2;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.HORIZONTAL;
        gbcLayout.weightx = 0.5;
        gbcLayout.weighty = 0.5;
        gbcLayout.anchor = GridBagConstraints.CENTER;
        this.add( this.pnlPrpsStat, gbcLayout );
        
        gbcLayout.gridx = 1;
        gbcLayout.gridy = 3;
        this.add( this.pnlPrpsGauss, gbcLayout );
        
        gbcLayout.gridx = 1;
        gbcLayout.gridy = 4;
        this.add( this.pnlPrpsDblGauss, gbcLayout );
    }
    
    /**
     * Initializes the GUI to the current
     * measurement data (if there is any).
     *
     * 
     * @since  Apr 23, 2010
     * @author Christopher K. Allen
     */
    private void initGui() {
        if (this.setMsmt != null)
            this.lbxDevSel.setDeviceList( this.setMsmt.getDeviceIdSet() );
    }
    
    /**
     * Clears out all data in the GUI display.
     *
     * 
     * @since  Apr 27, 2010
     * @author Christopher K. Allen
     */
    private void clearAll() {
        this.lbxDevSel.clear();
        
        this.pltSignals.clear();
        
        this.pnlPrpsDblGauss.clearDisplay();
        this.pnlPrpsGauss.clearDisplay();
        this.pnlPrpsStat.clearDisplay();
    }

}
