/*
 * ParticleProbe.java
 *
 * Created on August 13, 2002, 4:22 PM
 */

package xal.model.probe;


import xal.tools.beam.PhaseVector;
import xal.tools.data.DataAdaptor;

import xal.model.probe.traj.ParticleProbeState;
import xal.model.probe.traj.ParticleTrajectory;
import xal.model.probe.traj.ProbeState;
import xal.model.xml.ParsingException;


/**
 *  ParticleProbe extends the base BeamProbe to manage a particle state which is a Vector
 *  of homogeneous phase space variables.
 *
 * @author Christopher K. Allen
 * @author Craig McChesney
 */

public class ParticleProbe extends Probe {

    
    /*
     *  Local Attributes
     */
    
    /** phase coordinates of the particle location */ 
    private PhaseVector     m_vecCoords;
    
    

    /*
     * Initialization
     */
    
    /** 
     *  Default constructor for ParticleProbe. 
     *  Creates a new (empty) instance of ParticleProbe.
     */
    public ParticleProbe() {
        super( );
        m_vecCoords = new PhaseVector();
    }
    
    /**
     *  Copy constructor for ParticleProbe.  Performs deep copy
     *  of target particle probe.
     *
     *  @param  probe   ParticleProbe object to be cloned
     */
    public ParticleProbe(ParticleProbe probe)   {
        super(probe);
        
        // Copy phase coordinate vector
        this.setPhaseCoordinates( probe.phaseCoordinates() );
    }
    
    @Override
    public ParticleProbe copy() {
        return new ParticleProbe( this );
    }
    
	
    /** 
     *  Returns homogeneous phase space coordinates of the particle.  The units
     *  are meters and radians.
     *
     *  @return     vector (x,x',y,y',z,z',1) of phase space coordinates
     */
    public PhaseVector phaseCoordinates()  { 
    	return this.m_vecCoords;
    }
	
    
    /** 
	 *  Set the phase coordinates of the probe.  
	 *
	 *  @param  vecPhase    new homogeneous phase space coordinate vector
	 */
    public void setPhaseCoordinates(PhaseVector vecPhase) {
        this.m_vecCoords = new PhaseVector(vecPhase);
    }
	
	
	/**
	 * Get the fixed orbit about which betatron oscillations occur.
	 * @return the fixed orbit vector (x,x',y,y',z,z',1)
	 */
	public PhaseVector getFixedOrbit() {
    	return this.m_vecCoords;		
	}

    

    /*
     * Trajectory Support
     */

    /**
     * Creates a new <code>Trajectory</code> object for <code>ParticleProbe</code> types.
     * 
     * @return  new, empty <code>ParticleTrajectory</code> object
     */
    @Override
    public ParticleTrajectory createTrajectory() {
        return new ParticleTrajectory();
    }
    
    /**
     * Captures the probe's state and return it as a new <code>ProbeState</code>
     * object.
     * 
     * @return  new <code>ParticleProbeState</code> object initialized to current state
     */
    @Override
    public ParticleProbeState createProbeState() {
        return new ParticleProbeState(this);
    }
    
    
    /**
     * Capture the current probe state to the <code>ProbeState</code> argument.  Note
     * that the argument must be of the concrete type <code>ParticleProbeState</code>.
     * 
     * @param   state   <code>ProbeState</code> to recieve this probe's state information
     * 
     * @exception IllegalArgumentException  argument is not of type <code>ParticleProbeState</code>
     */   
    @Override
    public void applyState(ProbeState state) {
        if (!(state instanceof ParticleProbeState))
            throw new IllegalArgumentException("invalid probe state");
        super.applyState(state);
        setPhaseCoordinates(((ParticleProbeState)state).phaseCoordinates());
    }
    
    @Override
    protected ProbeState readStateFrom(DataAdaptor container) throws ParsingException {
        ParticleProbeState state = new ParticleProbeState();
        state.load(container);
        return state;
    }
}
