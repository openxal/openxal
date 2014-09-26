/**
 * ScadaAnnotationException.java
 *
 * @author  Christopher K. Allen
 * @since	Sep 22, 2011
 */
package xal.smf.scada;

/**
 * Exception thrown when the annotations necessary to 
 * describe the SCADA operations, for the current class
 * or data structure, are inconsistent, invalid, or missing.
 * 
 * <p>
 * <b>Ported from XAL on Jul 15, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author Christopher K. Allen
 * @since   Sep 22, 2011
 */
public class ScadaAnnotationException extends RuntimeException {

    /** Serialization version number */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new, empty <code>ScadaAnnotationException</code>. 
     *
     * @author  Christopher K. Allen
     * @since   Sep 22, 2011
     */
    public ScadaAnnotationException() {
        super();
    }

    /**
     * Creates a new <code>ScadaAnnotationException</code> with
     * the given error message. 
     * 
     * @param strMsg    error message for the exception
     *
     * @author  Christopher K. Allen
     * @since   Sep 22, 2011
     */
    public ScadaAnnotationException(String strMsg) {
        super(strMsg);
    }

    /**
     * Creates a new <code>ScadaAnnotationException</code> with
     * the given source exception. 
     * 
     * @param expSrc    exception raising this exception
     *
     * @author  Christopher K. Allen
     * @since   Sep 22, 2011
     */
    public ScadaAnnotationException(Throwable expSrc) {
        super(expSrc);
    }

    /**
     * Creates a new <code>ScadaAnnotationException</code> with
     * the given error message and source exception. 
     * 
     * @param strMsg    error message for the exception
     * @param expSrc    exception raising this exception
     *
     * @author  Christopher K. Allen
     * @since   Sep 22, 2011
     */
    public ScadaAnnotationException(String strMsg, Throwable expSrc) {
        super(strMsg, expSrc);
    }
    
}
