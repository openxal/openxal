/**
 * FmtFileLogger.java
 *
 *  Created	: Dec 16, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.tools.logging;


import java.io.IOException;
import java.util.Calendar;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.XMLFormatter;

/**
 * Saves logging messages to file in formatted text.  Either
 * XML or a simple ASCII text format are possible in the
 * current implementation.
 * 
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Dec 16, 2009
 * @author Christopher K. Allen
 */
public class FileLogger2 extends EventLoggerBase {

    
    
    /*
     * Instance Attributes
     */
    
    /** The URL of the logging file */
    private final String                strUrl;
    
    /** flag indicating use of a continual logging */
    private final boolean               bolAppend;
    
    /** flag indicating production of verbose debugging info */
    private final boolean               bolDebug;
    
    
    
    /** The logging file handle */
    private FileHandler                 hndFile;
    
    /** The STDERR console handle */
    private ConsoleHandler              hndCons;
    
    /** The use XML formatter flag */
    private boolean                     bolXmlFmt;

    
    /** The application logger object */
    private Logger                      lgrEvts;
    

    
    
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
    public FileLogger2(String strUrl, boolean bolAppend, boolean bolDebug) {
        this.strUrl = strUrl;
        this.bolAppend = bolAppend;
        this.bolDebug = bolDebug;
        
        this.bolXmlFmt = false;
    }
    
    /**
     * Create a new <code>FileLogger</code> object with
     * the given append property.
     *
     * @param strUrl    location of the log file backing store
     * @param bolAppend creates a continual log if true 
     *                  (appends to existing log file)
     * @param bolDebug  Verbose debugging information (to console)
     * @param bolLogOn  enable file logging if <code>true</code>
     *                  disable otherwise
     *
     * @since     Nov 25, 2009
     * @author    Christopher K. Allen
     */
    public FileLogger2(String strUrl, boolean bolAppend, boolean bolDebug, boolean bolLogOn) {
        this.strUrl = strUrl;
        this.bolAppend = bolAppend;
        this.bolDebug = bolDebug;
        
        this.setLogging(bolLogOn);

        this.bolXmlFmt = false;
    }
    
    
    
    /*
     * Logging Operations
     */
    
    /**
     * <p>
     * Toggle between XML formatting and
     * simple formatting for logged text.
     *
     * @param bolXmlFmt <code>true</code> coerces the output to XML
     *                  <code>false</code> uses a simple text format
     * 
     * @since  Dec 16, 2009
     * @author Christopher K. Allen
     */
    public void setXmlFormat(boolean bolXmlFmt) {
        if (bolXmlFmt = this.bolXmlFmt)
            return;
        
        this.bolXmlFmt = bolXmlFmt;
        
        if (bolXmlFmt = true) {
            XMLFormatter        fmtXml = new XMLFormatter();
            
            this.hndFile.setFormatter( fmtXml );
            this.hndCons.setFormatter( fmtXml );

        } else {
            SimpleFormatter        fmtSmp = new SimpleFormatter();
            
            this.hndFile.setFormatter( fmtSmp );
            this.hndCons.setFormatter( fmtSmp );
            
        }
    }
    
    /**
     * Setup the logging services and begin logging.
     *
     * @return  <code>true</code> if logging service was 
     * successfully started. 
     * 
     * @since  Nov 12, 2009
     * @author Christopher K. Allen
     */
    public boolean beginLogging() {
        
        // Create the formal logging object
        String          strClsName = this.getClass().getName();
        this.lgrEvts = Logger.getLogger(strClsName);

        // Create the output file handler and the console handler atomically
        try {
            Formatter   fmtDefault = new SimpleFormatter();
            
            this.hndFile = new FileHandler(strUrl, bolAppend);
            this.hndCons = new ConsoleHandler();
            
            this.hndFile.setFormatter(fmtDefault);
            this.hndCons.setFormatter(fmtDefault);
            
            
        } catch (IllegalArgumentException e) {
            String      strErr = "SERIOUS LOGGER ERROR: Bad format in file name: " + this.strUrl;
            
            System.err.println(strErr);
            this.lgrEvts.log(Level.SEVERE, strErr);
            e.printStackTrace();
            return false;

        } catch (IOException e) {
            String      strErr = "SERIOUS LOGGER ERROR: Logging file not found: " + this.strUrl;
            
            System.err.println(strErr);
            this.lgrEvts.log(Level.SEVERE, strErr);
            e.printStackTrace();
            return false;
            
        } catch (SecurityException e) {
            String      strErr = "SERIOUS LOGGER ERROR: Log file insufficient access privileges: " + this.strUrl;
            
            System.err.println(strErr);
            this.lgrEvts.log(Level.SEVERE, strErr);
            e.printStackTrace();
            return false;
        }
        
        // Add the message handlers
        if (this.bolDebug) {
            this.hndCons.setLevel(Level.ALL);
            this.hndFile.setLevel(Level.ALL);

            this.lgrEvts.addHandler(this.hndFile);
            this.lgrEvts.addHandler(this.hndCons);

        } else {
            this.hndCons.setLevel(Level.ALL);
            this.hndFile.setLevel(Level.ALL);
//            this.hndCons.setLevel(Level.WARNING);
//            this.hndFile.setLevel(Level.WARNING);
        
            this.lgrEvts.addHandler(this.hndFile);
        }
        
        // Log that we have started logging
        this.logInfo(this.getClass(), "Logging service started: " + Calendar.getInstance().getTime().toString() );
        return true;
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
            this.hndCons.close();
            this.hndFile.close();


        } catch (SecurityException e) {
            String      strErr = "SERIOUS LOGGER ERROR: Log file insufficient access privileges: " + this.hndFile.toString();
            
            this.lgrEvts.log(Level.SEVERE, strErr);
            e.printStackTrace();
        }
        
    }
    

    
    
    
    
    
    /**
     * Log a message, specifying source class and method, with no 
     * arguments. If the logger is currently enabled for the 
     * given message level then the given message is forwarded to 
     * all the registered output Handler objects.
     * 
     * @param lvlSev    severity of the posting
     * @param clsSrc    the source class type posting the message 
     * @param strMsg    event message to log
     *  
     * @since 	Dec 16, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.tools.logging.IEventLogger#postLog(java.util.logging.Level, java.lang.Class, java.lang.String)
     */
    @Override
    public void postLog(Level lvlSev, Class<?> clsSrc, String strMsg) {

        this.lgrEvts.logp(lvlSev, clsSrc.getName(), "", strMsg + "\n");
    }
}
