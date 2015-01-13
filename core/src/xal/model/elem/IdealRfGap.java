/*
 *  IdealRfGap.java
 *
 *  Created on October 22, 2002, 1:58 PM
 */
package xal.model.elem;

import java.io.PrintWriter;

import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.sync.IRfCavityCell;
import xal.model.elem.sync.IRfGap;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.sim.scenario.LatticeElement;
import xal.smf.impl.RfGap;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.RelativisticParameterConverter;
import xal.tools.math.poly.UnivariateRealPolynomial;

/**
 *  <p>
 *  Represents the action of an ideal RF gap. Gap is modeled as a thin element
 *  whose accelerating action is given by the Panofsky formula. </p> <p>
 *  </p>
 *  <p>
 *  The gap provides acceleration to the propagation probe as well as
 *  longitudinal focusing and radial defocusing. These mechanisms are
 *  implemented according to that provided by an ideal gap where the effects can
 *  be described analytically.
 *  </p>
 *  <p>
 *  <h4>CKA NOTES:</h4>
 *  <br/>
 *  &middot; The gap length <i>L</i> should not be used in any time or phase
 *  calculations since it is absorbed by adjacent drift spaces. (So I believe.)
 *  For example, drift time &Delta;<i>t</i> should probably not include the 
 *  term &Delta;<i>t</i> &prop; &omega;<i>L</i>/2. 
 *  <br/>
 *  <br/>
 *  &middot; The <i>phase correction</i> parameter &Delta;&phi; I believe is the
 *  change in probe phase due to propagation from the first gap in the cavity.
 *  Thus, the first gap has a correction of &Delta;&phi; and the probe phase
 *  there &phi;<sub>0</sub> is simply the cavity phase &phi;<sub><i>cav</i></sub>.
 *  <br/>
 *  <br/> 
 *  &middot; It is essential that the probe has the correct phase coming into
 *  this element.  If this is a first
 *  gap (see <code>{@link #isFirstGap()}</code>) then that 
 *  phase must be the phase of the cavity, given by
 *  <code>{@link #getPhase()}</code>.  Thus, we must set the
 *  probe phase to the phase of the cavity at these special gaps. (This is
 *  unfortunately necessary since we cannot implement RF cavity elements
 *  with the current lattice generator.)  In the Element/Algorithm/Probe
 *  architecture elements do not modify probes.  Thus, although it's a kluge
 *  no matter which way you do it, the probe phase should be reset by the
 *  algorithm.
 *  <br/>
 *  <br/>
 *  &middot; There are provisions for both an offset of gap electrical center
 *  with geometric center and for the Fourier sine transit time factor <i>S</i>.
 *  And both values are used.  This creates a potential inconsistency since the sine
 *  transit time factor can account for any shifts in the field center
 *  (i.e., the Fourier sine and cosine transforms together can represent
 *  <b>any</b> continuous function.)  I'm not sure if the provided <i>S</i>(&beta;)
 *  is taken at the geometric center or the electrical center (there it would
 *  probably be zero).  If at the geometric center you are probably shifting
 *  everything right back to the geometric center by using the offset.
 *  </p>
 *
 * @author     Christopher K. Allen
 * @since      November 22, 2005
 * @version    Nov 23, 2014
 */
public class IdealRfGap extends ThinElement implements IRfGap, IRfCavityCell {

	/*
	 *  Global Constants
	 */
	
    /**
	 *  the string type identifier for all IdealRfGap objects
	 */
	public final static String s_strType = "IdealRfGap"; //$NON-NLS-1$

	
	//
	// DataAdaptor Data Tags
	//
	
	/**
	 *  Parameters for XAL MODEL LATTICE dtd
	 */
	public final static String s_strParamETL = "ETL"; //$NON-NLS-1$
	
	/**
	 *  Description of the Field
	 */
	public final static String s_strParamPhase = "Phase"; //$NON-NLS-1$
	
	/**
	 *  Description of the Field
	 */
	public final static String s_strParamFreq = "Frequency"; //$NON-NLS-1$
	
	
	//
	// Numeric Constants
	//
	
    /** The number 2&pi; */
    public final static double  DBL_2PI = 2.0*Math.PI;

    
    /** Error tolerance in the iterative search for phase change through RF gap */
    private static final double DBL_PHASECALC_CNVERR = 1.0e-6;

    /** Maximum number of allowable iterations in the phase change search */
    private static final int INT_PHASECALC_MAXITER = 50;
    

    /*
     * Internal Data Structures
     */
    
    /**
     * Represents the longitudinal conjugate variables of 
     * phase and energy.
     *
     * @author Christopher K. Allen
     * @since  Nov 28, 2014
     */
    private class EnergyVariables {
        
        /** particle phase */
        public double       phi;
        
        /** particle energy */
        public double       W;
        
        /** Initializing Constructor */
        public EnergyVariables(double phi, double W) {
            this.phi = phi;
            this.W   = W;
        }

        /** Zero argument constructor */
        public EnergyVariables() {
            this.phi = 0.0;
            this.W   = 0.0;
        }
    }
    
    
	/*
	 * Global Attributes
	 */
	
	/**
	 * TODO CKA - Remove
	 * 
	 * Don't know what this is? CKA
	 */
	public static double COEFF_X = 1.0;

	/**
	 * TODO CKA - Remove
	 * 
     * Don't know what this is? CKA
	 */
	public static double COEFF_Y = 1.0;	
	

	/*
	 * Class Dynamic Variables???
	 */
	
    /**
     * TODO CKA - Remove
     * 
     *  These are kluge jobs for RF cavities.  Very dangerous since
     *  they are class variables. 
     */
    private static double FIRST_GAP_PHASE_CORR = 0.;
    
    /**
     * TODO CKA - Remove
     * 
     *  These are kluge jobs for RF cavities.  Very dangerous since
     *  they are class variables. 
     */
    private static double STRUCTURE_PHASE = 0.;
    
//    /**
//     *  These are kluge jobs for RF cavities.  Very dangerous since
//     *  they are class variables. 
//     */
//    private static double UPSTREAM_EXIT_TIME = 0.;

    /** 
     * TODO CKA - Remove
     * 
     * the phase kick correction applied at the gap center [rad]
     * */
    private static double DELTA_PHASE_CORRECTION = 0.;

	
	
	/*
	 *  Local Attributes
	 */
	
    //
    // Operating Parameters
    //
    
	/**
	 *  ETL product of gap
	 */
	private double m_dblETL = 0.0;

	/**
	 *  phase delay of gap w.r.t. the synchronous particle
	 */
	private double m_dblPhase = 0.0;

    /**
     *  the on axis accelerating field (V)
     */
    private double dblFieldE0 = 0.;

	/**
	 *  operating frequency of the gap
	 */
	private double m_dblFreq = 0.0;

	
	//
	// Gap Properties
	//
	
    /**
     *  the separation of the gap center from the cell center (m)
     */
    private double gapOffset = 0.;
    
    /**
     *  the accelerating cell length
     */
    private double cellLength = 0.;

    /**
     *  fit of the TTF vs. beta
     */
    private UnivariateRealPolynomial fitTTF;

