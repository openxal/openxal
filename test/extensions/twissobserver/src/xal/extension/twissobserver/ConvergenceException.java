/**
 * ConvergenceException.java
 *
 * @author Christopher K. Allen
 * @since  Sep 4, 2012
 *
 */

/**
 * ConvergenceException.java
 *
 * @author  Christopher K. Allen
 * @since	Sep 4, 2012
 */
package xal.extension.twissobserver;

import java.util.concurrent.ExecutionException;

/**
 * Exception thrown when iterative process does not converge to a
 * solution.
 *
 * @author Christopher K. Allen
 * @since   Sep 4, 2012
 */
public class ConvergenceException extends ExecutionException {

    /** Serialization version     */
    private static final long serialVersionUID = 1L;

    
    /*
     * Initialization
     */
    
    /**
     * Create a new, uninitialized <code>ConvergenceException</code>.
     *
     * @author  Christopher K. Allen
     * @since   Sep 4, 2012
     */
    public ConvergenceException() {
    }

    /**
     * Create a new <code>ConvergenceException</code> with the given
     * description.
     * 
     * @param strMsg    message describing the exception condition
     *
     * @author  Christopher K. Allen
     * @since   Sep 4, 2012
     */
    public ConvergenceException(String strMsg) {
        super(strMsg);
    }

    /**
     * Create a new <code>ConvergenceException</code> which was initiated by 
     * the given exception object.
     * 
     * @param excOrigin     The original exception that is spawning this one 
     *
     * @author  Christopher K. Allen
     * @since   Sep 4, 2012
     */
    public ConvergenceException(Throwable excOrigin) {
        super(excOrigin);
    }

    /**
     * Create a new <code>ConvergenceException</code> which was initiated by 
     * the given exception object and has the given description.
     * 
     * @param strMsg        message describing the exception condition
     * @param excOrigin     The original exception that is spawning this one 
     *
     * @author  Christopher K. Allen
     * @since   Sep 4, 2012
     */
    public ConvergenceException(String strMsg, Throwable excOrigin) {
        super(strMsg, excOrigin);
    }
    

}
