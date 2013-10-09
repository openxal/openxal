/*
 * LatticeIterator.java
 *
 * Created on March 19, 2003, 9:30 AM
 */

package xal.sim.slg;

/**
 * The LatticeIterator interface.
 *
 * @author  wdklotz
 */
public interface LatticeIterator {
    public boolean hasNext();
    public Element next();
    public void remove();
}
