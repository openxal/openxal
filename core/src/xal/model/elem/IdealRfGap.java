/*
 *  IdealRfGap.java
 *
 *  Created on October 22, 2002, 1:58 PM
 */
package xal.model.elem;

import java.io.PrintWriter;

import xal.model.IElement;
import xal.model.IModelDataSource;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.source.RfGapDataSource;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.RelativisticParameterConverter;
import xal.tools.math.poly.UnivariateRealPolynomial;

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
 *@author     Christopher K. Allen
 *@created    November 22, 2005
 */
public class IdealRfGap extends ThinElement implements IRfGap {

    
    
	/*
	 *  Global Constants
	 */
	/**
	 *  the string type identifier for all IdealRfGap objects
	 */
	public final static String s_strType = "IdealRfGap";

	/**
	 *  Parameters for XAL MODEL LATTICE dtd
	 */
	public final static String s_strParamETL = "ETL";
	/**
	 *  Description of the Field
	 */
	public final static String s_strParamPhase = "Phase";
	/**
	 *  Description of the Field
	 */
	public final static String s_strParamFreq = "Frequency";
    
    

//	/**
//	 * flag to calculate Emittance Growth factor (from Trace3D)
//	 */
//	private static final boolean calcPhaseSpread = true;
 
	private static final boolean debugT3d = false;

    
    
	/*
	 *  Defining Attributes
	 */
    
    /**
     *  flag indicating that this is the leading gap of a cavity
     */
    private boolean initialGap = false;

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

    
    /*********************************
     *  
     * CKA: This should be a property of the probe. Not a static variable in the element?
     * <br/>
     * Moreover, it's never modified.  It retains the value 0 throughout it's existence so
     * it's not really used anyway. It appears to have been used in the past but commented
     * out in the method elapsedTime(IProbe).
     */
	
    /** Holder for the upstream gap phase. This is used when dynamically 
    * determining the phase advance from gap to gap, using the elapsed
    * time attribute from the probe. You still need this info, to 
    * determine what phase to "slip" from [rad] */
    static private double upstreamExitPhase = 0.;
    
    /** CKA: This should be a property of the probe. Not a static variable in the element? */
    /** the time the probe leaves the upstream gap. Used to 
    * calculate the phasew advance when gaps have drifts between them.
    */
    static private double upstreamExitTime = 0.;   
    
    /** the separation of the gap center from the cell center (m) */
    private double gapOffset = 0.;
    
    /** the on axis accelerating field (V) */
    private double E0 = 0.;
    
    /** the accelerating cell length  */
    private double cellLength = 0.;

    /** CKA: Why is phi0 a class variable?  It's used only locally in transferMap(). */
    /** the phase at the cell center (used when calculating the phase advance) */
    private double phi0 = 0.;
    
    /** the energy gained in this gap (eV) */
    private double theEnergyGain = 0.;
    
    /**  = 0 if the gap is part of a 0 mode cavity structure (e.g. DTL)
    * = 1 if the gap is part of a pi mode cavity (e.g. CCL, Superconducting)
    */
    private double structureMode =0;
    
    /** fit of the TTF vs. beta */
    private UnivariateRealPolynomial TTFFit;
    
   /** fit of the TTF-prime vs. beta */
    private UnivariateRealPolynomial TTFPrimeFit;

    /** fit of the S factor vs. beta */
    private UnivariateRealPolynomial SFit;
    
   /** fit of the S-prime vs. beta */
    private UnivariateRealPolynomial SPrimeFit;

    
    
    
    /*
     * Initialization 
     */
    
    
    /** 
     *  Creates a new instance of IdealRfGap 
     *
     *  @param  strId       instance identifier of element
     *  @param  dblETL      field/transit time/length factor for gap (in <b>volts</b>)
     *  @param  dblPhase    operating phase of gap (in <b>radians</b>)
     *  @param  dblFreq     operating RF frequency of gap (in <b>Hertz</b>)
     */
    public IdealRfGap(String strId, double dblETL, double dblPhase, double dblFreq) {
        super(s_strType, strId);
        
        this.setETL(dblETL);
        this.setPhase(dblPhase);
        this.setFrequency(dblFreq);
    };
    
    /** 
     *  JavaBean constructor - creates a new unitialized instance of IdealRfGap
     *
     *  <b>BE CAREFUL</b>
     */
    public IdealRfGap() {
        super(s_strType);
    };
    
    


    
    /**
     * <p> 
     * return whether this gap is the initial gap of a cavity
     * </p>
     * <p>
     * <h4>CKA NOTES:</h4>
     * - This is a very brittle mechanism for identifying cavities and must be
     * refactored.  Cavities should be containers of <code>IdealRfGap</code> objects,
     * not implied by an attribute on an RF gap.  
     * <br/>
     * <br/>
     * - The worst part of this architecture is that it requires the algorithm to maintain
     * <strong>static</strong> variables to keep track of the "cavities" during simulation.
     * </p>
     * 
     * @return  returns the <i>am I the first gap of an RF cavity?</i> flag
     */
    public boolean isFirstGap() { return initialGap;}
     
    
//    public boolean getCalcPhaseSpread() {
//        return calcPhaseSpread;
//    }
     

    /*
     *  IRfGap Interface
     */
    
     /**  Return the ETL product of the gap, where E is the longitudinal electric field, T is the
      *  transit time factor, and L is the gap length.
      *
      *  @return     the ETL product of the gap (in <bold>volts</bold>).
      */
    public double getETL() { return m_dblETL; };
     
    /**  
     *  Return the RF phase delay of the gap with respect to the synchonous particle.
     *
     *  @return     phase delay w.r.t. synchonous particle (in <bold>radians</bold>).
     */
     public double getPhase() { return m_dblPhase; };

    /**  
     * Get the operating frequency of the RF gap.
     *
     *  @return  frequency of RF gap (in <bold>Hertz</bold>)
     */
    public double getFrequency() { return m_dblFreq; };
     
     
     /**  
     * Set the ETL product of the RF gap where
     *      E is the longitudinal electric field of the gap,
     *      T is the transit time factor of the gap,
     *      L is the length of the gap.
     * <p>
     * The maximum energy gain from the gap is given by qETL where q is the charge
     * (in coulombs) of the species particle.
     *
     *  @param  dblETL  ETL product of gap (in <bold>volts</bold>).
     */
    public void setETL(double dblETL) { m_dblETL = dblETL; };
     
    /**  Set the phase delay of the RF in gap with respect to the synchronous particle.
     *  The actual energy gain from the gap is given by qETLcos(dblPhi) where dbkPhi is
     *  the phase delay.
     *
     *  @param  dblPhase    phase delay of the RF w.r.t. synchronous particle (in <bold>radians</bold>).
     */
    public void setPhase(double dblPhase)   { m_dblPhase = dblPhase; };
     
    /**  Set the operating frequency of the RF gap.
     *
     *  @param dblFreq  frequency of RF gap (in <bold>Hertz</bold>)
     */
    public void setFrequency(double dblFreq) { m_dblFreq = dblFreq; };
     
    /** Set the on accelerating field
    * @param E - the on axis field (V/m)
    */
    public void setE0(double E) { E0 = E;}
     /** Get the on accelerating field (V/m)
    */   
    public double getE0() {return E0;}
    
    /** return the cell length (m) */
    public double getCellLength() { return cellLength;}
    
    
    
    /*
     *  IElement Interface
     */
    
    /**
     *
     * @see xal.model.elem.Element#initializeFrom(xal.model.IModelDataSource)
     *
     * @author Christopher K. Allen
     * @since  Nov 6, 2013
     */
    @Override
    public void initializeFrom(IModelDataSource source) throws ModelException {
        
        // Check argument and throw exception if not of expected type
        if (! (source instanceof RfGapDataSource) )
            throw new IllegalArgumentException("Expected instance of RfGapDataSource, got: " + source.getClass().getName());
        RfGapDataSource sourceGap = (RfGapDataSource) source;
        
        // Initialize from source values
        initialGap = sourceGap.isFirstGap();
        cellLength = sourceGap.getGapLength();
        gapOffset = sourceGap.getGapOffset();
        TTFPrimeFit = sourceGap.getTTFPrimeFit();
        TTFFit = sourceGap.getTTFFit();	
        SPrimeFit = sourceGap.getSPrimeFit();
        SFit = sourceGap.getSFit();
        structureMode = sourceGap.getStructureMode();
    }
    
