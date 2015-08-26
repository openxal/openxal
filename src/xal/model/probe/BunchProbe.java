/*
 * BunchProbe.java
 *
 * Created on November 12, 2002, 6:17 PM
 * Modifcations:
 *      11/2006 - CKA changed the primary state variables to bunch frequency Q
 *                and beam current I
 *      11/2013 - CKA removed the "betatron phase" attribute.
 */

package xal.model.probe;


import xal.tools.annotation.AProperty.Units;
import xal.model.probe.traj.BunchProbeState;




/**
 *  <p>
 *  Abstract base class for all probes having beam properties.  That is derived classes should
 *  represent probes with collective beam dynamics.
 *  </p>
 *  <h3>Note:</h3>
 *  <p>
 *  The bunch charge <i>Q</i> is computed from the beam current <i>I</i> and 
 *  bunch frequency <i>f</i> as 
 *  <br>
 *  <br>
 *  &nbsp; &nbsp;  <i>Q</i> = <i>I/f</i>
 * </p>
 * 
 * @author  Christopher K. Allen
 * @since   Nov 2, 2002
 */
public abstract class BunchProbe<S extends BunchProbeState<S>> extends Probe<S> {
    
    
    /*
     *  Abstract Methods
     */
    
    
//    /** 
//     *  Abstract - Returns the correlation matrix (sigma matrix) in homogeneous
//     *  phase space coordinates.
//     *
//     *  @return         <zz^T> =| <x*x>  <x*xp>  <x*y>  <x*yp>  <x*z>  <x*zp>  <x>  |
//     *                          | <xp*x> <xp*xp> <xp*y> <xp*yp> <xp*z> <xp*zp> <xp> |
//     *                            ...
//     *
//     *  @see    xal.tools.beam.PhaseMatrix
//     */
//    public abstract CovarianceMatrix getCorrelation();
//    
    
    
    /*
     *  Initialization
     */
    
    
    /**
     *  Default constructor.
     * 
     *  Since BunchProbe is abstract constructor should only be calls by a derived class.
     *  Creates a new (empty) instance of BunchProbe.
     */
    protected BunchProbe()   {
        super();
//        this.vecPhsBeta = R3.zero();
    }
  
    /**
     *  Copy constructor - clones the argument
     *  Since BunchProbe is abstract constructor should only be calls by a derived class.
     *
     *  @param  probe   BunchProbe object to be cloned
     */
    public BunchProbe(final BunchProbe<S> probe)   {
        super(probe);       
        this.setBunchFrequency(probe.getBunchFrequency());
        this.setBeamCurrent(probe.getBeamCurrent());
//        this.setBetatronPhase(new R3(probe.getBetatronPhase()));
    };        
    
    /**
     * Set the bunch arrival time frequency.
     * 
     * @param f     new bunch frequency in <b>Hz</b>
     */
    public void setBunchFrequency(double f) {
        this.stateCurrent.setBunchFrequency(f);
    }
 
    /**
     *  Set the total beam current.
     * 
     * @param   I   new beam current in <b>Amperes</b>
     */
    public void setBeamCurrent(double I)    { 
        this.stateCurrent.setBeamCurrent(I);
    };
    

//    /**
//     *  Set the total beam charge 
//     * 
//     *  @param  Q   beam charge in <b>Coulombs</b>
//     */
//    public void setBeamCharge(double Q)     { m_dblBmQ = Q; };
    

    
    
    /*
     *  Attribute Query
     */
    
    /**
     * Returns the bunch frequency, that is the frequency of 
     * the bunches need to create the beam current.
     * 
     * The bunch frequency f is computed from the beam current 
     * I and bunch charge Q as 
     *  
     *      f = I/Q
     *      
     * @return  bunch frequency in Hertz
     */
	@Units( "Hz" )
    public double getBunchFrequency()  {
        return this.stateCurrent.getBunchFrequency();
    };
    
    /** 
     * Returns the total beam current 
     * 
     * @return  beam current in <b>amps</b>
     */
	@Units( "amps" )
    public double getBeamCurrent() { 
        return this.stateCurrent.getBeamCurrent();
     }

//    /**
//     * Returns the betatron phase with space charge for all three phase planes.
//     * 
//     * @return vector (psix,psiy,psiz) of phases in <b>radians </b>
//     */
//    public R3 getBetatronPhase() {
//        return this.vecPhsBeta;
//    }

    
    /*
     * Computed Properties
     */
    
    /** 
     * Computes and returns the charge in each beam bunch
     * 
     * @return  beam charge in <b>coulombs</b>
     */
    public double bunchCharge() {
    	return this.stateCurrent.bunchCharge();  	
    }

