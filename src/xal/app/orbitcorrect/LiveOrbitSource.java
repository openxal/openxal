/*
 *  LiveOrbitSource.java
 *
 *  Created on Tue Jun 15 10:07:11 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;

import xal.ca.*;
import xal.tools.correlator.*;
import xal.tools.data.DataAdaptor;
//import xal.tools.Lock;
import xal.smf.*;
import xal.smf.impl.BPM;

import java.util.*;
import java.text.SimpleDateFormat;


/**
 * LiveOrbitSource is an orbit source that generates Orbit instances from live channel access data.
 * @author   tap
 * @since    Jun 15, 2004
 */
public class LiveOrbitSource extends OrbitSource implements RepRateListener {
	/** active orbit */
	protected MutableOrbit _activeOrbit;
	
	/** last orbit */
	protected Orbit _latestOrbit;

	/** accelertor rep-rate monitor */
	protected RepRateMonitor _repRateMonitor;
	
	/** correlator for monitoring BPM signals */
	protected BPMCorrelator _bpmCorrelator;
	
	/** poster of correlation events */
	protected PeriodicPoster<BpmRecord> _bpmPeriodicPoster;
	
	/** beam event channel */
	protected Channel _beamEventChannel;
	
	/** handler of BPM correlation events */
	protected BPMCorrelationHandler _bpmCorrelationHandler;


	/**
	 * Primary Constructor
	 * @param  label     Label for this orbit source.
	 * @param  sequence  The sequence for which this orbit source supplies orbits.
	 * @param  bpmAgents      The BPM agents to include in the Orbit.
	 * @param useBeamEventTrigger determines whether to trigger on beam events
	 */
	public LiveOrbitSource( final String label, final AcceleratorSeq sequence, final List<BpmAgent> bpmAgents, final boolean useBeamEventTrigger ) {
		super( label, sequence, bpmAgents );
		setUseBeamEventTrigger( useBeamEventTrigger );
	}


	/**
	 * Constructor
	 * @param label  Label for this orbit source.
	 * @param useBeamEventTrigger determines whether to trigger on beam events
	 */
	public LiveOrbitSource( final String label, final boolean useBeamEventTrigger ) {
		this( label, null, null, useBeamEventTrigger );
	}


	/** Initial setup */
    //BPMCorrelator does not take parameterized types from Correlator and BPMCorrelator's warning were suppressed,
    //giving an unchecked warning to _bpmCorrelator which is suppressed
    @SuppressWarnings( "unchecked" )
	protected void setup() {
		_repRateMonitor = new RepRateMonitor();
		_repRateMonitor.addRepRateListener( this );
		
		_bpmCorrelator = new BPMCorrelator( BpmAgent.DEFAULT_CORRELATION_WINDOW );
		_bpmPeriodicPoster = new PeriodicPoster<BpmRecord>( _bpmCorrelator, 1.0 );
		_bpmCorrelationHandler = new BPMCorrelationHandler();
		_bpmPeriodicPoster.addCorrelationNoticeListener( _bpmCorrelationHandler );
	}


	/** dispose of this instance's resources */
	public void dispose() {
		removeBpmMonitors();
		_repRateMonitor.removeRepRateListener( this );
		//removeBeamEventsMonitor();
		_repRateMonitor.dispose();
		super.dispose();
	}


	/**
	 * Set the sequence and its BPM agents to monitor
	 * @param sequence  The new sequence value
	 * @param bpmAgents      the list of BPMs to monitor
	 */
	public void setSequence( final AcceleratorSeq sequence, final List<BpmAgent> bpmAgents ) {
        super.setSequence( sequence, bpmAgents );
        
        // perform this outside of the state synchronization
        _proxy.sequenceChanged( this, sequence, getBpmAgents() );
	}

    
    
    /** Subclasses may implement this method to prepare for sequence assignment */
    protected void beforeSequenceAssignment( final AcceleratorSeq sequence, final List<BpmAgent> bpmAgents ) {
        removeBpmMonitors();
        
        final Accelerator accelerator = ( sequence != null ) ? sequence.getAccelerator() : null;
        if ( accelerator != getAccelerator() ) {
            _repRateMonitor.setAccelerator( accelerator );
        }
    }
    
    
    /** Subclasses may implement this method to perform action immediately after sequence assignment */
    protected void afterSequenceAssignment( final AcceleratorSeq sequence, final List<BpmAgent> bpmAgents ) {
        _activeOrbit = new MutableOrbit( sequence );
        _latestOrbit = _activeOrbit.getOrbit();
        
        if ( bpmAgents != null ) {
            monitorBpms();
            monitorBeamEvents();
        }
    }
    

