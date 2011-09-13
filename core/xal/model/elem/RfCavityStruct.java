/*
 * Created on Feb 19, 2004
 */
package xal.model.elem;

import xal.tools.math.poly.UnivariateRealPolynomial;

import xal.model.ModelException;
import xal.model.IModelDataSource;
import xal.model.source.RfCavityDataSource;

/**
 * <p>
 * Models a general RF cavity accelerating structure.  The details of the structure are 
 * not specified (e.g., DTL, CCL, CCDTL, etc.).  This element is typically
 * used as a container of <code>{@link IdealRfGap}</code> elements and, as such,
 * can model a variety of such structures.  
 * </p>
 * <p>
 * The transit time function 
 * "<i>T</i>(<i>k</i>)" and "<i>S</i>(<i>k</i>)", along with their derivatives,
 * can be specified for the 
 * cavity in order to account for the varying transit time factors among
 * gaps within (as a function of particle velocity).
 * </p>
 * 
 * @author Craig McChesney
 * @author Christopher K. Allen
 */
public class RfCavityStruct extends ElementSeq {
    
    /*
     * Global Constants
     */
     
     /** string type identifier for class */
    public static final String s_strType = "RfCavityStruct";
    
    
    
    //
    // Instance Variables ======================================================
    //
    
    /** The cavity amplitude */
    private double                  dblRfAmp;
    
    /** The cavity phase */
    private double                  dblRfPhs;
    
    

    /** Fourier cosine transform of the axial longitudinal electric field w.r.t. k */ 
    private UnivariateRealPolynomial plyT;
    
    /** Derivative of Fourier cosine transform of the axial longitudinal electric field */ 
    private UnivariateRealPolynomial plyTp;

    /** Fourier sine transform of the axial longitudinal electric field w.r.t. k*/ 
    private UnivariateRealPolynomial plyS;
    
    /** Derivative of Fourier sine transform of the axial longitudinal electric field */ 
    private UnivariateRealPolynomial plySp;

    
    
    
    /*
     * Initialization
     */
    
    // Constructors ============================================================
    /**
     * Creates a new, empty <code>RfCavityStruct</code> structure.
     * 
     */
    public RfCavityStruct() {
        super(s_strType);
    }
    
    
    // State Getters / Setters =================================================
    
    /**
     * Set the amplitude of the the RF power inside the
     * cavity. 
     * 
     * @param dblRfAmp  amplitude of RF in cavity (in Volts)
     */
    public void setRfAmplitude(double dblRfAmp) {
        this.dblRfAmp = dblRfAmp;
    }


    /**
     * Sets the phase of the RF inside the cavity with respect
     * to the synchronous particle. 
     * 
     * @param dblRfPhs  new phase of RF w.r.t. the synchronous particle (in radians)
     */
    public void setRfPhase(double dblRfPhs) {
        this.dblRfPhs = dblRfPhs;
    }


    /** 
     * Sets the transit time "T" function, the Fourier cosine transform 
     * of the longitudinal electric field.
     *
     * @param polynomial    function T(k)
     *
     * @author Christopher K. Allen
     * @since  Apr 19, 2011
     */
    public void setT(UnivariateRealPolynomial polynomial) {
        plyT = polynomial;
    }


    /** 
     * Sets the derivative of the transit time "T" function, the Fourier cosine transform 
     * of the longitudinal electric field.
     *
     * @param polynomial    function T'(k)
     *
     * @author Christopher K. Allen
     * @since  Apr 19, 2011
     */
    public void setTp(UnivariateRealPolynomial polynomial) {
        plyTp = polynomial;
    }


    /** 
     * Sets the transit time "S" function, the Fourier sine transform 
     * of the longitudinal electric field.
     *
     * @param polynomial    function S(k)
     *
     * @author Christopher K. Allen
     * @since  Apr 19, 2011
     */
    public void setS(UnivariateRealPolynomial polynomial) {
        plyS = polynomial;
    }


    /** 
     * Sets the derivative of the transit time "S" function, the Fourier sine transform 
     * of the longitudinal electric field.
     *
     * @param polynomial    function S'(k)
     *
     * @author Christopher K. Allen
     * @since  Apr 19, 2011
     */
    public void setSp(UnivariateRealPolynomial polynomial) {
        plySp = polynomial;
    }

    
    /*
     * Attributes
     */

    /**
     * @return the dblRfAmp
     */
    public double getRfAmplitude() {
        return dblRfAmp;
    }


    /**
     * @return the dblRfPhs
     */
    public double getRfPhase() {
        return dblRfPhs;
    }


    /** 
     * Return a polynomial fit of the transit time factor as a function of beta.
     * (CKA - is it &beta; or <i>k</i>, the wave number?)
     * 
     *  @return the function <i>T</i>(<i>k</i>) where <i>k</i> is particle wave number
     */  
    public UnivariateRealPolynomial getT() {
        return plyT;
    }
    
    /** return a polynomial fit of the transit time factor prime as a function of beta */  
    public UnivariateRealPolynomial getTp() {
        return plyTp;  
    }
    
    /** return a polynomial fit of the "S" transit time factor as a function of beta */  
    public UnivariateRealPolynomial getS() {
        return plyS;
    }

    /** return a polynomial fit of the "S" transit time factor prime as a function of beta */  
    public UnivariateRealPolynomial getSp() {
        return plySp;
    }

    /**
     * Initialize this element from the supplied source.
     * 
     * @param source data source to initialize from
     * 
     * @throws IllegalArgumentException if source is not a RfCavityDataSource
     * @throws ModelException never
     */
    public void initializeFrom(IModelDataSource source) throws ModelException {
        
        // try to cast to expected source type
        RfCavityDataSource cavSource = null;
        try {
            cavSource = (RfCavityDataSource) source;
        } catch (ClassCastException ex) {
            // throw exception if not of expected type
            throw new IllegalArgumentException(
                "expected instance of RfCavityDataSource in initializeFrom, got: " + 
                source.getClass().getName());
        }
        
        // initialize from the source
        this.setT( cavSource.getTTFFit() );
        this.setTp( cavSource.getTTFPrimeFit() );
        this.setS( cavSource.getSTFFit() );
        this.setSp( cavSource.getSTFPrimeFit() );
        
    }

}
