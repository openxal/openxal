/*
 * TraceSource.java
 *
 * Created on Fri Jun 11 16:39:28 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.orbitcorrect;

//unused
//import xal.tools.Lock;
import xal.tools.data.KeyedRecord;


/**
 * TraceSource is a source of traces.
 * @author  tap
 * @since Jun 11, 2004
 */
abstract public class TraceSource implements KeyedRecord {
	/** key indicating the trace source type */
	final static public String TYPE_KEY = "TYPE";
	
	/** key indicating the trace source label */
	final static public String LABEL_KEY = "LABEL";
	
	/** lock for synchronizing access from competing threads that want to access the source */
	//unused
    //protected Lock _busyLock;
	
	/** Determines if the source is enabled for display */
	protected boolean _enabled;
	
	/** Label for the trace */
	protected String _label;
	
	/** the type of the trace source */
	final protected String _type;
	
	
	/**
	 * Constructor
	 * @param label the label for the trace
	 * @param type the type of trace (e.g. X Avg)
	 */
	public TraceSource( final String label, final String type ) {
		_type = type;
		_label = label;
		_enabled = false;
		//_busyLock = new Lock();
	}
	
	
	/**
	 * Constructor
	 */
	public TraceSource() {
		this( "", "" );
	}
	
	
    //Unused lock functions
    
	/**
	 * Try to get a lock on the source so it can be queried in an non-busy state
	 * @return true if the lock is successful and false if not
	 */
//	public boolean tryLock() {
//		return _busyLock.tryLock();
//	}
	
	
	/**
	 * Unlock the source once we are done querying it
	 */
//	public void unlock() {
//		_busyLock.unlock();
//	}
	
	
	/**
	 * Get the label for the source
	 * @return a string which labels the source
	 */
	public String getLabel() {
		return _label;
	}
	
	
	/**
	 * Determine if the source is enabled for display
	 * @return true if the source is enabled for display and false if not
	 */
	public boolean isEnabled() {
		return _enabled;
	}
	
	
	/**
	 * Set whether the source is enabled for display
	 * @param enabled true to enable the source for display and false to disable it
	 */
	public void setEnabled( final boolean enabled ) {
		_enabled = enabled;
	}
	
	
	/**
	 * Get the latest available trace
	 * @return the latest trace available or null if none is available
	 */
	abstract public Trace getTrace();
	
	
	/**
	 * Get the value associated with the specified key.
	 * @param key The key for which to get the associated value.
	 * @return The value as an Object.
	 */
    public Object valueForKey( final String key ) {
		if ( key.equals( TYPE_KEY ) ) {
			return _type;
		}
		else if ( key.equals( LABEL_KEY ) ) {
			return _label;
		}
		else {
			return null;
		}
	}
}




