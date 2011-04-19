/**
 * TwissProbeState.java
 * 
 * Created : December, 2006
 * Author  : Christopher K. Allen
 * 
 * 
 */
package xal.model.probe.traj;

import xal.tools.beam.SpaceIndex3D;
import xal.tools.beam.PhaseIndex;
import xal.tools.beam.RelativisticParameterConverter;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.Twiss;
import xal.tools.beam.Twiss3D;

import xal.tools.data.IDataAdaptor;
import xal.tools.data.DataFormatException;

import xal.model.probe.TwissProbe;
import xal.model.xml.ParsingException;



/**
 * Saves the state of a <code>TwissProbe</code> at a particular instance.
 * 
 * @author Christopher K. Allen
 * @version $id:
 * 
 */
public class TwissProbeState extends BunchProbeState implements IPhaseState {



    /*
     * Global Constants
     */

    
    /** element label for twiss probe data */
    protected static final String   LABEL_TWISSPROBE = "twissprobe";
    
    /** element label for centroid vector */
    protected static final String   LABEL_CENT = "centroid";

    /** element label for response matrix */
    protected static final String   LABEL_RESP = "resp";
    
    /** element label for twiss parameters */
    protected static final String   LABEL_TWISS = "twiss";
    
    /** general value attribute tag */
    protected static final String   ATTR_VALUE = "value";
    
    

    /*
     * Local Attributes
     */

    /** centroid position in phase space */
    private PhaseVector         vecCent;
    
    /** accumulated response matrix */
    private PhaseMatrix         matResp;

    /** current twiss parameters */
    private Twiss3D             envTwiss;
    
    
//    /** the analytic bunch properties */
//    private BunchDescriptor     desBunch;
//    
    
    
    
    
    /*
     * Initialization
     */    


    /**
     * Default constructor.  Create a new, empty <code>EnvelopeProbeState<code> object.
     */    
    public TwissProbeState() {
        super();
//        this.desBunch = new BunchDescriptor();
        this.vecCent = PhaseVector.zero();
        this.matResp = PhaseMatrix.identity();
        this.envTwiss = new Twiss3D();
    }
	
