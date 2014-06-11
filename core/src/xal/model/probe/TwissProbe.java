/*
 * TwissProbe.java
 *
 * Created on December, 2006
 * 
 * Christopher K. Allen
 */

package xal.model.probe;

import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.beam.Twiss3D;
import xal.tools.beam.Twiss3D.IND_3D;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.math.r3.R3;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.model.probe.traj.TransferMapState;
import xal.model.probe.traj.TwissProbeState;
import xal.model.probe.traj.TwissTrajectory;
import xal.model.xml.ParsingException;

/**
 * <p>
 * <code>EnvelopeProbe</code> represents the RMS beam envelopes of a beam.
 * Specifically, the state of this probe is the 7x7 matrix of homogeneous phase
 * space moments up to, and including second order. This is the correlation
 * matrix for the beam and is represented as
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <b>&chi;</b> = &lt; <b>zz</b><sup>T</sup>&gt;
 * <br/>
 * <br/>
 * where <b>z </b>=(x,x',y,y',z,z',1) is the vector of homogeneous phase space
 * coordinates, and &lt;.&gt; is the moment operator with respect to the beam
 * distribution.
 * </p>
 * 
 * @author Christopher K. Allen
 * @author Craig McChesney
 */

public class TwissProbe extends BunchProbe {
    
    
    
    /*
	 * Local Attributes
	 */

    /** centroid position in phase space */
    private PhaseVector         vecCent;
    
    /** accumulated response matrix */
    private PhaseMatrix         matResp;

    /** particle betatron phase (with space charge if present) */
    private R3                 vecPhsBeta;
  
    /** current twiss parameters */
    private Twiss3D             envTwiss;
    
    /** probe trajectory */
    private Trajectory<TwissProbeState> trajectory;
    
    
    

    

	/*
	 * Initialization
	 */

	/**
	 * Default Constructor. Creates a new, empty instance of TwissProbe
	 */
	public TwissProbe() {
        super();
        this.vecCent = PhaseVector.newZero();
        this.matResp = PhaseMatrix.identity();
        this.vecPhsBeta = R3.zero();
        this.envTwiss   = new Twiss3D();
	};

	/**
	 * Copy constructor - clones the argument
	 * 
	 * @param prbParent
	 *            <code>TwissProbe</code> object to be cloned
	 */
	public TwissProbe(TwissProbe prbParent) {
		super(prbParent);

        this.setCentroid(new PhaseVector( prbParent.getCentroid() ));
        this.setResponseMatrix(new PhaseMatrix( prbParent.getResponseMatrix() ));
        this.setBetatronPhase(new R3(prbParent.getBetatronPhase()));
        this.setTwiss(new Twiss3D( prbParent.getTwiss() ));
	};
    
    /**
     * Initializing constructor - initialize from data adaptor
     * 
     * Create an new <code>TwissProbe</code> object and initialize
     * its state variables according to the data in the data source
     * exposing the <code>DataAdaptor</code> interface.
     * 
     * @param   daSource    data source containing state variable values
     * 
     * @throws  DataFormatException     unable to parse, bad data format
     */
    public TwissProbe(DataAdaptor daSource)  
        throws DataFormatException
    {
        this();
        this.load(daSource);
    }

    
    /*
     * Probe Overrides
     */
    
    /**
     * Initialize this probe from the one specified.
     * 
     * @param probe to copy
     */
    @Override
    protected void initializeFrom( final Probe probe ) {
        super.initializeFrom( probe );
        
        applyState( probe.createProbeState() );
        createTrajectory();
    }

    /**
     * Make a deep copy of this probe and return it.
     * 
     * @return  a clone of this probe object
     *
     * @see xal.model.probe.Probe#copy()
     *
     * @author Christopher K. Allen
     * @since  Nov 5, 2013
     */
    @Override
    public TwissProbe copy() {
        return new TwissProbe( this );
    }
    

    
    /**
     * Set the centroid location of the beam bunch in homogeneous
     * coordinates.
     * 
     * @param   vecCentroid     new centroid of the bunch (x,x',y,y',z,z',1)
     */
    public void setCentroid(PhaseVector vecCentroid)   {
        this.vecCent = vecCentroid;
    }
    
    /**
     * Set the first-order response matrix accumulated by the Envelope since its initial
     * state.  Note that this response includes the effects of space charge.
     * 
     * @param matResp   first-order response matrix in homogeneous coordinates
     */
    public void setResponseMatrix(PhaseMatrix matResp)  {
        this.matResp = matResp;
    }

