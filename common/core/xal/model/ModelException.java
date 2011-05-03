/*
 * ModelException.java
 *
 * Created on September 9, 2002, 5:14 PM
 */

package xal.model;

import xal.XalException;



/**
 * Base exception class for exceptions thrown by the
 * XAL online model.
 *
 * @author  Christopher K. Allen
 * @since   Sept 9, 2002
 */
public class ModelException extends XalException {
    
    
    /** Serialization version number */
    private static final long serialVersionUID = 1L;

    
    
    /** Creates a new instance of ModelException */
    public ModelException() {
        super();
    };
    
    /** Creates a new instance of ModelException with message */
    public ModelException(String strMsg) {
        super(strMsg);
    };
    
    /**
     * Create a new <code>ModelException</code> object which
     * is spawned (in principle) by the given exception object
     * and has the given explanation message.
     * 
     * @param strMsg    explanation message for the exception
     * @param excSrc    originating cause for the exception
     *
     * @author  Christopher K. Allen
     * @since   Apr 27, 2011
     */
    public ModelException(String strMsg, Exception excSrc) {
        super(strMsg, excSrc);
    }
}
