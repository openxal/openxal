/**
 * JavaLogger.java
 *
 *  Created	: Aug 20, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.tools.logging;

import java.util.Calendar;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.XMLFormatter;

/**
 * Implements the <code>IEventLogger</code> interface using the Java
 * <code>Logger</code> class.
 * 
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Aug 20, 2010
 * @author Christopher K. Allen
 * 
 * @see IEventLogger
 * @see java.util.logging.Logger 
 */
public class JavaLogger extends EventLoggerBase {

    
    /*
     * Instance Variables
     */
    
    /** The Java logger name */
    private final String                strName; 
    
    /** flag indicating production of verbose debugging info */
    private final boolean               bolDebug;
    
    
    /** The STDERR console handle */
    private ConsoleHandler              hndCons;
    
    /** The use XML formatter flag */
    private boolean                     bolXmlFmt;

    
    /** The application logger object */
    private Logger                      lgrEvts;
    

    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>FileLogger</code> object with
     * the given append property.
     *
     * @param strName   Name of the logger - use <code>Logger.GLOBAL_LOGGER_NAME</code> to 
     *                  request the Java global logger.
     * @param bolDebug  Verbose debugging information (to console)
     * @param bolLogOn  enable logging if <code>true</code>
     *                  disable otherwise
     *
     * @since     Nov 25, 2009
     * @author    Christopher K. Allen
     */
    public JavaLogger(String strName, boolean bolDebug, boolean bolLogOn) {
        this.strName  = strName;
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
            
            this.hndCons.setFormatter( fmtXml );

        } else {
            SimpleFormatter     fmtSmp = new SimpleFormatter();
            
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
        this.lgrEvts = Logger.getLogger(this.strName);

        // Create the output file handler and the console handler atomically
        Formatter   fmtDefault = new SimpleFormatter();

        this.hndCons = new ConsoleHandler();
        this.hndCons.setFormatter(fmtDefault);
            
        
        // Add the message handlers
        if (this.bolDebug) {
            this.hndCons.setLevel(Level.ALL);
            this.lgrEvts.addHandler(this.hndCons);

        } else {
            this.hndCons.setLevel(Level.ALL);
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
        
        // Flush the stream to the logging file and close
        this.hndCons.close();
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
     * @since   Dec 16, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.tools.logging.IEventLogger#postLog(java.util.logging.Level, java.lang.Class, java.lang.String)
     */
    @Override
    public void postLog(Level lvlSev, Class<?> clsSrc, String strMsg) {

        this.lgrEvts.logp(lvlSev, clsSrc.getName(), "", strMsg + "\n");
    }
}