    /**
     * Set the betatron phase for each phase plane.
     * 
     * @param vecPhase  vector (&psi;<sub><i>x</i></sub>,&psi;<sub><i>y</i></sub>,&psi;<sub><i>z</i></sub>) 
     *                  of betatron phases in <b>radians </b>
     */
    public void setBetatronPhase(R3 vecPhase) {
        this.vecPhsBeta = vecPhase;
    }

    /**
     * Set the Twiss parameters for the given phase plane.
     * 
     * @param   iPlane  phase plane index
     * @param   twiss   twiss parameters
     */
    public void setTwiss(IND_3D iPlane, Twiss twiss)   {
        this.envTwiss.setTwiss(iPlane, twiss);
    }
    
    /** 
     * Set all the twiss parameters for the probe 
     * 
     * @param envTwiss new 3 dimensional array of Twiss objects (hor, vert,long)
     * 
     * @see xal.tools.beam.Twiss
     */
    public void setTwiss(Twiss3D envTwiss) {
        this.envTwiss = envTwiss;
    }
    
    
    
	/*
	 * Data Query
	 */

    /**
     * Get the centroid location of the beam bunch in homogeneous
     * coordinates.
     * 
     * @return  centroid of the bunch (x,x',y,y',z,z',1)
     */
    public PhaseVector  getCentroid()   {
        return this.vecCent;
    }
    
    /**
     * Get the first-order response matrix accumulated by the probe since its initial
     * state.  
     * 
     * @return  first-order response matrix in homogeneous coordinates
     */
    public PhaseMatrix getResponseMatrix()  {
        return this.matResp;
    }
    
    /**
     * Returns the betatron phase with space charge for all three phase planes.
     * 
     * @return vector (&psi;<sub><i>x</i></sub>,&psi;<sub><i>y</i></sub>,&psi;<sub><i>z</i></sub>) 
     *                  of betatron phases in <b>radians </b>
     */
    public R3 getBetatronPhase() {
        return this.vecPhsBeta;
    }
    
    /**
     * Returns the Twiss parameters for the given phase plane.
     * 
     * @param   iPlane  phase plane index
     * 
     * @return  twiss parameters for given phase plane
     */
    public Twiss    getTwiss(IND_3D iPlane)    {
        return this.envTwiss.getTwiss(iPlane);
    }
    
    /** 
     * Returns the array of Twiss parameters for this 
     * state for all three planes.
     * 
     * @return array(twiss-H, twiss-V, twiss-L)
     */
    public Twiss3D getTwiss() { 
        return this.envTwiss;
    }
    
    
    

    /*
     * Computed Properties
     */

    
    /**
     *  Convenience Method:  Returns the rms emittances for this state as
     *  from the individual Twiss parameters.  
     * 
     * @return array (ex,ey,ez) of rms emittances
     */
    public double[] rmsEmittances() {
        double  arrEmit[] = new double[3];
        
        for (IND_3D i : IND_3D.values()) 
            arrEmit[i.val()] = this.getTwiss(i).getEmittance();
        
        return arrEmit;
    }
    
    
    
	/*
	 * Trajectory Support
	 */

	/**
	 * Creates a snapshot of the current state and returns it as a
	 * <code>ProbeState</code> object of the proper type.
	 * 
	 * @return a new <code>TwissProbeState</code> encapsulating the probe's
	 *         current state
	 */
    @Override
	public TwissProbeState createProbeState() {
		return new TwissProbeState(this);
	}

	/**
	 * Creates a <code>Trajectory&lt;TwissProbeState&gt;</code> object of the
	 * proper type for saving the probe's history.
	 * 
	 * @return a new, empty <code>Trajectory&lt;TwissProbeState&gt;</code> 
	 * 		for saving the probe's history
	 * 
	 * @author Jonathan Freed
	 */
    @Override
	public Trajectory<TwissProbeState> createTrajectory() {
		this.trajectory = new Trajectory<TwissProbeState>();
		return this.trajectory;
	}
    
    /**
     * Retrieves the trajectory of the proper type for the probe.
     * 
     * @return a <code>Trajectory&lt;TwissProbeState&gt;</code> that is the 
     * 		trajectory of the probe
     * 
     * @author Jonathan Freed
     */
    @Override
    public Trajectory<TwissProbeState> getTrajectory() {
    	return this.trajectory;
    }

