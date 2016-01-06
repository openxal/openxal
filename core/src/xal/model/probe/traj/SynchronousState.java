/*
 * Created on Jun 1, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package xal.model.probe.traj;

import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.math.r3.R3;
import xal.model.probe.SynchronousProbe;

/**
 * @author Christopher K. Allen
 */
public class SynchronousState extends ProbeState<SynchronousState> {
    

    /*
     * Global Constants
     */

    /** element tag for RF phase */
    protected static final String LABEL_SYNCH = "synch";
    
    /** attribute tag for betatron phase */
    protected static final String ATTR_PHASEBETA = "phasebeta";
    
    /** attribute tag for RF phase */
    protected static final String ATTR_PHASERF = "phaserf";



    /*
     * Local Attributes
     */
     
    /** Synchronous particle position with respect to any RF drive phase */
    private double              m_dblPhsRf;

    /** synchronous particle betatron phase without space charge */
    private R3                  m_vecPhsBeta;
    
    


    /*
     * Initialization
     */
     
     
    /**
     * Default constructor.  Create a new <code>SynchronousState</code> 
     * object with zero state values.
     */
    public SynchronousState() {
        super();
        this.m_dblPhsRf		= 0.0;
        this.m_vecPhsBeta	= R3.zero();
    }
    
    /**
     * Copy constructor for SynchronousState.  Initializes the new
     * <code>SynchronousState</code> objects with the state attributes
     * of the given <code>SynchronousState</code>.
     *
     * @param stateSync     initializing state
     *
     * @author Christopher K. Allen, Jonathan M. Freed
     * @since  Jun 26, 2014
     */
    public SynchronousState(final SynchronousState stateSync){
    	super(stateSync);
    	
    	this.m_dblPhsRf		= stateSync.m_dblPhsRf;
    	this.m_vecPhsBeta	= stateSync.m_vecPhsBeta.clone();
    }

    /**
     * Copy constructor.  Create a new <code>SynchronousState</code> object
     * and initialize the state to that of the specified probe argument.
     *  
     * @param probe     probe containing initializing state information
     */
    public SynchronousState(final SynchronousProbe probe) {
        super(probe);
        this.setBetatronPhase( probe.getBetatronPhase().clone() );
        this.setRfPhase( probe.getRfPhase() );
    }
    
    
    /*
     * Property Accessors 
     */
    
    /**
     * Set the betatron phase of the synchronous particle without space charge.
     * The betatron phase of all three planes is maintained as an 
     * <code>R3</code> vector object.  Thus, the betatron phase of each plane
     * is set simultaneously.
     * 
     * @param vecPhase      vector (psix,psiy,psiz) of betatron phases in <b>radians</b>
     */
    public void     setBetatronPhase(R3 vecPhase)   {
        this.m_vecPhsBeta = vecPhase;
    }
    
    /**
     * Set the phase location of the synchronous particle with respect to the 
     * drive RF power.
     *  
     * @param dblPhase      synchronous particle phase w.r.t. RF in <b>radians</b>
     */
    public void     setRfPhase(double dblPhase) {
        this.m_dblPhsRf = dblPhase;
    }
    
    
    /**
     * Return the betatron phase advances in each plane.
     * 
     * @return  vector (psix,psiy,psiz) of betatron phases in <b>radians</b>
     */
    public R3   getBetatronPhase()  {
        return this.m_vecPhsBeta;
    }
    
    /**
     * Return the phase location of the synchronous particle with respect
     * to any driving RF power.
     * 
     * @return      phase location of synchronous particle in <b>radians</b>
     */
    public double   getRfPhase()    {
        return this.m_dblPhsRf;    
    }
    


    /*
     * ProbeState Overrides
     */

    /**
     * Implements the clone operation required by the base class
     * <code>ProbeState</code>
     *
     * @see xal.model.probe.traj.ProbeState#copy()
     *
     * @author Christopher K. Allen
     * @since  Jun 27, 2014
     */
    @Override
    public SynchronousState copy() {
        return new SynchronousState(this);
    }
    
    /**
     * Save the probe state values to a data store represented by the 
     * <code>DataAdaptor</code> interface.
     * 
     * @param daptSink     data sink to receive state information
     * 
     * @see xal.model.probe.traj.ProbeState#addPropertiesTo(gov.DataAdaptor.tools.data.IDataAdaptor)
     */
    @Override
    protected void addPropertiesTo(DataAdaptor daptSink) {
        super.addPropertiesTo(daptSink);

        DataAdaptor daptSync = daptSink.createChild(SynchronousState.LABEL_SYNCH);
        daptSync.setValue(SynchronousState.ATTR_PHASEBETA, this.getBetatronPhase().toString());
        daptSync.setValue(SynchronousState.ATTR_PHASERF, this.getRfPhase());
    }

    /**
     * Restore the state values for this probe state object from the data store
     * represented by the <code>DataAdaptor</code> interface.
     * 
     * @param   daptSrc             data source for probe state information
     * @throws  DataFormatException    error in data format
     * 
     * @see xal.model.probe.traj.ProbeState#readPropertiesFrom(gov.DataAdaptor.tools.data.IDataAdaptor)
     */
    @Override
    protected void readPropertiesFrom(DataAdaptor daptSrc)
        throws DataFormatException 
    {
        super.readPropertiesFrom(daptSrc);

        DataAdaptor daptSync = daptSrc.childAdaptor(SynchronousState.LABEL_SYNCH);
        if (daptSync == null)
            throw new DataFormatException("SynchronousState#readPropertiesFrom(): no child element = " + LABEL_SYNCH);
        
        if (daptSync.hasAttribute(SynchronousState.ATTR_PHASEBETA))   {
            String  strBeta = daptSync.stringValue(SynchronousState.ATTR_PHASEBETA);
            this.setBetatronPhase( new R3( strBeta ) );
        }
        if (daptSync.hasAttribute(SynchronousState.ATTR_PHASERF))
            this.setRfPhase( daptSync.doubleValue(SynchronousState.ATTR_PHASERF) );
    }

    
    /*
     * Object Overrides
     */
    
    /**
     * Returns a string representation of this particle state.  Currently returns only 
     * the super class implementation.
     * 
     * @return      the value <code>super.toString()</code>
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return super.toString();
    }

}
