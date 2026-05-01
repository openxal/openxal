/*
 * ChannelSuite.java
 *
 * Created on September 18, 2002, 4:32 PM
 */

package xal.smf;

import xal.ca.*;
import xal.tools.data.*;
import xal.tools.transforms.ValueTransform;

import java.util.*;


/**
 * Manage the mapping of handles to signals and channels for a node.  A signal 
 * is the unique PV name used for accessing EPICS records.  A handle is a 
 * high level name used to access a PV in a specific context.  For example a 
 * channel suite instance typically represents a suite of PVs associated with 
 * a particular element.  Consider a BPM element.  It has several PVs associated
 * with it.  The handles are labels common to all BPMs such as "xAvg", "yAvg", ...
 * The handle is used to access a particular PV when applied to an element.  So 
 * for example "xAvg" applied to BPM 1 of the MEBT refers to the specific PV
 * "MEBT_Diag:BPM01:xAvg".  Thus a handle is to a ChannelSuite instance much like an 
 * instance variable is to an instance of a class.
 *
 * @author  tap
 */
public class ChannelSuite implements DataListener {
	static final public String DATA_LABEL = "channelsuite";
    
    /** map of channels keyed by handle */
    final private Map<String,Channel> CHANNEL_HANDLE_MAP;
	
    /** channel factory for getting channels */
	final private ChannelFactory CHANNEL_FACTORY;
    
    /** Signal Suite */
    final private SignalSuite SIGNAL_SUITE;
    
    
    /** Creates a new instance of ChannelSuite using the default channel factory */
    public ChannelSuite() {
		this( null );
    }
	
	
	/**
	 * Primary constructor for creating an instance of channel suite
	 * @param channelFactory channel factory (or null for default factory) for generating channels
	 */
	public ChannelSuite( final ChannelFactory channelFactory ) {
		CHANNEL_FACTORY = channelFactory != null ? channelFactory : ChannelFactory.defaultFactory();
        CHANNEL_HANDLE_MAP = new HashMap<String,Channel>();
        SIGNAL_SUITE = new SignalSuite();
	}


	/** get the channel factory */
	public ChannelFactory getChannelFactory() {
		return CHANNEL_FACTORY;
	}
    
    
    /** 
     * dataLabel() provides the name used to identify the class in an 
     * external data source.
     * @return a tag that identifies the receiver's type
     */
    public String dataLabel() { return DATA_LABEL; }
    
    
    /**
     * Update the data based on the information provided by the data provider.
     * @param adaptor The adaptor from which to update the data
     */
    public void update( final DataAdaptor adaptor ) {
        SIGNAL_SUITE.update( adaptor );
    }
    
    
    /**
     * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
        SIGNAL_SUITE.write( adaptor );
    }


	/**
	 * Programmatically add or replace a channel corresponding to the specified handle
	 * @param handle The handle referring to the signal
	 * @param signal PV signal associated with the handle
	 * @param settable indicates whether the channel is settable
	 * @param transformKey Key of the signal's transformation
	 * @param valid specifies whether the channel is marked valid
	 */
	public void putChannel( final String handle, final String signal, final boolean settable, final String transformKey, final boolean valid ) {
		SIGNAL_SUITE.putChannel( handle, signal, settable, transformKey, valid );
	}


	/**
	 * Convenience method to programmatically add or replace a channel corresponding to the specified handle with valid set to true
	 * @param handle The handle referring to the signal
	 * @param signal PV signal associated with the handle
	 * @param settable indicates whether the channel is settable
	 * @param transformKey Key of the signal's transformation
	 */
	public void putChannel( final String handle, final String signal, final boolean settable, final String transformKey ) {
		putChannel( handle, signal, settable, transformKey, true );
	}


	/**
	 * Convenience method to programmatically add or replace a channel corresponding to the specified handle with valid set to true and no transform
	 * @param handle The handle referring to the signal
	 * @param signal PV signal associated with the handle
	 * @param settable indicates whether the channel is settable
	 */
	public void putChannel( final String handle, final String signal, final boolean settable ) {
		putChannel( handle, signal, settable, null );
	}

	
	/**
	 * Programmatically assign a transform for the specified name
	 * @param name key for associating the transform
	 * @param transform the value transform
	 */
	public void putTransform( final String name, final ValueTransform transform ) {
		SIGNAL_SUITE.putTransform( name, transform );
	}