    /**
     * Returns the time taken for the probe to propagate through element.
     * 
     *  @param  probe   propagating probe
     *  
     *  @return         value of zero 
     */
    @Override
    public double elapsedTime(IProbe probe)  {
//        double deltaPhaseCorrection;
//        double entrancePhase;
//        double dPhi1;
//        double dPhi2;
//        
//        // Initial energy parameters
//        double Er = probe.getSpeciesRestEnergy();
//        double Wi = probe.getKineticEnergy();
//        double bi = probe.getBeta();
//        double gi = probe.getGamma();
//        double dW = this.energyGain(probe);
//        
//        // Final energy parameters
//        double Wf = Wi + dW;
//        double gf = Wf/Er + 1.0;
//        double bf = Math.sqrt(1.0 - 1.0/(gf*gf));
//        
//        double b0 = (bi + bf)/2.;
//        double g0 = (gi + gf)/2.;
//        
//        // Phase change from drift from start of cell to center of cell
//        //  gapOffsets have to do with electrical centers != geometric centers, 
//        //  e.g. DTLs (small effect).
//        if(!isFirstGap()) {
//            entrancePhase = upstreamExitPhase;
//            dPhi1 = 2. * Math.PI * (getCellLength()/2. + gapOffset) * getFrequency() /(bi * IElement.LightSpeed);
//        }
//        else {
//            // phase is prescibed at the center for this guy
//            dPhi1 = 0.;
//            entrancePhase = getPhase();
//        }   
//        
//        // phase change from center correction factor:
//        
//        //old T.owens way (uses simple "constant T'" scaling with beta)
//        //deltaPhaseCorrection = -2. * Math.PI * getFrequency() * getE0() * Math.pow(getCellLength(), 2.) / (Math.pow(b0, 3.) * Math.pow(g0, 3.) * Er * IElement.LightSpeed) * (1. + structureMode) * Math.sin(phi0) * TTFPrimeFit.evaluateAt(b0);
//        
//        double ETL = getETL();
//        
//        //old deltaPhaseCorrection = -2. * Math.PI * getE0() * getCellLength() / (Math.pow(b0, 2.) * Math.pow(g0, 3.) * Er) * Math.sin(phi0) * TTFPrimeFit.evaluateAt(b0);	
//        //new 
//        deltaPhaseCorrection = -2. * Math.PI * ETL / ((b0*b0) * (g0*g0*g0) * Er) * Math.sin(phi0) * TTFPrimeFit.evaluateAt(b0);	
//        
//        // phase change from drift from center to cell exit:
//        dPhi2 = 2. * Math.PI * (getCellLength()/2. - gapOffset) * getFrequency() /(bf * IElement.LightSpeed);
//        
//        upstreamExitPhase = entrancePhase + dPhi1 + dPhi2 + deltaPhaseCorrection - (2 - structureMode) * Math.PI;
//        
//        upstreamExitTime = probe.getTime() + getCellLength()/(2. * bf * IElement.LightSpeed) + deltaPhaseCorrection/ (getFrequency() * 2 *Math.PI);
//        // update the elapsed time to account for the phase correction term
////      probe.setTime(deltaPhaseCorrection/(getFrequency() * 2. * Math.PI));
//        double dT = ( deltaPhaseCorrection/(this.getFrequency() * 2.0 *Math.PI) );
        
		double deltaPhaseCorrection;

		// Initial energy parameters
		double Er = probe.getSpeciesRestEnergy();
		double Wi = probe.getKineticEnergy();
		double bi = probe.getBeta();
		double gi = probe.getGamma();
		double dW = this.energyGain(probe);

		// Final energy parameters
		double Wf = Wi + dW;
		double gf = Wf / Er + 1.0;
		double bf = Math.sqrt(1.0 - 1.0 / (gf * gf));

		deltaPhaseCorrection = 0;
		// phase change from center correction factor:
		double V0 = getE0() * getCellLength();
		double bTb = bi * TTFFit.evaluateDerivativeAt(bi);
		double sin_phase = getPhase();
		double sinPhi = Math.sin(sin_phase);
		deltaPhaseCorrection = -(V0 / (gi * gi * gi * bi * bi * Er)) * bTb * sinPhi;

		// update the elapsed time to account for the phase correction term
		double dT = ((deltaPhaseCorrection) / (this.getFrequency() * 2.0 * Math.PI));

		//the gap offset correction
		dT = dT + gapOffset * (1. / (bi * IElement.LightSpeed) - 1. / (bf * IElement.LightSpeed));

		//the time when probe will exit this gap
		upstreamExitTime = probe.getTime() + dT + (getCellLength()/2.) / (bf * IElement.LightSpeed);

//    	System.out.println("dT = " + dT);
        return dT;
    }
    
//    /** the interface method to provide the energy gain.
//    * since this calculation has gotten complicated it is done 
//    * in the TransferMap method and the answer is returned here. */
//    
//    public double energyGain(IProbe probe) { return energyGain;}   
// 

    /** 
      * Compute the energy gain of the RF gap for a probe including the effects 
      * of calculating the phase advance.
      *
      * the interface method to provide the energy gain.
      * since this calculation has gotten complicated it is done 
      * in the TransferMap method and the answer is returned here.
      * 
      * CKA NOTES:
      * - This is a very dangerous way to implement this IElement requirement.
      * You must be sure that the transferMap() method is called first - thus
      * we have state dependence where none was assumed.
      * 
      * - Java convension suggests that energyGain() is a computed parameter to
      * begin with.
      *  @param  probe  uses the particle species charge
      *
      * @return         energy gain for this probe (<b>in electron-volts</b>)
      */
      
    @Override
    public double energyGain(IProbe probe) {
        return theEnergyGain;
    }
    
    
    /**
     * CKA:  The variable phi0 is computed incorrectly.  I don't know why, but I assume it
     * has something to do with the method of monitoring the time of flight between gaps
     * (and RF phase advance during that time).  A more robust technique of doing this would probably
     * involve using the probe itself to do this; elements should not be "aware" of each other, a 
     * consequence of this static variable method.  In the Element/Algorithm/Probe architecture,
     * it is the job  of the probe to be aware of the modeling elements.
     * 
     */
    /** 
     * Routine to calculate the energy gain along with the phase advanve     
     * a method that is called once by transferMatrix to calculate the energy gain.
     * This prevents energy gain calculation from being repeated many times.
     * Importantly it provides a workaround from the eneryGain being calculated
     * after the upstreamExitPhase is updated elsewhere
     */
    private void compEnergyGain(IProbe probe) {
//      double ELOld = getE0() * getCellLength();
        double EL = getETL();
        //
        
        double bh, gh, factor, dPhiDriftTime, dPhiDriftPhase;
        double dPsi = 0.;
        double dPhi1 = 0.;
        double dPhiGapOffset = 0.;
        double bc;  // the central beta
        
        // Initial energy parameters
        double Er = probe.getSpeciesRestEnergy();
//      double Wi = probe.getKineticEnergy();
        double bi = probe.getBeta();
        double gi = probe.getGamma();
        
        // get phase at the gap center:
        if(!isFirstGap()) {
            // phase change from drifting beam:
            dPhi1 = 2. * Math.PI * (getCellLength()/2.) * getFrequency() /(bi * IElement.LightSpeed);
            dPhiGapOffset = 2. * Math.PI * gapOffset * getFrequency() /(bi * IElement.LightSpeed);
            // phase slip from any possible drifts between this gap and previous
            dPhiDriftTime = (probe.getTime() - dPhi1/(getFrequency() * 2 * Math.PI)  - upstreamExitTime);
            
            dPhiDriftPhase = 2. * Math.PI * Math.IEEEremainder(dPhiDriftTime * getFrequency(), (1.  - structureMode/2.) );
            
            //phi0 = upstreamExitPhase  + dPhi1 + dPhiDriftPhase + dPhiGapOffset + structureMode * Math.PI;
            phi0 = upstreamExitPhase  + dPhi1 + dPhiDriftPhase + dPhiGapOffset;
            // correction factor for center (needs iteration)
            // crude iteration: to "solve" for phi0
            if(structureMode < 0) {
                for (int i=0; i < 2; i++) {
                    bc = bi + EL/(2. * bi * (gi*gi*gi) * Er) * (TTFFit.evaluateAt(bi) * Math.cos(phi0) +  SFit.evaluateAt(bi)*Math.sin(phi0));
                    bh = (bi + bc)/2.;
                    gh = Math.sqrt(1./(1. - bh*bh));
                    factor = (Math.sin(phi0) * TTFPrimeFit.evaluateAt(bi) + Math.cos(phi0) * SPrimeFit.evaluateAt(bi));
                    dPsi = -Math.PI * EL /((bh*bh) * (gh*gh*gh) * Er)* (1+structureMode) * factor;
                    phi0 = upstreamExitPhase  + dPhi1 +  dPhiDriftPhase + dPsi + dPhiGapOffset;
                }
            }
            
            setPhase(phi0); // set the phase to that used at the gap center
            
        }
        // for first gap use input for phase at the gap center
        else {
            phi0 = this.getPhase();
        }
        bc = bi + EL/(2. * bi * (gi*gi*gi) * Er) * (TTFFit.evaluateAt(bi) * Math.cos(phi0) +  SFit.evaluateAt(bi)*Math.sin(phi0));             
        double Q   = Math.abs( probe.getSpeciesCharge() );
        
        theEnergyGain = Q*EL*Math.cos(phi0) * TTFFit.evaluateAt(bc);
        System.out.println("theEnergyGain = " + theEnergyGain + ", EL = " + EL + ", TTF = " + TTFFit.evaluateAt(bc) + ", bc = " + bc);
        
        //this means phi0 = 0 has max. acceleartion
        
        /* old method */
        /*
         double bg = (1 + structureMode) * getCellLength() * getFrequency()/IElement.LightSpeed;
         
         double fac = 2. * Math.PI*(bg/bc - 1.) * TTFPrimeFit.evaluateAt(bc);
         theEnergyGain = Q*EL*Math.cos(phi0) * (TTFFit.evaluateAt(bc) - fac);
         */
    }
    
