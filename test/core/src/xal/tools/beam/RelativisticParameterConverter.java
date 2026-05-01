/*
 * Created on Dec 18, 2003
 *
 */
package xal.tools.beam;

/**
 * @author Christopher K. Allen
 */
public class RelativisticParameterConverter {
    
    
    /** 
     *  Computes the relativistic factor gamma from the current beta value
     *  
     *  @param  beta    speed of probe w.r.t. the speed of light
     *  @return         relativistic factor gamma
     */
    static public double computeGammaFromBeta(double beta) { 
        return 1.0/Math.sqrt(1.0 - beta*beta); 
    };
    
    /**
     *  Convenience function for computing the relativistic factor gamma from a 
     *  particle's kinetic energy and rest energy.
     *
     *  @param  W       kinetic energy of the particle
     *  @param  Er      rest energy of particle
     * 
     *  @return         relativistic factor gamma
     */
    static public double computeGammaFromEnergies(double W, double Er)   {
        double gamma = W/Er + 1.0;
        
        return gamma;
    };
    
    /**
     *  Convenience function for computing the probe's velocity beta (w.r.t. the 
     *  speed of light) from the relativistic factor gamma.
     *
     *  @param beta     relativistic factor gamma
     *  @return         speed of probe (w.r.t. speed of light)
     */
    static public double computeBetaFromGamma(double gamma) {
        double beta = Math.sqrt(1.0 - 1.0/(gamma*gamma));

        return beta;
    };
    

    /**
     *  Convenience function for computing the probe's velocity beta (w.r.t. the 
     *  speed of light) from the particle's kinetic and rest energies.
     *
     * @param  W       kinetic energy of the particle (eV)
     * @param  Er      rest energy of particle (eV)
     *  @return         speed of probe (w.r.t. speed of light)
     */
    static public double computeBetaFromEnergies(double W, double Er) {
        double gamma = computeGammaFromEnergies(W, Er);
        double beta  = computeBetaFromGamma(gamma);

        return beta;
    };
    

    /**
     * Convenience function for computing momentum from kinetic energy
     *
     * @param  W       kinetic energy of the particle (eV)
     * @param  Er      rest energy of particle (eV)
     * @return         particle momentum in eV/c where c is the speed of light
     */ 
    static public double computeMomentumFromEnergies(double W, double Er) {
       double gamma = computeGammaFromEnergies(W, Er);
       double beta = computeBetaFromGamma(gamma);
       
       return beta*gamma*Er;
    }
}