    /**
     *  fit of the TTF-prime vs. beta
     */
    private UnivariateRealPolynomial fitTTFPrime;

    /**
     *  fit of the S factor vs. beta
     */
    private UnivariateRealPolynomial fitSTF;

    /**
     *  fit of the S-prime vs. beta
     */
    private UnivariateRealPolynomial fitSTFPrime;


    //
    // RF Cavity Properties
    // 
    
    /**
     *  = 0 if the gap is part of a 0 mode cavity structure (e.g. DTL) = 1 if the
     *  gap is part of a pi mode cavity (e.g. Super-conducting)
     */
    private double dblCavModeConst = 0.;

    /** 
     * The index of the cavity cell (within the parent cavity) containing this gap.
     */
    private int     indCell = 0;
    
    //
    // Legacy
    //
    
    /**
	 * TODO CKA - Remove
	 * 
	 *  flag indicating that this is the leading gap of a cavity
	 */
    @Deprecated
	private boolean initialGap = false;

	/**
	 * TODO CKA - Remove
	 * 
	 *  the energy gained in this gap (eV)
	 */
    @Deprecated
	private double theEnergyGain = 0.;

	
	/*
	 * Initialization
	 */
	
	/**
	 *  Creates a new instance of IdealRfGap
	 *
	 *@param  strId     instance identifier of element
	 *@param  dblETL    field/transit time/length factor for gap (in <b>volts</b> )
	 *@param  dblPhase  operating phase of gap (in <b>radians</b> )
	 *@param  dblFreq   operating RF frequency of gap (in <b>Hertz</b> )
	 */
	public IdealRfGap(String strId, double dblETL, double dblPhase, double dblFreq) {
		super(s_strType, strId);

		this.setETL(dblETL);
		this.setPhase(dblPhase);
		this.setFrequency(dblFreq);
	}

	/**
	 *  JavaBean constructor - creates a new uninitialized instance of IdealRfGap <b>
	 *  BE CAREFUL</b>
	 */
	public IdealRfGap() {
		super(s_strType);
	}


	/*
	 * Attribute Query
	 */
	
	/**
	 *  return whether this gap is the initial gap of a cavity
	 *
	 *@return    The firstGap value
	 */
	public boolean isFirstGap() {
	    return initialGap;
	}

	/**
	 *  return the cell length (m)
	 *  
	 *  <p>
	 *  <h4>CKA NOTES:</h4>
	 *  &middot; I believe this is the length of the overall gap cell
	 *  structure, not just the gap itself.
	 *  <br/>
	 *  &middot; Specifically, it is the distance from one gap center
	 *  to the next in an accelerating structure.
	 *  <br/>
	 *  &middot; I'm not sure if the electric field maximum is averaged over this
	 *  quantity, I suppose it should be.
	 *  </p>
	 *
	 *@return    The cellLength value
	 */
    public double getCellLength() {
	    return cellLength;
    }
    
    /**
     * Return the displacement of the "effective gap" (i.e., the gap model)
     * from the true center of the actual RF gap center.  The true center
     * would be the value provided to the SMF accelerator hierarchy.
     *  
     * @return  the difference between the RF gap true center and effective center (in meters)
     *
     * @author Christopher K. Allen
     * @since  Nov 17, 2014
     */
    public double getGapOffset() {
        return this.gapOffset;
    }
    
    

    /*
     * Operations
     */
    
    /**
     * Compute the wavelength of the RF.
     * 
     * @return  RF wavelength in <b>meters</b>
     */
    public double   wavelengthRF()  {
        
        // Compute the RF wavelength
        double c      = IElement.LightSpeed;
        double f      = getFrequency();
        double lambda = c/f;

        return lambda;
    }

    /**
     * Compute and return the mid-gap normalized velocity for the
     * given probe.
     * 
     * NOTE:
     * - Because of the state-dependent nature of the energy calculations
     * (this needs to be fixed), this function will only work correctly
     * if the function energyGain() is consistent.
     * 
     * @param   probe   probe containing energy information
     * 
     * @return  average or "mid-gap" velocity in units of <b>c</b>
     * 
     * @see IdealRfGap#energyGain(IProbe)
     */
    public double   betaMidGap(IProbe probe)   {
        
        // Get probe parameters at initial energy
        double Er = probe.getSpeciesRestEnergy();
        double Wi = probe.getKineticEnergy();
        double dW = this.energyGain(probe);
        double Wa = Wi + dW/2.0;
        
        double beta = RelativisticParameterConverter.computeBetaFromEnergies(Wa, Er);
        
        return beta;
    }
    
    /**
     *  a method that is called once by transferMatrix to calculate the energy
     *  gain. This prevents energy gain calculation from being repeated many times.
     *  Importantly it provides a workaround from the eneryGain being calculated
     *  after the upstreamExitPhase is updated elsewhere
     *
     *@param  probe  The Parameter
     *@return        The Return Value
     */
    /*
     *  public double compEnergyGain(IProbe probe) {
     *  }
     */
    /**
     *  Get the transverse focusing constant for a particular probe. The focusing
     *  constant is used in the construction of the transfer matrix for the RF gap.
     *  A gap provides longitudinal focusing and transverse defocusing as well as a
     *  gain in beam energy. This focusing constant describes the effect in the
     *  transverse direction, which is defocusing and, therefore, negative. <p>
     *
     *  The value represents the thin lens focusing constant for an ideal RF gap
     *  (this is the inverse of the focal length). To compute the focusing action
     *  for the lens we must include beam energy, which is changing through the
     *  gap. We use the value of beta for which the beam has received half the
     *  total energy gain.
     *
     *@param  probe  beam energy and particle charge are taken from the probe
     *@return        (de)focusing constant (<b>in radians/meter</b> )
     */
    public double compTransFocusing(IProbe probe) {

//        double  q = IElement.UnitCharge;
        double c = IElement.LightSpeed;

        double Q = Math.abs(probe.getSpeciesCharge());
        double Er = probe.getSpeciesRestEnergy();
        double Wi = probe.getKineticEnergy();

        double Wbar = Wi + this.energyGain(probe) / 2.0;
        double gbar = Wbar / Er + 1.0;
        double bbar = Math.sqrt(1.0 - 1.0 / (gbar * gbar));
        double bgbar = bbar * gbar;

        double ETL = this.getETL();
        double phi = this.getPhase();
        double f = this.getFrequency();

//        double   kr = Math.PI*Q*ETL*f*Math.sin(-phi)/(q*c*Er*bgbar*bgbar);
        double kr = Math.PI * Q * ETL * f * Math.sin(-phi) / (c * Er * bgbar * bgbar);

        return kr;
    }