    /** 
     * See if this channel suite manages the specified signal.
     * @param signal The PV signal to check for availability.
     * @return true if the PV signal is available and false if not.
     */
    protected boolean hasSignal( final String signal ) {
        return SIGNAL_SUITE.hasSignal( signal );
    }
    
    
    /** 
     * See if this channel suite manages the specified handle.
     * @param handle The handle to check for availability.
     * @return true if the handle is available and false if not.
     */
    final public boolean hasHandle( final String handle ) {
        return SIGNAL_SUITE.hasHandle( handle );
    }
    
    
    /** 
     * Get all of the handles managed by the is channel suite.
     * @return The handles managed by this channel suite.
     */
    final public Collection<String> getHandles() {
        return SIGNAL_SUITE.getHandles();
    }
    
    
    /** 
     * Get the channel signal corresponding to the handle.
     * @param handle The handle for which to get the PV signal name.
     * @return Get the PV signal name associated with the specified handle or null if it is not found.
     */
    final public String getSignal( final String handle ) {
        return SIGNAL_SUITE.getSignal( handle );
    }
    
    
    /**
     * Get the transform associated with the specified handle.
     * @param handle The handle for which to get the transform.
     * @return The transform for the specified handle.
     */
    final public ValueTransform getTransform( final String handle ) {
        return SIGNAL_SUITE.getTransform( handle );
    }


    /**
     * Determine whether the handle's corresponding PV is valid.
     * @param handle The handle for which to get the validity.
     * @return validity state of the PV or false if there is no entry for the handle
     */
    final public boolean isValid( final String handle ) {
		return SIGNAL_SUITE.isValid( handle );
    }

    
    /** 
     * Get the channel corresponding to the specified handle.
     * @param handle The handle for which to get the associated Channel.
     * @return The channel associated with the specified handle.
     */
    public Channel getChannel( final String handle ) {
        // first see if we have ever cached the channel
        Channel channel = CHANNEL_HANDLE_MAP.get( handle );
        
        if ( channel == null ) {                    // if the channel was never cached ...
            final String signal = getSignal( handle );      // lookup the signal
            if ( signal != null ) {                 // get the channel from the channel factory
                final ValueTransform transform = getTransform( handle );
                if ( transform != null ) {
                    channel = CHANNEL_FACTORY.getChannel( signal, transform );
                }
                else {
                    channel = CHANNEL_FACTORY.getChannel( signal );
                }

				channel.setValid( isValid( handle ) );

				if (channel instanceof IServerChannel) {
					((IServerChannel)channel).setSettable( SIGNAL_SUITE.isSettable(handle) );
				}

            }
            
            // if we have a channel, cache it for future access
            if ( channel != null ) {
                CHANNEL_HANDLE_MAP.put( handle, channel );
            }
        }
        
        return channel;
    }    
}




/**
 * SignalSuite represents the map of handle/signal pairs that identifies a
 * channel and associates it with a node via the handle.
 *
 * @author  tap
 */
class SignalSuite {
	/** hash set of handles that are settable by default */
	final static private Set<String> SETTABLE_CHANNEL_HANDLES = new HashSet<>();

	// assign the settable handles
	static {
		final String[] HANDLES = { "I_Set", "fieldSet", "cycleEnable", "cavAmpSet", "cavPhaseSet", "deltaTRFStart", "deltaTRFEnd", "tDelay", "blankBeam" };

		for ( final String handle : HANDLES ) {
			SETTABLE_CHANNEL_HANDLES.add( handle );
		}
	}

	/** map of signal entries keyed by handle */
	final private Map<String,SignalEntry> SIGNAL_MAP;        // handle-PV name table

	/** map of transforms keyed by name */
	final private Map<String,ValueTransform> TRANSFORM_MAP;     // handle-value transform table


	/** Creates a new instance of SignalSuite */
	public SignalSuite() {
		SIGNAL_MAP = new HashMap<String,SignalEntry>();
		TRANSFORM_MAP = new HashMap<String,ValueTransform>();
	}


	/** determine if the handle is a settable handle (fixed time lookup since using a Hash Set) */
    private boolean isHandleSettable( final String handle ) {
		return SETTABLE_CHANNEL_HANDLES.contains( handle );
	}


