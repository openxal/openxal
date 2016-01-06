/**
 * TwissProbeState.java
 * 
 * Created : December, 2006
 * Author  : Christopher K. Allen
 * 
 * 
 */
package xal.model.probe.traj;

import xal.model.probe.TwissProbe;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.beam.Twiss3D;
import xal.tools.beam.Twiss3D.IND_3D;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.math.r3.R3;



/**
 * Saves the state of a <code>TwissProbe</code> at a particular instance.
 * 
 * @author Christopher K. Allen
 * @version $id:
 * 
 */
public class TwissProbeState extends BunchProbeState<TwissProbeState> {



    /*
     * Global Constants
     */


    //
    //  Data Persistence
    //
    
    /** element label for twiss probe data */
    private static final String   LABEL_TWISSPROBE = "twissprobe";
    
    /** element label for centroid vector */
    private static final String   LABEL_CENT = "centroid";

    /** element label for response matrix */
    private static final String   LABEL_RESP = "resp";
    
    /** element label for betatron phase */
    private static final String   LABEL_PHASE = "phase";
    
//    /** element label for twiss parameters */
//    private static final String   LABEL_TWISS = "twiss";
//    
//    /** general value attribute tag */
//    private static final String   ATTR_VALUE = "value";
    
    
    //
    // Persistence Version
    //
    
    /** the data format version attribute */
    private static final String   ATTR_VERSION = "ver";
    
    /** the data format version */
    private static final int     INT_VERSION = 2;
    


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
    
    
    
    
    
    
    /*
     * Initialization
     */    


    /**
     * Default constructor.  Create a new, empty <code>EnvelopeProbeState</code> object.
     */    
    public TwissProbeState() {
        super();
        
        this.vecCent    = PhaseVector.newZero();
        this.matResp    = PhaseMatrix.identity();
        this.vecPhsBeta = R3.zero();
        this.envTwiss   = new Twiss3D();
    }
    
    /**
     * Copy constructor for TwissProbeState.  Initializes the new
     * <code>TwissProbeState</code> objects with the state attributes
     * of the given <code>TwissProbeState</code>.
     *
     * @param twissProbeState     initializing state
     *
     * @author Christopher K. Allen, Jonathan M. Freed
     * @since  Jun 26, 2014
     */
    public TwissProbeState(final TwissProbeState twissProbeState){
    	super(twissProbeState);
    	
    	this.envTwiss	= twissProbeState.envTwiss.copy();
    	this.matResp	= twissProbeState.matResp.clone();
    	this.vecCent	= twissProbeState.vecCent.clone();
    	this.vecPhsBeta	= twissProbeState.vecPhsBeta.clone();
    }
	
