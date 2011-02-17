package xal.ca;
/*
 * GetException.java
 *
 * Created on November 16, 2001, 4:13 PM
 */

/**
 *
 * @author  CKAllen
 */
public class GetException extends ChannelException {

    /**
     * Creates new <code>GetException</code> without detail message.
     */
    public GetException() {
    }


    /**
     * Constructs an <code>GetException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public GetException(String msg) {
        super(msg);
    }
}


