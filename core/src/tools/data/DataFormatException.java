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
}
