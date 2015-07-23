package xal.model.probe.traj;

import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.Twiss;
import xal.tools.data.DataAdaptor;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.Probe;
import xal.model.xml.ParsingException;



/**
 * Encapsulates the state of an EnvelopeProbe at a particular point in time.
 * 
 * @author Craig McChesney, Christopher K. Allen
 * @version $id:
 * 
 */
public class EnvelopeProbeState extends BunchProbeState<EnvelopeProbeState> { 
//    implements ProbeStateFactory<EnvelopeProbeState> /* implements IPhaseState */ {



    /*
     * Global Constants
     */

    /** element tag for envelope data */
    protected static final String ENVELOPE_LABEL = "envelope";

//    /** attribute tag for response matrix */
//    private static final String RESP_LABEL = "resp";
//    
//    /** attribute tag for perturbation matrix (current response) */
//    private static final String PERTURB_LABEL = "perturb";
//
    /** element tag for centroid data */
    protected static final String CENTROID_LABEL = "centroid";

    /** attribute tag for centroid value vector */
    private static final String VALUE_LABEL = "value";
    

    
    /** attribute tag for correlation matrix */
    private static final String CORR_LABEL = "correlation";
    
    protected static final String ALPHA_X_LABEL = "alphaX";
    protected static final String BETA_X_LABEL = "betaX";
    protected static final String EMIT_X_LABEL = "emitX";
    protected static final String ALPHA_Y_LABEL = "alphaY";
    protected static final String BETA_Y_LABEL = "betaY";
    protected static final String EMIT_Y_LABEL = "emitY";
    protected static final String ALPHA_Z_LABEL = "alphaZ";
    protected static final String BETA_Z_LABEL = "betaZ";
    protected static final String EMIT_Z_LABEL = "emitZ";
    

    /*
     * Local Attributes
     */
     
    /** current response matrix (Sako) */
    private PhaseMatrix         matPert;

    /** accumulated response matrix */
    private PhaseMatrix         matResp;
    
    
    /** accumulated response matrix (no space charge) */
    private PhaseMatrix         matRespNoSpaceCharge;

    /** envelope state */
    private CovarianceMatrix   matCov;

    
    
//    /** 
//     * the twiss parameters calculated from the transfer matrix 
//     * (not calculated from the correlation matrix, except for
//     * the initialization)
//     * 
//     * CKA NOTES:
//     * - This attribute is redundant in the sense that all "Twiss parameter"
//     * information is contained within the correlation matrix.  The correlation
//     * matrix was intended as the primary attribute for an <code>EnvelopeProbe</code>.
//     * 
//     * - The dynamics of this attribute are computed from transfer matrices,
//     * however, with space charge the transfer matrices are computed using the
//     * correlation matrix.  Thus, these parameters are inconsistent in the 
//     * presence of space charge.
//     * 
//     * - I have made a separate Probe class, <code>TwissProbe</code> which has
//     * Twiss parameters as its primary state.
//     * 
//     * - For all these reason I am deprecating this attribute
//     * 
//     * @deprecated
//     */
//    @Deprecated
//    private Twiss [] twissParams;
    
    
    /** 
     * Instead of saving the covariance matrix to a <code>DataAdaptor</code>
     * the Twiss parameters projected from the covariance matrix are saved.
     * 
     * @deprecated  saving only the Twiss parameters leaves and incomplete state and should be avoided
     */
    @Deprecated
    private boolean bolSaveTwiss = false;


    
    
    
    /*
     * Initialization
     */    


    /**
     * Default constructor.  Create a new, empty <code>EnvelopeProbeState</code> object.
     */    
    public EnvelopeProbeState() {
        super();
        
        this.matCov = CovarianceMatrix.newIdentity();
        this.matPert = PhaseMatrix.identity();
        this.matResp = PhaseMatrix.identity();
        this.matRespNoSpaceCharge = PhaseMatrix.identity();
    }
    
    /**
     * Copy constructor for EnvelopeProbeState.  Initializes the new
     * <code>EnvelopeProbeState</code> objects with the state attributes
     * of the given <code>EnvelopeProbeState</code>.
     *
     * @param prsEnv     initializing state
     *
     * @author Christopher K. Allen, Jonathan M. Freed
     * @since  Jun 26, 2014
     */
    public EnvelopeProbeState(final EnvelopeProbeState prsEnv){
    	super(prsEnv);
    	
    	this.bolSaveTwiss	= prsEnv.bolSaveTwiss;
    	this.matCov			= prsEnv.matCov.clone();
    	this.matPert		= prsEnv.matPert.clone();
    	this.matResp		= prsEnv.matResp.clone();
    	this.matRespNoSpaceCharge = prsEnv.matRespNoSpaceCharge.clone();
    }
	
    /**
     * Initializing Constructor.  Create a new <code>EnvelopeProbeState</code> object and
     * initialize it to the state of the probe argument.
     * 
     * @param probe     <code>EnvelopeProbe</code> containing initializing state information
     */
    public EnvelopeProbeState(final EnvelopeProbe probe) {
        super(probe);
        
        this.setCovariance( probe.getCovariance().clone() );
        this.setResponseMatrix( probe.getResponseMatrix().clone() );
        this.setResponseMatrixNoSpaceCharge( probe.getResponseMatrixNoSpaceCharge().clone() );
        this.setPerturbationMatrix( probe.getCurrentResponseMatrix().clone() );

        //obsolete this.setTwiss(probe.getTwiss());
//        this.twissParams = probe.getCovariance().computeTwiss();
//        this.bolSaveTwiss = probe.getSaveTwissFlag();
//        this.setTwiss(probe.getCovariance().computeTwiss());
//        this.setSaveTwissFlag(probe.getSaveTwissFlag());
	//sako

    }
    
    
    /*
     * Base Class Interface
     */
    
