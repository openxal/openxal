/**
 * AcceleratingRfGap.java
 *
 * Author  : Christopher K. Allen
 * Since   : Oct 1, 2015
 */
package xal.tools.beam.optics;

import xal.tools.math.Complex;
import xal.tools.beam.EnergyVector;
import xal.tools.beam.em.AxialFieldSpectrum;

/**
 * Class for modeling an accelerating RF gap as a thin lens.
 *
 *
 * @author Christopher K. Allen
 * @since  Oct 1, 2015
 */
public class AcceleratingRfGap {

    
    /*
     * Global Constants
     */

    /** the value of 2&pi; */
    private static final double     DBL_2PI = 2.0 * Math.PI;

    /** Speed of light in a vacuum (meters/second) */
    private static final double     DBL_LGHT_SPD = 299792458.0;
    
    
    
    /** default error tolerance when searching for consistent gain parameters */
    private static final double     DBL_ERR_TOL = 1.0e-16;
    
    /** maximum number of allowable iterations when searching for consistent gain parameters */
    private static final int        CNT_MAX_ITER = 100;
    
    
    /*
     * Internal Classes
     */

    /**
     * Enumeration specifying the upstream location or downstream locations for
     * the RF accelerating gap.
     *
     *
     * @author Christopher K. Allen
     * @since  Nov 2, 2015
     */
    public enum LOC {
        
        /** Constant identifying the pre-gap (upstream) location */
        PREGAP,
        
        /** Constant identifying the post-gap (downstream) location */
        POSTGAP;

        
        /*
         * Operations
         */
        
        /**
         * Compute and return the value of the generalized Hamiltonian function 
         * <i>H</i> at the given longitudinal
         * phase coordinates for the given accelerating RF gap.
         *  
         * @param gap   Hamiltonian function for this gap
         * @param phi   particle phase location &phi; (radians)
         * @param k     particle wave number <i>k</i> (radians/meter)
         * 
         * @return      Hamiltonian function <i>H</i>(&phi;,<i>k</i>)
         * 
         * @see AcceleratingRfGap#preGapHamiltonain(double, double)
         * @see AcceleratingRfGap#postGapHamiltonain(double, double)
         *
         * @since  Nov 3, 2015,   Christopher K. Allen
         */
        public Complex   gapHamiltonian(AcceleratingRfGap gap, double phi, double k) {
            if (this.equals(PREGAP))
                return gap.preGapHamiltonain(phi, k);
            else
                return gap.postGapHamiltonain(phi, k);
        }
        
        /**
         * Compute and return the derivative of the generalized Hamiltonian function 
         * <i>dH</i>/<i>d</i>&phi; with respect to particle phase location &phi; 
         * for the given accelerating RF gap.
         *  
         * @param gap   gap for which the parameters are computed
         * @param phi   particle phase location &phi; (radians)
         * @param k     particle wave number <i>k</i> (radians/meter)
         * 
         * @return      value of derivative <i>dH</i>(&phi;,<i>k</i>)/<i>d</i>&phi;
         * 
         * @see AcceleratingRfGap#dphiPreGapHamiltonian(double, double)
         * @see AcceleratingRfGap#dphiPostGapHamiltonian(double, double)
         *
         * @since  Nov 3, 2015,   Christopher K. Allen
         */
        public Complex dphiGapHamiltonian(AcceleratingRfGap gap, double phi, double k) {
            if (this.equals(PREGAP))
                return gap.dphiPreGapHamiltonian(phi, k);
            else
                return gap.dphiPostGapHamiltonian(phi, k);
        }
        
        /**
         * Compute and return the derivative of the generalized Hamiltonian function 
         * <i>dH</i>/<i>dk</i> with respect to particle wave number <i>k</i> 
         * for the given accelerating RF gap.
         *  
         * @param gap   gap for which the parameters are computed
         * @param phi   particle phase location &phi; (radians)
         * @param k     particle wave number <i>k</i> (radians/meter)
         * 
         * @return      value of derivative <i>dH</i>(&phi;,<i>k</i>)/<i>dk</i>
         * 
         * @see AcceleratingRfGap#dkPreGapHamiltonian(double, double)
         * @see AcceleratingRfGap#dkPostGapHamiltonian(double, double)
         *
         * @since  Nov 3, 2015,   Christopher K. Allen
         */
        public Complex  dkGapHamiltonian(AcceleratingRfGap gap, double phi, double k) {
            if (this.equals(PREGAP))
                return gap.dkPreGapHamiltonian(phi, k);
            else
                return gap.dkPostGapHamiltonian(phi, k);
        }
        
    }
    
