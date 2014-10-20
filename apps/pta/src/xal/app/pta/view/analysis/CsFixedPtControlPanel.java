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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import xal.app.pta.daq.MeasurementData;
import xal.app.pta.rscmgt.AppProperties;
import xal.app.pta.rscmgt.PtaResourceManager;
import xal.app.pta.tools.swing.NumberTextField;
import xal.app.pta.tools.swing.NumberTextField.FMT;
import xal.extension.twissobserver.BunchLengthSimulator;
import xal.extension.twissobserver.ConvergenceException;
import xal.extension.twissobserver.CsFixedPtEstimator;
import xal.extension.twissobserver.Measurement;
import xal.extension.twissobserver.TransferMatrixGenerator;
import xal.model.ModelException;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.profile.ProfileDevice.IProfileData;
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


    /*
     * Inner Classes
     */

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

            Double  dblMaxErr = AppProperties.NUMERIC.CSFP_MAXERROR.getValue().asDouble();
            double  dblLogMax = Math.log10( dblMaxErr );
            int     intLogMax = (int)Math.floor( dblLogMax );

            this.barCurErr.setMinimum(0);
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
        public void iterationUpdate(int cntIter, double dblError, double dblAlpha) {
            this.txtCurIter.setDisplayValue(cntIter);
            this.txtCurErr.setDisplayValue(dblError);
            this.txtCurAlpha.setDisplayValue(dblAlpha);

            double  dblLogErr = Math.log10(dblError);
            int     intLogErr = (int) Math.floor(dblLogErr);
            this.barCurErr.setValue(-intLogErr);
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
            this.add( new JLabel("Rel. Error "), gbcLayout );

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
            this.add( new JLabel(" Residual Error "), gbcLayout );

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

    //        /**
    //         * Attach this GUI panel to the given fixed point Courant-Snyder
    //         * parameter estimator.  Specifically, the CS parameter estimator
    //         * sends its progress information to the <code>ProgressUpdatePanel</code>
    //         * contained in this GUI.
    //         * 
    //         * @param cseRecon  the CS estimator producing progress information
    //         *
    //         * @author Christopher K. Allen
    //         * @since  Oct 7, 2014
    //         */
    //        public void attachToCsEstimator(CsFixedPtEstimator cseRecon) {
    //            cseRecon.addProgressListener(this.pnlSolnProg);
    //        }

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
     * Returns the value of beam curren display in the GUI.
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
     * @throws ModelException       an error occurred when creating the online model or during simulation
     * @throws ConvergenceException the Courant-Snyder reconstruction algorithm failed to converge
     *
     * @author Christopher K. Allen
     * @since  Oct 7, 2014
     */
    public CovarianceMatrix   estimateCovariance(
            String strDevIdRecon,
            List<String> lstDevIdMsmts,
            AcceleratorSeq smfSeqRecon,
            MeasurementData setMsmts
            ) throws ModelException, ConvergenceException 
    {
        // Retrieve the PV Logger ID of the machine state at the time of measurement
        long    lngPvLogId = setMsmts.getPvLoggerId();

        // Get the RMS envelope sizes from the measurement data and pack 
        //  into Measurement data structures
        ArrayList<Measurement>  arrMsmts = this.processMeasurementData(lstDevIdMsmts, setMsmts);

        // Simulate the longitudinal RMS values within the data set 
        //  in order to approximate space charge effects
        this.simulateBunchLengths(smfSeqRecon, lngPvLogId, arrMsmts);

        // Create the CS parameter estimator and attach it to the
        //  progress update panel so the user can see the results
        CsFixedPtEstimator  cseRecon = this.createEstimator(smfSeqRecon, lngPvLogId);

        cseRecon.addProgressListener(this.pnlSolnProg);

        // Solve for the Courant-Snyder parameters
        Double      dblBnchFreq = this.txtBnchFreq.getDisplayValue().doubleValue();
        Double      dblBmCurr   = this.txtBmCurr.getDisplayValue().doubleValue();

        CovarianceMatrix    matRecon  = cseRecon.computeReconstruction(strDevIdRecon, dblBnchFreq, dblBmCurr, arrMsmts);

        return matRecon;
    }

    /*
     * Courant-Snyder Parameter Support Methods
     */

    /**
     * This method creates and returns a new <code>CsFixedPtEstimator</code> 
     * object using
     * the given transfer matrix generation engine and the numeric parameters
     * provided by the user (i.e., taken from the GUI).
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
    private CsFixedPtEstimator   createEstimator(AcceleratorSeq smfSeqRecon, long lngPvLogId) throws ModelException {

        // First create the transfer matrix generator for the Courant-Snyder parameter estimator
        TransferMatrixGenerator trxRecon    = new TransferMatrixGenerator(smfSeqRecon, lngPvLogId);

        // Retrieve the parameters needed for the estimator from the GUI then create
        //  the estimator with the above transfer matrix generator
        Integer     intMaxIter = this.txtMaxIter.getDisplayValue().intValue();
        Double      dblMaxErr  = this.txtMaxErr.getDisplayValue().doubleValue();
        Double      dblFpAlpha = this.txtFpAlpha.getDisplayValue().doubleValue();

        CsFixedPtEstimator  cseFxdPt = new CsFixedPtEstimator(intMaxIter, dblMaxErr, dblFpAlpha, trxRecon);
        cseFxdPt.setDebug(true);

        return cseFxdPt;
    }

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
    private ArrayList<Measurement>  processMeasurementData(List<String> lstDevIdMsmts, MeasurementData setMsmts) {

        // Create the array for measurement data and get the measurement scaling factor
        double                  dblScaleStd = AppProperties.MSMT.SCALE_STD_WIRE.getValue().asDouble();
        ArrayList<Measurement>  lstMsmts    = new ArrayList<>();
        
        for (String strDevId : lstDevIdMsmts) {

            IProfileData    datMsmt = setMsmts.getDataForDeviceId(strDevId);

            Measurement msmt = new Measurement();
            msmt.strDevId  = strDevId;
            msmt.dblSigHor = datMsmt.getDataAttrs().hor.stdev * dblScaleStd;
            msmt.dblSigVer = datMsmt.getDataAttrs().ver.stdev * dblScaleStd;
            msmt.dblSigLng = 0.0;

            lstMsmts.add(msmt);
        }

        return lstMsmts;
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


        Integer     intMaxIter = AppProperties.NUMERIC.CSFP_MAXITER.getValue().asInteger();
        this.txtMaxIter = new NumberTextField(FMT.INT, intMaxIter);

        Double      dblMaxErr = AppProperties.NUMERIC.CSFP_MAXERROR.getValue().asDouble();
        this.txtMaxErr = new NumberTextField(FMT.SCI_3, dblMaxErr);

        Double      dblAlpha = AppProperties.NUMERIC.CSFP_ALPHA.getValue().asDouble();
        this.txtFpAlpha = new NumberTextField(FMT.DEC_3, dblAlpha);

        this.pnlSolnProg = new ProgressUpdatePanel();

        //          String      strLocIcn = AppProperties.ICON.TWS_COMPUTE.getValue().asString();
        //          ImageIcon   icnRecon = PtaResourceManager.getImageIcon(strLocIcn);
        //          this.butCmp = new JButton("Compute", icnRecon);
        this.butCmp = new JButton("Compute");
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
                for (ActionListener lsn : CsFixedPtControlPanel.this.lstCmpLsrs)
                    lsn.actionPerformed(
                            new ActionEvent(
                                    CsFixedPtControlPanel.this, 
                                    ActionEvent.ACTION_PERFORMED, 
                                    "Compute"
                                    )
                            );
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

        // The beam current
        gbcLayout.gridx = 0;
        gbcLayout.gridy++;
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
        this.add( new JLabel("Maximum Error"), gbcLayout );

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

    /**
     * Clears out all data in the GUI display.
     * 
     * @since  Apr 27, 2010
     * @author Christopher K. Allen
     */
    private void clearAll() {

    }

}