    /**
     *  Get the longitudinal focusing constant for a particular probe. The focusing
     *  constant is used in the construction of the transfer matrix for the RF gap.
     *  A gap provides longitudinal focusing and transverse defocusing as well as a
     *  gain in beam energy. This focusing constant describes the effect in the
     *  longitudinal direction, which is focusing and, therefore, positive. <p>
     *
     *  The value represents the thin lens focusing constant for an ideal RF gap
     *  (this is the inverse of the focal length). To compute the focusing action
     *  for the lens we must include beam energy, which is changing through the
     *  gap. We use the value of beta for which the beam has received half the
     *  total energy gain.
     *
     *@param  probe  beam energy and particle charge are taken from the probe
     *@return        (de)focusing constant (<b>in radians/meter</b> )
     */
    public double compLongFocusing(IProbe probe) {

        double Er = probe.getSpeciesRestEnergy();
        double Wi = probe.getKineticEnergy();

        double Wbar = Wi + this.energyGain(probe) / 2.0;
        double gbar = Wbar / Er + 1.0;

        double kr = this.compTransFocusing(probe);
        double kz = -2.0 * kr * gbar * gbar;

        return kz;
    }

    
    /*
     *  IRfGap Interface
     */
    
    /**
	 *  Return the ETL product of the gap, where E is the longitudinal electric
	 *  field, T is the transit time factor, and L is the gap length.
	 *
	 *@return    the ETL product of the gap (in <bold>volts</bold> ).
	 */
	@Override
	public double getETL() {
		return m_dblETL;
	}

	/**
	 *  Return the RF phase delay of the gap with respect to the synchronous
	 *  particle.
	 *
	 *@return    phase delay w.r.t. synchronous particle (in <bold>radians</bold> ).
	 */
    @Override
	public double getPhase() {
		return m_dblPhase;
	}

	/**
	 *  Get the operating frequency of the RF gap.
	 *
	 *@return    frequency of RF gap (in <bold>Hertz</bold> )
	 */
    @Override
	public double getFrequency() {
		return m_dblFreq;
	}

	/**
	 *  Set the ETL product of the RF gap where E is the longitudinal electric
	 *  field of the gap, T is the transit time factor of the gap, L is the length
	 *  of the gap. <p>
	 *
	 *  The maximum energy gain from the gap is given by qETL where q is the charge
	 *  (in coulombs) of the species particle.
	 *
	 *@param  dblETL  ETL product of gap (in <bold>volts</bold> ).
	 */
    @Override
	public void setETL(double dblETL) {
		m_dblETL = dblETL;
	}

	/**
	 *  Set the phase delay of the RF in gap with respect to the synchronous
	 *  particle. The actual energy gain from the gap is given by qETLcos(dblPhi)
	 *  where dbkPhi is the phase delay.
	 *
	 *@param  dblPhase  phase delay of the RF w.r.t. synchronous particle (in
	 *      <bold>radians</bold> ).
	 */
    @Override
	public void setPhase(double dblPhase) {
		m_dblPhase = dblPhase;
	}

	/**
	 *  Set the operating frequency of the RF gap.
	 *
	 *@param  dblFreq  frequency of RF gap (in <bold>Hertz</bold> )
	 */
    @Override
	public void setFrequency(double dblFreq) {
		m_dblFreq = dblFreq;
	}

	/**
	 * Set the on accelerating field E - the on axis field (V/m)
	 *
	 * @param  E  The new E0 value
	 */
    @Override
	public void setE0(double E) {
		dblFieldE0 = E;
	}

	/**
	 *  Get the on accelerating field (V/m)
	 *
	 *@return    The e0 value
	 */
    @Override
	public double getE0() {
		return dblFieldE0;
	}

    
    /*
     * IRfCavityCell Interface
     */

    /**
     *
     * @see xal.model.elem.sync.IRfCavityCell#setCavityCellIndex(int)
     *
     * @since  Jan 8, 2015   by Christopher K. Allen
     */
    @Override
    public void setCavityCellIndex(int indCell) {
        this.indCell = indCell;
    }

    /**
     *
     * @see xal.model.elem.sync.IRfCavityCell#setCavityModeConstant(double)
     *
     * @since  Jan 8, 2015   by Christopher K. Allen
     */
    @Override
    public void setCavityModeConstant(double dblCavModeConst) {
        this.dblCavModeConst = dblCavModeConst;
    }

    /**
     *
     * @see xal.model.elem.sync.IRfCavityCell#getCavityCellIndex()
     *
     * @since  Jan 8, 2015   by Christopher K. Allen
     */
    @Override
    public int getCavityCellIndex() {
        return this.indCell;
    }

    /**
     * <p>
     * Returns the structure mode <b>number</b> <i>q</i> for the cavity in which this 
     * gap belongs.  Here the structure mode number is defined in terms of
     * the fractional phase advance between cells, with respect to &pi;.  
     * To make this explicit
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>q</i> = 0  &nbsp; &nbsp; &rAarr;  0 mode
     * <br/>
     * &nbsp; &nbsp; <i>q</i> = 1/2 &rArr; &pi;/2 mode
     * <br/>
     * &nbsp; &nbsp; <i>q</i> = 1  &nbsp; &nbsp; &rAarr;  &pi; mode
     * <br/>
     * <br/>
     * Thus, a cavity mode constant of <i>q</i> = 1/2 indicates a &pi;/2
     * phase advance between adjacent cells and a corresponding cell amplitude
     * function <i>A<sub>n</sub></i> of
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>A<sub>n</sub></i> = cos(<i>nq</i>&pi;)
     * <br/>
     * <br/>
     * where <i>n</i> is the index of the cell within the coupled cavity.
     * </p>
     * 
     * @return  the cavity mode constant for the cell containing this gap
     *
     * @see <i>RF Linear Accelerators</i>, Thomas P. Wangler (Wiley, 2008).
     * 
     * @author Christopher K. Allen
     * @since  Nov 20, 2014
     */
    public double getCavityModeConstant() {
        return this.dblCavModeConst;
    }


    /*
     *  IElement Interface
     */

    /**
     *  Returns the time taken for the probe to propagate through element.
     *  
     * @param  probe  propagating probe
     *
     * @return        The time taken to propagate through gap including phase shift &delta;&phi; 
     *               and any gap offset &Delta;<i>l</i> (asymmetric drifting at initial and final energies) 
     */
    @Override
    public double elapsedTime(IProbe probe) {

        // Probe parameters
        double c  = IElement.LightSpeed;
        double Er = probe.getSpeciesRestEnergy();
        double Wi = probe.getKineticEnergy();
        double bi = probe.getBeta();
        //      double gi = probe.getGamma();

        // Gap parameters
        double f  = this.getFrequency();
        double dl = this.getGapOffset();

        // Gap effects
        EnergyVariables prmGapEff = this.compGapPhaseAndEnergyImpulses(probe);

        double dW    = prmGapEff.W;
        double d_phi = prmGapEff.phi;

        // Final energy parameters
        double Wf = Wi + dW;
        double gf = Wf / Er + 1.0;
        double bf = Math.sqrt(1.0 - 1.0 / (gf * gf));

        // update the elapsed time to account for the phase correction term
        double dT = d_phi/(2.0*Math.PI*f);

        //the gap offset correction
        dT = dT + dl*( 1.0/ (bi*c) - 1.0/ (bf*c));

        //        //the time when probe will exit this gap
        // TODO Delete this!
        //        UPSTREAM_EXIT_TIME = probe.getTime() + dT + (getCellLength()/2.) / (bf*c);

        return dT;
    }