    /**
     * Class <code>NoConvergenceException</code>.  This exception class is thrown
     * in the case of a search algorithm that does not converge.
     *
     *
     * @author Christopher K. Allen
     * @since  Oct 15, 2015
     */
    public class NoConvergenceException extends RuntimeException {

        
        
        /*
         * Global Constants
         */
        /** Java serialization version  */
        private static final long serialVersionUID = 1L;

        
        /**
         * Zero argument constructor for NoConvergenceException.
         *
         * @since  Oct 15, 2015,   Christopher K. Allen
         */
        public NoConvergenceException() {
            super();
        }

        /**
         * Initializing constructor for NoConvergenceException.
         *
         * @param message   string message describing exception and/or cause
         * @param cause     source of this exception an exception chain
         *
         * @since  Oct 15, 2015,   Christopher K. Allen
         */
        public NoConvergenceException(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Initializing constructor for NoConvergenceException.
         *
         * @param message   string message describing exception and/or cause
         *
         * @since  Oct 15, 2015,   Christopher K. Allen
         */
        public NoConvergenceException(String message) {
            super(message);
        }

        /**
         * Initializing constructor for NoConvergenceException.
         *
         * @param cause     source of this exception an exception chain
         *
         * @since  Oct 15, 2015,   Christopher K. Allen
         */
        public NoConvergenceException(Throwable cause) {
            super(cause);
        }
        
    }
    
//    public class    PhaseVariable {
//        
//        /** the particle phase w.r.t. to the RF cycle (radians) */
//        public double       phi;
//        
//       
//        /** the kinetic energy of the particle (in electron-Volts) */ 
//       public double        W;
//    }
    
    
    /*
     * Local Attributes
     */
    
    //
    // Defining Parameters
    //
    
//    /** initial particle wave number coming into the gap fields */
//    private final double        ki;

//    /** length of the gap - used for an initial approximation of mid-gap synch phase 
//     * 
//     * @deprecated  CKA I don't think we'll need this, length is not well defined
//     * */
//    private final double        dblGapLen;
    
    /** total potential drop <i>V</i><sub>0</sub> across accelerating gap */
    private double              dblFldMag;  
    
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
    
    
    //
    //  Numeric Parameters
    //
    
    /** maximum number of iterations allowable in iterative search for gain parameters */
    private int             cntIterMax;
    
    /** acceptable error tolerance when iterating for consistent gain parameters */
    private double          dblErrTol;
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Initializing constructor for AcceleratingRfGap.  All parameters needed for 
     * defining the RF accelerating gap are provided.
     *
     * @param f         time-harmonic frequency of the accelerating field (Hz)
     * @param L         length of gap, used for initial approximation of phase (meters)
     * @param V0        total potential drop across gap axial field (Volts)
     * @param spcRfFld  spectrum of the RF field along design axis
     *
     * @since  Oct 1, 2015,   Christopher K. Allen
     */
    public AcceleratingRfGap(double f, double V0, AxialFieldSpectrum spcRfFld) {
//        this.dblGapLen = L;
        this.dblFldFrq = f;
        this.dblFldMag = V0;
        this.spcFldSpc = spcRfFld;
        
        this.dblRfWvNm  = DBL_2PI*f/DBL_LGHT_SPD;
        
        this.cntIterMax = CNT_MAX_ITER;
        this.dblErrTol  = DBL_ERR_TOL;
    }
    
    /**
     * Set the maximum number of allowable iterations during a search for the consistent
     * gain parameters &Delta;&phi; and &Delta;<i>W</i>.  The default value for this quantity  
     * is given by the constant <code>{@link #CNT_MAX_ITER}</code>.
     * 
     * @param cntIterMax    new value for maximum iteration count in gain computations
     *
     * @since  Oct 13, 2015,   Christopher K. Allen
     */
    public void     setMaxIterations(int cntIterMax) {
        this.cntIterMax = cntIterMax;
    }
    
    /**
     * Sets the acceptable error tolerance when iterating for consistent gain parameters
     * &Delta;<i>W</i> and &Delta;&phi;.  The default value for this quantity is 
     * given by the constant <code>{@link #DBL_ERR_TOL}</code>.
     * 
     * @param dblErrTol     new value for the error tolerance in gain computations
     *
     * @since  Oct 9, 2015,   Christopher K. Allen
     */
    public void    setErrorTolerance(double dblErrTol) {
        this.dblErrTol = dblErrTol;
    }
    
    /**
     * Resets the total potential gain across the accelerating gap.
     * 
     * @param V0    the integral &int;<i>E<sub>z</i></i>(<i>z<i>) <i>dz</i> (in Volts)
     *
     * @since  Oct 16, 2015,   Christopher K. Allen
     */
    public void     setRfFieldPotential(double V0) {
        this.dblFldMag = V0;
    }

    
    /*
     * Attributes
     */
    
