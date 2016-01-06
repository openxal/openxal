package xal.model.probe.traj;

import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.model.probe.ParticleProbe;

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

    
    //
    //  Data Persistence
    //
    
    /** label for particle data node */
    private static final String LABEL_PARTICLE = "particle";
    
    /** label for the phase coordinate data node */
    private static final String LABEL_COORDS = "coordinates";
    
    /** label for the response matrix data node */
    private static final String LABEL_RESP = "resp";
    
    
    //
    // Persistence Version
    //
    
    /** the data format version attribute */
    private static final String   ATTR_VERSION = "ver";
    
    /** the data format version */
    private static final int     INT_VERSION = 2;
    

    //
    // Backward Compatibility
    //
    
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
        
        DataAdaptor nodePart = container.createChild(LABEL_PARTICLE);
        nodePart.setValue(ATTR_VERSION, INT_VERSION);
        
        DataAdaptor nodeCoords = nodePart.createChild(LABEL_COORDS);
        this.getPhaseCoordinates().save(nodeCoords);
        
        DataAdaptor nodeResp = nodePart.createChild(LABEL_RESP);
        this.getResponseMatrix().save(nodeResp);;
    }
    
    /**
     * Recover the state values particular to <code>BunchProbeState</code> objects 
     * from the data source.
     *
     *  @param  container   data source represented by a <code>DataAdaptor</code> interface
     * 
     *  @exception DataFormatException     state information in data source is malformatted
     */
    @Override
    protected void readPropertiesFrom(DataAdaptor container) 
            throws DataFormatException {
        super.readPropertiesFrom(container);
        
        DataAdaptor nodePart = container.childAdaptor(LABEL_PARTICLE);
        if (nodePart == null)
            throw new DataFormatException("ParticleProbeState#readPropertiesFrom(): no child element = " + LABEL_PARTICLE);
        
        // Read the version number.  We don't do anything with it since there was no version
        //  attribute before version 2.  But it's here if necessary in the future.
        @SuppressWarnings("unused")
        int     intVersion = 0;
        if (nodePart.hasAttribute(ATTR_VERSION))
            intVersion = nodePart.intValue(ATTR_VERSION);

        // Read data if the old format is found
        if (nodePart.hasAttribute(VALUE_LABEL)) {
            String  strVecFmt = nodePart.stringValue(VALUE_LABEL);
            this.setPhaseCoordinates(new PhaseVector(strVecFmt));
        }
        
        if (nodePart.hasAttribute(RESP_LABEL)) {
            String  strMatFmt = nodePart.stringValue(RESP_LABEL);
            this.setResponseMatrix( new PhaseMatrix(strMatFmt));
        }
        
        
        // This is the current data format version
        try {
            DataAdaptor nodeCoords = nodePart.childAdaptor(LABEL_COORDS);
            if (nodeCoords != null) {
                PhaseVector vecCoords = PhaseVector.loadFrom(nodeCoords);
                this.setPhaseCoordinates(vecCoords);
            }
            
            DataAdaptor nodeResp = nodePart.childAdaptor(LABEL_RESP);
            if (nodeResp != null) {
                PhaseMatrix matResp = PhaseMatrix.loadFrom(nodeResp);
                this.setResponseMatrix(matResp);
            }
            
        } catch (DataFormatException e) {
            e.printStackTrace();
            throw new DataFormatException("The source data was corrupted - " + e.getMessage());
            
        }
        
        
    }
    
}
