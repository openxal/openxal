/*
 * TraceListener.java
 *
 * Created on December 17, 2002, 4:42 PM
 */

package xal.app.scope;

import xal.ca.*;

import java.util.List;
import java.util.Date;

/**
 * update the display to reflect the latest data
 *
 * @author  tap
 */
public interface TraceListener {
	/**
	 * Listener of trace events.
	 * @param sender the source of the trace event
	 * @param traceEvents the trace events captured
	 * @param timestamp the average time of all of the trace events
	 */
    public void updateTraces(Object sender, TraceEvent[] traceEvents, Date timestamp);
}