    /**
     * <p>  
     * Compute the transfer map for an ideal RF gap.
     * </p
     * <p>
     * New transfer matrix with same definitions of <i>k<sub>r</sub></i> and <i>k<sub>z</sub></i>
     * from Trace3D manual, but correctly considering XAL and trace3d longitudinal phase. 
     * transformation
     * </p>
     * <p>
     * Modified on 21 Jul 06 Sako (consistency checked with Trace3D).
     * </p>
     * 
     *  @param  probe       compute transfer map using parameters from this probe
     *
     *  @return             transfer map for the probe
     *
     *  @exception  ModelException  this should not occur
     */
    @Override
    protected PhaseMap transferMap(IProbe probe) throws ModelException {

        // Determine the current energy gain and focusing constants for the gap
        // the following section is to calculate the phase of the beam at each gap, rather than use hardwired phases.
        // update the energy gain first:
        if ( probe.getAlgorithm().useRfGapPhaseCalculation() ) { 
            compEnergyGain(probe);
        } else {
        	compEnergyGain(probe);
//            simpleEnergyGain(probe);
        }
        
        // Get probe parameters at initial energy
        double Er = probe.getSpeciesRestEnergy();
        double Wi = probe.getKineticEnergy();
        double bi = probe.getBeta();
        double gi = probe.getGamma();
//        double gi_2 = gi*gi;
        
        double dW = this.energyGain(probe);
        
        // Compute final energy parameters
        double Wf = Wi + dW;
        double gf = Wf/Er + 1.0;//gamma final
        double bf = Math.sqrt(1.0 - 1.0/(gf*gf));//beta
//        double gf_2 = gf*gf;

        // Compute average energy parameters (average)
//        double ga = this.gammaMidGap(probe);
        
        // Compute the momentum reduction
//        double  bgf = bf*gf;
//        double  bgi = bi*gi;
//        double  eta = bgi/bgf;

        // Compute the focusing constants
        double kt;      // transverse focusing
        double kz;      // longitudinal focusing
           
//        double ps  = this.getPhase();
//        double sin = Math.sin(ps);

//        kt = this.compTransFocusing(probe) * sin;
//        kz = -2.0*kt*ga*ga;
        kt = this.compTransFocusing(probe);
        kz = this.compLongFocusing(probe);

        if (debugT3d) {
    		System.out.println("bgin, bgf = "+bi*gi+" "+bf*gf);
    	}

//        double arrTran[][] = new double[][] {{1.0, 0.0}, {kt, eta }};
//        double arrLong[][] = new double[][] {{1.0, 0.0}, {kz/(gf_2), eta*gi_2/gf_2}};
		double arrTran[][] = new double[][]{{1.0, 0.0}, {kt / (bf * gf), bi * gi / (bf * gf)}};
		double arrLong[][] = new double[][]{{1.0, 0.0}, {(kz / (bf * gf)) , gi * bi / (gf * bf)}};
        
        PhaseMatrix matPhi = new PhaseMatrix();
        
        matPhi.setElem(6,6, 1.0);
        matPhi.setSubMatrix(0,1, 0,1, arrTran);
        matPhi.setSubMatrix(2,3, 2,3, arrTran);
        matPhi.setSubMatrix(4,5, 4,5, arrLong);
        
        return new PhaseMap(matPhi);
    }




    /*
     * Support Methods
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
     * Compute and return the mid-gap relativistic gamma function for the
     * given probe.
     * 
     * NOTE:
     * - Because of the state-dependendant nature of the energy calculations
     * (this needs to be fixed), this function will only work correctly
     * if the function energyGain() is consistent.
     * 
     * @param   probe   probe containing energy information
     * 
     * @return  average or "mid-gap" gamma value
     * 
     * @see IdealRfGap#energyGain(IProbe)
     */
    public double   gammaMidGap(IProbe probe)   {
        
        // Get probe parameters at initial energy
        double Er = probe.getSpeciesRestEnergy();
        double Wi = probe.getKineticEnergy();
        double dW = this.energyGain(probe);
        double Wa = Wi + dW/2.0;
        
        double gamma = RelativisticParameterConverter.computeGammaFromEnergies(Wa, Er);
        
        return gamma;
    }
    
    /**
     * Compute and return the final relativistic gamma function for the
     * given probe.
     * 
     * NOTE:
     * - Because of the state-dependendant nature of the energy calculations
     * (this needs to be fixed), this function will only work correctly
     * if the function energyGain() is consistent.
     * 
     * @param   probe   probe containing energy information
     * 
     * @return  final gamma value
     * 
     * @see IdealRfGap#energyGain(IProbe)
     */
    public double   gammaFinal(IProbe probe)   {
        
        // Get probe parameters at initial energy
        double Er = probe.getSpeciesRestEnergy();
        double Wi = probe.getKineticEnergy();
        double dW = this.energyGain(probe);
        double Wf = Wi + dW;
        
        double gamma = RelativisticParameterConverter.computeGammaFromEnergies(Wf, Er);
        
        return gamma;
    }
    
