/*
 * ChannelFactory.java
 *
 * Created on August 26, 2002, 1:24 PM
 */

package xal.ca;

import xal.tools.transforms.ValueTransform;

import java.util.*;
import java.lang.reflect.Method;


/**
 * ChannelFactory is a factory for generating channels.
 *
 * @author  tap
 */
abstract public class ChannelFactory {
    /** default channel factory instance */
    static final private ChannelFactory DEFAULT_FACTORY;
    
    
    /** map of channels keyed by signal name */
    private final Map<String,Channel> CHANNEL_MAP;
    
    
    static {
        DEFAULT_FACTORY = newFactory();
    }
    
    
    /** Creates a new instance of ChannelFactory */
    protected ChannelFactory() {
        CHANNEL_MAP = new Hashtable<String,Channel>();
    }
	
	
	/**
	 * Initialize the channel system
	 * @return true if the initialization was successful and false if not
	 */
	abstract public boolean init();
    
    
    /**  
     * Get a channel associated with the signal name.  If the channel is already 
     * in our map, then return it, otherwise create a new one and add it to our channel map.
     * @param signalName The PV signal name of the channel
     * @return The channel corresponding to the signal name
     */
    public Channel getChannel( final String signalName ) {
        Channel channel;
        
        if ( !CHANNEL_MAP.containsKey( signalName ) ) {
            channel = newChannel( signalName );
            CHANNEL_MAP.put( signalName, channel );
        }
        else {
            channel = CHANNEL_MAP.get( signalName );
        }
        
        return channel;
    }
    
    
    /**  
     * Get a channel associated with the signal name and transform.  If the channel is already 
     * in our map, then return it, otherwise create a new one and add it to our channel map.
     * @param signalName The PV signal name of the channel
     * @param transform The channel's value transform
     * @return The channel corresponding to the signal name
     */
    public Channel getChannel( final String signalName, final ValueTransform transform ) {
		final String channelID = Channel.generateId( signalName,  transform );
		if ( !CHANNEL_MAP.containsKey( channelID ) ) {
            final Channel channel = newChannel( signalName, transform );
            CHANNEL_MAP.put( channelID, channel );
			return channel;
        }
        else {
            final Channel channel = CHANNEL_MAP.get( channelID );
			return channel;
        }
    }
    
    
    /** 
	 * Create a concrete channel which makes an appropriate low level channel
	 * @param signalName PV for which to create a new channel
	 * @return a new channel for the specified signal name
	 */
    abstract protected Channel newChannel( final String signalName );
    
    
    /**
     * Create a new channel for the given signal name and set its value transform.
     * @param signalName The PV signal name
     * @param transform The value transform to use in the channel
     * @return The new channel
     */
    protected Channel newChannel(String signalName, ValueTransform transform) {
        Channel channel = newChannel( signalName );
        channel.setValueTransform( transform );
        return channel;
    }
    
    
    /** 
     * Get the default factory which determines the low level channel implementation
     * @return The default channel factory
     */
    static public ChannelFactory defaultFactory() {
        return DEFAULT_FACTORY;
    }
    
    
    /** 
     * Get the associated channel system from the channel factory implementation.
     * @return The channel system
     */
    abstract protected ChannelSystem channelSystem();
    
    
    /** 
	 * get the defualt system which handles static behavior of Channels 
	 * @return the channel system associated with the default channel factory
	 */
    static ChannelSystem defaultSystem() {
        return DEFAULT_FACTORY.channelSystem();
    }
    
    
    /** 
	 * Instantiate a new ChannelFactory
	 * @return a new channel factory
	 */
    static protected ChannelFactory newFactory() {
		try {
			// effectively returns ChannelFactoryPlugin.getChannelFactoryInstance()
			final Class<?> pluginClass = Class.forName( "xal.ca.ChannelFactoryPlugin" );
			final Method creatorMethod = pluginClass.getMethod( "getChannelFactoryInstance" );
			return (ChannelFactory)creatorMethod.invoke( null );
		}
		catch( Exception exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Failed to load the ChannelFactoryPlugin: " + exception.getMessage() );
		}
    }

	
	/**
	 * Instantiate a new server ChannelFactory
	 * @return a new server channel factory
	 */
	public static ChannelFactory newServerFactory() {
		try {
			// effectively returns ChannelFactoryPlugin.getServerChannelFactoryInstance()
			final Class<?> pluginClass = Class.forName( "xal.ca.ChannelFactoryPlugin" );
			final Method creatorMethod = pluginClass.getMethod( "getServerChannelFactoryInstance" );
			return (ChannelFactory)creatorMethod.invoke( null );
		}
		catch( Exception exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Failed to load the ChannelFactoryPlugin: " + exception.getMessage() );
		}
	}


    /** Print information about this factory */
    abstract public void printInfo();
}
