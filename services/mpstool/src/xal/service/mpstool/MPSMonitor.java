/*
 *  MPSMonitor.java
 *
 *  Created on Fri Mar 12 09:42:37 EST 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.service.mpstool;

import xal.tools.correlator.*;
import xal.ca.correlator.*;
import xal.ca.*;
import xal.extension.logbook.ElogUtility;

import java.util.*;
import java.text.*;
import java.math.*;

/**
 * MPSMonitor
 * @author    jdg
 * @author    tap
 */
public class MPSMonitor {
	/** format for displaying timestamps */
	private final static DateFormat TIMESTAMP_FORMAT;

	/** size of the MPS event buffer */
	public final static int MPS_EVENT_BUFFER_SIZE = xal.service.mpstool.MPSPortal.MPS_EVENT_BUFFER_SIZE;

	//-------------- Member variables --------------//
	
	/** flag indicating whether the first faults statisics should be logged */
	final private boolean LOG_STATISTICS;
	
	/** Chanenl wrappers */
	protected volatile ChannelWrapper[] _mpsChannelWrappers;

	/** Map of input monitors keyed by MPS signal */
	protected Map<String,InputMonitor> _inputMonitors;

	/** Type of MPS signals to monitor (e.g. FPL or FPAR) */
	protected String _mpsType;

	/** Source of MPS signals */
	protected SignalSource _signalSource;

	/** the correlator to use to gather MPS signals in a single macropulse */
	private ChannelCorrelator _correlator;

	/** the ordered list of most recent MPS events sorted by timestamp */
	private volatile LinkedList<MPSEvent> _mpsEventBuffer;

	/** Filter used to set the amount of missing MPS PVs allowed to constitute a legitimate correlation set */
	private CorrelationFilter<ChannelTimeRecord> _filter;

	/** The poster to grab + post correlations every 60 Hz */
	private PeriodicPoster<ChannelTimeRecord> _poster;

	/** time to wait while monitoring a correlated set (sec) */
	private Double _dwellTime;

	/** max timeStamp difference to consitute a correlated set (sec) */
	private Double _deltaT;

	/** Map of first hit trip statistics keyed by PV. This map gets cleared daily. */
	private Map<String,TripStatistics> _firstHitStats;
	
	/** Map of trip statistics keyed by PV. This map gets cleared daily. */
	private Map<String,TripStatistics> _mpsTripStats;


	/** time of last MPS event */
	private volatile Date _lastMPSEventTime;

	/** time of last MPS channel connection event */
	private volatile Date _lastMPSConnectionEventTime;

	/** time of last Input channel connection event */
	private volatile Date _lastInputConnectionEventTime;

	/** handler of connection events of MPS channels for this monitor */
	private MPSConnectionHandler _mpsConnectionHandler;

	/** handler of connection events of input channels for this monitor */
	private InputConnectionHandler _inputConnectionHandler;

	/** The start time for populating daily statistics */
	protected Calendar _startTime;

	/** Synchronization lock for accessing daily statistics */
	protected Object _statsLock;

	/** Timer for updating daily statistics */
	protected final Timer _statsUpdateTimer;

	
	// static initializer
	static {
		TIMESTAMP_FORMAT = new SimpleDateFormat( "MMM dd, yyyy HH:mm:ss" );
	}


	/**
	 * Primary Constructor
	 * @param mpsType       MPS Type (e.g. FPL or FPAR)
	 * @param signalSource  Data source that supplies MPS signals
	 */
	public MPSMonitor( final String mpsType, final SignalSource signalSource, final boolean logStatistics ) {
		LOG_STATISTICS = logStatistics;
		
		// Coles recommends 16ms since it is the shortest time between pulses
		_deltaT = new Double( 0.016 );

		_dwellTime = new Double( 0.1 );

		// Some internal stuff:
		_mpsEventBuffer = new LinkedList<>();
		_firstHitStats = new HashMap<String,TripStatistics>();
		_mpsTripStats = new HashMap<String,TripStatistics>();

		_mpsType = mpsType;
		_signalSource = signalSource;

		_statsLock = new Object();
		_startTime = Calendar.getInstance();
		_lastMPSEventTime = _startTime.getTime();
		_lastMPSConnectionEventTime = _startTime.getTime();
		_lastInputConnectionEventTime = _startTime.getTime();

		_statsUpdateTimer = startStatsUpdateTimer();
		
		_inputConnectionHandler = new InputConnectionHandler();

		loadSignals();
		setupCorrelator();
		restartCorrelator();
		
		System.out.println( "MPS Monitor is running for " + mpsType );
	}