	/**
	 * Apply the contents of ProbeState to update my current state. The argument
	 * supplying the new state should be of concrete type
	 * <code>TwissProbeState</code>.
	 * 
	 * @param state
	 *            <code>ProbeState</code> object containing new probe state
	 *            data
	 * 
	 * @exception IllegalArgumentException
	 *                wrong <code>ProbeState</code> subtype for this probe
	 */
    @Override
	public void applyState(ProbeState state) {
		if (!(state instanceof TwissProbeState))
			throw new IllegalArgumentException("invalid probe state");
		TwissProbeState stateTwiss = (TwissProbeState) state;

		super.applyState(stateTwiss);
        this.setCentroid(stateTwiss.getCentroid());
		this.setResponseMatrix(stateTwiss.getResponseMatrix());
        this.setTwiss(stateTwiss.getTwiss3D());
	}

    
    /**
     * Resets the probe to the saved initial state, if there is one and clears
     * the Trajectory.
     * 
     * @see xal.model.probe.Probe#reset()
     *
     * @author Christopher K. Allen
     * @since  Nov 5, 2013
     */
    @Override
    public void reset() {
        super.reset();
//        if (getAlgorithm() instanceof EnvTrackerAdapt)
//            try {
//                getAlgorithm().initialize();
//                
//            } catch (ModelException e) {
//                System.err.println("TwissProbe#reset() - Unable to initialize algorithm");
//                e.printStackTrace();
//                
//            }
    }
    

    
    /*
     * Support Methods
     */
    
    /**
     * Creates a new <code>TwissProbeState</code> object and initializes
     * it from the data source exposing the given <code>DataAdaptor</code>
     * interface.
     * 
     * @return  the newly instantiated and initialized <code>TwissProbeState</code> object
     *
     * @see xal.model.probe.Probe#readStateFrom(xal.tools.data.DataAdaptor)
     *
     * @author Christopher K. Allen
     * @since  Nov 5, 2013
     */
    @Override
    protected ProbeState readStateFrom(DataAdaptor container) throws ParsingException {
        TwissProbeState state = new TwissProbeState();
        state.load(container);
        return state;
    }
}

/*
 * Storage
 */


///**
//* Return the <code>BunchDescriptor</code> object encapsulating all the analytic
//* parameters describing the bunch.
//* 
//* @return  analytic parameters describing beam bunch
//*/
//private BunchDescriptor  getBunchParameters()    {
//  return this.desBunch;
//}
//
///**
//* Set all the analytic bunch description parameters at once.
//* 
//* @param   desBunch    encapsulation of all the bunch parameters
//*/
//private void setBunchParameters(BunchDescriptor desBunch) {
//  this.desBunch = desBunch;
//}
//

///**
//* Set the twiss parameters for each phase plane.
//* 
//* CKA NOTES:
//* - The current method signature is misleading.  If there is
//* an beam axis offset before this method is called, then that
//* offset is preserved, but the previous correlation matrix is
//* wiped out.  Thus, even though the method signature suggests
//* there will be no offset, there can be.
//* 
//* @param twiss
//*            array of Twiss objects for H, V , long. directions
//*/
//public void initFromTwiss(Twiss[] twiss) {
// this.arrTwiss = twiss;
// PhaseVector pv = getCorrelation().getMean();
// CovarianceMatrix cMat = CovarianceMatrix.buildCorrelation(twiss[0],
//         twiss[1], twiss[2], pv);
// this.setCorrelation(cMat);
//}

///** 
//* We want to deprecate this method from the base class <code>BunchProbe</code>
//* since we do not use beam current right now.
//* 
//* @param   dblCurrent  new beam current (not used)
//* 
//* @deprecated
//*/
//@Override
//public void setBeamCurrent(double dblCurrent)   {
// super.setBeamCurrent(dblCurrent);
//}
//
///**
//* We want to deprecate references to the beam current from the base class
//* <code>BunchProbe</code> since we are not considering it right now.
//* 
//* @return  beam current (not used in dynamics)
//* 
//* @deprecated  we do not use current in the dynamics
//*/
//@Override
//public double   getBeamCurrent()    {
//return 0.0; // super.getBeamCurrent();
//}
//

///**
//* Get the distribution profile descriptor.
//* 
//* @return  profile descriptor object for this distribution
//*/
//public ProfileIndex    getProfile()    {
//return this.getBunchParameters().getProfile();
//}
//


