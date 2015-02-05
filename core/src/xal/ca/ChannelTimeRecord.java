/*
 * ChannelTimeRecord.java
 *
 * Created on June 28, 2002, 3:03 PM
 */

package xal.ca;

import java.math.BigDecimal;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.FieldPosition;

/**
 * ChannelTimeRecord is a wrapper for channel data that has a value along with 
 * status information and a time stamp.
 *
 * @author  tap
 */
public class ChannelTimeRecord extends ChannelStatusRecord {
    protected Timestamp timestamp;
	
    
    /** 
	 * Creates new ChannelTimeRecord 
	 * @param adaptor from which to generate the record
	 */
    public ChannelTimeRecord(TimeAdaptor adaptor) {
        super(adaptor);
        timestamp = new Timestamp( adaptor.getTimestamp() );
    }
	
	
	/**
	 * Get the timestamp.
	 * @return the timestamp
	 */
	public Timestamp getTimestamp() {
		return timestamp;
	}

    
    /**
     * Get the time stamp in seconds since the Java epoch epoch.  Some 
     * precision is lost as we move away from the epoch since the double 
     * precision number cannot hold the full native precision.
     * @return The time stamp in seconds as a double.
     */
    public double timeStampInSeconds() {
        return timestamp.getSeconds();
    }
    
    
    /**
     * Override the inherited method to return a description of this object.
     * @return A description of this object.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        
        buffer.append( super.toString() );
        buffer.append( ", time: " + timestamp.toString() );
        
        return buffer.toString();
    }
}
