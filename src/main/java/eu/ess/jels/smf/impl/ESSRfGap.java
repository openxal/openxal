package eu.ess.jels.smf.impl;

import xal.smf.impl.RfGap;
import xal.tools.math.poly.UnivariateRealPolynomial;

/**
 * This gap implementation is extended to return correct (special) TTF/STF fits for the start gap. 
 * 
 * @author Ivo List
 *
 */
public class ESSRfGap extends RfGap {

	public ESSRfGap(String strId) {
		super(strId);
	}

	/** return a polynomial fit of the transit time factor as a function of beta */
	@Override
	public UnivariateRealPolynomial getTTFFit() {
        ESSRfCavity rfCav = (ESSRfCavity) this.getParent();
        if(isFirstGap()) 
        	return rfCav.getTTFFitStart();
        else if (isEndCell())
        	return rfCav.getTTFFitEnd();
        else
        	return rfCav.getTTFFit();		
	}

	/** return a polynomial fit of the TTF-prime factor as a function of beta */  
	@Override
	public UnivariateRealPolynomial getTTFPrimeFit() {
		ESSRfCavity rfCav = (ESSRfCavity) this.getParent();
        if(isFirstGap()) 
        	return rfCav.getTTFPrimeFitStart();
        else if (isEndCell())
        	return rfCav.getTTFPrimeFitEnd();
        else
        	return rfCav.getTTFPrimeFit();
	}

	/** return a polynomial fit of the S factor as a function of beta */  
	@Override
	public UnivariateRealPolynomial getSFit() {
		ESSRfCavity rfCav = (ESSRfCavity) this.getParent();
        if(isFirstGap()) 
        	return rfCav.getSTFFitStart();
        else if (isEndCell())
        	return rfCav.getSTFFitEnd();
        else
        	return rfCav.getSTFFit();
	}

	/** return a polynomial fit of the S-prime factor as a function of beta */  
	@Override
	public UnivariateRealPolynomial getSPrimeFit() {
		ESSRfCavity rfCav = (ESSRfCavity) this.getParent();
        if(isFirstGap()) 
        	return rfCav.getSTFPrimeFitStart();
        else if (isEndCell())
        	return rfCav.getSTFPrimeFitEnd();
        else
        	return rfCav.getSTFPrimeFit();
	}

}
