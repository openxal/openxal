/*
 * TimeAdaptor.java
 *
 * Created on August 26, 2002, 5:41 PM
 */

package xal.plugin.jca;

import xal.ca.TimeAdaptor;

import gov.aps.jca.dbr.*;

import java.math.BigDecimal;
import java.util.Date;


/**
 * Wrap a DBR TIME record for high level access
 *
 * @author  tap
 */
class DbrTimeAdaptor extends DbrStatusAdaptor implements TimeAdaptor {
	// constants
	/** Offset in seconds between the EPICS native epoch and the Java epoch */
	final static BigDecimal EPOCH_SECONDS_OFFSET = new BigDecimal( 7305*24*3600 );     // offset from standard Java epoch
	
	
    /** Creates a new instance of TimeAdaptor */
    public DbrTimeAdaptor( final DBR dbr ) {
        super( dbr );
    }
    	    
    
    /**
     * Time stamp in seconds since the Java epoch
     * Epics only provides nanosecond accuracy, so we limit the timestamp to 
     * nine decimal places to the right of the the decimal point.
     */
    public BigDecimal getTimestamp() {
        return convertToJavaTime( getRawTimestamp() );
    }
    
    
    /**
     * Time stamp in seconds since the Epics epoch of January 1, 1990
     * Epics only provides nanosecond accuracy, so we limit the timestamp to 
     * nine decimal places to the right of the the decimal point.
     */
    public BigDecimal getRawTimestamp() {
        return ((TIME)_dbr).getTimeStamp().asBigDecimal();
    }
    
    
    /**
     * Convert the raw time in seconds since January 1, 1990 to seconds since
     * the Java epoch with nanosecond precision.
     * @param rawSeconds The number of seconds since January 1, 1990.
     * @return The time in seconds since the Java epoch.
     */
    static protected BigDecimal convertToJavaTime( final BigDecimal rawSeconds ) {
        return rawSeconds.add( EPOCH_SECONDS_OFFSET ).setScale( 9, BigDecimal.ROUND_HALF_UP );
    }
}