    //    /** the interface method to provide the energy gain.
    //    * since this calculation has gotten complicated it is done
    //    * in the TransferMap method and the answer is returned here. */
    //
    /**
     *  Compute the energy gain of the RF gap for a probe including the effects of
     *  calculating the phase advance.
     *
     * @param  probe  uses the particle species charge
     * 
     * @return        energy gain for this probe (<b>in electron-volts</b> )
     */
    @Override
    public double energyGain(IProbe probe) {
        double  dW = this.compGapEnergyGain(probe);

        return dW;
    }

    /**
     * Compute and return the longitudinal phase advance (w.r.t. the RF) for
     * the given probe while propagating through this element.
     *
     * @see xal.model.elem.ThinElement#longitudinalPhaseAdvance(xal.model.IProbe)
     *
     * @author Christopher K. Allen
     * @since  Nov 23, 2014
     */
    @Override
    public double longitudinalPhaseAdvance(IProbe probe) {
        
        // TODO IMPLEMENT!
        return 0;
        
    }
    
//    /**
//     * Here we need to inform the algorithm (i.e., the
//     * <code>Tracker</code> object) of the gap exit time for use in the
//     * phase advance calculation of the next RF gap element.
//     *
//     * @see xal.model.elem.Element#propagate(xal.model.IProbe)
//     *
//     * @author Christopher K. Allen
//     * @since  Nov 24, 2014
//     */
//    @Override
//    public void propagate(IProbe probe) throws ModelException {
//        super.propagate(probe);
//        
//        IAlgorithm  alg = probe.getAlgorithm();
//        if (alg instanceof Tracker) {
//            Tracker tracker = (Tracker)alg;
//            tracker.setRfGapExitTime();
//        }
//    }

    /**
     * Compute the transfer map for an ideal RF gap.
     *
     * @param  probe               compute transfer map using parameters from this probe
     *
     * @return                     transfer map for the probe
     *
     * @exception  ModelException  this should not occur
     */
    @Override
    protected PhaseMap transferMap(IProbe probe) throws ModelException {

        //      System.out.println("This is " + this.getId());
        //      System.out.println("dblFieldE0 is   " + this.getE0());
        //      System.out.println("ETL is  " + this.getETL());
        //      System.out.println("");


        // Get probe parameters at initial energy
        double Er = probe.getSpeciesRestEnergy();
        double Wi = probe.getKineticEnergy();
        double bi = probe.getBeta();
        double gi = probe.getGamma();

        // Determine the current energy gain and focusing constants for the gap

        // the following section is to calculate the phase of the beam at each gap, rather than use hardwired phases.

        // update the energy gain first:
//        if(probe.getAlgorithm().useRfGapPhaseCalculation()) {
//            compEnergyGain(probe);
//        } else {
//            simpleEnergyGain(probe);
//        }

        double dW = this.compGapEnergyGain(probe);
        double kz = this.compLongFocusing(probe);
        double kt = this.compTransFocusing(probe);

        // Compute final energy parameters
        double Wf = Wi + dW;
        double gf = Wf / Er + 1.0;
        double bf = Math.sqrt(1.0 - 1.0 / (gf * gf));

        // Compute average energy parameters
        //      double Wb = (Wf + Wi) / 2.0;
        //      double gb = Wb / Er + 1.0;
        //        double bg = Math.sqrt(1.0 - 1.0/(gb*gb));


        // Compute component block matrices then full transfer matrix
        double arrTranX[][] = new double[][]{{1.0, 0.0}, {kt*COEFF_X / (bf * gf), bi * gi / (bf * gf)}};
        double arrTranY[][] = new double[][]{{1.0, 0.0}, {kt*COEFF_Y / (bf * gf), bi * gi / (bf * gf)}};

        // CKA - Corrected 7/14/2010
        //  Additional factor gbar^2 in the longitudinal focusing term 
        //      double arrLong[][] = new double[][]{{1.0, 0.0}, {(kz / (bf * gf)) * gb * gb / (gf * gf), gi * gi * gi * bi / (gf * gf * gf * bf)}};
        double arrLong[][] = new double[][]{{1.0, 0.0}, { kz / (bf * gf * gf * gf), gi * gi * gi * bi / (gf * gf * gf * bf)}};

        PhaseMatrix matPhi = new PhaseMatrix();

        matPhi.setElem(6, 6, 1.0);
        matPhi.setSubMatrix(0, 1, 0, 1, arrTranX);
        matPhi.setSubMatrix(2, 3, 2, 3, arrTranY);
        matPhi.setSubMatrix(4, 5, 4, 5, arrLong);

        // Do the phase update if this is desired:
        // do it here to resuse the bi, bf, etc. factors
        //  if(probe.getAlgorithm().useRfGapPhaseCalculation()) advancePhase(probe);

        //        PrintWriter os = new PrintWriter(System.out);
        //        matPhi.print(os);
        //        os.close();

        return new PhaseMap(matPhi);
    }

    /**
     * Conversion method to be provided by the user
     * 
     * @param latticeElement the SMF node to convert
     */
    @Override
    public void initializeFrom(LatticeElement element) {
        super.initializeFrom(element);      
        RfGap rfgap = (RfGap) element.getHardwareNode();

        // Initialize from source values
        initialGap = rfgap.isFirstGap();
        cellLength = rfgap.getGapLength();
        gapOffset = rfgap.getGapOffset();
        fitTTFPrime = rfgap.getTTFPrimeFit();
        fitTTF = rfgap.getTTFFit(); 
        fitSTFPrime = rfgap.getSPrimeFit();
        fitSTF = rfgap.getSFit();
        dblCavModeConst = rfgap.getStructureMode();
    }



	
    /*
     *  Object Overrides
     */

    /**
     *  Dump current state and content to output stream.
     *
     *@param  os  output stream object
     */
    @Override
    public void print(PrintWriter os) {
    	super.print(os);
    
    	os.println("  Gap ETL product    : " + this.getETL()); //$NON-NLS-1$
    	os.println("  Gap phase shift    : " + this.getPhase()); //$NON-NLS-1$
    	os.println("  RF frequency       : " + this.getFrequency()); //$NON-NLS-1$
    	os.println("  Axial field dblFieldE0     : " + this.getE0() );
    }

    
    /*
     *  Support Methods
     */
    
    /**
     * <p>
     * Computes and returns the electric field coefficient associated with the 
     * RF cavity cell containing this gap.  Specifically, if this gap belongs to
     * the <i>n<sup>th</sup></i> cell of the cavity (index origin 0) and the cavity
     * operates in the <i>q</i>&pi;-mode where <i>q</i> is the cavity mode constant,
     * then the field coefficient <i>A<sub>n</sub></i> is defined
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>A<sub>n</sub></i> &#8796; cos(<i>nq</i>&pi;) ,
     * <br/>
     * <br/>
     * so that the electric field <i>E<sub>n</sub></i>(<i>z</i>,<i>t</i>) at cell <i>n</i>
     * is given by 
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>E<sub>n</sub></i>(<i>z</i>,<i>t</i>) = 
     *          <i>A<sub>n</sub></i> <i>E</i><sub>0</sub>(<i>z</i>)
     *          cos(&omega;<i>t</i> + &phi;<sub>0</sub>) ,
     * <br/>
     * <br/>
     * where <i>E</i><sub>0</sub>(<i>z</i>) is the axial field profile of each cell.
     * </p>
     * 
     * @return  the cavity field coefficient <i>A<sub>n</sub></i> for this gap
     *
     * @since  Jan 12, 2015   by Christopher K. Allen
     */
    private double  compCavModeFieldCoeff() {
        final int       n = this.getCavityCellIndex();
        final double    q = this.getCavityModeConstant();
        
        final double    A = Math.cos(n*q*Math.PI);
        
        return A;
    }
    
