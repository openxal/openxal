/*
 * RawHistoryKeeper.java
 *
 * Created on Thu Aug 21 14:38:36 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.scope;

import xal.ca.Channel;
import xal.ca.ChannelTimeRecord;
import xal.tools.correlator.*;

import java.util.*;


/**
 * RawHistoryKeeper listens for new waveform correlation events and maintains the most recent event.  It also listens for
 * the PV change and time change events and keeps them as pending values.  When a waveform correlation event is captured, 
 * the pending values become the values to associate with this most recent (last) correlation.  In this way, we are more 
 * confident that the last time information, last PV and last correlation are consistent.  The association can't be guaranteed
 * since the time information is not correlated with the waveform since the time information may be posted only when it 
 * changes rather than along with the waveform.
 *
 * @author  tap
 */
public class RawHistoryKeeper implements CorrelationNotice<ChannelTimeRecord>, ChannelModelListener {
    // channel models
    protected ChannelModel[] channelModels;
	
	// history
	protected Correlation<ChannelTimeRecord> lastCorrelation;		// last correlation captured
	protected Map<String,WaveformTime> lastTimeMap;					// map of delay and sample period vs. channel id for the last correlation
	protected Map<String,String> lastPvMap;					// map of PV names vs. channel id for the last correlation
	
	// state
	protected boolean pendingTime;		// true if new times have been posted that are different from last times 
	protected Map<String,WaveformTime> pendingTimeMap;		// map of delay and sample period vs. channel id pending for the next correlation
	protected boolean pendingPv;		// true if new Pvs are pending for the next correlation
	protected Map<String,String> pendingPvMap;			// map of PV names vs. channel id pending for then next correlation
	protected Object lock;
	
	
	/**
	 * Construct a RawHistoryKeeper for the specified channel models.
	 * @param newChannelModels The channel models whose most recent history we wish to keep.
	 */
	public RawHistoryKeeper(ChannelModel[] newChannelModels) {
		lock = new Object();
		
		pendingTime = true;
		pendingTimeMap = new HashMap<>();
		lastTimeMap = new HashMap<>();
		
		pendingPv = true;
		pendingPvMap = new HashMap<>();
		lastPvMap = new HashMap<>();
		
		channelModels = newChannelModels;
		for ( int index = 0 ; index < channelModels.length ; index++ ) {
			ChannelModel channelModel = channelModels[index];
			channelModel.addChannelModelListener(this);
			updatePendingTimeHistory(channelModel);
			updatePendingPvHistory(channelModel);
		}
		updateTimeHistory();
	}
	
	
	/**
	 * Dispose of the this object's resources.  In particular we stop listening to channel model events.
	 */
	void dispose() {
		for ( int index = 0 ; index < channelModels.length ; index++ ) {
			channelModels[index].removeChannelModelListener(this);
		}
	}
	
	
	/**
	 * Update the time information to associate with the next waveform correlation.
	 * @param source The channel model whose time information has changed.
	 */
	protected void updatePendingTimeHistory(ChannelModel source) {
		synchronized(lock) {
			pendingTime = true;
			double delay = source.getWaveformDelay();
			double samplePeriod = source.getSamplePeriod();
			pendingTimeMap.put( source.getID(), new WaveformTime( delay, samplePeriod ) );
		}
	}
	
	
	/**
	 * Update the PV information to associate with the next waveform correlation.
	 * @param source The channel model whose PV information has changed.
	 */
	protected void updatePendingPvHistory(ChannelModel source) {
		synchronized(lock) {
			pendingPv = true;
			pendingPvMap.put( source.getID(), source.getChannelName() );
		}
	}
	
	
	/**
	 * Updates the time history with the most recent pending time history.  This is called 
	 * when the pending time information should be associated with the most recent waveform correlation.
	 */
	protected void updateTimeHistory() {
		synchronized(lock) {
			if ( pendingTime ) {
				lastTimeMap.putAll(pendingTimeMap);
				pendingTime = false;
			}
		}
	}
	
	
	/**
	 * Updates the PV history with the most recent pending PV history.  This is called 
	 * when the pending PV information should be associated with the most recent waveform correlation.
	 */
	protected void updatePvHistory() {
		synchronized(lock) {
			if ( pendingPv ) {
				lastPvMap.putAll(pendingPvMap);
				pendingPv = false;
			}
		}
	}
	
	
	/**
	 * Construct a new waveform snapshot from the latest correlation, latest time information and 
	 * latest PV information, and return this waveform snapshot.
	 * @return The waveform snapshot representing waveforms from the most recent pulse.
	 */
	public WaveformSnapshot getWaveformSnapshot() {
		if ( lastCorrelation == null ) {
			throw new RuntimeException("Attempt to output data when there are no waveforms...");
		}
		
		return new WaveformSnapshot( lastCorrelation, lastPvMap, lastTimeMap );
	}
	
	
	/**
	 * Handle the correlation event.  This method gets called when a correlation was posted.  The pending
	 * time and pending PV information is now associated with this new waveform correlation.
	 * @param sender The poster of the correlation event.
	 * @param correlation The correlation that was posted.
	 */
    public void newCorrelation( final Object sender, final Correlation<ChannelTimeRecord> correlation ) {
		synchronized(lock) {
			lastCorrelation = correlation;
			updateTimeHistory();
			updatePvHistory();
		}
	}
	
	
	/**
	 * Handle the no correlation event.  This method gets called when no correlation
	 * was found within some prescribed time period.
	 * @param sender The poster of the "no correlation" event.
	 */
    public void noCorrelationCaught(Object sender) {
	}
	
	
    /**
     * Event indicating that the specified channel is being enabled.
     * @param source ChannelModel posting the event.
     * @param channel The channel being enabled.
     */
    public void enableChannel(ChannelModel source, Channel channel) {}
    
    
    /**
     * Event indicating that the specified channel is being disabled.
     * @param source ChannelModel posting the event.
     * @param channel The channel being disabled.
     */
    public void disableChannel(ChannelModel source, Channel channel) {}
    
    
    /**
     * Event indicating that the channel model has a new channel.
     * @param source ChannelModel posting the event.
     * @param channel The new channel.
     */
    public void channelChanged(ChannelModel source, Channel channel) {
		updatePendingPvHistory(source);
		updatePendingTimeHistory(source);
	}
    
    
    /**
     * Event indicating that the channel model has a new array of element times.
     * @param source ChannelModel posting the event.
     * @param elementTimes The new element times array measured in turns.
     */
    public void elementTimesChanged(ChannelModel source, final double[] elementTimes) {
		updatePendingTimeHistory(source);
	}
}