    /**
     * Returns the maximum number of allowable iterations during the iterative search for
     * RF gap gain parameters &Delta;&phi; and &Delta;<i>W</i>.
     * 
     * @return      maximum iteration count in (&Delta;&phi;,&Delta;<i>W</i>) phase jump, 
     *                  energy gain computations
     *
     * @since  Oct 13, 2015,   Christopher K. Allen
     * 
     * @see AcceleratingRfGap#setMaxIterations()
     */
    public int      getMaxIterations() {
        return this.cntIterMax;
    }
    
    /**
     * Returns the maximum allowable error, the distance between iterates of 
     * (&Delta;&phi;,&Delta;<i>W</i>), before the current iterate is considered
     * a valid solution.  Once the <i>L</i><sub>2</sub> distance between the current
     * iterate of phase jump &Delta;&phi; and energy gain &Delta;<i>W</i> and the 
     * previous iterate is less than this value, then the iteration stops with a 
     * valid solution.
     * 
     * @return      maximum distance between solution iterations for valid solution
     *
     * @since  Oct 13, 2015,   Christopher K. Allen
     */
    public double   getErrorTolerance() {
        return this.dblErrTol;
    }

//    /**
//     * Returns the end-to-end length of the RF gap.  Note that this value is not
//     * always well-defined depending upon the gap geometry and definition.  The 
//     * primary use of this value is to compute initial values for mid-gap quantities.
//     * Thus, the gap length is not actually a defining quantity but provides an
//     * estimate of the distance a particle travels.
//     * 
//     * @return      end-to-end distance where gap is defined (meters)
//     *
//     * @since  Oct 9, 2015,   Christopher K. Allen
//     */
//    public double   getGapLength() {
//        return this.dblGapLen;
//    }
    
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
     * Returns the spectrum of the gap's axial electric field.
     * 
     * @return      spectrum object describing the electric field along the gap axis 
     *
     * @since  Oct 9, 2015,   Christopher K. Allen
     */
    public AxialFieldSpectrum    getFieldSpectrum() {
        return this.spcFldSpc;
    }
    
    
    /*
     * Operations
     */
    
