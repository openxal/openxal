/*
 * Created on Mar 10, 2004
 */
package xal.model.source;

import xal.tools.math.poly.UnivariateRealPolynomial;

import xal.model.IModelDataSource;

/**
 * Specifies interface for sources used to create RfCavity model elements.
 * 
 * @author Craig McChesney
 */
public interface RfCavityDataSource extends IModelDataSource {

	/** return a polynomial fit of the transit time factor as a function of beta */  
	public UnivariateRealPolynomial getTTFFit();

	/** return a polynomial fit of the transit time factor prime as a function of beta */  
	public UnivariateRealPolynomial getTTFPrimeFit();   
    
	/** return a polynomial fit of the "S" transit time factor as a function of beta */  
	public UnivariateRealPolynomial getSTFFit();

	/** return a polynomial fit of the "S" transit time factor prime as a function of beta */  
	public UnivariateRealPolynomial getSTFPrimeFit();
	       
}
