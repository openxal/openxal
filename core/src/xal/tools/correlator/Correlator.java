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
abstract public class Correlator<SourceType, RecordType, SourceAgentType extends SourceAgent<RecordType>> {
    protected MessageCenter localCenter;
    protected double _binTimespan;
    protected CorrelationTester<RecordType> correlationTester;
	
    // one binQueue for each channel
    private Map<String,SourceAgentType> sourceAgentTable;
    private StateNotice<RecordType> stateProxy;
    private volatile boolean isMonitoring;     // actively monitoring channels
    private AbstractBroadcaster<RecordType> broadcaster;
	private CorrelationPoster poster;
	
	
    /** Creates new Correlator */
    public Correlator(double aBinTimespan) {
        this(aBinTimespan, null);
    }
    
    
	/** Correlator constructor */
    public Correlator( final double aBinTimespan, final CorrelationFilter<RecordType> aFilter ) {
        isMonitoring = false;
        sourceAgentTable = new Hashtable<String,SourceAgentType>();
        correlationTester = new CorrelationTester<RecordType>( 0, aFilter );
        
        registerEvents();
        
		poster = new CorrelationPoster();
		useDefaultBroadcaster();
        
        setBinTimespan(aBinTimespan);
    }
    
    
    /** Register for notices. */
	@SuppressWarnings( "unchecked" )	// must cast StateNotice proxy to support Generics
    protected void registerEvents() {
        // internal correlator message center
        localCenter = new MessageCenter("Internal Correlator Messaging");
        
        /** register to broadcast changes of state */
        stateProxy = (StateNotice<RecordType>)localCenter.registerSource( this, StateNotice.class );
    }
    
    
    /**
     * Register the listener as a receiver of Correlation notices from this correlator.
	 * @param listener to register for receiving events
     */
    public void addListener( final CorrelationNotice<RecordType> listener ) {
        poster.addCorrelationNoticeListener( listener );
    }
    
    
    /**
     * Unregister the listener as a receiver of Correlation notices from this correlator.
	 * @param listener to remove from receiving events
     */
    public void removeListener( final CorrelationNotice<RecordType> listener ) {
        poster.removeCorrelationNoticeListener( listener );
    }
    
    
    /**
     * Return the broadcaster.
	 * @return The broadcaster used by the correlator.
     */
    AbstractBroadcaster<RecordType> getBroadcaster() {
        return broadcaster;
    }
	
	
	/**
	 * Set the broadcaster to the specified broadcater.
	 * @param newBroadcaster the new broadcaster to use.
	 */
	synchronized private void setBroadcaster( final AbstractBroadcaster<RecordType> newBroadcaster ) {
		if ( broadcaster != null ) {
			broadcaster.removeCorrelationNoticeListener(poster);
			localCenter.removeTarget(broadcaster, StateNotice.class);
			broadcaster.dispose();
		}
		
		broadcaster = newBroadcaster;
		broadcaster.setFullCount( numSources() );
		localCenter.registerTarget( broadcaster, StateNotice.class );
		broadcaster.addCorrelationNoticeListener( poster );
		
		broadcaster.binTimespanChanged( this, _binTimespan );		// make sure interested broadcasters get this info
	}
	
	
	/**
	 * Set the broadcaster to a default broadcaster.
	 * @return The new broadcaster.
	 */
	DefaultBroadcaster<RecordType> useDefaultBroadcaster() {
		if ( !(broadcaster instanceof DefaultBroadcaster) ) { 
			setBroadcaster( new DefaultBroadcaster<RecordType>( localCenter ) );
		}
		return (DefaultBroadcaster<RecordType>)broadcaster;
	}
	
	
	/**
	 * Set the broadcaster to a passive broadcaster.
	 * @return The new broadcaster.
	 */
	@SuppressWarnings( "unchecked" )	// enforces type internally
	PassiveBroadcaster<RecordType> usePassiveBroadcaster() {
		if ( !(broadcaster instanceof PassiveBroadcaster) ) {
			setBroadcaster( new PassiveBroadcaster<RecordType>( localCenter ) );
		}
		return (PassiveBroadcaster)broadcaster;
	}
	
	
	/**
	 * Set the broadcaster to a patient broadcaster.
	 * @return The new broadcaster.
	 */
	PatientBroadcaster<RecordType> usePatientBroadcaster() {
		if ( !(broadcaster instanceof PatientBroadcaster) ) {
			setBroadcaster( new PatientBroadcaster<RecordType>( localCenter ) );
		}
		return (PatientBroadcaster<RecordType>)broadcaster;
	}
	
	
	/**
	 * Set the broadcaster to a verbose broadcaster.
	 * @return The new broadcaster.
	 */
	VerboseBroadcaster<RecordType> useVerboseBroadcaster() {
		if ( !(broadcaster instanceof VerboseBroadcaster) ) {
			setBroadcaster( new VerboseBroadcaster<RecordType>( localCenter ) );
		}
		return (VerboseBroadcaster<RecordType>)broadcaster;
	}
    
    
    /** 
	 * Maximum time span allowed for events to be considered correlated
	 * @return the bin timespan
	 */
    public double binTimespan() {
        return _binTimespan;
    }
    
    
    /** 
	 * Set the maximum time span allowed for events to be considered correlated 
	 * @param timespan of the bins
	 */
    public void setBinTimespan(double timespan) {
        _binTimespan = timespan;
        stateProxy.binTimespanChanged(this, timespan);
    }
    
    
	/**
	 * Set the correlation filter to the one specified.
	 * @param newFilter The correlation filter to use.
	 */
    public void setCorrelationFilter( final CorrelationFilter<RecordType> newFilter ) {
        correlationTester.setFilter( newFilter );
        stateProxy.correlationFilterChanged( this, correlationTester.getFilter() );
    }
    
    
    /** 
	 * Get all of the channel agents managed by this correlator 
	 * @return collection of source agents
	 */
    protected Collection<SourceAgentType> getSourceAgents() {
        return sourceAgentTable.values();
    }
    
    
    /** Get a channel agent by name managed by this correlator */
    SourceAgentType getSourceAgent( final String sourceName ) {
        return sourceAgentTable.get(sourceName);
    }
    
    
    /** 
	 * Get all the names of all the sources managed by this correlator 
	 * @return names of the sources
	 */
    synchronized public Collection<String> getNamesOfSources() {
        return sourceAgentTable.keySet();
    }
    
    
    /**
	 * Number of channels being managed 
	 * @return number of sources
	 */
    public int numSources() {
        return sourceAgentTable.size();
    }
    
    
    /** 
	 * See if we already manage this channel 
	 * @param sourceName name of source to test
	 * @return true if the named source is in this correlator and false if not
	 */
    public boolean hasSource( final String sourceName ) {
        return sourceAgentTable.containsKey( sourceName );
    }
    
    
    /** 
     * Add a source to monitor.  The name provided with each source must be unique to that source.
     * Subclasses need to wrap this method to enforce the source type.
	 * @param source to add
	 * @param sourceName name of source to add
     */
    protected void addSource( final SourceType source, final String sourceName ) {
        addSource( source, sourceName, null );
    }
    
    
    /** 
     * Add a source to monitor.  If we already monitor a source as determined by the source name, then do nothing.
     * The record filter is used to determine whether or not to accept a reading of the specified source when the event is handled.  You can
     * create your own custom filter or use a pre-built one. Subclasses need to wrap this method to enforce the source type.
	 * @param source to add
	 * @param sourceName name of source to add
	 * @param recordFilter filter for the source
     */
    synchronized protected void addSource( final SourceType source, final String sourceName, final RecordFilter<RecordType> recordFilter ) {
        if ( hasSource(sourceName) )  return;
        
        final SourceAgentType sourceAgent = newSourceAgent( source, sourceName, recordFilter );

        sourceAgentTable.put(sourceName, sourceAgent);
        sourceAgent.setBinTimespan(_binTimespan);
        int numSources = numSources();
        if ( isMonitoring ) {
            sourceAgent.startMonitor();
        }
        stateProxy.sourceAdded(this, sourceName, numSources);
        correlationTester.setFullCount(numSources);
    }
    
    
    abstract protected SourceAgentType newSourceAgent( final SourceType source, final String sourceName, final RecordFilter<RecordType> recordFilter );
    