	/**
	 * Update the data based on the information provided by the data provider.
	 * @param adaptor The adaptor from which to update the data
	 */
	public void update( final DataAdaptor adaptor ) {
		final List<DataAdaptor> channelAdaptors = adaptor.childAdaptors( "channel" );
		for ( final DataAdaptor channelAdaptor : channelAdaptors  ) {
			final String handle = channelAdaptor.stringValue("handle");

			if ( !hasHandle( handle ) ) {
				SIGNAL_MAP.put( handle, new SignalEntry() );
			}
			final SignalEntry signalEntry = SIGNAL_MAP.get( handle );

			final String signal = channelAdaptor.stringValue( "signal" );
			if ( signal != null )  signalEntry.setSignal( signal );

			// if the settable attribute is specified, then use its value otherwise fallback to the default settable handles lookup
			if ( channelAdaptor.hasAttribute( "settable" ) ) {
				final boolean settable = channelAdaptor.booleanValue( "settable" );
				signalEntry.setSettable( settable );
			} else if ( isHandleSettable( handle ) ) {		// if settable is not explicitly specified, determine if the handle is settable by default
				signalEntry.setSettable( true );
			}

			if ( channelAdaptor.hasAttribute( "valid" ) ) {
				final boolean valid = channelAdaptor.booleanValue( "valid" );
				signalEntry.setValid( valid );
			}

			if ( channelAdaptor.hasAttribute( "transform" ) ) {
				final String transformKey = channelAdaptor.stringValue( "transform" );
				signalEntry.setTransformKey( transformKey );
			}
		}

		final List<DataAdaptor> transformAdaptors = adaptor.childAdaptors( "transform" );
		for ( final DataAdaptor transformAdaptor : transformAdaptors ) {
			final String name = transformAdaptor.stringValue( "name" );
			final ValueTransform transform = TransformFactory.getTransform( transformAdaptor );
			putTransform( name, transform );
		}
	}


	/**
	 * Write data to the data adaptor for storage.
	 * @param adaptor The adaptor to which the receiver's data is written
	 */
	public void write( final DataAdaptor adaptor ) {
		final Collection<Map.Entry<String,SignalEntry>> signalMapEntries = SIGNAL_MAP.entrySet();
		for ( final Map.Entry<String,SignalEntry> entry : signalMapEntries ) {
			final DataAdaptor channelAdaptor = adaptor.createChild("channel");
			final SignalEntry signalEntry = entry.getValue();

			channelAdaptor.setValue( "handle", entry.getKey() );
			channelAdaptor.setValue( "signal", signalEntry.signal() );
			channelAdaptor.setValue( "settable", signalEntry.settable() );
			channelAdaptor.setValue( "valid", signalEntry.isValid() );
			if ( signalEntry.getTransformKey() != null ) {
				channelAdaptor.setValue( "transform", signalEntry.getTransformKey() );
			}
		}
	}


	/**
	 * Programmatically add or replace a signal entry corresponding to the specified handle
	 * @param handle The handle referring to the signal entry
	 * @param signal PV signal associated with the handle
	 * @param settable indicates whether the channel is settable
	 * @param transformKey Key of the signal's transformation
	 * @param valid specifies whether the channel is marked valid
	 */
	public void putChannel( final String handle, final String signal, final boolean settable, final String transformKey, final boolean valid ) {
		final SignalEntry signalEntry = new SignalEntry( signal, settable, transformKey );
		signalEntry.setValid( valid );
		SIGNAL_MAP.put( handle, signalEntry );
	}


	/**
	 * Programmatically assign a transform for the specified name
	 * @param name key for associating the transform
	 * @param transform the value transform
	 */
	public void putTransform( final String name, final ValueTransform transform ) {
		TRANSFORM_MAP.put( name, transform );
	}


	/**
	 * Check if this suite manages the specified PV signal.
	 * @param signal The PV signal name for which to check.
	 * @return true if this suite manages the specified signal and false otherwise.
	 */
	boolean hasSignal( final String signal ) {
		for ( final SignalEntry entry : SIGNAL_MAP.values() ) {
			if ( entry.signal().equals( signal ) )  return true;
		}

		return false;
	}


