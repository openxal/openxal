/**
 * NullLogger.java
 *
 *  Created	: Dec 10, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.tools.logging;


import java.util.logging.Level;

/**
 * This logger class has no function.  It is meant to be used
 * as a null object where an <code>IEventLogger</code> type
 * is required.  The log postings are simply ignored.
 *
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 * 
 * @since  Dec 10, 2009
 * @author Christopher K. Allen
 */
public class NullLogger extends EventLoggerBase {


    /**
     * Do nothing.
     *
     * @since 	Dec 10, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.tools.logging.IEventLogger#postLog(java.util.logging.Level, java.lang.Class, java.lang.String)
     */
    @Override
    public void postLog(Level lvlSev, Class<?> clsSrc, String strMsg) {
    }

}
