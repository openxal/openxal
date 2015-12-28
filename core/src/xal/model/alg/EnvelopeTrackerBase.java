/*
 * EnvelopeTrackerBase.java
 * 
 * 
 * @author Christopher K. Allen
 * @since Feb 10, 2009
 * 
 */
package xal.model.alg;

import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.TraceXalUnitConverter;
import xal.tools.beam.Twiss;
import xal.tools.beam.PhaseMatrix.IND;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.em.BeamEllipsoid;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.data.DataTable;
import xal.tools.data.EditContext;
import xal.tools.data.GenericRecord;
import xal.tools.math.BesselFunction;
import xal.tools.math.ElementaryFunction;
import xal.tools.math.EllipticIntegral;
import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.IdealRfGap;
import xal.model.elem.IdealRfGapUpgraded;
import xal.model.probe.EnvelopeProbe;




/**
 * <h1>Abstract Base Class for Algorithms Propagating RMS Envelopes</h1>
 * 
 * <p>This class adds functionality beyond the base class <code>Tracker</code>
 * specific to tracker algorithms designed for RMS envelope beam probes, that is, 
 * probes which carry the RMS statistical properties as their beam state.
 * </p>
 * 
 * @author Christopher K. Allen
 * 
 * @since Feb 10, 2009
 */
public abstract class EnvelopeTrackerBase extends Tracker {


    /**
     * Enumerations for supported phase planes
     * 
     * @author Christopher K. Allen
     * @since Feb 19, 2009
     */
    
    public enum PhasePlane {
        
        /** Either transverse phase plane: horizontal <i>x</i> or vertical <i>y</i>. */
        TRANSVERSE  (1),
        
        /** The longitudinal phase plane <i>z</i> */
        LONGITUDINAL (2);
        
        /*
         * Local Attributes
         */
        
        /** index value (enumeration constant) */
        private final int i;
        
        
        /** 
         * Default enumeration constructor
         * 
         * @param i     Enumeration constant for phase plane
         */
        PhasePlane(int i)        { this.i = i; };
        
        /** 
         * Return the integer value of the index position
         *  
         * @return  Integer value of enumeration constant
         */
        public int val()    { return i; };
    }
    
    
    
    /**
     * <h1>RF Gap Emittance Growth Models</h1>
     * <p>
     * Enumerations identifying supported emittance growth models for
     * RF accelerating gaps. (Currently this applies primarily to the 
     * longitudinal situation.)
     * at the moment.  
     * </p>
     * <p>
     * Since longitudinal emittance growth mechanism is
     * not well documented in the <code>Trace3D</code> manual,
     * it is somewhat unclear how the effect is being modeled.  However,
     * it does appear to create a result that tracks the
     * <code>IMPACT</code> simulation better than the CKA method (at
     * least for one case).  At this point it is unclear whether there
     * is an error in the analysis of C.K. Allen, or <code>Trace3D</code>
     * is simply more accurate.
     * </p>
     * <p>
     * <strong>VALUES</strong>:
     * <br>
     * Currently there are two supported mechanisms for emittance
     * growth.
     * <br>
     * <code>Trace3D</code>: use the same mechanism described in the 
     * Trace3D manual.
     * <br>
     * <code>CKAllen</code>: use the mechanism described by C.K. Allen, 
     * <i>et. al.</i> (see below).
     * </p>
     * <p>
     * <strong>References</strong>
     * <br>
     * [1] M. Weiss,
     *      "Bunching of Intense Proton Beams with Six-Dimensional
     *       Matching to the Linac Acceptance", CERN/MPS/LI report 73-2,
     *       Geneva, Switzerland (1978).
     * <br>
     * [2] K.R. Crandall and D.P. Rusthoi, 
     *          "Trace 3-D Documentation", 
     *          LANL Report LA-UR-97-887 (1997), Appendix F.
     * <br>
     * [3] C.K. Allen, H. Sako, M. Ikegami, and G. Shen, "Emittance Growth
     *         Due to Phase Spread 
     *         for Proton Beams in Radio Frequency Accelerating Gaps", 
     *         (in preperation).
     * </p>
     * 
     * @author Christopher K. Allen
     * @since Feb 23, 2009
     */
    
    public enum EmitGrowthModel {
        
        /** Use exact model of Trace3D*/
        TRACE3D,
        
        /** Uniform distribution in each (uncorrelated) phase plane */
        UNIFORM1D,
        
        /** Gaussian distribution in each (uncorrelated) phase plane */
        GAUSSIAN1D,
        
        /** Uniform distribution in three spatial dimensions */
        UNIFORM3D,
        
        /** Gaussian distribution in three spatial dimensions */
        GAUSSIAN3D;
        
    }
    
    
    
    
    /*
     * Global Constants
     */
    
    /** EditContext table name containing basic envelope tracking parameters */
    public static final String TBL_LBL_ENVBASETRACKER = "EnvelopeBaseTracker";
    
    /** Table record primary key name */
//    public static final String TBL_PRIM_KEY_NAME = "name";
    
    /** data node label for EnvelopeTracker settings */
    public static final String LABEL_OPTIONS = "options";
    
    /** label for use simple tracking (no space charge) */
    public static final String ATTR_SCHEFF = "scheff";
    
    /** label for use simple tracking (no space charge) */
    public static final String ATTR_USESPACECHARGE = "useSpacecharge";
    
    /** label for emittance growth flag */
    protected static final String ATTR_EMITGROWTH = "emitgrowth";
    
    /** label for maximum step size **/
    public static final String ATTR_STEPSIZE = "stepsize";


    
    /*
     * LOCAL CONSTANTS
     */
    
    /** Conditional value where polynomial expansions are employed */
    private static final double SMALL_ARG = BesselFunction.SMALL_ARG;

    /** upright ellipse tolerance for using expedited space charge calculation */
    private static final double TOLER_CORRELATION = 0.01;
    
    /** distribution dependence factor - use that for the uniform beam */
    private static final double CONST_UNIFORM_BEAM = Math.pow(5.0, 1.5);
    
    
    /*
     * Local Variables
     */
    
    /** maximum distance to advance probe before applying space charge kick */
    private double dblMaxStep = 0.004;
    
    
    /** flag for using space charge */
    private boolean bolScheff = true;
    
    /** flag for simulating emittance growth */
    private boolean bolEmitGrowth = false;
    
    /** longitudinal emittance growth model - Default is TRACE3D */
    private EmitGrowthModel enmEmitGrowthModel = EmitGrowthModel.TRACE3D;

    
    
    
    
    
    
    
    
    
    /**
     * <h2>EnvelopeTrackerBase Constructor</h2>
     * <p>
     * This should be used by child classes to pass up
     * their class properties.
     * </p>
     *
     * @param strType       string type identifier of the class
     * @param intVersion    version number of class implementation
     * @param clsProbeType  the class type of valid probe
     */
    public EnvelopeTrackerBase(String strType, int intVersion,
            Class<? extends IProbe> clsProbeType) {
        super(strType, intVersion, clsProbeType);
    }

    /**
     * Copy constructor for EnvelopeTracker
     *
     * @param       sourceTracker   Tracker that is being copied
     */
    public EnvelopeTrackerBase(EnvelopeTrackerBase sourceTracker) {
        super(sourceTracker);

        this.dblMaxStep = sourceTracker.dblMaxStep;
        this.bolScheff = sourceTracker.bolScheff;
        this.bolEmitGrowth = sourceTracker.bolEmitGrowth;
        this.enmEmitGrowthModel = sourceTracker.enmEmitGrowthModel;
    }

    
    //public EnvelopeTrackerBase copy();

    /** 
     * Set maximum step size allowed between space charge kicks 
     * 
     * @param step      the new maximum step size for space charge calculations
     * 
     * @author Hiroyuki Sako
     */
    public void setStepSize(double step) {
        dblMaxStep = step;
    }

    /** 
     * <p>
     * Method to toggle the flag to use/not use space charge calculations. 
     * </p>
     * @param tf    the truth flag 
     */
    public void setUseSpacecharge(boolean tf) {
        bolScheff = tf;
    }

    /**
     * <p>
     * Set the emittance growth flag.  If set true then the
     * algorithm will simulate emittance growth through RF gaps.
     * </p>
     * <p>
     * <strong>NOTE</strong>: (CKA)
     * <br>
     * &middot; If set, the dynamics will no longer be consistent since
     * the response matrix and betatron phases will not reproduce
     * the current Twiss parameters.
     * </p>
     * 
     * @param   bolEmitGrowth   set true to simulation emittance growth
     * 
     * @see xal.model.elem.IdealRfGap
     * @see #setEmitGrowthModel(EmitGrowthModel)
     */
    public void setEmittanceGrowth(boolean bolEmitGrowth) {
        this.bolEmitGrowth = bolEmitGrowth;
    }

    /**
     * <h2>Set the emittance growth mechanism</h2>
     * <p>
     * Set the current mechanism for simulation emittance
     * growth from RF accelerating gaps.  We can either use the 
     * same model as <code>Trace3D</code> or the
     * generalized technique described in C.K. Allen <i>et. al.</i> 
     * (see references below).  This applies primarily to the 
     * longitudinal situation since the transverse cases show
     * very good agreement.
     * </p>
     * <p>
     * Since longitudinal emittance growth mechanism is
     * not well documented in the <code>Trace3D</code> manual,
     * it is somewhat unclear how the effect is being modeled.  However,
     * it does appear to create a result that tracks the
     * <code>IMPACT</code> simulation better than the CKA method (at
     * least for one case).  At this point it is unclear whether there
     * is an error in the analysis of C.K. Allen, or <code>Trace3D</code>
     * is simply more accurate.
     * </p>
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * <br>
     * &middot; This flag only has an effect when the 
     * <code>setEmittanceGrowth()</code> feature is set to <code>true</code>.
     * </p>
     * <p>
     * <strong>References</strong>
     * <br>
     * [1] M. Weiss,
     *      "Bunching of Intense Proton Beams with Six-Dimensional
     *       Matching to the Linac Acceptance", CERN/MPS/LI report 73-2,
     *       Geneva, Switzerland (1978).
     * <br>
     * [2] K.R. Crandall and D.P. Rusthoi, 
     *          "Trace 3-D Documentation", 
     *          LANL Report LA-UR-97-887 (1997), Appendix F.
     * <br>
     * [3] C.K. Allen, H. Sako, M. Ikegami, and G. Shen, "Emittance Growth
     *         Due to Phase Spread 
     *         for Proton Beams in Radio Frequency Accelerating Gaps", 
     *         (in preperation).
     * </p>
     * 
     * @param    enmModel  use the <code>Trace3D</code> method or the 
     *                    C.K. Allen <i>et. al.</i> method 
     */
    public void setEmitGrowthModel(EmitGrowthModel enmModel) {
        this.enmEmitGrowthModel = enmModel;
    }

    /**
     * <p>
     * Returns the maximum element subsection length (in meters) that the probe 
     * may be advanced before applying a space charge kick when space charge
     * is present.
     * </p>
     * 
     * @return  Maximum space charge integration step size.
     * 
     */
    public double getStepSize() {
    	return dblMaxStep;
    }

    /**
     * Returns the flag determining whether or not space charge effects are being
     * considered during the propagation.
     * 
     * @return  true if space charge forces are used, false otherwise
     */
    public boolean getUseSpacecharge() {
    	return this.bolScheff;
    }
    
    @Deprecated
    public boolean getSpaceChargeFlag() {
        return getUseSpacecharge();
    }

    /**
     * Return the emittance growth flag.
     * 
     * @return  true if we are simulating emittance growth, false otherwise
     * 
     * @see EnvelopeTracker#setEmittanceGrowth(boolean)
     */
    public boolean getEmittanceGrowth() {
        return this.bolEmitGrowth;
    }

    @Deprecated
    public boolean getEmittanceGrowthFlag() {
        return getEmittanceGrowth();
    }
    
