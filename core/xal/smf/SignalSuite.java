/*
 * SignalSuite.java
 *
 * Created on September 20, 2002, 1:48 PM
 */

package xal.smf;

import xal.tools.data.*;
import xal.tools.transforms.ValueTransform;

import java.util.*;


/**
 * SignalSuite represents the map of handle/signal pairs that identifies a 
 * channel and associates it with a node via the handle.
 *
 * @author  tap
 */
public class SignalSuite {
    protected Map<String,SignalEntry> _signalMap;        // handle-PV name table
    protected Map<String,ValueTransform> _transformTable;     // handle-value transform table
    
    
    /** Creates a new instance of SignalSuite */
    public SignalSuite() {
        _signalMap = new HashMap<String,SignalEntry>();
        _transformTable = new HashMap<String,ValueTransform>();
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
				_signalMap.put( handle, new SignalEntry() );
			}
			final SignalEntry signalEntry = _signalMap.get( handle );
			
            final String signal = channelAdaptor.stringValue( "signal" );
			if ( signal != null )  signalEntry.setSignal( signal );
			
			if ( channelAdaptor.hasAttribute( "settable" ) ) {
				final boolean settable = channelAdaptor.booleanValue( "settable" );
				signalEntry.setSettable( settable );
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
            _transformTable.put( name, transform );
        }        
    }
    
    
    /**
     * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
		final Collection<Map.Entry<String,SignalEntry>> signalMapEntries = _signalMap.entrySet();
		for ( final Map.Entry<String,SignalEntry> entry : signalMapEntries ) {
            final DataAdaptor channelAdaptor = adaptor.createChild("channel");
            final SignalEntry signalEntry = entry.getValue();
            
            channelAdaptor.setValue( "handle", entry.getKey() );
            channelAdaptor.setValue( "signal", signalEntry.signal() );
            channelAdaptor.setValue( "settable", signalEntry.settable() );
        }
    }
    
    
    /** 
     * Check if this suite manages the specified PV signal.
     * @param signal The PV signal name for which to check.
     * @return true if this suite manages the specified signal and false otherwise.
     */
    boolean hasSignal( final String signal ) {
		for ( final SignalEntry entry : _signalMap.values() ) {
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
        final SignalEntry signalEntry = _signalMap.get( handle );        
		return signalEntry != null ? signalEntry.signal() : null;
    }
    
    
    /** 
     * Get all of the handles within this suite.
     * @return The handles managed by this suite.
     */
    public Collection<String> getHandles() {
        return _signalMap.keySet();
    }
    
    
    /** 
     * Check if the signal suite manages the handle.
     * @param handle The handle for which to check availability.
     * @return true if the handle is available and false otherwise.
     */
    public boolean hasHandle( final String handle ) {
        return _signalMap.containsKey( handle );
    }
    
    
    /**
     * Check if the signal entry associated with the specified handle has an 
     * associated value transform.
     * @param handle The handle to check for an associated transform.
     * @return true if the handle has an associated value transform and false otherwise.
     */
    public boolean hasTransform( final String handle ) {
        final SignalEntry signalEntry = _signalMap.get( handle );
		return signalEntry != null ? ( signalEntry.getTransformKey() != null ) : false;
    }
    
    
    /**
     * Get the transform associated with the specified handle.
     * @param handle The handle for which to get the transform.
     * @return The transform for the specified handle.
     */
    public ValueTransform getTransform(String handle) {
        final SignalEntry signalEntry = _signalMap.get(handle);
		return signalEntry != null ? _transformTable.get( signalEntry.getTransformKey() ) : null;
    }
}



/** 
 * Entry in the signal map corresponding to a handle.  In the signal map
 * the key is a handle and the value is an instance of SignalEntry.
 */
class SignalEntry {
	private String _signal;   // the PV signal name
    private boolean _settable;		// whether the PV is settable
    private String _transformkey;   // Name of the transform if any
	
	
    /** Primary Constructor */
    public SignalEntry( final String signal, final boolean settable, final String transformKey ) {
        _signal = signal;
        _settable = settable;
        _transformkey = transformKey;
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
