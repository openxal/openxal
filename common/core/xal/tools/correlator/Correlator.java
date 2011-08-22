/*
 * Correlator.java
 *
 * Created on June 27, 2002, 8:26 AM
 */

package xal.tools.correlator;

import xal.tools.messaging.MessageCenter;

import java.awt.event.*;
import java.util.*;
import java.util.logging.*;


/**
 * The Correlator is the class that is used to setup monitoring of correlated
 * events.  It is the sole entry point to the outside world.  When correlations 
 * are found, the Correlator broadcasts the correlation.
 *
 * Note that all time is in seconds unless otherwise stated.
 *
 * @author  tap
 */
abstract public class Correlator {
    protected MessageCenter localCenter;
    protected double _binTimespan;
    protected CorrelationTester correlationTester;
	
    // one binQueue for each channel
    
    /** map of source agents keyed by name */
    private Map<String,SourceAgent> sourceAgentTable;
    
    private StateNotice stateProxy;
    private volatile boolean isMonitoring;     // actively monitoring channels
    private AbstractBroadcaster broadcaster;
	private CorrelationPoster poster;
	
	
    /** Creates new Correlator */
    public Correlator(double aBinTimespan) {
        this(aBinTimespan, null);
    }
    
    
	/** Correlator constructor */
    public Correlator(double aBinTimespan, CorrelationFilter aFilter) {
        isMonitoring = false;
        sourceAgentTable = new Hashtable<String,SourceAgent>();
        correlationTester = new CorrelationTester(0, aFilter);
        
        registerEvents();
        
		poster = new CorrelationPoster();
		useDefaultBroadcaster();
        
        setBinTimespan(aBinTimespan);
    }
    
    
    /** Register for notices. */
    protected void registerEvents() {
        // internal correlator message center
        localCenter = new MessageCenter("Internal Correlator Messaging");
        
        /** register to broadcast changes of state */
        stateProxy = localCenter.registerSource( this, StateNotice.class );
    }
    
    
    /**
     * Register the listener as a receiver of Correlation notices from this 
     * correlator.
     */
    public void addListener(CorrelationNotice listener) {
        poster.addCorrelationNoticeListener(listener);
    }
    
    
    /**
     * Unregister the listener as a receiver of Correlation notices from this 
     * correlator.
     */
    public void removeListener(CorrelationNotice listener) {
        poster.removeCorrelationNoticeListener(listener);
    }
    
    
    /**
     * Return the broadcaster.
	 * @return The broadcaster used by the correlator.
     */
    AbstractBroadcaster getBroadcaster() {
        return broadcaster;
    }
	
	
	/**
	 * Set the broadcaster to the specified broadcater.
	 * @param newBroadcaster the new broadcaster to use.
	 */
	synchronized private void setBroadcaster(AbstractBroadcaster newBroadcaster) {
		if ( broadcaster != null ) {
			broadcaster.removeCorrelationNoticeListener(poster);
			localCenter.removeTarget(broadcaster, StateNotice.class);
			broadcaster.dispose();
		}
		
		broadcaster = newBroadcaster;
		broadcaster.setFullCount( numSources() );
		localCenter.registerTarget(broadcaster, StateNotice.class);
		broadcaster.addCorrelationNoticeListener(poster);
		
		broadcaster.binTimespanChanged(this, _binTimespan);		// make sure interested broadcasters get this info
	}
	
	
	/**
	 * Set the broadcaster to a default broadcaster.
	 * @return The new broadcaster.
	 */
	DefaultBroadcaster useDefaultBroadcaster() {
		if ( !(broadcaster instanceof DefaultBroadcaster) ) { 
			setBroadcaster( new DefaultBroadcaster(localCenter) );
		}
		return (DefaultBroadcaster)broadcaster;
	}
	
	
	/**
	 * Set the broadcaster to a passive broadcaster.
	 * @return The new broadcaster.
	 */
	PassiveBroadcaster usePassiveBroadcaster() {
		if ( !(broadcaster instanceof PassiveBroadcaster) ) {
			setBroadcaster( new PassiveBroadcaster(localCenter) );
		}
		return (PassiveBroadcaster)broadcaster;
	}
	
	
	/**
	 * Set the broadcaster to a patient broadcaster.
	 * @return The new broadcaster.
	 */
	PatientBroadcaster usePatientBroadcaster() {
		if ( !(broadcaster instanceof PatientBroadcaster) ) {
			setBroadcaster( new PatientBroadcaster(localCenter) );
		}
		return (PatientBroadcaster)broadcaster;
	}
	
	
	/**
	 * Set the broadcaster to a verbose broadcaster.
	 * @return The new broadcaster.
	 */
	VerboseBroadcaster useVerboseBroadcaster() {
		if ( !(broadcaster instanceof VerboseBroadcaster) ) {
			setBroadcaster( new VerboseBroadcaster(localCenter) );
		}
		return (VerboseBroadcaster)broadcaster;
	}
    
    
    /** maximum time span allowed for events to be considered correlated */
    public double binTimespan() {
        return _binTimespan;
    }
    
    
    /** Set the maximum time span allowed for events to be considered correlated */
    public void setBinTimespan(double timespan) {
        _binTimespan = timespan;
        stateProxy.binTimespanChanged(this, timespan);
    }
    
    
	/**
	 * Set the correlation filter to the one specified.
	 * @param newFilter The correlation filter to use.
	 */
    public void setCorrelationFilter(CorrelationFilter newFilter) {
        correlationTester.setFilter(newFilter);
        stateProxy.correlationFilterChanged(this, correlationTester.getFilter());
    }
    
    
    /** Get all of the channel agents managed by this correlator */
    protected Collection<SourceAgent> getSourceAgents() {
        return sourceAgentTable.values();
    }
    
    
    /** Get a channel agent by name managed by this correlator */
    SourceAgent getSourceAgent(String sourceName) {
        return sourceAgentTable.get(sourceName);
    }
    
    
    /** Get all the names of all the sources managed by this correlator */
    synchronized public Collection<String> getNamesOfSources() {
        return sourceAgentTable.keySet();
    }
    
    
    /** Number of channels being managed */
    public int numSources() {
        return sourceAgentTable.size();
    }
    
    
    /** See if we already manage this channel */
    public boolean hasSource(String sourceName) {
        return sourceAgentTable.containsKey( sourceName );
    }
    
    
    /** 
     * Add a source to monitor.  The name provided with each source must 
     * be unique to that source.
     * Subclasses need to wrap this method to enforce the source type.
     */
    protected void addSource( final Object source, final String sourceName ) {
        addSource( source, sourceName, null );
    }
    
    
    /** 
     * Add a source to monitor.  If we already monitor a source as determined
     * by the source name, then do nothing.
     * The record filter is used to determine whether or not to accept a  
     * reading of the specified source when the event is handled.  You can 
     * create your own custom filter or use a pre-built one.
     * Subclasses need to wrap this method to enforce the source type.
     */
    synchronized protected void addSource(Object source, String sourceName, RecordFilter recordFilter) {
        if ( hasSource(sourceName) )  return;
        
        SourceAgent sourceAgent = newSourceAgent(source, sourceName, recordFilter);

        sourceAgentTable.put( sourceName, sourceAgent );
        sourceAgent.setBinTimespan(_binTimespan);
        int numSources = numSources();
        if ( isMonitoring ) {
            sourceAgent.startMonitor();
        }
        stateProxy.sourceAdded(this, sourceName, numSources);
        correlationTester.setFullCount(numSources);
    }
    
    
    abstract protected SourceAgent newSourceAgent(Object source, String sourceName, RecordFilter recordFilter);
    

