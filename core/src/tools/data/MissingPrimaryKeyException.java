/*
 * MissingPrimaryKeyException.java
 *
 * Created on September 26, 2002, 10:22 AM
 */

package xal.tools.data;

/**
 *
 * @author  tap
 */
public class MissingPrimaryKeyException extends RuntimeException {
    
    /** Creates a new instance of MissingPrimaryKeyException */
    public MissingPrimaryKeyException(String tableName) {
        super("The table: " + tableName + " has no primary key defined!  Every table must have at least one primary key defined.");
    }
    
}