    /**
     * <p>
     * Computes and returns the phase jump &Delta;&phi; and energy gain &Delta;<i>W</i>
     * imparted to a particle with the the given particle charge <i>Q</i> and 
     * rest energy <i>E<sub>r</sub></i> for the given gap region.  The particle enters this
     * region with initial gap phase &phi;<sub>0</sub> and initial 
     * kinetic energy <i>W<sub>i</sub></i>.  The returned values represent the effects of
     * the first have of this gap upon the so described particle.  This method uses
     * an iterative technique to compute the results in order to maintain a 
     * self-consistent set of expressions.  That is, the modeling expressions are a
     * set of transcendental equations which must be solved self-consistently.
     * Thus, we are relegated to iterative methods. 
     * </p>
     * <p>
     * The provided phase &phi;<sub>0</sub> is assumed
     * to be the phase of the particle at the center of the gap, if it were coasting
     * through the gap with kinetic energy <i>W<sub>i</sub></i> for the pre-gap computation, and
     * after the pre-gap phase jump &Delta;&phi;<sup>-</sup> for the post-gap computation.  
     * The phase value returned by this method, either &Delta;&phi;<sup>-</sup> or 
     * &Delta;&phi;<sup>+</sup> for the pre-gap or post-gap calculation, respectively, is 
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &Delta;&phi;<sup>-</sup> = +&phi;<sub>0</sub> - &phi;<sub>0</sub><sup>-</sup> ,
     * <br/>
     * &nbsp; &nbsp; &Delta;&phi;<sup>+</sup> = -&phi;<sub>0</sub> + &phi;<sub>0</sub><sup>+</sup> ,
     * <br/>
     * <br/>
     * where &phi;<sub>0</sub><sup>-</sup> is the thin-lens model gap center intercept of the incoming
     * coasting particle, &phi;<sub>0</sub><sup>+</sup> is the thin-lens model gap center intercept of the
     * outgoing coasting particle, and &phi;<sub>0</sub> is the actual particle phase at the gap center.  
     * The total phase jump at the gap center &Delta;&phi; is the sum of these two phase jumps,   
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &Delta;&phi; = &Delta;&phi;<sup>-</sup> + &Delta;&phi;<sup>+</sup> = &phi;<sub>0</sub><sup>+</sup> - &phi;<sub>0</sub><sup>-</sup> ,
     * <br/>
     * </p>
     * <p>
     * The returned energy gain, either &Delta;<i>W</i><sup>-</sup> for the pre-gap location or
     * &Delta;<i>W</i><sup>+</sup> for the post-gap location, is the energy gained by the particle in
     * those respective locations.  The total energy gained by a particle through the gap is the sum
     * of these energies
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &Delta;<i>W</i> = &Delta;<i>W</i><sup>-</sup> + &Delta;<i>W</i><sup>+</sup> . 
     * </p>
     * 
     * @param locHam    location of longitudinal gain calculations, with respect to gap center
     * @param Q         charge of the incoming particles in terms of fundamental charge <i>q</i> (unitless)
     * @param Er        rest energy of the incoming particles (electron-Volts)
     * @param vecInit   initial phase and energy pair 
     *                  (&phi;<sub>0</sub>,<i>W<sub>i</sub></i>) into specified gap region 
     *                  (radians, electron-Volts)
     *                   
     * @return          the corrective phase jump and energy gain pair (&Delta;&phi;,&Delta;<i>W</i>)
     *                  after impulse at specified location, pre- or post-gap (radians, electron-Volts)
     *                  
     * @throws  NoConvergenceException  the iterative search for (&Delta;&phi;,&Delta;<i>W</i>) failed to converge
     *
     * @since  Oct 15, 2015,   Christopher K. Allen
     */
    public EnergyVector  computeGapGains(LOC locHam, double Q, double Er, EnergyVector vecInit) throws NoConvergenceException {
        
        // Variable scaling constants (scales the "Hamiltonian")
        final double  V0 = Q * this.getRfFieldPotential();
        final double  Ki = Q * this.computeNormWaveNumber(vecInit.getEnergy(), Er);
        
        // For the asymptotic model, get the phase intercept, the initial kinetic energy, 
        //  and the initial wave number 
        double  phi0 = vecInit.getPhase();
        double  Wi   = vecInit.getEnergy();
        
        // Initialize the search variables.
        //  Use the phase intercept and initial energy as starting values
        double phi = phi0;  // the synchronous phase at the gap center
        double W   = Wi;    // the energy gained up to the gap center
        double k   = this.computeWaveNumber(W, Er);

        // Compute the starting values for phase jump and energy gain
        double dW   = - V0 * locHam.dphiGapHamiltonian(this, phi, k).imaginary();
        double dphi = + Ki * locHam.dkGapHamiltonian(this, phi, k).imaginary();
        
        // Initialize the search loop
        int     cntIter = 0;
        double  dblErr  = 10.0*this.getErrorTolerance();
        while (cntIter < this.getMaxIterations()) {

            // Compute the new phase and energy from the previously computed
            //  phase jump and energy gain
            phi = phi0 + dphi;
            W   = Wi + dW;
            k   = this.computeWaveNumber(W, Er);
            
            // Compute the new phase jump and energy gain from the new phase 
            //  and energies
            double dphi_i = + Ki * locHam.dkGapHamiltonian(this, phi, k).imaginary();
            double dW_i   = - V0 * locHam.dphiGapHamiltonian(this, phi, k).imaginary();
            
            // Compute stopping criteria values 
            cntIter++;
            dblErr = (dphi_i - dphi)*(dphi_i - dphi) + (dW - dW_i)*(dW - dW_i)/(Wi*Wi);

            // Update the values of the dependent variables wave number, phase jump, 
            //  and energy gain 
            dphi = dphi_i;
            dW   = dW_i;
            
            // Check for Cauchy sequence convergence.  Stop and return gains if passed. 
            if (dblErr < this.getErrorTolerance()) {
                EnergyVector     vecGains = new EnergyVector(dphi, dW);
                
                return vecGains;
            }
            
        }

        // If we made it outside the loop then there was no converge.
        String  strMsg = locHam.name() + ": failed to compute gap gain values after " 
                + cntIter 
                + " iterations.  Error = " 
                + dblErr;

        throw new NoConvergenceException(strMsg);
    }
    
    
    /**
     * <p>
     * Computes and returns the phase jump &Delta;&phi; and energy gain &Delta;<i>W</i>
     * imparted to a particle with the the given rest energy <i>E<sub>r</sub></i>,
     * gap phase &phi;<sub>0</sub><sup>-</sup>, and initial 
     * kinetic energy <i>W<sub>i</sub></i>.  The returned values represent the effects of
     * the first have of this gap upon the so described particle.  This method uses
     * an iterative technique to compute the results in order to maintain a 
     * self-consistent set of expressions.  That is, the modeling expressions are a
     * set of transcendental equations which must be solved self-consistently.
     * Thus, we are relegated to iterative methods. 
     * </p>
     * <p>
     * The provided phase &phi;<sub>0</sub><sup>-</sup> is assumed
     * to be the phase of the particle at the center of the gap if it were coasting
     * through the gap with kinetic energy <i>W<sub>i</sub></i>.  
     * The actual phase at the gap center &phi;<sub>0</sub> will be 
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &phi;<sub>0</sub> = &phi;<sub>0</sub><sup>-</sup> + &Delta;&phi; ,
     * <br/>
     * <br/>
     * where &Delta;&phi; is the phase jump returned by this method.  
     * </p>
     * <p>
     * The returned energy gain &Delta;<i>W</i> is that for the particle at the gap
     * center.  That is, the effects of the gap on the particle include an energy gain
     * of &Delta;<i>W</i> up to the gap center.  The total kinetic energy <i>W</i><sub>0</sub> 
     * at gap center is then
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>W</i><sub>0</sub> = <i>W<sub>i</i> + &Delta;<i>W</i>. 
     * </p>
     * 
     * @param Er        rest energy of the incoming particles (electron-Volts)
     * @param vecInit   gap phase and initial energy pair 
     *                  (&phi;<sub>0</sub><sup>-</sup>,<i>W<sub>i</sub></i>) 
     *                  (radians, electron-Volts)
     *                   
     * @return          the corrective phase jump and energy gain pair (&Delta;&phi;,&Delta;<i>W</i>)
     *                  at gap center (radians, electron-Volts)
     *                  
     * @throws  NoConvergenceException  the iterative search for (&Delta;&phi;,&Delta;<i>W</i>) failed to converge
     *
     * @since  Oct 15, 2015,   Christopher K. Allen
     */
    @Deprecated
    public EnergyVector  computePreGapGains(double Er, EnergyVector vecInit) throws NoConvergenceException {
        
        // Variable scaling constants (scales the "Hamiltonian")
        final double  V0 = this.getRfFieldPotential();
        final double  Ki = this.computeNormWaveNumber(vecInit.getEnergy(), Er);
        
        // For the asymptotic model, get the phase intercept, the initial kinetic energy, 
        //  and the initial wave number 
        double  phi0 = vecInit.getPhase();
        double  Wi   = vecInit.getEnergy();
//        double  ki   = this.computeWaveNumber(vecInit.getEnergy(), Er);
        
        // Initialize the search variables.
        //  Use the phase intercept and initial energy as starting values
        double phi = phi0;  // the synchronous phase at the gap center
        double W   = Wi;    // the energy gained up to the gap center
        double k   = this.computeWaveNumber(W, Er);

        // Compute the starting values for phase jump and energy gain
        double dW   = - V0 * this.dphiPreGapHamiltonian(phi, k).imaginary();
//        double dW   = - V0 * this.dphiPreGapHamiltonian(phi, k).imaginary();
        double dphi = + Ki * this.dkPreGapHamiltonian(phi, k).imaginary();
//        double dphi = + Ki * this.dkPreGapHamiltonian(phi, k).imaginary();
        
        // Initialize the search loop
        int     cntIter = 0;
        double  dblErr  = 10.0*this.getErrorTolerance();
        while (cntIter < this.getMaxIterations()) {

            // Compute the new phase and energy from the previously computed
            //  phase jump and energy gain
            phi = phi0 + dphi;
            W   = Wi + dW;
            k   = this.computeWaveNumber(W, Er);
            
            // Compute the new phase jump and energy gain from the new phase 
            //  and energies
//            double dphi_i = + Ki * this.dkPreGapHamiltonian(phi, k).imaginary();
            double dphi_i = + Ki * this.dkPreGapHamiltonian(phi, k).imaginary();
//            double dW_i   = - V0 * this.dphiPreGapHamiltonian(phi, k).imaginary();
            double dW_i   = - V0 * this.dphiPreGapHamiltonian(phi, k).imaginary();
            
            // Compute stopping criteria values 
            cntIter++;
            dblErr = (dphi_i - dphi)*(dphi_i - dphi) + (dW - dW_i)*(dW - dW_i)/(Wi*Wi);

            // Update the values of the dependent variables wave number, phase jump, 
            //  and energy gain 
            dphi = dphi_i;
            dW   = dW_i;
            
            // Check for Cauchy sequence convergence.  Stop and return gains if passed. 
            if (dblErr < this.getErrorTolerance()) {
                EnergyVector     vecGains = new EnergyVector(dphi, dW);
                
                return vecGains;
            }
            
        }

        // If we made it outside the loop then there was no converge.
        String  strMsg = "failed to compute gap gain values after " 
                + cntIter 
                + " iterations.  Error = " 
                + dblErr;

        throw new NoConvergenceException(strMsg);
    }
    
    
    /*
     * Support Methods
     */
    
