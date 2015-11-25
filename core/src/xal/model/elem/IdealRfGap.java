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
import xal.model.elem.sync.IRfGap;
import xal.sim.scenario.LatticeElement;
import xal.smf.impl.RfGap;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.RelativisticParameterConverter;
import xal.tools.math.fnc.poly.RealUnivariatePolynomial;

/**
 *  <p>
 *
 *  Represents the action of an ideal RF gap. Gap is modeled as a thin element
 *  whose accelerating action is given by the Panofsky formula. </p> <p>
 *
 *  The gap provides acceleration to the propagation probe as well as
 *  longitudinal focusing and radial defocusing. These mechanisms are
 *  implemented according to that provided by an ideal gap where the effects can
 *  be described analytically. </p>
 *
 * @author     Christopher K. Allen
 * @since      November 22, 2005
 */
public class IdealRfGap extends ThinElement implements IRfGap {

	/*
	 *  Global Attributes
	 */
	/**
	 *  the string type identifier for all IdealRfGap objects
	 */
	public final static String s_strType = "IdealRfGap"; //$NON-NLS-1$

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
	
	/**
	 * Don't know what this is? CKA
	 */
	public static double coeffX = 1.0;

	/**
     * Don't know what this is? CKA
	 */
	public static double coeffY = 1.0;	
	
	/*
	 *  Defining Attributes
	 */
	/**
	 *  ETL product of gap
	 */
	private double m_dblETL = 0.0;

	/**
	 *  phase delay of gap w.r.t. the synchronous particle
	 */
	private double m_dblPhase = 0.0;

	/**
	 *  operating frequency of the gap
	 */
	private double m_dblFreq = 0.0;

	/**
	 *  flag indicating that this is the leading gap of a cavity
	 */
	private boolean initialGap = false;


	/**
	 *  the separation of the gap center from the cell center (m)
	 */
	private double gapOffset = 0.;
	
	/**
	 *  These are kluge jobs for RF cavities.  Very dangerous since
	 *  they are class variables. 
	 */
	private static double firstGapPhaseCorr = 0.;
    /**
     *  These are kluge jobs for RF cavities.  Very dangerous since
     *  they are class variables. 
     */
	private static double structurePhase = 0.;
    /**
     *  These are kluge jobs for RF cavities.  Very dangerous since
     *  they are class variables. 
     */
	private static double upstreamExitTime = 0.;

  /** the phase kick correction applied at the gap center [rad]*/
  static private double deltaPhaseCorrection = 0.;

	/**
	 *  the on axis accelerating field (V)
	 */
	private double E0 = 0.;

	/**
	 *  the accelerating cell length
	 */
	private double cellLength = 0.;

	/**
	 *  the energy gained in this gap (eV)
	 */
	private double theEnergyGain = 0.;

	/**
	 *  = 0 if the gap is part of a 0 mode cavity structure (e.g. DTL) = 1 if the
	 *  gap is part of a pi mode cavity (e.g. CCL, Super-conducting)
	 */
	private double structureMode = 0.;

	/**
	 *  fit of the TTF vs. beta
	 */
	private RealUnivariatePolynomial TTFFit;

	/**
	 *  fit of the TTF-prime vs. beta
	 */
	private RealUnivariatePolynomial TTFPrimeFit;

	/**
	 *  fit of the S factor vs. beta
	 */
	private RealUnivariatePolynomial SFit;

	/**
	 *  fit of the S-prime vs. beta
	 */
	private RealUnivariatePolynomial SPrimeFit;

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
	 *  IRfGap Interface
	 */
	/**
	 *  Return the ETL product of the gap, where E is the longitudinal electric
	 *  field, T is the transit time factor, and L is the gap length.
	 *
	 *@return    the ETL product of the gap (in <b>volts</b> ).
	 */
	public double getETL() {
		return m_dblETL;
	}

	/**
	 *  Return the RF phase delay of the gap with respect to the synchronous
	 *  particle.
	 *
	 *@return    phase delay w.r.t. synchronous particle (in <b>radians</b> ).
	 */
	public double getPhase() {
		return m_dblPhase;
	}

	/**
	 *  Get the operating frequency of the RF gap.
	 *
	 *@return    frequency of RF gap (in <b>Hertz</b> )
	 */
	public double getFrequency() {
		return m_dblFreq;
	}


