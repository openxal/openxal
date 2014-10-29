/**
 * CsFixedPtControlPanel.java
 *
 * Author  : Christopher K. Allen
 * Since   : Oct 2, 2014
 */
package xal.app.pta.view.analysis;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import xal.app.pta.MainApplication;
import xal.app.pta.rscmgt.AppProperties;
import xal.app.pta.rscmgt.PtaResourceManager;
import xal.app.pta.tools.swing.NumberTextField;
import xal.app.pta.tools.swing.NumberTextField.FMT;
import xal.app.pta.view.CourantSnyderView;
import xal.extension.twissobserver.BunchLengthSimulator;
import xal.extension.twissobserver.ConvergenceException;
import xal.extension.twissobserver.CourantSnyderEstimator;
import xal.extension.twissobserver.CsFixedPtEstimator;
import xal.extension.twissobserver.CsZeroCurrentEstimator;
import xal.extension.twissobserver.Measurement;
import xal.extension.twissobserver.TransferMatrixGenerator;
import xal.model.ModelException;
import xal.model.alg.EnvTrackerAdapt;
import xal.model.probe.EnvelopeProbe;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.smf.AcceleratorSeq;
import xal.tools.beam.CovarianceMatrix;

/**
 * GUI class manages the user interaction for computing Courant-Snyder parameters
 * from profile measurement data. This class is responsible for initiating the
 * calculations and grabbing some numerical parameters input from the user.
 * The actually calculations are done elsewhere.  In particular, the actions
 * here are delegated to class <code>CourantSnyderView</code>. 
 *
 *
 * @author Christopher K. Allen
 * @since  Sep 30, 2014
 */
public class CsFixedPtControlPanel extends JPanel {

    /* 
     * Global Constants
     */

    /** Serialization version number  */
    private static final long serialVersionUID = 1L;

    /** Number of solution iterations before an update is displayed */
    private static final int        INT_FREQ_DISPL = AppProperties.NUMERIC.CSFP_ITER_MOD.getValue().asInteger();
    

    /*
     * Inner Classes
     */
    
    /**
     * Class <code>ButtonThread</code> launches a
     * {@link ActionListener#actionPerformed(ActionEvent)} method
     * as a thread so the calling object can return to its
     * execution.  It is intended to be used to fire action events
     * associated with the "Compute" button.
     *
     * @author Christopher K. Allen
     * @since  Oct 28, 2014
     */
    private class ButtonThread extends Thread {
        
        /*
         * Local Attributes
         */
        
        /** The action listener we are going to notify */
        private final ActionListener        lsnButton;
        
        /*
         * Initialization
         */
        
        /**
         * Constructor for ButtonThread.
         *
         * @param lsnButton     the listener to be notified when the event occurs.
         *
         * @author Christopher K. Allen
         * @since  Oct 28, 2014
         */
        public ButtonThread(ActionListener lsnButton) {
            this.lsnButton = lsnButton;
        }
        
        /*
         * ActionListener Interface
         */
        
