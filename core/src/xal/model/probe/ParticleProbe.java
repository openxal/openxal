/*
 * ParticleProbe.java
 *
 * Created on August 13, 2002, 4:22 PM
 */

package xal.model.probe;


import xal.tools.annotation.AProperty.NoEdit;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.model.probe.traj.ParticleProbeState;
import xal.model.probe.traj.Trajectory;


/**
 *  ParticleProbe extends the base BeamProbe to manage a particle state which is a Vector
 *  of homogeneous phase space variables.
 *
 * @author Christopher K. Allen
 * @author Craig McChesney
 * @since Aug 13, 2002
 * @version Nov 14, 2013
 * 
 */

public class ParticleProbe extends Probe<ParticleProbeState> {


    /*
     * Initialization
     */
    
    /** 
     *  Default constructor for ParticleProbe. 
     *  Creates a new (empty) instance of ParticleProbe.
     */
    public ParticleProbe() {
        super( );
        
        this.setPhaseCoordinates(new PhaseVector());
        this.setResponseMatrix(PhaseMatrix.identity());
    }
    
    /**
     *  Copy constructor for ParticleProbe.  Performs deep copy
     *  of target particle probe.
     *
     *  @param  probe   ParticleProbe object to be cloned
     */
    public ParticleProbe(final ParticleProbe probe)   {
        super(probe);
        
        // Copy phase coordinate vector
        this.setPhaseCoordinates( new PhaseVector(probe.getPhaseCoordinates()) );
        this.setResponseMatrix( new PhaseMatrix(probe.getResponseMatrix()) );
    }
    
    /**
     * Creates a clone of this <code>ParticleProbe</code> object and returns it.
     * This method is essentially a proxy to the constructor {@link #ParticleProbe(ParticleProbe)}.
     *
     * @see xal.model.probe.Probe#copy()
     *
     * @author Christopher K. Allen
     * @since  Nov 14, 2013
     */
    @Override
    public ParticleProbe copy() {
        return new ParticleProbe( this );
    }
    
	
    /** 
     *  Set the phase space coordinates of the probe.  This is the location <b>z</b>
     *  in homogeneous phase space coordinates <b>R</b><sup>6</sup> &times; {1}.
     *
     *  @param  vecPhase    new homogeneous phase space coordinate vector 
     *                      <b>z</b> = (<i>x, x', y, y', z, z', </i>1)<sup><i>T</i></sup>
	 */
    public void setPhaseCoordinates(PhaseVector vecPhase) {
    	this.stateCurrent.setPhaseCoordinates(vecPhase);
    }
	
    
    /**
     * <p>
     * Set the response matrix <b>&Phi;</b> for the particle at the given
     * state location <b>z</b>.  The response matrix represents the sensitivity of
     * the current phase coordinate position <b>z</b> to the initial phase coordinate
     * location <b>z</b><sub>0</sub> at the start of the simulation.  That is,
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>&Phi;</b> &equiv; &part;<b>z</b>/&part;<b>z</b><sub>0</sub>
     * <br>
     * <br>
     * so that small changes &Delta;<b>z</b><sub>0</sub> in the initial phase position
     * yield a corresponding change &Delta;<b>z</b> = <b>&Phi;</b>&Delta;<b>z</b><sub>0</sub>
     * in the current particle location.
     * </p>
     * 
     * @param matResp   the response matrix <b>&Phi;</b> &equiv; 
     *                  &part;<b>z</b>/&part;<b>z</b><sub>0</sub>the matResp to set
     */
    public void setResponseMatrix(PhaseMatrix matResp) {
        this.stateCurrent.setResponseMatrix(matResp);
    }

    /*
     * Attribute Query
     */
	
	/** 
     *  Returns homogeneous phase space coordinates of the particle.  The units
     *  are meters and radians.
     *
     *  @return     vector (x,x',y,y',z,z',1) of phase space coordinates
     */
    public PhaseVector getPhaseCoordinates()  { 
    	return this.stateCurrent.getPhaseCoordinates();
    }

