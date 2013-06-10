//
//  MachineSummarizer.java
//  xal
//
//  Created by Pelaia II, Tom on 9/27/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.labbook;

import xal.smf.*;
import xal.ca.*;

import java.util.*;
import java.text.SimpleDateFormat;


/** get machine summary information */
public class MachineSummarizer {
	/** list of channel wrappers to summarize */
	final protected List<ChannelWrapper> SUMMARY_CHANNEL_WRAPPERS;
	
	
	/** Constructor */
	public MachineSummarizer() {
		SUMMARY_CHANNEL_WRAPPERS = new ArrayList<ChannelWrapper>();
		
		addStringTimingChannel( "Machine Mode: ", TimingCenter.MACHINE_MODE_HANDLE );
		addStringTimingChannel( "Flavor: ", TimingCenter.ACTIVE_FLAVOR_HANDLE );
		addStringTimingChannel( "Rep Rate (Hz): ", TimingCenter.REP_RATE_HANDLE );
		addStringTimingChannel( "Beam Gate Width (Turns): ", TimingCenter.BEAM_REFERENCE_GATE_WIDTH );
		addStringTimingChannel( "Chopper Delay (Turns): ", TimingCenter.CHOPPER_DELAY );
		addStringTimingChannel( "Chopper Beam On (Turns): ", TimingCenter.CHOPPER_BEAM_ON );
		addStringTimingChannel( "Ring Frequency (MHz):  ", TimingCenter.RING_FREQUENCY_HANDLE );
		
		Channel.flushIO();
	}
	
	
	/** add the string channel wrapper */
	protected void addStringTimingChannel( final String title, final String handle ) {
		try {
			final Channel channel = TimingCenter.getDefaultTimingCenter().getChannel( handle );
			final ChannelWrapper wrapper = ChannelWrapper.getStringChannelWrapper( title, channel );
			if ( wrapper != null ) {
				SUMMARY_CHANNEL_WRAPPERS.add( ChannelWrapper.getStringChannelWrapper( title, channel ) );
			}
		}
		catch( NoSuchChannelException exception ) {
			System.err.println( "No timing channel found for handle:  " + handle );
		}
	}
	
	
	/** get the machine summary */
	public String getMachineSummary() {
		final StringBuilder buffer = new StringBuilder( "\n****** Machine Summary " );
		buffer.append( "at " + new SimpleDateFormat( "MMM dd, yyyy HH:mm:ss" ).format( new Date() ) );
		buffer.append( " ******" );
		
		for ( final ChannelWrapper wrapper : SUMMARY_CHANNEL_WRAPPERS ) {
			buffer.append( "\n" );
			if ( wrapper.isConnected() ) {
				final String valueString = wrapper.getValueAsString();
				if ( valueString != null ) {
					buffer.append( wrapper.getTitle() + valueString );
				}
				else {
					buffer.append( wrapper.getTitle() + "Not Available" );
				}
			}
			else {
				buffer.append( wrapper.getTitle() + "Not Available" );
			}
		}
		
		buffer.append( "\n***********************\n" );
		
		return buffer.toString();
	}
}
