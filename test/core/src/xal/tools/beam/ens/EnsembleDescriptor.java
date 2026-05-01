/*
 * BeamDescriptor.java
 *
 * Created on November 12, 2002, 5:58 PM
 */

package xal.tools.beam.ens;



/**
 *
 * @author  CKAllen
 */
public final class EnsembleDescriptor {

    /*
     *  Enumeration of Supported Distributions
     */
    
    /** No distribution profile specified - usually indicates error condition */
    public static final int DIST_NONE = 0;
    
    /** Kapchinskij-Vladimirskij (or canonical) distribution - uniformly distributed on phase-space surface */
    public static final int DIST_KV = 1;
    
    /** Waterbag distribution - uniform in 6D phase space */
    public static final int DIST_WATERBAG = 2;
    
    /** Parabolic distribution - parabolic in 6D phase space */
    public static final int DIST_PARABOLIC = 3;
    
    /** Semi-Gaussian distribution - uniform in 3D configuration, gaussian in momentum w/ 3 std cutoff */
    public static final int DIST_SEMIGAUSSIAN_3 = 4;
    
    /** Semi-Gaussian distribution - uniform in 3D configuration, gaussian in momentum w/ 4 std cutoff */
    public static final int DIST_SEMIGAUSSIAN_4 = 5;
    
    /** Gaussian distribution - gaussian in 6D phase space w/ 3 standard deviations cutoff */
    public static final int DIST_GAUSSIAN_3 = 6;
    
    /** Gaussian distribution - gaussian in 6D phase space w/ 3 standard deviations cutoff */
    public static final int DIST_GAUSSIAN_4 = 7;

    
    
    /*
     *  Public Attributes
     */
    
    /** statistical distribution of particle phase coordinates in ensemble */
    public int      enmProfile = DIST_NONE;
    
    /** number of particles in ensemble */
    public int      nCnt = 0;
    
    /** Twiss alpha parameter in x plane */
    public double   ax = 0.0;
    
    /** Twiss beta parameter in x plane */
    public double   bx = 0.0;
    
    /** beam rms emittance in x plane */
    public double   ex = 0.0;
    
    /** Twiss alpha parameter in y plane */
    public double   ay = 0.0;
    
    /** Twiss beta parameter in y plane */
    public double   by = 0.0;
    
    /** beam rms emittance in y plane */
    public double   ey = 0.0;
    
    /** Twiss alpha parameter in z plane */
    public double   az = 0.0;
    
    /** Twiss beta parameter in z plane */
    public double   bz = 0.0;
    
    /** beam rms emittance in z plane */
    public double   ez = 0.0;
    
};