    /**
     * <p>
     * Computes and returns the total propagation time for the probe from inception
     * until the end of the entire accelerating gap cell.  That is, the returned value
     * would be the total time <b>for the probe</b>, not the time interval for its
     * propagation through the accelerating cell.
     * </p>
     * <p>
     * <h4>CKA NOTES:</h4>
     * &middot; The gap phase change &Delta;&phi; is included in the exit time
     * calculation via the call to <code>{@link #elapsedTime(IProbe)}</code>
     * where it is calculated internally.
     * </p>
     * 
     * @param probe     probe containing parameters for gap computations
     * 
     * @return          probe time at exit of full accelerating cell
     *
     * @author Christopher K. Allen
     * @since  Nov 19, 2014
     */
    @Deprecated
    private double  compCellExitTime(final IProbe probe) {
        double  c  = IElement.LightSpeed;       // speed of light
        double  ti = probe.getTime();           // probe time at cell entrance
        double  bf = this.compFinalBeta(probe); // final probe velocity after gap
        double  dt = this.elapsedTime(probe);   // propagation time through gap
        
        double  dL = this.getCellLength()/2.0;  // half length of total accelerating cell
        
        double  tf = ti + dt + dL/(bf*c);
        
        return tf;
    }
    
    /**
     * Computes the phase shift due to probe drift time between gaps
     * in a coupled-cavity tank.  The returned result considers both the drift
     * time of the probe between gaps and the mode number of the cavity
     * fields. 
     * 
     * @param probe     drifting probe
     * 
     * @return      the phase shift occurring between the previous gap and this gap
     * 
     * @since  Nov 28, 2014 @author Christopher K. Allen
     * 
     * @deprecated  This is bullshit
     */
    @Deprecated
    private double  compCoupledCavityPhaseShift(IProbe probe) {

        // Compute the drifting time since the last gap
        ProbeState<?>   stateLastGap = probe.lookupLastStateFor( this.getType() );

        double  t_prev  = stateLastGap.getTime();   // the exit time of the previous gap
        double  t_curr  = probe.getTime();          // the entrance time of this gap
        double  t_drift = t_curr - t_prev;          // drifting time between two gaps

        // Compute the RF frequency of the operating mode
        double  f_0     = this.getFrequency();
        double  q_mode  = this.getCavityModeConstant();
        double  f_mode  = 2.0*q_mode*f_0;            

        // Compute the number of RF cycles taken between RF gaps
        //  and then the resulting phase shift
        int     nCycles = (int) Math.round(f_mode * t_drift);
        double  D_phi   =  nCycles*Math.PI;

        return D_phi;

        // TODO - Figure this out??
        //  applied after the above is computed and then added on the next call
        //        STRUCTURE_PHASE = STRUCTURE_PHASE - (2 - dblCavModeConst) * Math.PI;
        //        STRUCTURE_PHASE = Math.IEEEremainder(STRUCTURE_PHASE , (2. * Math.PI));
    }

    /**
     * <p>
     * Returns the advance in RF phase for a particle drifting at velocity &beta; for
     * a distance <i>l</i>.
     * The value is simply the time of flight &Delta;<i>t</i> needed to propagate
     * the distance <i>l</i> times the angular frequency &omega; &#8796 2&pi;<i>f</i>
     * of the cavity (<i>f</i> is the fundamental cavity frequency).
     * Specifically,
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &Delta;&phi; = &omega;&Delta;<i>t</i> = &omega;<i>l</i>/&beta;<i>c</i>
     *                            = (2&pi;/&beta;&lambda;)l = <i>kl</i>
     * <br/>
     * <br/>
     * where &Delta;&phi; is the change in phase due to the offset, &lambda; is the 
     * wavelength of the RF, and <i>k</i> is the wave number of the particle.
     * 
     * @param probe     probe object arriving at the gap
     * 
     * @return          the value  &Delta;&phi; from a particle drifting at 
     *                  velocity &beta; for distance <i>l</i>
     *
     * @author Christopher K. Allen
     * @since  Nov 19, 2014
     */
    private double   compDriftingPhaseAdvance(double beta, double len) {
    
        double c = IElement.LightSpeed;       // speed of light
        double f = this.getFrequency();
    
        //the correction for the gap offset needed
        double dt = len / (beta * c);
    
        double dphi = DBL_2PI * f * dt;
        
        return dphi;
    }
    
    /**
     * Computes and returns the phase change due to any gap offset between the
     * geometric center and the electrical center.  The phase change &Delta;&phi;
     * is given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &Delta;&phi; = &omega;<i>l</i>(1/&beta;<sub><i>i</sub>c</i>  - 1/&beta;<sub><i>f</sub>c</i>)
     * <br/>
     * <br/>
     * where &omega; is the angular frequency of the RF, <i>l</i> is the gap offset, 
     * &beta;<sub><i>i</i></sub> is the pre-gap velocity, and &beta;<sub><i>f</i></sub>
     * is the post-gap velocity.  Note that the phase change &Delta;&phi; can be negative
     * if the offset <i>l</i> is toward the downstream direction.  
     * 
     * @param beta_i    the pre-gap velocity &beta;<sub><i>i</i></sub>
     * @param beta_f    the post-gap velocity &beta;<sub><i>f</i></sub>
     * 
     * @return
     *
     * @since  Jan 13, 2015   by Christopher K. Allen
     */
    private double  compGapOffsetPhaseChange(double beta_i, double beta_f) {
        double dl     = this.getGapOffset();
        double dphi_i = this.compDriftingPhaseAdvance(beta_i, dl);
        double dphi_f = this.compDriftingPhaseAdvance(beta_f, dl);
        
        double dphi = dphi_i - dphi_f;
        
        return dphi;
    }