	/** Dispose of this monitor and its resources */
	public void dispose() {
		stopCorrelator();
		_statsUpdateTimer.cancel();
		checkDayUpdateDailyStats();    // flush any remaining stats
	}


	/**
	 * Get the named MPS type ("FPL" or "FPAR") of this monitor
	 *
	 * @return   The type of MPS signal to monitor (e.g. "FPL" or "FPAR")
	 */
	public String getMPSType() {
		return _mpsType;
	}


	/** Load the signals to monitor from the data source */
	public void loadSignals() {
		String[] signals = _signalSource.fetchMPSSignals( _mpsType );
		_mpsChannelWrappers = new ChannelWrapper[signals.length];
		for ( int index = 0; index < signals.length; index++ ) {
			_mpsChannelWrappers[index] = new ChannelWrapper( signals[index] );
		}
		_lastMPSConnectionEventTime = new Date();

		_inputMonitors = _signalSource.fetchInputMonitors( _mpsType );
		if ( _inputMonitors != null ) {
			_inputConnectionHandler.requestConnections( _inputMonitors.values() );
		}
		_lastInputConnectionEventTime = new Date();
	}


	/**
	 * Reload the signals to monitor from the data source. Stop the correlator, recreate a new
	 * correlator with the new signals and restart the correlator.
	 */
	public void reloadSignals() {
		stopCorrelator();
		_mpsConnectionHandler.ignoreAll( _mpsChannelWrappers );
		_correlator.dispose();
		_inputConnectionHandler.ignoreAll( _inputMonitors.values() );
		
		loadSignals();
		setupCorrelator();
		restartCorrelator();
	}


	/** set up the correlator from the PV list */
	private void setupCorrelator() {
		// we still want to catch MPS events, even if the macropulse just has 1 MPS PV
		_filter = CorrelationFilterFactory.minCountFilter( 1 );

		// Set up the Correlator;
		_correlator = new ChannelCorrelator( _deltaT.doubleValue(), _filter );
		// a trip is indicated by a value of 0 and the signal is okay if is 1
		RecordFilter<ChannelTimeRecord> recordFilter = RecordFilterFactory.equalityDoubleFilter( 0.0 );
		_mpsConnectionHandler = new MPSConnectionHandler( recordFilter );

		for ( int index = 0; index < _mpsChannelWrappers.length; index++ ) {
			final ChannelWrapper wrapper = _mpsChannelWrappers[index];
			_mpsConnectionHandler.requestToCorrelate( wrapper );
		}

		// create the poster object:
		_poster = new PeriodicPoster<ChannelTimeRecord>( _correlator, _dwellTime.doubleValue() );
		_poster.addCorrelationNoticeListener(
			new CorrelationNotice<ChannelTimeRecord>() {
				/**
				 * handle no correlation found events
				 *
				 * @param sender  - the provider of the correlation
				 */
				public void noCorrelationCaught( Object sender ) {
				}


				/**
				 * Handle a correlation event by logging the correlated MPS events
				 *
				 * @param sender       - the provider of the correlation
				 * @param correlation  - the correlation object containing the answer !
				 */
				public synchronized void newCorrelation( Object sender, Correlation<ChannelTimeRecord> correlation ) {
					checkDayUpdateDailyStats();
					MPSEvent newEvent = new MPSEvent( correlation );

					updateEventBuffer( newEvent );
					updateStats( newEvent );

					// mark the MPS event time to indicate that new event data is available
					_lastMPSEventTime = newEvent.getTimestamp();
				}
			} );
	}


	/**
	 * Get the correlator
	 * @return   The correlator which correlates MPS events
	 */
	public ChannelCorrelator getCorrelator() {
		return _correlator;
	}


	/**
	 * set the dwell time between correlate attempts
	 * @param aTime  new dwell time (sec)
	 */
	public void setDwellTime( Double aTime ) {
		_poster.setPeriod( aTime.doubleValue() );
		_dwellTime = aTime;
	}


	/**
	 * get the dwell time between correlate attempts
	 * @return   dwell time in seconds
	 */
	public Double getDwellTime() {
		return _dwellTime;
	}