        /**
         * We launch the <code>actionEvent()</code> method of our associated
         * <code>ActionListener</code> object. 
         *
         * @see java.lang.Thread#run()
         *
         * @author Christopher K. Allen
         * @since  Oct 28, 2014
         */
        @Override
        public void run() {
            ActionEvent evtButtonPressed = new ActionEvent(
                    CsFixedPtControlPanel.this, 
                    ActionEvent.ACTION_PERFORMED, 
                    "Compute"
                    );

            this.lsnButton.actionPerformed(evtButtonPressed);
        }
    }
    
//    private class CsSolverThread extends Thread {
//        
//        
//        /*
//         * Local Attributes
//         */
//        
//        /** The Courant-Snyder solver engine */
//        private CourantSnyderEstimator      cseSolver;
//        
//        
//        /** The identifier of the SMF device where the CS parameters are reconstructed */
//        private String      strDevIdRecon; 
//        
//        /** The beam bunch arrival frequency */
//        private double      dblBnchFreq;
//        
//        /** The beam current */
//        private double      dblBmCurr;
//        
//        /** The array of profile measurements */
//        private ArrayList<Measurement>  arrMsmts;
//        
//        
//        /*
//         * Initialization
//         */
//        
//        /**
//         * Constructor for CsSolverThread.
//         *
//         * @param cseSolver         the Courant-Snyder parameter solver engine
//         * @param strDevIdRecon     ID of device where CS parameters are reconstructed
//         * @param dblBnchFreq       bunch arrival frequency (Hz)
//         * @param dblBmCurr         beam current (A)
//         * @param arrMsmts          array of profile measurements
//         *
//         * @author Christopher K. Allen
//         * @since  Oct 22, 2014
//         */
//        public CsSolverThread(CourantSnyderEstimator cseSolver, String strDevIdRecon, double dblBnchFreq, double dblBmCurr, ArrayList<Measurement> arrMsmts) {
//            this.cseSolver     = cseSolver;
//            this.strDevIdRecon = strDevIdRecon;
//            this.dblBnchFreq   = dblBnchFreq;
//            this.dblBmCurr     = dblBmCurr;
//            this.arrMsmts      = arrMsmts;
//        }
//        
//        /**
//         * Launches the Courant-Snyder solver on the profile problem defined in the 
//         * constructor 
//         *
//         * @see java.lang.Thread#run()
//         *
//         * @author Christopher K. Allen
//         * @since  Oct 22, 2014
//         */
//        public void run() {
//            try {
//
//                this.cseSolver.computeReconstruction(this.strDevIdRecon, this.dblBnchFreq, this.dblBmCurr, this.arrMsmts);
//
//            } catch (Exception e) {
//
//                MainApplication.getEventLogger().logError(this.getClass(), "Courant-Snyder estimation failure - general exception");;
//                e.printStackTrace();
//
//            } 
//        }
//    }

    /**
     * GUI panel that displays the current progress of a <code>CsFixedPtEstimator</code>
     * object while searching for a solution.
     *
     * @author Christopher K. Allen
     * @since  Oct 2, 2014
     */
    private class ProgressUpdatePanel extends JPanel implements CsFixedPtEstimator.IProgressListener {


        /*
         * Global Constants
         */

        /** Serialization version number */
        private static final long serialVersionUID = 1L;


        /*
         * Local Attributes
         */
        /** The current iteration count */
        private NumberTextField             txtCurIter;

        /** The current residual error */
        private NumberTextField             txtCurErr;

        /** The current alpha tuning parameter */
        private NumberTextField             txtCurAlpha;


        /** A progress bar showing convergence to tolerable error level */
        private JProgressBar                barCurErr;


        /*
         * Initialization
         */

        /**
         * Constructor for ProgressUpdatePanel.  Builds all the GUI components
         * and lays them out on the <code>JPanel</code> base.
         *
         * @author Christopher K. Allen
         * @since  Oct 2, 2014
         */
        public ProgressUpdatePanel() {
            super();

            this.guiBuildComponents();
            this.guiLayoutComponents();
        }

        /**
         * <p>
         * Initializes the progress bar for displaying the residual error
         * in the current solution iterate.  Nothing else to do at this point.
         * </p>
         * <p>
         * <h4>NOTE</h4>
         * &middot; This method should be called just before the fixed point Courant-Snyder
         * algorithm is started.
         * </p>
         *
         * @author Christopher K. Allen
         * @since  Oct 3, 2014
         */
        public void initializeDisplay() {

            // Clear the progress text fields
            this.txtCurAlpha.clearDisplay();
            this.txtCurErr.clearDisplay();
            this.txtCurIter.clearDisplay();
            
            // Initialize the progress bar
            Double  dblMaxErr = AppProperties.NUMERIC.CSFP_MAXERROR.getValue().asDouble();
            double  dblLogMax = Math.log10( dblMaxErr );
            int     intLogMax = (int)Math.floor( dblLogMax );

            this.barCurErr.setMinimum(0);
            this.barCurErr.setValue(0);
            this.barCurErr.setMaximum(-intLogMax);
        }

        /*
         * IProgressListener Interface
         */

        /**
         * Updates the GUI display with the current iteration count, residual error,
         * and &alpha; parameter.
         *
         * @see xal.extension.twissobserver.CsFixedPtEstimator.IProgressListener#iterationUpdate(int, double, double)
         *
         * @author Christopher K. Allen
         * @since  Oct 2, 2014
         */
        @Override
        public void iterationUpdate(int cntIter, double dblAlpha, double dblError) {
            
            // Skip if this iteration is not a multiple of the display frequency
            if (cntIter % INT_FREQ_DISPL != 0)
                return;
            
            // Set the values in the GUI
            this.txtCurIter.setDisplayValue(cntIter);
            this.txtCurErr.setDisplayValue(dblError);
            this.txtCurAlpha.setDisplayValue(dblAlpha);

            double  dblLogErr = Math.log10(dblError);
            int     intLogErr = (int) Math.floor(dblLogErr);
            this.barCurErr.setValue(-intLogErr);
            
//            this.repaint();
        }

