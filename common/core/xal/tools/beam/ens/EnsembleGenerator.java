/*
 * EnsembleGenerator.java
 *
 * Created on November 12, 2002, 7:19 PM
 */

package xal.tools.beam.ens;

import xal.tools.beam.Twiss;




/**
 * Utility class for generating particle ensembles with given statistical 
 * properties.
 *
 * @author  CKAllen
 */
public final class EnsembleGenerator {
    
    
    /**
     * Creates a new ensemble according to the given ensemble descriptor.
     *
     * @param descr description of the desired particle ensemble
     * 
     * @return      particle ensemble object
     * 
     * @throws EnsembleException    general creation exception
     *
     * @author Christopher K. Allen
     * @since  Apr 19, 2011
     */
    public static final Ensemble generate(EnsembleDescriptor descr) throws EnsembleException {
        
        Twiss   twissX = new Twiss(descr.ax, descr.bx, descr.ex);
        Twiss   twissY = new Twiss(descr.ay, descr.by, descr.ey);
        Twiss   twissZ = new Twiss(descr.az, descr.bz, descr.ez);
        
        switch (descr.enmProfile)   {
            
            case EnsembleDescriptor.DIST_NONE:
                throw new EnsembleException("EnsembleGenerator::generate() - statistical profile not specified");
                
            case EnsembleDescriptor.DIST_KV:
                return generateKV(twissX, twissY, twissZ);
                
            case EnsembleDescriptor.DIST_GAUSSIAN_3:
                return generateGaussian(3, twissX, twissY, twissZ);
                
            case EnsembleDescriptor.DIST_GAUSSIAN_4:
                return generateGaussian(4, twissX, twissY, twissZ);
                
            case EnsembleDescriptor.DIST_WATERBAG:
                return generateWaterbag(twissX, twissY, twissZ);
                
            case EnsembleDescriptor.DIST_SEMIGAUSSIAN_3:
                return generateSemiGauss(3, twissX, twissY, twissZ);
                
            case EnsembleDescriptor.DIST_SEMIGAUSSIAN_4:
                return generateSemiGauss(4, twissX, twissY, twissZ);
                
            default:
                throw new EnsembleException("EnsembleGenerator::generate() - statistical profile not supported");
        }
    }
    
    
    
    private static Ensemble generateKV(Twiss csX, Twiss csY, Twiss csZ) throws EnsembleException {
        Ensemble ens = new Ensemble();
        
        return ens;
    }
    
    private static Ensemble generateWaterbag(Twiss csX, Twiss csY, Twiss csZ) throws EnsembleException {
        Ensemble ens = new Ensemble();
        
        return ens;
    }
    
    private static Ensemble generateSemiGauss(int nStd, Twiss csX, Twiss csY, Twiss csZ) throws EnsembleException {
        Ensemble ens = new Ensemble();
        
        return ens;
    }
    
    private static Ensemble generateGaussian(int nStd, Twiss csX, Twiss csY, Twiss csZ) throws EnsembleException {
        Ensemble ens = new Ensemble();
        
        return ens;
    }
    
}
