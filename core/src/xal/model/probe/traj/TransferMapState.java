/*
 * Created on Jun 9, 2004
 *
 * Copyright 2004 by SNS/LANL
 */

package xal.model.probe.traj;

import xal.tools.math.r3.R3;
import xal.tools.beam.Twiss;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseVector;
import xal.tools.data.DataAdaptor;

import xal.model.probe.Probe;
import xal.model.probe.TransferMapProbe;
import xal.model.xml.ParsingException;


/**
 * <p>
 * Probe state for the transfer map tracker.
 * </p>
 * <p>
 * <h4>NOTES: CKA</h4>
 * &middot; I noticed a very brittle situation with this implementation.  
 * <code>ICoordinateState</code> requires the methods 
 * <code>@link ICoordinateState#getPhaseCoordinates()}</code> etc.
 * Interface <code>ICoordinateState</code> extends <code>IProbeState</code>.
 * Well, <code>TransferMapState</code> inherits from <code>ProbeState</code>
 * which implements <code>IProbeState</code>.  It's getting dicey.
 * <br/>
 * &middot; The <code>TransferMapState</code> is really meant to compute
 * transfer maps only, and be very light weight.  If you want to compute
 * closed orbit maps we should make a different probe.
 * </p>
 * 
 * 
 * @author Christopher K. Allen
 * @author t6p
 */
public class TransferMapState extends ProbeState /* implements IPhaseState */ {
//    /** number of modes */
//    static final private int NUM_MODES = 3;

    /** element tag for RF phase */
    protected static final String LABEL_STATE = "transfer";

    /** attribute tag for betatron phase */
    protected static final String ATTR_MAP = "map";

    /** composite transfer map */
    private PhaseMap         mapPhiCmp;
    
    /** transfer map through this state */
    private PhaseMap        mapPhiPart;

    /** the trajectory */
    private TransferMapTrajectory _trajectory;

//    /** full turn map at this element */
//    @Deprecated
//    private PhaseMap _fullTurnMap;

//    /** closed orbit */
//    @Deprecated
//    private PhaseVector _closedOrbit;

    /** phase coordinates of the particle location */
    @Deprecated
    private PhaseVector[] _phaseCoordinates;

//    /** twiss parameters */
//    @Deprecated
//    private Twiss[] _twiss;

//    /** betatron phase */
//    @Deprecated
//    private R3 _betatronPhase;

//    /** dispersion */
//    @Deprecated
//    private double[] _dispersion;


    /** Constructor - create new <code>TransferMapState</code> and initialize to zero state values. */
    public TransferMapState() {
        
        setTransferMap( null );
        
        // CKA - removing the backpointer to the trajectory since it is never referenced
//        this( null, null, null );
    }


//    /**
//     * Primary constructor - create new <code>TransferMapState</code> and initialize to zero state values.
//     * @param trajectory the trajectory to which this state belongs
//     * @param transferMap the transfer map which is associated with this state
//     * @param coordinates the phase coordinates
//     */
//    public TransferMapState( final TransferMapTrajectory trajectory, final PhaseMap transferMap, final PhaseVector coordinates ) {
////        setTrajectory( trajectory );
//        setTransferMap( transferMap );
//        setPhaseCoordinates( coordinates );
//    }


    /**
     * Initializing constructor - create a new <code>TransferMapState</code> object and initialize it to the current state of the given probe.
     * @param probe probe object with initial state information
     */
    public TransferMapState( final TransferMapProbe probe ) {
        super( probe );

        // CKA - removing the backpointer to the trajectory since it is never referenced
//        setTrajectory( (TransferMapTrajectory)probe.getTrajectory() );
        setTransferMap( probe.getTransferMap() );
        setPhaseCoordinates( probe.getPhaseCoordinates() );
    }


