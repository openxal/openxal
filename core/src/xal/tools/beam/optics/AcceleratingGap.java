/**
 * AcceleratingGap.java
 *
 * Author  : Christopher K. Allen
 * Since   : Oct 1, 2015
 */
package xal.tools.beam.optics;

import xal.tools.beam.em.AxialFieldSpectrum;

/**
 * Class for modeling an accelerating RF gap as a thin lens.
 *
 *
 * @author Christopher K. Allen
 * @since  Oct 1, 2015
 */
public class AcceleratingGap {

    
    /*
     * Global Constants
     */

    /** the value of 2&pi; */
    private static final double     DBL_2PI = 2.0 * Math.PI;

    /** Speed of light in a vacuum (meters/second) */
    private static final double     DBL_LGHT_SPD = 299792458.0;   
    
    
    
    /*
     * Local Attributes
     */
    
    
    //
    // Defining Parameters
    //
    
    /** total potential drop across accelerating gap */
    private final double        dblFldMag;  
    
//    /** initial particle wave number coming into the gap fields */
//    private final double        ki;
    
    /** time-harmonic frequency of the gap RF field */
    private final double        dblFldFrq;
    
    
    /** spectrum of the accelerating fields along the design axis */
    private final AxialFieldSpectrum    spcFldSpc;

    
    
    //
    // Consistent Parameters
    //
    
//    /** normalized particle velocity approaching the gap */
//    private final double        bi;
//    
//    /** relativistic factor of particle entering the gap */
//    private final double        gi;
//    
//    /** initial kinetic energy of the particle approach gap fields */
//    private final double        Wi;
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Initializing constructor for AcceleratingGap.  All parameters needed for 
     * defining the RF accelerating gap are provided.
     *
     * @param V0        total potential drop across gap axial field
     * @param f         time-harmonic frequency of the accelerating field
     * @param spcRfFld  spectrum of the RF field along design axis
     *
     * @since  Oct 1, 2015,   Christopher K. Allen
     */
    public AcceleratingGap(double V0, double f, AxialFieldSpectrum spcRfFld) {
        this.dblFldMag = V0;
        this.dblFldFrq = f;
        this.spcFldSpc = spcRfFld;
    }

    
    /*
     * Support Methods
     */
    
    /**
     * Compute the normalized particle velocity &beta; for the given particle
     * wave number <i>k</i>.
     * 
     * @param k     wave number of the particle with respect to RF frequency (radians/meter)
     * 
     * @return      the normalized velocity &beta; of the particle for the given wave number <i>k</i>
     *
     * @since  Sep 28, 2015   by Christopher K. Allen
     */
    private double velocity(double k) {
        double  lambda = DBL_LGHT_SPD/this.dblFldFrq;
        double  beta   = DBL_2PI/(k*lambda);
        
        return beta;
    }
    
}
