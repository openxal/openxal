/*
 * RemoteMessageException.java
 *
 * Created on Thu Oct 09 15:51:49 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.extension.service;


/**
 * RemoteMessageException wraps exeptions thrown during exectution of a remote message.
 * @author  tap
 */
public class RemoteMessageException extends RuntimeException {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
	public RemoteMessageException( final String message, final Throwable cause ) {
		super( message, cause );
	}
}

