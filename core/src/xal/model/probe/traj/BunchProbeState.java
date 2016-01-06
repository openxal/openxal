/*
 * BunchProbeState.java
 *
 * Created on April, 2003, 5:15 PM
 * 
 * Modifications:
 *  11/2006     - CKA removed references to Twiss parameters 
 *                and correlation matrix
 *              - CKA changed primary state variables to 
 *                beam current and bunch frequency
 */

package xal.model.probe.traj;

import xal.tools.data.DataAdaptor;

import xal.model.probe.BunchProbe;
import xal.tools.data.DataFormatException;


/**
 * Encapsulates a BunchProbe's state at a point in time.  Contains
 * addition state variables for probes with beam-like behavior.
 * 
 * @author Craig McChesney
 * @author Christopher K. Allen
 * @version $id:
 * 
 */
public abstract class BunchProbeState<S extends BunchProbeState<S>> extends ProbeState<S>  {


    /*
     * Global Constants
     */

    // ************ I/O Support
    /** element tag for beam state data */    
    private static final String ELEM_BEAM = "beam";
    
    /** attribute tag for total beam current */
    private static final String ATTR_BEAMCURRENT = "I";
    
    /** attribute tag for total beam charge */
    private static final String ATTR_BUNCHFREQ = "f";
    
//    /** attribute tag for betatron phase advance */    
//    private static final String ATTR_BETAPHASE = "phase";
    


    /*
     * Local Attributes
     */
     
    /** bunch frequency in Hz */
    private double  dlbBunFreq = 0.0;
    
    /** Beam current */
    private double  dblBmCurr = 0.0;
    

    /*
     * Initialization
     */
    
    /**
     * Default constructor.  Creates an empty <code>BunchProbeState</code>.
     *
     */
    public BunchProbeState() {
    	super();
        this.dblBmCurr = 0.0;
        this.dlbBunFreq = 0.0;
    }
    
    /**
     * Copy constructor for BunchProbeState.  Initializes the new
     * <code>BunchProbeState</code> objects with the state attributes
     * of the given <code>BunchProbeState</code>.
     *
     * @param state     initializing state
     *
     * @author Christopher K. Allen
     * @author Jonathan M. Freed
     * @since  Jun 26, 2014
     */
    public BunchProbeState(final S state){
    	super(state);
    	
    	this.dblBmCurr	= state.getBeamCurrent();
    	this.dlbBunFreq	= state.getBunchFrequency();
    }
    
    /**
     * Initializing constructor.  Creates a new <code>BunchProbe</code> object initialized 
     * to the argument's state.
     * 
     * @param probe     probe object with which to initialize this state
     */
    public BunchProbeState(final BunchProbe<S> probe) {
        super(probe);
        this.setBunchFrequency(probe.getBunchFrequency());
        this.setBeamCurrent(probe.getBeamCurrent());
//        this.setBetatronPhase(probe.getBetatronPhase());
    }
    
    /*
     * Property Accessors
     */
    
    /**
     * Set the bunch arrival time frequency.
     * 
     * @param f     new bunch frequency in <b>Hz</b>
     */
    public void setBunchFrequency(double f) {
        this.dlbBunFreq = f;
    }
 
    /**
     *  Set the total beam current 
     * 
     * @param   I   new beam current in <b>Amperes</b>
     */
    public void setBeamCurrent(double I) {
        dblBmCurr = I;
    }
    
    /**
     * <p>
     * Returns the bunch frequency, that is, the rate at which
     * beam bunches pass a stationary point (in laboratory coordinates).
     * The frequency <i>f</i> of the bunches determines the beam current <i>I</i>.
     * </p>
     * <p>
     * The bunch frequency <i>f</i> is related to the beam current 
     * <i>I</i> and bunch charge <i>Q</i> as 
     * <br>
     * <br>
     * &nbsp; &nbsp; <i>f</i> = <i>I/Q</i>
     * <br>
     * <br>
     * </p>
     *      
     * @return  bunch frequency in Hertz
     */
    public double getBunchFrequency()  {
        return this.dlbBunFreq;
    };
    
    /** 
     * Returns the total beam current, which is the bunch charge <i>Q</i> times
     * the bunch frequency <i>f</i>.
     * 
     * @return  beam current in <b>amps</b>
     */
    public double getBeamCurrent() {
        return dblBmCurr;
    }
    
//    /**
//     * Returns the betatron phase of this bunch for all 3 phase places.
//     * 
//     * @return  vector (&psi;<sub><i>x</i></sub>, &psi;<sub><i>y</i></sub>, &psi;<sub><i>z</i></sub>) 
//     *
//     * @author Christopher K. Allen
//     * @since  Oct 23, 2013
//     */
//    public R3   getBunchBetatronPhase() {
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
        if (this.getBunchFrequency() > 0.0) {
            return this.getBeamCurrent()/this.getBunchFrequency();
            
        } else {
            return 0.0;
            
        }
    }
    
    /** 
     * <p>
     *  Returns the generalized, three-dimensional beam perveance <i>K</i>.  
     *  This value is defined to be
     *  </p>
     *  
     *      <i>K</i> = (<i>Q</i>/4*&pi;*<i>&epsilon;</i><sub>0</sub>)(1/&gamma;<sup>3</sup>&beta;<sup>2</sup>)(|<i>q</i>|/<i>E<sub>R</sub></i>) 
     *  
     *  <p>
     *  where <i>Q</i> is the bunch charge, <i>&epsilon;</i><sub>0</sub> is the permittivity
     *  of free space, <i>&gamma;</i> is the relativistic factor, <i>&beta;</i> is 
     *  the normalized design velocity, <i>q</i> is the individual particle charge 
     *  and <i>E<sub>R</sub></i> is the rest energy of the beam particles.
     *  </p>
     *  
     *  <h3>NOTES:</h3>
     *  <p>
     *  - The value (1/4&pi;&epsilon;<sub>0</sub>) is equal to 10<sup>-7</sup><i>c</i><sup>2</sup>
     *  where <i>c</i> is the speed of light.
     *  </p> 
     *  
     *  @return generalized beam perveance <b>Units: radians^2/meter</b>
     *  
     *  @author Christopher K. Allen
     */
    public double beamPerveance() {
    	
        // Get some shorthand
        double c     = LightSpeed;
        double gamma = this.getGamma();
        double bg2   = gamma*gamma - 1.0;

        // Compute independent terms
        double  dblPermT = 1.0e-7*c*c*this.bunchCharge();
        double  dblRelaT = 1.0/(gamma*bg2);
        double  dblEnerT = Math.abs(super.getSpeciesCharge())/super.getSpeciesRestEnergy();
        
        return dblPermT*dblRelaT*dblEnerT;  
    }

