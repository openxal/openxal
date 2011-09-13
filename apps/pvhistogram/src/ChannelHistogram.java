//
//  ChannelHistogram.java
//  xal
//
//  Created by Tom Pelaia on 2/12/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//

package xal.app.pvhistogram;

import java.util.logging.*;
import java.util.*;

import xal.ca.*;
import xal.tools.data.*;
import xal.tools.statistics.*;
import xal.tools.messaging.MessageCenter;
import xal.smf.NodeChannelRef;
import xal.smf.Accelerator;


/** model for the histogram document */
class ChannelHistogram implements DataListener {
 	/** the data adaptor label used for reading and writing this instance */
	static public final String DATA_LABEL = "ChannelHistogram";
	
	/** minimum buffer size */
	final static public int MINIMUM_BUFFER_SIZE;
	
	/** message center for dispatching events to registered listeners */
	final private MessageCenter MESSAGE_CENTER;
	
	/** proxy for forwarding events to registered listeners */
	final private ChannelHistogramListener EVENT_PROXY;
	
	/** monitor event handler */
	final private MonitorHandler MONITOR_HANDLER;
	
	/** connection event handler */
	final private ConnectionHandler CONNECTION_HANDLER;
	
	/** buffer of monitored values */
	final private LinkedList<Double> VALUE_BUFFER;
	
	/** statistics for the monitored samples */
	final private MutableUnivariateStatistics VALUE_STATS;
	
	/** lock for synchronizing histogram modifications and updates */
	final private Object HISTOGRAM_LOCK;
	
	/** lock for synchronizing channel changes */
	final private Object CHANNEL_LOCK;
	
	/** lower and upper limits of the values to histogram */
	final private double[] VALUE_RANGE;
	
	/** manually specified lower and upper limits of the values to histogram if auto limits is disabled */
	private double[] _manualValueRange;
	
	/** source of the channel to monitor */
	private ChannelSource _channelSource;
	
	/** channel monitor */
	private Monitor _monitor;
	
	/** histogram data measured in counts for each bin */
	private int[] _counts;
	
	/** size of the buffer holding monitor value samples */
	private int _bufferSize;
	
	/** number of bins in the histogram */
	private int _binCount;
	
	/** indicates whether the lower and upper limits are automatically calculated */
	private boolean _autoLimits;
	
	/** accelerator used to get node channel references */
	private Accelerator _accelerator;
	
	
	// static initializer
	static {
		MINIMUM_BUFFER_SIZE = 5;
	}
	
	
	/** Constructor */
	public ChannelHistogram() {
		MESSAGE_CENTER = new MessageCenter( "Channel Histogram" );
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, ChannelHistogramListener.class, MessageCenter.FRESH );
		
		CHANNEL_LOCK = new Object();
		HISTOGRAM_LOCK = new Object();
		
		VALUE_STATS = new MutableUnivariateStatistics();
		VALUE_BUFFER = new LinkedList<Double>();
		VALUE_RANGE = new double[] { -1.0, 1.0 };
		_manualValueRange = new double[] { -1.0, 1.0 };
		
		_bufferSize = 1000;
		_binCount = 24;
		_autoLimits = true;
		
		_counts = new int[_binCount];
		
