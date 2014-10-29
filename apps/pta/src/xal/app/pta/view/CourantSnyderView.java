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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import xal.app.pta.IConfigView;
import xal.app.pta.IDocView;
import xal.app.pta.MainApplication;
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
import xal.extension.twissobserver.ConvergenceException;
import xal.extension.twissobserver.Measurement;
import xal.extension.twissobserver.MeasurementCurve;
import xal.extension.widgets.olmplot.EnvelopeCurve;
import xal.extension.widgets.olmplot.PLANE;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.model.ModelException;
import xal.model.alg.EnvTrackerAdapt;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.Trajectory;
import xal.service.pvlogger.sim.PVLoggerDataSource;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.impl.profile.ProfileDevice.IProfileData;
import xal.tools.beam.CovarianceMatrix;

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
    
//    public interface PhasePlaneSelectionAction
    
    /**
     * Data structure containing all the data required to solve the 
     * Courant-Snyder reconstruction
     * problem as well as the reconstruction (i.e., the covariance matrix) itself.
     *
     * @author Christopher K. Allen
     * @since  Oct 23, 2014
     */
    public static class SolutionSet {

        /** PV Logger ID of the machine state during the measurement operation */
        public long                    lngPvLogId;
        
        /** The accelerator node at which the C-S parameters are being reconstructed */
        public AcceleratorNode         smfDevRecon;
        
        /** 
         * The accelerator sequence just large enough to contain the measurement 
         * locations and the reconstruction location.
         */
        public AcceleratorSeq          smfSeqRecon;
        
        /** The measurement data used in the reconstruction */ 
        public ArrayList<Measurement>  arrMsmts;

        
        /** The solution to the reconstruction problem */
        public CovarianceMatrix        matRecon;

        
        /*
         * Initialization
         */
        
        /**
         * Constructor for data structure <code>SolutionSet</code>.
         * Creates an empty data structure, fields having null values.
         *
         * @author Christopher K. Allen
         * @since  Oct 23, 2014
         */
        public SolutionSet() {
            this.lngPvLogId  = -1;
            this.smfDevRecon = null;
            this.smfSeqRecon = null;
            this.arrMsmts    = null;
            
            this.matRecon    = null;
        }
        
        /*
         * Operations
         */
        
        /**
         * Check that we have data to the problem.  Does not indicate whether or
         * not the data is good however.
         * 
         * @return
         *
         * @author Christopher K. Allen
         * @since  Oct 23, 2014
         */
        public boolean  hasAllData() {
            if (lngPvLogId==-1    || 
                    smfDevRecon==null || 
                    smfSeqRecon==null || 
                    arrMsmts==null
                    )
                return false;
            
            return true;
        }
        
        /**
         * Clears out all the problem data and solution.
         *
         * @author Christopher K. Allen
         * @since  Oct 28, 2014
         */
        public void clear() {
            this.lngPvLogId = -1;
            this.smfDevRecon = null;
            this.smfSeqRecon = null;
            this.arrMsmts    = null;
            this.matRecon    = null;
        }
    }

    
    /**
     * Class <code>CourantSnyderView</code> is a panel presenting a set of check
     * boxes for selecting phase planes.  The developer can determine which phase
     * planes the use has selected from the GUI with the method
     * <code>{@link #isSelected()}</code>.  
     *
     * @author Christopher K. Allen
     * @since  Oct 23, 2014
     */
    private class PhasePlaneSelectorPanel extends JPanel {
        

        /*
         * Global Constants
         */
        
        /** Serialization version ID  */
        private static final long serialVersionUID = 1L;
        
        
        /*
         * Local Attributes
         */
        
        /** Selection box for the horizontal envelope */
        private final Map<PLANE, JCheckBox>     mapPlnToChk;
        
//        /** The internal listener that catches selection events from the user */
//        private final ActionListener            lsnEvtChk;
//        
//        /** The set of external listeners that want to be informed of user selection events */
//        private final List<ActionListener>      lstChkLnrs;
        
        /*
         * Initialization
         */
        
        /**
         * Constructs a new <code>PhasePlaneSelectorPanel</code>, creates all
         * the internal check boxes and initializes them to checked.
         *
         * @author Christopher K. Allen
         * @since  Oct 23, 2014
         */
        public PhasePlaneSelectorPanel() {
            this.mapPlnToChk = new HashMap<>();
            
            for (PLANE plane : PLANE.values()) {
                String      strLabel = plane.name();
                JCheckBox   chkPlane = new JCheckBox(strLabel, true);
                
                this.mapPlnToChk.put(plane, chkPlane);
            }
            
            this.guiLayoutComponents();
        }
        
        /**
         * Sets all the check boxes (for all planes) to the given value.
         * 
         * @param bolChecked    all phase plane check boxes are checked if <code>true</code>,
         *                      unchecked if <code>false</code>
         *
         * @author Christopher K. Allen
         * @since  Oct 23, 2014
         */
        public void     setAll(boolean bolChecked) {
            for (PLANE plane : PLANE.values()) {
                JCheckBox   chkPlane = this.mapPlnToChk.get(plane);
              
                chkPlane.setSelected(bolChecked);
            }
        }
        
        /**
         * Register the given listener object to receive notifications whenever
         * one of the check boxes is changed by the user.
         * 
         * @param lsnChkEvt object to receive check box changed events
         *
         * @author Christopher K. Allen
         * @since  Oct 23, 2014
         */
        public void     addActionLister(ActionListener lsnChkEvt) {
            for (PLANE plane : PLANE.values()) {
                JCheckBox   chkPlane = this.mapPlnToChk.get(plane);
              
                chkPlane.addActionListener(lsnChkEvt);
            }
        }
        
        // 
        // Operations
        //
        
        /**
         * Returns the current state of the given check box.  
         * 
         * @param plane     gets the state for the check box corresponding to this phase plane
         * 
         * @return          <code>true</code> if the check box for the given phase plane is selected.
         *
         * @author Christopher K. Allen
         * @since  Oct 23, 2014
         */
        public boolean  isSelected(PLANE plane) {
            JCheckBox       chkPlane = this.mapPlnToChk.get(plane);
            boolean         bolPlane = chkPlane.isSelected();
            
            return bolPlane;
        }
        
        /**
         * Clears all the check boxes in the display
         *
         * @author Christopher K. Allen
         * @since  Oct 23, 2014
         */
        public void     clearAll() {
            this.setAll(false);
        }
       
        
        /*
         * Support Methods
         */
        
        /**
         * Lays out all the check boxes on the GUI front panel.
         *
         * @author Christopher K. Allen
         * @since  Oct 23, 2014
         */
        private void guiLayoutComponents() {
            Box boxPanel = Box.createVerticalBox();
            
            for (PLANE plane : PLANE.values() ) {
                JCheckBox   chkPlane = this.mapPlnToChk.get(plane);
                
                boxPanel.add(chkPlane);
                boxPanel.add(Box.createVerticalStrut(10));
            }
            
            this.add(boxPanel);
        }
    }
    
    
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
    private MeasurementData             setMsmts;
    
    /** The CS reconstruction solution and all its data */
    private SolutionSet                 setSoln;
    