    /** Stop managing the specified source. */
    synchronized public void removeSource(String sourceName) {
        SourceAgent sourceAgent = getSourceAgent(sourceName);
        sourceAgentTable.remove( sourceName );
        int numSources = numSources();
        stateProxy.sourceRemoved(this, sourceName, numSources);
        correlationTester.setFullCount(numSources);
        sourceAgent.shutdown();
    }
    
    
    /** Stop managing all registered sources */
    synchronized public void removeAllSources() {
        final Collection<String> sourceNames = new ArrayList<String>( getNamesOfSources() );
        for ( final String sourceName : sourceNames ) {
            removeSource( sourceName );
        }
    }
    
    
    /** 
     * Monitor until the timeout or until a complete correlation is found 
     * @param timeout time to wait in seconds
     */
    synchronized public void pulseMonitorWithTimeout(double timeout) {
        // make sure we stop existing monitor if any (e.g. destroy other timed task)
        stopMonitoring();
		
		final TimedBroadcaster timedBroadcaster = (broadcaster instanceof TimedBroadcaster) ? (TimedBroadcaster)broadcaster : new TimedBroadcaster(localCenter, timeout);
		final CorrelationNotice correlationListener = new CorrelationNotice() {
			public void newCorrelation(Object sender, Correlation correlation) {
				stopMonitoring();
				timedBroadcaster.removeCorrelationNoticeListener(this);
			}
			
			public void noCorrelationCaught(Object sender) {
				stopMonitoring();
				timedBroadcaster.removeCorrelationNoticeListener(this);
			}
		};
				
		timedBroadcaster.addCorrelationNoticeListener(correlationListener);
		timedBroadcaster.setRepeats(false);		// in case timedBroadcaster was reused
		timedBroadcaster.setPeriod(timeout);	// in case timedBroadcaster was reused
		
		if ( broadcaster != timedBroadcaster ) {
			setBroadcaster(timedBroadcaster);
		}
		startMonitoring();
    }
    
    
    /** 
     * Monitor and post the best partial correlation if the timeout is 
     * exceeded.  The timeout is rescheduled after every post. 
     * @param timeout time to wait in seconds
     */
    synchronized public void monitorWithTimeout(double timeout) {
        // make sure we stop existing monitor if any (e.g. destroy other timed task)
        stopMonitoring();
		
		final TimedBroadcaster timedBroadcaster = (broadcaster instanceof TimedBroadcaster) ? (TimedBroadcaster)broadcaster : new TimedBroadcaster(localCenter, timeout);
		timedBroadcaster.setPeriod(timeout);	// in case timedBroadcaster was reused
		timedBroadcaster.setRepeats(true);		// in case timedBroadcaster was reused
		
		if ( broadcaster != timedBroadcaster ) {
			setBroadcaster(timedBroadcaster);
		}
		startMonitoring();
    }
    
    
    /** Start monitoring the managed sources. */
    public void startMonitoring() {
        if ( !isMonitoring ) {
            // broadcast the request so the listening source agents start their monitors
            stateProxy.willStartMonitoring(this);
            isMonitoring = true;
        }
    }
    
    
    /** Stop monitoring the managed sources. */
    public void stopMonitoring() {
        if ( isMonitoring ) {
            // broadcast the request so the listening source agents stop their monitors
            stateProxy.willStopMonitoring(this);
            isMonitoring = false;
        }
    }
	
	
	/**
	 * Determine if the correlator is running
	 * @return true if the correlator is running and false otherwise.
	 */
	public boolean isRunning() {
		return isMonitoring;
	}
    
    
    /** Dispose of the correlator and its overhead */
    public void dispose() {
        stopMonitoring();
        removeAllSources();
        localCenter.removeSource(this, StateNotice.class);		
		localCenter.removeTarget(broadcaster, StateNotice.class);
        broadcaster.dispose();
		poster.dispose();
    }
    
    
    /** 
     * <code>fetchCorrelationWithTimeout()</code> is a convenience method that 
     * allows the user a simple way to fetch a correlation without handling 
     * events and implementing a listener.  The method spawns a fetch and 
     * blocks until a correlation is retrieved or the timeout has expired.
     * The resulting correlation is returned.  If no correlation was found  
     * within the timeout, null is returned.
     */
    public Correlation fetchCorrelationWithTimeout(double aTimeout) {
        WaitingListener listener = new WaitingListener();
        return listener.listenWithTimeout(aTimeout);
    }
	
	
	
