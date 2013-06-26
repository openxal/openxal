//
//  CorrectorSupplyListener.java
//  xal
//
//  Created by Thomas Pelaia on 5/9/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.orbitcorrect;

import xal.ca.*;

/** handle corrector supply events */
public interface CorrectorSupplyListener {
	/**
	 * Event indicating that the supply's enable state changed.
	 * @param agent the corrector supply agent posting this event
	 * @param enabled new corrector enable state; true if the corrector is enabled and false if not
	 */
	public void enableChanged( CorrectorSupply agent, boolean enabled );
	
	
	/**
	 * The PV's monitored value has changed.
	 * @param agent The corrector supply agent with the channel whose value has changed
	 * @param record The channel time record of the new value
	 * @param field the new field
	 */
	public void fieldChanged( CorrectorSupply agent, ChannelTimeRecord record, double field );
	
	
	/**
	 * The supply's field limits have changed.
	 * @param agent the corrector supply agent with the channel whose field has changed
	 * @param lowerFieldLimit the lower field limit
	 * @param upperFieldLimit the upper field limit
	 */
	public void fieldLimitsChanged( CorrectorSupply agent, double lowerFieldLimit, double upperFieldLimit );
	
	
	/**
	 * The channel's connection has changed.  Either it has established a new connection or the existing connection has dropped.
	 * @param agent The corrector supply agent with the channel whose connection has changed
	 * @param connected The channel's new connection state
	 */
	public void connectionChanged( CorrectorSupply agent, boolean connected );	
}
