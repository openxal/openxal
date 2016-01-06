/*
 * Created on Jun 9, 2004
 *
 * Copyright 2004 by SNS/LANL
 */

package xal.model.probe.traj;

import xal.tools.beam.PhaseMap;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.model.probe.Probe;
import xal.model.probe.TransferMapProbe;


/**
 * <p>
 * Probe state for the transfer map tracker.
 * </p>
 * <h3>NOTES: CKA</h3>
 * <p>
 * &middot; I noticed a very brittle situation with this implementation.  
 * <code>ICoordinateState</code> requires the methods 
 * <code>@link ICoordinateState#getPhaseCoordinates()}</code> etc.
 * Interface <code>ICoordinateState</code> extends <code>IProbeState</code>.
 * Well, <code>TransferMapState</code> inherits from <code>ProbeState</code>
 * which implements <code>IProbeState</code>.  It's getting dicey.
 * <br>
 * &middot; The <code>TransferMapState</code> is really meant to compute
 * transfer maps only, and be very light weight.  If you want to compute
 * closed orbit maps we should make a different probe.
 * </p>
 * 
 * 
 * @author Christopher K. Allen
 * @author t6p
 */
public class TransferMapState extends ProbeState<TransferMapState> {
    
    /*
     * Global Constants
     */
    
//    /** number of modes */
//    static final private int NUM_MODES = 3;

    /** element tag for RF phase */
    protected static final String LABEL_STATE = "transfer";

    /** attribute tag for betatron phase */
    protected static final String ATTR_MAP = "map";

    
    /*
     * Local Attributes
     */
    
    /** composite transfer map */
    private PhaseMap         mapPhiCmp;
    
    /** transfer map through this state */
    private PhaseMap        mapPhiPart;


//    /** phase coordinates of the particle location */
//    @Deprecated
//    private PhaseVector[] _phaseCoordinates;



    /*
     * Initialization
     */
    
    /** 
     * Constructor, create new <code>TransferMapState</code> and 
     * initialize to zero state values. 
     */
    public TransferMapState() {
        super();
    	
        this.setTransferMap( PhaseMap.identity() );
        this.setPartialTransferMap( PhaseMap.identity() );
        
//        this.setPhaseCoordinates( PhaseVector.newZero() );
    }
    
    /**
     * Copy constructor for TransferMapState.  Initializes the new
     * <code>TransferMapState</code> objects with the state attributes
     * of the given <code>TransferMapState</code>.
     *
     * @param transferMapState     initializing state
     *
     * @author Christopher K. Allen, Jonathan M. Freed
     * @since  Jun 26, 2014
     */
    public TransferMapState(final TransferMapState transferMapState){
    	super(transferMapState);
    	
//    	this._phaseCoordinates	= transferMapState._phaseCoordinates.clone();
    	this.mapPhiCmp	= transferMapState.mapPhiCmp.copy();
    	this.mapPhiPart	= transferMapState.mapPhiPart.copy();
    }


    /**
     * Initializing constructor - create a new <code>TransferMapState</code> object and initialize it to the current state of the given probe.
     * @param probe probe object with initial state information
     */
    @SuppressWarnings("deprecation")
	public TransferMapState( final TransferMapProbe probe ) {
        super( probe );

//        this.setPhaseCoordinates( probe.getPhaseCoordinates().clone() );
        this.setTransferMap( probe.getTransferMap().copy() );
        this.setPartialTransferMap( probe.getPartialTransferMap().copy() );
    }


    /*
     * Property Accessors
     */
    
    
    /**
     * Set the current composite transfer map up to the current probe location.
     * @param   mapTrans    transfer map in homogeneous phase coordinates
     * @see Probe#createTrajectory()
     * 
     */
    public void setTransferMap( final PhaseMap mapTrans ) {
        this.mapPhiCmp = ( mapTrans != null ) ? mapTrans : PhaseMap.identity();
    }
    
    /**
     * Sets the current partial transfer map, or "through map".  This map transfers
     * particle phase coordinates through a distance occupied by this state.
     * 
     * @param mapPart   transfer map through this state
     *
     * @author Christopher K. Allen
     * @since  Nov 22, 2013
     */
    public void setPartialTransferMap( final PhaseMap mapPart ) { 
        this.mapPhiPart = ( mapPart != null ) ? mapPart : PhaseMap.identity();
    }

