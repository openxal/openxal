/**
 * PvLoggerException.java
 *
 *  Created	: Mar 19, 2010
 *  Author      : Christopher K. Allen 
 */

package xal.service.pvlogger;

/**
 * Exception thrown for a general runtime error involving
 * the PV logger tools.
 *
 * @since  Mar 19, 2010
 * @author Christopher K. Allen
 */
public class PvLoggerException extends Exception {

    /**  Serialization Version */
    private static final long serialVersionUID = 1L;

    
    /**
     * Create a new <code>PvLoggerException</code> object.
     *
     *
     * @since     Mar 19, 2010
     * @author    Christopher K. Allen
     */
    public PvLoggerException() {
        super();
    }

    /**
     * Create a new <code>PvLoggerException</code> object.
     *
     * @param strMsg    message describing the runtime error
     *
     * @since     Mar 19, 2010
     * @author    Christopher K. Allen
     */
    public PvLoggerException(String strMsg) {
        super(strMsg);
    }

    /**
     * Create a new <code>PvLoggerException</code> object.
     *
     * @param excSrc    the source exception if this except is one of a chain
     *
     * @since     Mar 19, 2010
     * @author    Christopher K. Allen
     */
    public PvLoggerException(Throwable excSrc) {
        super(excSrc);
    }

    /**
     * Create a new <code>PvLoggerException</code> object.
     *
     * @param strMsg    message describing the runtime error
     * @param excSrc    the source exception if this except is one of a chain
     *
     * @since     Mar 19, 2010
     * @author    Christopher K. Allen
     */
    public PvLoggerException(String strMsg, Throwable excSrc) {
        super(strMsg, excSrc);
    }

}
