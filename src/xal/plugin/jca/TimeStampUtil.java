/*
 * TimeStampUtil.java
 *
 * Created on July 1, 2002, 1:43 PM
 */

package xal.plugin.jca;

import gov.aps.jca.dbr.TimeStamp;
import java.util.Date;

/**
 * Utility for handling the JCA timestamp and converting to Java time.
 * @author  tap
 */
class TimeStampUtil {
    // The epics timestamp has an epoch of 1990 rather than Java's epoch of 1970
    // There are 7305 days between 1/1/1970 and 1/1/1990 (accounts for leap year)
    static public final double epochSecondsOffset = 7305*24*3600;
    
    /**
     * return the timestamp as a Java Date
     * Some precision is lost since the Date class only supports millisecond resolution
     */
    static public Date date( final TimeStamp timestamp ) {
        double seconds = javaSeconds( timestamp );
        long milliSeconds = (long)( 1000 * seconds );
        
        return new Date( milliSeconds );
    }
    
    
    static public double javaSeconds( final TimeStamp timestamp ) {
        return timestamp.asDouble() + epochSecondsOffset; 
    }
    
    
    static public double epicsSeconds( final double javaSeconds ) {
        return javaSeconds - epochSecondsOffset;        
    }
    
    
    /** Creates new TimeStampUtil */
    protected TimeStampUtil() {
    }
}
