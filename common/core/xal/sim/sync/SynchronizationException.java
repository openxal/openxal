/*
 * SynchronizationException.java
 *
 * Created on November 14, 2002, 3:55 PM
 */

package xal.sim.sync;

/**
 *
 * @author  CKAllen
 */
public class SynchronizationException extends xal.model.ModelException {
    
    /** Serialization version */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a new instance of <code>SynchronizationException</code> without detail message.
     */
    public SynchronizationException() {
    }
    
    
    /**
     * Constructs an instance of <code>SynchronizationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public SynchronizationException(String msg) {
        super(msg);
    }
}
