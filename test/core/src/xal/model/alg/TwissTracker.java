/*
 * TwissTracker.java
 *
 *  Created : December, 2006
 *  Author  : Christopher K. Allen
 *  
 */
 
package xal.model.alg;

import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseMatrix.IND;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.RelativisticParameterConverter;
import xal.tools.beam.TraceXalUnitConverter;
import xal.tools.beam.Twiss;
import xal.tools.beam.Twiss3D;
import xal.tools.beam.Twiss3D.IND_3D;
import xal.tools.beam.em.BeamEllipsoid;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.data.DataTable;
import xal.tools.data.EditContext;
import xal.tools.data.GenericRecord;
import xal.tools.math.r3.R3;
import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.IdealMagQuad;
import xal.model.elem.IdealRfGap;
import xal.model.probe.TwissProbe;


/**
 * <p>
 * Tracking algorithm for <code>TwissProbe</code> objects.  The <code>TwissProbe</code>'s
 * primary state object, a <code>BunchDescriptor</code> object containing three sets of Twiss
 * parameters (one for each phase plane), is advanced using the linear
 * dynamics portion of any beamline element (<code>IElement</code> exposing object) 
 * Moreover, currently the dynamics are not coupled between phase planes.  The
 * values of the Twiss parameter propagated according to  formula 2.54 from S.Y. Lee's book.
 * </p>
 * <h3>NOTES:</h3>
 * <p>
 * &middot; There is necessarily no coupling between the phase planes from hardware elements.  There
 * can be, however, coupling from space charge effects
 * <br>
 * &middot; Something else?
 * </p> 
 * 
 * @author Christopher K. Allen
 */
public class TwissTracker extends Tracker {

    
    /*
     * Global Constants
     */
    
    
    // EditContext
    /** EditContext table name containing EnvelopeTracker parameters */
    protected final static String      TBL_LBL_TWISSTRACKER = "TwissTracker";
    
    /** Table record primary key name */
    protected final static String      TBL_PRIM_KEY_NAME = "name";
    
    
    // Archiving
    /** data node label for EnvelopeTracker settings */
    protected final static String      LABEL_OPTIONS   = "options";
    
    /** label for simulating space charge effects */
    protected final static String      ATTR_SCHEFF   = "scheff";
    
    /** label for simulating space charge effects */
    protected final static String      ATTR_USESPACECHARGE = "useSpaceCharge";
    
    /** label for emittance growth flag */
    protected final static String      ATTR_EMITGROWTH = "emitgrowth";
    
    /** label for maxstepsize **/
    protected final static String      ATTR_STEPSIZE   = "stepsize";

    
    
    /*
     *  Global Attributes
     */
    
    /** string type identifier for algorithm */
    public static final String      s_strTypeId = TwissTracker.class.getName();
    
    /** current algorithm version */
    public static final int         s_intVersion = 1;
    
    /** probe type recognized by this algorithm */
    public static final Class<TwissProbe>       s_clsProbeType = TwissProbe.class;
    
    
    
    /*
     *  Local Attributes
     */
     
    /** 
     * maximum distance to advance probe before applying space charge kick 
     */
    private double      dblStepSize = 0.004; 

    /**
     * Flag for simulating emittance growth.
     */
    private boolean     bolEmitGrowth = false;
    
    /** 
     * flag for simulating space charge effects.
     */
    private boolean     bolScheff = false;

     
     
    
    /*
     * Initialization
     */

    /** 
     *  Creates a new instance of EnvelopeTracker 
     */
    public TwissTracker() { 
        super(s_strTypeId, s_intVersion, s_clsProbeType);
    };
    
    /** 
     *  Creates a new, empty, instance of EnvelopeTracker.
     *  
     *  This method is a protected constructor meant only for child classes.
     *
     *  @param      strType         string type identifier of algorithm
     *  @param      intVersion      version of algorithm
     *  @param      clsProbeType    class object for probe handled by this algorithm.
     */
    protected TwissTracker(String strType, int intVersion, Class<? extends IProbe> clsProbeType) {
        super(strType, intVersion, clsProbeType);
    }
    
    /**
     * Copy constructor for TwissTracker
     *
     * @param       sourceTracker   Tracker that is being copied
     */
    public TwissTracker( TwissTracker sourceTracker ) {
        super( sourceTracker );
        
        this.dblStepSize = sourceTracker.dblStepSize;
        this.bolEmitGrowth = sourceTracker.bolEmitGrowth;
        this.bolScheff = sourceTracker.bolScheff;
    }
    
