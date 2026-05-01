/*
 * TimedBroadcaster.java
 *
 * Created on Fri Sep 05 13:52:18 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.tools.correlator;

import xal.tools.messaging.MessageCenter;

import javax.swing.Timer;
import java.awt.event.*;


/**
 * TimedBroadcaster broadcasts the most recent best correlation (or noCorrelationCaught) at a specified period.
 * Only the most recent best correlation within a given time interval will be posted unless there are other 
 * full count correlations in which case they will be posted immediately.  Other correlations within
 * that same time period will be dropped.  This broadcaster may be set to repeat or to fire only once.
 *
 * @author  tap
 */
class TimedBroadcaster<RecordType> extends AbstractBroadcaster<RecordType> {
	private Correlation<RecordType> bestPartialCorrelation;   // less than full count
    private boolean isFresh;
    protected javax.swing.Timer timer;
	
	
    /** Creates a new instance of Broadcaster */
    public TimedBroadcaster( final MessageCenter aLocalCenter, final double period ) {
		super( aLocalCenter );
        isFresh = false;
        bestPartialCorrelation = null;
        
        int msecPeriod = (int)(period * 1000);
        timer = new Timer(msecPeriod, new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				postBestPartialCorrelation();
       		}
		});
        timer.setRepeats(true);
        timer.setCoalesce(true);
	}


	/** get the best correlation which may be partial */
	public Correlation<RecordType> getBestPartialCorrelation() {
		return bestPartialCorrelation;
	}

    
    /** 
	 * Get the timer period.
	 * @return The timer period in seconds.
	 */
    public double getPeriod() {
        int msecPeriod = timer.getDelay();
        return ((double)(msecPeriod)) / 1000.;
    }
    
    
    /** 
	 * Set the timer period.
	 * @param period The timer period in seconds.
	 */
    public void setPeriod(double period) {
        int msecPeriod = (int)(period * 1000);
        timer.setDelay(msecPeriod);
    }
	
	
	/**
	 * Set whether the broadcaster's timer repeats or only fires once.
	 * @param repeatFlag true to repeat or false to fire only once.
	 */
	public void setRepeats(boolean repeatFlag) {
		timer.setRepeats(repeatFlag);
	}
	
	
	/**
	 * Determine whether the broadcaster's timer repeats or only fires once.
	 * @return true if the timer repeats or false if it only fires once.
	 */
	public boolean isRepeats() {
		return timer.isRepeats();
	}
	
    
    /**
     * Post the best partial correlation when requested since a full correlation may not be available.
     */
    synchronized void postBestPartialCorrelation() {
        if ( isFresh ) {
            postCorrelation( bestPartialCorrelation );
        }
        else {
            correlationProxy.noCorrelationCaught( this );
        }
    }
	
	
    /**
     * Handle the BinListener event by comparing the number of correlated records and saving it as the best
	 * correlation if it has more than the previous best.  It never posts events, but the best correlation
	 * can be requested at any time.
	 * @param sender The bin agent that published the new correlation.
	 * @param correlation The new correlation.
     */
    synchronized public void newCorrelation( final BinAgent<RecordType> sender, final Correlation<RecordType> correlation ) {
        int numRecords = correlation.numRecords();
        
        if ( numRecords == fullCount ) {    	// broadcast a full correlation immediately
			bestPartialCorrelation = null;
			isFresh = false;
            postCorrelation(correlation);
			if ( timer.isRepeats() )  timer.restart();
        }
        else if ( !isFresh || (numRecords >= bestPartialCorrelation.numRecords() ) ) {
            bestPartialCorrelation = correlation;
            isFresh = true;
        }
    }
	
	
	/**
	 * Handle the advance notice of the correlator stopping.
	 * @param sender The correlator that will stop.
	 */
    public void willStopMonitoring( final Correlator<?,RecordType,?> sender ) {
		timer.stop();
	}
	
	
	/**
	 * Handle the advance notice of the correlator starting.
	 * @param sender The correlator that will start.
	 */
    public void willStartMonitoring( final Correlator<?,RecordType,?> sender ) {
		timer.start();
	}
}