//    /** The solution to the reconstruction problem */
//    private CovarianceMatrix            matRecon;
//    
//    /** The accelerator sequence, or combo sequence, containing all the measurement points and reconstruction location */
//    private AcceleratorSeq              smfSeqRecon;
//    
//    /** A bunch length simulator used to approximate the longitudinal size long the reconstruction region */
//    private BunchLengthSimulator        blsMsmtDataLng;
//    
//    /** A fixed-point method Courant-Snyder estimator */
//    private CsFixedPtEstimator          cseFixedPtRecon;
//    
//    /** The list of measurements we are using for reconstruction */
//    private List<Measurement>           lstMmtsRec;
    
    
    //
    // GUI Components
    
    /** The reconstruction location selector (at which device) */
    private DeviceSelectorPanel         pnlRecLoc;
    
    /** The data selector (i.e., from which device) */
    private DeviceSelectorList          lbxMmtData;
    
    /** The fixed point reconstruction algorithm interface */
    private CsFixedPtControlPanel       pnlFxdPtCltr;

    
    /** Phase plane selector for solution display */
    private PhasePlaneSelectorPanel     pnlPhsSel;
    
    /** The computed Courant-Snyder parameters display */
    private Twiss3PlaneDisplayPanel     pnlTws3d;
    
    /** The envelopes resulting to the reconstructed Courant-Snyder parameters */
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
    public CourantSnyderView(MainDocument docMain) {
        super();
        this.docMain = docMain;
        
        this.setSoln = new SolutionSet();
//        this.lstMmtsRec = new LinkedList<>();
        
        this.guiBuildComponents();
        this.guiBuildActions();
        this.guiLayoutComponents();
        this.guiInitialize();
    }
    
    
    /*
     * IDeviceSelectionListener Interface
     */
    
    /**
     * Want to catch the events that come from our own device selection listener (from the
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
        
        this.setMsmts = docMain.getMeasurementData();
        
        if (this.setMsmts != null) 
            this.lbxMmtData.setDeviceList( this.setMsmts.getDeviceIdSet() );
    }

    
//    /*
//     * ActionListener Interface
//     */
//
//    /**
//     * Catches the change state notifications coming from the phase plane selection
//     * panel which tells use which envelopes to plot. 
//     *
//     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
//     *
//     * @author Christopher K. Allen
//     * @since  Oct 23, 2014
//     */
//    @Override
//    public void actionPerformed(ActionEvent e) {
//        // TODO Auto-generated method stub
//
//    }
//
//
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
        
        // We have the data and the reconstruction location, now make the beamline and solve
