/*
 * SettingsNotice.java
 *
 * Created on July 2, 2002, 2:04 PM
 */

package xal.tools.correlator;


/**
 * StateNotice is an interface for Correlator state events. 
 * 
 * @author  tap
 * @version 
 */
interface StateNotice<RecordType> {
	/**
	 * Handle the source added event.
	 * @param sender The correlator to which the source has been added.
	 * @param name The name identifying the new source.
	 * @param newCount The new number of sources correlated.
	 */
    public void sourceAdded( Correlator<?,RecordType,?> sender, String name, int newCount );
	
	
	/**
	 * Handle the source removed event.
	 * @param sender The correlator from which the source has been removed.
	 * @param name The name identifying the new source.
	 * @param newCount The new number of sources correlated.
	 */
    public void sourceRemoved( Correlator<?,RecordType,?> sender, String name, int newCount );
	
	
	/**
	 * Handle the bin timespan changed event.
	 * @param sender The correlator whose timespan bin has changed.
	 * @param newTimespan The new timespan used by the correlator.
	 */
    public void binTimespanChanged( Correlator<?,RecordType,?> sender, double newTimespan );
	
	
	/**
	 * Handle the advance notice of the correlator stopping.
	 * @param sender The correlator that will stop.
	 */
    public void willStopMonitoring( Correlator<?,RecordType,?> sender );
	
	
	/**
	 * Handle the advance notice of the correlator starting.
	 * @param sender The correlator that will start.
	 */
    public void willStartMonitoring( Correlator<?,RecordType,?> sender );
	
	
	/**
	 * Handle the correlation filter changed event.
	 * @param sender The correlator whose correlation filter has changed.
	 * @param newFilter The new correlation filter to use.
	 */
    public void correlationFilterChanged( Correlator<?,RecordType,?> sender, CorrelationFilter<RecordType> newFilter );
}