    /**
     * Initializing Constructor.  Create a new <code>TwissProbeState</code> object and
     * initialize it to the state of the probe argument.
     * 
     * @param probe     <code>TwissProbe</code> containing initializing state information
     */
    public TwissProbeState(final TwissProbe probe) {
        super(probe);
        
        this.setCentroid( new PhaseVector( probe.getCentroid().clone() ) );
        this.setResponseMatrix( new PhaseMatrix( probe.getResponseMatrix().clone() ) );
        this.setBetatronPhase( new R3( probe.getBetatronPhase().clone() ) );
        this.setTwiss( new Twiss3D(probe.getTwiss().copy()) );
    }
    
    
    /*
     * Property Accessors
     */
    
    
//    /** 
//     * We want to deprecate this method from the base class <code>BunchProbeState</code>
//     * since we do not use beam current right now.
//     * 
//     * @param   dblCurrent  new beam current (not used)
//     * 
//     * @deprecated
//     */
//    @Override
//    public void setBeamCurrent(double dblCurrent)   {
//        super.setBeamCurrent(dblCurrent);
//    }
//
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
     * Set the betatron phase with space charge for each phase plane.
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
     * @param arrTwiss  new 3 dimensional array of Twiss objects (hor, vert,long)
     * 
     * @see xal.tools.beam.Twiss3D
     */
    public void setTwiss(Twiss3D arrTwiss) {
        this.envTwiss = arrTwiss;
    }
     	
	
//    /**
//     * Get the distribution profile descriptor.
//     * 
//     * @return  profile desriptor object for this distribution
//     */
//    public ProfileIndex    getProfile()    {
//        return this.getBunchParameters().getProfile();
//    }
//    
//    /**
//     * We want to deprecate references to the beam current from the base class
//     * <code>BunchProbe</code> since we are not considering it right now.
//     * 
//     * @return  beam current (not used in dynamics)
//     * 
//     * @deprecated  we do not use current in the dynamics
//     */
//    @Override
//    public double   getBeamCurrent()    {
//        return 0.0; // super.getBeamCurrent();
//    }
//    
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
     * Returns the Twiss parameters for this state for all three planes.
     * 
     * @return all three twiss parameter sets 
     */
    public Twiss3D  getTwiss3D()    {
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
     * ICoordinateState Interface
     */
    
    /** 
     * <p>
     *  Returns homogeneous phase space coordinates of the centroid.  The units
     *  are meters and radians.
     *  </p>
     *  <h3>CKA NOTE:</h3>
     *  <p>
     *  - This method simply returns the value of TwissProbeState#getCentroid()
     *  <br>
     *  - It is included to support the <code>IPhaseState</code> interface
     *  </p>
     *
     *  @return     vector (x,x',y,y',z,z',1) of phase space coordinates
     *  
     *  @see    TwissProbeState#getCentroid()
     */
    public PhaseVector getPhaseCoordinates() {
        return this.getCentroid();
    }
    
    /**
     * Get the fixed orbit about which betatron oscillations occur.
     * 
     *  CKA NOTE:
     *  This method simply returns the value of getCentroid().  It is here
     *  for backward compatibility just to satisfy the IPhaseState interface.
     *
     * @return the fixed orbit vector (x,x',y,y',z,z',1)
     */
    public PhaseVector getFixedOrbit() {
        return this.getCentroid();
    }
    
    
    /*
     * IPhaseState Interface
     */
    
    /** 
     * Returns the array of Twiss parameters for this 
     * state for all three planes.
     * 
     * @return array(twiss-H, twiss-V, twiss-L)
     */
    public Twiss[] getTwiss() { 
        Twiss[] arrTwiss = this.envTwiss.getTwiss();
        
        return arrTwiss;
    }
    
    /**
     * Returns the betatron phase with space charge for all three phase
     * planes.
     * 
     * @return  vector (&psi;<sub><i>x</i></sub>,&psi;<sub><i>y</i></sub>,&psi;<sub><i>z</i></sub>) 
     *                  of betatron phases in <b>radians </b>
     */
    public R3 getBetatronPhase() {
        return this.vecPhsBeta;
    }

//    
//    /**
//     * <p>
//     * Convenience function for returning the chromatic dispersion as defined by
//     * D.C. Carey in "The Optics of Charged Particle Beams".  This value is taken
//     * from the response matrix in the off-energy column (z').
//     * </p>
//     * <p>
//     * <h4>NOTE:</h4>
//     * For X and Y coordinates We convert to the conventional definition of 
//     * dispersion dx/(dp/p) by dividing the (x|z') element of the first-order response 
//     * matrix by relativistic gamma squared.
//     * <br>
//     * <br>
//     * See D.C. Carey, "The Optics of Charged Particle Beams".
//     * </p> 
//     * 
//     * @param   index   phase coordinate index of desired "dispersion"
//     * 
//     * @return  chromatic dispersion in <b>meters/radian</b> or <b>radians/radian</b>
//     * 
//     */
//    public double getChromDispersion(PhaseIndex index)  {
//        
//        if (index==PhaseIndex.X || index==PhaseIndex.Y) {
//            double  W  = this.getKineticEnergy();
//            double  Er = this.getSpeciesRestEnergy(); 
//            double  gamma = RelativisticParameterConverter.computeGammaFromEnergies(W, Er);
//            double  d     = this.getResponseMatrix().getElem(index.val(), PhaseMatrix.IND_ZP);
//            
//            return d/(gamma*gamma);
//            
//        } else {
//            double d = this.getResponseMatrix().getElem(index.val(), PhaseMatrix.IND_ZP);
//            
//            return d;
//        }
//    } 
//    
//    
//    /**
//     * Set the "Chromatic dispersion" element of the response matrix.  That is
//     * we set the (index,z') element of the response matrix to the given value.
//     * <br>
//     * <br>
//     *  See D.C. Carey, "The Optics of Charged Particle Beams".
//     *  
//     * @param   index   phase coordinate index of desired "dispersion"
//     * @param   d       dispersion in <b>meters/radian</b> or <b>radians/radian</b>
//     */
//    public void setChromDispersion(PhaseIndex index, double d)  {
//        this.getResponseMatrix().setElem(index.val(), PhaseMatrix.IND_ZP, d);
//    }


    
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
    public TwissProbeState  copy() {
        return new TwissProbeState(this);
    }
    
    /**
     * Save the state values particular to <code>TwissProbeState</code> objects
     * to the data sink.
     * 
     *  @param  daSink   data sink represented by <code>DataAdaptor</code> interface
     */
    @Override
    protected void addPropertiesTo(DataAdaptor daSink) {
        super.addPropertiesTo(daSink);
        
        DataAdaptor daProbe = daSink.createChild(LABEL_TWISSPROBE);
        daProbe.setValue(ATTR_VERSION, INT_VERSION);
        
        DataAdaptor daCent = daProbe.createChild(LABEL_CENT);
        this.getCentroid().save(daCent);
        
        DataAdaptor daResp = daProbe.createChild(LABEL_RESP);
        this.getResponseMatrix().save(daResp);
        
        DataAdaptor daPhase = daProbe.createChild(LABEL_PHASE);
        this.getBetatronPhase().save(daPhase);

        this.getTwiss3D().save(daProbe);
//        DataAdaptor daTwiss = daProbe.createChild(TwissProbeState.LABEL_TWISS);
//        this.getTwiss3D().save(daTwiss);
    }
        
    /**
     * Recover the state values particular to <code>TwissProbeState</code> objects 
     * from the data source.
     *
     *  @param  daSource   data source represented by a <code>DataAdaptor</code> interface
     * 
     *  @exception DataFormatException     state information in data source is malformatted
     */
    @Override
    protected void readPropertiesFrom(DataAdaptor daSource) 
        throws DataFormatException 
    {
        super.readPropertiesFrom(daSource);
        
        DataAdaptor daProbe = daSource.childAdaptor(LABEL_TWISSPROBE);
        if (daProbe == null)
            throw new DataFormatException("TwissProbeState#readPropertiesFrom(): no child element = " + LABEL_TWISSPROBE);
        
        // Read the version number.  We don't do anything with it since there was no version
        //  attribute before version 2.  But it's here if necessary in the future.
        @SuppressWarnings("unused")
        int     intVersion = 0;
        if (daProbe.hasAttribute(ATTR_VERSION))
            intVersion = daProbe.intValue(ATTR_VERSION);
        
        try {
            DataAdaptor daCent = daProbe.childAdaptor(LABEL_CENT);
            if (daCent != null) {
                PhaseVector vecCent = new PhaseVector(daCent);
                this.setCentroid(vecCent);
            }
            
            DataAdaptor daResp = daProbe.childAdaptor(LABEL_RESP);
            if (daResp != null) {
                PhaseMatrix matResp = new PhaseMatrix(daResp);
                this.setResponseMatrix(matResp);
            }
            
            DataAdaptor daPhase = daProbe.childAdaptor(LABEL_PHASE);
            if (daPhase != null) {
                R3  vecPhase = new R3(daPhase);
                this.setBetatronPhase(vecPhase);
            }
            
//            DataAdaptor daTwiss = daProbe.childAdaptor(TwissProbeState.LABEL_TWISS);
//            if (daTwiss != null)   {
//                Twiss3D envTwiss = new Twiss3D(daTwiss);
//                this.setTwiss(envTwiss);
//            }
            
            Twiss3D envTwiss = new Twiss3D(daProbe);
            this.setTwiss(envTwiss);
            
        } catch (DataFormatException e) {
            e.printStackTrace();
            throw new DataFormatException(e.getMessage());
            
        }
    }
    
    
    
//    /**
//     * Set all the analytic bunch description parameters at once.
//     * 
//     * @param   desBunch    encapsulation of all the bunch parameters
//     */
//    private void setBunchParameters(BunchDescriptor desBunch) {
//        this.desBunch = desBunch;
//    }
//
//    
//    /**
//     * Return the <code>BunchDescriptor</code> object encapsulating all the analytic
//     * parameters describing the bunch.
//     * 
//     * @return  analytic parameters describing beam bunch
//     */
//    private BunchDescriptor  getBunchParameters()    {
//        return this.desBunch;
//    }
//
    
    
    
    /*
     * Debugging
     */
     
     
     
//    /**
//     * Write out state information to a string.
//     * 
//     * @return     text version of internal state data
//     */
//    public String toString() {
//        return super.toString() + " correlation: " + getCorrelationMatrix().toString();
//    }   
//    
    
    
    
}
