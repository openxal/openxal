/*
 * MagnetPowerSupply.java
 *
 * Created on June 27, 2003, 3:52 PM
 */

package xal.smf.impl;

import xal.smf.*;
import xal.tools.data.*;
import xal.ca.*;

import java.util.*;


/**
 * PowerSupply is the abstract super class of all power supplies.
 * @author  tap
 */
public abstract class MagnetPowerSupply implements DataListener {
    final protected Accelerator accelerator;
    final protected ChannelSuite channelSuite;
	protected String strId;

    // cycle state constants
    public static final int CYCLE_INVALID = 0;
    public static final int CYCLING = 1;
    public static final int CYCLE_VALID = 2;
    
    // channel handles
    public static final String CYCLE_STATE_HANDLE = "cycleState";
    public static final String CURRENT_SET_HANDLE = "I_Set"; 
    public static final String CURRENT_RB_HANDLE = "I"; 
        
    
    /** Creates a new instance of PowerSupply using the same channel factory as the provided accelerator */
    public MagnetPowerSupply( final Accelerator anAccelerator ) {
		this.accelerator = anAccelerator;
		this.channelSuite = anAccelerator != null ? new ChannelSuite( anAccelerator.channelSuite().getChannelFactory() ) : new ChannelSuite();
    }
    
    
    /**
     * Get the unique power supply ID
     * @return The power supply ID
     */
    public String getId() {
        return strId;
    }


	/** 
	 * Get the associated accelerator
	 * @return the accelerator
	 */
	public Accelerator getAccelerator() {
		return accelerator;
	}
    
    
    /**
     * Get the power supply type
     * @return The power supply type
     */
    abstract public String getType();
    
    
    /** 
     * dataLabel() provides the name used to identify the class in an 
     * external data source.
     * @return a tag that identifies the receiver's type
     */
    public String dataLabel() {
        return "PS";
    }
    
    
    /**
     * Instructs the receiver to update its data based on the given adaptor.
     * @param adaptor The adaptor from which to update the receiver's data
     */
    public void update(DataAdaptor adaptor) {
        strId = adaptor.stringValue("id");
        DataAdaptor suiteAdaptor = adaptor.childAdaptor("channelsuite");
        channelSuite.update(suiteAdaptor);
    }
    
    
    /**
     * Instructs the receiver to write its data to the adaptor for external
     * storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write(DataAdaptor adaptor) {
        adaptor.setValue("id", strId);
        adaptor.setValue("type", getType());
        adaptor.writeNode(channelSuite);
    }
    
    
    /**
     * Get the channel suite.
     * @return the channel suite.
     */
    public ChannelSuite getChannelSuite() {
        return channelSuite;
    }
    
    
    /**
     * Find the channel for the specified handle.
     * @param handle The handle for the channel to fetch
     * @return the channel if found or null if not found
     */
    public Channel findChannel( final String handle ) {
        return channelSuite.getChannel( handle );
    }
    
    
    /**
     * Get the channel for the specified handle.
     * @param handle The handle for the channel to fetch
     * @return the channel
     */
    public Channel getChannel( final String handle ) throws NoSuchChannelException {
        final Channel channel = findChannel( handle );
        
        if ( channel == null ) {
            throw new NoSuchChannelException( this, handle );
        }
        
        return channel;
    }
    
    
    /**
     * Get the channel corresponding to the specified handle and connect it. 
     * @param handle The handle for the channel to get.
     * @return The channel associated with this node and the specified handle or null if there is no match.
     * @throws xal.smf.NoSuchChannelException if no such channel as specified by the handle is associated with this node.
     * @throws xal.ca.ConnectionException if the channel cannot be connected
     */
    public Channel getAndConnectChannel( final String handle ) throws NoSuchChannelException, ConnectionException {
        Channel channel = getChannel( handle );
        channel.connectAndWait();
        
        return channel;
    }
    
    
    /** 
     * Gets the cycle state of the magnet.  The magnet may be in one of three 
     * states: cycle is invalid (field changed in reverse direction of initial setting), 
     * cycling in progress or cycle is valid
     * @return One of CYCLE_INVALID, CYCLING or CYCLE_VALID
     */
    public int getCycleState() throws ConnectionException, GetException {
        Channel cycleStateChannel = getAndConnectChannel(CYCLE_STATE_HANDLE);
        
	return cycleStateChannel.getValInt();
    }
    
    
    /**
     * Get the magnet power supply current
     * @return the magnet power supply current in amperes
     * @throws xal.ca.ConnectionException if the readback channel cannot be connected
     * @throws xal.ca.GetException if the readback channel get action fails
     */
    public double getCurrent() throws ConnectionException, GetException {
        Channel currentRBChannel = getAndConnectChannel(CURRENT_RB_HANDLE);
            
	return currentRBChannel.getValDbl();
    }
    
    
    /**
     * Set the magnet power supply current.
     * @param current The current in amperes
     * @throws xal.ca.ConnectionException if the put channel cannot be connected
     * @throws xal.ca.PutException if the put channel set action fails
     */
    public void setCurrent(double current) throws ConnectionException, PutException {
        Channel currentSetChannel = getAndConnectChannel(CURRENT_SET_HANDLE);
        currentSetChannel.putVal(current);
    }
    

    /** get the current lower settable limit (A) */  
    public double upperCurrentLimit() throws ConnectionException, GetException {
        Channel currentSetChannel = getAndConnectChannel(CURRENT_SET_HANDLE);
        
        return currentSetChannel.upperControlLimit().doubleValue();
    }
    

    /** get the current lower settable limit (A) */  
    public double lowerCurrentLimit() throws ConnectionException, GetException {
        Channel currentSetChannel = getAndConnectChannel(CURRENT_SET_HANDLE);
        
        return currentSetChannel.lowerControlLimit().doubleValue();
    }
    
    
    /**
     * Get the accelerator nodes which are tied to this supply.
     * @return The collection of nodes that use this supply.
     */
	@SuppressWarnings( "rawtypes" )
    public Collection getNodes() {
        return getNodes( accelerator.getAllNodesOfType(Electromagnet.s_strType) );
    }
        
        
    /**
     * Get the subset of nodes from trialNodes which are supplied by this power supply.
     * @param trialNodes The collection of nodes from which to check for matches.
     * @return The collection of nodes that use this supply.
     */
    public <NodeType extends AcceleratorNode> Collection<NodeType> getNodes( final Collection<NodeType> trialNodes ) {
        final Collection<NodeType> nodes = new HashSet<NodeType>();
                
        for ( final NodeType trialNode : trialNodes ) {
            if ( suppliesNode( trialNode ) ) {
                nodes.add( trialNode );
            }
        }
        
        return nodes;
    }
    
    
    /**
     * Check if the electromagnet is supplied by this power supply.
     * @param node The electromagnet to check
     * @return true if the node is supplied by this supply and false otherwise
     */
    abstract public boolean suppliesNode(AcceleratorNode node);
}
