//
//  KnobElementListener.java
//  xal
//
//  Created by Thomas Pelaia on 12/7/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.app.knobs;

import xal.ca.*;


/** Knob element event */
public interface KnobElementListener {
	/** event indicating that the knob element's channel has changed */
	public void channelChanged( final KnobElement element, final Channel channel );
	
	
	/** event indicating that the element's coefficient has changed */
	public void coefficientChanged( final KnobElement element, final double coefficient );
	
	
	/** connection changed event */
	public void connectionChanged( final KnobElement element, final boolean isConnected );
	
	
	/** ready state changed */
	public void readyStateChanged( final KnobElement element, final boolean isReady );
	
	
	/** value changed event */
	public void valueChanged( final KnobElement element, final double value );
	
	
	/** value setting published */
	public void valueSettingPublished( final KnobElement element );
}
