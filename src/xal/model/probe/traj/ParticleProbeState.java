package xal.model.probe.traj;

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
public class ParticleProbeState extends ProbeState implements ICoordinateState {



    /*
     * Global Constants
     */

    /** element tag for particle data */
    protected static final String PARTICLE_LABEL = "particle";
    
    /** attribute tag for coordinate vector */
    private static final String VALUE_LABEL = "coordinates";
    
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
    
    
	
	
    /*
     * Initialization
     */	

    /**
     * Default constructor.  Creates a new, empty <code>ParticleProbeState</code> object.
     */	
    public ParticleProbeState() {
        this.m_vecCoords = new PhaseVector();
    }
    
    /**
     * Initializing constructor.  Creates a new <code>ParticleProbeState</code> object and
     * initialize it to the state of the probe argument.
     * 
     * @param probe     <code>ParticleProbe</code> containing initial state data
     */
    public ParticleProbeState(ParticleProbe probe) {
        super(probe);
        this.setPhaseCoordinates( probe.getPhaseCoordinates() );
    }
    
    /** 
     *  Set the phase coordinates of the probe.  
     *
     *  @param  vecPhase    new homogeneous phase space coordinate vector
     */
    public void setPhaseCoordinates(PhaseVector vecPhase) {
        this.m_vecCoords = new PhaseVector(vecPhase);
    }


    
    /*
     * Data Query
     */    
    
    /** 
     *  Returns homogeneous phase space coordinates of the particle.  The units
     *  are meters and radians.
     *
     *  @return     vector (x,x',y,y',z,z',1) of phase space coordinates
     */
    public PhaseVector getPhaseCoordinates() {
        return this.m_vecCoords;
    }
	
	
	/**
	 * Get the fixed orbit about which betatron oscillations occur.
	 * @return the reference orbit vector (x,x',y,y',z,z',1)
	 */
	public PhaseVector getFixedOrbit() {
        return this.m_vecCoords;
	}
    


    /*
     * Debugging
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
     * Internal Support
     */    
     
     
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
        
        setPhaseCoordinates(new PhaseVector(partNode.stringValue(VALUE_LABEL)));
    }
    
}