    /**
     * Compute and return the mid-gap normalized velocity for the
     * given probe.
     * 
     * NOTE:
     * - Because of the state-dependendant nature of the energy calculations
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
     * Compute and return the final normalized velocity for the
     * given probe.
     * 
     * NOTE:
     * - Because of the state-dependendant nature of the energy calculations
     * (this needs to be fixed), this function will only work correctly
     * if the function energyGain() is consistent.
     * 
     * @param   probe   probe containing energy information
     * 
     * @return  final velocity in units of <b>c</b>
     * 
     * @see IdealRfGap#energyGain(IProbe)
     */
    public double   betaFinal(IProbe probe)   {
        
        // Get probe parameters at initial energy
        double Er = probe.getSpeciesRestEnergy();
        double Wi = probe.getKineticEnergy();
        double dW = this.energyGain(probe);
        double Wf = Wi + dW;
        
        double beta = RelativisticParameterConverter.computeBetaFromEnergies(Wf, Er);
        
        return beta;
    }

    
    
    
    /** 
      * Compute the energy gain of the RF gap for a probe assuming a fixed
      * default phase at the gap center.
      *
      *  @param  probe  uses the particle species charge
      *
      * @return         energy gain for this probe (<b>in electron-volts</b>)
      */
    public double simpleEnergyGain(IProbe probe) {
//         
//        System.out.println("simpleEnergyGain()");
//        
        double ETL = this.getETL();
        double Q   = Math.abs( probe.getSpeciesCharge() );
        double phi = this.getPhase();
        return theEnergyGain = Q*ETL*Math.cos( phi );
    }     
     
     
    /**
     * CKA:  I commented out some code that was never used.
     */
    /**
     * <p>
     *  Get the transverse focusing constant for a particular probe.  The focusing constant 
     *  is used in the construction of the transfer matrix for the RF gap.  A gap provides
     *  longitudinal focusing and transverse defocusing as well as a gain in beam energy.
     *  This focusing constant describes the effect in the transverse direction, which
     *  is defocusing and, therefore, negative.
     *  </p>
     *  <p>
     *  The value represents the thin lens focusing constant for an ideal RF gap 
     *  (this is the inverse of the focal length).  To compute the focusing action 
     *  for the lens we must include beam energy, which is changing through the gap.  We
     *  use the value of beta for which the beam has received half the total energy gain.
     *  </p>
     *
     *  @param  probe   beam energy and particle charge are taken from the probe
     *
     *  @return         (de)focusing constant (<b>in radians/meter</b>)
     */
    public double compTransFocusing(IProbe probe)    {  //calculate kx'

//        double  c = IElement.LightSpeed;
//        
//        double  Q = Math.abs( probe.getSpeciesCharge() );
//        double Er = probe.getSpeciesRestEnergy();
//        
//        double ga = this.gammaMidGap(probe);
//        double bga_2 = ga*ga - 1.0;
//        
//        double bf = this.betaFinal(probe);
//        double gf = this.gammaFinal(probe);
//
//        double  ETL = this.getETL();
////        double  phi = this.getPhase();
//        double    f = this.getFrequency();
//        
//        double   kr = -Math.PI*Q*ETL*f/(c*Er*bga_2*bf*gf);
////        double   kr = -Math.PI*Q*ETL*f*Math.sin(phi)/(c*Er*bga_2*bf*gf);
        
		double c = IElement.LightSpeed;

		double Q = Math.abs(probe.getSpeciesCharge());
		double Er = probe.getSpeciesRestEnergy();
		double Wi = probe.getKineticEnergy();
		double gi = Wi / Er + 1.0;
		double bi = Math.sqrt(1.0 - 1.0 / (gi * gi));
		
		double Wbar = Wi + this.energyGain(probe) / 2.0;
		double gbar = Wbar / Er + 1.0;
		double bbar = Math.sqrt(1.0 - 1.0 / (gbar * gbar));
		double bgbar = bbar * gbar;

//		double Wf = Wi + this.energyGain(probe);
//		double gf = 1 + Wf/Er;
//		double bf = Math.sqrt(1.0 - 1.0 / (gf * gf));
		
//		double ETL = this.getETL();
		double phi = this.getPhase();
		double f = this.getFrequency();
//		double EL = this.getE0()*this.getCellLength();
		
		//double ttf_b_avg = theEnergyGain /( Q * EL * Math.cos(phi) );
//		double theEnergyGain_im = Q * EL * Math.cos(phi) * TTFFit.evaluateAt(bi);
				
		
		//find out new beta final, and if there is a large difference, reevaluate TTF at average beta
//		double Wf_im = Wi + theEnergyGain_im;
//		double gf_im = Wf_im / Er + 1.0;
//		double bf_im = Math.sqrt(1.0 - 1.0 / (gf_im * gf_im));
		
		double b_avg = bi;
//		double TTF_pdiff = 100*(TTFFit.evaluateAt(bf_im)-TTFFit.evaluateAt(bi))/TTFFit.evaluateAt(bi);
//		if(TTF_pdiff>5)
//		{
//			double g_avg = (gi + gf)/2;
//			b_avg = Math.sqrt(1.0 - 1.0 / (g_avg * g_avg));
//			//theEnergyGain = Q * EL * Math.cos(phi0) * TTFFit.evaluateAt(b_avg);
//		}
//				
		
		//double kr = Math.PI * Q * getE0()*ttf_b_avg*getCellLength() * f * Math.sin(-phi) / (c * Er * bgbar * bgbar);
//        double   kr = Math.PI*Q*ETL*f*Math.sin(-phi)/(q*c*Er*bgbar*bgbar);
		//double kr = Math.PI * Q * ETL * f * Math.sin(-phi) / (c * Er * bgbar * bgbar);
		double kr = Math.PI * Q * getE0()*TTFFit.evaluateAt(b_avg)*getCellLength() * f * Math.sin(-phi) / (c * Er * bgbar * bgbar);//should TTF be at bi, or bbar?

//		System.out.println("kr = " + kr);		

		return kr;
    };
    
    /**
     * <p>
     *  Get the longitudinal focusing constant for a particular probe.  The focusing constant 
     *  is used in the construction of the transfer matrix for the RF gap.  A gap provides
     *  longitudinal focusing and transverse defocusing as well as a gain in beam energy.
     *  This focusing constant describes the effect in the longitudinal direction, which
     *  is focusing and, therefore, positive.
     *  </p>
     *  <p>
     *  The value represents the thin lens focusing constant for an ideal RF gap 
     *  (this is the inverse of the focal length).  To compute the focusing action 
     *  for the lens we must include beam energy, which is changing through the gap.  We
     *  use the value of beta for which the beam has received half the total energy gain.
     *  </p>
     *
     *  @param  probe   beam energy and particle charge are taken from the probe
     *
     *  @return         (de)focusing constant (<b>in radians/meter</b>)
     *  
     *  //@deprecated  currently incorrect - I believe this is correct now
     */
//    @Deprecated
    public double compLongFocusing(IProbe probe)    {
        
////        double   c = IProbe.LightSpeed;
////        double   Q = Math.abs( probe.getSpeciesCharge() );
////        double  Er = probe.getSpeciesRestEnergy();
////
////        double ETL = this.getETL();
////        double phi = this.getPhase();
////        double   f = this.getFrequency();
////        
////        double  ba = this.betaMidGap(probe);
////        double  bf = this.betaFinal(probe);
////        double  gf = this.gammaFinal(probe);
////        
////        double kz = 2.0*Math.PI*Q*ETL*Math.sin(phi)*f/(c*Er*bf*gf*ba);
////        
////        return kz;
//
//        // Compute average energy parameters (average)
//        double ga = this.gammaMidGap(probe);
//        
//        
//        double  kt = this.compTransFocusing(probe);
//        double  kz = -2.0*kt*ga*ga;
		double Er = probe.getSpeciesRestEnergy();
		double Wi = probe.getKineticEnergy();

		double Wbar = Wi + this.energyGain(probe) / 2.0;
		double gbar = Wbar / Er + 1.0;

		double kr = this.compTransFocusing(probe);
		double kz = -2.0* kr * gbar * gbar;//-2.0 * kr * gbar * gbar;
        
//		System.out.println("kz = " + kz);
        return kz;
    };
    
    

