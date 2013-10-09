package eu.ess.jels.smf.impl;

import xal.smf.attr.RfCavityBucket;
import xal.smf.impl.RfCavity;
import eu.ess.jels.smf.attr.ESSRfCavityBucket;
import eu.ess.jels.tools.math.InverseRealPolynomial;

/**
 * This RfCavity implementation is extended to:
 *  - provide special TTF/STF fits for the start gap
 *  - to fit TTF/STF using TraceWin parameters using InverseRealPolinomial
 * 
 * @author Ivo List
 *
 */
public class ESSRfCavity extends RfCavity {
	public ESSRfCavity(String strId) {
		super(strId);
		setRfField(new ESSRfCavityBucket());
	}

	public ESSRfCavity(String strId, int intReserve) {
		super(strId, intReserve);
		setRfField(new ESSRfCavityBucket());
	}
		
	@Override
	public ESSRfCavityBucket getRfField() {
		return (ESSRfCavityBucket)m_bucRfCavity;
	}

	/** return a fit of the transit time factor as a function of beta */
	@Override
	public InverseRealPolynomial getTTFFit() {
		RfCavityBucket rfCavBuc = this.getRfField();
		return new InverseRealPolynomial(rfCavBuc.getTTFCoefs());
	}

	 /** return a fit of the transit time factor prime as a function of beta */  
	@Override
	public InverseRealPolynomial getTTFPrimeFit() {
		RfCavityBucket rfCavBuc = this.getRfField();
		return new InverseRealPolynomial(rfCavBuc.getTTFPrimeCoefs());
	}

	/** return a fit of the "S" transit time factor as a function of beta */  
	@Override
	public InverseRealPolynomial getSTFFit() {
		RfCavityBucket rfCavBuc = this.getRfField();
		return new InverseRealPolynomial(rfCavBuc.getSTFCoefs());
	}

	 /** return a fit of the "S" transit time factor prime as a function of beta */ 
	@Override
	public InverseRealPolynomial getSTFPrimeFit() {
		RfCavityBucket rfCavBuc = this.getRfField();
		return new InverseRealPolynomial(rfCavBuc.getSTFPrimeCoefs());
	}

	/** return a fit of the transit time factor for end cells as a function of beta */
	@Override
	public InverseRealPolynomial getTTFFitEnd() {
		RfCavityBucket rfCavBuc = this.getRfField();
		return new InverseRealPolynomial(rfCavBuc.getTTF_endCoefs());
	}

	/** return a fit of the transit time factor prime for end cells as a function of beta */
	@Override
	public InverseRealPolynomial getTTFPrimeFitEnd() {
		RfCavityBucket rfCavBuc = this.getRfField();
		return new InverseRealPolynomial(rfCavBuc.getTTFPrime_endCoefs());
	}

	/** return a fit of the "S" transit time factor for end cells as a function of beta */
	@Override
	public InverseRealPolynomial getSTFFitEnd() {
		RfCavityBucket rfCavBuc = this.getRfField();
		return new InverseRealPolynomial(rfCavBuc.getSTF_endCoefs());
	}

	/** return a fit of the "S" transit time factor prime for end cells as a function of beta */
	@Override
	public InverseRealPolynomial getSTFPrimeFitEnd() {
		RfCavityBucket rfCavBuc = this.getRfField();
		return new InverseRealPolynomial(rfCavBuc.getSTFPrime_endCoefs());
	}
	
	/** return a fit of the transit time factor for start cells as a function of beta */
	public InverseRealPolynomial getTTFFitStart() {
		ESSRfCavityBucket rfCavBuc = this.getRfField();
		return new InverseRealPolynomial(rfCavBuc.getTTF_startCoefs());
	}

	/** return a fit of the transit time factor prime for start cells as a function of beta */
	public InverseRealPolynomial getTTFPrimeFitStart() {
		ESSRfCavityBucket rfCavBuc = this.getRfField();
		return new InverseRealPolynomial(rfCavBuc.getTTFPrime_startCoefs());
	}

	/** return a fit of the "S" transit time factor for start cells as a function of beta */
	public InverseRealPolynomial getSTFFitStart() {
		ESSRfCavityBucket rfCavBuc = this.getRfField();
		return new InverseRealPolynomial(rfCavBuc.getSTF_startCoefs());
	}

	/** return a fit of the "S" transit time factor prime for start cells as a function of beta */
	public InverseRealPolynomial getSTFPrimeFitStart() {
		ESSRfCavityBucket rfCavBuc = this.getRfField();
		return new InverseRealPolynomial(rfCavBuc.getSTFPrime_startCoefs());
	}	
}