	/**
	 * set the time window for correlated data
	 * @param delta  correlation time window (sec)
	 */
	public void setDeltaT( Double delta ) {
		_deltaT = delta;
		_correlator.setBinTimespan( delta.doubleValue() );
	}


	/**
	 * get the time window to define a correlation
	 * @return   The correlation time window in seconds
	 */
	public Double getDeltaT() {
		return _deltaT;
	}


	/**
	 * Get the array of MPS channel wrappers
	 * @return   the array of monitored MPS channel wrappers
	 */
	public ChannelWrapper[] getMPSChannelWrappers() {
		return _mpsChannelWrappers;
	}


	/**
	 * Get the collection of input monitors
	 * @return   the collection of input monitors
	 */
	public Collection<InputMonitor> getInputMonitors() {
		return _inputMonitors.values();
	}


	/**
	 * Get the input monitor corresponding to the specified MPS signal.
	 * @param mpsSignal  The MPS signal for which to get the corresponding input monitor
	 * @return           the input monitor for the MPS signal or null if none exists
	 */
	public InputMonitor getInputMonitor( final String mpsSignal ) {
		return _inputMonitors.get( mpsSignal );
	}


	/**
	 * Get the input signal corresponding to the specified MPS signal.
	 * @param mpsSignal  The MPS signal for which to get the corresponding input signal
	 * @return           the input signal for the MPS signal or null if none exists
	 */
	public String getInputSignal( final String mpsSignal ) {
		final InputMonitor inputMonitor = getInputMonitor( mpsSignal );
		return inputMonitor != null ? inputMonitor.getSignal() : null;
	}


	/**
	 * Determing if the correlator is running.
	 * @return   true if the correlator is running and false otherwise.
	 */
	public boolean isRunning() {
		return _correlator.isRunning();
	}


	/**
	 * Determing if the poster is running.
	 * @return   true if the poster is running and false otherwise.
	 */
	public boolean isPosting() {
		return _poster.isRunning();
	}


	/** Stops the poster from posting, but the correlator is still running behind the scene */
	public void pausePoster() {
		_poster.stop();
	}


	/** Stop looking for MPS trips */
	public void stopCorrelator() {
		_poster.stop();
		_correlator.stopMonitoring();
	}


	/** Restart the poster after a pause */
	public void restartCorrelator() {
		// make sure correlator is really going
		_correlator.startMonitoring();
		_poster.start();
	}


	/**
	 * Get the timestamp of the latest MPS event. This can be used by clients to determine if
	 * their first hit log is current.
	 * @return   the wall clock timestamp of the latest MPS event
	 */
	public Date getLastMPSEventTime() {
		// check to see if the stats need to be cleared and clear them if necessary
		checkDayUpdateDailyStats();

		return _lastMPSEventTime;
	}


	/**
	 * Get the timestamp of the last MPS channel event. This can be used by clients to determine if
	 * their list of channels is up to date. This timestamp changes when the list of monitored
	 * channels changes or the connection state of one or more monitored channels changes.
	 * @return   the wall clock timestamp of the latest channel event.
	 */
	public Date getLastMPSChannelEventTime() {
		return _lastMPSConnectionEventTime;
	}


	/**
	 * Get the timestamp of the last MPS channel event. This can be used by clients to determine if
	 * their list of channels is up to date. This timestamp changes when the list of monitored
	 * channels changes or the connection state of one or more monitored channels changes.
	 * @return   the wall clock timestamp of the latest channel event.
	 */
	public Date getLastInputChannelEventTime() {
		return _lastInputConnectionEventTime;
	}


	/**
	 * Update the circular buffer of MPS events to include the latest event
	 * @param newEvent  the latest MPS event
	 */
	private void updateEventBuffer( final MPSEvent newEvent ) {
		synchronized ( _mpsEventBuffer ) {
			_mpsEventBuffer.addFirst( newEvent );

			while ( _mpsEventBuffer.size() > MPS_EVENT_BUFFER_SIZE ) {
				_mpsEventBuffer.removeLast();
			}
		}
	}


	/**
	 * Updates the daily statistics of MPS trips.
	 * @param newEvent  The new MPS event to include in the statistics
	 */
	private void updateStats( final MPSEvent newEvent ) {
		synchronized ( _statsLock ) {
			updateFirstHitStats( newEvent );
			updateMPSTripStats( newEvent );
		}
	}