    /**
     * CKA: I commented out all the deprecated methods and the local variable below that is 
     * consequently never used.
     */
//    boolean phaseSpreadT3d = false; //default = false;

//    /** 
//     * <p>
//     * Compute the phase spread of the bunch for a probe (based on Trace3D RfGap.f)
//     * </p>
//     * <p>
//     * <h4>CKA Notes:</h4>
//     * - This method needs to be optimized now that I understand what it is doing.
//     * In XAL, longitundinal coordinate <i>z</i> is the "phase spread", but in meters. 
//     * To convert to phase spread <i>&delta;&phi;</i> in radians we have
//     * <br/>
//     * <br/>
//     * &nbsp; &nbsp; &delta;&phi; = 2&pi;<i>z</i/>/(&beta;&lambda;) ,
//     * <br/>
//     * <br/>
//     * where &lambda; is the wavelength of the RF.  So, for &lt;&delta;&phi;<sup>2</sup>&gt;
//     * we get
//     * <br/>
//     * <br/> 
//     * &nbsp; &nbsp; &lt;&delta;&phi;<sup>2</sup>&gt; = &lt;<i>z</i><sup>2</sup>&gt;2&pi;<i>f</i>
//     *                                                /(&beta;<i>c</i>) ,
//     * <br/>
//     * <br/>
//     * where <i>f</i> is the RF frequency of the gap and c is the speed of light.
//     * <br/>
//     * <br/>
//     * - For the optional computation <b>phaseSpreadT3d</b> (which apparently is not
//     * used) I am not sure what is happening, or why <y'y'> is significant?
//     * </p>
//     * 
//     *  @param  probe   we are computing the phase spread for this probe at the current
//     *                  <code>IdealRfGap</code> condition
//     *
//     * @return         phase spread (half width) for this probe (<b>radian</b>)
//     * 
//     * @author Hiroyuki Sako
//     * @author Christopher K. Allen
//     * @version Nov 6, 2013
//     *  
//     * @deprecated  this should be in the algorithm class
//     */
//    
//    @Deprecated
//    public double phaseSpread(EnvelopeProbe probe) {
//        
//        
//        double phaseSpreadCalculated = 0;
//        
////        if (!calcPhaseSpread) {
////            return phaseSpreadCalculated;
////        }
//        //? double dE = getE0() * getCellLength();
//        //      probe.updateTwiss(probe.getCorrelation(), dE);
//        //obsolete probe.updateTwiss(probe.getCorrelation());
//        //obsolete Twiss twiss[] = probe.getTwiss();
//        Twiss [] twiss = probe.getCovariance().computeTwiss();
//        //sako
//        double Er = probe.getSpeciesRestEnergy();
//        double Wi = probe.getKineticEnergy();
//        
//        double Wbar = Wi + this.energyGain(probe)/2.0;
//        //   double gbar = Wbar/Er + 1.0;
//        //   double bbar = Math.sqrt(1.0 - 1.0/(gbar*gbar));
//        
//        //def
//        TraceXalUnitConverter t3dxal = TraceXalUnitConverter.newConverter(getFrequency(),Er,Wi);
//        //temp sako
//        //   TraceXalUnitConverter t3dxal = TraceXalUnitConverter.newConverter(probe.getBeamFreq(),Er,Wbar);
//        
//        Twiss t3dtwissz = t3dxal.xalToTraceLongitudinal(twiss[2]);
//        
//        double emitz = t3dtwissz.getEmittance();
//        double betaz = t3dtwissz.getBeta();
//        
//        phaseSpreadCalculated = Math.sqrt(emitz*betaz)*2*Math.PI/360; //radian
//        
//        //betaaverage is  not there!!! is it ok?
//        
//        //sako for test. Try to use average energy to calculate dphiav
//        
//        
//        if (phaseSpreadT3d) {
//            
//            double gbar = Wbar/Er + 1.0;
//            double bbar = Math.sqrt(1.0 - 1.0/(gbar*gbar));
//            
////            double gi = Wi/Er + 1.;
////            double bi = Math.sqrt(1-1/(gi*gi));
//            double clight = IProbe.LightSpeed;
//            double freq = getFrequency();
//            
//            double wavel = clight/freq;
//            
//            CovarianceMatrix     matCorXAL  = (probe).getCovariance();//this need to be convert to t3d unit
//            
////            double lambda = wavel;
////            double gammai = 1.0 + (Wi / Er);
////            double vnorm = Math.sqrt(1.0 - (1.0 / (gammai * gammai)));
//            
//            
//            
//            //      Twiss twissXAL[] = matCorXAL.twissParameters();
//            
//            //      Twiss t3dtwissx = t3dxal.xalToTraceTransverse(twiss[0]);
//            //      Twiss t3dtwissy = t3dxal.xalToTraceTransverse(twiss[1]);
//            //      CovarianceMatrix matCor = CovarianceMatrix.buildCorrelation(t3dtwissx,t3dtwissy,t3dtwissz);
//            
//            
//            double sigma55 = matCorXAL.getElem(4,4);
//            
//            
//            
////            double dphit3dcor = 1.0/(vnorm*lambda)/(Math.PI*2.);//probably this is correct
//            
//            double dphit3d = 2.*Math.PI*Math.sqrt(sigma55)/(bbar*wavel); 
//            
//            phaseSpreadCalculated = dphit3d;//temp
//        }
//        
//        //      DP=TWOPI*H*SQRT(SIG(5,5))/(BAV*WAVEL)
//        
//        
//        return phaseSpreadCalculated;
//    }
//    
//    
    
//    /**
//     * @deprecated  This should go in the algorithm class
//     */
//    @Deprecated
//    public double correctTransFocusingPhaseSpread(EnvelopeProbe probe) {
//        double dphi = phaseSpread(probe);
//        double cor = 1.;
//        cor = 1-dphi*dphi/14;
//        //    	if (dphi != 0) {
//        if (dphi > 0.1) {
//            cor = 15/dphi/dphi*(3/dphi/dphi*(Math.sin(dphi)/dphi-Math.cos(dphi))-Math.sin(dphi)/dphi);	
//        }
//        //    	}
//        return cor;
//    }
//    
    
//    /**
//     ********************************************************************************
//     //new implementation by sako, 7 Aug 06, to do trans/long simultanously
//      //used in EnvTrackerAdapt, EnvelopeTracker
//       *******************************************************************************
//       *
//       * @deprecated Should go in algorithm class
//       */
//    @Deprecated
//    public double [] correctSigmaPhaseSpread(IProbe probe) {
//        
//        double dfac[] = new double[2];
//        
//        double dfacT = 0d;
//        double dfacL = 0d;
//        
//        dfac[0] = 0d;
//        dfac[1] = 0d;
//        //transverse
//        
//        if (!(probe instanceof EnvelopeProbe)) {
//            return dfac;
//        }
//        
//        double phi = getPhase();
//        double dphi = phaseSpread((EnvelopeProbe)probe);
//        
//        double tdp = 2*dphi;
//        double sintdp = Math.sin(tdp);
//        
//        
//        double f2t = 1-tdp*tdp/14;
//        if (tdp>0.1) {
//            f2t = 3*(sintdp/tdp-Math.cos(tdp))/tdp/tdp; //APPENDIX F (Trace3D manual)
//            f2t = 15*(f2t-sintdp/tdp)/tdp/tdp;
//        }
//        double sinphi = Math.sin(phi);
//        double cosphi = Math.cos(phi);
//        double G1 = 0.5*(1+(sinphi*sinphi-cosphi*cosphi)*f2t);
//        double Q = probe.getSpeciesCharge();
//        double h = 1;//harmic number
//        double m = probe.getSpeciesRestEnergy();
//        double w = probe.getKineticEnergy();
//        double dw = energyGain(probe);
//        double wa = w+dw/2;
//        double betagammaa = Math.sqrt(wa/m*(2+wa/m));
//        
//        double wf = w+dw;
//        double betagammaf =Math.sqrt(wf/m*(2+wf/m));
////        double gammai = (w+m)/m;
////        double betagammai =Math.sqrt(w/m*(2+w/m));
////        double gammaf = (wf+m)/m;
////        double gammaa = (wa+m)/m;
////        double betagamma0 = Math.sqrt(w/m*(2+w/m));
//        
//        double clight = IProbe.LightSpeed;
//        double freq = getFrequency();
//        double lambda = clight/freq;
//        
//        //was def    	
//        //	double cayd = h*Math.PI*getETL()*Math.abs(Q)/(m*betagammaa*betagammaa*betagamma0*lambda);
//        //sako 21 jul 06
//        double cay = h*Math.abs(Q)*Math.PI*getETL()/(m*betagammaa*betagammaa*betagammaf*lambda); //Kx'
//        double f1 =  1-dphi*dphi/14;
//        if (dphi > 0.1) {
//            f1 = 15/dphi/dphi*(3/dphi/dphi*(Math.sin(dphi)/dphi-Math.cos(dphi))-Math.sin(dphi)/dphi);	
//        }
//        dfacT = cay*cay*(G1-sinphi*sinphi*f1*f1);
//        
//        
//        //longitudinal
//        double f2l = 1-tdp*tdp/14;
//        if (tdp>0.1) {
//            f2l = 3*(sintdp/tdp-Math.cos(tdp))/tdp/tdp;
//            f2l = 15*(f2l-sintdp/tdp)/tdp/tdp;
//        }
//        
//        //def	double cayz = 2*cay*gammaa*gammaa;
//        double cayz = 2*cay; //this is best
//        double cayp = cayz*cayz*dphi*dphi;
//        dfacL = cayp*(0.125*cosphi*cosphi+(1./576.)*dphi*dphi*sinphi*sinphi);
//        
//        dfac[0] = dfacT;
//        dfac[1] = dfacL;
//        
//        return dfac;
//    }
//    
//    /**
//     * <p>
//     * Calculation of emittance increase due to phase spread
//     * based on calculations in Trace3d (RfGap.f)
//     * </p>
//     * <p>
//     * Used in EnvTrackerAdapt, EnvelopeTracker
//     * </p>
//     * <p>
//     * <h4>CKA Notes:</h4>
//     * - I think this should go in the <b>Algorithm</b> class.
//     * It expects an <code>EnvelopeProbe</code> - element objects
//     * should really not be concerned with the type of probe.
//     * </p>
//     *   
//     * @param probe     envelope probe object (something with emittance and moments)
//     * 
//     * @return  the change in emittance after going through this element
//     * 
//     * @deprecated  this should go in the algorithm class - it calls phaseSpread(EnvelopeProbe)
//     */
//    @Deprecated
//    public double correctTransSigmaPhaseSpread(IProbe probe) {
//        
//        double dfac = 1;
//        if (!(probe instanceof EnvelopeProbe)) {
//            return dfac;
//        }
//        
//        double phi = getPhase();
//        double dphi = phaseSpread((EnvelopeProbe)probe);
//        double f1   = correctTransFocusingPhaseSpread((EnvelopeProbe)probe);
//        double tdp = 2*dphi;
//        double f2 = 1-tdp*tdp/14;
//        if (tdp>0.1) {
//            double sintdp = Math.sin(tdp);
//            f2 = 3*(sintdp/tdp-Math.cos(tdp))/tdp/tdp; //APPENDIX F (Trace3D manual)
//            f2 = 15*(f2-sintdp/tdp)/tdp/tdp;
//        }
//        double sinphi = Math.sin(phi);
//        double cosphi = Math.cos(phi);
//        double G1 = 0.5*(1+(sinphi*sinphi-cosphi*cosphi)*f2);
//        double Q = probe.getSpeciesCharge();
//        double h = 1;//harmic number
//        double m = probe.getSpeciesRestEnergy();
//        double w = probe.getKineticEnergy();
//        double dw = energyGain(probe);
//        double wa = w+dw/2;
//        double betagammaa = Math.sqrt(wa/m*(2+wa/m));
//        
//        double wf = w+dw;
//        double betagammaf =Math.sqrt(wf/m*(2+wf/m));
//        
//        
//        double clight = IProbe.LightSpeed;
//        double freq = getFrequency();
//        double lambda = clight/freq;
//        //    	double cay = h*Math.PI*getETL()*Math.abs(Q)/(m*betagammaa*betagammaa*betagammaa*lambda); //Kx'
//        //sako 21 jul 06
//        
//        double cay = Math.abs(Q)*h*Math.PI*getETL()/(m*betagammaa*betagammaa*betagammaf*lambda); //Kx'
//        
//        dfac = cay*cay*(G1-sinphi*sinphi*f1*f1);
//        
//        return dfac;
//        
//    }
//    
//    
//    /**
//     * <p>
//     * Calculation of emittance increase due to phase spread
//     * based on calculations in Trace3d (RfGap.f)
//     * </p>
//     * <p>
//     * used in EnvTrackerAdapt, EnvelopeTracker
//     * </p>
//     * <p>
//     * <h4>CKA Notes:</h4>
//     * - I think this should go in the <b>Algorithm</b> class.
//     * It expects an <code>EnvelopeProbe</code> - element objects
//     * should really not be concerned with the type of probe.
//     * </p>
//     * 
//     * @param probe envelope-type probe (something with emittance and moments)
//     * 
//     * @return  the increase in longitudinal emittance due to finite phase spread
//     * 
//     * @deprecated  should go in algorithm class
//     */
//    @Deprecated
//    public double correctLongSigmaPhaseSpread(IProbe probe) {
//        
//        double dfac = 1;
//        if (!(probe instanceof EnvelopeProbe)) {
//            return dfac;
//        }
//        
//        double phi = getPhase();
//        double dphi = phaseSpread((EnvelopeProbe)probe);
//        double tdp = 2*dphi;
//        
//        double f2 = 1-tdp*tdp/14;
//        if (tdp>0.1) {
//            double sintdp = Math.sin(tdp);
//            f2 = 3*(sintdp/tdp-Math.cos(tdp))/tdp/tdp;
//            f2 = 15*(f2-sintdp/tdp)/tdp/tdp;
//        }
//        double sinphi = Math.sin(phi);
//        double cosphi = Math.cos(phi);
//        
//        double Q = probe.getSpeciesCharge();
//        double h = 1;//harmic number
//        double m = probe.getSpeciesRestEnergy();
//        double w = probe.getKineticEnergy();
//        double dw = energyGain(probe);
//        double wa = w+dw/2;
//        double gammaa = (wa+m)/m;
//        double betagammaa = Math.sqrt(wa/m*(2+wa/m));
////        double betagamma0 = Math.sqrt(w/m*(2+w/m));
//        double clight = IProbe.LightSpeed;
//        double freq = getFrequency();
//        double lambda = clight/freq;
//        
//        
//        double wf = w+dw;
//        double betagammaf =Math.sqrt(wf/m*(2+wf/m));
//        
//        //was def    	
//        //	double cayd = h*Math.PI*getETL()*Math.abs(Q)/(m*betagammaa*betagammaa*betagamma0*lambda);
//        
//        //21 jul 06
//        double cay = h*Math.PI*getETL()*Math.abs(Q)/(m*betagammaa*betagammaa*betagammaf*lambda);
//        
//        //	cay = cayd;
//        
//        double cayz = 2*cay*gammaa*gammaa;
//        double cayp = cayz*cayz*dphi*dphi;
//        dfac = cayp*(0.125*cosphi*cosphi+(1./576.)*dphi*dphi*sinphi*sinphi);
//        
//        return dfac;
//        
//    }
//
//


    
    /*
     *  Testing and Debugging
     */
    
    
    /**
     *  Dump current state and content to output stream.
     *
     *  @param  os      output stream object
     */
    @Override
    public void print(PrintWriter os)    {
        super.print(os);
        
        os.println("  Gap ETL product    : " + this.getETL() );
        os.println("  Gap phase shift    : " + this.getPhase() );
        os.println("  RF frequency       : " + this.getFrequency() );
    }
}