        /**
         * Nothing to do here.
         *
         * @see xal.extension.twissobserver.CsFixedPtEstimator.IProgressListener#searchComplete(xal.tools.beam.CovarianceMatrix)
         *
         * @author Christopher K. Allen
         * @since  Oct 23, 2014
         */
        @Override
        public void searchComplete(CovarianceMatrix matRecon) {
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
            this.txtCurIter  = new NumberTextField(FMT.INT);
            this.txtCurIter.setEditable(false);
            this.txtCurIter.setBackground(Color.GRAY);

            this.txtCurErr   = new NumberTextField(FMT.SCI_3);
            this.txtCurErr.setEditable(false);
            this.txtCurErr.setBackground(Color.GRAY);

            this.txtCurAlpha = new NumberTextField(FMT.DEC_3);
            this.txtCurAlpha.setEditable(false);
            this.txtCurAlpha.setBackground(Color.GRAY);

            this.barCurErr = new JProgressBar(JProgressBar.HORIZONTAL);
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

            // The Title
            //                String      strUrlIcon = AppProperties.ICON.CMP_TORUS.getValue().asString();
            //                ImageIcon   imgTitle = PtaResourceManager.getImageIcon(strUrlIcon);
            //                JLabel      lblTitle = new JLabel("Algorithm Progress", imgTitle, SwingConstants.CENTER);
            JLabel      lblTitle = new JLabel("Algorithm Progress");
            lblTitle.setFont( lblTitle.getFont().deriveFont(Font.BOLD) );

            gbcLayout.gridx = 0;
            gbcLayout.gridy = 0;
            gbcLayout.gridwidth  = 3;
            gbcLayout.gridheight = 1;
            gbcLayout.fill    = GridBagConstraints.HORIZONTAL;
            gbcLayout.weightx = 0.0;
            gbcLayout.weighty = 0.0;
            gbcLayout.anchor = GridBagConstraints.CENTER;
            this.add( lblTitle, gbcLayout );

            // The progress bar
            gbcLayout.gridx = 0;
            gbcLayout.gridy++;
            gbcLayout.gridwidth  = 1;
            gbcLayout.gridheight = 1;
            gbcLayout.fill    = GridBagConstraints.NONE;
            gbcLayout.weightx = 0.0;
            gbcLayout.weighty = 0.0;
            gbcLayout.anchor = GridBagConstraints.LINE_END;
            this.add( new JLabel("Covergence "), gbcLayout );

            gbcLayout.gridx = 1;
            gbcLayout.gridwidth  = 2;
            gbcLayout.gridheight = 1;
            gbcLayout.fill    = GridBagConstraints.BOTH;
            gbcLayout.weightx = 0.0;
            gbcLayout.weighty = 0.0;
            gbcLayout.anchor = GridBagConstraints.CENTER;
            this.add( this.barCurErr, gbcLayout );

            // The progress parameters labels row
            gbcLayout.gridy++;
            gbcLayout.gridwidth  = 1;
            gbcLayout.gridheight = 1;
            gbcLayout.fill    = GridBagConstraints.NONE;
            gbcLayout.weightx = 0.0;
            gbcLayout.weighty = 0.0;
            gbcLayout.anchor = GridBagConstraints.LINE_START;

            gbcLayout.gridx = 0;
            this.add( new JLabel("Iteration  "), gbcLayout );

            gbcLayout.gridx = 1;
            this.add( new JLabel(" Conv. Error "), gbcLayout );

            gbcLayout.gridx = 2;
            this.add( new JLabel("  Alpha Value"), gbcLayout );


            // The progress parameters themselves
            gbcLayout.gridy++;
            gbcLayout.gridwidth  = 1;
            gbcLayout.gridheight = 1;
            gbcLayout.fill    = GridBagConstraints.BOTH;
            gbcLayout.weightx = 0.0;
            gbcLayout.weighty = 0.0;
            gbcLayout.anchor = GridBagConstraints.LINE_START;

            gbcLayout.gridx = 0;
            this.add( this.txtCurIter, gbcLayout );

            gbcLayout.gridx = 1;
            this.add( this.txtCurErr, gbcLayout );

            gbcLayout.gridx = 2;
            this.add( this.txtCurAlpha, gbcLayout );
        }
    }