    /**
     * Creates a deep copy of TwissTracker
     */
    @Override
    public TwissTracker copy() {
        return new TwissTracker( this );
    }
    
    
    /** 
     * Set the step size used for PMQ and any other
     * exceptional propagation.  Normally we take one step through
     * a beamline element
     *
     * @param   dblStepSize     new step size in <b>meters</b>
     */
    public void setStepSize(double dblStepSize) {
        this.dblStepSize = dblStepSize;
    }
    
    /**
     * Set the emittance growth flag.  If set true then the
     * algorithm will simulate emittance growth through RF gaps.
     * 
     * NOTE:
     * If set, the dynamics will no longer be consistent since
     * the response matrix and betatron phases will not reproduce
     * the current Twiss parameters.
     * 
     * @param   bolEmitGrowth   set true to simulation emittance growth
     * 
     * @see xal.model.elem.IdealRfGap
     */
    public void setEmittanceGrowth(boolean bolEmitGrowth)   {
        this.bolEmitGrowth = bolEmitGrowth;
    }
    
    /** 
     * Method to set the flag to use/notuse spacecharge.  
     * 
     * @param   bolScheff      set whether or not to simulation space charge effects
     */
    public void setUseSpacecharge(boolean bolScheff) {
        this.bolScheff = bolScheff;
    }
    
    

    
    /*
     *  Accessing
     */
    
    /**
     * Returns the element subsection length (in meters) that the probe 
     * may be advanced for exceptional cases.  Typically, probe is propagated
     * through the entire element.
     * 
     * @return  special case step size in <b>meters</b>
     */
    public double getStepSize() {
    	return dblStepSize;
    }

    /**
     * Return the emittance growth flag.
     * 
     * @return  true if we are simulating emittance growth, false otherwise
     * 
     * @see TwissTracker#setEmittanceGrowth(boolean)
     */
    public boolean  getEmittanceGrowthFlag() {
        return this.bolEmitGrowth;
    }
    
    /**
     * Returns the flag determining whether or not space charge effects are being
     * considered during the propagation.
     * 
     * @return  true if space charge forces are used, false otherwise
     */
    public boolean  getSpaceChargeFlag()    {
        return this.bolScheff;
    }

    
    
    /*
     *  Tracker Abstract Methods
     */
    
    /**
     * Propagates the probe through the element.
     *
     *  @param  probe   probe to propagate
     *  @param  elem    element acting on probe
     *
     *  @exception  ModelException  invalid probe type or error in advancing probe
     */
    @Override
    public void doPropagation(IProbe probe, IElement elem) throws ModelException {
        
        int     nSteps;
        double  dblSize;
        
        if(this.getSpaceChargeFlag())
            nSteps = (int) Math.max(Math.ceil(elem.getLength() / getStepSize()), 1);
        else 
            nSteps = 1;
        
        dblSize = elem.getLength() / nSteps;

        for (int i=0 ; i<nSteps ; i++) {
            this.advanceState(probe, elem, dblSize);
            this.advanceProbe(probe, elem, dblSize);
        }
    }
    


    
    /*
     * IArchive Interface
     */
    
    /**
     * Load the parameters of this <code>IAlgorithm</code> object from the
     * table data in the given <code>EditContext</code>.  
     * 
     * Here we load only the parameters specific to the base class.  It is expected
     * that Subclasses should override this method to recover the data particular 
     * to there own operation.
     * 
     * @param   strPrimKeyVal   primary key value specifying the name of the data record
     * @param   ecTableData     EditContext containing table data
     * 
     * @see xal.tools.data.IContextAware#load(String, EditContext)
     */
    @Override
    public void load(final String strPrimKeyVal, final EditContext ecTableData) throws DataFormatException {
        super.load(strPrimKeyVal, ecTableData);
        
        // Get the algorithm class name from the EditContext
        DataTable     tblAlgorithm = ecTableData.getTable( TwissTracker.TBL_LBL_TWISSTRACKER);
        GenericRecord recTracker = tblAlgorithm.record( TwissTracker.TBL_PRIM_KEY_NAME,  strPrimKeyVal );

        if ( recTracker == null ) {
            recTracker = tblAlgorithm.record( TwissTracker.TBL_PRIM_KEY_NAME, "default" );  // just use the default record
        }

        final boolean   bolEmitGrwth = recTracker.booleanValueForKey( TwissTracker.ATTR_EMITGROWTH );
        final boolean   bolUseSpChg = recTracker.booleanValueForKey( TwissTracker.ATTR_SCHEFF );
        final double    dblStepSize = recTracker.doubleValueForKey( TwissTracker.ATTR_STEPSIZE );
        
        this.setStepSize( dblStepSize );
        this.setEmittanceGrowth( bolEmitGrwth );
        this.setUseSpacecharge( bolUseSpChg );
    }    