    /**
     * Implements the cloning operation required by the base class
     * <code>ProbeState</code>.
     *
     * @see xal.model.probe.traj.ProbeState#copy()
     *
     * @author Christopher K. Allen
     * @since  Jun 27, 2014
     */
    @Override
    public EnvelopeProbeState   copy() {
        return new EnvelopeProbeState(this);
    }
    
    /*
     * Attribute Setters
     */
    
    /**
     * <p>
     * Changes the behavior of the persistence methods (from the 
     * <code>DataAdaptor</code> methods).
     * By setting this flag to <code>true</code> the Twiss
     * parameter attributes will be saved <b>instead</b> to a <code>DataAdapter</code> 
     * interface rather that the full correlation matrix.  The default behavior for this class
     * is to save the correlation matrix.
     * </p>
     * <h3>CKA Notes:</h3>
     * <p>
     * - This can be dangerous as we have the 
     * potential to loose a lot of information.  In particular,
     * if the probe has pasted through a bend or a steering
     * magnet, the Twiss parameters do not contain enough information
     * to restart the probe.
     * <br> 
     * - This is clearly a kluge; use this method with caution.
     * It is provided to maintain backward compatibility.
     * </p>
     * 
     * @param   bolSaveTwiss    behavior of save state methods 
     * 
     * @see EnvelopeProbeState#addPropertiesTo(DataAdaptor)
     * 
     * @deprecated  Storing only the Twiss parameters leaves an incomplete state 
     *              and may lead to erroneous results
     */
    @Deprecated
    public void setSaveTwissFlag(boolean bolSaveTwiss)    {
        this.bolSaveTwiss = bolSaveTwiss;
    }
    
