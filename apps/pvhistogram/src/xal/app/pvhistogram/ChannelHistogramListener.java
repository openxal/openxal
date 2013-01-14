//
//  ChannelHistogramListener.java
//  xal
//
//  Created by Tom Pelaia on 2/12/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//

package xal.app.pvhistogram;

import xal.tools.statistics.UnivariateStatistics;

import java.util.List;


/** interface for channel histogram messages */
public interface ChannelHistogramListener {
	/** event indicating that the model's channel source has changed */
	public void channelSourceChanged( final ChannelHistogram model, final ChannelSource channelSource );
	
	/** event indicating that the model's histogram has changed */
	public void histogramUpdated( final ChannelHistogram model, final double[] range, final int[] counts, final List<Double> values, final UnivariateStatistics statistics );
}
