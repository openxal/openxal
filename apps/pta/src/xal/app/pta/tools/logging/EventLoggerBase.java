/**
 * EventLoggerBase.java
 *
 *  Created	: Dec 16, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.tools.logging;


import java.util.logging.Level;

/**
 * <p>
 * Base class for <code>IEventLogger</code> implementations.
 * This class performs most boilerplate functions required
 * of the interface.  One need only implement the
 * method
 * <br/>
 * <br/>
 *      postLog(Level,Class<?>,String)
 * <br/>
 * <br/>
 * to satisfy the interface requirements.
 * </p>
 * 
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Dec 16, 2009
 * @author Christopher K. Allen
 */
public abstract class EventLoggerBase implements IEventLogger {

    
    /*
     * Instances Attributes
     */
    
    /** Logging on/off toggle */
    private boolean     bolOn;
    
    
    
    
    /*
     * Operations
     */
    
    /**
     * Toggles the logging on and off. 
     *
     * @param bolOn     value <code>true</code> turns on logging 
     *
     * @since  Feb 26, 2010
     * @author Christopher K. Allen
     */
    public void setLogging(boolean bolOn) {
        this.bolOn = bolOn;
    }


    /**
     * Return the current logging state. 
     *
     * @return <code>true</code> if logging is on
     *         <code>false</code> is logging is off
     *
     * @since  Feb 26, 2010
     * @author Christopher K. Allen
     */
    public boolean isLoggingOn() {
        return bolOn;
    }


    
    /**
     * <p>
     * Derived classes must implement this method in order to complete
     * the <code>IEventLogger</code> interface.
     * </p>
     * <p>
     * This method posts a message to the log with the given
     * level of severity.
     *
     * @since   Dec 16, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.tools.logging.IEventLogger#postLog(java.util.logging.Level, java.lang.Class, java.lang.String)
     * @see java.util.logging.Level
     */
    @Override
    abstract public void postLog(Level lvlSev, Class<?> clsSrc, String strMsg);


    /**
     * Posts an information message to the log.
     *
     * @param clsSrc    the class type posting the message 
     * @param strMsg    event message to log
     * 
     * @since   Dec 16, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.tools.logging.IEventLogger#logInfo(java.lang.Class, java.lang.String)
     */
    @Override
    public void logConfig(Class<?> clsSrc, String strMsg) {
        if (this.bolOn)
            this.postLog(Level.CONFIG, clsSrc, strMsg);
    }

    /**
     * Posts an information message to the log.
     *
     * @param clsSrc    the class type posting the message 
     * @param strMsg    event message to log
     * 
     * @since   Dec 16, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.tools.logging.IEventLogger#logInfo(java.lang.Class, java.lang.String)
     */
    @Override
    public void logInfo(Class<?> clsSrc, String strMsg) {
        if (this.bolOn)
            this.postLog(Level.INFO, clsSrc, strMsg);
    }

    /**
     * Posts a warning message to the log.
     * 
     * @param clsSrc    the class type posting the message 
     * @param strMsg    event message to log
     * 
     * @since 	Dec 16, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.tools.logging.IEventLogger#logWarning(java.lang.Class, java.lang.String)
     */
    @Override
    public void logWarning(Class<?> clsSrc, String strMsg) {
        if (this.bolOn)
            this.postLog(Level.WARNING, clsSrc, strMsg);
    }

    /**
     *
     * @param clsSrc    the class type posting the message 
     * @param strMsg    event message to log
     * 
     * @since 	Dec 16, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.tools.logging.IEventLogger#logError(java.lang.Class, java.lang.String)
     */
    @Override
    public void logError(Class<?> clsSrc, String strMsg) {
        if (this.bolOn)
            this.postLog(Level.SEVERE, clsSrc, strMsg);
    }

    /**
     *
     * @param clsSrc    the class type posting the message
     * @param e         the exception thrown 
     * @param strMsg    event message to log
     * 
     * @since 	Dec 16, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.tools.logging.IEventLogger#logException(java.lang.Class, java.lang.Exception, java.lang.String[])
     */
    @Override
    public void logException(Class<?> clsSrc, Exception e, String... strMsg) {
        if (this.bolOn) {
            Class<? extends Exception>      clsExc = e.getClass();

            String  strPost = clsExc.getName() + ":Exception thrown - " + e.getLocalizedMessage();
            if (strMsg != null)
                if (strMsg.length == 1)
                    strPost += ": " + strMsg;

            this.postLog(Level.SEVERE, clsSrc, strPost);
        }
    }


}