    /*
     * Local Attributes
     */

    //
    // GUI Components
    //

    /** The beam current */
    private NumberTextField             txtBmCurr;

    /** The bunch arrival frequency */
    private NumberTextField             txtBnchFreq;
    
    /** Switch to turn on/off space charge effects during computation */
    private JCheckBox                   chkScheff;


    /** The maximum number of fixed point iterations */
    private NumberTextField             txtMaxIter;

    /** The maximum error tolerance */
    private NumberTextField             txtMaxErr;

    /** Fixed point tuning parameter */
    private NumberTextField             txtFpAlpha;


    /** The solution progress panel */
    private ProgressUpdatePanel         pnlSolnProg;


    /** User action for reconstructing the Courant-Snyder parameters */
    private JButton                     butCmp;


    //
    // Event Response
    //

    /** List of objects monitoring requests for computation (the button event) */
    private final List<ActionListener>  lstCmpLsrs;


    /*
     * Initialization
     */

    /**
     * Constructor for CsFixedPtControlPanel.
     *
     *
     * @author Christopher K. Allen
     * @since  Sep 30, 2014
     */
    public CsFixedPtControlPanel() {
        super();

        this.lstCmpLsrs = new LinkedList<>();

        this.guiBuildComponents();
        this.guiBuildActions();
        this.guiLayoutComponents();
        this.guiInitialize();
    }


    /*
     * Data Query
     */
    
    /**
     * Returns the value of bunch arrival frequency as displayed in the GUI.
     * 
     * @return      beam bunch arrival frequency (in Hz)
     *
     * @author Christopher K. Allen
     * @since  Oct 3, 2014
     */
    public double   getBunchFreq() {

        Double      dblBnchFreq = this.txtBnchFreq.getDisplayValue().doubleValue();

        return dblBnchFreq;
    }

    /**
     * Returns the value of beam current display in the GUI.
     * 
     * @return      beam current in Amperes
     *
     * @author Christopher K. Allen
     * @since  Oct 3, 2014
     */
    public double   getBeamCurrent() {

        Double      dblBmCurr = this.txtBmCurr.getDisplayValue().doubleValue();

        return dblBmCurr;
    }

    
    /*
     * Operations
     */

    /**
     * Add the given listener to the list of listeners monitoring the
     * "Compute" button event.
     * 
     * @param lsnCmpBut     new compute button event listener
     *
     * @author Christopher K. Allen
     * @since  Oct 3, 2014
     */
    public void addComputeButtonListener(ActionListener lsnCmpBut) {
        this.lstCmpLsrs.add(lsnCmpBut);
    }

    /**
     * Clears out all data in the GUI display.
     * 
     * @since  Apr 27, 2010
     * @author Christopher K. Allen
     */
    public void clearAll() {
        this.pnlSolnProg.initializeDisplay();
    }

    /**
     * Estimates the Courant-Snyder parameters, or more specifically the 
     * covariance matrix, at the requested reconstruction location given the
     * profile devices to use, the accelerator sector containing the 
     * measurements, and set of profile measurements for all devices involved.
     * Note that the list of devices used in the reconstruction can be less the
     * the number of profile data sets. 
     * 
     * @param strDevIdRecon device ID where the Courant-Snyder parameters are computed
     * @param lstDevIdMsmts devices whose data is used to compute the parameters
     * @param smfSeqRecon   accelerator sector that contains all measurement and reconstruction locations
     * @param setMsmts      set of all profile measures for the problem
     * 
     * @return              covariance matrix at reconstruction location computed from data
     * 
     * @throws Exception    either the model failed or the algorithm failed to converge 
     * 
     * @author Christopher K. Allen
     * @since  Oct 7, 2014
     */
//    public CovarianceMatrix   estimateCovariance(
//            String strDevIdRecon,
//            ArrayList<Measurement> arrMsmts,
//            AcceleratorSeq smfSeqRecon,
//            long lngPvLogId
//            ) throws ModelException, ConvergenceException, Exception 
//    {
        public void estimateCovariance(CourantSnyderView.SolutionSet setSoln) throws ModelException, ConvergenceException, Exception {
            
        this.simulateBunchLengths(setSoln.smfSeqRecon, setSoln.lngPvLogId, setSoln.arrMsmts);

        // Get the beam parameters
        Double      dblBnchFreq = this.txtBnchFreq.getDisplayValue().doubleValue();
        Double      dblBmCurr   = this.txtBmCurr.getDisplayValue().doubleValue();

        
        // Create the CS parameter estimator and attach it to the
        //  progress update panel so the user can see the results
        CourantSnyderEstimator  cseRecon = this.createEstimator(setSoln.smfSeqRecon, setSoln.lngPvLogId);

        this.pnlSolnProg.initializeDisplay();
        
        String              strDevRecId = setSoln.smfDevRecon.getId();
        CovarianceMatrix    matRecon    = cseRecon.computeReconstruction(strDevRecId, dblBnchFreq, dblBmCurr, setSoln.arrMsmts);
        
        setSoln.matRecon = matRecon;
    }

