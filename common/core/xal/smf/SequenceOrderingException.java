/*
 * SequenceOrderingException.java
 *
 * Created on November 11, 2002, 1:01 PM
 */

package xal.smf;

import java.util.Collection;
import java.util.Collections;

/**
 * SequenceOrderingException is thrown by the orderSequences() method in 
 * AcceleratorSeq when the supplied collection of sequences cannot be 
 * ordered back to back or a unique path cannot be found.
 *
 * @author  tap
 */
public class SequenceOrderingException extends RuntimeException {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
    Collection _sequences;
    
    /** Creates a new instance of SequenceOrderingException */
    public SequenceOrderingException(Collection sequences) {
        super();
        _sequences = Collections.unmodifiableCollection(sequences);
    }
    
    
    /** Override getMessage() to be meaningful. */
    public String getMessage() {
        return "Attempt to create an ordered sequence list from a collection of sequences " +
        "that cannot be linked due to the predecessor constraint.";
    }
    
    
    /** Get the sequences that were attempted to be ordered */
    public Collection getSequences() {
        return _sequences;
    }
}