    /**
     * Set the transfer map across this state, from entrance to exit.
     * 
     * @param   mapTrans    transfer map in homogeneous phase coordinates
     * 
     * @see Probe#createTrajectory()
     */
    public void setStateTransferMap( final PhaseMap mapTrans ) {
        this.mapPhiPart = ( mapTrans != null ) ? mapTrans : PhaseMap.identity();
    }
    
    /**
     * Get the composite transfer map at this state location.  The
     * composite map <b>&phi;</b> maps the phase coordinates of 
     * particles at the beginning of the <code>TransferMapTrajectory</code>
     * parent (i.e., at the first state of the trajectory object)
     * to this state.
     * 
     * @return transfer map in homogeneous phase space coordinates
     */
    public PhaseMap getTransferMap()  {
        return this.mapPhiCmp;
    }
    
    /**
     * <p>
     * Returns the partial transfer map that transports particle
     * phase coordinates through the space occupied by this state.
     * </p>
     * <p>
     * Get the partial transfer map that maps particle phase coordinates from
     * the previous state exit to this state's exit.  The product of all these
     * partial transfer maps from the first state to this state would yield
     * the returned value of <code>{@link #getTransferMap()}</code>.
     * </p>
     *  
     * @return      partial transfer map through this state
     *
     * @author Christopher K. Allen
     * @since  Nov 22, 2013
     */
    public PhaseMap getPartialTransferMap() {
        return this.mapPhiPart;
    }
    
//    /**
//     * Get the partial transfer map that maps particle phase coordinates from
//     * the previous state exit to this state's exit.  The product of all these
//     * partial transfer maps from the first state to this state would yield
//     * the returned value of <code>{@link #getTransferMap()}</code>. 
//     * 
//     * @return partial transfer map in homogeneous phase space coordinates
//     */
//    public PhaseMap getStateTransferMap()  {
//        return this.mapPhiPart;
//    }
    


//    /** 
//     *  Returns homogeneous phase space coordinates of the closed orbit.  The units are meters and radians.
//     *  @return vector (x,x',y,y',z,z',1) of phase space coordinates
//     * @deprecated Moved to xal.tools.beam.calc 
//     */
//	@Deprecated
//    public PhaseVector getPhaseCoordinates() {
//        return _phaseCoordinates[0];
//    }
//
//
//
//
//    /** 
//     *  Set the phase coordinates of the probe.  
//     *  @param  vecPhase new homogeneous phase space coordinate vector
//     *  
//     *  @deprecated TransferMapProbes do not have phase vectors
//     */
//    @Deprecated
//    public void setPhaseCoordinates( final PhaseVector vecPhase ) {
//        _phaseCoordinates = new PhaseVector[] { vecPhase != null ? new PhaseVector( vecPhase ) : new PhaseVector() };
//    }
//


    /*
     * ProbeState Overrides
     */
    
    /**
     * Implements the clone operation required by the base class
     * <code>ProbeState</code>.
     *
     * @see xal.model.probe.traj.ProbeState#copy()
     *
     * @author Christopher K. Allen
     * @since  Jun 27, 2014
     */
    @Override
    public TransferMapState copy() {
        return new TransferMapState(this);
    }
    /**
     * Save the probe state values to a data store represented by the <code>DataAdaptor</code> interface.
     * @param daptSink    data sink to receive state information
     * @see xal.model.probe.traj.ProbeState#addPropertiesTo(gov.DataAdaptor.tools.data.IDataAdaptor)
     */
    @Override
    protected void addPropertiesTo(DataAdaptor daptSink) {
        super.addPropertiesTo(daptSink);

        DataAdaptor daptMap = daptSink.createChild(TransferMapState.LABEL_STATE);
        this.getTransferMap().save(daptMap);
    }

    /**
     * Restore the state values for this probe state object from the data store
     * represented by the <code>DataAdaptor</code> interface.
     * @param   daptSrc             data source for probe state information
     * @throws  DataFormatException    error in data format
     * @see xal.model.probe.traj.ProbeState#readPropertiesFrom(gov.DataAdaptor.tools.data.IDataAdaptor)
     */
    @Override
    protected void readPropertiesFrom(DataAdaptor daptSrc) throws DataFormatException {
        super.readPropertiesFrom(daptSrc);

        DataAdaptor daptMap = daptSrc.childAdaptor(TransferMapState.LABEL_STATE);
        if (daptMap == null)
            throw new DataFormatException("TransferMapState#readPropertiesFrom(): no child element = " + LABEL_STATE);
        this.getTransferMap().load(daptMap);
    }

    /**
     * Get a string representation of this state.
     * @return a string representation of this state
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return super.toString();
    }
}
