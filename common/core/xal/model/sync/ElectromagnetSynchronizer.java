/*
 * Created on Oct 28, 2003
 */
package xal.model.sync;

import java.util.Map;

import xal.model.IElement;
import xal.model.elem.IElectromagnet;

import xal.smf.proxy.ElectromagnetPropertyAccessor;

/**
 * Synchronizes IElectromagnet elements using the supplied value map.
 * 
 * @author Craig McChesney
 */
public class ElectromagnetSynchronizer implements Synchronizer {

	/*
	 * @see gov.sns.xal.model.sync.Synchronizer#resync(gov.sns.xal.model.IElement, java.util.Map)
	 */
	public void resync(IElement anElem, Map valueMap)
			throws SynchronizationException {
		if (! (anElem instanceof IElectromagnet))
			throw new IllegalArgumentException(
				"expected IElectromagnet instance, got: " + 
				anElem.getClass().getName());
		IElectromagnet mag = (IElectromagnet) anElem;
		Double field = (Double) valueMap.get(
				ElectromagnetPropertyAccessor.PROPERTY_FIELD);
		if (field == null)
			throw new SynchronizationException("missing value for Field property");
		mag.setMagField(field.doubleValue());
	}

	/*
	 * @see gov.sns.xal.model.sync.Synchronizer#checkSynchronization(gov.sns.xal.model.IElement, java.util.Map)
	 */
	public void checkSynchronization(IElement anElem, Map valueMap) 
			throws SynchronizationException {
		if (! (anElem instanceof IElectromagnet))
			throw new IllegalArgumentException(
				"expected IElectromagnet instance, got: " + 
				anElem.getClass().getName());
		IElectromagnet mag = (IElectromagnet) anElem;
		Double field = (Double) valueMap.get(
				ElectromagnetPropertyAccessor.PROPERTY_FIELD);
		if (field == null)
			throw new SynchronizationException("missing value for Field property");
		if (mag.getMagField() != field.doubleValue())
			throw new SynchronizationException("synchronized value doesn't agree with node property");
	}

}
