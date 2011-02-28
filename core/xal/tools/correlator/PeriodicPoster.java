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
public class PeriodicPoster implements ActionListener {
    protected Correlator correlator;
    protected PassiveBroadcaster broadcaster;
    protected javax.swing.Timer timer;
    
	
    /** 
	 * Creates a new instance of PeriodicPoster
	 * @param aCorrelator The correlator providing the correlations.
	 * @param period The posting period.
	 */
    public PeriodicPoster(Correlator aCorrelator, double period) {
        correlator = aCorrelator;
		
		broadcaster = correlator.usePassiveBroadcaster();
        
        int msecPeriod = (int)(period * 1000);
        timer = new javax.swing.Timer(msecPeriod, this);
        timer.setRepeats(true);
        timer.setCoalesce(true);
    }
    
    
    /** 
	 * Get the associated correlator
	 * @return the correlator providing the correlations.
	 */
    public Correlator getCorrelator() {
        return correlator;
    }
    
    
    /** 
	 * Get the timer period
	 * @return The timer period.
	 */
    public double getPeriod() {
        int msecPeriod = timer.getDelay();
        return ((double)(msecPeriod)) / 1000.;
    }
    
    
    /** 
	 * Set the timer period 
	 * @param period The new timer period.
	 */
    public void setPeriod(double period) {
        int msecPeriod = (int)(period * 1000);
        timer.setDelay(msecPeriod);
    }
	
	
	/**
	 * Determine if the poster is running
	 * @return true if the poster is running and false if not.
	 */
	public boolean isRunning() {
		return timer.isRunning();
	}

    
    /** 
	 * Start the timer 
	 */
    public void start() {
        timer.start();
    }
    
    
    /** Stop posting */
    public void stop() {
        timer.stop();
    }
    
    
    /** Restart posting */
    public void restart() {
        timer.restart();
    }
    
    
    /** Dispose of the poster */
    public void dispose() {
        timer.stop();
    }
    
    
    /** 
	 * Add the listener of re-broadcast correlation notices.
	 * @param listener A listener of the correlation notice.
	 */
    public void addCorrelationNoticeListener(CorrelationNotice listener) {
        correlator.addListener(listener);
    }
    
    
    /** 
	 * Remove the listener of re-broadcast correlations
	 * @param listener A listener of the correlation notice.
	 */
    public void removeCorrelationNoticeListener(CorrelationNotice listener) {
        correlator.removeListener(listener);
    }

    
    /** 
	 * Implement ActionListener interface to rebroadcast the best correlation.
	 * @param event The timer event indicating that it is time to post a correlation.
	 */
    synchronized public void actionPerformed(ActionEvent event) {
        broadcaster.postBestPartialCorrelation();
    }    
}


