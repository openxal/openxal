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


