/**
 * AcceleratingGap.java
 *
 * Author  : Christopher K. Allen
 * Since   : Oct 1, 2015
 */
package xal.tools.beam.optics;

import xal.tools.math.Complex;

import xal.tools.beam.RelativisticParameterConverter;
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
    
    /** wave number of the gap accelerating field in free space */
    private final double        dblRfWvNm;
    
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
        
        this.dblRfWvNm = DBL_2PI*f/DBL_LGHT_SPD;
    }

    
    /*
     * Attributes
     */
    
    /**
     * Returns the total (integrated) potential gain of the RF field across the 
     * accelerating gap.  This is the value given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>V</i><sub>0</sub> &trie; &int;<i>E<sub>z</sub></i>(0,<i>z</i>)<i>dz</i> ,
     * <br/>
     * <br/>
     * where the integral is taken over the entire real line <i>z</i> &in; (-&infin;,+&infin;).
     * This value represents the total available accelerating RF energy and an upper
     * limit for energy gain.
     * 
     * @return      the total potential <i>V</i><sub>0</sub> across the accelerating gap (in Volts) 
     *
     * @since  Oct 2, 2015,   Christopher K. Allen
     */
    public double   getRfFieldPotential() {
        return this.dblFldMag;
    }
    
    /**
     * Returns the time-harmonic frequency of gap accelerating field.
     * 
     * @return      the RF frequency of the electric field (Hz)
     *
     * @since  Sep 28, 2015   by Christopher K. Allen
     */
    public double   getRfFrequency() {
        return this.dblFldFrq;
    }
    
    /**
     * Get the free space wave number <i>k</i><sub>0</sub> of the gap accelerating fields.
     * This quantity has the formula
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>k</i><sub>0</sub> = 2&pi;/&lambda;
     * <br/>
     * <br/>
     * where &lambda; = <i>c</i>/<i>f</i> is the wave length of the RF in free space.
     *   
     * @return  free space wave number <i>k</i><sub>0</sub> of gap RF
     *
     * @since  Oct 1, 2015,   Christopher K. Allen
     */
    public double   getRfWaveNumber() {
        return this.dblRfWvNm;
    }

    
    /*
     * Operations
     */
    
    public double   computePreGapEnergyGain(double Er, double bi) {
        
        final double  ki = this.computeWaveNumber(bi);

        return 0.0;
    }
    
    
    
    /*
     * Support Methods
     */
    
    
    //
    // Phase Variables
    //
    
    private double phaseGainPreGap(double ang, double k) {
        return 0.0;
    }
    
    /**
     * Computes and returns the pre-gap "Hamiltonian" function 
     * <i>H</i><sup>-</sup>(&phi;, <i>k</i>).  This complex function is the product of the
     * pre-envelope spectrum &Escr;<sup>-</sup>(<i>k</i>) and mid-gap synchronous
     * phase &exponentiale;<sup>-<i>i</i> &phi;</sup>.  That is,
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>H</i><sup>-</sup>(&phi;, <i>k</i>) = &Escr;<sup>-</sup>(<i>k</i>)&exponentiale;<sup>-<i>i</i> &phi;</sup>.
     * <br/>
     * <br/>
     * The phase jump &Delta;&phi;<sup>-</sup> and energy gain &Delta;<i>W</i><sup>-</sup> are 
     * dependent upon this quantity.
     * 
     * @param phi       the mid-gap synchronous phase angle &phi; (in radians)
     * @param k         the synchronous particle wave number (in radians/meter)
     * 
     * @return          value of the Hamiltonian <i>H</i><sup>-</sup> at &phi; and <i>k</i>
     *
     * @since  Oct 7, 2015,   Christopher K. Allen
     */
    private Complex preGapHamiltonain(double phi, double k) {
        Complex     cpxPreSpc = this.spcFldSpc.preEnvSpectrum(k);
        Complex     cpxPreAng = Complex.euler(k);
        Complex     cpxHamilt = cpxPreSpc.times(cpxPreAng);
        
        return cpxHamilt;
    }
    
    /**
     * Computes and returns the derivative of the pre-gap "Hamiltonian" function 
     * <i>H</i><sup>-</sup>(&phi;, <i>k</i>) with respect to the wave number <i>k</i>,
     * that is, the value   <i>dH</i><sup>-</sup>(&phi;, <i>k</i>)/<i>dk</i>.
     * This complex-valued function of variables &phi; and <i>k</i> is given by 
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>dH</i><sup>-</sup>(&phi;, <i>k</i>)/<i>dk</i> = 
     *                                    [<i>d</i>&Escr;<sup>-</sup>(<i>k</i>)/<i>dk</i>] &exponentiale;<sup>-<i>i</i> &phi;</sup>.
     * <br/>
     * <br/>
     * where &Escr;<sup>-</sup>(<i>k</i>) is the pre-envelope spectrum and 
     * &exponentiale;<sup>-<i>i</i> &phi;</sup> is the mid-gap synchronous
     * phase.  
     * The phase jump &Delta;&phi;<sup>-</sup> is directly proportional to the imaginary part
     * of this quantity.
     * 
     * @param phi       the mid-gap synchronous phase angle &phi; (in radians)
     * @param k         the synchronous particle wave number (in radians/meter)
     * 
     * @return          value of the Hamiltonian <i>H</i><sup>-</sup> at &phi; and <i>k</i>
     *
     * @since  Oct 7, 2015,   Christopher K. Allen
     */
    private Complex dkPreGapHamiltonian(double phi, double k) {
        Complex     cpxDkPreSpc = this.spcFldSpc.dkPreEnvSpectrum(k);
        Complex     cpxPreAngle = Complex.euler(k);
        Complex     cpxDkHamilt = cpxDkPreSpc.times(cpxPreAngle);
        
        return cpxDkHamilt;
    }
    
    /**
     * Computes and returns the derivative of the pre-gap "Hamiltonian" function 
     * <i>H</i><sup>-</sup>(&phi;, <i>k</i>) with respect to the mid-gap 
     * synchronous phase &phi;,
     * that is, the value   <i>dH</i><sup>-</sup>(&phi;, <i>k</i>)/<i>d</i>&phi;.
     * This complex-valued function of variables &phi; and <i>k</i> is given by 
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>dH</i><sup>-</sup>(&phi;, <i>k</i>)/<i>d</i>&phi; = 
     *                                       &Escr;<sup>-</sup>(<i>k</i>) <i>d</i>&exponentiale;<sup>-<i>i</i> &phi;</sup>/<i>d</i>&phi;
     *                                    = -<i>i</i> <i>H</i><sup>-</sup>(&phi;,<i>k</i>) ,
     * <br/>
     * <br/>
     * where &Escr;<sup>-</sup>(<i>k</i>) is the pre-envelope spectrum and 
     * &exponentiale;<sup>-<i>i</i> &phi;</sup> is the mid-gap synchronous
     * phase.  
     * The energy gain &Delta;<i>W</i><sup>-</sup> is proportional to the 
     * imaginary part of this quantity.
     * 
     * @param phi       the mid-gap synchronous phase angle &phi; (in radians)
     * @param k         the synchronous particle wave number (in radians/meter)
     * 
     * @return          value of the Hamiltonian <i>H</i><sup>-</sup> at &phi; and <i>k</i>
     *
     * @since  Oct 7, 2015,   Christopher K. Allen
     */
    private Complex dphiPreGapHamiltonian(double phi, double k) {
        return Complex.ZERO;
        
    }
    
    
    //
    // Beam Particle Properties
    //
    
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
    private double computeNormVelocity(double k) {
        double  lambda = DBL_LGHT_SPD/this.getRfFrequency();
        double  beta   = DBL_2PI/(k*lambda);
        
        return beta;
    }
    
    /**
     * <p>
     * Compute and return the particle wave number <i>k</i> for the given normalized 
     * particle velocity &beta;.  The formula is
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>k</i> = 2&pi;/&beta;&lambda; ,
     * <br/>
     * <br/>
     * where &lambda; is the wavelength of the accelerating RF.
     * </p>
     * 
     * @param beta      normalized probe velocity
     * 
     * @return          particle wave number with respect to the RF
     *
     * @since  Feb 16, 2015   by Christopher K. Allen
     */
    private double computeWaveNumber(double beta) {
        double lambda = DBL_LGHT_SPD/this.getRfFrequency();
        double k      = DBL_2PI/(beta*lambda);

        return k;
    }
    
    /** 
     *  Computes the relativistic factor &gamma;from the given &beta; value.
     *  
     *  @param  beta    particle velocity normalized  w.r.t. the speed of light
     *  
     *  @return         relativistic factor &gamma;
     */
    private double computeGamma(double beta) { 
        return 1.0/Math.sqrt(1.0 - beta*beta); 
    };
    
    /**
     * <p>
     * Compute and return the normalized wave number <i>K</i>.  This quantity appears as
     * a constant in the phase jump expressions.  The value of <i>K</i>
     * is defined by the formula
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>K</i> &trie; (1/&beta;<sup>3</sup>&gamma;<sup>3</sup>)
     *                               (<i>qV</i><sub>0</sub>/<i>mc</i><sup>2</sup>)<i>k</i><sub>0</sub>
     * <br/>
     * <br/>
     * where &beta; is the normalized particle velocity, &gamma; is the relativistic factor;
     * <i>V</i><sub>0</sub> is the total (integrated) RF field potential across the gap, 
     * <i>mc</i><sup>2</sup>/<i>q</i> is the rest mass in electron-Volts, and 
     * <i>k</i><sub>0</sub> &trie; 2&pi;/&lambda;
     * is the free-space wave number of the accelerating RF field with wavelength &lambda;.
     * </p>
     * <p>
     * The quantity <i>K</i> can be interpreted as the particle wave number in energy space
     * rather that momentum space.
     * </p>
     * 
     * @param beta      normalized particle velocity &beta; (unitless)
     * @param Er        particle rest mass (in electron-Volts)
     * 
     * @return          normalized wave number <i>K</i> (in radians/meter)
     *
     * @since  Oct 1, 2015,   Christopher K. Allen
     */
    private double computeNormWaveNumber(double beta, double Er) {
        double gamma = this.computeGamma(beta);
        double bg    = Math.sqrt(gamma*gamma - 1.0);
        double bg3   = bg * bg * bg;
        double k0    = this.getRfWaveNumber();
        
        double En    = Er*bg3;
        double K     = k0/En;
        
        return K;
    }
}
