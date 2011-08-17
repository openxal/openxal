package xal.ca;
/*
 * BadChannelException.java
 *
 * Created on October 30, 2001, 10:36 AM
 */


/**
 *
 * @author  Christopher K. Allen
 * @version 1.0
 */


public class BadChannelException extends ChannelException {
    /** required for serializable objects */
    private static final long serialVersionUID = 1L;

    /**
     * Creates new <code>BadChannelException</code> without detail message.
     */
    public BadChannelException() {
    }


    /**
     * Constructs an <code>BadChannelException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public BadChannelException(String msg) {
        super(msg);
    }
}


