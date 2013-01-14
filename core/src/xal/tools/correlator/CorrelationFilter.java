/*
 * CorrelationFilter.java
 *
 * Created on July 25, 2002, 1:55 PM
 */

package xal.tools.correlator;

/**
 * Interface for a filter which can accept or reject a correlation based on 
 * some criteria detemined by the implementation of the filter.
 *
 * @author  tap
 */
public interface CorrelationFilter<RecordType> {
    public boolean accept( Correlation<RecordType> correlation, int fullCount );
}
