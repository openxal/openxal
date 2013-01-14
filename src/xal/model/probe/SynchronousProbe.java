/*
 * Created on May 28, 2004
 *
 */
package xal.model.probe;

import xal.tools.data.DataAdaptor;
import xal.tools.math.r3.R3;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.SynchronousState;
import xal.model.probe.traj.SynchronousTrajectory;
import xal.model.xml.ParsingException;



/**
 * This class represents the behavior of the synchronous particle of a particle
 * beam bunch.  Thus, its use is intended for evaluation of machine designs and
 * the variation of design trajectories with respect to machine parameters. 
 * 
 * @author Christopher K. Allen
 */
public class SynchronousProbe extends Probe {




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
     * 
     */
    public SynchronousProbe() {
        super();
        this.setRfPhase(0.0);
        this.setBetatronPhase( new R3() );
    }

    /**
     * @param probe
     */
    public SynchronousProbe(SynchronousProbe probe) {
        super(probe);
        this.setRfPhase( probe.getRfPhase() );
        this.setBetatronPhase( probe.getBetatronPhase() );
    }




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
    
    
    /*
     * Attribute Query
     */
     
     
     /**
      * Return the phase of the synchronous particle with respect to any 
      * RF drive power.
      * 
      * @return     phase location of synchronous particle in <b>radians</b>
      */
     public double  getRfPhase()    {
         return this.m_dblPhsRf;
     }
     
     /**
      * Return the set of betatron phase advances for any particle in the 
      * synchronous bucket.  The phase advances for all threes planes are 
      * returned.  Space charge is not considered.
      * 
      * @return vector (sigx,sigy,sigz) of phase advances without space charge
      */
     public R3      getBetatronPhase()  {
         return this.m_vecPhsBeta;
     }
    
    
    


    /*
     * Trajectory Support
     */

    /**
     * Create and return a <code>Trajectory</code> object of the appropriate
     * specialty type - here <code>SynchronousTrajectory</code>.  The 
     * trajectory object is empty, containing no particle history.
     * 
     * @return  empty trajectory object for this probe type
     * 
     * @see xal.model.probe.Probe#createTrajectory()
     */
     @Override
     public SynchronousTrajectory createTrajectory() {
         return new SynchronousTrajectory();
    }
     
    /**
     * Return a new <code>ProbeState</code> object, of the appropriate type,
     * initialized to the current state of this probe.
     * 
     * @return  probe state object of type <code>SynchronousState</code>
     * 
     * @see xal.model.probe.Probe#createProbeState()
     */
     @Override
     public SynchronousState createProbeState() {
         return new SynchronousState(this);
    }
     
    /**
     * Capture the current probe state to the <code>ProbeState</code> argument.  Note
     * that the argument must be of the concrete type <code>SynchronousState</code>.
     * 
     * @param   state   <code>ProbeState</code> to receive this probe's state information
     * 
     * @exception IllegalArgumentException  argument is not of type <code>SynchronousState</code>
     */   
    @Override
    public void applyState(ProbeState state) {
        if (!(state instanceof SynchronousState))
            throw new IllegalArgumentException("invalid probe state");
        SynchronousState    stateSync = (SynchronousState) state;
        
        super.applyState(state);
        this.setBetatronPhase( stateSync.getBetatronPhase() );
        this.setRfPhase( stateSync.getRfPhase() );
    }
    
    @Override
    protected ProbeState readStateFrom(DataAdaptor container) throws ParsingException {
        SynchronousState state = new SynchronousState();
        state.load(container);
        return state;
    }
}