	/**
	 * Get the latest orbit.
	 * @return   the latest orbit.
	 */
	public Orbit getOrbit() {
		synchronized( _activeOrbit ) {
			return _latestOrbit;
		}
	}


	/** Listen for BPM Agent events from this source's BPM agents */
	protected void monitorBpms() {
		for ( final BpmAgent bpmAgent : _bpmAgents ) {
			_bpmCorrelator.addBPM( bpmAgent );
			_repRateMonitor.addRepRateListener( bpmAgent );
		}
		_bpmCorrelator.startMonitoring();
		_bpmPeriodicPoster.start();
	}


	/** Stop listening for BPM Agent events from this source's BPM agents */
	protected void removeBpmMonitors() {
		if ( _bpmAgents == null ) {
			return;
		}

		_bpmPeriodicPoster.stop();
		_bpmCorrelator.stopMonitoring();
		for ( final BpmAgent bpmAgent : _bpmAgents ) {
			_bpmCorrelator.removeBPM( bpmAgent );
			_repRateMonitor.removeRepRateListener( bpmAgent );
		}
	}
	
	
	/**
	 * Notification that the rep-rate has changed.
	 * @param monitor  The monitor announcing the new rep-rate.
	 * @param repRate  The new rep-rate.
	 */
	public void repRateChanged( RepRateMonitor monitor, double repRate ) {
		if ( _bpmCorrelator != null ) {
			// if repRate is undefined or outside the expected range, revert to default otherwise
			// make the time window half of the rep-rate
			double timeWindow = ( !Double.isNaN( repRate ) && ( repRate > 0 ) && ( repRate < 10000 ) ) ? 0.5 / repRate : BpmAgent.DEFAULT_CORRELATION_WINDOW;
			_bpmCorrelator.setBinTimespan( timeWindow );
		}
	}
	
	
	/** Monitor beam events which indicate that a new beam pulse has been generated.  */
	protected void monitorBeamEvents() {
		if ( _beamEventChannel == null && _sequence != null ) {
			final TimingCenter timingCenter = _sequence.getAccelerator().getTimingCenter();
			if ( timingCenter != null ) {
				_beamEventChannel = timingCenter.findChannel( TimingCenter.SLOW_DIAGNOSTIC_EVENT_HANDLE );
				if ( _beamEventChannel != null ) {
					_bpmCorrelator.addBeamEvent( _beamEventChannel );
				}
			}
		}
	}
	
	
	/** Set whether or not to monitor beam events.  */
	public void setUseBeamEventTrigger( final boolean useBeamEventTrigger ) {
		if ( useBeamEventTrigger ) {
			monitorBeamEvents();
		}
		else {
			_beamEventChannel = null;	// must set this to null since it is tested when configuring the monitor
			_bpmCorrelator.removeBeamEvent();
		}
	}
	
	
	
	/**
	 * Handle correlation events
	 * @author   t6p
	 */
	protected class BPMCorrelationHandler implements CorrelationNotice<BpmRecord> {
		/**
		 * Handle the correlation event.  This method gets called when a correlation was posted.
		 * @param sender The poster of the correlation event.
		 * @param correlation The correlation that was posted.
		 */
		public void newCorrelation( final Object sender, final Correlation<BpmRecord> correlation ) {
			synchronized( _activeOrbit ) {
				final MutableOrbit orbit = new MutableOrbit( _sequence );
				for ( final BpmAgent bpmAgent : _bpmAgents ) {
					final BpmRecord record = correlation.getRecord( bpmAgent.getID() );
					if ( record != null ) {
						orbit.addRecord( record );
					}
				}
				_latestOrbit = orbit.getOrbit();
				_proxy.orbitChanged( LiveOrbitSource.this, orbit );
			}
		}
		
		
		/**
		 * Handle the no correlation event.  This method gets called when no correlation was found within some prescribed time period.
		 * @param sender The poster of the "no correlation" event.
		 */
		public void noCorrelationCaught( final Object sender ) {}
	}
	
	
	
	
    /**
	 * Write the live orbit source as a snapshot orbit.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
		final Orbit latestOrbit = getOrbit();
		if ( latestOrbit != null ) {
			final SimpleDateFormat dateFormat = new SimpleDateFormat( "MM/dd/yyyy HH:mm:ss" );
			final String snapshotLabel = getLabel() + " (" + dateFormat.format( latestOrbit.getTimeStamp() ) + ")";
			final SnapshotOrbitSource snapshot = new SnapshotOrbitSource( snapshotLabel, latestOrbit );
			snapshot.write( adaptor );
		}
	}
}

