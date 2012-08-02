/*
 * ChannelRecordFilter.java
 *
 * Created on July 23, 2002, 1:42 PM
 */

package xal.tools.correlator;

/**
 * <code>RecordFilter</code> is used in the correlator to accept or 
 * reject a record read for a particular channel.  For example, the filter 
 * could be used to throw away a BPM reading where the signal is below some 
 * threshold.
 *
 * @author  tap
 */
public interface RecordFilter<RecordType> {
    public boolean accept( final RecordType record );
}