	/**
	 *  return wheteher this gap is the initial gap of a cavity
	 *
	 *@return    The firstGap value
	 */
	public boolean isFirstGap() {
		return initialGap;
	}

	/**
	 *  Set the ETL product of the RF gap where E is the longitudinal electric
	 *  field of the gap, T is the transit time factor of the gap, L is the length
	 *  of the gap. <p>
	 *
	 *  The maximum energy gain from the gap is given by qETL where q is the charge
	 *  (in coulombs) of the species particle.
	 *
	 *@param  dblETL  ETL product of gap (in <b>volts</b> ).
	 */
	public void setETL(double dblETL) {
		m_dblETL = dblETL;
	}

	/**
	 *  Set the phase delay of the RF in gap with respect to the synchronous
	 *  particle. The actual energy gain from the gap is given by qETLcos(dblPhi)
	 *  where dbkPhi is the phase delay.
	 *
	 *@param  dblPhase  phase delay of the RF w.r.t. synchonouse particle (in
	 *      <b>radians</b> ).
	 */
	public void setPhase(double dblPhase) {
		m_dblPhase = dblPhase;
	}

	/**
	 *  Set the operating frequency of the RF gap.
	 *
	 *@param  dblFreq  frequency of RF gap (in <b>Hertz</b> )
	 */
	public void setFrequency(double dblFreq) {
		m_dblFreq = dblFreq;
	}

	/**
	 *  Set the on accelerating field @ param E - the on axis field (V/m)
	 *
	 *@param  E  The new e0 value
	 */
	public void setE0(double E) {
		E0 = E;
	}

	/**
	 *  Get the on accelerating field (V/m)
	 *
	 *@return    The e0 value
	 */
	public double getE0() {
		return E0;
	}

