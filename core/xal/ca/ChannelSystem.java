/*
 * ChannelSystem.java
 *
 * Created on August 27, 2002, 2:22 PM
 */

package xal.ca;

/**
 * ChannelSystem is a wrapper for static methods of Channel subclasses that 
 * are public and should be generally visible.  This is necessary since there  
 * is no abstract static method construct in Java.  A subclass of ChannelSystem
 * will handle the request as appropriate by calling static methods of the 
 * target Channel subclass.
 *
 * @author  tap
 */
abstract public class ChannelSystem {
    protected volatile boolean willSyncRequest;
    
	
    /** Creates a new instance of ChannelSystem */
    protected ChannelSystem() {
        willSyncRequest = false;
    }
    
    
	/**
	 * Set the debug mode of the channel system.
	 * @param debugFlag True to enable debug mode and false to disable debug mode.
	 */
    abstract public void setDebugMode( final boolean debugFlag );
    
    
	/**
	 * Set whether the requests are handled synchronously or asynchronously
	 * @param newStatus true for synchronous mode and false for asynchronous mode
	 * @deprecated We no longer support global synchronization flags since they are not thread safe
	 */
    public void setSyncRequest( final boolean newStatus ) {
        willSyncRequest = newStatus;
    }
    
    
	/**
	 * Determine whether requests will be handled synchronously
	 * @return true if requests are handled synchronously
	 * @deprecated We no longer support global synchronization flags since they are not thread safe
	 */
    public boolean willSyncRequest() {
        return willSyncRequest;
    }
	
	
	/** flush requests to the server */
	abstract public void flushIO();
    
    
	/**
	 * Schedule an IO request with the specified timeout
	 * @param timeout the maximum time to wait for a successful pend IO 
	 */
    abstract public boolean pendIO( final double timeout );
	
	
	/**
	 * Schedule the queued requests with the specified timeout
	 * @param timeout the maximum time to wait for successful handling of the request
	 */
    abstract public void pendEvent( final double timeout );
}
