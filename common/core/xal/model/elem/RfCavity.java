/*
 * Created on Feb 19, 2004
 */
package xal.model.elem;

import xal.tools.math.poly.UnivariateRealPolynomial;

import xal.model.ModelException;
import xal.model.IModelDataSource;
import xal.model.source.RfCavityDataSource;

/**
 * @author Craig McChesney
 * @author Christopher K. Allen
 */
public class RfCavity extends ElementSeq {
    
    /*
     * Global Constants
     */
     
     /** string type identifier for class */
    public static final String s_strType = "RfCavity";
    
    
    
    // Instance Variables ======================================================
    

    private UnivariateRealPolynomial polynomialTtf;
    private UnivariateRealPolynomial polynomialTtfPrime;
    private UnivariateRealPolynomial polynomialStf;
    private UnivariateRealPolynomial polynomialStfPrime;

    
    
    // Constructors ============================================================
    /**
     */
    public RfCavity() {
        super(s_strType);
    }
    
    
    // State Getters / Setters =================================================
    
    /** return a polynomial fit of the transit time factor as a function of beta */  
    public UnivariateRealPolynomial getTtf() {
        return polynomialTtf;
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
    public void setTtf(UnivariateRealPolynomial polynomial) {
        polynomialTtf = polynomial;
    }

    /** return a polynomial fit of the transit time factor prime as a function of beta */  
    public UnivariateRealPolynomial getTtfPrime() {
        return polynomialTtfPrime;  
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
    public void setTtfPrime(UnivariateRealPolynomial polynomial) {
        polynomialTtfPrime = polynomial;
    }

    /** return a polynomial fit of the "S" transit time factor as a function of beta */  
    public UnivariateRealPolynomial getStf() {
        return polynomialStf;
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
    public void setStf(UnivariateRealPolynomial polynomial) {
        polynomialStf = polynomial;
    }

    /** return a polynomial fit of the "S" transit time factor prime as a function of beta */  
    public UnivariateRealPolynomial getStfPrime() {
        return polynomialStfPrime;
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
    public void setStfPrime(UnivariateRealPolynomial polynomial) {
        polynomialStfPrime = polynomial;
    }


    // Element Overrides =======================================================
    
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
        setTtf(cavSource.getTTFFit());
        setTtfPrime(cavSource.getTTFPrimeFit());
        setStf(cavSource.getSTFFit());
        setStfPrime(cavSource.getSTFPrimeFit());
        
    }

}