    //
    // Phase Variables
    //
    
    /**
     * Returns the energy gain of a particle up to the middle of the gap
     * given that its middle-gap phase is &phi; and its pre-gap wave number
     * is <i>k</i>.  This value is computed from a complex Hamiltonian 
     * <i>H</i><sup>-</sup>(&phi;,<i>k</i>) built from the product of the pre-gap spectral
     * envelope &Escr;<sup>-</sup>(<i>k</i>) and the complex angle
     * &exponentiale;<sup>-<i>i</i>&phi;</sup>.  Specifically, the energy gain
     * &Delta;<i>W</i> is 
     * is 
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &Delta;<i>W</i> = -<i>V</i><sub>0</sub> Im <i>dH</i>/<i>d</i>&phi; ,
     * <br/>
     * <br/>
     * where <i>V</i><sub>0</sub> is the potential drop across the gap and Im indicates the
     * imaginary part of a complex number.  Note that the wave number is the "best fit"
     * constant describing the dynamic value that changes as it traverses the gap.
     * 
     * 
     * @param phi       the synchronous particle phase &phi; at the gap center (radians)
     * @param k         the wave number <i>k</i> of the particle within the gap (radians/meter) 
     * 
     * @return          the energy gain of the particle 
     *
     * @since  Oct 9, 2015,   Christopher K. Allen
     */
    private  double   computePreGapEnergyGain(double phi, double k) {

        Complex     cpxDphiHamil = this.dphiPreGapHamiltonian(phi, k);
        double      dblImagHamil = cpxDphiHamil.imaginary();
        double      dW = - this.getRfFieldPotential() * dblImagHamil; 
        
        return dW;
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
        Complex     cpxPreAng = Complex.euler(phi);
        Complex     cpxHamilt = cpxPreSpc.times(cpxPreAng);
        
        return cpxHamilt;
    }
    