    /** 
     * Load the parameters of the algorithm from a data source exposing the
     * <code>IArchive</code> interface.
     * The superclass <code>load</code> method is called first, then the properties
     * particular to <code>EnvTrackerAdapt</code> are loaded.
     * 
     * @param   daSource     data source containing state data
     * 
     * @see xal.tools.data.IArchive#load(xal.tools.data.DataAdaptor)
     */
    @Override
    public void load(DataAdaptor daSource) {
        super.load(daSource);
        
        DataAdaptor daTwiss = daSource.childAdaptor(LABEL_OPTIONS);
        
        if (daTwiss != null)  {
            if (daTwiss.hasAttribute(ATTR_EMITGROWTH))
                this.setEmittanceGrowth(daTwiss.booleanValue(ATTR_EMITGROWTH));
            
            if (daTwiss.hasAttribute(ATTR_STEPSIZE)) 
                this.setStepSize( daTwiss.doubleValue(ATTR_STEPSIZE) );

            
                if (daTwiss.hasAttribute(ATTR_SCHEFF)) {
                    this.setUseSpacecharge( daTwiss.booleanValue(ATTR_SCHEFF) );
                    
                } else if (daTwiss.hasAttribute(ATTR_USESPACECHARGE)) { // Backward compatibility
                   
                    this.setUseSpacecharge( daTwiss.booleanValue(ATTR_USESPACECHARGE));
                }
                
            }
        
    }
    
    
    /**
     * Save the state and settings of this algorithm to a data source 
     * exposing the <code>DataAdaptor</code> interface.  Subclasses should
     * override this method to store the data particular to there own 
     * operation.
     * 
     * @param   daSink     data source to receive algorithm configuration
     * 
     * @see xal.tools.data.IArchive#save(xal.tools.data.DataAdaptor)
     */
    @Override
    public void save(DataAdaptor daSink) {
        
//        DataAdaptor daptAlg = daptArchive.createChild(NODETAG_ALG);
//        daptAlg.setValue(ATTRTAG_TYPE, this.getType());
//        daptAlg.setValue(ATTRTAG_VER, this.getVersion());
//        
//        DataAdaptor daptTrack = daptAlg.createChild(NODETAG_TRACKER);
//        daptTrack.setValue(ATTRTAG_DEBUG, this.getDebugMode());
//        daptTrack.setValue(ATTRTAG_UPDATE, this.getProbeUpdatePolicy());
//        daptTrack.setValue(ATTRTAG_RFGAP_PHASE, this.useRfGapPhaseCalculation());

        super.save(daSink);
        
        DataAdaptor daptAlg = daSink.childAdaptor(NODETAG_ALG);
        
        DataAdaptor daptOpt = daptAlg.createChild(LABEL_OPTIONS);
        daptOpt.setValue(ATTR_STEPSIZE, this.getStepSize());
        daptOpt.setValue(ATTR_EMITGROWTH, this.getEmittanceGrowthFlag());
        daptOpt.setValue(ATTR_SCHEFF, this.getSpaceChargeFlag());
        daptOpt.setValue(ATTR_USESPACECHARGE, this.getSpaceChargeFlag());
        
        
    }
     
    
    /*
     *  Internal Support Functions
     */
    
    
    /** 
     *  Advances the probe state through a subsection of the element with the
     *  specified length.  Applies a space charge kick at the end of the element
     *  subsection for any probe having nonzero beam current.
     *
     *  @param  ifcElem     interface to the beam element
     *  @param  ifcProbe    interface to the probe
     *  @param  dblLen      length of element subsection to advance through
     *
     *  @exception ModelException     bad element transfer matrix/corrupt probe state
     */
    protected void advanceState(IProbe ifcProbe, IElement ifcElem, double dblLen) 
        throws ModelException 
    {
        
        // Identify probe
        TwissProbe   probe = (TwissProbe)ifcProbe;
        
        // Get initial conditions of probe
        double              gamma    = probe.getGamma();
        R3                  vecPhs0  = probe.getBetatronPhase();
        Twiss3D             twiss0   = probe.getTwiss();
        PhaseVector         vecCent0 = probe.getCentroid();
        PhaseMatrix         matResp0 = probe.getResponseMatrix();
        
        // Compute the element matrix properties
        double      dW      = ifcElem.energyGain(probe, dblLen);
        PhaseMap    mapElem = ifcElem.transferMap(ifcProbe, dblLen);
        PhaseMatrix matPhi  = mapElem.getFirstOrder();
        
        
        if(this.getSpaceChargeFlag()) {            // Get the space charge kick
            double              K = probe.beamPerveance();
            CovarianceMatrix    matTau = CovarianceMatrix.buildCovariance(probe.getTwiss());
            BeamEllipsoid       ellipsoid = new BeamEllipsoid(gamma, matTau);
            PhaseMatrix         matPhiSC  = ellipsoid.computeScheffMatrix(dblLen/2.0, K);
            
            matPhi  = matPhiSC.times( matPhi.times(matPhiSC) );
        }
        
        
        // sako - put alignment error in sigma matrix
        if (ifcElem instanceof IdealMagQuad)     
            matPhi = this.exceptionIdealQuad((IdealMagQuad)ifcElem, matPhi);
        
        
        // Compute the new probe states
        PhaseVector vecCent1  = matPhi.times( vecCent0 );
        PhaseMatrix matResp1  = matPhi.times( matResp0 );
        Twiss3D     twiss1    = this.computeTwiss(probe, matPhi, dW);
        R3          vecDPhs   = this.compPhaseAdvance(twiss0, twiss1, matPhi);
        R3          vecPhs1   = vecPhs0.plus( vecDPhs );
        
        
        // Save the new state variables in the probe
        probe.setCentroid(vecCent1);
        probe.setResponseMatrix(matResp1);
        probe.setTwiss(twiss1);
        probe.setBetatronPhase(vecPhs1);
        
        
        // sako - emittance growth effect for RFGap
        if (bolEmitGrowth && (ifcElem instanceof IdealRfGap))  {  
            twiss1  = this.exceptionIdealRfGap(probe, (IdealRfGap)ifcElem);
            vecDPhs = this.compPhaseAdvance(twiss0, twiss1, matPhi);
            vecPhs1 = vecPhs0.plus( vecDPhs );
            
            probe.setTwiss(twiss1);
            probe.setBetatronPhase( vecPhs1 );
        }
    };
    
    
    /**
     * Method to handle exceptional case of propagation through an 
     * <code>IdealMagQuad</code> element.  The returned matrix is the
     * transfer matrix for the <code>IdealMagQuadrupole</code> element
     * augmented with any special processing.
     * 
     * The argument <code>matPhi</code> is actually the transfer matrix of the
     * argument <code>elem</code> as computed by the element for some given length.  
     * However, at this point it has already been computed once and we avoid 
     * recomputing <code>matPhi</code> simply by providing it as an argument.
     * 
     * NOTES by H. SAKO
     *  put alignment error in sigma matrix
     * 
     * @param   elem        <code>IdealMagQuad</code> element for exceptional processing
     * @param   matPhi      transfer matrix for <code>elem</code> to be processed
     * 
     * @return  transfer matrix of <code>elem</code> after special processing
     * 
     * @author  Hiroyuki Sako
     * @author  Christopher K. Allen
     */
    private PhaseMatrix exceptionIdealQuad(IdealMagQuad elem, PhaseMatrix matPhi) {
        
        double delx = elem.getAlignX();
        double dely = elem.getAlignY();
        double delz = elem.getAlignZ();
        
        
        if ((delx != 0)||(dely != 0)||(delz !=0)) {
            PhaseMatrix T  = PhaseMatrix.identity();
            PhaseMatrix Ti = PhaseMatrix.identity();
            
            T.setElem(IND.X,IND.HOM, -delx);
            T.setElem(IND.Y,IND.HOM, -dely);
            T.setElem(IND.Z,IND.HOM, -delz);
            
            Ti.setElem(IND.X,IND.HOM, delx);
            Ti.setElem(IND.Y,IND.HOM, dely);
            Ti.setElem(IND.Z,IND.HOM, delz);
            
            matPhi = Ti.times(matPhi).times(T);    
        }
        
        return matPhi;
    }