    /**
     * Initializing Constructor.  Create a new <code>TwissProbeState</code> object and
     * initialize it to the state of the probe argument.
     * 
     * @param probe     <code>TwissProbe</code> containing initializing state information
     */
    public TwissProbeState(TwissProbe probe) {
        super(probe);
        this.setCentroid(probe.getCentroid());
        this.setResponseMatrix(probe.getResponseMatrix());
        this.setTwiss(probe.getTwiss());
//        this.setBunchParameters(probe.getBunchParameters());
    }
    
    
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
     * Set the Twiss parameters for the given phase plane.
     * 
     * @param   iPlane  phase plane index
     * @param   twiss   twiss parameters
     */
    public void setTwiss(SpaceIndex3D iPlane, Twiss twiss)   {
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
    
    
    
    /*
     * Attribute Queries
     */
     	
	
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
    public Twiss    getTwiss(SpaceIndex3D iPlane)    {
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
        
        for (SpaceIndex3D i : SpaceIndex3D.values()) 
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
     *  <p>
     *  <h4>CKA NOTE:</h4>
     *  - This method simply returns the value of TwissProbeState#getCentroid()
     *  <br/>
     *  - It is included to support the <code>IPhaseState</code> interface
     *  </p>
     *
     *  @return     vector (x,x',y,y',z,z',1) of phase space coordinates
     *  
     *  @see    TwissProbeState#getCentroid()
     */
    public PhaseVector phaseCoordinates() {
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
     *  
     * @see    xal.model.probe.traj.IPhaseState
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
        Twiss[] arrTwiss = new Twiss[3];
        
        for (SpaceIndex3D index : SpaceIndex3D.values())    {
            arrTwiss[index.val()] = this.getTwiss(index);
        }
        
        return arrTwiss;
    }
    

    
    /**
     * <p>
     * Convenience function for returning the chromatic dispersion as defined by
     * D.C. Carey in "The Optics of Charged Particle Beams".  This value is taken
     * from the response matrix in the off-energy column (z').
     * </p>
     * <p>
     * <h4>NOTE:</h4>
     * For X and Y coordinates We convert to the conventional definition of 
     * dispersion dx/(dp/p) by dividing the (x|z') element of the first-order response 
     * matrix by relativistic gamma squared.
     * <br/>
     * <br/>
     * See D.C. Carey, "The Optics of Charged Particle Beams".
     * </p> 
     * 
     * @param   index   phase coordinate index of desired "dispersion"
     * 
     * @return  chromatic dispersion in <b>meters/radian</b> or <b>radians/radian</b>
     * 
     */
    public double getChromDispersion(PhaseIndex index)  {
        
        if (index==PhaseIndex.X || index==PhaseIndex.Y) {
            double  W  = this.getKineticEnergy();
            double  Er = this.getSpeciesRestEnergy(); 
            double  gamma = RelativisticParameterConverter.computeGammaFromEnergies(W, Er);
            double  d     = this.getResponseMatrix().getElem(index.val(), PhaseMatrix.IND_ZP);
            
            return d/(gamma*gamma);
            
        } else {
            double d = this.getResponseMatrix().getElem(index.val(), PhaseMatrix.IND_ZP);
            
            return d;
        }
    } 
    
    
    /**
     * Set the "Chromatic dispersion" element of the response matrix.  That is
     * we set the (index,z') element of the response matrix to the given value.
     * <br/>
     * <br/>
     *  See D.C. Carey, "The Optics of Charged Particle Beams".
     *  
     * @param   index   phase coordinate index of desired "dispersion"
     * @param   d       dispersion in <b>meters/radian</b> or <b>radians/radian</b>
     */
    public void setChromDispersion(PhaseIndex index, double d)  {
        this.getResponseMatrix().setElem(index.val(), PhaseMatrix.IND_ZP, d);
    }


    
    /*
     * Support Methods
     */ 
    
    
    /**
     * Save the state values particular to <code>TwissProbeState</code> objects
     * to the data sink.
     * 
     *  @param  daSink   data sink represented by <code>IDataAdaptor</code> interface
     */
    @Override
    protected void addPropertiesTo(IDataAdaptor daSink) {
        super.addPropertiesTo(daSink);
        
        IDataAdaptor daProbe = daSink.createChild(TwissProbeState.LABEL_TWISSPROBE);
        
        IDataAdaptor daCent = daProbe.createChild(TwissProbeState.LABEL_CENT);
        this.getCentroid().save(daCent);
        
        IDataAdaptor daResp = daProbe.createChild(TwissProbeState.LABEL_RESP);
        this.getResponseMatrix().save(daResp);

        this.getTwiss3D().save(daProbe);
//        IDataAdaptor daTwiss = daProbe.createChild(TwissProbeState.LABEL_TWISS);
//        this.getTwiss3D().save(daTwiss);
    }
        
    /**
     * Recover the state values particular to <code>TwissProbeState</code> objects 
     * from the data source.
     *
     *  @param  daSource   data source represented by a <code>IDataAdaptor</code> interface
     * 
     *  @exception ParsingException     state information in data source is malformatted
     */
    @Override
    protected void readPropertiesFrom(IDataAdaptor daSource) 
        throws ParsingException 
    {
        super.readPropertiesFrom(daSource);
        
        IDataAdaptor daProbe = daSource.childAdaptor(TwissProbeState.LABEL_TWISSPROBE);
        if (daProbe == null)
            throw new ParsingException("TwissProbeState#readPropertiesFrom(): no child element = " + LABEL_TWISSPROBE);
        
        try {
            IDataAdaptor daCent = daProbe.childAdaptor(TwissProbeState.LABEL_CENT);
            if (daCent != null) {
                PhaseVector vecCent = new PhaseVector(daCent);
                this.setCentroid(vecCent);
            }
            
            IDataAdaptor daResp = daProbe.childAdaptor(TwissProbeState.LABEL_RESP);
            if (daResp != null) {
                PhaseMatrix matResp = new PhaseMatrix(daResp);
                this.setResponseMatrix(matResp);
            }
            
//            IDataAdaptor daTwiss = daProbe.childAdaptor(TwissProbeState.LABEL_TWISS);
//            if (daTwiss != null)   {
//                Twiss3D envTwiss = new Twiss3D(daTwiss);
//                this.setTwiss(envTwiss);
//            }
            
            Twiss3D envTwiss = new Twiss3D(daProbe);
            this.setTwiss(envTwiss);
            
        } catch (DataFormatException e) {
            e.printStackTrace();
            throw new ParsingException(e.getMessage());
            
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
