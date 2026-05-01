//
//  CorrelationModelListener.java
//  xal
//
//  Created by Tom Pelaia on 12/10/08.
//  Copyright 2008 Oak Ridge National Lab. All rights reserved.
//

package xal.app.xyzcorrelator;

import xal.ca.*;
import xal.tools.correlator.Correlation;

import java.util.*;


/** interface for capturing correlation model events */
public interface CorrelationModelListener {
	/** the monitored channels have changed */
	public void monitoredChannelsChanged( final List<Channel> channels );
	
	
	/** the plottings channels have changed */
	public void plottingChannelsChanged( final List<Channel> channels );
	
		
	/** correlation captured */
	public void correlationCaptured( final Correlation<ChannelTimeRecord> correlation, final List<ChannelTimeRecord> plotRecords );
}
