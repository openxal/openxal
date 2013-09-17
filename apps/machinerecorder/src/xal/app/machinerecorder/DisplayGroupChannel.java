//
// DisplayGroupChannel.java
// XAL Active
//
// Created by Tom Pelaia on 3/20/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.app.machinerecorder;


/** Represents a channel in the context of a group */
public class DisplayGroupChannel extends ContextualChannel {
	/** Constructor */
    public DisplayGroupChannel( final ChannelWrapper wrapper, final boolean enablePlotting ) {
		super( wrapper, enablePlotting );
    }
	
	
	/** Determine whether the channel should be plotted */
	public boolean getEnablePlotting() {
		return super.getEnabled();
	}
	
	
	/** Set whether the channel should be plotted */
	public void setEnablePlotting( final boolean shouldPlot ) {
		super.setEnabled( shouldPlot );
	}
}