	/**
	 * Update the first hit statistics to include the new MPS event.
	 * @param newEvent  The new MPS event to include in the daily statistics.
	 */
	private void updateFirstHitStats( final MPSEvent newEvent ) {
		synchronized ( _statsLock ) {
			final String firstPV = newEvent.getFirstSignalEvent().getSignal();
			incrementFirstHits( firstPV );
		}
	}


	/**
	 * Increment the number of times the MPS signal has been the first to trip in correlated MPS trips.
	 * The stats are only valid for the present day.
	 * @param mpsPV   The MPS PV for which to increment the first hit trips
	 */
	protected final void incrementFirstHits( final String mpsPV ) {
		synchronized ( _statsLock ) {
			TripStatistics stats = _firstHitStats.get( mpsPV );
			if ( stats == null ) {
				stats = getMPSTripStats( mpsPV );
				_firstHitStats.put( mpsPV, stats );
			}
			stats.incrementFirstHits();
		}
	}


	/**
	 * Incrment the number of times the MPS signal tripped.
	 * The stats are only valid for the present day.
	 * @param mpsPV   The MPS PV for which to increment the trips
	 */
	protected final void incrementMPSTrips( final String mpsPV ) {
		synchronized ( _statsLock ) {
			getMPSTripStats( mpsPV ).incrementMPSTrips();
		}
	}


	/**
	 * Incrment the number of times the MPS signal's input has tripped.
	 * The stats are only valid for the present day.
	 * @param mpsPV   The MPS PV for which to increment the input statistics
	 */
	protected final void incrementInputTrips( final String mpsPV ) {
		synchronized ( _statsLock ) {
			getMPSTripStats( mpsPV ).incrementInputTrips();
		}
	}
	
	
	/**
	 * Get the trip statistics for the specified MPS PV.
	 * @param mpsPV the PV for which to retrieve the trip statistics 
	 * @return the trip statistics for the specified MPS PV
	 */
	protected final TripStatistics getMPSTripStats( final String mpsPV ) {
		synchronized ( _statsLock ) {
			TripStatistics stats = _mpsTripStats.get( mpsPV );
			if ( stats == null ) {
				stats = new TripStatistics( mpsPV, getInputSignal(mpsPV) );
				_mpsTripStats.put( mpsPV, stats );
			}
			
			return stats;
		}
	}


	/**
	 * Update the statistics that maintains the number of MPS trips per MPS signal to include the
	 * specified MPS event.
	 * @param mpsEvent  The new MPS event to include in the statistics.
	 */
	private void updateMPSTripStats( final MPSEvent mpsEvent ) {
		synchronized ( _statsLock ) {
			final List<SignalEvent> signalEvents = mpsEvent.getSignalEvents();
			final List<InputMonitor> inputMonitors = new ArrayList<>( signalEvents.size() );
			//final Iterator<SignalEvent> eventIter = signalEvents.iterator();
            
            for(SignalEvent signalEvent : signalEvents)
            {
			//while ( eventIter.hasNext() ) {
				//final SignalEvent signalEvent = (SignalEvent)eventIter.next();
				final String signal = signalEvent.getSignal();
				InputMonitor inputMonitor = getInputMonitor( signal );
				if ( inputMonitor != null ) {
					inputMonitor.requestValueUpdate();
					inputMonitors.add( inputMonitor );
				}
				incrementMPSTrips( signalEvent );
			}
			
			try {
				// allow a little time for the input requests to be processed
				Thread.sleep( 10 );
				incrementInputTrips( inputMonitors );
			}
			catch ( Exception exception ) {}
		}
	}


	/**
	 * Get the number of times the MPS signal has tripped within the present day.
	 * @param signal  The MPS signal for which to get the trip count.
	 * @return        The number of times the MPS signal has tripped.
	 */
//	public final int getMPSTripCount( final String signal ) {
//		synchronized ( _statsLock ) {
//			return _mpsTripStats.containsKey( signal ) ? ( (Integer)_mpsTripStats.get( signal ) ).intValue() : 0;
//		}
//	}


	/**
	 * Increment the number of MPS trips for the specified signal event.
	 * @param signalEvent  The new signal event to include in the statistics.
	 */
	private void incrementMPSTrips( final SignalEvent signalEvent ) {
		synchronized ( _statsLock ) {
			final String signal = signalEvent.getSignal();
			incrementMPSTrips( signal );
		}
	}


