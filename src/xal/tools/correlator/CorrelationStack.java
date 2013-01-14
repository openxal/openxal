/*
 * CorrelationStack.java
 *
 * Created on May 27, 2003, 1:17 PM
 */

package xal.tools.correlator;

import xal.tools.messaging.MessageCenter;

import java.util.*;


/**
 * CorrelationStack is a correlator utility that uses a fixed sized, buffer to collect correlations
 * from a correlator.  An external object can then request all or the oldest 
 * correlations in which case the retrieved correlations are removed from the 
 * buffer.  Hence the CorrelationStack is a LILO stack.
 * CorrelationStack runs the correlator in "patient" mode (PatientBroadcaster), so the correlator posts
 * mutually exclusive correlations each of which has passed the filter tests.
 *
 * @author  tap
 */
public class CorrelationStack<RecordType> {
    /** buffer is a LILO stack of correlations with the oldest correlations having the smallest indices. */
    protected LinkedList<Correlation<RecordType>> buffer;
    protected int stackSize;
	protected Correlation<RecordType> lastCorrelation;
    
	/** Correlator */
    protected Correlator<?,RecordType,?> correlator;
	
    
    /** Creates a new instance of CorrelationStack */
    public CorrelationStack( final Correlator<?,RecordType,?> aCorrelator, final int aStackSize ) {
        stackSize = aStackSize;
        buffer = new LinkedList<Correlation<RecordType>>();
		lastCorrelation = null;
		
        correlator = aCorrelator;
		correlator.usePatientBroadcaster();
		correlator.addListener( new CorrelationNotice<RecordType>() {
			public void newCorrelation( final Object sender, final Correlation<RecordType> correlation ) {
				push( correlation );
			}
			
			public void noCorrelationCaught( Object sender ) {}
		});
    }
    
    
    /**
     * Get the number of correlations on the stack.
     * @return The number of correlations on the stack.
     */
    public int getCorrelationCount() {
        synchronized( buffer ) {
            return buffer.size();
        }
    }
    
    
    /**
     * Determines whether there are any correlations left on the stack.
     * @return true if there are no correlations on the stack and false otherwise.
     */
    public boolean isEmpty() {
        synchronized( buffer ) {
            return buffer.isEmpty();
        }
    }
	
	
	/**
	 * Empty all correlations from the buffer.
	 */
	public void clearBuffer() {
		synchronized( buffer ) {
			buffer.clear();
		}
	}
    
    
    /**
     * Removes the oldest correlation in the buffer and returns it.
     * @return The oldest correlation on the stack.
     * @throws java.util.NoSuchElementException if there are no correlations on the stack.
     */
    public Correlation<RecordType> popCorrelation() throws NoSuchElementException {
        synchronized( buffer ) {
            return buffer.removeFirst();
        }
    }
	
	
    /**
     * Pop all correlations from the stack.
     * @return all correlations on the stack.
     */
    public List<Correlation<RecordType>> popAllCorrelations() {
        synchronized(buffer) {
            final List<Correlation<RecordType>> correlations = new ArrayList<Correlation<RecordType>>( buffer );
            buffer.removeAll( correlations );
			
			return correlations;
        }
    }
    
    
    /**
     * Push a correlation onto the stack.
     * @param correlation The correlation to push onto the stack.
     */
    protected void push( final Correlation<RecordType> correlation ) {
        synchronized( buffer ) {
            buffer.addLast( correlation );
            trimBuffer();
        }
    }
    
    
    /**
     * Trim the buffer down to the stackSize by removing the oldest correlations.
     */
    protected void trimBuffer() {
        synchronized( buffer ) {
            while ( buffer.size() > stackSize ) {
                buffer.removeFirst();
            }
        }
    }
}
