/*
 * GenerationException.java
 *
 * Created on October 2, 2002, 5:14 PM
 */

package xal.sim.latgen;

import xal.model.ModelException;

/**
 * Exception class thrown during the generation of a model lattice.
 *
 * @author  Christopher K. Allen
 * @since   Apr, 2011
 */
public class GenerationException extends ModelException {
    
    /** Serialization version */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a new instance of <code>GenerationException</code> without detail message.
     */
    public GenerationException() {
    }
    
    
    /**
     * Constructs an instance of <code>GenerationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public GenerationException(String msg) {
        super(msg);
    }
    
    /**
     * Create a new <code>GenerationException</code> object which
     * is spawned (in principle) by the given exception object
     * and has the given explanation message.
     * 
     * @param strMsg    explanation message for the exception
     * @param excSrc    originating cause for the exception
     *
     * @author  Christopher K. Allen
     * @since   Apr 27, 2011
     */
    public GenerationException(String strMsg, Exception excSrc) {
        super(strMsg, excSrc);
    }
}