    /**
     * Method to handle exceptional case of propagation through an 
     * <code>IdealRfGap</code> element.  The returned matrix is the
     * correlation matrix augmented with any special processing.
     * 
     * The argument <code>matTau</code> is the original correlation matrix after
     * (normal) propagation throught the <code>elem</code> element.  
     * 
     * NOTES by H. SAKO
     *  Increase emittance using same (nonlinear) procedure on the second
     *  moments as in Trace3D. 
     *  
     * NOTES by C.K. Allen
     * - Although not as obvious, the procedure is the same as that for 
     * the correlation matrix case (<code>EnvelopeProbe</code>) objects.
     * The <x'x'> element is modified by the formula
     * 
     *      <x'x'> = <x'x'> + kx*<xx>
     *      
     * where kx is the phase spread.  Of course there are simular equations
     * for the other phase planes.  In this case we must adjust the three
     * Twiss parameters (a,b,e) = (<xx'>/e,<xx>/e,<xx><x'x'>-<xx'>^2).
     * 
     * @param   probe       <code>TwissProbe</code> containing twiss parameters
     * @param   elem        <code>IdealRfGap</code> element for exceptional processing
     * 
     * @return  array of Twiss parameters which include emittance growth from RF gap
     * 
     * @author  Hiroyuki Sako
     * @author  Christopher K. Allen
     */
    private Twiss3D exceptionIdealRfGap(TwissProbe probe, IdealRfGap elem) {

        // Loop variables
        double  a0, b0, e0;     // old Twiss parameters
        double  a1, b1, e1;     // new Twiss parameters
        
        double  k;              // "phase spread"
        double  ratio;          // ratio between old and new emittances

        
        // Begin loop
        Twiss3D twissEnv0 = probe.getTwiss();   // old Twiss parameters
        Twiss3D twissEnv1 = new Twiss3D();      // returned new Twiss parameters
        
        for (IND_3D index : IND_3D.values()) {
            
            a0 = twissEnv0.getTwiss(index).getAlpha();
            b0 = twissEnv0.getTwiss(index).getBeta();
            e0 = twissEnv0.getTwiss(index).getEmittance();
            
            if (index != IND_3D.Z)    // transverse plane
                k = this.correctTransSigmaPhaseSpread(probe, elem);
                
            else                            // longitudinal plane
                k = this.correctLongSigmaPhaseSpread(probe, elem);
//                k = elem.compLongFocusing(probe);
                
            ratio = Math.sqrt( 1.0 + k*b0*b0 );

            a1 = a0/ratio;
            b1 = b0/ratio;
            e1 = e0*ratio;
            
            twissEnv1.setTwiss(index, new Twiss(a1, b1, e1));
        }
        
        return twissEnv1;
    }

    
//    /**
//     * Compute and return the betatron phase advance for the centroid produced
//     * by this matrix when used as a transfer matrix.
//     * 
//     * @param   twissEnv0   twiss parameters before action by <code>matPhi</code>
//     * @param   twissEnv1   twiss parameters after action by <code>matPhi</code>
//     * @param   matPhi      transfer matrix
//     * 
//     * @return  vector (sigx,sigy,sigz) of phase advances in <b>radians</b>
//     */
//    private R3   compPhaseAdvance(Twiss3D twissEnv0, Twiss3D twissEnv1, PhaseMatrix matPhi)  {
//        
//        int     iElem;      // matrix element index
//        double  dblR12;     // sub-matrix element R12
//        double  dblPhsAd;   // phase advance
//        double  dblBeta0;   // Twiss beta before action of matPhi
//        double  dblBeta1;   // Twiss beta after action of matPhi
//        R3      vecPhsAd = new R3();    // returned set of phase advances
//        
//        // Loop through each plane
//        for (IND_3D index : IND_3D.values()) {
//            iElem = 2*index.val();
//            dblR12 = matPhi.getElem(iElem, iElem+1);
//            
//            dblBeta0 = twissEnv0.getTwiss(index).getBeta();
//            dblBeta1 = twissEnv1.getTwiss(index).getBeta();
//            
//            final double dblAlphInt = twsInt[mode].getAlpha();
//            
//            final double dblM11 = matPhi.getElem( 2*mode, 2*mode );
//            final double dblM12 = matPhi.getElem( 2*mode, 2*mode + 1 );
//
//            dblPhsAd = Math.asin(dblR12/Math.sqrt(dblBeta0 * dblBeta1) );
//            
//            vecPhsAd.set(index.val(), dblPhsAd);
//            
//        }
//        
//        return vecPhsAd;
//    }
    
