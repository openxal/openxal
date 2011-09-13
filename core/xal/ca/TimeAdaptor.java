/*
 * TimeAdaptor.java
 *
 * Created on August 27, 2002, 9:25 AM
 */

package xal.ca;

/**
 *
 * @author  tap
 */
public interface TimeAdaptor extends StatusAdaptor {
    /** Time stamp in seconds since the epoch used by Java */
    public java.math.BigDecimal getTimestamp();
}