    /**
     * <h2>Return the emittance growth model</h2>
     * <p>
     * Get the current mechanism for simulating emittance
     * growth from RF accelerating gaps.  We are either using the 
     * that of <code>Trace3D</code> or the 
     * generalized technique described in C.K. Allen <i>et. al.</i>
     * (see references below).  This applies primarily to the 
     * longitudinal situation since the transverse cases show
     * very good agreement.
     * </p>
     * <p>
     * Since longitudinal emittance growth mechanism is
     * not well documented in the <code>Trace3D</code> manual,
     * it is somewhat unclear how the effect is being modeled.  However,
     * it does appear to create a result that tracks the
     * <code>IMPACT</code> simulation better than the CKA method (at
     * least for one case).  At this point it is unclear whether there
     * is an error in the analysis of C.K. Allen, or <code>Trace3D</code>
     * is simply more accurate.
     * </p>
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * <br>
     * &middot; This flag only has an effect when the 
     * <code>setEmittanceGrowth()</code> feature is set to <code>true</code>.
     * </p>
     * <p>
     * <strong>References</strong>
     * <br>
     * [1] M. Weiss,
     *      "Bunching of Intense Proton Beams with Six-Dimensional
     *       Matching to the Linac Acceptance", CERN/MPS/LI report 73-2,
     *       Geneva, Switzerland (1978).
     * <br>
     * [2] K.R. Crandall and D.P. Rusthoi, 
     *          "Trace 3-D Documentation", 
     *          LANL Report LA-UR-97-887 (1997), Appendix F.
     * <br>
     * [3] C.K. Allen, H. Sako, M. Ikegami, and G. Shen, "Emittance Growth
     *         Due to Phase Spread 
     *         for Proton Beams in Radio Frequency Accelerating Gaps", 
     *         (in preperation).
     * </p>
     * 
     * @return enumeration code for particular emittance growth mechanism
     */
    public EmitGrowthModel  getEmitGrowthModel() {
        return enmEmitGrowthModel;
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
     * @see xal.tools.data.IContextAware#load(String, xal.tools.data.EditContext)
     */
    @Override
    public void load(final String strPrimKeyVal, final EditContext ecTableData) throws DataFormatException {
        super.load(strPrimKeyVal, ecTableData);
        
        // Get the algorithm class name from the EditContext
        DataTable     tblAlgorithm = ecTableData.getTable( TBL_LBL_ENVBASETRACKER);
        GenericRecord recTracker = tblAlgorithm.record( Tracker.TBL_PRIM_KEY_NAME,  strPrimKeyVal );
    
        if ( recTracker == null ) {
            recTracker = tblAlgorithm.record( Tracker.TBL_PRIM_KEY_NAME, "default" );  // just use the default record
        }
    
        final boolean   bolEmitGrw  = recTracker.booleanValueForKey( ATTR_EMITGROWTH );
        final boolean   bolUseSpChg = recTracker.booleanValueForKey( ATTR_SCHEFF );
        final double    dblStepSize = recTracker.doubleValueForKey( ATTR_STEPSIZE );
    
        this.setEmittanceGrowth( bolEmitGrw );
        this.setStepSize( dblStepSize );
        this.setUseSpacecharge( bolUseSpChg );
    }

    /** 
     * Load the parameters of the algorithm from a data source exposing the
     * <code>IArchive</code> interface.
     * The superclass <code>load</code> method is called first, then the properties
     * particular to <code>EnvTrackerAdapt</code> are loaded.
     * 
     * @see xal.tools.data.IArchive#load(xal.tools.data.DataAdaptor)
     */
    @Override
    public void load(DataAdaptor daptArchive) {
        super.load(daptArchive);
        
        DataAdaptor daEnv = daptArchive.childAdaptor(LABEL_OPTIONS);
        if (daEnv != null)  {
            if (daEnv.hasAttribute(ATTR_SCHEFF)) {
                this.setUseSpacecharge( daEnv.booleanValue(ATTR_SCHEFF) );
            } else if (daEnv.hasAttribute(ATTR_USESPACECHARGE)) { // Backward compatibility
               this.setUseSpacecharge( daEnv.booleanValue(ATTR_USESPACECHARGE));
            }
            
            if (daEnv.hasAttribute(ATTR_EMITGROWTH))
                this.setEmittanceGrowth(daEnv.booleanValue(ATTR_EMITGROWTH));
            
            if (daEnv.hasAttribute(ATTR_STEPSIZE)) 
                this.setStepSize( daEnv.doubleValue(ATTR_STEPSIZE) );
        }
    }

    /**
     * Save the state and settings of this algorithm to a data source 
     * exposing the <code>DataAdaptor</code> interface.  Subclasses should
     * override this method to store the data particular to there own 
     * operation.
     * 
     * @param   daptArchive     data source to receive algorithm configuration
     * 
     * @see xal.tools.data.IArchive#save(xal.tools.data.DataAdaptor)
     */
    @Override
    public void save(DataAdaptor daptArchive) {

        //        DataAdaptor daptAlg = daptArchive.createChild(NODETAG_ALG);
        //        daptAlg.setValue(ATTRTAG_TYPE, this.getType());
        //        daptAlg.setValue(ATTRTAG_VER, this.getVersion());
        //        
        //        DataAdaptor daptTrack = daptAlg.createChild(NODETAG_TRACKER);
        //        daptTrack.setValue(ATTRTAG_DEBUG, this.getDebugMode());
        //        daptTrack.setValue(ATTRTAG_UPDATE, this.getProbeUpdatePolicy());
        //        daptTrack.setValue(ATTRTAG_RFGAP_PHASE, this.useRfGapPhaseCalculation());

        super.save(daptArchive);

        DataAdaptor daptAlg = daptArchive.childAdaptor(NODETAG_ALG);

        DataAdaptor daptOpt = daptAlg.createChild(LABEL_OPTIONS);
        daptOpt.setValue(ATTR_SCHEFF, this.getUseSpacecharge());
        daptOpt.setValue(ATTR_STEPSIZE, this.getStepSize());
        daptOpt.setValue(ATTR_EMITGROWTH, this.getEmittanceGrowth());
        daptOpt.setValue(ATTR_USESPACECHARGE, this.getUseSpacecharge());

    }

    /**
     * <h2>Add Displacement Error to Transfer Matrix</h2>
     * <p>
     * Method to add the effects of a spatially displaced to the
     * beamline element represented by the given 
     * transfer matrix.  The returned matrix is the
     * original transfer matrix conjugated by the displacement
     * matrix representing the displacement vector <b>&Delta;r</b>
     * <br>
     * <br>
     * &nbsp; <b>&Delta;r</b> &equiv; (<i>dx,dy,dz</i>).
     * <br>
     * </p>
     * <p>
     * <strong>NOTES</strong>: (H. SAKO)
     * <br>
     * &middot; added alignment error in sigma matrix
     * </p>
     * 
     * @param dx    spatial displacement in <i>x</i> plane
     * @param dy    spatial displacement in <i>y</i> plane
     * @param dz    spatial displacement in <i>z</i> plane
     * @param   matPhi      transfer matrix <b>&Phi;</b> to be processed
     * 
     * @return  transfer matrix <b>&Phi;</b> after applying displacement
     * 
     * @author  Hiroyuki Sako
     * @author  Christopher K. Allen
     * 
     * @see PhaseMatrix
     * @see PhaseMatrix#translation(PhaseVector)
     * 
     * @since Feb 20, 2009, version 2
     */
    protected PhaseMatrix modTransferMatrixForDisplError(double dx, double dy, double dz, PhaseMatrix matPhi) {
        
        if ((dx != 0)||(dy != 0)||(dz !=0)) {
            PhaseMatrix T  = PhaseMatrix.identity();
            PhaseMatrix Ti = PhaseMatrix.identity();
            
            T.setElem(IND.X,IND.HOM, -dx);
            T.setElem(IND.Y,IND.HOM, -dy);
            T.setElem(IND.Z,IND.HOM, -dz);
            
            Ti.setElem(IND.X,IND.HOM, dx);
            Ti.setElem(IND.Y,IND.HOM, dy);
            Ti.setElem(IND.Z,IND.HOM, dz);
            
            PhaseMatrix matPhiDspl = Ti.times(matPhi).times(T);
            
            return matPhiDspl;
            
        } 

        return matPhi;

    }

    /**
     * <p>
     * Method to modify the transfer matrix when we are simulating emittance
     * growth.  Currently, the method only considers the  case of propagation 
     * through an <code>IdealRfGap</code> element.  If the <code>IElement</code>
     * argument is any other type of element, nothing is done.
     * </p>
     * <p>
     * The argument <code>matPhi</code> is the original transfer matrix for
     * (normal) propagation through the <code>elem</code> element.
     * </p>  
     * 
     * <p>
     * <strong>NOTES</strong>: (H. SAKO)
     *  <br>
     *  &middot; Increase emittance using same (nonlinear) procedure on the second
     *  moments as in Trace3D. 
     * <br>
     * <br>
     * (C.K. Allen)
     * <br>
     * &middot; The &lt;x'|x&gt; transfer matrix element is modified by 
     * the formula
     * <br>
     * <br>
     * &nbsp;  &lt;x'|x&gt; = &lt;x'|x&gt;<i>F<sub>t</sub></i>(&Delta;<i>&phi;</i>)
     * <br>
     * <br>
     * where &Delta;<i>&phi;</i> is the longitudial phase spread and 
     * <i>F<sub>t</sub></i>(&Delta;<i>&phi;</i>)
     * is an approximation to &lt;sin(<i>&phi;</i>)&gt;. 
     * <br>
     * <br>
     * &middot; Originally, the &lt;z'|z&gt; transfer matrix element is modified 
     * by the formula
     * <br>
     * <br> &nbsp;   &lt;z'|z&gt; = &lt;z'|z&gt;(1 - &Delta;<i>&phi;</i><sup>2</sup>/12)
     * <br>
     * <br>
     * This approximation is given in the Trace3D manualThis formula is accurate only 
     * for <i>d&phi;</i> &lt;&lt;.  Even then, the results
     * are questionable. For a more in depth treatment of longitudinal emittance 
     * growth see the reference below.
     * <br>
     * &middot; The two-term expansion for 
     * <i>F<sub>z</sub></i>(&Delta;<i>&phi;</i>) 
     * (see {@link #compLongFourierTransform(double)})
     * is given as
     * <br>
     * <br>
     * &nbsp; <i>F<sub>z</sub></i>(&Delta;<i>&phi;</i>) &asymp;
     *        1 - 3&Delta;<i>&phi;</i><sup>2</sup>/14
     * <br>
     * <br>
     * which does not correspond to the Trace3D manual.  So I do not know where they
     * got the number.
     * </p>
     * <p>
     * <strong>Reference</strong>
     * <br>
     * C.K. Allen, Hiroyuki Sako, et. al., 
     * "Emittance Growth Due to Phase Spread for Proton Beams 
     * in a Radio Frequency Accelerating Gap"
     * (in preparation). 
     * </p>    
     * @param   dphi    effective phase spread &Delta;<i>&phi;</i>
     * @param   matPhi  transfer matrix <b>&Phi;</b> for conserved normalized emittance
     * 
     * @return  Transfer matrix &Phi; after modifying focusing term
     * 
     * @throws  ModelException  unsupport/unknown emittance growth model
     *
     * @see #compTransFourierTransform(double)
     * @see #compLongFourierTransform(double)
     * @see EnvelopeTrackerBase#effPhaseSpread(EnvelopeProbe, IdealRfGap)
     * @see EnvelopeTrackerBase#momentSine(double)
     * 
     * @author  Hiroyuki Sako
     * @author  Christopher K. Allen
     */
    protected PhaseMatrix modTransferMatrixForEmitGrowth(double dphi, PhaseMatrix   matPhi) 
        throws ModelException
    {
        
        if (!this.getEmittanceGrowth())
            return matPhi;

        // Compute auxiliary parameters
        double  Ft;     // transverse plane Fourier transform
        double  Fz;     // longitudinal plane Fourier transform

        Ft = this.compTransFourierTransform(dphi);
        Fz = this.compLongFourierTransform(dphi);
        
//        if (this.getEmitGrowthModel() == EmitGrowthModel.TRACE3D)   {
//
//            Ft      = this.momentSine(dphi);
//            Fz = 1.0 - dphi*dphi/12.0;  
//            
//        } else if (this.getEmitGrowthModel() == EmitGrowthModel.UNIFORM3D) {
//            
//            Ft = this.compTransFourierTransform(dphi);
//            Fz = this.compLongFourierTransform(dphi);
//            
//        } else {
//
//            String  strMsg = "";
//            strMsg += "EnvelopeTrackerBase#modTransferMatrixForEmitGrowth():";
//            strMsg += " Serious Error in conditional statement";
//            System.err.println(strMsg);
//            throw new ModelException(strMsg);
//        }

        // Modify the transfer matrix
        double  fl;     // thin-lens focal-length element of tranfer matrix

        fl = matPhi.getElem(IND.Xp, IND.X);
        matPhi.setElem(IND.Xp, IND.X, fl*Ft);

        fl = matPhi.getElem(IND.Yp, IND.Y);
        matPhi.setElem(IND.Yp, IND.Y, fl*Ft);

        fl = matPhi.getElem(IND.Zp, IND.Z);
        matPhi.setElem(IND.Zp, IND.Z, fl*Fz);
        //            matPhi.setElem(PhaseIndexHom.Zp, PhaseIndexHom.Z, fl*(1.0 - dphi_2/12.0));
        //            matPhi.setElem(PhaseIndexHom.Zp, PhaseIndexHom.Z, k*(1.0 + dp_2/12.0));

        return matPhi;
    }

    /**
     * <p>Method to compute the space charge transfer matrix for the given
     * length, probe and modeling element.
     * </p>
     * 
     * <p>The correlation matrix of the probe is used to determine the space charge 
     * transfer matrix.  The transfer matrix of the elem is computed for half
     * the distance provided, the correlation matrix is advanced this half step,
     * and the space charge matrix is computed there.  Thus, the space charge
     * matrix is always computed at the center of the distance <code>dblLen</code>.
     * </p>
     * 
     * NOTE:
     * <p>This half-step business maintains consistency with the Trace3D algorithm.
     * Once inside the element you are essentially doing leap-frog integration, 
     * which may or may not be more accurate then integration at full steps. The 
     * final algorithms are both second-order accurate, simply by the way the
     * final transfer matrix is computed.
     * </p>
     * 
     * @param   dblLen  incremental path length over which space charge is applied 
     * @param   probe   <code>EnvelopeProbe</code> containing correlation matrix
     * @param   elem    <code>IElement</code> where probe is currently located
     * 
     * @return  space-charge transfer matrix for incremental distance
     *  
     * @throws ModelException   could not compute the transfer map for given element 
     * 
     * @see xal.tools.beam.em.BeamEllipsoid
     */
    protected PhaseMatrix compScheffMatrix(double dblLen, EnvelopeProbe probe, IElement elem)
            throws ModelException {

        
        // Get probe parameters
        double              gamma  = probe.getGamma();
        double              K      = probe.beamPerveance();
        CovarianceMatrix   tau0   = probe.getCovariance();
        
        
        // Compute the space charge matrix 
        //      Compute the correlations in configuration space
        double covXX = tau0.computeCentralCovXX();
        double covYY = tau0.computeCentralCovYY();
        double covZZ = tau0.computeCentralCovZZ();
        
        double covXY = tau0.computeCentralCovXY();
        double covXZ = tau0.computeCentralCovXZ();
        double covYZ = tau0.computeCentralCovYZ();
        
        double corr = (covXY*covXY)/(covXX*covYY) 
                    + (covXZ*covXZ)/(covXX*covZZ) 
                    + (covYZ*covYZ)/(covYY*covZZ);

        
        // Compute space charge matrix
        PhaseMatrix     matPhiSc;       // space charge transfer matrix for dblLen
        
        if (corr < EnvelopeTrackerBase.TOLER_CORRELATION) { // beam is upright
            double g_2 = gamma*gamma;

            // Compute elliptic integrals
            double RDx = EllipticIntegral.RD(covYY, g_2*covZZ, covXX)/EnvelopeTrackerBase.CONST_UNIFORM_BEAM;
            double RDy = EllipticIntegral.RD(g_2*covZZ, covXX, covYY)/EnvelopeTrackerBase.CONST_UNIFORM_BEAM;
            double RDz = EllipticIntegral.RD(covXX, covYY, g_2*covZZ)/EnvelopeTrackerBase.CONST_UNIFORM_BEAM;
          
            // Compute defocusing constants in the laboratory frame
            double kx = gamma*dblLen*K*RDx;
            double ky = gamma*dblLen*K*RDy;
            double kz = gamma*dblLen*K*RDz;
            
            // Build the space charge transfer matrix in beam coordinates
            matPhiSc = PhaseMatrix.identity();
            
            matPhiSc.setElem(IND.Xp, IND.X, kx);
            matPhiSc.setElem(IND.Yp, IND.Y, ky);
            matPhiSc.setElem(IND.Zp, IND.Z, kz);
            
            // Transform to laboratory coordinates
            PhaseVector z  = tau0.getMean();
            PhaseMatrix T  = PhaseMatrix.translation(z.negate());
            PhaseMatrix Ti = PhaseMatrix.translation(z);
            
            matPhiSc = Ti.times( matPhiSc.times(T) );
            
        } else {    // Beam is tilted in configuration space
            
            // Compute the space charge matrix in the beam frame and transform back 
            BeamEllipsoid   ellipsoid = new BeamEllipsoid(gamma, tau0);
            matPhiSc                  = ellipsoid.computeScheffMatrix(dblLen, K);
        }
        

        // Return the space charge matrix
        return matPhiSc;

        /*
         * Previous Implementation moving &tau;<sub>0<sup> a have step forward.
         */
//                    // Get probe parameters
//                    double              gamma  = probe.getGamma();
//                    double              K      = probe.beamPerveance();
//                    CovarianceMatrix   tau0   = probe.getCorrelation();
//                    
//                    
//                    // Get element transfer matrix for a half step
//                    PhaseMap    mapPhi = elem.transferMap(probe, dblLen/2.0);
//                    PhaseMatrix matPhi = mapPhi.getFirstOrder();
//                    
//                    
//                    // Move the covariance matrix forward a half step
//                    PhaseMatrix         matTau1   = tau0.conjugateTrans(matPhi);
//                    CovarianceMatrix   tau1      = new CovarianceMatrix(matTau1);
//            
//                    
//                    // Compute the space charge matrix at center of step dblLen
//                    //      Compute the correlations in configuration space
//                    double sigXX = tau1.computeCovXX();
//                    double sigYY = tau1.computeCovYY();
//                    double sigZZ = tau1.computeCovZZ();
//                    
//                    double sigXY = tau1.computeCovXY();
//                    double sigXZ = tau1.computeCovXZ();
//                    double sigYZ = tau1.computeCovYZ();
//                    
//                    double corr = (sigXY*sigXY)/(sigXX*sigYY) 
//                                + (sigXZ*sigXZ)/(sigXX*sigZZ) 
//                                + (sigYZ*sigYZ)/(sigYY*sigZZ);
//            
//                    
//                    // Compute space charge matrix
//                    PhaseMatrix     matPhiSc;       // space charge transfer matrix for dblLen
//                    
//            //        if (false) { // beam is upright
//                    if (corr < EnvelopeTrackerBase.TOLER_CORRELATION) { // beam is upright
//                        double g_2 = gamma*gamma;
//            
//                        // Compute elliptic integrals
//                        double RDx = EllipticIntegral.RD(sigYY, g_2*sigZZ, sigXX)/EnvelopeTrackerBase.CONST_UNIFORM_BEAM;
//                        double RDy = EllipticIntegral.RD(g_2*sigZZ, sigXX, sigYY)/EnvelopeTrackerBase.CONST_UNIFORM_BEAM;
//                        double RDz = EllipticIntegral.RD(sigXX, sigYY, g_2*sigZZ)/EnvelopeTrackerBase.CONST_UNIFORM_BEAM;
//                      
//                        // Compute defocusing constants in the laboratory frame
//                        double kx = gamma*dblLen*K*RDx;
//                        double ky = gamma*dblLen*K*RDy;
//                        double kz = gamma*dblLen*K*RDz;
//                        
//                        // Build the space charge transfer matrix in beam coordinates
//                        matPhiSc = PhaseMatrix.identity();
//                        
//                        matPhiSc.setElem(PhaseIndexHom.Xp, PhaseIndexHom.X, kx);
//                        matPhiSc.setElem(PhaseIndexHom.Yp, PhaseIndexHom.Y, ky);
//                        matPhiSc.setElem(PhaseIndexHom.Zp, PhaseIndexHom.Z, kz);
//                        
//                        // Transform to laboratory coordinates
//                        PhaseVector z  = tau1.getMean();
//                        PhaseMatrix T  = PhaseMatrix.translation(z.negate());
//                        PhaseMatrix Ti = PhaseMatrix.translation(z);
//                        
//                        matPhiSc = Ti.times( matPhiSc.times(T) );
//                        
//                    } else {    // Beam is tilted in configuration space
//                        
//                        // Compute the space charge matrix in the beam frame and transform back 
//                        BeamEllipsoid   ellipsoid = new BeamEllipsoid(gamma, tau1);
//                        matPhiSc                  = ellipsoid.computeScheffMatrix(dblLen, K);
//                    }
//                    
//            
//                    // Return the space charge matrix
//                    return matPhiSc;
                
    }

    /**
     * <h2>Emittance Growth Function for Phase Spread in RF Gap</h2>
     * <p>
     * Calculation of the emittance growth function describing the
     * emittance growth due to finite phase spread in
     * an RF gap.  (Note that the growth function differs
     * for each density distribution and for each phase plane.)  
     * The particular phase plane
     * is identified by the argument <code>plane</code>, which is 
     * currently either transverse or longitudinal.  The density
     * distribution is specified by the <em>emittance growth model</em>
     * (see {@link #setEmitGrowthModel(EmitGrowthModel)}).  
     * We currently assume the beam bunch to be
     * axially symmetric.  We denote the growth function
     * in the transverse plane as 
     * <i>G<sub>t</sub></i>(<i>&phi;<sub>s</sub></i>,&Delta;<i>&phi;</i>)
     * and the growth function in the longitudinal plane as
     * <i>G<sub>z</sub></i>(<i>&phi;<sub>s</sub></i>,&Delta;<i>&phi;</i>).
     * </p>
     * <p>
     * The emittance growth calculation was originally described by
     * M. Weiss for then implemented in Trace3d.  C.K. Allen, 
     * <i>et. al.</i> generalized the results for arbitrary
     * distributions and the longitudinal case (see references
     * below).  
     * </p>
     * <p> 
     * The emittance growth function 
     * <i>G<sub>t</sub></i>(<i>&phi;<sub>s</sub></i>,&Delta;<i>&phi;</i>)
     * for the transverse plane is defined as
     * <br>
     * <br>
     * &nbsp; <i>G<sub>t</sub></i>(<i>&phi;<sub>s</sub></i>,&Delta;<i>&phi;</i>)
     * &equiv; <i>S<sub>t</sub></i>(&Delta;<i>&phi;</i>) -
     *         sin<sup>2</sup> <i>&phi;<sub>s</sub></i>
     *         <i>T<sub>t</sub></i>(&Delta;<i>&phi;</i>)
     * <br>
     * <br>
     * where <i>&phi;<sub>s</sub></i> is the synchronous particle phase,
     * &Delta;<i>&phi;</i> is the <em>effective</em> phase spread,
     * and functions <i>S<sub>t</sub></i>(&Delta;<i>&phi;</i>) and
     * <i>T<sub>t</sub></i>(&Delta;<i>&phi;</i>) are given by
     * <br>
     * <br>
     * &nbsp; <i>S<sub>t</sub></i>(&Delta;<i>&phi;</i>) 
     *        &equiv; &frac12;[1 - <i>F<sub>t</sub></i>(2&Delta;<i>&phi;</i>)]
     * <br>
     * <br>
     * and
     * <br>
     * <br>
     * &nbsp; <i>T<sub>t</sub></i>(&Delta;<i>&phi;</i>) 
     *        &equiv;  
     *        <i>F<sub>t</sub></i><sup>2</sup>(&Delta;<i>&phi;</i>)
     *        - <i>F<sub>t</sub></i>(2&Delta;<i>&phi;</i>)
     * <br>
     * <br>
     * respectively.  There are analogous formulas for the longitudinal
     * emittance growth function 
     * <i>G<sub>z</sub></i>(<i>&phi;<sub>s</sub></i>,&Delta;<i>&phi;</i>)
     * where the transverse Fourier transform 
     * <i>F<sub>t</sub></i>
     * is replaced by the longitudinal Fourier transform
     * <i>F<sub>z</sub></i>.  (See the methods
     * <code>{@link #compTransFourierTransform(double)}</code> and
     * <code>{@link #compLongFourierTransform(double)}</code>.)
     * </p>
     * <p>
     * The before gap and after gap transverse RMS emittances, 
     * <i>&epsilon;<sub>t,i</sub></i> and 
     * <i>&epsilon;<sub>t,f</sub></i>, respectively, 
     * are related by the following formula:
     * <br>
     * <br>
     * &nbsp; <i>&epsilon;<sub>t,f</sub></i><sup>2</sup> = 
     *        <i>&eta;</i><sup>2</sup><i>&epsilon;<sub>t,i</sub></i><sup>2</sup> +
     *        &Delta;<i>&epsilon;<sub>t,f</sub></i><sup>2</sup>
     * <br>
     * <br>
     * where <i>&eta;</i> is the momentum compaction due to acceleration
     * <br>
     * <br>
     *  <i>&eta;</i> &equiv; 
     *    <i>&beta;<sub>i</sub>&gamma;<sub>i</sub></i>/<i>&beta;<sub>f</sub>&gamma;<sub>f</sub></i>
     * <br>
     * <br>
     * and &Delta;<i>&epsilon;<sub>t,f</sub></i> is the emittance increase term 
     * <br>
     * <br>
     * &nbsp;  &Delta;<i>&epsilon;<sub>t,f</sub></i><sup>2</sup> &equiv; 
     *        &Delta;&lt;<i>x'<sub>f</sub></i><sup>2</sup>&gt;
     *        &lt;<i>x<sub>f</sub></i><sup>2</sup>&gt;<sup>2</sup>.
     * <br>
     * <br>
     * where
     * <br>
     * <br>
     * &nbsp;  &Delta;&lt;<i>x'<sub>f</sub></i><sup>2</sup>&gt; &equiv; 
     *        <i>k<sub>t</sub></i><sup>2</sup>
     *        <i>G<sub>t</sub></i>(<i>&phi;<sub>s</sub></i>,&Delta;<i>&phi;</i>)
     *        &lt;<i>x<sub>i</sub></i><sup>2</sup>&gt;.
     * <br>
     * <br>
     * and where <i>x'<sub>f</sub></i> and <i>x<sub>i</sub></i> represent the 
     * after-gap divergence angle and before-gap position for <em>either</em>
     * transverse phase plane, respectively.
     * Once again there are analogous formulas for the before and after gap
     * longitudinal plane emittances 
     * <i>&epsilon;<sub>z,i</sub></i> and 
     * <i>&epsilon;<sub>z,f</sub></i>, respectively, with
     * <i>G<sub>t</sub></i>(<i>&phi;<sub>s</sub></i>,&Delta;<i>&phi;</i>)
     * replaced by 
     * <i>G<sub>z</sub></i>(<i>&phi;<sub>s</sub></i>,&Delta;<i>&phi;</i>) 
     * and <i>x</i><sub>(<i>f,i</i>)</sub> replaced by 
     * <i>z</i><sub>(<i>f,i</i>)</sub>.  
     * </p> 
     * <p>
     * <strong>NOTES</strong>: CKA
     * <br>
     * &middot; Since we are modeling the RF gap as a thin lens, only the 
     * momentum (divergance angle) is modified, &lt;<i>x</i><sup>2</sup>&gt;,
     * &lt;<i>y</i><sup>2</sup>&gt;, and &lt;<i>z</i><sup>2</sup>&gt; remain
     * unaffected.  Thus, &lt;<i>x<sub>f</sub></i><sup>2</sup>&gt;
     * = &lt;<i>x<sub>i</sub></i><sup>2</sup>&gt; and
     * &lt;<i>z<sub>f</sub></i><sup>2</sup>&gt;
     * = &lt;<i>z<sub>i</sub></i><sup>2</sup>&gt; and may be computed
     * as such in the above.
     * <p>
     * <strong>References</strong>
     * <br>
     * [1] M. Weiss,
     *      "Bunching of Intense Proton Beams with Six-Dimensional
     *       Matching to the Linac Acceptance", CERN/MPS/LI report 73-2,
     *       Geneva, Switzerland (1978).
     * <br>
     * [2] K.R. Crandall and D.P. Rusthoi, 
     *          "Trace 3-D Documentation", 
     *          LANL Report LA-UR-97-887 (1997), Appendix F.
     * <br>
     * [3] C.K. Allen, H. Sako, M. Ikegami, and G. Shen, "Emittance Growth
     *         Due to Phase Spread 
     *         for Proton Beams in Radio Frequency Accelerating Gaps", 
     *         (in preperation).
     * </p>
     * 
     *
     * @param   plane   Compute the emittance growth function for this phase plane 
     * @param   phi_s   the synchronous particle phase <i>&phi;<sub>s</sub></i> 
     *                  in <i>radians</i>
     * @param   dphi    effective phase spread &Delta;<i>&phi;</i> (half-width) 
     *                  of equivalent uniform beam in <i>radians</i>
     * 
     * @return The value of the emittance growth function
     *         <i>G<sub>t</sub></i>(<i>&phi;<sub>s</sub></i>,&Delta;<i>&phi;</i>) or
     *         <i>G<sub>z</sub></i>(<i>&phi;<sub>s</sub></i>,&Delta;<i>&phi;</i>)
     * 
     * @throws ModelException   unknown/unsupport emittance growth model,
     *                          or unknown/unsupported phase plane
     *                          
     * @author Christopher K. Allen
     * @since Feb 19, 2009
     * 
     * @see EnvelopeTrackerBase#compTransFourierTransform(double)
     * @see EnvelopeTrackerBase#compLongFourierTransform(double)
     * @see EnvelopeTrackerBase#effPhaseSpread(EnvelopeProbe, IdealRfGap)
     */
    protected double compEmitGrowthFunction(PhasePlane plane, double phi_s, double dphi) 
        throws ModelException
    {
        
        // Compute the Fourier transforms
        double      F;          // 3D Fourier transform
        double      FdblAng;    // double angle Fourier transform
        if (plane == PhasePlane.TRANSVERSE) {

            F       = this.compTransFourierTransform(dphi);
            FdblAng = this.compTransFourierTransform(2.0*dphi);
        
        } else if (plane == PhasePlane.LONGITUDINAL) {

            F       = this.compLongFourierTransform(dphi);
            FdblAng = this.compLongFourierTransform(2.0*dphi);

        }  else {
        
            String  strMsg = "";
            strMsg += "EnvelopeTrackerBase#comp3dEmitGrowthFuncUnifDistr():";
            strMsg += " Serious Error in conditional statement";
            System.err.println(strMsg);
            throw new ModelException(strMsg);
        }


        // Compute the auxiliary functions
        double  sin_s = Math.sin(phi_s);
        double  sin_2 = sin_s*sin_s;
        double  F_2   = F*F;
        double  S     = 0.5*(1. - FdblAng);
        double  T     = F_2 - FdblAng;
        
        // Compute the growth function and return it.
        double  G   = S - sin_2*T;
        
        return G;
    }

    /**
     * <h2>Transverse Emittance Growth Fourier Transform</h2>
     * <p>
     * Java method for evaluating the Fourier-Bessel transform
     * needed to compute transverse emittance growth due to finite
     * longitudinal phase spread.  The technique 
     * for computing emittance growth
     * due to phase spread is described in C.K. Allen, <i>et. al.</i>, 
     * "Emittance Growth Due to Phase Spread 
     * for Proton Beams in Radio Frequency Accelerating Gaps."  This work is
     * a generalization of that covered in the Trace3D users' manual, 
     * Appendix G, which is in turn based upon the work of 
     * M. Weiss (see references below). 
     * </p>
     * <p>
     * When considering only one (tranverse) phase plane beams the 
     * transform 
     * <i>F<sub>t</sub></i>(&Delta;<i>&phi;</i>)
     * evaluated here is given as follows:
     * <br>
     * <br>
     * &nbsp; <i>F<sub>t</sub></i>(&Delta;<i>&phi;</i>) &equiv; 
     *        (2/<i>f</i><sub>1</sub>)
     *          &int;<i>J</i><sub>0</sub>(&Delta;<i>&phi;s</i>) 
     *          <i>f</i>(<i>s</i><sup>2</sup>)<i>s</i><sup></sup> <i>ds</i>,
     * <br>
     * <br>
     * where <i>f</i> is the density distribution, 
     * <i>J<sub>n</sub></i>(<i>s</i>) is the <i>n</i><sup>th</sup>-order
     * cylindrical Bessel function of the first kind,
     * &Delta;<i>&phi;</i> is the effective phase spread of the equivalent
     * uniform beam,
     * <i>s</i> is the transform variable.
     * and <i>f<sub>k</sub></i> is the number
     * <br>
     * <br>
     * &nbsp; <i>f<sub>k</sub></i> &equiv; 
     *                 &int; <i>f</i>(<i>s</i>)<i>s<sup>k</sup></i> <i>ds</i>.
     * <br>
     * <br>
     * Both integrals are taken from 0 to &infin;.
     * </p>
     * <p>
     * When considering three spatial dimensions 
     * the transform 
     * <i>F<sub>t</sub></i>(&Delta;<i>&phi;</i>)
     * evaluated here is given as follows:
     * <br>
     * <br>
     * &nbsp; <i>F<sub>t</sub></i>(&Delta;<i>&phi;</i>) &equiv; 
     *        (2/<i>f</i><sub>3/2</sub>)
     *          &int;[<i>j</i><sub>0</sub>(&Delta;<i>&phi;s</i>) 
     *        + <i>j</i><sub>2</sub>(&Delta;<i>&phi;s</i>)]
     *          <i>f</i>(<i>s</i><sup>2</sup>)<i>s</i><sup>4</sup> <i>ds</i>,
     * <br>
     * <br>
     * where <i>f</i> is the density distribution, 
     * <i>j<sub>n</sub></i>(<i>s</i>) is the <i>n</i><sup>th</sup>-order
     * spherical Bessel function of the first kind,
     * &Delta;<i>&phi;</i> is the effective phase spread of the equivalent
     * uniform beam, and
     * <i>s</i> is the transform variable.
     * Again, both integrals are taken from 0 to &infin;.
     * </p>
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * <br>
     * &middot; This method actually falls out of the calculation
     * for &lt;x<sup>2</sup>sin <i>&phi;</i>(<i>z</i>)&gt;. 
     * The assumption that <i>x</i> and <i>z</i> are uncorrelated 
     * yields the result
     * <br>
     * <br>
     *  &nbsp; &lt;x<sup>2</sup>sin <i>&phi;</i>(<i>z</i>)&gt; = 
     *     &lt;x<sup>2</sup>&gt; 
     *     sin <i>&phi;<sub>s</sub></i> 
     *     <i>F<sub>t</sub></i>(&Delta;<i>&phi;</i>)
     * <br>
     * <br>
     * where <i>F<sub>t</sub></i>(<i>d&phi;</i>) = &lt;sin(&Delta;<i>&phi;</i>)&gt; 
     * is this method, 
     * sin <i>&phi;<sub>s</sub></i> is the synchronous particle phase, 
     * and &Delta;<i>&phi;</i> is the <em>effective</em> phase spread of
     * the distribution.
     * <br>  
     * &middot; The value of 
     * &lt;sin<sup>2</sup> <i>&phi;</i>)(<i>z</i>)&gt; can also be
     * computed from this method.  The formula is
     * <br>
     * <br>
     * &nbsp; &lt;sin<sup>2</sup> <i>&phi;</i>(<i>z</i>)&gt;  
     *        = <i>S<sub>t</sub></i>(&Delta;<i>&phi;</i>)
     *        + sin<sup>2</sup> <i>&phi;<sub>s</sub></i> 
     *          <i>F<sub>t</sub></i>(2&Delta;<i>&phi;</i>)
     * <br>
     * <br>
     * where 
     * <br>
     * <br>
     * &nbsp; <i>S<sub>t</sub></i>(&Delta;<i>&phi;</i>) 
     *        &equiv; &frac12;[1 - <i>F<sub>t</sub></i>(2&Delta;<i>&phi;</i>)]
     * <br>
     * <br>
     * has analogy with sin<sup>2</sup> &Delta;<i>&phi;</i> 
     * </p>
     * <p>
     * <strong>References</strong>
     * <br>
     * [1] M. Weiss,
     *      "Bunching of Intense Proton Beams with Six-Dimensional
     *       Matching to the Linac Acceptance", CERN/MPS/LI report 73-2,
     *       Geneva, Switzerland (1978).
     * <br>
     * [2] K.R. Crandall and D.P. Rusthoi, 
     *          "Trace 3-D Documentation", 
     *          LANL Report LA-UR-97-887 (1997), Appendix F.
     * <br>
     * [3] C.K. Allen, H. Sako, M. Ikegami, and G. Shen, "Emittance Growth
     *         Due to Phase Spread 
     *         for Proton Beams in Radio Frequency Accelerating Gaps", 
     *         (in preperation).
     * </p>
     * 
     *
     * @param   dphi    effective phase spread &Delta;<i>&phi;</i> (half-width) 
     *                  of equivalent uniform beam in <b>radians</b>
     * 
     * @return  The value of transform <i>F<sub>t</sub></i>(&Delta;<i>&phi;</i>) 
     *          for the uniform distribution. 
     *
     * @throws  ModelException unsupported/unknown emittance growth model
     * 
     * @author Christopher K. Allen
     * @since  Feb 17, 2009
     * 
     * @see #effPhaseSpread(EnvelopeProbe, IdealRfGap)
     * @see xal.model.elem.IdealRfGap
     */
    protected double compTransFourierTransform(double dphi)   
        throws ModelException
    {
        
        EmitGrowthModel model;  // the emittance growth model we are using
        double          Ft;     // transform value for emittance growth model
        
        model = this.getEmitGrowthModel();
        if (model == EmitGrowthModel.TRACE3D) {
            
            Ft = this.fourierTransTrace3d(dphi);
            
        } else if (model == EmitGrowthModel.UNIFORM1D)  {
         
            Ft = this.fourierTrans1dUniform(dphi);
            
        } else if (model == EmitGrowthModel.GAUSSIAN1D) {
            
            Ft = this.fourierTrans1dGaussian(dphi);
            
        } else if (model == EmitGrowthModel.UNIFORM3D)  {
            
            Ft = this.fourierTrans3dUniform(dphi);
            
        } else if (model == EmitGrowthModel.GAUSSIAN3D) {
            
            Ft = this.fourierTrans3dGaussian(dphi);
            
        } else {
            
            String  strMsg = "";
            strMsg += "EnvelopeTrackerBase#compTransFourierTransform():";
            strMsg += " Serious Error in conditional statement";
            System.err.println(strMsg);
            throw new ModelException(strMsg);
            
        }
        
        return Ft;
    }
    
    /**
     * <h2>Longitudinal Fourier Transform for Uniform Distribution</h2>
     * <p>
     * Java method for evaluating the Fourier-Bessel transform
     * needed to compute longitudinal emittance growth due to finite
     * longitudinal phase spread in RF accelerating gaps.  
     * For information on this effect see
     * due to phase spread is described in C.K. Allen, <i>et. al.</i>, 
     * "Emittance Growth Due to Phase Spread 
     * for Proton Beams in Radio Frequency Accelerating Gaps."  This work is
     * a generalization of that covered in the Trace3D users' manual, 
     * Appendix G for the longitudinal direction. M. Weiss treated the
     * transverse direction (see references below). 
     * </p>
     * <p>
     * When considering only one (uncorrelated) phase plane beams the 
     * transform 
     * <i>F<sub>z</sub></i>(&Delta;<i>&phi;</i>)
     * evaluated here is given as follows:
     * <br>
     * <br>
     * &nbsp; <i>F<sub>z</sub></i>(&Delta;<i>&phi;</i>) &equiv; 
     *        (2/<i>f</i><sub>1</sub>)
     *          &int; [<i>J</i><sub>0</sub>(&Delta;<i>&phi;s</i>) -
     *                 <i>J</i><sub>2</sub>(&Delta;<i>&phi;s</i>)]
     *          <i>f</i>(<i>s</i><sup>2</sup>)<i>s</i><sup>3</sup> <i>ds</i>,
     * <br>
     * <br>
     * where <i>f</i> is the density distribution, 
     * <i>J<sub>n</sub></i>(<i>s</i>) is the <i>n</i><sup>th</sup>-order
     * cylindrical Bessel function of the first kind,
     * &Delta;<i>&phi;</i> is the effective phase spread of the equivalent
     * uniform beam,
     * <i>s</i> is the transform variable.
     * and <i>f<sub>k</sub></i> is the number
     * <br>
     * <br>
     * &nbsp; <i>f<sub>k</sub></i> &equiv; 
     *                 &int; <i>f</i>(<i>s</i>)<i>s<sup>k</sup></i> <i>ds</i>.
     * <br>
     * <br>
     * Both integrals are taken from 0 to &infin;.
     * </p>
     * <p>
     * When considering three spatial dimensions 
     * the transform 
     * <i>F<sub>z</sub></i>(&Delta;<i>&phi;</i>)
     * evaluated here is given as follows:
     * <br>
     * <br>
     * &nbsp; <i>F<sub>z</sub></i>(&Delta;<i>&phi;</i>) &equiv; 
     *        (2/<i>f</i><sub>3/2</sub>)
     *          &int;[<i>j</i><sub>0</sub>(&Delta;<i>&phi;s</i>) 
     *        - 2<i>j</i><sub>2</sub>(&Delta;<i>&phi;s</i>)]
     *          <i>f</i>(<i>s</i><sup>2</sup>)<i>s</i><sup>4</sup> <i>ds</i>,
     * <br>
     * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; =        
     *          <i>F<sub>t</sub></i>(&Delta;<i>&phi;</i>)  -
     *          (2/<i>f</i><sub>3/2</sub>)
     *          &int;<i>j</i><sub>2</sub>(&Delta;<i>&phi;s</i>)]
     *          <i>f</i>(<i>s</i><sup>2</sup>)<i>s</i><sup>4</sup> <i>ds</i>,
     * <br>
     * <br>
     * where <i>f</i> is the density distribution, 
     * <i>j<sub>n</sub></i>(<i>s</i>) is the <i>n</i><sup>th</sup>-order
     * spherical Bessel function of the first kind,
     * &Delta;<i>&phi;</i> is the effective phase spread of the equivalent
     * uniform beam,
     * and <i>s</i> is the transform variable.
     * The number <i>f<sub>k</sub></i> is as before.
     * </p>
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * <br>
     * &middot; This method falls out of the computation
     * for &lt;x<sup>2</sup>sin <i>&phi;</i>(<i>z</i>)&gt;. 
     * At least when considering the longitudinal phase plane independly, 
     * it can be shown that 
     * <br>
     * <br>
     *  &nbsp; &lt;z<sup>2</sup>sin <i>&phi;</i>(<i>z</i>)&gt; = 
     *     &lt;z<sup>2</sup>&gt; 
     *     sin <i>&phi;<sub>s</sub></i> 
     *     <i>F<sub>z</sub></i>(&Delta;<i>&phi;</i>)
     * <br>
     * <br>
     * where <i>F<sub>z</sub></i>(<i>d&phi;</i>) = &lt;sin(&Delta;<i>&phi;</i>)&gt; 
     * is this method, 
     * sin <i>&phi;<sub>s</sub></i> is the synchronous particle phase, 
     * and &Delta;<i>&phi;</i> is the <em>effective</em> phase spread of
     * the distribution.  
     * <br>
     * &middot; The technique for computing longitudinal emittance growth
     * is not covered in the Trace3D manual.  A two-term power series
     * expansion for this function is simply stated, 
     * but no development is presented.  
     * <br>
     * &middot; The result returned by this method has a different
     * power series expansion about &Delta;<i>&phi;</i> = 0 than 
     * that presented in the Trace3D manual.
     * </p>
     * <p>
     * <strong>References</strong>
     * <br>
     * [1] M. Weiss,
     *      "Bunching of Intense Proton Beams with Six-Dimensional
     *       Matching to the Linac Acceptance", CERN/MPS/LI report 73-2,
     *       Geneva, Switzerland (1978).
     * <br>
     * [2] K.R. Crandall and D.P. Rusthoi, 
     *          "Trace 3-D Documentation", 
     *          LANL Report LA-UR-97-887 (1997), Appendix F.
     * <br>
     * [3] C.K. Allen, H. Sako, M. Ikegami, and G. Shen, "Emittance Growth
     *         Due to Phase Spread 
     *         for Proton Beams in Radio Frequency Accelerating Gaps", 
     *         (in preperation).
     * </p>
     * 
     *
     * @param   dphi    effective phase spread &Delta;<i>&phi;</i> (half-width) 
     *                  of equivalent uniform beam in <b>radians</b>
     * 
     * @return  The value of transform <i>F<sub>z</sub></i>(&Delta;<i>&phi;</i>) 
     *          for the uniform distribution. 
     *
     * @throws  ModelException unsupported/unknown emittance growth model
     * 
     * @author Christopher K. Allen
     * @since  Feb 17, 2009
     * 
     * @see #effPhaseSpread(EnvelopeProbe, IdealRfGap)
     * @see #compTransFourierTransform(double)
     * @see #compEmitGrowthFunction(PhasePlane, double, double)
     * @see xal.model.elem.IdealRfGap
     */
    protected double compLongFourierTransform(double dphi) 
        throws ModelException
    {

        EmitGrowthModel model;  // the emittance growth model we are using
        double          Fz;     // transform value for emittance growth model
        
        model = this.getEmitGrowthModel();
        if (model == EmitGrowthModel.TRACE3D) {
            
            Fz = this.fourierLongTrace3d(dphi);
            
        } else if (model == EmitGrowthModel.UNIFORM1D)  {
         
            Fz = this.fourierLong1dUniform(dphi);
            
        } else if (model == EmitGrowthModel.GAUSSIAN1D) {
            
            Fz = this.fourierLong1dGaussian(dphi);
            
        } else if (model == EmitGrowthModel.UNIFORM3D)  {
            
            Fz = this.fourierLong3dUniform(dphi);
            
        } else if (model == EmitGrowthModel.GAUSSIAN3D) {
            
            Fz = this.fourierLong3dGaussian(dphi);
            
        } else {
            
            String  strMsg = "";
            strMsg += "EnvelopeTrackerBase#compLongFourierTransform():";
            strMsg += " Serious Error in conditional statement";
            System.err.println(strMsg);
            throw new ModelException(strMsg);
            
        }
        
        return Fz;
    }

    
    
    
    //
    //  Fourier Transforms
    //
    
    
    //
    //  Trace3D Transforms
    //
    
    /**
     * <h2>Transverse Fourier Transform given by Trace3D </h2>
     * <p>
     * This method returns the longitudinal Fourier-Bessel transform needed
     * to compute emittance growth from finite phase spread in an
     * RF accelerating gap.
     * Here we return the exact same value as Trace3D.  It considers
     * a uniform beam in three spatial dimensions.  
     * </p>
     * <p>
     * The method computes the kluge of &lt;sin(&phi;)&gt;.  In this calculation
     * we assume that the transverse and longitudinal phase planes are 
     * uncorrelated and that the beam distribution is a uniform ellipsoid
     * (that's why the sinc() function pops up).  Pretty restrictive - Sacherer's 
     * theorem does not apply here.
     * </p>
     * <p>
     * This quantity is used when computing the transverse emittance increase
     * in an RF gap due to a finite phase spread in the beam.
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * <br>
     * &middot; This method is used to approximate &lt;x<sup>2</sup>sin(&phi;)&gt;, 
     * which is at least third order in the phase coordinates.
     * <br>
     * 
     * &middot; The assumption that <i>x</i> and <i>z</i> are not correlated 
     * yields the result
     * <br>
     * <br>
     *  &nbsp; &lt;x<sup>2</sup>sin(&phi;)&gt; = &lt;x<sup>2</sup>&gt; <i>f</i>(<i>d&phi;</i>)
     * <br>
     * <br>
     * where <i>f</i>(<i>d&phi;</i>) &equiv; &lt;sin(<i>d&phi;</i>)is this method, 
     * and <i>d&phi;</i> is the "<em>phase spread</em>" of
     * the distribution.  The phase spread is defined
     * <br>
     * <br>
     * &nbsp;   <i>d&phi;</i> = &lt;(<i>&phi; - &phi;<sub>s</sub></i>)<sup>2</sup>&gt;<sup>1/2</sup>
     * <br>
     * <br>
     * where <i>&phi;<sub>s</sub></i> is the synchronous particle phase.
     * </p>
     * <p>
     * See K.R. Crandall and D.P. Rusthoi, 
     * </p>
     *          <ul><li>
     *          "Trace 3-D Documentation", 
     *          LANL Report LA-UR-97-887 (1997), Appendix F.
     *          </li></ul>
     * 
     *
     * @param   dphi    effective phase spread &Delta;<i>&phi;</i> (half-width) 
     *                  of equivalent uniform beam in <b>radians</b>
     * 
     * 
     * @return  The value of transform <i>F<sub>t</sub></i>(&Delta;<i>&phi;</i>) 
     *          given by Trace3D which is the value of &lt;sin(<i>&phi;</i>)&gt; 
     *          = <i>f</i>(<var>dp</var>).
     * 
     * @author Christopher K. Allen
     * @since  Feb 17, 2009
     * 
     * @see #momentSine(double)
     * @see #compTransFourierTransform(double)
     * @see #effPhaseSpread(EnvelopeProbe, IdealRfGap)
     * @see xal.model.elem.IdealRfGap
     */
    protected double fourierTransTrace3d(double dphi)   {
            
      double dp_2 = dphi*dphi;
      
          if (dphi < 0.1) { 
              // Avoid singularity at zero - Taylor expansion
              return 1.0 - dp_2/14.0 + dp_2*dp_2/504.0;
      
          }
          
          // Use full expression
          double T    = 3.0/dp_2;

          double sinc = ElementaryFunction.sinc(dphi);
          double cos  = Math.cos(dphi);

          return (5.0*T)*(sinc*(T-1.0) - cos*T);
    }

    /**
     * <h2>Longitudinal Fourier Transform given by Trace3D </h2>
     * <p>
     * This method returns the longitudinal Fourier-Bessel transform needed
     * to compute emittance growth from finite phase spread in an
     * RF accelerating gap.
     * Here we return the exact same value as Trace3D.  It considers
     * a uniform beam in three spatial dimensions.  
     * </p>
     * <p>The returned 
     * value (which is not derived or explained in the Trace3D
     * manual) is
     * <br>
     * <br>
     *  &nbsp; <i>F<sub>z</sub></i>(&Delta;<i>&phi;</i>) = 
     *          1 - &Delta;<i>&phi;</i><sup>2</sup>/12
     * <br>
     * <br>
     * This value is taken from the Trace3D code.  The manual actually
     * quotes it as  
     * 1 + &Delta;<i>&phi;</i><sup>2</sup>/12.
     * </p>
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * </p>
     * <p>
     * <strong>References</strong>
     * <br>
     * [1] M. Weiss,
     *      "Bunching of Intense Proton Beams with Six-Dimensional
     *       Matching to the Linac Acceptance", CERN/MPS/LI report 73-2,
     *       Geneva, Switzerland (1978).
     * <br>
     * [2] K.R. Crandall and D.P. Rusthoi, 
     *          "Trace 3-D Documentation", 
     *          LANL Report LA-UR-97-887 (1997), Appendix F.
     * <br>
     * [3] C.K. Allen, H. Sako, M. Ikegami, and G. Shen, "Emittance Growth
     *         Due to Phase Spread 
     *         for Proton Beams in Radio Frequency Accelerating Gaps", 
     *         (in preperation).
     * </p>
     * 
     * @param   dphi    effective phase spread &Delta;<i>&phi;</i> (half-width) 
     *                  of equivalent uniform beam in <b>radians</b>
     * 
     * @return  The value of transform <i>F<sub>z</sub></i>(&Delta;<i>&phi;</i>) 
     *          given by Trace3D. 
     *
     * 
     * @author Christopher K. Allen
     * @since  Feb 17, 2009
     * 
     * @see #effPhaseSpread(EnvelopeProbe, IdealRfGap)
     * @see #compTransFourierTransform(double)
     * @see xal.model.elem.IdealRfGap
     */
    protected double fourierLongTrace3d(double dphi) {
    
        double Fz = 1.0 - dphi*dphi/12.0;  
    
        return Fz;
    }

    
    
    //
    //  Three Spatial Dimension Transforms
    //
    
    /**
     * <h2>Longitudinal Fourier Transform for 3D Uniform Distribution</h2>
     * <p>
     * This method return the longitudinal Fourier-Bessel transform needed
     * to compute emittance growth from finite phase spread in an
     * RF accelerating gap.
     * Here we consider the case when the beam is uniformly distributed 
     * over three spatial dimensions.
     * For this distribution <i>f</i>(<i>s</i>), we find that 
     * <br>
     * <br>
     * &nbsp; <i>F<sub>z</sub></i>(&Delta;<i>&phi;</i>) = 
     * 15 <i>j</i><sub>2</sub>(&Delta;<i>&phi;</i>)/&Delta;<i>&phi;</i><sup>2</sup> -
     * 15 <i>j</i><sub>3</sub>(&Delta;<i>&phi;</i>)/&Delta;<i>&phi;</i> 
     * <br>
     * <br> 
     * For small arguments we avoid any numerically singular behavior 
     * at &Delta;<i>&phi;</i> = 0 by Taylor
     * expanding.  We have
     * <br>
     * <br>
     * &nbsp; <i>F<sub>z</sub></i>(&Delta;<i>&phi;</i>) &asymp; 
     *        1 - <i>x</i><sup>2</sup>/7 + <i>x</i><sup>4</sup>/168 -
     *        <i>x</i><sup>6</sup>/8316 + 5<i>x</i><sup>8</sup>/3459456 +
     *        O(<i>x</i><sup>17/2</sup>)
     * <br>
     * <br>
     * Currently we are expanding to sixth order.
     * </p>
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * @param   dphi    effective phase spread &Delta;<i>&phi;</i> (half-width) 
     *                  of equivalent uniform beam in <b>radians</b>
     * 
     * @return  The value of transform <i>F<sub>z</sub></i>(&Delta;<i>&phi;</i>) 
     *          for the 3D uniform distribution. 
     *
     * 
     * @author Christopher K. Allen
     * @since  Feb 17, 2009
     * 
     * @see #effPhaseSpread(EnvelopeProbe, IdealRfGap)
     * @see #compTransFourierTransform(double)
     * @see xal.model.elem.IdealRfGap
     */
    protected double fourierLong3dUniform(double dphi) {
    
        if (Math.abs(dphi) < EnvelopeTrackerBase.SMALL_ARG) {
    
            // Numerically unstable about dphi=0, compute expansion about dphi=0 
            double      x_2 = dphi*dphi;
            double      x_4 = x_2*x_2;
            double      x_6 = x_2*x_4;
    
            return  1.0 - x_2/7.0 + x_4/168.0 - x_6/8316.0;
        }

        // Numerically stable, compute exact expresson
        double dphi_2 = dphi*dphi;
        double j2     = BesselFunction.j2(dphi);
        double j3     = BesselFunction.j3(dphi);
        double Fz     = 15.0*(j2/dphi_2 - j3/dphi);

        return Fz;
    }

    /**
     * <h2>Transverse Fourier Transform for 3D Uniform Distribution</h2>
     * <p>
     * This method return the transverse Fourier-Bessel transform needed
     * to compute emittance growth from finite phase spread in an
     * RF accelerating gap.
     * Here we consider the case when the beam is uniformly distributed 
     * over three spatial dimensions.
     * For this distribution <i>f</i>(<i>s</i>), we find that 
     * <br>
     * <br>
     * &nbsp; <i>F<sub>t</sub></i>(&Delta;<i>&phi;</i>) = 
     * 15 <i>j</i><sub>2</sub>(&Delta;<i>&phi;</i>)/&Delta;<i>&phi;</i><sup>2</sup>
     * <br>
     * <br> 
     * For small arguments we avoid any numerically singular behavior 
     * at &Delta;<i>&phi;</i> = 0 by Taylor
     * expanding.  We have
     * <br>
     * <br>
     * &nbsp; <i>F<sub>t</sub></i>(&Delta;<i>&phi;</i>) &asymp; 
     *        1 - <i>x</i><sup>2</sup>/14 + <i>x</i><sup>4</sup>/504 -
     *        <i>x</i><sup>6</sup>/33264 + <i>x</i><sup>8</sup>/3459456 +
     *        O(<i>x</i><sup>17/2</sup>)
     * <br>
     * <br>
     * Currently we are expanding to sixth order.
     * </p>
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * </p>
     *
     * @param   dphi    effective phase spread &Delta;<i>&phi;</i> (half-width) 
     *                  of equivalent uniform beam in <b>radians</b>
     * 
     * @return  The value of transform <i>F<sub>t</sub></i>(&Delta;<i>&phi;</i>) 
     *          for the uniform distribution. 
     *
     * 
     * @author Christopher K. Allen
     * @since  Feb 17, 2009
     * 
     * @see #compTransFourierTransform(double)
     * @see #effPhaseSpread(EnvelopeProbe, IdealRfGap)
     * @see xal.model.elem.IdealRfGap
     */
    protected double fourierTrans3dUniform(double dphi)   {
        
        if (Math.abs(dphi) < EnvelopeTrackerBase.SMALL_ARG) {
            
            // Numerically unstable about dphi=0, compute expansion about dphi=0 
            double      x_2 = dphi*dphi;
            double      x_4 = x_2*x_2;
            double      x_6 = x_2*x_4;
            
            return  1.0 - x_2/14.0 + x_4/504.0 - x_6/33264.0;
    
        }  

        // Numerically stable, compute exact expression
        double dphi_2 = dphi*dphi;
        double j2     = BesselFunction.j2(dphi);
        double Ft     = 15.0*j2/dphi_2;

        return Ft;
    }

    /**
     * <h2>Transverse Fourier Transform for 3D Gaussian Distribution</h2>
     * <p>
     * This method return the transverse Fourier-Bessel transform needed
     * to compute emittance growth from finite phase spread in an
     * RF accelerating gap.
     * Here we consider the case when the beam has a Gaussian distribution 
     * over three spatial dimensions.
     * For this distribution <i>f</i>(<i>s</i>), we find that 
     * <br>
     * <br>
     * &nbsp; <i>F<sub>t</sub></i>(&Delta;<i>&phi;</i>) = 
     * <i>e</i><sup>-&Delta;<i>&phi;</i>&circ;2/10</sup>
     * <br>
     * <br> 
     * There is no need for a small argument expansion since the
     * above expression is numerically stable.
     * </p>
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * </p>
     *
     * @param   dphi    effective phase spread &Delta;<i>&phi;</i> (half-width) 
     *                  of equivalent uniform beam in <b>radians</b>
     * 
     * @return  The value of transform <i>F<sub>t</sub></i>(&Delta;<i>&phi;</i>) 
     *          for the Gaussian distribution. 
     *
     * 
     * @author Christopher K. Allen
     * @since  Feb 17, 2009
     * 
     * @see #compTransFourierTransform(double)
     * @see #effPhaseSpread(EnvelopeProbe, IdealRfGap)
     * @see xal.model.elem.IdealRfGap
     */
    protected double fourierTrans3dGaussian(double dphi)   {
            
        // Numerically stable, compute exact expression
        double dphi_2 = dphi*dphi;
        double Ft     = Math.exp(-dphi_2/10.0);
    
        return Ft;
    }

    /**
     * <h2>Longitudinal Fourier Transform for 3D Gaussian Distribution</h2>
     * <p>
     * This method return the longitudinal Fourier-Bessel transform needed
     * to compute emittance growth from finite phase spread in an
     * RF accelerating gap.
     * Here we consider the case when the beam has a Gaussian distribution
     * over the three spatial dimensions.
     * For this distribution <i>f</i>(<i>s</i>), we find that 
     * <br>
     * <br>
     * &nbsp; <i>F<sub>z</sub></i>(&Delta;<i>&phi;</i>) = 
     *   (1 - &Delta;<i>&phi;</i><sup>2</sup>/5)
     *   <i>e</i><sup>-&Delta;<i>&phi;</i>&circ;2/10</sup>
     * <br>
     * <br> 
     * There is no need for a small argument expansion since the
     * above expression is numerically stable.
     * </p>
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * @param   dphi    effective phase spread &Delta;<i>&phi;</i> (half-width) 
     *                  of equivalent uniform beam in <b>radians</b>
     * 
     * @return  The value of transform <i>F<sub>z</sub></i>(&Delta;<i>&phi;</i>) 
     *          for the 3D Gaussian distribution. 
     *
     * 
     * @author Christopher K. Allen
     * @since  Feb 17, 2009
     * 
     * @see #effPhaseSpread(EnvelopeProbe, IdealRfGap)
     * @see #compTransFourierTransform(double)
     * @see xal.model.elem.IdealRfGap
     */
    protected double fourierLong3dGaussian(double dphi) {
    
        
        // Numerically stable, compute exact expression
        double dphi_2 = dphi*dphi;
        double deriv  = 1.0 - dphi_2/5.0;
        double Ft     = Math.exp(-dphi_2/10.0);
        double Fz     = deriv*Ft;
    
        return Fz;
    }

    
    
    //
    //  Single (Uncorrelated) Phase Space Distributions
    //
    
    
    /**
     * <h2>Transverse Fourier Transform for Single Phase Plane</h2>
     * <p>
     * This method return the transverse Fourier-Bessel transform needed
     * to compute emittance growth from finite phase spread in an
     * RF accelerating gap.
     * Here we consider the case when the beam is uniformly distributed 
     * in one transverse phase plane.
     * For this distribution <i>f</i>(<i>s</i>), we find that 
     * <br>
     * <br>
     * &nbsp; <i>F<sub>t</sub></i>(&Delta;<i>&phi;</i>) = 
     *      2 <i>J</i><sub>1</sub>(&Delta;<i>&phi;</i>)/&Delta;<i>&phi;</i> 
     * <br>
     * <br> 
     * For small arguments we avoid any numerically singular behavior 
     * at &Delta;<i>&phi;</i> = 0 by Taylor
     * expanding.  We have
     * <br>
     * <br>
     * &nbsp; <i>F<sub>t</sub></i>(&Delta;<i>&phi;</i>) &asymp; 
     *        1 - <i>x</i><sup>2</sup>/8 + <i>x</i><sup>4</sup>/192 -
     *        <i>x</i><sup>6</sup>/9216 + <i>x</i><sup>8</sup>/737280 +
     *        O(<i>x</i><sup>9</sup>)
     * <br>
     * <br>
     * Currently we are expanding to sixth order.
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * </p>
     *
     * @param   dphi    effective phase spread &Delta;<i>&phi;</i> (half-width) 
     *                  of equivalent uniform beam in <b>radians</b>
     * 
     * @return  The value of transform <i>F<sub>t</sub></i>(&Delta;<i>&phi;</i>) 
     *          for the single phase plane uniform distribution. 
     *
     * 
     * @author Christopher K. Allen
     * @since Feb 25, 2009
     * 
     * @see #compTransFourierTransform(double)
     * @see #effPhaseSpread(EnvelopeProbe, IdealRfGap)
     * @see xal.model.elem.IdealRfGap
     */
    protected double fourierTrans1dUniform(double dphi)   {
        
        if (Math.abs(dphi) < EnvelopeTrackerBase.SMALL_ARG) {
            
            // Numerically unstable about dphi=0, compute expansion about dphi=0 
            double      x_2 = dphi*dphi;
            double      x_4 = x_2*x_2;
            double      x_6 = x_2*x_4;
            
            return  1.0 - x_2/8.0 + x_4/192.0 - x_6/9216.0;
    
        }   

        // Numerically stable, compute exact expression
        double J1     = BesselFunction.J1(dphi);
        double Ft     = 2.0*J1/dphi;

        return Ft;
    }


    /**
     * <h2>Longitudinal Fourier Transform for Single Phase Plane</h2>
     * <p>
     * This method return the longitudinal Fourier-Bessel transform needed
     * to compute emittance growth from finite phase spread in an
     * RF accelerating gap.
     * Here we consider the case when the beam is uniformly distributed 
     * in one transverse phase plane.
     * For this distribution <i>f</i>(<i>s</i>), we find that 
     * <br>
     * <br>
     * &nbsp; <i>F<sub>z</sub></i>(&Delta;<i>&phi;</i>) = 
     *      8 <i>J</i><sub>2</sub>(&Delta;<i>&phi;</i>)/&Delta;<i>&phi;</i><sup>2</sup> -  
     *      8 <i>J</i><sub>3</sub>(&Delta;<i>&phi;</i>)/&Delta;<i>&phi;</i> 
     * <br>
     * <br> 
     * For small arguments we avoid any numerically singular behavior 
     * at &Delta;<i>&phi;</i> = 0 by Taylor
     * expanding.  We have
     * <br>
     * <br>
     * &nbsp; <i>F<sub>z</sub></i>(&Delta;<i>&phi;</i>) &asymp; 
     *        1 - <i>x</i><sup>2</sup>/4 + 5<i>x</i><sup>4</sup>/384 -
     *        7<i>x</i><sup>6</sup>/23040 + <i>x</i><sup>8</sup>/245760 +
     *        O(<i>x</i><sup>9</sup>)
     * <br>
     * <br>
     * Currently we are expanding to sixth order.
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * </p>
     *
     * @param   dphi    effective phase spread &Delta;<i>&phi;</i> (half-width) 
     *                  of equivalent uniform beam in <b>radians</b>
     * 
     * @return  The value of transform <i>F<sub>t</sub></i>(&Delta;<i>&phi;</i>) 
     *          for the single phase plane uniform distribution. 
     *
     * 
     * @author Christopher K. Allen
     * @since Feb 25, 2009
     * 
     * @see #compTransFourierTransform(double)
     * @see #effPhaseSpread(EnvelopeProbe, IdealRfGap)
     * @see xal.model.elem.IdealRfGap
     */
    protected double fourierLong1dUniform(double dphi)   {
        
        if (Math.abs(dphi) < EnvelopeTrackerBase.SMALL_ARG) {
            
            // Numerically unstable about dphi=0, compute expansion about dphi=0 
            double      x_2 = dphi*dphi;
            double      x_4 = x_2*x_2;
            double      x_6 = x_2*x_4;
            
            return  1.0 - x_2/4.0 + 5.0*x_4/384.0 - 7.0*x_6/23040.0;
    
        } 

        // Numerically stable, compute exact expression
        double dphi_2 = dphi*dphi;
        double J2     = BesselFunction.Jn(2,dphi);
        double J3     = BesselFunction.Jn(3, dphi);
        double Fz     = 8.0*(J2/dphi_2 - J3/dphi);

        return Fz;
    }

    /**
     * <h2>Transverse Fourier Transform for 2D Gaussian Distribution</h2>
     * <p>
     * This method return the transverse Fourier-Bessel transform needed
     * to compute emittance growth from finite phase spread in an
     * RF accelerating gap.
     * Here we consider the case when the beam is Gaussian distributed in
     * each (uncorrelated) phase plane.
     * For this distribution <i>f</i>(<i>s</i>), we find that 
     * <br>
     * <br>
     * &nbsp; <i>F<sub>t</sub></i>(&Delta;<i>&phi;</i>) = 
     * <i>e</i><sup>-&Delta;<i>&phi;</i>&circ;2/8</sup>
     * <br>
     * <br> 
     * There is no need for a small argument expansion since the
     * above expression is numerically stable.
     * </p>
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * </p>
     *
     * @param   dphi    effective phase spread &Delta;<i>&phi;</i> (half-width) 
     *                  of equivalent uniform beam in <b>radians</b>
     * 
     * @return  The value of transform <i>F<sub>t</sub></i>(&Delta;<i>&phi;</i>) 
     *          for the Gaussian distribution. 
     *
     * 
     * @author Christopher K. Allen
     * @since Feb 25, 2009
     * 
     * @see #compTransFourierTransform(double)
     * @see #effPhaseSpread(EnvelopeProbe, IdealRfGap)
     * @see xal.model.elem.IdealRfGap
     */
    protected double fourierTrans1dGaussian(double dphi)   {
            
        // Numerically stable, compute exact expression
        double dphi_2 = dphi*dphi;
        double Ft     = Math.exp(-dphi_2/8.0);
    
        return Ft;
    }

    /**
     * <h2>Longitudinal Fourier Transform for 2D Gaussian Distribution</h2>
     * <p>
     * This method return the longitudinal Fourier-Bessel transform needed
     * to compute emittance growth from finite phase spread in an
     * RF accelerating gap.
     * Here we consider the case when the beam is Gaussian distributed 
     * in each (uncorrelated) phase plane.
     * For this distribution <i>f</i>(<i>s</i>), we find that 
     * <br>
     * <br>
     * &nbsp; <i>F<sub>z</sub></i>(&Delta;<i>&phi;</i>) = 
     *   (1 - &Delta;<i>&phi;</i><sup>2</sup>/4)
     *   <i>e</i><sup>-&Delta;<i>&phi;</i>&circ;2/8</sup>
     * <br>
     * <br> 
     * There is no need for a small argument expansion since the
     * above expression is numerically stable.
     * </p>
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * @param   dphi    effective phase spread &Delta;<i>&phi;</i> (half-width) 
     *                  of equivalent uniform beam in <b>radians</b>
     * 
     * @return  The value of transform <i>F<sub>z</sub></i>(&Delta;<i>&phi;</i>) 
     *          for the Gaussian distribution in single phase plane. 
     *
     * 
     * @author Christopher K. Allen
     * @since Feb 25, 2009
     * 
     * @see #effPhaseSpread(EnvelopeProbe, IdealRfGap)
     * @see #compTransFourierTransform(double)
     * @see xal.model.elem.IdealRfGap
     */
    protected double fourierLong1dGaussian(double dphi) {
        
        // Numerically stable, compute exact expression
        double dphi_2 = dphi*dphi;
        double deriv  = 1.0 - dphi_2/4.0;
        double Ft     = Math.exp(-dphi_2/8.0);
        double Fz     = deriv*Ft;
    
        return Fz;
    }

    /** 
     * <h2>Effective Phase Spread for Equivalent Uniform Beam</h2>
     * <p>
     * Compute the longitudinal phase spread of the bunch with respect to the
     * RF in an RF gap element (based on Trace3D RfGap.f)  The phase spread
     * is computed assuming a <b>uniform</b> distribution.  The returned value
     * is then the <em>effective</em> phase spread for the equivalent uniform beam.
     * (see below).
     * </p>
     * <p>
     * In XAL, longitundinal coordinate <i>z</i> is the "phase spread", but in meters. 
     * To convert to phase spread <i>d&phi;</i> in radians we have
     * <br>
     * <br>
     * &nbsp;   <i>d&phi;</i> = 2&pi;<i>z</i>/(&beta;&lambda;)
     * <br>
     * <br>     
     * where &lambda; is the wavelength of the RF.  To simplify matters make the definition
     * <br> 
     * <br>
     * &nbsp; <i>k</i> &equiv; 2&pi;/&beta;&lambda;,
     * <br>
     * <br>
     * which is the synchronous particle wave number.
     * So, for &lt;<i>d&phi;</i><sup>2</sup>&gt; we get
     * <br>
     * <br>
     * &nbsp; &lt;<i>d&phi;</i><sup>2</sup>&gt; = <i>k</i><sup>2</sup>&lt;<i>z</i><sup>2</sup>&gt;.
     * <br>
     * <br>
     * Note then that &lt;<i>d&phi;</i><sup>2</sup>&gt;<sup>1/2</sup> is the 
     * <em>RMS</em> phase spread.
     * </p>
     * <p>
     * I am using the mid-gap value for &beta;, that is, &beta; average.  And, thus,
     * <i>k</i> is also the mid-gap wave number.
     * </p>
     * <p>
     * We need to multiply &lt;z<sup>2</sup>&gt; by 5 to get the 
     * "(three-dimensional) equivalent uniform beam" longitudinal
     * semi-axis (even though there is no uniform equivalent beam for
     * emittance growth).
     * </p>
     * <p>
     * Putting this all together gives the following value for the 
     * <em>effective</em> phase phase spread for the equivalent uniform beam, 
     * &Delta;<i>&phi;</i>:
     * <br>
     * <br>
     * &nbsp; &Delta;<i>&phi;</i> = <i>k</i>&lt;5<i>z</i><sup>2</sup>&gt;<sup>1/2</sup>.
     * <br>
     * <br>
     * The above is the value returned by this method.  Note that &Delta;<i>&phi;</i>
     * is also referred to as the beam <em>half-length</em> (with respect to the
     * RF phase).
     * </p>
     * <p>
     * <strong>NOTES</strong>: (CKA)
     * <br>
     * <br>
     * &middot; Note that the RMS phase spread can also be represented as
     * <br>
     * <br>
     * &nbsp;   <i>d&phi;</i> = &lt;[<i>&phi;(s) - &phi;<sub>s</sub></i>]<sup>2</sup>&gt;<sup>1/2</sup>
     * <br>
     * <br>
     * where <i>&phi;<sub>s</sub></i> is the synchronous particle phase.
     * </p>
     *
     * @param  probe    probe containing relativistic data
     * @param  elem     the RF gap modeling element creating the bunch phase spread
     *
     * @return  phase spread (half width) &Delta;<i>&phi;</i> &equiv; 
     *          &lt;5<i>d&phi;</i><sup>2</sup>&gt;<sup>1/2</sup> 
     *          for this probe (<b>radians</b>)
     * 
     * @author Hiroyuki Sako
     * @author Christopher K. Allen
     */
    protected double effPhaseSpread(EnvelopeProbe probe, IdealRfGap elem) {
            
            // Compute the RF wavelength
            double lambda = elem.wavelengthRF();
            
            // Compute the mid-gap velocity 
//            double beta = elem.compMidGapBeta(probe);     // CKA for IdealRfGapUpgraded
            double beta = elem.betaMidGap(probe);           // CKA for IdealRfGap
    
            // Compute the mid-gap wave number
            double k    = (2.0*Math.PI)/(beta*lambda);
            
            // Compute the longitudinal phase spread 
            double z_2  = 5.0*probe.getCovariance().getElem(IND.Z, IND.Z);
            double dphi = k * Math.sqrt(z_2);
            
            return dphi;
    //        
    //        
    //        double Er = probe.getSpeciesRestEnergy();
    //        double Wi = probe.getKineticEnergy();
    //        
    //        Twiss[] twiss = probe.getTwiss().getTwiss();
    //        
    //        TraceXalUnitConverter t3dxal = TraceXalUnitConverter.newConverter(elem.getFrequency(),Er,Wi);
    //        Twiss twissLongT3d = t3dxal.xalToTraceLongitudinal(twiss[2]);
    //        
    //        double emitz = twissLongT3d.getEmittance();
    //        double betaz = twissLongT3d.getBeta();
    //        
    //        return Math.sqrt(emitz*betaz)*2*Math.PI/360.0; //radian
        }

    /** 
     * <p>
     * Compute the phase spread of the bunch for a probe (based on Trace3D RfGap.f)
     * </p>
     * <h3>CKA Notes:</h3>
     * <p>
     * - This method needs to be optimized now that I understand what it is doing.
     * In XAL, longitundinal coordinate <i>z</i> is the "phase spread", but in meters. 
     * To convert to phase spread <i>&delta;&phi;</i> in radians we have
     * <br>
     * <br>
     * &nbsp; &nbsp; &delta;&phi; = 2&pi;<i>z</i>/(&beta;&lambda;) ,
     * <br>
     * <br>
     * where &lambda; is the wavelength of the RF.  So, for &lt;&delta;&phi;<sup>2</sup>&gt;
     * we get
     * <br>
     * <br> 
     * &nbsp; &nbsp; &lt;&delta;&phi;<sup>2</sup>&gt; = &lt;<i>z</i><sup>2</sup>&gt;2&pi;<i>f</i>
     *                                                /(&beta;<i>c</i>) ,
     * <br>
     * <br>
     * where <i>f</i> is the RF frequency of the gap and c is the speed of light.
     * <br>
     * <br>
     * - For the optional computation <b>phaseSpreadT3d</b> (which apparently is not
     * used) I am not sure what is happening, or why &lt;y'y'&gt; is significant?
     * </p>
     * 
     *  @param  probe   we are computing the phase spread for this probe at the current
     *                  <code>IdealRfGap</code> condition
     * @param gap       the RF gap modeling element creating the bunch phase spread
     *
     * @return         phase spread (half width) for this probe (<b>radian</b>)
     * 
     * @author Hiroyuki Sako
     * @author Christopher K. Allen
     * @version Nov 6, 2013
     */
    protected double phaseSpread(EnvelopeProbe probe, IdealRfGap gap) {
        
        // The answer
        double dblPhaseSpreadCalc = 0.0;
        
        //sako
        double Er   = probe.getSpeciesRestEnergy();
        double Wi   = probe.getKineticEnergy();
        double Wbar = Wi + gap.energyGain(probe)/2.0;
        
        //def
        TraceXalUnitConverter t3dxal = TraceXalUnitConverter.newConverter(gap.getFrequency(),Er,Wi);
        
        Twiss [] twiss = probe.getCovariance().computeTwiss();
        Twiss t3dtwissz = t3dxal.xalToTraceLongitudinal(twiss[2]);
        
        double emitz = t3dtwissz.getEmittance();
        double betaz = t3dtwissz.getBeta();
        
        dblPhaseSpreadCalc = Math.sqrt(emitz*betaz)*2*Math.PI/360; //radian
        //betaaverage is  not there!!! is it ok?
        
        //sako for test. Try to use average energy to calculate dphiav
        boolean phaseSpreadT3d = false;
        if (phaseSpreadT3d) {
            
            double gbar = Wbar/Er + 1.0;
            double bbar = Math.sqrt(1.0 - 1.0/(gbar*gbar));
            double clight = IProbe.LightSpeed;
            double freq = gap.getFrequency();
            double wavel = clight/freq;
            
          //this need to be convert to t3d unit
            CovarianceMatrix     matCorXAL  = probe.getCovariance();
            
            double sigma55 = matCorXAL.getElem(4,4);
            double dphit3d = 2.*Math.PI*Math.sqrt(sigma55)/(bbar*wavel); 
            
            dblPhaseSpreadCalc = dphit3d;//temp
        }
        
        return dblPhaseSpreadCalc;
    }
    
    /**
     * Moved from <code>IdealRfGap</code>.
     */
    protected double correctTransFocusingPhaseSpread(EnvelopeProbe probe, IdealRfGap gap) {
        double dphi = this.phaseSpread(probe, gap);
        double cor = 1.;
        cor = 1-dphi*dphi/14;
        //      if (dphi != 0) {
        if (dphi > 0.1) {
            cor = 15/dphi/dphi*(3/dphi/dphi*(Math.sin(dphi)/dphi-Math.cos(dphi))-Math.sin(dphi)/dphi);  
        }
        //      }
        return cor;
    }
    
    /**
     * new implementation by sako, 7 Aug 06, to do trans/long simultanously
     * used in EnvTrackerAdapt, EnvelopeTracker
     *
     * @param probe     envelope probe object (something with emittance and moments)
     * @param gap       the RF gap modeling element creating the bunch phase spread
     */
    protected double [] correctSigmaPhaseSpread(EnvelopeProbe probe, IdealRfGap gap) {
        
        double dfac[] = new double[2];
        
        double dfacT = 0d;
        double dfacL = 0d;
        
//        dfac[0] = 0d;
//        dfac[1] = 0d;
//        
//        //transverse
//        if (!(probe instanceof EnvelopeProbe)) {
//            return dfac;
//        }
//        
        double phi = gap.getPhase();
        double dphi = this.phaseSpread(probe, gap);
        
        double tdp = 2*dphi;
        double sintdp = Math.sin(tdp);
        
        
        double f2t = 1-tdp*tdp/14;
        if (tdp>0.1) {
            f2t = 3*(sintdp/tdp-Math.cos(tdp))/tdp/tdp; //APPENDIX F (Trace3D manual)
            f2t = 15*(f2t-sintdp/tdp)/tdp/tdp;
        }
        double sinphi = Math.sin(phi);
        double cosphi = Math.cos(phi);
        double G1 = 0.5*(1+(sinphi*sinphi-cosphi*cosphi)*f2t);
        double Q = probe.getSpeciesCharge();
        double h = 1;//harmic number
        double m = probe.getSpeciesRestEnergy();
        double w = probe.getKineticEnergy();
        double dw = gap.energyGain(probe);
        double wa = w+dw/2;
        double betagammaa = Math.sqrt(wa/m*(2+wa/m));
        
        double wf = w+dw;
        double betagammaf =Math.sqrt(wf/m*(2+wf/m));
        
        double clight = IProbe.LightSpeed;
        double freq = gap.getFrequency();
        double lambda = clight/freq;
        
        double cay = h*Math.abs(Q)*Math.PI*gap.getETL()/(m*betagammaa*betagammaa*betagammaf*lambda); //Kx'
        double f1 =  1-dphi*dphi/14;
        if (dphi > 0.1) {
            f1 = 15/dphi/dphi*(3/dphi/dphi*(Math.sin(dphi)/dphi-Math.cos(dphi))-Math.sin(dphi)/dphi);   
        }
        dfacT = cay*cay*(G1-sinphi*sinphi*f1*f1);
        
        
        //longitudinal
        double f2l = 1-tdp*tdp/14;
        if (tdp>0.1) {
            f2l = 3*(sintdp/tdp-Math.cos(tdp))/tdp/tdp;
            f2l = 15*(f2l-sintdp/tdp)/tdp/tdp;
        }
        
        //def   double cayz = 2*cay*gammaa*gammaa;
        double cayz = 2*cay; //this is best
        double cayp = cayz*cayz*dphi*dphi;
        dfacL = cayp*(0.125*cosphi*cosphi+(1./576.)*dphi*dphi*sinphi*sinphi);
        
        dfac[0] = dfacT;
        dfac[1] = dfacL;
        
        return dfac;
    }
    
    
    /**
     * <p>
     * Calculation of emittance increase due to phase spread
     * based on calculations in Trace3d (RfGap.f)
     * </p>
     * <p>
     * Used in EnvTrackerAdapt, EnvelopeTracker
     * </p>
     * <h3>CKA Notes:</h3>
     * <p>
     * - I think this should go in the <b>Algorithm</b> class.
     * It expects an <code>EnvelopeProbe</code> - element objects
     * should really not be concerned with the type of probe.
     * </p>
     *   
     * @param probe     envelope probe object (something with emittance and moments)
     * @param gap       the RF gap modeling element creating the bunch phase spread
     * 
     * @return  the change in emittance after going through this element
     */
    public double correctTransSigmaPhaseSpread(EnvelopeProbe probe, IdealRfGap gap) {
        
        double dfac = 1;
//        if (!(probe instanceof EnvelopeProbe)) {
//            return dfac;
//        }
        
        double phi = gap.getPhase();
        double dphi = this.phaseSpread(probe, gap);
        double f1   = correctTransFocusingPhaseSpread(probe, gap);
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
        double dw = gap.energyGain(probe);
        double wa = w+dw/2;
        double betagammaa = Math.sqrt(wa/m*(2+wa/m));
        
        double wf = w+dw;
        double betagammaf =Math.sqrt(wf/m*(2+wf/m));
        
        
        double clight = IProbe.LightSpeed;
        double freq = gap.getFrequency();
        double lambda = clight/freq;
        //      double cay = h*Math.PI*getETL()*Math.abs(Q)/(m*betagammaa*betagammaa*betagammaa*lambda); //Kx'
        //sako 21 jul 06
        
        double cay = Math.abs(Q)*h*Math.PI*gap.getETL()/(m*betagammaa*betagammaa*betagammaf*lambda); //Kx'
        
        dfac = cay*cay*(G1-sinphi*sinphi*f1*f1);
        
        return dfac;
    }
    
    
    /**
     * <p>
     * Calculation of emittance increase due to phase spread
     * based on calculations in Trace3d (RfGap.f)
     * </p>
     * <p>
     * used in EnvTrackerAdapt, EnvelopeTracker
     * </p>
     * <h3>CKA Notes:</h3>
     * <p>
     * - I think this should go in the <b>Algorithm</b> class.
     * It expects an <code>EnvelopeProbe</code> - element objects
     * should really not be concerned with the type of probe.
     * </p>
     * 
     * @param probe envelope-type probe (something with emittance and moments)
     * @param gap       the RF gap modeling element creating the bunch phase spread
     * 
     * @return  the increase in longitudinal emittance due to finite phase spread
     */
    public double correctLongSigmaPhaseSpread(EnvelopeProbe probe, IdealRfGap gap) {
        
        double dfac = 1;
//        if (!(probe instanceof EnvelopeProbe)) {
//            return dfac;
//        }
        
        double phi = gap.getPhase();
        double dphi = this.phaseSpread(probe, gap);
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
        double dw = gap.energyGain(probe);
        double wa = w+dw/2;
        double gammaa = (wa+m)/m;
        double betagammaa = Math.sqrt(wa/m*(2+wa/m));
//        double betagamma0 = Math.sqrt(w/m*(2+w/m));
        double clight = IProbe.LightSpeed;
        double freq = gap.getFrequency();
        double lambda = clight/freq;
        
        
        double wf = w+dw;
        double betagammaf =Math.sqrt(wf/m*(2+wf/m));
        
        //was def       
        //  double cayd = h*Math.PI*getETL()*Math.abs(Q)/(m*betagammaa*betagammaa*betagamma0*lambda);
        
        //21 jul 06
        double cay = h*Math.PI*gap.getETL()*Math.abs(Q)/(m*betagammaa*betagammaa*betagammaf*lambda);
        
        //  cay = cayd;
        
        double cayz = 2*cay*gammaa*gammaa;
        double cayp = cayz*cayz*dphi*dphi;
        dfac = cayp*(0.125*cosphi*cosphi+(1./576.)*dphi*dphi*sinphi*sinphi);
        
        return dfac;
    }



//    /**
//     * <p>
//     * Calculation of the emittance growth factor of an RF gap
//     * in the transverse plane.  The emittance growth is caused 
//     * by a finite longitudinal phase spread of the beam.
//     * </p>
//     * <p>
//     * The calculation is based upon the results of M. Weiss for
//     * the transverse case.  This is also the same value used
//     * in Trace3d (RfGap.f).  
//     * </p>
//     * <p>
//     * Consider the <i>x</i> phase plane.  The emittance growth 
//     * effect is achieved
//     * by first multiplying the element &lt;x'|x&gt; of the RF gap transfer
//     * matrix <b>&Phi;</b> by the factor <i>F<sub>t</sub></i>(&Delta;&phi;)
//     * returned by method <code>momentSine(double)</code>.  Once the 
//     * covariance matrix <b>&tau;</b> is propagated by the modified transfer 
//     * matrix <b>&Phi;</b>, the moment &lt;<i>x</i><sup>2</sup>&gt; is 
//     * augmented by the result of this function.
//     * </p>
//     * <p>
//     * <strong>References</strong>
//     * <br>
//     * [1] M. Weiss,
//     *      "Bunching of Intense Proton Beams with Six-Dimensional
//     *       Matching to the Linac Acceptance", CERN/MPS/LI report 73-2,
//     *       Geneva, Switzerland (1978).
//     * </p>
//     * 
//     * @param   probe   probe containing covariance moment data
//     * @param   elem    RF Gap producing emittance growth 
//     * 
//     * @return  transverse emittance growth factor 
//     * 
//     * @see xal.model.elem.IdealRfGap
//     * 
//     * @deprecated  The functionality of this method has been replaced by
//     *              {@link #compEmitGrowthFunction(PhasePlane, double, double)}
//     */
//    protected double emitGrowthCoefTrans(EnvelopeProbe probe, IdealRfGap elem) {
//    //        double  bf  = elem.betaFinal(probe);
//    //        double  gf  = elem.gammaFinal(probe);
//    //        double  k   = elem.compTransFocusing(probe)/(bf*gf);
//            double  k   = elem.compTransFocusing(probe);
//            double  k_2 = k*k;
//            
//            double  ps  = elem.getPhase();
//            double  dp  = this.effPhaseSpread(probe, elem);
//            double  f   = this.momentSine(dp);
//            double  g   = this.momentSineSquared(ps, dp);
//            double  fsin = f*Math.sin(ps);
//            
//            double  T = g - fsin*fsin;
//            
//            return k_2*T;
//        }
//
//    /**
//         * <p>
//         * Calculation of the emittance growth factor of an RF gap
//         * in the longitudinal plane.  The emittance growth is caused 
//         * by a finite longitudinal phase spread of the beam.
//         * </p>
//         * <p>
//         * The calculation is the same as that performed in Trace3D
//         * for the longitudinal case.
//         * </p>
//         * <p>
//         * The emittance growth effect is achieved
//         * by first multiplying the element &lt;z'|z&gt; of the RF gap transfer
//         * matrix <b>&Phi;</b> by the factor <i>F<sub>t</sub></i>(&Delta;&phi;)
//         * returned by method <code>momentSine(double)</code>.  Once the 
//         * covariance matrix <b>&tau;</b> is propagated by the modified transfer 
//         * matrix <b>&Phi;</b>, the moment &lt;<i>z</i><sup>2</sup>&gt; is 
//         * augmented by the result of this function.
//         * </p>
//         * <p>
//         * <strong>NOTES</strong>: (CKA)
//         * <br>
//         * &middot; <strong>Important</strong>: This method returns a second-order
//         * Taylor expansion about the point &Delta;&phi; = 0.  Because this type
//         * of approximation is parabolic in the phase spread &Delta;&phi; it has no 
//         * limit as &Delta;&phi; &rarr; &infin;.
//         * <br>
//         * &middot; The method for calculating this result is not explained in the
//         * Trace3D manual, only presented.
//         * &middot; There is a descrepancy between the manual
//         * and the Trace3D code. This method returns the result given in the
//         * Trace3D code.
//         * <p>
//         * See K.R. Crandall and D.P. Rusthoi, 
//         *          <ul>
//         *          "Trace 3-D Documentation", 
//         *          LANL Report LA-UR-97-887 (1997), Appendix F.
//         *          </ul>
//         * </p>
//         * 
//         * @param   probe   probe containing moment data
//         * @param   elem    RF Gap producing emittance growth (type IdealRfGap)
//         * 
//         * @return  longitudinal growth factor
//         * 
//         * @see xal.model.elem.IdealRfGap
//         * 
//         * @deprecated  The functionality of this method has been replaced by
//         *              {@link #compEmitGrowthFunction(PhasePlane, double, double)}
//         */
//        protected double emitGrowthCoefLong(EnvelopeProbe probe, IdealRfGap elem) {
//        
////                double  gf   = elem.gammaFinal(probe);
//        //        double  bf   = elem.betaFinal(probe);
//        //        double  bgf  = bf*gf;
////                double  gf_2 = gf*gf;
//                double  ga   = elem.gammaMidGap(probe);
//                
//                double  k    = -2.0*elem.compTransFocusing(probe)*ga*ga;
//        //        k = k/(bgf);
//                
//                double  ps  = elem.getPhase();
//                double  dp  = this.effPhaseSpread(probe, elem);
//                double  sin = dp*Math.sin(ps);
//                double  cos = Math.cos(ps);
//                
//    //            double  T    = cos*cos/8.0 + sin/576.0;
//                double  T    = cos*cos/8.0 + sin*sin/576.0;
//                double  kdp  = k*dp;
//                
//    //            return kdp*kdp*T /(gf_2*gf_2);
//                return kdp*kdp*T;
//            }
//
//    /**
//     * <p>
//     * Function for computing the kluge of &lt;sin(&phi;)&gt;.  In this calculation
//     * we assume that the transverse and longitudinal phase planes are 
//     * uncorrelated and that the beam distribution is a uniform ellipsoid
//     * (that's why the sinc() function pops up).  Pretty restrictive - Sacherer's 
//     * theorem does not apply here.
//     * </p>
//     * <p>
//     * This quantity is used when computing the transverse emittance increase
//     * in an RF gap due to a finite phase spread in the beam.
//     * <p>
//     * <strong>NOTES</strong>: (CKA)
//     * <br>
//     * &middot; This method is used to approximate &lt;x<sup>2</sup>sin(&phi;)&gt;, 
//     * which is at least third order in the phase coordinates.
//     * <br>
//     * 
//     * &middot; The assumption that <i>x</i> and <i>z</i> are not correlated 
//     * yields the result
//     * <br>
//     * <br>
//     *  &nbsp; &lt;x<sup>2</sup>sin(&phi;)&gt; = &lt;x<sup>2</sup>&gt; <i>f</i>(<i>d&phi;</i>)
//     * <br>
//     * <br>
//     * where <i>f</i>(<i>d&phi;</i>) &equiv; &lt;sin(<i>d&phi;</i>)is this method, 
//     * and <i>d&phi;</i> is the "<em>phase spread</em>" of
//     * the distribution.  The phase spread is defined
//     * <br>
//     * <br>
//     * &nbsp;   <i>d&phi;</i> = &lt;(<i>&phi; - &phi;<sub>s</sub></i>)<sup>2</sup>&gt;<sup>1/2</sup>
//     * <br>
//     * <br>
//     * where <i>&phi;<sub>s</sub></i> is the synchronous particle phase.
//     * </p>
//     * <p>
//     * See K.R. Crandall and D.P. Rusthoi, 
//     *          <ul>
//     *          "Trace 3-D Documentation", 
//     *          LANL Report LA-UR-97-887 (1997), Appendix F.
//     *          </ul>
//     * </p>
//     * 
//     *
//     * @param   dp      phase spread half-width in <b>radians</b>
//     * 
//     * @return  the value of &lt;sin(<i>&phi;</i>)&gt; = <i>f</i>(<var>dp</var>) 
//     *
//     * 
//     * @author Christopher K. Allen
//     * 
//     * @see xal.model.elem.IdealRfGap
//     * @see EnvelopeTrackerBase#compTransFourierTransform(double)
//     * 
//     * @deprecated  This method is replaced by the method 
//     *              <code>transFourierBesselTransformUniform(double)</code>
//     *              which computes
//     *              exactly the same result but is a theoretic generalization.
//     */
//    protected double momentSine(double dp) {
//        double dp_2 = dp*dp;
//    
//        if (dp < 0.1) { // Avoid singularity at zero - Taylor expansion
//            return 1.0 - dp_2/14.0 + dp_2*dp_2/504.0;
//    
//        } else {        // Full expression
//            double T    = 3.0/dp_2;
//            
//            double sinc = ElementaryFunction.sinc(dp);
//            double cos  = Math.cos(dp);
//            
//            return (5.0*T)*(sinc*(T-1.0) - cos*T);
//            
//        }
//    }

//    /**
//     * <p>
//     * Function for computing the kluge of &lt;sin<sup>2</sup>(&phi;)&gt;.  
//     * In this calculation
//     * we assume that the transverse and longitudinal phase planes are 
//     * uncorrelated and that the beam distribution is a uniform ellipsoid
//     * (that's why the sinc() function pops up).  Pretty restrictive - Sacherer's 
//     * theorem does not apply here.
//     * </p>
//     * <p>
//     * This quantity is used when computing the transverse emittance increase
//     * in an RF gap due to a finite phase spread in the beam.
//     * <p>
//     * <p>
//     * <strong>NOTES</strong>: (CKA)
//     * <br>
//     * &middot; This method is used to approximate
//     * &lt;<i>x</i><sup>2</sup>sin<sup>2</sup>(<i>&phi;</i>)&gt;, which
//     * is at least fourth order in the phase coordinates
//     * <br>
//     * &middot; The assumption that <i>x</i> and <i>z</i> are not correlated 
//     * yields the result
//     * <br>
//     * <br>
//     * &nbsp;  &lt;<i>x</i><sup>2</sup>sin<sup>2</sup>(<i>&phi;</i>)> = &lt;<i>x</i><sup>2</sup>&gt;<i>g</i>(<i>d&phi;</i>)
//     * <br>
//     * <br>
//     * where <i>g</i>(<i>d&phi;</i>) &equiv; &lt;sin<sup>2</sup>(<i>&phi;</i>)&gt; 
//     * is this method, and <i>d&phi;</i> is the 
//     * "<em>phase spread</em>" of
//     * the distribution.  The phase spread is given by
//     * <br>
//     * <br>
//     * &nbsp; <i>d&phi;</i> = &lt;(<i>&phi; - &phi;<sub>s</sub></i>)<sup>2</sup>&gt;<sup>1/2</sup>
//     * <br>
//     * <br>
//     * where <i>&phi;<sub>s</sub></i> is the synchronous particle phase.
//     * </p>
//     *
//     * @param   ps      synchronous particle phase 
//     * @param   dp      phase spread half-width in <b>radians</b>
//     * 
//     * @return  the value of &lt;sin<sup>2</sup>(<i>&phi;</i>)&gt; = <i>g</i>(<i>d&phi;</i>)  
//     * 
//     * @author Christopher K. Allen
//     * 
//     * @see Appendix F of the Trace3D manual.
//     * @see xal.model.elem.IdealRfGap
//     * 
//     * @deprecated  This method will no longer be necessary once 
//     *              {@link #compTransFourierTransform(double)}
//     *              is used.
//     */
//    protected double momentSineSquared(double ps, double dp) {
//        double  f   = this.momentSine(2.0*dp);
//        double  cos = Math.cos(2.0*ps);
//        
//        return 0.5*(1.0 - cos*f);
//    }

}