    /**
     * Set the first-order response matrix of the current element slice
     * 
     * @param matPerturb   first-order response matrix in homogeneous coordinates
     */
    public void setPerturbationMatrix(PhaseMatrix matPerturb)  {
        this.matPert = matPerturb;
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
     * Set the first-order response matrix accumulated by the Envelope since its initial
     * state.  Note that this response does not include the effects of space charge.
     * 
     * @param matResp   first-order response matrix in homogeneous coordinates
     */
    public void setResponseMatrixNoSpaceCharge(PhaseMatrix matResp)  {
        this.matRespNoSpaceCharge = matResp;
    }

    /**
     *  Set the correlation matrix for this probe (7x7 matrix in homogeneous coordinates).
     *
     *  @param  matTau    new phase space covariance matrix of this probe
     *
     *  @see xal.tools.beam.CovarianceMatrix
     */
    public void setCovariance(CovarianceMatrix matTau) {
        matCov = matTau;
    }

//    /** 
//     * Set the twiss parameters for the probe 
//     * 
//     * @param twiss new 3 dimensional array of Twiss objects (horizontal, vertical and longitudinal)
//     * 
//     * @see xal.tools.beam.Twiss
//     * @see EnvelopeProbeState#getTwiss()
//     * 
//     * @deprecated
//     */
//    @Deprecated
//    public void setTwiss(Twiss [] twiss) {
//        twissParams = twiss;
//    }
    
    
    
    /*
     * Attribute Queries
     */
     	
	


    /**
     * Get the first-order response matrix accumulated by the Envelope since its initial
     * state.  Note that this response includes the effects of space charge.
     * 
     * @return  first-order response matrix in homogeneous coordinates
     */
    public PhaseMatrix getResponseMatrix()  {
        return this.matResp;
    }
    
    /**
     * Get the first-order response matrix accumulated by the Envelope since its initial
     * state.  Note that this response does not include the effects of space charge.
     * 
     * @return  first-order response matrix in homogeneous coordinates
     */
    public PhaseMatrix getResponseMatrixNoSpaceCharge()  {
        return this.matRespNoSpaceCharge;
    }
    
    /**
     * Get the first-order response matrix of current element slice
     * 
     * @return  first-order response matrix in homogeneous coordinates
     */
    public PhaseMatrix getPerturbationMatrix()  {
        return this.matPert;
    }
    
    /** 
     *  Returns the correlation matrix of this state in homogeneous 
     *  phase space coordinates.  This is the primary state attribute
     *  for <code>EnvelopeProbe</code> objects.
     * 
     * @return  7x7 matrix &lt;zz^T&gt; in homogeneous coordinates
     */
    public CovarianceMatrix getCovarianceMatrix()   {
        return matCov;
    }
    
    /**
     * Return the save Twiss parameters flag.  If this flag is set then
     * only the Twiss parameters are saved to a <code>DataAdaptor</code>
     * object.
     * 
     * NOTES: 
     * This can be dangerous as we have the 
     * potential to loss a lot of information.  In particular,
     * if the probe has pasted through a bend or a steering
     * magnet, the Twiss parameters do not contain enough information
     * to restart the probe. 
     * 
     * @return Twiss parameter save flag
     * 
     * @see Probe#save(DataAdaptor)
     * @see Probe#applyState(ProbeState)
     * 
     * @deprecated  associated with the redundant state variable <code>twissParams</code>
     */
    @Deprecated
    public boolean getSaveTwissFlag()   {
        return this.bolSaveTwiss;
    }
    
    
    

    /*
     * Computed Properties
     */

    
    /**
     *  Convenience Method: Returns the covariance matrix of this state in 
     *  homogeneous phase space coordinates.  This value is computed directly 
     *  from the correlation matrix.
     * 
     * @return  &lt;<b>zz</b><sup><i>T</i></sup>&gt; - &lt;<b>z</b>&gt;&lt;<b>z</b>&gt;<sup><i>T</i></sup>
     * 
     * @see xal.tools.beam.CovarianceMatrix#computeCentralCovariance()
     */
    public  CovarianceMatrix centralCovariance()   {
        return getCovarianceMatrix().computeCentralCovariance();
    }
    
    /**
     *  Convenience Method:  Returns the rms emittances for this state as
     *  determined by the <b>correlation matrix</b>.  This value is computed
     *  directly from the correlation matrix and is independent of the
     *  <code>twissParams</code> local attribute.
     * 
     * @return array (&epsilon;<sub>x</sub>,&epsilon;<sub>y</sub>,&epsilon;<sub>z</sub>) of rms emittances
     */
    public double[] rmsEmittances() {
        return getCovarianceMatrix().computeRmsEmittances();
    }
    
    /**
     * <p>
     * Return the twiss parameters for this state calculated from the 
     * covariance matrix.
     * </p>
     * <h3>CKA Notes:</h3>
     * <p>
     * - Use this method with caution.  The returned information is incomplete,
     * it is taken only from the three 2&times;2 diagonal blocks of the correlation
     * matrix and, therefore, does not contain the full state of the beam.  In
     * general, you cannot restart the beam with the returned parameters, for
     * example, in the case of bends, offsets, dipoles, etc.
     * </p>
     *
     * @return  twiss parameters computed from diagonal blocks of the correlation matrix
     */
    public Twiss[] twissParameters() {
        return getCovarianceMatrix().computeTwiss();
    }
    
    
//    /**
//     * get the array of twiss objects for this state for all three planes
//     * @deprecated This method does not provide correct Twiss info with any dipole bend presented.  Should use getTwiss() from EnvelopeProbe.
//     * @return array(twiss-H, twiss-V, twiss-L
//     */
//    public Twiss[] getTwiss() {
//        return twissParameters();
//    }
//    
//    
//    /*
//     * CKA - Why do we have three methods that return exactly the same thing?
//     */ 

    /** 
     *  Convenience Method: Return the phase space coordinates of the centroid 
     *  in homogeneous coordinates.  This value is taken from the correlation
     *  matrix.
     *
     *  @return         &lt;z&gt; = (&lt;x&gt;, &lt;xp&gt;, &lt;y&gt;, &lt;yp&gt;, &lt;z&gt;, &lt;zp&gt;, 1)^T
     *  
     *  @see    xal.tools.beam.CovarianceMatrix#getMean()
     */
    public PhaseVector phaseMean()  {
        return getCovarianceMatrix().getMean();
    }
    
    
    
    
    /**
     * <p>
     * Save the state values particular to <code>EnvelopeProbeState</code> objects
     * to the data sink.  In particular we save only the data in the 2x2 diagonal
     * blocks of the correlation matrix, and as Twiss parameters.
     * </p>
     * <h3>CKA NOTE:</h3>
     * <p>
     * - <strong>Be careful</strong> when using this method!  It is here as a convenience only!  
     * It saves the <code>EnvelopeProbeState</code> information in the save format as
     * the load()/save() methods do, but you cannot restore an <code>EnvelopeProbe</code>
     * object from these data.
     * </p>
     * 
     *  @param  daSink   data sink represented by <code>DataAdaptor</code> interface
     */
    public void saveStateAsTwiss(DataAdaptor daSink) {
        DataAdaptor stateNode = daSink.createChild(STATE_LABEL);
        stateNode.setValue(TYPE_LABEL, getClass().getName());
        stateNode.setValue("id", this.getElementId());
        
        super.addPropertiesTo(stateNode);
        
        
        DataAdaptor envNode = stateNode.createChild(EnvelopeProbeState.ENVELOPE_LABEL);
        //sako this is bad for dispersion (2008/07/07) envNode.setValue(EnvelopeProbeState.RESP_LABEL, this.getResponseMatrix().toString());
        //sako this is unnecessary  (2008/07/07) envNode.setValue(EnvelopeProbeState.PERTURB_LABEL, this.getPerturbationMatrix().toString());
        
        Twiss[]   arrTwiss = this.twissParameters();
        
        envNode.setValue(EnvelopeProbeState.ALPHA_X_LABEL, arrTwiss[0].getAlpha());
        envNode.setValue(EnvelopeProbeState.BETA_X_LABEL, arrTwiss[0].getBeta());
        envNode.setValue(EnvelopeProbeState.EMIT_X_LABEL, arrTwiss[0].getEmittance());
        envNode.setValue(EnvelopeProbeState.ALPHA_Y_LABEL, arrTwiss[1].getAlpha());
        envNode.setValue(EnvelopeProbeState.BETA_Y_LABEL, arrTwiss[1].getBeta());
        envNode.setValue(EnvelopeProbeState.EMIT_Y_LABEL, arrTwiss[1].getEmittance());
        envNode.setValue(EnvelopeProbeState.ALPHA_Z_LABEL, arrTwiss[2].getAlpha());
        envNode.setValue(EnvelopeProbeState.BETA_Z_LABEL, arrTwiss[2].getBeta());
        envNode.setValue(EnvelopeProbeState.EMIT_Z_LABEL, arrTwiss[2].getEmittance());           
    }
    
    
    /*
     * ProbeState Overrides
     */ 
    
    /**
     * Save the state values particular to <code>EnvelopeProbeState</code> objects
     * to the data sink.
     * 
     *  @param  container   data sink represented by <code>DataAdaptor</code> interface
     */
    @Override
    protected void addPropertiesTo(DataAdaptor container) {
        super.addPropertiesTo(container);
        
        DataAdaptor envNode = container.createChild(EnvelopeProbeState.ENVELOPE_LABEL);
        //sako this is bad for dispersion (2008/07/07) envNode.setValue(EnvelopeProbeState.RESP_LABEL, this.getResponseMatrix().toString());
//      sako this is unnecessary  (2008/07/07) envNode.setValue(EnvelopeProbeState.PERTURB_LABEL, this.getPerturbationMatrix().toString());
        
        if (!bolSaveTwiss)
            envNode.setValue(EnvelopeProbeState.CORR_LABEL, this.getCovarianceMatrix().toString());
        else {
            //sako
//            this.setTwiss(this.twissParameters());
            
            Twiss[] twiss = this.twissParameters();
            envNode.setValue(EnvelopeProbeState.ALPHA_X_LABEL, twiss[0].getAlpha());
            envNode.setValue(EnvelopeProbeState.BETA_X_LABEL, twiss[0].getBeta());
            envNode.setValue(EnvelopeProbeState.EMIT_X_LABEL, twiss[0].getEmittance());
            envNode.setValue(EnvelopeProbeState.ALPHA_Y_LABEL, twiss[1].getAlpha());
            envNode.setValue(EnvelopeProbeState.BETA_Y_LABEL, twiss[1].getBeta());
            envNode.setValue(EnvelopeProbeState.EMIT_Y_LABEL, twiss[1].getEmittance());
            envNode.setValue(EnvelopeProbeState.ALPHA_Z_LABEL, twiss[2].getAlpha());
            envNode.setValue(EnvelopeProbeState.BETA_Z_LABEL, twiss[2].getBeta());
            envNode.setValue(EnvelopeProbeState.EMIT_Z_LABEL, twiss[2].getEmittance());           
            
            //sako
            envNode.setValue(EnvelopeProbeState.CORR_LABEL, this.getCovarianceMatrix().toString());
            
        }
    }
        
    /**
     * Recover the state values particular to <code>EnvelopeProbeState</code> objects 
     * from the data source.
     *
     *  @param  container   data source represented by a <code>DataAdaptor</code> interface
     * 
     *  @exception ParsingException     state information in data source is malformatted
     */
    @Override
    protected void readPropertiesFrom(DataAdaptor container) 
        throws ParsingException 
    {
        super.readPropertiesFrom(container);
        
        DataAdaptor envNode = container.childAdaptor(EnvelopeProbeState.ENVELOPE_LABEL);
        if (envNode == null)
            throw new ParsingException("EnvelopeProbeState#readPropertiesFrom(): no child element = " + ENVELOPE_LABEL);
        
//        Twiss[] twissParams;
        if (envNode.hasAttribute(EnvelopeProbeState.ALPHA_X_LABEL)) {
            Twiss[] twiss = new Twiss[3];
            twiss[0] = new Twiss(envNode.doubleValue(EnvelopeProbeState.ALPHA_X_LABEL), 
                    envNode.doubleValue(EnvelopeProbeState.BETA_X_LABEL),
                    envNode.doubleValue(EnvelopeProbeState.EMIT_X_LABEL));
            twiss[1] = new Twiss(envNode.doubleValue(EnvelopeProbeState.ALPHA_Y_LABEL), 
                    envNode.doubleValue(EnvelopeProbeState.BETA_Y_LABEL),
                    envNode.doubleValue(EnvelopeProbeState.EMIT_Y_LABEL));
            twiss[2] = new Twiss(envNode.doubleValue(EnvelopeProbeState.ALPHA_Z_LABEL), 
                    envNode.doubleValue(EnvelopeProbeState.BETA_Z_LABEL),
                    envNode.doubleValue(EnvelopeProbeState.EMIT_Z_LABEL));
//            this.setTwiss(twiss);
//            twissParams = twiss;
            
            DataAdaptor parNode = container.childAdaptor(EnvelopeProbeState.CENTROID_LABEL);
            if (parNode == null) {
                this.setCovariance(CovarianceMatrix.buildCovariance(twiss[0], twiss[1], twiss[2]));
//              throw new ParsingException("EnvelopeProbeState#readPropertiesFrom(): no child element = " + EnvelopeProbeState.PARTICLE_LABEL);
            } else {
//                if (parNode.hasAttribute(EnvelopeProbeState.X_LABEL)) {
//                    PhaseVector phaseV = new PhaseVector();
//                    phaseV.setx(parNode.doubleValue(EnvelopeProbeState.X_LABEL));
//                    phaseV.setxp(parNode.doubleValue(EnvelopeProbeState.XP_LABEL));
//                    phaseV.sety(parNode.doubleValue(EnvelopeProbeState.Y_LABEL));
//                    phaseV.setyp(parNode.doubleValue(EnvelopeProbeState.YP_LABEL));
//                    phaseV.setz(parNode.doubleValue(EnvelopeProbeState.Z_LABEL));
//                    phaseV.setzp(parNode.doubleValue(EnvelopeProbeState.ZP_LABEL));
//                
//                    this.setCorrelation(CovarianceMatrix.buildCorrelation(twiss[0], twiss[1], twiss[2], phaseV));
//                }
                if (parNode.hasAttribute(EnvelopeProbeState.VALUE_LABEL))   {
                    String      strCent = parNode.stringValue(EnvelopeProbeState.VALUE_LABEL);
                    PhaseVector vecCent = new PhaseVector(strCent);
                    
                    this.setCovariance(CovarianceMatrix.buildCovariance(twiss[0], twiss[1], twiss[2], vecCent));
                }
                
            }
            
        } else if (envNode.hasAttribute(EnvelopeProbeState.CORR_LABEL))   {
            CovarianceMatrix matChi = new CovarianceMatrix(envNode.stringValue(EnvelopeProbeState.CORR_LABEL));
            this.setCovariance(matChi);
            // initialize the state twiss parameters from the correlation matrix
//            twissParams = matChi.computeTwiss();
//            this.setTwiss(matChi.computeTwiss());
        }
        
        /* sako 2008/07/07
        if (envNode.hasAttribute(EnvelopeProbeState.RESP_LABEL)) {
            PhaseMatrix matResp = new PhaseMatrix(envNode.stringValue(EnvelopeProbeState.RESP_LABEL));
            this.setResponseMatrix(matResp);
        }
        */
        
        /* sako 2008/07/07
        // TODO - treatment for matCurResp?  CKA - Added 2006/11
        if (envNode.hasAttribute(EnvelopeProbeState.PERTURB_LABEL)) {
            PhaseMatrix matPert = new PhaseMatrix(envNode.stringValue(EnvelopeProbeState.PERTURB_LABEL));
            this.setPerturbationMatrix(matPert);
        }
        */
    }

    
    /*
     * Object Overrides
     */
     
    /**
     * Write out state information to a string.
     * 
     * @return     text version of internal state data
     */
    @Override
    public String toString() {
        return super.toString() + " covariance: " + getCovarianceMatrix().toString() 
                                + ", response: " + this.getResponseMatrix().toString();
    }

//	/**
//	 * TODO This method should override an abstract method in the base class
//	 * <code>Probe</code>.  If <code>Probe</code> is refactored so that it
//	 * has a type template parameter <code>S</code>, say
//	 * <br>
//	 * <br>
//	 * &nbsp; &nbsp; <code>class Probe&lt;S extends ProbeState&gt;</code>
//	 * <br>
//	 * <br>
//	 * then this method simply creates the typed probe state and exactly fills
//	 * out the virtual method, which should have a signature
//     * <br>
//     * <br>
//     * &nbsp; &nbsp; <code>public S ProbeState#create()</code>
//     * <br>
//     * <br>
//	 *  
//	 * @return new, uninitialized probe state
//	 *
//	 * @author Christopher K. Allen
//	 * @since  Jun 24, 2014
//	 */
//	public EnvelopeProbeState create() {
//		return new EnvelopeProbeState();
//	}


//    /*
//     * IPhaseCoordinate Interface
//     */
//    
//    /** 
//     *  <p>
//     *  Returns homogeneous phase space coordinates of the particle.  The units
//     *  are meters and radians.
//     *  </p>
//     *  <p>
//     *  <h4>CKA NOTE:</h4>
//     *  This method simply returns the value of EnvelopeProbeState#phaseMean()
//     *  </p>
//     *
//     *  @return     vector (x,x',y,y',z,z',1) of phase space coordinates
//     *  
//     *  @see    EnvelopeProbeState#phaseMean()
//     */
//    @Override
//    public PhaseVector getPhaseCoordinates() {
//        return phaseMean();
//    }
//    
//    /**
//     * <p>
//     * Get the fixed orbit about which betatron oscillations occur.
//     * </p>
//     * <p>
//     * <h4>CKA NOTE:</h4>
//     *  &middot; This method simply returns the value of EnvelopeProbeState#phaseMean()
//     *  <br>
//     *  &middot; This method really has no context unless we are in a ring and then
//     *  it would represent the fixed-orbit position at this state (position), otherwise
//     *  ???
//     * </p>
//     *
//     * @return the fixed orbit vector (x,x',y,y',z,z',1)
//     *  
//     * @see    EnvelopeProbeState#phaseMean()
//     */
//    @Override
//    public PhaseVector getFixedOrbit() {
//        return phaseMean();
//    }
//    
//
//    /*
//     * IPhaseState Interface
//     */
//    
//    /**
//     * <p> 
//     * Returns the (independent attribute) array of Twiss parameters for this 
//     * state for all three planes.
//     * </p>
//     * <p>
//     * <h4>CKA NOTES:</h4>
//     * - This attribute is redundant in the sense that all "Twiss parameter"
//     * information is contained within the covariance matrix.  The covariance
//     * matrix was intended as the primary attribute of an <code>EnvelopeProbe</code>.
//     * <br> 
//     * - The dynamics of this attribute are computed from transfer matrices,
//     * however, with space charge the transfer matrices are computed using the
//     * covariance matrix.  Thus these parameters are inconsistent in the 
//     * presence of space charge.
//     * <br>
//     * - I have made a separate Probe class, <code>TwissProbe</code> which has
//     * Twiss parameters as its primary state.
//     * <br>
//     * - Now this method returns the same quantities as <code>{@link #twissParameters()}</code>
//     * - For all these reason I am deprecating this method
//     * </p>
//     * 
//     * @return array [twiss-H, twiss-V, twiss-L] of Twiss parameters in each phase plane
//     * 
//     * @deprecated redundant state variable
//     */
//    @Deprecated
//    public Twiss[] getTwiss() { 
//        return this.twissParams;
//    }
//
//    /**
//     * Returns the betatron phase with space charge for all three phase
//     * planes.
//     * 
//     * @return  vector (psix,psiy,psiz) of phases in <b>radians</b>
//     */
//    @Override
//    public R3 getBetatronPhase() {
//        return super.getBunchBetatronPhase();
//    }
//    
    
    
    
//=====================================================================================
//
//  A much more practical implemetation is to have a parameter specifying 
//  phase coordinate rather than six separate functions.
//
    
//    /**
//     * Convenience function for returning the x plane chromatic dispersion as defined by
//     * D.C. Carey in "The Optics of Charged Particle Beams".
//     * 
//     * NOTE:
//     * We convert to the conventional definition of dispersion dx/(dp/p) by dividing
//     * the (x|z') element of the first-order response matrix by relativistic gamma 
//     * squared. 
//     * 
//     * @return  x plane chromatic dispersion in <b>meters/radian</b>
//     * 
//     * @see  Reference text D.C. Carey, "The Optics of Charged Particle Beams"
//     */
//    public double getChromDispersionX()  {
//        double  W  = this.getKineticEnergy();
//        double  Er = this.getSpeciesRestEnergy(); 
//        double  gamma = RelativisticParameterConverter.computeGammaFromEnergies(W, Er);
//        double  d     = this.getResponseMatrix().getElem(PhaseMatrix.IND_X, PhaseMatrix.IND_ZP);
//        
//        return d/(gamma*gamma);
//	//return d;//be carefull. Previous J-PARC vesion def is this.
//    } 
//	
//    /**
//     * Convenience function for returning the y plane chromatic dispersion as defined by
//     * D.C. Carey in "The Optics of Charged Particle Beams".
//     * 
//     * NOTE:
//     * We convert to the conventional definition of dispersion dy/(dp/p) by dividing
//     * the (y|z') element of the first-order response matrix by relativistic gamma 
//     * squared. 
//     * 
//     * @return  y plane chromatic dispersion in <b>meters/radian</b>
//     * 
//     * @see D.C. Carey, "The Optics of Charged Particle Beams"
//     */
//    public double getChromDispersionY()  {
//        double  W  = this.getKineticEnergy();
//        double  Er = this.getSpeciesRestEnergy(); 
//        double  gamma = RelativisticParameterConverter.computeGammaFromEnergies(W, Er);
//        double  d     = this.getResponseMatrix().getElem(PhaseMatrix.IND_Y, PhaseMatrix.IND_ZP);
//        
//        return d/(gamma*gamma);
//        //return d;
//    }
//    
//       
////sako, 20 dec 2004, add other dispersion functions
//    /**
//     * Convenience function for returning the x' plane chromatic dispersion as defined by
//     * D.C. Carey in "The Optics of Charged Particle Beams".
//     * 
//     * NOTE:
//     * We convert to the conventional definition of dispersion dx'/(dp/p) by dividing
//     * the (x'|z') element of the first-order response matrix by relativistic gamma 
//     * squared. 
//     * 
//     * @return  x' plane chromatic dispersion in <b>meters/radian</b>
//     * 
//     * @see D.C. Carey, "The Optics of Charged Particle Beams"
//     */
//    public double getChromDispersionXP()  {
//        double  W  = this.getKineticEnergy();
//        double  Er = this.getSpeciesRestEnergy(); 
//        double  gamma = RelativisticParameterConverter.computeGammaFromEnergies(W, Er);
//        double  d     = this.getResponseMatrix().getElem(PhaseMatrix.IND_XP, PhaseMatrix.IND_ZP);
//        
//        return d/(gamma*gamma);
//        //return d;
//    } 
//	
//    /**
//     * Convenience function for returning the y' plane chromatic dispersion as defined by
//     * D.C. Carey in "The Optics of Charged Particle Beams".
//     * 
//     * NOTE:
//     * We convert to the conventional definition of dispersion dy'/(dp/p) by dividing
//     * the (y'|z') element of the first-order response matrix by relativistic gamma 
//     * squared. 
//     * 
//     * @return  y' plane chromatic dispersion in <b>meters/radian</b>
//     * 
//     * @see D.C. Carey, "The Optics of Charged Particle Beams"
//     */
//    public double getChromDispersionYP()  {
//        double  W  = this.getKineticEnergy();
//        double  Er = this.getSpeciesRestEnergy(); 
//        double  gamma = RelativisticParameterConverter.computeGammaFromEnergies(W, Er);
//        double  d     = this.getResponseMatrix().getElem(PhaseMatrix.IND_YP, PhaseMatrix.IND_ZP);
//        
//        return d/(gamma*gamma);
//        //return d;
//    }
//    
//    
//    /**
//     * Convenience function for returning the z plane chromatic dispersion as defined by
//     * D.C. Carey in "The Optics of Charged Particle Beams".
//     * 
//     * NOTE:
//     * We convert to the conventional definition of dispersion dz/(dp/p) by dividing
//     * the (z|z') element of the first-order response matrix by relativistic gamma 
//     * squared. 
//     * 
//     * @return  z plane chromatic dispersion in <b>meters/radian</b>
//     * 
//     * @see D.C. Carey, "The Optics of Charged Particle Beams"
//     */
//    public double getChromDispersionZ()  {
//        //double  W  = this.getKineticEnergy();
//        //double  Er = this.getSpeciesRestEnergy(); 
//        //double  gamma = ParameterConverter.computeGammaFromEnergies(W, Er);
//        double  d     = this.getResponseMatrix().getElem(PhaseMatrix.IND_Z, PhaseMatrix.IND_ZP);
//        
//        //return d/(gamma*gamma);
//        return d;
//    } 
//	
//    /**
//     * Convenience function for returning the z' plane chromatic dispersion as defined by
//     * D.C. Carey in "The Optics of Charged Particle Beams".
//     * 
//     * NOTE:
//     * We convert to the conventional definition of dispersion dzp/(dp/p) by dividing
//     * the (z'|z') element of the first-order response matrix by relativistic gamma 
//     * squared. 
//     * 
//     * @return  z' plane chromatic dispersion in <b>meters/radian</b>
//     * 
//     * @see D.C. Carey, "The Optics of Charged Particle Beams"
//     */
//    public double getChromDispersionZP()  {
//        //double  W  = this.getKineticEnergy();
//        //uble  Er = this.getSpeciesRestEnergy(); 
//        //uble  gamma = ParameterConverter.computeGammaFromEnergies(W, Er);
//        double  d     = this.getResponseMatrix().getElem(PhaseMatrix.IND_ZP, PhaseMatrix.IND_ZP);
//        
//        //return d/(gamma*gamma);
//        return d;
//    }
//    
//    
//    /** setter for dispersion, Sako, 16 Mar 06 */
//    public void setChromDispersionX(double d)  {
//        //double  W  = this.getKineticEnergy();
//        //double  Er = this.getSpeciesRestEnergy(); 
//        //double  gamma = RelativisticParameterConverter.computeGammaFromEnergies(W, Er);
//        this.getResponseMatrix().setElem(PhaseMatrix.IND_X, PhaseMatrix.IND_ZP, d);
//    } 
//	
//    /** setter for dispersion, Sako, 16 Mar 06 */
//    public void setChromDispersionXP(double d)  {
//        //double  W  = this.getKineticEnergy();
//        //double  Er = this.getSpeciesRestEnergy(); 
//        //double  gamma = ParameterConverter.computeGammaFromEnergies(W, Er);
//        this.getResponseMatrix().setElem(PhaseMatrix.IND_XP, PhaseMatrix.IND_ZP, d);
//    } 
//
//    /** setter for dispersion, Sako, 16 Mar 06 */
//    public void setChromDispersionY(double d)  {
//        //double  W  = this.getKineticEnergy();
//        //uble  Er = this.getSpeciesRestEnergy(); 
//        //uble  gamma = RelativisticParameterConverter.computeGammaFromEnergies(W, Er);
//        this.getResponseMatrix().setElem(PhaseMatrix.IND_Y, PhaseMatrix.IND_ZP, d);
//    }
//    /** setter for dispersion, Sako, 16 Mar 06 */
//    public void setChromDispersionYP(double d)  {
//        //double  W  = this.getKineticEnergy();
//        //uble  Er = this.getSpeciesRestEnergy(); 
//        //uble  gamma = ParameterConverter.computeGammaFromEnergies(W, Er);
//        this.getResponseMatrix().setElem(PhaseMatrix.IND_YP, PhaseMatrix.IND_ZP, d);
//    }
//    /** setter for dispersion, Sako, 16 Mar 06 */
//    public void setChromDispersionZ(double d)  {
//        //double  W  = this.getKineticEnergy();
//        //double  Er = this.getSpeciesRestEnergy(); 
//        //double  gamma = ParameterConverter.computeGammaFromEnergies(W, Er);
//        this.getResponseMatrix().setElem(PhaseMatrix.IND_Z, PhaseMatrix.IND_ZP, d);
//    } 
//    /** setter for dispersion, Sako, 16 Mar 06 */
//    public void setChromDispersionZP(double d)  {
//        //double  W  = this.getKineticEnergy();
//        //uble  Er = this.getSpeciesRestEnergy(); 
//        //uble  gamma = ParameterConverter.computeGammaFromEnergies(W, Er);
//        this.getResponseMatrix().setElem(PhaseMatrix.IND_ZP, PhaseMatrix.IND_ZP, d);
//    }
    
    
    
    
//    /**
//     * dispersion x without space charge
//     * 
//     * @return  x plane chromatic dispersion in <b>meters/radian</b>
//     * 
//     * @see D.C. Carey, "The Optics of Charged Particle Beams"
//     */
//    public double getChromDispersionXNoSpaceCharge()  {
//        double  W  = this.getKineticEnergy();
//        double  Er = this.getSpeciesRestEnergy(); 
//        double  gamma = RelativisticParameterConverter.computeGammaFromEnergies(W, Er);
//        double  d     = this.getResponseMatrixNoSpaceCharge().getElem(PhaseMatrix.IND_X, PhaseMatrix.IND_ZP);
//        
//        return d/(gamma*gamma);
//    } 
//	
//    /**
//     *     * dispersion y without space charge
//     * 
//     * @return  y plane chromatic dispersion in <b>meters/radian</b>
//     * 
//     * @see D.C. Carey, "The Optics of Charged Particle Beams"
//     */
//    public double getChromDispersionYNoSpaceCharge()  {
//        double  W  = this.getKineticEnergy();
//        double  Er = this.getSpeciesRestEnergy(); 
//        double  gamma = RelativisticParameterConverter.computeGammaFromEnergies(W, Er);
//        double  d     = this.getResponseMatrixNoSpaceCharge().getElem(PhaseMatrix.IND_Y, PhaseMatrix.IND_ZP);
//        
//        return d/(gamma*gamma);
//    }
//    
//    /**
//     * dispersion x' without space charge
//      * 
//     * @return  x' plane chromatic dispersion in <b>meters/radian</b>
//     * 
//     * @see D.C. Carey, "The Optics of Charged Particle Beams"
//     */
//    public double getChromDispersionXPNoSpaceCharge()  {
//        double  W  = this.getKineticEnergy();
//        double  Er = this.getSpeciesRestEnergy(); 
//        double  gamma = RelativisticParameterConverter.computeGammaFromEnergies(W, Er);
//        double  d     = this.getResponseMatrixNoSpaceCharge().getElem(PhaseMatrix.IND_XP, PhaseMatrix.IND_ZP);
//        
//        return d/(gamma*gamma);
//    } 
//	
//    /**
//     * dispersion y' without space charge
//     * 
//     * @return  y' plane chromatic dispersion in <b>meters/radian</b>
//     * 
//     * @see D.C. Carey, "The Optics of Charged Particle Beams"
//     */
//    public double getChromDispersionYPNoSpaceCharge()  {
//        double  W  = this.getKineticEnergy();
//        double  Er = this.getSpeciesRestEnergy(); 
//        double  gamma = RelativisticParameterConverter.computeGammaFromEnergies(W, Er);
//        double  d     = this.getResponseMatrixNoSpaceCharge().getElem(PhaseMatrix.IND_YP, PhaseMatrix.IND_ZP);
//        
//        return d/(gamma*gamma);
//    }
    
    
    
    
    
//    /**
//     * dispersion x with space charge
// 
//     * 
//     * @return  x plane chromatic dispersion in <b>meters/radian</b>
//     * 
//     * @see Ohkawa, Ikegami, NUM A 576 (2007) 274
//      */
//    public double getChromDispersionXSpaceCharge()  {
//        double  W  = this.getKineticEnergy();
//        double  Er = this.getSpeciesRestEnergy(); 
//        double  gamma = RelativisticParameterConverter.computeGammaFromEnergies(W, Er);
//        double  d     = this.getCovarianceMatrix().getElem(PhaseMatrix.IND_X, PhaseMatrix.IND_ZP)
//        / this.getCovarianceMatrix().getElem(PhaseMatrix.IND_ZP,PhaseMatrix.IND_ZP);
//        
//        return d/(gamma*gamma);//Is gamma necessary?
//    } 
//	
//    /**
//     *     * dispersion y with space charge
//     * 
//     * @return  y plane chromatic dispersion in <b>meters/radian</b>
//     * 
//     * @see D.C. Carey, "The Optics of Charged Particle Beams"
//     */
//    public double getChromDispersionYSpaceCharge()  {
//        double  W  = this.getKineticEnergy();
//        double  Er = this.getSpeciesRestEnergy(); 
//        double  gamma = RelativisticParameterConverter.computeGammaFromEnergies(W, Er);
//        double  d     = this.getResponseMatrixNoSpaceCharge().getElem(PhaseMatrix.IND_Y, PhaseMatrix.IND_ZP)
//                / this.getCovarianceMatrix().getElem(PhaseMatrix.IND_ZP,PhaseMatrix.IND_ZP);
//        
//        return d/(gamma*gamma);
//    }
//    
//    /**
//     * dispersion x' with space charge
//      * 
//     * @return  x' plane chromatic dispersion in <b>meters/radian</b>
//     * 
//     * @see D.C. Carey, "The Optics of Charged Particle Beams"
//     */
//    public double getChromDispersionXPSpaceCharge()  {
//        double  W  = this.getKineticEnergy();
//        double  Er = this.getSpeciesRestEnergy(); 
//        double  gamma = RelativisticParameterConverter.computeGammaFromEnergies(W, Er);
//        double  d     = this.getResponseMatrixNoSpaceCharge().getElem(PhaseMatrix.IND_XP, PhaseMatrix.IND_ZP)
//                / this.getCovarianceMatrix().getElem(PhaseMatrix.IND_ZP,PhaseMatrix.IND_ZP);
//        
//        return d/(gamma*gamma);
//    } 
//	
//    /**
//     * dispersion y' with space charge
//     * 
//     * @return  y' plane chromatic dispersion in <b>meters/radian</b>
//     * 
//     * @see D.C. Carey, "The Optics of Charged Particle Beams"
//     */
//    public double getChromDispersionYPSpaceCharge()  {
//        double  W  = this.getKineticEnergy();
//        double  Er = this.getSpeciesRestEnergy(); 
//        double  gamma = RelativisticParameterConverter.computeGammaFromEnergies(W, Er);
//        double  d     = this.getResponseMatrixNoSpaceCharge().getElem(PhaseMatrix.IND_YP, PhaseMatrix.IND_ZP)
//                / this.getCovarianceMatrix().getElem(PhaseMatrix.IND_ZP,PhaseMatrix.IND_ZP);
//        return d/(gamma*gamma);
//    }
    
}
