/*
 * BinListener.java
 *
 * Created on May 5, 2003, 10:26 AM
 */

package xal.tools.correlator;

/**
 *
 * @author  tap
 */
public interface BinListener<RecordType> {
    public void newCorrelation( BinAgent<RecordType> sender, Correlation<RecordType> correlation );
    public void willReset( BinAgent<RecordType> sender );
}
