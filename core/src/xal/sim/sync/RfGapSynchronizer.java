/*
 * Created on Oct 28, 2003
 */
package xal.sim.sync;

import java.util.Map;

import xal.model.IElement;
import xal.model.elem.sync.IRfGap;
import xal.smf.proxy.RfGapPropertyAccessor;

/**
 * Synchronizes IRfGap lattice elements using the values contained in the
 * supplied map.
 * 
 * @author Craig McChesney
 */
public class RfGapSynchronizer implements Synchronizer {

	// Synchronizer Interface ==================================================
	
	/*
	 * @see xal.model.sync.Synchronizer#resync(xal.model.IElement, java.util.Map)
	 */
	public void resync( final IElement anElem, final Map<String,Double> valueMap ) throws SynchronizationException {
		if ( !(anElem instanceof IRfGap) ) throw new IllegalArgumentException( "expected instance of IRfGap, got: " + anElem.getClass().getName() );
				
		final IRfGap gap = (IRfGap) anElem;
		

		double ETL; // the ETL product
		double phase; // gap phase
		double freq; // gap frequency
		double E0; // The gap voltage

		ETL = getValue( RfGapPropertyAccessor.PROPERTY_ETL, valueMap );
		phase = getValue( RfGapPropertyAccessor.PROPERTY_PHASE, valueMap );
		freq = getValue( RfGapPropertyAccessor.PROPERTY_FREQUENCY, valueMap );
		E0 = getValue( RfGapPropertyAccessor.PROPERTY_E0, valueMap );

		gap.setETL( ETL );
		gap.setPhase( phase );
		gap.setFrequency( freq );
		gap.setE0( E0 );
	}
	
	/*
	 * @see xal.model.sync.Synchronizer#checkSynchronization(xal.model.IElement, java.util.Map)
	 */
	public void checkSynchronization( final IElement anElem, final Map<String,Double> valueMap ) throws SynchronizationException {
		if ( !(anElem instanceof IRfGap) ) throw new IllegalArgumentException( "expected instance of IRfGap, got: " + anElem.getClass().getName() );
		
		final IRfGap gap = (IRfGap) anElem;


		double ETL; // the ETL product
		double phase; // gap phase
		double freq; // gap frequency

		ETL = getValue( RfGapPropertyAccessor.PROPERTY_ETL, valueMap );
		phase = getValue( RfGapPropertyAccessor.PROPERTY_PHASE, valueMap );
		freq = getValue( RfGapPropertyAccessor.PROPERTY_FREQUENCY, valueMap );

		if ( (gap.getETL() != ETL) || (gap.getPhase() != phase) || (gap.getFrequency() != freq) ) throw new SynchronizationException("gap properties don't match element values");
	}


	// Private Support =========================================================
	
	protected double getValue( final String property, final Map<String,Double> values ) throws SynchronizationException {
		final Double val = values.get( property );
		if ( val == null ) throw new SynchronizationException( "Error getting value for property: " + property );
		return val.doubleValue();
	}

}
