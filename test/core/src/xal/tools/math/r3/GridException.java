/*
 * GridException.java
 *
 * Created on January 27, 2003, 2:04 PM
 */

package xal.tools.math.r3;

import xal.tools.math.MathException;

/**
 *
 * @author  Christopher Allen
 */
public class GridException extends MathException {
    
    /** Serialization  version number */
    private static final long serialVersionUID = 1L;



    /**
     * Creates a new instance of <code>GridException</code> without detail message.
     */
    public GridException() {
    }
    
    
    /**
     * Constructs an instance of <code>GridException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public GridException(String msg) {
        super(msg);
    }
}
