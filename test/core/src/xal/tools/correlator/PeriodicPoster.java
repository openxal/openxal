/*
 * PeriodicPoster.java
 *
 * Created on February 13, 2003, 9:54 AM
 */

package xal.tools.correlator;

import xal.tools.messaging.MessageCenter;

import javax.swing.Timer;
import java.awt.event.*;


/**
 * PerodicPoster is an auxiliary class for posting correlations periodically.  Unlike the TimedBroadcaster, 
 * it strictly only posts the most recent best correlation within a given time interval.  Other correlations within
 * that same time period will be dropped.  The poster will repeat forever until it is stopped.
 *
 * @author  tap
 */
public class PeriodicPoster<RecordType> implements ActionListener {
    final private Correlator<?, RecordType, ? extends SourceAgent<RecordType>> CORRELATOR;
    final private PassiveBroadcaster<RecordType> BROADCASTER;
    final private javax.swing.Timer TIMER;
    
	
    /** 
	 * Creates a new instance of PeriodicPoster
	 * @param aCorrelator The correlator providing the correlations.
	 * @param period The posting period.
	 */
    public PeriodicPoster( final Correlator<?, RecordType, ? extends SourceAgent<RecordType>> aCorrelator, final double period ) {
        CORRELATOR = aCorrelator;
		
		BROADCASTER = CORRELATOR.usePassiveBroadcaster();
        
        int msecPeriod = (int)(period * 1000);
        TIMER = new javax.swing.Timer(msecPeriod, this);
        TIMER.setRepeats(true);
        TIMER.setCoalesce(true);
    }
    
    
    /** 
	 * Get the associated correlator
	 * @return the correlator providing the correlations.
	 */
    public Correlator<?, RecordType, ? extends SourceAgent<RecordType>> getCorrelator() {
        return CORRELATOR;
    }
    
    
    /** 
	 * Get the timer period
	 * @return The timer period.
	 */
    public double getPeriod() {
        int msecPeriod = TIMER.getDelay();
        return ((double)(msecPeriod)) / 1000.;
    }
    
    
    /** 
	 * Set the timer period 
	 * @param period The new timer period.
	 */
    public void setPeriod(double period) {
        int msecPeriod = (int)(period * 1000);
        TIMER.setDelay(msecPeriod);
    }
	
	
	/**
	 * Determine if the poster is running
	 * @return true if the poster is running and false if not.
	 */
	public boolean isRunning() {
		return TIMER.isRunning();
	}

    
    /** 
	 * Start the timer 
	 */
    public void start() {
        TIMER.start();
    }
    
    
    /** Stop posting */
    public void stop() {
        TIMER.stop();
    }
    
    
    /** Restart posting */
    public void restart() {
        TIMER.restart();
    }
    
    
    /** Dispose of the poster */
    public void dispose() {
        TIMER.stop();
    }
    
    
    /** 
	 * Add the listener of re-broadcast correlation notices.
	 * @param listener A listener of the correlation notice.
	 */
    public void addCorrelationNoticeListener( final CorrelationNotice<RecordType> listener ) {
        CORRELATOR.addListener( listener );
    }
    
    
    /** 
	 * Remove the listener of re-broadcast correlations
	 * @param listener A listener of the correlation notice.
	 */
    public void removeCorrelationNoticeListener( final CorrelationNotice<RecordType> listener ) {
        CORRELATOR.removeListener( listener );
    }

    
    /** 
	 * Implement ActionListener interface to rebroadcast the best correlation.
	 * @param event The timer event indicating that it is time to post a correlation.
	 */
    synchronized public void actionPerformed( final ActionEvent event ) {
        BROADCASTER.postBestPartialCorrelation();
    }    
}