    /**
     * <p>
     * Computes and returns the phase at the position of the gap's 
     * <b>electrical entrance</b>.
     * The longitudinal phase of the given probe is assumed to be at the
     * gap's geometric entrance as it arrives, a correction is necessary if 
     * the geometric and electrical centers are offset.
     * </p>
     * <p>
     * <h4>NOTE</h4>
     * &middot; It is essential that the probe has the correct phase.  
     * If this is the first
     * gap then that phase must be the phase of the cavity, given by
     * <code>{@link #getPhase()}</code>.  Thus, we must set the
     * probe phase to the phase of the cavity at these special gaps. (This is
     * unfortunately necessary since we cannot implement RF cavity elements
     * with the current lattice generator.)  In the Element/Algorithm/Probe
     * architecture elements do not modify probes.  Thus, although it's a kluge
     * no matter which way you do it, the probe phase should be reset by the
     * algorithm.
     * <br/>
     * <br/>
     * &middot; To get the phase at the middle of the gap (again, electrical middle)
     * you can call <code>{@link #compGapPhaseAndEnergyImpulses(IProbe)}</code> and add
     * <b>half</b> that value to this method's returned value.
     * </p>
     * 
     * @param probe     probe containing phase information
     * 
     * @return          the probe phase corrected as necessary for any gap offset
     * 
     * @since  Nov 26, 2014 @author Christopher K. Allen
     */
    private double  compGapEntrancePhase(IProbe probe) {
        
        // Get the phase of the probe at the gap geometric center
//        double phi0 = this.isFirstGap() ? this.getPhase() : probe.getLongitinalPhase();
        double phi0 = probe.getLongitinalPhase();
    
        // Correct the phase as needed for any difference from electrical center
        double  bi  = probe.getBeta();
        double  dl  = this.getGapOffset();
        double dphi = this.compDriftingPhaseAdvance(bi, dl);
        
        double phi  = phi0 + dphi;
        
        return phi;
    }

    /**
     * Computes and returns the longitudinal phase change &delta;&phi; energy 
     * gain &Delta;<i>W</i> through 
     * the gap due to acceleration.  Although this is a thin lens the longitudinal
     * phase must have a impulsive change &delta;&phi; sympletically conjugate 
     * to the change &Delta;<i>W</i> in longitudinal energy.
     *  
     * @param probe     probe propagating through gap, we use its phase and energy parameters
     * 
     * @return          the change in phase &delta;&phi; of the 
     *                  give probe through the gap 
     *
     * @since  Nov 28, 2014  @author Christopher K. Allen
     */
    private double  compGapPhaseGain(final IProbe probe) {
        
        EnergyVariables     cordGapEffects = this.compGapPhaseAndEnergyImpulses(probe);
        
        return cordGapEffects.phi;
    }
    
    /**
     * Computes and returns the final particle velocity through the gap.  That is,
     * we return the velocity of the particle after it has passed through the gap.
     * 
     * @param probe     contains the parameters for gap action
     * 
     * @return          the final probe particle velocity &beta;<sub><i>f</i></sub> with 
     *                  respect to the speed of light <i>c</i>
     *
     * @author Christopher K. Allen
     * @since  Nov 19, 2014
     */
    private double  compFinalBeta(final IProbe probe) {
    
        // Initial energy parameters
        double Er = probe.getSpeciesRestEnergy();
        double Wi = probe.getKineticEnergy();
        
        // The energy gain through the gap
        double dW = this.compGapEnergyGain(probe);
    
        // Final energy parameters
        double Wf = Wi + dW;
        double gf = Wf / Er + 1.0;
        double bf = Math.sqrt(1.0 - 1.0 / (gf * gf));
    
        return bf;
    }

    /**
     *  <p> 
     *  Routine to calculate the energy gain through the gap.  If the 
     *  <code>useRfGapPhaseCalculation</code> flag in the probe's algorithm
     *  is set to <code>true</code> the full iterative search for these
     *  parameters is used.  (This is done by deferring to 
     *  <code>{@link #compGapPhaseAndEnergyImpulses(IProbe)}</code>.)  Otherwise
     *  a simple evaluation of the Panofsky equation is used.
     *  <p>
     *  <h4>CKA NOTES: 11/17/2014</h4>
     *  &middot; This method was at the heart of some major architectural issues.
     *  <br/>
     *  &middot; We have a state-dependent situation, the computed results being
     *  dependent upon the state of <b>class</b> variables.
     *  <br/>
     *  &middot; These class state variables should most likely be local properties
     *  of the probe objects.
     *  </p>
     *
     * @param probe     probe propagating through gap, we use its phase and energy parameters
     * 
     * @return          the change in energy &Delta; <i>W</i> of the 
     *                  give probe through the gap 
     *
     */
    private double compGapEnergyGain(final IProbe probe) {
    
        // If we are not using the cavity RF phase model we just return the
        //  results of the Panofsky equation
        //  Maybe we have the useRfGapPhaseCalculation flag in the probe??!!
        if (probe.getAlgorithm().useRfGapPhaseCalculation() == false) {
            double Q    = Math.abs( probe.getSpeciesCharge() );
    
            double ETL  = this.getETL();
            double phi0 = this.getPhase();   
            
            double dW   = Q * ETL * Math.cos( phi0);  NO
            
            return dW;
        }
    
        // Else we do the full energy gain calculation
        EnergyVariables crdGapEffects = this.compGapPhaseAndEnergyImpulses(probe);
        
        return crdGapEffects.W;
    }

    /**
     * Computes the phase &phi;<sub>mid</sub> and energy <i>W</i><sub>mid</sub> of the 
     * probe at the middle of the gap. The returned values are given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &phi;<sub>mid</sub> = &phi;<sub>0</sub> + &delta;&phi;/2,
     * <br/>
     * &nbsp; &nbsp; <i>W</i><sub>mid</sub> = <i>W</i><sub>0</sub> + &Delta;<i>W</i>/2
     * <br/>
     * <br/>
     * where &phi;<sub>0</sub> is the probe phase at the gap entrance and
     * <i>W</i><sub>0</sub> is the probe energy at the gap entrance; 
     * 
     * @param probe     probe progating through the gap
     * 
     * @return          mid-gap longitudinal phase coordinates (&phi;<sub>mid</sub>,<i>W</i><sub>mid</sub>)
     *
     * @since  Jan 13, 2015   by Christopher K. Allen
     * 
     * @see #compGapPhaseAndEnergyImpulses(IProbe)
     */
    private EnergyVariables compMidGapPhaseAndEnergy(IProbe probe) {

        // Get the phase and energy at the entrance of the gap
        double  W_i   = probe.getKineticEnergy();
        double  phi_i = this.compGapEntrancePhase(probe);
        
//         TODO :!!!??!?!?!
        
        // Create the longitudinal phase variable object to return
        EnergyVariables varMidGap  = new EnergyVariables();

        // If we are not using the cavity RF phase model we just return the
        //  results of the Panofsky equation
        //  Maybe we have the useRfGapPhaseCalculation flag in the probe??!!
        if (probe.getAlgorithm().useRfGapPhaseCalculation() == false) {
            double phi0 = this.compGapEntrancePhase(probe);
            double dW   = this.compGapEnergyGain(probe);  
            
            double W0   = probe.getKineticEnergy();

            return varMidGap;
            
        } 

        EnergyVariables varDelVals = this.compGapPhaseAndEnergyImpulses(probe);
        double  d_W   = varDelVals.W;
        double  d_phi = varDelVals.phi;
        
        varMidGap.W   = W_i + d_W/2.0;
        varMidGap.phi = phi_i + d_phi/2.0;
        
        return varDelVals;
    }
    
