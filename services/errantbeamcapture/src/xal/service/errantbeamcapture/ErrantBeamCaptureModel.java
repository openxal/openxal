//
// ErrantBeamCaptureModel.java
// xal
//
// Created by Tom Pelaia on 3/26/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.service.errantbeamcapture;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import xal.tools.dispatch.DispatchQueue;
import xal.ca.*;


/** MPSEventCaptureModel */
public class ErrantBeamCaptureModel {
	/** format for file timestamp */
	final static private SimpleDateFormat FILE_TIMESTAMP_FORMAT;
	
	/** format for recording a timestamp up to seconds (subsecond is provided by record formatting) */
	final static private SimpleDateFormat RECORD_ROOT_TIMESTAMP_FORMAT;
	
	/** format for the year */
	final static private SimpleDateFormat YEAR_FORMAT;
	
	/** format for month */
	final static private SimpleDateFormat MONTH_FORMAT;
	
	/** format for the day */
	final static private SimpleDateFormat DAY_FORMAT;
	
	/** default value for a good state of the trip PVs */
	final static private double DEFAULT_TRIP_GOOD_STATE_TARGET = 1.0;
	
	/** default range for the tolerance (seconds) for sync PV timestamps relative to trip timestamp */
	final static private double[] DEFAULT_SYNC_TOLERANCE = new double[] { -0.01, 0.01 };
	
	/** default batch request timeout */
	final static private double DEFAULT_BATCH_TIMEOUT = 60.0;
	
	/** location of the errant beam capture documents directory */
	final private File DOCUMENTS_DIRECTORY;
	
	/** Machine Protection System channels to monitor for trips */
	final private List<Channel> LATCH_TRIP_CHANNELS;
	
	/** channels expected to be synchronized with the trip event */
	final private List<Channel> SYNC_CHANNELS;
	
	/** channels for which to report the latest value but not expected to be synchronized with the trip event */
	final private List<Channel> ASYNC_CHANNELS;
	
	/** targets for the good state values of the trip PVs */
	final private List<Double> TRIP_GOOD_STATE_TARGETS;
	
	/** monitor trip state changes */
	final private MachineStateMonitor TRIP_MONITOR;
	
	/** timeout for batch requests */
	private double _batchRequestTimeout;
	