	/**
	 * Get the latest values for the specified input monitors. Check which inputs have tripped and
	 * increment their daily trip statistics accordingly.
	 * @param inputMonitors  the input monitors to check
	 */
	private void incrementInputTrips( final List<InputMonitor> inputMonitors ) {
		Iterator<InputMonitor> monitorIter = inputMonitors.iterator();
		while ( monitorIter.hasNext() ) {
			final InputMonitor monitor = monitorIter.next();
			if ( monitor.isInputTripped() ) {
				final String mpsSignal = monitor.getMPSPV();
				incrementInputTrips( mpsSignal );
			}
		}
	}


	/**
	 * Check to see if the day has changed since the the startTime. If so, reset the startTime to
	 * be the start of the new day and clear the daily statistics.
	 */
	private void checkDayUpdateDailyStats() {
		synchronized ( _statsLock ) {
			int today = Calendar.getInstance().get( Calendar.DATE );
			int startDay = _startTime.get( Calendar.DATE );

			if ( today != startDay ) {
				if ( LOG_STATISTICS ) {
					publishDailyStats();
				}
				resetDailyStats();
			}
		}
	}


	/** Publish the latest daily stats.  */
	private void publishDailyStats() {
		publishDailyStatsToLogbook();
		publishDailyStatsToDatabase();
	}


	/** Publish the latest daily stats.  */
	private void publishDailyStatsToLogbook() {
		try {
			final String firstHitText = getFirstHitText();
			final String mpsTripSummary = getMPSTripSummary();
			final String summary = firstHitText + "\n\n\n" + mpsTripSummary;
			
			// make sure the entry text is well below the 4000 character limit
			final ElogUtility logbookUtility = ElogUtility.defaultUtility();
			final int textLimit = logbookUtility.getMaxBodySize() - 200;	// account of safety margin
			final String entryText = summary.length() < textLimit ? summary : summary.substring( 0, textLimit ) + "\n\nToo many more trips to fit complete summary here...";
			
			final String title = "MPS " + _mpsType + " Daily Statistics";
			
			final String tripReport = getFirstHitReport();
			
			if ( tripReport != null ) {
				final String reportName = "MPS " + _mpsType + " First Hit Report";
				logbookUtility.postEntry( ElogUtility.CONTROLS_LOGBOOK, title, entryText, reportName, "html", tripReport.getBytes() );
			}
			else {
				logbookUtility.postEntry( ElogUtility.CONTROLS_LOGBOOK, title, entryText );
			}
		}
		catch ( Exception exception ) {
			System.err.println( "Exception while publishing daily stats to logbook: " + exception );
		}
	}


	/** Publish the latest daily stats.  */
	private void publishDailyStatsToDatabase() {
		try {
			final Collection<TripStatistics> stats = _mpsTripStats.values();
			_signalSource.publishDailyStatistics( _startTime.getTime(), stats );
		}
		catch ( Exception exception ) {
			System.err.println( "Exception while publishing daily stats to database: " + exception );
		}
	}


	/** Reset the daily statistics by clearing them and setting the startTime to the beginning of the day. */
	private synchronized void resetDailyStats() {
		synchronized ( _statsLock ) {
			_firstHitStats.clear();
			_mpsTripStats.clear();
			
			Calendar newStartTime = Calendar.getInstance();
			// since the day changed, the new start time must be valid since midnight
			_startTime = new GregorianCalendar( newStartTime.get( Calendar.YEAR ), newStartTime.get( Calendar.MONTH ), newStartTime.get( Calendar.DATE ) );
			_lastMPSEventTime = new Date();
		}
	}


	/**
	 * Get the buffer of MPS events
	 * @return   the buffer of MPS events
	 */
	public List<MPSEvent> getMPSEventBuffer() {
		synchronized ( _mpsEventBuffer ) {
			return new ArrayList<MPSEvent>( _mpsEventBuffer );
		}
	}