    /**
     * Compute and return the betatron phase advance for the given Twiss
     * parameters and transfer matrix.  There are no assumptions on the 
     * Twiss parameters, they do not need to describe the matched envelope.
     * Likewise with the given transfer matrix, it does not need to be a full
     * turn matrix, or the matrix of a periodic cell.
     * The returned values are restricted to the interval [0,2&pi;).
     * 
     * @param   twissEnv0   twiss parameters before action by <code>matPhi</code>
     * @param   twissEnv1   twiss parameters after action by <code>matPhi</code>
     * @param   matPhi      transfer matrix
     * 
     * @return  vector (&psi;<sub><i>x</i></sub>,&psi;<sub><i>y</i></sub>,&psi;<sub><i>z</i></sub>) of phase advances in <b>radians</b>
     */
    private R3 compPhaseAdvance(Twiss3D twsInit, Twiss3D twsFinal, PhaseMatrix matPhi) {

        final Twiss3D twsFnl = twsFinal;
        final Twiss3D twsInt = twsInit;

        final double[] arrPhsAdv = new double[3];
        
        for ( IND_3D mode : IND_3D.values() ) {
            int     iElem = 2 * mode.val();
            
            final double dblBetaFnl = twsFnl.getTwiss(mode).getBeta();
            final double dblBetaInt = twsInt.getTwiss(mode).getBeta();
            
            final double dblAlphInt = twsInt.getTwiss(mode).getAlpha();
            
            final double dblM11 = matPhi.getElem( 2*iElem, 2*iElem );
            final double dblM12 = matPhi.getElem( 2*iElem, 2*iElem + 1 );

            // Compute the phase advance for this plane
            double dblSinPhs = dblM12 / Math.sqrt( dblBetaFnl * dblBetaInt );
            dblSinPhs = Math.max( Math.min( dblSinPhs, 1.0 ), -1.0 );     // make sure it is in the range [-1, 1]

            final double   dblPhsAdv  = Math.asin( dblSinPhs );
            
            // Compute the cosine of phase advance for identifying the phase quadrant
            final double cosPhase = dblM11 * Math.sqrt( dblBetaInt / dblBetaFnl) - dblAlphInt * dblSinPhs;

            // Put the phase advance in the positive real line
            if ( cosPhase >= 0 ) {
                if ( dblSinPhs >= 0 ) {

                    arrPhsAdv[mode.val()] = dblPhsAdv;
                } else {

                    arrPhsAdv[mode.val()] = 2 * Math.PI + dblPhsAdv;                 
                }

            } else {

                arrPhsAdv[mode.val()] = Math.PI - dblPhsAdv;
            }           
        }

        // Pack into vector format and return
        R3 vecPhases = new R3( arrPhsAdv );

        return vecPhases;
    }


    
    /**
     * Advance the twiss parameters using the given transfer matrix based upon
     * formula 2.54 from S.Y. Lee's book.  
     * 
     * CKA NOTES:
     * - This method will only work correctly for a beam that is
     * unccorrelated in the phase planes.
     * 
     * @param probe     probe with target twiss parameters
     * @param matPhi    the transfer matrix of the element
     * @param dW        the energy gain of this element (eV)
     *
     * @return  set of new twiss parameter values
     */
    private Twiss3D computeTwiss(TwissProbe probe, PhaseMatrix matPhi, double dW) {
        
        
        // Compute relativistic parameters ratios
        double ratTran;     // emittance decrease ratio for transverse plane 
        double ratLong;     // emittance decrease ratio for longitudinal plane 
        
        if (dW == 0.0)  {
            ratTran = 1.0;
            ratLong = 1.0;
            
        } else {
            double  ER = probe.getSpeciesRestEnergy();
            double  W0 = probe.getKineticEnergy();
            double  W1 = W0 + dW;
            
            double g0 = probe.getGamma();
            double b0 = probe.getBeta();
            double g1 = RelativisticParameterConverter.computeGammaFromEnergies(W1, ER);
            double b1 = RelativisticParameterConverter.computeBetaFromGamma(g1);

            ratTran = (g0*b0)/(b1*g1);
            ratLong = ratTran*(g0*g0)/(g1*g1 );
            
        }
        
        
        // Twiss parameters
        Twiss3D twissEnv0 = probe.getTwiss();   // old values of Twiss parameters
        Twiss3D twissEnv1 = new Twiss3D();        // propagated values of twiss parameters
        
        double  alpha0, beta0, gamma0;  // old twiss parameters
        double  emit0;                  // old (unnormalized) emittance
        double  alpha1, beta1;          // new twiss parameters
        double  emit1;                  // new (unnormalized) emittance

        // Transfer matrix diagonal sub-block
        double Rjj;     // .
        double Rjjp;    //  | Rjj  Rjjp  |
        double Rjpj;    //  | Rjpj Rjpjp |
        double Rjpjp;   //                .

        int j = 0;
        for (IND_3D index : IND_3D.values()) { // for each phase plane
            j = 2 * index.val();
            
            // assume constant normalized emittance
            alpha0 = twissEnv0.getTwiss(index).getAlpha();
            beta0  = twissEnv0.getTwiss(index).getBeta();
            gamma0 = twissEnv0.getTwiss(index).getGamma();
            emit0  = twissEnv0.getTwiss(index).getEmittance();
            
            Rjj   = matPhi.getElem(j,  j);
            Rjjp  = matPhi.getElem(j,  j+1);
            Rjpj  = matPhi.getElem(j+1,j);
            Rjpjp = matPhi.getElem(j+1,j+1);
            
            beta1  = Rjj*Rjj*beta0 - 2.*Rjj*Rjjp*alpha0 + Rjjp*Rjjp*gamma0;
            alpha1 = -Rjj*Rjpj*beta0 + (Rjj*Rjpjp + Rjjp*Rjpj)*alpha0 - Rjjp*Rjpjp*gamma0;

            if (index==IND_3D.Z) // longitudinal plane
                emit1 = emit0 * ratLong; 
            else     // transver plane
                emit1 = emit0 * ratTran;
            
            twissEnv1.setTwiss(index, new Twiss(alpha1, beta1, emit1) );
        }
        
        return twissEnv1;
    }
    
    
    