    /**
     * Creates and returns an envelope probe for the purpose of displaying the resulting
     * online model simulation of the Courant-Snyder solutions.  We need to create it here
     * because this panel keeps the space charge effects.
     * 
     * @param smfSeqRecon   accelerator sequence being simulated
     * @param matCovRecon   the initial covariance matrix to use for the probe
     * 
     * @return              a new envelope probe object, or <code>null</code> if the
     *                      algorithm factory or probe factory failed to instantiate
     *
     * @author Christopher K. Allen
     * @since  Oct 22, 2014
     */
    public EnvelopeProbe   createEnvelopeProbe(AcceleratorSeq smfSeqRecon, CovarianceMatrix matCovRecon) {
        
        double  dblBmCurr   = this.txtBmCurr.getDisplayValue().doubleValue();
        double  dblBnchFreq = this.txtBnchFreq.getDisplayValue().doubleValue();
        try {
            EnvTrackerAdapt algRecon = AlgorithmFactory.createEnvTrackerAdapt(smfSeqRecon);
            EnvelopeProbe   prbRecon = ProbeFactory.getEnvelopeProbe(smfSeqRecon, algRecon);
            
            if (this.chkScheff.isSelected())
                prbRecon.setBeamCurrent(dblBmCurr);
            else
                prbRecon.setBeamCurrent(0);
            
            prbRecon.setBunchFrequency(dblBnchFreq);
            prbRecon.setCovariance(matCovRecon);
            
            return prbRecon;
    
        } catch (InstantiationException e) {
            MainApplication.getEventLogger().logWarning(this.getClass(), "Unable to create envelope probe for C-S solution");
            e.printStackTrace();
            
            return null;
        }   
    }


    /*
     * Courant-Snyder Parameter Support Methods
     */

    /**
     * This method creates and returns a new <code>CsFixedPtEstimator</code> 
     * object using
     * the given transfer matrix generation engine and the numeric parameters
     * provided by the user (i.e., taken from the GUI).  The transfer matrix
     * generator is synchronized to the given PV Logger ID.
     *  
     * @param   smfSeqRecon accelerator sequence where measurements and reconstruction location lie
     * @param   lngPvLogId  PV logger snapshot ID of the machine state during the measurements
     * 
     * @return              a new Courant-Snyder estimation engine which using the fixed-point method
     *
     * @throws ModelException an error occurred while instantiating the machine model

     * @author Christopher K. Allen
     * @since  Oct 1, 2014
     */
    private CourantSnyderEstimator   createEstimator(AcceleratorSeq smfSeqRecon, long lngPvLogId) throws ModelException {

        // First create the transfer matrix generator for the Courant-Snyder parameter estimator
        TransferMatrixGenerator trxRecon    = new TransferMatrixGenerator(smfSeqRecon, lngPvLogId);

        // Retrieve the parameters needed for the estimator from the GUI then create
        //  the estimator with the above transfer matrix generator
        Integer     intMaxIter = this.txtMaxIter.getDisplayValue().intValue();
        Double      dblMaxErr  = this.txtMaxErr.getDisplayValue().doubleValue();
        Double      dblFpAlpha = this.txtFpAlpha.getDisplayValue().doubleValue();

        if (this.chkScheff.isSelected()) {  // include space charge effects
            CsFixedPtEstimator  cseFxdPt = new CsFixedPtEstimator(intMaxIter, dblMaxErr, dblFpAlpha, trxRecon);
            cseFxdPt.addProgressListener(this.pnlSolnProg);
//            cseFxdPt.setDebug(true);

            return cseFxdPt;
            
        } else {    // ignore space charge effects
            
            CsZeroCurrentEstimator  cseZeroCurr = new CsZeroCurrentEstimator(trxRecon);
//          cseZeroCurr.setDebug(true);

          return cseZeroCurr;
        }
    }
    
    

//    /**
//     * This method creates and returns a new <code>ZeroCurrentEstimator</code> 
//     * object using by creating a transfer matrix generator for the given
//     * accelerator sequence and synchronized to the given PV Logger ID.
//     *  
//     * @param   smfSeqRecon accelerator sequence where measurements and reconstruction location lie
//     * @param   lngPvLogId  PV logger snapshot ID of the machine state during the measurements
//     * 
//     * @return              a new Courant-Snyder estimation engine which using the fixed-point method
//     *
//     * @throws ModelException an error occurred while instantiating the machine model
//
//     * @author Christopher K. Allen
//     * @since  Oct 1, 2014
//     */
//    private CsZeroCurrentEstimator   createZeroCurrentEstimator(AcceleratorSeq smfSeqRecon, long lngPvLogId) throws ModelException {
//
//        // First create the transfer matrix generator for the Courant-Snyder parameter estimator
//        TransferMatrixGenerator trxRecon    = new TransferMatrixGenerator(smfSeqRecon, lngPvLogId);
//
//        // Create the estimator and return
//        CsZeroCurrentEstimator  cseZeroCurr = new CsZeroCurrentEstimator(trxRecon);
////        cseZeroCurr.setDebug(true);
//
//        return cseZeroCurr;
//    }