/*
 * Storage
 */



//protected PhaseMap transferMapOld(IProbe probe) throws ModelException {
//
//        // Get probe parameters at initial energy
//        double Er = probe.getSpeciesRestEnergy();
//        double Wi = probe.getKineticEnergy();
//        double bi = probe.getBeta();
//        double gi = probe.getGamma();
//      
// // Determine the current energy gain and focusing constants for the gap
//  
//  // the following section is to calculate the phase of the beam at each gap, rather than use hardwired phases.
//  
// // update the energy gain first:
//  if( probe.getAlgorithm().useRfGapPhaseCalculation() ) 
//      compEnergyGain(probe);
//   else 
//       simpleEnergyGain(probe);
//  
//        double dW = this.energyGain(probe);
//
//        double kz = 0;
//        double kt = 0;
//       
//        if (probe instanceof EnvelopeProbe) {
//      kz = compLongFocusingPhaseSpread((EnvelopeProbe)probe);
//      kt = compTransFocusingPhaseSpread((EnvelopeProbe)probe);
//        } else {
//      kz = this.compLongFocusing(probe);
//      kt = this.compTransFocusing(probe);
//
//  }
//        
//        // Compute final energy parameters
//        double Wf = Wi + dW;
//        double gf = Wf/Er + 1.0;//gamma average
//        double bf = Math.sqrt(1.0 - 1.0/(gf*gf));//beta
//
//        // Compute average energy parameters (average)
//        double Wav = (Wf + Wi)/2.0;
//        double gav = Wav/Er + 1.0;
//        
//        
//        // Compute component block matrices then full transfer matrix
//        double arrTran[][] = new double[][] {{1.0, 0.0}, {kt/(bf*gf), bi*gi/(bf*gf)}};
//        double arrLong[][] = new double[][] {{1.0, 0.0}, {(kz/(bf*gf))*gav*gav/(gf*gf), gi*gi*gi*bi/(gf*gf*gf*bf)}};
//        
//        PhaseMatrix matPhi = new PhaseMatrix();
//        
//        matPhi.setElem(6,6, 1.0);
//        matPhi.setSubMatrix(0,1, 0,1, arrTran);
//        matPhi.setSubMatrix(2,3, 2,3, arrTran);
//        matPhi.setSubMatrix(4,5, 4,5, arrLong);
//        
//  
//  // Do the phase update if this is desired:
//  // do it here to resuse the bi, bf, etc. factors
////    if(probe.getAlgorithm().useRfGapPhaseCalculation()) advancePhase(probe);
//  
////        PrintWriter os = new PrintWriter(System.out);
////        matPhi.print(os);
////        os.close();
//
//        return new PhaseMap(matPhi);
//    }




