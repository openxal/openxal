package xal.extension.widgets.olmplot;


import java.awt.Color;
import java.lang.reflect.Method;

import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ParticleProbeState;
import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseVector;

/**
 * Enumeration of all the phase planes for the simulation.  There is
 * an RMS envelope value for each plane. 
 *
 * @author Christopher K. Allen
 * @since   Nov 26, 2012
 */
public enum PLANE {
    
    /** Enumeration for the horizontal plane */
    HOR("getx", "getSigmaX", Color.RED),
    
    /** Enumeration representing the vertical plane */
    VER("gety", "getSigmaY", Color.BLUE),
    
    /** Enumeration representing the longitudinal plane */
    LNG("getz", "getSigmaZ", Color.GREEN);
    
    
    /*
     * Operations
     */
    
    /**
     * Return the color associated with this phase plane.
     *
     * @return  default color for the phase plane
     *
     * @author Christopher K. Allen
     * @since  Nov 26, 2012
     */
    public Color    getColor() {
        return this.clrPlane;
    }
    
    /**
     * Extracts the particle position of the phase plane represented by this enumeration constant
     * from the given particle probe state.
     *
     * @param state <code>ParticleProbeState</code> object containing particle state information
     * 
     * @return      the value of the RMS envelope of this phase plane within the given state
     *
     * @author Christopher K. Allen
     * @since  Nov 26, 2012
     */
    public double   getParticlePos(ParticleProbeState state) {
        try {
            PhaseVector vecPhase  = state.getPhaseCoordinates();
            Method      mthGetPos = PhaseVector.class.getMethod(this.strMthPar);
            Double      dblPosVal = (Double)mthGetPos.invoke(vecPhase);

            return dblPosVal;
            
        } catch (Exception e) {
            String  strMsg = this.getClass().getName() + " is unable to invoke method " + this.strMthPar;
            System.err.println(strMsg);
            e.printStackTrace();
            
            throw new RuntimeException(strMsg, e);
        }
    }
    
    /**
     * Extracts the RMS envelope size of the phase plane represented by this enumeration constant
     * from the given envelope probe state.
     *
     * @param state <code>EnvelopeProbeState</code> object containing RMS envelope information
     * 
     * @return      the value of the RMS envelope of this phase plane within the given state
     *
     * @author Christopher K. Allen
     * @since  Nov 26, 2012
     */
    public double   getRmsEnvelope(EnvelopeProbeState state) {
        try {
            CovarianceMatrix    matSigma  = state.getCovarianceMatrix();
            Method              mthGetEnv = CovarianceMatrix.class.getMethod(this.strMthEnv);
            Double              dblEnvVal = (Double)mthGetEnv.invoke(matSigma);

            return dblEnvVal;
            
        } catch (Exception e) {
            String  strMsg = this.getClass().getName() + " is unable to invoke method " + this.strMthEnv;
            System.err.println(strMsg);
            e.printStackTrace();
            
            throw new RuntimeException(strMsg, e);
        }
    }
    
    
    /*
     * Attributes
     */
    
    /** Method name for ParticleProbeState phase coordinate */
    private final String  strMthPar;
    
    /** Method name for EnvelopeProbeState envelope size */
    private final String  strMthEnv;
    
    /** Color associated with the given phase plane */
    private final Color   clrPlane;
    
    /**
     * Create the enumeration constant saving the name of the
     * method needed to acquire its envelope value.
     * 
     * @param strMthPar name of the method returning the particle coordinate value  
     * @param strMthEnv name of method returning envelope value
     * @param clrPlane  color used for the phase plane of this enumeration
     *
     * @author  Christopher K. Allen
     * @since   Nov 26, 2012
     */
    private PLANE(String strMthPar, String strMthEnv, Color clrPlane) {
        this.strMthPar = strMthPar;
        this.strMthEnv = strMthEnv;
        this.clrPlane  = clrPlane;
    }
    
}