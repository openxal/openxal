package xal.ca;
/*
 * PutException.java
 *
 * Created on November 16, 2001, 4:14 PM
 */


/**
 *
 * @author  CKAllen
 */
public class PutException extends ChannelException {
    /** required for serializable objects */
    private static final long serialVersionUID = 1L;

    
    /**
     * Creates new <code>PutException</code> without detail message.
     */
    public PutException() {
    }


    /**
     * Constructs an <code>PutException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public PutException(String msg) {
        super(msg);
    }
}