    /** 
     * <p>
     *  Returns the generalized, three-dimensional beam perveance <i>K</i>.  
     *  This value is defined to be
     *  </p>
     *  
     *      K = (Q/4*pi*e0)*(1/gamma^3*beta^2)*(|q|/ER) 
     *  
     *  <p>
     *  where <i>Q</i> is the bunch charge, <i>e0</i> is the permittivity
     *  of free space, <i>gamma</i> is the relativitic factor, <i>beta</i> is 
     *  the normalized design velocity, <i>q</i> is the charge of the beam
     *  particles and <i>ER</i> is the rest energy of the beam partiles.
     *  </p>
     *  
     *  <p>
     *  NOTES:
     *  - The value (1/4*pi*e0) is equal to 1e-7*c^2 where <i>c</i> is the
     *  speed of light. 
     *  
     *  @return generalized beam perveance <b>Units: radians^2/meter</b>
     *  
     *  @author Christopher K. Allen
     */
    public double beamPerveance() {
        return this.stateCurrent.beamPerveance();
    }

    
    /*
     * Probe Overrides
     */

    /**
     * Just restating <code>Probe.{@link #createProbeState()}</code>
     *
     * @see xal.model.probe.Probe#createProbeState()
     *
     * @author Christopher K. Allen
     * @since  Nov 5, 2013
     */
    @Override
    public abstract S createProbeState();
    
    /**
     * Just restating <code>Probe.{@link #createEmptyProbeState()}</code>.
     * 
     * @author Jonathan M. Freed
     * @since Jul 1, 2014
     */
    @Override
    public abstract S createEmptyProbeState();
    

//    /**
//     * Applies the properties of the state that is passed in to the current
//     * state of the probe.
//     * 
//     * @param state - the state to apply to the probe
//     * 
//     * @author Jonathan M. Freed
//     * @since Jul 9, 2014
//     */
//    @Override
//    public void applyState(final S state) {
//        this.stateCurrent = state.copy();
////        super.applyState(state);
////        this.setBunchFrequency( state.getBunchFrequency() );
////        this.setBeamCurrent( state.getBeamCurrent() );
//    }

    
    
}





//
// Storage
//

///** 
//*  Returns the beam perveance <b>Units: radians^2/meter</b>
//*  
//*  TODO    This could be optimized (CKA)
//*/
//public double beamPerveance() {
//// double gamma = this.getGamma();
//// double bg2   = gamma*gamma - 1.0;
// 
// double  c = LightSpeed;
// double  dblPermT = 1.e-7*c*c*this.bunchCharge();
//// double  dblPermT = this.bunchCharge()/(2.0*Math.PI*Permittivity);
// double  dblRelaT = 1.0/(super.getGamma()*super.getBetaGamma()*super.getBetaGamma());
// double  dblEnerT = Math.abs(super.getSpeciesCharge())/super.getSpeciesRestEnergy();
// 
// 
// 
// return dblPermT*dblRelaT*dblEnerT;  
//}


///**
//*  Return the covariance matrix of the distribution.  Note that this can be computed
//*  from the correlation matrix in homogeneous coordinates since the mean values are 
//*  included in that case.
//*
//*  @return     <(z-<z>)*(z-<z>)^T> = <z*z^T> - <z>*<z>^T
//*/
//public CovarianceMatrix  phaseCovariance() {
// return getCorrelation().getCovariance();
//}
//
///** 
//*  Return the phase space coordinates of the centroid in homogeneous coordinates 
//*
//*  @return         <z> = (<x>, <xp>, <y>, <yp>, <z>, <zp>, 1)^T
//*/
//public PhaseVector phaseMean()  {
// return getCorrelation().getMean();
//}
//


///** return the time elapsed from the start of the probe tracking (sec) */
//public double getElapsedTime() { return elapsedTime;}
//
///** set the time elapsed from the start of the probe tracking (sec) 
//* @param time - the elapsed time (sec)
//*/
//public void setElapsedTime(double time) {elapsedTime = time; }



///** advance the time the probe has spent traveling down the beam line
//@ param the step size to advance (m)
//*/
//
//public void advanceElapsedTime(double h) {
//double deltaT;
//
//deltaT = h / (IConstants.LightSpeed * getBeta());
//setElapsedTime(getElapsedTime() + deltaT);
//}
///** update the elapsed time by a specified time increment
//* This is used in the RF gap (thin lens) kick correction.
//@ param the time amount to correct the integrated elapsed time by
//*/
//
//public void setTime(double dt) {
//setElapsedTime(getElapsedTime() + dt);
//}