//        long                    lngPvLogId    = this.setMsmts.getPvLoggerId();
//        AcceleratorNode         smfDevRecon   = this.identifyReconstructionLocation();
//        AcceleratorSeq          smfSeqRecon   = this.identifyReconstructionBeamline();
//        ArrayList<Measurement>  arrMsmts      = this.processMeasurementData();
        this.setSoln.lngPvLogId    = this.setMsmts.getPvLoggerId();
        this.setSoln.smfDevRecon   = this.identifyReconstructionLocation();
        this.setSoln.smfSeqRecon   = this.identifyReconstructionBeamline();
        this.setSoln.arrMsmts      = this.processMeasurementData();

        // Bail out if there is a problem
        if (this.setSoln.hasAllData() == false)
            return;
        
        // Compute the Courant-Snyder parameters via the C-S control panel then display
        //  the solution
        try {
            this.pnlFxdPtCltr.estimateCovariance(this.setSoln);

            this.displayReconSolution();
            
        } catch (ModelException e) {
            MainApplication.getEventLogger().logError(this.getClass(), "Courant-Snyder estimation failure - online model exception");;
            e.printStackTrace();
            
        } catch (ConvergenceException e) {
            MainApplication.getEventLogger().logError(this.getClass(), "Courant-Snyder estimation failure - no convergence in algorithm");
            e.printStackTrace();
            
        } catch (Exception e) {
            MainApplication.getEventLogger().logError(this.getClass(), "Courant-Snyder estimation failure - general computation failure in base class");
            e.printStackTrace();
            
        }
    }
    
