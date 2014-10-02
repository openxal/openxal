/**
 * Twiss3PlaneDisplayPanel.java
 *
 * Author  : Christopher K. Allen
 * Since   : Oct 2, 2014
 */
package xal.app.pta.view.analysis;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

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
        
        JLabel  lblHor = new JLabel("Horizontal ");
        Box     boxHor = Box.createVerticalBox();
        boxHor.add(lblHor);
        boxHor.add(this.pnlTwsHor);
        
        JLabel  lblVer = new JLabel("Vertical ");
        Box     boxVer = Box.createVerticalBox();
        boxVer.add(lblVer);
        boxVer.add(this.pnlTwsVer);
        
        JLabel  lblLng = new JLabel("Longitudinal ");
        Box     boxLng = Box.createVerticalBox();
        boxLng.add(lblLng);
        boxLng.add(this.pnlTwsLng);
        
        Box boxPnl = Box.createVerticalBox();
        
        boxPnl.add(boxHor);
        boxPnl.add(Box.createVerticalStrut(10));
        boxPnl.add(boxVer);
        boxPnl.add(Box.createVerticalStrut(10));
        boxPnl.add(boxLng);
        
        this.add(boxPnl);
    }
}