    /**
     * Approximates the RMS bunch lengths for the given measurement data by simulating
     * beam propagation using the bunch frequency and beam current taken from the
     * GUI text fields.  The bunch length fields of the given measurement data are
     * populated with these values.
     * 
     * @param blsSeqRecon   a bunch length simulator for the accelerator sector containing measurement locations
     * @param arrMsmts      RMS beam size measurement
     * 
     * @throws ModelException   general error - either model synchronization or simulation failed
     *
     * @author Christopher K. Allen
     * @since  Oct 7, 2014
     */
    private void simulateBunchLengths(AcceleratorSeq smfSeqRecon, long lngPvLogId, ArrayList<Measurement> arrMsmts) throws ModelException {
        BunchLengthSimulator    blsSeqRecon = new BunchLengthSimulator(smfSeqRecon, lngPvLogId);

        Double      dblBnchFreq = this.txtBnchFreq.getDisplayValue().doubleValue();
        Double      dblBmCurr   = this.txtBmCurr.getDisplayValue().doubleValue();

        blsSeqRecon.generateBunchLengths(arrMsmts, dblBnchFreq, dblBmCurr);
    }

    


    /*
     * GUI Support Methods
     */

    /**
     * Creates the individual GUI components.
     * 
     * @since  Dec 13, 2011
     * @author Christopher K. Allen
     */
    private void guiBuildComponents() {
        Double      dblBnchFreq = AppProperties.SIM.BNCHFREQ.getValue().asDouble();
        this.txtBnchFreq = new NumberTextField(FMT.ENGR_3, dblBnchFreq);

        Double      dblBmCurr = AppProperties.SIM.BMCURR.getValue().asDouble();
        this.txtBmCurr = new NumberTextField(FMT.ENGR_3, dblBmCurr);
        
//        String      strIcnScheff = AppProperties.ICON.TWS_SCHEFF.getValue().asString();
//        ImageIcon   icnScheff    = PtaResourceManager.getImageIcon(strIcnScheff);
//        this.chkScheff = new JCheckBox("Use Space Charge", icnScheff);
        this.chkScheff = new JCheckBox("Use Space Charge", true);
        this.chkScheff.setToolTipText("Include space charge effects when checked");


        Integer     intMaxIter = AppProperties.NUMERIC.CSFP_MAXITER.getValue().asInteger();
        this.txtMaxIter = new NumberTextField(FMT.INT, intMaxIter);

        Double      dblMaxErr = AppProperties.NUMERIC.CSFP_MAXERROR.getValue().asDouble();
        this.txtMaxErr = new NumberTextField(FMT.SCI_3, dblMaxErr);

        Double      dblAlpha = AppProperties.NUMERIC.CSFP_ALPHA.getValue().asDouble();
        this.txtFpAlpha = new NumberTextField(FMT.DEC_3, dblAlpha);

        this.pnlSolnProg = new ProgressUpdatePanel();

        //          String      strLocIcn = AppProperties.ICON.TWS_COMPUTE.getValue().asString();
        //          ImageIcon   icnRecon = PtaResourceManager.getImageIcon(strLocIcn);
//        this.butCmp = new JButton("Compute", icnRecon);
        String      strIcnScheff = AppProperties.ICON.TWS_SCHEFF.getValue().asString();
        ImageIcon   icnScheff    = PtaResourceManager.getImageIcon(strIcnScheff);
        this.butCmp = new JButton("Compute", icnScheff);
//      this.butCmp = new JButton("Compute");
    }

