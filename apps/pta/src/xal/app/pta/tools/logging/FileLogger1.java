/**
 * FileLogger.java
 *
 *  Created	: Nov 25, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.tools.logging;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Writes (unformatted) logging information to 
 * an output stream.
 * 
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Nov 25, 2009
 * @author Christopher K. Allen
 * 
 */
public class FileLogger1 extends EventLoggerBase {


    /*
     * Instance Attributes
     */
    
    /** The backing store file descriptor */
    private final File                        fileLog;
    
    /** flag indicating use of a continual logging */
    private final boolean                     bolAppend;
    
    /** flag indicating production of verbose debugging info */
    private final boolean                     bolDebug;
    
    
    /** The application logger object */
    private Logger                      lgrEvts;
    
    /** The application logging stream */
    private OutputStreamWriter          wtrLog;
    

    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>FileLogger</code> object with
     * the given append property.
     *
     * @param strUrl    location of the log file backing store
     * @param bolAppend creates a continual log if true 
     *                  (appends to existing log file)
     * @param bolDebug  Verbose debugging information (to console)
     *
     * @since     Nov 25, 2009
     * @author    Christopher K. Allen
     */
    public FileLogger1(String strUrl, boolean bolAppend, boolean bolDebug) {
        this( new File(strUrl), bolAppend, bolDebug );
    }
    
    /**
     * Create a new <code>FileLogger</code> object.
     *
     * @param fileLog   location of the log file backing store
     * @param bolAppend creates a continual log if true 
     *                  (appends to existing log file)
     * @param bolDebug  Verbose debugging information (to console)
     *
     * @since     Nov 25, 2009
     * @author    Christopher K. Allen
     */
    public FileLogger1(File fileLog, boolean bolAppend, boolean bolDebug) {
        this.fileLog   = fileLog;
        this.bolAppend = bolAppend;
        this.bolDebug  = bolDebug;
    }
    
    
    
    /*
     * Logging Operations
     */
    
    /**
     * Setup the logging services and begin logging.
     *
     * 
     * @since  Nov 12, 2009
     * @author Christopher K. Allen
     */
    public void beginLogging() {
        
        // Create the formal logging object
        String          strClsName = this.getClass().getName();
        this.wtrLog = null;
        this.lgrEvts = Logger.getLogger(strClsName);
        
        
        // Create the logging output file and write out log buffer
        try {
            OutputStream          osLog      = new FileOutputStream(this.fileLog, this.bolAppend);
            OutputStreamWriter    wtrLog     = new OutputStreamWriter(osLog);
            
            this.wtrLog = wtrLog;
            
        } catch (FileNotFoundException e) {
            String      strErr = "SERIOUS LOGGER ERROR: Logging file not found: " + this.fileLog.getPath();
            
            this.lgrEvts.log(Level.SEVERE, strErr);
            System.err.println(strErr);
            e.printStackTrace();
            
        } catch (SecurityException e) {
            String      strErr = "SERIOUS LOGGER ERROR: Log file insufficient access privileges: " + this.fileLog.getPath();
            
            this.lgrEvts.log(Level.SEVERE, strErr);
            System.err.println(strErr);
            e.printStackTrace();
        }
        
        // Log that we are closing the application
        this.logInfo(this.getClass(), "Logging service started: " + Calendar.getInstance().getTime().toString() );
    }
    

