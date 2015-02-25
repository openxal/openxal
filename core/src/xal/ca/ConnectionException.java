package xal.ca;
/*
 * ConnectionException.java
 *
 * Created on October 19, 2001, 11:21 AM
 */

/**
 *
 * @author  CKAllen
 */
public class ConnectionException extends ChannelException {
    /** required for serializable objects */
    private static final long serialVersionUID = 1L;
    
    private Channel _channel;

    /**
     * Creates new <code>ConnectionException</code> without detail message.
     */
    public ConnectionException() {
    }


    /**
     * Constructs an <code>ConnectionException</code> with the specified detail message.
	 * @param channel for which the exception was thrown
     * @param msg the detail message.
     */
    public ConnectionException(Channel channel, String msg) {
        super(msg);
        _channel = channel;
    }
    
    
    /**
     * Get the channel for which the connection exception was thrown.
     * @return The channel for which the connection exception was thrown.
     */
    public Channel getChannel() {
        return _channel;
    }
}


