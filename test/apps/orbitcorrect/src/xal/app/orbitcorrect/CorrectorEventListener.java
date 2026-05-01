/*
 * CorrectorEventListener.java
 *
 * Created on Thu Jan 08 09:08:11 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.orbitcorrect;

import xal.ca.*;


/**
 * CorrectorEventListener
 *
 * @author  tap
 */
public interface CorrectorEventListener {
	/**
	 * Event indicating that the corrector's enable state changed.
	 * @param agent the corrector agent posting this event
	 * @param enabled new corrector enable state; true if the corrector is enabled and false if not
	 */
	public void enableChanged( CorrectorAgent agent, boolean enabled );
	
	
	/**
	 * The PV's monitored value has changed.
	 * @param agent The corrector agent with the channel whose value has changed
	 * @param record The channel time record of the new value
	 * @param field the new field
	 */
	public void fieldChanged( CorrectorAgent agent, ChannelTimeRecord record, double field );
	
	
	/**
	 * The corrector's field limits have changed.
	 * @param agent the corrector agent with the channel whose field has changed
	 * @param lowerFieldLimit the lower field limit
	 * @param upperFieldLimit the upper field limit
	 */
	public void fieldLimitsChanged( CorrectorAgent agent, double lowerFieldLimit, double upperFieldLimit );
	
	
	/**
	 * The channel's connection has changed.  Either it has established a new connection or
	 * the existing connection has dropped.
	 * @param agent The Corrector agent with the channel whose connection has changed
	 * @param connected The channel's new connection state
	 */
	public void connectionChanged( CorrectorAgent agent, boolean connected );
}