///** 
//* a method that is called once by transferMatrix to calculate the energy gain.
//* This prevents energy gain calculation from being repeated many times.
//* Importantly it provides a workaround from the eneryGain being calculated
//* after the upstreamExitPhase is updated elsewhere
//*/
//
//public double compEnergyGain(IProbe probe) {
//// 
//// System.out.println("calcEnergyGain()");
//// 
// double dPsi = 0.;
// double dPhi1 = 0.;
// double bc;  // the central beta
//
// // Initial energy parameters
// double Er = probe.getSpeciesRestEnergy();
// double Wi = probe.getKineticEnergy();
// double bi = probe.getBeta();
// double gi = probe.getGamma();
// //double dW = this.energyGain(probe);
//
// // Final energy parameters
// //double Wf = Wi + dW;
// //double gf = Wf/Er + 1.0;
// //double bf = Math.sqrt(1.0 - 1.0/(gf*gf));
//
// //double b0 = (bi + bf)/2.;
// //double g0 = (gi + gf)/2.;
//
//// double     bi = probe.getBeta();
//// double gi = probe.getGamma();
//
// double EL = getE0() * getCellLength();
// double bh, gh, factor;
//
////bc = bi + EL/(2. * bi * Math.pow(gi, 3.) * Er) * (TTFFit.evaluateAt(bi) * Math.cos(phi0) + SFit.evaluateAt(bi)*Math.sin(phi0));
//
// // get phase at the gap center:
// if(!isFirstGap()) {
//     // phase change from drifting beam:
//     dPhi1 = 2. * Math.PI * (getCellLength()/2. - gapOffset) * getFrequency() /(bi * IElement.LightSpeed);
//     phi0 = upstreamExitPhase  + dPhi1;
//     // correction factor for center (needs iteration)
//     // crude iteration: to "solve" for phi0
//     for (int i=0; i < 2; i++) {
//         bc = bi + EL/(2. * bi * Math.pow(gi, 3.) * Er) * (TTFFit.evaluateAt(bi) * Math.cos(phi0) +  SFit.evaluateAt(bi)*Math.sin(phi0));
//         bh = (bi + bc)/2.;
//         gh = Math.sqrt(1./(1. - bh*bh));
//         factor = (Math.sin(phi0) * TTFPrimeFit.evaluateAt(bi) + Math.cos(phi0) * SPrimeFit.evaluateAt(bi));
//         dPsi = -Math.PI * EL /(Math.pow(bh, 2.) * Math.pow(gh, 3.) * Er) * factor;
//         phi0 = upstreamExitPhase  + dPhi1 +  dPsi;
//     }
//     setPhase(phi0); // set the phase to that used at the gap center
//}
//// for first gap use input for phase at the gap center
//else {
//    phi0 = this.getPhase();
//}
// bc = bi + EL/(2. * bi * Math.pow(gi, 3.) * Er) * (TTFFit.evaluateAt(bi) * Math.cos(phi0) +  SFit.evaluateAt(bi)*Math.sin(phi0));             
//     double Q   = Math.abs( probe.getSpeciesCharge() );
// double bg = getCellLength() * getFrequency()/IElement.LightSpeed;
//
// double fac = 2. * Math.PI*(bg/bc - 1.) * TTFPrimeFit.evaluateAt(bc);
//
// theEnergyGain = Q*EL*Math.cos(phi0) * (TTFFit.evaluateAt(bc) - fac);
// return theEnergyGain;
//}
//





//private void advancePhase(IProbe probe)  {
//
//
//double entrancePhase;
//double dPhi1;
//double dPhi2;
//
//
//// Initial energy parameters
//double Er = probe.getSpeciesRestEnergy();
//double Wi = probe.getKineticEnergy();
//double bi = probe.getBeta();
//double gi = probe.getGamma();
//double dW = this.energyGain(probe);
//
//// Final energy parameters
//double Wf = Wi + dW;
//double gf = Wf/Er + 1.0;
//double bf = Math.sqrt(1.0 - 1.0/(gf*gf));
//
//double b0 = (bi + bf)/2.;
//double g0 = (gi + gf)/2.;
//
//
////double bi = probe.getBeta();
////double gi = probe.getGamma();
////
////double b0 = (bi + bf)/2.;
////double g0 = (gi + gf)/2.;
//
//// phase change from drift from start of cell to center of cell
//// gapOffsets have to do with electrical centers != geometric centers, e.g. DTLs (small effect).
//if(!isFirstGap()) {
//  entrancePhase = upstreamExitPhase;
//  dPhi1 = 2. * Math.PI * (getCellLength()/2. - gapOffset) * getFrequency() /(bi * IElement.LightSpeed);
//}
//else {
//  // phase is prescibed at the center for this guy
//  dPhi1 = 0.;
//  entrancePhase = getPhase();
//}   
//
//// phase change from center correction factor:
//deltaPhaseCorrection = -2. * Math.PI * getFrequency() * getE0() * Math.pow(getCellLength(), 2.) / (Math.pow(b0, 3.) * Math.pow(g0, 3.) * Er * IElement.LightSpeed) * Math.sin(phi0) * TTFPrimeFit.evaluateAt(b0);
//
//// phase change from drift from center to cell exit:
//dPhi2 = 2. * Math.PI * (getCellLength()/2. + gapOffset) * getFrequency() /(bf * IElement.LightSpeed);   
//
//
//upstreamExitPhase = entrancePhase + dPhi1 + dPhi2 + deltaPhaseCorrection - 2 * Math.PI;
//
//// update the elapsed time to account for the phase correction term
//
//probe.setTime(deltaPhaseCorrection/(getFrequency() * 2. * Math.PI));
//}