    /**
     * Computes and returns the post-gap "Hamiltonian" function 
     * <i>H</i><sup>+</sup>(&phi;, <i>k</i>).  This complex function is the product of the
     * post-envelope spectrum &Escr;<sup>+</sup>(<i>k</i>) and mid-gap synchronous
     * phase &exponentiale;<sup>-<i>i</i> &phi;</sup>.  That is,
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>H</i><sup>+</sup>(&phi;, <i>k</i>) = &Escr;<sup>+</sup>(<i>k</i>)&exponentiale;<sup>-<i>i</i> &phi;</sup>.
     * <br/>
     * <br/>
     * The phase jump &Delta;&phi;<sup>+</sup> and energy gain &Delta;<i>W</i><sup>+</sup> are 
     * dependent upon this quantity.
     * 
     * @param phi       the mid-gap synchronous phase angle &phi; (in radians)
     * @param k         the synchronous particle wave number (in radians/meter)
     * 
     * @return          value of the Hamiltonian <i>H</i><sup>+</sup> at &phi; and <i>k</i>
     *
     * @since  Oct 7, 2015,   Christopher K. Allen
     */
    private Complex postGapHamiltonain(double phi, double k) {
        Complex     cpxPostSpc = this.spcFldSpc.postEnvSpectrum(k);
        Complex     cpxPostAng = Complex.euler(phi);
        Complex     cpxHamilt = cpxPostSpc.times(cpxPostAng);
        
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
        Complex     cpxPreAngle = Complex.euler(phi);
        Complex     cpxDkHamilt = cpxDkPreSpc.times(cpxPreAngle);
        
        return cpxDkHamilt;
    }
    
    /**
     * Computes and returns the derivative of the post-gap "Hamiltonian" function 
     * <i>H</i><sup>+</sup>(&phi;, <i>k</i>) with respect to the wave number <i>k</i>,
     * that is, the value   <i>dH</i><sup>+</sup>(&phi;, <i>k</i>)/<i>dk</i>.
     * This complex-valued function of variables &phi; and <i>k</i> is given by 
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>dH</i><sup>+</sup>(&phi;, <i>k</i>)/<i>dk</i> = 
     *                                    [<i>d</i>&Escr;<sup>+</sup>(<i>k</i>)/<i>dk</i>] &exponentiale;<sup>-<i>i</i> &phi;</sup>.
     * <br/>
     * <br/>
     * where &Escr;<sup>+</sup>(<i>k</i>) is the post-envelope spectrum and 
     * &exponentiale;<sup>-<i>i</i> &phi;</sup> is the mid-gap synchronous
     * phase.  
     * The phase jump &Delta;&phi;<sup>+</sup> is directly proportional to the imaginary part
     * of this quantity.
     * 
     * @param phi       the mid-gap synchronous phase angle &phi; (in radians)
     * @param k         the synchronous particle wave number (in radians/meter)
     * 
     * @return          value of the Hamiltonian <i>H</i><sup>+</sup> at &phi; and <i>k</i>
     *
     * @since  Oct 7, 2015,   Christopher K. Allen
     */
    private Complex dkPostGapHamiltonian(double phi, double k) {
        Complex     cpxDkPostSpc = this.spcFldSpc.dkPostEnvSpectrum(k);
        Complex     cpxPostAngle = Complex.euler(phi);
        Complex     cpxDkHamilt  = cpxDkPostSpc.times(cpxPostAngle);
        
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
        Complex     cpxHamil = this.preGapHamiltonain(phi, k);
        Complex     cpxRotat = Complex.IUNIT.negate();
        Complex     cpxDphiHamil = cpxHamil.times(cpxRotat);
        
        return  cpxDphiHamil;
    }
    
