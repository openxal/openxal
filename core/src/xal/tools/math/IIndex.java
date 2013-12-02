/**
 * IIndex.java
 *
 * Author  : Christopher K. Allen
 * Since   : Oct 17, 2013
 */
package xal.tools.math;

/**
 * Interface <code>IIndex</code> is exposed by objects
 * representing matrix and vector indices.  In particular, the <code>enum</code>
 * types that are matrix indices expose this interface.
 *
 *
 * @author Christopher K. Allen
 * @since  Oct 17, 2013
 */
public interface IIndex {

    /**
     * Returns the value of this matrix index object.
     * 
     * @return  the numerical index represented by this object 
     *
     * @author Christopher K. Allen
     * @since  Sep 25, 2013
     */
    public abstract int val();

}