    /**
     * calculation of emittance increase due to phase spread
     * based on calculations in Trace3d (RfGap.f)
     * 
     * @param probe
     * 
     * @return
     */
    private double correctTransSigmaPhaseSpread(TwissProbe probe, IdealRfGap elem) {
        
        double dfac = 1;
        
        double phi = elem.getPhase();
        double dphi = phaseSpread(probe, elem);
        double f1   = correctTransFocusingPhaseSpread(probe, elem);
        double tdp = 2*dphi;
        double f2 = 1-tdp*tdp/14;
        if (tdp>0.1) {
            double sintdp = Math.sin(tdp);
            f2 = 3*(sintdp/tdp-Math.cos(tdp))/tdp/tdp; //APPENDIX F (Trace3D manual)
            f2 = 15*(f2-sintdp/tdp)/tdp/tdp;
        }
        double sinphi = Math.sin(phi);
        double cosphi = Math.cos(phi);
        double G1 = 0.5*(1+(sinphi*sinphi-cosphi*cosphi)*f2);
        double Q = probe.getSpeciesCharge();
        double h = 1;//harmic number
        double m = probe.getSpeciesRestEnergy();
        double w = probe.getKineticEnergy();
        double dw = elem.energyGain(probe);
        double wa = w+dw/2;
        double betagammaa = Math.sqrt(wa/m*(2+wa/m));
        
        double wf = w+dw;
        double betagammaf =Math.sqrt(wf/m*(2+wf/m));
        
        
        double clight = IProbe.LightSpeed;
        double freq = elem.getFrequency();
        double lambda = clight/freq;
        
        double cay = Math.abs(Q)*h*Math.PI*elem.getETL()/(m*betagammaa*betagammaa*betagammaf*lambda); //Kx'
        
        dfac = cay*cay*(G1-sinphi*sinphi*f1*f1);
        
        return dfac;
    }
    
