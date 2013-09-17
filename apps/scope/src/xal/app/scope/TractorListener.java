/*
 * TractorListener.java
 *
 * Created on August 8, 2003, 3:04 PM
 */

package xal.app.scope;

/**
 * TractorListener receives TractorKnob value change events. 
 *
 * @author  tap
 */
public interface TractorListener {
    /**
     * Handle the value changed event from the tractor knob.
     * @param knob The knob that posted the value change event.
     * @param value The new value.
     */
    public void valueChanged(TractorKnob knob, long value);
}
