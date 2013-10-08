package eu.ess.jels.smf.impl;

import xal.smf.attr.RfCavityBucket;
import xal.smf.impl.RfCavity;
import eu.ess.jels.tools.math.InverseRealPolynomial;

public class ESSRfCavity extends RfCavity {
	public ESSRfCavity(String strId) {
		super(strId);
	}

	public ESSRfCavity(String strId, int intReserve) {
		super(strId, intReserve);
	}
	
	@Override
	public InverseRealPolynomial getTTFFit() {
		RfCavityBucket rfCavBuc = this.getRfField();
		return new InverseRealPolynomial(rfCavBuc.getTTFCoefs());
	}

	@Override
	public InverseRealPolynomial getTTFPrimeFit() {
		RfCavityBucket rfCavBuc = this.getRfField();
		return new InverseRealPolynomial(rfCavBuc.getTTFPrimeCoefs());
	}

	@Override
	public InverseRealPolynomial getSTFFit() {
		RfCavityBucket rfCavBuc = this.getRfField();
		return new InverseRealPolynomial(rfCavBuc.getSTFCoefs());
	}

	@Override
	public InverseRealPolynomial getSTFPrimeFit() {
		RfCavityBucket rfCavBuc = this.getRfField();
		return new InverseRealPolynomial(rfCavBuc.getSTFPrimeCoefs());
	}

	@Override
	public InverseRealPolynomial getTTFFitEnd() {
		RfCavityBucket rfCavBuc = this.getRfField();
		return new InverseRealPolynomial(rfCavBuc.getTTF_endCoefs());
	}

	@Override
	public InverseRealPolynomial getTTFPrimeFitEnd() {
		RfCavityBucket rfCavBuc = this.getRfField();
		return new InverseRealPolynomial(rfCavBuc.getTTFPrime_endCoefs());
	}

	@Override
	public InverseRealPolynomial getSTFFitEnd() {
		RfCavityBucket rfCavBuc = this.getRfField();
		return new InverseRealPolynomial(rfCavBuc.getSTF_endCoefs());
	}

	@Override
	public InverseRealPolynomial getSTFPrimeFitEnd() {
		RfCavityBucket rfCavBuc = this.getRfField();
		return new InverseRealPolynomial(rfCavBuc.getSTFPrime_endCoefs());
	}
}