    /**
     * Computes and returns the derivative of the post-gap "Hamiltonian" function 
     * <i>H</i><sup>+</sup>(&phi;, <i>k</i>) with respect to the mid-gap 
     * synchronous phase &phi;,
     * that is, the value   <i>dH</i><sup>+</sup>(&phi;, <i>k</i>)/<i>d</i>&phi;.
     * This complex-valued function of variables &phi; and <i>k</i> is given by 
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>dH</i><sup>+</sup>(&phi;, <i>k</i>)/<i>d</i>&phi; = 
     *                                       &Escr;<sup>+</sup>(<i>k</i>) <i>d</i>&exponentiale;<sup>-<i>i</i> &phi;</sup>/<i>d</i>&phi;
     *                                    = -<i>i</i> <i>H</i><sup>+</sup>(&phi;,<i>k</i>) ,
     * <br/>
     * <br/>
     * where &Escr;<sup>+</sup>(<i>k</i>) is the post-envelope spectrum and 
     * &exponentiale;<sup>-<i>i</i> &phi;</sup> is the mid-gap synchronous
     * phase.  
     * The energy gain &Delta;<i>W</i><sup>+</sup> is proportional to the 
     * imaginary part of this quantity.
     * 
     * @param phi       the mid-gap synchronous phase angle &phi; (in radians)
     * @param k         the synchronous particle wave number (in radians/meter)
     * 
     * @return          value of the Hamiltonian <i>H</i><sup>+</sup> at &phi; and <i>k</i>
     *
     * @since  Oct 7, 2015,   Christopher K. Allen
     */
    private Complex dphiPostGapHamiltonian(double phi, double k) {
        Complex     cpxHamil = this.postGapHamiltonain(phi, k);
        Complex     cpxRotat = Complex.IUNIT.negate();
        Complex     cpxDphiHamil = cpxHamil.times(cpxRotat);
        
        return  cpxDphiHamil;
    }
    
    
    //
    // Beam Particle Properties
    //
    
//    /**
//     * Compute the normalized particle velocity &beta; for the given particle
//     * wave number <i>k</i>.
//     * 
//     * @param k     wave number of the particle with respect to RF frequency (radians/meter)
//     * 
//     * @return      the normalized velocity &beta; of the particle for the given wave number <i>k</i>
//     *
//     * @since  Sep 28, 2015   by Christopher K. Allen
//     */
//    private double computeNormVelocity(double k) {
//        double  lambda = DBL_LGHT_SPD/this.getRfFrequency();
//        double  beta   = DBL_2PI/(k*lambda);
//        
//        return beta;
//    }
//    
//    /**
//     * <p>
//     * Compute and return the particle wave number <i>k</i> for the given normalized 
//     * particle velocity &beta;.  The formula is
//     * <br/>
//     * <br/>
//     * &nbsp; &nbsp; <i>k</i> = 2&pi;/&beta;&lambda; ,
//     * <br/>
//     * <br/>
//     * where &lambda; is the wavelength of the accelerating RF.
//     * </p>
//     * 
//     * @param beta      normalized probe velocity
//     * 
//     * @return          particle wave number with respect to the RF (radians/meter)
//     *
//     * @since  Oct 12, 2015   by Christopher K. Allen
//     */
//    private double computeWaveNumber(double beta) {
//        double lambda = DBL_LGHT_SPD/this.getRfFrequency();
//        double k      = DBL_2PI/(beta*lambda);
//
//        return k;
//    }
    
