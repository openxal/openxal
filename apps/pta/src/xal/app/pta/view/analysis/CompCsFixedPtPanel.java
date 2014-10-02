/**
 * CompCsFixedPtPanel.java
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

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import xal.app.pta.rscmgt.AppProperties;
import xal.app.pta.rscmgt.PtaResourceManager;
import xal.app.pta.tools.swing.NumberTextField;
import xal.app.pta.tools.swing.NumberTextField.FMT;
import xal.extension.twissobserver.CsFixedPtEstimator;
import xal.extension.twissobserver.TransferMatrixGenerator;

/**
     * GUI class manages the user interaction for computing Courant-Snyder parameters
     * from profile measurement data. This class is responsible for initiating the
     * calculations and grabbing some numerical parameters input from the user.
     * The actually calculations are done elsewhere.  In particular, the actions
     * here are delegated to class <code>CompCourantSnyderView</code>. 
     *
     *
     * @author Christopher K. Allen
     * @since  Sep 30, 2014
     */
    public class CompCsFixedPtPanel extends JPanel {
        
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
                String      strUrlIcon = AppProperties.ICON.CMP_PROGRESS.getValue().asString();
                ImageIcon   imgTitle = PtaResourceManager.getImageIcon(strUrlIcon);
                JLabel      lblTitle = new JLabel("Algorithm Progress", imgTitle, SwingConstants.CENTER);
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
                
                // The progress parameters labels row
                gbcLayout.gridy = 1;
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
                gbcLayout.gridy = 2;
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
        
        
        /*
         * Initialization
         */
        
        /**
         * Constructor for CompCsFixedPtPanel.
         *
         *
         * @author Christopher K. Allen
         * @since  Sep 30, 2014
         */
        public CompCsFixedPtPanel() {
            super();
            
            this.guiBuildComponents();
            this.guiBuildActions();
            this.guiLayoutComponents();
            this.guiInitialize();
        }
        
        
        /*
         * Operations
         */
        
        /**
         * This method creates and returns a new <code>CsFixedPtEstimator</code> 
         * object using
         * the given transfer matrix generation engine and the numeric parameters
         * provided by the user (i.e., taken from the GUI).
         *  
         * @param tmgAccelSeq   a transfer matrix generator for Courant-Snyder estimator engines

         * @return              a new Courant-Snyder estimation engine which using the fixed-point method
         *
         * @author Christopher K. Allen
         * @since  Oct 1, 2014
         */
        public CsFixedPtEstimator   createEstimator(TransferMatrixGenerator tmgAccelSeq) {
            Integer     intMaxIter = this.txtMaxIter.getDisplayValue().intValue();
            Double      dblMaxErr  = this.txtMaxErr.getDisplayValue().doubleValue();
            Double      dblFpAlpha = this.txtFpAlpha.getDisplayValue().doubleValue();
            
            CsFixedPtEstimator  cseFxdPt = new CsFixedPtEstimator(intMaxIter, dblMaxErr, dblFpAlpha, tmgAccelSeq);
            
            return cseFxdPt;
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
            Double      dblBnchFreq = AppProperties.SIM.BNCHFREQ.getValue().asDouble();
            this.txtBnchFreq = new NumberTextField(FMT.ENGR_3, dblBnchFreq);
            
            Double      dblBmCurr = AppProperties.SIM.BMCURR.getValue().asDouble();
            this.txtBmCurr = new NumberTextField(FMT.ENGR_3, dblBmCurr);
            
            
            Integer     intMaxIter = AppProperties.NUMERIC.CSFP_MAXITER.getValue().asInteger();
            this.txtMaxIter = new NumberTextField(FMT.INT, intMaxIter);
            
            Double      dblMaxErr = AppProperties.NUMERIC.CSFP_MAXERROR.getValue().asDouble();
            this.txtMaxErr = new NumberTextField(FMT.ENGR_3, dblMaxErr);
            
            Double      dblAlpha = AppProperties.NUMERIC.CSFP_ALPHA.getValue().asDouble();
            this.txtFpAlpha = new NumberTextField(FMT.DEC_3, dblAlpha);
            
            this.pnlSolnProg = new ProgressUpdatePanel();
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
                    Number  nmbBnchFreq = CompCsFixedPtPanel.this.txtBnchFreq.getDisplayValue();
                    String  strBnchFreq = nmbBnchFreq.toString();
                    AppProperties.SIM.BNCHFREQ.getValue().set(strBnchFreq);
                }
            };
            this.txtBnchFreq.addActionListener(actBnchFreq);
            
            // The beam current value has changed
            ActionListener  actBmCurr = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    Number  nmbBmCurr = CompCsFixedPtPanel.this.txtBmCurr.getDisplayValue();
                    String  strBmCurr = nmbBmCurr.toString();
                    AppProperties.SIM.BMCURR.getValue().set(strBmCurr);
                }
            };
            this.txtBmCurr.addActionListener(actBmCurr);
            
            // The maximum number of iterations has been changed
            ActionListener  actMaxIter = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    Number nmbMaxIter = CompCsFixedPtPanel.this.txtMaxIter.getDisplayValue();
                    String strMaxIter = nmbMaxIter.toString();
                    AppProperties.NUMERIC.CSFP_MAXITER.getValue().set(strMaxIter);
                }
            };
            this.txtMaxIter.addActionListener(actMaxIter);
            
            // The maximum number of iterations has been changed
            ActionListener  actMaxErr = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    Number nmbMaxErr = CompCsFixedPtPanel.this.txtMaxErr.getDisplayValue();
                    String strMaxErr = nmbMaxErr.toString();
                    AppProperties.NUMERIC.CSFP_MAXERROR.getValue().set(strMaxErr);
                }
            };
            this.txtMaxErr.addActionListener(actMaxErr);
            
            // The maximum number of iterations has been changed
            ActionListener  actFpAlpha = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    Number nmbFpAlpha = CompCsFixedPtPanel.this.txtFpAlpha.getDisplayValue();
                    String strFpAlpha = nmbFpAlpha.toString();
                    AppProperties.NUMERIC.CSFP_MAXERROR.getValue().set(strFpAlpha);
                }
            };
            this.txtFpAlpha.addActionListener(actFpAlpha);
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
            String      strUrlIcon = AppProperties.ICON.CMP_TWISS.getValue().asString();
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
            gbcLayout.gridy = 1;
            gbcLayout.gridwidth  = 1;
            gbcLayout.gridheight = 1;
            gbcLayout.fill    = GridBagConstraints.NONE;
            gbcLayout.weightx = 0.0;
            gbcLayout.weighty = 0.0;
            gbcLayout.anchor = GridBagConstraints.LINE_END;
            this.add( new JLabel("Bunch freq. (Hz)"), gbcLayout );

            gbcLayout.gridx = 1;
            gbcLayout.gridy = 1;
            gbcLayout.gridwidth  = 1;
            gbcLayout.gridheight = 1;
            gbcLayout.fill    = GridBagConstraints.HORIZONTAL;
            gbcLayout.weightx = 0.0;
            gbcLayout.weighty = 0.0;
            gbcLayout.anchor = GridBagConstraints.LINE_START;
            this.add( this.txtBnchFreq, gbcLayout );
            
            // The beam current
            gbcLayout.gridx = 0;
            gbcLayout.gridy = 2;
            gbcLayout.gridwidth  = 1;
            gbcLayout.gridheight = 1;
            gbcLayout.fill    = GridBagConstraints.NONE;
            gbcLayout.weightx = 0.0;
            gbcLayout.weighty = 0.0;
            gbcLayout.anchor = GridBagConstraints.LINE_END;
            this.add( new JLabel("Beam current (Amps)"), gbcLayout );

            gbcLayout.gridx = 1;
            gbcLayout.gridy = 2;
            gbcLayout.gridwidth  = 1;
            gbcLayout.gridheight = 1;
            gbcLayout.fill    = GridBagConstraints.HORIZONTAL;
            gbcLayout.weightx = 0.0;
            gbcLayout.weighty = 0.0;
            gbcLayout.anchor = GridBagConstraints.LINE_START;
            this.add( this.txtBmCurr, gbcLayout );
            
            // A buffer
            gbcLayout.gridx = 0;
            gbcLayout.gridy = 3;
            gbcLayout.gridwidth  = 2;
            gbcLayout.gridheight = 1;
            gbcLayout.fill    = GridBagConstraints.HORIZONTAL;
            gbcLayout.weightx = 0.0;
            gbcLayout.weighty = 0.0;
            gbcLayout.anchor = GridBagConstraints.CENTER;
            this.add( new JLabel(" "), gbcLayout );
            
            // The maximum iterations 
            gbcLayout.gridx = 0;
            gbcLayout.gridy = 4;
            gbcLayout.gridwidth  = 1;
            gbcLayout.gridheight = 1;
            gbcLayout.fill    = GridBagConstraints.NONE;
            gbcLayout.weightx = 0.0;
            gbcLayout.weighty = 0.0;
            gbcLayout.anchor = GridBagConstraints.LINE_END;
            this.add( new JLabel("Maximum Iterations "), gbcLayout );
            
            gbcLayout.gridx = 1;
            gbcLayout.gridy = 4;
            gbcLayout.gridwidth  = 1;
            gbcLayout.gridheight = 1;
            gbcLayout.fill    = GridBagConstraints.HORIZONTAL;
            gbcLayout.weightx = 0.1;
            gbcLayout.weighty = 0.0;
            gbcLayout.anchor = GridBagConstraints.LINE_START;
            this.add( this.txtMaxIter, gbcLayout );
            
            // The maximum error tolerance
            gbcLayout.gridx = 0;
            gbcLayout.gridy = 5;
            gbcLayout.gridwidth  = 1;
            gbcLayout.gridheight = 1;
            gbcLayout.fill    = GridBagConstraints.NONE;
            gbcLayout.weightx = 0.0;
            gbcLayout.weighty = 0.0;
            gbcLayout.anchor = GridBagConstraints.LINE_END;
            this.add( new JLabel("Maximum Error"), gbcLayout );
            
            gbcLayout.gridx = 1;
            gbcLayout.gridy = 5;
            gbcLayout.gridwidth  = 1;
            gbcLayout.gridheight = 1;
            gbcLayout.fill    = GridBagConstraints.HORIZONTAL;
            gbcLayout.weightx = 0.1;
            gbcLayout.weighty = 0.0;
            gbcLayout.anchor = GridBagConstraints.LINE_START;
            this.add( this.txtMaxErr, gbcLayout );
            
            // The alpha tuning parameter
            gbcLayout.gridx = 0;
            gbcLayout.gridy = 6;
            gbcLayout.gridwidth  = 1;
            gbcLayout.gridheight = 1;
            gbcLayout.fill    = GridBagConstraints.NONE;
            gbcLayout.weightx = 0.0;
            gbcLayout.weighty = 0.0;
            gbcLayout.anchor = GridBagConstraints.LINE_END;
            this.add( new JLabel("Tuning Parameter"), gbcLayout );

            gbcLayout.gridx = 1;
            gbcLayout.gridy = 6;
            gbcLayout.gridwidth  = 1;
            gbcLayout.gridheight = 1;
            gbcLayout.fill    = GridBagConstraints.HORIZONTAL;
            gbcLayout.weightx = 0.1;
            gbcLayout.weighty = 0.0;
            gbcLayout.anchor = GridBagConstraints.LINE_START;
            this.add( this.txtFpAlpha, gbcLayout );

            // A buffer
            gbcLayout.gridx = 0;
            gbcLayout.gridy = 7;
            gbcLayout.gridwidth  = 2;
            gbcLayout.gridheight = 1;
            gbcLayout.fill    = GridBagConstraints.HORIZONTAL;
            gbcLayout.weightx = 0.0;
            gbcLayout.weighty = 0.0;
            gbcLayout.anchor = GridBagConstraints.CENTER;
            this.add( new JLabel(" "), gbcLayout );
            
            // The progress update panel
            gbcLayout.gridx = 0;
            gbcLayout.gridy = 8;
            gbcLayout.gridwidth  = 2;
            gbcLayout.gridheight = 2;
            gbcLayout.fill    = GridBagConstraints.BOTH;
            gbcLayout.weightx = 0.0;
            gbcLayout.weighty = 0.0;
            gbcLayout.anchor = GridBagConstraints.CENTER;
            this.add( this.pnlSolnProg, gbcLayout );
            
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