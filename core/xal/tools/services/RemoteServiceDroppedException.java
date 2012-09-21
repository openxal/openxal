/*
 * RemoteServiceDroppedException
 *
 * Created on Thu Sep 06 12:58:12 EDT 2012
 *
 * Copyright (c) 2012 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.tools.services;


/**
 * RemoteServiceDroppedException indicates that the remote session has dropped during a client request.
 * @author  tap
 */
public class RemoteServiceDroppedException extends RuntimeException {
    /** serialization ID */
    private static final long serialVersionUID = 1L;

	public RemoteServiceDroppedException( final String message ) {
		super( message );
	}
}

