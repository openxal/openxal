/**
 * Twiss3PlaneDisplayPanel.java
 *
 * Author  : Christopher K. Allen
 * Since   : Oct 2, 2014
 */
package xal.app.pta.view.analysis;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import xal.app.pta.tools.swing.NumberTextField;
import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.Twiss;


/**
 * Presents a panel display of Courant-Snyder parameters for all three
 * phase planes.  The parameters are grouped according to the
 * enclosing phase plane.
 *
 * @author Christopher K. Allen
 * @since  Oct 2, 2014
 */
public class Twiss3PlaneDisplayPanel extends JPanel {

    
    /*
     * Global Constants
     */
    
    /** Serialization version number */
    private static final long serialVersionUID = 1L;

    
    /*
     * Local Attributes
     */
    
    /** Courant-Snyder parameters for the horizontal plane */
    private TwissValuesDisplayPanel       pnlTwsHor;
    
    /** Courant-Snyder parameters for the vertical plane */
    private TwissValuesDisplayPanel       pnlTwsVer;
    
    /** Courant-Snyder parameters for the longitudinal plane */
    private TwissValuesDisplayPanel       pnlTwsLng;
    
    
    /*
     * Initialization
     */
    
    /**
     *  Creates a new panel for displaying Courant-Snyder parameters
     *  in all three phase planes.
     *
     * @author Christopher K. Allen
     * @since  Oct 2, 2014
     */
    public Twiss3PlaneDisplayPanel() {
        super();
        
        this.guiBuildComponents();
        this.guiLayoutComponents();
    }
    
    
    /**
     * Enables or disables the edit capabilities of the text fields
     * used to display the Courant-Snyder parameters.
     * 
     * @param bolEdit   enables text box editing if <code>true</code>,
     *                  disables if <code>false</code>
     *
     * @author Christopher K. Allen
     * @since  Oct 3, 2014
     */
    public void setEditable(boolean bolEdit) {

        this.pnlTwsHor.setEditable(bolEdit);
        this.pnlTwsVer.setEditable(bolEdit);
        this.pnlTwsLng.setEditable(bolEdit);
    }
    
    
    /*
     * Operations
     */
    
    /**
     * Displays the Courant-Snyder parameters for three phase planes 
     * as computed from the given covariance matrix.
     *  
     * @param matCov    the statistical covariance matrix for a beam 
     *
     * @author Christopher K. Allen
     * @since  Oct 2, 2014
     */
    public void display(CovarianceMatrix matCov) {
        Twiss[]     arrTwiss = matCov.computeTwiss();

        this.pnlTwsHor.display(arrTwiss[0]);
        this.pnlTwsVer.display(arrTwiss[1]);
        this.pnlTwsLng.display(arrTwiss[2]);
    }
    
    /**
     * Retrieves the currently displayed Courant-Snyder parameters from the text
     * fields, creates a new <code>Twiss</code> object from them and returns it.
     *   
     * @return  a Courant-Snyder parameter set corresponding to the currently displayed values
     *
     * @author Christopher K. Allen
     * @since  Oct 3, 2014
     */
    public CovarianceMatrix getDisplayValues() {
        
        Twiss   twsHor = this.pnlTwsHor.getDisplayValues();
        Twiss   twsVer = this.pnlTwsVer.getDisplayValues();
        Twiss   twsLng = this.pnlTwsLng.getDisplayValues();
        
        CovarianceMatrix    matCov = CovarianceMatrix.buildCovariance(twsHor, twsVer, twsLng);

        return matCov;
    }
    
    /**
     * Clears all signal property values leaving display fields blank.
     * 
     * @since  Dec 13, 2011
     * @author Christopher K. Allen
     */
    public void     clearDisplay() {
        this.pnlTwsHor.clearDisplay();
        this.pnlTwsVer.clearDisplay();
        this.pnlTwsLng.clearDisplay();
    }
    
    
    /*
     * Support Methods
     */
    
