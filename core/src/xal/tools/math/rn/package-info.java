/**
 * package-info.java
 *
 * Author  : Christopher K. Allen
 * Since   : Jul 21, 2015
 */
/**
 * <p>
 * Package for doing linear algebra in the general base space of <b>R</b><sup><i>n</i></sup>
 * where <i>n</i> is a positive integer.  Thus, the shape of vectors and matrices can be 
 * arbitrary, including non-square matrices.  This package is essentially a small, 
 * self-contained matrix package useful for doing basic matrix-vector operations.
 * </p>
 * <p> 
 * There exist sub-packages in <code>xal.tools.math</code> for doing linear algebra that
 * are optimized for specific values of <i>n</i>, those that occur frequently in beam
 * physics.  Those packages should be used whenever possible, as the operations performed
 * herein for the general case are necessarily more expensive.
 * </p>
 *
 * @author Christopher K. Allen
 * @since  Jul 21, 2015
 */
package xal.tools.math.rn;