	/** tolerance (seconds) range for the timestamp of sync PVs relative to the trip timestamp */
	private double[] _batchSyncRequestTolerance;
		
	
	// static initializer
	static {
		FILE_TIMESTAMP_FORMAT = new SimpleDateFormat( "yyyy_MM_dd_HHmmss_SSS" );
		RECORD_ROOT_TIMESTAMP_FORMAT = new SimpleDateFormat( "yyyyMMdd_HHmmss" );
		YEAR_FORMAT = new SimpleDateFormat( "yyyy" );
		MONTH_FORMAT = new SimpleDateFormat( "MMMM" );
		DAY_FORMAT = new SimpleDateFormat( "dd" );
	}
	
	
	/** Constructor */
    public ErrantBeamCaptureModel() {
		final String documentsRoot = System.getProperty( "xal.service.errantbeamcapture.DocumentsRoot" );

		if ( documentsRoot == null ) {
			final String message = "The property \"xal.service.errantbeamcapture.DocumentsRoot\" is required but has not been specified. Terminating...";
			System.err.println( message );
			throw new RuntimeException( message );
		}

		DOCUMENTS_DIRECTORY = new File( documentsRoot );

		if ( !DOCUMENTS_DIRECTORY.exists() ) {
			final String message = "The documents root directory \"" + documentsRoot + "\" specified in the \"xal.service.errantbeamcapture.DocumentsRoot\" property does not exist. Terminating...";
			System.err.println( message );
			throw new RuntimeException( message );
		}
		
		LATCH_TRIP_CHANNELS = new ArrayList<Channel>();
		SYNC_CHANNELS = new ArrayList<Channel>();
		ASYNC_CHANNELS = new ArrayList<Channel>();
		
		TRIP_GOOD_STATE_TARGETS = new ArrayList<Double>();
		
		_batchSyncRequestTolerance = DEFAULT_SYNC_TOLERANCE;
		_batchRequestTimeout = DEFAULT_BATCH_TIMEOUT;
		
		TRIP_MONITOR = new MachineStateMonitor();
    }
	
	
	/** begin monitoring events and logging latched MPS trips and associated PV data */
	public void run() {
		stop();		// stop just in case we are running
		
		parseInput();
		
		// monitor trips
		monitorTrips();
		
		System.out.println( "Errant Beam Capture running..." );
	}
	
	
	/** begin monitoring events and logging latched MPS trips and associated PV data */
	public void stop() {
		clear();		
		System.out.println( "Errant Beam Capture stopping..." );
	}
	
	
	/** clear all settings */
	private void clear() {
		TRIP_MONITOR.clear();
		LATCH_TRIP_CHANNELS.clear();
		SYNC_CHANNELS.clear();
		ASYNC_CHANNELS.clear();
		
		TRIP_GOOD_STATE_TARGETS.clear();
	}

	
	/** Monitor the channels to trigger the capture event */
	private void addTripChannelsToMonitor( final List<Channel> channels ) {
		final int channelCount = channels.size();
		final List<Double> tripGoodStateTargets = new ArrayList<Double>( TRIP_GOOD_STATE_TARGETS );
		final int stateCount = tripGoodStateTargets.size();
		for ( int channelIndex = 0 ; channelIndex < channelCount ; channelIndex++ ) {
			final Channel channel = channels.get( channelIndex );
			
			// get the channel's corresponding good state from the list otherwise use the default
			final double goodStateTarget = channelIndex < stateCount ? tripGoodStateTargets.get( channelIndex ) : DEFAULT_TRIP_GOOD_STATE_TARGET;
			
			TRIP_MONITOR.addChannel( channel, goodStateTarget );
			System.out.println( "Monitor trip state for channel: " + channel.getId() + " with good state target: " + goodStateTarget );
		}
		
		Channel.flushIO();
	}
	
	
	/** request channel connections in advance of use to reduce delay in getting records */
	private void requestChannelConnections( final List<Channel> channels ) {
		for ( final Channel channel : channels ) {
			channel.requestConnection();
			System.out.println( "Requesting connection for channel: " + channel.getId() );
		}
		
		Channel.flushIO();
	}
	
	
	/** monitor for latched MPS trips */
	private void monitorTrips() {		
		final List<Channel> latchTripChannels = new ArrayList<Channel>( LATCH_TRIP_CHANNELS );
				
		// collect async channels to fetch in batch
		final List<Channel> batchAsyncChannels = new ArrayList<Channel>();
		batchAsyncChannels.addAll( ASYNC_CHANNELS );
		
		// collect sync channels to fetch in batch
		final List<Channel> batchSyncChannels = new ArrayList<Channel>();
		batchSyncChannels.addAll( SYNC_CHANNELS );
		
		requestChannelConnections( batchAsyncChannels );
		requestChannelConnections( batchSyncChannels );
		addTripChannelsToMonitor( latchTripChannels );		
		
		final DispatchQueue queue = DispatchQueue.getGlobalDefaultPriorityQueue();
		
		TRIP_MONITOR.setStateChangeListener( new MachineStateChangeListener() {
			/** handle machine state change event */
			public void machineStateChanged( final MachineStateMonitor tripMonitor, final boolean isGoodState, final Timestamp timestamp ) {
				if ( !isGoodState ) {	// machine tripped
					System.out.println( "Captured a trip at " + timestamp );
					
					final Date eventTime = timestamp.getDate();
					
					final MachineState tripState = tripMonitor.getCurrentMachineState();
					
					System.out.println( "Submitting the batch async request..." );
					
					final BatchGetValueTimeRequest batchAsyncRequest = new BatchGetValueTimeRequest( batchAsyncChannels );
					batchAsyncRequest.submit();	// submit the request now to capture without delay

					System.out.println( "Submitting the batch sync request..." );

					final BatchChannelSyncRequest batchSyncRequest = new BatchChannelSyncRequest( batchSyncChannels, timestamp, _batchSyncRequestTolerance );
					batchSyncRequest.submit();	// submit the request now to capture without delay
										
					// JCA won't send events on the current thread so we need to wait on a new thread
					System.out.println( "Dispatching operation to the queue..." );
					queue.dispatchAsync( new Runnable() {
						public void run() {
							System.out.println( "Running the queued operation..." );
							final Date startTime = new Date();
							
							// wait up to the specified timeout for the request to complete
							batchAsyncRequest.waitForCompletion( _batchRequestTimeout );
							System.out.println( "Batch async complete..." );
							
							// determine how much time (seconds) elapsed since the beginning of the request
							final double timeElapsed = ( (double)( new Date().getTime() - startTime.getTime() ) ) / 1000.0;
							
							// wait up to the specified remaining timeout for the request to complete
							batchSyncRequest.waitForCompletion( _batchRequestTimeout - timeElapsed );
							System.out.println( "Batch sync complete..." );
							batchSyncRequest.dispose();		// clear monitors
														
							// publish the trip event
							System.out.println( "Publishing trip..." );
							publishTripEvent( eventTime, batchAsyncChannels, batchAsyncRequest, batchSyncChannels, batchSyncRequest, LATCH_TRIP_CHANNELS, tripState );
							System.out.println( "Trip published..." );
						}
					});
				}
			}
		});
		
	}
	
	
	/** Publish the trip event */
	private void publishTripEvent( final Date eventTime, final List<Channel> batchAsyncChannels, final BatchGetValueTimeRequest batchAsyncRequest, final List<Channel> batchSyncChannels, final BatchChannelSyncRequest batchSyncRequest, final List<Channel> tripChannels, final MachineState tripState ) {
		try {
			final String filename = FILE_TIMESTAMP_FORMAT.format( eventTime ) + ".dat";
			final String yearString = YEAR_FORMAT.format( eventTime );
			final String monthString = MONTH_FORMAT.format( eventTime );
			final String dayString = DAY_FORMAT.format( eventTime );
			final File eventDirectory = new File( DOCUMENTS_DIRECTORY, "events" );
			final File yearDirectory = new File( eventDirectory, yearString );
			final File monthDirectory = new File( yearDirectory, monthString );
			final File dayDirectory = new File( monthDirectory, dayString );
			
			if ( !dayDirectory.exists() )  dayDirectory.mkdirs();
			if ( dayDirectory.exists() ) {
				final File outputFile = new File( dayDirectory, filename );
				final FileWriter writer = new FileWriter( outputFile );
				
				publishMachineState( tripState, tripChannels, writer );
				publishBatchSyncFetch( batchSyncRequest, batchSyncChannels, writer );
				publishBatchAsyncFetch( batchAsyncRequest, batchAsyncChannels, writer );
				
				writer.flush();
				writer.close();
			}
			else {
				System.err.println( "Failed to create directory: " + dayDirectory );
			}
		}
		catch( Exception exception ) {
			exception.printStackTrace();
		}
	}
	
	
	/** write the batch async fetch events to the writer */
	private void publishBatchAsyncFetch( final BatchGetValueTimeRequest batchRequest, final List<Channel> channels, final Writer writer ) throws IOException {
		for ( final Channel channel : channels ) {
			final ChannelTimeRecord record = batchRequest.getRecord( channel );
			if ( record != null ) {
				publishRecord( record, channel.getId(), writer );
			}
			else {
				System.out.println( "No record for channel: " + channel.getId() );
			}
		}
	}
	
	
	/** write the batch sync fetch events to the writer */
	private void publishBatchSyncFetch( final BatchChannelSyncRequest batchRequest, final List<Channel> channels, final Writer writer ) throws IOException {
		for ( final Channel channel : channels ) {
			final ChannelTimeRecord record = batchRequest.getRecord( channel );
			if ( record != null ) {
				publishRecord( record, channel.getId(), writer );
			}
			else {
				System.out.println( "No record for channel: " + channel.getId() );
			}
		}
	}
	
	
	/** write the machine state event to the writer */
	private void publishMachineState( final MachineState machineState, final List<Channel> channels, final Writer writer ) throws IOException {
		for ( final Channel channel : channels ) {
			final String channelID = channel.getId();
			final ChannelTimeRecord record = machineState.getRecordForChannelID( channelID );
			if ( record != null ) {
				publishRecord( record, channelID, writer );
			}
			else {
				System.out.println( "No record for channel: " + channelID );
			}
		}
	}
	
	
	/** write the record to the writer */
	private void publishRecord( final ChannelTimeRecord record, final String channelID, final Writer writer ) throws IOException {
		writer.write( channelID );
		writer.write( " " );
		
		writer.write( record.getTimestamp().toString( RECORD_ROOT_TIMESTAMP_FORMAT ) );
		writer.write( " " );
		
		writer.write( String.valueOf( record.status() ) );
		writer.write( " " );
		
		writer.write( String.valueOf( record.severity() ) );
		writer.write( " " );
		
		final String[] stringArrayValue = record.stringArray();
		for ( final String stringValue : stringArrayValue ) {
			writer.write( stringValue );
			writer.write( " " );
		}
		
		writer.write( "\n\n" );
		writer.flush();
	}
	
	
	/** parse the input file with the channels to monitor */
	public void parseInput() {		
		// archive of keyed lists
		final Map<String,List<String>> keyedArchive = new HashMap<String,List<String>>();
		
		try {
			final File inputFile = new File( DOCUMENTS_DIRECTORY, "input.ebconf" );
			final FileReader inputFileReader = new FileReader( inputFile );
			final BufferedReader inputReader = new BufferedReader( inputFileReader );
			InputDocumentParser.parse( inputReader, keyedArchive );
		}
		catch( Exception exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Exception parsing input file", exception );
		}
		
		// extract the channels to monitor from the archive by examining the keys and parsing the dot delimited flags contained in the key
		for ( final String key : keyedArchive.keySet() ) {
			final String[] keyFlags = key.split( "\\." );		// flags are dot delimited
			boolean isPV = false;
			boolean isSync = false;
			boolean isSuffix = false;
			boolean isRoot = false;
			boolean isTrip = false;
			boolean isState = false;
			boolean isGoodTarget = false;
			boolean isBatch = false;
			boolean isTimeout = false;
			boolean isTolerance = false;
			String identifier = null;
			// loop over each flag and determine how to process the archived list associated with the key (order does not matter except for the identifier)
			for ( final String keyFlag : keyFlags ) {
				if ( keyFlag.equalsIgnoreCase( "pv" ) )  isPV = true;
				else if ( keyFlag.equalsIgnoreCase( "sync" ) )  isSync = true;
				else if ( keyFlag.equalsIgnoreCase( "suffix" ) )  isSuffix = true;
				else if ( keyFlag.equalsIgnoreCase( "root" ) )  isRoot = true;
				else if ( keyFlag.equalsIgnoreCase( "trip" ) )  isTrip = true;
				else if ( keyFlag.equalsIgnoreCase( "state" ) )  isState = true;
				else if ( keyFlag.equalsIgnoreCase( "good" ) )  isGoodTarget = true;
				else if ( keyFlag.equalsIgnoreCase( "batch" ) )  isBatch = true;
				else if ( keyFlag.equalsIgnoreCase( "timeout" ) )  isTimeout = true;
				else if ( keyFlag.equalsIgnoreCase( "tolerance" ) )  isTolerance = true;
				else {	// anything that isn't a known flag is dot chained as an identifier
					if ( identifier == null )  identifier = keyFlag;
					else  identifier += "." + keyFlag;
				}
			}
			
			//System.out.println( "\t isPV: " + isPV + ", isSync: " + isSync + ", isSuffix: " + isSuffix + ", isRoot: " + isRoot + ", isTrip: " + isTrip + ", identifier: " + identifier );
			
			if ( isPV && !isRoot ) {	// process channels for all PVs (roots are processed through suffixes related by the identifier)
				final List<Channel> channels = getChannelsForKeyIdentifierSuffixKey( keyedArchive, key, identifier, isSuffix );
				if ( isTrip ) {
					LATCH_TRIP_CHANNELS.addAll( channels );	
				}
				else if ( isSync ) {
					SYNC_CHANNELS.addAll( channels ); 
				}
				else {
					ASYNC_CHANNELS.addAll( channels );
				}
			}
			else if ( isTrip && isState && isGoodTarget ) {
				final List<Double> goodStateTargets = getTripGoodStateTargetsForKey( keyedArchive, key );
				TRIP_GOOD_STATE_TARGETS.addAll( goodStateTargets );
			}
			else if ( isBatch && isTimeout ) {
				_batchRequestTimeout = getDoubleValueForKey( keyedArchive, key, DEFAULT_BATCH_TIMEOUT );
			}
			else if ( isSync && isTolerance ) {
				_batchSyncRequestTolerance = getDoubleArrayForKey( keyedArchive, key, 2, DEFAULT_SYNC_TOLERANCE );
			}
		}
	}
	
	
	/** Get the double value from the keyed archive corresponding to the specified key or the default value if there isn't a match */
	static private double getDoubleValueForKey( final Map<String,List<String>> keyedArchive, final String key, final double defaultValue ) {
		final List<String> rawValues = keyedArchive.get( key );
		if ( rawValues != null && rawValues.size() == 1 ) {		// there should be exactly one value
			final String rawValue = rawValues.get( 0 );
			try {
				return Double.parseDouble( rawValue );
			}
			catch( Exception exception ) {
				System.err.println( "Invalid double value: " + rawValue );
				exception.printStackTrace();
				System.exit( 1 );
				return defaultValue;	// never executed but needed to satisfy return requirement
			}
		}
		else {
			return defaultValue;
		}
	}
	
	
	/** Get the double array from the keyed archive corresponding to the specified key or the default array if there isn't a match */
	static private double[] getDoubleArrayForKey( final Map<String,List<String>> keyedArchive, final String key, final int size, final double[] defaultArray ) {
		final List<String> rawValues = keyedArchive.get( key );
		if ( rawValues != null && rawValues.size() == size ) {
			final double[] array = new double[size];
			for ( int index = 0 ; index < size ; index++ ) {
				final String rawValue = rawValues.get( index );
				try {
					array[index] = Double.parseDouble( rawValue );
				}
				catch( Exception exception ) {
					System.err.println( "Invalid double value: " + rawValue );
					exception.printStackTrace();
					System.exit( 1 );
					return defaultArray;	// never executed but needed to satisfy return requirement
				}
			}
			return array;
		}
		else {
			return defaultArray;
		}
	}
	
	
	/** Get the target values for good state corresponding to the trip PVs */
	static private List<Double> getTripGoodStateTargetsForKey( final Map<String,List<String>> keyedArchive, final String key ) {
		final List<String> rawValues = keyedArchive.get( key );
		if ( rawValues != null ) {
			final List<Double> targetValues = new ArrayList<Double>( rawValues.size() );
			for ( final String rawValue : rawValues ) {
				try {
					final double value = Double.parseDouble( rawValue );
					targetValues.add( value );
				}
				catch ( Exception exception ) {
					if ( rawValue.equalsIgnoreCase( "default" ) ) {
						targetValues.add( DEFAULT_TRIP_GOOD_STATE_TARGET );
					}
					else {
						System.err.println( "Invalid trip state target: " + rawValue );
						System.err.println( "Trip state must either be a numeric value or \"default\" (without quotes)." );
						exception.printStackTrace();
						System.exit( 1 );
					}
				}
			}
			
			return targetValues;
		}
		else {
			return Collections.<Double>emptyList();
		}
	}
	
	
	/** Get the channels for specified key and optional identifier and suffix */
	static private List<Channel> getChannelsForKeyIdentifierSuffixKey( final Map<String,List<String>> keyedArchive, final String key, final String identifier, final boolean isSuffix ) {
		if ( isSuffix ) {
			return getChannelsForChannelKeySuffixKey( keyedArchive, "root." + identifier, key );
		}
		else {
			return getChannelsForChannelKey( keyedArchive, key );
		}
	}
	
	
	/** Get the channels for the specified key and suffix */
	static private List<Channel> getChannelsForChannelKeySuffixKey( final Map<String,List<String>> keyedArchive, final String channelKey, final String suffixKey ) {
		final List<String> suffixes = keyedArchive.get( suffixKey );
		return getChannelsForChannelKey( keyedArchive, channelKey, suffixes );
	}	
	
	
	/** Get the channels for the specified key and suffix */
	static private List<Channel> getChannelsForChannelKey( final Map<String,List<String>> keyedArchive, final String key, final List<String> suffixes ) {
		final List<String> pvRoots = keyedArchive.get( key );
		
		if ( pvRoots != null ) {
			final ChannelFactory channelFactory = ChannelFactory.defaultFactory();
			final List<Channel> channels = new ArrayList<Channel>( pvRoots.size() * suffixes.size() );
			
			for ( final String pvRoot : pvRoots ) {
				for ( final String suffix : suffixes ) {
					final Channel channel = channelFactory.getChannel( pvRoot + suffix );
					channels.add( channel );
				}
			}
			
			return channels;
		}
		else {
			return Collections.<Channel>emptyList();
		}
	}
	
	
	/** Get the channels for the specified key and suffix */
	static private List<Channel> getChannelsForChannelKey( final Map<String,List<String>> keyedArchive, final String key ) {
		final List<String> suffixes = Collections.singletonList( "" );
		return getChannelsForChannelKey( keyedArchive, key, suffixes );
	}
}