    private EnergyVariables compGapPhaseAndEnergyGain(IProbe probe) {
        
        // If we are not using the cavity RF phase model we just return the
        //  results of the Panofsky equation.  We use the probe phase at the center 
        //  of the (with no impulse), and the Panofsky equation for the energy gain.
        //
        //  Maybe we have the useRfGapPhaseCalculation flag in the probe??!!
        if (probe.getAlgorithm().useRfGapPhaseCalculation() == false) {
            double Q    = Math.abs( probe.getSpeciesCharge() );
            
            double ETL  = this.getETL();
            double phi0 = this.compGapEntrancePhase(probe);   
            
            double dW   = Q * ETL * Math.cos( phi0);  NO
            
            return dW;

            return varMidGap;
            
        } 
    }
    
    /**
     * <p>
     * Computes and returns the longitudinal phase change &delta;&phi; energy 
     * gain &Delta;<i>W</i> through 
     * the gap due to acceleration.  Although this is a thin lens the longitudinal
     * phase must have a impulsive change sympletically conjugate to the change
     * &Delta;<i>W</i> in longitudinal energy. 
     * </p>
     * <p>
     * The calculation is done using a fixed-point search on formulas for the gap
     * phase change &delta;&phi; and energy gain &Delta;<i>W</i> which are taking
     * from Lapostolle and Weiss's <i>Formulae for Linear Accelerators</i> 
     * CERN PS-2000-01. 
     * </p>
     * <p>
     * <h4>CKA NOTES</h4>
     * &middot; The strategy is to compute the mid-gap velocity &beta;<i><sub>mid</sub></i>
     * and phase change &delta;&phi;<i><sub>mid</sub></i>. All other gap parameters can be computed
     * from these values.
     * <br/>
     * <br/>
     * &middot; We start with the phase of the probe at the entrance of the gap, call
     * it &phi;<sub>0</sub>, see <code>{@link #compGapEntrancePhase(IProbe)}</code>.
     * Then all the initial parameters for the loop,
     * i.e., &Delta;<i>W</i><sub>0</sub>, &beta;<sub>0</sub>, <i>T</i>(&beta;<sub>0</sub>),
     * <i>S</i>(&beta;<sub>0</sub>), etc., are computed from that value.
     * <br/>
     * <br/>
     * &middot; The values are computed with the (maybe naive) assumption that the mid-gap
     * phase change &delta;&phi;<i><sub>mid</sub></i> is equal to half the total
     * gap phase change &delta;&phi;, or &delta;&phi;<i><sub>mid</sub></i> = &delta;&phi;/2.
     * <br/>
     * <br/>
     * &middot; We also assume (perhaps more accurately) that the mid-gap energy gain
     * &Delta;<i>W</i><sub><i>mid</i></sub> is half the total energy gain &Delta;<i>W</i>,
     * or &Delta;<i>W</i><sub><i>mid</i></sub> = &Delta;<i>W</i>/2.
     * <br/>
     * <br/>
     * &middot; I am avoiding the use of <code>fitTTFprime</code> and <code>fitSTFPrime</code>
     * because I do not know what values they represent.  That is, are they 
     * <i>dT</i>(&beta;)</i>/<i>d</i>&beta; or <i>dT</i>(&beta;)/<i>dk</i>?
     * </p>
     * 
     * @param probe     probe propagating through gap, we use its phase and energy parameters
     * 
     * @return          the change in phase &delta;&phi; and energy &Delta; <i>W</i> of the 
     *                  give probe through the gap 
     *
     * @author Christopher K. Allen
     * @since  Nov 26, 2014
     */
    private EnergyVariables compMidGapCorrections(IProbe probe) {

        // General parameters
        //        double c = IElement.LightSpeed;

        // Initial probe parameters
        double Q  = Math.abs(probe.getSpeciesCharge());
        double Er = probe.getSpeciesRestEnergy();       
        double bi = probe.getBeta();
        double Wi = probe.getKineticEnergy();

        //
        //  IMPORTANT!!!!
        //
        //  TODO: We need to check that the probe is giving the correct phase here.
        //        The phase must be set with respect to the first gap 
        //
        double phi0 = this.compGapEntrancePhase(probe); // phase at gap "electrical" entrance

        //phase change from center correction factor for future time calculations
        //in PARMILA TTFPrime and SPrime are in [1/cm] units, we use [m]
        //  CKA - I will try to eliminate these quantities because I cannot
        //        determine that they are correct
        //        double ttf_prime = 0.01*this.fitTTFPrime.evaluateAt(bi);
        //        double stf_prime = 0.01*this.fitSTFPrime.evaluateAt(bi);

        // Gap parameters
        double E0  = this.getE0();
        double L   = this.getCellLength();
        double qEL = Q * E0 * L;

        double ttf = this.fitTTF.evaluateAt(bi);        // cosine transit time factor
        double stf = this.fitSTF.evaluateAt(bi);        // sine transit time factor

        // CKA - Now I believe the object is to compute the mid-gap wave number k_mid
        //       and phase change d_phi. All other gap parameters can be computed
        //       from these values.

        // Initialize the search 
        //

        // Starting phase is the phase at gap entrance and
        //  starting energy gain  uses initial phase and beta
//          starting energy gain (mid-gap) uses initial phase and beta
        double phi = phi0;
//        double dW  = qEL*(ttf*Math.cos(phi0) - stf*Math.sin(phi0))/2.0;
        double dW  = qEL*( ttf*Math.cos(phi0) - stf*Math.sin(phi0) );

        // CKA - If we are using the standard definition of S(k) it should be negative
        //      double dE_gap = qEL*(ttf*Math.cos(phi0) + stf*Math.sin(phi0))/2.0; 

        double W_mid = Wi + dW/2.0; 
        double g_mid = W_mid/Er + 1.0;
        double b_mid = Math.sqrt(1.0 - 1.0/(g_mid*g_mid));

        double r_mid = (1/(b_mid*b_mid*g_mid*g_mid*g_mid)); // relativistic effects (energy)

        // The derivative dT(k)/dk occures in Lapostolle's formula, we have T(b)
        //  Thus, we must use dT(k)/dk = db(k)/dk dT[b(k)]/db = -(b/k)T'(b)
        double d_T   = this.fitTTF.derivativeAt(b_mid);
        double d_S   = this.fitSTF.derivativeAt(b_mid);
        double d_phi = -(qEL/Er)*r_mid*b_mid*( d_T*Math.sin(phi) - d_S*Math.cos(phi) );

        int         cntIter   = 0;
        double      dblCnvErr = 10 * DBL_PHASECALC_CNVERR;
        while ( dblCnvErr > DBL_PHASECALC_CNVERR && cntIter < INT_PHASECALC_MAXITER) {

            // Use the newly computed transit time factors and mid gap phase
            //  We use the previous mid-gap beta as an approximation
            phi = phi0 + d_phi/2.0; 
            ttf = this.fitTTF.evaluateAt(b_mid);
            stf = this.fitSTF.evaluateAt(b_mid);

            dW = qEL*( ttf*Math.cos(phi) - stf*Math.sin(phi) );

            // Now that we have the new mid-gap energy gain we compute the new energy parameters
            W_mid = Wi + dW/2.0;
            g_mid = W_mid/Er + 1.0;
            b_mid = Math.sqrt(1.0 - 1.0/(g_mid*g_mid));

            r_mid = (1/(b_mid*b_mid*g_mid*g_mid*g_mid)); // relativistic effects (energy)

            // Update the value for phase change to the gap center
            d_T   = this.fitTTF.derivativeAt(b_mid);
            d_S   = this.fitSTF.derivativeAt(b_mid);
            d_phi = -(qEL/Er)*r_mid*b_mid*( d_T*Math.sin(phi) - d_S*Math.cos(phi) );

            dblCnvErr = Math.abs( d_phi - 2.0*(phi - phi0) );
            cntIter++;
        }

        if (d_phi > DBL_PHASECALC_CNVERR) 
            System.err.println("WARNING! IdealRfGap#compGapPhaseChange() did not converge.");

        return new EnergyVariables(d_phi, dW);
    }
    

//    /**
//     *  <p> 
//     *  Routine to calculate the energy gain along with the phase advance.
//     *  </p>
//     *  <p> 
//     *  A method that is called once by transferMatrix to calculate the energy gain.
//     *  This prevents energy gain calculation from being repeated many times.
//     *  Importantly it provides a workaround from the eneryGain being calculated
//     *  after the upstreamExitPhase is updated elsewhere
//     *  </p>
//     *  <p>
//     *  <h4>CKA NOTES: 11/17/2014</h4>
//     *  &middot; This method is at the heart of some major architectural issues.
//     *  <br/>
//     *  &middot; We have a state-dependent situation, the computed results being
//     *  dependent upon the state of <b>class</b> variables.
//     *  <br/>
//     *  &middot; These class state variables should most likely be local properties
//     *  of the probe objects.
//     *  </p>
//     *
//     * @param  probe  The Parameter
//     */
//    private void compEnergyGain_old(final IProbe probe) {
//        double EL = getE0() * getCellLength();
//
//        // Initial energy parameters
//        double Er = probe.getSpeciesRestEnergy();		
//        double bi = probe.getBeta();
//        //		double gi = probe.getGamma();		
//        double Wi = probe.getKineticEnergy();
//
//        double phi0 = 0.;
//
//        double arrival_time = probe.getTime();
//
//        //the correction for the gap offset needed
//        arrival_time = arrival_time + gapOffset / (bi * IElement.LightSpeed);
//
//        // get phase at the gap center:
//        if(!isFirstGap()) {
//            phi0 = 2. * Math.PI * arrival_time * getFrequency() - FIRST_GAP_PHASE_CORR;
//            double driftTime = probe.getTime() - ((getCellLength()/2.)/(bi * IElement.LightSpeed) + UPSTREAM_EXIT_TIME);
//            int nLabmda = (int) Math.round(2*dblCavModeConst*driftTime*getFrequency());
//            STRUCTURE_PHASE = STRUCTURE_PHASE + Math.PI*nLabmda;
//            phi0 = phi0 + STRUCTURE_PHASE;
//            //phi0 = Math.IEEEremainder(phi0, (2. * Math.PI * (1.0 - dblCavModeConst / 2.0)));
//            setPhase(phi0);
//        }
//        // for first gap use input for phase at the gap center
//        else {
//            STRUCTURE_PHASE = 0.0;
//            FIRST_GAP_PHASE_CORR = 2.0 * Math.PI * arrival_time * getFrequency() - getPhase();
//            phi0 = getPhase();
//        }
//
//        // CKA - theEnergyGain is dangling, cannot be used by getEnergyGain() until this is called 
//        double Q = Math.abs(probe.getSpeciesCharge());
//        theEnergyGain = Q * EL * Math.cos(phi0) * fitTTF.evaluateAt(bi);
//
//        STRUCTURE_PHASE = STRUCTURE_PHASE - (2 - dblCavModeConst) * Math.PI;
//        STRUCTURE_PHASE = Math.IEEEremainder(STRUCTURE_PHASE , (2. * Math.PI));
//
//        //phase change from center correction factor for future time calculations
//        //in PARMILA TTFPrime and SPrime are in [1/cm] units, we use [m]
//        DELTA_PHASE_CORRECTION = 0;
//        double ttf = fitTTF.evaluateAt(bi);
//        double ttf_prime = 0.01*fitTTFPrime.evaluateAt(bi);
//        double stf = fitSTF.evaluateAt(bi);
//        double stf_prime = 0.01*fitSTFPrime.evaluateAt(bi);
//        double freq = getFrequency();
//        //		double phi_gap = phi0;
//        double dE_gap = Q*EL*(ttf*Math.cos(phi0) + stf*Math.sin(phi0))/2.0;
//        double b_gap0 = Math.sqrt(1.-Er*Er/((Er+Wi+dE_gap)*(Er+Wi+dE_gap)));	
//        double k_gap0 = 2*Math.PI*freq/(b_gap0*IElement.LightSpeed);	
//        double gamma_gap = Math.sqrt(1./(1.-b_gap0*b_gap0));
//        double b_gap = b_gap0;
//        double k_gap = k_gap0;
//        double dlt_phi = (Q*EL/(Er*gamma_gap*gamma_gap*gamma_gap*b_gap*b_gap))*k_gap*(ttf_prime*Math.sin(phi0) - stf_prime*Math.cos(phi0))/2.0;
//        for( int i = 0; i < 3; i++){
//            b_gap = Math.sqrt(1.-Er*Er/((Er+Wi+dE_gap)*(Er+Wi+dE_gap)));	
//            k_gap = 2*Math.PI*freq/(b_gap*IElement.LightSpeed);	
//            gamma_gap = Math.sqrt(1./(1.-b_gap*b_gap));
//            dE_gap = Q*EL*((ttf + ttf_prime*(k_gap - k_gap0))*Math.cos(phi0+dlt_phi) + (stf + stf_prime*(k_gap - k_gap0))*Math.sin(phi0+dlt_phi))/2.0;
//            dlt_phi = (Q*EL/(Er*gamma_gap*gamma_gap*gamma_gap*b_gap*b_gap))*k_gap*(ttf_prime*Math.sin(phi0+dlt_phi) - stf_prime*Math.cos(phi0+dlt_phi))/2.0;
//        }
//        //System.out.println("Stop "+this.getId() + "dlt_phi ="+(180*dlt_phi/Math.PI)+" bi="+bi+" b_gap="+b_gap+" dE_gap="+dE_gap+" Wi="+Wi);
//        //the energy gain and phase are known
//        //now we calculate the total energy gain and phase
//        theEnergyGain = Q*EL*((ttf + ttf_prime*(k_gap - k_gap0))*Math.cos(phi0+dlt_phi));
//        DELTA_PHASE_CORRECTION = (Q*EL/(Er*gamma_gap*gamma_gap*gamma_gap*b_gap*b_gap))*k_gap*(ttf_prime*Math.sin(phi0+dlt_phi));		
//    
//        //System.out.println(this.getId() + " " + (Math.IEEEremainder(phi0 * 57.295779, 360.)) + "  " + Wi + "  " + theEnergyGain);
//    }
}

