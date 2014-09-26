/**
 * CourantSnyderEstimator.java
 *
 * @author  Christopher K. Allen
 * @since	Dec 16, 2011
 */
package xal.app.pta.tools.analysis;

import java.util.List;

import xal.app.pta.daq.ScannerData;
import xal.smf.AcceleratorNode;

/**
 * Analyzer class for estimating Courant-Snyder parameters from wire-scanner
 * data.
 * 
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 * 
 * TODO: FINISH!
 *
 * @author Christopher K. Allen
 * @since   Dec 16, 2011
 */
public class CourantSnyderEstimator {

    
    /*
     * Local Attributes
     */
    
    /** The device where the Courant-Snyder parameters are reconstructed */
    @SuppressWarnings("unused")
    private AcceleratorNode         smfRecLoc;
    
    /** List of wire scanner data sets used to estimate the Courant-Snyder parameters */
    @SuppressWarnings("unused")
    private List<ScannerData>        lstDevDat;
}