	/**
	 * Post correlations on behalf of the correlator.
	 */
	private class CorrelationPoster implements CorrelationNotice {
		private MessageCenter postCenter;
		private CorrelationNotice postProxy;
		
		
		/**
		 *
		 */
		public CorrelationPoster() {        
			postCenter = new MessageCenter("Correlator Poster");      // external poster
			postProxy = postCenter.registerSource(this, CorrelationNotice.class);
		}
		
		
		/** Dispose of this poster and all of its overhead */
		void dispose() {
			postCenter.removeSource(this, CorrelationNotice.class);
		}
		
		
		/**
		 * Register the listener as a receiver of Correlation notices from this 
		 * correlator.
		 */
		public void addCorrelationNoticeListener(CorrelationNotice listener) {
			postCenter.registerTarget(listener, this, CorrelationNotice.class);
		}
		
		
		/**
		 * Unregister the listener as a receiver of Correlation notices from this 
		 * correlator.
		 */
		public void removeCorrelationNoticeListener(CorrelationNotice listener) {
			postCenter.removeTarget(listener, this, CorrelationNotice.class);
		}
		
		
		/**
		 * Handle the correlation event.  This method gets called when a correlation
		 * was posted.
		 * @param sender The poster of the correlation event.
		 * @param correlation The correlation that was posted.
		 */
		public void newCorrelation(Object sender, Correlation correlation) {
			postProxy.newCorrelation(Correlator.this, correlation);
		}
		
		
		/**
		 * Handle the no correlation event.  This method gets called when no correlation
		 * was found within some prescribed time period.
		 * @param sender The poster of the "no correlation" event.
		 */
		public void noCorrelationCaught(Object sender) {
			postProxy.noCorrelationCaught(Correlator.this);
		}
	}
    
    
    
    /**
     * <code>WaitingListener</code> is an internal class used to implement the 
     * <code>fetchCorrelationWithTimeout()</code> method.  It sets itself up 
     * as a listener of events and waits until a correlation is found or 
     * the timeout has expired.
     */
    private class WaitingListener implements CorrelationNotice {
        private transient Correlation correlation;
        
        public WaitingListener() {
            correlation = null;
        }
        
        synchronized public Correlation listenWithTimeout(double timeout) {
            addListener( this );
            pulseMonitorWithTimeout( timeout );
            try {
                wait( (long)( 1000 * timeout ) );	// block until we get an event or the timeout has expired
            }
            catch( InterruptedException exception ) {
				Logger.getLogger("global").log( Level.SEVERE, "Error while waiting for a correlation.", exception );
                System.err.println( exception );
            }
            finally {
                removeListener( this );
            }
            
            return correlation;
        }
        
        synchronized public void newCorrelation(Object sender, Correlation newCorrelation) {
            correlation = newCorrelation;
            this.notify();
        }
        
        synchronized public void noCorrelationCaught(Object sender) {
            this.notify();
        }        
    }
}
