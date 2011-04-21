/*
 * Created on Mar 17, 2004
 */
package xal.model.sync;

import java.util.Map;

import xal.model.IElement;
import xal.model.elem.IRfCavity;
import xal.smf.proxy.RfCavityPropertyAccessor;

/**
 * @author Craig McChesney
 */
public class RfCavitySynchronizer implements Synchronizer {

	/*
	 * @see gov.sns.xal.model.sync.Synchronizer#resync(gov.sns.xal.model.IElement, java.util.Map)
	 */
	public void resync(IElement anElem, Map valueMap)
		throws SynchronizationException {

		if (! (anElem instanceof IRfCavity))
			throw new IllegalArgumentException(
				"expected IRfCavity instance, got: " + 
				anElem.getClass().getName());
		IRfCavity rfCav = (IRfCavity) anElem;
		Double amp = (Double) valueMap.get(
				RfCavityPropertyAccessor.PROPERTY_AMPLITUDE);
		if (amp == null)
			throw new SynchronizationException("missing value for RF Amplitude property");
		rfCav.setCavAmp(amp.doubleValue());

		Double phase = (Double) valueMap.get(
				RfCavityPropertyAccessor.PROPERTY_PHASE);
		if (phase == null)
			throw new SynchronizationException("missing value for RF Phase property");
		rfCav.setCavPhase(amp.doubleValue());
	}

	/*
	 * @see gov.sns.xal.model.sync.Synchronizer#checkSynchronization(gov.sns.xal.model.IElement, java.util.Map)
	 */
	public void checkSynchronization(IElement anElem, Map valueMap)
		throws SynchronizationException {

		if (! (anElem instanceof IRfCavity))
			throw new IllegalArgumentException(
				"expected IRfCavity instance, got: " + 
				anElem.getClass().getName());
		IRfCavity rfCav = (IRfCavity) anElem;
		Double amp = (Double) valueMap.get(
				RfCavityPropertyAccessor.PROPERTY_AMPLITUDE);
		if (amp == null)
			throw new SynchronizationException("missing value for RF Amplitude property");
		if (rfCav.getCavAmp() != amp.doubleValue())
			throw new SynchronizationException("synchronized value doesn't agree with node property");

		Double phase = (Double) valueMap.get(
				RfCavityPropertyAccessor.PROPERTY_PHASE);
		if (phase == null)
			throw new SynchronizationException("missing value for RF Phase property");
		if (rfCav.getCavPhase() != phase.doubleValue())
			throw new SynchronizationException("synchronized value doesn't agree with node property");
	}

}
