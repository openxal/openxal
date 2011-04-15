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
}
