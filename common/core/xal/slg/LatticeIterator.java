/*
 * LatticeIterator.java
 *
 * Created on March 19, 2003, 9:30 AM
 */

package xal.slg;

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