		MONITOR_HANDLER = new MonitorHandler();
		CONNECTION_HANDLER = new ConnectionHandler();
	}
	
    
    /** provides the name used to identify the class in an external data source. */
    public String dataLabel() {
        return DATA_LABEL;
    }
    
    
    /** Instructs the receiver to update its data based on the given adaptor. */
    public void update( final DataAdaptor adaptor ) {
		if ( adaptor.hasAttribute( "bufferSize" ) ) {
			setBufferSize( adaptor.intValue( "bufferSize" ) );
		}
		
		if ( adaptor.hasAttribute( "binCount" ) ) {
			setBinCount( adaptor.intValue( "binCount" ) );
		}
		
		if ( adaptor.hasAttribute( "autoLimits" ) ) {
			setAutoLimits( adaptor.booleanValue( "autoLimits" ) );
		}
		
		if ( adaptor.hasAttribute( "manualLowerLimit" ) && adaptor.hasAttribute( "manualUpperLimit" ) ) {
			final double lowerLimit = adaptor.doubleValue( "manualLowerLimit" );
			final double upperLimit = adaptor.doubleValue( "manualUpperLimit" );
			final double[] range = new double[] { lowerLimit, upperLimit };
			setManualValueRange( range );
		}
		
		final ChannelSource channelSource = ChannelSource.getChannelSource( adaptor, _accelerator );
		setChannelSource( channelSource );
    }
    
    
    /** Instructs the receiver to write its data to the adaptor for external storage. */
    public void write( final DataAdaptor adaptor ) {
        adaptor.setValue( "bufferSize", _bufferSize );
        adaptor.setValue( "binCount", _binCount );
		adaptor.setValue( "autoLimits", _autoLimits );
		adaptor.setValue( "manualLowerLimit", _manualValueRange[0] );
		adaptor.setValue( "manualUpperLimit", _manualValueRange[1] );
		
		if ( _channelSource != null ) {
			_channelSource.write( adaptor );
		}
    }
	
	
	/** set the accelerator */
	public void setAccelerator( final Accelerator accelerator ) {
		_accelerator = accelerator;
	}
	
	
	/** set whether or not to use auto limits */
	public void setAutoLimits( final boolean useAutoLimits ) {
		_autoLimits = useAutoLimits;
	}
	
	
	/** determine whether this model is using auto limits */
	public boolean getAutoLimits() {
		return _autoLimits;
	}
	
	
	/** get the value range to use for the histogram */
	private double[] getHistogramRange() {
		return _autoLimits ? new double[] { VALUE_RANGE[0], VALUE_RANGE[1] } : new double[] { _manualValueRange[0], _manualValueRange[1] };

	}
	
	
	/** get the manual value range */
	public double[] getManualValueRange() {
		return _manualValueRange;
	}
	
	
	/** set the manual value range */
	public void setManualValueRange( final double[] range ) {
		_manualValueRange = range;
	}
	
	
	/** Get the bin count */
	public int getBinCount() {
		return _binCount;
	}
	
	
	/** set the number of bins to use for the histogram */
	public void setBinCount( final int binCount ) {
		synchronized ( HISTOGRAM_LOCK ) {
			_binCount = binCount;
			_counts = new int[binCount];
			populateHistogram();
		}
	}
	
	
	/** add the specified listener as a receiver of channel histogram events from this instance */
	public void addChannelHistogramListener( final ChannelHistogramListener listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, ChannelHistogramListener.class );
	}
	
	
	/** remove the specified listener from receiving channel histogram events from this instance */
	public void removeChannelHistogramListener( final ChannelHistogramListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, ChannelHistogramListener.class );
	}
	
	
	/** clear the buffer and derived data */
	public void clear() {
		synchronized ( HISTOGRAM_LOCK ) {
			VALUE_BUFFER.clear();
			VALUE_STATS.clear();
			clearHistogram();
		}
	}
	
	
	/** clear the just the histogram */
	private void clearHistogram() {
		synchronized ( HISTOGRAM_LOCK ) {
			for ( int index = 0 ; index < _counts.length ; index++ ) {
				_counts[index] = 0;
			}
		}
	}
	
	
	/** populate the histogram from the buffer and return the histogram counts */
	private int[] populateHistogram() {
		synchronized ( HISTOGRAM_LOCK ) {
			return populateHistogram( getHistogramRange() );
		}
	}
	
	
	/** populate the histogram from the buffer and return the histogram counts */
	private int[] populateHistogram( final double[] range ) {
		synchronized ( HISTOGRAM_LOCK ) {
			clearHistogram();
			final double lowerLimit = range[0];
			final double span = range[1] - lowerLimit;
			final double scale = _binCount / span;
			if ( span > 0 ) {
				for ( final double value : VALUE_BUFFER ) {
					final int bin = (int)Math.floor( ( value - lowerLimit ) * scale );
					if ( bin < _counts.length ) {
						++_counts[bin];
					}
				}
			}
			final int[] counts = new int[_counts.length];
			System.arraycopy( _counts, 0, counts, 0, counts.length );
			return counts;
		}
	}
	
	
	/** get the buffer size */
	public int getBufferSize() {
		return _bufferSize;
	}
	
	
	/** set the buffer size */
	public void setBufferSize( final int bufferSize ) {
		_bufferSize = bufferSize > MINIMUM_BUFFER_SIZE ? bufferSize : MINIMUM_BUFFER_SIZE;
	}
	
	
	/** set the channel source from a PV */
	public void setChannelSource( final String pv ) {
		setChannelSource( ChannelSource.getInstance( pv ) );
	}
	
	
	/** set the channel source from a node channel reference */
	public void setChannelSource( final NodeChannelRef channelRef ) {
		setChannelSource( ChannelSource.getInstance( channelRef ) );
	}
	
	
	/** set the channel source from a node channel reference */
	private void setChannelSource( final ChannelSource channelSource ) {
		synchronized ( CHANNEL_LOCK ) {
			final ChannelSource oldSource = _channelSource;
			_channelSource = null;		// flag indicating that the channel source is being modified
			if ( oldSource != null ) {
				final Channel oldChannel = oldSource.getChannel();
				if ( oldChannel != null ) {
					oldChannel.removeConnectionListener( CONNECTION_HANDLER );
				}
			}
			
			stopMonitor();
			
			_channelSource = channelSource;
			if ( channelSource != null ) {
				final Channel channel = channelSource.getChannel();
				if ( channel != null ) {
					channel.addConnectionListener( CONNECTION_HANDLER );
					channel.requestConnection();
					Channel.flushIO();
				}
			}			
		}
		
		clear();	// clear the buffer
		
		EVENT_PROXY.channelSourceChanged( this, channelSource );
	}
	
	
	/** start the monitor for the channel source */
	public void startMonitor() {
		synchronized ( CHANNEL_LOCK ) {
			final ChannelSource channelSource = _channelSource;
			if ( channelSource != null ) {
				final Channel channel = channelSource.getChannel();
				if ( channel.isConnected() ) {
					startMonitor( channel );
				}
			}
		}
	}
	
	
	/** start monitoring the specified channel */
	private void startMonitor( final Channel channel ) {
		synchronized ( CHANNEL_LOCK ) {
			final Monitor oldMonitor = _monitor;
			if ( oldMonitor == null ) {
				try {
					_monitor = channel.addMonitorValue( MONITOR_HANDLER, Monitor.VALUE );
					Channel.flushIO();
				}
				catch ( Exception exception ) {
					Logger.getLogger("global").log( Level.WARNING, "Exception attempting to create a monitor on channel: " + channel.getId(), exception );
					
				}				
			}
		}
	}
	
	
	/** stop monitoring */
	public void stopMonitor() {
		synchronized ( CHANNEL_LOCK ) {
			final Monitor oldMonitor = _monitor;
			if ( oldMonitor != null ) {
				oldMonitor.clear();
			}
			_monitor = null;
		}
	}
	
	
	
	/** handler for monitor callbacks */
	private class MonitorHandler implements IEventSinkValue {
		/** callback for the monitor event */
		public void eventValue( final ChannelRecord record, final Channel channel ) {
			double value = Double.NaN;
			synchronized ( CHANNEL_LOCK ) {
				if ( _channelSource != null && _channelSource.getChannel() == channel ) {	// ensures that we aren't processing old channels during transition
					value = record.doubleValue();
				}
			}
			if ( !Double.isNaN( value ) && !Double.isInfinite( value ) ) {
				final List<Double> buffer = new ArrayList<Double>();
				UnivariateStatistics stats;
				int[] counts;
				double[] range;
				synchronized ( HISTOGRAM_LOCK ) {		// use the buffer instance to synchronize anything associated with values
					VALUE_BUFFER.add( value );
					VALUE_STATS.addSample( value );
					while ( VALUE_BUFFER.size() > _bufferSize ) {
						final double oldValue = VALUE_BUFFER.removeFirst();
						VALUE_STATS.removeSample( oldValue );
					}
					final List<Double> orderedList = new ArrayList<Double>( VALUE_BUFFER );
					Collections.sort( orderedList );
					final int valueCount = orderedList.size();
					if ( valueCount > 1 ) {
						VALUE_RANGE[0] = orderedList.get(0);
						VALUE_RANGE[1] = orderedList.get( valueCount - 1 );
					}
					else if ( valueCount == 1 ) {
						VALUE_RANGE[0] = orderedList.get(0) - 1.0;
						VALUE_RANGE[1] = VALUE_RANGE[1] + 1.0;
					}
					buffer.addAll( VALUE_BUFFER );
					stats = new UnivariateStatistics( VALUE_STATS );
					range = getHistogramRange();
					counts = populateHistogram( range );	// for now just rebuild the entire histogram; need to modify this to just update for changes
				}
				EVENT_PROXY.histogramUpdated( ChannelHistogram.this, range, counts, buffer, stats );
			}
		}
	}
	
	
	
	/** handler for connection events */
	private class ConnectionHandler implements ConnectionListener {
		/** callback for the connect event */
		public void connectionMade( final Channel channel ) {
			if (  _monitor == null && channel != null ) {
				startMonitor();
			}
		}
		
		
		/** callback for the disconnect event */
		public void connectionDropped( final Channel channel ) {}
	}
}
