/**
 * DataAnalysisView.java
 *
 * @author Christopher K. Allen
 * @since  Nov 16, 2011
 *
 */
package xal.app.pta.view;

import xal.app.pta.IConfigView;
import xal.app.pta.IDocView;
import xal.app.pta.MainConfiguration;
import xal.app.pta.MainDocument;
import xal.app.pta.daq.MeasurementData;
import xal.app.pta.tools.analysis.SignalAnalyzer;
import xal.app.pta.view.analysis.SingleSignalDisplay;
import xal.app.pta.view.cmn.DeviceSelectorList;
import xal.app.pta.view.plt.MultiGraphDisplayPanel;
import xal.smf.impl.WireScanner;
import xal.smf.impl.profile.ProfileDevice;
import xal.smf.impl.profile.ProfileDevice.ANGLE;
import xal.smf.impl.profile.ProfileDevice.IProfileData;
import xal.smf.impl.profile.Signal;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Analyzes measurement data taken from the profile device.  Normalizes the data and computes the beam positions
 * and sizes along with confidence intervals for the values.
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author Christopher K. Allen
 * @since   Nov 16, 2011
 */
public class DataAnalysisView extends JPanel implements IDocView, IConfigView, ListSelectionListener {

    
    /*
     * Global Constants 
     */
    
    /** Serialization version */
    private static final long serialVersionUID = 1L;

    
    
    /** Curve color of the raw beam data */
    private static final Color  CLR_CRV_RAW = Color.BLACK;
    
    /** Size of the raw data curve */
    private static final int    SZ_CRV_RAW = 1;

    
    /** Curve color of the fitted beam data */
    private static final Color  CLR_CRV_FIT = Color.RED;
    
    /** Size of the fitted data curve */
    private static final int    SZ_CRV_FIT = 2;

    
    /** Color of the equivalent Gaussian curve */
    private static final Color  CLR_CRV_RMS = Color.GREEN;
    
    /** Size (thickness) of the equivalent gaussian curve */
    private static final int    SZ_CRV_RMS = 2;
    
    /** Color of the beam position line in graph */
    private static final Color  CLR_POS_RMS = Color.MAGENTA;
    
    /** Color of the beam size lines in graph */
    private static final Color  CLR_SIG_RMS = Color.BLUE;

    
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
    private DeviceSelectorList           lbxDevSel;

    
    /** The profile signal plots */
    private MultiGraphDisplayPanel      pltSignals;
    
    /** Label for signal parameters */
    private JLabel                     lblSigParms; 
   
    /** The signal properties in horizontal plane */
    private SingleSignalDisplay         pnlSigHor;
    
    /** The signal properties in vertical plane */
    private SingleSignalDisplay         pnlSigVer;
    
    /** The signal properties in diagonal plane */
    private SingleSignalDisplay         pnlSigDia;
    
    
    

    /*
     * Initialization
     */
    
    /**
     * Create a new <code>DataAnalysisView</code> object.
     *
     * @param docMain   the application measurement data
     *
     * @since     Apr 23, 2010
     * @author    Christopher K. Allen
     */
    public DataAnalysisView(MainDocument docMain) {
        this.setMsmt = docMain.getMeasurementData();
        
        this.guiBuildComponents();
        this.guiLayoutComponents();
        this.guiInitialize();
    }
    

    
    /*
     * ListSelectionListener Interface
     */
    
