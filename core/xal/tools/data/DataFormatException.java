/*
 * DataFormatException.java
 *
 * Created on February 27, 2003, 8:13 AM
 */

package xal.tools.data;

/**
 *
 * @author  Christopher Allen
 */
public class DataFormatException extends java.lang.RuntimeException {
    
    /** Serialization version */
    private static final long serialVersionUID = 1L;

    
    
    /**
     * Creates a new instance of <code>DataFormatException</code> without detail message.
     */
    public DataFormatException() {
    }
    
    
    /**
     * Constructs an instance of <code>DataFormatException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public DataFormatException(String msg) {
        super(msg);
    }
    
    /**
     * Create a new instance of <code>DataFormatException</code> with the given
     * error message and the given root cause for the exception.
     * 
     * @param msg   descriptive message for exception
     * @param exc   originating exception object (i.e., causing this exception)
     *
     * @author  Christopher K. Allen
     * @since   May 11, 2011
     */
    public DataFormatException(String msg, Exception exc) {
        super(msg, exc);
    }
}
