/*
 * MathException.java
 *
 * Created on January 27, 2003, 2:02 PM
 */

package xal.tools.math;

/**
 * Exception class for <code>xal.tools.math</code> package.
 *
 * @author  Christopher K. Allen
 * @since   Jan 27, 2003
 */
public class MathException extends java.lang.Exception {
    
    /**
     * Serialization version identifier
     */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a new instance of <code>MathException</code> without detail message.
     */
    public MathException() {
    }
    
    
    /**
     * Constructs an instance of <code>MathException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public MathException(String msg) {
        super(msg);
    }
}