///**
//*  Including emittance increase due to the phase spread
//*  Get the transverse focusing constant for a particular probe.  The focusing constant 
//*  is used in the construction of the transfer matrix for the RF gap.  A gap provides
//*  longitudinal focusing and transverse defocusing as well as a gain in beam energy.
//*  This focusing constant describes the effect in the transverse direction, which
//*  is defocusing and, therefore, negative.
//*  <p>
//*  The value represents the thin lens focusing constant for an ideal RF gap 
//*  (this is the inverse of the focal length).  To compute the focusing action 
//*  for the lens we must include beam energy, which is changing through the gap.  We
//*  use the value of beta for which the beam has received half the total energy gain.
//*
//*  @param  probe   beam energy and particle charge are taken from the probe
//*
//*  @return         (de)focusing constant (<b>in radians/meter</b>)
//*/
//public double compTransFocusingPhaseSpread(EnvelopeProbe probe)    { //calculate kx' (cor) (=cay in RfGap.f)
// 
// double  c = IElement.LightSpeed;
// 
// double  Q = Math.abs( probe.getSpeciesCharge() );
// double Er = probe.getSpeciesRestEnergy();
// double Wi = probe.getKineticEnergy();
// 
// double  dW   = this.energyGain(probe);
// 
// double Wbar = Wi + dW/2.0; //bar = averge
// double gbar = Wbar/Er + 1.0;
// double bbar = Math.sqrt(1.0 - 1.0/(gbar*gbar));
// double bgbar = bbar*gbar;
// 
// double  ETL = this.getETL();
// double  phi = this.getPhase();
// double    f = this.getFrequency();
// 
// double cor = correctTransFocusingPhaseSpread(probe); // 1-dphi*dphi/14
////double   kt = Math.PI*Q*ETL*f*Math.sin(-phi)/(q*c*Er*bgbar*bgbar);
// 
// double kt = Q*Math.PI*ETL/(Er*bgbar*bgbar*c/f)*Math.sin(-phi)*cor; 
// // kt = cay*bgf*cor*sin(-phi)
// 
// //   double cay = kt/(bf*gf)/cor/Math.sin(-phi);
// 
// 
// return kt;
//}

//public double correctLongFocusingPhaseSpread(EnvelopeProbe probe) { //correction factor for kz'
//    double dphi = phaseSpread(probe);
//    // this is default        
//    double cor = 1-dphi*dphi/12;
//    
//    //in Trace3D manual, changed on 21 Jul 06 sako
//    //    double cor = 1+dphi*dphi/12;
//    
//    return cor;
//}
//

///**
// * *************************
// //new implementation by sako, 7 Aug 06
//  // calculate kt and kz simultanuously
//   //used in transferMap
//    ***************************
//    */
//public double [] compFocusingPhaseSpread(EnvelopeProbe probe) {
//    double k[] = new double[2];
//    double kt=0;
//    double kz=0;
//    
//    k[0] = 0;
//    k[1] = 0;
//    
////    if (!(probe instanceof EnvelopeProbe)) {
////        return k;
////    }
////    
//    //transverse
//    
//    double  c = IElement.LightSpeed;
//    
//    double  Q = Math.abs( probe.getSpeciesCharge() );
//    double Er = probe.getSpeciesRestEnergy();
//    double Wi = probe.getKineticEnergy();
//    
//    double  dW   = this.energyGain(probe);
//    
//    double Wbar = Wi + dW/2.0; //bar = averge
//    double gbar = Wbar/Er + 1.0;
//    double bbar = Math.sqrt(1.0 - 1.0/(gbar*gbar));
//    double bgbar = bbar*gbar;
//    
//    double  ETL = this.getETL();
//    double  phi = this.getPhase();
//    double    f = this.getFrequency();
//    
//    
//    double dphi = phaseSpread(probe);
//    
//    double f1 = 1-dphi*dphi/14;
//    
//    if (dphi>0.1) {
//        f1=3.*(Math.sin(dphi)/dphi-Math.cos(dphi))/(dphi*dphi);
//        f1=15.*(f1-Math.sin(dphi)/dphi)/(dphi*dphi);
//    }
//    
//    double ktraw = Q*Math.PI*ETL/(Er*bgbar*bgbar*c/f)*Math.sin(-phi);
//    
//    if (debugT3d) {
//        System.out.println("PI, ETL, m, c"+Math.PI+" "+ETL+" "+Er+" "+c);
//        System.out.println("sin(-phi)"+Math.sin(-phi));
//        System.out.println("bgbar = "+bgbar);
//    }
//    
//    kt = ktraw*f1; //new
//    
//    double corl = 1-dphi*dphi/12;
//    
//    double kr = ktraw*corl;
//    
//    //def
//    kz = -2.0*kr*gbar*gbar;
//    //sako repaired
//    //    kz = -2.0*kr;
//    
//    
//    k[0] = kt;
//    k[1] = kz;
//    
//    return k;
//}



///**
// * IdealRfGap.java
// * Redefine function to return exactly same definitions as kx and kz in Trace3D manual (Appendix F)
// * Created on August 19, 2006 
// * */
//
//public double [] compK(EnvelopeProbe probe) {
//    double k[] = new double[2];
//    double kt=0d;
//    double kz=0d;
//    
//    k[0] = 0d;
//    k[1] = 0d;
//    
////    if (!(probe instanceof EnvelopeProbe)) {
////        return k;
////    }
////    
//    //transverse
//    
//    double  Q = Math.abs( probe.getSpeciesCharge() );
//    double Er = probe.getSpeciesRestEnergy();
//    double Wi = probe.getKineticEnergy();
//    
//    double  dW   = this.energyGain(probe);
//    
//    double Wbar = Wi + dW/2.0; //bar = averge
//    double gbar = Wbar/Er + 1.0;
//    double bbar = Math.sqrt(1.0 - 1.0/(gbar*gbar));
//    double bgbar = bbar*gbar;
//    
//    double Wf =  Wi+dW;
//    
//    double  ETL = this.getETL();
//    double  c = IElement.LightSpeed;
//    double  f = this.getFrequency();
//    double  gf = Wf/Er + 1.0;
//    double  bf = Math.sqrt(1.0 - 1.0/(gf*gf));
//    
//    double dphi = phaseSpread(probe);
//    
//    double f1 = 1-dphi*dphi/14;
//    
//    if (dphi>0.1) {
//        f1=3.*(Math.sin(dphi)/dphi-Math.cos(dphi))/(dphi*dphi);
//        f1=15.*(f1-Math.sin(dphi)/dphi)/(dphi*dphi);
//    }
//    
//    double ktraw = Q*Math.PI*ETL/(Er*bgbar*bgbar*c/f)/(bf*gf); //now sin(-phi) is not there and 1/(betaf*gammaf) is there
//    
//    kt = ktraw*f1; //new
//    
//    double corl = 1-dphi*dphi/12;
//    
//    double kzraw = ktraw*corl;
//    
//    kz = -2.0*kzraw*gbar*gbar;
//    //sako repaired
//    //    kz = -2.0*kr;
//    
//    
//    k[0] = kt;
//    k[1] = kz;
//    
//    return k;
//}


///**
// *  Including emittance increase due to phase spread
// *  Get the longitudinal focusing constant for a particular probe.  The focusing constant 
// *  is used in the construction of the transfer matrix for the RF gap.  A gap provides
// *  longitudinal focusing and transverse defocusing as well as a gain in beam energy.
// *  This focusing constant describes the effect in the longitudinal direction, which
// *  is focusing and, therefore, positive.
// *  <p>
// *  The value represents the thin lens focusing constant for an ideal RF gap 
// *  (this is the inverse of the focal length).  To compute the focusing action 
// *  for the lens we must include beam energy, which is changing through the gap.  We
// *  use the value of beta for which the beam has received half the total energy gain.
// *
// *  @param  probe   beam energy and particle charge are taken from the probe
// *
// *  @return         (de)focusing constant (<b>in radians/meter</b>)
// */
//public double compLongFocusingPhaseSpread(EnvelopeProbe probe)    {  //calculate kz'
//
//    double Er = probe.getSpeciesRestEnergy();
//    double Wi = probe.getKineticEnergy();
//    
//    double Wbar = Wi + this.energyGain(probe)/2.0;
//
//    double gbar = Wbar/Er + 1.0; //gamma-bar
//
//      double cor  = correctLongFocusingPhaseSpread(probe); // 1-dphi*dphi/12
//
//      double cort = correctTransFocusingPhaseSpread(probe); // 1-dphi*dphi/14
//
////was def
////        double kr = this.compTransFocusing(probe)*cor; // kx'*(1+dphi*dphi/12)
//
//    double kr = 0;
//
////added on 21 Jul 06
////I forgot to apply cort
//    if (probe instanceof EnvelopeProbe) {
//    kr = compTransFocusingPhaseSpread((EnvelopeProbe)probe)/cort*cor;
//} else {
//    kr = this.compTransFocusing(probe)/cort*cor; // kx'*(1-dphi*dphi/12)
//}
//
//    double kz = -2.0*kr*gbar*gbar;
//    
//    return kz;
//}
//
