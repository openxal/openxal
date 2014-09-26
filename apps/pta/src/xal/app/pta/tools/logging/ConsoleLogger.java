/**
 * ConsoleLogger.java
 *
 *  Created	: Dec 10, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.tools.logging;


import java.util.logging.Level;

/**
 * Logger implementation that send log postings to the 
 * console (standard output).
 *
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Dec 10, 2009
 * @author Christopher K. Allen
 */
public class ConsoleLogger extends EventLoggerBase {

//    /**
//     * Logs an error message to the console.
//     *
//     * @param clsSrc    the class type posting the message 
//     * @param strMsg    event message to log
//     * 
//     * @since 	Dec 10, 2009
//     * @author  Christopher K. Allen
//     *
//     * @see xal.app.pta.tools.IEventLogger#logError(java.lang.Class, java.lang.String)
//     */
//    @Override
//    public void logError(Class<?> clsSrc, String strMsg) {
//        this.postLog(Level.SEVERE, clsSrc, strMsg);
//    }
//
//    /**
//     * Logs an information message to the console.
//     *
//     * @param clsSrc    the class type posting the message 
//     * @param strMsg    event message to log
//     *
//     * @since 	Dec 10, 2009
//     * @author  Christopher K. Allen
//     *
//     * @see xal.app.pta.tools.IEventLogger#logInfo(java.lang.Class, java.lang.String)
//     */
//    @Override
//    public void logInfo(Class<?> clsSrc, String strMsg) {
//        this.postLog(Level.INFO, clsSrc, strMsg);
//    }
//
//    /**
//     * Logs a warning message to the console.
//     *
//     * @param clsSrc    the class type posting the message 
//     * @param strMsg    event message to log
//     *
//     * @since 	Dec 10, 2009
//     * @author  Christopher K. Allen
//     *
//     * @see xal.app.pta.tools.IEventLogger#logWarning(java.lang.Class, java.lang.String)
//     */
//    @Override
//    public void logWarning(Class<?> clsSrc, String strMsg) {
//        this.postLog(Level.WARNING, clsSrc, strMsg);
//    }
//
    /**
     * <p>
     * Posts a message to the console with the given severity. 
     * </p>
     * <p>
     * Currently the application logging consists of both
     * a formal <code>Logger</code> object and a local logging
     * file in the resources directory.
     * </p>
     *
     * @param lvlSev    severity of the posting
     * @param clsSrc    the class type posting the message 
     * @param strMsg    event message to log
     *
     *
     * @since 	Dec 10, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.tools.logging.IEventLogger#postLog(java.util.logging.Level, java.lang.Class, java.lang.String)
     */
    @Override
    public void postLog(Level lvlSev, Class<?> clsSrc, String strMsg) {
        String  strHdr  = clsSrc.getName() + ":" + strMsg + "\n";
        String  strLog = lvlSev.getName() + ":" + strHdr;
        
        System.out.println(strLog);
    }

    /**
//     * <p>
//     * Post an exception event to the console.  The exception type and message is
//     * also written out, along with the message string if non-null.
//     * </p>
//     * 
//     * @since   Dec 10, 2009
//     * @author  Christopher K. Allen
//     *
//     * @see xal.app.pta.tools.IEventLogger#logException(java.lang.Class, java.lang.Exception, java.lang.String[])
//     */
//    @Override
//    public void logException(Class<?> clsSrc, Exception e, String... strMsg) {
//        Class<? extends Exception>      clsExc = e.getClass();
//        
//        String  strPost = clsExc.getName() + ":Exception thrown - " + e.getLocalizedMessage();
//        if (strMsg != null)
//            if (strMsg.length == 1)
//                strPost += ": " + strMsg;
//        
//        this.postLog(Level.SEVERE, clsSrc, strPost);
//    }
//
}
