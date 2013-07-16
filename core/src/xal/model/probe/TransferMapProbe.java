/*
 * Created on May 28, 2004
 *
 *  Copyright SNS/LANL, 2004
 * 
 */
package xal.model.probe;

import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseVector;
import xal.tools.data.DataAdaptor;

import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.TransferMapState;
import xal.model.probe.traj.TransferMapTrajectory;
import xal.model.xml.ParsingException;


/**
 * <p>
 * Probe that tracks all the transfer maps between modeling elements.
 * Note there is no beam dynamics <i>per se</i>, the probe simply collects all
 * the transfer maps as provided by the beamline elements for the design 
 * synchronous particle.
 * </p>
 * <p>
 * If you wish to compute the transfer matrices for an envelope model that includes
 * space charge effects, then you should employ and <code>{@link EnvelopeProbe}</code>
 * and call the method <code>{@link EnvelopeProbeState#getResponseMatrix}</code>.
 * </p>
 * 
 * @author Christopher K. Allen
 * @since  May 28, 2004
 */
public class TransferMapProbe extends Probe {
    /** composite transfer map */
    private PhaseMap m_mapTrans;
    
    /** phase coordinates of the particle location */ 
    private PhaseVector _phaseCoordinates;
    
    
    /**
     * Default constructor.  Create a new <code>TransferMapProbe</code> with
     * zero initial state.
     */
    public TransferMapProbe() {
        super();
        
        m_mapTrans = PhaseMap.identity();
        _phaseCoordinates = new PhaseVector();
    }

    /**
     * sako for turn by turn running, call instead of reset
     *
     *
     * @author H. Sako
     * @since  Apr 14, 2011
     */
    public void setupNextTurn() {
        this.setPosition(0);
        PhaseVector pv = this.phaseCoordinates();
        this.m_trajHist = this.createTrajectory();
        pv.setz(0);
        this.setPhaseCoordinates(pv);
    }

    /**
     * Initializing constructor.  Create a new <code>TransferMapProbe</code> and
     * initialize its state to that of the given probe.
     * @param probe probe containing initial state information
     */
    public TransferMapProbe( final TransferMapProbe probe ) {
        super(probe);
        
        this.setTransferMap( new PhaseMap( probe.m_mapTrans) );
        this.setPhaseCoordinates( new PhaseVector(probe._phaseCoordinates) );
    }
    
    @Override
    public TransferMapProbe copy() {
        return new TransferMapProbe( this );
    }
    
    
    /**
     * Set the current composite transfer map up to the current probe
     * location.
     * 
     * @param   mapTrans    transfer map in homogeneous phase coordinates
     * 
     * @see xal.model.probe.Probe#createTrajectory()
     */
    public void setTransferMap(PhaseMap mapTrans)   {
        this.m_mapTrans = mapTrans;
    }
    
    
     /**
      * Get the composite transfer map for the current probe location.
      * 
      * @return transfer map in homogeneous phase space coordinates
      */
     public PhaseMap getTransferMap()  {
         return this.m_mapTrans;
     }
    
     
    /** 
     *  Returns homogeneous phase space coordinates of the particle.  The units
     *  are meters and radians.
     *  @return vector (x,x',y,y',z,z',1) of phase space coordinates
     */
    public PhaseVector phaseCoordinates()  { 
        return _phaseCoordinates;
    }
    
    
    /** 
     *  Set the phase coordinates of the probe.  
     *  @param  vecPhase new homogeneous phase space coordinate vector
     */
    public void setPhaseCoordinates( final PhaseVector vecPhase ) {
        _phaseCoordinates = new PhaseVector( vecPhase );
    }
    
     
    /**
     * Create and return a <code>Trajectory</code> object of the appropriate
     * specialty type - here <code>TransferMapTrajectory</code>.  The 
     * trajectory object is empty, containing no particle history.
     * @return  empty trajectory object for this probe type
     * @see xal.model.probe.Probe#createTrajectory()
     */
    @Override
    public TransferMapTrajectory createTrajectory() {
        return new TransferMapTrajectory();
    }
    
    
    /**
     * Return a new <code>ProbeState</code> object, of the appropriate type,
     * initialized to the current state of this probe.
     * @return  probe state object of type <code>TransferMapState</code>
     * @see xal.model.probe.Probe#createProbeState()
     */
    @Override
    public TransferMapState createProbeState() {
        return new TransferMapState(this);
    }
    
    
    /**
     * Capture the current probe state to the <code>ProbeState</code> argument.  Note
     * that the argument must be of the concrete type <code>TransferMapState</code>.
     * @param   state   <code>ProbeState</code> to receive this probe's state information
     * @exception IllegalArgumentException  argument is not of type <code>TransferMapState</code>
     */
    @Override
    public void applyState( final ProbeState state ) {
        if ( !(state instanceof TransferMapState) ) throw new IllegalArgumentException("invalid probe state");
        final TransferMapState stateTrans = (TransferMapState) state;
        
        super.applyState(state);
        stateTrans.setTrajectory( (TransferMapTrajectory)m_trajHist );
        setTransferMap( stateTrans.getTransferMap() );
        setPhaseCoordinates( stateTrans.phaseCoordinates() );
    }
    
    
    /**
     * Subclasses should override this method to perform any required post processing upon completion 
     * of algorithm processing.  This method implementation does nothing.
     */
    @Override
    public void performPostProcessing() {
        final PhaseMap fullTurnMap = m_mapTrans.copy();
        ((TransferMapTrajectory)getTrajectory()).setFullTurnMap( fullTurnMap );
    }
    
    
    /**
     * Initialize this probe from the one specified.
     * @param probe the probe from which to initialize this one
     */
    @Override
    protected void initializeFrom( final Probe probe ) {
        super.initializeFrom( probe );
        
        applyState( probe.createProbeState() );
        createTrajectory();
    }
    
    @Override
    protected ProbeState readStateFrom(DataAdaptor container) throws ParsingException {
        TransferMapState state = new TransferMapState();
        state.load(container);
        return state;
    }
}