	/**
	 * Get the list of MPS events which have occured since the specified time. Events which occur
	 * before or at the specified time are excluded from the list.
	 * @param time  The time since which we wish to get events
	 * @return      the list of events since the specified time
	 */
	public List<MPSEvent> getMPSEventsSince( final Date time ) {
		synchronized ( _mpsEventBuffer ) {
			final int count = _mpsEventBuffer.size();
			int index;
			for ( index = 0; index < count; index++ ) {
				final MPSEvent event = _mpsEventBuffer.get( index );
				if ( !event.getTimestamp().after( time ) ) {
					break;
				}
			}
			return _mpsEventBuffer.subList( 0, index );
		}
	}
	
	
	/** 
	 * Get the top first hit statistics 
	 * @param count limit to the number of top records to get
	 * @return the top count trip records ordered from most to least MPS first hits
	 */
	private List<TripStatistics> getTopFirstHitStats( final int count ) {
		synchronized ( _statsLock ) {
			if ( _firstHitStats.isEmpty() ) {
				return Collections.<TripStatistics>emptyList();
			}
			else {
				final List<TripStatistics> records = new ArrayList<TripStatistics>( _firstHitStats.values() );
				Collections.sort( records, TripStatistics.firstHitComparator() );
				Collections.reverse( records );
				return records.size() <= count ? records : records.subList( 0, count );	// get the top "count" trip records
			}
		}		
	}
	
	
	/**
	 * Get a plain text summary of the top first hit statistics
	 * @return summary of the first hit statistics
	 */
	private String getFirstHitText( final Calendar startTime, final List<TripStatistics> topRecords ) {
		if ( topRecords == null || topRecords.size() == 0 ) {
			return "No MPS events since " + TIMESTAMP_FORMAT.format( startTime.getTime() );
		}
		else {
			final StringBuffer statsBuffer = new StringBuffer( "MPS Top 10 First Hits since " + TIMESTAMP_FORMAT.format( startTime.getTime() ) + "\n\n" );
			
			statsBuffer.append( "(First Hits, Total Trips, MPS PV) \n" );
			for ( final TripStatistics record : topRecords  ) {
				statsBuffer.append( record.getFirstHits() + ", " + record.getMPSTrips() + ", " + record.getMPSPV() + "\n" );
			}
			
			return statsBuffer.toString();
		}
	}
	
	
	/**
	 * Get a plain text summary of the top first hit statistics
	 * @return summary of the first hit statistics
	 */
	public String getFirstHitText() {
		synchronized ( _statsLock ) {
			final List<TripStatistics> topRecords = getTopFirstHitStats( 10 );
			return getFirstHitText( _startTime, topRecords );
		}
	}
	
	
	/**
	 * Generate a report of the top first hit statistics.
	 * @return HTML report of the top first hit statistics
	 */
	private String getFirstHitReport() {
		synchronized ( _statsLock ) {
			final List<TripStatistics> topRecords = getTopFirstHitStats( 10 );
			return getTripReport( "Top 10 " + _mpsType +  " MPS First Hits", _startTime, topRecords );
		}
	}
	
	
	/**
	 * Generate a trip report of the specified trip statistics.
	 * @return HTML report of the trip statistics
	 */
	private String getTripReport( final String label, final Calendar startTime, final List<TripStatistics> tripRecords ) {		
		if ( tripRecords == null || tripRecords.size() == 0 ) {
			return null;
		}
		else {
			final String title = label + " since " + TIMESTAMP_FORMAT.format( startTime.getTime() );
			final StringBuffer report = new StringBuffer( "<html>" );
			report.append( "<head>" );
			report.append( "<title>" + title + "</title>" );
			report.append( "<style>" );
			report.append( "table.data { background-color: #DDD; border-collapse: collapse; } " );
			report.append( "th, td { margin: 5px; border-style: solid; border-width: 1.0px; border-color: black; padding: 1.0px; } " );
			report.append( "th { padding-left: 2em; padding-right: 2em; background-color: #BBB; } " );
			report.append( "td.numeric { text-align: right; color: blue; padding-left: 1em; padding-right: 2px; } " );
			report.append( "td.text { text-align: left; color: black; padding-left: 1em; padding-right: 1em; } " );
			report.append( "td.empty { text-align: center; color: gray; padding-left: 1em; padding-right: 1em; } " );
			report.append( "</style>" );
			report.append( "</head>" );
			report.append( "<body>" );
			report.append( "<div style=\"text-align: center;\">" );
			report.append( "<H2 style=\"text-align: center;\">" + title + "</H2><br>" );
			report.append( "<table class=\"data\" style=\"margin-left: auto; margin-right: auto;\">" );
			report.append( "<tr> <th>First Hits</th> <th>Total Trips</th> <th>MPS PV</th> <th>Input trips</th> <th>Input PV</th> </tr>" );
			for ( final TripStatistics record : tripRecords  ) {				
				report.append( "<tr>" );
				report.append( "<td class=\"numeric\">" + record.getFirstHits() + "</td>" );
				report.append( "<td class=\"numeric\">" + record.getMPSTrips() + "</td>" );
				report.append( "<td class=\"text\">" + record.getMPSPV() + "</td>" );
				
				final String inputPV = record.getInputSignal();
				if ( inputPV != null ) {
					report.append( "<td class=\"numeric\">" + record.getInputTrips() + "</td>" );
					report.append( "<td class=\"text\">" + inputPV + "</td>" );
				}
				else {
					report.append( "<td class=\"empty\"> - </td>" );
					report.append( "<td class=\"empty\"> - </td>" );
				}
				
				report.append( "</tr>" );
			}
			report.append( "</table>" );
			report.append( "</div>" );
			report.append( "</body>" );
			report.append( "</html>" );
			
			return report.toString();
		}
	}
	
	
	/** 
	 * Get the top MPS trip statistics 
	 * @param count limit to the number of top records to get
	 * @return the top count trip records ordered from most to least MPS trips
	 */
	private List<TripStatistics> getTopMPSTripStats( final int count ) {
		synchronized ( _statsLock ) {
			if ( _mpsTripStats.isEmpty() ) {
				return Collections.<TripStatistics>emptyList();
			}
			else {
				final List<TripStatistics> records = new ArrayList<TripStatistics>( _mpsTripStats.values() );
				Collections.sort( records, TripStatistics.mpsTripComparator() );
				Collections.reverse( records );
				return records.size() <= count ? records : records.subList( 0, count );	// get the top "count" trip records
			}
		}		
	}
	
	
	/**
	 * Generate a summary of the MPS trip statistics
	 * @return summary of the MPS trip statistics
	 */
	private String getMPSTripSummary( final Calendar startTime, final List<TripStatistics> tripRecords ) {
		if ( tripRecords == null || tripRecords.size() == 0 ) {
			return "No MPS trips since " + TIMESTAMP_FORMAT.format( startTime.getTime() );
		}
		else {
			final StringBuffer statsBuffer = new StringBuffer( "MPS trip summary since " + TIMESTAMP_FORMAT.format( startTime.getTime() ) );
			statsBuffer.append("\n\n");
						
			statsBuffer.append( "(MPS Trips, First Hits, MPS PV, Input Trips, Input PV) \n" );
			for ( final TripStatistics tripStats : tripRecords  ) {
				statsBuffer.append( tripStats.getMPSTrips() );
				statsBuffer.append( ", " + tripStats.getFirstHits() );
				statsBuffer.append( ", " + tripStats.getMPSPV() );
				
				final String inputPV = tripStats.getInputSignal();
				if ( inputPV != null ) {
					statsBuffer.append( ", " + tripStats.getInputTrips() );
					statsBuffer.append( ", " + inputPV );
				}
				
				statsBuffer.append( "\n" );
			}
			
			return statsBuffer.toString();
		}
	}
	
	
	/**
	 * Generate a summary of the top ten MPS trips.
	 * @return a summary of the MPS trips
	 */
	public String getMPSTripSummary() {
		synchronized ( _statsLock ) {
			final List<TripStatistics> topRecords = getTopMPSTripStats( 10 );
			return getMPSTripSummary( _startTime, topRecords );
		}
	}


