package xal.model.probe.traj;

import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.data.DataAdaptor;
import xal.model.probe.ParticleProbe;
import xal.model.xml.ParsingException;

/**
 * Encapsulates the state of a <code>ParticleProbe</code> at a particular point
 * in time.
 * 
 * @author Craig McChesney
 * @version $id:
 * 
 */
public class ParticleProbeState extends ProbeState<ParticleProbeState> /*implements ICoordinateState */ {



    /*
     * Global Constants
     */

    /** element tag for particle data */
    protected static final String PARTICLE_LABEL = "particle";
    
    /** attribute tag for coordinate vector */
    private static final String VALUE_LABEL = "coordinates";
    
    /** attribute tag for response matrix */
    private static final String  RESP_LABEL = "response";
    
    protected static final String X_LABEL = "x";
    protected static final String Y_LABEL = "y";
    protected static final String Z_LABEL = "z";
    protected static final String XP_LABEL = "xp";
    protected static final String YP_LABEL = "yp";
    protected static final String ZP_LABEL = "zp";
    

    /*
     *  Local Attributes
     */
    
    /** phase coordinates of the particle location */ 
    private PhaseVector     m_vecCoords;
    
    /** response matrix for initial coordinate sensitivity */
    private PhaseMatrix     matResp;
    
	
	
    /*
     * Initialization
     */	

    /**
     * Default constructor.  Creates a new, empty <code>ParticleProbeState</code> object.
     */	
    public ParticleProbeState() {
    	super();
        this.m_vecCoords	= PhaseVector.newZero();
        this.matResp		= PhaseMatrix.identity();
    }
    
    
    /**
     * Copy constructor for ParticleProbeState.  Initializes the new
     * <code>ParticleProbeState</code> objects with the state attributes
     * of the given <code>ParticleProbeState</code>.
     *
     * @param particleProbeState     initializing state
     *
     * @author Christopher K. Allen, Jonathan M. Freed
     * @since  Jun 26, 2014
     */
    public ParticleProbeState(final ParticleProbeState particleProbeState){
    	super(particleProbeState);
    	
    	this.m_vecCoords	= particleProbeState.m_vecCoords.clone();
    	this.matResp		= particleProbeState.matResp.clone();
    }
    
    /**
     * Initializing constructor.  Creates a new <code>ParticleProbeState</code> object
     * which contains a deep copy of the state of the given probe object.
     * 
     * @param probe     <code>ParticleProbe</code> containing cloned initial state data
     */
    public ParticleProbeState(final ParticleProbe probe) {
        super(probe);
        this.setPhaseCoordinates( new PhaseVector(probe.getPhaseCoordinates().clone()) );
        this.setResponseMatrix( new PhaseMatrix(probe.getResponseMatrix().clone()) );
    }
    
    
    /*
     * Property Accessors
     */
    
    /** 
     *  Set the phase space coordinates of the probe.  This is the location <b>z</b>
     *  in homogeneous phase space coordinates <b>R</b><sup>6</sup> &times; {1}.
     *
     *  @param  vecPhase    new homogeneous phase space coordinate vector 
     *                      <b>z</b> = (<i>x, x', y, y', z, z', </i>1)<sup><i>T</i></sup>
     */
    public void setPhaseCoordinates(PhaseVector vecPhase) {
        this.m_vecCoords = new PhaseVector(vecPhase);
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
        this.matResp = matResp;
    }
    
    /** 
     *  <p>
     *  Returns homogeneous phase space coordinates of the particle.  The units
     *  are meters and radians.
     *  </p>
     *  This is the location <b>z</b>
     *  in homogeneous phase space coordinates <b>R</b><sup>6</sup> &times; {1}.
     *
     *
     *  @return     vector <b>z</b> = (<i>x,x',y,y',z,z',</i>1)<sup><i>T</i></sup> of phase space coordinates
     */
    public PhaseVector getPhaseCoordinates() {
        return this.m_vecCoords;
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
        return matResp;
    }

    /**
	 * Get the fixed orbit about which betatron oscillations occur.
	 * @return the reference orbit vector (x,x',y,y',z,z',1)
	 * 
	 * @deprecated This is a duplicate of {@link #getPhaseCoordinates()} but with a 
	 *             misleading name.  I plan to get rid of it.
	 */
    @Deprecated
	public PhaseVector getFixedOrbit() {
        return this.m_vecCoords;
	}
    


    /*
     * Object Overrides
     */
     
    /**
     * Write out the state information in text form.
     * 
     * @return  internal state information as a <code>String</code>.
     */
    @Override
    public String toString() {
        return super.toString() + " coords: " + getPhaseCoordinates().toString();
    }       
    

    /*
     * ProbeState Overrides
     */    
     
    /**
     * Copies and returns a new, identical instance of <b>this</b> 
     * <code>ParticleProbeState</code>.
     * 
     * @return a copy of <b>this</b> <code>ParticleProbeState</code>
     */
    @Override
    public ParticleProbeState copy(){
        return new ParticleProbeState(this);
    }
     
    /**
     * Save the state values particular to <code>BunchProbeState</code> objects
     * to the data sink.
     * 
     *  @param  container   data sink represented by <code>DataAdaptor</code> interface
     */
    @Override
    protected void addPropertiesTo(DataAdaptor container) {
        super.addPropertiesTo(container);
        
        DataAdaptor partNode = container.createChild(PARTICLE_LABEL);
        partNode.setValue(VALUE_LABEL, getPhaseCoordinates().toString());
        partNode.setValue(RESP_LABEL, this.getResponseMatrix().toString());
    }
    
    /**
     * Recover the state values particular to <code>BunchProbeState</code> objects 
     * from the data source.
     *
     *  @param  container   data source represented by a <code>DataAdaptor</code> interface
     * 
     *  @exception ParsingException     state information in data source is malformatted
     */
    @Override
    protected void readPropertiesFrom(DataAdaptor container) 
            throws ParsingException {
        super.readPropertiesFrom(container);
        
        DataAdaptor partNode = container.childAdaptor(PARTICLE_LABEL);
        if (partNode == null)
            throw new ParsingException("ParticleProbeState#readPropertiesFrom(): no child element = " + PARTICLE_LABEL);
        
        String  strVecFmt = partNode.stringValue(VALUE_LABEL);
        String  strMatFmt = partNode.stringValue(RESP_LABEL);
        
        setPhaseCoordinates(new PhaseVector(strVecFmt));
        setResponseMatrix( new PhaseMatrix(strMatFmt));
    }
    
}
