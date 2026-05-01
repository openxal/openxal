/*
 * TraceSource.java
 *
 * Created on Fri Aug 15 10:04:19 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.scope;

import xal.ca.*;
import xal.tools.correlator.*;


/**
 * TraceSource is an interface implemented by models which provide scope traces.
 *
 * @author  tap
 */
interface TraceSource {
	/**
	 * Get the label for this source
	 * @return The source's label
	 */
	public String getLabel();
	
	
    /**
     * Get the trace event for this trace source extracted from the correlation.
	 * @param correlation The correlation from which the trace is extracted.
	 * @return the trace event corresponding to this trace source and the correlation
     */
    public TraceEvent getTraceEvent( Correlation<ChannelTimeRecord> correlation );
	
	
    /**
     * Get the trace for this trace source extracted from the correlation.
	 * @param correlation The correlation from which the trace is extracted.
	 * @return the trace corresponding to this trace source and the correlation
     */
    public double[] getTrace( Correlation<ChannelTimeRecord> correlation );
    
    
    /**
     * Get the array of time elements for the waveform.  Each time element represents
     * the time associated with the corresponding waveform element.  The time unit is 
     * a turn.
     * @return The element times in units of turns relative to cycle start.
     */
    public double[] getElementTimes();
}

