/*
 * CorrelationNotice.java
 *
 * Created on June 27, 2002, 9:25 AM
 */

package xal.tools.correlator;

/**
 * CorrelationNotice is the interface used in notifying listeners that a 
 * new correlation has been found or no correlation has been caught due to
 * a timeout.
 *
 * @author  tap
 */
public interface CorrelationNotice<RecordType> {
	/**
	 * Handle the correlation event.  This method gets called when a correlation was posted.
	 * @param sender The poster of the correlation event.
	 * @param correlation The correlation that was posted.
	 */
    public void newCorrelation( Object sender, Correlation<RecordType> correlation );
	
	
	/**
	 * Handle the no correlation event.  This method gets called when no correlation was found within some prescribed time period.
	 * @param sender The poster of the "no correlation" event.
	 */
    public void noCorrelationCaught( Object sender );
}

