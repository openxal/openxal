/*
 * PropagationException.java
 *
 * Created on October 7, 2002, 10:46 PM
 */

package xal.model;

/**
 * Model exceptions specific to propagation a probe object down
 * an element lattice.
 * 
 * @author  Christopher Allen
 */
public class PropagationException extends ModelException {
    
    /** Serialization version */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a new instance of <code>PropagationException</code> without detail message.
     */
    public PropagationException() {
    }
    
    
    /**
     * Constructs an instance of <code>PropagationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public PropagationException(String msg) {
        super(msg);
    }
}
