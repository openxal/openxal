/*
 * ConnectionListener.java
 *
 * Created on August 26, 2002, 2:02 PM
 */

package xal.ca;

/**
 * ConnectionListener is an interface for channel connection events.
 *
 * @author  tap
 */
public interface ConnectionListener {
    
    /**
     * Indicates that a connection to the specified channel has been established.
     * @param channel The channel which has been connected.
     */
    public void connectionMade(Channel channel);
    
    /**
     * Indicates that a connection to the specified channel has been dropped.
     * @param channel The channel which has been disconnected.
     */
    public void connectionDropped(Channel channel);
}