//
//    /*
//     * CovarianceMatrix Properties
//     */
//
//    
//    /**
//     *  Returns the covariance matrix of this state in homogeneous
//     *  phase space coordinates.
//     * 
//     * @return      <zz^T> - <z><z>^T
//     */
//    public  CovarianceMatrix phaseCovariance()   {
//        return phaseCorrelation().getCovariance();
//    }
//    
//    /**
//     *  Returns the rms emittances for this state.
//     * 
//     * @return array (ex,ey,ez) of rms emittances
//     */
//    public double[] rmsEmittances() {
//		return phaseCorrelation().rmsEmittances();
//    }
//    
//    /**
//     * Return the twiss parameters for this state calculated from the 
//     * correlation matrix
//     * 
//     * @deprecated This method does not provide correct Twiss info with any dipole bend.  Should use getTwiss() from EnvelopeProbe class.
//     */
//    public Twiss[] twissParameters() {
//    	return phaseCorrelation().twissParameters();
//    }
//	
//	
//    /**
//	 * get the array of twiss objects for this state for all three planes
//     * @deprecated This method does not provide correct Twiss info with any dipole bend presented.  Should use getTwiss() from EnvelopeProbe.
//	 * @return array(twiss-H, twiss-V, twiss-L
//	 */
//    public Twiss[] getTwiss() {
//		return twissParameters();
//	}
//	
//    
//    /** 
//     *  Abstract - Return the phase space coordinates of the centroid in homogeneous coordinates 
//     *
//     *  @return         <z> = (<x>, <xp>, <y>, <yp>, <z>, <zp>, 1)^T
//     */
//    public PhaseVector phaseMean()  {
//        return phaseCorrelation().getMean();
//    }
//	
//	
//    /** 
//	 *  Returns homogeneous phase space coordinates of the particle.  The units
//	 *  are meters and radians.
//	 *
//	 *  @return     vector (x,x',y,y',z,z',1) of phase space coordinates
//	 */
//    public PhaseVector getPhaseCoordinates() {
//		return phaseMean();
//	}
//	
//	
//	/**
//	 * Get the fixed orbit about which betatron oscillations occur.
//	 * @return the fixed orbit vector (x,x',y,y',z,z',1)
//	 */
//	public PhaseVector getFixedOrbit() {
//		return phaseMean();
//	}
//	

    /*
     * Debugging
     */
     
     
    /**
     * Write out state information to a string.
     * 
     * @return     text version of internal state data
     */
    @Override
    public String toString() {
        return super.toString() + 
                " curr: " + getBeamCurrent() + 
                " freq: " + getBunchFrequency();
//                " freq: " + getBunchFrequency() +
//                " phase: " + getBunchBetatronPhase();
    }
	


    /*
     * Support Methods
     */	
	
    /**
     * Save the state values particular to <code>BunchProbeState</code> objects
     * to the data sink.
     * 
     *  @param  daSink   data sink represented by <code>DataAdaptor</code> interface
     */
    @Override
    protected void addPropertiesTo(DataAdaptor daSink) {
        super.addPropertiesTo(daSink);
        DataAdaptor datBunch = daSink.createChild(ELEM_BEAM);
        datBunch.setValue(ATTR_BUNCHFREQ,   getBunchFrequency());
        datBunch.setValue(ATTR_BEAMCURRENT, getBeamCurrent());
//        datBunch.setValue(ATTR_BETAPHASE,   getBunchBetatronPhase().toString());
        
    }
    
    /**
     * Recover the state values particular to <code>BunchProbeState</code> objects 
     * from the data source.
     *
     *  @param  daSource   data source represented by a <code>DataAdaptor</code> interface
     * 
     *  @exception DataFormatException     state information in data source is malformatted
     */
    @Override
    protected void readPropertiesFrom(DataAdaptor daSource) throws DataFormatException {
        super.readPropertiesFrom(daSource);
        
        DataAdaptor daBunch = daSource.childAdaptor(ELEM_BEAM);
        if (daBunch == null)
            throw new DataFormatException("BunchProbeState#readPropertiesFrom(): no child element = " + ELEM_BEAM);

        if (daBunch.hasAttribute(ATTR_BUNCHFREQ))
            setBunchFrequency(daBunch.doubleValue(ATTR_BUNCHFREQ));
        if (daBunch.hasAttribute(ATTR_BEAMCURRENT))            
            setBeamCurrent(daBunch.doubleValue(ATTR_BEAMCURRENT));
//        if (daBunch.hasAttribute(ATTR_BETAPHASE)) {
//            R3  vecPhase = new R3( daBunch.stringValue(ATTR_BETAPHASE) );
//            this.setBetatronPhase( vecPhase );
//        }
    }

}
