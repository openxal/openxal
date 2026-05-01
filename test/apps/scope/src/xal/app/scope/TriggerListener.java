/*
 * TriggerListener.java
 *
 * Created on May 30, 2003, 3:32 PM
 */

package xal.app.scope;

/**
 * Interface that specifies events posted by a Trigger instance.
 *
 * @author  tap
 */
public interface TriggerListener {
    /**
     * Event indicating that the specified trigger has been enabled.
     * @param source Trigger posting the event.
     */
    public void triggerEnabled(Trigger source);
    
    
    /**
     * Event indicating that the specified trigger has been disabled.
     * @param source Trigger posting the event.
     */
    public void triggerDisabled(Trigger source);
    
    
    /**
     * Event indicating that the trigger channel state has changed.
     * @param source Trigger posting the event.
     */
    public void channelStateChanged(Trigger source);
}