    /**
     * Set the trajectory to the one specified.
     * @param trajectory the trajectory to use.
     */
    public void setTrajectory( final TransferMapTrajectory trajectory ) {
        _trajectory = trajectory;
    }


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
     * Get the partial transfer map that maps particle phase coordinates from
     * the previous state exit to this state's exit.  The product of all these
     * partial transfer maps from the first state to this state would yield
     * the returned value of <code>{@link #getTransferMap()}</code>. 
     * 
     * @return partial transfer map in homogeneous phase space coordinates
     */
    public PhaseMap getStateTransferMap()  {
        return this.mapPhiPart;
    }
    

//    /** 
//     * Returns the array of twiss objects for this state for all three planes.
//     * 
//     * These are the closed orbit Twiss parameters at this state location.
//     * The phase tunes are the closed orbit tunes.
//     * 
//     * @return array (twiss-H, twiss-V, twiss-L)
//     * 
//     * @deprecated  Transfer maps do not have Twiss parameters
//     */
//    public Twiss[] getTwiss() {
//        calculateTwissIfNeeded();
//
//        return _twiss;
//    }


//    /**
//     * Get the dispersion.
//     * @return  x axis chromatic dispersion in <b>meters</b>
//     * @deprecated Moved to xal.tools.beam.calc 
//     */
//    public double getChromDispersionX()  {
//        calculateDispersionIfNeeded();
//
//        return _dispersion[0];
//    } 
//
//
//    /**
//     * Get the dispersion slope.
//     * @return  x axis chromatic dispersion slope.
//     * @deprecated Moved to xal.tools.beam.calc 
//     */
//    public double getChromDispersionSlopeX()  {
//        calculateDispersionIfNeeded();
//
//        return _dispersion[1];
//    } 
//
//
//    /**
//     * Get the dispersion.
//     * @return  y axis chromatic dispersion in <b>meters</b>
//     * @deprecated Moved to xal.tools.beam.calc 
//     */
//    public double getChromDispersionY()  {
//        calculateDispersionIfNeeded();
//
//        return _dispersion[2];
//    } 
//
//
//    /**
//     * Get the dispersion slope.
//     * @return  y axis chromatic dispersion slope.
//     * @deprecated Moved to xal.tools.beam.calc 
//     */
//    public double getChromDispersionSlopeY()  {
//        calculateDispersionIfNeeded();
//
//        return _dispersion[3];
//    } 


    /** 
     *  Returns homogeneous phase space coordinates of the closed orbit.  The units are meters and radians.
     *  @return vector (x,x',y,y',z,z',1) of phase space coordinates
     * @deprecated Moved to xal.tools.beam.calc 
     */
    public PhaseVector getPhaseCoordinates() {
        return _phaseCoordinates[0];
    }


//    /**
//     * Get the turn by turn array of phase coordinates for the specified number of turns.
//     * @param turns the number of turns for which to get the turn by turn array of phase coordinates
//     * @return the turn by turn array of phase coordinates
//     */
//    public PhaseVector[] phaseCoordinatesTurnByTurn( final int turns ) {
//        if ( turns < 0 ) {
//            throw new IllegalArgumentException( "The number of turns must be non-negative, but you provided: " + turns );
//        }
//
//        if ( turns >= _phaseCoordinates.length ) {
//            PhaseVector[] vector = new PhaseVector[turns + 1];
//            System.arraycopy( _phaseCoordinates, 0, vector, 0, _phaseCoordinates.length );
//
//            final PhaseMatrix fullTurnMatrix = getFullTurnMap().getFirstOrder();
//            for ( int turn = _phaseCoordinates.length ; turn <= turns ; turn++ ) {
//                vector[turn] = fullTurnMatrix.times( vector[turn - 1] );
//            }
//
//            _phaseCoordinates = vector;
//        }
//
//        final PhaseVector[] result = new PhaseVector[turns + 1];
//        System.arraycopy( _phaseCoordinates, 0, result, 0, turns+1 );
//        return result;
//    }



//    /**
//     * Get one turn of phase coordinates at the specified number of turns.
//     * @param turns the number of turns for which to get the turn by turn array of phase coordinates
//     * @return the turn by turn array of phase coordinates
//     */
//    public PhaseVector phaseCoordinatesTurn( final int turns ) {
//        if ( turns < 0 ) {
//            throw new IllegalArgumentException( "The number of turns must be non-negative, but you provided: " + turns );
//        }
//
//        if ( turns >= _phaseCoordinates.length ) {
//            PhaseVector[] vector = new PhaseVector[turns + 1];
//            System.arraycopy( _phaseCoordinates, 0, vector, 0, _phaseCoordinates.length );
//
//            final PhaseMatrix fullTurnMatrix = getFullTurnMap().getFirstOrder();
//            //sako
//
//
//            for ( int turn = _phaseCoordinates.length ; turn <= turns ; turn++ ) {
//                vector[turn] = fullTurnMatrix.times( vector[turn - 1] );
//            }
//
//            _phaseCoordinates = vector;
//        }
//
//        final PhaseVector[] result = new PhaseVector[turns + 1];
//        System.arraycopy( _phaseCoordinates, 0, result, 0, turns+1 );
//        return result[turns];
//    }


    /** 
     *  Set the phase coordinates of the probe.  
     *  @param  vecPhase new homogeneous phase space coordinate vector
     *  
     *  @deprecated TransferMapProbes do not have phase vectors
     */
    @Deprecated
    public void setPhaseCoordinates( final PhaseVector vecPhase ) {
        _phaseCoordinates = new PhaseVector[] { vecPhase != null ? new PhaseVector( vecPhase ) : new PhaseVector() };
    }


//    /**
//     * Get the closed orbit about which betatron oscillations occur.
//     * @return the fixed orbit vector (x,x',y,y',z,z',1)
//     * 
//     * @deprecated  TransferMapProbes do not have fixed orbits
//     */
//    @Deprecated
//    public PhaseVector getFixedOrbit() {
//        calculateClosedOrbitIfNeeded();
//
//        return _closedOrbit;
//    }


//    /** 
//     * Calculate the closed orbit if needed. 
//     * 
//     * @deprecated  This is being moved to xal.tools.beam.calc
//     * */
//    @Deprecated
//    public void calculateClosedOrbitIfNeeded() {
//        if ( _closedOrbit == null ) {
//            _closedOrbit = getFullTurnMap().calculateFixedPoint();
//        }
//    }


//    /** 
//     * Calculate the twiss parameters if needed. 
//     * 
//     * These are the closed orbit Twiss parameters at this state location.
//     * The phase tunes are the closed orbit tunes.
//     * 
//     * @deprecated  This is being moved to xal.tools.beam.calc
//     */
//    @Deprecated
//    private void calculateTwissIfNeeded() {
//        if ( _twiss == null ) {
//            final double PI2 = 2 * Math.PI;
//            final double[] tunes = _trajectory.getTunes();
//            final PhaseMatrix matrix = getFullTurnMap().getFirstOrder();
//
//            final Twiss[] twiss = new Twiss[NUM_MODES];
//            for ( int mode = 0 ; mode < NUM_MODES ; mode++ ) {
//                final int index = 2 * mode;
//                final double sinMu = Math.sin( PI2 * tunes[mode] );// _tunes could be NaN
//                final double m11 = matrix.getElem( index, index );
//                final double m12 = matrix.getElem( index, index + 1 );
//                final double m22 = matrix.getElem( index + 1, index + 1 );
//                final double beta = m12 / sinMu;
//                final double alpha = ( m11 - m22 ) / ( 2 * sinMu );
//                final double emittance = Double.NaN;
//                twiss[mode] = new Twiss( alpha, beta, emittance );
//            }
//
//            _twiss = twiss;
//        }
//    }


//    /** 
//     * Calculate the betatron phase advance if necessary. This is a 
//     * general quantity valid around the ring.
//     * 
//     * 
//     * @deprecated  This is being moved to xal.tools.beam.calc
//     * */
//    @Deprecated
//    private void calculateBetatronPhaseIfNeeded() {
//        if ( _betatronPhase != null )  return;
//
//        final Twiss[] twiss = getTwiss();
//        final Twiss[] initialTwiss = ((IPhaseState)_trajectory.initialState()).getTwiss();
//
//        final double[] phases = new double[NUM_MODES];
//        for ( int mode = 0 ; mode < NUM_MODES ; mode++ ) {
//            final double beta = twiss[mode].getBeta();
//            final double initialBeta = initialTwiss[mode].getBeta();
//            final double initialAlpha = initialTwiss[mode].getAlpha();
//            final double m11 = getTransferMap().getFirstOrder().getElem( 2*mode, 2*mode );
//            final double m12 = getTransferMap().getFirstOrder().getElem( 2*mode, 2*mode + 1 );
//
//            double sinPhase = m12 / Math.sqrt( beta * initialBeta );
//            sinPhase = Math.max( Math.min( sinPhase, 1.0 ), -1.0 );		// make sure it is in the range [-1, 1]
//            //sako (I think this is wrong)			final double cosPhase = m11 * Math.sqrt( beta / initialBeta ) - initialAlpha * sinPhase;
//            //sako		
//            final double cosPhase = m11 * Math.sqrt( initialBeta / beta) - initialAlpha * sinPhase;
//            //org
//            //		final double cosPhase = m11 * Math.sqrt( beta / initialBeta ) - initialAlpha * sinPhase;
//
//            final double phase = Math.asin( sinPhase );
//
//            if ( cosPhase >= 0 ) {
//                if ( sinPhase >= 0 ) {
//                    phases[mode] = phase;					
//                }
//                else {
//                    phases[mode] = 2 * Math.PI + phase;					
//                }
//            }
//            else {
//                phases[mode] = Math.PI - phase;
//            }			
//        }
//
//        _betatronPhase = new R3( phases );
//    }


//    /** Calculate the chromatic dispersion if necessary */
//    private void calculateDispersionIfNeeded() {
//        if ( _dispersion == null ) {
//            _dispersion = getFullTurnMap().calculateDispersion(getGamma());
//        }
//    }


//    /**
//     * Get the full turn map at this element.
//     * 
//     * @return the full turn map at this element
//     * 
//     * @deprecated  <code>TransferMapState</code>s do not know about rings, they
//     *              are just states
//     */
//    public PhaseMap getFullTurnMap() {
//        calculateFullTurnMapIfNeeded();
//
//        return _fullTurnMap;
//    }


//    /**
//     * Get the betatron phase for all three phase planes from the probe's origin.  
//     * Currently this is just the fractional betatron phase.
//     * @return  vector (psix,psiy,psiz) of phases in radians
//     */
//    public R3 getBetatronPhase() {
//        calculateBetatronPhaseIfNeeded();
//
//        return _betatronPhase;
//    }


//    /**
//     * Get the betatron phase for all three phase planes from the probe's origin and for the specified number of turns.
//     * Currently this is just the fractional betatron phase.
//     * @param turns the number of turns for which to calculate the phase advance
//     * @return  vector (psix,psiy,psiz) of phases in radians
//     */
//    @Deprecated
//    public R3 getBetatronPhase( final int turns ) {
//        final int num_modes = 3;
//        final double[] phases = getBetatronPhase().toArray();
//        final double[] tunes = _trajectory.getTunes();
//        final double PI2 = 2 * Math.PI;
//
//        for ( int mode = 0 ; mode < num_modes ; mode++ ) {
//            phases[mode] += PI2 * turns * tunes[mode];
//        }
//
//        return new R3( phases );
//    }


//    /** 
//     * Calculate the full turn map. 
//     * 
//     * @deprecated  This is being moved to xal.tools.beam.calc
//     * */
//    @Deprecated
//    private void calculateFullTurnMapIfNeeded() {
//        if ( _fullTurnMap == null ) {
//            _fullTurnMap = mapPhiCmp.compose( _trajectory.getFullTurnMapAtOrigin() ).compose( mapPhiCmp.inverse() );
//        }
//    }



    /**
     * Save the probe state values to a data store represented by the <code>DataAdaptor</code> interface.
     * @param daptSink    data sink to receive state information
     * @see gov.sns.xal.model.probe.traj.ProbeState#addPropertiesTo(gov.DataAdaptor.tools.data.IDataAdaptor)
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
     * @throws  ParsingException    error in data format
     * @see gov.sns.xal.model.probe.traj.ProbeState#readPropertiesFrom(gov.DataAdaptor.tools.data.IDataAdaptor)
     */
    @Override
    protected void readPropertiesFrom(DataAdaptor daptSrc) throws ParsingException {
        super.readPropertiesFrom(daptSrc);

        DataAdaptor daptMap = daptSrc.childAdaptor(TransferMapState.LABEL_STATE);
        if (daptMap == null)
            throw new ParsingException("TransferMapState#readPropertiesFrom(): no child element = " + LABEL_STATE);
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
