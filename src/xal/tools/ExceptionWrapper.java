/*
 * ExceptionWrappe.java
 *
 * Created on March 14, 2002, 8:22 AM
 */

package xal.tools;

/**
 * Convenience class to wrap an exception with a runtime exception so 
 * exception handling isn't so irritating. This method was implemented before
 * Java supported exception causes.  It has been updated to use the new support and
 * this class remains for backward compatibility.
 *
 * @author  tap
 */
public class ExceptionWrapper extends RuntimeException {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
    
    /** Creates new ExceptionWrapper */
    public ExceptionWrapper(Exception cause) {
		super(cause);
    }
    
    
	/**
	 * Get the root cause
	 * @return the root cause
	 */
    public Exception rootException() {
        return (Exception)getCause();
    }
}