    /**
     * Display the graph and signal properties of the
     * newly selected device.
     * 
     * @param   evt  list selection event - not used
     *
     * @since Nov 16, 2011
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    @Override
    public void valueChanged(ListSelectionEvent evt) {
        
        // Get the hardware device ID from the listbox and retrieve its data
        String     strDevId = this.lbxDevSel.getSelectedDevice();
        IProfileData datDev = this.setMsmt.getDataForDeviceId(strDevId); 
        
        this.pltSignals.clear();
        
        if (datDev == null)
            return;

        // Draw the raw data and fitted data curves on the graph
        this.pltSignals.setCurveLabel("raw");
        this.pltSignals.setCurvePoints(true);
        this.pltSignals.setCurveColor(CLR_CRV_RAW);
        this.pltSignals.setCurveThickness(SZ_CRV_RAW);
        this.pltSignals.displayRawData(datDev);
        
        this.pltSignals.setCurveLabel("fit");
        this.pltSignals.setCurvePoints(false);
        this.pltSignals.setCurveColor(CLR_CRV_FIT);
        this.pltSignals.setCurveThickness(SZ_CRV_FIT);
        this.pltSignals.displayFittedData(datDev);

        // Compute the RMS statistics, plot the equivalent Gaussian, then annotation the graphs with RMS properties
        Signal          sigCleanHor = this.scrubRawData(ANGLE.HOR, datDev);
        Signal          sigCleanVer = this.scrubRawData(ANGLE.VER, datDev);
        Signal          sigCleanDia = this.scrubRawData(ANGLE.DIA, datDev);
        
        SignalAnalyzer  anlHor = new SignalAnalyzer(sigCleanHor);
        SignalAnalyzer  anlVer = new SignalAnalyzer(sigCleanVer);
        SignalAnalyzer  anlDia = new SignalAnalyzer(sigCleanDia);

        //      Plot the equivalent Gaussian
        this.pltSignals.setCurveLabel("rms");
        this.pltSignals.setCurvePoints(false);
        this.pltSignals.setCurveColor(CLR_CRV_RMS);
        this.pltSignals.setCurveThickness(SZ_CRV_RMS);
        
        WireScanner.DataFit datGauss = new WireScanner.DataFit();
        datGauss.hor = anlHor.equivGaussianWithBaseline();
        datGauss.ver = anlVer.equivGaussianWithBaseline();
        datGauss.dia = anlDia.equivGaussianWithBaseline();
        this.pltSignals.displayProfile(datGauss);
        
        //      Annotate the graph with the beam size and position as computed by the analyzer
        this.pltSignals.addVerticalLine(WireScanner.ANGLE.HOR, anlHor.getBeamPosition(), CLR_POS_RMS);
        this.pltSignals.addVerticalLine(WireScanner.ANGLE.HOR, anlHor.getBeamPosition()-anlHor.getBeamRmsSize(), CLR_SIG_RMS);
        this.pltSignals.addVerticalLine(WireScanner.ANGLE.HOR, anlHor.getBeamPosition()+anlHor.getBeamRmsSize(), CLR_SIG_RMS);
        
        this.pltSignals.addVerticalLine(WireScanner.ANGLE.VER, anlVer.getBeamPosition(), CLR_POS_RMS);
        this.pltSignals.addVerticalLine(WireScanner.ANGLE.VER, anlVer.getBeamPosition()-anlVer.getBeamRmsSize(), CLR_SIG_RMS);
        this.pltSignals.addVerticalLine(WireScanner.ANGLE.VER, anlVer.getBeamPosition()+anlVer.getBeamRmsSize(), CLR_SIG_RMS);
        
        this.pltSignals.addVerticalLine(WireScanner.ANGLE.DIA, anlDia.getBeamPosition(), CLR_POS_RMS);
        this.pltSignals.addVerticalLine(WireScanner.ANGLE.DIA, anlDia.getBeamPosition()-anlDia.getBeamRmsSize(), CLR_SIG_RMS);
        this.pltSignals.addVerticalLine(WireScanner.ANGLE.DIA, anlDia.getBeamPosition()+anlDia.getBeamRmsSize(), CLR_SIG_RMS);
        
        // Display the results of the analysis
        this.pnlSigHor.display( anlHor );
        this.pnlSigVer.display( anlVer );
        this.pnlSigDia.display( anlDia );
    }

    
    /*
     * IConfigView Interface
     */
    
