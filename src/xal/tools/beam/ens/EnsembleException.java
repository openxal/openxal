/*
 * EnsembleException.java
 *
 * Created on November 12, 2002, 7:21 PM
 */

package xal.tools.beam.ens;

/**
 * Exceptions related to multi-particle simulations, specifically, that involving 
 * the ensemble of particles being propagated.
 *
 * @author  CKAllen
 */
public class EnsembleException extends xal.model.ModelException {
    
    /** Serialization version */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a new instance of <code>EnsembleException</code> without detail message.
     */
    public EnsembleException() {
    }
    
    
    /**
     * Constructs an instance of <code>EnsembleException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public EnsembleException(String msg) {
        super(msg);
    }
}