    /**
     * Shut down the logging service.  Closes log file.
     *
     * 
     * @since  Nov 19, 2009
     * @author Christopher K. Allen
     */
    public void endLogging() {
        // Log that we are closing the application
        this.logInfo(this.getClass(), "Logging service stopped: " + Calendar.getInstance().getTime().toString() );
        
        try {
            
            // Flush the stream to the logging file and close
            this.wtrLog.flush();
            this.wtrLog.close();


        } catch (SecurityException e) {
            String      strErr = "SERIOUS LOGGER ERROR: Log file insufficient access privileges: " + this.fileLog.getPath();
            
            this.lgrEvts.log(Level.SEVERE, strErr);
            e.printStackTrace();

        } catch (FileNotFoundException e) {
            String      strErr = "SERIOUS LOGGER ERROR: Logging file not found: " + this.fileLog.getPath();

            this.logError(this.getClass(), strErr);
            System.err.println(strErr);
            e.printStackTrace();
            
        } catch (IOException e) {
            String      strErr = "SERIOUS LOGGER ERROR: General log file I/O failure: " + this.fileLog.getPath();

            this.logError(this.getClass(), strErr);
            System.err.println(strErr);
            e.printStackTrace();
        }
        
    }
    

    
    /*
     * Logging Methods
     */

    
//    /**
//     * Logs an information message to the application 
//     * event log.  Might also send the message to the
//     * console.
//     *
//     * @param clsSrc    the class type posting the message 
//     * @param strMsg    event message to log
//     * 
//     * @since  Nov 10, 2009
//     * @author Christopher K. Allen
//     * 
//     * @see     MainApplication#postLog(Level, Class, String)
//     */
//    public void logInfo(Class<?> clsSrc, String strMsg) {
//        this.postLog(Level.INFO, clsSrc, strMsg);
//    }
//
//    /**
//     * Logs a warning message to the application 
//     * event log.  Might also send the message to the
//     * console.
//     *
//     * @param clsSrc    the class type posting the message 
//     * @param strMsg    event message to log
//     * 
//     * @since  Nov 10, 2009
//     * @author Christopher K. Allen
//     * 
//     * @see     MainApplication#postLog(Level, Class, String)
//     */
//    public void logWarning(Class<?> clsSrc, String strMsg) {
//        this.postLog(Level.WARNING, clsSrc, strMsg);
//    }
//
//    /**
//     * Logs an error message to the application 
//     * event log.  Might also send the message to the
//     * console.
//     *
//     * @param clsSrc    the class type posting the message 
//     * @param strMsg    event message to log
//     * 
//     * @since  Nov 10, 2009
//     * @author Christopher K. Allen
//     * 
//     * @see     MainApplication#postLog(Level, Class, String)
//     */
//    public void logError(Class<?> clsSrc, String strMsg) {
//        this.postLog(Level.SEVERE, clsSrc, strMsg);
//    }
//    
    /**
     * <p>
     * Posts a message to the application 
     * event log.  Might also send the message to the
     * console.
     * </p>
     * <p>
     * Currently the application logging consists of both
     * a formal <code>Logger</code> object and a local logging
     * file in the resources directory.
     * </p>
     *
     * @param lvlEvent  severity of the posting
     * @param clsSrc    the class type posting the message 
     * @param strMsg    event message to log
     *
     * @since  Nov 12, 2009
     * @author Christopher K. Allen
     */
    @Override
    public void postLog(Level lvlEvent, Class<?> clsSrc, String strMsg) {
        String  strLog  = clsSrc.getName() + ":" + strMsg + "\n";
        String  strFile = lvlEvent.getName() + ":" + strLog;
        
        if (this.bolDebug) 
            this.lgrEvts.log(lvlEvent, strLog);
        
        try {
            this.wtrLog.write(strFile);
            this.wtrLog.flush();
            
            
        } catch (NullPointerException e) {
            String      strErr = "SERIOUS LOGGER ERROR: Logging file unopen";
            
            this.lgrEvts.log(Level.SEVERE, strErr);
            System.err.println(strErr);
            e.printStackTrace();
            
        } catch (IOException e) {
            String      strErr = "SERIOUS LOGGER ERROR: Cannot write above log to file. ";
            
            this.lgrEvts.log(Level.SEVERE, strErr);
            System.err.println(strLog);
            System.err.print(strErr);
            e.printStackTrace();
        }
    }

//    /**
//     * <p>
//     * Post the exception to the log.  The exception type and message is
//     * written to the log, along with the message string if non-null.
//     * </p>
//     *
//     * @since 	Dec 2, 2009
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
//
}
