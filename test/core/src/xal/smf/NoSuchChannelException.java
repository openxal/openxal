package xal.smf;

import xal.smf.impl.MagnetPowerSupply;


/**
 * NoSuchChannelException is thrown when a channel is requested for a specified
 * handle and either a node or a power supply and no such channel is found.
 */
public class NoSuchChannelException extends RuntimeException {
    /** serialization ID */
    private static final long serialVersionUID = 1L;

    
    /**
     * Creates new <code>NoSuchChannelException</code> without detail message.
     */
    public NoSuchChannelException() {
    }


    /**
     * Constructs an <code>NoSuchChannelException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public NoSuchChannelException(String msg) {
        super(msg);
    }
    
    
    /**
     * NoSuchChannelException constructor for a missing channel corresponding to the specified 
     * node and handle.
     * @param node The node where to find the channel
     * @param handle The node's handle for the channel
     */
    public NoSuchChannelException(AcceleratorNode node, String handle) {
        this("Channel cannot be found for node: " + node.getId() + " and handle: "  + handle);
    }
    
    
    /**
     * NoSuchChannelException constructor for a missing channel corresponding to the specified 
     * powerSupply and handle.
     * @param powerSupply The node where to find the channel
     * @param handle The handle for the channel
     */
    public NoSuchChannelException(MagnetPowerSupply powerSupply, String handle) {
        this("Channel cannot be found for power supply: " + powerSupply.getId() + " and handle: "  + handle);
    }
    
    
    /**
     * NoSuchChannelException constructor for a missing channel corresponding to the specified 
     * timing center and handle.
     * @param timingCenter The timingCenter where to find the channel
     * @param handle The handle for the channel
     */
    public NoSuchChannelException(TimingCenter timingCenter, String handle) {
        this("Channel with handle: \"" + handle + "\" cannot be found in the timing center.");
    }
}