    /**
     * <p>
     * Compute and return the particle wave number <i>k</i> for the given particle energy <i>W</i>
     * and rest energy <i>E<sub>r</sub></i> = <i>mc</i><sup>2</sup>. The formula is
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>k</i> = <i>k</i><sub>0</sub>/&radic;(1 - 1/&gamma;<sup>2</sup>) , 
     * <br/>
     * <br/>
     * where <i>k</i><sub>0</i> = 2&pi;/&lambda; is the wave number of the RF in free space and
     * &gamma; = 1 + <i>W</i>/<i>E<sub>r</sub></i> is the relativistic factor.
     * </p>
     * 
     * @param W         particle kinetic energy <i>W</i> (electron-Volts)
     * @param Er        particle rest mass <i>mc</i><sup>2</sup>/<i>q</i> (electron-Volts)
     * 
     * @return          particle wave number with respect to the RF (radians/meter)
     *
     * @since  Oct 12, 2015   by Christopher K. Allen
     */
    private double computeWaveNumber(double W, double Er) {
        double gamma  = this.computeGammaFromEnergy(W, Er);
        double beta   = Math.sqrt(1.0 - 1.0/(gamma*gamma));
        double k0     = this.getRfWaveNumber();

        double k      = k0/beta;;

        return k;
    }
    
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
     * @param W         particle kinetic energy <i>W</i> (electron-Volts)
     * @param Er        particle rest mass <i>mc</i><sup>2</sup>/<i>q</i> (electron-Volts)
     * 
     * @return          normalized wave number <i>K</i> (in radians/meter)
     *
     * @since  Oct 1, 2015,   Christopher K. Allen
     */
    public double computeNormWaveNumber(double W, double Er) {
        double gamma = this.computeGammaFromEnergy(W, Er);
        double bg    = Math.sqrt(gamma*gamma - 1.0);
        double bg3   = bg * bg * bg;
        
        double V0    = this.getRfFieldPotential();
        double k0    = this.getRfWaveNumber();
        
        double En    = Er*bg3;
        double K     = (V0/En) * k0;
        
        return K;
    }

//    /** 
//     *  Computes the relativistic factor &gamma;from the given &beta; value.
//     *  
//     *  @param  beta    particle velocity normalized  w.r.t. the speed of light
//     *  
//     *  @return         relativistic factor &gamma;
//     */
//    private double computeGammaFromBeta(double beta) { 
//        return 1.0/Math.sqrt(1.0 - beta*beta); 
//    };
//    
    /**
     * Compute and return the relativistic factor &gamma; from the given kinetic
     * energy and given rest energy <i>E<sub>r</sub></i> = <i>mc</i><sup>2</sup>. 
     * This value is given by the formula
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &gamma; = 1 + <i>W</i>/<i>mc</i><sup>2</sup> ,
     * <br/> 
     * <br/>
     * where the energy quantities can be in any (consistent) units but electron-
     * volts are typically used.
     * 
     * @param W     kinetic energy <i>W</i> of particle (electron-Volts)
     * @param Er    rest energy <i>E<sub>r</sub></i> = <i>mc</i><sup>2</sup> of particle (electron-Volts)
     * 
     * @return      the relativistic factor &gamma; for a particle of energy <i>W</i> and
     *              rest energy <i>E<sub>r</sub></i>
     *
     * @since  Oct 12, 2015,   Christopher K. Allen
     */
    private double computeGammaFromEnergy(double W, double Er) {
        double  gamma = 1.0 + W/Er;
        
        return gamma;
    }
    
//    /**
//     * Computes and return the kinetic energy of a particle traveling at the given normalized
//     * velocity which has the given rest mass.  The value <i>W</i> of this quantity is given 
//     * by the following formula:
//     * <br/>
//     * <br/>
//     * &nbsp; &nbsp; <i>W</i> = (&gamma; - 1)<i>mc</i><sup>2</sup> ,
//     * <br/>
//     * <br/>
//     * where &gamma; = 1/&radic;(1 - &beta;<sup>2</sup>) is the relativistic factor and 
//     * <i>mc</i><sup>2</sup> is the particle's rest mass (in electron-Volts). The 
//     * relativistic factor &gamma; is defined by the normalized velocity &beta; which
//     * is, in turn, determined by the wave number <i>k</i>.
//     * 
//     * @param k         particle wave number w.r.t. RF frequency (radians/meter)
//     * @param Er        particle rest mass (in electron-Volts)
//     * 
//     * @return          kinetic energy of a particle of the given rest mass at the given velocity (eV)
//     *
//     * @since  Oct 9, 2015,   Christopher K. Allen
//     */
//    private double computeKineticEnergy(double k, double Er) {
//        double  dblBeta   = this.computeNormVelocity(k);
//        double  dblGamma  = this.computeGammaFromBeta(dblBeta);
//        double  dblKinNrg = (dblGamma - 1.0)*Er;
//        
//        return dblKinNrg;
//    }
}