	/**
	 *  return the cell length (m)
	 *
	 *@return    The cellLength value
	 */
	public double getCellLength() {
		return cellLength;
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
    
	/*
	 *  IElement Interface
	 */

	/**
	 *  Returns the time taken for the probe to propagate through element.
	 *
	 *@param  probe  propagating probe
	 *@return        value of zero
	 */
	@Override
    public double elapsedTime(IProbe probe) {

		// Initial energy parameters
		double Er = probe.getSpeciesRestEnergy();
		double Wi = probe.getKineticEnergy();
		double bi = probe.getBeta();
//		double gi = probe.getGamma();
		double dW = this.energyGain(probe);

		// Final energy parameters
		double Wf = Wi + dW;
		double gf = Wf / Er + 1.0;
		double bf = Math.sqrt(1.0 - 1.0 / (gf * gf));

		// update the elapsed time to account for the phase correction term
		double dT = ((deltaPhaseCorrection) / (this.getFrequency() * 2.0 * Math.PI));

		//the gap offset correction
		dT = dT + gapOffset * (1. / (bi * IElement.LightSpeed) - 1. / (bf * IElement.LightSpeed));

		//the time when probe will exit this gap
		upstreamExitTime = probe.getTime() + dT + (getCellLength()/2.) / (bf * IElement.LightSpeed);

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
	 *@param  probe  uses the particle species charge
	 *@return        energy gain for this probe (<b>in electron-volts</b> )
	 */

	@Override
    public double energyGain(IProbe probe) {
		return theEnergyGain;
	}

	/**
	 *  Routine to calculate the energy gain along with the phase advance. 
	 *  A
	 *  method that is called once by transferMatrix to calculate the energy gain.
	 *  This prevents energy gain calculation from being repeated many times.
	 *  Importantly it provides a workaround from the eneryGain being calculated
	 *  after the upstreamExitPhase is updated elsewhere
	 *
	 * @param  probe  The Parameter
	 */
	private void compEnergyGain(IProbe probe) {
		double EL = getE0() * getCellLength();

		// Initial energy parameters
		double Er = probe.getSpeciesRestEnergy();		
		double bi = probe.getBeta();
//		double gi = probe.getGamma();		
		double Wi = probe.getKineticEnergy();
		
		double phi0 = 0.;

		double arrival_time = probe.getTime();

		//the correction for the gap offset needed
		arrival_time = arrival_time + gapOffset / (bi * IElement.LightSpeed);

		// get phase at the gap center:
		if(!isFirstGap()) {
			phi0 = 2. * Math.PI * arrival_time * getFrequency() - firstGapPhaseCorr;
			double driftTime = probe.getTime() - ((getCellLength()/2.)/(bi * IElement.LightSpeed) + upstreamExitTime);
			int nLabmda = (int) Math.round(2*structureMode*driftTime*getFrequency());
			structurePhase = structurePhase + Math.PI*nLabmda;
			phi0 = phi0 + structurePhase;
			//phi0 = Math.IEEEremainder(phi0, (2. * Math.PI * (1.0 - structureMode / 2.0)));
			setPhase(phi0);
		}
		// for first gap use input for phase at the gap center
		else {
			structurePhase = 0.;
			firstGapPhaseCorr = 2. * Math.PI * arrival_time * getFrequency() - getPhase();
			phi0 = getPhase();
		}

		double Q = Math.abs(probe.getSpeciesCharge());
		theEnergyGain = Q * EL * Math.cos(phi0) * TTFFit.evaluateAt(bi);

		structurePhase = structurePhase - (2 - structureMode) * Math.PI;
		structurePhase = Math.IEEEremainder(structurePhase , (2. * Math.PI));
		
		//phase change from center correction factor for future time calculations
		//in PARMILA TTFPrime and SPrime are in [1/cm] units, we use [m]
		deltaPhaseCorrection = 0;
		double ttf = TTFFit.evaluateAt(bi);
		double ttf_prime = 0.01*TTFPrimeFit.evaluateAt(bi);
		double stf = SFit.evaluateAt(bi);
		double stf_prime = 0.01*SPrimeFit.evaluateAt(bi);
		double freq = getFrequency();
//		double phi_gap = phi0;
		double dE_gap = Q*EL*(ttf*Math.cos(phi0) + stf*Math.sin(phi0))/2.0;
		double b_gap0 = Math.sqrt(1.-Er*Er/((Er+Wi+dE_gap)*(Er+Wi+dE_gap)));	
		double k_gap0 = 2*Math.PI*freq/(b_gap0*IElement.LightSpeed);	
		double gamma_gap = Math.sqrt(1./(1.-b_gap0*b_gap0));
		double b_gap = b_gap0;
		double k_gap = k_gap0;
		double dlt_phi = (Q*EL/(Er*gamma_gap*gamma_gap*gamma_gap*b_gap*b_gap))*k_gap*(ttf_prime*Math.sin(phi0) - stf_prime*Math.cos(phi0))/2.0;
    for( int i = 0; i < 3; i++){
			b_gap = Math.sqrt(1.-Er*Er/((Er+Wi+dE_gap)*(Er+Wi+dE_gap)));	
			k_gap = 2*Math.PI*freq/(b_gap*IElement.LightSpeed);	
			gamma_gap = Math.sqrt(1./(1.-b_gap*b_gap));
			dE_gap = Q*EL*((ttf + ttf_prime*(k_gap - k_gap0))*Math.cos(phi0+dlt_phi) + (stf + stf_prime*(k_gap - k_gap0))*Math.sin(phi0+dlt_phi))/2.0;
			dlt_phi = (Q*EL/(Er*gamma_gap*gamma_gap*gamma_gap*b_gap*b_gap))*k_gap*(ttf_prime*Math.sin(phi0+dlt_phi) - stf_prime*Math.cos(phi0+dlt_phi))/2.0;
		}
		//System.out.println("Stop "+this.getId() + "dlt_phi ="+(180*dlt_phi/Math.PI)+" bi="+bi+" b_gap="+b_gap+" dE_gap="+dE_gap+" Wi="+Wi);
		//the energy gaine and phase are known
		//now we calculate the total energy gain and phase
		theEnergyGain = Q*EL*((ttf + ttf_prime*(k_gap - k_gap0))*Math.cos(phi0+dlt_phi));
		deltaPhaseCorrection = (Q*EL/(Er*gamma_gap*gamma_gap*gamma_gap*b_gap*b_gap))*k_gap*(ttf_prime*Math.sin(phi0+dlt_phi));		
		
		//System.out.println(this.getId() + " " + (Math.IEEEremainder(phi0 * 57.295779, 360.)) + "  " + Wi + "  " + theEnergyGain);
	}

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

//    	System.out.println("This is " + this.getId());
//    	System.out.println("E0 is   " + this.getE0());
//    	System.out.println("ETL is  " + this.getETL());
//    	System.out.println("");
    	

		// Get probe parameters at initial energy
		double Er = probe.getSpeciesRestEnergy();
		double Wi = probe.getKineticEnergy();
		double bi = probe.getBeta();
		double gi = probe.getGamma();

// Determine the current energy gain and focusing constants for the gap

		// the following section is to calculate the phase of the beam at each gap, rather than use hardwired phases.

// update the energy gain first:
		if(probe.getAlgorithm().getRfGapPhaseCalculation()) {
			compEnergyGain(probe);
		} else {
			simpleEnergyGain(probe);
		}

		double dW = this.energyGain(probe);

		double kz = this.compLongFocusing(probe);
		double kt = this.compTransFocusing(probe);

		// Compute final energy parameters
		double Wf = Wi + dW;
		double gf = Wf / Er + 1.0;
		double bf = Math.sqrt(1.0 - 1.0 / (gf * gf));

		// Compute average energy parameters
//		double Wb = (Wf + Wi) / 2.0;
//		double gb = Wb / Er + 1.0;
//        double bg = Math.sqrt(1.0 - 1.0/(gb*gb));


		// Compute component block matrices then full transfer matrix
		double arrTranX[][] = new double[][]{{1.0, 0.0}, {kt*coeffX / (bf * gf), bi * gi / (bf * gf)}};
		double arrTranY[][] = new double[][]{{1.0, 0.0}, {kt*coeffY / (bf * gf), bi * gi / (bf * gf)}};
		
		// CKA - Corrected 7/14/2010
		//  Additional factor gbar^2 in the longitudinal focusing term 
//		double arrLong[][] = new double[][]{{1.0, 0.0}, {(kz / (bf * gf)) * gb * gb / (gf * gf), gi * gi * gi * bi / (gf * gf * gf * bf)}};
        double arrLong[][] = new double[][]{{1.0, 0.0}, { kz / (bf * gf * gf * gf), gi * gi * gi * bi / (gf * gf * gf * bf)}};

		PhaseMatrix matPhi = new PhaseMatrix();

		matPhi.setElem(6, 6, 1.0);
		matPhi.setSubMatrix(0, 1, 0, 1, arrTranX);
		matPhi.setSubMatrix(2, 3, 2, 3, arrTranY);
		matPhi.setSubMatrix(4, 5, 4, 5, arrLong);

		// Do the phase update if this is desired:
		// do it here to resuse the bi, bf, etc. factors
//	if(probe.getAlgorithm().useRfGapPhaseCalculation()) advancePhase(probe);

//        PrintWriter os = new PrintWriter(System.out);
//        matPhi.print(os);
//        os.close();

		return new PhaseMap(matPhi);
	}


	/*
	 *  Support Methods
	 */
	/**
	 *  Compute the energy gain of the RF gap for a probe assuming a fixed default
	 *  phase at the gap center.
	 *
	 *@param  probe  uses the particle species charge
	 *@return        energy gain for this probe (<b>in electron-volts</b> )
	 */
	public double simpleEnergyGain(IProbe probe) {
//
//        System.out.println("simpleEnergyGain()");
//
		double ETL = this.getETL();
		double Q = Math.abs(probe.getSpeciesCharge());
		double phi = this.getPhase();
		return theEnergyGain = Q * ETL * Math.cos(phi);
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
	 *  Testing and Debugging
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
		os.println("  Axial field E0     : " + this.getE0() );
	}
	
	/**
	 * Conversion method to be provided by the user
	 * 
	 * @param element the SMF node to convert
	 */
	@Override
	public void initializeFrom(LatticeElement element) {
		super.initializeFrom(element);		
		RfGap rfgap = (RfGap) element.getHardwareNode();
		
	    // Initialize from source values
	    initialGap = rfgap.isFirstGap();
	    cellLength = rfgap.getGapLength();
	    gapOffset = rfgap.getGapOffset();
	    TTFPrimeFit = rfgap.getTTFPrimeFit();
	    TTFFit = rfgap.getTTFFit();	
	    SPrimeFit = rfgap.getSPrimeFit();
	    SFit = rfgap.getSFit();
	    structureMode = rfgap.getStructureMode();
	}
}