    /** 
	 * Stop managing the specified source. 
	 * @param sourceName name of source to remove
	 */
    synchronized public void removeSource( final String sourceName ) {
        final SourceAgentType sourceAgent = getSourceAgent(sourceName);
        sourceAgentTable.remove(sourceName);
        final int numSources = numSources();
        stateProxy.sourceRemoved( this, sourceName, numSources );
        correlationTester.setFullCount( numSources );
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
	@SuppressWarnings( "unchecked" )		// must cast broadcaster type 
    synchronized public void pulseMonitorWithTimeout(double timeout) {
        // make sure we stop existing monitor if any (e.g. destroy other timed task)
        stopMonitoring();
		
		final TimedBroadcaster<RecordType> timedBroadcaster = (broadcaster instanceof TimedBroadcaster) ? (TimedBroadcaster<RecordType>)broadcaster : new TimedBroadcaster<RecordType>( localCenter, timeout );
		final CorrelationNotice<RecordType> correlationListener = new CorrelationNotice<RecordType>() {
			public void newCorrelation( final Object sender, final Correlation<RecordType> correlation ) {
				stopMonitoring();
				timedBroadcaster.removeCorrelationNoticeListener( this );
			}
			
			public void noCorrelationCaught( final Object sender ) {
				stopMonitoring();
				timedBroadcaster.removeCorrelationNoticeListener( this );
			}
		};
				
		timedBroadcaster.addCorrelationNoticeListener(correlationListener);
		timedBroadcaster.setRepeats(false);		// in case timedBroadcaster was reused
		timedBroadcaster.setPeriod(timeout);	// in case timedBroadcaster was reused
		
		if ( broadcaster != timedBroadcaster ) {
			setBroadcaster( timedBroadcaster );
		}
		startMonitoring();
    }
    
    
    /** 
     * Monitor and post the best partial correlation if the timeout is 
     * exceeded.  The timeout is rescheduled after every post. 
     * @param timeout time to wait in seconds
     */
	@SuppressWarnings( "unchecked" )		// must cast broadcaster type 
    synchronized public void monitorWithTimeout(double timeout) {
        // make sure we stop existing monitor if any (e.g. destroy other timed task)
        stopMonitoring();
		
		final TimedBroadcaster<RecordType> timedBroadcaster = (broadcaster instanceof TimedBroadcaster) ? (TimedBroadcaster<RecordType>)broadcaster : new TimedBroadcaster<RecordType>( localCenter, timeout );
		timedBroadcaster.setPeriod(timeout);	// in case timedBroadcaster was reused
		timedBroadcaster.setRepeats(true);		// in case timedBroadcaster was reused
		
		if ( broadcaster != timedBroadcaster ) {
			setBroadcaster( timedBroadcaster );
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
     * <code>fetchCorrelationWithTimeout()</code> is a convenience method that allows the user a simple way to fetch a correlation without handling
     * events and implementing a listener.  The method spawns a fetch and blocks until a correlation is retrieved or the timeout has expired.
     * The resulting correlation is returned.  If no correlation was found  within the timeout, null is returned.
	 * @param aTimeout timeout for fetching a correlation
	 * @return fetched correlation or null if none within the timeout
     */
    public Correlation<RecordType> fetchCorrelationWithTimeout( final double aTimeout ) {
        return new WaitingListener().listenWithTimeout( aTimeout );
    }
	
	
	
	/**
	 * Post correlations on behalf of the correlator.
	 */
	private class CorrelationPoster implements CorrelationNotice<RecordType> {
		private MessageCenter postCenter;
		private CorrelationNotice<RecordType> postProxy;
		
		
		/**
		 * Constructor
		 */
		@SuppressWarnings( "unchecked" )	// must cast postProxy for Generics
		public CorrelationPoster() {        
			postCenter = new MessageCenter("Correlator Poster");      // external poster
			postProxy = (CorrelationNotice<RecordType>)postCenter.registerSource(this, CorrelationNotice.class);
		}
		
		
		/** Dispose of this poster and all of its overhead */
		void dispose() {
			postCenter.removeSource( this, CorrelationNotice.class );
		}
		
		
		/**
		 * Register the listener as a receiver of Correlation notices from this 
		 * correlator.
		 */
		public void addCorrelationNoticeListener( final CorrelationNotice<RecordType> listener ) {
			postCenter.registerTarget( listener, this, CorrelationNotice.class );
		}
		
		
		/**
		 * Unregister the listener as a receiver of Correlation notices from this 
		 * correlator.
		 */
		public void removeCorrelationNoticeListener( final CorrelationNotice<RecordType> listener ) {
			postCenter.removeTarget( listener, this, CorrelationNotice.class );
		}
		
		
		/**
		 * Handle the correlation event.  This method gets called when a correlation
		 * was posted.
		 * @param sender The poster of the correlation event.
		 * @param correlation The correlation that was posted.
		 */
		public void newCorrelation( final Object sender, final Correlation<RecordType> correlation ) {
			postProxy.newCorrelation( Correlator.this, correlation );
		}
		
		
		/**
		 * Handle the no correlation event.  This method gets called when no correlation
		 * was found within some prescribed time period.
		 * @param sender The poster of the "no correlation" event.
		 */
		public void noCorrelationCaught( final Object sender ) {
			postProxy.noCorrelationCaught(Correlator.this);
		}
	}
    
    
    
    /**
     * <code>WaitingListener</code> is an internal class used to implement the 
     * <code>fetchCorrelationWithTimeout()</code> method.  It sets itself up 
     * as a listener of events and waits until a correlation is found or 
     * the timeout has expired.
     */
    private class WaitingListener implements CorrelationNotice<RecordType> {
		/** latest captured correlation */
        private transient Correlation<RecordType> correlation;
        
		
		/** Constructor */
        public WaitingListener() {
            correlation = null;
        }
        
		
		/** 
		 * Wait and listen for a correlation 
		 * @param timeout the maximum time (seconds) to wait for a correlation
		 */
		@SuppressWarnings( "unchecked" )	// need to cast broadcaster as TimedBroadcaster
		public Correlation<RecordType> listenWithTimeout( final double timeout ) {
            addListener( this );
            pulseMonitorWithTimeout( timeout );
			
            try {
				synchronized( WaitingListener.this ) {
					WaitingListener.this.wait( (long)( 1000 * timeout ) );	// block until we get an event or the timeout has expired
				}
            }
            catch( InterruptedException exception ) {
				Logger.getLogger("global").log( Level.SEVERE, "Error while waiting for a correlation.", exception );
                System.err.println( exception );
            }
            finally {
                removeListener( this );
            }

			// there is a race condition with the timed broadcaster so we must make sure we get its best correlation if any before assuming there is none
			if ( correlation == null && Correlator.this.broadcaster instanceof TimedBroadcaster ) {
				final TimedBroadcaster<RecordType> pulsedBroadcaster = (TimedBroadcaster)Correlator.this.broadcaster;
				correlation = pulsedBroadcaster.getBestPartialCorrelation();
			}

			return correlation;
        }
        
		
		/** Handle the latest captured correlation */
		public void newCorrelation( final Object sender, final Correlation<RecordType> newCorrelation ) {
            correlation = newCorrelation;
			synchronized( WaitingListener.this ) {
				WaitingListener.this.notifyAll();
			}
        }
		
        
		/** No correlation was caught within the timeout */
		public void noCorrelationCaught( final Object sender ) {
			synchronized( WaitingListener.this ) {
				WaitingListener.this.notifyAll();
			}
        }        
    }
}