    /**
     * Create the individual Courant-Snyder parameter display panels for
     * each phase plane.
     *
     * @author Christopher K. Allen
     * @since  Oct 2, 2014
     */
    private void guiBuildComponents() {
        this.pnlTwsHor = new TwissValuesDisplayPanel();
        this.pnlTwsHor.setBorder( new BevelBorder(BevelBorder.LOWERED) );

        this.pnlTwsVer = new TwissValuesDisplayPanel();
        this.pnlTwsVer.setBorder(new BevelBorder(BevelBorder.LOWERED) );
        
        this.pnlTwsLng = new TwissValuesDisplayPanel();
        this.pnlTwsLng.setBorder( new BevelBorder(BevelBorder.LOWERED) );
    }
    
    /**
     * Lays out the panels for the individual Courant-Snyder displays
     * on this panel.
     *
     * @author Christopher K. Allen
     * @since  Oct 2, 2014
     */
    private void guiLayoutComponents() {
        
        this.setLayout( new GridBagLayout() );
        
        GridBagConstraints       gbcLayout = new GridBagConstraints();

        gbcLayout.insets = new Insets(0,0,5,5);
        
        // The horizontal Courant-Snyder parameters
        JLabel  lblHor = new JLabel("Horizontal ");
        
        gbcLayout.gridx = 0;
        gbcLayout.gridy = 0;
        gbcLayout.gridwidth  = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.NONE;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        this.add( lblHor, gbcLayout );
        
        gbcLayout.gridy = 1;
        gbcLayout.fill  = GridBagConstraints.HORIZONTAL;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor  = GridBagConstraints.CENTER;
        this.add( this.pnlTwsHor, gbcLayout);
        
        // The vertical Courant-Snyder parameters
        JLabel  lblVer = new JLabel("Vertical ");
        
        gbcLayout.gridx = 0;
        gbcLayout.gridy = 2;
        gbcLayout.gridwidth  = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.NONE;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        this.add( lblVer, gbcLayout );
        
        gbcLayout.gridy = 4;
        gbcLayout.fill  = GridBagConstraints.HORIZONTAL;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor  = GridBagConstraints.CENTER;
        this.add( this.pnlTwsVer, gbcLayout);
        
        // The longitudinal Courant-Snyder parameters
        JLabel  lblLng = new JLabel("Longitudinal ");
        
        gbcLayout.gridx = 0;
        gbcLayout.gridy = 5;
        gbcLayout.gridwidth  = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.NONE;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor = GridBagConstraints.LINE_START;
        this.add( lblLng, gbcLayout );
        
        gbcLayout.gridy = 6;
        gbcLayout.fill  = GridBagConstraints.HORIZONTAL;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor  = GridBagConstraints.CENTER;
        this.add( this.pnlTwsLng, gbcLayout);
        
//        JLabel  lblHor = new JLabel("Horizontal ");
//        Box     boxHor = Box.createVerticalBox();
//        boxHor.add(lblHor);
//        boxHor.add(this.pnlTwsHor);
//        
//        JLabel  lblVer = new JLabel("Vertical ");
//        Box     boxVer = Box.createVerticalBox();
//        boxVer.add(lblVer);
//        boxVer.add(this.pnlTwsVer);
//        
//        JLabel  lblLng = new JLabel("Longitudinal ");
//        Box     boxLng = Box.createVerticalBox();
//        boxLng.add(lblLng);
//        boxLng.add(this.pnlTwsLng);
//        
//        Box boxPnl = Box.createVerticalBox();
//        
//        boxPnl.add(boxHor);
//        boxPnl.add(Box.createVerticalStrut(10));
//        boxPnl.add(boxVer);
//        boxPnl.add(Box.createVerticalStrut(10));
//        boxPnl.add(boxLng);
//        
//        this.add(boxPnl);
    }
}
