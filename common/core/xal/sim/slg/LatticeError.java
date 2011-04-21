/*
 * LatticeError.java
 *
 * Created on March 17, 2003, 1:00 PM
 */

package xal.sim.slg;

/**
 * @author  wdklotz
 */
public class LatticeError extends java.lang.Exception {   
    private String message="Lattice-Error";
    
    /**
     * Creates a new instance of <code>LatticeError</code> without detail message.
     */
    public LatticeError() {
    }
    
    
    /**
     * Constructs an instance of <code>LatticeError</code> with the specified detail message.
     * @param msg the detail message.
     */
    public LatticeError(String msg) {
        message+=": "+msg;
    }
    
    public String toString() {
        return message;
    }
    
    public String getMessage() {
        return toString();
    }
}