	/**
	 * Get the PV signal associated with the handle.
	 * @param handle The handle for which to get the associated PV signal.
	 * @return The signal associated with the specified handle.
	 */
	public String getSignal( final String handle ) {
		final SignalEntry signalEntry = getSignalEntry( handle );
		return signalEntry != null ? signalEntry.signal() : null;
	}


	/**
	 * Get all of the handles within this suite.
	 * @return The handles managed by this suite.
	 */
	public Collection<String> getHandles() {
		return SIGNAL_MAP.keySet();
	}


	/**
	 * Check if the signal suite manages the handle.
	 * @param handle The handle for which to check availability.
	 * @return true if the handle is available and false otherwise.
	 */
	public boolean hasHandle( final String handle ) {
		return SIGNAL_MAP.containsKey( handle );
	}


	/**
	 * Check if the signal entry associated with the specified handle has an
	 * associated value transform.
	 * @param handle The handle to check for an associated transform.
	 * @return true if the handle has an associated value transform and false otherwise.
	 */
	public boolean hasTransform( final String handle ) {
		final SignalEntry signalEntry = getSignalEntry( handle );
		return signalEntry != null ? ( signalEntry.getTransformKey() != null ) : false;
	}


	/**
	 * Get the transform associated with the specified handle.
	 * @param handle The handle for which to get the transform.
	 * @return The transform for the specified handle.
	 */
	public ValueTransform getTransform(String handle) {
		final SignalEntry signalEntry = getSignalEntry( handle );
		return signalEntry != null ? TRANSFORM_MAP.get( signalEntry.getTransformKey() ) : null;
	}


	/**
	 * Determine whether the handle's corresponding PV is valid.
	 * @param handle The handle for which to get the validity.
	 * @return validity state of the PV or false if there is no entry for the handle
	 */
	public boolean isValid( final String handle ) {
		final SignalEntry signalEntry = getSignalEntry( handle );
		return signalEntry != null ? signalEntry.isValid() : false;
	}


	/**
	 * Determine whether the handle's corresponding PV is valid.
	 * @param handle The handle for which to get the validity.
	 * @return validity state of the PV or false if there is no entry for the handle
	 */
	public boolean isSettable( final String handle ) {
		final SignalEntry signalEntry = getSignalEntry( handle );
		return signalEntry != null ? signalEntry.settable() : false;
	}


	/**
	 * Get the signal entry for the handle.
	 * @param handle The handle for which to get the entry.
	 * @return signal entry for the handle or null if there is none
	 */
	private SignalEntry getSignalEntry( final String handle ) {
		return SIGNAL_MAP.get( handle );
	}
}



/**
 * Entry in the signal map corresponding to a handle.  In the signal map
 * the key is a handle and the value is an instance of SignalEntry.
 */
class SignalEntry {
	private String _signal;			// the PV signal name
	private boolean _settable;		// whether the PV is settable
	private boolean _valid;			// whether the channel is marked valid
	private String _transformkey;   // Name of the transform if any


	/** Primary Constructor */
	public SignalEntry( final String signal, final boolean settable, final String transformKey ) {
		_signal = signal;
		_settable = settable;
		_transformkey = transformKey;
		_valid = true;
	}


	/** Constructor */
	public SignalEntry() {
		this( null, false, null );
	}


	/**
	 * Get whether the PV is settable or not.
	 * @return true if the PV is settable and false otherwise.
	 */
	public boolean settable() {
		return _settable;
	}


	/** set the settable property */
	public void setSettable( final boolean isSettable ) {
		_settable = isSettable;
	}


	/** get the valid status of the PV */
	public boolean isValid() {
		return _valid;
	}


	/** mark the valid status of the PV */
	public void setValid( final boolean isValid ) {
		_valid = isValid;
	}


	/**
	 * Get the PV signal name.
	 * @return The PV signal name.
	 */
	public String signal() {
		return _signal;
	}


	/** set the signal */
	public void setSignal( final String signal ) {
		_signal = signal;
	}


	/**
	 * Get the name of the associated transform used
	 * @return the name of the associated transform
	 */
	public String getTransformKey() {
		return _transformkey;
	}


	/** set the transform key */
	public void setTransformKey( final String transformKey ) {
		_transformkey = transformKey;
	}
}