	/**
	 * Create and start a new timer for checking every hour whether to update the daily statistics.
	 * @return   a new timer for scheduling daily statistics updates.
	 */
	public Timer startStatsUpdateTimer() {
		final long period = 3600 * 1000;    // milliseconds in an hour
		Calendar now = Calendar.getInstance();
		Calendar startTime = Calendar.getInstance();
		startTime.clear();
		// check every hour at one minute past the hour
		startTime.set( now.get( Calendar.YEAR ), now.get( Calendar.MONTH ), now.get( Calendar.DATE ), now.get( Calendar.HOUR ), 1 );

		// schedule updates once a minute
		final Timer timer = new Timer();
		timer.scheduleAtFixedRate(
			new TimerTask() {
				/** update the daily statistics */
				public void run() {
					checkDayUpdateDailyStats();
				}
			}, startTime.getTime(), period );

		return timer;
	}


	/**
	 * Handle connection events from monitored MPS channels.
	 * @author    t6p
	 */
	private class MPSConnectionHandler implements ConnectionListener {
		/** Record filter to use for connected channels with the correlator */
		protected RecordFilter<ChannelTimeRecord> _recordFilter;


		/**
		 * Constructor
		 * @param recordFilter  The record filter to apply to connected channels with the correlator
		 */
		public MPSConnectionHandler( final RecordFilter<ChannelTimeRecord> recordFilter ) {
			_recordFilter = recordFilter;
		}


