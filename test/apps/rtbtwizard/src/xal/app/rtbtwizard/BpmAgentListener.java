//
//  BpmAgentListener.java
//  xal
//
//  Created by Tom Pelaia on 7/18/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//

package xal.app.rtbtwizard;


/** listener of BPM Agent events */
public interface BpmAgentListener {
	/** event indicating that the X running average value changed */
	public void xRunningAverageValueChanged( final BpmAgent source, final double value );
	
	
	/** event indicating that the Y running average value changed */
	public void yRunningAverageValueChanged( final BpmAgent source, final double value );
}
