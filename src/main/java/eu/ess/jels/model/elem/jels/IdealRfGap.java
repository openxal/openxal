/*
 *  IdealRfGap.java
 *
 *  Created on October 22, 2002, 1:58 PM
 */
package eu.ess.jels.model.elem.jels;

import java.io.PrintWriter;

import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.IRfGap;
import xal.model.elem.ThinElement;
import xal.sim.scenario.LatticeElement;
import xal.smf.impl.RfGap;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
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

        
    /** the separation of the gap center from the cell center (m) */
    private double gapOffset = 0.;
    
    /** the on axis accelerating field (V) */
    private double E0 = 0.;
    
    /** the accelerating cell length  */
    private double cellLength = 0.;
    
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
     * return wheteher this gap is the initial gap of a cavity 
     */
    public boolean isFirstGap() { return initialGap;}
     
    
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
     * Returns the time taken for the probe to propagate through element.
     * 
     *  @param  probe   propagating probe
     *  
     *  @return         value of zero 
     */
    @Override
    public double elapsedTime(IProbe probe)  {
    	return 0.0;
    }

    /** 
      * Compute the energy gain of the RF gap for a probe including the effects 
      * of calculating the phase advance.
      *
      *
      * @return         energy gain for this probe (<b>in electron-volts</b>)
      */
      
    @Override
    public double energyGain(IProbe probe)
    {    	
    	return (gamma_end - 1.0) * probe.getSpeciesRestEnergy() - probe.getKineticEnergy();
    	//return (gamma_end - probe.getGamma()) * probe.getSpeciesRestEnergy();    	
    	//return getETL()*Math.cos(getPhase());
    }
    
    protected double computeBetaFromGamma(double gamma) {    	    
        //double beta = Math.sqrt(1.0 - 1.0/Math.pow(gamma,2));
        double beta = Math.sqrt(Math.pow(gamma,2) - 1.0)/gamma;
        return beta;
    };
       
    private double gamma_end;
    
    
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
    	PhaseMatrix matPhi = new PhaseMatrix(); 	
     	double lambda=LightSpeed/getFrequency();
    	
    	double Phis;
    	if (isFirstGap()) Phis = getPhase();
    	else {    		 
    		double lastGapPosition = probe.getLastGapPosition();
    		double position = probe.getPosition();
    		if (lastGapPosition == position) {
    			Phis = getPhase(); // we are visiting gap for the second time
    		} else {
	    		Phis = probe.getLastGapPhase();
	    		Phis += 2*Math.PI*(position - lastGapPosition)/(lambda*probe.getBeta());
	    		if (structureMode == 1) Phis += Math.PI;
	    		setPhase(Phis);
    		}
    	}
    	
    	double E0TL = getETL();
    	double DeltaPhi = 0;
    	if (E0TL==0)
    	{
    		matPhi = PhaseMatrix.identity();    		
    	}
    	else
    	{        	
    		double mass = probe.getSpeciesRestEnergy();
    		double gamma_start=probe.getGamma();
    		double beta_start=computeBetaFromGamma(gamma_start);//probe.getBeta();
    		
    		double beta_end;
    		double gamma_avg;
    		
        	double kx;
        	double ky;
        	double kxy;
        	double kz;
        	
        	double C;
        	
    		if (TTFFit.getCoef(0)!=0)
    		{
    			double gamma_middle=gamma_start+E0TL/mass*Math.cos(Phis)/2;    			
    			double beta_middle= computeBetaFromGamma(gamma_middle);
    			
    			// k=betas/beta_middle;
    			double T=TTFFit.evaluateAt(beta_middle); // = Ts+kTs*(k-1)+k2Ts*pow(k-1,2)/2;
    			double kT=-beta_middle*TTFFit.evaluateDerivativeAt(beta_middle); // = k*(kTs+k2Ts*(k-1));
    			double E0TL_scaled=E0TL*T/TTFFit.getCoef(1);

    			gamma_end=gamma_start+E0TL_scaled/mass*Math.cos(Phis);    			
    			beta_end = computeBetaFromGamma(gamma_end);
    			gamma_avg=(gamma_end+gamma_start)/2;
    			//double beta_avg=(beta_end+beta_start)/2;
    			double beta_avg = computeBetaFromGamma(gamma_avg);
    
    			DeltaPhi=E0TL_scaled/mass*Math.sin(Phis)/(Math.pow(gamma_avg,2)*beta_avg)*(kT/T);
    			kxy=-Math.PI*E0TL_scaled/mass*Math.sin(Phis)/(Math.pow(gamma_avg*beta_avg,2)*lambda);
    			kx=1-E0TL_scaled/(2*mass)*Math.cos(Phis)/(Math.pow(beta_avg,2)*Math.pow(gamma_avg,3))*(Math.pow(gamma_avg,2)+kT/T);
    			ky=1-E0TL_scaled/(2*mass)*Math.cos(Phis)/(Math.pow(beta_avg,2)*Math.pow(gamma_avg,3))*(Math.pow(gamma_avg,2)-kT/T);
    			kz=2*Math.PI*(E0TL_scaled/mass)*Math.sin(Phis)/(Math.pow(beta_avg,2)*lambda);
    			
    			C=Math.sqrt((beta_start*gamma_start)/(beta_end*gamma_end*kx*ky));
    		}
    		else
    		{    			
    			gamma_end=gamma_start+E0TL/mass*Math.cos(Phis);    			
    			beta_end = computeBetaFromGamma(gamma_end);
    			
    			gamma_avg=(gamma_end+gamma_start)/2;
    			//double beta_avg=(beta_end+beta_start)/2;
    			double beta_avg = computeBetaFromGamma(gamma_avg);

    			kxy=-Math.PI*E0TL*Math.sin(Phis)/(Math.pow(gamma_avg*beta_avg,2)*lambda*mass);
    			kx=1-E0TL/(2*mass)*Math.cos(Phis)/(Math.pow(beta_avg,2)*gamma_avg);
    			ky=kx;
    			kz=2*Math.PI*E0TL*Math.sin(Phis)/(Math.pow(beta_avg,2)*lambda*mass);
    			
    			C=1.0;
    		}
    		
    		

    		matPhi.setElem(0, 0, kx*C);
    		matPhi.setElem(1,0,kxy/(beta_end*gamma_end));
    		matPhi.setElem(1,1,ky*C);

    		matPhi.setElem(2,2,kx*C);
    		matPhi.setElem(3,2,kxy/(beta_end*gamma_end));
    		matPhi.setElem(3,3,ky*C);

    		/*matPhi.setElem(4,4,1);
    		matPhi.setElem(5,4,kz/(beta_end*Math.pow(gamma_end,3)));
    		matPhi.setElem(5,5,(beta_start*Math.pow(gamma_start,3))/(beta_end*Math.pow(gamma_end,3)));*/
    		matPhi.setElem(4,4,gamma_end/gamma_start);
    		matPhi.setElem(5,4,kz/(beta_end*Math.pow(gamma_end,2)*gamma_start));
    		matPhi.setElem(5,5,(beta_start*Math.pow(gamma_start,2))/(beta_end*Math.pow(gamma_end,2)));
    	}
            
    	probe.setLastGapPhase(Phis + DeltaPhi);
    	probe.setLastGapPosition(probe.getPosition());
    	matPhi.setElem(6,6,1);
        return new PhaseMap(matPhi);
    }
    
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

    /**
	 * Conversion method to be provided by the user
	 * 
	 * @param latticeElement the SMF node to convert
	 */
	@Override
	public void initializeFrom(LatticeElement element) {		
		RfGap rfgap = (RfGap) element.getNode();
		
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