		/**
		 * Request that the channel monitor events be correlated within the specified correlator.
		 * @param wrapper  The channel wrapper to correlate
		 */
		public void requestToCorrelate( ChannelWrapper wrapper ) {
			wrapper.addConnectionListener( this );
			if ( wrapper.isConnected() ) {
				connectionMade( wrapper.getChannel() );
			}
			else {
				wrapper.requestConnection();
			}
		}


		/**
		 * Ignore connection events from the specified wrapper
		 * @param wrapper  the wrapper from which to ignore connection events
		 */
		public void ignore( ChannelWrapper wrapper ) {
			wrapper.removeConnectionListener( this );
		}


		/**
		 * Ignore connection events from each channel in the wrappers array
		 * @param wrappers  The array of wrappers to ignore
		 */
		public void ignoreAll( final ChannelWrapper[] wrappers ) {
			for ( int index = 0; index < wrappers.length; index++ ) {
				ignore( wrappers[index] );
			}
		}


		/**
		 * Indicates that a connection to the specified channel has been established. Add the channel
		 * to the correlator with the record filter. Update the connection event timestamp.
		 * @param channel  The channel which has been connected.
		 */
		public void connectionMade( Channel channel ) {
			if ( !_correlator.hasSource( channel.channelName() ) ) {
				_correlator.addChannel( channel, _recordFilter );
			}
			_lastMPSConnectionEventTime = new Date();
		}


		/**
		 * Indicates that a connection to the specified channel has been dropped. Update the
		 * connection event timestamp.
		 * @param channel  The channel which has been disconnected.
		 */
		public void connectionDropped( Channel channel ) {
			_lastMPSConnectionEventTime = new Date();
		}
	}


	
	/**
	 * Handle connection events from monitored channels.
	 * @author    t6p
	 */
	private class InputConnectionHandler implements ConnectionListener {
		/**
		 * Request a connection for the channel wrapper and monitor its connection events.
		 * @param wrapper  The channel wrapper to correlate
		 */
		public void requestConnection( final ChannelWrapper wrapper ) {
			wrapper.addConnectionListener( this );
			wrapper.requestConnection();
		}
		
		
		/**
		 * Request connections for the channel wrappers and monitor their connection events.
		 * @param wrappers  The channel wrappers to monitor
		 */
		public void requestConnections( final Collection<InputMonitor> wrappers ) {
			for ( final InputMonitor wrapper : wrappers ) {
				requestConnection( wrapper );
			}
			Channel.flushIO();
		}
		
		
		/**
		 * Ignore connection events from the specified wrapper
		 * @param wrapper  the wrapper from which to ignore connection events
		 */
		public void ignore( final ChannelWrapper wrapper ) {
			wrapper.removeConnectionListener( this );
		}
		
		
		/**
		 * Ignore connection events from each channel in the wrappers array
		 * @param wrappers  The collection of wrappers to ignore
		 */
		public void ignoreAll( final Collection<InputMonitor> wrappers ) {
			if ( wrappers == null )  return;
			
			for ( final InputMonitor wrapper : wrappers ) {
				ignore( wrapper );
			}
		}
		
		
		/**
		 * Indicates that a connection to the specified channel has been established. Add the channel
		 * to the correlator with the record filter. Update the connection event timestamp.
		 * @param channel  The channel which has been connected.
		 */
		public void connectionMade( final Channel channel ) {
			_lastInputConnectionEventTime = new Date();
		}
		
		
		/**
		 * Indicates that a connection to the specified channel has been dropped. Update the
		 * connection event timestamp.
		 * @param channel  The channel which has been disconnected.
		 */
		public void connectionDropped( final Channel channel ) {
			_lastInputConnectionEventTime = new Date();
		}
	}
}