    /**
     * calculation of emittance increase due to phase spread
     * based on calculations in Trace3d (RfGap.f)
     * 
     * @param probe     beam probe with finite phase spread
     * @param elem      RF Gap producing the emittance increase
     * 
     * @return          incremental increase in longitudinal RMS emittance for probe
     */
    private double correctLongSigmaPhaseSpread(TwissProbe probe, IdealRfGap elem) {
        
        double phi = elem.getPhase();
        double dphi = this.phaseSpread(probe, elem);
        double tdp = 2*dphi;
        
        double f2 = 1-tdp*tdp/14;
        if (tdp>0.1) {
            double sintdp = Math.sin(tdp);
            f2 = 3*(sintdp/tdp-Math.cos(tdp))/tdp/tdp;
            f2 = 15*(f2-sintdp/tdp)/tdp/tdp;
        }
        double sinphi = Math.sin(phi);
        double cosphi = Math.cos(phi);
        
        double Q = probe.getSpeciesCharge();
        double h = 1;//harmic number
        double m = probe.getSpeciesRestEnergy();
        double w = probe.getKineticEnergy();
        double dw = elem.energyGain(probe);
        double wa = w+dw/2;
        double gammaa = (wa+m)/m;
        double betagammaa = Math.sqrt(wa/m*(2+wa/m));
//        double betagamma0 = Math.sqrt(w/m*(2+w/m));
        double clight = IProbe.LightSpeed;
        double freq = elem.getFrequency();
        double lambda = clight/freq;
        
        
        double wf = w+dw;
        double betagammaf =Math.sqrt(wf/m*(2+wf/m));
        
        double cay = h*Math.PI*elem.getETL()*Math.abs(Q)/(m*betagammaa*betagammaa*betagammaf*lambda);
        
        double cayz = 2*cay*gammaa*gammaa;
        double cayp = cayz*cayz*dphi*dphi;
        double dfac = cayp*(0.125*cosphi*cosphi+(1./576.)*dphi*dphi*sinphi*sinphi);
        
        return dfac;
    }