    /**
     * We simply clear the GUI here.
     * 
     * @since Nov 16, 2011
     * @see xal.app.pta.IConfigView#updateAccelerator(xal.app.pta.MainConfiguration)
     */
    @Override
    public void updateAccelerator(MainConfiguration docMain) {
        this.clearAll();
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
     * IDocView Interface
     */

    /**
     * Populate the device selection list with the IDs of the devices
     * used in the new measurement set. 
     *
     * @since Nov 16, 2011
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
     * Support Methods
     */
    
    /**
     * Creates the individual GUI components.
     * 
     * @since  Dec 13, 2011
     * @author Christopher K. Allen
     */
    private void guiBuildComponents() {
        this.lbxDevSel = new DeviceSelectorList();
        this.lbxDevSel.registerSelectionListener(this);
        
        MultiGraphDisplayPanel.LAYOUT   lytGraph = MultiGraphDisplayPanel.LAYOUT.HOR;
        
        this.pltSignals = new MultiGraphDisplayPanel(lytGraph);
        this.pltSignals.setLegendVisible(true);
        this.pltSignals.setLegendKey("");
        
        Font    fntLabel = this.getFont().deriveFont((float) 18.0);
        this.lblSigParms = new JLabel("Computed Signal Properties");
        this.lblSigParms.setFont(fntLabel);
        
        this.pnlSigHor = new SingleSignalDisplay();
        this.pnlSigHor.setBorder( new TitledBorder("Horizontal") );
        
        this.pnlSigVer = new SingleSignalDisplay();
        this.pnlSigVer.setBorder( new TitledBorder("Vertical") );
        
        this.pnlSigDia = new SingleSignalDisplay();
        this.pnlSigDia.setBorder( new TitledBorder("Diagonal") );
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
        gbcLayout.gridwidth  = 3;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.BOTH;
        gbcLayout.weightx = 0.5;
        gbcLayout.weighty = 0.9;
        gbcLayout.anchor = GridBagConstraints.CENTER;
        this.add( this.pltSignals, gbcLayout );
        
        // The signal properties label
        gbcLayout.gridx = 1;
        gbcLayout.gridy = 1;
        gbcLayout.gridwidth  = 3;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.HORIZONTAL;
        gbcLayout.weightx = 0.5;
        gbcLayout.weighty = 0.1;
        gbcLayout.anchor = GridBagConstraints.CENTER;
        this.add( this.lblSigParms, gbcLayout );
        
        // Signal properties
        gbcLayout.gridx = 1;
        gbcLayout.gridy = 2;
        gbcLayout.gridwidth  = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.HORIZONTAL;
        gbcLayout.weightx = 0.5;
        gbcLayout.weighty = 0.1;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        this.add( this.pnlSigHor, gbcLayout );
        
        gbcLayout.gridx = 2;
        gbcLayout.gridy = 2;
        this.add( this.pnlSigVer, gbcLayout );
        
        gbcLayout.gridx = 3;
        gbcLayout.gridy = 2;
        this.add( this.pnlSigDia, gbcLayout );
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
            this.lbxDevSel.setDeviceList( this.setMsmt.getDeviceIdSet() );
    }
    
    /**
     * Clears out all data in the GUI display.
     * 
     * @since  Apr 27, 2010
     * @author Christopher K. Allen
     */
    private void clearAll() {
        this.lbxDevSel.clear();
        
        this.pltSignals.clear();
        
        this.pnlSigHor.clearDisplay();
        this.pnlSigVer.clearDisplay();
        this.pnlSigDia.clearDisplay();
    }
    
    /**
     * Processes the given projection data of the given profile data set by
     * removing the data for wires that are flagged invalid.  This method works
     * only on the "raw data" of the given projection data.
     * 
     * @param angle         projection angle of the signal
     * @param datProfile    projection data containing the raw signal set
     * 
     * @return              signal for given angle, scrubbed of data for bad wires
     *
     * @author Christopher K. Allen
     * @since  Jul 2, 2014
     */
    private Signal  scrubRawData(ProfileDevice.ANGLE angle, IProfileData datProfile) {
        int         cntWires = datProfile.getDataSize();
        Signal      sigRaw   = datProfile.getRawData().getSignal(angle);
        
        List<Double>   lstPos = new LinkedList<Double>();
        List<Double>   lstVal = new LinkedList<Double>();
        
        for (int iWire=0; iWire<cntWires; iWire++) {
            if ( !datProfile.isValidWire(angle, iWire) )
                continue;
            
            lstPos.add( sigRaw.pos[iWire] );
            lstVal.add( sigRaw.val[iWire] );
        }
        
        Signal  sigClean = new Signal();
        sigClean.cnt = lstPos.size();
        sigClean.navg = sigRaw.navg;
        sigClean.nvar = sigRaw.nvar;
        sigClean.pos  = new double[sigClean.cnt];
        sigClean.val  = new double[sigClean.cnt];
        
        int iPos = 0;
        for (Double dblPos : lstPos) 
            sigClean.pos[iPos++] = dblPos;
        
        int iVal = 0;
        for (Double dblVal : lstVal) 
            sigClean.val[iVal++] = dblVal;
        
        return sigClean;
    }
    
}
