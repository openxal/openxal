package xal.tools.beam;

/**
 *
 * @author  Christopher Allen
 */
public class BeamException extends java.lang.Exception {
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    

    /**
     * Creates a new instance without detail message.
     */
    public BeamException() {
    }
    
    
    /**
     * Constructs an instance with the specified detail message.
     * @param msg the detail message.
     */
    public BeamException(String msg) {
        super(msg);
    }
}
