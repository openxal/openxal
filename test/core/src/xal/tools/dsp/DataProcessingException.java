/**
 * DataProcessingException.java
 * 
 * Created      : August, 2007
 * Author       : Christopher K. Allen
 */
package xal.tools.dsp;


/**
 * Exception representing an unrecoverable error in data processing.
 * 
 * @author Christopher K. Allen
 *
 */
public class DataProcessingException extends RuntimeException {

    /* 
     * Global Constants
     */
    
    /** Serialization version identifier */
    private static final long serialVersionUID = 1L;

    
    /*
     * Initialization
     */
    
    
    /**
     * Create a new exception with no detail message.
     */
    public DataProcessingException() {
        super();
    }

    /**
     * Create a new exception with a cause.  The cause object is saved for later 
     * retrieval by the <code>Throwable.getCause()</code> method. A null value is 
     * permitted, and indicates that the cause is nonexistent or unknown.

     * @param cause     cause of the exception
     * 
     * @see java.lang.Throwable#getCause()
     */
    public DataProcessingException(Throwable cause) {
        super(cause);
    }

    /**
     * Create a new exception with a detail message.
     * 
     * @param message   detail message
     */
    public DataProcessingException(String message) {
        super(message);
    }

    /**
     * Create a new exception with a detail message and cause.
     * 
     * @param message   detail message
     * @param cause     the cause object
     * 
     * @see DataProcessingException#DataProcessingException(Throwable)
     */
    public DataProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

}