//    /**
//     * Creates a Courant-Snyder parameter estimator for the current reconstruction
//     * accelerator sector and current measurement set (PV logger ID) by consulting
//     * the Courant-Snyder Fixed Point Estimator control panel.  The panel contains 
//     * the numeric parameters to run the search.
//     * 
//     * @return  fixed point Courant-Snyder estimator object for current measurements 
//     * 
//     * @throws ModelException error in instantiating the machine model
//     *
//     * @author Christopher K. Allen
//     * @since  Oct 7, 2014
//     */
//    private CsFixedPtEstimator createCsEstimator() throws ModelException {
//        
//        long                    lngPvLogId  = this.setMsmt.getPvLoggerId();
//        TransferMatrixGenerator trxRecon    = new TransferMatrixGenerator(this.smfSeqRecon, lngPvLogId);
//        CsFixedPtEstimator      cseFxdPt    = this.pnlFxdPtCltr.createEstimator(trxRecon);
//        
//        return cseFxdPt;
//    }
//    
    /**
     * Returns the SMF accelerator node where the Courant-Snyder reconstruction is
     * to take place.
     * 
     * @return  reconstruction location or <code>null</code> if none is selected
     *
     * @author Christopher K. Allen
     * @since  Oct 22, 2014
     */
    private AcceleratorNode identifyReconstructionLocation() {
        
        // Get the beamline where the computations are made
        //  This object will be used by many of the support methods below
        AcceleratorNode smfDevRecon   = this.pnlRecLoc.getSelectedDevice();

        if (smfDevRecon == null) { 
            MainApplication.getApplicationDocument().displayWarning("No Reconstruction Location", "You must select a beamline location");
        
            return null;
        }
        
        return smfDevRecon;
    }
    
    /**
     * Determines all the necessary accelerator sequences to perform the 
     * Courant-Snyder parameter computation.  If the data and reconstruction
     * location are contained in multiple sequences then a <code>AcceleratorSeqCombo</code>
     * is constructed that minimally contains all the locations.
     * 
     * @return  an accelerator sequence (or combo sequence) containing all measurement and 
     *          reconstruction locations
     *
     * @author Christopher K. Allen
     * @since  Oct 6, 2014
     */
    private AcceleratorSeq identifyReconstructionBeamline() {
        
        // Identify all the accelerator sequences containing measurement locations
        //  and the reconstruction location.
        //
        //  We store all the sequences into an ordered Set container since it does not
        //  allow duplicate entries.
        Comparator<AcceleratorSeq> cmpSeqOrder = new Comparator<AcceleratorSeq>() {
            @Override
            public int compare(AcceleratorSeq smfSeq1, AcceleratorSeq smfSeq2) {
                
                if (smfSeq1.getPosition() < smfSeq2.getPosition())
                    return -1;
                else if (smfSeq1.getPosition() > smfSeq2.getPosition())
                    return +1;
                else
                    return 0;
            }
        };
        Set<AcceleratorSeq> setSeqMsmts = new TreeSet<>(cmpSeqOrder);

        // The measurement devices identifying the measurement locations.
        //  Get their parent sequence and store that sequence in the set of sequences
        Accelerator                smfAccel    = this.docMain.getAccelerator();
        List<String>               lstDevIds   = this.lbxMmtData.getSelectedDevices();

        for (String strDevId : lstDevIds) {
            AcceleratorNode smfDevMsmt = smfAccel.getNode(strDevId);
            AcceleratorSeq  smfSeqMsmt = smfDevMsmt.getParent();
            
            setSeqMsmts.add(smfSeqMsmt);
        }

        // Add the reconstruction location into the mix
        AcceleratorNode     smfDevRecon = this.pnlRecLoc.getSelectedDevice();
        AcceleratorSeq      smfSeqRecon = smfDevRecon.getParent();
        
        setSeqMsmts.add(smfSeqRecon);
        
        
        // If the measurement data lives in only one sequence we are done
        int     cntSeqs = setSeqMsmts.size();
        if (cntSeqs == 1)
            return smfSeqRecon;
        
        // Else the measurement data lives in more than one sequence and we must glue
        //  them together into a combo sequence
        List<AcceleratorSeq>    lstSeqMsmts = new LinkedList<>();
        for (AcceleratorSeq smfSeqMsmt : setSeqMsmts) 
            lstSeqMsmts.add(smfSeqMsmt);
 
        String              strCmbId1   = lstSeqMsmts.get( 0 ).getId();
        String              strCmbId2   = lstSeqMsmts.get( cntSeqs - 1 ).getId();
        String              strCmbId    = strCmbId1 + "-" + strCmbId2;
        AcceleratorSeqCombo smfCmbMsmts = new AcceleratorSeqCombo(strCmbId, lstSeqMsmts);
        
        return smfCmbMsmts;
    }
    
    /**
     * Retrieve the RMS beam sizes from the current application measurement
     * data.
     *  
     * @return  an array list of RMS beam sizes for each measurement device
     *
     * @throws ModelException   an error occurred while instantiating machine model
     *  
     * @author Christopher K. Allen
     * @since  Oct 6, 2014
     */
    private ArrayList<Measurement>  processMeasurementData() {
    
        List<String>    lstDevIdMsmts = this.lbxMmtData.getSelectedDevices();
        
        // Check that data has been selected
        if (lstDevIdMsmts.size() == 0) {
            MainApplication.getApplicationDocument().displayWarning("No Data", "You must select a data set");
            
            return null;
        }
        
        // Create the array for measurement data and get the measurement scaling factor
        double                  dblScaleStd = AppProperties.MSMT.SCALE_STD_WIRE.getValue().asDouble();
        ArrayList<Measurement>  lstMsmts    = new ArrayList<>();
        
        for (String strDevId : lstDevIdMsmts) {
    
            IProfileData    datMsmt = this.setMsmts.getDataForDeviceId(strDevId);
    
            Measurement msmt = new Measurement();
            msmt.strDevId  = strDevId;
            msmt.dblSigHor = datMsmt.getDataAttrs().hor.stdev * dblScaleStd;
            msmt.dblSigVer = datMsmt.getDataAttrs().ver.stdev * dblScaleStd;
            msmt.dblSigLng = 0.0;
    
            lstMsmts.add(msmt);
        }
    
        return lstMsmts;
    }

    /**
     * Simulations the envelope trajectories for the given accelerator sequence using the
     * given initial covariance matrix for the RMS beam envelopes.  The simulation solution
     * is then drawn in the GUI graph panel along with the original measurements.
     * 
     * @param seqRecon  the accelerator sequence containing measurements and simulation 
     * @param matRecon  the initial covariance matrix for the RMS beam envelopes, 
     *                  i.e., to start the simulation
     *
     * @author Christopher K. Allen
     * @since  Oct 22, 2014
     */
    private void    displayReconSolution() {
        
        // Check if there is a solution to display
        if (this.setSoln.matRecon == null)
            return;
        
        long                    lngPvLogId = this.setSoln.lngPvLogId;
        AcceleratorSeq          seqRecon   = this.setSoln.smfSeqRecon;
        ArrayList<Measurement>  arrMsmts   = this.setSoln.arrMsmts;
        CovarianceMatrix        matRecon   = this.setSoln.matRecon;

        // Create an online model synchronized to the given pv logger machine state
        //  Run the model then display the resulting solution envelopes on the screen 
        try {
            
            Scenario        modRecon = Scenario.newScenarioFor(seqRecon);
            EnvelopeProbe   prbRecon = this.pnlFxdPtCltr.createEnvelopeProbe(seqRecon, matRecon);
            
            PVLoggerDataSource  pvlMachState = new PVLoggerDataSource(lngPvLogId);
            modRecon = pvlMachState.setModelSource(seqRecon, modRecon);
            modRecon.setProbe(prbRecon);
            
            modRecon.resync();
            modRecon.run();
            
            Trajectory<EnvelopeProbeState>  trjSoln = modRecon.getTrajectory();
            
            // Clear the graph
            this.pltEnvs.removeAllGraphData();
            
            // Display the computed solution on the GUI graph
            for (PLANE plane : PLANE.values()) 
                if (this.pnlPhsSel.isSelected(plane)) {
                    EnvelopeCurve   crvSoln = new EnvelopeCurve(plane, trjSoln);

                    crvSoln.setGraphProperty("", plane.name() ); 
                    this.pltEnvs.addGraphData(crvSoln);
                }
            
            // Add the measurement points to the graph
            for (PLANE plane : PLANE.values()) 
                if (this.pnlPhsSel.isSelected(plane)) {
                    MeasurementCurve    crvMsmts = new MeasurementCurve(plane, seqRecon, arrMsmts);

                    crvMsmts.setGraphProperty("", plane.name() ); 
                    this.pltEnvs.addGraphData(crvMsmts);
                }

        } catch (ModelException e) {
            MainApplication.getApplicationDocument().displayError(
                    "Data Error", 
                    "Unable to display simulation data obtained from the C-S parameter reconstruction"
                    );
            MainApplication.getEventLogger().logWarning(getClass(), 
                    "Unable to display simulation data obtained from the C-S parameter reconstruction"
                    );
            e.printStackTrace();
        }

        // Update the Courant-Snyder parameter display and the graph display
        this.pnlTws3d.display(matRecon);
        this.pltEnvs.refreshGraphJPanel();
    }
    
    
    // 
    //  GUI Construction
    //
    
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
        
        this.pnlPhsSel = new PhasePlaneSelectorPanel();
        this.pnlPhsSel.setAll(true);
        
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
        
        // Respond to the changing of phase plane solutions to display
        ActionListener  lsnEvtChk = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                CourantSnyderView.this.displayReconSolution();
            }
            
        };
        this.pnlPhsSel.addActionLister(lsnEvtChk);
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

        // The phase plane solution envelopes choser location
        Box     boxSolnChk = Box.createVerticalBox();
        JLabel  lblSolnChk = new JLabel("Solutions from CS Estimates");
        
        boxSolnChk.add(lblSolnChk);
        boxSolnChk.add(Box.createVerticalStrut(10));
        boxSolnChk.add(this.pnlPhsSel);
        
        gbcLayout.gridx = 2;
        gbcLayout.gridy = 1;
        gbcLayout.gridwidth  = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.VERTICAL;
        gbcLayout.weightx = 0.0;
        gbcLayout.weighty = 0.1;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        this.add( boxSolnChk, gbcLayout );

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
        
        if (this.setMsmts != null)
            this.lbxMmtData.setDeviceList( this.setMsmts.getDeviceIdSet() );
    }
    
    /**
     * Clears out all data in the GUI display.
     * 
     * @since  Apr 27, 2010
     * @author Christopher K. Allen
     */
    private void clearAll() {
        this.setSoln.clear();

        this.pnlRecLoc.clearSelections();
        this.lbxMmtData.clear();
        this.pnlFxdPtCltr.clearAll();
        this.pltEnvs.removeAllGraphData();
        this.pnlTws3d.clearDisplay();
        this.pltEnvs.removeAllGraphData();
    }

}