    /**
     * Define the actions of the interactive GUI components.
     *
     * @author Christopher K. Allen
     * @since  Sep 30, 2014
     */
    private void guiBuildActions() {

        // The bunch frequency has been changed 
        ActionListener actBnchFreq = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Number  nmbBnchFreq = CsFixedPtControlPanel.this.txtBnchFreq.getDisplayValue();
                String  strBnchFreq = nmbBnchFreq.toString();
                AppProperties.SIM.BNCHFREQ.getValue().set(strBnchFreq);
            }
        };
        this.txtBnchFreq.addActionListener(actBnchFreq);

        // The beam current value has changed
        ActionListener  actBmCurr = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Number  nmbBmCurr = CsFixedPtControlPanel.this.txtBmCurr.getDisplayValue();
                String  strBmCurr = nmbBmCurr.toString();
                AppProperties.SIM.BMCURR.getValue().set(strBmCurr);
            }
        };
        this.txtBmCurr.addActionListener(actBmCurr);

        // The maximum number of iterations has been changed
        ActionListener  actMaxIter = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Number nmbMaxIter = CsFixedPtControlPanel.this.txtMaxIter.getDisplayValue();
                String strMaxIter = nmbMaxIter.toString();
                AppProperties.NUMERIC.CSFP_MAXITER.getValue().set(strMaxIter);
            }
        };
        this.txtMaxIter.addActionListener(actMaxIter);

        // The maximum number of iterations has been changed
        ActionListener  actMaxErr = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Number nmbMaxErr = CsFixedPtControlPanel.this.txtMaxErr.getDisplayValue();
                String strMaxErr = nmbMaxErr.toString();
                AppProperties.NUMERIC.CSFP_MAXERROR.getValue().set(strMaxErr);
            }
        };
        this.txtMaxErr.addActionListener(actMaxErr);

        // The maximum number of iterations has been changed
        ActionListener  actFpAlpha = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Number nmbFpAlpha = CsFixedPtControlPanel.this.txtFpAlpha.getDisplayValue();
                String strFpAlpha = nmbFpAlpha.toString();
                AppProperties.NUMERIC.CSFP_MAXERROR.getValue().set(strFpAlpha);

                CsFixedPtControlPanel.this.pnlSolnProg.initializeDisplay();
            }
        };
        this.txtFpAlpha.addActionListener(actFpAlpha);

        // The compute CS parameters button
        ActionListener  actCompute = new ActionListener() {

            /** Fire an action event to all the listeners of the compute button event */
            @Override
            public void actionPerformed(ActionEvent e) {
                for (ActionListener lsn : CsFixedPtControlPanel.this.lstCmpLsrs) {
                    ButtonThread    thdBut = new ButtonThread(lsn);
                    
                    thdBut.start();
                }
//                    lsn.actionPerformed(
//                            new ActionEvent(
//                                    CsFixedPtControlPanel.this, 
//                                    ActionEvent.ACTION_PERFORMED, 
//                                    "Compute"
//                                    )
//                            );
//                
//                MainApplication.getApplicationDocument().displayConfirmDialog("Thread Notice", "Returning from compute button.");
            }
        };
        this.butCmp.addActionListener(actCompute);
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

        // The Title
        String      strUrlIcon = AppProperties.ICON.CMP_TORUS.getValue().asString();
        ImageIcon   imgTitle = PtaResourceManager.getImageIcon(strUrlIcon);
        JLabel      lblTitle = new JLabel("Courant-Snyder Fixed-Point Reconstruction", imgTitle, SwingConstants.CENTER);

        lblTitle.setFont( lblTitle.getFont().deriveFont(Font.BOLD) );
        gbcLayout.gridx = 0;
        gbcLayout.gridy = 0;
        gbcLayout.gridwidth  = 2;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.HORIZONTAL;
        gbcLayout.weightx = 0.0;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor = GridBagConstraints.CENTER;
        this.add( lblTitle, gbcLayout );

        // The bunch arrival frequency
        gbcLayout.gridx = 0;
        gbcLayout.gridy++;
        gbcLayout.gridwidth  = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.NONE;
        gbcLayout.weightx = 0.0;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor = GridBagConstraints.LINE_END;
        this.add( new JLabel("Bunch freq. (Hz)"), gbcLayout );

        gbcLayout.gridx = 1;
        gbcLayout.gridwidth  = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.HORIZONTAL;
        gbcLayout.weightx = 0.0;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        this.add( this.txtBnchFreq, gbcLayout );
        
        // The Use Space Charge check box
        gbcLayout.gridx = 0;
        gbcLayout.gridy++;
        gbcLayout.gridwidth  = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.HORIZONTAL;
        gbcLayout.weightx = 0.0;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        this.add( this.chkScheff, gbcLayout );

        // The beam current
        gbcLayout.gridx = 0;
        gbcLayout.gridwidth  = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.NONE;
        gbcLayout.weightx = 0.0;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor = GridBagConstraints.LINE_END;
        this.add( new JLabel("Beam current (A)"), gbcLayout );

        gbcLayout.gridx = 1;
        gbcLayout.gridwidth  = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.HORIZONTAL;
        gbcLayout.weightx = 0.0;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        this.add( this.txtBmCurr, gbcLayout );

        // A buffer
        gbcLayout.gridx = 0;
        gbcLayout.gridy++;
        gbcLayout.gridwidth  = 2;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.HORIZONTAL;
        gbcLayout.weightx = 0.0;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor = GridBagConstraints.CENTER;
        this.add( new JLabel(" "), gbcLayout );

        // The maximum iterations 
        gbcLayout.gridx = 0;
        gbcLayout.gridy++;
        gbcLayout.gridwidth  = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.NONE;
        gbcLayout.weightx = 0.0;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor = GridBagConstraints.LINE_END;
        this.add( new JLabel("Max. Iterations "), gbcLayout );

        gbcLayout.gridx = 1;
        gbcLayout.gridwidth  = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.HORIZONTAL;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        this.add( this.txtMaxIter, gbcLayout );

        // The maximum error tolerance
        gbcLayout.gridx = 0;
        gbcLayout.gridy++;
        gbcLayout.gridwidth  = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.NONE;
        gbcLayout.weightx = 0.0;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor = GridBagConstraints.LINE_END;
        this.add( new JLabel("Error Toler."), gbcLayout );

        gbcLayout.gridx = 1;
        gbcLayout.gridwidth  = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.HORIZONTAL;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        this.add( this.txtMaxErr, gbcLayout );

        // The alpha tuning parameter
        gbcLayout.gridx = 0;
        gbcLayout.gridy++;
        gbcLayout.gridwidth  = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.NONE;
        gbcLayout.weightx = 0.0;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor = GridBagConstraints.LINE_END;
        this.add( new JLabel("Initial Alpha"), gbcLayout );

        gbcLayout.gridx = 1;
        gbcLayout.gridwidth  = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.HORIZONTAL;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        this.add( this.txtFpAlpha, gbcLayout );

        // A buffer
        gbcLayout.gridx = 0;
        gbcLayout.gridy++;
        gbcLayout.gridwidth  = 2;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.HORIZONTAL;
        gbcLayout.weightx = 0.0;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor = GridBagConstraints.CENTER;
        this.add( new JLabel(" "), gbcLayout );

        // The progress update panel
        gbcLayout.gridx = 0;
        gbcLayout.gridy++;
        gbcLayout.gridwidth  = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.BOTH;
        gbcLayout.weightx = 0.0;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor = GridBagConstraints.CENTER;
        this.add( this.pnlSolnProg, gbcLayout );

        // Compute button
        gbcLayout.gridx = 1;
        gbcLayout.gridwidth  = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.HORIZONTAL;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor = GridBagConstraints.LINE_END;
        this.add( this.butCmp, gbcLayout );

    }

    /**
     * Initializes the GUI to the current
     * measurement data (if there is any).
     * 
     * @since  Apr 23, 2010
     * @author Christopher K. Allen
     */
    private void guiInitialize() {

    }

}