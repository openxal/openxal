/**
 * IEventLogger.java
 *
 *  Created	: Nov 30, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.tools.logging;

import java.util.logging.Level;

/**
 * Defines the basic functionality of a message logging
 * utility for application events.  The source of the messages
 * provided as an argument in the methods.  The sink for the 
 * messages depends upon the particular implementation of the
 * logging service. 
 * 
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Nov 30, 2009
 * @author Christopher K. Allen
 */
public interface IEventLogger {

    /**
     * Logs a configuration message to the event log.  
     *
     * @param clsSrc    the class type posting the message 
     * @param strMsg    event message to log
     * 
     * 
     * @since  Nov 30, 2009
     * @author Christopher K. Allen
     */
    public void logConfig(Class<?> clsSrc, String strMsg);
    
    /**
     * Logs an information message to the event log.  
     *
     * @param clsSrc    the class type posting the message 
     * @param strMsg    event message to log
     * 
     * 
     * @since  Nov 30, 2009
     * @author Christopher K. Allen
     */
    public void logInfo(Class<?> clsSrc, String strMsg);
    
    /**
     * Logs a warning message to the event log.  
     *
     * @param clsSrc    the class type posting the message 
     * @param strMsg    event message to log
     * 
     * 
     * @since  Nov 30, 2009
     * @author Christopher K. Allen
     */
    public void logWarning(Class<?> clsSrc, String strMsg);
    
    /**
     * Logs an error message to the event log.  
     *
     * @param clsSrc    the class type posting the message 
     * @param strMsg    event message to log
     * 
     * 
     * @since  Nov 30, 2009
     * @author Christopher K. Allen
     */
    public void logError(Class<?> clsSrc, String strMsg);
    
    /**
     * Logs an exception event, with optional message string,
     * to the event log.
     *
     * @param clsSrc    the source of the event
     * @param e         the exception we wish to log
     * @param strMsg    an optional message to include in the log
     * 
     * @since  Dec 2, 2009
     * @author Christopher K. Allen
     */
    public void logException(Class<?> clsSrc, Exception e, String ...strMsg);
    
    /**
     * <p>
     * Posts a message of general severity to the event log.  
     * </p>
     * <p>
     * The severity is identified by the 
     * <code>java.util.Logging.Level</code> class.
     * </p>
     *
     * @param lvlSev    severity of the posting
     * @param clsSrc    the class type posting the message 
     * @param strMsg    event message to log
     *
     * @since  Nov 30, 2009
     * @author Christopher K. Allen
     */
    public void postLog(Level lvlSev, Class<?> clsSrc, String strMsg);
}
