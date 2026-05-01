/*
 * ChartModelListener.java
 *
 * Created on Mon Jul 19 13:03:26 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.orbitcorrect;

import xal.extension.widgets.plot.FunctionGraphsJPanel;

import java.util.List;


/**
 * ChartModelListener
 *
 * @author  tap
 * @since Jul 19, 2004
 */
public interface ChartModelListener {
	/**
	 * An event indicating that the chart model's trace sources have changed.
	 * @param model the chart model which posted the event
	 * @param traceSources the chart model's new trace sources
	 */
	public void traceSourcesChanged(ChartModel model, List<TraceSource> traceSources);
	
	
	/**
	 * An event indicating that the chart's properties have changed.
	 * @param model the chart model which posted the event
	 * @param chart the chart whose properties have changed
	 */
	public void chartPropertiesChanged(ChartModel model, FunctionGraphsJPanel chart);
}

