/*
 * MissingAttributeException.java
 *
 * Created on March 11, 2003, 10:50 AM
 */

package xal.tools.data;

/**
 *
 * @author  Christopher Allen
 */
public class MissingDataException extends DataFormatException {
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
    

    /**
     * Creates a new instance of <code>MissingAttributeException</code> without detail message.
     */
    public MissingDataException() {
    }
    
    
    /**
     * Constructs an instance of <code>MissingAttributeException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public MissingDataException(String msg) {
        super(msg);
    }
}
