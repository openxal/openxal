package xal.ca;
/*
 * ChannelException.java
 *
 * Created on October 19, 2001, 11:17 AM
 */


/**
 * Base exception for channel operations
 * @author  CKAllen
 * @author  tapsns
 */
public class ChannelException extends java.lang.Exception {
    /** required for serializable objects */
    private static final long serialVersionUID = 1L;

    /**
     * Creates new <code>ChannelException</code> without detail message.
     */
    public ChannelException() {
    }


    /**
     * Constructs an <code>ChannelException</code> with the specified detail message.
     * @param message the detail message.
     */
    public ChannelException( final String message ) {
        super( message );
    }
}