    /**
     * Appears to be a 2nd-order approximation of something in
     * Appendix F of the Trace3D manual.
     * 
     * @param probe     beam probe with finite phase spread
     * @param elem      RF Gap producing the emittance increase
     * 
     * @return          incremental increase in transverse RMS emittance for probe
     */
    private double correctTransFocusingPhaseSpread(TwissProbe probe, IdealRfGap elem) {
        double cor;

        double dphi = this.phaseSpread(probe, elem);
        
        if (dphi > 0.1) {
            cor = 15/dphi/dphi*(3/dphi/dphi*(Math.sin(dphi)/dphi-Math.cos(dphi))-Math.sin(dphi)/dphi);  

        } else {    // Avoid the sinc function pole 
            cor = 1-dphi*dphi/14;

        }
        return cor;
    }
    
    /** 
     * Compute the phase spread of the bunch for a probe (based on Trace3D RfGap.f)
     * 
     * CKA Notes:
     * - This method needs to be optimized now that I understand what it is doing.
     * In XAL, longitundinal coordinate <i>z</i> is the "phase spread", but in meters. 
     * To convert to phase spread <i>dphi</i> in radians we have
     * 
     *      dphi = (z/(beta*lambda))*2*pi
     *      
     * where lambda is the wavelength of the RF.  So, for <dphi^2> we get
     * 
     *      <dphi^2> = 2*Pi*<z^2>*f/(beta*c)
     *   
     * where f is the RF frequency of the gap and c is the speed of light.
     * 
     * - For the optional computation <b>phaseSpreadT3d</b> (which apparently is not
     * used) I am not sure what is happening, or why <y'y'> is significant?
     * 
     *
     * @param  probe
     *
     * @return  phase spread (half width) for this probe (<b>radian</b>)
     * 
     * @author Hiroyuki Sako
     */
    private double phaseSpread(TwissProbe probe, IdealRfGap elem) {
        
        
        double Er = probe.getSpeciesRestEnergy();
        double Wi = probe.getKineticEnergy();
        
        Twiss[] twiss = probe.getTwiss().getTwiss();
        
        TraceXalUnitConverter t3dxal = TraceXalUnitConverter.newConverter(elem.getFrequency(),Er,Wi);
        Twiss twissLongT3d = t3dxal.xalToTraceLongitudinal(twiss[2]);
        
        double emitz = twissLongT3d.getEmittance();
        double betaz = twissLongT3d.getBeta();
        
        return Math.sqrt(emitz*betaz)*2*Math.PI/360.0; //radian
    }
    
    

}



/*
 *  Storage
 */
 
 
