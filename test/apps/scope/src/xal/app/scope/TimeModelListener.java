/*
 * TimeModelListener.java
 *
 * Created on July 14, 2003, 9:34 AM
 */

package xal.app.scope;


/**
 * Interface implemented by listeners of the TimeModel events.
 *
 * @author  tap
 */
public interface TimeModelListener {
    /**
     * Event indicating that the time units of the time model sender has changed.
     * @param sender The sender of the event.
     */
    public void timeUnitsChanged(TimeModel sender);
    
    
    /**
     * Event indicating that the time conversion of the time model sender has changed.
     * This is most likely due to the scaling changing.  For example the turn to 
     * microsecond conversion is monitored and may change during the lifetime of 
     * the application.
     * @param sender The sender of the event.
     */
    public void timeConversionChanged(TimeModel sender);
}
