package xal.ca;
/*
 * MonitorException.java
 *
 * Created on November 16, 2001, 4:24 PM
 */

/**
 *
 * @author  CKAllen
 */
public class MonitorException extends ChannelException {
    /** required for serializable objects */
    private static final long serialVersionUID = 1L;

    /**
     * Creates new <code>MonitorException</code> without detail message.
     */
    public MonitorException() {
    }


    /**
     * Constructs an <code>MonitorException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public MonitorException(String msg) {
        super(msg);
    }
}


