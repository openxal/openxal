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
public interface BinListener {
    public void newCorrelation(BinAgent sender, Correlation correlation);
    public void willReset(BinAgent sender);
}
