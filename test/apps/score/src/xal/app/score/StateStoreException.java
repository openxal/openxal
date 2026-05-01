/*
 * StateStoreException.java
 *
 * Created on Thu Feb 05 14:32:21 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.score;


/**
 * StateStoreException wraps exceptions thrown by the state store.
 * @author  tap
 */
public class StateStoreException extends RuntimeException {
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
	/**
	 * Wrap state store exceptions
	 * @param message The description of the exception
	 * @param cause The cause of the exception
	 */
	public StateStoreException( String message, Throwable cause ) {
		super( message, cause );
	}
}

