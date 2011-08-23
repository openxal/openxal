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
    
    
    /** Creates a new instance of ChannelSuite */
    public ChannelSuite() {
		this( ChannelFactory.defaultFactory() );
    }
	
	
	/**
	 * Primary constructor for creating an instance of channel suite
	 */
	public ChannelSuite( final ChannelFactory channelFactory ) {
		CHANNEL_FACTORY = channelFactory;
        CHANNEL_HANDLE_MAP = new HashMap<String,Channel>();
        SIGNAL_SUITE = new SignalSuite();
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
     * Get the channel corresponding to the specified handle.
     * @param handle The handle for which to get the associated Channel.
     * @return The channel associated with the specified handle.
     */
    public Channel getChannel( final String handle ) {
        // first see if we have ever cached the channel
        Channel channel = CHANNEL_HANDLE_MAP.get( handle );
        
        if ( channel == null ) {                    // if the channel was never cached ...
            String signal = getSignal( handle );      // lookup the signal
            if ( signal != null ) {                 // get the channel from the channel factory
                ValueTransform transform = getTransform( handle );
                if ( transform != null ) {
                    channel = CHANNEL_FACTORY.getChannel( signal, transform );
                }
                else {
                    channel = CHANNEL_FACTORY.getChannel( signal );
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