    /**
     * <p>
     * Returns the response matrix <b>&Phi;</b> for the particle at the given
     * state location <b>z</b>.  The response matrix represents the sensitivity of
     * the current phase coordinate position <b>z</b> to the initial phase coordinate
     * location <b>z</b><sub>0</sub> at the start of the simulation.  That is,
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>&Phi;</b> &equiv; &part;<b>z</b>/&part;<b>z</b><sub>0</sub>
     * <br>
     * <br>
     * so that small changes &Delta;<b>z</b><sub>0</sub> in the initial phase position
     * yield a corresponding change &Delta;<b>z</b> = <b>&Phi;</b>&Delta;<b>z</b><sub>0</sub>
     * in the current particle location.
     * </p>
     * 
     * @return the response matrix <b>&Phi;</b> &equiv; 
     *          &part;<b>z</b>/&part;<b>z</b><sub>0</sub>
     */
    public PhaseMatrix getResponseMatrix() {
        return this.stateCurrent.getResponseMatrix();
    }

    /**
     * Get the fixed orbit about which betatron oscillations occur.
     * @return the reference orbit vector (x,x',y,y',z,z',1)
     * 
     * @deprecated This is a duplicate of {@link #getPhaseCoordinates()} but with a 
     *             misleading name.  I plan to get rid of it.
     */
	@NoEdit
    @Deprecated
	public PhaseVector getFixedOrbit() {
		return this.stateCurrent.getFixedOrbit();	
	}

    

    /*
     * Trajectory Support
     */

	/**
	 * Creates a <code>Trajectory&lt;ParticleProbeState&gt;</code> object of the
	 * proper type for saving the probe's history.
	 * 
	 * @return a new, empty <code>Trajectory&lt;ParticleProbeState&gt;</code> 
	 * 		for saving the probe's history
	 * 
	 * @author Jonathan M. Freed
	 */
    @Override
    public Trajectory<ParticleProbeState> createTrajectory() {
        return new Trajectory<ParticleProbeState>(ParticleProbeState.class);
    }
    
    /**
     * Captures the probe's state and return it as a new <code>ProbeState</code>
     * object.
     * 
     * @return  new <code>ParticleProbeState</code> object initialized to current state
     * 
     * @author Christopher K. Allen
     * @since  Aug 13, 2002
     * @version Nov 14, 2013
     * 
     * @see xal.model.probe.Probe#createProbeState()
     */
    @Override
    public ParticleProbeState createProbeState() {
	        return new ParticleProbeState(this);
    }
    
	/**
	 * Creates a new, empty <code>ParticleProbeState</code>.
	 * 
	 * @return a new, empty <code>ParticleProbeState</code>
	 * 
	 * @author Jonathan M. Freed
	 * @since Jul 1, 2014
	 */
	@Override
	public ParticleProbeState createEmptyProbeState(){
		return new ParticleProbeState();
	}
    
    
//    /**
//     * Capture the current probe state to the <code>ProbeState</code> argument.  Note
//     * that the argument must be of the concrete type <code>ParticleProbeState</code>.
//     * 
//     * @param   state   <code>ProbeState</code> to receive this probe's state information
//     * 
//     * @exception IllegalArgumentException  argument is not of type <code>ParticleProbeState</code>
//     */   
//    @Override
//    public void applyState(ParticleProbeState state) {
//        
//        // Check if state is the right type
//        if (!(state instanceof ParticleProbeState))
//            throw new IllegalArgumentException("invalid probe state");
//        
//        ParticleProbeState  pps = (ParticleProbeState)state;
//        
//        // Set the properties of this probe according to the probe state
//        super.applyState(pps);
//        this.setPhaseCoordinates( pps.getPhaseCoordinates() );
//        this.setResponseMatrix( pps.getResponseMatrix() );
//    }
    
    @Override
    protected ParticleProbeState readStateFrom(DataAdaptor container) throws DataFormatException {
        ParticleProbeState state = new ParticleProbeState();
        state.load(container);
        return state;
    }
}
