package xal.smf;

public class ChannelAlreadyBoundException extends xal.ca.ChannelException {

    /**
     * Creates new <code>ChannelAlreadyBoundException</code> without detail message.
     */
    public ChannelAlreadyBoundException() {
    }


    /**
     * Constructs an <code>ChannelAlreadyBoundException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ChannelAlreadyBoundException(String msg) {
        super(msg);
    }
}

